package com.poco.StaticAnalysis;
import java.util.*;

import com.poco.Library.SRE;
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
    Map<String, String> bindings;

    public NoMatchingActions(PoCoParser parser, LinkedHashSet<String> possibleinputs) {
        this.parser = parser;
        this.start = new ArrayList<String>();
        this.possibleinputs = possibleinputs;
        this.bindings = new HashMap<String, String>();
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
        for (int i = 0; i < list.size(); i++) {
            re = list.get(i);
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
                errorsegment = re;
            }
            break;
        }


        return true;
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
        String re = "";
        if(ctx.match() != null) {
            if(ctx.match().ire() != null) {
                if (ctx.match().ire().re().size() > 0) {
                    //The first re is always the action
                    PoCoParser.ReContext rectx =  ctx.match().ire().re().get(0);
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
