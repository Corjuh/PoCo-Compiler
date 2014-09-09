package com.poco.StaticAnalysis;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseListener;

/**
 * Created by Danielle on 8/5/2014.
 */

public class NoMatchingActions extends PoCoParserBaseListener {
    PoCoParser parser;
    List<String> start;
    boolean finalresult = true;
    String errorsegment = "";
    LinkedHashSet<String> possibleinputs;

    public NoMatchingActions(PoCoParser parser, LinkedHashSet<String> possibleinputs) {
        this.parser = parser;
        this.start = new ArrayList<String>();
        this.possibleinputs = possibleinputs;
    }
    @Override
    public void exitPolicy(PoCoParser.PolicyContext ctx) {
        NoMatchingActions(start);
        if(!finalresult) {
            System.out.println("Warning: The exchange '" + errorsegment + "' does not match any possible input actions.");
        }
    }

    private boolean NoMatchingActions(List<String> list)
    {
        String re = "";
        String restring = "";
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).split("\\(")[0] == "_") {
                re = ".*";
            } else {
                re = list.get(i).split("\\(")[0];
            }
            restring = list.get(i).split("\\(")[0];
            Boolean result = true;
            Iterator<String> itr =  possibleinputs.iterator();
            while(itr.hasNext())
            {
                if(itr.next().matches(re)) {
                    result = false;
                    break;
                }
            }
            if(result)
            {
                finalresult = false;
                errorsegment = restring;
            }
            break;
        }


        return true;
    }

    @Override
    public void exitIre(PoCoParser.IreContext ctx) {
        //only dealing with actual function calls now - TODO: expand to more types
        if(ctx.re().size() > 0 && ctx.re().get(0).function() != null) {
            start.add(ctx.re().get(0).getText());
        }
        else
        {
            start.add("_");
        }
    }
}
