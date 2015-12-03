import com.poco.PoCoRuntime.*;
import java.lang.reflect.Method;

import java.lang.reflect.Constructor;

public aspect Aspectcomb1root {
    private RootPolicy comb1root = new RootPolicy();

    public Aspectcomb1root() {
        comb1root.addChild(new ClassLoaders());
    }

    pointcut PC4Reflection():
        call (* Method.invoke(Object, Object...)) && !within(com.poco.Promoter);

    Object around(): PC4Reflection()   { 
        return new SRE(null,"."); 
    }

    pointcut PointCut0():
        call(java.lang.ClassLoader+.new(..));

    before(): PointCut0() {
        ArrayList<TypeVal> vals = new Abs_LoadDangerClass().handleActions(thisJoinPoint,null);
        if (!RuntimeUtils.valMatch(vals.get(0),"null")) {
            String[] varNames = null;
            DataWH dh = new DataWH();
            String evtSig = AbsActUtils.genAbsSig("Abs_LoadDangerClass",vals);
            dh.addTypVal("ClassLoaders_call", "java.lang.String", evtSig);
            RuntimeUtils.UpdatePolicyVars(dh, comb1root);
        } 
    }

    Object around(): PointCut0() {
        comb1root.queryAction(new Action("Abs_LoadDangerClass()"));
        if(comb1root.hasRes4Action()) {
            return comb1root.getRes4Action();
        } else
            return proceed();
    }

    pointcut PointCut1(Method run):
        target(run) &&call(Object Method.invoke(..));

    Object around(Method run): PointCut1(run) {
        if (RuntimeUtils.matchingStack(comb1root.promotedEvents,run)) {
            comb1root.promotedEvents.pop();
            Object ret = proceed(run);
            String retTyp = RuntimeUtils.trimClassName(ret.getClass().toString());
            PromotedResult promRes = new PromotedResult(run,ret);
            comb1root.queryAction(promRes);
            return ret;
        }
        else
            return proceed(run);
    }

    class ClassLoaders extends Policy {
        public ClassLoaders() {
            super();
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                MapExecution mapExec0 = new MapExecution("*");
                mapExec0.setOperator("Union");
                SRE sre0 = new SRE(null, null);
                sre0.setNegativeRE("Abs_LoadDangerClass(!#java.lang.String{null})");
                mapExec0.setMatchSre(sre0);
                SequentialExecution exec1 = new SequentialExecution("*");
                Exchange exch0 = new Exchange();
                Matchs matchs0 = new Matchs();
                matchs0.setNOT(true);
                Match match0 = new Match();
                match0.setMatchString("Abs_LoadDangerClass(!#java.lang.String{null})");
                matchs0.addChild(match0);
                exch0.addMatcher(matchs0);
                SRE sre1 = new SRE(null, null);
                exch0.setSRE(sre1);
                exec1.addChild(exch0);
                exec1.setHasExch(true);
                mapExec0.addChild(exec1);
                SequentialExecution exec2 = new SequentialExecution("none");
                Exchange exch1 = new Exchange();
                Matchs matchs1 = new Matchs();
                Match match1 = new Match();
                match1.setMatchString("Abs_LoadDangerClass(!#java.lang.String{null})");
                matchs1.addChild(match1);
                exch1.addMatcher(matchs1);
                SRE sre2 = new SRE(null, null);
                sre2.setPositiveRE("* com.poco.ClassLoaders_Trans.confirmDialog(#java.lang.String{$ClassLoaders_stacktrace})");
                exch1.setSRE(sre2);
                exec2.addChild(exch1);
                exec2.setHasExch(true);
                mapExec0.addChild(exec2);
                SequentialExecution exec3 = new SequentialExecution("*");
                Exchange exch2 = new Exchange();
                Matchs matchs2 = new Matchs();
                matchs2.setNOT(true);
                ResMatch match2 = new ResMatch();
                match2.setMatchString("* com.poco.ClassLoaders_Trans.confirmDialog(#java.lang.String{$ClassLoaders_stacktrace})");
                match2.setResultMatchStr("");
                matchs2.addChild(match2);
                exch2.addMatcher(matchs2);
                SRE sre3 = new SRE(null, null);
                sre3.setPositiveRE("* com.poco.ClassLoaders_Trans.confirmDialog(#java.lang.String{$ClassLoaders_stacktrace})");
                exch2.setSRE(sre3);
                exec3.addChild(exch2);
                exec3.setHasExch(true);
                mapExec0.addChild(exec3);
                SequentialExecution groupedExec4 = new SequentialExecution("none");
                AlternationExecution alterExec5 = new AlternationExecution("none");
                SequentialExecution exec6 = new SequentialExecution("none");
                Exchange exch3 = new Exchange();
                Matchs matchs3 = new Matchs();
                ResMatch match3 = new ResMatch();
                match3.setMatchString("* com.poco.ClassLoaders_Trans.confirmDialog(#java.lang.String{$ClassLoaders_stacktrace})");
                match3.setResultMatchStr("0");
                matchs3.addChild(match3);
                exch3.addMatcher(matchs3);
                SRE sre4 = new SRE(null, null);
                sre4.setPositiveRE("$ClassLoaders_call");
                exch3.setSRE(sre4);
                exec6.addChild(exch3);
                exec6.setHasExch(true);
                alterExec5.addChild(exec6);
                SequentialExecution exec7 = new SequentialExecution("none");
                Exchange exch4 = new Exchange();
                Match match4 = new Match("*");
                exch4.addMatcher(match4);
                SRE sre5 = new SRE(null, null);
                exch4.setSRE(sre5);
                exec7.addChild(exch4);
                exec7.setHasExch(true);
                alterExec5.addChild(exec7);
                groupedExec4.addChild(alterExec5);
                mapExec0.addChild(groupedExec4);
                rootExec.addChild(mapExec0);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
                this.addVar("ClassLoaders_call", "java.lang.String", null);
                this.addVar("ClassLoaders_stacktrace", "java.lang.String", null);
                this.addVar("ClassLoaders_evtSig", "java.lang.String", null);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
