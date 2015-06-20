package com.poco.PoCoRuntime;

public class InfintMatch extends Match {
    private SRE matchSre;
    public void setSRE(SRE sre) {
        this.matchSre = sre;
    }

    @Override
    public String toString() {
        return "InfintMatch{" +
                "matchSre=" + matchSre +
                '}';
    }
    @Override
    /**
     * if match the event, then return true, else return false
     */
    public boolean accepts(Event event) {
        return SREUtil.isInfinite(matchSre.genSRE());
    }
}
