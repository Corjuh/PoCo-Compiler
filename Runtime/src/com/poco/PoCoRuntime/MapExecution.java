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
	public boolean childrenExhausted() {
		if (this.children.size() > 0 && currentCursor < this.children.size()) {
			Class<AbstractExecution> classAE = AbstractExecution.class;
			Class<? extends EventResponder> classChild = children.get(
					this.currentCursor).getClass();
			if (classAE.isAssignableFrom(classChild)) {
				boolean result = false;
				AbstractExecution temp = (AbstractExecution) children
						.get(this.currentCursor);
				if (temp.exhausted == true)
					result = true;
				return result;
			}
		}
		return true;
	}

	@Override
	public SRE query(Event event) {
		if (children.size() == 0 || exhausted)
			return null;
		EventResponder currentChild = children.get(currentCursor);

		String pos = null, neg = null;
		resultBool = true;
		resultSRE = currentChild.query(event);
		if (resultSRE != null) {
			pos = resultSRE.getPositiveRE();
			neg = resultSRE.getNegativeRE();
		}
		if (neg != null) {
			if (neg.indexOf('(') != -1)
				neg = neg.substring(0, neg.indexOf('('));
		}
		resultSRE = new SRE(pos, neg);
		resultSRE = SREUtil.performBOPs(operator, matchSre, resultSRE);

		if (!getCurrentChildModifier("isZeroPlus")
				&& !getCurrentChildModifier("isOnePlus") && childrenExhausted())
				advanceCursor();
		
		return resultSRE;

	}

	@Override
	public boolean accepts(Event event) {
		if (children.size() == 0) {
			return false;
		}
		boolean result = children.get(currentCursor).accepts(event);

		if (!result && currentCursor != children.size() - 1) {
			while (!result) {
				if (getCurrentChildModifier("isZeroPlus")) {
					advanceCursor();
					result = children.get(currentCursor).accepts(event);
				} else {
					break;
				}
			}
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
		return "MapExecution [currentCursor" + currentCursor + "; operator="
				+ operator + ", matchSre=" + matchSre + ", isZeroPlus="
				+ isZeroPlus + ", isOnePlus=" + isOnePlus + ", children="
				+ children + "]\n";
	}

}
