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

    private static List<File> searchInFolder(File folder, String fileName){
        List<File> result = new ArrayList<>();
        File[] files = folder.listFiles();
        if(files != null){
            for(File file : files){
                if(file.isDirectory() && file.getName().equals("X")){
                    System.out.println("\u001B Searching in directory: " + file.getAbsolutePath());
                    result.addAll(searchInFolder(file, fileName));
                } else if(file.isFile() && file.getName().equals(fileName)){
                    System.out.println("\u001B Found file: " + file.getAbsolutePath());
                    result.add(file);
                }
            }
        }
        return result;
    }

    private static List<File> searchFilesWithText(File directory, String searchText) {
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

   /* public static File findSmaliFile(String directoryPath, String smaliFileName) {
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            System.out.println("Directory not found: " + directoryPath);
            return null;
        }

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory() && file.getName().startsWith("smali")) {
                        System.out.println("Searching in directory: " + file.getAbsolutePath());
                        // Recursively search in subdirectories named "smali_classesX"
                        findSmaliFile(file.getAbsolutePath() + File.separator + "X", smaliFileName);
                    } else if (file.isFile() && file.getName().equals(smaliFileName)) {
                        // Found the desired Smali file
                        System.out.println("Found Smali file: " + file.getAbsolutePath());
                        return file;
                    }
                }
            }
        }
        return null;
    }*/

    public static List<File> findSmaliFile(File root, String fileToSearch) throws InterruptedException {
        List<File> result = new ArrayList<>();

        if (root.isDirectory()) {
            File[] directories = root.listFiles((file, name) -> name.startsWith("smali") && new File(file, name).isDirectory());

            if (directories != null) {
                ExecutorService executor = Executors.newFixedThreadPool(directories.length);

                List<Future<List<File>>> futures = new ArrayList<>();

                for (File directory : directories) {
                    System.out.println("\u001B Searching in directory: " + directory.getAbsolutePath());
                    futures.add(executor.submit(() -> searchInFolder(directory, fileToSearch)));
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
}
