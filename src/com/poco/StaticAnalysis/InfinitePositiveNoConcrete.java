package com.poco.StaticAnalysis;

import com.poco.Library.SRE;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseListener;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.RegExp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by Danielle on 8/5/2014.
 */

public class InfinitePositiveNoConcrete extends PoCoParserBaseListener {
    PoCoParser parser;
    List<PoCoParser.SreContext> start;
    boolean finalresult = true;

    public InfinitePositiveNoConcrete(PoCoParser parser) {
        this.parser = parser;
        this.start = new ArrayList<PoCoParser.SreContext>();
    }
    @Override
    public void exitPolicy(PoCoParser.PolicyContext ctx) {
        finalresult = InfinitePositiveNoConcrete(start);
        if(finalresult) {
            System.out.println("Warning: The policy '" + ctx.ppol().pocopol().id().getText() + "' has infinite positive results that do not suggest a specific action. This can cause unexpected behavior");
        }
    }

    private boolean InfinitePositiveNoConcrete(List<PoCoParser.SreContext> list)
    {
        for (int i = 0; i < list.size(); i++) {
            boolean result = false;
            List<String> concreteEvents = FinitePositiveSegments(list.get(i), new ArrayList<String>());
            Automaton a1 = SRE.GetPositiveSet(list.get(i));
            if(!a1.isFinite()) {
                for (int j = 0; j < concreteEvents.size(); j++) {
                    String re = concreteEvents.get(j);
                    re = re.replaceAll("%", ".*");
                    RegExp r2 = new RegExp(re);
                    Automaton a2 = r2.toAutomaton();
                    Automaton a3 = a1.intersection(a2);
                    if (!a3.isEmpty()) {
                        result = true;
                    }
                }
                if (!result) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<String> FinitePositiveSegments(PoCoParser.SreContext sre, List<String> start)
    {
        if(sre.PLUS() != null)
        {
            String re = sre.re().getText();
            re = re.replaceAll("%", ".*");
            RegExp r1 = new RegExp(re);
            Automaton a1 = r1.toAutomaton();
            if(a1.isFinite())
            {
                start.add(sre.re().getText());
                return start;
            }
            else
            {
                return start;
            }
        }
        else if(sre.MINUS() != null || sre.NEUTRAL() != null)
        {
            //straight negative or neutral sre. not interested in it
            return start;
        }
        else {
            if (sre.srebop() != null) {
                start = FinitePositiveSegments(sre.sre(0), start);
                start = FinitePositiveSegments(sre.sre(1), start);
                return start;
            }
            if (sre.sreuop() != null) {
                start = FinitePositiveSegments(sre.sre(0), start);
                return start;
            }
            if (sre.FOLD() != null) {
                //not handled yet
                return start;
            }
            if (sre.DOLLAR().size() > 0) {
                //not handled yet
                return start;
            }
            if (sre.LPAREN() != null) {
                start = FinitePositiveSegments(sre.sre(0), start);
                return start;
            }
        }
        return start;
    }

    @Override
    public void exitExch(PoCoParser.ExchContext ctx) {
        if(ctx.sre() != null)
        {
            start.add(ctx.sre());
        }
    }
}
