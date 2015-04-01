// Generated from /Users/caoyan/GitHub/PoCo-Compiler/Parser/grammar/PoCoParser.g4 by ANTLR 4.x
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PoCoParser}.
 */
public interface PoCoParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PoCoParser#arglist}.
	 * @param ctx the parse tree
	 */
	void enterArglist(@NotNull PoCoParser.ArglistContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#arglist}.
	 * @param ctx the parse tree
	 */
	void exitArglist(@NotNull PoCoParser.ArglistContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#metapol}.
	 * @param ctx the parse tree
	 */
	void enterMetapol(@NotNull PoCoParser.MetapolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#metapol}.
	 * @param ctx the parse tree
	 */
	void exitMetapol(@NotNull PoCoParser.MetapolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#transaction}.
	 * @param ctx the parse tree
	 */
	void enterTransaction(@NotNull PoCoParser.TransactionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#transaction}.
	 * @param ctx the parse tree
	 */
	void exitTransaction(@NotNull PoCoParser.TransactionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#args}.
	 * @param ctx the parse tree
	 */
	void enterArgs(@NotNull PoCoParser.ArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#args}.
	 * @param ctx the parse tree
	 */
	void exitArgs(@NotNull PoCoParser.ArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#rewild}.
	 * @param ctx the parse tree
	 */
	void enterRewild(@NotNull PoCoParser.RewildContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#rewild}.
	 * @param ctx the parse tree
	 */
	void exitRewild(@NotNull PoCoParser.RewildContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sredisj}.
	 * @param ctx the parse tree
	 */
	void enterSredisj(@NotNull PoCoParser.SredisjContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sredisj}.
	 * @param ctx the parse tree
	 */
	void exitSredisj(@NotNull PoCoParser.SredisjContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#transbody}.
	 * @param ctx the parse tree
	 */
	void enterTransbody(@NotNull PoCoParser.TransbodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#transbody}.
	 * @param ctx the parse tree
	 */
	void exitTransbody(@NotNull PoCoParser.TransbodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#srebop}.
	 * @param ctx the parse tree
	 */
	void enterSrebop(@NotNull PoCoParser.SrebopContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#srebop}.
	 * @param ctx the parse tree
	 */
	void exitSrebop(@NotNull PoCoParser.SrebopContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(@NotNull PoCoParser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(@NotNull PoCoParser.FunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#transactionlist}.
	 * @param ctx the parse tree
	 */
	void enterTransactionlist(@NotNull PoCoParser.TransactionlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#transactionlist}.
	 * @param ctx the parse tree
	 */
	void exitTransactionlist(@NotNull PoCoParser.TransactionlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#fxnname}.
	 * @param ctx the parse tree
	 */
	void enterFxnname(@NotNull PoCoParser.FxnnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#fxnname}.
	 * @param ctx the parse tree
	 */
	void exitFxnname(@NotNull PoCoParser.FxnnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#srecase}.
	 * @param ctx the parse tree
	 */
	void enterSrecase(@NotNull PoCoParser.SrecaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#srecase}.
	 * @param ctx the parse tree
	 */
	void exitSrecase(@NotNull PoCoParser.SrecaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreactions}.
	 * @param ctx the parse tree
	 */
	void enterSreactions(@NotNull PoCoParser.SreactionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreactions}.
	 * @param ctx the parse tree
	 */
	void exitSreactions(@NotNull PoCoParser.SreactionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#reuop}.
	 * @param ctx the parse tree
	 */
	void enterReuop(@NotNull PoCoParser.ReuopContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#reuop}.
	 * @param ctx the parse tree
	 */
	void exitReuop(@NotNull PoCoParser.ReuopContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#pocopol}.
	 * @param ctx the parse tree
	 */
	void enterPocopol(@NotNull PoCoParser.PocopolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#pocopol}.
	 * @param ctx the parse tree
	 */
	void exitPocopol(@NotNull PoCoParser.PocopolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#policyargs}.
	 * @param ctx the parse tree
	 */
	void enterPolicyargs(@NotNull PoCoParser.PolicyargsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#policyargs}.
	 * @param ctx the parse tree
	 */
	void exitPolicyargs(@NotNull PoCoParser.PolicyargsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#policyarg}.
	 * @param ctx the parse tree
	 */
	void enterPolicyarg(@NotNull PoCoParser.PolicyargContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#policyarg}.
	 * @param ctx the parse tree
	 */
	void exitPolicyarg(@NotNull PoCoParser.PolicyargContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#idlist}.
	 * @param ctx the parse tree
	 */
	void enterIdlist(@NotNull PoCoParser.IdlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#idlist}.
	 * @param ctx the parse tree
	 */
	void exitIdlist(@NotNull PoCoParser.IdlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#vardecls}.
	 * @param ctx the parse tree
	 */
	void enterVardecls(@NotNull PoCoParser.VardeclsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#vardecls}.
	 * @param ctx the parse tree
	 */
	void exitVardecls(@NotNull PoCoParser.VardeclsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#pimport}.
	 * @param ctx the parse tree
	 */
	void enterPimport(@NotNull PoCoParser.PimportContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#pimport}.
	 * @param ctx the parse tree
	 */
	void exitPimport(@NotNull PoCoParser.PimportContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreequals}.
	 * @param ctx the parse tree
	 */
	void enterSreequals(@NotNull PoCoParser.SreequalsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreequals}.
	 * @param ctx the parse tree
	 */
	void exitSreequals(@NotNull PoCoParser.SreequalsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#srecomp}.
	 * @param ctx the parse tree
	 */
	void enterSrecomp(@NotNull PoCoParser.SrecompContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#srecomp}.
	 * @param ctx the parse tree
	 */
	void exitSrecomp(@NotNull PoCoParser.SrecompContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#ppol}.
	 * @param ctx the parse tree
	 */
	void enterPpol(@NotNull PoCoParser.PpolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#ppol}.
	 * @param ctx the parse tree
	 */
	void exitPpol(@NotNull PoCoParser.PpolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#treedefs}.
	 * @param ctx the parse tree
	 */
	void enterTreedefs(@NotNull PoCoParser.TreedefsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#treedefs}.
	 * @param ctx the parse tree
	 */
	void exitTreedefs(@NotNull PoCoParser.TreedefsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreunion}.
	 * @param ctx the parse tree
	 */
	void enterSreunion(@NotNull PoCoParser.SreunionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreunion}.
	 * @param ctx the parse tree
	 */
	void exitSreunion(@NotNull PoCoParser.SreunionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#policy}.
	 * @param ctx the parse tree
	 */
	void enterPolicy(@NotNull PoCoParser.PolicyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#policy}.
	 * @param ctx the parse tree
	 */
	void exitPolicy(@NotNull PoCoParser.PolicyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#opparamlist}.
	 * @param ctx the parse tree
	 */
	void enterOpparamlist(@NotNull PoCoParser.OpparamlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#opparamlist}.
	 * @param ctx the parse tree
	 */
	void exitOpparamlist(@NotNull PoCoParser.OpparamlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#match}.
	 * @param ctx the parse tree
	 */
	void enterMatch(@NotNull PoCoParser.MatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#match}.
	 * @param ctx the parse tree
	 */
	void exitMatch(@NotNull PoCoParser.MatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#pimports}.
	 * @param ctx the parse tree
	 */
	void enterPimports(@NotNull PoCoParser.PimportsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#pimports}.
	 * @param ctx the parse tree
	 */
	void exitPimports(@NotNull PoCoParser.PimportsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#pinst}.
	 * @param ctx the parse tree
	 */
	void enterPinst(@NotNull PoCoParser.PinstContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#pinst}.
	 * @param ctx the parse tree
	 */
	void exitPinst(@NotNull PoCoParser.PinstContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#macrodecl}.
	 * @param ctx the parse tree
	 */
	void enterMacrodecl(@NotNull PoCoParser.MacrodeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#macrodecl}.
	 * @param ctx the parse tree
	 */
	void exitMacrodecl(@NotNull PoCoParser.MacrodeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#object}.
	 * @param ctx the parse tree
	 */
	void enterObject(@NotNull PoCoParser.ObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#object}.
	 * @param ctx the parse tree
	 */
	void exitObject(@NotNull PoCoParser.ObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#paramlist}.
	 * @param ctx the parse tree
	 */
	void enterParamlist(@NotNull PoCoParser.ParamlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#paramlist}.
	 * @param ctx the parse tree
	 */
	void exitParamlist(@NotNull PoCoParser.ParamlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreresults}.
	 * @param ctx the parse tree
	 */
	void enterSreresults(@NotNull PoCoParser.SreresultsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreresults}.
	 * @param ctx the parse tree
	 */
	void exitSreresults(@NotNull PoCoParser.SreresultsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#re}.
	 * @param ctx the parse tree
	 */
	void enterRe(@NotNull PoCoParser.ReContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#re}.
	 * @param ctx the parse tree
	 */
	void exitRe(@NotNull PoCoParser.ReContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#execution}.
	 * @param ctx the parse tree
	 */
	void enterExecution(@NotNull PoCoParser.ExecutionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#execution}.
	 * @param ctx the parse tree
	 */
	void exitExecution(@NotNull PoCoParser.ExecutionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#srepunion}.
	 * @param ctx the parse tree
	 */
	void enterSrepunion(@NotNull PoCoParser.SrepunionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#srepunion}.
	 * @param ctx the parse tree
	 */
	void exitSrepunion(@NotNull PoCoParser.SrepunionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#id}.
	 * @param ctx the parse tree
	 */
	void enterId(@NotNull PoCoParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#id}.
	 * @param ctx the parse tree
	 */
	void exitId(@NotNull PoCoParser.IdContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#map}.
	 * @param ctx the parse tree
	 */
	void enterMap(@NotNull PoCoParser.MapContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#map}.
	 * @param ctx the parse tree
	 */
	void exitMap(@NotNull PoCoParser.MapContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sre}.
	 * @param ctx the parse tree
	 */
	void enterSre(@NotNull PoCoParser.SreContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sre}.
	 * @param ctx the parse tree
	 */
	void exitSre(@NotNull PoCoParser.SreContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#rebop}.
	 * @param ctx the parse tree
	 */
	void enterRebop(@NotNull PoCoParser.RebopContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#rebop}.
	 * @param ctx the parse tree
	 */
	void exitRebop(@NotNull PoCoParser.RebopContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#treedef}.
	 * @param ctx the parse tree
	 */
	void enterTreedef(@NotNull PoCoParser.TreedefContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#treedef}.
	 * @param ctx the parse tree
	 */
	void exitTreedef(@NotNull PoCoParser.TreedefContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreuop}.
	 * @param ctx the parse tree
	 */
	void enterSreuop(@NotNull PoCoParser.SreuopContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreuop}.
	 * @param ctx the parse tree
	 */
	void exitSreuop(@NotNull PoCoParser.SreuopContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#macrodecls}.
	 * @param ctx the parse tree
	 */
	void enterMacrodecls(@NotNull PoCoParser.MacrodeclsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#macrodecls}.
	 * @param ctx the parse tree
	 */
	void exitMacrodecls(@NotNull PoCoParser.MacrodeclsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#qid}.
	 * @param ctx the parse tree
	 */
	void enterQid(@NotNull PoCoParser.QidContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#qid}.
	 * @param ctx the parse tree
	 */
	void exitQid(@NotNull PoCoParser.QidContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#srepos}.
	 * @param ctx the parse tree
	 */
	void enterSrepos(@NotNull PoCoParser.SreposContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#srepos}.
	 * @param ctx the parse tree
	 */
	void exitSrepos(@NotNull PoCoParser.SreposContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreconj}.
	 * @param ctx the parse tree
	 */
	void enterSreconj(@NotNull PoCoParser.SreconjContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreconj}.
	 * @param ctx the parse tree
	 */
	void exitSreconj(@NotNull PoCoParser.SreconjContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreneg}.
	 * @param ctx the parse tree
	 */
	void enterSreneg(@NotNull PoCoParser.SrenegContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreneg}.
	 * @param ctx the parse tree
	 */
	void exitSreneg(@NotNull PoCoParser.SrenegContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#ire}.
	 * @param ctx the parse tree
	 */
	void enterIre(@NotNull PoCoParser.IreContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#ire}.
	 * @param ctx the parse tree
	 */
	void exitIre(@NotNull PoCoParser.IreContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#fieldlist}.
	 * @param ctx the parse tree
	 */
	void enterFieldlist(@NotNull PoCoParser.FieldlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#fieldlist}.
	 * @param ctx the parse tree
	 */
	void exitFieldlist(@NotNull PoCoParser.FieldlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#matchs}.
	 * @param ctx the parse tree
	 */
	void enterMatchs(@NotNull PoCoParser.MatchsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#matchs}.
	 * @param ctx the parse tree
	 */
	void exitMatchs(@NotNull PoCoParser.MatchsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#exch}.
	 * @param ctx the parse tree
	 */
	void enterExch(@NotNull PoCoParser.ExchContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#exch}.
	 * @param ctx the parse tree
	 */
	void exitExch(@NotNull PoCoParser.ExchContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#vardecl}.
	 * @param ctx the parse tree
	 */
	void enterVardecl(@NotNull PoCoParser.VardeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#vardecl}.
	 * @param ctx the parse tree
	 */
	void exitVardecl(@NotNull PoCoParser.VardeclContext ctx);
}