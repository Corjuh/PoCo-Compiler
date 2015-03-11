package com.poco.PoCoCompiler;

import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import com.poco.Extractor.Closure;
import com.poco.Extractor.VarTypeVal;
import com.sun.deploy.config.Config;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caoyan on 11/7/14.
 */
public class ExtractClosure extends PoCoParserBaseVisitor<Void> {
    private Closure closure;

    public Closure getClosure() {
        return closure;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    public ExtractClosure(Closure closure) {
        this.closure = closure;
    }

    private boolean frmOpparlist = false;
    private boolean parseRe = false;
    private String closureVal;

    /**
     * Generates code for class representing a PoCo policy. This is the first visit method called.
     *
     * @param ctx
     * @return
     */
    @Override
    public Void visitPocopol(@NotNull PoCoParser.PocopolContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitVardecls(@NotNull PoCoParser.VardeclsContext ctx) {
        if (ctx.vardecl() != null) {
            //now only deal with single policy, need add parents in future
            closure = new Closure();
            visitChildren(ctx);
        }
        return null;
    }

    @Override
    public Void visitVardecl(@NotNull PoCoParser.VardeclContext ctx) {
        if (ctx.id() != null) {
            closure.addClosure(ctx.id().getText(), new VarTypeVal(null, null, null));
        }
        return null;
    }

    @Override
    public Void visitMacrodecls(@NotNull PoCoParser.MacrodeclsContext ctx) {
        if (ctx.macrodecl() != null) {
            if (closure == null)
                closure = new Closure();
            visitChildren(ctx);
        }
        return null;
    }

    @Override
    public Void visitMacrodecl(@NotNull PoCoParser.MacrodeclContext ctx) {
        String str = null;
        VarTypeVal varTyCal;

        if (ctx.RETYPE() != null)
            str = ctx.re().getText();
        else
            str = ctx.sre().getText();

        String reg = "#(.+)\\{(.+)\\}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            String strVal = matcher.group(1).toString().trim();
            String qid = matcher.group(2).toString();
            if (qid.contains("$")) {
                String qidName = getQid(qid);
                if (qidName != null && closure != null && closure.getContext(qidName) != null) {
                    varTyCal = new VarTypeVal(matcher.group(1).toString().trim(), ctx.re(), qid);
                } else
                    varTyCal = new VarTypeVal(matcher.group(1).toString().trim(), ctx.re(), qid);
            } else {
                varTyCal = new VarTypeVal(matcher.group(1).toString().trim(), null, getObjVal(str));
            }
        } else {
            if (str.contains("$")) {
                varTyCal = new VarTypeVal("java.lang.String", ctx.re(), ctx.re().getText());
            } else {
                varTyCal = new VarTypeVal("java.lang.String", null, getObjVal(str));
            }
        }
        closure.addClosure(ctx.id().getText(), varTyCal);
        return null;
    }

    public Void visitRe(@NotNull PoCoParser.ReContext ctx) {
        if (ctx.AT() != null) {
            if (closure != null) {
                String str = ctx.re(0).getText();
                String reg = "#(.+)\\{(.+)\\}";
                Pattern pattern = Pattern.compile(reg);
                Matcher matcher = pattern.matcher(str);
                //it is an object type value
                if (matcher.find()) {
                    closure.updateClosure(ctx.id().getText(), new VarTypeVal(matcher.group(1).toString().trim(), ctx.re(0), getObjVal(str)));
                    closure.updateLinkedVal(ctx.id().getText(), getObjVal(str));

                } else if (ctx.re(0).rewild() != null) {
                    closure.updateClosure(ctx.id().getText(), new VarTypeVal(closure.getType(ctx.id().getText()), ctx.re(0), ".*"));
                    closure.updateLinkedVal(ctx.id().getText(), ".*");
                } else { //it is not an object type value,  then we need parse it
                    if (ctx.getText().contains("$")) {
                        parseRe = true;
                        closureVal = "";
                        visitChildren(ctx);
                        closure.updateClosure(ctx.id().getText(), new VarTypeVal(closure.getType(ctx.id().getText()), ctx.re(0), closureVal));
                        closure.updateLinkedVal(ctx.id().getText(), closureVal);
                        closureVal = "";
                        parseRe = false;
                    }
                    else {
                        closure.updateClosure(ctx.id().getText(), new VarTypeVal(closure.getType(ctx.id().getText()), null, ctx.re(0).getText()));
                        closure.updateLinkedVal(ctx.id().getText(), ctx.re(0).getText());
                    }

                }
            } else //bcz variable has to be declared, so the closure should not be null!
                throw new NullPointerException("No such var exist.");
        } else {
            //only parse the value inside the AT()
            if (parseRe == true) {
                //not the parameter part
                if (frmOpparlist == false) {
                    if (ctx.qid() != null) {
                        if (closure != null && closure.getContext(ctx.qid().getText()) != null) {
                            closureVal += closure.getContext(ctx.qid().getText());
                            if (ctx.opparamlist() != null) {
                                if (ctx.opparamlist().getText().equals("%")) {
                                    closureVal += "(..)";
                                } else {
                                    frmOpparlist = true;
                                    visitChildren(ctx);
                                    frmOpparlist = false;
                                }
                            }
                        } else {
                            throw new NullPointerException("No such var exist.");
                        }
                    } else if (ctx.function() != null) {
                        closureVal += ctx.function().fxnname().getText();
                        if (ctx.function().INIT() != null) {
                            closureVal = ctx.function().fxnname().getText();
                            closureVal = closureVal.substring(0,closureVal.length()-1);
                        }
                        else if (ctx.function().arglist() != null) {
                            frmOpparlist = true;
                            visitChildren(ctx);
                            frmOpparlist = false;
                        }
                    } else {
                        closureVal += ctx.getText();
                    }
                } else { //if (frmOpparlist == true)
                    if (ctx.qid() != null) {
                        if (closure != null && closure.getContext(ctx.qid().getText()) != null)
                            closureVal += "(" + closure.getContext(ctx.qid().getText()) + ")";
                        else
                            throw new NullPointerException("No such var exist.");
                    } else //function arglist
                        closureVal += "(" + ctx.getText() + ")";
                }
            }
            else
                visitChildren(ctx);
        }
        return null;
    }

    private static String getObjVal(String str) {
        String reg = "#(.+)\\{(.+)\\}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find())
            return matcher.group(2).toString().trim();
        else
            return str;
    }

    private String up8QidVal(String str) {
        String returnStr = "";
        int index = str.indexOf("$");
        while (index != -1) {
            int breakIndex = splitStr(str);
            String qidval = str.substring(index + 1, breakIndex);
            if (closure.isContains(qidval)) {
                VarTypeVal val = closure.loadClosure(qidval);
                if (val != null && val.getVarContext() != null) {
                    returnStr += str.substring(0, index);
                    returnStr += val.getVarContext();
                } else
                    returnStr += str.substring(0, breakIndex + 1);
            } else {
                returnStr += str.substring(0, breakIndex + 1);
            }
            if (breakIndex + 1 == str.length()) {
                returnStr += str = str.substring(breakIndex, str.length());
                break;
            }
            str = str.substring(breakIndex + 1, str.length());
            index = str.indexOf("$");
        }
        return returnStr;
    }

    /**
     * Split str by (,),. to find the $
     *
     * @param str
     */
    private static int splitStr(String str) {
        int[] positions = new int[4];
        positions[0] = str.indexOf("(");
        positions[1] = str.indexOf(")");
        positions[2] = str.indexOf(".");
        positions[3] = str.indexOf(" ");

        Arrays.sort(positions);
        for (int index : positions) {
            if (index != -1) return index;
        }
        return -1;
    }

    private String getQid(String str) {
        int index = str.indexOf('$');
        if (index != -1) {
            str = str.substring(index + 1, str.length());
            return str.split(" ")[0];
        }
        return null;
    }
}
