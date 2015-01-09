package com.poco.PoCoRuntime;

import java.util.ArrayList;

/**
 * Base class that both sequential and alternating executions inherit from. Defines functionality
 * that any execution should have.
 */
public abstract class AbstractExecution extends EventResponder implements Matchable, Queryable {
    protected boolean isZeroPlus, isOnePlus;
    protected ArrayList<EventResponder> children = new ArrayList<>();

    public AbstractExecution(String modifier) throws PoCoException {
        interpretModifier(modifier);
    }

    public AbstractExecution() throws PoCoException {
        interpretModifier("none");
    }

    private void interpretModifier(String modifier) throws PoCoException {
        switch (modifier) {
            case "none":
                isZeroPlus = isOnePlus = false;
                break;
            case "*":
                isZeroPlus = true;
                isOnePlus = false;
                break;
            case "+":
                isZeroPlus = false;
                isOnePlus = true;
                break;
            default:
                throw new PoCoException("Incorrect execution modifier " + modifier);
        }
    }

    public void addChild(EventResponder child) {
        children.add(child);
    }

    public ArrayList<EventResponder> getChildren() {
        return children;
    }
    public boolean isZeroPlus() {
        return isZeroPlus;
    }

    public boolean isOnePlus() {
        return isOnePlus;
    }
}
