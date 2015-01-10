package com.poco.PoCoRuntime;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

/**
 * Created by caoyan on 1/8/15.
 */

public class SRELib {
    /**
     * This function is used to perform Binary Set Operations on SRES
     *
     * @param operator
     *            It defines the set operator, which can be Union, Punion,
     *            Disjunction, Conjunction and Equals
     * @param sre1
     *            It is the first SRE value
     * @param sre2
     *            It is the second SRE value
     * @return the result SRE
     */
    public static SRE PerformBOPs(String operator, SRE sre1, SRE sre2) {
        if (isEmpty(sre1) && isEmpty(sre2))
            return null;

        String positiveRE = null, negativeRE = null;
        switch (operator) {
            // Positive favoring union of SREs
            case "Union":
                positiveRE = operate("Union", sre1.positiveRE(), sre2.positiveRE());
                negativeRE = operate("InterSection", sre1.negativeRE(),
                        sre2.negativeRE());
                if(negativeRE != null)
                    negativeRE = operate("Minus", negativeRE, positiveRE);
                break;

            // Negative favoring union of SREs
            case "Punion":
                positiveRE = operate("Union", sre1.positiveRE(), sre2.positiveRE());
                negativeRE = operate("InterSection", sre1.negativeRE(),
                        sre2.negativeRE());
                if(positiveRE != null)
                    positiveRE = operate("Minus", positiveRE, negativeRE);
                break;

            // Disjunction of SREs
            case "Disjunction":
                positiveRE = operate("Union", sre1.positiveRE(), sre2.positiveRE());
                negativeRE = operate("InterSection", sre1.negativeRE(),
                        sre2.negativeRE());
                break;

            // Conjunction of SREs
            case "Conjunction":
                positiveRE = operate("InterSection", sre1.positiveRE(),
                        sre2.positiveRE());
                negativeRE = operate("Union", sre1.negativeRE(), sre2.negativeRE());
                break;

            // Positive where SREs match in sign
            case "Equals":
                if (isEqual(sre1.positiveRE(), sre2.positiveRE())
                        && isEqual(sre1.negativeRE(), sre2.negativeRE()))
                    positiveRE = operate("Union", sre1.positiveRE(),
                            sre1.negativeRE());
                break;
            default:
                break;
        }
        return new SRE(positiveRE, negativeRE);
    }

    /**
     * This function is used to perform Binary Set Operations on SRES
     *
     * @param operator
     *            It defines the set operator, which can be Complement, action,
     *            result, positive and negative
     * @param sre1
     *            It is the first SRE value
     * @return the result SRE
     */
    public static SRE PerformUOPs(String operator, SRE sre) {
        if (isEmpty(sre))
            return null;

        switch (operator) {
            case "Complement": // Switches sign of SRE
                return new SRE(sre.getNegativeRE(), sre.getPositiveRE());
			/*
			 * case "Action": //Includes only the actions in SRE
			 *
			 * case "Result": //Includes only the results in SRE
			 */
            case "Positive": // Includes only positive portion of SRE
                return new SRE(sre.getPositiveRE(), null);
            case "Negative": // Includes only negative portion of SRE
                return new SRE(null, sre.getNegativeRE());
            default:
                return sre;
        }
    }

    public static SRE GetBaseSRE(SRE sre) {
        Class<BopSRE> classBS = BopSRE.class;
        Class<UopSRE> classUS = UopSRE.class;
        Class<? extends SRE> classChild = sre.getClass();
        if (classBS.isAssignableFrom(classChild)) { // BopSRE
            BopSRE bopSre = (BopSRE) sre;
            SRE result = SRELib.PerformBOPs(bopSre.getSrebop(),
                    GetBaseSRE(bopSre.sre1), GetBaseSRE(bopSre.sre2));
            // System.out.println(result);
            return result;
        } else if (classUS.isAssignableFrom(classChild)) { // UopSRE
            UopSRE uopSre = (UopSRE) sre;
            SRE result = SRELib.PerformUOPs(uopSre.getSreuop(),
                    GetBaseSRE(uopSre.sre));
            // System.out.println(result);
            return result;
        } else
            return sre;
    }

    /**
     * This function use to operate set operations
     *
     * @param op
     *            This is the first parameter to specify the set operator, which
     *            can be union, punion, conjunction and disjunction
     * @param sre1
     *            This is the first sre value
     * @param sre2
     *            This is the second sre value
     * @return
     */
    private static String operate(String op, String sre1, String sre2) {
        if(sre1 == null || sre2== null)
            return null;

        String returnRe = null;
        if (!sre1.trim().equals("")) { // sre1pos is not empty
            if (!sre2.trim().equals("")) { // case both positive is not empty
                RegExp re1 = new RegExp(sre1.replace("%", ".*"));
                RegExp re2 = new RegExp(sre2.replace("%", ".*"));
                Automaton am1 = re1.toAutomaton();
                Automaton am2 = re2.toAutomaton();
                switch (op) {
                    case "Union":
                        returnRe = unionOP(am1, am2);
                        break;
                    case "InterSection":
                        returnRe = interSectOP(am1, am2);
                        break;
                    case "Minus":
                        returnRe = minusOP(am1, am2);
                        break;
                    default:
                        break;
                }
            } else { // sre2pos is empty, so union is just the sre1Pos
                returnRe = sre1;
            }
        } else { // sre1pos is empty
            if (!sre2.trim().equals("")) { // case both positive is not empty
                returnRe = sre2;
            }
        }
        return returnRe;
    }

    private static String unionOP(Automaton am1, Automaton am2) {
        Automaton amResult = am1.union(am2);
        if (!amResult.isEmpty())
            return amResult.toString();
        else
            return null;
    }

    private static String interSectOP(Automaton am1, Automaton am2) {
        Automaton amResult = am1.intersection(am2);
        if (!amResult.isEmpty())
            return amResult.toString();
        else
            return null;
    }

    private static String minusOP(Automaton am1, Automaton am2) {
        Automaton amResult = am1.minus(am2);
        if (!amResult.isEmpty())
            return amResult.toString();
        else
            return null;
    }

    private static boolean isEmpty(SRE sre) {
        String srePos = sre.positiveRE();
        String sreNeg = sre.negativeRE();
        if (srePos.trim().equals("") && sreNeg.trim().equals(""))
            return true;
        else
            return false;
    }

    private static boolean isEqual(String sre1, String sre2) {
        RegExp re1 = new RegExp(sre1.replace("%", ".*"));
        RegExp re2 = new RegExp(sre2.replace("%", ".*"));
        Automaton am1 = re1.toAutomaton();
        Automaton am2 = re2.toAutomaton();
        if (am1.equals(am2))
            return true;
        else
            return false;
    }
}