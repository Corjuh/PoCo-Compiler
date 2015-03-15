package com.poco.Extractor;

import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caoyan on 11/7/14.
 */
public class PointCutExtractor extends PoCoParserBaseVisitor<Void> {
    private LinkedHashSet<String> nodes = new LinkedHashSet<String>();
    //use this to distinguish the result from the action for generating advices
    private LinkedHashSet<String> nodes4Results = new LinkedHashSet<String>();
    private String pointcutStr;
    private Closure closure;
    private boolean ptFromOpparamlist = false;
    private boolean isResult = false;
    private boolean varBinding = false;
    
    public PointCutExtractor(Closure closure) {
        this.closure = closure;
    }

    private static String scrubString(String input) {
        return input.replaceAll("(%|\\$[a-zA-Z0-9\\.\\-_]+)", "");
    }

    @Override
    public Void visitRe(@NotNull PoCoParser.ReContext ctx) {
        if (ptFromOpparamlist == false) {
            if (varBinding == false)
                pointcutStr = "* ";
            else
                pointcutStr += "* ";
            if (ctx.rewild() != null) {
                pointcutStr = pointcutStr + " * (..); ";
            } else if (ctx.qid() != null) {
                pointcutStr = "$$" + ctx.qid().getText();
                if (ctx.opparamlist() != null) {
                    if (ctx.opparamlist().getText().equals("%")) {
                        pointcutStr += "(..)";
                        add2NodesNodes4Result(pointcutStr);
                    } else {
                        ptFromOpparamlist = true;
                        visitChildren(ctx);
                        ptFromOpparamlist = false;
                        add2NodesNodes4Result(pointcutStr);
                    }
                } else {
                    add2NodesNodes4Result(pointcutStr); 
                }
            } else if (ctx.function() != null) {
                if (ctx.function().INIT() != null) {
                    // delete * since there is no return val for new
                    pointcutStr = pointcutStr.substring(0, pointcutStr.length() - 2);
                    pointcutStr += ctx.function().fxnname().getText() + "new";
                } else
                    pointcutStr = pointcutStr + ctx.function().fxnname().getText();
                if (ctx.function().arglist() != null) {
                    if (ctx.function().arglist().getText().length() == 0) {
                        pointcutStr += "()";
                    } else {
                        ptFromOpparamlist = true;
                        visitChildren(ctx);
                        ptFromOpparamlist = false;
                    }
                }
                add2NodesNodes4Result(pointcutStr);
            } else if (ctx.object() != null) {
                if (ctx.object().POUND() != null)
                    pointcutStr = ctx.object().re().getText();
                else if (ctx.object().qid() != null)
                    pointcutStr = pointcutStr + ctx.object().qid().getText();
            } else if (ctx.AT() != null) {
                varBinding = true;
                pointcutStr = "@" + ctx.id().getText() + "@";
                visitChildren(ctx);
                varBinding = false;
            } else {
                visitChildren(ctx);
            }
        } else {  //ptFromOpparamlist = true
            if (ctx.rewild() != null) {
                pointcutStr = pointcutStr + "(..)";
            } else if (ctx.qid() != null) {
                String strval = ctx.qid().getText();
                if (closure != null && closure.isContains(strval))
                    pointcutStr += "($$" + strval + ")";
                else
                    throw new NullPointerException("No such var exist."); 
            } else if (ctx.AT() != null) {
                String strval = ctx.id().getText();
                if (closure != null && closure.isContains(strval))
                    pointcutStr += "($$@" + strval + "@) ";
                else
                    throw new NullPointerException("No such var exist.");
            } else if (ctx.object() != null) {
                if (ctx.object().POUND() != null) {
                    //format as #qid{re}
                    pointcutStr = pointcutStr + "(" + ctx.object().qid().getText();
                    String reStr = ctx.object().re().getText();
                    //if reStr does not contain the "$", means the value is static,
                    //so we can check it statically, otherwise we have to check it dynamically
                    if (!reStr.contains("$")) {
                        if (reStr.equals("%"))
                            pointcutStr += "$$*.*$$";
                        else
                            pointcutStr += "$$" + reStr.replaceAll("%", "*") + "$$";
                    } else { //so the value will be dynamic
                        pointcutStr += reStr;
                    }
                    pointcutStr += ")";
                } else if (ctx.id() != null) {
                    pointcutStr = pointcutStr + "(" + ctx.id().getText() + ")";
                } else {  //Null case
                    pointcutStr = pointcutStr + "(..)";
                }

            } else if (ctx.function() != null) {
                //TODO: add detail later
                pointcutStr = pointcutStr + ctx.function().fxnname().getText();
            } else if (ctx.rebop() != null) {
                visitRe(ctx.re(0));
                visitRe(ctx.re(1));
            } else {
                if(ctx.getText().trim().length() >0)
                    pointcutStr = pointcutStr + "(" + ctx.getText() + ")";
            }
        }
        return null;
    }

