package com.poco.PoCoCompiler;

import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import com.poco.Extractor.Closure;
import com.poco.Extractor.VarTypeVal;
import org.antlr.v4.runtime.misc.NotNull;

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

        VarTypeVal varTyCal;

        if (ctx.RETYPE() != null) {
            varTyCal = new VarTypeVal(VarTypeVal.ClosureType.RE_TYPE, ctx.re(), null);
        } else {
            varTyCal = new VarTypeVal(VarTypeVal.ClosureType.SRE_TYPE, null, ctx.sre());
        }
        closure.addClosure(ctx.id().getText(), varTyCal);

        return null;
    }

}
