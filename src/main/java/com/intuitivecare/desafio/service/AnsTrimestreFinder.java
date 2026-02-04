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

    private static final String URL_BASE = "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/";

    public List<String> encontrarLinksUltimosTrimestres(int quantidade) throws IOException {
        List<String> anos = identificarAnos();
        List<String> linksZipEncontrados = new ArrayList<>();

        for (String ano : anos) {
            if (linksZipEncontrados.size() >= quantidade) break;

            String urlAno = URL_BASE + ano + "/";
            System.out.println("Explorando ano: " + ano);

            List<String> zipsDoAno = buscarZipsNaPagina(urlAno);
            zipsDoAno.sort(Collections.reverseOrder());

            linksZipEncontrados.addAll(zipsDoAno);
        }

        if (linksZipEncontrados.size() > quantidade) {
            return linksZipEncontrados.subList(0, quantidade);
        }
        return linksZipEncontrados;
    }

    private List<String> identificarAnos() throws IOException {
        System.out.println("Conectando a raiz da ANS...");
        Document doc = Jsoup.connect(URL_BASE).timeout(10000).get();
        Elements links = doc.select("a[href]");

        List<String> anos = new ArrayList<>();
        for (Element link : links) {
            String href = link.attr("href").replace("/", "");
            if (href.matches("\\d{4}")) {
                anos.add(href);
            }
        }
        anos.sort(Collections.reverseOrder());
        return anos;
    }

    private List<String> buscarZipsNaPagina(String url) throws IOException {
        List<String> zips = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).timeout(10000).get();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String href = link.attr("href");
                String urlCompleta = url + href;

                if (href.toLowerCase().endsWith(".zip")) {
                    zips.add(urlCompleta);
                }
                else if (href.endsWith("/") && !href.startsWith("/") && !href.startsWith(".")) {
                    zips.addAll(buscarZipsNaPagina(urlCompleta));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler: " + url + " - " + e.getMessage());
        }
        return zips;
    }
}