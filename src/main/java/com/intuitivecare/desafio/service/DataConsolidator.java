package com.intuitivecare.desafio.service;

import com.intuitivecare.desafio.config.DatabaseConfig;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataConsolidator {

    private static final String OUTPUT_FILE = "data/consolidado_despesas.csv";

    // Query Otimizada: Realiza o JOIN e ja traz os dados de enriquecimento (Item 2.2 do desafio)
    // Isso evita processamento custoso em memoria depois via Java.
    private static final String SQL_RELATORIO = """
        SELECT 
            op.registro_ans,
            op.cnpj, 
            op.razao_social, 
            op.modalidade,
            op.uf,
            EXTRACT(QUARTER FROM dc.data_competencia) as trimestre, 
            EXTRACT(YEAR FROM dc.data_competencia) as ano, 
            dc.vl_saldo_final as valor_despesas
        FROM demonstracoes_contabeis dc
        INNER JOIN operadoras op ON dc.reg_ans = op.registro_ans
        WHERE dc.descricao ILIKE '%Eventos/Sinistros%' 
           OR dc.descricao ILIKE '%Despesas Assistenciais%'
        ORDER BY op.razao_social, dc.data_competencia;
    """;

    public void gerarRelatorio() {
        System.out.println("Iniciando consolidacao e enriquecimento de dados (SQL Join)...");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_RELATORIO);
             ResultSet rs = stmt.executeQuery();
             FileWriter writer = new FileWriter(OUTPUT_FILE, StandardCharsets.UTF_8);
             // Configurando o cabecalho completo exigido no Item 2.2
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .builder()
                     .setHeader("Registro_ANS", "CNPJ", "Razao_Social", "Modalidade", "UF", "Trimestre", "Ano", "Valor_Despesas")
                     .setDelimiter(';')
                     .build())) {

            int count = 0;
            while (rs.next()) {
                csvPrinter.printRecord(
                        rs.getInt("registro_ans"),
                        rs.getString("cnpj"),
                        rs.getString("razao_social"),
                        rs.getString("modalidade"),
                        rs.getString("uf"),
                        rs.getInt("trimestre"),
                        rs.getInt("ano"),
                        rs.getBigDecimal("valor_despesas")
                );
                count++;
                if (count % 20000 == 0) System.out.println("   Processados " + count + " registros...");
            }

            System.out.println("--------------------------------------------------");
            System.out.println("Relatorio enriquecido gerado: " + OUTPUT_FILE);
            System.out.println("Total de linhas: " + count);
            System.out.println("--------------------------------------------------");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new DataConsolidator().gerarRelatorio();
    }
}