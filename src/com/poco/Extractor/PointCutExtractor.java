package com.poco.Extractor;

import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;
import sun.jvm.hotspot.debugger.posix.elf.ELFSectionHeader;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caoyan on 11/7/14.
 */
public class PointCutExtractor extends PoCoParserBaseVisitor<Void> {
    private HashMap<String, HashSet<String>> nodes = new HashMap<String, HashSet<String>>();
    //use this to distinguish the result from the action for generating advices
    private Hashtable<String, HashSet<String>> nodes4Results = new Hashtable<String, HashSet<String>>();
    private String pointcutStr;
    private HashSet<String> varBind4thisPC = new HashSet<String>();
    //used to store all the paramaters that needed as obj in order to promote or process
    //since not all the variables are needed as obj type
    private HashSet<String> objParams = new HashSet<String>();
    private Closure closure;
    private boolean ptFromOpparamlist = false;
    private boolean isResult = false;
    private boolean varBinding = false;
    private String policyName = "";

    public PointCutExtractor(Closure closure) {
        this.closure = closure;
        pointcutStr = "";
    }

    private static String scrubString(String input) {
        return input.replaceAll("(%|\\$[a-zA-Z0-9\\.\\-_]+)", "");
    }

    @Override
    public Void visitPocopol(@NotNull PoCoParser.PocopolContext ctx){
        policyName = ctx.id().getText().trim()+"_";
        visitChildren(ctx);
        return null;
    }


    @Override
    public Void visitRe(@NotNull PoCoParser.ReContext ctx) {
        if (ptFromOpparamlist == false) {
            pointcutStr = "* ";
            if (ctx.rewild() != null) {
                pointcutStr = pointcutStr + " * (..); ";
            } else if (ctx.qid() != null) {
                if (closure != null && closure.isContains(policyName+ctx.qid().getText())) {
                    String funcStr = closure.getContext(policyName+ctx.qid().getText());
                    //function name included return type;
                    if(getFunctionName(funcStr).split(" ").length ==2)
                        pointcutStr = funcStr;
                    else if(getFunctionName(funcStr).contains(".new")) {
                        pointcutStr = funcStr;
                    }else
                        pointcutStr +=funcStr;
                } else {
                    throw new NullPointerException("No such var exist.");
                }
                if (ctx.opparamlist() != null) {
                    pointcutStr = getFunctionName(pointcutStr);
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
                    if (pointcutStr == null)
                        pointcutStr = ctx.function().fxnname().getText() + "new";
                    else {
                        if (pointcutStr.length() > 1 && pointcutStr.startsWith("*"))
                            pointcutStr = pointcutStr.substring(1, pointcutStr.length()).trim();
                        pointcutStr += ctx.function().fxnname().getText() + "new";
                    }
                    if (ctx.function().arglist() != null) {
                        if (ctx.function().arglist().getText().equals("%")) {
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
                } else {
                    pointcutStr = pointcutStr + ctx.function().fxnname().getText();
                    if (ctx.function().arglist() != null) {
                        if (ctx.function().arglist().getText().length() == 0) {
                            pointcutStr += "()";
                        } else {
                            ptFromOpparamlist = true;
                            visitChildren(ctx);
                            ptFromOpparamlist = false;
                        }
                    }else
                        pointcutStr += "()";
                    add2NodesNodes4Result(pointcutStr);
                }
            }else if (ctx.object() != null) {
                    if (ctx.object().POUND() != null)
                        pointcutStr = ctx.object().re().getText();
                    else if (ctx.object().qid() != null) {
                        pointcutStr = pointcutStr + ctx.object().qid().getText();
                    }
                } else if (ctx.AT() != null) {
                    varBinding = true;
                    varBind4thisPC.add(policyName +ctx.id().getText());
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
                    if (closure != null && closure.isContains(policyName+strval)) {
                        pointcutStr += "($$" +policyName+strval + "$$)";
                        objParams.add(policyName+strval);
                    } else
                        throw new NullPointerException("No such var exist.");
                } else if (ctx.AT() != null) {
                    String strval = ctx.id().getText();
                    if (closure != null && closure.isContains(policyName+strval)) {
                        varBind4thisPC.add(policyName+ctx.id().getText());
                        pointcutStr += "($$" + policyName+strval + "$$) ";
                        objParams.add(policyName+strval);
                    } else
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
                                pointcutStr += "$$$*.*$$$";
                            else
                                pointcutStr += "$$$" + reStr.replaceAll("%", "*") + "$$$";
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
                    if (ctx.getText().trim().length() > 0) {
                        if(!isEmptyParent(ctx.getText().trim()))
                            pointcutStr = pointcutStr + "(" + ctx.getText() + ")";
                    }
                }
            }
            return null;
        }

        @Override
        public Void visitExecution (@NotNull PoCoParser.ExecutionContext ctx){
            if (ctx.map() != null) {
                visitSre(ctx.map().sre());
                visitExecution(ctx.map().execution());
            } else {
                visitChildren(ctx);
            }
            return null;
        }

        @Override
        public Void visitExch (@NotNull PoCoParser.ExchContext ctx){
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
        public Void visitMatch (@NotNull PoCoParser.MatchContext ctx){
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
        public Void visitMacrodecls (@NotNull PoCoParser.MacrodeclsContext ctx){
            return null;
        }

        @Override
        public Void visitVardecls (@NotNull PoCoParser.VardeclsContext ctx){
            return null;
        }

        @Override
        public Void visitIre (@NotNull PoCoParser.IreContext ctx){
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
        } else if (ctx.srebop() != null) {
            visitChildren(ctx.sre(0));
            visitChildren(ctx.sre(1));
        } else if (ctx.sreuop() != null) {
            visitChildren(ctx.sre(0));
        } else {
            visitChildren(ctx);
        }
        return null;
    }

    public Hashtable<String, HashSet<String>> getPCStrings() {
        return new Hashtable<String, HashSet<String>>(nodes);
    }

    public Hashtable<String, HashSet<String>> getPCStrs4Results() {
        return new Hashtable<String, HashSet<String>>(nodes4Results);
    }

    public HashSet<String> getObjParams() {
        return objParams;
    }

    public void add2NodesNodes4Result(String pointcutStr) {
        pointcutStr = pointcutStr.trim();
        if (!isResult) {
            if (nodes.containsKey(pointcutStr))
                varBind4thisPC.addAll(nodes.get(pointcutStr));
            nodes.put(pointcutStr, varBind4thisPC);
            varBind4thisPC = new HashSet<String>();
        } else {
            if (nodes.containsKey(pointcutStr)) {
                varBind4thisPC.addAll(nodes.get(pointcutStr));
                nodes.remove(pointcutStr);
            }
            if (nodes4Results.containsKey(pointcutStr))
                varBind4thisPC.addAll(nodes4Results.get(pointcutStr));
            nodes4Results.put(pointcutStr, varBind4thisPC);
            varBind4thisPC = new HashSet<String>();
        }
    }

    public String getFunctionName(String str) {
        int leftPara = str.indexOf("(");
        int righPara = str.indexOf(")");
        if(leftPara!=-1 && righPara!=-1 && leftPara <righPara)
            return str.substring(0,leftPara);
        return str;
    }

    public boolean isEmptyParent(String str) {
        str = str.trim();
        if(str.length() >=2 && str.charAt(0)== '(' && str.charAt(str.length()-1) ==')') {
            if(str.length() == 2)
                 return true;
            if(str.substring(1,str.length()-1).trim().length() ==0)
                return true;
        }
        return false;
    }
}