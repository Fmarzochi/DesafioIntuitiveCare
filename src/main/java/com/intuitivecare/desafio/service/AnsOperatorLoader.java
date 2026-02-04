package com.intuitivecare.desafio.service;

import com.intuitivecare.desafio.config.DatabaseConfig;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class AnsOperatorLoader {

    private static final String URL_CADASTRO = "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/Relatorio_cadop.csv";
    private static final String FILE_PATH = "data/operadoras.csv";

    public void executar() {
        try {
            baixarArquivo();
            criarTabelaOperadoras();
            carregarNoBanco();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void baixarArquivo() throws IOException {
        System.out.println("Baixando dados cadastrais das operadoras...");
        File destino = new File(FILE_PATH);

        URL url = new URL(URL_CADASTRO);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);

        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, Paths.get(FILE_PATH), StandardCopyOption.REPLACE_EXISTING);
        }

        System.out.println("Download concluido: " + FILE_PATH);
        System.out.println("Tamanho do arquivo: " + destino.length() + " bytes");
    }

    private void criarTabelaOperadoras() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS operadoras (" +
                "   registro_ans INT PRIMARY KEY," +
                "   cnpj VARCHAR(30)," +
                "   razao_social VARCHAR(500)," +
                "   modalidade VARCHAR(200)," +
                "   uf VARCHAR(5)" +
                ");";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'operadoras' verificada/criada.");
        }
    }

    private void carregarNoBanco() throws IOException, SQLException {
        System.out.println("Iniciando leitura do CSV...");

        String insertSql = "INSERT INTO operadoras (registro_ans, cnpj, razao_social, modalidade, uf) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (registro_ans) DO NOTHING";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql);
             Reader reader = Files.newBufferedReader(Paths.get(FILE_PATH), Charset.forName("ISO-8859-1"));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setDelimiter(';')
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            conn.setAutoCommit(false);

            // Debug: Verificando colunas
            System.out.println("Colunas detectadas: " + csvParser.getHeaderNames());

            int count = 0;
            for (CSVRecord record : csvParser) {
                try {
                    // --- CORREÇÃO AQUI ---
                    // Trocamos "Registro_ANS" por "REGISTRO_OPERADORA" conforme o log de erro
                    stmt.setInt(1, Integer.parseInt(record.get("REGISTRO_OPERADORA")));
                    stmt.setString(2, record.get("CNPJ"));
                    stmt.setString(3, record.get("Razao_Social"));
                    stmt.setString(4, record.get("Modalidade"));
                    stmt.setString(5, record.get("UF"));

                    stmt.addBatch();
                    count++;

                    if (count % 500 == 0) {
                        stmt.executeBatch();
                        conn.commit();
                        System.out.println("   Importadas " + count + " operadoras...");
                    }
                } catch (Exception e) {
                    // Log reduzido para não poluir o console se houver linhas em branco no final
                    if(record.size() > 1) {
                        System.err.println("Aviso: Falha na linha " + record.getRecordNumber() + ": " + e.getMessage());
                    }
                }
            }
            stmt.executeBatch();
            conn.commit();
            System.out.println("Sucesso! Total de operadoras ativas importadas: " + count);
        }
    }

    public static void main(String[] args) {
        new AnsOperatorLoader().executar();
    }
}