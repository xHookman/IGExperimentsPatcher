package com.chacha.igexperimentspatcher;

import brut.common.BrutException;
import brut.directory.ExtFile;
import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Patcher {
    private final File apkFile;
    private ApkUtils apkUtils;
    private String method, methodToPatch;
    private File fileToRecompile;
    public Patcher(File apkFile){
        System.out.println("patcher constructor");
        this.apkFile = apkFile;
    }

    public void patch() throws BrutException {
        System.out.println("Patching: " + apkFile.getAbsolutePath());
        this.apkUtils = new ApkUtils();
      /*  if(new File(apkFile.getName() + ".out").exists()) {
            System.out.println("decompiled folder already exists, skipping decompilation");
        } else*/
            apkUtils.decompile(apkFile);

        List<File> f = apkUtils.getFileForExperiments();
        try {
            enableExperiments(f.get(0));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        apkUtils.compileToApk(apkFile, new ExtFile(getFileToRecompile()));
    }

    private String extractMethodName() {
        return method;
    }

    private String searchMethodContentForExperiments(File file) throws IOException {
        boolean inMethod = false;
        StringBuilder methodContent = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(".method public static")) {
                    // Start of a new method
                    method = line;
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

    private String findClassToPatch(String methodContent, File file) throws IOException {
        boolean inMethod = false;
        if (methodContent == null) {
            System.out.println("No method found for experiments");
            return null;
        }
        String methodToPatch = extractMethodName();
        String classToPatch;

        Pattern pattern = Pattern.compile("invoke-static \\{[^\\}]+\\}, ([^;]+);->(\\w+)\\(");
        Matcher matcher;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(methodToPatch)) {
                    // Start of a new method
                    inMethod = true;
                } else if (inMethod) {
                    //System.out.println("Actual line: " + line);
                    matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        // Extract the class name from the matched group
                        classToPatch = matcher.group(1);
                        classToPatch = classToPatch.substring(3);

                        String methodName = matcher.group(2);

                        this.methodToPatch = methodName;

                        return classToPatch;
                    }
                }
            }
        }
        return null;
    }

    private void makeMethodReturnTrue(File classFileToPatch, String methodToPatch){
        System.out.println("Patching method...");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(classFileToPatch));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            boolean inMethod = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(".method public static final " + methodToPatch)) {
                    System.out.println("In method: " + line);
                    // Start of a new method
                    inMethod = true;
                } else if (inMethod) {
                    System.out.println("Actual line: " + line);
                    if (line.trim().contains("return")) {
                        System.out.println("Patching line: " + line);
                        // Replace the line with the patched line
                        line = "const/4 v0, 0x1\nreturn v0\n";
                        inMethod = false;
                    }
                }
                stringBuilder.append(line).append("\n");
            }
            reader.close();

            // Write the patched method to the file
            FileWriter writer = new FileWriter(classFileToPatch);
            writer.write(stringBuilder.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Method patched successfully.");
    }

    private void findAndPatchMethod(File file) throws IOException, InterruptedException {
        String methodContent = searchMethodContentForExperiments(file);
        String classToPatch = findClassToPatch(methodContent, file);

        System.out.println("Class to patch: " + classToPatch);
        System.out.println("Method to patch: " + methodToPatch);

        File fileToPatch = FileTextSearch.findSmaliFile(apkUtils.getOutDir(), classToPatch + ".smali").get(0);
        System.out.println("File to patch: " + fileToPatch.getAbsolutePath());
        setFileToRecompile(fileToPatch);
        makeMethodReturnTrue(fileToPatch, methodToPatch);
    }

    private void enableExperiments(File file) throws IOException, InterruptedException {
        System.out.println("Enabling experiments...");
        findAndPatchMethod(file);
        System.out.println("Experiments enabled successfully.");
    }

    private void setFileToRecompile(File file){
        File currentFile = file.getAbsoluteFile();

        // Iterate until we reach the root directory
        while (!currentFile.getName().startsWith("smali")) {
            currentFile = currentFile.getParentFile();
        }
        fileToRecompile = currentFile;
    }
    public File getFileToRecompile(){
        return fileToRecompile;
    }

}
