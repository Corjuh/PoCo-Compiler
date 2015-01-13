// Generated from E:/Users/Danielle/IdeaProjects/PoCo-Compiler/Parser/grammar\PoCoParser.g4 by ANTLR 4.4.1-dev
package com.poco.PoCoParser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PoCoParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PoCoParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PoCoParser#arglist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArglist(@NotNull PoCoParser.ArglistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#metapol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMetapol(@NotNull PoCoParser.MetapolContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#transaction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction(@NotNull PoCoParser.TransactionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgs(@NotNull PoCoParser.ArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#rewild}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRewild(@NotNull PoCoParser.RewildContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sredisj}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSredisj(@NotNull PoCoParser.SredisjContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#transbody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransbody(@NotNull PoCoParser.TransbodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#srebop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSrebop(@NotNull PoCoParser.SrebopContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction(@NotNull PoCoParser.FunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#fxnname}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFxnname(@NotNull PoCoParser.FxnnameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#transactionlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionlist(@NotNull PoCoParser.TransactionlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#srecase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSrecase(@NotNull PoCoParser.SrecaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreactions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreactions(@NotNull PoCoParser.SreactionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#reuop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReuop(@NotNull PoCoParser.ReuopContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#pocopol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPocopol(@NotNull PoCoParser.PocopolContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#policyargs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolicyargs(@NotNull PoCoParser.PolicyargsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#policyarg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolicyarg(@NotNull PoCoParser.PolicyargContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#idlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdlist(@NotNull PoCoParser.IdlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#vardecls}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVardecls(@NotNull PoCoParser.VardeclsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#pimport}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPimport(@NotNull PoCoParser.PimportContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreequals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreequals(@NotNull PoCoParser.SreequalsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#srecomp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSrecomp(@NotNull PoCoParser.SrecompContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#ppol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPpol(@NotNull PoCoParser.PpolContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#treedefs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTreedefs(@NotNull PoCoParser.TreedefsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreunion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreunion(@NotNull PoCoParser.SreunionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#policy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolicy(@NotNull PoCoParser.PolicyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#opparamlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpparamlist(@NotNull PoCoParser.OpparamlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#match}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatch(@NotNull PoCoParser.MatchContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#pimports}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPimports(@NotNull PoCoParser.PimportsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#pinst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPinst(@NotNull PoCoParser.PinstContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#macrodecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacrodecl(@NotNull PoCoParser.MacrodeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#object}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject(@NotNull PoCoParser.ObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#paramlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamlist(@NotNull PoCoParser.ParamlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreresults}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreresults(@NotNull PoCoParser.SreresultsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#re}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRe(@NotNull PoCoParser.ReContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#execution}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecution(@NotNull PoCoParser.ExecutionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#srepunion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSrepunion(@NotNull PoCoParser.SrepunionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(@NotNull PoCoParser.IdContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#map}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMap(@NotNull PoCoParser.MapContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sre}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSre(@NotNull PoCoParser.SreContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#rebop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRebop(@NotNull PoCoParser.RebopContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#treedef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTreedef(@NotNull PoCoParser.TreedefContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreuop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreuop(@NotNull PoCoParser.SreuopContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#macrodecls}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacrodecls(@NotNull PoCoParser.MacrodeclsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#qid}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQid(@NotNull PoCoParser.QidContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#srepos}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSrepos(@NotNull PoCoParser.SreposContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreconj}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreconj(@NotNull PoCoParser.SreconjContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreneg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreneg(@NotNull PoCoParser.SrenegContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#ire}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIre(@NotNull PoCoParser.IreContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#fieldlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldlist(@NotNull PoCoParser.FieldlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#matchs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchs(@NotNull PoCoParser.MatchsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#exch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExch(@NotNull PoCoParser.ExchContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#vardecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVardecl(@NotNull PoCoParser.VardeclContext ctx);
}