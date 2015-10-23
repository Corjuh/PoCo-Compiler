package com.poco.Extractor;

import java.util.ArrayList;

/**
 * Created by caoyan on 10/28/14.
 */
public class VarTypeVal {

    private String varType;
    private ArrayList<String> args;
    private String varContext;

    public VarTypeVal() {
        this.varType = null; 
    }

    public VarTypeVal(String varType, String varContext) {
        this.varType = varType; 
        this.varContext = varContext;
        this.args = null;
    }

    public VarTypeVal(String varType, String varContext, ArrayList<String> arguments) {
        this.varType = varType;
        this.varContext = varContext;
        this.args = arguments;
    }

    public String getVarContext() {
        return varContext;
    }

    public void setVarContext(String varContext) {
        this.varContext = varContext;
    }

    public String getVarType() {
        return varType;
    }

    public void setVarType(String varType) {
        this.varType = varType;
    }

    public ArrayList<String> getFunArgs() {
        return this.args;
    }

    public void addFunArgs(String[] arguments) {
        this.args = new ArrayList<>();
        for(String str: arguments)
            args.add(str);
    }

    @Override
    public String toString() {
        return "VarTypeVal{" +
                "varType='" + varType + '\'' + 
                ", varContext='" + varContext + '\'' +
                '}';
    }
}
