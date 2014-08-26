package com.poco.PoCoRuntime;

import java.util.ArrayList;

/**
 * Tree of Match objects
 */
public class Matchs implements Matchable {
    private ArrayList<Matchable> children;

    public Matchs() {
        children = new ArrayList<>();
    }

    public void addChild(Matchable child) {
        children.add(child);
    }

    @Override
    public boolean accepts(Event event) {
        // TODO: This is placeholder code. Matchs needs to intelligently query its children.
        for (Matchable child : children) {
            if (child.accepts(event)) {
                return true;
            }
        }
        return false;
    }
}
