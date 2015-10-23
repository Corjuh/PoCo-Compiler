package com.poco.StaticAnalysis;

import com.poco.Extractor.Closure;
import com.poco.PoCoCompiler.PoCoUtils;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class extracts all the security-relevant methods to a list while scanning the policies.
 * Then, by comparing this list with the list of the target application's all deployed methods,
 * we will be able warning the policy designer with those method.
 */


public class NoMatchingActions extends PoCoParserBaseVisitor<Void> {
    private Closure closure;
    private HashSet<String> methodSigs;
    private String policyName = null;

    public NoMatchingActions(Closure closure) {
        this.closure = closure;
        methodSigs = new HashSet<>();
    }

    @Override
    public Void visitPocopol(PoCoParser.PocopolContext ctx) {
        policyName = ctx.id().getText();
        visitChildren(ctx);
        return null;
    }

    //skip vars
    public Void visitVardecl(PoCoParser.VardeclContext ctx) {
        return null;
    }

    @Override
    public Void visitRe(PoCoParser.ReContext ctx) {
        // can skip variable case since it will either
        // be dynamic or has been added thru visitMacrodecl
        if (ctx.function() != null && ctx.function().fxnname() != null) {
            String methodName = ctx.function().fxnname().getText().trim();
            if (PoCoUtils.isVariable(methodName))
                methodName = loadFuncFrmClousre(methodName.substring(1));
                String funName = methodName.split("\\s+")[methodName.split("\\s+").length-1];
            if (methodName != null && funName.contains(".")) {
                String argStr = "";
                if (ctx.function().INIT() != null) {
                    methodName = methodName + "<init>";
                }
                if (ctx.function().arglist() != null) {
                    argStr = ctx.function().arglist().getText().trim();
                    argStr = getArgsig(argStr);
                }
                if (methodName.contains("("))
                    methodName = methodName.substring(0, methodName.indexOf('('));
                methodSigs.add(methodName + "(" + argStr + ")");
            }
        }
        visitChildren(ctx);
        return null;
    }

    public HashSet<String> getAllMethods() {
        if (methodSigs != null && methodSigs.size() > 0)
            return methodSigs;

        return null;
    }

    private String getArgsig(String methodStr) {
        String[] args = methodStr.split(",");
        if (args != null) {
            String argSig = "";
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("%")) {
                    argSig += "%";
                } else if (PoCoUtils.isPoCoObject(args[i])) {
                    argSig += PoCoUtils.getObjType(args[i]);
                } else if (args[i].startsWith("$")) {
                    String temp = getVarTypFrmClousre(PoCoUtils.getVariableName(args[i]));
                    if (temp == null)
                        temp = "java.lang.String";
                    argSig += temp;
                }else
                    argSig += args[i];
                if (i != args.length - 1)
                    argSig += ",";
            }
            return argSig;
        }
        return null;
    }

    private String getMethodInfo(String methodStr, int mode) {
        String reg = "(.+)\\((.*)\\)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(methodStr);
        if (matcher.find()) {
            if (mode == 1)
                return matcher.group(1).trim();
            else if (matcher.group(2).trim().length() > 0)
                return matcher.group(2).trim();
        } else if (mode == 1)
            return methodStr;

        return null;
    }

    private String loadFuncFrmClousre(String varName) {
        if (closure == null)
            return null;

        if (closure.isFunctionsContain(policyName + "_" + varName))
            return closure.getFunctionContext(policyName + "_" + varName);

        return null;
    }

    private String getVarTypFrmClousre(String varName) {
        if (closure == null)
            return null;
        if (closure.isVarsContain(policyName + "_" + varName))
            return closure.getVarType(policyName + "_" + varName);
        else if (closure.isFunctionsContain(policyName + "_" + varName))
            return closure.getMacroType(policyName + "_" + varName);
        return null;
    }

}