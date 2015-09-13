package com.poco.PoCoCompiler;

import com.poco.Extractor.Closure;
import com.poco.Extractor.PointCutExtractor;
import com.poco.Extractor.PolicyTreeNode;

import java.io.PrintWriter;
import java.util.*;

public class PointCutGen {
    private final int indentLevel;
    private final PrintWriter out;
    private String aspectName;
    //private String policyName;
    private String pocoRoot;

    private Closure closure;
    private HashMap<String, String> bindingVars;
    //add this in order to generate the different kinds of advices for pointcuts
    private HashMap<String, HashMap<String, String>> actionPtCut;
    private HashMap<String, HashMap<String, String>> resultPtCut;
    private HashMap<String, HashMap<String, String>> prmResPtCut;
    private HashMap<String, HashMap<String, String>> actResPtCut;
    private int pointcutNum = 0;

    //used to save the policy hierarchy information
    private HashMap<String, PolicyTreeNode> policy2Properities;

    public PointCutGen(PrintWriter out, int indentLevel, Closure closure,
                       PointCutExtractor pcExactor) {
        this.out = out;
        this.pocoRoot = pcExactor.getRoot();
        this.aspectName = "Aspect" + pocoRoot;
        this.indentLevel = indentLevel;
        this.closure = closure;
        this.actionPtCut = pcExactor.getPCStrings();

        this.resultPtCut = pcExactor.getPCStrs4Result();
        this.prmResPtCut = pcExactor.getPCStrs4Promoter();
        this.actResPtCut = new HashMap<String, HashMap<String, String>>();
        this.policy2Properities = pcExactor.getPolicy2Props();
    }

    public void GenAspectJ() {

        outLine(1, "}\n");

        //step 1: add pointcut for monitoring reflection events, only allow poco to do so
        genPt4Reflect();

        //step 2: check the action and result pointcuts sets, if a method needs to be monitored
        //both before and after proceed, then we will need to generate 2 advices for this method
        checkCommonActResPCs();

        //step 3: generate advice for not-promoted action
        genPointCut4Actions();

        //step 4: generate advice for not-promoted result
        genPointCut4Results();

        //step 5: generate advices for those methods that need monitor both before and after proceed
        genPointCut4Events();

        //setp 6: generate advice for promoted action
        genAdvice4PromotedActions();

    }


    private void genPt4Reflect() {
        outLine(1, "pointcut PC4Reflection():");
        outLine(2, "call (* Method.invoke(Object, Object...)) && !within(com.poco.Promoter);\n");
        outLine(1, "Object around(): PC4Reflection()   { ");
        outLine(2, "return new SRE(null,\".\"); ");
        outLine(1, "}\n");
    }

    /**
     * if an method needs to be monitored both before and after proceed,
     * then two advices needs to generated
     */
    private void checkCommonActResPCs() {
        if (actionPtCut.keySet().size() > 0 && resultPtCut.keySet().size() > 0) {
            HashMap<String, HashMap<String, String>> temp4actionPC = new HashMap<>();

            Set<String> keys = actionPtCut.keySet();
            for (Iterator<String> key = keys.iterator(); key.hasNext(); ) {
                String entry = key.next();
                if (resultPtCut.containsKey(entry)) {
                    bindingVars = resultPtCut.get(entry);
                    bindingVars.putAll(actionPtCut.get(entry));
                    resultPtCut.remove(entry);
                    actResPtCut.put(entry, bindingVars);
                } else
                    temp4actionPC.put(entry, actionPtCut.get(entry));

                if (resultPtCut.keySet().size() == 0) break;
            }

            actionPtCut = temp4actionPC;
        }
    }

    private void genPointCut4Actions() {
        genPointCuts(actionPtCut, 0);
    }

    private void genPointCut4Results() {
        genPointCuts(resultPtCut, 1);
    }

    private void genPointCut4Events() {
        genPointCuts(actResPtCut, 2);
    }

