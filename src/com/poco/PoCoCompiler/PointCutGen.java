package com.poco.PoCoCompiler;

import com.poco.Extractor.Closure;
import com.poco.Extractor.PointCutExtractor;
import com.poco.Extractor.PolicyTreeNode;
import com.sun.xml.internal.ws.api.ha.StickyFeature;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.*;

public class PointCutGen {
    private final int indentLevel;
    private final PrintWriter out;
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
        this.pocoRoot = pcExactor.getRoot() + "root";
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


    /*
        pointcut PointCut1(java.lang.String value0):
            call(java.io.File.new(..,java.lang.String)) && args(*,value0);
        Object around(java.lang.String value0): PointCut1(value0) { ... }
        =============================================================================
        pointcut PointCut1(argStrs[0]):
            call(java.io.File.new(argStrs[3])) && args(argStrs[1]);
        Object around(argStrs[0]): PointCut1(//argStrs[2]) { ... }
    */
    private void genPointCuts(HashMap<String, HashMap<String, String>> target, int mode) {
        Set<String> keys = target.keySet();
        ArrayList<Around4Act> around4Acts = new ArrayList<>();

        for (Iterator<String> key = keys.iterator(); key.hasNext(); ) {
            String entry = key.next();
            bindingVars = target.get(entry);

            String policyName = getPolicyName(entry);
            entry = removePolicyName(entry);

            if (PoCoUtils.getMtdName(entry).startsWith("abs_")) {
                Hashtable<String, String> argVal4Match = null;
                ArrayList<String> concreteMtds = new ArrayList<>();
                HashSet<String> donelist = new HashSet<>();
                //if policy specified that only match a method when a particular parameter has certain value,
                //then advice should only monitor the method when this parameter's value matches
                Hashtable<String, String> varsNeed2Bind4Act = new Hashtable<>();
                Hashtable<String, String> varsNeed2Bind4Res = new Hashtable<>();

                concreteMtds = AbstractActions.handleAbcCase(entry);
                String absActName = PoCoUtils.getMtdName(entry);

                //handle abs's arg building
                String[] absArgList = PoCoUtils.getArgArray(entry);
                if (absArgList != null) {
                    int count = 0;
                    for (int i = 0; i < absArgList.length; i++) {
                        if (absArgList[i].startsWith("$") && closure.isFunctionsContain(absArgList[i].substring(1)))
                            absArgList[i] = loadValFrmClosure(absArgList[i].substring(1));
                        String argValStr = PoCoUtils.getObjVal(absArgList[i]);
                        String argTyp = PoCoUtils.getObjType(absArgList[i]);
                        if (argValStr != null) {
                            String bindKind = "";
                            if (argValStr.startsWith("$") && bindingVars.containsKey(argValStr.substring(1))) {
                                if (argTyp == null)
                                    argTyp = "java.lang.String value";

                                bindKind = bindingVars.get(argValStr.substring(1)).toString();

                                if (bindKind.equals("result")) {
                                    varsNeed2Bind4Res.put(argTyp + " vals.get(" + count+")", absArgList[i]);
                                } else {
                                    varsNeed2Bind4Act.put(argTyp + " vals.get(" + count+")", absArgList[i]);
                                }
                                donelist.add(argValStr.substring(1));
                            }

                            //store the arg along with the value it need to be matched
                            //the % means it can match any value, in such case, no if condition needed.
                            if (!bindKind.equals("action%")) {
                                if (argVal4Match == null)
                                    argVal4Match = new Hashtable<>();
                                if (PoCoUtils.reContainNotMatch(absArgList[i])) {
                                    argVal4Match.put("!" + argTyp + " vals.get(" + count+")", argValStr);
                                } else {
                                    argVal4Match.put(argTyp + " vals.get(" + count+")", argValStr);
                                }
                            }
                            count++;
                        }
                    }
                }

                //handle the case of binding action signature to a variable,
                //or binding return value to a variable
                if (bindingVars.size() > 0) {
                    Set vars = bindingVars.keySet();
                    for (Iterator<String> it = vars.iterator(); it.hasNext(); ) {
                        String varName = (String) it.next();
                        if (donelist.contains(varName))
                            continue;
                        //binding return value to a variable case
                        if (bindingVars.get(varName).toString().equals("result")) {
                            //varsNeed2Bind4Res.put(PoCoUtils.getMethodRtnTyp(methodSig) + " ret", "$" + varName.toString());
                            varsNeed2Bind4Res.put("Object ret", "$" + varName.toString());
                        } else {  //binding action signature to a variable case
                            varsNeed2Bind4Act.put("java.lang.String evtSig", "sig$" + varName.toString());
                        }
                    }
                }

                for (String concreteMtd : concreteMtds) {
                    ArrayList<String> argStrs0 = new ArrayList<>();
                    ArrayList<String> argStrs1 = new ArrayList<>();
                    ArrayList<String> argStrs2 = new ArrayList<>();
                    ArrayList<String> argStrs3 = new ArrayList<>();

                    String methodName = PoCoUtils.getMtdNmInfo(concreteMtd);
                    String[] argsList = PoCoUtils.getArgArray(concreteMtd);
                    // concentrate method's arglist
                    if (argsList != null) {
                        int count = 0;
                        for (int i = 0; i < argsList.length; i++) {
                            if (PoCoUtils.isPoCoObject(argsList[i])) {
                                String argTyp = PoCoUtils.getObjType(argsList[i]);
                                argStrs3.add(argTyp);
                                argStrs0.add(argTyp + " value" + count);
                                argStrs1.add("value" + count);
                                argStrs2.add("value" + count);
                                count++;
                            } else if (argsList[i].trim().equals("..")) {
                                argStrs1.add("..");
                                argStrs3.add("..");
                            } else {
                                argStrs1.add("*");
                                argStrs3.add(argsList[i]);
                            }
                        }
                    }

                    String[] argStrs = new String[]{arr2Str(argStrs0), arr2Str(argStrs1), arr2Str(argStrs2), arr2Str(argStrs3)};

                    //code gen for this pointcut
                    String methodSig = codeGen4PointCutDef(argStrs, methodName);

                    //generate code for action adivce
                    if (mode == 0) {
                        around4Acts.add(genAdvice4Actions("PointCut" + pointcutNum++, argStrs, varsNeed2Bind4Act, argVal4Match, absActName, policyName));
                    } else if (mode == 1) //generate code for action adivce
                        genAdvice4Results("PointCut" + pointcutNum++, argStrs, varsNeed2Bind4Res, argVal4Match, absActName);
                    else
                        genAdvice4Events("PointCut" + pointcutNum++, argStrs, varsNeed2Bind4Act, varsNeed2Bind4Res, argVal4Match, absActName);
                    bindingVars = new HashMap<String, String>();
                }

            } else {
                HashSet<String> donelist = new HashSet<>();

                ArrayList<String> argStrs0 = new ArrayList<>();
                ArrayList<String> argStrs1 = new ArrayList<>();
                ArrayList<String> argStrs2 = new ArrayList<>();
                ArrayList<String> argStrs3 = new ArrayList<>();

                //if policy specified that only match a method when a particular parameter has certain value,
                //then advice should only monitor the method when this parameter's value matches
                Hashtable<String, String> varsNeed2Bind4Act = new Hashtable<>();
                Hashtable<String, String> varsNeed2Bind4Res = new Hashtable<>();

                Hashtable<String, String> argVal4Match = null;

                //Step 1: get name, return type of the method that needs to be monitored.
                String methodName = PoCoUtils.getMtdNmInfo(entry);
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

                        String argValStr = PoCoUtils.getObjVal(argsList[i]);
                        String argTyp = PoCoUtils.getObjType(argsList[i]);
                        if (argValStr != null) {
                            String bindKind = "";
                            if (argValStr.startsWith("$") && bindingVars.containsKey(argValStr.substring(1))) {
                                if (argTyp == null)
                                    argTyp = "java.lang.String value";

                                bindKind = bindingVars.get(argValStr.substring(1)).toString();

                                if (bindKind.equals("result")) {
                                    //put info in monitorVal
                                    varsNeed2Bind4Res.put(argTyp + " value" + count, argsList[i]);
                                } else {
                                    //action or sig case, which means that the binding happens before allowing proceeding
                                    varsNeed2Bind4Act.put(argTyp + " value" + count, argsList[i]);
                                }
                                donelist.add(argValStr.substring(1));
                            }

                            //store the arg along with the value it need to be matched
                            //the % means it can match any value, in such case, no if condition needed.
                            if (!bindKind.equals("action%")) {
                                //System.out.println("argTyp: " + argTyp + "; arg: " + argValStr + "; bindKind: "+bindKind);
                                if (argVal4Match == null)
                                    argVal4Match = new Hashtable<>();
                                if (PoCoUtils.reContainNotMatch(argsList[i])) {
                                    argVal4Match.put("!" + argTyp + " value" + count, argValStr);
                                } else {
                                    argVal4Match.put(argTyp + " value" + count, argValStr);
                                }
                            }

                            //generate the correct argument String for aspectj advice
                            argStrs3.add(argTyp);
                            argStrs0.add(argTyp + " value" + count);
                            argStrs1.add("value" + count);
                            argStrs2.add("value" + count);
                            count++;
                        } else if (argsList[i].trim().equals("..")) {
                            argStrs1.add("..");
                            argStrs3.add("..");
                        } else {
                            argStrs1.add("*");
                            argStrs3.add(argsList[i]);
                        }
                    }
                }

                String[] argStrs = new String[]{arr2Str(argStrs0), arr2Str(argStrs1), arr2Str(argStrs2), arr2Str(argStrs3)};

                //code gen for this pointcut
                String methodSig = codeGen4PointCutDef(argStrs, methodName);
                //handle the case of binding action signature to a variable,
                //or binding return value to a variable
                if (bindingVars.size() > 0) {
                    Set vars = bindingVars.keySet();
                    for (Iterator<String> it = vars.iterator(); it.hasNext(); ) {
                        String varName = (String) it.next();
                        if (donelist.contains(varName))
                            continue;
                        //binding return value to a variable case
                        if (bindingVars.get(varName).toString().equals("result")) {
                            varsNeed2Bind4Res.put(PoCoUtils.getMethodRtnTyp(methodSig) + " ret", "$" + varName.toString());
                        } else {  //binding action signature to a variable case
                            varsNeed2Bind4Act.put("java.lang.String evtSig", "sig$" + varName.toString());
                        }
                    }
                }

                //generate code for action adivce
                if (mode == 0) {
                    around4Acts.add(genAdvice4Actions("PointCut" + pointcutNum++, argStrs, varsNeed2Bind4Act, argVal4Match, null, policyName));
                } else if (mode == 1) //generate code for action adivce
                    genAdvice4Results("PointCut" + pointcutNum++, argStrs, varsNeed2Bind4Res, argVal4Match, null);
                else
                    genAdvice4Events("PointCut" + pointcutNum++, argStrs, varsNeed2Bind4Act, varsNeed2Bind4Res, argVal4Match, null);
                bindingVars = new HashMap<String, String>();
            }

        }

        //new generate around for all action pointcut
        for (Around4Act act: around4Acts ) {
            genAround(act.getArg4Advice(), act.getPointCutName(), act.getArg4PointCut(), act.getAbstractAct());
        }
    }

    private String removePolicyName(String entry) {
        assert entry != null && entry.trim().length() > 0;
        int rIndex = entry.indexOf('>', 0);
        return entry.substring(rIndex + 1, entry.length());
    }


    private String getPolicyName(String entry) {
        assert entry != null && entry.trim().length() > 0;
        int lIndex = entry.indexOf('<', 0);
        int rIndex = entry.indexOf('>', 0);
        return entry.substring(lIndex + 1, rIndex);
    }


    private String codeGen4PointCutDef(String[] argStrs, String methodName) {
        //if there are some parameters of the method need to be monitored
        String callStr = "";
        if (argStrs[2].length() > 0) {
            outLine(1, "pointcut PointCut%d(%s):", pointcutNum, argStrs[0]);
            callStr = (methodName + "(" + argStrs[3] + ")").replace("\\", "");
            // not the abstract action case
            if (argStrs[3].trim().length() > 0)
                outLine(2, "call(%s) && args(%s);\n", callStr, argStrs[1]);
            else
                outLine(2, "call(%s);\n", callStr);
        } else {

            callStr = methodName.replace("\\", "");
            outLine(1, "pointcut PointCut%d():", pointcutNum);

            if (argStrs[3].trim().length() > 0)
                outLine(2, "call(" + callStr + "(" + argStrs[3] + "));\n");
            else
                outLine(2, "call(" + callStr + "())\";\n");
        }
        return callStr;
    }

    private Around4Act genAdvice4Actions(String pointcutName, String[] argStrs, Hashtable varsNeed2Bind, Hashtable argVal4Match, String isAbsCase, String PolicyName) {
        //if it is conditional match or need dynamically bind value to variables
        if (varsNeed2Bind != null || argVal4Match != null) {
            outLine(1, "before(%s): %s(%s) {", argStrs[0], pointcutName, argStrs[2]);

            //if there are arguments this pointcut is monitoring
            if (argStrs[2] != null && argStrs[2].length() > 0) {
                //it is the abstraction action case, then need load the parameters
                if (isAbsCase != null)
                    outLine(2, "ArrayList<TypeVal> vals = AbsActions." + isAbsCase + "(thisJoinPoint," + argStrs[2] + ");");
                else
                    outLine(2, "ArrayList<TypeVal> vals = RuntimeUtils.getVals(\"" + getParaList(argStrs[0]) + "\"," + argStrs[2] + ");");
            }

            //generate conditional statement for conditional monitoring case
            String conditionState = genCoditionStatements(argVal4Match, isAbsCase);
            //if is the conditional monitoring case
            if (conditionState != null && conditionState.length() > 0) {
                outLine(2, "if (" + conditionState + ") {");
                outLine(3, "String[] varNames = null;");
                outLine(3, "DataWH dh = new DataWH();");

                //in order to generate the correct method signature that includes the variable info,
                //need dynamic genearate variable information
                //handleSigVar(handleSig, 3);

                if (argStrs[2] != null && argStrs[2].trim().length() > 0) {
                    outLine(3, "Object[] objs = new Object[]{" + argStrs[2] + "};");

                    if (isAbsCase == null)
                        outLine(3, "String evtSig = RuntimeUtils.genSigFrmJP(thisJoinPoint, \"" + argStrs[3] + "\", objs, varNames);");
                    else
                        outLine(3, "String evtSig = AbsActUtils.genAbsSig(\"" + isAbsCase + "\",vals);");
                    valueBind4Advices(varsNeed2Bind, 3);
                    outLine(3, "dh.addTypVal(\"" + PolicyName + "_evtSig\", \"java.lang.String\",evtSig);");
                } else {
                    valueBind4Advices(varsNeed2Bind, 3);
                }
                outLine(3, "RuntimeUtils.UpdatePolicyVars(dh, " + pocoRoot + ");");
                outLine(2, "} ");

            } else {
                //handle vars in order to generate correct method signatures info, which includes the variable infos
                //handleSigVar(handleSig, 2);
                if (argStrs[2] != null && argStrs[2].trim().length() > 0) {
                    outLine(2, "String[] varNames = null;");
                    outLine(2, "Object[] objs = new Object[]{" + argStrs[2] + "};");

                    if (isAbsCase == null)
                        outLine(2, "String evtSig = RuntimeUtils.genSigFrmJP(thisJoinPoint, \"" + argStrs[3] + "\", objs, varNames);");
                    else
                        outLine(2, "String evtSig = AbsActUtils.genAbsSig(\"" + isAbsCase + "\",vals);");

                    valueBind4Advices(varsNeed2Bind, 2);
                    outLine(2, "dh.addTypVal(\"" + PolicyName + "_evtSig\", \"java.lang.String\",evtSig);");
                    outLine(2, "RuntimeUtils.UpdatePolicyVars(dh, " + pocoRoot + ");");
                } else {
                    valueBind4Advices(varsNeed2Bind, 3);
                }
            }
            outLine(1, "}\n");
        }
        return new Around4Act(argStrs[0], pointcutName, argStrs[2], isAbsCase);
    }

    private void genAround(String arg1, String ptName, String arg2, String isAbsCase) {
        if (arg1 != null)
            outLine(1, "Object around(%s): %s(%s) {", arg1, ptName, arg2);
        else
            outLine(1, "Object around(): %s() {", ptName);
        genAround4Actions(isAbsCase, 2, arg2);
    }

    private void genAround4Actions(String isAbsCase, int indentLevel, String vals) {
        if (isAbsCase == null)
            outLine(indentLevel, pocoRoot + ".queryAction(new Action(thisJoinPoint));");
        else {
            if (vals == null || vals.trim().length() == 0)
                outLine(indentLevel, pocoRoot + ".queryAction(new Action(\"" + isAbsCase + "()\"));");
            else {
                outLine(indentLevel, "ArrayList<TypeVal> vals = AbsActions." + isAbsCase + "(thisJoinPoint," + vals + ");");
                outLine(indentLevel, pocoRoot + ".queryAction(new Action(AbsActUtils.genAbsSig(\"" + isAbsCase + "\",vals)));");
            }
        }
        outLine(indentLevel, "if(" + pocoRoot + ".hasRes4Action()) {");
        outLine(indentLevel + 1, "return " + pocoRoot + ".getRes4Action();");
        outLine(indentLevel, "} else");
        if (vals != null && vals.length() > 0)
            outLine(indentLevel + 1, "return proceed(%s);", vals);
        else
            outLine(indentLevel + 1, "return proceed();");

        outLine(indentLevel - 1, "}\n");
    }


    private String getParaList(String argStr) {
        String[] paraList = argStr.split(",");
        for (int i = 0; i < paraList.length; i++)
            paraList[i] = paraList[i].split("\\s+")[0];

        return Arrays.toString(paraList).replace("[", "").replace("]", "").replace(" ", "");
    }

    private void genAdvice4Results(String pointcutName, String[] argStrs, Hashtable varsNeed2Bind, Hashtable argVal4Match, String isAbsCase) {
        if (varsNeed2Bind != null || argVal4Match != null) {
            outLine(1, "Object around(%s): %s(%s) {", argStrs[0], pointcutName, argStrs[2]);

            //generate conditional statement for conditional monitoring case
            String conditionState = genCoditionStatements(argVal4Match, isAbsCase);

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
                //handleSigVar(handleSig, 2);
                outLine(2, "Object ret = proceed(%s);", argStrs[2]);
                //valueBind4Advices(varsNeed2Bind, 2);
                genEvent4queryAction(2);
            }
        } else {
            outLine(1, "Object around(): %s() {", pointcutName);
            //handleSigVar(handleSig, 2);
            outLine(2, "Object ret = proceed();", argStrs[2]);
            //valueBind4Advices(varsNeed2Bind, 2);
            genEvent4queryAction(2);
        }

        outLine(1, "}\n");
    }

    private void genAdvice4Events(String pointcutName, String[] argStrs, Hashtable varsNeed2Bind4Act, Hashtable varsNeed2Bind4Res, Hashtable argVal4Match, String isAbsCase) {
        if (varsNeed2Bind4Act != null || varsNeed2Bind4Res != null || argVal4Match != null) {
            outLine(1, "Object around(%s): %s(%s) {", argStrs[0], pointcutName, argStrs[2]);
            //generate conditional statement for conditional monitoring case
            String conditionState = genCoditionStatements(argVal4Match, isAbsCase);
            //if is the conditional monitoring case
            if (conditionState != null && conditionState.length() > 0) {
                outLine(2, "if (" + conditionState + ") {");
                outLine(3, "String[] varNames = null;");
                //handleSigVar(handleSig, 3);
                valueBind4Advices(varsNeed2Bind4Act, 3);
                if (argStrs[2] != null && argStrs[2].trim().length() > 0) {
                    //outLine(3, "Object[] objs = new Object[]{" + argStrs[2] + "};");

                    if (isAbsCase == null)
                        outLine(3, "String evtSig = RuntimeUtils.genSigFrmJP(thisJoinPoint, \"" + argStrs[3] + "\", objs, varNames);");
                    else
                        outLine(3, "String evtSig = AbsActUtils.genAbsSig(\"" + isAbsCase + "\",vals);");

                    outLine(3, pocoRoot + ".queryAction(new Action(evtSig));");
                } else {
                    outLine(3, pocoRoot + ".queryAction(new Action(thisJoinPoint));");
                }
                outLine(3, "Object ret = proceed(%s);", argStrs[2]);
                valueBind4Advices(varsNeed2Bind4Res, 3);
                //generate code for create Event Object,which will be used for policy query action
                genEvent4queryAction(3);
                outLine(2, "}");
                outLine(2, "else");
                outLine(3, "return proceed(%s);", argStrs[2]);
            } else {
                outLine(2, "String[] varNames = null;");
                valueBind4Advices(varsNeed2Bind4Act, 2);
                //handleSigVar(handleSig, 2);
                if (argStrs[2] != null && argStrs[2].trim().length() > 0) {
                    //outLine(2, "Object[] objs = new Object[]{" + argStrs[2] + "};");

                    if (isAbsCase == null)
                        outLine(2, "String evtSig = RuntimeUtils.genSigFrmJP(thisJoinPoint, \"" + argStrs[3] + "\", objs, varNames);");
                    else
                        outLine(2, "String evtSig = AbsActUtils.genAbsSig(\"" + isAbsCase + "\",vals);");

                    outLine(2, pocoRoot + ".queryAction(new Action(evtSig));");
                } else {
                    outLine(2, pocoRoot + ".queryAction(new Action(thisJoinPoint));");
                }
                outLine(2, "Object ret = proceed(%s);", argStrs[2]);
                valueBind4Advices(varsNeed2Bind4Res, 2);
                genEvent4queryAction(2);
            }
        } else {
            outLine(1, "Object around(): %s() {", pointcutName);
            //handleSigVar(handleSig, 2);
            valueBind4Advices(varsNeed2Bind4Act, 2);
            outLine(2, pocoRoot + ".queryAction(new Action(thisJoinPoint));");
            outLine(2, "Object ret = proceed();", argStrs[2]);
            valueBind4Advices(varsNeed2Bind4Res, 2);
            genEvent4queryAction(2);
        }
        outLine(1, "}\n");
    }

    private void genEvent4queryAction(int offset) {
        outLine(offset, "Result result = new Result(thisJoinPoint, ret);");
        outLine(offset, pocoRoot + ".queryAction(result);");
        outLine(offset, "return result.getResult();");
    }

    private String genCoditionStatements(Hashtable<String, String> argVal4Match, String isAbsCase) {
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

        ArrayList<String> conditions = new ArrayList<>();
        for (i = 0; i < varTyps.length; i++) {
            //need handle the case where we do not care about the type and value
            if (varVals[i] == null) continue;
            String matchState;
            if (PoCoUtils.isVariable(varVals[i]))
                matchState = genCondStatWVar(varTyps[i], varNams[i], varVals[i], isAbsCase, i);
            else
                matchState = genCondStatWVal(varTyps[i], varNams[i], varVals[i], isAbsCase, i);

            if (matchState != null)
                conditions.add(matchState);
        }

        if (conditions.size() > 0)
            return conditions.toString().replace("[", "").replace("]", "").replace(", ", "&&");
        else
            return null;
    }

    private String genCondStatWVal(String type, String valName, String matchVal, String isAbsCase, int index) {
        return genCoditionStatement(type, valName, matchVal, 0, isAbsCase, index);
    }

    private String genCondStatWVar(String type, String valName, String matchVal, String isAbsCase, int index) {
        return genCoditionStatement(type, valName, matchVal, 1, isAbsCase, index);
    }

    /**
     * @param type
     * @param valName
     * @param matchVal
     * @param mode     0: the val is value, 1: the val is variable
     * @return
     */
    private String genCoditionStatement(String type, String valName, String matchVal, int mode, String isAbsCase, int index) {
        if (matchVal != null && matchVal.length() > 0) {
            boolean isNotMatch = type.startsWith("!");
            if (isNotMatch)
                type = type.substring(1);
            matchVal = matchVal.replace("%", "*");

            String str = genValueofStr(type, valName);
            if (PoCoUtils.isPoCoObject(matchVal))
                matchVal = PoCoUtils.getObjVal(matchVal);

            String retStr = "RuntimeUtils.valMatch(vals.get(" + index + "),\"" + matchVal.replace("\\", "") + "\")";
//            if (isAbsCase == null)
//                retStr = "RuntimeUtils.valMatch(" + str + ", \"" + matchVal.replace("\\", "") + "\")";
//            else
//                retStr = "AbsActUtils.valMatch(vals.get(" + index + "), \"" + matchVal.replace("\\", "") + "\")";

            if (isNotMatch)
                retStr = "!" + retStr;

            return retStr;


//            if (isPrimitiveType(type)) {
//                String str = genValueofStr(type, valName);
//
//                if (PoCoUtils.isPoCoObject(matchVal))
//                    matchVal = PoCoUtils.getObjVal(matchVal);
//
//                String retStr = "";
//                if (isAbsCase == null)
//                    retStr = " RuntimeUtils.strValMatch(" + str + ", \"" + matchVal.replace("\\", "") + "\")";
//                else
//                    retStr =  "RuntimeUtils.strValMatch(AbsActions." + isAbsCase + "(thisJoinPoint," + str + "), \"" + matchVal.replace("\\", "") + "\")";
//
//                if (isNotMatch)
//                    retStr = "!" + retStr;
//                return retStr;
//            } else {
//                if (matchVal.startsWith("$") && closure.isVarsContain(matchVal.substring(1))) {
//                    matchVal = "RuntimeUtils.getObjFrmDbWH(\"" + matchVal.substring(1) + "\")";
//
//                    //not abstract action case
//                    if (isAbsCase == null)
//                        return "RuntimeUtils.objMatch(" + valName + ", " + matchVal + ")";
//                    else
//                        return "RuntimeUtils.objMatch(AbsActions." + isAbsCase + "(thisJoinPoint," + valName + ")," + matchVal + ")";
//                } else {
//                    if (isAbsCase == null)
//                        return "RuntimeUtils.objMatch(" + valName + ", \"" + matchVal + "\")";
//                    else
//                        return "RuntimeUtils.objMatch(AbsActions." + isAbsCase + "(thisJoinPoint," + valName + "), \"" + matchVal + "\")";
//                }
//            }
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
            ArrayList<String> sb4NamList = new ArrayList<>();
            int setSize = set.size();

            int index = 0;
            for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
                String argName = it.next();
                String varName = varsNeed2Bind.get(argName).toString();

                //if it is sig binding case, it will not be used for this query
                if (varName.startsWith("sig$")) {
                    varName = varName.substring(4);
                } else {
                    if (PoCoUtils.isPoCoObject(varName)) {
                        sb4NamList.add(varName.substring(1));
                        varName = PoCoUtils.getObjVal(varName);
                    }
                    varName = varName.substring(1);
                }

                //typVal[0] will be the new type  of the variable and
                //typVal[1] will be the new value of the variable
                String[] typVal = argName.split("\\s+");
                typVal[1] = typVal[1].replace("..", "*");
                outLine(offset, "dh.addTypVal(\"" + varName + "\", \"" + typVal[0] + "\", " + typVal[1] + ");");
            }
            if (sb4NamList.size() > 0)
                outLine(offset, "varNames = new String[] {\"" + arr2Str(sb4NamList) + "\"};");
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
        outLine(2, "if (RuntimeUtils.matchingStack(" + pocoRoot + ".promotedEvents,run)) {");
        outLine(3, pocoRoot + ".promotedEvents.pop();");
        outLine(3, "Object ret = proceed(run);");
        outLine(3, "String retTyp = RuntimeUtils.trimClassName(ret.getClass().toString());");
        genVarBing4Prom();
        outLine(3, "PromotedResult promRes = new PromotedResult(run,ret);");
        outLine(3, pocoRoot + ".queryAction(promRes);");
        outLine(3, "return ret;");
        outLine(2, "}");
        outLine(2, "else");
        outLine(3, "return proceed(run);");
        outLine(1, "}\n");
    }

    private void outAdviceInvokeConstructor(String pointcutName) {
        outLine(1, "Object around(Constructor run): %s(run) {", pointcutName);
        outLine(2, "if (RuntimeUtils.matchStack4Constr(" + pocoRoot + ".promotedEvents, run)) {");
        outLine(3, pocoRoot + ".promotedEvents.pop();");
        outLine(3, "Object ret = proceed(run);");
        outLine(3, "String retTyp = run.getName();");
        genVarBing4Prom();

        outLine(3, "PromotedResult promRes = new PromotedResult(run, ret);");
        outLine(3, pocoRoot + ".queryAction(promRes);");
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
                outLine(4, "dh.addTypVal(\"" + str + "\",retTyp, ret);", i++);
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

    private String arr2Str(ArrayList<String> strs) {
        assert strs != null;

        return strs.toString().replace("[", "").replace("]", "").replace(", ", ",");
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