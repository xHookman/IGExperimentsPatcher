package com.chacha.igexperimentspatcher;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExperimentsUtils {

    /**
     * Find the class name and method name called to enable experiments
     * @param fileCallingExperiments the files containing the method calling the method to enable experiments
     * @return the class name to patch
     */
    public WhatToPatch findWhatToPatch(File fileCallingExperiments) {
        boolean inMethod = false;
        WhatToPatch whatToPatch = new WhatToPatch();
        String mtdCallingMtdToPatch = getMtdCallingMtdToPatch(fileCallingExperiments);

        Pattern pattern = Pattern.compile("invoke-static \\{[^}]+\\}, \\w+/(\\w+);->(\\w+)\\(\\w+/(\\w+);\\)Z"); // Regex to match the method call, invoke-static {p1}, LX/19o;->A00(LX/0pg;)Z
        Matcher matcher;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileCallingExperiments))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(mtdCallingMtdToPatch)) {
                    // Start of a new method
                    inMethod = true;
                } else if (inMethod) {
                    matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        extractClassToPatch(matcher, whatToPatch);
                        return whatToPatch;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private void extractClassToPatch(Matcher matchedLine, WhatToPatch whatToPatch){
        // Extract the class name from the matched group
        whatToPatch.setClassToPatch(matchedLine.group(1));
        whatToPatch.setMethodToPatch(matchedLine.group(2));
        whatToPatch.setArgumentType(matchedLine.group(3));
    }

    /**
     * @param file the file containing the method calling the method to enable experiments
     * @return the method header
     */
    private String getMtdCallingMtdToPatch(File file) {
        String method = null;
        boolean inMethod = false;
        StringBuilder methodContent = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(".method public static")) {
                    // Start of a new method
                    method = line;
                    inMethod = true;
                    methodContent.setLength(0); // Clear the method content
                } else if (inMethod) {
                   // System.out.println("methodContent: " + methodContent);

                    methodContent.append(line).append("\n");
                    if (line.trim().equals(".end method")) {
                        // End of the method
                        inMethod = false;
                        // Check if the method contains the search text
                        if (methodContent.toString().contains("is_employee")) {
                            System.out.println("Found method calling the method to enable experiments: " + method + "\n");
                            return method;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("No method found for experiments");
        return null;
    }


    /**
     * Patch the method to return true
     * @param classFileToPatch the file containing the method to patch
     * @param methodToPatch the method name to patch
     */
    public void makeMethodReturnTrue(File classFileToPatch, String methodToPatch){
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


}
