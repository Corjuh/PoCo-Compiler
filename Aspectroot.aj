import com.poco.PoCoRuntime.*;
import java.lang.reflect.Method;

import java.lang.reflect.Constructor;

public aspect Aspectroot {
    private RootPolicy root = new RootPolicy();

    public Aspectroot() {
        DataWH.dataVal.put("DenyEmails_msg",new TypeVal("java.lang.String",""));
        root.setStrategy("Union");
        root.addChild(new  DenyEmails());
        root.addChild(new  NoOpenPorts());
    }

    pointcut PC4Reflection():
        call (* Method.invoke(Object, Object...)) && !within(com.poco.Promoter);

    Object around(): PC4Reflection()   { 
        return new SRE(null,"."); 
    }

    pointcut PointCut0(javax.mail.Message value0):
        call(void javax.mail.Transport.send(javax.mail.Message)) && args(value0);

    Object around(javax.mail.Message value0): PointCut0(value0) {
        DataWH.updateTyeVal("DenyEmails_msg", "javax.mail.Message", value0);
        DataWH.address2ObjVal.put(Integer.toString(System.identityHashCode(value0)),value0);
        root.queryAction(new Action(thisJoinPoint));
        if(root.hasRes4Action()) {
            return root.getRes4Action();
        } else
            return proceed(value0);
    }

    pointcut PointCut1(int value0):
        call(java.net.ServerSocket.new(int)) && args(value0);

    Object around(int value0): PointCut1(value0) {
        if (!RuntimeUtils.strValMatch(new Integer(value0).toString(), "143|993|25|110|995")) {
            String[] varNames = null;
            String arg0 = RuntimeUtils.genValueofStr(value0);
            Object[] objs = new Object[]{value0};
            root.queryAction(new Action(thisJoinPoint, "int", objs, varNames));
            if(root.hasRes4Action()) {
                return root.getRes4Action();
            } else
                return proceed(value0);
        } else
            return proceed(value0);
    }

    class DenyEmails extends Policy {
        public DenyEmails() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                SequentialExecution groupedExec0 = new SequentialExecution("*");
                SequentialExecution exec1 = new SequentialExecution("*");
                Exchange exch0 = new Exchange();
                Matchs matchs0 = new Matchs();
                matchs0.setNOT(true);
                Match match0 = new Match();
                match0.setMatchString("void javax.mail.Transport.send(*)");
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
                match1.setMatchString("void javax.mail.Transport.send($DenyEmails_msg)");
                matchs1.addChild(match1);
                exch1.addMatcher(matchs1);
                BopSRE bopSRE1 = new BopSRE("Union",null, null);
                SRE sre2 = new SRE(null, null);
                sre2.setPositiveRE("* com.poco.MailUtil.logMessage($DenyEmails_msg)");
                bopSRE1.setSre1(sre2);
                SRE sre3 = new SRE(null, null);
                sre3.setNegativeRE("void javax.mail.Transport.send(*)");
                bopSRE1.setSre2(sre3);
                exch1.setSRE(bopSRE1);
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
    class NoOpenPorts extends Policy {
        public NoOpenPorts() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                SequentialExecution exec3 = new SequentialExecution("*");
                Exchange exch2 = new Exchange();
                Match match2 = new Match("java.net.ServerSocket.new(!#int{143|993|25|110|995})");
                exch2.addMatcher(match2);
                SRE sre4 = new SRE(null, null);
                sre4.setNegativeRE("java.net.ServerSocket.new(!#int{143|993|25|110|995})");
                exch2.setSRE(sre4);
                exec3.addChild(exch2);
                exec3.setHasExch(true);
                rootExec.addChild(exec3);
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
