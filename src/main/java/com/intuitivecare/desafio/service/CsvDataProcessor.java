package com.intuitivecare.desafio.service;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Stream;

@Service
public class CsvDataProcessor {

    private final DataSource dataSource;

    // Eu preciso do DataSource para pegar a conexao nativa do Postgres
    public CsvDataProcessor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void processar() {
        String diretorioDestino = "data/extracted";

        try (Stream<Path> paths = Files.walk(Paths.get(diretorioDestino))) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .forEach(this::copiarArquivoDiretoParaBanco);
        } catch (IOException e) {
            System.err.println("Erro ao listar arquivos: " + e.getMessage());
        }
    }

    private void copiarArquivoDiretoParaBanco(Path caminhoArquivo) {
        System.out.println("Iniciando COPY do arquivo (Alta Velocidade): " + caminhoArquivo.getFileName());

        // Eu uso o comando COPY do Postgres. Ele espera CSV com ponto e virgula e aspas.
        // O encoding LATIN1 (ISO-8859-1) resolve o problema dos acentos automaticamente.
        String sql = "COPY demonstracoes_contabeis (data_competencia, reg_ans, cd_conta_contabil, descricao, vl_saldo_inicial, vl_saldo_final) " +
                "FROM STDIN WITH (FORMAT CSV, DELIMITER ';', HEADER, QUOTE '\"', ENCODING 'LATIN1')";

        try (Connection conn = dataSource.getConnection();
             FileInputStream fileInputStream = new FileInputStream(caminhoArquivo.toFile())) {

            // Eu preciso desempacotar a conexao para acessar a API especifica do Postgres
            if (conn.isWrapperFor(BaseConnection.class)) {
                BaseConnection pgConnection = conn.unwrap(BaseConnection.class);
                CopyManager copyManager = new CopyManager(pgConnection);

                // Esta linha faz todo o trabalho pesado em segundos
                long rowsUpdated = copyManager.copyIn(sql, fileInputStream);

                System.out.println("Arquivo importado com sucesso. Total de linhas inseridas: " + rowsUpdated);
            } else {
                System.err.println("Erro: A conexao nao e nativa do PostgreSQL. Nao posso usar o modo Turbo.");
            }

        } catch (SQLException | IOException e) {
            System.err.println("Erro critico na importacao via COPY: " + e.getMessage());
            e.printStackTrace();
        }
    }
}