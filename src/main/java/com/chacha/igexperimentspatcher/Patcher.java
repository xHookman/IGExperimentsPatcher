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
    private String methodToPatch;
    private File fileToRecompile;
    public Patcher(File apkFile){
        this.apkFile = apkFile;
    }

    /**
     * Patch the apk file
     */
    public void patch() throws BrutException {
        System.out.println("Patching: " + apkFile.getAbsolutePath());
        this.apkUtils = new ApkUtils();
        apkUtils.decompile(apkFile);

        List<File> f = apkUtils.getFileForExperiments();
        try {
            enableExperiments(f.get(0));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        apkUtils.compileToApk(apkFile, new ExtFile(getFileToRecompile()));
    }

    /**
     * Search the method calling the method to enable experiments
     * @param file the file containing the method calling the method to enable experiments
     * @return the method header
     */
    private String searchMethodEnablingExperiments(File file) throws IOException {
        String method = null;
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
                            System.out.println("Found called method to enable experiments");
                            setFileToRecompile(file);
                            return method;
                        }
                    }
                }
            }
        }
        System.out.println("No method found for experiments");
        return null;
    }

    /**
     * Find the class name and method name called to enable experiments
     * @param file the file containing the method calling the method to enable experiments
     * @return the class name to patch
     */
    private String findClassToPatch(File file) throws IOException {
        boolean inMethod = false;

        String methodToPatch = searchMethodEnablingExperiments(file);
        String classToPatch;

        Pattern pattern = Pattern.compile("invoke-static \\{[^}]+}, ([^;]+);->(\\w+)\\("); // Regex to match the method call
        Matcher matcher;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(methodToPatch)) {
                    // Start of a new method
                    inMethod = true;
                } else if (inMethod) {
                    matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        // Extract the class name from the matched group
                        classToPatch = matcher.group(1);
                        classToPatch = classToPatch.substring(3);

                        this.methodToPatch = matcher.group(2);
                        return classToPatch;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Patch the method to return true
     * @param classFileToPatch the file containing the method to patch
     * @param methodToPatch the method name to patch
     */
    private void makeMethodReturnTrue(File classFileToPatch, String methodToPatch){
        System.out.println("Patching method...");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(classFileToPatch));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            boolean inMethod = false;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(".method public static final " + methodToPatch)) {
                    // Start of a new method
                    inMethod = true;
                } else if (inMethod) {
                    if (line.trim().contains("return")) {
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

    /**
     * Enable experiments by patching the method
     * @param file the file containing the method to patch
     */
    private void enableExperiments(File file) throws IOException, InterruptedException {
        System.out.println("Enabling experiments...");
        String classToPatch = findClassToPatch(file);

        System.out.println("Class to patch: " + classToPatch);
        System.out.println("Method to patch: " + methodToPatch);

        File fileToPatch = FileTextSearch.findSmaliFile(apkUtils.getOutDir(), classToPatch + ".smali").get(0);

        System.out.println("File to patch: " + fileToPatch.getAbsolutePath());
        setFileToRecompile(fileToPatch);
        makeMethodReturnTrue(fileToPatch, methodToPatch);
        System.out.println("Experiments enabled successfully.");
    }

    /**
     * Set the file to know which smali folder is to recompile to .dex
     * @param file the smali file edited
     */
    private void setFileToRecompile(File file){
        File currentFile = file.getAbsoluteFile();

        // Iterate until we reach the root directory
        while (!currentFile.getName().startsWith("smali")) {
            currentFile = currentFile.getParentFile();
        }
        fileToRecompile = currentFile;
    }

    /**
     * @return the file to recompile to .dex
     */
    public File getFileToRecompile(){
        return fileToRecompile;
    }

}
