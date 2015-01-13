package com.poco.StaticAnalysis;
import com.poco.Library.SRE;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseListener;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.*;

/**
 * Created by Danielle on 8/5/2014.
 */

public class NondeterministicLoops extends PoCoParserBaseListener {
    PoCoParser parser;
    List<String> start;
    boolean finalresult = false;
    String errorsegment1 = "";
    String errorsegment2 = "";
    LinkedHashSet<String> possibleinputs;
    Map<String, String> bindings;

    public NondeterministicLoops(PoCoParser parser) {
        this.parser = parser;
        this.start = new ArrayList<String>();
        this.possibleinputs = possibleinputs;
        this.bindings = new HashMap<String, String>();
    }
    @Override
    public void exitPocopol(PoCoParser.PocopolContext ctx) {
        NondeterministicLoops(start);
        if(finalresult) {
            System.out.println("Warning: The loop '" + errorsegment1 + "' followed by '" + errorsegment2 + "' is nondeterministic. This may cause unexpected behavior.");
        }
        this.start = new ArrayList<String>();
        this.possibleinputs = possibleinputs;
        this.bindings = new HashMap<String, String>();
    }

    private boolean NondeterministicLoops(List<String> list)
    {
        List<String> lastexchange = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i) == "START GROUP")
            {
                List<String> group = new ArrayList<String>();
                for (i=++i; i < list.size(); i++)
                {
                    if(list.get(i) != "END GROUP")
                    {
                        group.add(list.get(i));
                    }
                    else
                    {
                        NondeterministicLoops(group);
                        break;
                    }
                }
                lastexchange = group;
            }
            else if(list.get(i) == "STAR" || list.get(i) == "PLUS")
            {
                List<String> remaining = new ArrayList<>();
                if(list.size() > i+1) {
                    remaining = list.subList(i + 1, list.size());
                }
                CheckItem(lastexchange, remaining);
            }
            else if(list.get(i) == "CONCAT" || list.get(i) == "BAR") {}
            else
            {
                //exchange
                lastexchange.clear();
                lastexchange.add(list.get(i));
            }
        }
        return true;
    }

    private void CheckItem(List<String> lastexchange, List<String> list)
    {
        List<String> segment = new ArrayList<String>();
        List<String> nextsegment = new ArrayList<String>();
        for (int i = 0; i < lastexchange.size(); i++) {
            if(lastexchange.get(i) == "START GROUP")
            {
                List<String> group = new ArrayList<String>();
                for (i=++i; i < lastexchange.size(); i++)
                {
                    if(lastexchange.get(i) != "END GROUP")
                    {
                        group.add(lastexchange.get(i));
                    }
                    else
                    {
                        ReduceGroup(group, i, segment);
                        break;
                    }
                }
            }
            else if(lastexchange.get(i) == "CONCAT")
            {
                if(segment.get(segment.size()-1) == "STAR")
                {
                    segment.add(lastexchange.get(i));
                }
                else
                {
                    break;
                }
            }
            else
            {
                //exchange, STAR, BAR, or PLUs
                segment.add(lastexchange.get(i));
            }
        }
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i) == "START GROUP")
            {
                List<String> group = new ArrayList<String>();
                for (i=++i; i < list.size(); i++)
                {
                    if(list.get(i) != "END GROUP")
                    {
                        group.add(list.get(i));
                    }
                    else
                    {
                        ReduceGroup(group, i, segment);
                        break;
                    }
                }
            }
            else if(list.get(i) == "CONCAT")
            {
                if(nextsegment.size() == 0)
                {
                    //if we are just starting into next segment and it is CONCAT, ignore
                }
                else if(nextsegment.get(nextsegment.size()-1) == "STAR")
                {
                    nextsegment.add(list.get(i));
                }
                else
                {
                    ProcessSegment(segment, nextsegment);
                    break;
                }
            }
            else
            {
                //exchange, STAR, BAR, or PLUs
                nextsegment.add(list.get(i));
            }
        }
        ProcessSegment(segment, nextsegment);
    }

    private void ProcessSegment(List<String> segment, List<String> nextsegment)
    {
        String re = "";
        String segmentstring = "";
        if(segment.size() == 0 || nextsegment.size() == 0)
            return;
        for (int i = 0; i < segment.size(); i++) {
            if(segment.get(i) == "START GROUP" || segment.get(i) == "STAR" || segment.get(i) == "BAR" || segment.get(i) == "CONCAT" || segment.get(i) == "PLUS")
            {

            }
            else
            {
                //exchange
                re += "(";
                if(segment.get(i).split("\\(")[0] == "_")
                {
                    re += ".*";
                }
                else {
                    re += segment.get(i);
                }
                segmentstring += segment.get(i);
                segmentstring += "|";
                re += ")|";
            }
        }
        re = re.substring(0,re.length()-1);
        segmentstring = segmentstring.substring(0,segmentstring.length()-1);

        String nextre = "";
        String nextsegmentstring = "";
        for (int i = 0; i < nextsegment.size(); i++) {
            if(nextsegment.get(i) == "START GROUP" || nextsegment.get(i) == "STAR" || nextsegment.get(i) == "BAR" || nextsegment.get(i) == "CONCAT" || nextsegment.get(i) == "PLUS")
            {

            }
            else
            {
                //exchange
                nextre += "(";
                if(nextsegment.get(i).split("\\(")[0] == "_")
                {
                    nextre += ".*";
                }
                else {
                    nextre += nextsegment.get(i);
                }
                nextsegmentstring += nextsegment.get(i);
                nextsegmentstring += "|";
                nextre += ")|";
            }
        }
        if(nextre.length() > 0) {
            nextre = nextre.substring(0, nextre.length() - 1);
            nextsegmentstring = nextsegmentstring.substring(0, nextsegmentstring.length() - 1);

            re = re.replace("%", ".*");
            re = re.replace("<", "\\<");
            re = re.replace(">", "\\>");
            RegExp r1 = new RegExp(re);
            nextre = nextre.replace("%", ".*");
            nextre = nextre.replace("<", "\\<");
            nextre = nextre.replace(">", "\\>");
            RegExp r2 = new RegExp(nextre);
            Automaton a1 = r1.toAutomaton();
            Automaton a2 = r2.toAutomaton();
            Automaton a3 = a1.intersection(a2);
            if (!a3.isEmpty()) {
                finalresult = true;
                errorsegment1 = segmentstring;
                errorsegment2 = nextsegmentstring;
            }
        }
    }

    private void ReduceGroup(List<String> group, int next, List<String> segment)
    {
        for (int i = 0; i < group.size(); i++) {
            if(group.get(i) == "START GROUP")
            {
                List<String> subgroup = new ArrayList<String>();
                for (i=++i; i < group.size(); i++)
                {
                    if(subgroup.get(i) != "END GROUP")
                    {
                        subgroup.add(group.get(i));
                    }
                    else
                    {
                        ReduceGroup(subgroup, i, segment);
                        i++;
                        break;
                    }
                }
            }
            else if(group.get(i) == "STAR")
            {
                segment.add(group.get(i));
            }
            else if(group.get(i) == "BAR")
            {
                segment.add(group.get(i));
            }
            else if(group.get(i) == "CONCAT")
            {
                if(segment.get(segment.size()-1) == "STAR")
                {
                    segment.add(group.get(i));
                }
                else
                {
                    break;
                }
            }
            else if(group.get(i) == "PLUS")
            {
                segment.add(group.get(i));
            }
            else
            {
                //exchange
                segment.add(group.get(i));
            }
        }
    }



    @Override
    public void exitExch(PoCoParser.ExchContext ctx) {
        if(ctx.INPUTWILD() != null) {
            start.add("_");
        }
    }

    @Override
    public void enterMap(@NotNull PoCoParser.MapContext ctx)
    {
        start.add("START GROUP");
    }

    @Override
    public void exitMap(@NotNull PoCoParser.MapContext ctx)
    {
        start.add("END GROUP");
    }

    @Override
    public void enterExecution(PoCoParser.ExecutionContext ctx) {
        if(ctx.children.get(0).getText().equals("(")) {
            start.add("START GROUP");
        }
    }
    @Override
    public void exitExecution(PoCoParser.ExecutionContext ctx) {
        if(ctx.children.size() > 0 && ctx.children.get(0).getText().equals("(")) {
            start.add("END GROUP");
        }
        else if(ctx.children.size() > 1 && ctx.children.get(1).getText().equals("*")) {
            start.add("STAR");
        }
        else if(ctx.children.size() > 1 && ctx.children.get(1).getText().equals("+")) {
            start.add("PLUS");
        }
        else if(ctx.children.size() == 2 && ctx.execution().size() == 2) {
            int index = 1;
            for(int j = start.size() - 2; j > 0; j--)
            {
                if((!start.get(j+1).equals("BAR") && !start.get(j+1).equals("CONCAT") && !start.get(j+1).equals("END GROUP") && !start.get(j+1).equals("STAR"))
                        && (!start.get(j).equals("BAR") && !start.get(j).equals("CONCAT") && !start.get(j).equals("START GROUP")))
                {
                    index=j+1;
                    break;
                }
            }
            start.add(index, "CONCAT");
        }
        else if(ctx.children.size() == 3 && ctx.BAR() != null) {
            int index = 1;
            for(int j = start.size() - 2; j > 0; j--)
            {
                if((!start.get(j+1).equals("BAR") && !start.get(j+1).equals("CONCAT") && !start.get(j+1).equals("END GROUP") && !start.get(j+1).equals("STAR"))
                        && (!start.get(j).equals("BAR") && !start.get(j).equals("CONCAT") && !start.get(j).equals("START GROUP")))
                {
                    index=j+1;
                    break;
                }
            }
            start.add(index, "BAR");
        }
        else if(ctx.exch() != null && ctx.exch().matchs() != null)
        {
            String re = createRE(ctx.exch().matchs());
            start.add(re);
        }
    }

    private String createRE(PoCoParser.MatchsContext matchs)
    {
        String re = "";
        if(matchs.match() != null) {
            if(matchs.match().ire() != null) {
                if (matchs.match().ire().re().size() > 0) {
                    //The first re is always the action
                    PoCoParser.ReContext rectx =  matchs.match().ire().re().get(0);
                    while(rectx.AT() != null)
                    {
                        rectx = rectx.re().get(0);
                    }
                    if(rectx.DOLLAR() != null)
                    {
                        re = SRE.replaceValues(rectx,bindings);
                    }
                    else {
                        re = rectx.getText();
                    }
                } else {
                    return ".*";
                }
                re = re.split("\\(")[0];
                re = re.replace("%", ".*");
                return re;
            }
        }
        else if(matchs.BOOLUOP() != null)
        {
            if(matchs.BOOLUOP().getText().equals("!") && matchs.matchs().size() > 0)
            {
                String re1 = createRE(matchs.matchs(0));
                return "~(" + re1 + ")";
            }
        }
        else if(matchs.BOOLBOP() != null)
        {
            if(matchs.BOOLBOP().getText().equals("&&") && matchs.matchs().size() > 0)
            {
                String re1 = createRE(matchs.matchs(0));
                String re2 = createRE(matchs.matchs(1));
                if(re1.isEmpty())
                    return re2;
                if(re2.isEmpty())
                    return re1;
                return "(" + re1 + ")&(" + re2 + ")";
            }
            if(matchs.BOOLBOP().getText().equals("||") && matchs.matchs().size() > 0)
            {
                String re1 = createRE(matchs.matchs(0));
                String re2 = createRE(matchs.matchs(1));
                if(re1.isEmpty())
                    return re2;
                if(re2.isEmpty())
                    return re1;
                return "(" + re1 + ")|(" + re2 + ")";
            }
        }
        else if(matchs.matchs().size() == 1)
        {
            return createRE(matchs.matchs(0));
        }
        return re;
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
