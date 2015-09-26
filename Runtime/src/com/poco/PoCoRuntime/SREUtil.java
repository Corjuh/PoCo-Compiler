package com.poco.PoCoRuntime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

public class SREUtil {
	/**
	 * This function is used to perform Strategy on given SREs.
	 *
	 * @param operator
	 *            the strategy wants to be performed
	 * @param sres
	 *            a list of SRES that strategy will be performed on
	 * @return
	 */
	public static CalculatedSRE performStrategy(String operator,
												ArrayList<SRE> sres) {
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
			case "Complement":
			case "Action":
			case "Result":
			case "Positive":
			case "Negative":
				return performUOPs(operator, sres.get(0));
			default:
				// will add example for user-defined logic
				PoCoCombinators combinator = new PoCoCombinators();
				combinator.setLogic("operator");
				return combinator.performLogic(sres).convert2CalculatedSre();
		}
	}

	/**
	 * A binary operation will be performed on two SREs.
	 *
	 * @param operator
	 *            the strategy wants to be performed
	 * @param sre1
	 *            the first SRE will be performed on
	 * @param sre2
	 *            the second SRE will be performed on
	 * @return
	 */
	public static CalculatedSRE performBOPs(String operator, SRE sre1, SRE sre2) {
		if (isSREValNeutral(sre1) && isSREValNeutral(sre2))
			return null;
		ArrayList<SRE> sres = new ArrayList<SRE>();
		sres.add(sre1);
		sres.add(sre2);
		return performBOPs(operator, sres);
	}

	/**
	 * A binary operation will be performed on a list of SREs.
	 *
	 * @param operator
	 *            the binary operator
	 * @param sres
	 *            list of SRES that will be performed on
	 * @return
	 */
	public static CalculatedSRE performBOPs(String operator, ArrayList<SRE> sres) {
		if (sres.size() == 0)
			return null;

		if (sres.size() == 1)
			return sres.get(0).convert2CalculatedSre();

		switch (operator) {
			case "Union":
				return unionOp(sres);
			case "Punion":
				return punionOp(sres);
			case "Disjunction":
				return disjunctionOp(sres);
			case "Conjunction":
				return conjunctionOp(sres);
			default: // Equals
				return equalsOp(sres);
		}
	}

	/**
	 * Optimistic Union (i.e., Positive favoring union) a set of SRES
	 *
	 * @param sres
	 *            a set of SREs that need to be unioned
	 * @return a SRE with unioned value, alone with a set of positive SREs and a
	 *         set of negative SRES
	 */
	private static CalculatedSRE unionOp(ArrayList<SRE> sres) {
		return unionPunionOp(sres, 0);
	}

	/**
	 * Pessimistic Union (i.e., Negative favoring union) a set of SRES
	 *
	 * @param sres
	 * @return
	 */
	private static CalculatedSRE punionOp(ArrayList<SRE> sres) {
		return unionPunionOp(sres, 1);
	}

	private static CalculatedSRE unionPunionOp(ArrayList<SRE> sres, int mode) {
		if (sres.size() == 0)
			return null;

		CalculatedSRE retSRE = new CalculatedSRE();
		// 1. Convert sre to convert2CalculatedSre(vars value will be loaded)
		for (SRE sre : sres)
			sre = sre.convert2CalculatedSre();

		// 2. collect all the distinct positive and negative REs
		ArrayList<Automaton> posAms = genPosAutomaton(sres, 0);
		ArrayList<Automaton> negAms = genNegAutomaton(sres, 0);

		// 3. calculate the unioned positive value
		retSRE.setPosAutomaton(performUnionOp(posAms));

		// 4. calculate the unioned negative value
		retSRE.setNegAutomaton(performUnionOp(negAms));

		// 5. union those automatons, and since Positive favoring union of
		// SREs, need subtract it from negative
		if (retSRE.getPosAutomaton() != null
				&& retSRE.getNegAutomaton() != null) {
			if (mode == 0) // union
				retSRE.setNegAutomaton(retSRE.getNegAutomaton().minus(
						retSRE.getPosAutomaton()));
			else
				// mode == 1 punion
				retSRE.setPosAutomaton(retSRE.getNegAutomaton().minus(
						retSRE.getPosAutomaton()));
		}
		// ================================================================
		// 6. Add all the concrete events and results
		retSRE.addConcreteMethods(getConcreteMethods(sres));
		retSRE.addConcreteResults(getConcreteResults(sres));

		return retSRE;
	}

	private static HashSet<String> getConcreteMethods(ArrayList<SRE> sres) {
		HashSet<String> hashset = new HashSet<String>();
		for (SRE sre : sres) {
			if (!isSREValNeutral(sre))
				hashset.addAll(sre.convert2CalculatedSre().getConcreteMethods());
		}
		return hashset;
	}

	private static HashSet<String> getConcreteResults(ArrayList<SRE> sres) {
		HashSet<String> hashset = new HashSet<String>();
		for (SRE sre : sres) {
			if (!isSREValNeutral(sre))
				hashset.addAll(sre.convert2CalculatedSre().getConcreteResults());
		}
		return hashset;
	}

	private static Automaton performUnionOp(ArrayList<Automaton> ams) {
		if (ams == null || ams.size() == 0)
			return null;

		CalculatedSRE retSRE = null;
		int count = ams.size();
		if (ams.size() == 1)
			return ams.get(0);
		else
			return Automaton.union(ams);
	}

	private static CalculatedSRE equalsOp(ArrayList<SRE> sres) {
		// pos = (pos1 n pos2) U (neg1 n neg2);
		// nes = % - pos
		if (sres.size() == 0)
			return null;
		// 1. declare the return value
		CalculatedSRE retSRE = new CalculatedSRE();

		// 2. collect all the distinct positive and negative REs
		ArrayList<Automaton> posAms = genPosAutomaton(sres, 0);
		ArrayList<Automaton> negAms = genNegAutomaton(sres, 0);

		// 3. perform intersection operation on those those automatons
		Automaton amPosResult = automatonConj(posAms);
		Automaton amNegResult = automatonConj(negAms);

		// 4. Subtract positive from negative
		amPosResult = amPosResult.union(amNegResult);
		amNegResult = amPosResult.complement();

		// 5. add the conjuncted value to the returnSRE
		retSRE.setPosAutomaton(amPosResult);
		retSRE.setNegAutomaton(amNegResult);

		// ================================================================
		// 6. Add all the concrete events and results
		retSRE.addConcreteMethods(getConcreteMethods(sres));
		retSRE.addConcreteResults(getConcreteResults(sres));

		// 7. return
		return retSRE;
	}

	private static CalculatedSRE conjunctionOp(ArrayList<SRE> sres) {
		return conDisjuncOp(sres, 0);
	}

	private static CalculatedSRE disjunctionOp(ArrayList<SRE> sres) {
		return conDisjuncOp(sres, 1);
	}

	private static CalculatedSRE conDisjuncOp(ArrayList<SRE> sres, int mode) {
		if (sres.size() == 0)
			return null;
		// 1. Declare returnSRE
		CalculatedSRE retSRE = new CalculatedSRE();

		// 2. collect all the distinct positive and negative REs, and generate
		// Automatons. if the sre is just a simple SRE, then directly get info
		// from its RE values; if it is an UopSRE, then need load from its SRE
		// field in order to generate correct automaton
		ArrayList<Automaton> posAms = genPosAutomaton(sres, 0);
		ArrayList<Automaton> negAms = genNegAutomaton(sres, 0);

		// 3. perform union and intersection operation on those those automatons
		if (mode == 0) {
			// conjunction
			retSRE.setPosAutomaton(automatonConj(posAms));
			retSRE.setNegAutomaton(performUnionOp(negAms));
		} else {
			// disjunction
			retSRE.setPosAutomaton(performUnionOp(posAms));
			retSRE.setNegAutomaton(automatonConj(negAms));
		}
		// ================================================================
		// 4. Add all the concrete events and results
		retSRE.addConcreteMethods(getConcreteMethods(sres));
		retSRE.addConcreteResults(getConcreteResults(sres));

		return retSRE;
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

	public static ArrayList<Automaton> genPosAutomaton(ArrayList<SRE> sres,
													   int mode) {
		return genPosNegAutomaton(sres, 0, mode);
	}

	public static ArrayList<Automaton> genNegAutomaton(ArrayList<SRE> sres,
													   int mode) {
		return genPosNegAutomaton(sres, 1, mode);
	}

	private static ArrayList<Automaton> genPosNegAutomaton(ArrayList<SRE> sres,
														   int posNegFlag, int mode) {
		ArrayList<Automaton> ret = new ArrayList<Automaton>();
		for (SRE sre : sres) {
			if (!isSREValNeutral(sre)) {
				Automaton temp = null;
				if (posNegFlag == 0)
					temp = sre.convert2CalculatedSre().getPosAutomaton();
				else
					temp = sre.convert2CalculatedSre().getNegAutomaton();

				if (temp != null || mode == 1)
					ret.add(temp);
			} else if (mode == 1)
				ret.add(null);
		}
		return ret;
	}

	public static CalculatedSRE performUOPs(String operator, SRE sre) {
		if (isSREValNeutral(sre))
			return null;

		switch (operator) {
			// Switches sign of SRE
			case "Complement":
				return sre.complement().convert2CalculatedSre();

			// Includes only the actions in SRE
			case "Action":
				return sre.getAction().convert2CalculatedSre();

			// Includes only the results in SRE
			case "Result":
				return sre.getResult().convert2CalculatedSre();

			// Includes only positive portion of SRE
			case "Positive":
				return sre.getPostiveVal().convert2CalculatedSre();

			// Includes only negative portion of SRE
			default:// "Negative":
				return sre.getNegativeVal().convert2CalculatedSre();
		}
	}

	/**
	 * This function is used to check if the given SRE is Infinite or not.
	 *
	 * @param sre
	 * @return it returns ture if either positiveRE or negativeRE is Infinite
	 */
	public static boolean isInfinite(SRE sre) {
		if (isSREValNeutral(sre))
			return false;

		CalculatedSRE calcSre = sre.convert2CalculatedSre();

		return (!calcSre.getPosAutomaton().isFinite() || !calcSre
				.getNegAutomaton().isFinite());
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
		if (isSREValNeutral(sre1))
			return true;
		else if (isSREValNeutral(sre2))
			// if sre1 is not null and sre2 is null return false
			return false;

		// generate automaton for both sre1 and sre2
		CalculatedSRE am1 = sre1.convert2CalculatedSre();
		CalculatedSRE am2 = sre2.convert2CalculatedSre();

		return (am1.getPosAutomaton().subsetOf(am2.getPosAutomaton()) && am1
				.getNegAutomaton().subsetOf(am2.getNegAutomaton()));
	}

	public static boolean isEquals(SRE sre1, SRE sre2) {
		if (isSREValNeutral(sre1) && isSREValNeutral(sre2))
			return true;
		// generate automaton for both sre1 and sre2
		CalculatedSRE am1 = sre1.convert2CalculatedSre();
		CalculatedSRE am2 = sre2.convert2CalculatedSre();

		if (am1.getPosAutomaton().equals(am2.getPosAutomaton())
				&& am1.getNegAutomaton().equals(am2.getNegAutomaton()))
			return true;
		else
			return false;
	}

	/**
	 * This function checks whether a given SRE is neutral(i.e., both the
	 * positive value and negative value are null).
	 *
	 * @param sre
	 * @return
	 */
	public static boolean isSREValNeutral(SRE sre) {
		if (sre == null) {
			return true;
		} else {
			CalculatedSRE am = sre.convert2CalculatedSre();
			return (am.getPosAutomaton() == null || am.getPosAutomaton()
					.isEmpty())
					&& (am.getNegAutomaton() == null || am.getNegAutomaton()
					.isEmpty());
		}
	}

	public static boolean isSreFieldNull(String sreStr) {
		return (sreStr == null || sreStr.equals("null") || sreStr.trim()
				.equals(""));
	}

	public static String getPatternFrmType(String poCoObjTyp) {
		switch (poCoObjTyp) {
			case "int":
			case "Integer":
				return "[0-9]+";
			case "float":
			case "double":
				return "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";
			case "boolean":
				return "ture|false";
			default:
				return "*";
		}

	}

	public static String validateSREStr(String str) {
		if (str == null || str.trim().length() == 0)
			return null;

		// if this SRE string specifies only one event with no alternation
		if (!hasAlternation(str)) {
			return validateStr(str);
		} else {
			// this is the case where the SRE string specifies multiply events.
			// Within each SRE string, we assume that there will be no nested
			// alternations between events.
			ArrayList<String> strs = splitSreStr(str);
			if (strs.size() == 1) {
				// This is the case that the alternation happens within the
				// individual event,
				// With in each individual event, we also assume that the nested
				// alternations are non-existing.
				return getAlternationVal(strs.get(0));
			} else {
				for (int i = 0; i < strs.size(); i++) {
					if (hasAlternation(strs.get(i)))
						strs.set(i, getAlternationVal(strs.get(i)));
					else
						strs.set(i, validateStr(strs.get(i)));
				}
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				for (int i = 0; i < strs.size(); i++) {
					sb.append("(" + strs.get(i) + ")");
					if (i != strs.size() - 1)
						sb.append("|");
				}
				sb.append(")");
				return sb.toString();
			}
		}
	}

	public static String getAlternationVal(String str) {
		if (str == null)
			return null;

		int[] barIndexes = SREUtil.getSplitIndexes(str, "|");
		if (barIndexes == null || barIndexes.length == 0)
			return validateStr(str);

		String head = "";
		String fstEle = str.substring(0, barIndexes[0]);
		String lasEle = str.substring(barIndexes[barIndexes.length - 1] + 1);
		String tail = "";

		if (fstEle.length() > 0) {
			int pindex1 = fstEle.lastIndexOf('(');
			int cindex1 = fstEle.lastIndexOf('{');
			int max1 = Math.max(pindex1, cindex1);

			int pindex2 = lasEle.lastIndexOf(')');
			int cindex2 = lasEle.lastIndexOf('}');
			int min2 = -1;
			if (pindex2 == -1)
				min2 = cindex2;
			else {
				if (cindex2 == -1)
					min2 = pindex2;
				else
					min2 = Math.min(pindex2, cindex2);
			}

			int found = 0;
			if (max1 == -1)
				found = 0;
			else if (pindex1 == max1 && pindex2 == min2)
				found = 1;
			else if (cindex1 == max1 && cindex2 == min2)
				found = 2;

			switch (found) {
				case 1: // ")"
					head = fstEle.substring(0, pindex1 + 1);
					fstEle = fstEle.substring(pindex1 + 1);
					lasEle = lasEle.substring(0, pindex2);
					tail = lasEle.substring(pindex2);
					int sIndex1 = getSplitPoint(fstEle);
					if (sIndex1 != -1) {
						head += fstEle.substring(0, sIndex1 + 1);
						fstEle = fstEle.substring(sIndex1 + 1);
					}
					break;
				case 2:// "}""
					head = fstEle.substring(0, cindex1 + 1);
					fstEle = fstEle.substring(cindex1 + 1);
					tail = lasEle.substring(cindex2);
					lasEle = lasEle.substring(0, cindex2);
					int sIndex2 = getSplitPoint(fstEle);
					if (sIndex2 != -1) {
						head += fstEle.substring(0, sIndex2 + 1);
						fstEle = fstEle.substring(sIndex2 + 1);
					}
					break;
				default:
					break;
			}

			StringBuilder sb = new StringBuilder();
			sb.append(validateStr(head));
			sb.append("((" + validateStr(fstEle) + ")|");

			if (barIndexes.length > 1) {
				for (int i = 1; i < barIndexes.length; i++) {
					String temp = str.substring(barIndexes[i - 1] + 1,
							barIndexes[i]);
					if (temp.trim().length() > 0) {
						sb.append("(" + validateStr(temp) + ")");
						sb.append("|");
					}
				}
			}
			if (lasEle.length() > 0)
				sb.append("(" + validateStr(lasEle) + "))");
			else {
				sb.deleteCharAt(sb.length() - 1);
				sb.append(")");
			}
			sb.append(validateStr(tail));
			return sb.toString();
		}
		return null;
	}

	public static String validateStr(String str) {
		if (str == null)
			return null;

		return str.replace(".", "\\.").replace("(", "\\(").replace("$", "\\$")
				.replace(")", "\\)").replace("{", "\\{").replace("}", "\\}")
				.replace("#", "\\#").replace("@", "\\@").replace("?", "\\?")
				.replace("*", "(.*)");
	}

	public static boolean containsVariable(String str) {
		if (isSreFieldNull(str))
			return false;
		return isContaintheChar(str, "$");
	}

	public static Automaton genMethodAutomaton() {
		RegExp method = new RegExp(
				"[A-Z_a-z][A-Z_a-z0-9]*(\\.[A-Z_a-z][A-Z_a-z0-9]*)*\\(.*\\)");
		return method.toAutomaton();
	}

	/**
	 * check if the given automaton is a method automaton or not
	 *
	 * @param am
	 * @return
	 */
	public static boolean isMethod(Automaton am) {
		Automaton amAct = genMethodAutomaton();
		return am.subsetOf(amAct);
	}

	/**
	 * In PoCo, "|" can be within a SRE to separate each individual event or
	 * event pattern, but nested alternation will not be allowed.
	 * -`java.io.File.<init>(#String{%})|java.io.File.<init>(\*,#String{%})'
	 * And, within each individual event or event pattern, alternation (but no
	 * nested alternation) can also be used. For example, the
	 * "FileWriter.<init>(#String{%.mdb|%.bad})|FileWriter.<init>(#String{%.mdb|%.bad},boolean)"
	 *
	 * @param str
	 * @return
	 */
	public static ArrayList<String> splitSreStr(String str) {
		ArrayList<String> retStrs = new ArrayList<String>();
		int splitIndex = str.indexOf('|');

		// step 1, get all the index for the "|"s (indexes for "|" literals are
		// excluded)
		int[] barIndexes = getSplitIndexes(str, "|");
		// this is the case that alternation is non-existing.
		if (barIndexes == null) {
			retStrs.add(str);
		} else {
			int startIndex = 0;
			for (int index : barIndexes) {
				String leftStr = str.substring(startIndex, index);
				if (RuntimeUtils.isMethod(leftStr)
						|| RuntimeUtils.isPoCoObject(leftStr)
						|| RuntimeUtils.isVariableCase(leftStr)) {
					// add the leftStr to the array list first, then locate for
					// next alternation
					retStrs.add(str.substring(startIndex, index));
					if (index == str.length() - 1) {
						inValidSREstr(str);
					} else
						startIndex = index + 1;
				} else {
					if (index == str.length() - 1) {
						inValidSREstr(str);
					}
				}
			}
			if (startIndex + 1 < str.length())
				retStrs.add(str.substring(startIndex));
		}
		return retStrs;
	}

	private static int getSplitPoint(String str) {
		int index1 = str.lastIndexOf(':');
		int index2 = str.lastIndexOf(' ');
		int index3 = str.lastIndexOf(',');
		int index4 = str.lastIndexOf(';');
		int[] ints = { index1, index2, index3, index4 };
		Arrays.sort(ints);
		for (int index : ints) {
			if (index > -1)
				return index;
		}
		return -1;
	}

	/**
	 * This function returns the indexes of all occurrences of specific
	 * character (not escaped) in a string,
	 *
	 * @param str
	 * @param split
	 * @return
	 */
	private static int[] getSplitIndexes(String str, String split) {
		if (!str.contains(split))
			return null;

		ArrayList<Integer> indexes = new ArrayList<Integer>();
		int index = str.indexOf(split);
		while (index != -1) {
			if (index - 1 >= 0 && str.substring(index - 1, index).equals("\\")) {
				if (index + 1 < str.length())
					index = str.indexOf(split, index + 1);
				else
					break;
			} else {
				indexes.add(index);
				index = str.indexOf(split, index + 1);
			}
		}

		int[] indexs = null;
		if (indexes.size() > 0) {
			int count = indexes.size();
			indexs = new int[count];
			for (int i = 0; i < count; i++) {
				indexs[i] = Integer.valueOf(indexes.get(i));
				if (i > 0 && (indexs[i - 1] + 1) == indexs[i])
					inValidSREstr(str);
			}
		}
		return indexs;
	}

	public static boolean hasAlternation(String str) {
		return (getSplitIndexes(str, "|") != null);
	}

	public static boolean isContaintheChar(String str, String theChar) {
		return (getSplitIndexes(str, theChar) != null);
	}

	public static boolean reContainNotMatch(String str) {
		if (str == null)
			return false;
		// We assume that there will be no nested PoCo object
		Pattern pattern = Pattern.compile("!#(.+)\\{(.+)\\}");
		Matcher matcher = pattern.matcher(str);
		return matcher.find();
	}

	public static String getNotMatchStr(String str) {
		Pattern pattern = Pattern.compile("!#(.+)\\{(.+)\\}");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find())
			return matcher.group(0);
		else
			return null;
	}

	private static void inValidSREstr(String str) {
		System.err.println("The specified SRE: \"" + str + "\" is invalid!");
		System.exit(-1);
	}
}