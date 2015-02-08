package com.poco.PoCoCompiler;

import com.poco.Extractor.Closure;
import com.poco.Extractor.VarTypeVal;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

/**
 * Generates the Java code to create a PoCoPolicy object representing
 * the parsed PoCo policy.
 */
public class PolicyVisitor extends PoCoParserBaseVisitor<Void> {
    private final int indentLevel;
    private final PrintWriter out;

    private int executionNum;
    private Stack<String> executionNames;
    //use to record the current modifier for this execution,
    //since one execution can only have one exchange as children,
    //we will set the exchange has the same modifier
    //this will only be used to set the modifier attribute for exchange
    private String currentModifier;

    private int exchangeNum;
    private String currentExchange;
    private boolean isReturnValue;

    private int matchsNum;
    private Stack<String> matchsNames;

    private int matchNum;
    private String currentMatch;
    //use this flag for the match when it is the @id[`re'] case, we need know it is a match
    private boolean isMatch = false;

    private Closure closure;

    private int sreNum;
    private Stack<String> sreNames;
    //set flag indicate this is a srebop case, sre0 & sre1 should be added as srebop's children
    private boolean isSreBop1 = false; //child1
    private boolean isSreBop2 = false; //child2

    private boolean matchRHS = false;
    private boolean hasAsterisk = false;
    private boolean hasPlus = false;

    private boolean isSre = false;
    private boolean isSrePos = false;

    private boolean isIre = false;
    private boolean isAction = false;
    //if the match for an exec is combined w/ &&| or, we will not skip the %(at least for now),
    //and late automaton can deal with it such as <Action(`@act[%]') && @out[`$p'] => +`fopen($f)'>
    private boolean isCombinedMatch = false;
    private boolean isResult = false;
    private boolean isResultMatch = false;

    private boolean isMapSre = false;

    //use to save the transactions that need to be added into Util
    private String transactions = null;

