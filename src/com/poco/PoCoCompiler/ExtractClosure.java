package com.poco.PoCoCompiler;

import com.poco.Extractor.Closure;
import com.poco.Extractor.VarTypeVal;
import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caoyan on 11/7/14.
 */
public class ExtractClosure extends PoCoParserBaseVisitor<Void> {
    private Closure closure;

    public Closure getClosure() {
        return closure;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    public ExtractClosure(Closure closure) {
        this.closure = closure;
    }

    private boolean frmOpparlist = false;
    private boolean parseRe = false;
    private String closureVal;

    /**
     * Generates code for class representing a PoCo policy. This is the first visit method called.
     *
     * @param ctx
     * @return
     */
    @Override
    public Void visitPocopol(@NotNull PoCoParser.PocopolContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitVardecls(@NotNull PoCoParser.VardeclsContext ctx) {
        if (ctx.vardecl() != null) {
            //now only deal with single policy, need add parents in future
            closure = new Closure();
            visitChildren(ctx);
        }
        return null;
    }

    @Override
    public Void visitVardecl(@NotNull PoCoParser.VardeclContext ctx) {
        if (ctx.id() != null) {
            closure.addClosure(ctx.id().getText(), new VarTypeVal(null, null));
        }
        return null;
    }

    @Override
    public Void visitMacrodecls(@NotNull PoCoParser.MacrodeclsContext ctx) {
        if (ctx.macrodecl() != null) {
            if (closure == null)
                closure = new Closure();
            visitChildren(ctx);
        }
        return null;
    }

    @Override
    public Void visitMacrodecl(@NotNull PoCoParser.MacrodeclContext ctx) {
        String str = null;
        VarTypeVal varTyCal;

        if (ctx.RETYPE() != null)
            str = ctx.re().getText();
        else
            str = ctx.sre().getText();

        String reg = "#(.+)\\{(.+)\\}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            String strVal = matcher.group(1).toString().trim();
            String qid = matcher.group(2).toString();
            varTyCal = new VarTypeVal(matcher.group(1).toString().trim(), getObjVal(str));
        } else {
            varTyCal = new VarTypeVal(null, getObjVal(str));
        }
        closure.addClosure(ctx.id().getText(), varTyCal);
        return null;
    }

    public Void visitRe(@NotNull PoCoParser.ReContext ctx) {
        return null;
    }

    private static String getObjVal(String str) {
        String tempStr = "";
        String returnStr = "";
        String reg = "#(.+)\\{(.+)\\}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find())
            tempStr = matcher.group(2).toString().trim().replaceAll("\\\\", "");
        else
            tempStr = str.replaceAll("\\\\", "");

        if (tempStr.indexOf('$') == -1) {
            returnStr = tempStr;
        } else {
            while (tempStr.indexOf('$') != -1) {
                returnStr += tempStr.substring(0, tempStr.indexOf('$'));
                tempStr = tempStr.substring(tempStr.indexOf('$'), tempStr.length());
                if (tempStr.indexOf(' ') != -1) {
                    returnStr += "$" + tempStr.substring(0, tempStr.indexOf(' ')) + "$$";
                    tempStr = tempStr.substring(tempStr.indexOf(' ') + 1, tempStr.length());
                }
            }
            returnStr += tempStr;
        }
        return returnStr;
    } 
}
