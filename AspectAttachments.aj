import com.poco.PoCoRuntime.*;
import java.lang.reflect.Method;

public aspect AspectAttachments {
    private DummyRootPolicy root = new DummyRootPolicy( new Attachments() );

    public AspectAttachments() {
        DataWH.dataVal.put("Attachments_call",new TypeVal("java.lang.String","java.io.FileWriter.new($ext())| java.io.FileWriter.new($ext(),boolean)"));
    }

    pointcut PointCut0(java.lang.String value0):
        call(java.io.FileWriter.new(java.lang.String,..)) && args(value0,*);

    Object around(java.lang.String value0): PointCut0(value0) {
        if (RuntimeUtils.StringMatch(value0, ".exe|.vbs|.hta|.mdb|.bad")) {
            root.queryAction(new Event(thisJoinPoint));
            return proceed(value0);
        } else
            return proceed(value0);
    }

    pointcut PointCut1(java.lang.String value0):
        call(java.io.FileWriter.new(java.lang.String)) && args(value0);

    Object around(java.lang.String value0): PointCut1(value0) {
        if (RuntimeUtils.StringMatch(value0, ".exe|.vbs|.hta|.mdb|.bad")) {
            DataWH.updateValue("Attachments_call","java.io.FileWriter.new(java.lang.String)");
            root.queryAction(new Event(thisJoinPoint));
            return proceed(value0);
        } else
            return proceed(value0);
    }

    pointcut PointCut2(Method run):
        target(run) &&call(Object Method.invoke(..));

    Object around(Method run): PointCut2(run) {
        String className = RuntimeUtils.trimClassName(run.getDeclaringClass().toString());
        className =RuntimeUtils.concatClsMethod(className, run.getName());
        if (RuntimeUtils.matchingStack(root.promotedEvents,className)) {
            root.promotedEvents.pop();
            Object ret = proceed(run);
            PromotedEvent event = new PromotedEvent(thisJoinPoint,RuntimeUtils.getInvokeMethoSig(run),"Result",ret);
            root.queryAction(event);
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
                sre0.setNegativeRE("java.io.FileWriter.new(#java.lang.String{.exe|.vbs|.hta|.mdb|.bad})|java.io.FileWriter.new(#java.lang.String{.exe|.vbs|.hta|.mdb|.bad},boolean)");
                mapExec0.setMatchSre(sre0);
                SequentialExecution exec1 = new SequentialExecution("*");
                Exchange exch0 = new Exchange();
                Matchs matchs0 = new Matchs();
                matchs0.setNOT(true);
                Match match0 = new Match();
                match0.setMatchString("java.io.FileWriter.new(#java.lang.String{.exe|.vbs|.hta|.mdb|.bad})|java.io.FileWriter.new(java.lang.String,boolean)");
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
                match1.setMatchString("java.io.FileWriter.new(#java.lang.String{.exe|.vbs|.hta|.mdb|.bad})|java.io.FileWriter.new(java.lang.String,boolean)");
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