    /**
     * Constructor
     *
     * @param out         output stream to write to (a PrintWriter-wrapped Java or AspectJ file)
     * @param indentLevel base indent level for all code generated by PolicyVisitor
     */
    public PolicyVisitor(PrintWriter out, int indentLevel, Closure closure1) {
        this.out = out;
        this.indentLevel = indentLevel;

        // Initialize execution data structures
        this.executionNum = 0;
        this.executionNames = new Stack<>();

        // Initialize exchange data structures
        this.exchangeNum = 0;
        this.currentExchange = null;
        this.isReturnValue = false;

        // Initialize Matchs data structures
        this.matchsNum = 0;
        this.matchsNames = new Stack<>();

        // Initialize Match data structures
        this.matchNum = 0;
        this.currentMatch = null;

        // Initialize SRE data structures
        this.sreNum = 0;
        this.closure = closure1;
        this.sreNames = new Stack<>();
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

            for (int i = 0; i < trueIndent; i++) {
                out.format(" ");
            }
        }
        out.format(text, args);
    }

    /**
     * Generates code for class representing a PoCo policy. This is the first visit method called.
     *
     * @param ctx
     * @return
     */
    @Override
    public Void visitPocopol(@NotNull PoCoParser.PocopolContext ctx) {
        String policyName = ctx.id().getText();

        outLine(0, "class %s extends Policy {", policyName);
        outLine(1, "public %s() {", policyName);
        outLine(2, "try {");
        outLine(3, "SequentialExecution rootExec = new SequentialExecution(\"none\");");
        executionNames.push("rootExec");
        visitChildren(ctx);
        executionNames.pop();
        outLine(3, "rootExec.getCurrentChildModifier();");
        outLine(3, "setRootExecution(rootExec);");
        outLine(2, "} catch (PoCoException pex) {");
        outLine(3, "System.out.println(pex.getMessage());");
        outLine(3, "pex.printStackTrace();");
        outLine(3, "System.exit(-1);");
        outLine(2, "}");
        outLine(1, "}");
        outLine(0, "}");

        //outLine(0, "%s policy1 = new %s();", policyName, policyName);
        //outLine(0, "root.setChild(policy1);");

        return null;
    }

    @Override
    public Void visitParamlist(@NotNull PoCoParser.ParamlistContext ctx) {
        // TODO: Support actual policy parameters (as references to policy objects)
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitExecution(@NotNull PoCoParser.ExecutionContext ctx) {
        // TODO: Support non-sequential executions
        if (ctx.BAR() != null) { // will be the alternation execution(e.g.,  execution_1 |execution)
            String modifier = getModifierFlag();
            String executionName = "alterExec" + executionNum++;
            executionNames.push(executionName);
            outLine(3, "AlternationExecution %s = new AlternationExecution(\"%s\");", executionName, modifier);
            visitExecution(ctx.execution(0));
            visitExecution(ctx.execution(1));
            executionNames.pop();
            outLine(3, "%s.addChild(%s);", executionNames.peek(), executionName);
        } else if (ctx.map() != null) {
            String modifier = getModifierFlag();
            String executionName = "mapExec" + executionNum++;
            executionNames.push(executionName);
            outLine(3, "MapExecution %s = new MapExecution(\"%s\");", executionName, modifier);
            outLine(3, "%s.setOperator(\"%s\");", executionName, ctx.map().srebop().getText());
            isMapSre = true;
            visitSre(ctx.map().sre());
            isMapSre = false;
            visitExecution(ctx.map().execution());
            executionNames.pop();
            outLine(3, "%s.addChild(%s);", executionNames.peek(), executionName);
        } else if (ctx.LPAREN() != null) {// && !isAlternation) {   //for grouped executions but not alternation
            String modifier = getModifierFlag();
            String seqExec = "groupedExec" + executionNum++;
            executionNames.push(seqExec);
            outLine(3, "SequentialExecution %s = new SequentialExecution(\"%s\");", seqExec, modifier);
            visitChildren(ctx);
            executionNames.pop();
            outLine(3, "%s.addChild(%s);", executionNames.peek(), seqExec);
        } else {
            if (ctx.exch() != null) {
                String modifier = getModifierFlag();
                String executionName = "exec" + executionNum++;
                executionNames.push(executionName);
                outLine(3, "SequentialExecution %s = new SequentialExecution(\"%s\");", executionName, modifier);
                currentModifier = modifier;
                visitChildren(ctx);
                executionNames.pop();
                outLine(3, "%s.addChild(%s);", executionNames.peek(), executionName);
            } else {
                setModifierFlag((ctx.ASTERISK() != null), (ctx.PLUS() != null));
                visitChildren(ctx);
            }
        }
        return null;
    }

    @Override
    public Void visitExch(@NotNull PoCoParser.ExchContext ctx) {
        // Create Exchange object
        String exchangeName = "exch" + exchangeNum++;
        // Visit children to flesh out the exchange object
        currentExchange = exchangeName;
        outLine(3, "Exchange %s = new Exchange();", exchangeName);
        currentModifier = "none";
        // The code for the match object is not generated here unless the match portion is a wildcard
        boolean isWildcardMatch = (ctx.INPUTWILD() != null);
        if (isWildcardMatch) {
            //String matchName = "match" + matchNum++;
            //outLine(3, "Match %s = new Match(%s);", matchName, "\"%\"");
            //outLine(3, "%s.addMatcher(%s);", exchangeName, matchName);
            matchRHS = true;
            visitSre(ctx.sre());
            matchRHS = false;
        } else if (ctx.matchs() != null) {
            String matchsName = "matchs" + matchsNum++;
            // Create matchs object
            outLine(3, "Matchs %s = new Matchs();", matchsName);
            // Visit children
            matchsNames.push(matchsName);
            visitMatchs(ctx.matchs());
            outLine(3, "%s.addMatcher(%s);", currentExchange, matchsName);
            matchsNames.pop();
        }
        // Let the child SRE know it's a return value, so that it attaches itself to this exchange
        isReturnValue = true;
        visitSre(ctx.sre());
        isReturnValue = false;
        currentExchange = null;
        // Add Exchange to containing execution
        outLine(3, "%s.addChild(%s);", executionNames.peek(), exchangeName);
        return null;
    }

    @Override
    public Void visitMatchs(@NotNull PoCoParser.MatchsContext ctx) {
        // If the child node is a Match object, this Matchs object is unnecessary
        boolean hasBooluop = (ctx.BOOLUOP() != null);
        boolean hasAnd = false;
        boolean hasOr = false;

        if (ctx.BOOLBOP() != null) {
            if (ctx.BOOLBOP().getText().equals("||"))
                hasOr = true;
            else
                hasAnd = true;
        }
        boolean hasMatch = (ctx.match() != null);
        if (!hasMatch) {
            if (hasBooluop)
                outLine(3, "%s.setNOT(true);", matchsNames.peek());
            if (hasAnd)
                outLine(3, "%s.setAND(true);", matchsNames.peek());
            else if (hasOr)
                outLine(3, "%s.setOR(true);", matchsNames.peek());

            //set up the flag so the % case will not be skipped (e.g., <Action(`[%]')&&@out[`$p']=>+`fopen($f)'>)
            if (hasBooluop || hasAnd || hasOr)
                isCombinedMatch = true;
            visitChildren(ctx);
            //reset the flag after done visit this match
            isCombinedMatch = false;
        } else {
            // This matchs simply wraps a match object. No need to create a matchs object
            visitChildren(ctx);
        }

        return null;
    }

    @Override
    public Void visitMatch(@NotNull PoCoParser.MatchContext ctx) {
        if (ctx.INFINITE() != null) {
            String matchName = "otherMatch" + matchNum++;
            outLine(3, "OtherMatch %s = new OtherMatch(\"Infinite\", null, null);", matchName);
            currentMatch = matchName;
            isMatch = true; //set isMatch for ture, so match as @out[`$p'] case will be knowned as match
            visitChildren(ctx);
            isMatch = false;
            currentMatch = null;

            if (matchsNames.empty()) {
                // Add match object to parent exchange
                outLine(3, "%s.addMatcher(%s);", currentExchange, matchName);
            } else {
                System.out.println("I am here");
                // Add match object to parent matchs
                outLine(3, "%s.addChild(%s);", matchsNames.peek(), matchName);
            }
        } else if (ctx.SUBSET() != null || ctx.SREEQUALS() != null) {

        } else {
            // Create match object
            String matchName = "match" + matchNum++;
            outLine(3, "Match %s = new Match();", matchName);
            // TODO: Handle case when something other than an ire is in a match object
            // Visit children
            currentMatch = matchName;
            isMatch = true; //set isMatch for ture, so match as @out[`$p'] case will be knowned as match
            visitChildren(ctx);
            isMatch = false;
            currentMatch = null;
            // Add to parent
            if (matchsNames.empty()) {
                // Add match object to parent exchange
                outLine(3, "%s.addMatcher(%s);", currentExchange, matchName);
            } else {
                // Add match object to parent matchs
                outLine(3, "%s.addChild(%s);", matchsNames.peek(), matchName);
            }
        }
        return null;
    }

    @Override
    public Void visitIre(@NotNull PoCoParser.IreContext ctx) {
        isIre = true;
        if (ctx.ACTION() != null) {
            isAction = true;
            isResult = false;
            visitRe(ctx.re(0));
        } else {
            isAction = false;  //is result
            isResult = true;
            visitRe(ctx.re(0));
            isResultMatch = true;
            visitRe(ctx.re(1));
            isResultMatch = false;
        }
        isIre = false;
        return null;
    }

    @Override
    public Void visitSre(@NotNull PoCoParser.SreContext ctx) {
        // TODO: Support SREs existing outside of the "return" part of an exchange
        if (matchRHS == true) {
            if (ctx.re() != null) {
                visitRe(ctx.re());
            } else if (ctx.qid() != null) {
                String str = loadFromClosure(ctx.qid().getText());
                if (str == null)
                    str = ctx.qid().getText();
                String matchName = "match" + matchNum++;
                outLine(3, "Match %s = new Match(\"%s\");", matchName, scrubString(str));
                outLine(3, "%s.addMatcher(%s);", currentExchange, matchName);
            }
        } else {
            if (ctx.NEUTRAL() != null) {
                String sreName = "sre" + sreNum++;
                if (isMapSre)
                    outLine(3, "%s.setMatchSre(%s);", executionNames.peek(), sreName);
                else {
                    outLine(3, "SRE %s = new SRE(null, null);", sreName);
                    if (isReturnValue) {
                        outLine(3, "%s.setSRE(%s);", currentExchange, sreName);
                    }
                }
            } else if (ctx.PLUS() != null | ctx.MINUS() != null) { // (+|-)`re' case
                String sreName = "sre" + sreNum++;
                sreNames.push(sreName);
                outLine(3, "SRE %s = new SRE(null, null);", sreName);
                isSre = true;
                isSrePos = (ctx.PLUS() != null);
                visitRe(ctx.re());
                isSre = false;
                sreNames.pop();
                if (!sreNames.empty())
                    setSREvalue(sreNames.peek(), sreName);
            } else if (ctx.srebop() != null) {  // srebop(sre0,sre1) case
                //set flag indicate this is a srebop case, sre0 & sre1 should be added as srebop's children
                String sreName = "bopSRE" + sreNum++;
                outLine(3, "BopSRE %s = new BopSRE(\"%s\",null, null);", sreName, ctx.srebop().getText());
                sreNames.push(sreName);
                isSreBop1 = true;
                isSreBop2 = false;
                visitSre(ctx.sre(0));

                isSreBop1 = false;
                isSreBop2 = true;
                visitSre(ctx.sre(1));
                isSreBop2 = false;
                sreNames.pop();
                if (!sreNames.empty())
                    setSREvalue(sreNames.peek(), sreName);
                else {
                    if (isMatch) {
                        if (currentMatch.contains("otherMatch"))
                            outLine(3, "%s.SetSRE1(%s);", currentMatch, sreName);
                    }
                }
                //outLine(3, "%s.setSRE1();", matchName);
            } else if (ctx.sreuop() != null) {   // sreuop(sre0) case
                String sreuopStr = null;
                if (ctx.sreuop().srecomp() != null)
                    sreuopStr = "Complement";
                else if (ctx.sreuop().sreactions() != null)
                    sreuopStr = "Actions";
                else if (ctx.sreuop().sreresults() != null)
                    sreuopStr = "Results";
                else if (ctx.sreuop().srepos() != null)
                    sreuopStr = "Positive";
                else //if(ctx.sreuop().sreneg() != null)
                    sreuopStr = "Negative";

                String sreName = "uopSRE" + sreNum++;
                outLine(3, "UopSRE %s = new UopSRE(\"%s\", null);", sreName, sreuopStr);
                sreNames.push(sreName);
                visitSre(ctx.sre(0));
                sreNames.pop();
                if (!sreNames.empty())
                    setSREvalue(sreNames.peek(), sreName);
            } else if (ctx.qid() != null) {   //$qid case
                //for qid case, currently put the qid info in the positiveRE, so when
                //SreUop is null, then we will check if it is pos or neg, when
                //SreUop is not null, then we treat pos differently
                String sreName = "sre" + sreNum++;
                outLine(3, "SRE %s = new SRE(%s, null);", sreName, ctx.qid().getText());
                if (!sreNames.empty())
                    setSREvalue(sreNames.peek(), sreName);
                else
                    outLine(3, "%s.setSRE(%s);", currentExchange, sreName);
            } else if (ctx.LPAREN() != null) {  //() case
                visitChildren(ctx);
            }
        }
        return null;
    }

    @Override
    public Void visitVardecl(@NotNull PoCoParser.VardeclContext ctx) {
        if (ctx.id() != null) {
            VarTypeVal varTyCal;
            if (ctx.RETYPE() != null) {
                varTyCal = new VarTypeVal(VarTypeVal.ClosureType.RE_TYPE, null, null);
            } else {
                varTyCal = new VarTypeVal(VarTypeVal.ClosureType.SRE_TYPE, null, null);
            }
            closure.addClosure(ctx.id().getText(), varTyCal);
        }

        return null;
    }

    @Override
    public Void visitRe(@NotNull PoCoParser.ReContext ctx) {
        if (matchRHS == true) {
            String matchStr = null;
            if (ctx.function() != null) {
                matchStr = ctx.function().fxnname().getText();
                if (ctx.function().arglist().re().rewild() == null)
                    matchStr += "(" + ctx.function().arglist().getText() + ")";
            } else {
                matchStr = scrubString(ctx.getText());
            }
            String matchName = "match" + matchNum++;
            outLine(3, "Match %s = new Match(\"%s\");", matchName, matchStr);
            outLine(3, "%s.addMatcher(%s);", currentExchange, matchName);

        } else {
            if (ctx.AT() != null) {
                if (ctx.id() != null) {
                    if (closure != null) {
                        VarTypeVal val = closure.loadClosure(ctx.id().getText());
                        val.setReContext(ctx.re(0));
                        closure.updateClosure(ctx.id().getText(), val);
                    }
                } else
                    throw new NullPointerException("No such var exist.");
                visitRe(ctx.re(0));
            } else if (ctx.rewild() != null) {
                if (isResult) {//it is the re case for ire result
                    if (isResultMatch)
                        outLine(3, "%s.setResultMatchStr(\".\");", scrubString(currentMatch));
                } else if (isAction && isCombinedMatch) {
                    //System.out.println(ctx.getText());
                    outLine(3, "%s.setMatchString(\".\");", currentMatch);
                }
            } else {
                String content;
                if (ctx.DOLLAR() != null) {
                    content = loadFromClosure(ctx.qid().getText());
                    if (content == null) {
                        content = ctx.qid().getText();
                    }
                } else {
                    if (ctx.object() != null)
                        content = parseObject(ctx.object().qid().getText(), ctx.object().re().getText());
                    else
                        content = ctx.getText();
                }

                if (isSre) {
                    if (isSrePos)
                        outLine(3, "%s.setPositiveRE(\"%s\");", sreNames.peek(), scrubString(content));
                    else
                        outLine(3, "%s.setNegativeRE(\"%s\");", sreNames.peek(), scrubString(content));

                    if (isMapSre)
                        outLine(3, "%s.setMatchSre(%s);", executionNames.peek(), sreNames.peek());
                    //if it is Bopsre case, we will need postpone setSRE till pop to the right Sre
                    if (isReturnValue && !isSreBop2 && !isSreBop1)
                        outLine(3, "%s.setSRE(%s);", currentExchange, sreNames.peek());
                } else if (isIre) {
                    if (isAction) {
                        outLine(3, "%s.setAsAction();", currentMatch);
                        outLine(3, "%s.setMatchString(\"%s\");", currentMatch, scrubString(content));
                    } else {
                        if (!isResultMatch) {
                            outLine(3, "%s.setAsResult();", currentMatch);
                            outLine(3, "%s.setMatchString(\"%s\");", currentMatch, scrubString(content));
                        } else
                            outLine(3, "%s.setResultMatchStr(\"%s\");", currentMatch, scrubString(content));
                    }
                } else if (isMatch) {
                    outLine(3, "%s.setMatchString(\"%s\");", currentMatch, scrubString(content));
                }

            }
        }
        return null;
    }

    @Override
    public Void visitTransaction(@NotNull PoCoParser.TransactionContext ctx) {
        String transaction = ctx.transbody().getText();
        transactions=transaction.substring(0, transaction.length()-15);
        return null;
    }

    public boolean hasTransation() {
        if(transactions != null)
            return true;
        return false;
    }

    public String getTransactions() {
        return transactions;
    }

    /**
     * use to get the last seen modifier and reset the flags of the modifiers to false
     */
    public String getModifierFlag() {
        String result = "none";
        if (hasAsterisk)
            result = "*";
        else if (hasPlus)
            result = "+";
        hasAsterisk = false;
        hasPlus = false;
        return result;
    }

    /**
     * use to set the modifier flag
     *
     * @param asterisk if the execution has asterisk
     * @param plus     if the execution has plus
     */
    public void setModifierFlag(boolean asterisk, boolean plus) {
        hasAsterisk = asterisk;
        hasPlus = plus;
    }


    public void setSREvalue(String fieldName, String sreName) {
        if (fieldName.contains("uopSRE"))
            outLine(3, "%s.setSRE(%s);", fieldName, sreName);
        else if (fieldName.contains("bopSRE")) {
            if (isSreBop1)
                outLine(3, "%s.setSRE1(%s);", fieldName, sreName);
            if (isSreBop2)
                outLine(3, "%s.setSRE2(%s);", fieldName, sreName);
        }
    }

    /**
     * This method use to track the value from object format
     */
    private String parseObject(String type, String value) {
        switch (type) {
            case "Integer":
                return getIntVal(value);
            //will add more case here
            default:
                return value;
        }
    }

    private String getIntVal(String input) {
        int result = -99;
        switch (input) {
            case "JOptionPane.OK_OPTION":
                result = 0;
                break;
            case "JOptionPane.NO_OPTION":
                result = 1;
                break;
        }
        return Integer.toString(result);
    }

    private static String scrubString(String input) {
        return input.replaceAll("(%|\\$[a-zA-Z0-9\\.\\-_]+)", "");
    }

    public String loadFromClosure(String varName) {
        String strval = null;
        if (closure != null)
            if (closure.loadClosure(varName) != null)
                if (closure.loadClosure(varName).getVarType() == VarTypeVal.ClosureType.RE_TYPE &&
                        closure.loadClosure(varName).getReContext() != null)
                    strval = closure.loadClosure(varName).getReContext().getText();
                else if (closure.loadClosure(varName).getVarType() == VarTypeVal.ClosureType.SRE_TYPE &&
                        closure.loadClosure(varName).getSreContext() != null)
                    strval = closure.loadClosure(varName).getSreContext().getText();

        return strval;
    }
}
