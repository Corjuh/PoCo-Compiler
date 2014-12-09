package com.poco.StaticAnalysis;
import com.poco.Library.SRE;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseListener;

import java.util.*;

/**
 * Created by Danielle on 8/5/2014.
 */

public class UncoveredExecutionPaths extends PoCoParserBaseListener {
    PoCoParser parser;
    List<String> start;
    boolean finalresult = true;
    String errorsegment = "";
    LinkedHashSet<String> possibleinputs;
    Map<String, String> bindings;

    public UncoveredExecutionPaths(PoCoParser parser, LinkedHashSet<String> possibleinputs) {
        this.parser = parser;
        this.start = new ArrayList<String>();
        this.possibleinputs = possibleinputs;
        this.bindings = new HashMap<String, String>();
    }
    @Override
    public void exitPolicy(PoCoParser.PolicyContext ctx) {
        UncoveredExecutionPaths(start);
        if(!finalresult) {
            System.out.println("Warning: There are uncovered execution paths in segment '" + errorsegment + "'. This may cause unexpected behavior.");
        }
    }

    private boolean UncoveredExecutionPaths(List<String> list)
    {
        List<String> segment = new ArrayList<String>();
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
            else if(list.get(i) == "STAR")
            {
                segment.add(list.get(i));
            }
            else if(list.get(i) == "BAR")
            {
                segment.add(list.get(i));
            }
            else if(list.get(i) == "CONCAT")
            {
                if(segment.get(segment.size()-1) == "STAR")
                {
                    segment.add(list.get(i));
                }
                else
                {
                    ProcessSegment(segment);
                    segment=new ArrayList<String>();
                }
            }
            else if(list.get(i) == "PLUS")
            {
                segment.add(list.get(i));
            }
            else
            {
                //exchange
                segment.add(list.get(i));
            }
        }
        ProcessSegment(segment);
        return true;
    }

    private void ProcessSegment(List<String> segment)
    {
        String re = "";
        String segmentstring = "";
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
                    re += segment.get(i).split("\\(")[0];
                }
                segmentstring += segment.get(i).split("\\(")[0];
                segmentstring += "|";
                re += ")|";
            }
        }
        re = re.substring(0,re.length()-1);
        segmentstring = segmentstring.substring(0,segmentstring.length()-1);

        Iterator<String> itr =  possibleinputs.iterator();
        while(itr.hasNext())
        {
            if(!itr.next().matches(re)) {
                finalresult = false;
                errorsegment = segmentstring;
                break;
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
                    UncoveredExecutionPaths(group.subList(i + 1, group.size()));
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
                if((!start.get(j+1).equals("BAR") && !start.get(j+1).equals("CONCAT"))
                        && (!start.get(j).equals("BAR") && !start.get(j).equals("CONCAT")))
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
                if((!start.get(j+1).equals("BAR") && !start.get(j+1).equals("CONCAT"))
                        && (!start.get(j).equals("BAR") && !start.get(j).equals("CONCAT")))
                {
                    index=j+1;
                    break;
                }
            }
            start.add(index, "BAR");
        }
        else if(ctx.exch() != null && ctx.exch().matchs() != null)
        {
            PoCoParser.MatchsContext matchs = ctx.exch().matchs();
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
                            re = SRE.replaceValues(rectx, bindings);
                        }
                        else {
                            re = rectx.getText();
                        }
                    } else {
                        start.add(".*");
                        return;
                    }
                }
            }
            re = re.split("\\(")[0];
            re = re.replace("%", ".*");
            start.add(re);
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

    @Override
    public void exitMatchs(PoCoParser.MatchsContext ctx) {

    }
}
