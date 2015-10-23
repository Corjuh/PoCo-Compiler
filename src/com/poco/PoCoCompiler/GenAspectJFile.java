package com.poco.PoCoCompiler;

import com.poco.Extractor.Closure;
import com.poco.Extractor.PointCutExtractor;
import com.poco.Extractor.VarTypeVal;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

/**
 * Created by yan on 7/6/15.
 */
public class GenAspectJFile extends PoCoParserBaseVisitor<Void> {
    private final int indentLevel;
    private final PrintWriter out;
    private String aspectName;
    private String pocoRoot;
    private ArrayList<String> policies;

    private Stack<String> currentParentRoot;

    private Stack<String> currentPolicyName;
    private StringBuilder policyArgs;

    //sb4Binding is used for the case of name binding with @
    private StringBuilder sb4Binding;
    private Stack<String> currentBinding;

    private HashSet<String> defindedPolicies;

    private Closure closure;

    public GenAspectJFile(PrintWriter out, int indentLevel, Closure closure, PointCutExtractor pcExactor) {
        this.out = out;
        this.indentLevel = indentLevel;
        this.pocoRoot = pcExactor.getRoot() + "root";
        if (pocoRoot == null || pocoRoot.length() == 0)
            this.aspectName = "AspectRoot";
        else
            this.aspectName = "Aspect" + pocoRoot;

        policies = pcExactor.getPolicyNames();

        this.closure = closure;
        currentParentRoot = new Stack<>();
        currentPolicyName = new Stack<>();
        currentBinding = new Stack<>();
        defindedPolicies = new HashSet<>();

        //step 1: gen aspectj prologue.
        outAspectPrologue();

        //step 2: gen DataHW for storing dynamic binding variables.
        genDataHW();

        //step 3: visitpolicy to get policy hierarchy info

    }

    private void outAspectPrologue() {
        outLine(0, "import com.poco.PoCoRuntime.*;");
        outLine(0, "import java.lang.reflect.Method;\n");
        outLine(0, "import java.lang.reflect.Constructor;\n");
        outLine(0, "public aspect %s {", aspectName);

        outLine(1, "private RootPolicy %s = new RootPolicy();\n", this.pocoRoot);
        defindedPolicies.add(this.pocoRoot);
    }

    private void genDataHW() {
        outLine(1, "public " + aspectName + "() {");
        for (Object varname : closure.getVars().keySet()) {
            VarTypeVal temp = (VarTypeVal) closure.getVars().get(varname);
            String typ = temp.getVarType();
            if (typ == null || typ.trim().length() == 0 || typ.trim().equals("null"))
                typ = "java.lang.String";
            outLine(2, "DataWH.dataVal.put(\"" + varname + "\"," + "new TypeVal(\"" + typ + "\",\"\"));");
        }
        //if there is no root policy, we will just declear a root and add all the polices to it.
        if (pocoRoot == null || pocoRoot.length() == 0) {
            for (String policy : policies)
                outLine(1, "root.addChild(new %s());", policy);
        }
    }

    /**
     * Outputs one line of Java/AspectJ code to the out object (always ends in newline).
     *
     * @param indent indent level of current line (relative to the existing indent level)
     * @param text   code to write out (printf style formatting used)
     * @param args   printf-style arguments
     */
    private void outLine(int indent, String text, Object... args) {
        outPartial(indent, text, args);
        outPartial(-1, "\n");
    }

    /**
     * Outputs Java/AspectJ code without appending newline. Use a negative indent value to
     * disable indents.
     *
     * @param indent indent level (relative to existing indent level), or negative to disable indents
     * @param text   code to write out (printf style formatting used)
     * @param args   printf-style arguments
     */
    private void outPartial(int indent, String text, Object... args) {
        if (indent >= 0) {
            int trueIndent = (indent + indentLevel) * 4;
            for (int i = 0; i < trueIndent; i++)
                out.format(" ");
        }
        out.format(text, args);
    }

