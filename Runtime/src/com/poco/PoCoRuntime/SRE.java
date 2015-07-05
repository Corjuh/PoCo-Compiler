package com.poco.PoCoRuntime;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

/**
 * Signed regular expression from PoCo.
 */
public class SRE {
    private String positiveRE;
    private String negativeRE;

    public void setPositiveRE(String positiveRE) {
        this.positiveRE = positiveRE;
    }

    public void setNegativeRE(String negativeRE) {
        //this.negativeRE = convertSRE(negativeRE);
        this.negativeRE = negativeRE;
    }

    public SRE() {
        positiveRE = null;
        negativeRE = null;
    }

    public SRE(String positive, String negative) {
        if(positive!= null && positive.trim().length()>0)
            this.positiveRE = positive;

        if(negative!= null && negative.trim().length()>0)
            negativeRE = negative;
    }

    public boolean isNeutral() {
        if (SREUtil.isSreFieldNull(positiveRE)
                && SREUtil.isSreFieldNull(negativeRE))
            return true;
        else {
            String posTemp = getReValue(this.positiveRE);
            String negTemp = getReValue(this.negativeRE);
            return (SREUtil.isSreFieldNull(posTemp) && SREUtil.isSreFieldNull(negTemp));
        }
    }

    private String getReValue(String reStr) {
        String returnStr = reStr;
        if (SREUtil.isSreFieldNull(returnStr) && RuntimeUtils.isVariable(returnStr))
            returnStr = RuntimeUtils.loadValFrmDataWH(returnStr);
        return returnStr;
    }

    public SRE complement() {
        String posTemp = getReValue(this.positiveRE);
        String negTemp = getReValue(this.negativeRE);
        return new SRE(negTemp, posTemp);
    }

    /**
     * Includes only the actions in SRE
     *
     * @return
     */
    public SRE getAction() {
        String posTemp = getReValue(this.positiveRE);
        String negTemp = getReValue(this.negativeRE);

        RegExp rePos = new RegExp(posTemp.replace("%", ".*"));
        RegExp reNeg = new RegExp(negTemp.replace("%", ".*"));
        RegExp action = new RegExp(".+\\(.*\\)");

        Automaton amPos = rePos.toAutomaton();
        Automaton amNeg = reNeg.toAutomaton();
        Automaton amAct = action.toAutomaton();

        SRE returnSRE = new SRE();
        if (amPos.subsetOf(amPos.intersection(amAct)))
            returnSRE.setPositiveRE(posTemp);

        if (amNeg.subsetOf(amNeg.intersection(amAct)))
            returnSRE.setNegativeRE(negTemp);

        return returnSRE;

    }

    /**
     * Includes only the results in SRE resPos =
     * amPos.minus(amPos.intersection(amAct)); resNeg =
     * amNeg.minus(amNeg.intersection(amAct));
     *
     * @return
     */
    public SRE getResult() {
        String posTemp = getReValue(this.positiveRE);
        String negTemp = getReValue(this.negativeRE);

        RegExp rePos = new RegExp(posTemp.replace("%", ".*"));
        RegExp reNeg = new RegExp(negTemp.replace("%", ".*"));
        RegExp action = new RegExp(".+\\(.*\\)");
        Automaton amPos = rePos.toAutomaton();
        Automaton amNeg = reNeg.toAutomaton();
        Automaton amAct = action.toAutomaton();

        Automaton resPos = amPos.minus(amPos.intersection(amAct));
        Automaton resNeg = amNeg.minus(amNeg.intersection(amAct));

        SRE returnSRE = new SRE();
        if (amPos.subsetOf(resPos))
            returnSRE.setPositiveRE(posTemp);

        if (amNeg.subsetOf(resNeg))
            returnSRE.setNegativeRE(negTemp);

        return returnSRE;
    }

    /**
     * Includes only positive portion of SRE
     *
     * @return
     */
    public SRE getPostiveVal() {
        return new SRE(getReValue(this.positiveRE), null);
    }

    /**
     * Includes only negative portion of SRE
     *
     * @return
     */
    public SRE getNegativeVal() {
        return new SRE(null, getReValue(this.negativeRE));
    }

    public String getPositiveRE() {
        return positiveRE;
    }

    public String getNegativeRE() {
        return negativeRE;
    }

    /**
     * Converts an SRE containing PoCo-specific syntax to an acceptable Java
     * RegEx
     *
     * @param sre
     *            PoCo SRE
     * @return Proper Java RegEx
     */
    public static String convertSRE(String sre) {
        if (sre == null || sre.trim().length()==0)
            return null;

        return sre.replaceAll("\\(%\\)", "");
    }

    @Override
    public String toString() {
        return "SRE [positiveRE=" + positiveRE + ", negativeRE=" + negativeRE  + "]";
    }
}