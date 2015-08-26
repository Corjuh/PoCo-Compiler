import com.poco.PoCoRuntime.*;
import java.lang.reflect.Method;

import java.lang.reflect.Constructor;

public aspect AspectRoot {
    private RootPolicy null = new RootPolicy();

    public AspectRoot() {
        DataWH.dataVal.put("OutgoingMail_msg",new TypeVal("java.lang.String",""));
        DataWH.dataVal.put("OutgoingMail_ContactInfo",new TypeVal("java.lang.String",""));
    root.addChild( new OutgoingMail() );
    }

    pointcut PC4Reflection():
        call (* Method.invoke(Object, Object...)) && !within(com.poco.Promoter);

    Object around(): PC4Reflection()   { 
        return new SRE(null,"."); 
    }

    pointcut PointCut0(javax.mail.Message value0):
        call(void javax.mail.Transport.send(..,javax.mail.Message)) && args(*,value0);

    Object around(javax.mail.Message value0): PointCut0(value0) {
        if (RuntimeUtils.StringMatch(String.valueOf(value0), "$OutgoingMail_msg")) {
            DataWH.updateTyeVal("OutgoingMail_msg", "javax.mail.Message", value0);
            root.queryAction(new Action(thisJoinPoint));
            Object ret = proceed(value0);
            Result result = new Result(thisJoinPoint, ret);
            root.queryAction(result);
            return result.getResult();
        }
        else
            return proceed(value0);
    }

    pointcut PointCut1(Method run):
        target(run) &&call(Object Method.invoke(..));

    Object around(Method run): PointCut1(run) {
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

    class OutgoingMail extends Policy {
        public OutgoingMail() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                MapExecution mapExec0 = new MapExecution("*");
                mapExec0.setOperator("Union");
                SRE sre0 = new SRE(null, null);
                sre0.setNegativeRE("void javax.mail.Transport.send($OutgoingMail_msg)");
                mapExec0.setMatchSre(sre0);
                SequentialExecution exec1 = new SequentialExecution("*");
                Exchange exch0 = new Exchange();
                Matchs matchs0 = new Matchs();
                matchs0.setNOT(true);
                Match match0 = new Match();
                match0.setWildcard(true);
                match0.setMatchString("void javax.mail.Transport.send($OutgoingMail_msg)");
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
                match1.setWildcard(true);
                match1.setMatchString("void javax.mail.Transport.send($OutgoingMail_msg)");
                matchs1.addChild(match1);
                exch1.addMatcher(matchs1);
                SRE sre2 = new SRE(null, null);
                sre2.setPositiveRE("com.poco.MailUtil.logMessage($OutgoingMail_msg)");
                exch1.setSRE(sre2);
                exec2.addChild(exch1);
                exec2.setHasExch(true);
                mapExec0.addChild(exec2);
                SequentialExecution exec3 = new SequentialExecution("*");
                Exchange exch2 = new Exchange();
                Matchs matchs2 = new Matchs();
                matchs2.setNOT(true);
                Matchs matchs3 = new Matchs();
                matchs3.setOR(true);
                ResMatch match2 = new ResMatch();
                match2.setMatchString("com.poco.MailUtil.logMessage($OutgoingMail_msg)");
                match2.setWildcard(true);
                matchs3.addChild(match2);
                ResMatch match3 = new ResMatch();
                match3.setMatchString("void javax.mail.Transport.send($OutgoingMail_msg)");
                match3.setWildcard(true);
                matchs3.addChild(match3);
                matchs2.addChild(matchs3);
                exch2.addMatcher(matchs2);
                SRE sre3 = new SRE(null, null);
                sre3.setPositiveRE("com.poco.MailUtil.logMessage($OutgoingMail_msg)");
                exch2.setSRE(sre3);
                exec3.addChild(exch2);
                exec3.setHasExch(true);
                mapExec0.addChild(exec3);
                SequentialExecution groupedExec4 = new SequentialExecution("none");
                AlternationExecution alterExec5 = new AlternationExecution("none");
                SequentialExecution exec6 = new SequentialExecution("none");
                Exchange exch3 = new Exchange();
                Matchs matchs4 = new Matchs();
                ResMatch match4 = new ResMatch();
                match4.setMatchString("void javax.mail.Transport.send($OutgoingMail_msg)");
                match4.setWildcard(true);
                matchs4.addChild(match4);
                exch3.addMatcher(matchs4);
                SRE sre4 = new SRE(null, null);
                exch3.setSRE(sre4);
                exec6.addChild(exch3);
                exec6.setHasExch(true);
                alterExec5.addChild(exec6);
                SequentialExecution exec7 = new SequentialExecution("none");
                Exchange exch4 = new Exchange();
                Matchs matchs5 = new Matchs();
                ResMatch match5 = new ResMatch();
                match5.setMatchString("com.poco.MailUtil.logMessage($OutgoingMail_msg)");
                match5.setWildcard(true);
                matchs5.addChild(match5);
                exch4.addMatcher(matchs5);
                SRE sre5 = new SRE(null, null);
                sre5.setPositiveRE("com.poco.MailUtil.confirm($OutgoingMail_msg)");
                exch4.setSRE(sre5);
                exec7.addChild(exch4);
                exec7.setHasExch(true);
                alterExec5.addChild(exec7);
                groupedExec4.addChild(alterExec5);
                mapExec0.addChild(groupedExec4);
                SequentialExecution exec8 = new SequentialExecution("*");
                Exchange exch5 = new Exchange();
                Matchs matchs6 = new Matchs();
                matchs6.setNOT(true);
                Matchs matchs7 = new Matchs();
                matchs7.setOR(true);
                ResMatch match6 = new ResMatch();
                match6.setMatchString("com.poco.MailUtil.confirm($OutgoingMail_msg)");
                match6.setWildcard(true);
                matchs7.addChild(match6);
                ResMatch match7 = new ResMatch();
                match7.setMatchString("void javax.mail.Transport.send($OutgoingMail_msg)");
                match7.setWildcard(true);
                matchs7.addChild(match7);
                matchs6.addChild(matchs7);
                exch5.addMatcher(matchs6);
                SRE sre6 = new SRE(null, null);
                sre6.setPositiveRE("com.poco.MailUtil.confirm($OutgoingMail_msg)");
                exch5.setSRE(sre6);
                exec8.addChild(exch5);
                exec8.setHasExch(true);
                mapExec0.addChild(exec8);
                SequentialExecution groupedExec9 = new SequentialExecution("none");
                AlternationExecution alterExec10 = new AlternationExecution("none");
                SequentialExecution exec11 = new SequentialExecution("none");
                Exchange exch6 = new Exchange();
                Matchs matchs8 = new Matchs();
                ResMatch match8 = new ResMatch();
                match8.setMatchString("void javax.mail.Transport.send($OutgoingMail_msg)");
                match8.setWildcard(true);
                matchs8.addChild(match8);
                exch6.addMatcher(matchs8);
                SRE sre7 = new SRE(null, null);
                exch6.setSRE(sre7);
                exec11.addChild(exch6);
                exec11.setHasExch(true);
                alterExec10.addChild(exec11);
                SequentialExecution groupedExec12 = new SequentialExecution("none");
                AlternationExecution alterExec13 = new AlternationExecution("none");
                SequentialExecution exec14 = new SequentialExecution("none");
                Exchange exch7 = new Exchange();
                Matchs matchs9 = new Matchs();
                ResMatch match9 = new ResMatch();
                match9.setMatchString("com.poco.MailUtil.confirm($OutgoingMail_msg)");
                match9.setResultMatchStr("0");
                matchs9.addChild(match9);
                exch7.addMatcher(matchs9);
                SRE sre8 = new SRE(null, null);
                sre8.setPositiveRE("com.poco.MailUtil.addBcc($OutgoingMail_msg,#java.lang.String{ta4poco@gmail.com})");
                exch7.setSRE(sre8);
                exec14.addChild(exch7);
                exec14.setHasExch(true);
                alterExec13.addChild(exec14);
                SequentialExecution exec15 = new SequentialExecution("none");
                Exchange exch8 = new Exchange();
                Matchs matchs10 = new Matchs();
                ResMatch match10 = new ResMatch();
                match10.setMatchString("com.poco.MailUtil.confirm($OutgoingMail_msg)");
                match10.setResultMatchStr("1");
                matchs10.addChild(match10);
                exch8.addMatcher(matchs10);
                SRE sre9 = new SRE(null, null);
                sre9.setNegativeRE("void javax.mail.Transport.send(*)");
                exch8.setSRE(sre9);
                exec15.addChild(exch8);
                exec15.setHasExch(true);
                alterExec13.addChild(exec15);
                groupedExec12.addChild(alterExec13);
                alterExec10.addChild(groupedExec12);
                groupedExec9.addChild(alterExec10);
                mapExec0.addChild(groupedExec9);
                SequentialExecution exec16 = new SequentialExecution("*");
                Exchange exch9 = new Exchange();
                Matchs matchs11 = new Matchs();
                matchs11.setNOT(true);
                Matchs matchs12 = new Matchs();
                matchs12.setOR(true);
                ResMatch match11 = new ResMatch();
                match11.setMatchString("com.poco.MailUtil.addBcc($OutgoingMail_msg,java.lang.String)");
                match11.setWildcard(true);
                matchs12.addChild(match11);
                ResMatch match12 = new ResMatch();
                match12.setMatchString("void javax.mail.Transport.send($OutgoingMail_msg)");
                match12.setWildcard(true);
                matchs12.addChild(match12);
                matchs11.addChild(matchs12);
                exch9.addMatcher(matchs11);
                SRE sre10 = new SRE(null, null);
                sre10.setPositiveRE("com.poco.MailUtil.addBcc($OutgoingMail_msg,#java.lang.String{ta4poco@gmail.com})");
                exch9.setSRE(sre10);
                exec16.addChild(exch9);
                exec16.setHasExch(true);
                mapExec0.addChild(exec16);
                SequentialExecution groupedExec17 = new SequentialExecution("none");
                AlternationExecution alterExec18 = new AlternationExecution("none");
                SequentialExecution exec19 = new SequentialExecution("none");
                Exchange exch10 = new Exchange();
                Matchs matchs13 = new Matchs();
                ResMatch match13 = new ResMatch();
                match13.setMatchString("void javax.mail.Transport.send($OutgoingMail_msg)");
                match13.setWildcard(true);
                matchs13.addChild(match13);
                exch10.addMatcher(matchs13);
                SRE sre11 = new SRE(null, null);
                exch10.setSRE(sre11);
                exec19.addChild(exch10);
                exec19.setHasExch(true);
                alterExec18.addChild(exec19);
                SequentialExecution exec20 = new SequentialExecution("none");
                Exchange exch11 = new Exchange();
                Matchs matchs14 = new Matchs();
                ResMatch match14 = new ResMatch();
                match14.setMatchString("com.poco.MailUtil.addBcc($OutgoingMail_msg,java.lang.String)");
                match14.setWildcard(true);
                matchs14.addChild(match14);
                exch11.addMatcher(matchs14);
                SRE sre12 = new SRE(null, null);
                sre12.setPositiveRE("");
                exch11.setSRE(sre12);
                sre12.setPositiveRE("String com.poco.MailUtil.concatContactMsg($OutgoingMail_msg,$OutgoingMail_ContactInfo)");
                exch11.setSRE(sre12);
                exec20.addChild(exch11);
                exec20.setHasExch(true);
                alterExec18.addChild(exec20);
                groupedExec17.addChild(alterExec18);
                mapExec0.addChild(groupedExec17);
                SequentialExecution exec21 = new SequentialExecution("*");
                Exchange exch12 = new Exchange();
                Matchs matchs15 = new Matchs();
                matchs15.setNOT(true);
                Matchs matchs16 = new Matchs();
                matchs16.setOR(true);
                ResMatch match15 = new ResMatch();
                match15.setMatchString("String com.poco.MailUtil.concatContactMsg($OutgoingMail_msg,$OutgoingMail_ContactInfo)");
                match15.setWildcard(true);
                matchs16.addChild(match15);
                ResMatch match16 = new ResMatch();
                match16.setMatchString("void javax.mail.Transport.send($OutgoingMail_msg)");
                match16.setWildcard(true);
                matchs16.addChild(match16);
                matchs15.addChild(matchs16);
                exch12.addMatcher(matchs15);
                SRE sre13 = new SRE(null, null);
                sre13.setPositiveRE("");
                exch12.setSRE(sre13);
                sre13.setPositiveRE("String com.poco.MailUtil.concatContactMsg($OutgoingMail_msg,$OutgoingMail_ContactInfo)");
                exch12.setSRE(sre13);
                exec21.addChild(exch12);
                exec21.setHasExch(true);
                mapExec0.addChild(exec21);
                SequentialExecution groupedExec22 = new SequentialExecution("none");
                AlternationExecution alterExec23 = new AlternationExecution("none");
                SequentialExecution exec24 = new SequentialExecution("none");
                Exchange exch13 = new Exchange();
                Matchs matchs17 = new Matchs();
                ResMatch match17 = new ResMatch();
                match17.setMatchString("void javax.mail.Transport.send($OutgoingMail_msg)");
                match17.setWildcard(true);
                matchs17.addChild(match17);
                exch13.addMatcher(matchs17);
                SRE sre14 = new SRE(null, null);
                exch13.setSRE(sre14);
                exec24.addChild(exch13);
                exec24.setHasExch(true);
                alterExec23.addChild(exec24);
                SequentialExecution exec25 = new SequentialExecution("none");
                Exchange exch14 = new Exchange();
                Matchs matchs18 = new Matchs();
                ResMatch match18 = new ResMatch();
                match18.setMatchString("String com.poco.MailUtil.concatContactMsg($OutgoingMail_msg,$OutgoingMail_ContactInfo)");
                match18.setWildcard(true);
                matchs18.addChild(match18);
                exch14.addMatcher(matchs18);
                SRE sre15 = new SRE(null, null);
                sre15.setPositiveRE("void javax.mail.Transport.send($OutgoingMail_msg)");
                exch14.setSRE(sre15);
                exec25.addChild(exch14);
                exec25.setHasExch(true);
                alterExec23.addChild(exec25);
                groupedExec22.addChild(alterExec23);
                mapExec0.addChild(groupedExec22);
                SequentialExecution exec26 = new SequentialExecution("none");
                Exchange exch15 = new Exchange();
                Matchs matchs19 = new Matchs();
                ResMatch match19 = new ResMatch();
                match19.setMatchString("void javax.mail.Transport.send($OutgoingMail_msg)");
                match19.setWildcard(true);
                matchs19.addChild(match19);
                exch15.addMatcher(matchs19);
                SRE sre16 = new SRE(null, null);
                sre16.setPositiveRE("#java.lang.String{OK}");
                exch15.setSRE(sre16);
                exec26.addChild(exch15);
                exec26.setHasExch(true);
                mapExec0.addChild(exec26);
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
