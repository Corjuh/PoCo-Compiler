package com.poco.PoCoRuntime;

/**
 * Objects that can match events must implement this interface (i.e. Policy, Execution, Exchange, Matchs, Match).
 */
public interface Matchable {
    public boolean accepts(Event event);
}
