package com.intuitivecare.desafio.service;

import com.intuitivecare.desafio.model.Operadora;
import com.intuitivecare.desafio.repository.OperadoraRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnsOperatorLoader {

    @Autowired
    private OperadoraRepository operadoraRepository;

    private static final String URL_CADASTRO = "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/Relatorio_cadop.csv";
    private static final String FILE_PATH = "data/operadoras.csv";

    public void executar() {
        try {
            // Garante que a pasta data existe
            new File("data").mkdirs();

            baixarArquivo();
            carregarNoBanco();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void baixarArquivo() throws IOException {
        System.out.println("Baixando dados cadastrais das operadoras...");
        File destino = new File(FILE_PATH);

        // Se o arquivo já existe e é recente, não baixa de novo (opcional, ajuda nos testes)
        // if (destino.exists()) { System.out.println("Arquivo já existe."); return; }

        URL url = new URL(URL_CADASTRO);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);

        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, Paths.get(FILE_PATH), StandardCopyOption.REPLACE_EXISTING);
        }

        System.out.println("Download concluído: " + FILE_PATH);
    }

    private void carregarNoBanco() throws IOException {
        System.out.println("Lendo CSV e salvando no banco via JPA...");

        try (Reader reader = Files.newBufferedReader(Paths.get(FILE_PATH), Charset.forName("ISO-8859-1"));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setDelimiter(';')
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            List<Operadora> loteOperadoras = new ArrayList<>();
            int count = 0;

            for (CSVRecord record : csvParser) {
                try {
                    Operadora op = new Operadora();

                    // Converte os dados do CSV para o Objeto Java
                    op.setRegistroAns(record.get("REGISTRO_OPERADORA")); // ID
                    op.setCnpj(record.get("CNPJ"));
                    op.setRazaoSocial(record.get("Razao_Social"));
                    op.setModalidade(record.get("Modalidade"));
                    op.setUf(record.get("UF"));

                    loteOperadoras.add(op);
                    count++;

                    // Salva em lotes de 1000 para ser mais rápido
                    if (loteOperadoras.size() >= 1000) {
                        operadoraRepository.saveAll(loteOperadoras);
                        loteOperadoras.clear();
                        System.out.println("Processadas " + count + " operadoras...");
                    }

                } catch (Exception e) {
                    if (record.size() > 1) {
                        System.err.println("Erro na linha " + record.getRecordNumber() + ": " + e.getMessage());
                    }
                }
            }

            // Salva o restante que sobrou na lista
            if (!loteOperadoras.isEmpty()) {
                operadoraRepository.saveAll(loteOperadoras);
            }

            System.out.println("Sucesso! Total de operadoras importadas: " + count);
        }
    }
}