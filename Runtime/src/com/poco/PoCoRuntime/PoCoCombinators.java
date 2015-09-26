package com.poco.PoCoRuntime;

import java.util.ArrayList;

public class PoCoCombinators {
    private ICombinatorLogic logic = new FirstApplicable();

    public SRE performLogic(ArrayList<SRE> sres) {
        return this.logic.performLogic(sres);
    }

    public void setLogic(ICombinatorLogic newLogic) {
        this.logic = newLogic;
    }

    public void setLogic(String str) {
        switch (str) {
            case "FirstApplicable":
                this.logic = new FirstApplicable();
                break;
            default:
                this.logic = new FirstApplicable();
                break;
        }
    }
}