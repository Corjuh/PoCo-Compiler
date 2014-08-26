package com.poco.PoCoRuntime;

/**
 * Signed regular expression from PoCo.
 */
public class SRE {
    private String positiveRE = null;
    private String negativeRE = null;

    public SRE() {

    }

    public SRE(String positive, String negative) {
        positiveRE = convertSRE(positive);
        negativeRE = convertSRE(negative);
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

    public String negativeRE() {
        if (negativeRE != null) {
            return negativeRE;
        } else {
            return "";
        }
    }

    /**
     * Converts an SRE containing PoCo-specific syntax to an acceptable Java RegEx
     * @param sre PoCo SRE
     * @return Proper Java RegEx
     */
    public static String convertSRE(String sre) {
        if (sre == null) {
            return null;
        }

        // TODO: Flesh out this method. Currently just replaces removes the wildcard
        return sre.replaceAll("\\(%\\)", "");
    }
}
