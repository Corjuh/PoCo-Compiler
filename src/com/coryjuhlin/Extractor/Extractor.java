package com.coryjuhlin.Extractor;

import com.coryjuhlin.PoCoParser.PoCoParser;
import com.coryjuhlin.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Visits a PoCo policy's Parse tree and extracts all matchable regular expressions
 */
public class Extractor extends PoCoParserBaseVisitor<Void> {
    private LinkedHashSet<String> matchStrings = new LinkedHashSet<>();

    private static String scrubString(String input) {
        return input.replaceAll("(%|\\$[a-zA-Z0-9\\.\\-_]+)", "");
    }

    @Override
    public Void visitRe(@NotNull PoCoParser.ReContext ctx) {
        String scrubbedRE = scrubString(ctx.getText());

        if (scrubbedRE != null && scrubbedRE.length() > 0) {
            matchStrings.add(scrubbedRE);
        }

        return null;
    }

    public void printMatchStrings() {
        for (String matchString : matchStrings) {
            System.out.println(matchString);
        }
    }

    public ArrayList<String> getMatchStrings() {
        return new ArrayList<String>(matchStrings);
    }
}
