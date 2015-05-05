package com.poco.PoCoRuntime;

import java.util.ArrayList;

/**
 * Tree of Match objects. What's the plural of Match? Matchs, of course!
 *
 * Matchs can have a unary (NOT) or binary (AND/OR) operator associated with them, which will change their behavior
 * when querying their children.
 */
public class Matchs implements Matchable {
    private ArrayList<Matchable> children;
    private boolean isAND = false;
    private boolean isOR = false;
    private boolean isNOT = false;

    public void setAND(boolean isAND) {
        this.isAND = isAND;
    }

    public void setOR(boolean isOR) {
        this.isOR = isOR;
    }

    public void setNOT(boolean isNOT) {
        this.isNOT = isNOT;
    }

    /**
     * Constructs a Matchs object with given operator (if any).
     * @param operator modifier of execution (e.g. &&, ||, or !)
     * @throws PoCoException
     */
    public Matchs(String operator) throws PoCoException {
        setOperator(operator);
        children = new ArrayList<>();
    }

    public Matchs() throws PoCoException {
        this("");
    }

    /**
     * Adds a Matchable object as a child of this Matchs.
     * @param child Matchable object to be added
     * @throws PoCoException
     */
    public void addChild(Matchable child) throws PoCoException {
        if (isNOT && children.size() >= 1) {
            // NOT is a unary operator, so we should only have one child.
            throw new PoCoException("Trying to add more than one child to a NOT Matchs object");
        }

        children.add(child);
    }

    /**
     * Sets the operator ('&&', '||', '!') of this Matchs object. Raises exception if unknown value is provided.
     * @param operator String either '&&', '||', or '!'
     * @throws PoCoException
     */
    public void setOperator(String operator) throws PoCoException {
        if (operator == null) {
            isAND = isOR = isNOT = false;
            return;
        }

        switch (operator) {
            case "&&":
                isAND = true;
                isOR = isNOT = false;
                break;
            case "||":
                isOR = true;
                isAND = isNOT = false;
                break;
            case "!":
                isNOT = true;
                isAND = isOR = false;
                break;
            case "":
                isNOT = isAND = isOR = false;
                break;
            default:
                throw new PoCoException("Incorrect operator '" + operator + "' set on Matchs object!");
        }
    }

    /**
     * Queries children to see if they accept the PoCo event. For OR Matchs, the query is short-circuited. Empty
     * Matchs objects always return false.
     * @param event PoCo event provided by AspectJ pointcut
     * @return whether this Matchs' children (and itself) accept or can provide a result for the event.
     */
    @Override
    public boolean accepts(Event event) {  
        // An empty Matchs object does not accept anything
        if (children.size() == 0) {
            return false;
        }
        // Short-circuited OR operation
        if (isOR) {
            for (Matchable child : children) {
                if (child.accepts(event))  
                    return true;
            }

            return false;
        } 
        // Both AND and NOT require all children to be evaluated
        boolean[] results = new boolean[children.size()];
        for (int i = 0; i < children.size(); i++) {
            results[i] = children.get(i).accepts(event); 
        }
         
        if (isNOT) {  
            return !results[0];
        } else if (isAND) {
            // AND all results together to get final result
            boolean combo = true;
            for (boolean result : results) {
                combo = combo && result;
            }
            return combo;
        }
        else { //Single child and isNot=false
        	return results[0];
        }
        //return false;
    }

    /*
     *  Status Methods
     */
    public boolean isAND() {
        return isAND;
    }

    public boolean isOR() {
        return isOR;
    }

    public boolean isNOT() {
        return isNOT;
    }

    public int getNumChildren() {
        return children.size();
    }

	@Override
	public String toString() {
		return "Matchs [children=" + children + ", isAND=" + isAND + ", isOR="
				+ isOR + ", isNOT=" + isNOT + "]";
	}
    
    
}
