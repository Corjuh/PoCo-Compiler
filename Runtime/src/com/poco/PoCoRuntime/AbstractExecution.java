package com.poco.PoCoRuntime;

import java.util.ArrayList;

/**
 * Base class that both sequential and alternating executions inherit from. Defines functionality
 * that any execution should have.
 */
public abstract class AbstractExecution extends EventResponder implements Matchable, Queryable {
    protected boolean isZeroPlus, isOnePlus;
    protected ArrayList<EventResponder> children = new ArrayList<>();

    public AbstractExecution(String modifier) {
        interpretModifier(modifier);
    }

    public AbstractExecution() {
        interpretModifier("none");
    }

    private void interpretModifier(String modifier) {
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
                System.out.println("Incorrect execution modifier " + modifier);
                System.exit(-1);
        }
    }

    public void addChild(EventResponder child) {
        children.add(child);
    }
}
