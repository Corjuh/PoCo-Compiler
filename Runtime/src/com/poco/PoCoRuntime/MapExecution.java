package com.poco.PoCoRuntime;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caoyan on 12/3/14.
 */
public class MapExecution extends SequentialExecution implements Queryable,
		Matchable {
	private String operator;
	private SRE matchSre = null;

	public MapExecution(String modifier, String operator, SRE matchSre)
			throws PoCoException {
		super(modifier);
		this.operator = operator;
		this.matchSre = matchSre;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public void setMatchSre(SRE matchSre) {
		this.matchSre = matchSre;
	}

	public MapExecution(String modifier) throws PoCoException {
		super(modifier);
	}

	// use to set the current modifier for the first child before start query,
	// and later update modifier while advance cursor
	public boolean getCurrentChildModifier(String str) {
		if (currentCursor < children.size()) {
			if (str.equals("isZeroPlus"))
				return ((AbstractExecution) this.children.get(currentCursor))
						.isZeroPlus();
			else
				return ((AbstractExecution) this.children.get(currentCursor))
						.isOnePlus();
		} else
			return false;
	}

	@Override
	public SRE query(Event event) {
		if (children.size() == 0 || exhausted)
			return null;
		EventResponder currentChild = children.get(currentCursor);
		if (currentChild.accepts(event)) {
			String pos = null, neg = null;
			resultBool = true;
			if (!getCurrentChildModifier("isZeroPlus")
					&& !getCurrentChildModifier("isOnePlus")) {
				advanceCursor();
			}
			resultSRE = currentChild.query(event);
			if (resultSRE != null) {
				pos = resultSRE.getPositiveRE();
				neg = resultSRE.getNegativeRE();
			}
			if (pos != null)
				pos = getValue(pos);
			if (neg != null) {
				neg = getValue(neg);
				if (neg.indexOf('(') != -1)
					neg = neg.substring(0, neg.indexOf('('));
			}
			resultSRE = new SRE(pos, neg);
			resultSRE = SREUtil.performBOPs(operator, matchSre, resultSRE);
			return resultSRE;
		} else { // not accepting
			if (getCurrentChildModifier("isZeroPlus")) {
				// We can skip a zero-plus (*) modifier
				advanceCursor();
				this.query(event);
			} else {
				// CurrentChild doesn't accept and can't be skipped
				resultBool = false;
				return null;
			}
		}

		return null;
	}

	@Override
	public boolean accepts(Event event) {
		if (children.size() == 0) {
			return false;
		}
		boolean result = children.get(currentCursor).accepts(event);
		while (!result) {
			if (getCurrentChildModifier("isZeroPlus")) {
				advanceCursor();
				result = children.get(currentCursor).accepts(event);
			} else
				break;
		}
		return result;
	}

	@Override
	public void addChild(EventResponder child) {
		super.addChild(child);
	}

	@Override
	public ArrayList<EventResponder> getChildren() {
		return super.getChildren();
	}

	@Override
	public String toString() {
		return "MapExecution [operator=" + operator + ", matchSre=" + matchSre
				+ ", isZeroPlus=" + isZeroPlus + ", isOnePlus=" + isOnePlus
				+ ", children=" + children + "]\n";
	}

	public String getValue(String strSre) {
		// have to manipulate the string for new case, since signature will
		// not include new keyword
		if (strSre.indexOf('(') != -1) {
			String funName = strSre.substring(0, strSre.indexOf('('));
			String reg = "(.*)(\\$\\$(.+)\\$\\$)(.*)";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(funName);
			boolean needUpdate = matcher.find();
			while (needUpdate) {
				// need delete (, otherwise cause issues.
				String replaceStr = DataWH.closure.get(matcher.group(3).trim());
				funName = funName.replace(matcher.group(2).trim(), replaceStr);
				matcher = pattern.matcher(funName);
				needUpdate = matcher.find();
			}
			String argPrt = strSre.substring(strSre.indexOf('('),
					strSre.length());
			if (funName.substring(funName.length()-4,funName.length()).equals(".new"))
				strSre = funName.substring(0, funName.length() - 4) + argPrt;
		}
		return strSre;
	}
}