import com.poco.PoCoRuntime.*;
import java.lang.reflect.Method;

import java.lang.reflect.Constructor;

public aspect AspectRoot {
    private RootPolicy null = new RootPolicy();

    public AspectRoot() {
        DataWH.dataVal.put("Attachments_call",new TypeVal("java.lang.String",""));
    root.addChild( new Attachments() );
    }

    pointcut PC4Reflection():
        call (* Method.invoke(Object, Object...)) && !within(com.poco.Promoter);

    Object around(): PC4Reflection()   { 
        return new SRE(null,"."); 
    }

    pointcut PointCut0(java.lang.String value0):
        call(java.io.FileWriter.new(java.lang.String,boolean)) && args(value0, value1);

    Object around(java.lang.String value0): PointCut0(value0) {
        if (RuntimeUtils.valueMatch(value0, "*.exe|*.vbs|*.hta|*.mdb|*.bad")) {
            String[] varNames = null;
            DataWH.updateTyeVal("Attachments_call", "java.lang.String", "java.io.FileWriter.new(#java.lang.String{value0},boolean)");
            Object[] objs = new Object[]{value0};
            root.queryAction(new Action(thisJoinPoint, "java.lang.String,boolean", objs, varNames));
            if(root.hasRes4Action()) {
                return root.getRes4Action();
            } else
                return proceed(value0);
        } else
            return proceed(value0);
    }

    pointcut PointCut1(java.lang.String value0):
        call(java.io.FileWriter.new(java.lang.String)) && args(value0);

    Object around(java.lang.String value0): PointCut1(value0) {
        if (RuntimeUtils.valueMatch(value0, "*.exe|*.vbs|*.hta|*.mdb|*.bad")) {
            String[] varNames = null;
            DataWH.updateTyeVal("Attachments_call", "java.lang.String", "java.io.FileWriter.new(#java.lang.String{value0})");
            Object[] objs = new Object[]{value0};
            root.queryAction(new Action(thisJoinPoint, "java.lang.String", objs, varNames));
            if(root.hasRes4Action()) {
                return root.getRes4Action();
            } else
                return proceed(value0);
        } else
            return proceed(value0);
    }

    pointcut PointCut2(Method run):
        target(run) &&call(Object Method.invoke(..));

    Object around(Method run): PointCut2(run) {
        if (RuntimeUtils.matchingStack(root.promotedEvents,run)) {
            root.promotedEvents.pop();
            Object ret = proceed(run);
            String retTyp = RuntimeUtils.trimClassName(ret.getClass().toString());
            PromotedResult promRes = new PromotedResult(thisJoinPoint,run,ret);
            root.queryAction(promRes);
            return ret;
        }
        else
            return proceed(run);
    }

    class Attachments extends Policy {
        public Attachments() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                MapExecution mapExec0 = new MapExecution("*");
                mapExec0.setOperator("Union");
                SRE sre0 = new SRE(null, null);
                sre0.setNegativeRE("java.io.FileWriter.new(#java.lang.String{*.exe|*.vbs|*.hta|*.mdb|*.bad})|java.io.FileWriter.new(#java.lang.String{*.exe|*.vbs|*.hta|*.mdb|*.bad},boolean)");
                mapExec0.setMatchSre(sre0);
                SequentialExecution exec1 = new SequentialExecution("*");
                Exchange exch0 = new Exchange();
                Matchs matchs0 = new Matchs();
                matchs0.setNOT(true);
                Match match0 = new Match();
                match0.setMatchString("java.io.FileWriter.new(#java.lang.String{*.exe|*.vbs|*.hta|*.mdb|*.bad})|java.io.FileWriter.new(java.lang.String,boolean)");
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
                match1.setMatchString("java.io.FileWriter.new(#java.lang.String{*.exe|*.vbs|*.hta|*.mdb|*.bad})|java.io.FileWriter.new(java.lang.String,boolean)");
                matchs1.addChild(match1);
                exch1.addMatcher(matchs1);
                SRE sre2 = new SRE(null, null);
                sre2.setPositiveRE("com.poco.RuntimeDemo.ShowDialog(#java.lang.String{Allowing a dangerous file is creating via: $Attachments_call ?})");
                exch1.setSRE(sre2);
                exec2.addChild(exch1);
                exec2.setHasExch(true);
                mapExec0.addChild(exec2);
                SequentialExecution exec3 = new SequentialExecution("*");
                Exchange exch2 = new Exchange();
                Matchs matchs2 = new Matchs();
                matchs2.setNOT(true);
                ResMatch match2 = new ResMatch();
                match2.setMatchString("com.poco.RuntimeDemo.ShowDialog(java.lang.String)");
                match2.setWildcard(true);
                matchs2.addChild(match2);
                exch2.addMatcher(matchs2);
                SRE sre3 = new SRE(null, null);
                sre3.setPositiveRE("com.poco.RuntimeDemo.ShowDialog(#java.lang.String{Allowing a dangerous file is creating via: $Attachments_call ?})");
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
                match3.setMatchString("com.poco.RuntimeDemo.ShowDialog(java.lang.String)");
                match3.setResultMatchStr("0");
                matchs3.addChild(match3);
                exch3.addMatcher(matchs3);
                SRE sre4 = new SRE(null, null);
                sre4.setPositiveRE("$Attachments_call");
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
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
