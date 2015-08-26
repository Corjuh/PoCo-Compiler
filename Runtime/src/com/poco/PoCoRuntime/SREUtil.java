package com.poco.PoCoRuntime;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by caoyan on 1/8/15.
 */

public class SREUtil {

	// use for case of operate on two SREs
	public static SRE performBOPs(String operator, SRE sre1, SRE sre2) {
		if (isEmpty(sre1) && isEmpty(sre2))
			return null;
		else {
			ArrayList<SRE> sres = new ArrayList<SRE>();
			sres.add(sre1);
			sres.add(sre2);
			return performBOPs(operator, sres);
		}
	}

	public static SRE performBOPs(String operator, ArrayList<SRE> sres) {
		switch (operator) {
			case "Union":
				return unionOp(sres);
			case "Punion":
				return punionOp(sres);
			case "Disjunction":
				return disjunctionOp(sres);
			case "Conjunction":
				return conjunctionOp(sres);
			case "Equals":
				return equalsOp(sres);
			default:
				return sres.get(0);
		}
	}

	/**
	 * Optimistic Union a set of SRES, with Positive favoring union of SREs
	 *
	 * @param sres
	 *            a set of SREs that need to be unioned
	 * @return a SRE with unioned value, alone with a set of positive SREs and a
	 *         set of negative SRES
	 */
	private static SRE unionOp(ArrayList<SRE> sres) {
		// 1. Declare returnSRE
		CalculatedSRE returnSRE = new CalculatedSRE();

		// 2. collect all the distinct positive and negative REs, and generate
		// Automatons if the sre is just a simple SRE, then directly get info
		// from its RE values, NEEDS to handle the VARIABLE case;
		// if it is an UopSRE, then need load from its SRE
		// field in order to generate correct automaton.
		ArrayList<Automaton> posAms = genPosAutomaton(sres);
		ArrayList<Automaton> negAms = genNegAutomaton(sres);

		// 3. union those automatons, and since Positive favoring union of SREs,
		// need subtract it from negative
		Automaton amPosResult = Automaton.union(posAms);
		Automaton amNegResult = Automaton.union(negAms);
		// Subtract positive from negative
		if (amNegResult != null)
			amNegResult = amNegResult.minus(amPosResult);

		// 4. add all those positive values into returnSRE since it is favored
		// by optimistic union, we can just direct at all.
//		for (SRE sre : sres) {
//			if (isCalculatedSRE(sre))
//				returnSRE.AddPosSREs(((CalculatedSRE) sre).getPosSREs());
//			else {
//				SRE temp = new SRE(sre.getPositiveRE(), null);
//				if(!isEmpty(temp))
//					returnSRE.AddPosSRE(temp);
//			}
//		}

		// 5. only those in the unioned negative value will be added to
		// returnSRE,we also need to handle the variable case for checking
		// if it satisfies the condition, that is be subset of Negvals
//		ArrayList<String> negReStrs = getNegREsfrmSre(sres);
//		ArrayList<Automaton> negReAms = genAutomaton(negReStrs);
//		int i = 0;
//		for (Automaton autom : negReAms) {
//			if (autom.subsetOf(amNegResult))
//				returnSRE.AddNegSRE(new SRE(null, negReStrs.get(i)));
//			i++;
//		}

		// 6. add the unioned value to the returnSRE
		returnSRE.setPositiveRE(amPosResult.toString());
		returnSRE.setNegativeRE(amNegResult.toString());

		return returnSRE;
	}

