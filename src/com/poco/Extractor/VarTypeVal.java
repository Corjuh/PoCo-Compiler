package com.poco.Extractor;

/**
 * Created by caoyan on 10/28/14.
 */
public class VarTypeVal {

    private String varType; 
    private String varContext;

    public VarTypeVal() {
        this.varType = null; 
    }

    public VarTypeVal(String varType, String varContext) {
        this.varType = varType; 
        this.varContext = varContext;
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

    @Override
    public String toString() {
        return "VarTypeVal{" +
                "varType='" + varType + '\'' + 
                ", varContext='" + varContext + '\'' +
                '}';
    }
}
