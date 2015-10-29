package com.poco.Extractor;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by caoyan on 10/28/14.
 */
public class Closure {
    private Hashtable<String, VarTypeVal> vars;
    private Hashtable<String, VarTypeVal> functions;
    private ArrayList<String> varFunNames;
    private ArrayList<String> policyNames;

    public Hashtable getVars() {
        return vars;
    }

    public Hashtable getFunctions() {
        return functions;
    }

    public Closure() {
        vars = new Hashtable<String, VarTypeVal>();
        functions = new Hashtable<String, VarTypeVal>();
        varFunNames = new ArrayList<String>();
        policyNames = new ArrayList<String>();
    }

    public void addPolicy(String policyName) {
        if(this.policyNames.contains(policyName)) {
            System.err.println("The policy: " + policyName + "has been defined more than once, please check!");
            System.exit(-1);
        }
        this.policyNames.add(policyName);
    }

    public ArrayList<String> getPolicies() {
        return this.policyNames;
    }

    public void addVar(String varName, VarTypeVal typeVal) {
        checkedAdd(varName, typeVal, vars);
    }

    public void addFunction(String varName, VarTypeVal typeVal) {
        checkedAdd(varName, typeVal, functions);
    }

    private void checkedAdd(String varName, VarTypeVal typeVal, Hashtable<String, VarTypeVal> target) {
        varNameValidation(varName);
        valValidation(typeVal);
        target.put(varName, typeVal);
        varFunNames.add(varName);
    }


    public void updateVar(String varName, VarTypeVal typeVal) {
        checkedUpdate(varName, typeVal, vars);
    }

    public void updateFunction(String varName, VarTypeVal typeVal) {
        checkedUpdate(varName, typeVal, functions);
    }

    private void checkedUpdate(String varName, VarTypeVal typeVal, Hashtable<String, VarTypeVal> target) {
        varNameValidation(varName);
        valValidation(typeVal);
        if (target.containsKey(varName))
            target.put(varName, typeVal);
    }


    public VarTypeVal loadFrmVars(String varName) {
        return checkedLoad(varName, vars);
    }

    public VarTypeVal loadFrmFunctions(String varName) {
        return checkedLoad(varName, functions);
    }

    private VarTypeVal checkedLoad(String varName, Hashtable<String, VarTypeVal> target) {
        varNameValidation(varName);
        return (VarTypeVal) (target.get(varName));
    }


    public boolean isVarsContain(String varName) {
        return isTargetContains(varName, vars);
    }

    public boolean isFunctionsContain(String varName) {
        return isTargetContains(varName, functions);
    }

    private boolean isTargetContains(String varName, Hashtable<String, VarTypeVal> target) {
        varNameValidation(varName);
        return target.containsKey(varName);
    }


    public String getVarContext(String varName) {
        return getTargetContext(varName, vars);
    }

    public String getFunctionContext(String varName) {
        return getTargetContext(varName, functions);
    }

    private String getTargetContext(String varName, Hashtable<String, VarTypeVal> target) {
        VarTypeVal vals = checkedLoad(varName, target);
        if (vals != null)
            return vals.getVarContext();
        return null;
    }

    public String getVarType(String varName) {
        return getTargetTyp(varName, vars);
    }

    public String getMacroType(String varName) {
        return getTargetTyp(varName, functions);
    }

    public ArrayList<String> getAllVarNames() {
        if (varFunNames != null)
            return varFunNames;
        return null;
    }

    private String getTargetTyp(String varName, Hashtable<String, VarTypeVal> target) {
        VarTypeVal vals = checkedLoad(varName, target);
        if (vals != null)
            return vals.getVarType();
        return null;
    }

    private void varNameValidation(String varName) {
        if (varName == null)
            throw new NullPointerException("The name of the variable or function cannot be NULL!");
    }

    private void valValidation(VarTypeVal typVal) {
        if (typVal == null)
            throw new NullPointerException("Can't put null Object into closure.");
    }

    /**
     * just for debug
     */
    public void printClosure() {
        if (vars != null) {
            for (Object varname : vars.keySet()) {
                System.out.println(varname + ": ");
                System.out.println("Type    : " + loadFrmVars((String) varname).getVarType());
                System.out.println("Context : " + loadFrmVars((String) varname).getVarContext());
                System.out.println("==========================================================");
            }
        }
        if (functions != null) {
            for (Object varname : functions.keySet()) {
                System.out.println(varname + ": ");
                System.out.println("Type    : " + loadFrmFunctions((String) varname).getVarType());
                System.out.println("Args    : " + loadFrmFunctions((String) varname).getFunArgs());
                System.out.println("Context : " + loadFrmFunctions((String) varname).getVarContext());
                System.out.println("************************************************************");
            }
        }
    }
}