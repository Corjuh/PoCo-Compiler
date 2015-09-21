package com.poco.Extractor;

import com.poco.PoCoCompiler.PoCoUtils;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.HashSet;
import java.util.Stack;

/**
 * Created by caoyan on 11/7/14.
 */
public class ExtractClosure extends PoCoParserBaseVisitor<Void> {
    private Closure closure;
    private String policyName = "";
    private StringBuilder currParsArgs;
    private StringBuilder currParsVal;
    private Stack<String> bindings;
    private Stack<Integer> parsingFlags;
    private Stack<Integer> flagStack4Funcs;
    //this stack is used to flag different status related to the RE
    private Stack<Integer> flagStack4RE;
    private Stack<String> funArgTypLs;

    //will use to check if there are duplicated policy names defined within a file
    private HashSet<String> policyNames;


    public ExtractClosure() {
        closure = new Closure();
        this.bindings = new Stack<>();
        this.parsingFlags = new Stack<>();
        this.flagStack4Funcs = new Stack<>();
        this.flagStack4RE = new Stack<>();
        this.funArgTypLs = new Stack<>();
        currParsVal = new StringBuilder();
        currParsArgs = new StringBuilder();
        policyNames = new HashSet<>();
    }

    public Closure getClosure() {
        return this.closure;
    }

    private void resetCurrParsVal() {
        currParsVal = new StringBuilder();
    }

    private void resetCurrParsArgs() {
        currParsArgs = new StringBuilder();
    }

