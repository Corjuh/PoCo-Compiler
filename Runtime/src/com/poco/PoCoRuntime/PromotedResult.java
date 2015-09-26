package com.poco.PoCoRuntime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;

public class PromotedResult extends Result {

    //private String promotedMethod;

    public PromotedResult(JoinPoint joinPoint, Constructor run, Object ret) {
        // Step 1: setSigFrmJoinPoint
        super(joinPoint);
        // Step 3: set promotedMethod
        //this.promotedMethod = className;
        this.sig4MatchSre = RuntimeUtils.getConstruSig(run);
        // Step4. set return result
        this.result = ret;
    }

    public PromotedResult(JoinPoint joinPoint, Method run, Object ret) {
        super(joinPoint);
        //this.promotedMethod = RuntimeUtils.getInvokeMethoSig(run);
        this.sig4MatchSre = RuntimeUtils.getInvokeMethoSig(run);
        this.setResult(ret);
    }

	/*
	 * public PromotedResult(String signature, String promotedMethod) {
	 * super(signature); this.promotedMethod = promotedMethod; }
	 */

    @Override
    public String getSignature() {
        return sig4MatchSre;
    }

    @Override
    public String toString() {
        return "PromotedResult [result=" + result + ", resultStr=" + resultStr
                + ", sig4MatchSre=" + sig4MatchSre + ", signature=" + signature
                + ", eventType=" + eventType + "]";
    }

}