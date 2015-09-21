import com.poco.PoCoRuntime.*;
import java.lang.reflect.Method;

import java.lang.reflect.Constructor;

public aspect AspectRoot {
    private RootPolicy null = new RootPolicy();

    public AspectRoot() {
    root.addChild( new AllowOnlyMIME() );
    }

    pointcut PC4Reflection():
        call (* Method.invoke(Object, Object...)) && !within(com.poco.Promoter);

    Object around(): PC4Reflection()   { 
        return new SRE(null,"."); 
    }

    pointcut PointCut0(int value0):
        call(java.net.Ser*v*rSocket.new(int)) && args(value0);

    Object around(int value0): PointCut0(value0) {
        if (!RuntimeUtils.valueMatch(new Integer(value0).toString(), "143|993|25|110|995")) {
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

    class AllowOnlyMIME extends Policy {
        public AllowOnlyMIME() {
            try {
                SequentialExecution rootExec = new SequentialExecution("none");
                SequentialExecution exec0 = new SequentialExecution("*");
                Exchange exch0 = new Exchange();
                Match match0 = new Match("java.net.Ser*v*erSocket.new(!#int{143|993|25|110|995})");
                exch0.addMatcher(match0);
                SRE sre0 = new SRE(null, null);
                sre0.setNegativeRE("java.net.Ser*v*erSocket.new(!#int{143|993|25|110|995})");
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
}
