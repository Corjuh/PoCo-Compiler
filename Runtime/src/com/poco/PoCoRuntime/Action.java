package com.poco.PoCoRuntime;

import org.aspectj.lang.JoinPoint;

public class Action extends Event {

    public Action() {
        super();
        this.eventType = "Action";
    }

    public Action(JoinPoint joinPoint) {
        super(joinPoint);
        this.eventType = "Action";
    }

    public Action(JoinPoint joinPoint, String matchStr, Object[] objs, String[] varNames) {
        super(joinPoint,matchStr,objs, varNames);
        this.eventType = "Action";
    }


    public Object getResult() {
        return null;
    }

    @Override
    public void setResult(Object result){
        throw new NullPointerException("Action does not contain result.");
    }
}