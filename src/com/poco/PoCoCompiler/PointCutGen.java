package com.poco.PoCoCompiler;

import com.poco.Extractor.Closure;
import com.poco.Extractor.VarTypeVal;

import java.io.PrintWriter;
import java.util.*;

public class PointCutGen {
    private final int indentLevel;
    private final PrintWriter out;
    private String aspectName;
    private String policyName;

    private Closure closure;
    private HashMap<String, String> bindingVars;
    //add this in order to generate the different kinds of advices for pointcuts
    private HashMap<String, HashMap<String, String>> actionPtCut;
    private HashMap<String, HashMap<String, String>> resultPtCut;
    private HashMap<String, HashMap<String, String>> prmResPtCut;
    private HashMap<String, HashMap<String, String>> actResPtCut;
    private HashMap<String, String> objParams;
    private int pointcutNum = 0;


    public void setObjParams(HashMap<String, String> objParams) {
        this.objParams = objParams;
    }

    public PointCutGen(PrintWriter out, String policyName, int indentLevel, Closure closure,
                       HashMap actionPCs, HashMap resPCs, HashMap promResPCs) {
        this.out = out;
        this.policyName = policyName;
        this.aspectName = "Aspect" + policyName;
        this.indentLevel = indentLevel;
        this.closure = closure;
        this.actionPtCut = actionPCs;

        this.resultPtCut = resPCs;
        this.prmResPtCut = promResPCs;
        this.actResPtCut = new HashMap<String, HashMap<String, String>>();
    }

    public void GenAspectJ() {
        //step 1: gen aspectj prologue.
        outAspectPrologue();

        //step 2: gen DataHW for storing dynamic binding variables.
        genDataHW();

        //step 3: add pointcut for monitoring reflection events, only allow poco to do so
        //genPt4Reflect();

        //step 4: check the action and result pointcuts sets, if a method needs to be monitored
        //both before and after proceed, then we will need to generate 2 advices for this method
        checkCommonActResPCs();

        //step 5: generate advice for not-promoted action
        genPointCut4Actions();

        //step 6: generate advice for not-promoted result
        genPointCut4Results();

        //step 7: generate advices for those methods that need monitor both before and after proceed
        genPointCut4Events();

        //setp 7: generate advice for promoted action
        genAdvice4PromotedActions();

    }

    private void outAspectPrologue() {
        outLine(0, "import com.poco.PoCoRuntime.*;");
        outLine(0, "import java.lang.reflect.Method;\n");
        outLine(0, "public aspect %s {", aspectName);
        outLine(1, "private DummyRootPolicy root = new DummyRootPolicy( new %s() );\n", policyName);
    }

    private void genDataHW() {
        if (objParams != null && objParams.size() > 0) {
            outLine(1, "public " + aspectName + "() {");

            for (String key : objParams.keySet()) {
                String str = "";
                String varContext = "";
                VarTypeVal varTypeVal = (VarTypeVal) closure.getVars().get(key);

                //if this variable is used to save an method signature
                if (objParams.get(key).toString().equals("action")) {
                    if (varTypeVal != null)
                        varContext = varTypeVal.getVarContext();
                    if (varContext == null || varContext.equals("%"))
                        varContext = "*";
                    str = genTypValStr("java.lang.String", PoCoUtils.validateStr(varContext));
                } else { //variable will be used to store an object
                    String varType = null;
                    if (varTypeVal != null) {
                        varType = varTypeVal.getVarType();
                        varContext = varTypeVal.getVarContext();
                    }
                    if (varType == null || varType.equals(""))
                        varType = "java.lang.String";
                    if (varContext == null || varContext.equals("%")) {
                        if (varType.equals("java.lang.String"))
                            varContext = "*";
                        else
                            varContext = "null";
                    }
                    str = genTypValStr(varType, PoCoUtils.validateStr(varContext));
                }
                outLine(2, "DataWH.dataVal.put(\"" + key + "\"," + str.replace("%", "*"));
            }
        }
        outLine(1, "}\n");
    }

