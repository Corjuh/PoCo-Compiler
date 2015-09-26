package com.poco.PoCoRuntime;

import java.util.ArrayList;
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
	 * This constructor creates an Action match
	 *
	 * @param matchString
	 */
	public Match(String matchString) {
		// This constructor creates an Action match
		isWildcard = (matchString == "_");
		this.matchString = matchString;
	}

	public boolean isWildcard() {
		return isWildcard;
	}

	public void setWildcard(boolean isWildcard) {
		this.isWildcard = isWildcard;
	}

	public String getMatchString() {
		return matchString;
	}

	public void setMatchString(String matchString) {
		isWildcard = (matchString == "_");
		this.matchString = matchString;
	}

	@Override
	public String toString() {
		return "Match [matchString=" + matchString + ", isWildcard="
				+ isWildcard + "]";
	}

	protected String getEventSig4Match(Event evt) {
		return evt.sig4MatchSre;
	}

	public String getEventSig(Event evt) {
		return evt.getSignature();
	}

	@Override
	/**
	 * if match the event, then return true, else return false
	 */
	public boolean accepts(Event event) {
		if (isWildcard)
			return true;

		// When the matchString is empty (match is rewild case) then
		// directly return true
		if (this.matchString == null || this.matchString.trim().length() == 0)
			return true;

		// if it is the action, just match the event signature with
		// matchString
		ArrayList<String> matchs = SREUtil.splitSreStr(matchString);
		for (int i = 0; i < matchs.size(); i++) {
			String temp = handlVar4Action(matchs.get(i));
			if (temp != null
					&& RuntimeUtils.matchFunction(handlVar4Action(getEventSig4Match(event)),
					temp)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * there are 3 sub-cases for each string to match an action, a. it is a
	 * method string without containing any variables --
	 * e.g.,`java.io.FileWriter.<init>(#java.lang.String{.mdb|.bad}) -- check
	 * method signature, and certain arg value as requested
	 *
	 * b. a variable is used for save method name -- e.g., $call =
	 * `java.io.FileWriter.<init>(#String{.mdb|.bad})' -- check method signature
	 * and certain arg value as requested -- if its arg contains variables, will
	 * be take care as case c.
	 *
	 * c. it is a method string w/ containing var(s) as its arguments --
	 * e.g.,`com.poco.RuntimeDemo.ShowDialog($message)' -- check method
	 * signature, and certain arg value as requested
	 *
	 * (if it is an object method call -- e.g.,`$ig.start()' -- in this case,
	 * this object method call must be a promoted case, and since it is
	 * promoted, it will only be the case checking the result. Therefore, this
	 * case cannot be existed in action match. )
	 *
	 * Nevertheless, since the AspectJ already handles the method signature
	 * comparison for matching signatures, thereby certain arg value as
	 * requested
	 *
	 * @param sreStrVal
	 * @return
	 */
	protected String handlVar4Action(String sreStrVal) {
		if (!SREUtil.isContaintheChar(sreStrVal, "$"))
			return sreStrVal;

		if (sreStrVal.startsWith("$")) {
			String varName = RuntimeUtils.getVariableName(sreStrVal);
			if (!DataWH.dataVal.containsKey(varName)) {
				System.err.println("The \"" + varName
						+ "\" is an invalid variable name.");
				System.exit(-1);
			}
			String typ = DataWH.dataVal.get(varName.substring(1)).getType();
			if (typ != null && typ.equals("java.lang.String")) {
				sreStrVal = sreStrVal.replace(varName,
						RuntimeUtils.getStrValFrmDataWH(varName));
			} else {
				// the variable is an object will be called
				String newVal = Integer.toString(System
						.identityHashCode(RuntimeUtils.getObjFrmDbWH(varName)));
				sreStrVal = sreStrVal.replace(varName, newVal);
			}
		}

		if (!SREUtil.containsVariable(sreStrVal))
			return sreStrVal;

		String funName = RuntimeUtils.getMethodInfos(sreStrVal);
		String argStr = RuntimeUtils.getfunArgstr(sreStrVal);

		if (argStr != null && argStr.trim().length() > 0) {
			String[] args = argStr.split(",");
			for (int i = 0; i < args.length; i++) {
				if (SREUtil.containsVariable(args[i])) {
					String varName = RuntimeUtils.getVariableName(args[i]);
					if (!DataWH.dataVal.containsKey(varName.substring(1))) {
						System.err.println("The \"" + varName
								+ "\" is an invalid variable name.");
						System.exit(-1);
					} else {
						String typ = DataWH.dataVal.get(varName.substring(1))
								.getType();
						Object val = DataWH.dataVal.get(varName.substring(1))
								.getObj();
						if (args[i].startsWith("$")) {
							args[i] = RuntimeUtils.getVar(typ, val, false);
						} else {
							if (RuntimeUtils.isPrimitiveType(typ))
								args[i] = args[i].replace(varName, val.toString().replace(",", ";"));
							else {
								int valAddress = System.identityHashCode(val);
								args[i] = args[i].replace(varName, Integer.toString(valAddress));
							}
						}
					}
				}
			}
			argStr = RuntimeUtils.strArrJoin(args,",");
		}
		return funName + "(" + argStr+ ")";
	}
}