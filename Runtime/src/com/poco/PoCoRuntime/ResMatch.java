package com.poco.PoCoRuntime;

import java.util.ArrayList;

/**
 * This class is extended from the Match, and it is used for the Result case of
 * IRE
 *
 * @author yan
 *
 */
public class ResMatch extends Match {
	private String resultMatchStr; /* use to compare result */

	public ResMatch() {
		super();
		this.resultMatchStr = null;
	}

	public ResMatch(String matchString, String resultMatchStr) {
		super(matchString);
		this.resultMatchStr = resultMatchStr;
	}

	public ResMatch(String matchString) {
		super(matchString);
		this.resultMatchStr = "";
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
		String eventSig = RuntimeUtils.getMethodSignature(event.getSignature());
		// Step 1: check the result
		if (!isWildcard) {
			if (!RuntimeUtils.isMatching(this.resultMatchStr, event.getResult()
					.toString()))
				return false;
		}
		// Step 2: make sure it is from the right method
		ArrayList<String> matchs = SREUtil.splitSreStr(matchString);
		for (int i = 0; i < matchs.size(); i++) {
			String temp = handlVar4Action(matchs.get(i));
			temp = RuntimeUtils.getMethodSignature(temp);
			if (temp != null && RuntimeUtils.matchFunction(eventSig, temp)) {
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