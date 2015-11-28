package com.poco.StaticAnalysis;

import com.poco.Extractor.Closure;
import com.poco.PoCoCompiler.PoCoUtils;
import com.poco.PoCoParser.PoCoLexer;
import com.poco.PoCoParser.PoCoParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticAnalysis {
    private Closure closure;
    LinkedHashSet<String> methods = new LinkedHashSet<String>();

    public StaticAnalysis(Closure closure) {
        this.closure = closure;
    }

    public void StaticAnalysis(CharStream input, LinkedHashSet<String> possibleinputs) {
        Iterator<String> itr = possibleinputs.iterator();

        while (itr.hasNext()) {
            String method = itr.next().replace("\\(.*\\)", "");
            method = method.substring(method.indexOf(' ') + 1);
            methods.add(method);
        }

        PoCoLexer lexer = new PoCoLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PoCoParser parser = new PoCoParser(tokens);
        ParserRuleContext tree = parser.policy(); // parse
        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker

        try {
            UnusedBindings unusedBindings = new UnusedBindings();
            unusedBindings.visit(tree);
            if (unusedBindings.hasUnInitialedVars()) {
                HashSet<String> useB4Initialed = unusedBindings.getUseB4Initialed();
                if(useB4Initialed != null) {
                    int count = genWarning4Vars(useB4Initialed, 1);
                    if (count > 1)
                        System.out.print("\n\t\t are used before being initialized.\n");
                    else
                        System.out.println(" is used before being initialized.");
                }
                HashSet<String> unInitialedVars = unusedBindings.getUnInitialedVars();
                if(unInitialedVars != null) {
                    int count = genWarning4Vars(unInitialedVars, 1);
                    if (count > 1)
                        System.out.print("\n\t\t are uninitialized.\n");
                    else
                        System.out.println(" is uninitialized.");
                }
                System.exit(-1);
            }

            if (unusedBindings.hasUnusedVars()) {
                HashSet<String> unusedVars = unusedBindings.getUnusedVars();
                int count = genWarning4Vars(unusedVars,0);

                if (count > 1)
                    System.out.print("\n\t\t are unused.\n\n");
                else
                    System.out.println(" is unused.\n");
            }
        } catch (Exception ex) {
            System.out.println("Error: There was an error performing 'Unused Bindings'");
        }

        try {
            UncoveredExecutionPaths uncoveredExecutionPaths = new UncoveredExecutionPaths(parser, methods);
            walker.walk(uncoveredExecutionPaths, tree);
        } catch (Exception ex) {
            System.out.println("Error: There was an error performing 'Uncovered Execution Paths'");
        }

        try {
            NoMatchingActions noMatchingActions = new NoMatchingActions(closure);
            noMatchingActions.visit(tree);
            HashSet<String> allSpecifiedMtds = noMatchingActions.getAllMethods();
            ArrayList<String> unUsedMethod = checkAllMethod(allSpecifiedMtds, methods);
            if (unUsedMethod != null) {
                int count = unUsedMethod.size();
                int i = 0;
                if (count == 1)
                    System.out.print("Warning: The concerned method: \n\t\t\t");
                else
                    System.out.print("Warning: The concerned methods: \n\t\t\t");

                for (Iterator<String> it = unUsedMethod.iterator(); it.hasNext(); ) {
                    System.out.print(it.next());
                    if (i++ != count - 1)
                        System.out.print("; \n\t\t\t");
                }
                if (count > 1)
                    System.out.print("\n\t\t");
                System.out.println(" may not match any possible input actions.\n");
            }
        } catch (Exception ex) {
            System.out.println("Error: There was an error performing 'No Matching Actions'");
        }


        try {
            NondeterministicLoops nondeterministicLoops = new NondeterministicLoops(parser);
            walker.walk(nondeterministicLoops, tree);
        } catch (Exception ex) {
            System.out.println("Error: There was an error performing 'Nondeterministic Loops'");
        }

        try {
            InfinitePositiveNoConcrete infinitePositiveNoConcrete = new InfinitePositiveNoConcrete(parser);
            walker.walk(infinitePositiveNoConcrete, tree);
        } catch (Exception ex) {
            System.out.println("Error: There was an error performing 'Infinite Positive Result With No Concrete Events'");
        }

        try {
            EmptyPositiveNegativeInput emptyPositiveNegativeInput = new EmptyPositiveNegativeInput(parser);
            walker.walk(emptyPositiveNegativeInput, tree);
        } catch (Exception ex) {
            System.out.println("Error: There was an error performing 'Empty Positive with Negative Input'");
        }
    }

    /**
     *
     * @param unusedVars
     * @param mode use to generate different type of message 0: warning; 1: Error
     * @return
     */
    private int genWarning4Vars(HashSet<String> unusedVars, int mode) {
        if(mode == 0)
            System.out.print("Warning: \t");
        else
            System.out.print("Error: \t");
        int count = unusedVars.size();
        int i = 0;
        for (Iterator<String> it = unusedVars.iterator(); it.hasNext(); ) {
            String temp = it.next();
            int lPindex = temp.indexOf('(');
            System.out.print(temp.substring(0, lPindex) + " policy's declared variable: " +
                    temp.substring(lPindex + 1, temp.length() - 1));
            if (i++ != count - 1)
                System.out.print("; \n\t\t\t");
        }
        return count;
    }

    private ArrayList<String> checkAllMethod(HashSet<String> allSpecifiedMtds, LinkedHashSet<String> methods) {
        ArrayList<String> unusedMth = new ArrayList<>();

        for (Iterator<String> it = allSpecifiedMtds.iterator(); it.hasNext(); ) {
            boolean isused = false;
            String mtd = it.next();
            String reg = validateStr(mtd);
            for (Iterator<String> method = methods.iterator(); method.hasNext(); ) {
                if (isMatching(reg, method.next())) {
                    isused = true;
                    break;
                }
            }
            if (!isused)
                unusedMth.add(mtd);
        }
        if (unusedMth.size() > 0)
            return unusedMth;
        else
            return null;
    }

    private String validateStr(String str) {
        if (str == null)
            return null;

        str = str.replace(".", "\\.").replace("(", "\\(").replace("$", "\\$")
                .replace(")", "\\)").replace("{", "\\{").replace("}", "\\}")
                .replace("#", "\\#").replace("@", "\\@").replace("?", "\\?")
                .replace("%", "(.*)");

        String funcNme = PoCoUtils.getMtdNmInfo(str);
        if (funcNme.split("\\s+").length == 1)
            str = "(.*)" + str;
        return str;
    }

    private boolean isMatching(String reg, String str4Match) {
        if (str4Match == null)
            return false;

        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str4Match);
        return matcher.find();
    }

}
