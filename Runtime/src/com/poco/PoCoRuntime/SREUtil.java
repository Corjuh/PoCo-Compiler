package com.poco.PoCoRuntime;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caoyan on 1/8/15.
 */

public class SREUtil {

	// use for case of operate on two SREs
	public static SRE performBOPs(String operator, SRE sre1, SRE sre2) {
		if (isEmpty(sre1) && isEmpty(sre2))
			return null;
		else
			return performBOPs(operator, new SRE[] {sre1, sre2});
	}

	public static SRE performBOPs(String operator, SRE[] sres) {
		switch (operator) {
			case "Union"		:
				return unionOp(sres);
			case "Punion"		:
				return punionOp(sres);
			case "Disjunction"	:
				return disjunctionOp(sres);
			case "Conjunction"	:
				return conjunctionOp(sres);
			case "Equals"		:
				return equalsOp(sres);
			default				:	return null;
		}
	}

	private static SRE equalsOp(SRE[] sres) {
		// pos = (pos1 n pos2) U (neg1 n neg2);
		// nes = % - pos

		ArrayList<Automaton> posAms = genPosAutomaton(sres);
		ArrayList<Automaton> negAms = genNegAutomaton(sres);

		Automaton amPosResult = automatonConj(posAms);
		Automaton amNegResult = automatonConj(negAms);

		amPosResult = amPosResult.union(amNegResult);
		amNegResult = amPosResult.complement();

		return new SRE(amPosResult.toString(), amNegResult.toString());
	}

	private static SRE conjunctionOp(SRE[] sres) {
		// pos = (pos1 n pos2) U (neg1 n neg2);
		ArrayList<Automaton> posAms = genPosAutomaton(sres);
		ArrayList<Automaton> negAms = genNegAutomaton(sres);

		Automaton amPosResult = automatonConj(posAms);
		Automaton amNegResult = automatonConj(negAms);

		amPosResult = amPosResult.union(amNegResult);

		return new SRE(amPosResult.toString(), amNegResult.toString());
	}

	private static SRE disjunctionOp(SRE[] sres) {
		ArrayList<Automaton> posAms = genPosAutomaton(sres);
		ArrayList<Automaton> negAms = genNegAutomaton(sres);

		Automaton amPosResult = Automaton.union(posAms);
		Automaton amNegResult = Automaton.union(negAms);

		return new SRE(amPosResult.toString(),	amNegResult.toString());
	}

	private static SRE punionOp(SRE[] sres) {
		ArrayList<Automaton> posAms = genPosAutomaton(sres);
		ArrayList<Automaton> negAms = genNegAutomaton(sres);

		Automaton amPosResult = Automaton.union(posAms);
		Automaton amNegResult = Automaton.union(negAms);

		// Negative favoring union of SREs
		if (amPosResult != null)
			amPosResult =  amPosResult.minus(amNegResult);

		return new SRE(amPosResult.toString(),	amNegResult.toString());
	}

	private static SRE unionOp(SRE[] sres) {
		ArrayList<Automaton> posAms = genPosAutomaton(sres);
		ArrayList<Automaton> negAms = genNegAutomaton(sres);

		Automaton amPosResult = Automaton.union(posAms);
		Automaton amNegResult = Automaton.union(negAms);

		// Positive favoring union of SREs
		if (amNegResult != null)
			amNegResult =  amNegResult.minus(amPosResult);

		return new SRE(amPosResult.toString(),	amNegResult.toString());

	}

	private static Automaton automatonConj(ArrayList<Automaton> posAms) {
		int i = 0;
		Automaton calcultedPosAm =null;
		for(Iterator<Automaton> it=posAms.iterator(); it.hasNext();i++) {
			if(i==0) {
				calcultedPosAm = it.next();
			}else {
				Automaton temp = it.next();
				if(calcultedPosAm == null || temp == null)
					break;
				else
					calcultedPosAm = calcultedPosAm.intersection(temp);
			}
		}
		return calcultedPosAm;
	}

	private static ArrayList<Automaton> genPosAutomaton(SRE[] sres) {
		return genAutomaton(sres,0);
	}

	private static ArrayList<Automaton> genNegAutomaton(SRE[] sres) {
		return genAutomaton(sres,1);
	}

	private static ArrayList<Automaton> genAutomaton(SRE[] sres, int mode) {
		String[] sreStrs = new String[sres.length];
		for(int i = 0; i<sres.length; i++) {
			if(mode ==0)
				sreStrs[i] = sres[i].positiveRE().trim();
			else
				sreStrs[i] = sres[i].negativeRE().trim();
		}

		ArrayList<Automaton> returnAms = new ArrayList<>();
		for(int i = 0; i<sres.length; i++) {
			returnAms.add(new RegExp(RuntimeUtils.validateStr(sreStrs[i])).toAutomaton());
		}
		return returnAms;
	}

