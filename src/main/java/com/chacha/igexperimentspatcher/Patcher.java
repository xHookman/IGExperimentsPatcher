package com.chacha.igexperimentspatcher;

import java.io.*;
import java.util.List;

public class Patcher {
    private final String pathToApk;
    private String methodToPatch;
    public Patcher(String pathToApk){
        System.out.println("patcher constructor");
        this.pathToApk = pathToApk;
    }

    public void patch() {
        System.out.println("Patching: " + pathToApk);
        ApkUtils apkUtils = new ApkUtils();
        /*if(new File("decompiled").exists()) {
            System.out.println("decompiled folder already exists, skipping decompilation");
        } else*/
            apkUtils.decompile(pathToApk);

        List<File> f = apkUtils.getFileForExperiments();
       // System.out.println("Found experiments file: " + f);
        try {
            //DEBUG
            //File patchFile = new File("ig.apk.out/smali_classes5/X/Bh4.smali");
            enableExperiments(f.get(0));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // trouver et renvoyer le nom de la méthode à patcher,
    // chercher dans le fichier smali la méthode complète avec ce nom
    // remplacer la ligne avec le regex par const/4 v0, 0x1

    private String extractMethodName() {
        return methodToPatch;
    }
    private String searchMethodContentForExperiments(String filePath) throws IOException {
        boolean inMethod = false;
        StringBuilder methodContent = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
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
                            return methodContent.toString();
                        }
                    }
                }
            }
        }
        System.out.println("No method found for experiments");
        return null;
    }

    private void replaceMethodInFile(String filePath) throws IOException {
        String methodContent = searchMethodContentForExperiments(filePath);
        boolean inMethod = false;
        if (methodContent == null) {
            System.out.println("No method found for experiments");
            return;
        }
        System.out.println("Method content: " + methodContent);
        String methodToPatch = extractMethodName();
        StringBuilder originalContent = new StringBuilder();

        System.out.println("Method to patch: " + methodToPatch);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
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
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
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


        replaceMethodInFile(file.getAbsolutePath());
        System.out.println("Experiments enabled successfully.");
    }
}
