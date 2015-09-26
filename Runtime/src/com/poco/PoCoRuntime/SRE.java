package com.poco.PoCoRuntime;

import java.util.ArrayList;
import java.util.HashSet;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

/**
 * Signed regular expression from PoCo.
 */
public class SRE {
    private String positiveRE;
    private String negativeRE;

    // used for split cases, so will only do once
    private ArrayList<String> posREs;
    private ArrayList<String> negREs;

    public void setPositiveRE(String positive) {
        if (!SREUtil.isSreFieldNull(positive)) {
            this.positiveRE = positive;
            this.posREs = SREUtil.splitSreStr(this.positiveRE);
        } else
            this.positiveRE = null;
    }

    public void setNegativeRE(String negative) {
        if (!SREUtil.isSreFieldNull(negative)) {
            this.negativeRE = negative;
            this.negREs = SREUtil.splitSreStr(this.negativeRE);
        } else
            this.negativeRE = null;
    }

    public SRE() {
        positiveRE = null;
        negativeRE = null;
    }

    public SRE(String positive, String negative) {
        setPositiveRE(positive);
        setNegativeRE(negative);
    }

    private SRE(String positive, String negative, ArrayList<String> posREs,
                ArrayList<String> negREs) {
        this.positiveRE = positive;
        this.negativeRE = negative;
        this.posREs = posREs;
        this.negREs = negREs;
    }

    /**
     * Both positive and negative value are null, then return true
     *
     * @return
     */
    public boolean isNeutral() {
        if (this.positiveRE == null && this.negativeRE == null)
            return true;
        SRE afterLoadVars = getAbsVal();
        return SREUtil.isSreFieldNull(afterLoadVars.positiveRE)
                && SREUtil.isSreFieldNull(afterLoadVars.negativeRE);
    }

    /**
     * This function is use to obtain the absolute value of a SRE by applying
     * all its variables' value.
     */
    public SRE getAbsVal() {
        String pos = null;
        String neg = null;
        ArrayList<String> poss = null;
        ArrayList<String> negs = null;

        if (!SREUtil.isSreFieldNull(this.positiveRE)) {
            poss = loadReVals(this.posREs);
            pos = genREfromsplitREs(poss);
        }
        if (!SREUtil.isSreFieldNull(this.negativeRE)) {
            negs = loadReVals(this.negREs);
            neg = genREfromsplitREs(negs);
        }
        return new SRE(pos, neg, poss, negs);
    }

    private ArrayList<String> loadReVals(ArrayList<String> res) {
        ArrayList<String> newRes = new ArrayList<String>();
        for (String re : res) {
            String varVal = RuntimeUtils.applyVarValues(re);
            if (varVal != null) {
                ArrayList<String> varVals = SREUtil.splitSreStr(varVal);
                for (String val : varVals)
                    newRes.add(val);
            }
        }
        if (!newRes.isEmpty())
            return newRes;
        else
            return null;
    }

