// Generated from /Users/yan/Dropbox/PoCo-Compiler/Parser/grammar/PoCoParser.g4 by ANTLR 4.5.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PoCoParser}.
 */
public interface PoCoParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PoCoParser#policy}.
	 * @param ctx the parse tree
	 */
	void enterPolicy(PoCoParser.PolicyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#policy}.
	 * @param ctx the parse tree
	 */
	void exitPolicy(PoCoParser.PolicyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#ppol}.
	 * @param ctx the parse tree
	 */
	void enterPpol(PoCoParser.PpolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#ppol}.
	 * @param ctx the parse tree
	 */
	void exitPpol(PoCoParser.PpolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#pocopol}.
	 * @param ctx the parse tree
	 */
	void enterPocopol(PoCoParser.PocopolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#pocopol}.
	 * @param ctx the parse tree
	 */
	void exitPocopol(PoCoParser.PocopolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#metapol}.
	 * @param ctx the parse tree
	 */
	void enterMetapol(PoCoParser.MetapolContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#metapol}.
	 * @param ctx the parse tree
	 */
	void exitMetapol(PoCoParser.MetapolContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#pimport}.
	 * @param ctx the parse tree
	 */
	void enterPimport(PoCoParser.PimportContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#pimport}.
	 * @param ctx the parse tree
	 */
	void exitPimport(PoCoParser.PimportContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#pimports}.
	 * @param ctx the parse tree
	 */
	void enterPimports(PoCoParser.PimportsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#pimports}.
	 * @param ctx the parse tree
	 */
	void exitPimports(PoCoParser.PimportsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#treedef}.
	 * @param ctx the parse tree
	 */
	void enterTreedef(PoCoParser.TreedefContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#treedef}.
	 * @param ctx the parse tree
	 */
	void exitTreedef(PoCoParser.TreedefContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#treedefs}.
	 * @param ctx the parse tree
	 */
	void enterTreedefs(PoCoParser.TreedefsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#treedefs}.
	 * @param ctx the parse tree
	 */
	void exitTreedefs(PoCoParser.TreedefsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#policyarg}.
	 * @param ctx the parse tree
	 */
	void enterPolicyarg(PoCoParser.PolicyargContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#policyarg}.
	 * @param ctx the parse tree
	 */
	void exitPolicyarg(PoCoParser.PolicyargContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#policyargs}.
	 * @param ctx the parse tree
	 */
	void enterPolicyargs(PoCoParser.PolicyargsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#policyargs}.
	 * @param ctx the parse tree
	 */
	void exitPolicyargs(PoCoParser.PolicyargsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#transactionlist}.
	 * @param ctx the parse tree
	 */
	void enterTransactionlist(PoCoParser.TransactionlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#transactionlist}.
	 * @param ctx the parse tree
	 */
	void exitTransactionlist(PoCoParser.TransactionlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#transaction}.
	 * @param ctx the parse tree
	 */
	void enterTransaction(PoCoParser.TransactionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#transaction}.
	 * @param ctx the parse tree
	 */
	void exitTransaction(PoCoParser.TransactionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#transbody}.
	 * @param ctx the parse tree
	 */
	void enterTransbody(PoCoParser.TransbodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#transbody}.
	 * @param ctx the parse tree
	 */
	void exitTransbody(PoCoParser.TransbodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#macrodecls}.
	 * @param ctx the parse tree
	 */
	void enterMacrodecls(PoCoParser.MacrodeclsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#macrodecls}.
	 * @param ctx the parse tree
	 */
	void exitMacrodecls(PoCoParser.MacrodeclsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#macrodecl}.
	 * @param ctx the parse tree
	 */
	void enterMacrodecl(PoCoParser.MacrodeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#macrodecl}.
	 * @param ctx the parse tree
	 */
	void exitMacrodecl(PoCoParser.MacrodeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#vardecls}.
	 * @param ctx the parse tree
	 */
	void enterVardecls(PoCoParser.VardeclsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#vardecls}.
	 * @param ctx the parse tree
	 */
	void exitVardecls(PoCoParser.VardeclsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#vardecl}.
	 * @param ctx the parse tree
	 */
	void enterVardecl(PoCoParser.VardeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#vardecl}.
	 * @param ctx the parse tree
	 */
	void exitVardecl(PoCoParser.VardeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#srecase}.
	 * @param ctx the parse tree
	 */
	void enterSrecase(PoCoParser.SrecaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#srecase}.
	 * @param ctx the parse tree
	 */
	void exitSrecase(PoCoParser.SrecaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#idlist}.
	 * @param ctx the parse tree
	 */
	void enterIdlist(PoCoParser.IdlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#idlist}.
	 * @param ctx the parse tree
	 */
	void exitIdlist(PoCoParser.IdlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#paramlist}.
	 * @param ctx the parse tree
	 */
	void enterParamlist(PoCoParser.ParamlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#paramlist}.
	 * @param ctx the parse tree
	 */
	void exitParamlist(PoCoParser.ParamlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#execution}.
	 * @param ctx the parse tree
	 */
	void enterExecution(PoCoParser.ExecutionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#execution}.
	 * @param ctx the parse tree
	 */
	void exitExecution(PoCoParser.ExecutionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#map}.
	 * @param ctx the parse tree
	 */
	void enterMap(PoCoParser.MapContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#map}.
	 * @param ctx the parse tree
	 */
	void exitMap(PoCoParser.MapContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#exch}.
	 * @param ctx the parse tree
	 */
	void enterExch(PoCoParser.ExchContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#exch}.
	 * @param ctx the parse tree
	 */
	void exitExch(PoCoParser.ExchContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#pinst}.
	 * @param ctx the parse tree
	 */
	void enterPinst(PoCoParser.PinstContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#pinst}.
	 * @param ctx the parse tree
	 */
	void exitPinst(PoCoParser.PinstContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#args}.
	 * @param ctx the parse tree
	 */
	void enterArgs(PoCoParser.ArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#args}.
	 * @param ctx the parse tree
	 */
	void exitArgs(PoCoParser.ArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#object}.
	 * @param ctx the parse tree
	 */
	void enterObject(PoCoParser.ObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#object}.
	 * @param ctx the parse tree
	 */
	void exitObject(PoCoParser.ObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#fieldlist}.
	 * @param ctx the parse tree
	 */
	void enterFieldlist(PoCoParser.FieldlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#fieldlist}.
	 * @param ctx the parse tree
	 */
	void exitFieldlist(PoCoParser.FieldlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#matchs}.
	 * @param ctx the parse tree
	 */
	void enterMatchs(PoCoParser.MatchsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#matchs}.
	 * @param ctx the parse tree
	 */
	void exitMatchs(PoCoParser.MatchsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#match}.
	 * @param ctx the parse tree
	 */
	void enterMatch(PoCoParser.MatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#match}.
	 * @param ctx the parse tree
	 */
	void exitMatch(PoCoParser.MatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#ire}.
	 * @param ctx the parse tree
	 */
	void enterIre(PoCoParser.IreContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#ire}.
	 * @param ctx the parse tree
	 */
	void exitIre(PoCoParser.IreContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sre}.
	 * @param ctx the parse tree
	 */
	void enterSre(PoCoParser.SreContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sre}.
	 * @param ctx the parse tree
	 */
	void exitSre(PoCoParser.SreContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreunion}.
	 * @param ctx the parse tree
	 */
	void enterSreunion(PoCoParser.SreunionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreunion}.
	 * @param ctx the parse tree
	 */
	void exitSreunion(PoCoParser.SreunionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreconj}.
	 * @param ctx the parse tree
	 */
	void enterSreconj(PoCoParser.SreconjContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreconj}.
	 * @param ctx the parse tree
	 */
	void exitSreconj(PoCoParser.SreconjContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sredisj}.
	 * @param ctx the parse tree
	 */
	void enterSredisj(PoCoParser.SredisjContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sredisj}.
	 * @param ctx the parse tree
	 */
	void exitSredisj(PoCoParser.SredisjContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreequals}.
	 * @param ctx the parse tree
	 */
	void enterSreequals(PoCoParser.SreequalsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreequals}.
	 * @param ctx the parse tree
	 */
	void exitSreequals(PoCoParser.SreequalsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#srepunion}.
	 * @param ctx the parse tree
	 */
	void enterSrepunion(PoCoParser.SrepunionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#srepunion}.
	 * @param ctx the parse tree
	 */
	void exitSrepunion(PoCoParser.SrepunionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#srebop}.
	 * @param ctx the parse tree
	 */
	void enterSrebop(PoCoParser.SrebopContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#srebop}.
	 * @param ctx the parse tree
	 */
	void exitSrebop(PoCoParser.SrebopContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#srecomp}.
	 * @param ctx the parse tree
	 */
	void enterSrecomp(PoCoParser.SrecompContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#srecomp}.
	 * @param ctx the parse tree
	 */
	void exitSrecomp(PoCoParser.SrecompContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreactions}.
	 * @param ctx the parse tree
	 */
	void enterSreactions(PoCoParser.SreactionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreactions}.
	 * @param ctx the parse tree
	 */
	void exitSreactions(PoCoParser.SreactionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreresults}.
	 * @param ctx the parse tree
	 */
	void enterSreresults(PoCoParser.SreresultsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreresults}.
	 * @param ctx the parse tree
	 */
	void exitSreresults(PoCoParser.SreresultsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#srepos}.
	 * @param ctx the parse tree
	 */
	void enterSrepos(PoCoParser.SreposContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#srepos}.
	 * @param ctx the parse tree
	 */
	void exitSrepos(PoCoParser.SreposContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreneg}.
	 * @param ctx the parse tree
	 */
	void enterSreneg(PoCoParser.SrenegContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreneg}.
	 * @param ctx the parse tree
	 */
	void exitSreneg(PoCoParser.SrenegContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#sreuop}.
	 * @param ctx the parse tree
	 */
	void enterSreuop(PoCoParser.SreuopContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#sreuop}.
	 * @param ctx the parse tree
	 */
	void exitSreuop(PoCoParser.SreuopContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#id}.
	 * @param ctx the parse tree
	 */
	void enterId(PoCoParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#id}.
	 * @param ctx the parse tree
	 */
	void exitId(PoCoParser.IdContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#qid}.
	 * @param ctx the parse tree
	 */
	void enterQid(PoCoParser.QidContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#qid}.
	 * @param ctx the parse tree
	 */
	void exitQid(PoCoParser.QidContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#opparamlist}.
	 * @param ctx the parse tree
	 */
	void enterOpparamlist(PoCoParser.OpparamlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#opparamlist}.
	 * @param ctx the parse tree
	 */
	void exitOpparamlist(PoCoParser.OpparamlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#re}.
	 * @param ctx the parse tree
	 */
	void enterRe(PoCoParser.ReContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#re}.
	 * @param ctx the parse tree
	 */
	void exitRe(PoCoParser.ReContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(PoCoParser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(PoCoParser.FunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#fxnname}.
	 * @param ctx the parse tree
	 */
	void enterFxnname(PoCoParser.FxnnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#fxnname}.
	 * @param ctx the parse tree
	 */
	void exitFxnname(PoCoParser.FxnnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#arglist}.
	 * @param ctx the parse tree
	 */
	void enterArglist(PoCoParser.ArglistContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#arglist}.
	 * @param ctx the parse tree
	 */
	void exitArglist(PoCoParser.ArglistContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#rebop}.
	 * @param ctx the parse tree
	 */
	void enterRebop(PoCoParser.RebopContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#rebop}.
	 * @param ctx the parse tree
	 */
	void exitRebop(PoCoParser.RebopContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#reuop}.
	 * @param ctx the parse tree
	 */
	void enterReuop(PoCoParser.ReuopContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#reuop}.
	 * @param ctx the parse tree
	 */
	void exitReuop(PoCoParser.ReuopContext ctx);
	/**
	 * Enter a parse tree produced by {@link PoCoParser#rewild}.
	 * @param ctx the parse tree
	 */
	void enterRewild(PoCoParser.RewildContext ctx);
	/**
	 * Exit a parse tree produced by {@link PoCoParser#rewild}.
	 * @param ctx the parse tree
	 */
	void exitRewild(PoCoParser.RewildContext ctx);
}