package com.poco.StaticAnalysis;
import com.poco.PoCoParser.PoCoLexer;
import com.poco.PoCoParser.PoCoParser;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class StaticAnalysis {
    LinkedHashSet<String> methods = new LinkedHashSet<String>();

    public void StaticAnalysis(CharStream input, LinkedHashSet<String> possibleinputs)
    {
        Iterator<String> itr = possibleinputs.iterator();
        while (itr.hasNext()) {
            String method = itr.next().replaceAll("\\(.*\\)", "");
            method = method.substring(method.indexOf(' ') + 1);
            methods.add(method);
        }

        PoCoLexer lexer = new PoCoLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PoCoParser parser = new PoCoParser(tokens);
        ParserRuleContext tree = parser.policy(); // parse
        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        try {
            UncoveredExecutionPaths uncoveredExecutionPaths = new UncoveredExecutionPaths(parser, methods);
            walker.walk(uncoveredExecutionPaths, tree);
        }
        catch(Exception ex){
            System.out.println("Error: There was an error performing 'Uncovered Execution Paths'");
        }
        try {
            NoMatchingActions noMatchingActions = new NoMatchingActions(parser, methods);
            walker.walk(noMatchingActions, tree);
        }
        catch(Exception ex){
            System.out.println("Error: There was an error performing 'No Matching Actions'");
        }
        try {
            UnusedBindings unusedBindings = new UnusedBindings(parser);
            walker.walk(unusedBindings, tree);
        }
        catch(Exception ex){
            System.out.println("Error: There was an error performing 'Unused Bindings'");
        }
        try {
            NondeterministicLoops nondeterministicLoops = new NondeterministicLoops(parser);
            walker.walk(nondeterministicLoops, tree);
        }
        catch(Exception ex){
            System.out.println("Error: There was an error performing 'Nondeterministic Loops'");
        }
        try {
            InfinitePositiveNoConcrete infinitePositiveNoConcrete = new InfinitePositiveNoConcrete(parser);
            walker.walk(infinitePositiveNoConcrete, tree);
        }
        catch(Exception ex){
            System.out.println("Error: There was an error performing 'Infinite Positive Result With No Concrete Events'");
        }
        try {
            EmptyPositiveNegativeInput emptyPositiveNegativeInput = new EmptyPositiveNegativeInput(parser);
            walker.walk(emptyPositiveNegativeInput, tree);
        }
        catch(Exception ex){
            System.out.println("Error: There was an error performing 'Empty Positive with Negative Input'");
        }
    }
}
