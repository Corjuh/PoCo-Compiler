package com.poco.PoCoRuntime;

/**
 * Created by yan on 6/11/15.
 */
public class AtMatch implements Matchable {
    private String matchValue;

    public AtMatch() {
        this.matchValue = null;
    }

    /**
     * This constructor creates an Action match (assuming is action by default)
     *
     * @param matchString
     */
    public AtMatch(String matchString) {
        // This constructor creates an Action match
        this.matchValue = matchString;
    }


    public String getMatchString() {
        return matchValue;
    }

    public void setMatchString(String matchString) {
        this.matchValue = matchString;
    }

    @Override
    public String toString() {
        return "Match [matchValue=" + matchValue + "]";
    }

    @Override
    /**
     * if match the event, then return true, else return false
     */
    public boolean accepts(Event event) {
        if (matchValue == null )
            return false;
        else if(RuntimeUtils.isVariable(matchValue.substring(1))){
            if(DataWH.dataVal.get(matchValue.substring(1)).getObj() != null)
                return true;
            else
                return false;
        }
        else  return true;
    }
}
