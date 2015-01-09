package com.poco.PoCoRuntime;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

/**
 * Created by caoyan on 1/8/15.
 */

public class SRELib {

    public static SRE PerformOPs(String operator,SRE sre1, SRE sre2) {
        if (isEmpty(sre1) && isEmpty(sre2))
            return null;

        String positiveRE= null, negativeRE=null;
        switch (operator) {
            case "Union":
                positiveRE = operate("Union", sre1.positiveRE(),
                        sre2.positiveRE());
                negativeRE = operate("InterSection", sre1.negativeRE(),
                        sre2.negativeRE());
                // negativeRE = operate("Minus", negativeRE, positiveRE);
                return new SRE(positiveRE, negativeRE);
            case "Punion":
                positiveRE = operate("Union", sre1.positiveRE(),
                        sre2.positiveRE());
                negativeRE = operate("InterSection", sre1.negativeRE(),
                        sre2.negativeRE());
                // negativeRE = operate("Minus", positiveRE, negativeRE);
                return new SRE(positiveRE, negativeRE);
            case "Disjunction":
                positiveRE = operate("InterSection", sre1.positiveRE(),
                        sre2.positiveRE());
                negativeRE = operate("Union", sre1.negativeRE(),
                        sre2.negativeRE());
                // negativeRE = operate("Minus", positiveRE, negativeRE);
                return new SRE(positiveRE, negativeRE);
            case "Conjunction":
                positiveRE = operate("InterSection", sre1.positiveRE(),
                        sre2.positiveRE());
                negativeRE = operate("Union", sre1.negativeRE(),
                        sre2.negativeRE());
                return new SRE(positiveRE, negativeRE);

            case "Equals":
                RegExp rePos1 = new RegExp(sre1.positiveRE().replace("%", ".*"));
                RegExp rePos2 = new RegExp(sre2.positiveRE().replace("%", ".*"));
                Automaton am1pos = rePos1.toAutomaton();
                Automaton am2pos = rePos2.toAutomaton();
                if (am1pos.equals(am2pos)) { //it positive part are same, then check negative part
                    RegExp reNeg1 = new RegExp(sre1.negativeRE().replace("%", ".*"));
                    RegExp reNeg2 = new RegExp(sre2.negativeRE().replace("%", ".*"));
                    Automaton am1Neg = reNeg1.toAutomaton();
                    Automaton am2Neg = reNeg2.toAutomaton();
                    if (am1Neg.equals(am2Neg)) {// if both negative is same then should bcm positive
                        Automaton amResult = am1pos.union(am1Neg);
                        if (!amResult.isEmpty())
                            positiveRE = amResult.toString();
                    }
                }
            default:
                return null;
        }
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
}
