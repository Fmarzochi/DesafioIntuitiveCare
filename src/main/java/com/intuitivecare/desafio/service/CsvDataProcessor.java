package com.intuitivecare.desafio.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets; // Importante para UTF-8
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CsvDataProcessor {

    private final JdbcTemplate jdbcTemplate;

    public CsvDataProcessor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void processar() {
        // 1. Limpeza para garantir que não duplique nem mantenha dados errados
        System.out.println("Limpando dados antigos da tabela demonstracoes_contabeis...");
        try {
            // "TRUNCATE" é mais rápido que "DELETE FROM" e zera o ID
            jdbcTemplate.execute("TRUNCATE TABLE demonstracoes_contabeis RESTART IDENTITY CASCADE");
        } catch (Exception e) {
            System.out.println("Aviso na limpeza (pode ser a primeira execução): " + e.getMessage());
        }

        String diretorioDestino = "data/extracted";

        try (Stream<Path> paths = Files.walk(Paths.get(diretorioDestino))) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .forEach(this::processarArquivoLinhaPorLinha);
        } catch (IOException e) {
            System.err.println("Erro ao listar arquivos: " + e.getMessage());
        }
    }

    @Transactional
    protected void processarArquivoLinhaPorLinha(Path caminhoArquivo) {
        System.out.println("Processando arquivo: " + caminhoArquivo.getFileName());

        String sqlInsert = "INSERT INTO demonstracoes_contabeis (data_competencia, reg_ans, cd_conta_contabil, descricao, vl_saldo_inicial, vl_saldo_final) VALUES (?, ?, ?, ?, ?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();

        // Mudamos para UTF_8. Se o arquivo original for UTF-8, isso corrige
        // tanto a leitura (crash) quanto a acentuação ("Assistência").
        try (BufferedReader reader = Files.newBufferedReader(caminhoArquivo, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setDelimiter(';')
                     .setQuote('"')
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build())) {

            for (CSVRecord record : csvParser) {
                // Tenta pegar a coluna DATA ou data_competencia
                String dataCompetencia = record.isMapped("DATA") ? record.get("DATA") : record.get(0);
                String regAns = record.get("REG_ANS");
                String cdConta = record.get("CD_CONTA_CONTABIL");
                String descricaoOriginal = record.get("DESCRICAO");

                // Tratamento de valores numéricos (Vírgula para Ponto)
                String saldoInicial = record.get("VL_SALDO_INICIAL").replace(",", ".");
                String saldoFinal = record.get("VL_SALDO_FINAL").replace(",", ".");

                // Padronização do Texto (Capitalize)
                String descricaoTratada = padronizarTexto(descricaoOriginal);

                batchArgs.add(new Object[]{
                        dataCompetencia,
                        regAns,
                        cdConta,
                        descricaoTratada,
                        Double.parseDouble(saldoInicial),
                        Double.parseDouble(saldoFinal)
                });

                // Grava em lotes de 2000 para ser rápido
                if (batchArgs.size() >= 2000) {
                    jdbcTemplate.batchUpdate(sqlInsert, batchArgs);
                    batchArgs.clear();
                }
            }

            // Grava o resto
            if (!batchArgs.isEmpty()) {
                jdbcTemplate.batchUpdate(sqlInsert, batchArgs);
            }

            System.out.println("Arquivo " + caminhoArquivo.getFileName() + " importado com sucesso (UTF-8).");

        } catch (Exception e) {
            System.err.println("Erro crítico ao processar " + caminhoArquivo + ": " + e.getMessage());
            // Se der erro de UTF-8 (MalformedInput), avisa para tentarmos ISO-8859-1
            if (e.getClass().getName().contains("MalformedInput")) {
                System.err.println("DICA: O arquivo não é UTF-8. Tente usar StandardCharsets.ISO_8859_1 no código.");
            }
        }
    }

    private String padronizarTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }

        String[] palavras = texto.toLowerCase().split(" ");
        StringBuilder resultado = new StringBuilder();

        for (String palavra : palavras) {
            if (palavra.length() > 0) {
                // Regra: Maiúscula se > 2 letras e não contiver caracteres especiais estranhos
                if (palavra.length() > 2 && !palavra.contains("/")) {
                    resultado.append(Character.toUpperCase(palavra.charAt(0)))
                            .append(palavra.substring(1));
                } else {
                    resultado.append(palavra);
                }
                resultado.append(" ");
            }
        }
        return resultado.toString().trim();
    }
}