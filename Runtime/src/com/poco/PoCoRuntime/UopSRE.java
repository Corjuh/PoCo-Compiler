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
        if(sre1 != null)
            this.sre = SREUtil.performUOPs(srebop, sre1);
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
        return (SREUtil.performUOPs(this.sreuop, this.sre)).complement();
    }

    /**
     * Includes only the actions in SRE
     *
     * @return
     */
    @Override
    public SRE getAction() {
        return (SREUtil.performUOPs(this.sreuop, this.sre)).getAction();
    }

    /**
     * Includes only the result in SRE
     *
     * @return
     */
    @Override
    public SRE getResult() {
        return (SREUtil.performUOPs(this.sreuop, this.sre)).getResult();
    }

    @Override
    /**
     * Includes only positive portion of SRE
     */
    public SRE getPostiveVal() {
        return (SREUtil.performUOPs(this.sreuop, this.sre)).getPostiveVal();
    }

    /**
     * Includes only negative portion of SRE
     *
     * @return
     */
    public SRE getNegativeVal() {
        return (SREUtil.performUOPs(this.sreuop, this.sre)).getNegativeVal();
    }

    public String getSreuop() {
        return sreuop;
    }
}