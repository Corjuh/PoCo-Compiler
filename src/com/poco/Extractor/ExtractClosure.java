package com.poco.Extractor;

import com.poco.PoCoCompiler.PoCoUtils;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

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

    public ExtractClosure() {
        closure = new Closure();
        this.bindings = new Stack<>();
        this.parsingFlags = new Stack<>();
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
        policyName = ctx.id().getText().trim() + "_";
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitParamlist(@NotNull PoCoParser.ParamlistContext ctx) {
        if (ctx.getText().trim().length() > 0) {
            visitChildren(ctx);
            String varVal = PoCoUtils.attachPolicyName(policyName, ctx.id().getText());
            VarTypeVal varTyVal = new VarTypeVal(ctx.qid().getText(), varVal);
            closure.addVar(policyName + ctx.id().getText(), varTyVal);
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
        if (ctx.id() != null)
            closure.addVar(policyName + ctx.id().getText(), new VarTypeVal(null, null));
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
            parsingFlags.push(ParsFlgConsts.closurFunwUop);
        else
            parsingFlags.push(ParsFlgConsts.clousrFunc);
        visitChildren(ctx);
        bindings.pop();
        parsingFlags.pop();
        return null;
    }

    public Void visitRe(@NotNull PoCoParser.ReContext ctx) {
        if (ctx.AT() != null) {
            handleVarBinding(ctx);
        } else {
            if (!isSkipable(ctx.getText())) {
                if (PoCoUtils.notParsingArgs(parsingFlags)) {
                    if (ctx.function() != null)
                        handleFunc(ctx);
                    else if (ctx.object() != null)
                        handleObj(ctx);
                    else if (ctx.rebop() != null)
                        handleRebop(ctx);
                    else if (ctx.qid() != null)
                        handleQid(ctx);
                    else
                        visitChildren(ctx);
                } else {
                    if (ctx.object() != null && isVariable(ctx.object().re().getText())) {
                        String newTyp = ctx.object().qid().getText().trim();
                        String varName = ctx.object().re().getText().trim();
                        up8ClosurVarTyp(policyName + varName.substring(1), newTyp);
                    }

                    String str = ctx.getText().trim();
                    if (isVariable(str))
                        str = getValFrmClousre(str);

                    currParsArgs.append(str + ",");
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
        visitChildren(ctx);
        String varName = policyName + bindings.peek();
        if (closure.loadFrmVars(varName) != null) {
            String typStr = closure.loadFrmVars(varName).getVarType();
            closure.updateVar(varName, new VarTypeVal(typStr, currParsVal.toString()));
        }
        bindings.pop();
    }

    private void handleQid(@NotNull PoCoParser.ReContext ctx) {
        String varName = policyName + ctx.qid().getText();
        if (closure != null && closure.loadFrmFunctions(varName) != null) {
            if (closure.loadFrmFunctions(varName).getVarContext() != null)
                currParsVal.append(closure.loadFrmFunctions(varName).getVarContext());
        }
    }

    private void handleRebop(@NotNull PoCoParser.ReContext ctx) {
        visitRe(ctx.re(0));
        if (!ctx.re(1).getText().trim().equals("") && !ctx.re(1).getText().trim().equals("()")) {
            currParsVal.append("|");
            visitRe(ctx.re(1));
        }
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
                if (PoCoUtils.closurFunwUop(parsingFlags))
                    temp = "[^" + temp + "]";
                temp = PoCoUtils.attachPolicyName(policyName, "#" + varTyp + "{" + temp + "}");
                closure.addFunction(policyName + bindings.peek(), new VarTypeVal(varTyp, temp));
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
        String funcStr = ctx.function().getText();
        if (ctx.function().INIT() != null)
            funcStr = PoCoUtils.attachPolicyName(policyName, ctx.function().fxnname().getText() + "new");
        if (ctx.function().arglist() != null) {
            if (funcStr.indexOf('(') != -1)
                funcStr = funcStr.substring(0, funcStr.indexOf('('));
            parsingFlags.push(ParsFlgConsts.parsArgs);
            resetCurrParsArgs();
            visitChildren(ctx.function().arglist());
            funcStr += "(" + PoCoUtils.trimEndPunc(currParsArgs.toString(), ",") + ")";
            parsingFlags.pop();
        }

        if (isParsClosurFuncs()) {
            if (PoCoUtils.closurFunwUop(parsingFlags))
                funcStr = "[^" + funcStr + "]";
            funcStr = PoCoUtils.attachPolicyName(policyName, funcStr);
            closure.addFunction(policyName + bindings.peek(), new VarTypeVal("java.lang.String", funcStr));
        } else {
            currParsVal.append(funcStr);
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

    private String getValFrmClousre(String varName) {
        String temp = PoCoUtils.attachPolicyName(policyName, varName).substring(1);
        if (closure != null && closure.loadFrmFunctions(temp) != null)
            return closure.loadFrmFunctions(temp).getVarContext();
        else if (closure != null && closure.loadFrmVars(temp) != null)
            return closure.loadFrmVars(temp).getVarContext();
        else
            return varName;
    }

    private boolean isSkipable(String ctxStrVal) {
        return (bindings.empty() || ctxStrVal.trim().equals("") || ctxStrVal.trim().equals("()"));
    }

    private boolean containsVariables(String str) {
        return str.contains("$");
    }

    private boolean isParsClosurFuncs() {
        return PoCoUtils.clousrFunc(parsingFlags) || PoCoUtils.closurFunwUop(parsingFlags);
    }
}