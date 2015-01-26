package com.poco.PoCoRuntime;

import java.util.ArrayList;

/**
 * Created by caoyan on 12/3/14.
 */
public class MapExecution extends SequentialExecution implements Queryable,
        Matchable {
    private String operator;
    private SRE matchSre = null;

    private Boolean isQueried = false;
    private Boolean resultBool = false;
    private SRE operatedSRE = null;

    public MapExecution(String modifier, String operator, SRE matchSre)
            throws PoCoException {
        super(modifier);
        this.operator = operator;
        this.matchSre = matchSre;
        this.operatedSRE = matchSre;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setMatchSre(SRE matchSre) {
        this.matchSre = matchSre;
        this.operatedSRE = matchSre;
    }

    public MapExecution(String modifier) throws PoCoException {
        super(modifier);
    }

    // use to set the current modifier for the first child before start query,
    // and later update modifier while advance cursor
    public boolean getCurrentChildModifier(String str) {
        if (currentCursor < children.size()) {
            if (str.equals("isZeroPlus"))
                return ((AbstractExecution) this.children.get(currentCursor))
                        .isZeroPlus();
            else
                return ((AbstractExecution) this.children.get(currentCursor))
                        .isOnePlus();
        } else
            return false;
    }

    private void advanceCursor() {
        if (isZeroPlus || isOnePlus)
            currentCursor = (currentCursor + 1) % children.size();
        else
            currentCursor++;
        if (currentCursor >= children.size())
            exhausted = true;
    }

    @Override
    public SRE query(Event event) {
        if (children.size() == 0 || exhausted) {
            return null;
        }
        if (!isQueried) {
            EventResponder currentChild = children.get(currentCursor);
            if (currentChild.accepts(event)) {
                isQueried = true;
                resultBool = true;
                if (!getCurrentChildModifier("isZeroPlus")
                        && !getCurrentChildModifier("isOnePlus")) {
                    advanceCursor();
                }
                operatedSRE = SREUtil.performBOPs(operator, operatedSRE, currentChild.query(event));
                return operatedSRE;
            } else { // not accepting
                if (getCurrentChildModifier("isZeroPlus")) {
                    // We can skip a zero-plus (*) modifier
                    advanceCursor();
                    this.query(event);
                } else {
                    // CurrentChild doesn't accept and can't be skipped
                    resultBool = false;
                    return null;
                }
            }
        } else {
            isQueried= false;
            return operatedSRE;
        }
        return null;
    }


    @Override
    public boolean accepts(Event event) {
        if (children.size() == 0) {
            return false;
        }
        // The first child of a sequential execution must accept for its parent
        // to accept
        this.query(event);
        return resultBool;
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
        return "MapExecution [operator=" + operator + ", matchSre=" + matchSre
                + ", isZeroPlus=" + isZeroPlus + ", isOnePlus=" + isOnePlus
                + ", children=" + children + "]\n";
    }
}