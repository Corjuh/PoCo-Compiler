package com.poco.PoCoRuntime;

/**
 * Signed regular expression from PoCo.
 */
/**
 * Signed regular expression from PoCo.
 */
public class SRE {
    // SRE Unary Operators: Complement; Actions; Results; Positive; Negative
    private String positiveRE = null;
    private String negativeRE = null;

    public void setPositiveRE(String positiveRE) {
        this.positiveRE = positiveRE;
        positiveRE = genSRE().getPositiveRE();
        negativeRE = genSRE().getNegativeRE();
    }

    public void setNegativeRE(String negativeRE) {
        this.negativeRE = negativeRE;
        positiveRE = genSRE().getPositiveRE();
        negativeRE = genSRE().getNegativeRE();
    }

    public SRE() {
        positiveRE = null;
        negativeRE = null;
    }

    public SRE(String positive, String negative) {
        positiveRE = convertSRE(positive);
        negativeRE = convertSRE(negative);
        positiveRE = genSRE().getPositiveRE();
        negativeRE = genSRE().getNegativeRE();
    }

    protected SRE genSRE() {
        return SRELib.GetBaseSRE(this);

    }

    public boolean isNeutral() {
        return (positiveRE == null && negativeRE == null);
    }

    public String positiveRE() {
        if (positiveRE != null) {
            return positiveRE;
        } else {
            return "";
        }
    }

    public String getPositiveRE() {
        return positiveRE;
    }

    public String getNegativeRE() {
        return negativeRE;
    }

    public String negativeRE() {
        if (negativeRE != null) {
            return negativeRE;
        } else {
            return "";
        }
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
        if (sre == null) {
            return null;
        }

        // TODO: Flesh out this method. Currently just replaces removes the
        // wildcard
        return sre.replaceAll("\\(%\\)", "");
    }

    @Override
    public String toString() {
        return "SRE [positiveRE=" + positiveRE + ", negativeRE=" + negativeRE
                + "]";
    }
}