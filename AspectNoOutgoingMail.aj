import com.poco.PoCoRuntime.*;
import java.lang.reflect.Method;

public aspect AspectNoOutgoingMail {
    private DummyRootPolicy root = new DummyRootPolicy( new NoOutgoingMail() );

    public AspectNoOutgoingMail() {
        DataWH.dataVal.put("NoOutgoingMail_msg",new TypeVal("javax.mail.Message",null));
    }

    pointcut PointCut0(javax.mail.Message value0):
        call(void javax.mail.Transport.send(javax.mail.Message)) && args(value0);

    Object around(javax.mail.Message value0): PointCut0(value0) {
        if (RuntimeUtils.StringMatch(String.valueOf(value0), DataWH.dataVal.get("NoOutgoingMail_msg"))) {
            DataWH.updateValue("NoOutgoingMail_msg", value0);
            root.queryAction(new Event(thisJoinPoint));
            return proceed(value0);
        } else
            return proceed(value0);
    }

    class NoOutgoingMail extends Policy {
        public NoOutgoingMail() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                SequentialExecution groupedExec0 = new SequentialExecution("*");
                SequentialExecution exec1 = new SequentialExecution("*");
                Exchange exch0 = new Exchange();
                Matchs matchs0 = new Matchs();
                matchs0.setNOT(true);
                Match match0 = new Match();
                match0.setMatchString("void javax.mail.Transport.send(.*)");
                matchs0.addChild(match0);
                exch0.addMatcher(matchs0);
                SRE sre0 = new SRE(null, null);
                exch0.setSRE(sre0);
                exec1.addChild(exch0);
                exec1.setHasExch(true);
                groupedExec0.addChild(exec1);
                SequentialExecution exec2 = new SequentialExecution("none");
                Exchange exch1 = new Exchange();
                Matchs matchs1 = new Matchs();
                Match match1 = new Match();
                match1.setWildcard(true);
                match1.setMatchString("void javax.mail.Transport.send(javax.mail.Message)");
                matchs1.addChild(match1);
                exch1.addMatcher(matchs1);
                BopSRE bopSRE1 = new BopSRE("Union",null, null);
                SRE sre2 = new SRE(null, null);
                sre2.setPositiveRE("com.poco.MailUtil.logMessage(#javax.mail.Message{$NoOutgoingMail_msg})");
                bopSRE1.setSRE1(sre2);
                SRE sre3 = new SRE(null, null);
                sre3.setNegativeRE("void javax.mail.Transport.send(.*)");
                bopSRE1.setSRE2(sre3);
                exec2.addChild(exch1);
                exec2.setHasExch(true);
                groupedExec0.addChild(exec2);
                rootExec.addChild(groupedExec0);
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
