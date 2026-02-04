package com.intuitivecare.desafio;

import com.intuitivecare.desafio.service.AnsOperatorLoader;
import com.intuitivecare.desafio.service.DadosAgregadosLoader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AnsDespesasApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnsDespesasApplication.class, args);
    }

    //  Spring injeta os dois carregadores
    @Bean
    public CommandLineRunner run(AnsOperatorLoader operadoraLoader, DadosAgregadosLoader agregadosLoader) {
        return args -> {
            System.out.println("=== INICIANDO SISTEMA ===");

            // 1. Carrega as Operadoras
            operadoraLoader.executar();

            // 2. Carrega os Dados Agregados do CSV 
            agregadosLoader.executar();

            System.out.println("=== SISTEMA PRONTO NA PORTA 8080 ===");
        };
    }
}