package com.poco.PoCoRuntime;

import org.aspectj.lang.JoinPoint;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class PromotedEvent extends Event{

	private String promotedMethod;

	public PromotedEvent(JoinPoint joinPoint, Constructor run, Object ret) {
		//Step 1: setSigFrmJoinPoint
		super(joinPoint);

		//Step 2: set eventType as result
		this.eventType = "result";

		//Step 3: set promotedMethod
		String className = RuntimeUtils.getConstruSig(run);
		this.promotedMethod = className;

		//Step4. set return result
		this.result = ret;
	}


	public PromotedEvent(JoinPoint joinPoint, Method run,String eventType,Object ret) {
		super(joinPoint);
		this.setEventType(eventType);
		this.promotedMethod = RuntimeUtils.getInvokeMethoSig(run);
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