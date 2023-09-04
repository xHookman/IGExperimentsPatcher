package com.chacha.igexperimentspatcher;

import brut.androlib.ApkDecoder;
import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.src.SmaliBuilder;
import brut.common.BrutException;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionMethod;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class ApkUtils {
    private File out;

    /**
     * Decompile an apk file
     * @param apkFile the apk file to decompile
     */
    public void decompile(File apkFile) {
        ExtFile apk = new ExtFile(apkFile);
        out = new File(apk.getName() + ".out");
        if(out.exists()) {
            System.out.println("decompiled folder already exists, skipping decompilation");
            return;
        }
        ApkDecoder decoder = new ApkDecoder(Config.getDefaultConfig(), apk);
        try {
            decoder.decode(out);
        } catch (AndrolibException | IOException | DirectoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the file containing the call to the method that enable experiments
     */
    public List<File> getFileForExperiments(){
        System.out.println("Searching for experiments file...");
        try {
           return FileTextSearch.searchFilesWithTextInDirectories(out, "const-string v0, \"is_employee\"");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
     }

     /**
      * @return the out directory where the apk was decompiled
      */
     public File getOutDir() {
         return out;
     }

        /**
        * Compile a smali directory to dex
        */
     private void compileSmaliToDex(ExtFile smaliDir, File dexFile) throws BrutException {
        /*if(dexFile.exists()) {
            System.out.println("dex file already exists, skipping compilation");
            return;
        }*/

        System.out.println("Compiling " + smaliDir + " to dex...");
        SmaliBuilder.build(smaliDir, dexFile, 0);
     }

    /**
     * Compile a smali directory to dex and copy it to the apk
     * @param apkFile the apk to copy the dex file to
     * @param smaliFile the smali directory to compile
     */
     public void compileToApk(File apkFile, ExtFile smaliFile) throws BrutException {
        Integer classesDexCount = Integer.getInteger(smaliFile.getName());
         String fileName;
        if(classesDexCount != null)
            fileName = "classes" + classesDexCount + ".dex";
        else
            fileName = "classes.dex";

        File dexFile = new File(fileName);
         compileSmaliToDex(smaliFile, dexFile);
         try {
             copyCompiledFileToApk(apkFile, dexFile);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         dexFile.delete();
     }


    /**
     * Copy a compiled dex file to the apk
     * @param apkFile the apk to copy the dex file to
     * @param dexFile the dex file to copy
     */
    private void copyCompiledFileToApk(File apkFile, File dexFile) throws IOException {
        System.out.println("Copying compiled " + dexFile.getName() + " to APK...");
        File newApk = new File(apkFile.getAbsolutePath().replace(".apk", "-patched.apk"));
        if(newApk.exists())
            newApk.delete();
        Files.copy(apkFile.toPath(), newApk.toPath());

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(CompressionMethod.STORE);
        new ZipFile(newApk).addFile(dexFile, zipParameters);

        System.out.println("Compiled " + dexFile.getName() + " copied to APK successfully.");
    }
}
