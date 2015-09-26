package com.poco.PoCoRuntime;

public class EqualsMatch extends Match{
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
        //Step 2: compare!
        return SREUtil.isEquals(matchSre1.getAbsVal(), matchSre2.getAbsVal());
    }
}
