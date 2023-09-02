package com.chacha.igexperimentspatcher;

import brut.common.BrutException;
import brut.directory.ExtFile;
import java.io.*;

public class Patcher {
    private final File apkFile;
    private ApkUtils apkUtils;
    private String methodToPatch;
    private File fileToRecompile;
    public Patcher(File apkFile){
        System.out.println("patcher constructor");
        this.apkFile = apkFile;
    }

    public void patch() throws BrutException {
        System.out.println("Patching: " + apkFile.getAbsolutePath());
        this.apkUtils = new ApkUtils();
        if(new File("decompiled").exists()) {
            System.out.println("decompiled folder already exists, skipping decompilation");
        } else
            apkUtils.decompile(apkFile);

       // List<File> f = apkUtils.getFileForExperiments();
        try {
            //DEBUG
            File patchFile = new File("ig.apk.out/smali_classes5/X/Bh4.smali");
            enableExperiments(patchFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        apkUtils.compile(new ExtFile(this.getFileToRecompile()));
    }

    // trouver et renvoyer le nom de la méthode à patcher,
    // chercher dans le fichier smali la méthode complète avec ce nom
    // remplacer la ligne avec le regex par const/4 v0, 0x1

    private String extractMethodName() {
        return methodToPatch;
    }
    private String searchMethodContentForExperiments(File file) throws IOException {
        boolean inMethod = false;
        StringBuilder methodContent = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(".method public static")) {
                    // Start of a new method
                    methodToPatch = line;
                    inMethod = true;
                    methodContent.setLength(0); // Clear the method content
                } else if (inMethod) {
                    methodContent.append(line).append("\n");
                    if (line.trim().equals(".end method")) {
                        // End of the method
                        inMethod = false;

                        // Check if the method contains the search text
                        if (methodContent.toString().contains("is_employee")) {
                            System.out.println("Found method for experiments");
                            setFileToRecompile(file);
                            return methodContent.toString();
                        }
                    }
                }
            }
        }
        System.out.println("No method found for experiments");
        return null;
    }

    private void replaceMethodInFile(File file) throws IOException {
        String methodContent = searchMethodContentForExperiments(file);

        boolean inMethod = false;
        if (methodContent == null) {
            System.out.println("No method found for experiments");
            return;
        }
        System.out.println("Method content: " + methodContent);
        String methodToPatch = extractMethodName();
        StringBuilder originalContent = new StringBuilder();

        System.out.println("Method to patch: " + methodToPatch);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(methodToPatch)) {
                    // Start of a new method
                        inMethod = true;
                        originalContent.append(line).append("\n");
                    } else if (inMethod) {
                    System.out.println("Actual line: " + line);

                    if (line.matches("\\s*invoke-static .*LX/[A-Za-z0-9/;]+->[A-Za-z0-9]+\\(.*\\)Z")) {
                  //  if(line.contains("invoke-static {p1}, LX/1D4;->A00(LX/0e2;)Z")){ // DEBUG
                        System.out.println("Found line to patch");
                        originalContent.append("\tconst/4 v0, 0x1").append("\n");
                    } else {
                        originalContent.append(line).append("\n");
                    }

                    if (line.trim().equals(".end method")) {
                        // End of the method
                        inMethod = false;
                    }
                } else {
                    originalContent.append(line).append("\n");
                }
            }

            // Write the modified content back to the Smali file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(originalContent.toString());
            }

            System.out.println("Line replaced successfully.");
        }
    }

    private void enableExperiments(File file) throws IOException {
        System.out.println("Enabling experiments...");
        //String methodContent = searchMethodContentForExperiments(file.getAbsolutePath());
       /* if(methodContent == null) {
            System.out.println("No method content found for experiments");
            return;
        }*/

       // methodContent = methodContent.replaceAll("invoke-static \\{p1}, LX/[A-Z0-9]+;->A0[0-9]+\\(.+?\\)Z", "const/4 v0, 0x1");
       // System.out.println("Patched method content:\n");
       // System.out.println(methodContent);
        /* Example of the line in smali code:
               invoke-static {p1}, LX/1D4;->A00(LX/0e2;)Z
         */


        replaceMethodInFile(file);
        System.out.println("Experiments enabled successfully.");
    }

    private void setFileToRecompile(File file){
        File currentFile = file.getAbsoluteFile();

        // Iterate until we reach the root directory
        while (!currentFile.getName().startsWith("smali_")) {
            currentFile = currentFile.getParentFile();
        }
        fileToRecompile = currentFile;
    }
    public File getFileToRecompile(){
        System.out.println("File to recompile: " + fileToRecompile.getAbsolutePath());
        return fileToRecompile;
    }
}
