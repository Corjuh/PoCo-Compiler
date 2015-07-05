package com.poco.PoCoRuntime;

import java.util.ArrayList;

public class CalculatedSRE extends SRE {
    private String positiveRE;
    private String negativeRE;
    private ArrayList<SRE> posSREs;
    private ArrayList<SRE> negSREs;

    public CalculatedSRE() {
        this.posSREs = new ArrayList<SRE>();
        this.negSREs = new ArrayList<SRE>();
    }

    public ArrayList<SRE> getPosSREs() {
        return posSREs;
    }

    public ArrayList<SRE> getNegSREs() {
        return negSREs;
    }

    public void AddPosSRE(SRE sre) {
        this.posSREs.add(sre);
    }

    public void AddPosSREs(ArrayList<SRE> sre) {
        this.posSREs.addAll(sre);
    }

    public void AddNegSRE(SRE sre) {
        this.negSREs.add(sre);
    }

    public void AddNegSREs(ArrayList<SRE> sre) {
        this.negSREs.addAll(sre);
    }

    public void setPositiveRE(String positiveRE) {
        this.positiveRE = positiveRE;
    }

    public void setNegativeRE(String negativeRE) {
        //this.negativeRE = convertSRE(negativeRE);
        this.negativeRE = negativeRE;
    }

    @Override
    public SRE complement() {
        CalculatedSRE sre = new CalculatedSRE();
        for(SRE temp: this.getNegSREs())
            sre.AddPosSRE(temp.complement());

        for(SRE temp: this.getPosSREs())
            sre.AddNegSRE(temp.complement());

        sre.setPositiveRE(this.negativeRE);
        sre.setNegativeRE(this.positiveRE);
        return sre;
    }

    public String getPositiveRE() {
        return positiveRE;
    }

    public String getNegativeRE() {
        return negativeRE;
    }

    public void setPosSREs(ArrayList<SRE> posSREs) {
        this.posSREs = posSREs;
    }

    public void setNegSREs(ArrayList<SRE> negSREs) {
        this.negSREs = negSREs;
    }

    @Override
    /**
     * Includes only positive portion of SRE
     */
    public SRE getPostiveVal() {
        CalculatedSRE sre = new CalculatedSRE();
        sre.AddPosSREs(this.getPosSREs());
        sre.setPositiveRE(this.positiveRE);
        return sre;
    }

    /**
     * Includes only negative portion of SRE
     *
     * @return
     */
    public SRE getNegativeVal() {
        CalculatedSRE sre = new CalculatedSRE();
        sre.AddNegSREs(this.getNegSREs());
        sre.setNegativeRE(this.negativeRE);
        return sre;
    }

    @Override
    public SRE getAction() {
        CalculatedSRE returnSre = new CalculatedSRE();
        for(SRE temp: this.getPosSREs())
            returnSre.AddPosSRE(temp.getAction());

        for(SRE temp: this.getNegSREs())
            returnSre.AddNegSRE(temp.getAction());

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
        for(SRE temp: this.getPosSREs())
            returnSre.AddPosSRE(temp.getResult());

        for(SRE temp: this.getNegSREs())
            returnSre.AddNegSRE(temp.getResult());

        SRE temp = (new SRE(this.negativeRE,this.positiveRE)).getResult();
        returnSre.setPositiveRE(temp.getPositiveRE());
        returnSre.setNegativeRE(temp.getNegativeRE());

        return returnSre;
    }

    @Override
    public String toString() {
        return "CalculatedSRE [ posSREs=" + posSREs + ", negSREs=" + negSREs
                + "]";
    }
}