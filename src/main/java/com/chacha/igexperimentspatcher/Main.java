package com.chacha.igexperimentspatcher;

import brut.common.BrutException;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws BrutException, IOException {
        if(args.length == 0) {
            showUsageError();
            return;
        }

        String path = null;
        boolean forXposed = false;
        int i;

        for(i=0; i<args.length; i++) {
            if(args[i].equals("-p")) {
                path = args[i+1];
            }

            if(args[i].equals("-x")) {
                forXposed = true;
            }
        }

        if(path == null) {
            showUsageError();
            return;
        }

        File fileToPatch = new File(path);

        if(!(fileToPatch.exists())){
            System.err.println("File not found: " + path);
            return;
        }

        Patcher patcher = new Patcher(fileToPatch);

        if(forXposed)
            patcher.findWhatToPatch();
        else
            patcher.patch();
    }
    private static void showUsageError(){
        System.out.println("No arguments provided. Usage: java -jar IGExperimentsPatcher.jar -p <path to apk>");
        System.out.println("Use -x if you only need the class and method to patch (for xposed module).");
    }
}