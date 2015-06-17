package com.poco.PoCoRuntime;

import org.aspectj.lang.JoinPoint;

/**
 * Represents an action/result intercepted by AspectJ for use by PoCo policies.
 * This object is created from an AspectJ JoinPoint object and provides all necessary
 * information for PoCo policies to make a decision.
 */
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

    public String getEventType() {
        return eventType;
    }

    protected void setEventType(String eventType) {
        this.eventType = eventType;
    }

    //setResult is necessary due to the fact the logic may need to update the return value
    public void setResult(Object result) {
        this.result = result;
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
        setSigFrmJoinPoint(joinPoint);
    }

    public Event(JoinPoint joinPoint, String eventType) {
        //Step 1. set event signature
        setSigFrmJoinPoint(joinPoint);
        //Step 2. set event type
        this.eventType = eventType;
    }

    public Event(JoinPoint joinPoint, String eventType, Object ret) {
        //Step 1. set event signature
        setSigFrmJoinPoint(joinPoint);
        //Step 2. set event type
        this.eventType = eventType;
        //step 3: set event result;
        if(RuntimeUtils.hasReturnValue(joinPoint.getSignature().toString()))
            this.result =ret;
        else
            this.result = new String("done");
    }

    protected void setSigFrmJoinPoint(JoinPoint joinPoint) {
        String methodName = RuntimeUtils.getMethodName(joinPoint.getSignature().toLongString());
        methodName = RuntimeUtils.downsizeMethodName(methodName);
        String argStr = RuntimeUtils.getfunArgstr(joinPoint.getSignature().toLongString());
        if(argStr == null)
            argStr = "";
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