    /**
     * Generage pointcut and advice for non-promoted action
     */
    private void genPointCuts(HashMap<String, HashMap<String, String>> target, int mode) {
        HashMap<String, HashMap<String, String>> pointcuts = target;

        Set<String> keys = pointcuts.keySet();
        for (Iterator<String> key = keys.iterator(); key.hasNext(); ) {
            String entry = key.next();
            bindingVars = pointcuts.get(entry);
            /*
            pointcut PointCut1(java.lang.String value0):
                call(java.io.File.new(..,java.lang.String)) && args(*,value0);
            Object around(java.lang.String value0): PointCut1(value0) { ... }
            =============================================================================
            pointcut PointCut1(argStrs[0]):
                call(java.io.File.new(argStrs[3])) && args(argStrs[1]);
            Object around(argStrs[0]): PointCut1(//argStrs[2]) { ... }
            */
            String[] argStrs = new String[]{"", "", "", "", ""};
            ArrayList<String> argStrs0 = new ArrayList<>();
            ArrayList<String> argStrs1 = new ArrayList<>();
            ArrayList<String> argStrs2 = new ArrayList<>();
            ArrayList<String> argStrs3 = new ArrayList<>();
            ArrayList<String> argStrs4 = new ArrayList<>();
            ArrayList<String> argStrs5 = new ArrayList<>();

            //if policy specified that only match a method when a particular parameter has certain value,
            //then advice should only monitor the method when this parameter's value matches
            Hashtable<String, String> varsNeed2Bind4Act = new Hashtable<>();
            Hashtable<String, String> varsNeed2Bind4Res = new Hashtable<>();

            Hashtable<String, String> argVal4Match = null;

            //Step 1: get name, return type of the method that needs to be monitored.
            String methodName = PoCoUtils.getMethodName(entry);

            //Step 2: get parameters info of the method that needs to be monitored.
            // a. 1st check the parameter object is a variable or not, and load info from the closure if so;
            // b. 2nd check the parameter's value is a variable or not, if it is a variable, then need check
            //    if this variable need to be bound in this advice.
            String[] argsList = PoCoUtils.getArgArray(entry);

            if (argsList != null) {
                int count = 0;
                for (int i = 0; i < argsList.length; i++) {
                    // a. 1st check the parameter object is a function or not, and load info from the closure if so;
                    if (argsList[i].startsWith("$") && closure.isFunctionsContain(argsList[i].substring(1)))
                        argsList[i] = loadValFrmClosure(argsList[i].substring(1));
                    // b. 2nd check the parameter's value is a variable or not to generate the correct argument
                    //    String for aspectj advice
                    //String argVal = PoCoUtils.getObjVal(argsList[i]);
                    String argValStr = PoCoUtils.getObjVal(argsList[i]);
                    String argTyp = PoCoUtils.getObjType(argsList[i]);

                    if (argValStr != null) {
                        if (argValStr.startsWith("$") && bindingVars.containsKey(argValStr.substring(1))) {
                            if (argTyp == null)
                                argTyp = "java.lang.String value";
                            if (bindingVars.get(argValStr.substring(1)).toString().equals("result")) {
                                //put info in monitorVal
                                varsNeed2Bind4Res.put(argTyp + " value" + count, argsList[i]);
                            } else {
                                //action or sig case, which means that the binding happens before allowing proceeding
                                varsNeed2Bind4Act.put(argTyp + " value" + count, argsList[i]);
                            }
                            bindingVars.remove(argValStr.substring(1));
                        }
                        //store the arg along with the value it need to be matched
                        if (argVal4Match == null)
                            argVal4Match = new Hashtable<>();
                        argVal4Match.put(argTyp + " value" + count, argValStr);

                        //generate the correct argument String for aspectj advice
                        //generate the correct argument String for aspectj advice
                        argStrs3.add(argTyp);
                        argStrs0.add(argTyp + " value" + count);
                        argStrs1.add("value" + count);
                        argStrs2.add("value" + count);
                        if (isPrimitiveType(argTyp)) {
                            if (argTyp.equals("java.lang.String") || argTyp.equals("String")) {
                                argStrs4.add("#" + argTyp + "{\"+" + "value" + count + "+\"}");
                            } else {
                                argStrs5.add("String arg" + count + " = genValueofStr(value)" + count);
                                argStrs4.add("#" + argTyp + "{\"+" + "arg" + count + "+\"}");
                            }
                        } else {
                            argStrs5.add("String arg" + count + " = getAddr(value)" + count);
                            argStrs4.add("#" + argTyp + "{\"+" + "arg" + count + "+\"}");
                        }
                        count++;
                    } else if (argsList[i].trim().equals("\\*")) {
                        argStrs3.add("..");
                        argStrs1.add("*");
                        argStrs4.add("*");
                    } else {
                        argStrs3.add(argsList[i]);
                        argStrs1.add(" value" + count++);
                        argStrs4.add(argsList[i]);
                    }
                }
                argStrs[0] = argStrs0.toString().replace("[", "").replace("]", "").replace(", ", ",");
                argStrs[1] = argStrs1.toString().replace("[", "").replace("]", "").replace(", ", ",");
                argStrs[2] = argStrs2.toString().replace("[", "").replace("]", "").replace(", ", ",");
                argStrs[3] = argStrs3.toString().replace("[", "").replace("]", "").replace(", ", ",");
                argStrs[4] = argStrs4.toString().replace("[", "").replace("]", "").replace(", ", ",");
            }
            //code gen for this pointcut
            String methodSig = codeGen4PointCutDef(argStrs, methodName);
            //handle the case of binding action signature to a variable,
            //or binding return value to a variable
            if (bindingVars.size() > 0) {
                Set vars = bindingVars.keySet();
                for (Iterator<String> it = vars.iterator(); it.hasNext(); ) {
                    String varName = (String) it.next();
                    //binding return value to a variable case
                    if (bindingVars.get(varName).toString().equals("result")) {
                        varsNeed2Bind4Res.put(PoCoUtils.getMethodRtnTyp(methodSig) + " ret", "$" + varName.toString());
                    } else {  //binding action signature to a variable case
                        String sig = methodName + "(" + argStrs[4] + ")";
                        varsNeed2Bind4Act.put("java.lang.String \"" + sig + "\"", "sig$" + varName.toString());
                    }
                }
            }

            //generate code for action adivce
            if (mode == 0)
                genAdvice4Actions("PointCut" + pointcutNum++, argStrs, varsNeed2Bind4Act, argVal4Match, argStrs5);
            else if (mode == 1) //generate code for action adivce
                genAdvice4Results("PointCut" + pointcutNum++, argStrs, varsNeed2Bind4Res, argVal4Match, argStrs5);
            else
                genAdvice4Events("PointCut" + pointcutNum++, argStrs, varsNeed2Bind4Act, varsNeed2Bind4Res, argVal4Match, argStrs5);
        }
    }

