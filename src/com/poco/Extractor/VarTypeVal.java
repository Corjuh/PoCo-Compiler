package com.poco.Extractor;

import com.poco.PoCoParser.PoCoParser;

/**
 * Created by caoyan on 10/28/14.
 */
public class VarTypeVal {
    public enum ClosureType {
        RE_TYPE,
        SRE_TYPE
    }

    private ClosureType varType;
    private PoCoParser.ReContext reContext;
    private PoCoParser.SreContext sreContext;


    public VarTypeVal(ClosureType type) {
        this.varType = type;
        this.reContext = null;
        this.sreContext = null;
    }

    public VarTypeVal(ClosureType type, PoCoParser.ReContext reContext, PoCoParser.SreContext sreContext) {
        this.varType = type;
        this.reContext = reContext;
        this.sreContext = sreContext;
    }

    public ClosureType getVarType() {
        return varType;
    }

    public void setVarType(ClosureType varType) {
        this.varType = varType;
    }

    public PoCoParser.ReContext getReContext() {
        return reContext;
    }

    public void setReContext(PoCoParser.ReContext reContext) {
        this.reContext = reContext;
    }

    public PoCoParser.SreContext getSreContext() {
        return sreContext;
    }

    public void setSreContext(PoCoParser.SreContext sreContext) {
        this.sreContext = sreContext;
    }

    @Override
    public String toString() {
        String reStr = "null";
        String sreStr = "null";
        if (reContext != null)
            reStr = reContext.getText().toString();
        if (sreContext != null)
            sreStr = sreContext.getText().toString();

        return "VarTypeVal{\n" +
                "varType = " + varType +
                "," +
                "\n sreContext= '" + sreStr + '\'' +
                ",\n reContext='" + reStr + '\'' +
                '}' +
                "\n";
    }
}
