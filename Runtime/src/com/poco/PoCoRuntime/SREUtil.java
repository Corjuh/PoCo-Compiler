package com.poco.PoCoRuntime;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caoyan on 1/8/15.
 */

public class SREUtil {
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
	public static SRE performBOPs(String operator, SRE sre1, SRE sre2) {
		if (isEmpty(sre1) && isEmpty(sre2))
			return null;

		String positiveRE = null, negativeRE = null;
		switch (operator) {
		case "Union":
		case "Punion":
		case "Disjunction":
			if (sre1.positiveRE().trim().equals(sre2.positiveRE().trim()))
				positiveRE = sre1.positiveRE().trim();
			else if (sre1.positiveRE().trim()
					.contains(sre2.positiveRE().trim()))
				positiveRE = sre1.positiveRE().trim();
			else if (sre2.positiveRE().trim()
					.contains(sre1.positiveRE().trim()))
				positiveRE = sre2.positiveRE().trim();
			else
				positiveRE = operate("Union", sre1.positiveRE(),
						sre2.positiveRE());

			if (sre1.negativeRE().trim().equals(sre2.negativeRE().trim()))
				negativeRE = sre1.negativeRE().trim();
			else if (sre1.negativeRE().trim()
					.contains(sre2.negativeRE().trim()))
				negativeRE = sre1.negativeRE().trim();
			else if (sre2.negativeRE().trim()
					.contains(sre1.negativeRE().trim()))
				negativeRE = sre2.negativeRE().trim();
			else
				negativeRE = operate("Union", sre1.negativeRE(),
						sre2.negativeRE());
			
			if (operator.equals("Union")) {
				// Positive favoring union of SREs
				if (negativeRE != null)
					negativeRE = operate("Minus", negativeRE, positiveRE);
			} else if (operator.equals("Punion")) {
				// Negative favoring union of SREs
				if (positiveRE != null)
					positiveRE = operate("Minus", positiveRE, negativeRE);
			}
			break;
		case "Conjunction":
			// pos = (pos1 n pos2) U (neg1 n neg2);
		case "Equals":
			if (sre1.positiveRE().trim().equals(sre2.positiveRE().trim()))
				positiveRE = sre1.positiveRE().trim();
			else if (sre1.positiveRE().trim()
					.contains(sre2.positiveRE().trim()))
				positiveRE = sre2.positiveRE().trim();
			else if (sre2.positiveRE().trim()
					.contains(sre1.positiveRE().trim()))
				positiveRE = sre1.positiveRE().trim();
			else
				positiveRE = operate("InterSection", sre1.positiveRE(),
						sre2.positiveRE());

			if (operator.equals("Union")) {
				if (sre1.negativeRE().trim().equals(sre2.negativeRE().trim()))
					negativeRE = sre1.negativeRE().trim();
				else if (sre1.negativeRE().trim()
						.contains(sre2.negativeRE().trim()))
					negativeRE = sre1.negativeRE().trim();
				else if (sre2.negativeRE().trim()
						.contains(sre1.negativeRE().trim()))
					negativeRE = sre2.negativeRE().trim();
				else
					negativeRE = operate("Union", sre1.negativeRE(),
							sre2.negativeRE());
			} else {
				if (sre1.negativeRE().trim().equals(sre2.negativeRE().trim()))
					negativeRE = sre1.negativeRE().trim();
				else if (sre1.negativeRE().trim()
						.contains(sre2.negativeRE().trim()))
					negativeRE = sre2.negativeRE().trim();
				else if (sre2.negativeRE().trim()
						.contains(sre1.negativeRE().trim()))
					negativeRE = sre1.negativeRE().trim();
				else
					negativeRE = operate("InterSection", sre1.negativeRE(),
							sre2.negativeRE());
				positiveRE = operate("Union", positiveRE, negativeRE);
				// nes = % - pos
				negativeRE = performUOPs("Complement",
						new SRE(positiveRE, null)).getNegativeRE();
			}
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
	 * @param sre
	 *            It is the first SRE value
	 * @return the result SRE
	 */
	public static SRE performUOPs(String operator, SRE sre) {
		if (isEmpty(sre))
			return null;
		switch (operator) {
		case "Complement": // Switches sign of SRE
			return new SRE(sre.getNegativeRE(), sre.getPositiveRE());
		case "Action": // Includes only the actions in SRE
			RegExp rePos = new RegExp(sre.getPositiveRE().replace("%", ".*"));
			RegExp reNeg = new RegExp(sre.getNegativeRE().replace("%", ".*"));
			RegExp action = new RegExp(".+\\(.*\\)");
			Automaton amPos = rePos.toAutomaton();
			Automaton amNeg = reNeg.toAutomaton();
			Automaton amAct = action.toAutomaton();
			return new SRE(amPos.intersection(amAct).toString(), amPos
					.intersection(amAct).toString());

		case "Result": // Includes only the results in SRE
			RegExp rePos1 = new RegExp(sre.getPositiveRE().replace("%", ".*"));
			RegExp reNeg1 = new RegExp(sre.getNegativeRE().replace("%", ".*"));
			RegExp action1 = new RegExp(".+\\(.*\\)");
			Automaton amPos1 = rePos1.toAutomaton();
			Automaton amNeg1 = reNeg1.toAutomaton();
			Automaton amAct1 = action1.toAutomaton();
			Automaton resPos = amPos1.minus(amPos1.intersection(amAct1));
			Automaton resNeg = amNeg1.minus(amNeg1.intersection(amAct1));
			return new SRE(resPos.toString(), resNeg.toString());

		case "Positive": // Includes only positive portion of SRE
			return new SRE(sre.getPositiveRE(), null);
		case "Negative": // Includes only negative portion of SRE
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
			// System.out.println(result);
			return result;
		} else if (classUS.isAssignableFrom(classChild)) { // UopSRE
			UopSRE uopSre = (UopSRE) sre;
			SRE result = SREUtil.performUOPs(uopSre.getSreuop(),
					getBaseSRE(uopSre.sre));
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
		if ((sre1 == null || sre1.trim().equals(""))
				&& (sre2 == null || sre2.trim().equals("")))
			return null;
		String returnRe = null;
		if (!sre1.trim().equals("")) { // sre1pos is not empty
			if (!sre2.trim().equals("")) { // case both positive is not empty
				RegExp re1 = new RegExp(validateStr(sre1));
				RegExp re2 = new RegExp(validateStr(sre2));
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

	private static boolean isEmpty(SRE sre) {
		String srePos = sre.positiveRE();
		String sreNeg = sre.negativeRE();
		if ((srePos.equals("null") || srePos.trim().equals(""))
				&& (sreNeg.equals("null") || sreNeg.trim().equals("")))
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

	private static String validateStr(String str) {
		return str.replaceAll("(%|\\$[a-zA-Z0-9\\.\\-_]+)", "").replaceAll(
				"#|\\{|\\}", "");
	}

	public static boolean StringMatch(String matchingVal, String matchingRegex) {
		String  regex = matchingRegex.replace(".", "\\.").replace("*", "(.*)");
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(matchingVal);
		if (matcher.find())
			return true;
		return false;
	}

	/**
	 * trim the first six letters from the full class name (e.g., trim
	 * "class java.lang.reflect.Method" to "java.lang.reflect.Method")
	 * 
	 * @param fullClassName
	 * @return
	 */
	public static String trimClassName(String fullClassName) {
		if (fullClassName != null && fullClassName.length() >= 6)
			if (fullClassName.startsWith("class "))
				return fullClassName.substring(6, fullClassName.length());
		return fullClassName;

	}

	public static String concatClsMethod(String className, String methodName) {
		return className.trim().concat(".").concat(methodName.trim());
	}
}
