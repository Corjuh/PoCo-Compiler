package com.poco.PoCoRuntime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResMatch extends Match {
	private String resultMatchStr; /* use to compare result */

	public ResMatch() {
		super();
		this.resultMatchStr = null;
	}

	public ResMatch(String matchString, boolean boolUop, String resultMatchStr) {
		super(matchString, boolUop, resultMatchStr);
		this.resultMatchStr = resultMatchStr;
	}

	public ResMatch(String matchString) {
		super(matchString);
		this.resultMatchStr = resultMatchStr;
	}

	public void setResultMatchStr(String resultMatchStr) {
		this.resultMatchStr = resultMatchStr;
	}

	public String getResultMatchStr() {
		return resultMatchStr;
	}

	@Override
	/**
	 * if match the event, then return true, else return false
	 */
	public boolean accepts(Event event) {
		String eventSig = getEventSig(event);

		if (isWildcard)
			return RuntimeUtils.matchFunction(eventSig, matchString);

		if (event.getResult() == null)
			return false;

		// if it is the action, just match the event signature with matchString
		// if the matchstrings are compound re, then we need check sub-item

		String[] matchs = RuntimeUtils.getMethodSignature(matchString).split(
				"\\|");

		// if it is result, we have to permit the action in order to get the
		// result. If action not done yet, permit action first then get
		// action result back, compare the result with resultMatchStr
		boolean result;
		for (int i = 0; i < matchs.length; i++) {
			if (RuntimeUtils.matchFunction(eventSig, matchs[i])) {
				String reg = this.resultMatchStr;
				Pattern ptn = Pattern.compile(reg);
				Matcher mth = ptn.matcher(event.getResult().toString());
				if (mth.find())
					return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "Match [matchString=" + matchString + ", isWildcard="
				+ isWildcard + ", resultMatchStr=" + resultMatchStr + "]";
	}
}