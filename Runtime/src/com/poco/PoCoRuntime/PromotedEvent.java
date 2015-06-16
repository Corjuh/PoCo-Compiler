package com.poco.PoCoRuntime;

import org.aspectj.lang.JoinPoint;

public class PromotedEvent extends Event{

	private String promotedMethod;
	
	public PromotedEvent(JoinPoint joinPoint, String eventType,String promotedMethod) {
		super(joinPoint);
		this.setEventType(eventType);
		this.promotedMethod = promotedMethod;
	}

	public PromotedEvent(JoinPoint joinPoint, String promotedMethod,String eventType,Object ret) {
		super(joinPoint);
		this.setEventType(eventType);
		this.promotedMethod = promotedMethod;
		this.setResult(ret);
	}
	
	public PromotedEvent(String eventType, String signature, String promotedMethod) {
		super(eventType, signature);
		this.promotedMethod = promotedMethod;
	}

	public PromotedEvent(String eventType) {
		super(eventType);
		this.promotedMethod = null;
	}
	
	public String getPromotedMethod() {
		return promotedMethod;
	}

	public void setPromotedMethod(String promotedMethod) {
		this.promotedMethod = promotedMethod;
	}

	@Override
	public String getSignature() {
        return promotedMethod;
    }
}