    private String codeGen4PointCutDef(String[] argStrs, String methodName) {
        //if there are some parameters of the method need to be monitored
        String callStr = "";
        if (argStrs[2].length() > 0) {
            outLine(1, "pointcut PointCut%d(%s):", pointcutNum, argStrs[0]);
            callStr = (methodName + "(" + argStrs[3] + ")").replace("\\", "");

            if (argStrs[3].trim().length() > 0)
                outLine(2, "call(%s) && args(%s);\n", callStr, argStrs[1]);
            else
                outLine(2, "call(%s);\n", callStr);

        } else {
            callStr = methodName.replace("\\", "");
            outLine(1, "pointcut PointCut%d():", pointcutNum);
            outLine(2, "call(%s(%s));\n", callStr, argStrs[3]);
        }
        return callStr;
    }

    private void genAdvice4Actions(String pointcutName, String[] argStrs, Hashtable varsNeed2Bind, Hashtable argVal4Match, ArrayList<String> handleSig) {
        //if it is conditional match or need dynamically bind value to variables
        if (varsNeed2Bind != null || argVal4Match != null) {
            outLine(1, "Object around(%s): %s(%s) {", argStrs[0], pointcutName, argStrs[2]);

            //generate conditional statement for conditional monitoring case
            String conditionState = genCoditionStatements(argVal4Match);
            //if is the conditional monitoring case
            if (conditionState != null && conditionState.length() > 0) {
                outLine(2, "if (" + conditionState + ") {");
                outLine(3, "String[] varNames = null;");

                //in order to generate the correct method signature that includes the variable info,
                //need dynamic genearate variable information
                handleSigVar(handleSig, 3);
                valueBind4Advices(varsNeed2Bind, 3);
                if (argStrs[2] != null && argStrs[2].trim().length() > 0) {
                    outLine(3, "Object[] objs = new Object[]{" + argStrs[2] + "};");
                    outLine(3, "root.queryAction(new Action(thisJoinPoint, \"" + argStrs[3] + "\", objs, varNames));");
                } else
                    outLine(3, "root.queryAction(new Action(thisJoinPoint));");

                outLine(3, "if(root.hasRes4Action()) {");
                outLine(4, "return root.getRes4Action();");
                outLine(3, "} else");
                outLine(4, "return proceed(%s);", argStrs[2]);
                outLine(2, "} else");
                outLine(3, "return proceed(%s);", argStrs[2]);
            } else {
                //handle vars in order to generate correct method signatures info, which includes the variable infos
                handleSigVar(handleSig, 2);
                valueBind4Advices(varsNeed2Bind, 2);
                outLine(2, "root.queryAction(new Action(thisJoinPoint));");
                outLine(2, "if(root.hasRes4Action()) {");
                outLine(3, "return root.getRes4Action();");
                outLine(2, "} else");
                outLine(3, "return proceed(%s);", argStrs[2]);
            }
        } else {
            outLine(1, "Object around(): %s() {", pointcutName);
            outLine(2, "root.queryAction(new Action(thisJoinPoint));");
            outLine(2, "if(root.hasRes4Action()) {");
            outLine(3, "return root.getRes4Action();");
            outLine(2, "} else");
            outLine(3, "return proceed();");
        }
        outLine(1, "}\n");
    }

