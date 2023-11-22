package com.chacha.igexperimentspatcher;

import java.io.File;
import java.io.FileNotFoundException;
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
    }

    //HAHAHA HELP windows is so trash the name is case insensitive so for example 19M.smali is same as 19m.smali :))))
    public static File similarFileExists(String targetFileName, File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().startsWith(targetFileName)) {
                        return f;
                    }
                }
            }
        }

        return null;
    }
    public static File findSmaliFile(WhatToPatch whatToPatch, ApkUtils apkUtils) throws FileNotFoundException {
        String path = whatToPatch.getClassToPatch();
        File[] classesFolders = apkUtils.getOutDir().listFiles((dir, name) -> name.startsWith(ApkUtils.DEX_BASE_NAME) && !name.endsWith(".dex"));

        if(classesFolders == null){
            throw new RuntimeException("No classes folder not found in " + apkUtils.getOutDir().getAbsolutePath());
        }

        for(File folder : classesFolders){
            File folderToSearchIn = new File(folder + File.separator + path.substring(0, path.lastIndexOf(".")));
            String fileNameToSearch = path.substring(path.lastIndexOf(".") + 1);
            File fileToPatch = similarFileExists(fileNameToSearch, folderToSearchIn);
            if(fileToPatch.exists()){
                return fileToPatch;
            }
        }

        throw new FileNotFoundException("Smali file to patch not found!");
    }
}
