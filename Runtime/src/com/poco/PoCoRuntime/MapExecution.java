package com.poco.PoCoRuntime;

import java.util.ArrayList;

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

	@Override
	public SRE query(Event event) {
		SRE resultSRE = super.query(event);
		resultSRE = resultSRE.getAbsVal();
		// perform BOP on resultSRE
		resultSRE = SREUtil.performBOPs(operator, matchSre.getAbsVal(),
				resultSRE);
		return resultSRE;
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