	public static SRE performUOPs(String operator, SRE sre) {
		if (isEmpty(sre))
			return null;
		switch (operator) {
			case "Complement":
				// Switches sign of SRE
				return new SRE(sre.getNegativeRE(), sre.getPositiveRE());

			case "Action":
				// Includes only the actions in SRE
				RegExp rePos = new RegExp(sre.getPositiveRE().replace("%", ".*"));
				RegExp reNeg = new RegExp(sre.getNegativeRE().replace("%", ".*"));
				RegExp action = new RegExp(".+\\(.*\\)");
				Automaton amPos = rePos.toAutomaton();
				Automaton amNeg = reNeg.toAutomaton();
				Automaton amAct = action.toAutomaton();
				return new SRE(amPos.intersection(amAct).toString(), amPos
						.intersection(amAct).toString());

			case "Result":
				// Includes only the results in SRE
				RegExp rePos1 = new RegExp(sre.getPositiveRE().replace("%", ".*"));
				RegExp reNeg1 = new RegExp(sre.getNegativeRE().replace("%", ".*"));
				RegExp action1 = new RegExp(".+\\(.*\\)");
				Automaton amPos1 = rePos1.toAutomaton();
				Automaton amNeg1 = reNeg1.toAutomaton();
				Automaton amAct1 = action1.toAutomaton();
				Automaton resPos = amPos1.minus(amPos1.intersection(amAct1));
				Automaton resNeg = amNeg1.minus(amNeg1.intersection(amAct1));
				return new SRE(resPos.toString(), resNeg.toString());

			case "Positive":
				// Includes only positive portion of SRE
				return new SRE(sre.getPositiveRE(), null);

			case "Negative":
				// Includes only negative portion of SRE
				return new SRE(null, sre.getNegativeRE());
			default:

				return sre;
		}
	}

	public static SRE getBaseSRE(SRE sre) {
		Class<BopSRE> classBS = BopSRE.class;
		Class<UopSRE> classUS = UopSRE.class;
		Class<? extends SRE> classChild = sre.getClass();
		if (classBS.isAssignableFrom(classChild)) { // BopSRE
			BopSRE bopSre = (BopSRE) sre;
			SRE result = SREUtil.performBOPs(bopSre.getSrebop(),
					getBaseSRE(bopSre.sre1), getBaseSRE(bopSre.sre2));
			return result;
		} else if (classUS.isAssignableFrom(classChild)) { // UopSRE
			UopSRE uopSre = (UopSRE) sre;
			SRE result = SREUtil.performUOPs(uopSre.getSreuop(),
					getBaseSRE(uopSre.sre));
			return result;
		} else {
			return sre;
		}
	}


	/**
	 * This function is used to check if the given SRE is Infinite or not.
	 *
	 * @param sre
	 * @return it returns ture if either positiveRE or negativeRE is Infinite
	 */
	public static boolean isInfinite(SRE sre) {
		if (isEmpty(sre))
			return false;

		RegExp rePos = new RegExp(sre.getPositiveRE().replace("%", ".*"));
		RegExp reNeg = new RegExp(sre.getNegativeRE().replace("%", ".*"));
		Automaton amPos = rePos.toAutomaton();
		Automaton amNeg = reNeg.toAutomaton();
		if (!amPos.isFinite() || !amNeg.isFinite())
			return true;
		else
			return false;
	}

	public static boolean isSubSet(SRE sre1, SRE sre2) {
		if (isEmpty(sre1))
			return true;

		RegExp rePos1 = new RegExp(sre1.getPositiveRE().replace("%", ".*"));
		RegExp reNeg1 = new RegExp(sre1.getNegativeRE().replace("%", ".*"));
		Automaton amPos1 = rePos1.toAutomaton();
		Automaton amNeg1 = reNeg1.toAutomaton();

		RegExp rePos2 = new RegExp(sre2.getPositiveRE().replace("%", ".*"));
		RegExp reNeg2 = new RegExp(sre2.getNegativeRE().replace("%", ".*"));
		Automaton amPos2 = rePos2.toAutomaton();
		Automaton amNeg2 = reNeg2.toAutomaton();

		if (amPos1.subsetOf(amPos2) && amNeg1.subsetOf(amNeg2))
			return true;
		else
			return false;
	}

	public static boolean isEquals(SRE sre1, SRE sre2) {
		if (isEmpty(sre1) && isEmpty(sre2))
			return true;

		RegExp rePos1 = new RegExp(sre1.getPositiveRE().replace("%", ".*"));
		RegExp reNeg1 = new RegExp(sre1.getNegativeRE().replace("%", ".*"));
		Automaton amPos1 = rePos1.toAutomaton();
		Automaton amNeg1 = reNeg1.toAutomaton();

		RegExp rePos2 = new RegExp(sre2.getPositiveRE().replace("%", ".*"));
		RegExp reNeg2 = new RegExp(sre2.getNegativeRE().replace("%", ".*"));
		Automaton amPos2 = rePos2.toAutomaton();
		Automaton amNeg2 = reNeg2.toAutomaton();

		if (amPos1.equals(amPos2) && amNeg1.equals(amNeg2))
			return true;
		else
			return false;
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

	public static boolean isEmpty(SRE sre) {
		if(sre == null)
			return true;
		else
			return isSreFieldNull(sre.positiveRE()) && isSreFieldNull(sre.getNegativeRE());
	}


	public static boolean isSreFieldNull(String sreStr) {
		if(sreStr == null)
			return true;
		return (sreStr.equals("null") || sreStr.trim().equals(""));
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