    private void genAdvice4Results(String pointcutName, String[] argStrs, Hashtable varsNeed2Bind, Hashtable argVal4Match, ArrayList<String> handleSig) {
        if (varsNeed2Bind != null || argVal4Match != null) {
            outLine(1, "Object around(%s): %s(%s) {", argStrs[0], pointcutName, argStrs[2]);

            //generate conditional statement for conditional monitoring case
            String conditionState = genCoditionStatements(argVal4Match);
            //if is the conditional monitoring case
            if (conditionState != null && conditionState.length() > 0) {
                outLine(2, "if (" + conditionState + ") {");
                outLine(3, "Object ret = proceed(%s);", argStrs[2]);
                //handleSigVar(handleSig, 3);

                //handling the variable binding
                valueBind4Advices(varsNeed2Bind, 3);

                //generate code for create Event Object,which will be used for policy query action
                genEvent4queryAction(3);
                outLine(2, "}");
                outLine(2, "else");
                outLine(3, "return proceed(%s);", argStrs[2]);
            } else {
                outLine(2, "Object ret = proceed(%s);", argStrs[2]);
                handleSigVar(handleSig, 2);
                //valueBind4Advices(varsNeed2Bind, 2);
                genEvent4queryAction(2);
            }
        } else {
            outLine(1, "Object around(): %s() {", pointcutName);
            outLine(2, "Object ret = proceed();", argStrs[2]);
            handleSigVar(handleSig, 2);
            //valueBind4Advices(varsNeed2Bind, 2);
            genEvent4queryAction(2);
        }

        outLine(1, "}\n");
    }

