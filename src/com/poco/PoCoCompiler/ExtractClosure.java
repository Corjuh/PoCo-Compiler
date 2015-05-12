package com.poco.PoCoCompiler;

import com.poco.Extractor.Closure;
import com.poco.Extractor.VarTypeVal;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caoyan on 11/7/14.
 */
public class ExtractClosure extends PoCoParserBaseVisitor<Void> {
    private Closure closure;
    private boolean frmOpparlist = false;
    private String closureVal;
    private String policyName = "";
    private Stack<String> bindings;

    public ExtractClosure(Closure closure) {
        this.closure = closure;
        this.bindings = new Stack<>();
    }

    public Closure getClosure() {
        return closure;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    /**
     * Generates code for class representing a PoCo policy. This is the first visit method called.
     *
     * @param ctx
     * @return
     */
    @Override
    public Void visitPocopol(@NotNull PoCoParser.PocopolContext ctx) {
        // need handle when policy has parameteres (e.g., OutgoingMail(String ContactInfo))
        closure = new Closure();
        policyName = ctx.id().getText().trim() + "_";
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitParamlist(@NotNull PoCoParser.ParamlistContext ctx) {
        if(ctx.getText().trim().length() == 0)
            return null;
        visitChildren(ctx);
        VarTypeVal varTyCal = new VarTypeVal(ctx.qid().getText(), ctx.id().getText());
        closure.addClosure(policyName + ctx.id().getText(), varTyCal);
        return null;
    }

    @Override
    public Void visitVardecls(@NotNull PoCoParser.VardeclsContext ctx) {
        if (ctx.vardecl() != null) {
            //now only deal with single policy, need add parents in future
            visitChildren(ctx);
        }
        return null;
    }

    @Override
    public Void visitVardecl(@NotNull PoCoParser.VardeclContext ctx) {
        if (ctx.id() != null) {
            closure.addClosure(policyName + ctx.id().getText(), new VarTypeVal(null, null));
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

        //regex for Object
        String reg = "#(.+)\\{(.+)\\}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher;

        //regex for function def.
        String reg1 = "(.+)\\((.*)\\)";
        Pattern pattern1 = Pattern.compile(reg1);
        Matcher matcher1 = pattern1.matcher(str);

        //@SendMail(msg)[`javax.mail.Transport.send(#javax.mail.Message{$msg})']: RE
        if (matcher1.find()) {
            String strFunNm = matcher1.group(1).toString().trim();
            String funTyp = "java.lang.String";
            if (strFunNm.split("\\s+").length == 2) {
                String temp = strFunNm.split("\\s+")[0].trim();
                if (!temp.equals("void") && !temp.equals("*"))
                    funTyp = temp;
            }
            //e.g.,  #javax.mail.Message{$msg}
            String args = matcher1.group(2).toString().trim();
            String[] strArgs = args.split(",");
            if (strArgs != null) {
                for (int i = 0; i < strArgs.length; i++) {
                    matcher = pattern.matcher(strArgs[i]);
                    if (matcher.find()) {
                        String varTyp = matcher.group(1).toString().trim();
                        String varName = matcher.group(2).toString();
                        if (varName.startsWith("$")) {
                            String varNm = varName.substring(1, varName.length());
                            String existTyp = closure.loadClosure(policyName + varNm).getVarType();
                            if (existTyp == null || !existTyp.equals(varTyp)) {
                                String context = closure.loadClosure(policyName + varNm).getVarContext();
                                closure.updateClosure(policyName + varNm, new VarTypeVal(varTyp, context));
                            }
                        }
                    }
                }
            }
            varTyCal = new VarTypeVal(funTyp, strFunNm + "(" + args + ")");
        } else {    //@ports()[!`#int{143|993|25|110|995|2080}'] :RE
            matcher = pattern.matcher(str);
            if (matcher.find()) {
                varTyCal = new VarTypeVal(matcher.group(1).toString().trim(), getObjVal(str));
            } else {
                varTyCal = new VarTypeVal(null, getObjVal(str));
            }
        }
        closure.addClosure(policyName + ctx.id().getText(), varTyCal);
        return null;
    }

    public Void visitRe(@NotNull PoCoParser.ReContext ctx) {
        if (ctx.AT() != null) {
            bindings.push(ctx.id().getText());
            visitChildren(ctx);
            bindings.pop();
        } else {
            if (!bindings.empty()) {
                if (frmOpparlist == false) {
                    if (ctx.function() != null) {
                        closureVal = "* ";
                        if (ctx.function().INIT() != null) {
                            closureVal = ctx.function().fxnname().getText() + "new";
                        } else {
                            //has return value type
                            if (ctx.function().fxnname().getText().trim().split(" ").length == 2)
                                closureVal = ctx.function().fxnname().getText();
                            else
                                closureVal += ctx.function().fxnname().getText();
                        }
                        if (ctx.function().arglist() != null) {
                            if (ctx.function().arglist().getText().length() == 0) {
                                closureVal += "()";
                            } else {
                                frmOpparlist = true;
                                visitChildren(ctx);
                                frmOpparlist = false;
                            }
                        } else {
                            closureVal += "()";
                        }
                        String existTyp;
                        if (closure.loadClosure(policyName + bindings.peek()) != null) {
                            if (closure.loadClosure(policyName + bindings.peek()).getVarType() == null)
                                existTyp = "java.lang.String";
                            else
                                existTyp = closure.loadClosure(policyName + bindings.peek()).getVarType();
                        } else
                            existTyp = "java.lang.String";
                            closure.updateClosure(policyName + bindings.peek(), new VarTypeVal(existTyp, closureVal));

                    } else if (ctx.DOLLAR() != null) {
                        if (closure.loadClosure(policyName + ctx.qid().getText()) != null) {
                            String newTyp = closure.loadClosure(policyName + ctx.qid().getText()).getVarType();
                            String oldTyp = "";
                            if (closure.loadClosure(policyName + bindings.peek()).getVarType() != null)
                                oldTyp = closure.loadClosure(policyName + bindings.peek()).getVarType();
                            if (!newTyp.equals(oldTyp)) {
                                String varContext = closure.loadClosure(policyName + ctx.qid().getText()).getVarContext();
                                closure.updateClosure(policyName + bindings.peek(), new VarTypeVal(newTyp, varContext));

                            }
                        }
                    } else {
                        visitChildren(ctx);
                    }
                } else {  //ptFromOpparamlist = true
                    if (ctx.rewild() != null) {
                        closureVal = closureVal + "(..)";
                    } else if (ctx.qid() != null) {
                        String strval = ctx.qid().getText().trim();
                        if (closure != null && closure.isContains(policyName + strval)) {
                            closureVal += "($$" + policyName + strval + "$$)";
                        } else
                            throw new NullPointerException("No such var exist.");
                    } else if (ctx.object() != null) {
                        if (ctx.object().POUND() != null) {
                            //format as #qid{re}
                            closureVal = closureVal + "(" + ctx.object().qid().getText();
                            String reStr = ctx.object().re().getText();
                            //if reStr does not contain the "$", means the value is static,
                            //so we can check it statically, otherwise we have to check it dynamically
                            if (!reStr.contains("$")) {
                                if (reStr.equals("%"))
                                    closureVal += "$$$*.*$$$";
                                else
                                    closureVal += "$$$" + reStr.replaceAll("%", "*") + "$$$";
                            } else { //so the value will be dynamic
                                closureVal += reStr;
                            }
                            closureVal += ")";
                        } else if (ctx.id() != null) {
                            closureVal = closureVal + "(" + ctx.id().getText() + ")";
                        } else {  //Null case
                            closureVal = closureVal + "(..)";
                        }
                    } else if (ctx.rebop() != null) {
                        visitRe(ctx.re(0));
                        visitRe(ctx.re(1));
                    } else {
                        if (ctx.getText().trim().length() > 0) {
                            String reg = "\\((.+)\\)";
                            Pattern pattern = Pattern.compile(reg);
                            Matcher matcher = pattern.matcher(ctx.getText().trim());
                            if (matcher.find())
                                closureVal = closureVal + "(" + ctx.getText() + ")";
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getObjVal(String str) {
        String tempStr = "";
        String returnStr = "";
        String reg = "#(.+)\\{(.+)\\}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find())
            tempStr = matcher.group(2).toString().trim().replaceAll("\\\\", "");
        else
            tempStr = str.replaceAll("\\\\", "");

        if (tempStr.indexOf('$') == -1) {
            returnStr = tempStr;
        } else {
            while (tempStr.indexOf('$') != -1) {
                returnStr += tempStr.substring(0, tempStr.indexOf('$'));
                tempStr = tempStr.substring(tempStr.indexOf('$'), tempStr.length());
                if (tempStr.indexOf(' ') != -1) {
                    returnStr += "$$" + policyName + tempStr.substring(1, tempStr.indexOf(' ')) + "$$";
                    tempStr = tempStr.substring(tempStr.indexOf(' ') + 1, tempStr.length());
                }
            }
            returnStr += tempStr;
        }
        return returnStr;
    }
}
