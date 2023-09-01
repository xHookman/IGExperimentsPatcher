package com.chacha.igexperimentspatcher;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        if(args.length == 0) {
            System.out.println("No arguments provided.");
            return;
        }
        String path = args[0];
        Patcher patcher = new Patcher(path);
        patcher.patch();
    }
}