package com.poco.PoCoRuntime;

import org.aspectj.lang.JoinPoint;

/**
 * Represents an action/result intercepted by AspectJ for use by PoCo policies.
 * This object is created from an AspectJ JoinPoint object and provides all necessary
 * information for PoCo policies to make a decision.
 */
public class Event {
    protected String signature;
    protected String eventType;
    
    protected Object result = null; 

    public Object getResult() {
		return result;
	}
    
    public void setResult(Object result) {
    	this.result = result;
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
    	String methodName = RuntimeUtils.getMethodName(joinPoint.getSignature().toLongString());
    	String argStr     = RuntimeUtils.getfunArgstr(joinPoint.getSignature().toLongString());
    	
    	if(joinPoint.getKind().equals("constructor-call"))   //the constructor case, attach.new
        	this.signature = methodName +".new("+ argStr + ")";
        else
        	this.signature = methodName +"("+ argStr + ")";
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
