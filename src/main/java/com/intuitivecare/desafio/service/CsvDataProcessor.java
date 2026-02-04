package com.intuitivecare.desafio.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvDataProcessor {

    private static final String DIR_EXTRACTED = "data/extracted";

    public void processar() throws IOException {
        List<Path> arquivosCsv = listarArquivosCsv();

        if (arquivosCsv.isEmpty()) {
            System.out.println("Nenhum arquivo CSV encontrado em " + DIR_EXTRACTED);
            return;
        }

        for (Path path : arquivosCsv) {
            lerArquivo(path);
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

    private void lerArquivo(Path path) {
        System.out.println("Processando: " + path.getFileName());

        try (Reader reader = Files.newBufferedReader(path, Charset.forName("ISO-8859-1"));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setDelimiter(';')
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            System.out.println("Colunas: " + csvParser.getHeaderNames());

            int count = 0;
            for (CSVRecord record : csvParser) {
                if (count >= 3) break;
                System.out.println("Registro " + (count + 1) + ": " + record.toString());
                count++;
            }
            System.out.println("--------------------------------------------------");

        } catch (IOException e) {
            System.err.println("Falha ao processar " + path + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            new CsvDataProcessor().processar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}