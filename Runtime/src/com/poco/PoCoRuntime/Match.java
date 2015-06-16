package com.poco.PoCoRuntime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Single unit for matching a PoCo event or result.
 */
public class Match implements Matchable {
	protected String matchString;
	protected boolean isWildcard;

	public Match() {
		this.isWildcard = false;
		this.matchString = null;
	}

	/**
	 * This constructor creates an Action match (assuming is action by default)
	 * 
	 * @param matchString
	 */
	public Match(String matchString) {
		// This constructor creates an Action match
		isWildcard = (matchString == "%");
		this.matchString = matchString;
	}

	public boolean isWildcard() {
		return isWildcard;
	}

	public void setWildcard(boolean isWildcard) {
		this.isWildcard = isWildcard;
	}

	/**
	 * Constructor for Match when it is result instead of action
	 * 
	 * @param matchString
	 * @param isAction
	 * @param isResult
	 * @param resultMatchStr
	 */
	public Match(String matchString, boolean boolUop, String resultMatchStr) {
		this.matchString = matchString;
		isWildcard = (matchString == "%");
	}

	public String getMatchString() {
		return matchString;
	}

	public void setMatchString(String matchString) {
		this.matchString = matchString;
	}

	@Override
	public String toString() {
		return "Match [matchString=" + matchString + ", isWildcard="
				+ isWildcard + "]";
	}

	public String getEventSig(Event evt) {
		return evt.getSignature(); 
	}

	@Override
	/**
	 * if match the event, then return true, else return false
	 */
	public boolean accepts(Event event) {
		if (isWildcard) {
			return RuntimeUtils.matchFunction(getEventSig(event), matchString);
		}else {
			// if it is the action, just match the event signature with
			// matchString
			String[] matchs = RuntimeUtils.getMethodSignature(matchString)
					.split("\\|");
			for (int i = 0; i < matchs.length; i++) {
				if (RuntimeUtils.matchFunction(getEventSig(event), matchs[i]))
					return true;
			}
			return false;
		}
	}

}
