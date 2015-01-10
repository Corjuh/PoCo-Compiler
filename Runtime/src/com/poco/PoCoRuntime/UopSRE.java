package com.poco.PoCoRuntime;

/**
 * Created by caoyan on 12/14/14.
 */
public class UopSRE extends SRE {
    // SRE Binary Operators: Union; Conjunction; Disjunction; Equals; Punion
    private String sreuop;
    SRE sre;

    public UopSRE(String srebop, SRE sre1) {
        this.sreuop = srebop;
        this.sre = sre1;
    }

    public SRE getSre() {
        return sre;
    }

    public void setSre(SRE sre) {
        this.sre = sre;
    }

    public String getSreuop() {
        return sreuop;
    }

    @Override
    protected SRE genSRE() {
        if (sreuop != null)
            return SRELib.PerformUOPs(sreuop, SRELib.GetBaseSRE(this.sre));
        else
            return null;
    }
}