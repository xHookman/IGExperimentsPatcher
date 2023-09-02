package com.chacha.igexperimentspatcher;

import brut.androlib.ApkDecoder;
import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.src.SmaliBuilder;
import brut.common.BrutException;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;
import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ApkUtils {
    private File out;
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


    public List<File> getFileForExperiments(){
        System.out.println("Searching for experiments file...");
        try {
           return FileTextSearch.searchFilesWithTextInDirectories(out, "const-string v0, \"is_employee\"");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
     }

     public File getOutDir() {
         return out;
     }

     private void compile(ExtFile smaliDir, File dexFile) throws BrutException {
        if(dexFile.exists()) {
            System.out.println("dex file already exists, skipping compilation");
            return;
        }

        System.out.println("Compiling " + smaliDir + " to dex...");
        SmaliBuilder.build(smaliDir, dexFile, 28);
     }

     public void compileToApk(File apkFile, ExtFile smaliFile) throws BrutException {
        File dexFile = new File(smaliFile.getName().replace("smali_", "") + ".dex");
        compile(smaliFile, dexFile);
         try {
             copyCompiledFileToApk(apkFile, dexFile);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }


    private void copyCompiledFileToApk(File apkFile, File dexFile) throws IOException {
        System.out.println("Copying compiled " + dexFile.getName() + " to APK...");
        File newApk = new File(apkFile.getAbsolutePath().replace(".apk", "-patched.apk"));
        String zipFilePath = apkFile.getPath();
        String filePath = dexFile.getPath();
        String newFilePath = "classes5.dex";
        String newZipFilePath = newApk.getPath();

        // Create a temporary file
        File tempFile = File.createTempFile("temp", ".zip");

        // Open the zip file and the temporary file
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempFile))) {

            // Iterate over the entries in the zip file
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                // If the entry is the one we want to replace, skip it
                if (name.equals(filePath)) {
                    continue;
                }

                // Add the entry to the temporary file
                zos.putNextEntry(new ZipEntry(name));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = zis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
            }

            // Add the new file to the temporary file
            zos.putNextEntry(new ZipEntry(filePath));
            byte[] buffer = new byte[1024];
            int length;
            try (FileInputStream fis = new FileInputStream(newFilePath)) {
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
            }
        }

        // Rename the temporary file to the new zip file
        File newZipFile = new File(newZipFilePath);
        if (!tempFile.renameTo(newZipFile)) {
            throw new IOException("Failed to rename temporary file");
        }

        System.out.println("Compiled " + dexFile.getName() + " copied to APK successfully.");
    }

        /* VERSION 299.0.0.0.96
        .method public static A18(LX/1Cs;LX/0e2;Ljava/lang/String;)V
    .registers 5

    .line 0
    invoke-virtual {p0, p2}, LX/1Cs;->A37(Ljava/lang/String;)V

    .line 1
    .line 2
    .line 3
    invoke-static {p1}, LX/1D4;->A00(LX/0e2;)Z

    .line 4
    .line 5
    .line 6
    move-result v0

    .line 7
    invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;

    .line 8
    .line 9
    .line 10
    move-result-object v1

    .line 11
    const-string v0, "is_employee"

    .line 12
    .line 13
    invoke-virtual {p0, v0, v1}, LX/0Ai;->A16(Ljava/lang/String;Ljava/lang/Boolean;)V

    .line 14
    .line 15
    .line 16
    return-void
    .line 17
    .line 18
.end method

         */
}
