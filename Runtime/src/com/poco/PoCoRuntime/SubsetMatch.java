package com.poco.PoCoRuntime;

public class SubsetMatch extends Match{
    private SRE matchSre1;
    private SRE matchSre2;

    public void setSRE1(SRE sre) {
        this.matchSre1 = sre;
    }

    public void setSRE2(SRE sre) {
        this.matchSre2 = sre;
    }


    @Override
    /**
     * if match the event, then return true, else return false
     */
    public boolean accepts(Event event) {
        SRE sre1 = matchSre1.getAbsVal();
        SRE sre2 = matchSre2.getAbsVal();
        return SREUtil.isSubSet(sre1,sre2);
    }
}