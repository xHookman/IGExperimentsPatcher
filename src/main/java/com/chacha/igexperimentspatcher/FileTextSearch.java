package com.chacha.igexperimentspatcher;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FileTextSearch {

    public static List<File> searchFilesWithTextInDirectories(File root, String searchText) throws InterruptedException {
        List<File> result = new ArrayList<>();

        if (root.isDirectory()) {
            File[] directories = root.listFiles((file, name) -> name.startsWith("smali_") && new File(file, name).isDirectory());

            if (directories != null) {
                ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

                List<Callable<List<File>>> tasks = new ArrayList<>();

                for (File directory : directories) {
                    tasks.add(() -> searchFilesWithText(directory, searchText));
                }

                try {
                    List<Future<List<File>>> futures = executor.invokeAll(tasks);

                    for (Future<List<File>> future : futures) {
                        result.addAll(future.get());
                    }

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    executor.shutdown();
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
                    result.addAll(searchFilesWithText(file, searchText));
                } else if (file.isFile() && containsText(file, searchText)) {
                    result.add(file);
                }
            }
        }

        return result;
    }

    private static boolean containsText(File file, String searchText) {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            System.out.print("\rSearching in file: " + file.getPath());
            return content.contains(searchText);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<File> findSmaliFile(File root, String fileToSearch) throws InterruptedException {
        List<File> result = new ArrayList<>();

        if (root.isDirectory()) {
            File[] directories = root.listFiles((file, name) -> name.startsWith("smali") && new File(file, name).isDirectory());

            if (directories != null) {
                ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

                List<Callable<List<File>>> tasks = new ArrayList<>();

                for (File directory : directories) {
                    tasks.add(() -> searchInFolder(directory, fileToSearch));
                }

                try {
                    List<Future<List<File>>> futures = executor.invokeAll(tasks);

                    for (Future<List<File>> future : futures) {
                        result.addAll(future.get());
                    }

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    executor.shutdown();
                }
            }
        }

        return result;
    }

    private static List<File> searchInFolder(File folder, String fileName) {
        fileName = getFileNameWithoutExtension(fileName);
        List<File> result = new ArrayList<>();

        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && file.getName().equals("X")) {
                    result.addAll(searchInFolder(file, fileName));
                } else if (file.isFile() && file.getName().startsWith(fileName)) {
                    System.out.println("\u001B Found file: " + file.getPath());
                    result.add(file);
                }
            }
        }

        return result;
    }

    private static String getFileNameWithoutExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        }
        return fileName.substring(0, index);
    }
}