    private void genAdvice4Events(String pointcutName, String[] argStrs, Hashtable varsNeed2Bind4Act, Hashtable varsNeed2Bind4Res, Hashtable argVal4Match, ArrayList<String> handleSig) {
        if (varsNeed2Bind4Act != null || varsNeed2Bind4Res != null || argVal4Match != null) {
            outLine(1, "Object around(%s): %s(%s) {", argStrs[0], pointcutName, argStrs[2]);
            //generate conditional statement for conditional monitoring case
            String conditionState = genCoditionStatements(argVal4Match);
            //if is the conditional monitoring case
            if (conditionState != null && conditionState.length() > 0) {
                outLine(2, "if (" + conditionState + ") {");
                outLine(3, "String[] varNames = null;");
                handleSigVar(handleSig, 3);
                valueBind4Advices(varsNeed2Bind4Act, 3);
                if (argStrs[2] != null && argStrs[2].trim().length() > 0) {
                    outLine(3, "Object[] objs = new Object[]{" + argStrs[2] + "};");
                    outLine(3, "root.queryAction(new Action(thisJoinPoint, \"" + argStrs[3] + "\", objs, varNames));");
                } else
                    outLine(3, "root.queryAction(new Action(thisJoinPoint));");
                outLine(3, "Object ret = proceed(%s);", argStrs[2]);
                valueBind4Advices(varsNeed2Bind4Res, 3);
                //generate code for create Event Object,which will be used for policy query action
                genEvent4queryAction(3);
                outLine(2, "}");
                outLine(2, "else");
                outLine(3, "return proceed(%s);", argStrs[2]);
            } else {
                valueBind4Advices(varsNeed2Bind4Act, 2);
                outLine(2, "root.queryAction(new Action(thisJoinPoint));");
                outLine(2, "Object ret = proceed(%s);", argStrs[2]);
                handleSigVar(handleSig, 2);
                valueBind4Advices(varsNeed2Bind4Res, 2);
                genEvent4queryAction(2);
            }
        } else {
            outLine(1, "Object around(): %s() {", pointcutName);
            handleSigVar(handleSig, 2);
            valueBind4Advices(varsNeed2Bind4Act, 2);
            outLine(2, "root.queryAction(new Action(thisJoinPoint));");
            outLine(2, "Object ret = proceed();", argStrs[2]);
            valueBind4Advices(varsNeed2Bind4Res, 2);
            genEvent4queryAction(2);
        }
        outLine(1, "}\n");
    }

    private void genEvent4queryAction(int offset) {
        outLine(offset, "Result result = new Result(thisJoinPoint, ret);");
        outLine(offset, "root.queryAction(result);");
        outLine(offset, "return result.getResult();");
    }

    private String genCoditionStatements(Hashtable<String, String> argVal4Match) {
        if (argVal4Match == null || argVal4Match.size() == 0)
            return null;

        int size = argVal4Match.size();
        String[] varTyps = new String[size];
        String[] varNams = new String[size];
        String[] varVals = new String[size];
        int i = 0;
        for (String key : argVal4Match.keySet()) {
            varTyps[i] = key.trim().split("\\s+")[0];
            varNams[i] = key.trim().split("\\s+")[1];
            varVals[i++] = argVal4Match.get(key);
        }

        String returnStr = "";
        for (i = 0; i < varTyps.length; i++) {
            //need handle the case where we do not care about the type and value
            if (varVals[i] == null) continue;
            String matchState;
            if (PoCoUtils.isVariable(varVals[i]))
                matchState = genCondStatWVar(varTyps[i], varNams[i], varVals[i]);
            else
                matchState = genCondStatWVal(varTyps[i], varNams[i], varVals[i]);

            if (matchState != null) {
                returnStr += matchState;
                if (i != varTyps.length - 1)
                    returnStr += " && ";
            }
        }
        return PoCoUtils.trimEndPunc(returnStr, " && ");
    }

    private String genCondStatWVal(String type, String valName, String matchVal) {
        return genCoditionStatement(type, valName, matchVal, 0);
    }

    private String genCondStatWVar(String type, String valName, String matchVal) {
        return genCoditionStatement(type, valName, matchVal, 1);
    }

