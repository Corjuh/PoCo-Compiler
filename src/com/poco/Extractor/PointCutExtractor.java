/*
test case
ConfirmAndAllowOnlyHTTP() :
var call :RE
@ports[`#Integer{80|443}'] :RE
map (Union, -`$NetworkConnection($ports)',
     <!Action(`$NetworkConnection($ports)') => Neutral>*
     <Action(`@call[$NetworkConnection($ports)]') => +`$Confirm($message)'>
     ( <Result(`$Confirm($message)', `#Integer{JOptionPane.OK_OPTION}') => +`$call'>
               | <_ => Neutral>)
)*

 */

package com.poco.Extractor;

import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by caoyan on 11/7/14.
 */
public class PointCutExtractor extends PoCoParserBaseVisitor<Void> {
    private LinkedHashSet<LinkedHashSet<String>> nodes = new LinkedHashSet<LinkedHashSet<String>>();

    private LinkedHashSet<String> pointcutStrings = new LinkedHashSet<>();
    private String pointcutStr;

    private Closure closure;

    private boolean ptFromOpparamlist = false;

    private boolean loadfmClosure = false;
    private ArrayList<String> clousreAl = null;


    public PointCutExtractor(Closure closure) {
        this.closure = closure;
    }

    private static String scrubString(String input) {
        return input.replaceAll("(%|\\$[a-zA-Z0-9\\.\\-_]+)", "");
    }

    @Override
    public Void visitRe(@NotNull PoCoParser.ReContext ctx) {
        boolean isFound = false;
        if (ptFromOpparamlist == false) {
            pointcutStr = " * ";
            if (ctx.rewild() != null) {
                pointcutStr = pointcutStr + " * (. .); ";
            } else if (ctx.qid() != null) {
                /**load from closure */
                clousreAl = new ArrayList<String>();
                loadFromClosure(ctx.qid().getText());
                if (clousreAl != null && clousreAl.size() > 0) {
                    for (Iterator<String> it = clousreAl.iterator(); it.hasNext(); ) {
                        pointcutStr = pointcutStr + it.next();
                    }
                } else
                    pointcutStr = pointcutStr + ctx.qid().getText();
                if (ctx.opparamlist() != null) {
                    ptFromOpparamlist = true;
                    visitChildren(ctx);
                    ptFromOpparamlist = false;
                } else {
                    pointcutStr = pointcutStr + "(..)";
                    pointcutStrings.add(pointcutStr);
                    nodes.add(pointcutStrings);
                    pointcutStrings = new LinkedHashSet<String>();
                }
            } else if (ctx.function() != null) {
                pointcutStr = pointcutStr + ctx.function().fxnname().getText();
                if (ctx.function().INIT() != null) {
                    pointcutStr = pointcutStr + "new";
                }
                if (ctx.function().arglist() != null) {
                    ptFromOpparamlist = true;
                    visitChildren(ctx);
                    ptFromOpparamlist = false;
                }
            } else if (ctx.object() != null) {
                pointcutStr = pointcutStr + ctx.object().qid().getText() + ".";
                if (ctx.object().fieldlist() != null) {
                    pointcutStr = pointcutStr + "&& (call (" + ctx.object().fieldlist() + "))";
                } else if (ctx.re() != null) {
                    // need handle re , not sure how yet, need to be modified
                }
            } else if (ctx.AT() != null) {
                if (ctx.re(0) != null) {
                    visitRe(ctx.re(0));
                }
            }
        } else {  //ptFromOpparamlist = true
            if (ctx.rewild() != null) {
                //need fix later, not sure how to handle #String{%.class}
                if (loadfmClosure == true)
                    clousreAl.add(pointcutStr + "( .. )");
                else
                    pointcutStr = pointcutStr + "( .. )";
            } else if (ctx.qid() != null) {
                clousreAl = new ArrayList<String>();
                loadFromClosure(ctx.qid().getText());
                if (clousreAl != null && clousreAl.size() > 0) {
                    String currentPCstr = pointcutStr;
                    for (Iterator<String> it = clousreAl.iterator(); it.hasNext(); ) {
                        pointcutStr = currentPCstr + it.next();
                        pointcutStrings.add(pointcutStr);
                    }
                } else {
                    pointcutStr = pointcutStr + "(" + ctx.qid().getText() + ") ";
                    pointcutStrings.add(pointcutStr);
                }
                nodes.add(pointcutStrings);
                pointcutStrings = new LinkedHashSet<String>();
            } else if (ctx.AT() != null) {
                if (loadfmClosure == true)
                    clousreAl.add(pointcutStr + "(" + ctx.re(0).getText() + ")");
                else {
                    pointcutStr = pointcutStr + "(" + ctx.re(0).getText() + ")";
                }
            } else if (ctx.object() != null) {
                if (ctx.object().POUND() != null) {
                    if (ctx.object().re() != null) {
                        if (loadfmClosure == true)
                            clousreAl.add("(..) ");
                        else
                            pointcutStr = pointcutStr + "(..) ";
                        //visitChildren(ctx.object().re());
                    } else { //fieldList
                        if (loadfmClosure == true)
                            clousreAl.add(pointcutStr + "(" + ctx.object().qid().getText() + ")");
                        else
                            pointcutStr = pointcutStr + "(" + ctx.object().qid().getText() + ")";
                    }
                } else if (ctx.id() != null) {
                    if (loadfmClosure == true)
                        clousreAl.add("(" + ctx.id().getText() + ") ");
                    else
                        pointcutStr = pointcutStr + "(" + ctx.id().getText() + ") ";
                } else {  //Null case
                    if (loadfmClosure == true)
                        clousreAl.add("(..) ");
                    else
                        pointcutStr = pointcutStr + "(..) ";
                }
            } else if (ctx.function() != null) {
                //TODO: add detail later
                if (loadfmClosure == true)
                    clousreAl.add("(" + ctx.function().fxnname().getText() + ctx.function().arglist().getText() + ")");
                else
                    pointcutStr = pointcutStr + ctx.function().fxnname().getText();
            } else if (ctx.rebop() != null) {
                visitRe(ctx.re(0));
                visitRe(ctx.re(1));
            } else {
                if (loadfmClosure == true)
                    clousreAl.add("(" + ctx.getText() + ") ");
                else
                    pointcutStr = pointcutStr + "(" + ctx.getText() + ") ";
            }
        }

        return null;
    }