    @Override
    public Void visitTreedef(@NotNull PoCoParser.TreedefContext ctx) {
        String policyId = ctx.id(0).getText();
        if ((policyId + "root").equals(pocoRoot))
            currentParentRoot.push(pocoRoot);
        else
            currentParentRoot.push(policyId);

        if (ctx.srebop() != null) {
            if (!policies.contains(currentParentRoot.peek())) {
                if (!defindedPolicies.contains(currentParentRoot.peek())) {
                    defindedPolicies.add(currentParentRoot.peek());
                    outLine(2, "NodePolicy %s = new NodePolicy();", currentParentRoot.peek());
                }
                //TREE id = srebop(policyargs) case
                outLine(2, "%s.setStrategy(\"%s\");", currentParentRoot.peek(), ctx.srebop().getText());
            } else {
                if (!defindedPolicies.contains(currentParentRoot.peek())) {
                    defindedPolicies.add(currentParentRoot.peek());
                    outLine(2, "Policy %s = new %s;", currentParentRoot.peek(), currentParentRoot.peek() + "(" + ")");
                }
            }
            visitChildren(ctx);
        } else if (ctx.id(1) != null) {
            //TREE id = id(policyargs) case

            currentPolicyName.push(ctx.id(1).getText().trim());

            policyArgs = new StringBuilder();
            visitChildren(ctx);

            String argStr = PoCoUtils.trimEndPunc(policyArgs.toString(), ",");

            if (pocoRoot.equals(currentParentRoot.peek())) {
                outLine(2, "%s.addChild( new %s );", pocoRoot, currentPolicyName.peek() + "(" + argStr + ")");
            } else {
                //NodePolicy case
                if (!policies.contains(currentParentRoot.peek())) {
                    if (!defindedPolicies.contains(currentParentRoot.peek())) {
                        defindedPolicies.add(currentParentRoot.peek());
                        outLine(2, "NodePolicy %s = new NodePolicy();", currentParentRoot.peek());
                    }

                    if (policies.contains(currentPolicyName.peek())) {
                        if (defindedPolicies.contains(currentPolicyName.peek()))
                            outLine(2, "%s.add(\"%s\");", currentParentRoot.peek(), currentPolicyName.peek());
                        else {
                            String[] args = argStr.split(",");
                            StringBuilder sb = new  StringBuilder();
                            for (int i = 0; i< args.length; i++) {
                                if (!defindedPolicies.contains(args[i])) {
                                    defindedPolicies.add(args[i]);
                                    sb.append("new " + args[i]+"()");
                                }else
                                    sb.append(args[i]);
                                if(i != args.length-1)
                                    sb.append(",");
                            }
                            outLine(2, "%s.addChild(new %s(%s));", currentParentRoot.peek(), currentPolicyName.peek(), sb.toString());
                        }
                    } else {
                        outLine(2, "%s.setStrategy(\"%s\");", currentParentRoot.peek(), currentPolicyName.peek());
                        String[] args = argStr.split(",");
                        for (String str : args) {
                            if (defindedPolicies.contains(str))
                                outLine(2, "%s.addChild(%s);", currentParentRoot.peek(), str);
                            else
                                outLine(2, "%s.addChild(new %s());", currentParentRoot.peek(), str);
                        }
                    }
                    //outLine(2, "Policy %s = new %s ;", currentParentRoot.peek(), currentPolicyName.peek() + "(" + argStr + ")");
                } else
                    outLine(2, "Policy %s = new %s ;", currentParentRoot.peek(), currentPolicyName.peek() + "(" + argStr + ")");
                defindedPolicies.add(currentParentRoot.peek());
            }

            currentPolicyName.pop();
        }
        currentParentRoot.pop();
        return null;
    }

    @Override
    public Void visitPolicyarg(@NotNull PoCoParser.PolicyargContext ctx) {
        //if AT()!= null, then need bind the policyArg, just need treat
        //this id as the policyName that we need to be bind.
        // e.g., @p2subtree[Policy2(Policy4())]
        //      push p2subtree onto currentPolicyName stack
        //also we know that with @, this id will be an alias of a policy DAG,
        if (ctx.AT() != null) {
            currentBinding.push(ctx.id().getText().trim());
            sb4Binding = new StringBuilder();
            visitChildren(ctx);
            outLine(2, "Policy %s = %s;", ctx.id().getText().trim(), PoCoUtils.trimEndPunc(sb4Binding.toString(), ","));
            defindedPolicies.add(ctx.id().getText().trim());
            policyArgs.append(ctx.id().getText().trim() + ",");
            currentBinding.pop();
        } else {
            currentPolicyName.push(ctx.id().getText().trim());
            if (ctx.policyargs().getText().trim().length() > 0) {
                visitChildren(ctx);
            }
            currentPolicyName.pop();

            if (!currentBinding.isEmpty()) {
                String argStr = sb4Binding.toString();
                sb4Binding = new StringBuilder();
                if (argStr.length() > 0) {
                    argStr = PoCoUtils.trimEndPunc(argStr, ",");
                    sb4Binding.append("new " + ctx.id().getText().trim() + "(" + argStr + "),");
                } else {
                    sb4Binding.append("new " + ctx.id().getText().trim() + "(),");
                }
            } else if (!currentPolicyName.isEmpty()) {
                String argStr = policyArgs.toString();
                policyArgs = new StringBuilder();
                if (argStr.length() > 0) {
                    argStr = PoCoUtils.trimEndPunc(argStr, ",");
                    //the case the argStr is the argument of the ctx.id()
                    if (ctx.policyargs().getText().trim().length() > 0) {
                        policyArgs.append(ctx.id().getText().trim() + "(" + argStr + "),");
                    } else {
                        policyArgs.append(argStr + "," + ctx.id().getText().trim() + ",");
                    }
                } else {
                    if (defindedPolicies.contains(ctx.id().getText().trim())) {
                        policyArgs.append(ctx.id().getText().trim() + ",");
                    } else {
                        policyArgs.append(ctx.id().getText().trim() + ",");
                    }
                }
            } else if (!currentParentRoot.isEmpty()) {
                if (defindedPolicies.contains(ctx.id().getText().trim())) {
                    outLine(2, "%s.addChild(%s);", currentParentRoot.peek(), ctx.id().getText().trim());
                } else {
                    defindedPolicies.add(ctx.getText().trim());
                    outLine(2, "%s.addChild(new %s);", currentParentRoot.peek(), ctx.getText().trim());
                }
            }
        }

        return null;
    }

}
