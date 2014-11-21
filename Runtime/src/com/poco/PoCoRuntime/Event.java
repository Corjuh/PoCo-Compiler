package com.poco.PoCoRuntime;

import org.aspectj.lang.JoinPoint;

/**
 * Represents an action/result intercepted by AspectJ for use by PoCo policies.
 * This object is created from an AspectJ JoinPoint object and provides all necessary
 * information for PoCo policies to make a decision.
 */
public class Event {
    private String signature;
    public String eventType;

    /**
     * Construct an event from explicitly passed details. Used when testing.
     *
     * @param signature method signature of event
     */
    public Event(String eventType, String signature) {
        this.eventType = eventType;
        this.signature = signature;
    }

    public Event(String eventType) {
        this.eventType = eventType;
        this.signature = "";
    }

    public Event(JoinPoint joinPoint) {
        this.signature = joinPoint.getSignature().toString();
    }

    public String getSignature() {
        return signature;
    }
}