	/**
	 * Pessimistic Union a set of SRES, with Negative favoring union of SREs
	 *
	 * @param sres
	 * @return
	 */
	private static SRE punionOp(ArrayList<SRE> sres) {
		// 1. Declare returnSRE
		CalculatedSRE returnSRE = new CalculatedSRE();

		// 2. collect all the distinct positive and negative REs, and generate
		// Automatons. if the sre is just a simple SRE, then directly get info
		// from its RE values; if it is an UopSRE, then need load from its SRE
		// field in order to generate correct automaton
		ArrayList<Automaton> posAms = genPosAutomaton(sres);
		ArrayList<Automaton> negAms = genNegAutomaton(sres);

		// 3. union those automatons, and since Positive favoring union of SREs,
		// need subtract it from negative
		Automaton amPosResult = Automaton.union(posAms);
		Automaton amNegResult = Automaton.union(negAms);
		// Subtract negative from positive
		if (amPosResult != null)
			amPosResult = amPosResult.minus(amNegResult);

		// 4. add all those negative RE values into returnSRE since it is
		// favored
		// by pessimistic union
//		for (SRE sre : sres) {
//			SRE temp = new SRE(null, sre.getNegativeRE());
//			if(!isEmpty(temp))
//				returnSRE.AddNegSRE(temp);
//		}

		// 5. only those in the unioned positive value will be added to
		// returnSRE, posReStrs will just be the orginal RE values, and
		// variable will be handled within genAutomaton function, since
		// we should add original variable name to the returnSRE
//		ArrayList<String> posReStrs = getPosREsfrmSre(sres);
//		ArrayList<Automaton> posReAms = genAutomaton(posReStrs);
//		int i = 0;
//		for (Automaton autom : posReAms) {
//			if (autom.subsetOf(amPosResult))
//				returnSRE.AddPosSRE(new SRE(posReStrs.get(i), null));
//			i++;
//		}

		// 6. add the unioned value to the returnSRE
		returnSRE.setPositiveRE(amPosResult.toString());
		returnSRE.setNegativeRE(amNegResult.toString());

		return returnSRE;
	}

	private static SRE equalsOp(ArrayList<SRE> sres) {
		// pos = (pos1 n pos2) U (neg1 n neg2);
		// nes = % - pos

		// 1. Declare returnSRE
		CalculatedSRE returnSRE = new CalculatedSRE();

		// 2. collect all the distinct positive and negative REs, and generate
		// Automatons. if the sre is just a simple SRE, then directly get info
		// from its RE values; if it is an UopSRE, then need load from its SRE
		// field in order to generate correct automaton
		ArrayList<Automaton> posAms = genPosAutomaton(sres);
		ArrayList<Automaton> negAms = genNegAutomaton(sres);

		// 3. perform intersection operation on those those automatons
		Automaton amPosResult = automatonConj(posAms);
		Automaton amNegResult = automatonConj(negAms);

		// 4. Subtract positive from negative
		amPosResult = amPosResult.union(amNegResult);
		amNegResult = amPosResult.complement();

		// 5. only those in the conjuncted positive value will be added to
		// returnSRE, posReStrs will just be the original RE values, and
		// variable will be handled within genAutomaton function, since
		// we should add original variable name to the returnSRE
//		ArrayList<String> reStrs = getPosREsfrmSre(sres);
//		ArrayList<Automaton> reAms = genAutomaton(reStrs);
//		int i = 0;
//		for (Automaton autom : reAms) {
//			if (autom.subsetOf(amPosResult))
//				returnSRE.AddPosSRE(new SRE(reStrs.get(i), null));
//			i++;
//		}

		// 6. only those in the conjuncted negative value will be added to
		// returnSRE,we also need to handle the variable case for checking
		// if it satisfies the condition, that is be subset of Negvals
//		i = 0;
//		reStrs = getNegREsfrmSre(sres);
//		reAms = genAutomaton(reStrs);
//		for (Automaton autom : reAms) {
//			if (autom.subsetOf(amNegResult))
//				returnSRE.AddNegSRE(new SRE(null, reStrs.get(i)));
//			i++;
//		}

		// 7. add the conjuncted value to the returnSRE
		returnSRE.setPositiveRE(amPosResult.toString());
		returnSRE.setNegativeRE(amNegResult.toString());

		return returnSRE;
	}