    @Override
    public Void visitPocopol(@NotNull PoCoParser.PocopolContext ctx) {
        String pName = ctx.id().getText().trim();

        //step 1: check if the policy name is unique or not,
        //if not, throw exception and quit
        if (policyNames.contains(pName)) {
            try {
                throw new Exception("More than one policy named: \"" + pName + "\" exist.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.exit(-1);
            }
        } else
            policyNames.add(pName);

        policyName = pName + "_";

        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitParamlist(@NotNull PoCoParser.ParamlistContext ctx) {
        if (ctx.getText().trim().length() > 0) {
            visitChildren(ctx);
            String varVal = PoCoUtils.attachPolicyName(policyName, ctx.id().getText());
            VarTypeVal varTyVal = new VarTypeVal(ctx.qid().getText(), varVal);

            String varName = policyName + ctx.id().getText();
            if (closure.isVarsContain(varName) || closure.isFunctionsContain(varName)) {
                try {
                    throw new Exception("More than one variable named: \"" + varName + "\" exist.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                closure.addVar(varName, varTyVal);

        }
        return null;
    }

    @Override
    public Void visitVardecls(@NotNull PoCoParser.VardeclsContext ctx) {
        if (ctx.vardecl() != null)
            visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitVardecl(@NotNull PoCoParser.VardeclContext ctx) {
        if (ctx.id() != null) {
            //check if the variable name is unique!
            String varName = policyName + ctx.id().getText();
            if (closure.isVarsContain(varName) || closure.isFunctionsContain(varName))
                try {
                    throw new Exception("More than one variable named: \"" + varName + "\" exist.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            else
                closure.addVar(varName, new VarTypeVal(null, null));
        }
        return null;
    }

    @Override
    public Void visitMacrodecls(@NotNull PoCoParser.MacrodeclsContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitMacrodecl(@NotNull PoCoParser.MacrodeclContext ctx) {
        bindings.push(ctx.id().getText().trim());
        if (ctx.BOOLUOP() != null)
            flagStack4Funcs.push(ParsFlgConsts.closurFunwUop);
        else
            flagStack4Funcs.push(ParsFlgConsts.clousrFunc);
        visitChildren(ctx);
        bindings.pop();
        flagStack4Funcs.pop();
        return null;
    }

    public Void visitRe(@NotNull PoCoParser.ReContext ctx) {
        if (ctx.AT() != null) {
            handleVarBinding(ctx);
        } else {
            if (!isSkipable(ctx.getText())) {
                if (PoCoUtils.notParsingArgs(parsingFlags)) {
                    if (ctx.function() != null) {
                        handleFunc(ctx);
                    } else if (ctx.object() != null) {
                        handleObj(ctx);
                    } else if (ctx.rebop() != null) {
                        handleRebop(ctx);
                    } else if (ctx.qid() != null) {
                        handleQid(ctx);
                    } else {
                        visitChildren(ctx);
                    }
                } else {
                    //if it parsing functions, no need update parameter
                    if (isParsClosurFuncs()) {
                        currParsArgs.append(ctx.getText().trim() + ",");
                    } else {
                        String str = ctx.getText().trim();
                        if (ctx.object() != null && isVariable(ctx.object().re().getText())) {
                            String newTyp = ctx.object().qid().getText().trim();
                            String varName = ctx.object().re().getText().trim();
                            up8ClosurVarTyp(policyName + varName.substring(1), newTyp);
                        }
                        if (isVariable(str)) {
                            String temp = PoCoUtils.attachPolicyName(policyName, str);
                            if (closure != null && closure.isFunctionsContain(temp.substring(1))) {
                                str = closure.getFunctionContext(temp);
                            } else if (funArgTypLs.size() > 0) {
                                String argTypOnStack = funArgTypLs.peek();
                                while (argTypOnStack.equals("\\*")) {
                                    currParsArgs.append("\\*,");
                                    funArgTypLs.pop();
                                    argTypOnStack = funArgTypLs.peek();
                                }
                                String varType = PoCoUtils.getObjType(argTypOnStack);
                                String varName = PoCoUtils.attachPolicyName(policyName, str);
                                str = "#" + varType + "{" + varName + "}" + ",";
                                funArgTypLs.pop();
                            } else if (closure != null && closure.isVarsContain(temp.substring(1))) {
                                str = "$" + temp + ",";
                            } else
                                PoCoUtils.throwNoSuchVarExpection(temp.substring(1));
                        } else if (ctx.rewild() != null) {
                            if (funArgTypLs.size() == 0) {
                                str += "*,";
                            } else {
                                str = funArgTypLs.peek() + ",";
                                funArgTypLs.pop();
                            }
                        } else
                            str += ",";
                        currParsArgs.append(str);
                    }
                }
            }
        }
        return null;
    }

    private boolean isVariable(String str) {
        return str != null && str.startsWith("$");
    }

    private void handleVarBinding(@NotNull PoCoParser.ReContext ctx) {
        bindings.push(ctx.id().getText());
        resetCurrParsVal();

        //the case of parsing the functions
        if (isParsClosurFuncs()) {
            visitChildren(ctx);
//            String varName = policyName + bindings.peek();
//            if (closure.loadFrmVars(varName) != null) {
//                String typStr = closure.loadFrmVars(varName).getVarType();
//                closure.updateVar(varName, new VarTypeVal(typStr, currParsVal.toString()));
//            }
        } else { //the case of parsing executions
            String varName = PoCoUtils.attachPolicyName(policyName, "$" + ctx.id().getText());
            if (PoCoUtils.isParsingArg(parsingFlags)) {
                String argTypOnStack = funArgTypLs.peek();
                while (argTypOnStack.equals("\\*")) {
                    currParsArgs.append("\\*,");
                    funArgTypLs.pop();
                    argTypOnStack = funArgTypLs.peek();
                }
                String varType = PoCoUtils.getObjType(argTypOnStack);
                currParsArgs.append("#" + varType + "{" + varName + "},");
                funArgTypLs.pop();
            } else {
                visitChildren(ctx);
                String varType = PoCoUtils.getMethodRtnTyp(currParsVal.toString());
                closure.updateVar(policyName + bindings.peek(), new VarTypeVal(varType, null));
            }
            bindings.pop();
        }
    }

    private void handleQid(@NotNull PoCoParser.ReContext ctx) {
        String varName = policyName + ctx.qid().getText();

        if (closure != null && closure.loadFrmFunctions(varName) != null) {
            if (closure.loadFrmFunctions(varName).getVarContext() != null) {
                String temp = closure.loadFrmFunctions(varName).getVarContext();
                if (ctx.opparamlist() != null) {
                    if (ctx.opparamlist().getText().trim().equals("%"))
                        return;
                    //get the function arguments' signature
                    String methodName = PoCoUtils.getMethodName(temp);
                    String[] args = PoCoUtils.getMethodArgLs(temp).split(",");
                    for (int i = args.length - 1; i >= 0; i--) {
                        funArgTypLs.push(args[i]);
                    }
                    parsingFlags.push(ParsFlgConsts.parsArgs);
                    resetCurrParsArgs();
                    visitChildren(ctx.opparamlist());
                    temp = methodName + "(" + PoCoUtils.trimEndPunc(currParsArgs.toString(), ",") + ")";
                    parsingFlags.pop();
                }
                resetCurrParsVal();
                currParsVal.append(temp);
                closure.updateFunction(varName, new VarTypeVal("java.lang.String", currParsVal.toString()));
            }
        }
    }

    private void handleRebop(@NotNull PoCoParser.ReContext ctx) {
        resetCurrParsVal();
        flagStack4RE.push(ParsFlgConsts.isReBop);
        visitRe(ctx.re(0));
        if (!ctx.re(1).getText().trim().equals("") && !ctx.re(1).getText().trim().equals("()")) {
            currParsVal.append("|");
            visitRe(ctx.re(1));
        }
        if (bindings.size() > 0) {
            String varName = policyName + bindings.peek();
            if (isParsClosurFuncs()) {
                if (closure.isVarsContain(varName) || closure.isFunctionsContain(varName)) {
                    try {
                        throw new Exception("More than one variable named: \"" + varName + "\" exist.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    closure.addFunction(varName, new VarTypeVal("java.lang.String", currParsVal.toString()));
            }
        }
        flagStack4RE.pop();
    }

    private void handleObj(@NotNull PoCoParser.ReContext ctx) {
        String varTyp = ctx.object().qid().getText();
        if (ctx.object().re().DOLLAR() != null) {
            String varName = policyName + ctx.object().re().qid().getText();
            up8ClosurVarTyp(varName, varTyp);
        } else {
            if (isParsClosurFuncs()) {
                String temp = ctx.object().re().getText();
                if (containsVariables(ctx.object().re().getText())) {
                    temp = getVarValFrmClosure(ctx.object().re().getText());
                }
                if (PoCoUtils.closurFunwUop(flagStack4Funcs))
                    temp = PoCoUtils.attachPolicyName(policyName, "!#" + varTyp + "{" + temp + "}");
                else
                    temp = PoCoUtils.attachPolicyName(policyName, "#" + varTyp + "{" + temp + "}");

                //check if the variable name is unique!
                String varName = policyName + bindings.peek();
                if (closure.isVarsContain(varName) || closure.isFunctionsContain(varName)) {
                    try {
                        throw new Exception("More than one variable named: \"" + varName + "\" exist.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    closure.addFunction(varName, new VarTypeVal(varTyp, temp));
            }
        }
    }

    private String getVarValFrmClosure(String str) {
        String varName = PoCoUtils.getVariableName(str);
        if (closure != null && closure.isFunctionsContain(policyName + varName)) {
            String newVal = closure.loadFrmFunctions(policyName + varName).getVarContext();
            if (newVal != null && PoCoUtils.getObjVal(newVal) != null)
                newVal = PoCoUtils.getObjVal(newVal);

            str = str.replace("$" + varName, newVal);
        }
        return str;
    }

    private void handleFunc(@NotNull PoCoParser.ReContext ctx) {
        String validStr = "";
        String methodName = ctx.function().fxnname().getText().trim();

        if (methodName.startsWith("$")) {
            validStr = PoCoUtils.attachPolicyName(policyName, methodName);
        } else if (ctx.function().INIT() != null) {
            validStr = methodName + "new";
        } else {
            //if it is not constructor case, then it is necessary to check whether the return type is
            // specified or not, if not, then attach "* " as its prefix.
            if (methodName.split("\\s").length == 1)
                validStr = "* " + methodName;
            else
                validStr = methodName;
        }

        if (ctx.function().arglist() != null) {
            if (validStr.indexOf('(') != -1) {
                validStr = validStr.substring(0, validStr.indexOf('('));
            }
            parsingFlags.push(ParsFlgConsts.parsArgs);
            resetCurrParsArgs();
            visitChildren(ctx.function().arglist());
            validStr += "(" + PoCoUtils.trimEndPunc(currParsArgs.toString(), ",") + ")";
            parsingFlags.pop();
        }

        if (isParsClosurFuncs()) {
            if (PoCoUtils.closurFunwUop(flagStack4Funcs))
                validStr = "[^" + validStr + "]";

            if (PoCoUtils.isReBopFlag(flagStack4RE))
                currParsVal.append(validStr);
            else {
                //check if the variable name is unique!
                String varName = policyName + bindings.peek();
                if (closure.isVarsContain(varName) || closure.isFunctionsContain(varName)) {
                    try {
                        throw new Exception("More than one variable named: \"" + varName + "\" exist.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    closure.addFunction(varName, new VarTypeVal("java.lang.String", validStr));
            }
        } else {
            currParsVal.append(validStr);
        }
    }

    private void up8ClosurVarTyp(String varName, String newTyp) {
        if (closure != null && closure.loadFrmVars(varName) != null) {
            String oldTyp = closure.loadFrmVars(varName).getVarType();
            if (oldTyp == null && !newTyp.equals(oldTyp)) {
                String oldVal = closure.loadFrmVars(varName).getVarContext();
                closure.updateVar(varName, new VarTypeVal(newTyp, oldVal));
            }
        }
    }

    private boolean isSkipable(String ctxStrVal) {
        return (ctxStrVal.trim().equals("") || ctxStrVal.trim().equals("()"));
    }

    private boolean containsVariables(String str) {
        return str.contains("$");
    }

    private boolean isParsClosurFuncs() {
        return PoCoUtils.clousrFunc(flagStack4Funcs) || PoCoUtils.closurFunwUop(flagStack4Funcs);
    }
}