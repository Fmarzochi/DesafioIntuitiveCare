package com.intuitivecare.desafio.service;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AnsFileDownloader {

    private static final String DIR_ZIPS = "data/zips";
    private static final String DIR_EXTRACTED = "data/extracted";

    public void baixarEExtrair(List<String> urls) throws IOException {
        Files.createDirectories(Paths.get(DIR_ZIPS));
        Files.createDirectories(Paths.get(DIR_EXTRACTED));

        for (String urlZip : urls) {
            String nomeArquivo = urlZip.substring(urlZip.lastIndexOf("/") + 1);
            File destinoZip = new File(DIR_ZIPS, nomeArquivo);

            System.out.println("Baixando: " + nomeArquivo + "...");
            FileUtils.copyURLToFile(new URL(urlZip), destinoZip, 10000, 20000);

            System.out.println("Extraindo: " + nomeArquivo + "...");
            unzip(destinoZip.toString(), DIR_EXTRACTED);
        }
        System.out.println("Operacao concluida em: " + DIR_EXTRACTED);
    }

    private void unzip(String zipFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) dir.mkdirs();

        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(dir, zipEntry);

                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry outside target path: " + zipEntry.getName());
        }
        return destFile;
    }

    public static void main(String[] args) {
        try {
            AnsTrimestreFinder finder = new AnsTrimestreFinder();
            List<String> links = finder.encontrarLinksUltimosTrimestres(3);

            if (!links.isEmpty()) {
                AnsFileDownloader downloader = new AnsFileDownloader();
                downloader.baixarEExtrair(links);
            } else {
                System.err.println("Nenhum link encontrado.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}