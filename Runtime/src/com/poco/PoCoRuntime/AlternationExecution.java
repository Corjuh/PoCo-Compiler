package com.poco.PoCoRuntime;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by caoyan on 12/4/14.
 */
public class AlternationExecution extends SequentialExecution
{
    ArrayList<SequentialExecution> execs;

    public void addSeqExec(SequentialExecution se) {
        execs.add(se);
    }


    public AlternationExecution(String modifier) throws PoCoException {
        super(modifier);
    }

    @Override
    public SRE query(Event event) {
        return super.query(event);
    }

    @Override
    public boolean accepts(Event event) {
        for(Iterator<SequentialExecution> it = execs.iterator(); it.hasNext();) {
            SequentialExecution se = (SequentialExecution) it;
            if (se.accepts(event))
                return true;
        }
        return false;
    }
}
