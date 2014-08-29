package com.poco.PoCoRuntime;

/**
 * Represents errors raised during the execution of a PoCo policy.
 */
public class PoCoException extends Exception {
    public PoCoException(String message) {
        super(message);
    }
}