	/**
		conjunction(ArrayList<SRE> sres) 
		1. returnSRE.pos = intersection(sres.pos)
		2. returnSRE.neg = union(sres.neg) 
	*/
	private static SRE conjunctionOp(ArrayList<SRE> sres) {
		// 1. Declare returnSRE
		CalculatedSRE returnSRE = new CalculatedSRE();

		// 2. collect all the distinct positive and negative REs, and generate
		// Automatons. if the sre is just a simple SRE, then directly get info
		// from its RE values; if it is an UopSRE, then need load from its SRE
		// field in order to generate correct automaton
		ArrayList<Automaton> posAms = genPosAutomaton(sres);
		ArrayList<Automaton> negAms = genNegAutomaton(sres);

		// 3. perform intersection operation on those those automatons
		Automaton amPosResult = automatonConj(posAms);
		Automaton amNegResult = Automaton.union(negAms);

		// 4. only those in the conjuncted positive value will be added to
		// returnSRE, posReStrs will just be the original RE values, and
		// variable will be handled within genAutomaton function, since
		// we should add original variable name to the returnSRE
//		ArrayList<String> reStrs = getPosREsfrmSre(sres);
//		ArrayList<Automaton> reAms = genAutomaton(reStrs);
//		int i = 0;
//		for (Automaton autom : reAms) {
//			if (autom.subsetOf(amPosResult))
//				returnSRE.AddPosSRE(new SRE(reStrs.get(i), null));
//			i++;
//		}

		// 5. only those in the unioned negative value will be added to
		// returnSRE,we also need to handle the variable case for checking
		// if it satisfies the condition, that is be subset of Negvals
//		i = 0;
//		reStrs = getNegREsfrmSre(sres);
//		reAms = genAutomaton(reStrs);
//		for (Automaton autom : reAms) {
//			if (autom.subsetOf(amNegResult))
//				returnSRE.AddNegSRE(new SRE(null, reStrs.get(i)));
//			i++;
//		}

		// 6. add the unioned value to the returnSRE
		returnSRE.setPositiveRE(amPosResult.toString());
		returnSRE.setNegativeRE(amNegResult.toString());

		return returnSRE;
	}

	/**
	disjunction(ArrayList<SRE> sres) 
	1. returnSRE.pos = union(sres.pos)
	2. returnSRE.neg = intersection(sres.pos)
	*/
	private static SRE disjunctionOp(ArrayList<SRE> sres) {
		// 1. Declare returnSRE
		CalculatedSRE returnSRE = new CalculatedSRE();

		// 2. collect all the distinct positive and negative REs, and generate
		// Automatons. if the sre is just a simple SRE, then directly get info
		// from its RE values; if it is an UopSRE, then need load from its SRE
		// field in order to generate correct automaton
		ArrayList<Automaton> posAms = genPosAutomaton(sres);
		ArrayList<Automaton> negAms = genNegAutomaton(sres);

		// 3. union those automatons,
		Automaton amPosResult = Automaton.union(posAms);
		Automaton amNegResult = automatonConj(negAms);

		// 4. add all pos and neg RE values into returnSRE since it is
//		for (SRE sre : sres) {
//			if (isCalculatedSRE(sre)) {
//				returnSRE.AddPosSREs(((CalculatedSRE) sre).getPosSREs());
//				returnSRE.AddNegSREs(((CalculatedSRE) sre).getNegSREs());
//			} else {
//				SRE temp = new SRE(sre.getPositiveRE(), null);
//				if(!isEmpty(temp))
//					returnSRE.AddPosSRE(temp);
//
//				temp = new SRE(null, sre.getNegativeRE());
//				if(!isEmpty(temp))
//					returnSRE.AddNegSRE(temp);
//			}
//		}

		// 5. add the unioned value to the returnSRE
		returnSRE.setPositiveRE(amPosResult.toString());
		returnSRE.setNegativeRE(amNegResult.toString());

		return returnSRE;
	}