    /**
     * @param type
     * @param valName
     * @param matchVal
     * @param mode     0: the val is value, 1: the val is variable
     * @return
     */
    private String genCoditionStatement(String type, String valName, String matchVal, int mode) {
        if (matchVal != null && matchVal.length() > 0) {
            matchVal = matchVal.replace("%", "*");
            if (isPrimitiveType(type)) {
                String str = genValueofStr(type, valName);
                if (PoCoUtils.isPoCoObject(matchVal))
                    matchVal = PoCoUtils.getObjVal(matchVal);
                //if(PoCoUtils.isVariable(matchVal) && closure.isFunctionsContain())
                return "RuntimeUtils.valueMatch(" + str + ", \"" + matchVal + "\")";
            } else {
                if (matchVal.startsWith("$") && closure.isVarsContain(matchVal.substring(1))) {
                    matchVal = "RuntimeUtils.getFrmDbWH(\"" + matchVal.substring(1) + "\")";
                    return "RuntimeUtils.ObjMatch(" + valName + ", " + matchVal + ")";
                } else
                    return "RuntimeUtils.ObjMatch(" + valName + ", \"" + matchVal + "\")";
            }
        }
        return null;
    }

    private String genValueofStr(String type, String valName) {
        String str = "";
        switch (type) {
            case "byte":
                return "new String(" + valName + ", \"UTF-8\");";
            case "int":
            case "short":
                return "new Integer(" + valName + ").toString()";
            case "long":
                return "Long.valueOf(" + valName + ")";
            case "double":
                return "Double.valueOf(" + valName + ")";
            case "float":
                return "Float.toString(" + valName + ")";
            case "boolean":
                return "Boolean.toString(" + valName + ")";
            case "char":
                return "Character.toString(" + valName + ")";
            case "String":
            case "java.lang.String":
                return valName;
            default:
                return "String.valueOf(" + valName + ")";
        }
    }

