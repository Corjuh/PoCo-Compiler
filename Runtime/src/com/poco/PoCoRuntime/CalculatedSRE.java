package com.poco.PoCoRuntime;

import java.util.ArrayList;

public class CalculatedSRE extends SRE {
    private String positiveRE;
    private String negativeRE;

    @Override
    public SRE getAction() {
        CalculatedSRE returnSre = new CalculatedSRE();
        SRE temp = (new SRE(this.negativeRE,this.positiveRE)).getAction();
        returnSre.setPositiveRE(temp.getPositiveRE());
        returnSre.setNegativeRE(temp.getNegativeRE());

        return returnSre;
    }

    /**
     * Includes only the result in SRE
     *
     * @return
     */
    @Override
    public SRE getResult() {
        CalculatedSRE returnSre = new CalculatedSRE();
        SRE temp = (new SRE(this.negativeRE,this.positiveRE)).getResult();
        returnSre.setPositiveRE(temp.getPositiveRE());
        returnSre.setNegativeRE(temp.getNegativeRE());
        return returnSre;
    }

}