    @Override
    public Void visitExecution(@NotNull PoCoParser.ExecutionContext ctx) {
        if (ctx.map() != null) {
            visitSre(ctx.map().sre());
            visitExecution(ctx.map().execution());
        } else {
            visitChildren(ctx);
        }
        return null;
    }

    @Override
    public Void visitExch(@NotNull PoCoParser.ExchContext ctx) {
        pointcutStr = null;
        if (ctx.INPUTWILD() != null) {
            //just need monitor the action on the RHS of =>
            visitSre(ctx.sre());
        } else if (ctx.matchs() != null) {
            visitMatchs(ctx.matchs());
            visitSre(ctx.sre());
        }
        return null;
    }

    @Override
    public Void visitMatch(@NotNull PoCoParser.MatchContext ctx) {
        /* match need to tracked for pointcut signature */
        if (ctx.ire() != null) {
            visitChildren(ctx);
        } else if (ctx.SUBSET() != null) {
            visitSre(ctx.sre(0));
            visitSre(ctx.sre(1));
        } else if (ctx.INFINITE() != null) {
            visitSre(ctx.sre(0));
        } else if (ctx.SREEQUALS() != null) {
            visitSre(ctx.sre(0));
            visitSre(ctx.sre(1));
        }
        return null;
    }

    @Override
    public Void visitMacrodecls(@NotNull PoCoParser.MacrodeclsContext ctx) {
        return null;
    }

    @Override
    public Void visitVardecls(@NotNull PoCoParser.VardeclsContext ctx) {
        return null;
    }

    @Override
    public Void visitIre(@NotNull PoCoParser.IreContext ctx) {
        if (ctx.ACTION() != null) {
            visitChildren(ctx);
        } else {
            isResult = true;
            visitRe(ctx.re(0));
            isResult = false;
        }
        return null;
    }

    public Void visitSre(@NotNull PoCoParser.SreContext ctx) {
        if (ctx.NEUTRAL() != null) {
            pointcutStr = null;
        }
        if (ctx.qid() != null) {
            pointcutStr = " * "; 
            pointcutStr = pointcutStr + ctx.qid().getText() + "(..)";
            add2NodesNodes4Result(pointcutStr);
        } else if (ctx.srebop() != null) {
            visitChildren(ctx.sre(0));
            add2NodesNodes4Result(pointcutStr);
            visitChildren(ctx.sre(1));
            add2NodesNodes4Result(pointcutStr);
        }  else if (ctx.sreuop() != null) {
            visitChildren(ctx.sre(0));
            add2NodesNodes4Result(pointcutStr);
        }  else {
            visitChildren(ctx);
        }
        return null;
    }

    public LinkedHashSet<String> getPCStrings() {
        return new LinkedHashSet<String>(nodes);
    }

    public LinkedHashSet<String> getPCStrs4Results() {
        return new LinkedHashSet<String>(nodes4Results);
    } 

    public void add2NodesNodes4Result(String pointcutStr) {
        String reg = "@(.+)@(.+)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(pointcutStr);
        if (matcher.find()) {
            if (isResult) {
                //if it is result, move the pointcut from the action
                nodes.remove(matcher.group(2));
                nodes4Results.add(pointcutStr);
            } else {
                //if(!nodes4Results.contains(pointcutStr))
                nodes.remove(matcher.group(2));
                nodes.add(pointcutStr);
            }
        } else {
            if (isResult) {
                nodes.remove(pointcutStr);
                nodes4Results.add(pointcutStr);
            } else {
                nodes.add(pointcutStr);
            }
        }
    }
}
