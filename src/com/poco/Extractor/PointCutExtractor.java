package com.poco.Extractor;

import com.poco.PoCoCompiler.PoCoUtils;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caoyan on 11/7/14.
 */
public class PointCutExtractor extends PoCoParserBaseVisitor<Void> {
    private HashMap<String, HashMap<String, String>> frmActPT2BndVar = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, HashMap<String, String>> frmResPT2BndVar = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, HashMap<String, String>> frmPrmPT2BndVar = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, HashMap<String, String>> frmPrmResPT2BndVar = new HashMap<String, HashMap<String, String>>();

    private Closure closure;
    private String policyName = "";

    //used to save the policy hierarchy information
    private HashMap<String, PolicyTreeNode> policy2Props = new HashMap<String, PolicyTreeNode>();

    private Stack<String> currRootName;

    private Stack<Integer> parsFlags;
    private Stack<Integer> actResFlags;
    private Stack<String> bindVarName;

    //use the 2nd String field to flag action bindings verse result bindings
    //sig    bindings bind method signature to the variable, while
    //action bindings happen before allowing proceeding, and
    //result bindings happen after  allowing proceeding
    private HashMap<String, String> varBind4thisPC = new HashMap<String, String>();

    public HashMap<String, PolicyTreeNode> getPolicy2Props() {
        return policy2Props;
    }

    private StringBuilder argStr;
    private StringBuilder pointcutStr;

    public PointCutExtractor(Closure closure) {
        this.closure = closure;
        this.pointcutStr = new StringBuilder();
        this.argStr = new StringBuilder();
        this.parsFlags = new Stack<>();
        this.actResFlags = new Stack<>();
        this.bindVarName = new Stack<>();
        this.currRootName = new Stack<>();
    }

    public void resetPTStr() {
        this.pointcutStr = new StringBuilder();
    }

    private static String scrubString(String input) {
        return input.replaceAll("(%|\\$[a-zA-Z0-9\\.\\-_]+)", "");
    }

    @Override
    public Void visitPimport(PoCoParser.PimportContext ctx) {
        //need to import those included policies' pointcut
        return null;
    }

    @Override
    public Void visitTreedef(PoCoParser.TreedefContext ctx) {
        String treeid = ctx.id(0).getText().trim();
        currRootName.push(treeid);
        if (ctx.srebop() != null) {
            String strategy = ctx.srebop().getText().trim();
            if (policy2Props.containsKey(treeid)) {
                PolicyTreeNode node = policy2Props.get(treeid);
                node.setStrategy(strategy);
                policy2Props.put(treeid, node);
            } else {
                policy2Props.put(treeid, new PolicyTreeNode(strategy));
            }
        } else if (ctx.id(1) != null) { //id case
            policy2Props.put(treeid, new PolicyTreeNode());
        } else {
            policy2Props.put(treeid, new PolicyTreeNode());
        }

        visitChildren(ctx);
        currRootName.pop();
        return null;
    }

    public Void visitPolicyarg(@NotNull PoCoParser.PolicyargContext ctx) {
        String childId = ctx.id().getText();
        if (!currRootName.isEmpty()) {
            String ancestor = currRootName.peek();

            if (policy2Props.containsKey(ancestor)) {
                PolicyTreeNode treeNode = policy2Props.get(ancestor);
                treeNode.addChildren(childId);
                policy2Props.put(ancestor, treeNode);

                if (policy2Props.containsKey(childId))
                    treeNode = policy2Props.get(childId);
                else
                    treeNode = new PolicyTreeNode();
                treeNode.setAncestor(ancestor);
                policy2Props.put(childId, treeNode);
            }
        }
        return null;
    }

