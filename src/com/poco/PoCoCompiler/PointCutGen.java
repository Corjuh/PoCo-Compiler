package com.poco.PoCoCompiler;

import com.poco.Extractor.Closure;
import com.poco.Extractor.PointCutExtractor;
import com.poco.Extractor.PolicyTreeNode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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

    /**
     * Generage pointcut and advice for non-promoted action
     */
    private void genPointCuts(HashMap<String, HashMap<String, String>> target, int mode) {

        Set<String> keys = target.keySet();
        for (Iterator<String> key = keys.iterator(); key.hasNext(); ) {
            String entry = key.next();
            bindingVars = target.get(entry);

            String policyName = getPolicyName(entry);
            entry = removePolicyName(entry);

            ArrayList<String> concreteMtds = new ArrayList<>();
            String absActName = null;
            if (PoCoUtils.getMtdName(entry).startsWith("abs_")) {
                concreteMtds = AbstractActions.handleAbcCase(entry);
                absActName = PoCoUtils.getMtdName(entry);
            } else
                concreteMtds.add(entry);

            for (String concreteMtd : concreteMtds) {
                HashSet<String> donelist = new HashSet<>();
                 /*
                pointcut PointCut1(java.lang.String value0):
                    call(java.io.File.new(..,java.lang.String)) && args(*,value0);
                Object around(java.lang.String value0): PointCut1(value0) { ... }
                =============================================================================
                pointcut PointCut1(argStrs[0]):
                    call(java.io.File.new(argStrs[3])) && args(argStrs[1]);
                Object around(argStrs[0]): PointCut1(//argStrs[2]) { ... }
                */

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
                String methodName = PoCoUtils.getMtdNmInfo(concreteMtd);
                //Step 2: get parameters info of the method that needs to be monitored.
                // a. 1st check the parameter object is a variable or not, and load info from the closure if so;
                // b. 2nd check the parameter's value is a variable or not, if it is a variable, then need check
                //    if this variable need to be bound in this advice.
                String[] argsList = PoCoUtils.getArgArray(concreteMtd);

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
                                if (argVal4Match == null)
                                    argVal4Match = new Hashtable<>();
                                if (PoCoUtils.reContainNotMatch(argsList[i])) {
                                    argVal4Match.put("!" + argTyp + " value" + count, argValStr);
                                }else {
                                    argVal4Match.put(argTyp + " value" + count, argValStr);
                                }

                            }

                            //generate the correct argument String for aspectj advice
                            argStrs3.add(argTyp);
                            argStrs0.add(argTyp + " value" + count);
                            argStrs1.add("value" + count);
                            argStrs2.add("value" + count);
                            if (isPrimitiveType(argTyp)) {
                                if (argTyp.equals("java.lang.String") || argTyp.equals("String")) {
                                    argStrs4.add("#" + argTyp + "{\"+" + "value" + count + "+\"}");
                                } else {
                                    argStrs5.add("String arg" + count + " = RuntimeUtils.genValueofStr(value" + count + ");");
                                    argStrs4.add("#" + argTyp + "{\"+" + "arg" + count + "+\"}");
                                }
                            } else {
                                argStrs5.add("int arg" + count + " = System.identityHashCode(value" + count + ");");
                                argStrs4.add("#" + argTyp + "{\"+" + "arg" + count + "+\"}");
                            }
                            count++;
                        } else if (argsList[i].trim().equals("..")) {
                            argStrs1.add("..");
                            argStrs3.add("..");
                            argStrs4.add("..");
                        } else {
                            argStrs1.add("*");
                            argStrs3.add("*");
                            argStrs4.add("*");
                        }
                    }
                }

                String[] argStrs = new String[]{arr2Str(argStrs0), arr2Str(argStrs1),
                        arr2Str(argStrs2), arr2Str(argStrs3), arr2Str(argStrs4)};

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
                            String sig = "RuntimeUtils.getNamFrmJP(thisJoinPoint)" + "+\"(" + argStrs[4] + ")\"";
                            varsNeed2Bind4Act.put("java.lang.String " + sig, "sig$" + varName.toString());
                        }
                    }
                }

                //generate code for action adivce
                if (mode == 0)
                    genAdvice4Actions("PointCut" + pointcutNum++, argStrs, varsNeed2Bind4Act, argVal4Match, argStrs5, absActName, policyName);
                else if (mode == 1) //generate code for action adivce
                    genAdvice4Results("PointCut" + pointcutNum++, argStrs, varsNeed2Bind4Res, argVal4Match, argStrs5, absActName);
                else
                    genAdvice4Events("PointCut" + pointcutNum++, argStrs, varsNeed2Bind4Act, varsNeed2Bind4Res, argVal4Match, argStrs5, absActName);
                bindingVars = new HashMap<String, String>();
            }
        }
    }

    private String removePolicyName(String entry) {
        assert entry != null && entry.trim().length()>0;
        int rIndex = entry.indexOf('>',0);
        return entry.substring(rIndex+1,entry.length());
    }



    private String getPolicyName(String entry) {
        assert entry != null && entry.trim().length()>0;
        int rIndex = entry.indexOf('>',0);
        return entry.substring(1,rIndex);
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

    private void genAdvice4Actions(String pointcutName, String[] argStrs, Hashtable varsNeed2Bind, Hashtable argVal4Match, ArrayList<String> handleSig, String isAbsCase, String PolicyName){
        //if it is conditional match or need dynamically bind value to variables
        if (varsNeed2Bind != null || argVal4Match != null) {
            outLine(1, "before(%s): %s(%s) {", argStrs[0], pointcutName, argStrs[2]);

            String paras = getParaList(argStrs[0]);

            //it is the abstraction action case, then need load the parameters
            if(argStrs[2] == null || argStrs[2].length()==0)
                ;
            else {
                if (isAbsCase != null)
                    outLine(2, "ArrayList<TypeVal> vals = AbsActions." + isAbsCase + "(thisJoinPoint," + argStrs[2] + ");");
                else
                    outLine(2, "ArrayList<TypeVal> vals = RuntimeUtils.getVals(\"" + paras + "\"," + argStrs[2] + ");");
            }

            //generate conditional statement for conditional monitoring case
            String conditionState = genCoditionStatements(argVal4Match, isAbsCase);
            //if is the conditional monitoring case
            if (conditionState != null && conditionState.length() > 0) {
                outLine(2, "if (" + conditionState + ") {");
                //outLine(3, "String[] varNames = null;");
                outLine(3, "DataWH dh = new DataWH();");

                //in order to generate the correct method signature that includes the variable info,
                //need dynamic genearate variable information
                handleSigVar(handleSig, 3);
                valueBind4Advices(varsNeed2Bind, 3);
                if (argStrs[2] != null && argStrs[2].trim().length() > 0) {
                    //outLine(3, "Object[] objs = new Object[]{" + argStrs[2] + "};");

                    if(isAbsCase == null)
                        outLine(3, "String evtSig = RuntimeUtils.genSigFrmJP(thisJoinPoint, \"" + argStrs[3] + "\", objs, varNames);");
                    else
                        outLine(3, "String evtSig = AbsActUtils.genAbsSig(\"" + isAbsCase + "\",vals);");
                    outLine(3, "dh.addTypVal(\""+PolicyName +"_evtSig\", \"java.lang.String\",evtSig);");

                    //outLine(3, pocoRoot + ".queryAction(new Action(evtSig),dh);");
                }
//                else {
//                    if(isAbsCase == null)
//                        outLine(3, pocoRoot + ".queryAction(new Action(thisJoinPoint),null);");
//                    else
//                        outLine(3, pocoRoot + ".queryAction(new Action(\""+isAbsCase+"()\"),null);");
//                }
//                outLine(3, "if(" + pocoRoot + ".hasRes4Action()) {");
//                outLine(4, "return " + pocoRoot + ".getRes4Action();");
//                outLine(3, "} else");
//                outLine(4, "return proceed(%s);", argStrs[2]);
//                outLine(2, "} else");
//                outLine(3, "return proceed(%s);", argStrs[2]);
                outLine(2, "} ");
            } else {
                //handle vars in order to generate correct method signatures info, which includes the variable infos
                outLine(2, "String[] varNames = null;");
                handleSigVar(handleSig, 2);
                valueBind4Advices(varsNeed2Bind, 2);
                if (argStrs[2] != null && argStrs[2].trim().length() > 0) {
                    //outLine(2, "Object[] objs = new Object[]{" + argStrs[2] + "};");

                    if(isAbsCase == null)
                        outLine(2, "String evtSig = RuntimeUtils.genSigFrmJP(thisJoinPoint, \"" + argStrs[3] + "\", objs, varNames);");
                    else
                        outLine(2, "String evtSig = AbsActUtils.genAbsSig(\"" + isAbsCase + "\",vals);");
                    outLine(2, "dh.addTypVal(\""+PolicyName +"_evtSig\", \"java.lang.String\",evtSig);");

                    //outLine(2, pocoRoot + ".queryAction(new Action(evtSig), dh);");
                }
//                else {
//                    if(isAbsCase == null)
//                        outLine(2, pocoRoot + ".queryAction(new Action(thisJoinPoint),null);");
//                    else
//                        outLine(2, pocoRoot + ".queryAction(new Action(\""+isAbsCase+"()\"),null);");
//                }
//                outLine(2, "if(" + pocoRoot + ".hasRes4Action()) {");
//                outLine(3, "return " + pocoRoot + ".getRes4Action();");
//                outLine(2, "} else");
//                outLine(3, "return proceed(%s);", argStrs[2]);
                outLine(2, "} ");
            }
        } else {
            outLine(1, "object around(): %s() {", pointcutName);

            if(isAbsCase == null)
                outLine(2, pocoRoot + ".queryAction(new Action(thisJoinPoint),null);");
            else
                outLine(2, pocoRoot + ".queryAction(new Action(\""+isAbsCase+"\"),null);");

            outLine(2, "if(" + pocoRoot + ".hasRes4Action()) {");
            outLine(3, "return " + pocoRoot + ".getRes4Action();");
            outLine(2, "} else");
            outLine(3, "return proceed();");
        }
        outLine(1, "}\n");
    }

    private String getParaList(String argStr) {
        String[] paraList  = argStr.split(",");
        for(int i = 0; i<paraList.length;i++)
            paraList[i] = paraList[i].split("\\s+")[0];

        return Arrays.toString(paraList).replace("[", "").replace("]","").replace(" ","");
    }

    private void genAdvice4Results(String pointcutName, String[] argStrs, Hashtable varsNeed2Bind, Hashtable argVal4Match, ArrayList<String> handleSig, String isAbsCase) {
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
                handleSigVar(handleSig, 2);
                outLine(2, "Object ret = proceed(%s);", argStrs[2]);
                //valueBind4Advices(varsNeed2Bind, 2);
                genEvent4queryAction(2);
            }
        } else {
            outLine(1, "Object around(): %s() {", pointcutName);
            handleSigVar(handleSig, 2);
            outLine(2, "Object ret = proceed();", argStrs[2]);
            //valueBind4Advices(varsNeed2Bind, 2);
            genEvent4queryAction(2);
        }

        outLine(1, "}\n");
    }

    private void genAdvice4Events(String pointcutName, String[] argStrs, Hashtable varsNeed2Bind4Act, Hashtable varsNeed2Bind4Res, Hashtable argVal4Match, ArrayList<String> handleSig, String isAbsCase) {
        if (varsNeed2Bind4Act != null || varsNeed2Bind4Res != null || argVal4Match != null) {
            outLine(1, "Object around(%s): %s(%s) {", argStrs[0], pointcutName, argStrs[2]);
            //generate conditional statement for conditional monitoring case
            String conditionState = genCoditionStatements(argVal4Match, isAbsCase);
            //if is the conditional monitoring case
            if (conditionState != null && conditionState.length() > 0) {
                outLine(2, "if (" + conditionState + ") {");
                outLine(3, "String[] varNames = null;");
                handleSigVar(handleSig, 3);
                valueBind4Advices(varsNeed2Bind4Act, 3);
                if (argStrs[2] != null && argStrs[2].trim().length() > 0) {
                    //outLine(3, "Object[] objs = new Object[]{" + argStrs[2] + "};");

                    if(isAbsCase == null)
                        outLine(3, "String evtSig = RuntimeUtils.genSigFrmJP(thisJoinPoint, \"" + argStrs[3] + "\", objs, varNames);");
                    else
                        outLine(3, "String evtSig = AbsActUtils.genAbsSig(\"" + isAbsCase + "\",vals);");

                    outLine(3, pocoRoot + ".queryAction(new Action(evtSig),dh);");
                } else {
                    outLine(3, pocoRoot + ".queryAction(new Action(thisJoinPoint),dh);");
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
                handleSigVar(handleSig, 2);
                if (argStrs[2] != null && argStrs[2].trim().length() > 0) {
                    //outLine(2, "Object[] objs = new Object[]{" + argStrs[2] + "};");

                    if(isAbsCase == null)
                        outLine(2, "String evtSig = RuntimeUtils.genSigFrmJP(thisJoinPoint, \"" + argStrs[3] + "\", objs, varNames);");
                    else
                        outLine(2, "String evtSig = AbsActUtils.genAbsSig(\"" + isAbsCase + "\",vals);");

                    outLine(2, pocoRoot + ".queryAction(new Action(evtSig),dh);");
                } else {
                    outLine(2, pocoRoot + ".queryAction(new Action(thisJoinPoint),dh);");
                }
                outLine(2, "Object ret = proceed(%s);", argStrs[2]);
                valueBind4Advices(varsNeed2Bind4Res, 2);
                genEvent4queryAction(2);
            }
        } else {
            outLine(1, "Object around(): %s() {", pointcutName);
            handleSigVar(handleSig, 2);
            valueBind4Advices(varsNeed2Bind4Act, 2);
            outLine(2, pocoRoot + ".queryAction(new Action(thisJoinPoint),dh);");
            outLine(2, "Object ret = proceed();", argStrs[2]);
            valueBind4Advices(varsNeed2Bind4Res, 2);
            genEvent4queryAction(2);
        }
        outLine(1, "}\n");
    }

    private void genEvent4queryAction(int offset) {
        outLine(offset, "Result result = new Result(thisJoinPoint, ret);");
        outLine(offset, pocoRoot + ".queryAction(result,null);");
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

        String returnStr = "";
        for (i = 0; i < varTyps.length; i++) {
            //need handle the case where we do not care about the type and value
            if (varVals[i] == null) continue;
            String matchState;
            if (PoCoUtils.isVariable(varVals[i]))
                matchState = genCondStatWVar(varTyps[i], varNams[i], varVals[i], isAbsCase,i);
            else
                matchState = genCondStatWVal(varTyps[i], varNams[i], varVals[i], isAbsCase,i);

            if (matchState != null) {
                returnStr += matchState;
                if (i != varTyps.length - 1)
                    returnStr += " && ";
            }
        }
        return PoCoUtils.trimEndPunc(returnStr, " && ");
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

            String retStr = "";
            if (isAbsCase == null)
                retStr = "RuntimeUtils.valMatch(" + str + ", \"" + matchVal.replace("\\", "") + "\")";
            else
                retStr = "AbsActUtils.valMatch(vals.get("+ index + "), \"" + matchVal.replace("\\", "") + "\")";

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
                    if (PoCoUtils.isPoCoObject(varName)) {
                        sb4NamList.append(varName.substring(1));
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
            if (sb4NamList.length() > 0) {
                String temp = PoCoUtils.trimEndPunc(sb4NamList.toString(), ",");
                outLine(offset, "String[] varNames = new String[] {\"" + temp + "\"};");
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
        outLine(2, "if (RuntimeUtils.matchingStack(" + pocoRoot + ".promotedEvents,run)) {");
        outLine(3, pocoRoot + ".promotedEvents.pop();");
        outLine(3, "Object ret = proceed(run);");
        outLine(3, "String retTyp = RuntimeUtils.trimClassName(ret.getClass().toString());");
        genVarBing4Prom();
        outLine(3, "PromotedResult promRes = new PromotedResult(run,ret);");
        outLine(3, pocoRoot + ".queryAction(promRes,dh);");
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
        outLine(3, pocoRoot + ".queryAction(promRes,null);");
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

    private String[] deleteExtraComma(String[] argStrs) {
        for (int i = 0; i < argStrs.length; i++)
            argStrs[i] = PoCoUtils.trimEndPunc(argStrs[i], ",");

        return argStrs;
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