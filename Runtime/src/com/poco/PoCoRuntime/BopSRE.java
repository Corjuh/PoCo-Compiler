package com.poco.PoCoRuntime;

/**
 * Created by caoyan on 12/14/14.
 */
public class BopSRE extends SRE {
    // SRE Binary Operators: Union; Conjunction; Disjunction; Equals; Punion
    private String srebop;
    SRE sre1;
    SRE sre2;

    public BopSRE(String srebop, SRE sre1, SRE sre2) {
        this.srebop = srebop;
        this.sre1 = sre1;
        this.sre2 = sre2;
    }

    public SRE getSre1() {
        return sre1;
    }

    public void setSRE1(SRE sre1) {
        this.sre1 = sre1;
    }

    public SRE getSre2() {
        return sre2;
    }

    public void setSRE2(SRE sre2) {
        this.sre2 = sre2;
    }

    @Override
    protected SRE genSRE() {
        if (srebop != null) {
            SRE tempSRE1 = SREUtil.getBaseSRE(this.sre1);
            SRE tempSRE2 = SREUtil.getBaseSRE(this.sre2);
            return SREUtil.performBOPs(srebop, tempSRE1, tempSRE2);
        } else
            return null;
    }

    public String getSrebop() {
        return srebop;
    }
}