package com.chacha.igexperimentspatcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileTextSearch {

    public static List<File> searchFilesWithTextInDirectories(File root, String searchText) throws InterruptedException {
        List<File> result = new ArrayList<>();

        if (root.isDirectory()) {
            File[] directories = root.listFiles((file, name) -> name.startsWith("smali_") && new File(file, name).isDirectory());

            if (directories != null) {
                ExecutorService executor = Executors.newFixedThreadPool(directories.length);

                List<Future<List<File>>> futures = new ArrayList<>();

                for (File directory : directories) {
                    System.out.println("\u001B Searching in directory: " + directory.getAbsolutePath());
                    futures.add(executor.submit(() -> searchFilesWithText(directory, searchText)));
                }

                for (Future<List<File>> future : futures) {
                    try {
                        result.addAll(future.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.MINUTES);
            }
        }

        return result;
    }

    public static List<File> searchFilesWithText(File directory, String searchText) {
        List<File> result = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && file.getName().equals("X")) {
                    System.out.println("\u001B Searching in X folder: " + file.getAbsolutePath());
                    result.addAll(searchFilesWithText(file, searchText));
                } else if (file.isFile()) {
                    System.out.println("\u001B Searching in file: " + file.getAbsolutePath());
                    if (containsText(file, searchText)) {
                        result.add(file);
                    }
                }
            }
        }

        return result;
    }

    public static boolean containsText(File file, String searchText) {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            // Use regular expression to match the search text (case-insensitive)
            String regex = "(?i).*" + Pattern.quote(searchText) + ".*";
            Matcher matcher = Pattern.compile(regex).matcher(content);
            return matcher.find();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void searchSmaliFiles(String folderPath, final String classNameToFind) throws IOException {
        Path start = Paths.get(folderPath);

        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Files.isRegularFile(file) && file.toString().endsWith(".smali")) {
                    // Read the content of the Smali file
                    String content = new String(Files.readAllBytes(file), "UTF-8");

                    // Check if the class name exists in the Smali file
                    if (content.contains(classNameToFind)) {
                        System.out.println("Class found in Smali file: " + file);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
