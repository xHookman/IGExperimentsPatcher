package com.chacha.igexperimentspatcher;

public class WhatToPatch {
    private String methodToPatch;
    private String classToPatch;

    public WhatToPatch(){
    }

    public void setClassToPatch(String classToPatch) {
        this.classToPatch = classToPatch;
    }

    public void setMethodToPatch(String methodToPatch) {
        this.methodToPatch = methodToPatch;
    }

    public String getMethodToPatch() {
        return methodToPatch;
    }

    public String getClassToPatch() {
        return classToPatch;
    }
}
