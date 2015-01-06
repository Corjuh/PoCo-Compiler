package com.poco.StaticAnalysis;

import com.poco.Library.SRE;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseListener;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.RegExp;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.*;

/**
 * Created by Danielle on 8/5/2014.
 */

public class InfinitePositiveNoConcrete extends PoCoParserBaseListener {
    PoCoParser parser;
    List<PoCoParser.SreContext> start;
    boolean finalresult = true;
    Map<String, String> bindings;
    List<PoCoParser.SreContext> mapSREs;
    List<PoCoParser.SrebopContext> mapOps;

    public InfinitePositiveNoConcrete(PoCoParser parser) {
        this.parser = parser;
        this.start = new ArrayList<PoCoParser.SreContext>();
        this.bindings = new HashMap<String, String>();
        this.mapSREs = new ArrayList<PoCoParser.SreContext>();
        this.mapOps = new ArrayList<PoCoParser.SrebopContext>();
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
            Automaton a1 = SRE.GetPositiveSet(list.get(i), bindings);
            if(!a1.isFinite()) {
                for (int j = 0; j < concreteEvents.size(); j++) {
                    String re = concreteEvents.get(j);
                    re = re.replace("%", ".*");
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
            String re = SRE.replaceValues(sre.re(), bindings);
            re = re.replace("%", ".*");
            re = re.replace("\\", "");
            re = re.replace("<", "\\<");
            re = re.replace(">", "\\>");
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
    public void enterMap(@NotNull PoCoParser.MapContext ctx)
    {
        if(ctx.srebop() != null) {
            mapSREs.add(ctx.sre());
            mapOps.add(ctx.srebop());
        }
    }

    @Override
    public void exitMap(@NotNull PoCoParser.MapContext ctx)
    {
        if(ctx.srebop() != null) {
            mapSREs.remove(mapSREs.size() - 1);
            mapOps.remove(mapOps.size() - 1);
        }
    }

    @Override
    public void exitExch(PoCoParser.ExchContext ctx) {
        if(ctx.sre() != null)
        {
            PoCoParser.SreContext sre = ctx.sre();
            for(int i = mapSREs.size() -1; i >= 0; i--)
            {
                PoCoParser.SreContext newsre = new PoCoParser.SreContext(new ParserRuleContext(), sre.invokingState);
                newsre.addChild(sre);
                newsre.addChild(mapSREs.get(i));
                newsre.addChild(mapOps.get(i));
                sre = newsre;
            }
            start.add(sre);
        }
    }

    @Override
    public void exitMacrodecl(PoCoParser.MacrodeclContext ctx) {
        if(ctx.re() != null) {
            if (bindings.containsKey(ctx.id().getText())) {
                bindings.remove(ctx.id().getText());
            }
            String value = SRE.replaceBinding(ctx.re(), bindings);
            if(ctx.LPAREN() != null) {
                PoCoParser.IdlistContext idctx = ctx.idlist();
                List<String> ids = new ArrayList<String>();
                while (idctx != null) {
                    if (idctx.id() != null) {
                        ids.add(0, idctx.id().getText());
                    }
                    idctx = idctx.idlist();
                }

                Iterator<String> itr = ids.iterator();
                int i = 0;
                while (itr.hasNext()) {
                    value = value.replace("$" + itr.next(), "{" + i + "}");
                    i++;
                }
            }
            bindings.put(ctx.id().getText(), value);
        }
    }

    @Override
    public void exitSrecase(PoCoParser.SrecaseContext ctx) {
        if(ctx.AT() != null)
        {
            //standard macro (not function)
            if(bindings.containsKey(ctx.id().getText()))
            {
                bindings.remove(ctx.id().getText());
            }
            bindings.put(ctx.id().getText(), ctx.re().getText());
        }
    }

    @Override
    public void exitMatch(PoCoParser.MatchContext ctx) {
        if(ctx.AT() != null)
        {
            //standard macro (not function)
            if(bindings.containsKey(ctx.id().getText()))
            {
                bindings.remove(ctx.id().getText());
            }
            bindings.put(ctx.id().getText(), ctx.re().getText());
        }
    }

    @Override
    public void exitRe(PoCoParser.ReContext ctx) {
        if(ctx.AT() != null)
        {
            //standard macro (not function)
            if(bindings.containsKey(ctx.id().getText()))
            {
                bindings.remove(ctx.id().getText());
            }
            bindings.put(ctx.id().getText(), ctx.re().get(0).getText());
        }
    }


}
