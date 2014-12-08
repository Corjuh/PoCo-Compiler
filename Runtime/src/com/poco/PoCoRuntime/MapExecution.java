package com.poco.PoCoRuntime;

import java.util.ArrayList;

/**
 * Created by caoyan on 12/3/14.
 */
public class MapExecution extends SequentialExecution implements Queryable, Matchable
{
    private String operator;
    private SRE    matchSre = null;

    public MapExecution(String modifier, String operator, SRE matchSre) throws PoCoException {
        super(modifier);
        this.operator = operator;
        this.matchSre = matchSre;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setMatchSre(SRE matchSre) {
        this.matchSre = matchSre;
    }

    public MapExecution(String modifier) throws PoCoException {
        super(modifier);
    }

    @Override
    public SRE query(Event event) {
        return super.query(event);
    }

    @Override
    public boolean accepts(Event event) {
        return super.accepts(event);
    }

    @Override
    public void addChild(EventResponder child) {
        super.addChild(child);
    }

    @Override
    public ArrayList<EventResponder> getChildren() {
        return super.getChildren();
    }

}
