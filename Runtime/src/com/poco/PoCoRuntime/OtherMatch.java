package com.poco.PoCoRuntime;

/**
 * Created by caoyan on 1/12/15.
 */
public class OtherMatch extends Match {
    private String operator;
    SRE sre1;
    SRE sre2;

    public OtherMatch() {
        this.sre1 = null;
        this.sre2 = null;
    }

    public OtherMatch(String operator) {
        this.operator = operator;
        this.sre1 = null;
        this.sre2 = null;
    }

    public OtherMatch(String operator, SRE sre1, SRE sre2) {
        this.operator = operator;
        this.sre1 = sre1;
        this.sre2 = sre2;
    }

    public void setSRE1(SRE sre1) {
        this.sre1 = sre1;
    }

    public void setSRE2(SRE sre2) {
        this.sre2 = sre2;
    }

    @Override
    public boolean accepts(Event event) {
        switch (operator) {
            case "Infinite":
                return SRELib.isInfinite(SRELib.GetBaseSRE(sre1));
            case "Subset":
                return SRELib.isSubSet(sre1,sre2);
            case "Equals":
                return SRELib.isEquals(sre1,sre2);
            default:
                break;
        }
        return false;
    }
}
