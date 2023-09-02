package com.chacha.igexperimentspatcher;

import brut.common.BrutException;

import java.io.File;

public class Main {
    public static void main(String[] args) throws BrutException {
        System.out.println("Hello world!");
        if(args.length == 0) {
            System.out.println("No arguments provided.");
            return;
        }
        String path = args[0];
        Patcher patcher = new Patcher(new File(path));
        patcher.patch();
    }
}