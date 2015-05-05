package com.poco.PoCoRuntime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Single unit for matching a PoCo event or result.
 */
public class Match implements Matchable {
	private String matchString;
	private boolean isWildcard;
	private boolean isAction;
	private boolean isResult;
	private String resultMatchStr; /* use to compare result */

	public Match() {
		this.isAction = true;
		this.isResult = false;
		this.isWildcard = false;
		this.matchString = null;
		this.resultMatchStr = null;
	}

	/**
	 * This constructor creates an Action match (assuming is action by default)
	 * 
	 * @param matchString
	 */
	public Match(String matchString) {
		// This constructor creates an Action match
		isAction = true;
		isResult = false;
		isWildcard = (matchString == "%");
		this.matchString = matchString;
	}

	/**
	 * Constructor for Match when it is result instead of action
	 * 
	 * @param matchString
	 * @param isAction
	 * @param isResult
	 * @param resultMatchStr
	 */
	public Match(String matchString, boolean isAction, boolean isResult,
			boolean boolUop, String resultMatchStr) {
		this.matchString = matchString;
		this.isAction = isAction;
		this.isResult = isResult;
		this.resultMatchStr = resultMatchStr;
		isWildcard = (matchString == "%");
	}

	public boolean isAction() {
		return isAction;
	}

	public void setAsAction() {
		this.isAction = true;
		this.isResult = false;
	}

	public boolean isResult() {
		return isResult;
	}

	public void setAsResult() {
		this.isAction = false;
		this.isResult = true;
	}

	public String getMatchString() {
		return matchString;
	}

	public void setMatchString(String matchString) {
		this.matchString = matchString;
	}

	public String getResultMatchStr() {
		return resultMatchStr;
	}

	public void setResultMatchStr(String resultMatchStr) {
		this.resultMatchStr = resultMatchStr;
	}

	@Override
	public String toString() {
		return "Match [matchString=" + matchString + ", isWildcard="
				+ isWildcard + ", isAction=" + isAction + ", isResult="
				+ isResult + ", resultMatchStr=" + resultMatchStr + "]";
	}

	@Override
	/**
	 * if match the event, then return true, else return false
	 */
	public boolean accepts(Event event) {
		if (isWildcard) 	return true;
		if (!isAction && event.getResult() == null) 	return false;

		// if it is the action, just match the event signature with matchString
		String reg = "(.*)(\\$\\$(.+)\\$\\$)(.*)";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(matchString);

		boolean needUpdate = matcher.find();
		while (needUpdate) {
			// need delete (, otherwise cause issues.
			String replaceStr = DataWH.closure.get(matcher.group(3).trim());
			if (replaceStr.indexOf('(') != -1)
				replaceStr = replaceStr.substring(0, replaceStr.indexOf('('));
			matchString = matchString.replace(matcher.group(2).trim(),
					replaceStr);
			matcher = pattern.matcher(matchString);
			needUpdate = matcher.find();
		}

		// if the matchstrings are compound re, then we need check sub-item
		String[] matchs = matchString.split("\\|");
		if (isAction) {
			for (int i = 0; i < matchs.length; i++) {
				String[] funRtnTypName = getfunTypName(matchs[i]);
				String[] sigRtnTypName = getfunTypName(event.getSignature());
				if (funRtnTypName[0].equals("*")
						|| sigRtnTypName[0].equals("*")
						|| funRtnTypName[0].contains(sigRtnTypName[0])) {
					pattern = Pattern.compile(matchs[i]);
					matcher = pattern.matcher(event.getSignature());
					boolean res = matcher.find();
					if (!res && i < matchs.length - 1)
						continue;
					return res;
				} 
			}
		} else {
			// if it is result, we have to permit the action in order to get the
			// result. If action not done yet, permit action first then get 
			//action result back, compare the result with resultMatchStr
			boolean result;
			for (int i = 0; i < matchs.length; i++) {
				String[] funRtnTypName = getfunTypName(matchs[i]);
				String[] sigRtnTypName = getfunTypName(event.getSignature());
				// if one of the return type is *, the return type must match.
				// otherwise check both value
				if (funRtnTypName[0].equals("*")
						|| sigRtnTypName[0].equals("*")
						|| funRtnTypName[0].contains(sigRtnTypName[0])) {
					if (funRtnTypName[1].endsWith("()"))
						funRtnTypName[1] = funRtnTypName[1].substring(0,
								funRtnTypName[1].length() - 2);
					if (sigRtnTypName[1]
							.contains("java.lang.reflect.Method.invoke")) {
						if (event.getPromotedMethod()
								.contains(funRtnTypName[1])) {
							pattern = Pattern.compile(resultMatchStr);
							matcher = pattern.matcher(event.getResult()
									.toString());
							result = matcher.find();
							if (!result && i < matchs.length - 1)
								continue;
							return result;
						}
					} else {
						if (sigRtnTypName[1].contains(funRtnTypName[1])) {
							pattern = Pattern.compile(resultMatchStr);
							matcher = pattern.matcher(event.getResult().toString());
							result = matcher.find();
							if (!result && i < matchs.length - 1)
								continue;
							return result;
						}
					}
				} 
			}
		}
		return false;
	}
	
	public String[] getfunTypName(String funStr) {
		String[] returnStr = new String[2];
		String[] temp = funStr.trim().split("\\s+");
		if (temp.length == 2) {
			returnStr[0] = temp[0].trim();
			returnStr[1] = temp[1].trim();
		} else {
			returnStr[0] = "*";
			returnStr[1] = funStr.trim();
		}
		return returnStr;
	}
}