	private static Automaton automatonConj(ArrayList<Automaton> posAms) {
		int i = 0;
		Automaton calcultedPosAm = null;
		for (Iterator<Automaton> it = posAms.iterator(); it.hasNext(); i++) {
			if (i == 0) {
				calcultedPosAm = it.next();
			} else {
				Automaton temp = it.next();
				if (calcultedPosAm == null || temp == null)
					break;
				else
					calcultedPosAm = calcultedPosAm.intersection(temp);
			}
		}
		return calcultedPosAm;
	}

	/**
	 * this function is used to collect all the positive and negative REs
	 * from sres, that is, -- for sre case, will collect from its positiveRE
	 * and negativeRE fields -- for CalculatedSRE case, will collect info
	 * from posSREs and negSREs fields
	 *
	 * @param sres  mode get the positive value when mode ==0
	 * @return
	 */

	public static ArrayList<String> getPosREsfrmSre(ArrayList<SRE> sres) {
		return getPosNegREsfrmSre(sres, 0);
	}

	public static ArrayList<String> getNegREsfrmSre(ArrayList<SRE> sres) {
		return getPosNegREsfrmSre(sres, 1);
	}

	private static ArrayList<String> getPosNegREsfrmSre(ArrayList<SRE> sres,
														int mode) {
		HashSet<String> hashset = new HashSet<String>();

		for (SRE sre : sres) {
			// 1. get the proper RE value from sre, that is
			// if it is just a simple sre, then directly load RE value from it.
			// if it is an UopSRE, then we will need to get RE value from its
			// SRE
//			if (isCalculatedSRE(sre)) {
//				CalculatedSRE temp = (CalculatedSRE) sre;
//				if (mode == 0)
//					hashset.addAll(getPosNegREsfrmSre(temp.getPosSREs(), mode));
//				else
//					hashset.addAll(getPosNegREsfrmSre(temp.getNegSREs(), mode));
//			} else { // a simple SRE case
				// 2. get the positive value when mode ==0
				if (mode == 0) {
					if (!isSreFieldNull(sre.getPositiveRE()))
						hashset.add(sre.getPositiveRE().trim());
				} else {
					if (!isSreFieldNull(sre.getNegativeRE()))
						hashset.add(sre.getNegativeRE().trim());
				}
			//}
		}
		return new ArrayList<String>(hashset);
	}

	public static ArrayList<Automaton> genPosAutomaton(ArrayList<SRE> sres) {
		ArrayList<String> sreStrs = getPosREsfrmSre(sres);
		//variable case will be handled in genAutomaton function
		return genAutomaton(sreStrs);
	}

	public static ArrayList<Automaton> genNegAutomaton(ArrayList<SRE> sres) {
		ArrayList<String> sreStrs = getNegREsfrmSre(sres);
		//variable case will be handled in genAutomaton function
		return genAutomaton(sreStrs);
	}

	private static ArrayList<Automaton> genAutomaton(ArrayList<String> sreStrs) {
		ArrayList<Automaton> returnAms = new ArrayList<>();
		for (String str : sreStrs) {
			if (RuntimeUtils.isVariable(str))
				str = RuntimeUtils.loadValFrmDataWH(str);
			returnAms.add(new RegExp(RuntimeUtils.validateStr(str))
					.toAutomaton());
		}
		return returnAms;
	}

