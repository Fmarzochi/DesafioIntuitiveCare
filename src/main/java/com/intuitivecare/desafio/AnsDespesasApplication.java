package com.intuitivecare.desafio;

import com.intuitivecare.desafio.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;

@SpringBootApplication
@EnableAsync
public class AnsDespesasApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnsDespesasApplication.class, args);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner run(AnsOperatorLoader operadoraLoader, DadosAgregadosLoader agregadosLoader, CsvDataProcessor csvProcessor) {
        return args -> {
            System.out.println("INICIANDO O SISTEMA EM MODO DE ALTA PERFORMANCE (COPY PROTOCOL)");

            // 1. Eu preparo o banco
            prepararBancoDeDados();

            // 2. Eu disparo o processamento
            new Thread(() -> {
                try {
                    executarCargaDeDados(operadoraLoader, agregadosLoader, csvProcessor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            System.out.println("SISTEMA PRONTO NA PORTA 8080 (O banco esta trabalhando em background)");
        };
    }

    private void prepararBancoDeDados() {
        // O comando COPY do Postgres e mais rapido se nao precisar converter numeros durante a leitura.
        // Eu faco a conversao numerica depois, via SQL, que e instantaneo.
        jdbcTemplate.execute("DROP TABLE IF EXISTS demonstracoes_contabeis");
        jdbcTemplate.execute("""
            CREATE UNLOGGED TABLE demonstracoes_contabeis (
                data_competencia VARCHAR(20),
                reg_ans VARCHAR(20),
                cd_conta_contabil VARCHAR(50),
                descricao TEXT,
                vl_saldo_inicial VARCHAR(50),
                vl_saldo_final VARCHAR(50)
            );
        """);
        // Indices sao criados DEPOIS da carga para nao frear a importacao
    }

    private void executarCargaDeDados(AnsOperatorLoader operadoraLoader, DadosAgregadosLoader agregadosLoader, CsvDataProcessor processor) throws Exception {
        System.out.println("Carregando Operadoras...");
        operadoraLoader.executar();

        AnsTrimestreFinder finder = new AnsTrimestreFinder();
        List<String> links = finder.encontrarLinksUltimosTrimestres(1);

        if (!links.isEmpty()) {
            AnsFileDownloader downloader = new AnsFileDownloader();
            downloader.baixarEExtrair(links);

            System.out.println("Iniciando ingestao via COPY Manager (Velocidade Maxima)...");

            // Aqui o processador usa o driver nativo do Postgres
            processor.processar();

            System.out.println("Criando indices para otimizar a busca...");
            jdbcTemplate.execute("CREATE INDEX idx_dc_descricao ON demonstracoes_contabeis(descricao);");
            jdbcTemplate.execute("CREATE INDEX idx_dc_reg_ans ON demonstracoes_contabeis(reg_ans);");

            System.out.println("Saneando dados de operadoras faltantes...");
            jdbcTemplate.update("""
                INSERT INTO operadoras (registro_ans, razao_social, cnpj, modalidade, uf)
                SELECT DISTINCT dc.reg_ans, 'Operadora Recuperada', '00000000000000', 'Desconhecida', 'BR'
                FROM demonstracoes_contabeis dc
                WHERE NOT EXISTS (SELECT 1 FROM operadoras op WHERE op.registro_ans = dc.reg_ans)
                AND dc.descricao ILIKE '%Eventos/Sinistros%'
            """);

            System.out.println("Consolidando e convertendo valores monetarios...");
            // Eu limpo a tabela de despesas para garantir dados novos
            jdbcTemplate.execute("DELETE FROM despesas");

            // Eu uso REPLACE para trocar virgula por ponto e CAST para numerico
            jdbcTemplate.execute("""
                INSERT INTO despesas (data_evento, descricao, valor, operadora_id)
                SELECT 
                    TO_DATE(data_competencia, 'YYYY-MM-DD'), 
                    descricao, 
                    CAST(REPLACE(vl_saldo_final, ',', '.') AS NUMERIC), 
                    reg_ans 
                FROM demonstracoes_contabeis 
                WHERE descricao ILIKE '%Eventos/Sinistros%' 
                AND CAST(REPLACE(vl_saldo_final, ',', '.') AS NUMERIC) > 0
            """);

            System.out.println("Calculando Dashboard...");
            jdbcTemplate.execute("DELETE FROM dados_agregados");
            jdbcTemplate.execute("""
                INSERT INTO dados_agregados (razao_social, total_despesas, uf, media_trimestral, qtd_registros)
                SELECT 
                    op.razao_social, 
                    SUM(d.valor), 
                    op.uf, 
                    AVG(d.valor), 
                    COUNT(d.id)
                FROM despesas d
                JOIN operadoras op ON d.operadora_id = op.registro_ans
                GROUP BY op.razao_social, op.uf
            """);
        }
        System.out.println("PROCESSO FINALIZADO");
    }
}