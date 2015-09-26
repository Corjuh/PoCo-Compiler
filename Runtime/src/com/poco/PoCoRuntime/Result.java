package com.poco.PoCoRuntime;

import org.aspectj.lang.JoinPoint;

public class Result extends Event {
    protected Object result = null;
    protected String resultStr = "";

    public Result() {
        super();
        this.eventType = "Result";
    }

    public Result(JoinPoint joinPoint) {
        super(joinPoint);
        this.eventType = "Result";
    }

    public Result(JoinPoint joinPoint, Object ret) {
        // Step 1. set event signature
        super(joinPoint);
        // Step 2. set event type
        this.eventType = "Result";
        // step 3: set event result;
        String retTyp = RuntimeUtils.getMethodRetTyp(RuntimeUtils.getMethodInfos(joinPoint.getSignature().toLongString()));
        if (!retTyp.equals("void")) {
            this.result = ret;
            this.resultStr = genMontorArgInfo(retTyp, ret);
        } else {
            this.result = new String("done");
            this.resultStr = "";
        }
    }

    public Object getResult() {
        return result;
    }
    // setResult is necessary due to the fact the logic may need to update the
    // return value
    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Result [result=" + result + ", resultStr=" + resultStr
                + ", sig4MatchSre=" + sig4MatchSre + ", signature=" + signature
                + ", eventType=" + eventType + "]";
    }


}