	public static SRE performUOPs(String operator, SRE sre) {
		if (isEmpty(sre))
			return null;
		switch (operator) {
			// Switches sign of SRE
			case "Complement":
				return sre.complement();

			// Includes only the actions in SRE
			case "Action":
				return sre.getAction();

			// Includes only the results in SRE
			case "Result":
				return sre.getResult();

			// Includes only positive portion of SRE
			case "Positive":
				return sre.getPostiveVal();

			// Includes only negative portion of SRE
			case "Negative":
				return sre.getNegativeVal();

			default:
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

		// generate automaton for both sre1 and sre2
		Automaton[] am = genAutomaton4SRE(sre);
		if (!am[0].isFinite() || !am[1].isFinite())
			return true;
		else
			return false;
	}

	/**
	 * return turn if sre1 is the subset of sre2
	 *
	 * @param sre1
	 * @param sre2
	 * @return
	 */
	public static boolean isSubSet(SRE sre1, SRE sre2) {
		// if sre1 is null then always return true
		if (isEmpty(sre1))
			return true;

		// generate automaton for both sre1 and sre2
		Automaton[] am1 = genAutomaton4SRE(sre1);
		Automaton[] am2 = genAutomaton4SRE(sre2);

		if (am1[0].subsetOf(am2[0]) && am1[1].subsetOf(am2[1]))
			return true;
		else
			return false;
	}

	public static boolean isEquals(SRE sre1, SRE sre2) {
		if (isEmpty(sre1) && isEmpty(sre2))
			return true;

		// generate automaton for both sre1 and sre2
		Automaton[] am1 = genAutomaton4SRE(sre1);
		Automaton[] am2 = genAutomaton4SRE(sre2);

		if (am1[0].equals(am2[0]) && am1[1].equals(am2[1]))
			return true;
		else
			return false;
	}

	public static boolean isEmpty(SRE sre) {
		if (sre == null) {
			return true;
		} else {
			// handle the variable case
			String posREstr = sre.getPositiveRE();
			String negREstr = sre.getNegativeRE();
			// handle the variable case
			if (RuntimeUtils.isVariable(posREstr))
				posREstr = RuntimeUtils.loadValFrmDataWH(posREstr);
			if (RuntimeUtils.isVariable(negREstr))
				negREstr = RuntimeUtils.loadValFrmDataWH(negREstr);

			return isSreFieldNull(posREstr) && isSreFieldNull(negREstr);
		}
	}

	public static boolean isSreFieldNull(String sreStr) {
		if (sreStr == null)
			return true;
		return (sreStr.equals("null") || sreStr.trim().equals(""));
	}

//	public static boolean isCalculatedSRE(SRE sre) {
//		Class<CalculatedSRE> calcSreClass = com.poco.PoCoRuntime.CalculatedSRE.class;
//		Class<? extends SRE> thisClass = sre.getClass();
//		return calcSreClass.isAssignableFrom(thisClass);
//	}

	public static boolean isUopSre(SRE sre) {
		Class<UopSRE> uopSreClass = com.poco.PoCoRuntime.UopSRE.class;
		Class<? extends SRE> thisClass = sre.getClass();
		return uopSreClass.isAssignableFrom(thisClass);
	}

	public static boolean isBopSre(SRE sre) {
		Class<BopSRE> bopSreClass = com.poco.PoCoRuntime.BopSRE.class;
		Class<? extends SRE> thisClass = sre.getClass();
		return bopSreClass.isAssignableFrom(thisClass);
	}

	public static Automaton[] genAutomaton4SRE(SRE sre) {
		Automaton[] returnVal = new Automaton[2];
		// 1. generate automaton for sre1, need to handle variable case
		// need handle if the sre is UopSRE case
		// if (isUopSRE(sre)) {
		// sre = ((UopSRE) sre).getSre();
		// if (sre == null)
		// sre = sre.genSRE();
		// }
		String posREstr = sre.getPositiveRE();
		String negREstr = sre.getNegativeRE();
		// handle the variable case
		if (RuntimeUtils.isVariable(posREstr))
			posREstr = RuntimeUtils.loadValFrmDataWH(posREstr);
		if (RuntimeUtils.isVariable(negREstr))
			negREstr = RuntimeUtils.loadValFrmDataWH(negREstr);

		RegExp rePos = new RegExp(posREstr.replace("%", ".*"));
		RegExp reNeg = new RegExp(negREstr.replace("%", ".*"));

		return new Automaton[] { rePos.toAutomaton(), reNeg.toAutomaton() };
	}
}