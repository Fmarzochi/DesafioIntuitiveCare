package com.intuitivecare.desafio.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnsTrimestreFinder {

    // URL oficial da ANS onde ficam os arquivos
    private static final String URL_BASE = "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/";

    public List<String> identificarUltimosTrimestres() throws IOException {
        System.out.println("1. Conectando ao site da ANS: " + URL_BASE);

        // Faz o download do HTML da página
        Document doc = Jsoup.connect(URL_BASE).timeout(10000).get();

        // Pega todos os links (tags <a>)
        Elements links = doc.select("a[href]");
        List<String> anosEncontrados = new ArrayList<>();

        for (Element link : links) {
            String href = link.attr("href");
            // Filtra apenas pastas que parecem anos (ex: "2022/", "2023/")
            if (href.matches("\\d{4}/")) {
                anosEncontrados.add(href.replace("/", ""));
            }
        }

        // Ordena do mais recente para o mais antigo (2023, 2022...)
        anosEncontrados.sort(Collections.reverseOrder());
        System.out.println("   Anos encontrados: " + anosEncontrados);

        return anosEncontrados;
    }

    // Método MAIN para testarmos agora mesmo se funciona
    public static void main(String[] args) {
        try {
            AnsTrimestreFinder finder = new AnsTrimestreFinder();
            finder.identificarUltimosTrimestres();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}