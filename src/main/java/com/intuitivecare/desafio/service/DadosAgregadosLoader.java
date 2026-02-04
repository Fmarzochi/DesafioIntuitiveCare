package com.intuitivecare.desafio.service;

import com.intuitivecare.desafio.model.DadosAgregados;
import com.intuitivecare.desafio.repository.DadosAgregadosRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class DadosAgregadosLoader {

    @Autowired
    private DadosAgregadosRepository repository;

    private static final String FILE_PATH = "data/despesas_agregadas.csv";

    public void executar() {
        if (!Files.exists(Paths.get(FILE_PATH))) {
            System.out.println("Arquivo 'despesas_agregadas.csv' não encontrado.");
            return;
        }

        try {
            carregarNoBanco();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarNoBanco() throws IOException {
        System.out.println("Importando dados agregados...");

        try (Reader reader = Files.newBufferedReader(Paths.get(FILE_PATH), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setDelimiter(';')
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            List<DadosAgregados> lote = new ArrayList<>();
            int count = 0;

            for (CSVRecord record : csvParser) {
                try {
                    DadosAgregados dado = new DadosAgregados();
                    dado.setRazaoSocial(record.get("Razao_Social"));
                    dado.setUf(record.get("UF"));

                    // AQUI ESTÁ A CORREÇÃO: trocamos vírgula por ponto antes de converter
                    dado.setTotalDespesas(parseDouble(record.get("Total_Despesas")));
                    dado.setMediaTrimestral(parseDouble(record.get("Media_Trimestral")));
                    dado.setDesvioPadrao(parseDouble(record.get("Desvio_Padrao")));

                    dado.setQtdRegistros(Integer.parseInt(record.get("Qtd_Registros")));

                    lote.add(dado);
                    count++;

                    if (lote.size() >= 500) {
                        repository.saveAll(lote);
                        lote.clear();
                    }
                } catch (Exception e) {
                    // Mostra o erro mas continua tentando as próximas linhas
                    System.err.println("Erro linha " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }

            if (!lote.isEmpty()) {
                repository.saveAll(lote);
            }
            System.out.println("Sucesso! " + count + " registros agregados importados.");
        }
    }

    // Método auxiliar corrigido para aceitar vírgula
    private Double parseDouble(String value) {
        if (value == null || value.isEmpty() || value.trim().equalsIgnoreCase("nan")) {
            return 0.0;
        }
        // Troca vírgula por ponto para o Java entender
        String cleanValue = value.replace(",", ".");
        return Double.parseDouble(cleanValue);
    }
}