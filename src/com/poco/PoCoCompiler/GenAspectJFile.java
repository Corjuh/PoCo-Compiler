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
    private int count = 0;

    private ArrayList<String> policyArgs = null;
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

        // all the specified base policies
        policies = closure.getPolicies();

        this.closure = closure;
        currentParentRoot = new Stack<>();
        currentBinding = new Stack<>();
        defindedPolicies = new HashSet<>();
        policyArgs = new ArrayList<>();

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
        if ((policyId + "root").equals(pocoRoot)) {
            //root case, the root has been declared
            currentParentRoot.push(policyId + "root");
        } else {
            currentParentRoot.push(policyId);
            if (!defindedPolicies.contains(currentParentRoot.peek())) {
                defindedPolicies.add(currentParentRoot.peek());
                outLine(2, "NodePolicy %s = new NodePolicy();", currentParentRoot.peek());
            }
        }

        if (ctx.srebop() != null || ctx.id(1) != null) {
            // Step 1: get the policy combining logic or policyId
            String currentLogic = "";
            if (ctx.srebop() != null)
                currentLogic = ctx.srebop().getText().trim();
            else if (ctx.id(1) != null)
                currentLogic = ctx.id(1).getText().trim();
            if (currentLogic.length() == 0) {
                System.err.println("You have specified invalid policy name or policy combining logic, please check!");
                System.exit(-1);
            }

            //it is the policy case, if is just a single base policy, direct add to the parent,
            //otherwise, will need to parse the argument first, then add on to the parent.
            if (policies.contains(currentLogic) || defindedPolicies.contains(currentLogic)) {
                if (ctx.policyargs() == null || ctx.policyargs().getText().trim().length() == 0) {
                    if (!defindedPolicies.contains(currentLogic)) {
                        defindedPolicies.add(currentParentRoot.peek());
                        outLine(2, "%s.addChild(new %s());", currentParentRoot.peek(), currentLogic);
                    } else
                        outLine(2, "%s.addChild(%s);", currentParentRoot.peek(), currentLogic);
                } else {
                    // step a: first parse the arguments
                    policyArgs.clear();
                    visitPolicyargs(ctx.policyargs());
                    // step b: add to the parent policy
                }
            } else {
                //it is the combining logic case
                outLine(2, "%s.setStrategy(\"%s\");", currentParentRoot.peek(), currentLogic);
                visitPolicyargs(ctx.policyargs());
            }
        }
        currentParentRoot.pop();
        return null;
    }

    @Override
    public Void visitPolicyarg(@NotNull PoCoParser.PolicyargContext ctx) {
//        //if AT()!= null, then need bind the policyArg, just need treat
//        //this id as the policyName that we need to be bind.
//        // e.g., @p2subtree[Policy2(Policy4())]
//        //      push p2subtree onto currentPolicyName stack
//        //also we know that with @, this id will be an alias of a policy DAG,
//        if (ctx.AT() != null) {
//            currentBinding.push(ctx.id().getText().trim());
//            sb4Binding = new StringBuilder();
//            visitChildren(ctx);
//            outLine(2, "Policy %s = %s;", ctx.id().getText().trim(), PoCoUtils.trimEndPunc(sb4Binding.toString(), ","));
//            defindedPolicies.add(ctx.id().getText().trim());
//            currentBinding.pop();
//        }
//
        if (ctx.id() != null) {
            String tempId = ctx.id().getText();
            String nodeName = tempId;
            if (ctx.LPAREN() != null) {
                if (!policies.contains(tempId) && !defindedPolicies.contains(tempId)) {
                    //combing Logic case, create a new PolicyNode
                    nodeName = "nodePolicy" + count++;
                    outLine(2, "NodePolicy %s = new NodePolicy();", nodeName);
                    outLine(2, "%s.setStrategy(\"%s\");", nodeName, tempId);
                    outLine(2, "%s.addChild(%s);", currentParentRoot.peek(),nodeName);
                }
                String tempArg = ctx.policyargs().getText().trim();
                // this is the case where the policy takes no argument, so direct add to its parent
                if (tempArg.length() == 0) {
                    if (policies.contains(tempId) || defindedPolicies.contains(tempId)) {
                        if (policies.contains(currentParentRoot.peek()) || defindedPolicies.contains(currentParentRoot.peek())) {
                            if (defindedPolicies.contains(tempId))
                                outLine(2, "%s.addChild(%s);", currentParentRoot.peek(), tempId);
                            else {
                                defindedPolicies.add(tempId);
                                outLine(2, "%s.addChild(new %s());", currentParentRoot.peek(), tempId);
                            }
                        } else {
                            if (defindedPolicies.contains(tempId))
                                policyArgs.add(tempId);
                            else {
                                defindedPolicies.add(tempId);
                                policyArgs.add("new " + tempId + "()");
                            }
                        }

                    }
                } else {
                    currentParentRoot.push(nodeName);
                    ArrayList<String> temp = new ArrayList<>();
                    if(policyArgs.size()>0)
                        temp.addAll(policyArgs);
                    policyArgs.clear();
                    visitPolicyargs(ctx.policyargs());

                    String newArgStr = "";
                    if (policies.contains(tempId) || defindedPolicies.contains(tempId)) {
                        if (defindedPolicies.contains(tempId))
                            newArgStr = tempId + "(" + genArgStrFrmArgList(policyArgs) + ")";
                        else {
                            defindedPolicies.add(tempId);
                            newArgStr = "new " + tempId + "(" + genArgStrFrmArgList(policyArgs) + ")";
                        }
                    } else {
                        //
                    }
                    policyArgs.clear();
                    if(temp.size()>0)
                        policyArgs.addAll(temp);
                    policyArgs.add(newArgStr);
                    currentParentRoot.pop();

                    if(policyArgs.size()>0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < policyArgs.size(); i++) {
                            sb.append(policyArgs.get(i));
                            if (i != policyArgs.size() - 1)
                                sb.append(",");
                        }
                        if(sb.toString().length()>0)
                            outLine(2, "%s.addChild(%s);", currentParentRoot.peek(), sb.toString());
                    }
                }
            } else {
                //This is the case where the policy argument is a non-policy type
                policyArgs.add(ctx.getText().trim());
            }
        } else {
            //number type case
            policyArgs.add(ctx.getText().trim());
        }

        return null;
    }

    private String genArgStrFrmArgList(ArrayList<String> args) {
        if (args == null || args.size() == 0)
            return "";
        else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.size(); i++) {
                String temp = args.get(i);
                if (policies.contains(temp) || defindedPolicies.contains(args.get(i))) {
                    if (defindedPolicies.contains(temp))
                        sb.append(temp + "()");
                    else {
                        sb.append("new " + temp + "()");
                        defindedPolicies.add(temp);
                    }
                } else
                    sb.append(temp);
                if (i != args.size() - 1)
                    sb.append(",");
            }
            return sb.toString();
        }
    }
}
