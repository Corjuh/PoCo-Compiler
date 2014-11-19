package com.poco.StaticAnalysis;
import java.util.*;

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

    private String replaceBinding(PoCoParser.ReContext ctx)
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
                                opValue = replaceValues(opctx.re());
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

    @Override
    public void exitMacrodecl(PoCoParser.MacrodeclContext ctx) {
        if(ctx.re() != null) {
            if (bindings.containsKey(ctx.id().getText())) {
                bindings.remove(ctx.id().getText());
            }
            String value = replaceBinding(ctx.re());
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

    private String replaceValues(PoCoParser.ReContext rectx)
    {
        String re = rectx.getText();
        if(bindings.containsKey(rectx.qid().getText()))
        {
            String replace = "$" + rectx.qid().getText();
            String value = bindings.get(rectx.qid().getText());

            PoCoParser.OpparamlistContext opctx = rectx.opparamlist();
            List<String> res = new ArrayList<String>();
            while(opctx != null)
            {
                if(opctx.re() != null)
                {
                    String opValue = opctx.re().getText();
                    if(opctx.re().DOLLAR() != null)
                    {
                        opValue = replaceValues(opctx.re());
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
                        re = replaceValues(rectx);
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