    /**
     * moved to common aspectj file
     */
    /*private void genPt4Reflect() {
        outLine(1, "pointcut PC4Reflection():");
        outLine(2, "call (* Method.invoke(Object, Object...)) && !within(com.poco.Promoter);\n");
        outLine(1, "Object around(): PC4Reflection()   { ");
        outLine(2, "return new SRE(null,\".\"); ");
        outLine(1, "}\n");
    }*/

    /**
     * if an method needs to be monitored both before and after proceed,
     * then two advices needs to generated
     */
    private void checkCommonActResPCs() {
        if (actionPtCut.keySet().size() > 0 && resultPtCut.keySet().size()>0) {
            HashMap<String, HashMap<String, String>> temp4actionPC = new HashMap<>();

            Set<String> keys = actionPtCut.keySet();
            for (Iterator<String> key = keys.iterator(); key.hasNext(); ) {
                String entry = key.next();
                if (resultPtCut.containsKey(entry)) {
                    bindingVars = resultPtCut.get(entry);
                    bindingVars.putAll(actionPtCut.get(entry));
                    resultPtCut.remove(entry);
                    actResPtCut.put(entry, bindingVars);
                }
                else
                    temp4actionPC.put(entry,actionPtCut.get(entry));

                if(resultPtCut.keySet().size()==0) break;
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
            String[] argStrs = new String[]{"", "", "", ""};

            //if policy specified that only match a method when a particular parameter has certain value,
            //then advice should only monitor the method when this parameter's value matches
            Hashtable<String, String> varsNeed2Bind = null;
            Hashtable<String, String> argVal4Match = null;

            //Step 1: get name, return type of the method that needs to be monitored.
            String methodName = PoCoUtils.getMethodInfo(entry, 1);


            //Step 2: get parameters info of the method that needs to be monitored.
            // a. 1st check the parameter object is a variable or not, and load info from the closure if so;
            // b. 2nd check the parameter's value is a variable or not, if it is a variable, then need check
            //    if this variable need to be bound in this advice.

            String[] argsList = PoCoUtils.getArgArray(entry);
            if (argsList != null) {
                int count = 0;
                for (int i = 0; i < argsList.length; i++) {
                    // a. 1st check the parameter object is a variable or not, and load info from the closure if so;
                    if (PoCoUtils.isVariable(argsList[i]))
                        argsList[i] = loadValFrmClosure(argsList[i].substring(1));

                    // b. 2nd check the parameter's value is a variable or not to generate the correct argument
                    //    String for aspectj advice
                    String argVal = PoCoUtils.getObjVal(argsList[i]);
                    if (argVal != null) {
                        String argTyp = PoCoUtils.getObjType(argsList[i]);
                        //if it is a variable
                        //if this variable need to be bound in this advice, need save the binding info
                        if (PoCoUtils.isVariable(argVal) && bindingVars.containsKey(argVal.substring(1))) {
                            //put info in monitorVal
                            if (varsNeed2Bind == null)
                                varsNeed2Bind = new Hashtable<>();
                            varsNeed2Bind.put(" value" + count, argVal);
                            bindingVars.remove(argVal.substring(1));
                        }
                        //store the arg along with the value it need to be matched
                        if (argVal4Match == null)
                            argVal4Match = new Hashtable<>();
                        argVal4Match.put(argTyp + " value" + count, argVal);
                        //generate the correct argument String for aspectj advice
                        genArgs4PTs(argStrs, argsList.length, count++, i, argTyp);
                    } else {
                        argStrs[3] += "..";
                        argStrs[1] += "*";
                        if (i != argsList.length - 1) {
                            argStrs[3] += ",";
                            argStrs[1] += ",";
                        }
                    }
                }
                //need delete extra comma in the end of each string
                argStrs = deleteExtraComma(argStrs);
            }
            //code gen for this pointcut
            String methodSig = codeGen4PointCutDef(argStrs, methodName);


            if(bindingVars.size() >0) {
                if (varsNeed2Bind == null)
                    varsNeed2Bind = new Hashtable<>();
                Set vars = bindingVars.keySet();
                for(Iterator<String> it = vars.iterator();it.hasNext(); ) {
                    String varName = (String) it.next();
                    varsNeed2Bind.put("\""+methodSig+ "\"", "$"+varName.toString());
                }
            }

            //generate code for action adivce
            if (mode == 0)
                genAdvice4Actions("PointCut" + pointcutNum++, argStrs, varsNeed2Bind, argVal4Match);
            else if (mode ==1) //generate code for action adivce
                genAdvice4Results("PointCut" + pointcutNum++, argStrs, varsNeed2Bind, argVal4Match);
            else
                genAdvice4Events("PointCut" + pointcutNum++, argStrs, varsNeed2Bind, argVal4Match);
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

    private String[] genArgs4PTs(String[] argStrs, int argCount, int count, int i, String argTyp) {
        argStrs[3] += argTyp;
        argStrs[0] += argTyp + " value" + count;
        argStrs[1] += "value" + count;
        argStrs[2] += "value" + count;
        if (i != argCount - 1) {
            argStrs[0] += ",";
            argStrs[1] += ",";
            argStrs[2] += ",";
            argStrs[3] += ",";
        }
        return argStrs;
    }

    private void genAdvice4Actions(String pointcutName, String[] argStrs, Hashtable varsNeed2Bind, Hashtable argVal4Match) {
        //if it is conditional match or need dynamically bind value to variables
        if (varsNeed2Bind != null || argVal4Match != null) {
            outLine(1, "Object around(%s): %s(%s) {", argStrs[0], pointcutName, argStrs[2]);

            //generate conditional statement for conditional monitoring case
            String conditionState = genCoditionStatements(argVal4Match);
            //if is the conditional monitoring case
            if (conditionState != null && conditionState.length() > 0) {
                outLine(2, "if (" + conditionState + ") {");
                valueBind4Advices(varsNeed2Bind, 3);
                outLine(3, "root.queryAction(new Event(thisJoinPoint));");
                outLine(3, "return proceed(%s);", argStrs[2]);
                outLine(2, "} else");
                outLine(3, "return proceed(%s);", argStrs[2]);
            } else {
                valueBind4Advices(varsNeed2Bind, 2);
                outLine(2, "root.queryAction(new Event(thisJoinPoint));");
                outLine(2, "return proceed(%s);", argStrs[2]);
            }
        }
        else {
            outLine(1, "Object around(): %s() {", pointcutName);
            outLine(2, "root.queryAction(new Event(thisJoinPoint));");
            outLine(2, "return proceed();");
        }
        outLine(1, "}\n");
    }

    private void genAdvice4Results(String pointcutName, String[] argStrs, Hashtable varsNeed2Bind, Hashtable argVal4Match) {
        if (varsNeed2Bind != null || argVal4Match != null) {
            outLine(1, "Object around(%s): %s(%s) {", argStrs[0], pointcutName, argStrs[2]);
            outLine(2, "Object ret = null;");

            //generate conditional statement for conditional monitoring case
            String conditionState = genCoditionStatements(argVal4Match);
            //if is the conditional monitoring case
            if (conditionState != null && conditionState.length() > 0) {
                outLine(2, "if (" + conditionState + ") {");
                outLine(3, "ret = proceed(%s);", argStrs[2]);
                valueBind4Advices(varsNeed2Bind, 3);

                //generate code for create Event Object,which will be used for policy query action
                genEvent4queryAction(3);

                outLine(2, "}");
                outLine(2, "else");
                outLine(3, "return proceed(%s);", argStrs[2]);
            } else {
                outLine(2, "root.queryAction(new Event(thisJoinPoint));");
                valueBind4Advices(varsNeed2Bind, 2);
                outLine(2, "return proceed(%s);", argStrs[2]);
            }
        } else {
            outLine(1, "Object around(): %s() {", pointcutName);
            outLine(2, "Object ret = proceed();", argStrs[2]);
            valueBind4Advices(varsNeed2Bind, 2);
            genEvent4queryAction(2);
        }

        outLine(1, "}\n");
    }

    private void genAdvice4Events(String pointcutName, String[] argStrs, Hashtable varsNeed2Bind, Hashtable argVal4Match) {
        //Step 1: generate result advice for this event
        genAdvice4Results(pointcutName, argStrs, varsNeed2Bind, argVal4Match);

        //Step 2:  generate the before advice to the monitoring method
        if (varsNeed2Bind != null || argVal4Match != null) {
            outLine(1, "before(%s): %s(%s) {", argStrs[0], pointcutName, argStrs[2]);

            //generate conditional statement for conditional monitoring case
            String conditionState = genCoditionStatements(argVal4Match);
            if (conditionState != null && conditionState.length() > 0) {
                outLine(2, "if (" + conditionState + ") {");

                valueBind4Advices(varsNeed2Bind, 3);
                outLine(3, "root.queryAction(new Event(thisJoinPoint));");
                //before will do not proceed the action, so no variable binding for result
                outLine(3, "return;");
                outLine(2, "}");
                outLine(2, "else   return;");
            } else {
                valueBind4Advices(varsNeed2Bind, 2);
                //before will do not proceed the action, so no variable binding for result
                outLine(3, "return;");
            }
            outLine(1, "}\n");
        }else {

            outLine(1, "before(): %s() {", pointcutName);
            valueBind4Advices(varsNeed2Bind, 2);
            outLine(3, "root.queryAction(new Event(thisJoinPoint));");
            //before will do not proceed the action, so no variable binding for result
            outLine(3, "return;");
            outLine(1, "}\n");
        }
    }

    private void genEvent4queryAction(int offset) {
        outLine(offset, "Event event = new Event(thisJoinPoint);");
        outLine(offset, "event.setEventType(\"Result\");");
        //if the monitoring method have no return type
        outLine(offset, "if(RuntimeUtils.hasReturnValue(thisJoinPoint.getSignature().toString()))");
        outLine(offset + 1, "event.setResult(ret);");
        outLine(offset, "else");
        outLine(offset + 1, "event.setResult(\"done\");");
        outLine(offset, "root.queryAction(event);");
        outLine(offset, "return event.getResult();");
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

            String str = genValueofStr(type, valName);

            if (mode == 0)
                return "RuntimeUtils.StringMatch(" + str + ", \"" + matchVal + "\")";
            else //if(mode == 1)
                return "RuntimeUtils.StringMatch(" + str + ", DataWH.dataVal.get(\"" + matchVal.substring(1) + "\"))";
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
            for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
                String argName = it.next();
                String varName = varsNeed2Bind.get(argName).toString().substring(1);
                outLine(offset, "DataWH.updateValue(\"" + varName + "\"," + argName + ");");
            }
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
        outLine(2, "String className = RuntimeUtils.trimClassName(run.getDeclaringClass().toString());");
        outLine(2, "className =RuntimeUtils.concatClsMethod(className, run.getName());");
        outLine(2, "if (RuntimeUtils.matchingStack(root.promotedEvents,className)) {");
        outLine(3, "root.promotedEvents.pop();");
        outLine(3, "Object ret = proceed(run);");
        genVarBing4Prom();
        outLine(3, "PromotedEvent event = new PromotedEvent(thisJoinPoint,RuntimeUtils.getInvokeMethoSig(run),\"Result\",ret);");
        outLine(3, "root.queryAction(event);");
        outLine(3, "return ret;");
        outLine(2, "}");
        outLine(2, "else");
        outLine(3, "return proceed(run);");
        outLine(1, "}\n");
    }

    private void outAdviceInvokeConstructor(String pointcutName) {
        outLine(1, "Object around(Constructor run): %s(run) {", pointcutName);
        outLine(2, "String className = RuntimeUtils.trimClassName(run.getDeclaringClass().toString()) + \".new\";");
        outLine(2, "if (RuntimeUtils.matchingStack(root.promotedEvents,className)) {");
        outLine(3, "root.promotedEvents.pop();");
        outLine(3, "Object ret = proceed(run);");
        //genVarBing4Prom();
        outLine(3, "Event event = new Event(thisJoinPoint);");
        outLine(3, "event.setEventType(\"Result\");");
        outLine(3, "if(run.getParameterCount() >0) {");
        outLine(4, "String argStr = \"\";");
        outLine(4, "Parameter[] paras = run.getParameters();");
        outLine(4, "for(int i = 0; i<paras.length; i++) {");
        outLine(5, "String temp = paras[i].getVarType().toString();");
        outLine(5, "if(temp.startsWith(\"class \"))");
        outLine(6, "argStr += temp.substring(6);");
        outLine(5, "else");
        outLine(6, "argStr += temp;");
        outLine(5, "if(i != paras.length-1)");
        outLine(6, "argStr +=\",\";");
        outLine(4, "}");
        outLine(4, "className += \"(\"+ argStr + \")\";");
        outLine(3, "}");
        outLine(3, "event.setPromotedMethod(className);");
        outLine(3, "event.setResult(ret);");
        outLine(3, "root.queryAction(event);");
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
            String varName = it.next();
            bindingVars = prmResPtCut.get(varName);
            if (bindingVars.size() > 0) {
                String str = (String) bindingVars.keySet().toArray()[0];
                outLine(3, "if(RuntimeUtils.StringMatch(\"" + varName + "\", className)){");
                outLine(4, "DataWH.updateValue(\"" + str + "\", ret);", i++);
                outLine(3, "}");
            }
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

    private String genTypValStr(String varType, String varContext) {
        switch (varType) {
            case "int":
                if (varContext.equals("null"))
                    return "new TypeVal(\"int\",null));";
                else
                    return "new TypeVal(\"int\",new Integer(\"" + varContext + "\")));";
            case "short":
                if (varContext.equals("null"))
                    return "new TypeVal(\"short\",null));";
                else
                    return "new TypeVal(\"short\",new Integer(\"" + varContext + "\")));";
            case "long":
                if (varContext.equals("null"))
                    return "new TypeVal(\"long\",null));";
                else
                    return "new TypeVal(\"long\",new Long(\"" + varContext + "\")));";
            case "double":
                if (varContext.equals("null"))
                    return "new TypeVal(\"double\",null));";
                else
                    return "new TypeVal(\"double\",new Double(\"" + varContext + "\")));";
            case "float":
                if (varContext.equals("null"))
                    return "new TypeVal(\"float\",null));";
                else
                    return "new TypeVal(\"float\",new Float(\"" + varContext + "\")));";
            case "boolean":
                if (varContext.equals("null"))
                    return "new TypeVal(\"boolean\",null));";
                else
                    return "new TypeVal(\"boolean\",new Boolean(\"" + varContext + "\")));";
            case "char":
                if (varContext.equals("null"))
                    return "new TypeVal(\"char\",null));";
                else
                    return "new TypeVal(\"char\",new Character(" + varContext + ".CharAt(0))));";
                //case "Message":
            case "javax.mail.Message":
                if (varContext.equals("null"))
                    return "new TypeVal(\"javax.mail.Message\",null));";
                else
                    return "new TypeVal(\"javax.mail.Message\",  null));";
            default:
                if (varContext.equals("null"))
                    return "new TypeVal(\"java.lang.String\",null));";
                else
                    return "new TypeVal(\"java.lang.String\",\"" + varContext + "\"));";
        }
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
}