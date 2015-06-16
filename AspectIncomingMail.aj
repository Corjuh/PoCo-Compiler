import com.poco.PoCoRuntime.*;
import java.lang.reflect.Method;

public aspect AspectIncomingMail {
    private DummyRootPolicy root = new DummyRootPolicy( new IncomingMail() );

    public AspectIncomingMail() {
        DataWH.dataVal.put("InterruptToCheckMem_ig",new TypeVal("java.lang.String","com.poco.InterruptGen.new(interval,percent)"));
        DataWH.dataVal.put("Attachments_call",new TypeVal("java.lang.String","java.io.FileWriter.new($ext())| java.io.FileWriter.new($ext(),boolean)"));
        DataWH.dataVal.put("OutgoingMail_msg",new TypeVal("javax.mail.Message",null));
        DataWH.dataVal.put("InterruptToCheckMem_interval",new TypeVal("long",new Long("interval")));
        DataWH.dataVal.put("IncomingMail_result",new TypeVal("java.lang.String","java.lang.String javax.mail.Message.getSubject()"));
        DataWH.dataVal.put("OutgoingMail_ContactInfo",new TypeVal("java.lang.String","ContactInfo"));
        DataWH.dataVal.put("InterruptToCheckMem_percent",new TypeVal("double",new Double("percent")));
        DataWH.dataVal.put("ClassLoaders_stacktrace",new TypeVal("java.lang.String","com.poco.MailUtil.CheckStack()"));
        DataWH.dataVal.put("IncomingMail_msg",new TypeVal("javax.mail.Message",  null));
        DataWH.dataVal.put("ClassLoaders_call",new TypeVal("java.lang.String","java.lang.ClassLoader+.new(*)"));
        DataWH.dataVal.put("ConfirmAndAllowOnlyHTTP_call",new TypeVal("java.lang.String","java.net.ServerSocket.new(#int{80|443})"));
    }

    pointcut PointCut0(int value0):
        call(java.net.ServerSocket.new(int)) && args(value0);

    Object around(int value0): PointCut0(value0) {
        if (RuntimeUtils.StringMatch(new Integer(value0).toString(), "80|443")) {
            DataWH.updateValue("ConfirmAndAllowOnlyHTTP_call","java.net.ServerSocket.new(int)");
            root.queryAction(new Event(thisJoinPoint));
            return proceed(value0);
        } else
            return proceed(value0);
    }

    pointcut PointCut1(java.lang.String value0):
        call(java.io.FileWriter.new(java.lang.String,..)) && args(value0,*);

    Object around(java.lang.String value0): PointCut1(value0) {
        if (RuntimeUtils.StringMatch(value0, ".exe|.vbs|.hta|.mdb|.bad")) {
            root.queryAction(new Event(thisJoinPoint));
            return proceed(value0);
        } else
            return proceed(value0);
    }

    pointcut PointCut2():
        call(* com.poco.RuntimeDemo.DoPrint());

    Object around(): PointCut2() {
        root.queryAction(new Event(thisJoinPoint));
        return proceed();
    }

    pointcut PointCut3():
        call(* Runtime.exec(..));

    Object around(): PointCut3() {
        root.queryAction(new Event(thisJoinPoint));
        return proceed();
    }

    pointcut PointCut4(java.lang.String value0):
        call(java.io.FileWriter.new(java.lang.String)) && args(value0);

    Object around(java.lang.String value0): PointCut4(value0) {
        if (RuntimeUtils.StringMatch(value0, ".exe|.vbs|.hta|.mdb|.bad")) {
            DataWH.updateValue("Attachments_call","java.io.FileWriter.new(java.lang.String)");
            root.queryAction(new Event(thisJoinPoint));
            return proceed(value0);
        } else
            return proceed(value0);
    }

    pointcut PointCut5(java.lang.String value0):
        call(java.io.File.new(java.lang.String)) && args(value0);

    Object around(java.lang.String value0): PointCut5(value0) {
        if (RuntimeUtils.StringMatch(value0, "*.class")) {
            root.queryAction(new Event(thisJoinPoint));
            return proceed(value0);
        } else
            return proceed(value0);
    }

    pointcut PointCut6(java.lang.String value0):
        call(java.io.File.new(..,java.lang.String)) && args(*,value0);

    Object around(java.lang.String value0): PointCut6(value0) {
        if (RuntimeUtils.StringMatch(value0, "*.class")) {
            root.queryAction(new Event(thisJoinPoint));
            return proceed(value0);
        } else
            return proceed(value0);
    }

    pointcut PointCut7():
        call(java.lang.ClassLoader+.new(..));

    Object around(): PointCut7() {
        DataWH.updateValue("ClassLoaders_call","java.lang.ClassLoader+.new");
        root.queryAction(new Event(thisJoinPoint));
        return proceed();
    }

    pointcut PointCut8(int value0):
        call(java.net.ServerSocket.new(int)) && args(value0);

    Object around(int value0): PointCut8(value0) {
        if (RuntimeUtils.StringMatch(new Integer(value0).toString(), "[^143|993|25|110|995]")) {
            root.queryAction(new Event(thisJoinPoint));
            return proceed(value0);
        } else
            return proceed(value0);
    }

    pointcut PointCut9():
        call(javax.mail.Message javax.mail.Folder.getMessage(..));

    Object around(): PointCut9() {
        Object ret = null;
        root.queryAction(new Event(thisJoinPoint));
        DataWH.updateValue("IncomingMail_msg","javax.mail.Message javax.mail.Folder.getMessage");
        return proceed();
    }

    pointcut PointCut10():
        call(java.lang.String javax.mail.Message.getSubject());

    Object around(): PointCut10() {
        Object ret = null;
        root.queryAction(new Event(thisJoinPoint));
        DataWH.updateValue("IncomingMail_result","java.lang.String javax.mail.Message.getSubject");
        return proceed();
    }

    pointcut PointCut11():
        call(int com.poco.Test.foo(..));

    Object around(): PointCut11() {
        Object ret = proceed();
        Event event = new Event(thisJoinPoint);
        event.setEventType("Result");
        if(RuntimeUtils.hasReturnValue(thisJoinPoint.getSignature().toString()))
            event.setResult(ret);
        else
            event.setResult("done");
        root.queryAction(event);
        return event.getResult();
    }

    pointcut PointCut12(javax.mail.Message value0):
        call(void javax.mail.Transport.send(javax.mail.Message)) && args(value0);

    Object around(javax.mail.Message value0): PointCut12(value0) {
        Object ret = null;
        if (RuntimeUtils.StringMatch(String.valueOf(value0), DataWH.dataVal.get("OutgoingMail_msg"))) {
            ret = proceed(value0);
            DataWH.updateValue("OutgoingMail_msg", value0);
            Event event = new Event(thisJoinPoint);
            event.setEventType("Result");
            if(RuntimeUtils.hasReturnValue(thisJoinPoint.getSignature().toString()))
                event.setResult(ret);
            else
                event.setResult("done");
            root.queryAction(event);
            return event.getResult();
        }
        else
            return proceed(value0);
    }

    before(javax.mail.Message value0): PointCut12(value0) {
        if (RuntimeUtils.StringMatch(String.valueOf(value0), DataWH.dataVal.get("OutgoingMail_msg"))) {
            DataWH.updateValue("OutgoingMail_msg", value0);
            root.queryAction(new Event(thisJoinPoint));
            return;
        }
        else   return;
    }

    pointcut PointCut13(Constructor run):
        target(run) && call(* Constructor.newInstance(..));

    Object around(Constructor run): PointCut13(run) {
        String className = RuntimeUtils.trimClassName(run.getDeclaringClass().toString()) + ".new";
        if (RuntimeUtils.matchingStack(root.promotedEvents,className)) {
            root.promotedEvents.pop();
            Object ret = proceed(run);
            Event event = new Event(thisJoinPoint);
            event.setEventType("Result");
            if(run.getParameterCount() >0) {
                String argStr = "";
                Parameter[] paras = run.getParameters();
                for(int i = 0; i<paras.length; i++) {
                    String temp = paras[i].getVarType().toString();
                    if(temp.startsWith("class "))
                        argStr += temp.substring(6);
                    else
                        argStr += temp;
                    if(i != paras.length-1)
                        argStr +=",";
                }
                className += "("+ argStr + ")";
            }
            event.setPromotedMethod(className);
            event.setResult(ret);
            root.queryAction(event);
            return ret;
        }
        else
            return proceed(run);
    }

    pointcut PointCut14(Method run):
        target(run) &&call(Object Method.invoke(..));

    Object around(Method run): PointCut14(run) {
        String className = RuntimeUtils.trimClassName(run.getDeclaringClass().toString());
        className =RuntimeUtils.concatClsMethod(className, run.getName());
        if (RuntimeUtils.matchingStack(root.promotedEvents,className)) {
            root.promotedEvents.pop();
            Object ret = proceed(run);
            if(RuntimeUtils.StringMatch("com.poco.InterruptGen.new(#long{$InterruptToCheckMem_interval},#double{$InterruptToCheckMem_percent})", className)){
                DataWH.updateValue("InterruptToCheckMem_ig", ret);
            }
            if(RuntimeUtils.StringMatch("* com.poco.MailUtil.CheckStack()", className)){
                DataWH.updateValue("ClassLoaders_stacktrace", ret);
            }
            PromotedEvent event = new PromotedEvent(thisJoinPoint,RuntimeUtils.getInvokeMethoSig(run),"Result",ret);
            root.queryAction(event);
            return ret;
        }
        else
            return proceed(run);
    }

    class Trivial extends Policy {
        public Trivial() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                SequentialExecution exec0 = new SequentialExecution("*");
                Exchange exch0 = new Exchange();
                SRE sre0 = new SRE(null, null);
                exch0.setSRE(sre0);
                exec0.addChild(exch0);
                exec0.setHasExch(true);
                rootExec.addChild(exec0);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
    class Test extends Policy {
        public Test() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                SequentialExecution groupedExec1 = new SequentialExecution("*");
                AlternationExecution alterExec2 = new AlternationExecution("none");
                SequentialExecution exec3 = new SequentialExecution("none");
                Exchange exch1 = new Exchange();
                Matchs matchs0 = new Matchs();
                ResMatch match0 = new ResMatch();
                match0.setMatchString("int com.poco.Test.foo(.*)");
                match0.setResultMatchStr("1");
                matchs0.addChild(match0);
                exch1.addMatcher(matchs0);
                SRE sre1 = new SRE(null, null);
                sre1.setPositiveRE("#int{11}");
                exch1.setSRE(sre1);
                exec3.addChild(exch1);
                exec3.setHasExch(true);
                alterExec2.addChild(exec3);
                SequentialExecution exec4 = new SequentialExecution("none");
                Exchange exch2 = new Exchange();
                Matchs matchs1 = new Matchs();
                ResMatch match1 = new ResMatch();
                match1.setMatchString("int com.poco.Test.foo(.*)");
                match1.setResultMatchStr("2");
                matchs1.addChild(match1);
                exch2.addMatcher(matchs1);
                SRE sre2 = new SRE(null, null);
                sre2.setPositiveRE("#int{22}");
                exch2.setSRE(sre2);
                exec4.addChild(exch2);
                exec4.setHasExch(true);
                alterExec2.addChild(exec4);
                groupedExec1.addChild(alterExec2);
                rootExec.addChild(groupedExec1);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
    class NoCreateClassFiles extends Policy {
        public NoCreateClassFiles() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                SequentialExecution exec5 = new SequentialExecution("*");
                Exchange exch3 = new Exchange();
                Matchs matchs2 = new Matchs("||");
                Match match2 = new Match("java.io.File.new(#java.lang.String{%.class})");
                matchs2.addChild(match2);
                exch3.addMatcher(matchs2);
                Match match3 = new Match("java.io.File.new(\*, #java.lang.String{.class})");
                matchs2.addChild(match3);
                exch3.addMatcher(matchs2);
                SRE sre3 = new SRE(null, null);
                sre3.setNegativeRE("java.io.File.new(#java.lang.String{%.class})|java.io.File.new(*,#java.lang.String{%.class})");
                exch3.setSRE(sre3);
                exec5.addChild(exch3);
                exec5.setHasExch(true);
                rootExec.addChild(exec5);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
    class DisSysCalls extends Policy {
        public DisSysCalls() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                SequentialExecution exec6 = new SequentialExecution("*");
                Exchange exch4 = new Exchange();
                Match match4 = new Match("Runtime.exec(*)");
                exch4.addMatcher(match4);
                SRE sre4 = new SRE(null, null);
                sre4.setNegativeRE("Runtime.exec(.*)");
                exch4.setSRE(sre4);
                exec6.addChild(exch4);
                exec6.setHasExch(true);
                rootExec.addChild(exec6);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
    class AllowOnlyMIME extends Policy {
        public AllowOnlyMIME() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                SequentialExecution exec7 = new SequentialExecution("*");
                Exchange exch5 = new Exchange();
                Match match5 = new Match("java.net.ServerSocket.new(#int{[^143|993|25|110|995]})");
                exch5.addMatcher(match5);
                SRE sre5 = new SRE(null, null);
                sre5.setNegativeRE("java.net.ServerSocket.new(#int{[^143|993|25|110|995]})");
                exch5.setSRE(sre5);
                exec7.addChild(exch5);
                exec7.setHasExch(true);
                rootExec.addChild(exec7);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
    class Attachments extends Policy {
        public Attachments() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                MapExecution mapExec8 = new MapExecution("*");
                mapExec8.setOperator("Union");
                SRE sre6 = new SRE(null, null);
                sre6.setNegativeRE("java.io.FileWriter.new(#java.lang.String{.exe|.vbs|.hta|.mdb|.bad})|java.io.FileWriter.new(#java.lang.String{.exe|.vbs|.hta|.mdb|.bad},boolean)");
                mapExec8.setMatchSre(sre6);
                SequentialExecution exec9 = new SequentialExecution("*");
                Exchange exch6 = new Exchange();
                Matchs matchs3 = new Matchs();
                matchs3.setNOT(true);
                Match match6 = new Match();
                match6.setMatchString("java.io.FileWriter.new(#java.lang.String{.exe|.vbs|.hta|.mdb|.bad})|java.io.FileWriter.new(java.lang.String,boolean)");
                matchs3.addChild(match6);
                exch6.addMatcher(matchs3);
                SRE sre7 = new SRE(null, null);
                exch6.setSRE(sre7);
                exec9.addChild(exch6);
                exec9.setHasExch(true);
                mapExec8.addChild(exec9);
                SequentialExecution exec10 = new SequentialExecution("none");
                Exchange exch7 = new Exchange();
                Matchs matchs4 = new Matchs();
                Match match7 = new Match();
                match7.setMatchString("java.io.FileWriter.new(#java.lang.String{.exe|.vbs|.hta|.mdb|.bad})|java.io.FileWriter.new(java.lang.String,boolean)");
                matchs4.addChild(match7);
                exch7.addMatcher(matchs4);
                SRE sre8 = new SRE(null, null);
                sre8.setPositiveRE("com.poco.RuntimeDemo.ShowDialog(#java.lang.String{Allowing a dangerous file is creating via: $Attachments_call ?})");
                exch7.setSRE(sre8);
                exec10.addChild(exch7);
                exec10.setHasExch(true);
                mapExec8.addChild(exec10);
                SequentialExecution exec11 = new SequentialExecution("*");
                Exchange exch8 = new Exchange();
                Matchs matchs5 = new Matchs();
                matchs5.setNOT(true);
                ResMatch match8 = new ResMatch();
                match8.setMatchString("com.poco.RuntimeDemo.ShowDialog(java.lang.String)");
                match8.setWildcard(true);
                matchs5.addChild(match8);
                exch8.addMatcher(matchs5);
                SRE sre9 = new SRE(null, null);
                sre9.setPositiveRE("com.poco.RuntimeDemo.ShowDialog(#java.lang.String{Allowing a dangerous file is creating via: $Attachments_call ?})");
                exch8.setSRE(sre9);
                exec11.addChild(exch8);
                exec11.setHasExch(true);
                mapExec8.addChild(exec11);
                SequentialExecution groupedExec12 = new SequentialExecution("none");
                AlternationExecution alterExec13 = new AlternationExecution("none");
                SequentialExecution exec14 = new SequentialExecution("none");
                Exchange exch9 = new Exchange();
                Matchs matchs6 = new Matchs();
                ResMatch match9 = new ResMatch();
                match9.setMatchString("com.poco.RuntimeDemo.ShowDialog(java.lang.String)");
                match9.setResultMatchStr("0");
                matchs6.addChild(match9);
                exch9.addMatcher(matchs6);
                SRE sre10 = new SRE(null, null);
                sre10.setPositiveRE("$Attachments_call");
                exch9.setSRE(sre10);
                exec14.addChild(exch9);
                exec14.setHasExch(true);
                alterExec13.addChild(exec14);
                SequentialExecution exec15 = new SequentialExecution("none");
                Exchange exch10 = new Exchange();
                SRE sre11 = new SRE(null, null);
                exch10.setSRE(sre11);
                exec15.addChild(exch10);
                exec15.setHasExch(true);
                alterExec13.addChild(exec15);
                groupedExec12.addChild(alterExec13);
                mapExec8.addChild(groupedExec12);
                rootExec.addChild(mapExec8);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
    class ConfirmAndAllowOnlyHTTP extends Policy {
        public ConfirmAndAllowOnlyHTTP() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                MapExecution mapExec16 = new MapExecution("*");
                mapExec16.setOperator("Union");
                SRE sre12 = new SRE(null, null);
                sre12.setNegativeRE("java.net.ServerSocket.new(#int{80|443})");
                mapExec16.setMatchSre(sre12);
                SequentialExecution exec17 = new SequentialExecution("*");
                Exchange exch11 = new Exchange();
                Matchs matchs7 = new Matchs();
                matchs7.setNOT(true);
                Match match10 = new Match();
                match10.setMatchString("java.net.ServerSocket.new(int)");
                matchs7.addChild(match10);
                exch11.addMatcher(matchs7);
                SRE sre13 = new SRE(null, null);
                exch11.setSRE(sre13);
                exec17.addChild(exch11);
                exec17.setHasExch(true);
                mapExec16.addChild(exec17);
                SequentialExecution exec18 = new SequentialExecution("*");
                Exchange exch12 = new Exchange();
                Matchs matchs8 = new Matchs();
                Match match11 = new Match();
                match11.setMatchString("java.net.ServerSocket.new(int)");
                matchs8.addChild(match11);
                exch12.addMatcher(matchs8);
                SRE sre14 = new SRE(null, null);
                sre14.setPositiveRE("com.poco.RuntimeDemo.ShowDialog(#java.lang.String{Allow to establish network connection via port: 80|443 ?})");
                exch12.setSRE(sre14);
                exec18.addChild(exch12);
                exec18.setHasExch(true);
                mapExec16.addChild(exec18);
                SequentialExecution groupedExec19 = new SequentialExecution("none");
                AlternationExecution alterExec20 = new AlternationExecution("none");
                SequentialExecution exec21 = new SequentialExecution("none");
                Exchange exch13 = new Exchange();
                Matchs matchs9 = new Matchs();
                ResMatch match12 = new ResMatch();
                match12.setMatchString("com.poco.RuntimeDemo.ShowDialog(java.lang.String)");
                match12.setResultMatchStr("0");
                matchs9.addChild(match12);
                exch13.addMatcher(matchs9);
                SRE sre15 = new SRE(null, null);
                sre15.setPositiveRE("$ConfirmAndAllowOnlyHTTP_call");
                exch13.setSRE(sre15);
                exec21.addChild(exch13);
                exec21.setHasExch(true);
                alterExec20.addChild(exec21);
                SequentialExecution exec22 = new SequentialExecution("none");
                Exchange exch14 = new Exchange();
                Match match13 = new Match("java.net.ServerSocket.new(#int{80|443})");
                exch14.addMatcher(match13);
                SRE sre16 = new SRE(null, null);
                sre16.setNegativeRE("$ConfirmAndAllowOnlyHTTP_call");
                exch14.setSRE(sre16);
                exec22.addChild(exch14);
                exec22.setHasExch(true);
                alterExec20.addChild(exec22);
                groupedExec19.addChild(alterExec20);
                mapExec16.addChild(groupedExec19);
                rootExec.addChild(mapExec16);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
    class ClassLoaders extends Policy {
        public ClassLoaders() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                MapExecution mapExec23 = new MapExecution("*");
                mapExec23.setOperator("Union");
                SRE sre17 = new SRE(null, null);
                sre17.setNegativeRE("java.lang.ClassLoader+.new(%)");
                mapExec23.setMatchSre(sre17);
                SequentialExecution exec24 = new SequentialExecution("*");
                Exchange exch15 = new Exchange();
                Matchs matchs10 = new Matchs();
                matchs10.setNOT(true);
                Match match14 = new Match();
                match14.setMatchString("java.lang.ClassLoader+.new(%)");
                matchs10.addChild(match14);
                exch15.addMatcher(matchs10);
                SRE sre18 = new SRE(null, null);
                exch15.setSRE(sre18);
                exec24.addChild(exch15);
                exec24.setHasExch(true);
                mapExec23.addChild(exec24);
                SequentialExecution exec25 = new SequentialExecution("none");
                Exchange exch16 = new Exchange();
                Matchs matchs11 = new Matchs();
                Match match15 = new Match();
                match15.setMatchString("java.lang.ClassLoader+.new(%)");
                matchs11.addChild(match15);
                exch16.addMatcher(matchs11);
                SRE sre19 = new SRE(null, null);
                sre19.setPositiveRE("com.poco.MailUtil.CheckStack()");
                exch16.setSRE(sre19);
                exec25.addChild(exch16);
                exec25.setHasExch(true);
                mapExec23.addChild(exec25);
                SequentialExecution exec26 = new SequentialExecution("*");
                Exchange exch17 = new Exchange();
                Matchs matchs12 = new Matchs();
                matchs12.setNOT(true);
                ResMatch match16 = new ResMatch();
                match16.setMatchString("com.poco.MailUtil.CheckStack()");
                match16.setWildcard(true);
                matchs12.addChild(match16);
                exch17.addMatcher(matchs12);
                SRE sre20 = new SRE(null, null);
                sre20.setPositiveRE("com.poco.MailUtil.CheckStack()");
                exch17.setSRE(sre20);
                exec26.addChild(exch17);
                exec26.setHasExch(true);
                mapExec23.addChild(exec26);
                SequentialExecution exec27 = new SequentialExecution("none");
                Exchange exch18 = new Exchange();
                Matchs matchs13 = new Matchs();
                ResMatch match17 = new ResMatch();
                match17.setMatchString("com.poco.MailUtil.CheckStack()");
                match17.setWildcard(true);
                matchs13.addChild(match17);
                exch18.addMatcher(matchs13);
                SRE sre21 = new SRE(null, null);
                sre21.setPositiveRE("com.poco.MailUtil.confirmDia(#java.lang.String{$ClassLoaders_stacktrace})");
                exch18.setSRE(sre21);
                exec27.addChild(exch18);
                exec27.setHasExch(true);
                mapExec23.addChild(exec27);
                SequentialExecution groupedExec28 = new SequentialExecution("none");
                AlternationExecution alterExec29 = new AlternationExecution("none");
                SequentialExecution exec30 = new SequentialExecution("none");
                Exchange exch19 = new Exchange();
                Matchs matchs14 = new Matchs();
                ResMatch match18 = new ResMatch();
                match18.setMatchString("com.poco.MailUtil.confirmDia(java.lang.String)");
                match18.setResultMatchStr("0");
                matchs14.addChild(match18);
                exch19.addMatcher(matchs14);
                SRE sre22 = new SRE(null, null);
                sre22.setPositiveRE("$ClassLoaders_call");
                exch19.setSRE(sre22);
                exec30.addChild(exch19);
                exec30.setHasExch(true);
                alterExec29.addChild(exec30);
                SequentialExecution exec31 = new SequentialExecution("none");
                Exchange exch20 = new Exchange();
                Matchs matchs15 = new Matchs();
                ResMatch match19 = new ResMatch();
                match19.setMatchString("com.poco.MailUtil.confirmDia(java.lang.String)");
                match19.setResultMatchStr("1");
                matchs15.addChild(match19);
                exch20.addMatcher(matchs15);
                SRE sre23 = new SRE(null, null);
                sre23.setNegativeRE("$ClassLoaders_call");
                exch20.setSRE(sre23);
                exec31.addChild(exch20);
                exec31.setHasExch(true);
                alterExec29.addChild(exec31);
                groupedExec28.addChild(alterExec29);
                mapExec23.addChild(groupedExec28);
                rootExec.addChild(mapExec23);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
    class InterruptToCheckMem extends Policy {
        public InterruptToCheckMem() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                SequentialExecution exec32 = new SequentialExecution("none");
                Exchange exch21 = new Exchange();
                Matchs matchs16 = new Matchs();
                Match match20 = new Match();
                match20.setMatchString("com.poco.RuntimeDemo.DoPrint()");
                matchs16.addChild(match20);
                exch21.addMatcher(matchs16);
                SRE sre24 = new SRE(null, null);
                sre24.setPositiveRE("com.poco.InterruptGen.new(#long{$InterruptToCheckMem_interval},#double{$InterruptToCheckMem_percent})");
                exch21.setSRE(sre24);
                exec32.addChild(exch21);
                exec32.setHasExch(true);
                rootExec.addChild(exec32);
                SequentialExecution exec33 = new SequentialExecution("*");
                Exchange exch22 = new Exchange();
                Matchs matchs17 = new Matchs();
                matchs17.setNOT(true);
                ResMatch match21 = new ResMatch();
                match21.setMatchString("com.poco.InterruptGen.new(long,double)");
                match21.setWildcard(true);
                matchs17.addChild(match21);
                exch22.addMatcher(matchs17);
                SRE sre25 = new SRE(null, null);
                sre25.setPositiveRE("com.poco.InterruptGen.new(#long{$InterruptToCheckMem_interval},#double{$InterruptToCheckMem_percent})");
                exch22.setSRE(sre25);
                exec33.addChild(exch22);
                exec33.setHasExch(true);
                rootExec.addChild(exec33);
                SequentialExecution exec34 = new SequentialExecution("none");
                Exchange exch23 = new Exchange();
                Matchs matchs18 = new Matchs();
                ResMatch match22 = new ResMatch();
                match22.setMatchString("com.poco.InterruptGen.new(long,double)");
                match22.setWildcard(true);
                matchs18.addChild(match22);
                exch23.addMatcher(matchs18);
                SRE sre26 = new SRE(null, null);
                sre26.setPositiveRE("$InterruptToCheckMem_InterruptToCheckMem_ig.start");
                exch23.setSRE(sre26);
                exec34.addChild(exch23);
                exec34.setHasExch(true);
                rootExec.addChild(exec34);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
    class OutgoingMail extends Policy {
        public OutgoingMail() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                MapExecution mapExec35 = new MapExecution("*");
                mapExec35.setOperator("Union");
                SRE sre27 = new SRE(null, null);
                sre27.setNegativeRE("void javax.mail.Transport.send(javax.mail.Message)");
                mapExec35.setMatchSre(sre27);
                SequentialExecution exec36 = new SequentialExecution("*");
                Exchange exch24 = new Exchange();
                Matchs matchs19 = new Matchs();
                matchs19.setNOT(true);
                Match match23 = new Match();
                match23.setWildcard(true);
                match23.setMatchString("void javax.mail.Transport.send(javax.mail.Message)");
                matchs19.addChild(match23);
                exch24.addMatcher(matchs19);
                SRE sre28 = new SRE(null, null);
                exch24.setSRE(sre28);
                exec36.addChild(exch24);
                exec36.setHasExch(true);
                mapExec35.addChild(exec36);
                SequentialExecution exec37 = new SequentialExecution("none");
                Exchange exch25 = new Exchange();
                Matchs matchs20 = new Matchs();
                Match match24 = new Match();
                match24.setWildcard(true);
                match24.setMatchString("void javax.mail.Transport.send(javax.mail.Message)");
                matchs20.addChild(match24);
                exch25.addMatcher(matchs20);
                SRE sre29 = new SRE(null, null);
                sre29.setPositiveRE("com.poco.MailUtil.logMessage(#javax.mail.Message{$OutgoingMail_msg})");
                exch25.setSRE(sre29);
                exec37.addChild(exch25);
                exec37.setHasExch(true);
                mapExec35.addChild(exec37);
                SequentialExecution exec38 = new SequentialExecution("*");
                Exchange exch26 = new Exchange();
                Matchs matchs21 = new Matchs();
                matchs21.setNOT(true);
                Matchs matchs22 = new Matchs();
                matchs22.setOR(true);
                ResMatch match25 = new ResMatch();
                match25.setMatchString("com.poco.MailUtil.logMessage(javax.mail.Message)");
                match25.setWildcard(true);
                matchs22.addChild(match25);
                ResMatch match26 = new ResMatch();
                match26.setMatchString("void javax.mail.Transport.send(javax.mail.Message)");
                match26.setWildcard(true);
                matchs22.addChild(match26);
                matchs21.addChild(matchs22);
                exch26.addMatcher(matchs21);
                SRE sre30 = new SRE(null, null);
                sre30.setPositiveRE("com.poco.MailUtil.logMessage(#javax.mail.Message{$OutgoingMail_msg})");
                exch26.setSRE(sre30);
                exec38.addChild(exch26);
                exec38.setHasExch(true);
                mapExec35.addChild(exec38);
                SequentialExecution groupedExec39 = new SequentialExecution("none");
                AlternationExecution alterExec40 = new AlternationExecution("none");
                SequentialExecution exec41 = new SequentialExecution("none");
                Exchange exch27 = new Exchange();
                Matchs matchs23 = new Matchs();
                ResMatch match27 = new ResMatch();
                match27.setMatchString("void javax.mail.Transport.send(javax.mail.Message)");
                match27.setWildcard(true);
                matchs23.addChild(match27);
                exch27.addMatcher(matchs23);
                SRE sre31 = new SRE(null, null);
                exch27.setSRE(sre31);
                exec41.addChild(exch27);
                exec41.setHasExch(true);
                alterExec40.addChild(exec41);
                SequentialExecution exec42 = new SequentialExecution("none");
                Exchange exch28 = new Exchange();
                Matchs matchs24 = new Matchs();
                ResMatch match28 = new ResMatch();
                match28.setMatchString("com.poco.MailUtil.logMessage(javax.mail.Message)");
                match28.setWildcard(true);
                matchs24.addChild(match28);
                exch28.addMatcher(matchs24);
                SRE sre32 = new SRE(null, null);
                sre32.setPositiveRE("com.poco.MailUtil.confirm(#javax.mail.Message{$OutgoingMail_msg})");
                exch28.setSRE(sre32);
                exec42.addChild(exch28);
                exec42.setHasExch(true);
                alterExec40.addChild(exec42);
                groupedExec39.addChild(alterExec40);
                mapExec35.addChild(groupedExec39);
                SequentialExecution exec43 = new SequentialExecution("*");
                Exchange exch29 = new Exchange();
                Matchs matchs25 = new Matchs();
                matchs25.setNOT(true);
                Matchs matchs26 = new Matchs();
                matchs26.setOR(true);
                ResMatch match29 = new ResMatch();
                match29.setMatchString("com.poco.MailUtil.confirm(javax.mail.Message)");
                match29.setWildcard(true);
                matchs26.addChild(match29);
                ResMatch match30 = new ResMatch();
                match30.setMatchString("void javax.mail.Transport.send(javax.mail.Message)");
                match30.setWildcard(true);
                matchs26.addChild(match30);
                matchs25.addChild(matchs26);
                exch29.addMatcher(matchs25);
                SRE sre33 = new SRE(null, null);
                sre33.setPositiveRE("com.poco.MailUtil.confirm(#javax.mail.Message{$OutgoingMail_msg})");
                exch29.setSRE(sre33);
                exec43.addChild(exch29);
                exec43.setHasExch(true);
                mapExec35.addChild(exec43);
                SequentialExecution groupedExec44 = new SequentialExecution("none");
                AlternationExecution alterExec45 = new AlternationExecution("none");
                SequentialExecution exec46 = new SequentialExecution("none");
                Exchange exch30 = new Exchange();
                Matchs matchs27 = new Matchs();
                ResMatch match31 = new ResMatch();
                match31.setMatchString("void javax.mail.Transport.send(javax.mail.Message)");
                match31.setWildcard(true);
                matchs27.addChild(match31);
                exch30.addMatcher(matchs27);
                SRE sre34 = new SRE(null, null);
                exch30.setSRE(sre34);
                exec46.addChild(exch30);
                exec46.setHasExch(true);
                alterExec45.addChild(exec46);
                SequentialExecution groupedExec47 = new SequentialExecution("none");
                AlternationExecution alterExec48 = new AlternationExecution("none");
                SequentialExecution exec49 = new SequentialExecution("none");
                Exchange exch31 = new Exchange();
                Matchs matchs28 = new Matchs();
                ResMatch match32 = new ResMatch();
                match32.setMatchString("com.poco.MailUtil.confirm(javax.mail.Message)");
                match32.setResultMatchStr("0");
                matchs28.addChild(match32);
                exch31.addMatcher(matchs28);
                SRE sre35 = new SRE(null, null);
                sre35.setPositiveRE("com.poco.MailUtil.addBcc(#javax.mail.Message{$OutgoingMail_msg},#java.lang.String{ta4poco@gmail.com})");
                exch31.setSRE(sre35);
                exec49.addChild(exch31);
                exec49.setHasExch(true);
                alterExec48.addChild(exec49);
                SequentialExecution exec50 = new SequentialExecution("none");
                Exchange exch32 = new Exchange();
                Matchs matchs29 = new Matchs();
                ResMatch match33 = new ResMatch();
                match33.setMatchString("com.poco.MailUtil.confirm(javax.mail.Message)");
                match33.setResultMatchStr("1");
                matchs29.addChild(match33);
                exch32.addMatcher(matchs29);
                SRE sre36 = new SRE(null, null);
                sre36.setNegativeRE("void javax.mail.Transport.send(.*)");
                exch32.setSRE(sre36);
                exec50.addChild(exch32);
                exec50.setHasExch(true);
                alterExec48.addChild(exec50);
                groupedExec47.addChild(alterExec48);
                alterExec45.addChild(groupedExec47);
                groupedExec44.addChild(alterExec45);
                mapExec35.addChild(groupedExec44);
                SequentialExecution exec51 = new SequentialExecution("*");
                Exchange exch33 = new Exchange();
                Matchs matchs30 = new Matchs();
                matchs30.setNOT(true);
                Matchs matchs31 = new Matchs();
                matchs31.setOR(true);
                ResMatch match34 = new ResMatch();
                match34.setMatchString("com.poco.MailUtil.addBcc(javax.mail.Message,java.lang.String)");
                match34.setWildcard(true);
                matchs31.addChild(match34);
                ResMatch match35 = new ResMatch();
                match35.setMatchString("void javax.mail.Transport.send(javax.mail.Message)");
                match35.setWildcard(true);
                matchs31.addChild(match35);
                matchs30.addChild(matchs31);
                exch33.addMatcher(matchs30);
                SRE sre37 = new SRE(null, null);
                sre37.setPositiveRE("com.poco.MailUtil.addBcc(#javax.mail.Message{$OutgoingMail_msg},#java.lang.String{ta4poco@gmail.com})");
                exch33.setSRE(sre37);
                exec51.addChild(exch33);
                exec51.setHasExch(true);
                mapExec35.addChild(exec51);
                SequentialExecution groupedExec52 = new SequentialExecution("none");
                AlternationExecution alterExec53 = new AlternationExecution("none");
                SequentialExecution exec54 = new SequentialExecution("none");
                Exchange exch34 = new Exchange();
                Matchs matchs32 = new Matchs();
                ResMatch match36 = new ResMatch();
                match36.setMatchString("void javax.mail.Transport.send(javax.mail.Message)");
                match36.setWildcard(true);
                matchs32.addChild(match36);
                exch34.addMatcher(matchs32);
                SRE sre38 = new SRE(null, null);
                exch34.setSRE(sre38);
                exec54.addChild(exch34);
                exec54.setHasExch(true);
                alterExec53.addChild(exec54);
                SequentialExecution exec55 = new SequentialExecution("none");
                Exchange exch35 = new Exchange();
                Matchs matchs33 = new Matchs();
                ResMatch match37 = new ResMatch();
                match37.setMatchString("com.poco.MailUtil.addBcc(javax.mail.Message,java.lang.String)");
                match37.setWildcard(true);
                matchs33.addChild(match37);
                exch35.addMatcher(matchs33);
                SRE sre39 = new SRE(null, null);
                sre39.setPositiveRE("");
                exch35.setSRE(sre39);
                sre39.setPositiveRE("String com.poco.MailUtil.concatContactMsg(#javax.mail.Message{$OutgoingMail_msg},#java.lang.String{$OutgoingMail_ContactInfo})");
                exch35.setSRE(sre39);
                exec55.addChild(exch35);
                exec55.setHasExch(true);
                alterExec53.addChild(exec55);
                groupedExec52.addChild(alterExec53);
                mapExec35.addChild(groupedExec52);
                SequentialExecution exec56 = new SequentialExecution("*");
                Exchange exch36 = new Exchange();
                Matchs matchs34 = new Matchs();
                matchs34.setNOT(true);
                Matchs matchs35 = new Matchs();
                matchs35.setOR(true);
                ResMatch match38 = new ResMatch();
                match38.setMatchString("String com.poco.MailUtil.concatContactMsg(javax.mail.Message,java.lang.String)");
                match38.setWildcard(true);
                matchs35.addChild(match38);
                ResMatch match39 = new ResMatch();
                match39.setMatchString("void javax.mail.Transport.send(javax.mail.Message)");
                match39.setWildcard(true);
                matchs35.addChild(match39);
                matchs34.addChild(matchs35);
                exch36.addMatcher(matchs34);
                SRE sre40 = new SRE(null, null);
                sre40.setPositiveRE("");
                exch36.setSRE(sre40);
                sre40.setPositiveRE("String com.poco.MailUtil.concatContactMsg(#javax.mail.Message{$OutgoingMail_msg},#java.lang.String{$OutgoingMail_ContactInfo})");
                exch36.setSRE(sre40);
                exec56.addChild(exch36);
                exec56.setHasExch(true);
                mapExec35.addChild(exec56);
                SequentialExecution groupedExec57 = new SequentialExecution("none");
                AlternationExecution alterExec58 = new AlternationExecution("none");
                SequentialExecution exec59 = new SequentialExecution("none");
                Exchange exch37 = new Exchange();
                Matchs matchs36 = new Matchs();
                ResMatch match40 = new ResMatch();
                match40.setMatchString("void javax.mail.Transport.send(javax.mail.Message)");
                match40.setWildcard(true);
                matchs36.addChild(match40);
                exch37.addMatcher(matchs36);
                SRE sre41 = new SRE(null, null);
                exch37.setSRE(sre41);
                exec59.addChild(exch37);
                exec59.setHasExch(true);
                alterExec58.addChild(exec59);
                SequentialExecution exec60 = new SequentialExecution("none");
                Exchange exch38 = new Exchange();
                Matchs matchs37 = new Matchs();
                ResMatch match41 = new ResMatch();
                match41.setMatchString("String com.poco.MailUtil.concatContactMsg(javax.mail.Message,java.lang.String)");
                match41.setWildcard(true);
                matchs37.addChild(match41);
                exch38.addMatcher(matchs37);
                SRE sre42 = new SRE(null, null);
                sre42.setPositiveRE("void javax.mail.Transport.send(#javax.mail.Message{$OutgoingMail_msg})");
                exch38.setSRE(sre42);
                exec60.addChild(exch38);
                exec60.setHasExch(true);
                alterExec58.addChild(exec60);
                groupedExec57.addChild(alterExec58);
                mapExec35.addChild(groupedExec57);
                SequentialExecution exec61 = new SequentialExecution("none");
                Exchange exch39 = new Exchange();
                Matchs matchs38 = new Matchs();
                ResMatch match42 = new ResMatch();
                match42.setMatchString("void javax.mail.Transport.send(javax.mail.Message)");
                match42.setWildcard(true);
                matchs38.addChild(match42);
                exch39.addMatcher(matchs38);
                SRE sre43 = new SRE(null, null);
                sre43.setPositiveRE("#java.lang.String{OK}");
                exch39.setSRE(sre43);
                exec61.addChild(exch39);
                exec61.setHasExch(true);
                mapExec35.addChild(exec61);
                rootExec.addChild(mapExec35);
                rootExec.getCurrentChildModifier();
                setRootExecution(rootExec);
            } catch (PoCoException pex) {
                System.out.println(pex.getMessage());
                pex.printStackTrace();
                System.exit(-1);
            }
        }
    }
    class IncomingMail extends Policy {
        public IncomingMail() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                MapExecution mapExec62 = new MapExecution("*");
                mapExec62.setOperator("Union");
                SRE sre44 = new SRE(null, null);
                sre44.setNegativeRE("javax.mail.Message javax.mail.Folder.getMessage(int)|java.lang.String javax.mail.Message.getSubject()");
                mapExec62.setMatchSre(sre44);
                SequentialExecution exec63 = new SequentialExecution("*");
                Exchange exch40 = new Exchange();
                Matchs matchs39 = new Matchs();
                matchs39.setNOT(true);
                ResMatch match43 = new ResMatch();
                match43.setMatchString("javax.mail.Message javax.mail.Folder.getMessage(int)|java.lang.String javax.mail.Message.getSubject()");
                match43.setWildcard(true);
                matchs39.addChild(match43);
                exch40.addMatcher(matchs39);
                SRE sre45 = new SRE(null, null);
                exch40.setSRE(sre45);
                exec63.addChild(exch40);
                exec63.setHasExch(true);
                mapExec62.addChild(exec63);
                SequentialExecution groupedExec64 = new SequentialExecution("none");
                AlternationExecution alterExec65 = new AlternationExecution("none");
                SequentialExecution groupedExec66 = new SequentialExecution("none");
                SequentialExecution exec67 = new SequentialExecution("none");
                Exchange exch41 = new Exchange();
                Matchs matchs40 = new Matchs();
                ResMatch match44 = new ResMatch();
                match44.setMatchString("javax.mail.Message javax.mail.Folder.getMessage(int)");
                match44.setWildcard(true);
                matchs40.addChild(match44);
                exch41.addMatcher(matchs40);
                SRE sre46 = new SRE(null, null);
                sre46.setPositiveRE("java.lang.String com.poco.MailUtil.logMessage(#javax.mail.Message{$IncomingMail_msg})");
                exch41.setSRE(sre46);
                exec67.addChild(exch41);
                exec67.setHasExch(true);
                groupedExec66.addChild(exec67);
                SequentialExecution exec68 = new SequentialExecution("*");
                Exchange exch42 = new Exchange();
                Matchs matchs41 = new Matchs();
                matchs41.setNOT(true);
                ResMatch match45 = new ResMatch();
                match45.setMatchString("java.lang.String com.poco.MailUtil.logMessage(javax.mail.Message)");
                match45.setWildcard(true);
                matchs41.addChild(match45);
                exch42.addMatcher(matchs41);
                SRE sre47 = new SRE(null, null);
                sre47.setPositiveRE("java.lang.String com.poco.MailUtil.logMessage(#javax.mail.Message{$IncomingMail_msg})");
                exch42.setSRE(sre47);
                exec68.addChild(exch42);
                exec68.setHasExch(true);
                groupedExec66.addChild(exec68);
                SequentialExecution exec69 = new SequentialExecution("none");
                Exchange exch43 = new Exchange();
                Matchs matchs42 = new Matchs();
                ResMatch match46 = new ResMatch();
                match46.setMatchString("java.lang.String com.poco.MailUtil.logMessage(javax.mail.Message)");
                match46.setWildcard(true);
                matchs42.addChild(match46);
                exch43.addMatcher(matchs42);
                SRE sre48 = new SRE(null, null);
                sre48.setPositiveRE("#java.lang.String{OK}");
                exch43.setSRE(sre48);
                exec69.addChild(exch43);
                exec69.setHasExch(true);
                groupedExec66.addChild(exec69);
                alterExec65.addChild(groupedExec66);
                SequentialExecution groupedExec70 = new SequentialExecution("none");
                SequentialExecution exec71 = new SequentialExecution("none");
                Exchange exch44 = new Exchange();
                Matchs matchs43 = new Matchs();
                ResMatch match47 = new ResMatch();
                match47.setMatchString("java.lang.String javax.mail.Message.getSubject()");
                match47.setWildcard(true);
                matchs43.addChild(match47);
                exch44.addMatcher(matchs43);
                SRE sre49 = new SRE(null, null);
                sre49.setPositiveRE("javax.mail.Message com.poco.MailUtil.spamifySubject(#java.lang.String{$IncomingMail_result})");
                exch44.setSRE(sre49);
                exec71.addChild(exch44);
                exec71.setHasExch(true);
                groupedExec70.addChild(exec71);
                SequentialExecution exec72 = new SequentialExecution("*");
                Exchange exch45 = new Exchange();
                Matchs matchs44 = new Matchs();
                matchs44.setNOT(true);
                ResMatch match48 = new ResMatch();
                match48.setMatchString("javax.mail.Message com.poco.MailUtil.spamifySubject(java.lang.String)");
                match48.setWildcard(true);
                matchs44.addChild(match48);
                exch45.addMatcher(matchs44);
                SRE sre50 = new SRE(null, null);
                sre50.setPositiveRE("javax.mail.Message com.poco.MailUtil.spamifySubject(#java.lang.String{$IncomingMail_result})");
                exch45.setSRE(sre50);
                exec72.addChild(exch45);
                exec72.setHasExch(true);
                groupedExec70.addChild(exec72);
                SequentialExecution exec73 = new SequentialExecution("none");
                Exchange exch46 = new Exchange();
                Matchs matchs45 = new Matchs();
                ResMatch match49 = new ResMatch();
                match49.setMatchString("javax.mail.Message com.poco.MailUtil.spamifySubject(java.lang.String)");
                match49.setWildcard(true);
                matchs45.addChild(match49);
                exch46.addMatcher(matchs45);
                SRE sre51 = new SRE(null, null);
                sre51.setPositiveRE("#java.lang.String{OK}");
                exch46.setSRE(sre51);
                exec73.addChild(exch46);
                exec73.setHasExch(true);
                groupedExec70.addChild(exec73);
                alterExec65.addChild(groupedExec70);
                groupedExec64.addChild(alterExec65);
                mapExec62.addChild(groupedExec64);
                rootExec.addChild(mapExec62);
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
