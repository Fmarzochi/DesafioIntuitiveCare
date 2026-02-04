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

    /**
     * Fluxo principal: Encontra anos -> Encontra ZIPs dentro dos anos -> Retorna os 3 últimos.
     */
    public List<String> encontrarLinksUltimosTrimestres(int quantidade) throws IOException {
        // 1. Acha os anos (ex: 2024, 2023)
        List<String> anos = identificarAnos();

        List<String> linksZipEncontrados = new ArrayList<>();

        // 2. Varre ano por ano até achar a quantidade de trimestres desejada
        for (String ano : anos) {
            if (linksZipEncontrados.size() >= quantidade) break;

            String urlAno = URL_BASE + ano + "/";
            System.out.println("   Explorando ano: " + ano);

            // Busca ZIPs recursivamente dentro do ano
            List<String> zipsDoAno = buscarZipsNaPagina(urlAno);

            // Ordena reverso para pegar sempre o 4T antes do 3T, etc.
            zipsDoAno.sort(Collections.reverseOrder());

            linksZipEncontrados.addAll(zipsDoAno);
        }

        // Garante que só retornamos o limite pedido (ex: 3)
        if (linksZipEncontrados.size() > quantidade) {
            return linksZipEncontrados.subList(0, quantidade);
        }
        return linksZipEncontrados;
    }

    private List<String> identificarAnos() throws IOException {
        System.out.println("1. Conectando à raiz da ANS...");
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

    /**
     * Busca arquivos .zip em uma URL. Se achar subpastas, entra nelas (Recursão).
     */
    private List<String> buscarZipsNaPagina(String url) throws IOException {
        List<String> zips = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).timeout(10000).get();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String href = link.attr("href");
                String urlCompleta = url + href;

                if (href.toLowerCase().endsWith(".zip")) {
                    // Achou um arquivo ZIP!
                    zips.add(urlCompleta);
                }
                else if (href.endsWith("/") && !href.startsWith("/") && !href.startsWith(".")) {
                    // Achou uma subpasta (ex: "1T2023/"), entra nela (Recursão)
                    // O filtro !startsWith evita voltar para o diretório pai (../)
                    zips.addAll(buscarZipsNaPagina(urlCompleta));
                }
            }
        } catch (Exception e) {
            System.err.println("   Erro ao ler: " + url + " - " + e.getMessage());
        }
        return zips;
    }

    public static void main(String[] args) {
        try {
            AnsTrimestreFinder finder = new AnsTrimestreFinder();
            List<String> links = finder.encontrarLinksUltimosTrimestres(3); // Queremos os 3 últimos

            System.out.println("\n--- RESULTADO FINAL (LINKS PARA BAIXAR) ---");
            links.forEach(System.out::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}