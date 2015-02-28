package com.poco.Extractor;

import com.poco.PoCoParser.PoCoParser;

/**
 * Created by caoyan on 10/28/14.
 */
public class VarTypeVal {

    private String varType;
    private PoCoParser.ReContext varLink;
    private String varContext;

    public VarTypeVal() {
        this.varType = null;
        this.varLink = null;
    }

    public VarTypeVal(String varType, PoCoParser.ReContext varLink, String varContext) {
        this.varType = varType;
        this.varLink = varLink;
        this.varContext = varContext;
    }

    public PoCoParser.ReContext getVarLink() {
        return varLink;
    }

    public void setVarLink(PoCoParser.ReContext varLink) {
        this.varLink = varLink;
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
                ", varLink=" + varLink +
                ", varContext='" + varContext + '\'' +
                '}';
    }
}
