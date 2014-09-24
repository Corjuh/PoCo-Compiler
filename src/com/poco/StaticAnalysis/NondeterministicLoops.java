package com.poco.StaticAnalysis;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseListener;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

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

    public NondeterministicLoops(PoCoParser parser) {
        this.parser = parser;
        this.start = new ArrayList<String>();
        this.possibleinputs = possibleinputs;
    }
    @Override
    public void exitPolicy(PoCoParser.PolicyContext ctx) {
        NondeterministicLoops(start);
        if(finalresult) {
            System.out.println("Warning: The loop '" + errorsegment1 + "' followed by '" + errorsegment1 + "' is nondeterministic. This may cause unexpected behavior.");
        }
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
                        ProcessSegment(segment, nextsegment);
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
        nextre = nextre.substring(0,nextre.length()-1);
        nextsegmentstring = segmentstring.substring(0,segmentstring.length()-1);

        RegExp r1 = new RegExp(re);
        RegExp r2 = new RegExp(nextre);
        Automaton a1 = r1.toAutomaton();
        Automaton a2 = r2.toAutomaton();
        Automaton a3 = a1.intersection(a2);
        if(!a3.isEmpty())
        {
            finalresult = true;
            errorsegment1 = segmentstring;
            errorsegment2 = nextsegmentstring;
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
    public void exitIre(PoCoParser.IreContext ctx) {
        //only dealing with actual function calls now - TODO: expand to more types
        if(ctx.re().size() > 0) {
            start.add(ctx.re().get(0).getText());
        }
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
            int index = start.size();
            if(start.get(start.size()-1) == "END GROUP" || (start.size() > 2 && (start.get(start.size()-2) == "CONCAT" || start.get(start.size()-2) == "BAR"))) {
                for(int j = start.size()-1; j >0; j--)
                {
                    if(start.get(j) == "START GROUP")
                    {
                        index=j+1;
                        break;
                    }
                }
            }
            start.add(index - 1, "CONCAT");
        }
        else if(ctx.children.size() == 3 && ctx.BAR() != null) {
            int index = start.size();
            if(start.get(start.size()-1) == "END GROUP" || (start.size() > 2 && (start.get(start.size()-2) == "CONCAT" || start.get(start.size()-2) == "BAR"))) {
                for(int j = start.size()-1; j >0; j--)
                {
                    if(start.get(j) == "START GROUP")
                    {
                        index=j+1;
                        break;
                    }
                }
            }
            start.add(index - 1, "BAR");
        }
    }
}
