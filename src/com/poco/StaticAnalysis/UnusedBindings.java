package com.poco.StaticAnalysis;

import com.poco.PoCoCompiler.PoCoUtils;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;

import java.util.HashSet;

/**
 * Almost all of the modern compilers are capable of locating the declared but unused variables
 * within programs, and so is PoCo compiler.
 * While compiling each policy, PoCo compiler's will first collect names all the declared variables'.
 * Then by scanning the policy body, compiler will be able to spot the unused ones.
 * <p/>
 * This warning is implemented in PoCo by simply constructing a list of all declared variables
 * and then removing them from the list as their usage is encountered during compilation.
 */

public class UnusedBindings extends PoCoParserBaseVisitor<Void> {
    //used to check whether the variable will be used or not
    HashSet<String> unusedVars;
    //used to check whether the variable will be initialed or not
    HashSet<String> unInitialedVars;
    HashSet<String> useB4Initialed;
    HashSet<String> unusedFuns;
    private String policyName = null;

    public UnusedBindings() {
        unusedVars = new HashSet<String>();
        unInitialedVars = new HashSet<String>();
        unusedFuns = new HashSet<String>();
        useB4Initialed = new HashSet<String>();
    }

    @Override
    public Void visitPocopol(PoCoParser.PocopolContext ctx) {
        policyName = ctx.id().getText();
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitParamlist(PoCoParser.ParamlistContext ctx) {
        if (ctx.getText().trim().length() > 0) {
            unusedVars.add(policyName + "(" + ctx.id().getText().trim() + ")");
        }
        return null;
    }

    @Override
    public Void visitVardecl(PoCoParser.VardeclContext ctx) {
        unusedVars.add(policyName + "(" + ctx.id().getText().trim() + ")");
        unInitialedVars.add(policyName + "(" + ctx.id().getText().trim() + ")");
        return null;
    }

    @Override
    public Void visitMacrodecl(PoCoParser.MacrodeclContext ctx) {
        unusedFuns.add(policyName + "(" + ctx.id().getText().trim() + ")");
        return null;
    }

    @Override
    public Void visitSre(PoCoParser.SreContext ctx) {
        if (ctx.DOLLAR() != null && ctx.qid() != null) {
            String varName = policyName + "(" + ctx.qid().getText().trim() + ")";
            if (unusedVars.contains(varName)) {
                //if the variable is used before it has been assigned a value
                if (unInitialedVars.contains(varName))
                    useB4Initialed.add(varName);
                unusedVars.remove(varName);
            } else if (unusedFuns.contains(varName))
                unusedFuns.remove(varName);
        }
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitRe(PoCoParser.ReContext ctx) {
        if (ctx.AT() != null) {
            if (unInitialedVars.contains(policyName + "(" + ctx.id().getText().trim() + ")"))
                unInitialedVars.remove(policyName + "(" + ctx.id().getText().trim() + ")");
        } else {
            if (ctx.DOLLAR() != null) {
                String varName = policyName + "(" + PoCoUtils.getVariableName("$" + ctx.qid().getText().trim()) + ")";
                if (unusedVars.contains(varName)) {
                    //if the variable is used before it has been assigned a value
                    if (unInitialedVars.contains(varName))
                        useB4Initialed.add(varName);
                    unusedVars.remove(varName);
                } else if (unusedFuns.contains(varName))
                    unusedFuns.remove(varName);
            }
        }
        visitChildren(ctx);
        return null;
    }

    public boolean hasUnInitialedVars() {
        //remove the unused var from uninitialized list
        if (unInitialedVars != null && unInitialedVars.size() > 0) {
            if (unusedVars != null && unusedVars.size() > 0)
                unInitialedVars.removeAll(unusedVars);
        }

        if ((unInitialedVars != null && unInitialedVars.size() > 0) ||
                (useB4Initialed != null && useB4Initialed.size() > 0))
            return true;

        return false;
    }

    public HashSet<String> getUseB4Initialed() {
        HashSet<String> unused = new HashSet<String>();
        if (useB4Initialed != null && useB4Initialed.size() > 0)
            unused.addAll(useB4Initialed);
        if (unused.size() > 0)
            return unused;

        return null;
    }

    public HashSet<String> getUnInitialedVars() {
        if (hasUnInitialedVars()) {
            if (unInitialedVars!= null && unInitialedVars.size() > 0)
                return unInitialedVars;
        }
        return null;
    }

    public boolean hasUnusedVars() {
        if ((unusedVars != null && unusedVars.size() > 0) || (unusedFuns != null && unusedFuns.size() > 0))
            return true;
        return false;
    }

    public HashSet<String> getUnusedVars() {
        HashSet<String> unused = new HashSet<String>();

        if (unusedVars != null && unusedVars.size() > 0)
            unused.addAll(unusedVars);
        if (unusedFuns != null && unusedFuns.size() > 0)
            unused.addAll(unusedFuns);

        if (unused.size() > 0)
            return unused;

        return null;
    }
}