package com.chacha.igexperimentspatcher;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
public class FileTextSearch {

    public static List<File> searchFilesWithText(File directory, String searchText) {
        List<File> result = new ArrayList<>();

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (Thread.interrupted()) {
                    //System.out.println("Stopping file search in " + directory.getParentFile().getName());
                    return result;
                }

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
            return content.contains(searchText);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }//



    public static File findSmaliFile(WhatToPatch whatToPatch, ApkUtils apkUtils) throws RuntimeException {
        String path = whatToPatch.getClassToPatch().replace(".", File.separator) + ".smali";
        File fileToPatch = null;
        File[] classesFolders = apkUtils.getOutDir().listFiles((dir, name) -> name.startsWith(ApkUtils.DEX_BASE_NAME) && !name.endsWith(".dex"));

        if(classesFolders == null){
            throw new RuntimeException("No smali folder not found in " + apkUtils.getOutDir().getAbsolutePath());
        }

        for(File file : classesFolders){
            if(new File(file.getAbsolutePath() + File.separator + path).exists()){
                fileToPatch = new File(file.getAbsolutePath() + File.separator + path);
                break;
            }
        }

        if(fileToPatch == null){
            throw new RuntimeException("File to patch not found");
        }

        return fileToPatch;
    }
}
