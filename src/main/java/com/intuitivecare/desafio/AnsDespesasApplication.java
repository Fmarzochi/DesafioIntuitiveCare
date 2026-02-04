package com.intuitivecare.desafio;

import com.intuitivecare.desafio.service.AnsOperatorLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AnsDespesasApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnsDespesasApplication.class, args);
    }

    // Esse método roda automaticamente assim que o servidor liga
    @Bean
    public CommandLineRunner run(AnsOperatorLoader loader) {
        return args -> {
            System.out.println("--- INICIANDO CARGA DE DADOS ---");
            loader.executar();
            System.out.println("--- CARGA FINALIZADA ---");
        };
    }
}