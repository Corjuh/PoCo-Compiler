package com.poco.StaticAnalysis;

import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseListener;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by Danielle on 8/5/2014.
 */

public class UnusedBindings extends PoCoParserBaseListener {
    PoCoParser parser;
    List<String> errorbindings = new ArrayList<String>();

    public UnusedBindings(PoCoParser parser) {
        this.parser = parser;
    }
    @Override
    public void exitPocopol(PoCoParser.PocopolContext ctx) {
        for(String binding : errorbindings)
            System.out.println("Warning: The binding '" + binding + "' is not used.");
    }

    @Override
    public void exitMacrodecl(PoCoParser.MacrodeclContext ctx)
    {
        errorbindings.add(ctx.id().getText());
    }

    @Override
    public void exitExch(PoCoParser.ExchContext ctx) {
        if(ctx.DOLLAR() != null)
        {
            errorbindings.remove(ctx.id().getText());
        }
    }

    @Override
    public void exitObject(PoCoParser.ObjectContext ctx) {
        if(ctx.DOLLAR() != null)
        {
            errorbindings.remove(ctx.id().getText());
        }
    }

    @Override
    public void exitSre(PoCoParser.SreContext ctx) {
        if(ctx.children.get(0).getText().equals("$"))
        {
            errorbindings.remove(ctx.qid().getText());
        }
        else if(ctx.children.size() >= 4 && ctx.children.get(3).getText().equals("$"))
        {
            errorbindings.remove(ctx.id(0).getText());
        }
    }

    @Override
    public void exitRe(PoCoParser.ReContext ctx) {
        if(ctx.children.get(0).getText().equals("$"))
        {
            errorbindings.remove(ctx.qid().getText());
        }
    }

    @Override
    public void exitMap(PoCoParser.MapContext ctx) {
        if(ctx.children.get(2).getText().equals("$"))
        {
            errorbindings.remove(ctx.id().getText());
        }
    }
}