    @Override
    public Void visitExecution(@NotNull PoCoParser.ExecutionContext ctx) {
        if (ctx.map() != null) {
            visitSre(ctx.map().sre());
            pointcutStrings = new LinkedHashSet<String>();
            pointcutStrings = null;
            visitExecution(ctx.map().execution());
        } else {
            visitChildren(ctx);
        }
        return null;
    }

    @Override
    public Void visitExch(@NotNull PoCoParser.ExchContext ctx) {
        pointcutStr = null;
        pointcutStrings = new LinkedHashSet<String>();
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
    public Void visitIre(@NotNull PoCoParser.IreContext ctx) {
        if (ctx.ACTION() != null)
            visitChildren(ctx);
        else
            visitRe(ctx.re(0));
        return null;
    }

    public Void visitSre(@NotNull PoCoParser.SreContext ctx) {
        if (ctx.NEUTRAL() != null) {
            pointcutStr = null;
        }
        if (ctx.qid() != null) {
            pointcutStr = " * ";
            clousreAl = new ArrayList<String>();
            loadFromClosure(ctx.qid().getText());
            if (clousreAl != null && clousreAl.size() > 0) {
                String currentstr = pointcutStr;
                pointcutStr = "";
                for (Iterator<String> it = clousreAl.iterator(); it.hasNext(); ) {
                    pointcutStr = currentstr + it.next() + "(..) ";
                    pointcutStrings.add(pointcutStr);
                }
            } else {
                pointcutStr = pointcutStr + ctx.qid().getText() + "(..)";
                pointcutStrings.add(pointcutStr);
            }
            nodes.add(pointcutStrings);
            pointcutStrings = new LinkedHashSet<String>();
        } else if (ctx.srebop() != null) {
            visitChildren(ctx.sre(0));
            pointcutStrings.add(pointcutStr);
            nodes.add(pointcutStrings);
            pointcutStrings = new LinkedHashSet<String>();
            visitChildren(ctx.sre(1));
            pointcutStrings.add(pointcutStr);
            nodes.add(pointcutStrings);
            pointcutStrings = new LinkedHashSet<String>();
        } else {
            visitChildren(ctx);
        }
        return null;
    }


    public LinkedHashSet<LinkedHashSet<String>> getgetPCStrings() {
        return new LinkedHashSet<LinkedHashSet<String>>(nodes);
    }

    public void loadFromClosure(String varName) {
        if (closure != null)
            if (closure.loadClosure(varName) != null) {
                if (closure.loadClosure(varName).getVarType() == VarTypeVal.ClosureType.RE_TYPE &&
                        closure.loadClosure(varName).getReContext() != null) {
                    loadfmClosure = true;
                    visitRe(closure.loadClosure(varName).getReContext());
                } else if (closure.loadClosure(varName).getVarType() == VarTypeVal.ClosureType.SRE_TYPE &&
                        closure.loadClosure(varName).getSreContext() != null) {
                    loadfmClosure = true;
                    visitSre(closure.loadClosure(varName).getSreContext());
                }
            }
        loadfmClosure = false;
    }
}
