package com.chacha.igexperimentspatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Patcher {
    private final String pathToApk;
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
        System.out.println("Found experiments file: " + f);
    }

    private String searchMethodContentForExperiments(String filePath) throws IOException {
        boolean inMethod = false;
        String currentMethod = null;
        StringBuilder methodContent = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(".method")) {
                    // Start of a new method
                    inMethod = true;
                    currentMethod = line.trim();
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
    private void enableExperiments(File file) throws IOException {
        System.out.println("Enabling experiments...");
        String methodContent = searchMethodContentForExperiments(file.getAbsolutePath());
        methodContent.replaceAll("invoke-static \\{p\\d+\\}, LX/[A-Z0-9]+;->A0[0-9]+\\(.+?\\)Z", "const/4 v0, 0x1");
        System.out.println("Patched method content:\n");
        System.out.println(methodContent);
        /* Example of the line in smali code:
            invoke-static {p1}, LX/12V;->A00(Lcom/instagram/service/session/UserSession;)Z
         */
    }
}
