package com.poco.PoCoRuntime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Single unit for matching a PoCo event or result.
 */
public class Match implements Matchable {
    private String  matchString;
    private boolean isWildcard;
    private boolean isAction;
    private boolean isResult;
    private String  resultMatchStr; /* use to compare result*/

    public Match () {
        this.isAction       = true;
        this.isResult       = false;
        this.isWildcard     = false;
        this.matchString    = null;
        this.resultMatchStr = null;
    }

    /**
     * This constructor creates an Action match (assuming is action by default)
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
     * @param matchString
     * @param isAction
     * @param isResult
     * @param resultMatchStr
     */
    public Match(String matchString, boolean isAction, boolean isResult, boolean boolUop,String resultMatchStr) {
        this.matchString    = matchString;
        this.isAction       = isAction;
        this.isResult       = isResult;
        this.resultMatchStr = resultMatchStr;
        isWildcard = (matchString == "%");
    }

    public boolean isAction() {
        return isAction;
    }

    public void    setAction(boolean isAction) {
        this.isAction = isAction;
    }

    public boolean isResult() {
        return isResult;
    }

    public void    setResult(boolean isResult) {
        this.isResult = isResult;
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
    public boolean accepts(Event event) {
        if (isWildcard) {
            return true;
        }

        Pattern pattern = Pattern.compile(matchString);
        Matcher matcher = pattern.matcher(event.getSignature());

        return matcher.find();
    }
}
