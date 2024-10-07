package io.github.glandais.npbot.npbot;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MarkdownAggregator {

    public static void main(String[] args) {
        try (InputStream fis = MarkdownAggregator.class.getResourceAsStream("/docs/wiki.zip")) {
            String aggregatedMarkdown = readMarkdownFromZip(fis);
            System.out.println(aggregatedMarkdown);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readMarkdownFromZip(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName();

                // On ne prend que les fichiers .md
                if (fileName.endsWith(".md")) {
                    // Récupère les parties du chemin pour les titres
                    String[] pathParts = fileName.split("/");

                    // Générer les titres basés sur l'arborescence
                    for (int i = 0; i < pathParts.length - 1; i++) {
                        result.append("#".repeat(1 + i))
                                .append(" ")
                                .append(formatPart(pathParts[i]))
                                .append("\n\n");
                    }

                    // Lire le contenu du fichier markdown et ajuster les niveaux de titres
                    String fileContent = readMarkdownFileContent(zis);
                    result.append(adjustMarkdownHeaderLevels(fileContent, pathParts.length - 1));
                    result.append("\n");
                }
            }
        }

        return result.toString();
    }

    private static String formatPart(String part) {
        part = part.replace("-", " ");
        return part;
    }

    private static String readMarkdownFileContent(InputStream is) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        return content.toString();
    }

    private static String adjustMarkdownHeaderLevels(String content, int levelOffset) {
        String[] lines = content.split("\n");
        StringBuilder adjustedContent = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("#")) {
                // Compter le nombre de '#' pour ajuster le niveau
                int headerLevel = 0;
                while (headerLevel < line.length() && line.charAt(headerLevel) == '#') {
                    headerLevel++;
                }

                // Ajouter l'offset de niveau au header
                adjustedContent.append("#".repeat(Math.max(0, headerLevel + levelOffset)))
                        .append(line.substring(headerLevel))
                        .append("\n");
            } else {
                adjustedContent.append(line).append("\n");
            }
        }

        return adjustedContent.toString();
    }
}
