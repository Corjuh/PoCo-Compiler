package com.poco.PoCoRuntime;

import org.aspectj.lang.JoinPoint;

/**
 * Represents an action/result intercepted by AspectJ for use by PoCo policies.
 * This object is created from an AspectJ JoinPoint object and provides all
 * necessary information for PoCo policies to make a decision.
 */
public abstract class Event {
    protected String sig4MatchSre;
    protected String signature;

    protected String eventType;

    public String getEventType() {
        return eventType;
    }

    public Event() {
        sig4MatchSre = "";
        signature = "";
        eventType = "";
    }

    public Event(JoinPoint joinPoint) {
        setSigFrmJoinPoint(joinPoint);
    }

    /**
     * This constructor is used for the case when there are existing monitoring
     * arguments, e.g., Object around(int value0): PointCut0(value0). In such
     * case, the method signature should be generated in the way to guarantee
     * that no any other events would match it.
     *
     * @param joinPoint
     * @param matchStr
     * @param objs
     */
    public Event(JoinPoint joinPoint, String matchStr, Object[] objs,
                 String[] varNames) {
        String methodSig = genMethodName4Sig(joinPoint);
        // Step 1: get method return type and name
        String methodRetTyp = RuntimeUtils.getMethodRetTyp(methodSig);
        String methodName = RuntimeUtils.getMethodName(methodSig);

        // Step 2: handle method arguments now

        // --- a. get the arguments info from the signature string,
        // do not use joinPoint.getArgs() because it will auto convert primitive
        // type to their "equivalent" non-primitive type
        String argStr = RuntimeUtils.getfunArgstr(methodSig);
        // String argStr =
        // RuntimeUtils.getfunArgstr(joinPoint.getSignature().toLongString());
        if (argStr != null || argStr.trim().length() > 0) {
            String[] args = argStr.split(",");
            StringBuilder argStr4Match = new StringBuilder();

            // Step 2: get monitoring arguments' info
            String[] montoringArgs = matchStr.split(",");
            int motorArgCounts = montoringArgs.length;

            // use to recorder which argument we are currently examining
            int index = 0;
            int index4Obj = 0;
            // use to know whether it is the last argument to be monitored,
            // since we cannot be sure for .. case
            String peek = null;
            boolean skipable = false;

            for (int i = 0; i < args.length; i++) {
                if (index + 1 >= motorArgCounts)
                    peek = null;
                else
                    peek = montoringArgs[index + 1];

                int tryMatching = argMatching(args[i], montoringArgs[index], peek, skipable);
                if (tryMatching == 0) {
                    // change this sig to either the value of this variable
                    // (for primitive type) and the address of this variable
                    // (non-primitive type case) the current value should
                    // match montoringArgs[index], and argVals[index]
                    if (index4Obj < objs.length) {
                        args[i] = genMontorArgInfo(montoringArgs[index],
                                objs[index4Obj]);

                        if (varNames != null)
                            argStr4Match.append(genMontorArg4Match(
                                    montoringArgs[index], objs[index4Obj],
                                    varNames[index]));
                        else
                            argStr4Match
                                    .append(genMontorArg4Match(
                                            montoringArgs[index],
                                            objs[index4Obj], null));
                    } else {
                        args[i] = montoringArgs[index];
                        argStr4Match.append(montoringArgs[index]);
                    }
                    if (i != args.length - 1)
                        argStr4Match.append(",");
                    index++;
                    index4Obj++;
                    continue;
                } else if (tryMatching == 1) {
                    // change this sig to either the value of this variable
                    // (for
                    // primitive type) and the address of this variable
                    // (non-primitive type case)
                    // the current value should match
                    // montoringArgs[index+1],
                    // and montoringArgs[index+1]
                    args[i] = genMontorArgInfo(montoringArgs[index + 1],
                            objs[index4Obj]);

                    if (varNames != null)
                        argStr4Match.append(genMontorArg4Match(
                                montoringArgs[index], objs[index4Obj],
                                varNames[index]));
                    else {
                        argStr4Match
                                .append(genMontorArg4Match(
                                        montoringArgs[index + 1],
                                        objs[index4Obj], null));
                    }
                    if (i != args.length - 1)
                        argStr4Match.append(",");
                    index += 2;
                    index4Obj++;
                    skipable = false;
                    continue;
                } else if (tryMatching == 2) {
                    skipable = false;
                    continue;
                } else if (tryMatching == 3) {
                    skipable = false;
                    break;
                } else if (tryMatching == 4) {
                    skipable = true;
                    continue;
                }
            }
            argStr = RuntimeUtils.strArrJoin(args, ",");
            this.signature = genMethodSigFrmParts(methodRetTyp, methodName,
                    argStr);
            this.sig4MatchSre = genMethodSigFrmParts(methodRetTyp, methodName,
                    argStr4Match.toString());
        } else {
            this.signature = genMethodSigFrmParts(methodRetTyp, methodName, "");
            this.sig4MatchSre = this.signature;
        }

    }

