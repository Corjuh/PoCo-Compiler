package com.poco.PoCoRuntime;

/**
 * Represents an exchange from a PoCo policy.
 */
public class Exchange extends EventResponder implements Matchable, Queryable {
    private Matchs matcher;
    private SRE returnSRE;

    public Exchange() {
        // Initialize...
    }

    public void addMatcher(Matchs matcher) {
        this.matcher = matcher;
    }

    public void setSRE(SRE sre) {
        this.returnSRE = sre;
    }

    @Override
    public boolean accepts(Event event) {
        return matcher.accepts(event);
    }

    @Override
    public SRE query(Event event) {
        if (this.accepts(event)) {
            return returnSRE;
        }
        return null;
    }
}
