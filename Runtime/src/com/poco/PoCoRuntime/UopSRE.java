package com.poco.PoCoRuntime;

/**
 * Created by caoyan on 12/14/14.
 */
public class UopSRE extends SRE {
    // SRE Unary Operators: srecomp; sreactions; sreresults; srepos; sreneg
    private String sreuop;
    private SRE sre;

    public UopSRE(String srebop, SRE sre1) {
        super();
        this.sreuop = srebop;
    }

    public SRE getSre() {
        return sre;
    }

    public void setSre(SRE sre) {
        this.sre = sre;
    }

    @Override
    /**
     * return the complement value of a UopSRE. which includes:
     * a) its SRE value need to be set as its complement
     * b) its posSRES value and negSREs need to be switched
     * Variable case can be ignore here
     */
    public SRE complement() {
        return this.getAbsVal().complement();
    }

    /**
     * Includes only the actions in SRE
     *
     * @return
     */
    @Override
    public SRE getAction() {
        return this.getAbsVal().getAction();
    }

    /**
     * Includes only the result in SRE
     *
     * @return
     */
    @Override
    public SRE getResult() {
        return this.getAbsVal().getResult();
    }

    @Override
    /**
     * Includes only positive portion of SRE
     */
    public SRE getPostiveVal() {
        return this.getAbsVal().getPostiveVal();
    }

    /**
     * Includes only negative portion of SRE
     *
     * @return
     */
    @Override
    public SRE getNegativeVal() {
        return this.getAbsVal().getNegativeVal();
    }

    /**
     * get the absolute value of this SRE, that is, apply all the variable
     * values.
     *
     * @return the absolute value
     */
    @Override
    public SRE getAbsVal() {
        SRE val4SRE1 = this.sre.getAbsVal();
        return SREUtil.performUOPs(this.sreuop, val4SRE1);
    }


    public String getSreuop() {
        return sreuop;
    }

    @Override
    public CalculatedSRE convert2CalculatedSre() {
        return SREUtil.performUOPs(this.sreuop, this.sre);
    }
}