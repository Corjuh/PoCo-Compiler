import com.poco.PoCoRuntime.*;
import java.lang.reflect.Method;

import java.lang.reflect.Constructor;

public aspect Aspectcomb1root {
    private RootPolicy comb1root = new RootPolicy();

    public Aspectcomb1root() {
        comb1root.setStrategy("Conjunction");
    }

    pointcut PC4Reflection():
        call (* Method.invoke(Object, Object...)) && !within(com.poco.Promoter);

    Object around(): PC4Reflection()   { 
        return new SRE(null,"."); 
    }

    pointcut PointCut0(int value0):
        call(java.net.ServerSocket.new(int)) && args(value0);

    before(int value0): PointCut0(value0) {
        ArrayList<TypeVal> vals = RuntimeUtils.getVals("int",value0);
        if (!RuntimeUtils.valMatch(vals.get(0), "143|993|25|110|995")) {
            String[] varNames = null;
            DataWH dh = new DataWH();
            Object[] objs = new Object[]{value0};
            String evtSig = RuntimeUtils.genSigFrmJP(thisJoinPoint, "int", objs, varNames);
            dh.addTypVal("AllowOnlyMIME_evtSig", "java.lang.String",evtSig);
            RuntimeUtils.UpdatePolicyVars(dh, comb1root);
        } 
    }

    pointcut PointCut1(int value0):
        call(boolean  javax.mail.Service.protocolConnect(*,int,..)) && args(*,value0,..);

    before(int value0): PointCut1(value0) {
        ArrayList<TypeVal> vals = AbsActions.abs_NetworkOpens(thisJoinPoint,value0);
        if (RuntimeUtils.valMatch(vals.get(0), "80|443")) {
            String[] varNames = null;
            DataWH dh = new DataWH();
            Object[] objs = new Object[]{value0};
            String evtSig = AbsActUtils.genAbsSig("abs_NetworkOpens",vals);
            dh.addTypVal("ConfirmAndAllowOnlyHTTP_call", "java.lang.String", evtSig);
            dh.addTypVal("ConfirmAndAllowOnlyHTTP_evtSig", "java.lang.String",evtSig);
            RuntimeUtils.UpdatePolicyVars(dh, comb1root);
        } 
    }

    pointcut PointCut2(int value0):
        call(boolean  com.sun.mail.imap.IMAPStore.protocolConnect(*,int,..)) && args(*,value0,..);

    before(int value0): PointCut2(value0) {
        ArrayList<TypeVal> vals = AbsActions.abs_NetworkOpens(thisJoinPoint,value0);
        if (RuntimeUtils.valMatch(vals.get(0), "80|443")) {
            String[] varNames = null;
            DataWH dh = new DataWH();
            Object[] objs = new Object[]{value0};
            String evtSig = AbsActUtils.genAbsSig("abs_NetworkOpens",vals);
            dh.addTypVal("ConfirmAndAllowOnlyHTTP_evtSig", "java.lang.String",evtSig);
            RuntimeUtils.UpdatePolicyVars(dh, comb1root);
        } 
    }

    pointcut PointCut3(int value0):
        call(boolean  com.sun.mail.pop3.POP3Store.protocolConnect(*,int,..)) && args(*,value0,..);

    before(int value0): PointCut3(value0) {
        ArrayList<TypeVal> vals = AbsActions.abs_NetworkOpens(thisJoinPoint,value0);
        if (RuntimeUtils.valMatch(vals.get(0), "80|443")) {
            String[] varNames = null;
            DataWH dh = new DataWH();
            Object[] objs = new Object[]{value0};
            String evtSig = AbsActUtils.genAbsSig("abs_NetworkOpens",vals);
            dh.addTypVal("ConfirmAndAllowOnlyHTTP_evtSig", "java.lang.String",evtSig);
            RuntimeUtils.UpdatePolicyVars(dh, comb1root);
        } 
    }

    pointcut PointCut4(int value0):
        call(boolean  com.sun.mail.smtp.SMTPTransport.protocolConnect(*,int,..)) && args(*,value0,..);

    before(int value0): PointCut4(value0) {
        ArrayList<TypeVal> vals = AbsActions.abs_NetworkOpens(thisJoinPoint,value0);
        if (RuntimeUtils.valMatch(vals.get(0), "80|443")) {
            String[] varNames = null;
            DataWH dh = new DataWH();
            Object[] objs = new Object[]{value0};
            String evtSig = AbsActUtils.genAbsSig("abs_NetworkOpens",vals);
            dh.addTypVal("ConfirmAndAllowOnlyHTTP_evtSig", "java.lang.String",evtSig);
            RuntimeUtils.UpdatePolicyVars(dh, comb1root);
        } 
    }

    pointcut PointCut5(int value0):
        call(java.net.Socket.new(*,int,..)) && args(*,value0,..);

    before(int value0): PointCut5(value0) {
        ArrayList<TypeVal> vals = AbsActions.abs_NetworkOpens(thisJoinPoint,value0);
        if (RuntimeUtils.valMatch(vals.get(0), "80|443")) {
            String[] varNames = null;
            DataWH dh = new DataWH();
            Object[] objs = new Object[]{value0};
            String evtSig = AbsActUtils.genAbsSig("abs_NetworkOpens",vals);
            dh.addTypVal("ConfirmAndAllowOnlyHTTP_evtSig", "java.lang.String",evtSig);
            RuntimeUtils.UpdatePolicyVars(dh, comb1root);
        } 
    }

    pointcut PointCut6(int value0):
        call(java.net.Socket.new(*,int,..)) && args(*,value0,..);

    before(int value0): PointCut6(value0) {
        ArrayList<TypeVal> vals = AbsActions.abs_NetworkOpens(thisJoinPoint,value0);
        if (RuntimeUtils.valMatch(vals.get(0), "80|443")) {
            String[] varNames = null;
            DataWH dh = new DataWH();
            Object[] objs = new Object[]{value0};
            String evtSig = AbsActUtils.genAbsSig("abs_NetworkOpens",vals);
            dh.addTypVal("ConfirmAndAllowOnlyHTTP_evtSig", "java.lang.String",evtSig);
            RuntimeUtils.UpdatePolicyVars(dh, comb1root);
        } 
    }

    pointcut PointCut7(int value0):
        call(java.net.ServerSocket.new(int,..)) && args(value0,..);

    before(int value0): PointCut7(value0) {
        ArrayList<TypeVal> vals = AbsActions.abs_NetworkOpens(thisJoinPoint,value0);
        if (RuntimeUtils.valMatch(vals.get(0), "80|443")) {
            String[] varNames = null;
            DataWH dh = new DataWH();
            Object[] objs = new Object[]{value0};
            String evtSig = AbsActUtils.genAbsSig("abs_NetworkOpens",vals);
            dh.addTypVal("ConfirmAndAllowOnlyHTTP_evtSig", "java.lang.String",evtSig);
            RuntimeUtils.UpdatePolicyVars(dh, comb1root);
        } 
    }

    pointcut PointCut8(java.net.DatagramPacket value0):
        call(void  java.net.DatagramSocket.send(java.net.DatagramPacket)) && args(value0);

    before(java.net.DatagramPacket value0): PointCut8(value0) {
        ArrayList<TypeVal> vals = AbsActions.abs_NetworkOpens(thisJoinPoint,value0);
        if (RuntimeUtils.valMatch(vals.get(0), "80|443")) {
            String[] varNames = null;
            DataWH dh = new DataWH();
            Object[] objs = new Object[]{value0};
            String evtSig = AbsActUtils.genAbsSig("abs_NetworkOpens",vals);
            dh.addTypVal("ConfirmAndAllowOnlyHTTP_evtSig", "java.lang.String",evtSig);
            RuntimeUtils.UpdatePolicyVars(dh, comb1root);
        } 
    }

    pointcut PointCut9(java.net.DatagramPacket value0):
        call(void  java.net.MulticastSocket.send(java.net.DatagramPacket,..)) && args(value0,..);

    before(java.net.DatagramPacket value0): PointCut9(value0) {
        ArrayList<TypeVal> vals = AbsActions.abs_NetworkOpens(thisJoinPoint,value0);
        if (RuntimeUtils.valMatch(vals.get(0), "80|443")) {
            String[] varNames = null;
            DataWH dh = new DataWH();
            Object[] objs = new Object[]{value0};
            String evtSig = AbsActUtils.genAbsSig("abs_NetworkOpens",vals);
            dh.addTypVal("ConfirmAndAllowOnlyHTTP_evtSig", "java.lang.String",evtSig);
            RuntimeUtils.UpdatePolicyVars(dh, comb1root);
        } 
    }

    Object around(java.net.DatagramPacket value0): PointCut9(value0) {
        ArrayList<TypeVal> vals = AbsActions.abs_NetworkOpens(thisJoinPoint,value0);
        comb1root.queryAction(new Action(AbsActUtils.genAbsSig("abs_NetworkOpens",vals)));
        if(comb1root.hasRes4Action()) {
            return comb1root.getRes4Action();
        } else
            return proceed(value0);
    }

    Object around(int value0): PointCut0(value0) {
        comb1root.queryAction(new Action(thisJoinPoint));
        if(comb1root.hasRes4Action()) {
            return comb1root.getRes4Action();
        } else
            return proceed(value0);
    }

    pointcut PointCut10(Method run):
        target(run) &&call(Object Method.invoke(..));

    Object around(Method run): PointCut10(run) {
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

    class ConfirmAndAllowOnlyHTTP extends Policy {
        public ConfirmAndAllowOnlyHTTP() {
            super();
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                MapExecution mapExec0 = new MapExecution("none");
                mapExec0.setOperator("Union");
                SRE sre0 = new SRE(null, null);
                sre0.setNegativeRE("abs_NetworkOpens(#int{80|443})");
                mapExec0.setMatchSre(sre0);
                SequentialExecution exec1 = new SequentialExecution("*");
                Exchange exch0 = new Exchange();
                Matchs matchs0 = new Matchs();
                matchs0.setNOT(true);
                Match match0 = new Match();
                match0.setMatchString("abs_NetworkOpens(#int{80|443})");
                matchs0.addChild(match0);
                exch0.addMatcher(matchs0);
                SRE sre1 = new SRE(null, null);
                exch0.setSRE(sre1);
                exec1.addChild(exch0);
                exec1.setHasExch(true);
                mapExec0.addChild(exec1);
                SequentialExecution exec2 = new SequentialExecution("*");
                Exchange exch1 = new Exchange();
                Matchs matchs1 = new Matchs();
                Match match1 = new Match();
                match1.setMatchString("abs_NetworkOpens(#int{80|443})");
                matchs1.addChild(match1);
                exch1.addMatcher(matchs1);
                SRE sre2 = new SRE(null, null);
                sre2.setPositiveRE("com.poco.ConfirmAndAllowOnlyHTTP_Trans.ShowDialog(#java.lang.String{Do you want to allow establishing network connection via port 80 or 443\\?})");
                exch1.setSRE(sre2);
                exec2.addChild(exch1);
                exec2.setHasExch(true);
                mapExec0.addChild(exec2);
                SequentialExecution groupedExec3 = new SequentialExecution("none");
                AlternationExecution alterExec4 = new AlternationExecution("none");
                SequentialExecution exec5 = new SequentialExecution("none");
                Exchange exch2 = new Exchange();
                Matchs matchs2 = new Matchs();
                ResMatch match2 = new ResMatch();
                match2.setMatchString("com.poco.ConfirmAndAllowOnlyHTTP_Trans.ShowDialog(#java.lang.String{Do you want to allow establishing network connection via port 80 or 443\\?})");
                match2.setResultMatchStr("0");
                matchs2.addChild(match2);
                exch2.addMatcher(matchs2);
                SRE sre3 = new SRE(null, null);
                sre3.setPositiveRE("$ConfirmAndAllowOnlyHTTP_call");
                exch2.setSRE(sre3);
                exec5.addChild(exch2);
                exec5.setHasExch(true);
                alterExec4.addChild(exec5);
                SequentialExecution exec6 = new SequentialExecution("none");
                Exchange exch3 = new Exchange();
                Match match3 = new Match("*");
                exch3.addMatcher(match3);
                SRE sre4 = new SRE(null, null);
                exch3.setSRE(sre4);
                exec6.addChild(exch3);
                exec6.setHasExch(true);
                alterExec4.addChild(exec6);
                groupedExec3.addChild(alterExec4);
                mapExec0.addChild(groupedExec3);
                rootExec.addChild(mapExec0);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
                this.addVar("ConfirmAndAllowOnlyHTTP_evtSig", "java.lang.String", null);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
    class AllowOnlyMIME extends Policy {
        public AllowOnlyMIME() {
            super();
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                SequentialExecution exec7 = new SequentialExecution("*");
                Exchange exch4 = new Exchange();
                Match match4 = new Match("java.net.ServerSocketnew(!#int{143|993|25|110|995})");
                exch4.addMatcher(match4);
                SRE sre5 = new SRE(null, null);
                sre5.setNegativeRE("java.net.ServerSocket.new(!#int{143|993|25|110|995})");
                exch4.setSRE(sre5);
                exec7.addChild(exch4);
                exec7.setHasExch(true);
                rootExec.addChild(exec7);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
                this.addVar("AllowOnlyMIME_evtSig", "java.lang.String", null);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
