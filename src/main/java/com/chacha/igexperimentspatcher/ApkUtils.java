package com.chacha.igexperimentspatcher;

import brut.androlib.ApkDecoder;
import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.src.SmaliBuilder;
import brut.androlib.src.SmaliDecoder;
import brut.common.BrutException;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;
import com.android.tools.smali.dexlib2.DexFileFactory;
import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.analysis.InlineMethodResolver;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedOdexFile;
import com.android.tools.smali.dexlib2.iface.DexFile;
import com.android.tools.smali.dexlib2.iface.MultiDexContainer;
import jadx.core.utils.android.AndroidResourcesUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionMethod;
import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.writer.builder.DexBuilder;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

public class ApkUtils {
    private File out;
    private File apkFile;

    public ApkUtils(File apkFile){
        this.apkFile = apkFile;
    }

    /**
     * Decompile an apk file
     */
    public void extractDexFiles() throws IOException, AndrolibException {
        out = new File(apkFile.getName() + ".out");
        if(out.exists()) {
            return;
        }

        ZipFile zipFile = new ZipFile(apkFile);
        for(FileHeader fileHeader : zipFile.getFileHeaders()){
            if(fileHeader.getFileName().endsWith(".dex")){
                System.out.println("Found dex file: " + fileHeader.getFileName());
                zipFile.extractFile(fileHeader, out.getAbsolutePath());
            }
        }
    }

    public File[] getDexFiles(){
        return out.listFiles((dir, name) -> name.endsWith(".dex"));
    }

    public File[] getDecompiledDexFiles(){
        return out.listFiles((dir, name) -> name.startsWith("classes") && dir.isDirectory());
    }
    public File decodeSmali(File dexFile) throws AndrolibException {
        File decodedSmali = new File(getOutDir().getAbsolutePath() + File.separator + dexFile.getName().replace(".dex", ""));

        if(decodedSmali.exists()) {
            System.out.println("smali folder already exists, skipping decompilation");
            return decodedSmali;
        }

        System.out.println("Decompiling " + dexFile.getName() + " to smali...");
        SmaliDecoder.decode(apkFile, decodedSmali, dexFile.getName(), false, 0);
        return decodedSmali;
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