    /**
     * This method is used to generate Event instance for the case when there
     * are no arguments need to be monitored or binded. For this case, the
     * signature for matching SRE and promoting will be the same
     *
     * @param joinPoint
     */
    protected void setSigFrmJoinPoint(JoinPoint joinPoint) {
        // Step 1: get method signature from joinPoint
        String methodSig = genMethodName4Sig(joinPoint);
        // Step 2: get return type
        String methodRetTyp = RuntimeUtils.getMethodRetTyp(methodSig);
        // Step 3: get method name
        String methodName = RuntimeUtils.getMethodName(methodSig);
        // Step 4: get method argument string value
        String argStr = RuntimeUtils.getfunArgstr(joinPoint.getSignature()
                .toLongString());

        this.signature = genMethodSigFrmParts(methodRetTyp, methodName, argStr);
        this.sig4MatchSre = this.signature;
    }

    private static String genMethodSigFrmParts(String retTyp,
                                               String methodName, String argStr) {
        if (argStr == null || argStr.trim().length() == 0)
            argStr = "";

        if (retTyp == null || retTyp.equals("null"))
            return methodName + "(" + argStr + ")";
        else
            return retTyp + " " + methodName + "(" + argStr + ")";
    }

    /**
     * Get method name, the argument as well as the return type
     *
     * @param joinPoint
     * @return
     */
    private String genMethodName4Sig(JoinPoint joinPoint) {
        String sigStr = joinPoint.getSignature().toLongString();
        boolean isConstr = joinPoint.getKind().equals("constructor-call");
        return downsizeMethodName(sigStr, isConstr);
    }

    /**
     * used to delete the extra property of a methodName, only keep the return
     * type and method name and its arguments
     *
     * @param methodStr
     * @return
     */
    private static String downsizeMethodName(String methodStr,
                                             boolean isConstructor) {
        if (methodStr == null)
            return null;
        String methodName = RuntimeUtils.getMethodName(methodStr);
        String argStr = RuntimeUtils.getfunArgstr(methodStr);
        // if it is constructor add.new
        if (isConstructor) {
            return methodName + ".new(" + argStr + ")";
        } else {
            String retType = RuntimeUtils.getMethodRetTyp(RuntimeUtils
                    .getMethodInfos(methodStr));
            return retType + " " + methodName + "(" + argStr + ")";
        }
    }

    protected String genMontorArgInfo(String varType, Object varVal) {
        if (RuntimeUtils.isPrimitiveType(varType))
            return "#" + varType + "{" + varVal.toString() + "}";
        else
            return "#" + varType + "{" + System.identityHashCode(varVal) + "}";
    }

    protected String genMontorArg4Match(String varType, Object varVal,
                                        String varName) {
        if (varName != null)
            return "#" + varName;

        if (RuntimeUtils.isPrimitiveType(varType))
            return "#" + varType + "{" + varVal.toString() + "}";
        else
            return "$" + System.identityHashCode(varVal);
    }

    protected int argMatching(String arg1, String arg2, String peekStr,
                              boolean skipable) {
        // argument type matched,
        if (arg1.equals(arg2)) {
            return 0;
            // when some of the parameters are not need to be matched
        } else if (arg2.equals("..")) {
            if (skipable) {
                if (peekStr != null) {
                    // it means find the right one, but need increase the index
                    if (arg1.equals(peekStr))
                        return 1;
                    else
                        // this case the argument is actually can be ignored
                        // without need to increase the index
                        return 2;
                } else {
                    return 3;
                }
            } else
                return 4;
        }
        return -1;
    }

    public String getSignature() {
        return signature;
    }

    public abstract Object getResult();

    public abstract void setResult(Object result);

    @Override
    public String toString() {
        return "Event [signature=" + signature + ", eventType=" + eventType
                + "]";
    }
}