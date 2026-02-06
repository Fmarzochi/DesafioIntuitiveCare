package com.intuitivecare.desafio.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CsvDataProcessor {

    private final JdbcTemplate jdbcTemplate;

    // Usamos JdbcTemplate para comandos SQL diretos
    public CsvDataProcessor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void processar() {
        String diretorioDestino = "data/extracted"; // Confirme se o caminho é esse mesmo na sua pasta

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
        System.out.println("Processando e Padronizando arquivo: " + caminhoArquivo.getFileName());

        String sqlInsert = "INSERT INTO demonstracoes_contabeis (data_competencia, reg_ans, cd_conta_contabil, descricao, vl_saldo_inicial, vl_saldo_final) VALUES (?, ?, ?, ?, ?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();

        // Configuração do CSV: Separador ponto-e-virgula, com cabeçalho, encoding ISO-8859-1 (Latin1) para acentos
        try (BufferedReader reader = Files.newBufferedReader(caminhoArquivo, StandardCharsets.ISO_8859_1);
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
                // 1. Ler os dados originais
                String dataCompetencia = record.get("DATA"); // Verifique se o nome da coluna no CSV é "DATA" ou "data_competencia"
                String regAns = record.get("REG_ANS");
                String cdConta = record.get("CD_CONTA_CONTABIL");
                String descricaoOriginal = record.get("DESCRICAO");
                String saldoInicial = record.get("VL_SALDO_INICIAL").replace(",", "."); // Troca vírgula por ponto para o Banco
                String saldoFinal = record.get("VL_SALDO_FINAL").replace(",", ".");

                // 2. APLICAR A PADRONIZAÇÃO (A mágica acontece aqui)
                String descricaoTratada = padronizarTexto(descricaoOriginal);

                // 3. Adicionar ao lote de gravação
                batchArgs.add(new Object[]{
                        dataCompetencia,
                        regAns,
                        cdConta,
                        descricaoTratada, // Grava a descrição bonita
                        Double.parseDouble(saldoInicial),
                        Double.parseDouble(saldoFinal)
                });

                // A cada 1000 linhas, grava no banco para não pesar a memória
                if (batchArgs.size() >= 1000) {
                    jdbcTemplate.batchUpdate(sqlInsert, batchArgs);
                    batchArgs.clear();
                }
            }

            // Grava o restante final
            if (!batchArgs.isEmpty()) {
                jdbcTemplate.batchUpdate(sqlInsert, batchArgs);
            }

            System.out.println("Arquivo finalizado com padronização de texto.");

        } catch (Exception e) {
            System.err.println("Erro ao processar arquivo " + caminhoArquivo + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Transforma texto MAIÚSCULO em Capitalizado.
     * Ex: "EVENTOS CONHECIDOS" -> "Eventos Conhecidos"
     */
    private String padronizarTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }

        String[] palavras = texto.toLowerCase().split(" ");
        StringBuilder resultado = new StringBuilder();

        for (String palavra : palavras) {
            if (palavra.length() > 0) {
                // Regra: Se a palavra tiver mais de 2 letras, coloca a primeira maiúscula.
                // Palavras curtas (de, da, e) ficam minúsculas.
                if (palavra.length() > 2) {
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