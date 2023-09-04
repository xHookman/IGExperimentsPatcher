package com.chacha.igexperimentspatcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileTextSearch {

    /**
     * Search for files containing the search text in the given root directory
     * @param root the root directory to search in (smali, smali_classes2, ...)
     * @param searchText the text to search
     * @return the list of files containing the search text
     */
    public static List<File> searchFilesWithTextInDirectories(File root, String searchText) throws InterruptedException {
        List<File> result = new ArrayList<>();

        if (root.isDirectory()) {
            File[] directories = root.listFiles((file, name) -> name.startsWith("smali_") && new File(file, name).isDirectory());

            if (directories != null) {
                ExecutorService executor = Executors.newFixedThreadPool(directories.length);

                List<Future<List<File>>> futures = new ArrayList<>();

                for (File directory : directories) {
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

    private static String getFileNameWithoutExtension(String fileName){
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        }
        return fileName.substring(0, index);
    }

    /**
     * Search for files with the given name in the given folder
     * @param folder the folder to search in
     * @param fileName the name of the file to search
     * @return the list of files with the given name
     */
    private static List<File> searchInFolder(File folder, String fileName) throws NullPointerException{
        fileName = getFileNameWithoutExtension(fileName);
        List<File> result = new ArrayList<>();
        File[] files = folder.listFiles();
        for(File file : files){
            if(file.isDirectory() && file.getName().equals("X")){
                result.addAll(searchInFolder(file, fileName));
            } else if(file.isFile() && file.getName().startsWith(fileName)){
                System.out.println("\u001B Found file: " + file.getPath());
                result.add(file);
            }
        }
        return result;
    }

    /**
     * Search for files containing the search text in the given directory in X folders
     * @param directory the directory to search in
     * @param searchText the text to search
     * @return the list of files containing the search text
     */
    private static List<File> searchFilesWithText(File directory, String searchText) throws NullPointerException{
        List<File> result = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) {
            throw new NullPointerException();
        }

        for (File file : files) {
            if (file.isDirectory() && file.getName().equals("X")) {
                System.out.println("Found X folder: " + file.getPath());
                result.addAll(searchFilesWithText(file, searchText));
            } else if (file.isFile()) {
                System.out.print("Searching method to patch in file: " + file.getPath() + "\r");
                if (containsText(file, searchText)) {
                    result.add(file);
                }
            }
        }
        return result;
    }

    /**
     * Check if the given file contains the given text
     * @param file the file to check
     * @param searchText the text to search
     * @return true if the file contains the text, false otherwise
     */
    private static boolean containsText(File file, String searchText) {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String regex = "(?i).*" + Pattern.quote(searchText) + ".*";
            Matcher matcher = Pattern.compile(regex).matcher(content);
            return matcher.find();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Search for files with the given name in the given root directory
     * @param root the root directory to search in (smali, smali_classes2, ...)
     * @param fileToSearch the name of the file to search
     * @return the list of files with the given name
     */
    public static List<File> findSmaliFile(File root, String fileToSearch) throws InterruptedException {
        List<File> result = new ArrayList<>();

        if (root.isDirectory()) {
            File[] directories = root.listFiles((file, name) -> name.startsWith("smali") && new File(file, name).isDirectory());

            if (directories != null) {
                ExecutorService executor = Executors.newFixedThreadPool(directories.length);

                List<Future<List<File>>> futures = new ArrayList<>();

                for (File directory : directories) {
                    System.out.println("Searching " + getFileNameWithoutExtension(fileToSearch) + " in directory: " + directory.getPath());
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
