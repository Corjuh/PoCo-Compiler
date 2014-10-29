package com.poco.StaticAnalysis;

import com.poco.Library.SRE;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseListener;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.RegExp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Danielle on 8/5/2014.
 */

public class EmptyPositiveNegativeInput extends PoCoParserBaseListener {
    PoCoParser parser;
    List<PoCoParser.SreContext> start;
    List<PoCoParser.IreContext> startire;
    boolean finalresult = true;

    public EmptyPositiveNegativeInput(PoCoParser parser) {
        this.parser = parser;
        this.start = new ArrayList<PoCoParser.SreContext>();
        this.startire = new ArrayList<PoCoParser.IreContext>();
    }
    @Override
    public void exitPolicy(PoCoParser.PolicyContext ctx) {
        finalresult = EmptyPositiveNegativeInput(start, startire);
        if(finalresult) {
            System.out.println("Warning: The policy '" + ctx.ppol().pocopol().id().getText() + "' has empty positive results and might disallow the input action. This can cause unexpected behavior");
        }
    }

    private boolean EmptyPositiveNegativeInput(List<PoCoParser.SreContext> list, List<PoCoParser.IreContext> listire)
    {
        for (int i = 0; i < list.size(); i++) {
            boolean result = false;
            Automaton positive = SRE.GetPositiveSet(list.get(i));
            Automaton negative = SRE.GetNegativeSet(list.get(i));
            if(positive.isEmpty()) {
                if(startire.get(i).re() != null && startire.get(i).re().size() > 0)
                {
                    String re = startire.get(i).re().get(0).getText();
                    re = re.replaceAll("%", ".*");
                    RegExp input = new RegExp(re);
                    Automaton a = input.toAutomaton();
                    if(!a.intersection(negative).isEmpty())
                    {
                        return true;
                    }
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
        if(ctx.matchs() != null)
        {
            if(ctx.matchs().match() != null)
            {
                //only handling limited cases right now
                if(ctx.matchs().match().ire() != null) {
                    startire.add(ctx.matchs().match().ire());
                    if(ctx.sre() != null)
                    {
                        start.add(ctx.sre());
                    }
                }
            }
        }
    }
}
