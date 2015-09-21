// Generated from /Users/yan/Dropbox/PoCo-Compiler/Parser/grammar/PoCoParser.g4 by ANTLR 4.5.1
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
	 * Visit a parse tree produced by {@link PoCoParser#policy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolicy(PoCoParser.PolicyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#ppol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPpol(PoCoParser.PpolContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#pocopol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPocopol(PoCoParser.PocopolContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#metapol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMetapol(PoCoParser.MetapolContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#pimport}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPimport(PoCoParser.PimportContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#pimports}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPimports(PoCoParser.PimportsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#treedef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTreedef(PoCoParser.TreedefContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#treedefs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTreedefs(PoCoParser.TreedefsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#policyarg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolicyarg(PoCoParser.PolicyargContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#policyargs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolicyargs(PoCoParser.PolicyargsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#transactionlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionlist(PoCoParser.TransactionlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#transaction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction(PoCoParser.TransactionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#transbody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransbody(PoCoParser.TransbodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#macrodecls}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacrodecls(PoCoParser.MacrodeclsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#macrodecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacrodecl(PoCoParser.MacrodeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#vardecls}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVardecls(PoCoParser.VardeclsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#vardecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVardecl(PoCoParser.VardeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#srecase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSrecase(PoCoParser.SrecaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#idlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdlist(PoCoParser.IdlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#paramlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamlist(PoCoParser.ParamlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#execution}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecution(PoCoParser.ExecutionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#map}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMap(PoCoParser.MapContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#exch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExch(PoCoParser.ExchContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#pinst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPinst(PoCoParser.PinstContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgs(PoCoParser.ArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#object}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject(PoCoParser.ObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#fieldlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldlist(PoCoParser.FieldlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#matchs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchs(PoCoParser.MatchsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#match}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatch(PoCoParser.MatchContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#ire}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIre(PoCoParser.IreContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sre}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSre(PoCoParser.SreContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreunion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreunion(PoCoParser.SreunionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreconj}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreconj(PoCoParser.SreconjContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sredisj}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSredisj(PoCoParser.SredisjContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreequals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreequals(PoCoParser.SreequalsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#srepunion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSrepunion(PoCoParser.SrepunionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#srebop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSrebop(PoCoParser.SrebopContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#srecomp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSrecomp(PoCoParser.SrecompContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreactions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreactions(PoCoParser.SreactionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreresults}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreresults(PoCoParser.SreresultsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#srepos}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSrepos(PoCoParser.SreposContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreneg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreneg(PoCoParser.SrenegContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#sreuop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSreuop(PoCoParser.SreuopContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(PoCoParser.IdContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#qid}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQid(PoCoParser.QidContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#opparamlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpparamlist(PoCoParser.OpparamlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#re}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRe(PoCoParser.ReContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction(PoCoParser.FunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#fxnname}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFxnname(PoCoParser.FxnnameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#arglist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArglist(PoCoParser.ArglistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#rebop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRebop(PoCoParser.RebopContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#reuop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReuop(PoCoParser.ReuopContext ctx);
	/**
	 * Visit a parse tree produced by {@link PoCoParser#rewild}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRewild(PoCoParser.RewildContext ctx);
}