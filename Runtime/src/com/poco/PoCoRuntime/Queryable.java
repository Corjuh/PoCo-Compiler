package com.poco.PoCoRuntime;

/**
 * All objects that can provide an SRE response to an action/result must implement this interface
 * (i.e. Policy, Execution, Exchange).
 */
public interface Queryable {
    public SRE query(Event event);
}