    private String genREfromsplitREs(ArrayList<String> newPos) {
        if (!newPos.isEmpty()) {
            if (newPos.size() == 1)
                return newPos.get(0);
            else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < newPos.size(); i++) {
                    sb.append(newPos.get(i));
                    if (i != newPos.size() - 1)
                        sb.append("|");
                }
                return sb.toString();
            }
        } else
            return null;
    }

    /**
     * get the complement value of this SRE
     *
     * @return
     */
    public SRE complement() {
        return new SRE(this.negativeRE, this.positiveRE,negREs,posREs);
    }

    /**
     * Includes only the actions in SRE
     *
     * @return
     */
    public SRE getAction() {
        Automaton amAct = SREUtil.genMethodAutomaton();

        CalculatedSRE sre = this.convert2CalculatedSre();
        if (sre.getPosAutomaton() != null)
            sre.setPosAutomaton(sre.getPosAutomaton().intersection(amAct));
        if (sre.getNegAutomaton() != null)
            sre.setNegAutomaton(sre.getNegAutomaton().intersection(amAct));

        return sre;
    }

    /**
     * Includes only the results in SRE resPos =
     * amPos.minus(amPos.intersection(amAct)); resNeg =
     * amNeg.minus(amNeg.intersection(amAct));
     *
     * @return
     */
    public SRE getResult() {
        CalculatedSRE sre = this.convert2CalculatedSre();
        CalculatedSRE actVal = (CalculatedSRE) this.getAction();
        if (sre.getPosAutomaton() != null && actVal.getPosAutomaton() != null)
            sre.setPosAutomaton(sre.getPosAutomaton().minus(
                    actVal.getPosAutomaton()));
        if (sre.getNegAutomaton() != null && actVal.getNegAutomaton() != null)
            sre.setNegAutomaton(sre.getNegAutomaton().minus(
                    actVal.getNegAutomaton()));

        return sre;
    }

    /**
     * Includes only positive portion of SRE
     *
     * @return
     */
    public SRE getPostiveVal() {
        return new SRE(this.positiveRE, null);
    }

    /**
     * Includes only negative portion of SRE
     *
     * @return
     */
    public SRE getNegativeVal() {
        return new SRE(null, this.negativeRE);
    }

    public String getPositiveRE() {
        return positiveRE;
    }

    public String getNegativeRE() {
        return negativeRE;
    }

    public CalculatedSRE convert2CalculatedSre() {
        CalculatedSRE sre = new CalculatedSRE();
        // Step 1, load variables.
        SRE afterLoadVars = getAbsVal();
        // Step 1: first generate automaton for positive RE
        // do not need handle the variable case here, since the variable value
        // will change at runtime
        if (!SREUtil.isSreFieldNull(afterLoadVars.positiveRE)) {
            // sub-step 1: generate the positive automaton from positiveRE
            // string value
            Automaton amPos = genAmFrmRePosNegREs(afterLoadVars.posREs);
            sre.setPosAutomaton(amPos);

            // sub-step 2: abstract concrete methods from positiveRE string
            // value.
            // Here will treat variable as concrete,and later on before promote
            // its value will be evaluated again within the rootpolicy.java
            HashSet<String> methods = abstractMethods(afterLoadVars.posREs);
            if (methods != null && methods.size() > 0)
                sre.addConcreteMethods(methods);

            // sub-step 2: abstract concrete results from positiveRE string
            // value.
            // Here will treat variable as concrete,and later on before promote
            // its value will be evaluated again within the rootpolicy.java
            HashSet<String> results = abstractReults(afterLoadVars.posREs);
            if (results != null && results.size() > 0)
                sre.addConcreteResults(results);
        }

        // Step 2: perform the same procedure for negative part of SRES
        if (!SREUtil.isSreFieldNull(this.negativeRE)) {
            // sub-step 1: generate the positive automaton from negativeRE
            // string value
            Automaton amNeg = genAmFrmRePosNegREs(afterLoadVars.negREs);
            sre.setNegAutomaton(amNeg);

            // sub-step 2: abstract concrete methods from negativeRE string
            // value.
            // Here will treat variable as concrete,and later on before promote
            // its value will be evaluated again within the rootpolicy.java
            HashSet<String> methods = abstractMethods(afterLoadVars.negREs);
            if (methods != null && methods.size() > 0)
                sre.addConcreteMethods(methods);

            // sub-step 2: abstract concrete results from negativeRE string
            // value.
            // Here will treat variable as concrete,and later on before promote
            // its value will be evaluated again within the rootpolicy.java
            HashSet<String> results = abstractReults(afterLoadVars.negREs);
            if (results != null && results.size() > 0)
                sre.addConcreteResults(results);
        }

        // Step 3: return the generated value
        return sre;
    }

    private static Automaton genAmFrmRePosNegREs(ArrayList<String> reStrs) {
        Automaton[] ams = new Automaton[reStrs.size()];
        for (int i = 0; i < reStrs.size(); i++) {
            // handle the not matching case
            if (SREUtil.reContainNotMatch(reStrs.get(i))) {
                if (RuntimeUtils.isMethod(reStrs.get(i))) {
                    String funNme = RuntimeUtils.getMethodInfos(reStrs.get(i));
                    String argStr = RuntimeUtils.getfunArgstr(reStrs.get(i));
                    String[] args = argStr.split(",");
                    String[] args4Match = new String[args.length];
                    for (int j = 0; j < args.length; j++) {
                        if (SREUtil.reContainNotMatch(args[j])) {
                            String notMatchingVal = SREUtil.getNotMatchStr(args[j]);
                            String sreVal = RuntimeUtils
                                    .getPoCoObjVal(notMatchingVal);
                            args4Match[j] = args[j].substring(1);
                            String validVals = SREUtil.getPatternFrmType(RuntimeUtils
                                    .getPoCoObjTyp(notMatchingVal));
                            args[j] = args[j].replace(sreVal, validVals)
                                    .substring(1);
                        } else
                            args4Match[j] = args[j];
                    }
                    String str4all = RuntimeUtils.strArrJoin(args, ",");
                    String str4Match = RuntimeUtils.strArrJoin(args4Match, ",");

                    Automaton am1 = new RegExp(handleSREwBar(funNme + "("
                            + str4all + ")")).toAutomaton();
                    Automaton am2 = new RegExp(handleSREwBar(funNme + "("
                            + str4Match + ")")).toAutomaton();
                    ams[i] = am1.minus(am2);
                } else {
                    // it is the result case
                    String notMatchingVal = SREUtil.getNotMatchStr(reStrs.get(i));
                    String sreVal = RuntimeUtils.getPoCoObjVal(notMatchingVal);
                    String newVal = notMatchingVal.replace(sreVal, "*");
                    String sre1 = reStrs.get(i).replace(notMatchingVal,
                            newVal.substring(1));
                    String sre2 = reStrs.get(i).replace(notMatchingVal,
                            notMatchingVal.substring(1));
                    Automaton am1 = new RegExp(handleSREwBar(sre1))
                            .toAutomaton();
                    Automaton am2 = new RegExp(handleSREwBar(sre2))
                            .toAutomaton();
                    ams[i] = am1.minus(am2);
                }
            } else {
                ams[i] = new RegExp(handleSREwBar(reStrs.get(i))).toAutomaton();
            }
        }

        if (reStrs.size() == 1)
            return ams[0];
        else {
            Automaton retAm = ams[0];
            for (int i = 1; i < ams.length; i++)
                retAm = retAm.union(ams[i]);
            return retAm;
        }
    }

    private static HashSet<String> abstractMethods(ArrayList<String> strVal) {
        if (strVal == null)
            return null;
        HashSet<String> ret = new HashSet<String>();
        for (String str : strVal) {
            if (RuntimeUtils.isMethod(str)) {
                String methodName = RuntimeUtils.getMethodName(str);
                String argStr = RuntimeUtils.getfunArgstr(str);
                if (!methodName.contains("*") && !methodName.contains("%")) {
                    if (argStr != null) {
                        if (!argStr.contains("*") && !argStr.contains("%"))
                            ret.add(str);
                    } else
                        ret.add(str);
                }
            }
        }
        return ret;
    }

    private static HashSet<String> abstractReults(ArrayList<String> strVal) {
        if (strVal == null)
            return null;
        HashSet<String> ret = new HashSet<String>();
        for (String str : strVal) {
            if (RuntimeUtils.isPoCoObject(str) && !str.contains("*")
                    && !str.contains("%") && !str.contains("|"))
                ret.add(str);
        }
        return ret;
    }

    private static String handleSREwBar(String str) {
        if (SREUtil.hasAlternation(str))
            return SREUtil.getAlternationVal(str);
        else
            return SREUtil.validateStr(str);

    }

    @Override
    public String toString() {
        return "SRE [positiveRE=" + positiveRE + ", negativeRE=" + negativeRE
                + "]";
    }
}