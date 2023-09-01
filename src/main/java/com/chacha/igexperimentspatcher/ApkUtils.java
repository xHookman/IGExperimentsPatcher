package com.chacha.igexperimentspatcher;

import brut.androlib.ApkDecoder;
import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.androlib.mod.SmaliMod;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;
import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ApkUtils {
    private File out;
    //private JadxDecompiler jadx;
    /*public void decompile(String path){
        System.out.println("Decompiling: " + path + "...");
        File inputFile = new File(path);
        File outputDir = new File("decompiled");
        JadxArgs args = new JadxArgs();
        args.setInputFile(inputFile);
        args.setOutDir(outputDir);
        args.setSkipResources(true);
        args.setDeobfuscationOn(false);
        args.setThreadsCount(2);
        args.setCodeWriterProvider(SimpleCodeWriter::new);
        args.setCodeCache(new NoOpCodeCache());

        try {
            jadx = new JadxDecompiler(args);
            jadx.load();
            jadx.save();

            System.out.println("Decompiled: " + outputDir.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public void decompile(String path){
        ExtFile apk = new ExtFile(new File(path));
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
        System.out.println("Decompiled: " + out.getAbsolutePath());
    }

     public List<File> getFileForExperiments(){
        System.out.println("Searching for experiments file...");
        try {
           return FileTextSearch.searchFilesWithTextInDirectories(out, "const-string v0, \"is_employee\"");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
