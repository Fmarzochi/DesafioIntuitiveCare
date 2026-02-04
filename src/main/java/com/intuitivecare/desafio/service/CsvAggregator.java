package com.intuitivecare.desafio.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.FileWriter;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CsvAggregator {

    private static final String INPUT_FILE = "data/consolidado_despesas.csv";
    private static final String OUTPUT_FILE = "data/despesas_agregadas.csv";

    public void processar() {
        System.out.println("Iniciando agregacao e calculo estatistico...");

        try (Reader reader = Files.newBufferedReader(Paths.get(INPUT_FILE), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder().setDelimiter(';').setHeader().setSkipHeaderRecord(true).build())) {

            // Mapa para agrupar: Chave (Razao Social + UF) -> Lista de Valores (Despesas)
            Map<String, List<BigDecimal>> agrupamento = new HashMap<>();

            for (CSVRecord record : csvParser) {
                // Item 2.1: Validacao simples (ignora valores zerados/negativos)
                String valorStr = record.get("Valor_Despesas");
                BigDecimal valor = new BigDecimal(valorStr);

                if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                String razao = record.get("Razao_Social");
                String uf = record.get("UF");
                String chave = razao + ";" + uf; // Chave composta para agrupar

                agrupamento.computeIfAbsent(chave, k -> new ArrayList<>()).add(valor);
            }

            System.out.println("Dados agrupados. Calculando estatisticas...");
            escreverResultado(agrupamento);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void escreverResultado(Map<String, List<BigDecimal>> dados) throws Exception {
        try (FileWriter writer = new FileWriter(OUTPUT_FILE, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .builder()
                     .setHeader("Razao_Social", "UF", "Total_Despesas", "Media_Trimestral", "Desvio_Padrao", "Qtd_Registros")
                     .setDelimiter(';')
                     .build())) {

            // Item 2.3: Ordenar por Valor Total (Maior para Menor)
            List<Map.Entry<String, List<BigDecimal>>> listaOrdenada = dados.entrySet().stream()
                    .sorted((e1, e2) -> {
                        BigDecimal total1 = e1.getValue().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal total2 = e2.getValue().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                        return total2.compareTo(total1); // Decrescente (Total2 > Total1)
                    })
                    .collect(Collectors.toList());

            int count = 0;
            for (Map.Entry<String, List<BigDecimal>> entry : listaOrdenada) {
                String[] chaves = entry.getKey().split(";");
                String razao = chaves[0];
                String uf = chaves[1];
                List<BigDecimal> valores = entry.getValue();

                // Calculos Matematicos
                BigDecimal total = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal media = total.divide(new BigDecimal(valores.size()), MathContext.DECIMAL32);
                double desvioPadrao = calcularDesvioPadrao(valores, media);

                // Gravacao no CSV
                csvPrinter.printRecord(
                        razao,
                        uf,
                        total.setScale(2, RoundingMode.HALF_UP),
                        media.setScale(2, RoundingMode.HALF_UP),
                        String.format("%.2f", desvioPadrao),
                        valores.size()
                );
                count++;
            }

            System.out.println("--------------------------------------------------");
            System.out.println("Arquivo Final Gerado: " + OUTPUT_FILE);
            System.out.println("Total de linhas agregadas: " + count);
            System.out.println("--------------------------------------------------");
        }
    }

    private double calcularDesvioPadrao(List<BigDecimal> valores, BigDecimal media) {
        if (valores.size() <= 1) return 0.0;
        double sumSquaredDiff = 0.0;
        for (BigDecimal val : valores) {
            double diff = val.doubleValue() - media.doubleValue();
            sumSquaredDiff += (diff * diff);
        }
        return Math.sqrt(sumSquaredDiff / valores.size());
    }

    public static void main(String[] args) {
        new CsvAggregator().processar();
    }
}