    private void valueBind4Advices(Hashtable varsNeed2Bind, int offset) {
        if (varsNeed2Bind != null && varsNeed2Bind.size() > 0) {
            Set<String> set = varsNeed2Bind.keySet();
            StringBuilder sb4NamList = new StringBuilder();
            int setSize = set.size();
            int index = 0;
            for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
                String argName = it.next();
                String varName = varsNeed2Bind.get(argName).toString();

                //if it is sig binding case, it will not be used for this query
                if (varName.startsWith("sig$")) {
                    varName = varName.substring(4);
                    if (index == set.size() - 1) {
                        if (sb4NamList.length() > 0 && sb4NamList.toString().endsWith(","))
                            sb4NamList.setLength(sb4NamList.length() - 1);
                    }
                } else {
                    varName = varName.substring(1);
                    sb4NamList.append(varName);
                    if (index++ != set.size() - 1)
                        sb4NamList.append(",");
                }
                //typVal[0] will be the new type  of the variable and
                //typVal[1] will be the new value of the variable
                String[] typVal = argName.replace("..", "*").split("\\s+");
                outLine(offset, "DataWH.updateTyeVal(\"" + varName + "\", \"" + typVal[0] + "\", " + typVal[1] + ");");

                if (!isPrimitiveType(typVal[0]))
                    outLine(offset, "DataWH.address2ObjVal.put(Integer.toString(System.identityHashCode(" + typVal[1] + ")),"
                            + typVal[1] + ");");
            }
            if (sb4NamList.length() > 0)
                outLine(offset, "varNames = new String[] {\"" + sb4NamList.toString() + "\"};");
        }
    }

    private void genAdvice4PromotedActions() {
        if (this.prmResPtCut.size() > 0) {
            boolean hasConstructorInvoke = false;
            boolean hasMethodInoke = false;
            Set<String> keys = prmResPtCut.keySet();
            for (Iterator<String> key = keys.iterator(); key.hasNext(); ) {
                if (key.next().contains(".new("))
                    hasConstructorInvoke = true;
                else
                    hasMethodInoke = true;
                if (hasConstructorInvoke && hasMethodInoke)
                    break;
            }

            if (hasConstructorInvoke) {
                outLine(1, "pointcut PointCut%d(Constructor run):", pointcutNum);
                outLine(2, "target(run) && call(* Constructor.newInstance(..));\n");
                outAdviceInvokeConstructor("PointCut" + pointcutNum);
                pointcutNum++;
            }

            if (hasMethodInoke) {
                outLine(1, "pointcut PointCut%d(Method run):", pointcutNum);
                outLine(2, "target(run) &&call(Object Method.invoke(..));\n");
                outAdvicePrologue4Result("PointCut" + pointcutNum);
                pointcutNum++;
            }
        }
    }

    private void outAdvicePrologue4Result(String pointcutName) {
        outLine(1, "Object around(Method run): %s(run) {", pointcutName);
        outLine(2, "if (RuntimeUtils.matchingStack(root.promotedEvents,run)) {");
        outLine(3, "root.promotedEvents.pop();");
        outLine(3, "Object ret = proceed(run);");
        outLine(3, "String retTyp = RuntimeUtils.trimClassName(ret.getClass().toString());");
        genVarBing4Prom();
        outLine(3, "PromotedResult promRes = new PromotedResult(thisJoinPoint,run,ret);");
        outLine(3, "root.queryAction(promRes);");
        outLine(3, "return ret;");
        outLine(2, "}");
        outLine(2, "else");
        outLine(3, "return proceed(run);");
        outLine(1, "}\n");
    }

    private void outAdviceInvokeConstructor(String pointcutName) {
        outLine(1, "Object around(Constructor run): %s(run) {", pointcutName);
        outLine(2, "if (RuntimeUtils.matchStack4Constr(root.promotedEvents, run)) {");
        outLine(3, "root.promotedEvents.pop();");
        outLine(3, "Object ret = proceed(run);");
        outLine(3, "String retTyp = run.getName();");
        genVarBing4Prom();

        outLine(3, "PromotedResult promRes = new PromotedResult(thisJoinPoint, run, ret);");
        outLine(3, "root.queryAction(promRes);");
        outLine(3, "return ret;");
        outLine(2, "}");
        outLine(2, "else");
        outLine(3, "return proceed(run);");
        outLine(1, "}\n");
    }

    private void genVarBing4Prom() {
        Set<String> set = prmResPtCut.keySet();
        int i = 0;
        for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
            String varName = it.next();//PoCoUtils.getMethodSignature(it.next());
            bindingVars = prmResPtCut.get(varName);
            if (bindingVars.size() > 0) {
                String str = (String) bindingVars.keySet().toArray()[0];
                outLine(3, "if(RuntimeUtils.matchSig(\"" + PoCoUtils.getMethodSignature(varName) + "\", run)){");
                outLine(4, "DataWH.updateTyeVal(\"" + str + "\",retTyp, ret);", i++);
                outLine(3, "}");
            }
        }
    }

    private void handleSigVar(ArrayList<String> handleSig, int indentLevel) {
        //handle vars in order to generate correct method signatures info, which includes the variable infos
        if (handleSig != null && handleSig.size() > 0) {
            for (String str : handleSig)
                outLine(indentLevel, str);
        }
    }

    private void outLine(int indent, String text, Object... args) {
        outPartial(indent, text, args);
        outPartial(-1, "\n");
    }

    private void outPartial(int indent, String text, Object... args) {
        if (indent >= 0) {
            int trueIndent = (indent + indentLevel) * 4;
            for (int i = 0; i < trueIndent; i++)
                out.format(" ");
        }
        out.format(text, args);
    }

    private String loadValFrmClosure(String varName) {
        if (closure != null && closure.loadFrmFunctions(varName) != null)
            return closure.loadFrmFunctions(varName).getVarContext();
        else if (closure != null && closure.loadFrmVars(varName) != null)
            return closure.loadFrmVars(varName).getVarContext();
        else
            return varName;
    }

    private String[] deleteExtraComma(String[] argStrs) {
        for (int i = 0; i < argStrs.length; i++)
            argStrs[i] = PoCoUtils.trimEndPunc(argStrs[i], ",");

        return argStrs;
    }

    private boolean isPrimitiveType(String varType) {
        switch (varType) {
            // primitive types
            case "byte":
            case "int":
            case "short":
            case "long":
            case "double":
            case "float":
            case "boolean":
            case "char":
            case "String":
            case "java.lang.String":
                return true;
            // Non-primitive types
            default:
                return false;
        }
    }
}