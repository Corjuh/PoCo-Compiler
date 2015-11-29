package com.poco.PoCoCompiler;

/**
 * Created by yan on 11/28/15.
 */
public class Around4Act {
    private String arg4Advice;
    private String pointCutName;
    private String abstractAct;
    private String arg4PointCut;

    public Around4Act(String arg4Advice, String pointCutName, String arg4PointCut, String abstractAct) {
        this.arg4Advice = arg4Advice;
        this.pointCutName = pointCutName;
        this.arg4PointCut = arg4PointCut;
        this.abstractAct = abstractAct;
    }

    public String getArg4Advice() {
        return arg4Advice;
    }

    public String getPointCutName() {
        return pointCutName;
    }

    public String getAbstractAct() {
        return abstractAct;
    }

    public String getArg4PointCut() {
        return arg4PointCut;
    }
}
