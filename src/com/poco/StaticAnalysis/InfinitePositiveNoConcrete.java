package com.poco.StaticAnalysis;

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
            Automaton a1 = GetPositiveSet(list.get(i));
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

    private Automaton GetPositiveSet(PoCoParser.SreContext sre)
    {
        if(sre.PLUS() != null)
        {
            String re = sre.re().getText();
            re = re.replaceAll("%", ".*");
            RegExp r1 = new RegExp(re);
            Automaton a1 = r1.toAutomaton();
            return a1;
        }
        else if(sre.MINUS() != null || sre.NEUTRAL() != null)
        {
            //straight negative or neutral sre. not interested in it
            return BasicAutomata.makeEmpty();
        }
        else {
            if (sre.srebop() != null) {
                //union
                if(sre.srebop().sreunion() != null)
                {
                    Automaton a1pos = GetPositiveSet(sre.sre(0));
                    Automaton a2pos = GetPositiveSet(sre.sre(1));
                    Automaton a1neg = GetNegativeSet(sre.sre(0));
                    Automaton a2neg = GetNegativeSet(sre.sre(1));
                    return a1pos.union(a2pos).minus(a1neg).minus(a2neg);
                }
                //srepunion or sredisj
                if(sre.srebop().srepunion() != null || sre.srebop().sredisj() != null)
                {
                    Automaton a1pos = GetPositiveSet(sre.sre(0));
                    Automaton a2pos = GetPositiveSet(sre.sre(1));
                    return a1pos.union(a2pos);
                }
                //sreconj
                if(sre.srebop().sreconj() != null)
                {
                    Automaton a1pos = GetPositiveSet(sre.sre(0));
                    Automaton a2pos = GetPositiveSet(sre.sre(1));
                    return a1pos.intersection(a2pos);
                }
                //sreequals
                if(sre.srebop().sreequals() != null)
                {
                    Automaton a1pos = GetPositiveSet(sre.sre(0));
                    Automaton a2pos = GetPositiveSet(sre.sre(1));
                    Automaton posintersect =  a1pos.intersection(a2pos);
                    Automaton a1neg = GetNegativeSet(sre.sre(0));
                    Automaton a2neg = GetNegativeSet(sre.sre(1));
                    Automaton negintersect =  a1pos.intersection(a2pos);
                    Automaton a1neutral = BasicAutomata.makeAnyString().minus(a1pos).minus(a1neg);
                    Automaton a2neutral = BasicAutomata.makeAnyString().minus(a2pos).minus(a2neg);
                    Automaton neutralintersect =  a1neutral.intersection(a2neutral);
                    return posintersect.union(negintersect).union(neutralintersect);
                }
                return BasicAutomata.makeEmpty();
            }
            if (sre.sreuop() != null) {
                //srecomp
                if(sre.sreuop().srecomp() != null) {
                    Automaton a1neg = GetNegativeSet(sre.sre(0));
                    return a1neg;
                }
                //sreactions
                if(sre.sreuop().sreactions() != null) {
                    //not handled yet
                    return BasicAutomata.makeEmpty();
                }
                //sreresults
                if(sre.sreuop().sreresults() != null) {
                    //not handled yet
                    return BasicAutomata.makeEmpty();
                }
                //srepos
                if(sre.sreuop().srepos() != null) {
                    Automaton a1pos = GetPositiveSet(sre.sre(0));
                    return a1pos;
                }
                //sreneg
                if(sre.sreuop().srepos() != null) {
                    return BasicAutomata.makeEmpty();
                }
            }
            if (sre.FOLD() != null) {
                //not handled yet
                return BasicAutomata.makeEmpty();
            }
            if (sre.DOLLAR().size() > 0) {
                //not handled yet
                return BasicAutomata.makeEmpty();
            }
            if (sre.LPAREN() != null) {
                return GetPositiveSet(sre.sre(0));
            }
        }
        return BasicAutomata.makeEmpty();
    }

    private Automaton GetNegativeSet(PoCoParser.SreContext sre)
    {
        if(sre.PLUS() != null)
        {
            //straight negative or neutral sre. not interested in it
            return BasicAutomata.makeEmpty();
        }
        else if(sre.MINUS() != null || sre.NEUTRAL() != null)
        {
            String re = sre.re().getText();
            re = re.replaceAll("%", ".*");
            RegExp r1 = new RegExp(re);
            Automaton a1 = r1.toAutomaton();
            return a1;
        }
        else {
            if (sre.srebop() != null) {
                //union or sreconj
                if(sre.srebop().sreunion() != null || sre.srebop().sreconj() != null)
                {
                    Automaton a1neg = GetNegativeSet(sre.sre(0));
                    Automaton a2neg = GetNegativeSet(sre.sre(1));
                    return a1neg.union(a2neg);
                }
                //srepunion or
                if(sre.srebop().srepunion() != null)
                {
                    Automaton a1pos = GetPositiveSet(sre.sre(0));
                    Automaton a2pos = GetPositiveSet(sre.sre(1));
                    Automaton a1neg = GetNegativeSet(sre.sre(0));
                    Automaton a2neg = GetNegativeSet(sre.sre(1));
                    return a1neg.union(a2neg).minus(a1pos).minus(a2pos);
                }
                //sredisj
                if(sre.srebop().sredisj() != null)
                {
                    Automaton a1neg = GetNegativeSet(sre.sre(0));
                    Automaton a2neg = GetNegativeSet(sre.sre(1));
                    return a1neg.intersection(a2neg);
                }
                //sreequals
                if(sre.srebop().sreequals() != null)
                {
                    Automaton a1pos = GetPositiveSet(sre.sre(0));
                    Automaton a2pos = GetPositiveSet(sre.sre(1));
                    Automaton posintersect =  a1pos.intersection(a2pos);
                    Automaton a1neg = GetNegativeSet(sre.sre(0));
                    Automaton a2neg = GetNegativeSet(sre.sre(1));
                    Automaton negintersect =  a1pos.intersection(a2pos);
                    Automaton a1neutral = BasicAutomata.makeAnyString().minus(a1pos).minus(a1neg);
                    Automaton a2neutral = BasicAutomata.makeAnyString().minus(a2pos).minus(a2neg);
                    Automaton neutralintersect =  a1neutral.intersection(a2neutral);
                    return BasicAutomata.makeAnyString().minus(posintersect).minus(negintersect).minus(neutralintersect);
                }
                return BasicAutomata.makeEmpty();
            }
            if (sre.sreuop() != null) {
                //srecomp
                if(sre.sreuop().srecomp() != null) {
                    Automaton a1pos = GetPositiveSet(sre.sre(0));
                    return a1pos;
                }
                //sreactions
                if(sre.sreuop().sreactions() != null) {
                    //not handled yet
                    return BasicAutomata.makeEmpty();
                }
                //sreresults
                if(sre.sreuop().sreresults() != null) {
                    //not handled yet
                    return BasicAutomata.makeEmpty();
                }
                //srepos
                if(sre.sreuop().srepos() != null) {
                    return BasicAutomata.makeEmpty();
                }
                //sreneg
                if(sre.sreuop().srepos() != null) {
                    Automaton a1neg = GetNegativeSet(sre.sre(0));
                    return a1neg;
                }
            }
            if (sre.FOLD() != null) {
                //not handled yet
                return BasicAutomata.makeEmpty();
            }
            if (sre.DOLLAR().size() > 0) {
                //not handled yet
                return BasicAutomata.makeEmpty();
            }
            if (sre.LPAREN() != null) {
                return GetNegativeSet(sre.sre(0));
            }
        }
        return BasicAutomata.makeEmpty();
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
