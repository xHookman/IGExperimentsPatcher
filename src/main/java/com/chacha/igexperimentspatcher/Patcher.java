package com.chacha.igexperimentspatcher;

import brut.androlib.exceptions.AndrolibException;
import brut.androlib.src.SmaliDecoder;
import brut.common.BrutException;
import brut.directory.ExtFile;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Patcher {
    private final File apkFile;
    private final ApkUtils apkUtils;
    private final ExperimentsUtils experimentsUtils;
    private WhatToPatch whatToPatch;
    private File smaliToRecompile;
    public Patcher(File apkFile){
        this.apkFile = apkFile;
        this.apkUtils = new ApkUtils(apkFile);
        this.experimentsUtils = new ExperimentsUtils();
    }

    /**
     * Find the class and method to patch
     */
    public void findWhatToPatch() throws IOException, AndrolibException {
        List<Future<?>> futures = new ArrayList<>();

        apkUtils.extractDexFiles();
        ExecutorService executor = Executors.newFixedThreadPool(apkUtils.getDexFiles().length);

        for(File smaliClass : apkUtils.getDecompiledDexFiles()){
            Future<?> future = executor.submit(() -> {
                File decodedSmali;

                try {
                    decodedSmali = apkUtils.decodeSmali(smaliClass);
                    System.out.println("\nDecompiled " + smaliClass.getName());
                } catch (AndrolibException ex) {
                    throw new RuntimeException(ex);
                }

                List<File> f = getFilesCallingExperiments(decodedSmali);
                if (f.isEmpty()) {
                    System.err.println("\nNo file calling experiments found in " + decodedSmali.getPath());
                } else {
                    try {
                        this.whatToPatch = experimentsUtils.findWhatToPatch(f.get(0));
                        System.out.println("Class to patch: " + whatToPatch.getClassToPatch());
                        System.out.println("Method to patch: " + whatToPatch.getMethodToPatch());
                        System.out.println("Argument type: " + whatToPatch.getArgumentType());
                    } catch (Exception e) {
                        System.err.println("\nError while finding what to patch: \n\n" + e.getMessage());
                    } finally {
                        executor.shutdownNow();
                    }
                }
            });

            futures.add(future);
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions as needed
            }
        }

        executor.shutdown(); // Shutdown the executor when done
    }

    /**
     * Patch the apk file
     */
    public void patch() throws BrutException {
        System.out.println("Patching: " + apkFile.getAbsolutePath());

        try {
            findWhatToPatch();
            enableExperiments(apkUtils);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        apkUtils.compileToApk(apkFile, new ExtFile(getFileToRecompile()));
    }

    /**
     * @return the file containing the call to the method that enable experiments
     */
    public List<File> getFilesCallingExperiments(File folderToSearchIn) {
        System.out.println("Searching for experiments file in " + folderToSearchIn + "...");
        return FileTextSearch.searchFilesWithText(folderToSearchIn, "const-string v0, \"is_employee\"");
    }


    /**
     * Enable experiments by patching the method
     */
    private void enableExperiments(ApkUtils apkUtils) throws IOException, InterruptedException {
        System.out.println("Enabling experiments...");
        File fileToPatch = FileTextSearch.findSmaliFile(whatToPatch, apkUtils);
        System.out.println("File to patch: " + fileToPatch.getAbsolutePath());
        setPkgToRecompile(fileToPatch);
        experimentsUtils.makeMethodReturnTrue(fileToPatch, whatToPatch.getMethodToPatch());
        System.out.println("Experiments enabled successfully.");
    }


    /**
     * Set smali folder to recompile to .dex
     * @param file the edited smali file
     */
    private void setPkgToRecompile(File file){
        File currentFile = file.getAbsoluteFile();

        // Iterate until we reach the root directory
        while (!currentFile.getName().startsWith("smali")) {
            currentFile = currentFile.getParentFile();
        }
        smaliToRecompile = currentFile;
    }

    /**
     * @return the file to recompile to .dex
     */
    public File getFileToRecompile(){
        return smaliToRecompile;
    }
}
