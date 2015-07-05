package com.poco.PoCoRuntime;

/**
 * Represents an exchange from a PoCo policy.
 */
public class Exchange extends EventResponder implements Matchable, Queryable {
    private Matchable matcher;
    private SRE returnSRE;
    private boolean isQueried;

    public Exchange() {
        // Initialize...
    }

    public void addMatcher(Matchable matcher) {
        this.matcher = matcher;
    }

    public void setSRE(SRE sre) {
        this.returnSRE = sre;
    }

    @Override
    public boolean accepts(Event event) {
        if (matcher == null) //_ case
            return true;
        return matcher.accepts(event);
    }

    @Override
    public SRE query(Event event) {
        if (this.accepts(event)) {
            if (SREUtil.isBopSre(returnSRE)) {
                BopSRE temp = (BopSRE) returnSRE;
                return SREUtil.performBOPs(temp.getSrebop(), temp.getSre1(), temp.getSre2());
            } else if (SREUtil.isUopSre(returnSRE)) {
                UopSRE temp = (UopSRE) returnSRE;
                return SREUtil.performUOPs(temp.getSreuop(), temp.getSre());
            } else
                return returnSRE;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Exchange [matcher=" + matcher + ", returnSRE=" + returnSRE
                + "]";
    }

    public void resetChildrenCursor() {
        return;
    }
}
