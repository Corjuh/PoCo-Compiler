package com.poco.Extractor;

import com.poco.PoCoCompiler.PoCoUtils;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.*;

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

    private Stack<Integer> parsFlags;
    private Stack<String> bindVarName;

    //used to store all the parameters that needed as obj in order to promote or process
    //since not all the variables are needed as obj type
    //better performance for gening DataWH, no need iterate all pointcuts
    private HashMap<String, String> objParams = new HashMap<String, String>();

    //use the 2nd String field to flag action bindings verse result bindings
    //action bindings bind method signature to the variable, while
    //result bindings bind method result    to the variable
    private HashMap<String, String> varBind4thisPC = new HashMap<String, String>();

    private StringBuilder argStr;
    private StringBuilder pointcutStr;

    public PointCutExtractor(Closure closure) {
        this.closure = closure;
        this.pointcutStr = new StringBuilder();
        this.argStr = new StringBuilder();
        this.parsFlags = new Stack<>();
        this.bindVarName = new Stack<>();
    }

    public void resetPTStr() {
        this.pointcutStr = new StringBuilder();
    }

    private static String scrubString(String input) {
        return input.replaceAll("(%|\\$[a-zA-Z0-9\\.\\-_]+)", "");
    }

    @Override
    public Void visitPocopol(@NotNull PoCoParser.PocopolContext ctx) {
        policyName = ctx.id().getText().trim() + "_";
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
            parsFlags.push(ParsFlgConsts.isAction);
            visitSre(ctx.sre());
            parsFlags.pop();
        } else if (ctx.matchs() != null) {
            visitMatchs(ctx.matchs());
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
        if (ctx.ACTION() != null)
            parsFlags.push(ParsFlgConsts.isAction);
        else
            parsFlags.push(ParsFlgConsts.isResult);

        visitRe(ctx.re(0));
        parsFlags.pop();
        return null;
    }

    public Void visitSre(@NotNull PoCoParser.SreContext ctx) {
        if (ctx.NEUTRAL() != null)
            resetPTStr();
        if (ctx.qid() != null) {
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
                bindVarName.push(policyName + ctx.id().getText());
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
                bindVarName.push(policyName + ctx.id().getText());
                //record the binding info then parse its children
                if (closure != null && closure.isVarsContain(policyName + ctx.id().getText())) {
                    varBind4thisPC.put(policyName + ctx.id().getText(), "result");
                    objParams.put(policyName + ctx.id().getText(), "result");
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
                varBind4thisPC.put(bindVarName.peek(), "action");
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
        if (closure != null && closure.isVarsContain(policyName + str)) {
            String varTyp = closure.loadFrmVars(policyName + str).getVarType();
            objParams.put(policyName + str, "result");
            argStr.append("#" + varTyp + "{$" + policyName + str + "},");
        } else if (closure != null && closure.isFunctionsContain(policyName + str)) {
            argStr.append(closure.getFunctionContext(policyName + str) + ",");
        } else {
            PoCoUtils.throwNoSuchVarExpection(str);
        }

    }

    private void handleFuncCase(@NotNull PoCoParser.ReContext ctx) {
        //1. first parse the method name info,
        //  1.1 constructor case needs to attach "new" to the method name
        //  1.2 otherwise, get the method name
        if (ctx.function().INIT() != null) {
            pointcutStr.append(ctx.function().fxnname().getText() + "new");
        } else {
            String temp = ctx.function().fxnname().getText().trim();
            pointcutStr.append(PoCoUtils.formatFuncRetTyp(temp));
        }
        // 3. parsing the function parameters
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
            String funcStr = getFunInfoFrmClosure(policyName + ctx.qid().getText());
            if (funcStr != null) {
                funcStr = PoCoUtils.formatFuncRetTyp(funcStr);
                pointcutStr.append(funcStr);
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
            objParams.put(qidStr, "action");
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
        if (closure != null && closure.isVarsContain(policyName + objInfos[0])) {
            pointcutStr.append("$" + policyName + objInfos[0] + "." + objInfos[1]);
            objParams.put(policyName + objInfos[0], "result");
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

    public HashMap<String, String> getObjParams() {
        return objParams;
    }

    private void add2PCHashmaps() {
        String ptStr = this.pointcutStr.toString().trim();
        //1. reset global variable pointcutStr
        resetPTStr();

        //2. add to the appropriate pointcut set
        if (!PoCoUtils.isResultFlag(parsFlags)) {
            //Add this pointcut to action set if it is an LHS action pointcut
            if (PoCoUtils.isActionFlag(parsFlags)) {
                addUp8ActionSet(ptStr);
            } else {
                //pointcut is not an IRE result and it is on the RHS of the exchange, then
                // 1. if there exists a same pointcut in action set, update the action set
                // is necessary due to the variable binding information
                // 2. otherwise, the pointcut will be added to promoted action set
                if (frmActPT2BndVar.containsKey(ptStr))
                    addUp8ActionSet(ptStr);
                else
                    addUp8PromtnSet(ptStr);
            }
        } else {//the pointcut is result case
            //1. if it is in promotion set then
            //   a. remove it from the promotion set
            //   b. add it to the promotion result set
            //2. if it is in promotion result set, then update the promotion result set,otherwise
            //3. if not all the case above then add to the result set
            if (frmPrmPT2BndVar.containsKey(ptStr)) {
                removeFrmPromSet(ptStr);
                addUp8PrmResSet(ptStr);
            } else if (frmPrmResPT2BndVar.containsKey(ptStr))
                addUp8PrmResSet(ptStr);
            else
                addUp8ResultSet(ptStr);
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
        return (PoCoUtils.isActionFlag(parsFlags));
    }

    private void resetVarBind4PC() {
        varBind4thisPC = new HashMap<String, String>();
    }

    public String getPolicyName() {
        return policyName.substring(0, policyName.length() - 1);
    }

}