    @Override
    public Void visitPocopol(@NotNull PoCoParser.PocopolContext ctx) {
        policyName = ctx.id().getText().trim();
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitMacrodecls(@NotNull PoCoParser.MacrodeclsContext ctx) {
        //skip all macrodels
        return null;
    }

    @Override
    public Void visitVardecls(@NotNull PoCoParser.VardeclsContext ctx) {
        //skip all macrodels
        return null;
    }

    @Override
    public Void visitExecution(@NotNull PoCoParser.ExecutionContext ctx) {
        if (ctx.map() != null)
            visitExecution(ctx.map().execution());
        else
            visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitExch(@NotNull PoCoParser.ExchContext ctx) {
        if (ctx.INPUTWILD() != null) {
            //just need monitor the action on the RHS of =>, but treat as LHS
            actResFlags.push(ParsFlgConsts.isAction);
            visitSre(ctx.sre());
            actResFlags.pop();
        } else if (ctx.matchs() != null) {
            visitMatchs(ctx.matchs());
            if (ctx.sre().NEUTRAL() == null)
                visitSre(ctx.sre());
        }
        return null;
    }

    @Override
    public Void visitMatch(@NotNull PoCoParser.MatchContext ctx) {
        /* match need to tracked for pointcut signature */
        if (ctx.SUBSET() != null) {
            visitSre(ctx.sre(0));
            visitSre(ctx.sre(1));
        } else if (ctx.INFINITE() != null) {
            visitSre(ctx.sre(0));
        } else if (ctx.SREEQUALS() != null) {
            visitSre(ctx.sre(0));
            visitSre(ctx.sre(1));
        } else
            visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitIre(@NotNull PoCoParser.IreContext ctx) {
        if (ctx.ACTION() != null) {
            actResFlags.push(ParsFlgConsts.isAction);
        } else
            actResFlags.push(ParsFlgConsts.isResult);

        visitRe(ctx.re(0));
        actResFlags.pop();
        return null;
    }

    public Void visitSre(@NotNull PoCoParser.SreContext ctx) {
        if (ctx.NEUTRAL() != null) {
            resetPTStr();
            return null;
        }

        if (ctx.qid() != null) {
            //$policy() case, should not generate the pointcut
            if(ctx.LPAREN()!= null && ctx.RPAREN()!= null)
                ;
            else
                pointcutStr.append("* " + ctx.qid().getText() + "(..)");
        } else if (ctx.srebop() != null) {
            visitChildren(ctx.sre(0));
            visitChildren(ctx.sre(1));
        } else if (ctx.sreuop() != null) {
            visitChildren(ctx.sre(0));
        } else
            visitChildren(ctx);

        return null;
    }

    @Override
    public Void visitRe(@NotNull PoCoParser.ReContext ctx) {
        if (PoCoUtils.notParsingArgs(parsFlags)) {
            if (ctx.qid() != null) {
                handleQidCase(ctx);
                if (pointcutStr.toString().trim().length() > 0) {
                    addBindVar2PC();
                    add2PCHashmaps();
                }
            } else if (ctx.function() != null) {
                handleFuncCase(ctx);
                addBindVar2PC();
                add2PCHashmaps();
            } else if (ctx.AT() != null) {
                bindVarName.push(policyName +"_"+ ctx.id().getText());
                visitChildren(ctx);
                bindVarName.pop();
            } else {
                visitChildren(ctx);
            }
        } else { // handle the case of parsing the parameters
            if (ctx.rewild() != null) {
                argStr.append("..,");
            } else if (ctx.qid() != null) {
                handleObj4ArgCase(ctx.qid().getText());
            } else if (ctx.AT() != null) {
                bindVarName.push(policyName +"_"+ ctx.id().getText());
                //record the binding info then parse its children
                if (closure != null && closure.isVarsContain(policyName+"_" + ctx.id().getText())) {
                    //if it is an action pointcut, the binding happens before allowing proceeding
                    //otherwise, the binding happens after proceeding (result)
                    if (isActionPointCut()) {
                        if (ctx.re(0).getText().equals("%"))
                            varBind4thisPC.put(policyName +"_"+ ctx.id().getText(), "action%");
                        else
                            varBind4thisPC.put(policyName+"_" + ctx.id().getText(), "action");
                    } else {
                        varBind4thisPC.put(policyName +"_"+ ctx.id().getText(), "result");
                    }
                    visitChildren(ctx);
                } else
                    PoCoUtils.throwNoSuchVarExpection(ctx.id().getText());
                bindVarName.pop();
            } else if (ctx.object() != null) {
                handleArgasObjCase(ctx);
            } else {
                if (ctx.getText().trim().length() > 0 && !ctx.getText().trim().equals("()")) {
                    String temp = PoCoUtils.attachPolicyName(policyName, ctx.getText().trim());
                    if (temp.endsWith("()"))
                        temp = temp.substring(0, temp.length() - 2);
                    argStr.append(temp + ",");
                }
            }
        }
        return null;
    }

    private void addBindVar2PC() {
        if (bindVarName.size() > 0) {
            //variable binding case needs to store binding info 1st, then visit children
            if (isActionPointCut()) {
                varBind4thisPC.put(bindVarName.peek(), "sig");
            } else {
                varBind4thisPC.put(bindVarName.peek(), "result");
            }
        }
    }

    private void handleArgasObjCase(@NotNull PoCoParser.ReContext ctx) {
        if (ctx.object().POUND() != null)
            argStr.append(ctx.object().getText());
        else if (ctx.DOLLAR() != null)
            argStr.append(ctx.getText());
        else   //Null case
            argStr.append("(..)");
    }

    private void handleObj4ArgCase(String str) {
        // 1. check the var list and var type and value the info if found, otherwise
        // 1. check the pre-defined functions and load the info if found, otherwise
        // 3. raise the NullPointerException
        if (closure != null && closure.isVarsContain(policyName+"_" + str)) {
            String varTyp = closure.loadFrmVars(policyName +"_"+ str).getVarType();
            argStr.append("#" + varTyp + "{$" + policyName +"_"+ str + "},");
        } else if (closure != null && closure.isFunctionsContain(policyName +"_"+ str)) {
            argStr.append(closure.getFunctionContext(policyName +"_" + str) + ",");
        } else {
            PoCoUtils.throwNoSuchVarExpection(str);
        }

    }

    private void handleFuncCase(@NotNull PoCoParser.ReContext ctx) {
        //1. first parse the method name info,
        //  1.1 constructor case needs to attach "new" to the method name
        //  1.2 otherwise, get the method name
        if (ctx.function().INIT() != null)
            pointcutStr.append(ctx.function().fxnname().getText().trim() + "new");
        else
            pointcutStr.append(ctx.function().fxnname().getText().trim());

        // 2. parsing the function parameters
        handlePara4FunCase(ctx);
    }

    private void handlePara4FunCase(@NotNull PoCoParser.ReContext ctx) {
        //if arglist is null then attach "()", otherwise get the arg info
        if (ctx.function().arglist() == null || ctx.function().arglist().getText().trim().length() == 0)
            pointcutStr.append("()");
        else {
            flag4ArgParsing();
            visitChildren(ctx);
            pointcutStr.append("(" + PoCoUtils.trimEndPunc(argStr.toString(), ",") + ")");
            parsFlags.pop();
        }
    }

    private void handleQidCase(@NotNull PoCoParser.ReContext ctx) {
        // 1. check to see if it is and method call from an object, if so
        //    1.1 attach the policy name to the variable name
        //    1.2 store the object into objParams HashMap
        // 2. if not the case above, then need load function info from closure
        if (isMthdCallFrmObj(ctx.qid().getText())) {
            up8ObjVarName(PoCoUtils.objMethodCall(ctx.qid().getText()));
        } else {
            if (closure.isFunctionsContain(policyName +"_"+ ctx.qid().getText())) {
                String funcStr = getFunInfoFrmClosure(policyName+"_" + ctx.qid().getText());
                funcStr = funcStr.replace("%", "*");
                if (funcStr != null) {
                    funcStr = PoCoUtils.formatFuncRetTyp(funcStr);
                    pointcutStr.append(funcStr);
                }
            }
        }
        // 3. parsing the function parameters
        if (ctx.opparamlist() != null)
            handlePara4Qid(ctx);
    }

    private String getFunInfoFrmClosure(String qidStr) {
        // if it is the function pre-defined, need get the func info from closure
        // 1. check the pre-defined functions and load the info if found, otherwise
        // 2. check the var list and load the info if found, otherwise
        // 3. raise the NullPointerException
        if (closure != null && closure.isFunctionsContain(qidStr)) {
            return closure.getFunctionContext(qidStr);
        } else if (closure != null && closure.isVarsContain(qidStr)) {
            return closure.getVarContext(qidStr);
        } else
            throw new NullPointerException("No such var named " + qidStr + " exist.");
    }

    private void handlePara4Qid(@NotNull PoCoParser.ReContext ctx) {
        //1. first flag for parsing parameters and reset ArgStr
        flag4ArgParsing();
        //2. parse the parameters
        visitRe(ctx.opparamlist().re());
        //3. append parameter info to the function name
        if (pointcutStr.toString().indexOf("(") == -1)
            pointcutStr.append("(" + PoCoUtils.trimEndPunc(argStr.toString(), ",") + ")");
        //4. pop the flag
        parsFlags.pop();
    }

    private void up8ObjVarName(String[] objInfos) {
        //  if closure is null or the var is not declared raise exception, otherwise
        //      1. attach the policy name to the variable name
        //      2. store the object into objParams HashMap
        if (closure != null && closure.isVarsContain(policyName+"_" + objInfos[0])) {
            pointcutStr.append("$" + policyName +"_"+ objInfos[0] + "." + objInfos[1]);
        } else
            PoCoUtils.throwNoSuchVarExpection(objInfos[0]);
    }

    /**
     * //check to see if it is a method call from an object
     *
     * @param str
     * @return return true if it is a method call from an object
     */
    private boolean isMthdCallFrmObj(String str) {
        return PoCoUtils.objMethodCall(str) != null;
    }

    public HashMap<String, HashMap<String, String>> getPCStrings() {
        return frmActPT2BndVar;
    }

    public HashMap<String, HashMap<String, String>> getPCStrs4Result() {
        return frmResPT2BndVar;
    }

    public HashMap<String, HashMap<String, String>> getPCStrs4Promoter() {
        return frmPrmResPT2BndVar;
    }

    private void add2PCHashmaps() {
        String ptStr = this.pointcutStr.toString().trim();
        ptStr = "<"+policyName+">"+handleTransCase(ptStr,this.policyName);
        //handle the case if there is "|" case, such as
        String[] funStrs = PoCoUtils.splitSreStr(ptStr);
        //reset global variable pointcutStr
        resetPTStr();
        for (String str : funStrs) {

            //1. if it is not the abstract action, then make sure the str is in right format,
            // that is, include return type
            String funName = getFunName(str);
            if(!funName.startsWith("abs_"))
                str = PoCoUtils.formatFuncRetTyp(str);

            //2. add to the appropriate pointcut set
            if (!PoCoUtils.isResultFlag(actResFlags)) {
                //Add this pointcut to action set if it is an LHS action pointcut
                if (PoCoUtils.isActionFlag(actResFlags)) {
                    addUp8ActionSet(str);
                } else {
                    //pointcut is not an IRE result and it is on the RHS of the exchange, then
                    // 1. if there exists a same pointcut in action set, update the action set
                    // is necessary due to the variable binding information
                    // 2. otherwise, the pointcut will be added to promoted action set
                    if (frmActPT2BndVar.containsKey(str))
                        addUp8ActionSet(str);
                    else
                        addUp8PromtnSet(str);
                }
            } else {//the pointcut is result case
                //1. if it is in promotion set then
                //   a. remove it from the promotion set
                //   b. add it to the promotion result set
                //2. if it is in promotion result set, then update the promotion result set,otherwise
                //3. if not all the case above then add to the result set
                if (frmPrmPT2BndVar.containsKey(str)) {
                    removeFrmPromSet(str);
                    addUp8PrmResSet(str);
                } else if (frmPrmResPT2BndVar.containsKey(str))
                    addUp8PrmResSet(str);
                else
                    addUp8ResultSet(str);
            }
        }
        resetVarBind4PC();
    }

    private void removeFrmActSet(String ptStr) {
        removeFrmSet(ptStr, frmActPT2BndVar);
    }

    private void removeFrmPromSet(String ptStr) {
        removeFrmSet(ptStr, frmPrmPT2BndVar);
    }

    private void removeFrmSet(String ptStr, HashMap<String, HashMap<String, String>> target) {
        varBind4thisPC.putAll(target.get(ptStr));
        target.remove(ptStr);
    }

    private void addUp8ActionSet(String ptStr) {
        addUp8Set(ptStr, frmActPT2BndVar);
    }

    private void addUp8ResultSet(String ptStr) {
        addUp8Set(ptStr, frmResPT2BndVar);
    }

    private void addUp8PromtnSet(String ptStr) {
        addUp8Set(ptStr, frmPrmPT2BndVar);
    }

    private void addUp8PrmResSet(String ptStr) {
        addUp8Set(ptStr, frmPrmResPT2BndVar);
    }

    private void addUp8Set(String ptStr, HashMap<String, HashMap<String, String>> target) {
        if (target.containsKey(ptStr))
            varBind4thisPC.putAll(target.get(ptStr));
        target.put(ptStr, varBind4thisPC);
    }

    private void flag4ArgParsing() {
        parsFlags.push(ParsFlgConsts.parsArgs);
        //resetArgStr
        this.argStr = new StringBuilder();
    }

    private boolean isActionPointCut() {
        return (PoCoUtils.isActionFlag(actResFlags));
    }

    private void resetVarBind4PC() {
        varBind4thisPC = new HashMap<String, String>();
    }


    public String getRoot() {
        Set<String> keys = this.policy2Props.keySet();
        for (String key : keys) {
            if (policy2Props.get(key).getAncestor() == null)
                return key;
        }
        return null;
    }

    private static String handleTransCase(String content, String policyName) {
        assert content!= null;
        //if it is abstract action case, then just return
        if(content.trim().length()>4 && PoCoUtils.getMtdName(content).startsWith("abs_"))
            return content;

        Pattern pattern = Pattern.compile("^(.+)\\((.*)\\)$");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String methodName = matcher.group(1).trim();
            if (methodName.length() > 0) {
                String[] temp = methodName.split("\\s+");
                methodName = temp[temp.length-1];
            }
            if (!methodName.startsWith("$") && !methodName.contains(".")) {
                content = content.replace(matcher.group(1), "com.poco." + policyName + "Trans." + methodName);
            }
        }

        return content;
    }

    private String getFunName(String funStr) {
        if(funStr == null)
            return null;
        String funInfos[] = funStr.split("\\s+");
        return funInfos[funInfos.length-1];
    }

}