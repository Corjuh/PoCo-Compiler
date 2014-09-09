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
        Iterator<String> itr =  possibleinputs.iterator();
        while(itr.hasNext()) {
            String method = itr.next().replaceAll("\\(.*\\)", "");
            method = method.substring(method.indexOf(' ') + 1);
            methods.add(method);
        }

        PoCoLexer lexer = new PoCoLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PoCoParser parser = new PoCoParser(tokens);
        ParserRuleContext tree = parser.policy(); // parse
        ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
        UncoveredExecutionPaths uncoveredExecutionPaths = new UncoveredExecutionPaths(parser, methods);
        walker.walk(uncoveredExecutionPaths, tree);
        NoMatchingActions noMatchingActions = new NoMatchingActions(parser, methods);
        walker.walk(noMatchingActions, tree);
    }
}
