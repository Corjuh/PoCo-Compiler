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

    /**
     * This constructor creates an Action match
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
     */
    public Match(String matchString, boolean isaction, boolean isresult) {

        isAction = isaction;
        isResult = isresult;

        isWildcard = (matchString == "%");
        this.matchString = matchString;
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
