package com.poco.PoCoRuntime;

public class InfintMatch extends Match {
    private SRE matchSre;

    public void setSRE(SRE sre) {
        this.matchSre = sre;
    }

    @Override
    public String toString() {
        return "InfintMatch{" + "matchSre=" + matchSre + '}';
    }

    @Override
    /**
     * if match the event, then return true, else return false need
     * load all the values for the variables so that the isInfinite can be evaluated
     */
    public boolean accepts(Event event) {
        return SREUtil.isInfinite(matchSre.getAbsVal());
    }
}
