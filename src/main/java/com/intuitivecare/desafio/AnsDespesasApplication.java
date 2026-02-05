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
@EnableAsync // Aqui eu habilitei o processamento paralelo para não travar o frontend
public class AnsDespesasApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnsDespesasApplication.class, args);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner run(AnsOperatorLoader operadoraLoader, DadosAgregadosLoader agregadosLoader) {
        return args -> {
            System.out.println("=== INICIANDO O SERVIDOR EM MODO ASSINCRONO ===");

            // 1. Eu preparo o banco de dados antes de iniciar o processamento pesado
            prepararBancoDeDados();

            // 2. Eu disparo o processamento em uma thread separada para liberar a porta 8080 imediatamente
            new Thread(() -> {
                try {
                    executarCargaDeDados(operadoraLoader, agregadosLoader);
                } catch (Exception e) {
                    System.err.println("Erro fatal no meu processamento de dados: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();

            // O sistema libera o acesso ao Frontend instantaneamente aqui
            System.out.println("=== SISTEMA PRONTO NA PORTA 8080 (Meus dados estao sendo processados em background...) ===");
        };
    }

    private void prepararBancoDeDados() {
        // Eu crio a tabela como UNLOGGED para aumentar drasticamente a velocidade de inserção
        // Isso evita que o Postgres perca tempo gravando logs de transação para dados temporários
        jdbcTemplate.execute("""
            CREATE UNLOGGED TABLE IF NOT EXISTS demonstracoes_contabeis (
                id BIGSERIAL PRIMARY KEY,
                data_competencia DATE,
                reg_ans VARCHAR(20),
                cd_conta_contabil VARCHAR(50),
                descricao TEXT,
                vl_saldo_inicial NUMERIC(20,2),
                vl_saldo_final NUMERIC(20,2)
            );
        """);

        // Eu crio índices para garantir que minhas consultas de saneamento sejam rápidas
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_dc_descricao ON demonstracoes_contabeis(descricao);");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_dc_reg_ans ON demonstracoes_contabeis(reg_ans);");
    }

    // Toda a minha lógica pesada fica isolada aqui para não impactar a performance da API
    private void executarCargaDeDados(AnsOperatorLoader operadoraLoader, DadosAgregadosLoader agregadosLoader) throws Exception {
        System.out.println(">>> Iniciando carga inicial de Operadoras...");
        operadoraLoader.executar();

        System.out.println(">>> Verificando arquivos ZIP na ANS...");
        AnsTrimestreFinder finder = new AnsTrimestreFinder();
        // Eu busco apenas o último trimestre para otimizar o tempo de teste
        List<String> links = finder.encontrarLinksUltimosTrimestres(1);

        if (!links.isEmpty()) {
            AnsFileDownloader downloader = new AnsFileDownloader();
            downloader.baixarEExtrair(links);

            System.out.println(">>> Processando o CSV gigante para o banco (Isso pode levar alguns minutos)...");
            CsvDataProcessor processor = new CsvDataProcessor();
            processor.processar();

            // PASSO DE SANEAMENTO
            // Aqui eu identifico operadoras que existem nas despesas mas não no cadastro
            // Eu as insiro automaticamente para evitar erros de chave estrangeira
            System.out.println(">>> Executando saneamento de dados para integridade referencial...");
            String sqlSanitize = """
                INSERT INTO operadoras (registro_ans, razao_social, cnpj, modalidade, uf)
                SELECT DISTINCT dc.reg_ans, 'Operadora Historica (Recuperada)', '00000000000000', 'Desconhecida', 'BR'
                FROM demonstracoes_contabeis dc
                WHERE NOT EXISTS (
                    SELECT 1 FROM operadoras op WHERE op.registro_ans = CAST(dc.reg_ans AS VARCHAR)
                )
                AND dc.descricao ILIKE '%Eventos/Sinistros%';
            """;
            int recuperados = jdbcTemplate.update(sqlSanitize);
            System.out.println("Saneamento concluido: " + recuperados + " operadoras recuperadas e cadastradas.");

            // PASSO FINAL (LOTE SQL)
            // Eu uso SQL direto para mover milhões de registros de uma vez só
            // Isso é muito mais performático do que processar linha a linha no Java
            System.out.println(">>> Transferindo e consolidando despesas para a API...");
            String sqlBatch = """
                INSERT INTO despesas (data_evento, descricao, valor, operadora_id)
                SELECT 
                    data_competencia, 
                    descricao, 
                    vl_saldo_final, 
                    CAST(reg_ans AS VARCHAR) 
                FROM demonstracoes_contabeis 
                WHERE descricao ILIKE '%Eventos/Sinistros%' 
                AND vl_saldo_final > 0
                AND NOT EXISTS (SELECT 1 FROM despesas LIMIT 1)
            """;
            jdbcTemplate.execute(sqlBatch);
            System.out.println("Tabela de despesas sincronizada e pronta para uso!");

        } else {
            System.out.println("AVISO: Nao encontrei novos arquivos ZIP para baixar.");
        }

        System.out.println(">>> Atualizando estatisticas do Dashboard...");
        agregadosLoader.executar();
        System.out.println("=== PROCESSAMENTO DE DADOS FOI FINALIZADO ===");
    }
}