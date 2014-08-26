package com.poco.PoCoRuntime;

import org.aspectj.lang.JoinPoint;

/**
 * Represents an action/result intercepted by AspectJ for use by PoCo policies.
 * This object is created from an AspectJ JoinPoint object and provides all necessary
 * information for PoCo policies to make a decision.
 */
public class Event {
    private String signature;

    public Event(JoinPoint joinPoint) {
        this.signature = joinPoint.getSignature().toString();
    }

    public String getSignature() {
        return signature;
    }
}
