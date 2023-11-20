package com.chacha.igexperimentspatcher;

import brut.common.BrutException;
import brut.directory.ExtFile;
import java.io.*;
import java.util.List;

public class Patcher {
    private final File apkFile;
    private final ApkUtils apkUtils;
    private final ExperimentsUtils experimentsUtils;
    private WhatToPatch whatToPatch;
    private File smaliToRecompile;
    public Patcher(File apkFile){
        this.apkFile = apkFile;
        this.apkUtils = new ApkUtils();
        this.experimentsUtils = new ExperimentsUtils();
    }

    /**
     * Find the class and method to patch
     */
    public void findWhatToPatch() throws IOException {
        apkUtils.decompile(apkFile);
        List<File> f = getFilesCallingExperiments();
        try {
            this.whatToPatch = experimentsUtils.findWhatToPatch(f.get(0));
        } catch (Exception e) {
            System.err.println("Error while finding what to patch: \n\n" + e.getMessage());
        }

        System.out.println("Class to patch: X." + whatToPatch.getClassToPatch());
        System.out.println("Method to patch: " + whatToPatch.getMethodToPatch());
        System.out.println("Argument type: " + whatToPatch.getArgumentType());
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
    public List<File> getFilesCallingExperiments(){
        System.out.println("Searching for experiments file...");
        try {
            return FileTextSearch.searchFilesWithTextInDirectories(apkUtils.getOutDir(), "const-string v0, \"is_employee\"");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Enable experiments by patching the method
     */
    private void enableExperiments(ApkUtils apkUtils) throws IOException, InterruptedException {
        System.out.println("Enabling experiments...");

        File fileToPatch = FileTextSearch.findSmaliFile(apkUtils.getOutDir(), whatToPatch.getClassToPatch() + ".smali").get(0);

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
