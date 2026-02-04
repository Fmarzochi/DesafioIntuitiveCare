package com.intuitivecare.desafio.service;

import com.intuitivecare.desafio.config.DatabaseConfig;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvDataProcessor {

    private static final String DIR_EXTRACTED = "data/extracted";
    private static final int BATCH_SIZE = 1000; // Insere a cada 1000 linhas para performance

    public void processar() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false); // Desativa commit automatico para usar Batch

            List<Path> arquivosCsv = listarArquivosCsv();
            if (arquivosCsv.isEmpty()) {
                System.out.println("Nenhum arquivo CSV encontrado.");
                return;
            }

            for (Path path : arquivosCsv) {
                processarArquivoBanco(conn, path);
            }

            System.out.println("Processamento finalizado com sucesso.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Path> listarArquivosCsv() throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(DIR_EXTRACTED))) {
            return walk
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.toString().toLowerCase().endsWith(".csv"))
                    .collect(Collectors.toList());
        }
    }

    private void processarArquivoBanco(Connection conn, Path path) {
        System.out.println("Iniciando importacao do arquivo: " + path.getFileName());

        String sql = "INSERT INTO demonstracoes_contabeis " +
                "(data_competencia, reg_ans, cd_conta_contabil, descricao, vl_saldo_inicial, vl_saldo_final) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        // Importante: A ANS usa ISO-8859-1 (Latin1), mas as vezes mistura.
        // Se os acentos ficarem errados no banco, trocaremos para StandardCharsets.ISO_8859_1
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setDelimiter(';')
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build());
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int count = 0;

            for (CSVRecord record : csvParser) {
                try {
                    // 1. DATA (Formato esperado YYYY-MM-DD no CSV da ANS recente)
                    stmt.setDate(1, Date.valueOf(record.get("DATA")));

                    // 2. REG_ANS
                    stmt.setInt(2, Integer.parseInt(record.get("REG_ANS")));

                    // 3. CD_CONTA_CONTABIL
                    stmt.setLong(3, Long.parseLong(record.get("CD_CONTA_CONTABIL")));

                    // 4. DESCRICAO
                    stmt.setString(4, record.get("DESCRICAO"));

                    // 5. SALDO INICIAL (Troca virgula por ponto para BigDecimal)
                    String saldoInicial = record.get("VL_SALDO_INICIAL").replace(",", ".");
                    stmt.setBigDecimal(5, new BigDecimal(saldoInicial));

                    // 6. SALDO FINAL
                    String saldoFinal = record.get("VL_SALDO_FINAL").replace(",", ".");
                    stmt.setBigDecimal(6, new BigDecimal(saldoFinal));

                    stmt.addBatch();
                    count++;

                    if (count % BATCH_SIZE == 0) {
                        stmt.executeBatch();
                        conn.commit();
                        System.out.println("   Processadas " + count + " linhas...");
                    }

                } catch (Exception e) {
                    System.err.println("Erro na linha " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }

            // Executa o restante que sobrou
            stmt.executeBatch();
            conn.commit();
            System.out.println("Arquivo finalizado. Total de linhas: " + count);

        } catch (IOException | SQLException e) {
            System.err.println("Falha fatal no arquivo " + path + ": " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new CsvDataProcessor().processar();
    }
}