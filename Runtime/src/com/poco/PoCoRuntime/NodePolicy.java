package com.poco.PoCoRuntime;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Extending from Policy class, this class will be used to declare those
 * policies that do not have concrete policy bodies, but only combing logics.
 *
 * @author yan
 *
 */
public class NodePolicy extends Policy {
    private String strategy = "";

    public NodePolicy(String strategy) {
        super();
        this.strategy = strategy;
    }

    public NodePolicy(String policyName, String strategy) {
        super();
        this.strategy = strategy;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    @Override
    public SRE query(Event event) {
        // 1. check if this policy is queried or not, if so return the queried
        // result
        if (this.isQueried()) {
            return this.queriedResult;
        } else {
            ArrayList<SRE> sreResults = new ArrayList<SRE>();
            for (Iterator<Policy> it = children.iterator(); it.hasNext();) {
                Policy temp = it.next();
                if (temp.isQueried())
                    sreResults.add(temp.getQueriedResult());
                else {
                    // no need to store result here, it will be done at its
                    // base case
                    sreResults.add(temp.query(event));
                }
            }

            this.isQueried = true;
            this.isAccept = true;
            this.queriedResult = SREUtil.performBOPs(this.strategy, sreResults);

            return this.queriedResult;
        }
    }
}