package com.poco;

import org.aspectj.lang.JoinPoint;

/**
 * Represents an action/result intercepted by AspectJ for use by PoCo policies.
 * This object is created from an AspectJ JoinPoint object and provides all necessary
 * information for PoCo policies to make a decision.
 */
public class Event {
    private String signature;
    private String eventType;
    private String promotedMethod;
    
    private Object result = null; 

    public Object getResult() {
		return result;
	}
    
    public void setResult(Object result) {
    	this.result = result;
    }

	public String getPromotedMethod() {
		return promotedMethod;
	}

	public void setPromotedMethod(String promotedMethod) {
		this.promotedMethod = promotedMethod;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

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

	@Override
	public String toString() {
		return "Event [signature=" + signature + ", eventType=" + eventType
				+ ", result=" + result + "]";
	}
    
    
}
