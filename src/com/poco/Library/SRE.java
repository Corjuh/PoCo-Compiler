package com.poco.Library;

import com.poco.PoCoParser.PoCoParser;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.RegExp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Danielle on 10/29/2014.
 */
public class SRE {
    public static Automaton GetPositiveSet(PoCoParser.SreContext sre)
    {
        return GetPositiveSet(sre, new HashMap<String, String>());
    }
    public static Automaton GetPositiveSet(PoCoParser.SreContext sre, Map<String, String> bindings)
    {
        if(sre.PLUS() != null)
        {
            String re = SRE.replaceValues(sre.re(), bindings);
            re = re.replace("%", ".*");
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
                    Automaton a1pos = GetPositiveSet(sre.sre(0), bindings);
                    Automaton a2pos = GetPositiveSet(sre.sre(1), bindings);
                    Automaton a1neg = GetNegativeSet(sre.sre(0), bindings);
                    Automaton a2neg = GetNegativeSet(sre.sre(1), bindings);
                    return a1pos.union(a2pos).minus(a1neg).minus(a2neg);
                }
                //srepunion or sredisj
                if(sre.srebop().srepunion() != null || sre.srebop().sredisj() != null)
                {
                    Automaton a1pos = GetPositiveSet(sre.sre(0), bindings);
                    Automaton a2pos = GetPositiveSet(sre.sre(1), bindings);
                    return a1pos.union(a2pos);
                }
                //sreconj
                if(sre.srebop().sreconj() != null)
                {
                    Automaton a1pos = GetPositiveSet(sre.sre(0), bindings);
                    Automaton a2pos = GetPositiveSet(sre.sre(1), bindings);
                    return a1pos.intersection(a2pos);
                }
                //sreequals
                if(sre.srebop().sreequals() != null)
                {
                    Automaton a1pos = GetPositiveSet(sre.sre(0), bindings);
                    Automaton a2pos = GetPositiveSet(sre.sre(1), bindings);
                    Automaton posintersect =  a1pos.intersection(a2pos);
                    Automaton a1neg = GetNegativeSet(sre.sre(0), bindings);
                    Automaton a2neg = GetNegativeSet(sre.sre(1), bindings);
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
                    Automaton a1pos = GetPositiveSet(sre.sre(0), bindings);
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
                return GetPositiveSet(sre.sre(0), bindings);
            }
        }
        return BasicAutomata.makeEmpty();
    }

    public static Automaton GetNegativeSet(PoCoParser.SreContext sre)
    {
        return GetNegativeSet(sre, new HashMap<String, String>());
    }
    public static Automaton GetNegativeSet(PoCoParser.SreContext sre, Map<String, String> bindings)
    {
        if(sre.PLUS() != null)
        {
            //straight negative or neutral sre. not interested in it
            return BasicAutomata.makeEmpty();
        }
        else if(sre.MINUS() != null || sre.NEUTRAL() != null)
        {
            String re = sre.re().getText();
            re = re.replace("%", ".*");
            RegExp r1 = new RegExp(re);
            Automaton a1 = r1.toAutomaton();
            return a1;
        }
        else {
            if (sre.srebop() != null) {
                //union or sreconj
                if(sre.srebop().sreunion() != null || sre.srebop().sreconj() != null)
                {
                    Automaton a1neg = GetNegativeSet(sre.sre(0), bindings);
                    Automaton a2neg = GetNegativeSet(sre.sre(1), bindings);
                    return a1neg.union(a2neg);
                }
                //srepunion or
                if(sre.srebop().srepunion() != null)
                {
                    Automaton a1pos = GetPositiveSet(sre.sre(0), bindings);
                    Automaton a2pos = GetPositiveSet(sre.sre(1), bindings);
                    Automaton a1neg = GetNegativeSet(sre.sre(0), bindings);
                    Automaton a2neg = GetNegativeSet(sre.sre(1), bindings);
                    return a1neg.union(a2neg).minus(a1pos).minus(a2pos);
                }
                //sredisj
                if(sre.srebop().sredisj() != null)
                {
                    Automaton a1neg = GetNegativeSet(sre.sre(0), bindings);
                    Automaton a2neg = GetNegativeSet(sre.sre(1), bindings);
                    return a1neg.intersection(a2neg);
                }
                //sreequals
                if(sre.srebop().sreequals() != null)
                {
                    Automaton a1pos = GetPositiveSet(sre.sre(0), bindings);
                    Automaton a2pos = GetPositiveSet(sre.sre(1), bindings);
                    Automaton posintersect =  a1pos.intersection(a2pos);
                    Automaton a1neg = GetNegativeSet(sre.sre(0), bindings);
                    Automaton a2neg = GetNegativeSet(sre.sre(1), bindings);
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
                    Automaton a1pos = GetPositiveSet(sre.sre(0), bindings);
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
                    Automaton a1neg = GetNegativeSet(sre.sre(0), bindings);
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

    public static String replaceValues(PoCoParser.ReContext rectx, Map<String, String> bindings)
    {
        String re = rectx.getText();
        if(rectx.DOLLAR() == null)
        {
            return re;
        }
        if(bindings.containsKey(rectx.qid().getText()))
        {
            String replace = "$" + rectx.qid().getText();
            String value = bindings.get(rectx.qid().getText());

            PoCoParser.OpparamlistContext opctx = rectx.opparamlist();
            List<String> res = new ArrayList<String>();
            if(opctx != null)
            {
                replace = replace + "(" + opctx.getText() + ")";
            }
            while(opctx != null)
            {
                if(opctx.re() != null)
                {
                    String opValue = opctx.re().getText();
                    if(opctx.re().DOLLAR() != null)
                    {
                        opValue = replaceValues(opctx.re(), bindings);
                    }

                    res.add(0, opValue);
                }
                opctx = opctx.opparamlist();
            }
            int i = 0;
            while (value.contains("{" + i + "}")) {
                if(res.size() <= i)
                {
                    break;
                }
                value = value.replace("{" + i + "}", res.get(i));
                i++;
            }
            re = re.replace(replace, value);
        }
        return re;
    }

    public static String replaceBinding(PoCoParser.ReContext ctx, Map<String, String> bindings)
    {
        String value = ctx.getText();

        //standard variable (not function)
        if(ctx.DOLLAR() != null) {
            if (bindings.containsKey(ctx.qid().getText())) {
                if (ctx.LPAREN() == null) {
                    value = value.replace("$" + ctx.qid().getText(), bindings.get(ctx.qid().getText()));
                } else {
                    PoCoParser.OpparamlistContext opctx = ctx.opparamlist();
                    List<String> res = new ArrayList<String>();
                    while (opctx != null) {
                        if (opctx.re() != null) {
                            String opValue = opctx.re().getText();
                            if (opctx.re().DOLLAR() != null) {
                                opValue = SRE.replaceValues(opctx.re(), bindings);
                            }

                            res.add(0, opValue);
                        }
                        opctx = opctx.opparamlist();
                    }
                    int i = 0;
                    String re = bindings.get(ctx.qid().getText());
                    while (re.contains("{" + i + "}")) {
                        if (res.size() <= i) {
                            break;
                        }
                        re = re.replace("{" + i + "}", res.get(i));
                        i++;
                    }

                    value = re;
                }
            }
        }

        return value;
    }
}
