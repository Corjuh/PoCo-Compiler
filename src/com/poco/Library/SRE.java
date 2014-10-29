package com.poco.Library;

import com.poco.PoCoParser.PoCoParser;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.RegExp;

/**
 * Created by Danielle on 10/29/2014.
 */
public class SRE {
    public static Automaton GetPositiveSet(PoCoParser.SreContext sre)
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

    public static Automaton GetNegativeSet(PoCoParser.SreContext sre)
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
}
