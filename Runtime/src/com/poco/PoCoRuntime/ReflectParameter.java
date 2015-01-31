package com.poco.PoCoRuntime;

/**
 * Created by cory on 12/21/14.
 */
public class ReflectParameter {
    protected String parameterType;
    protected String parameterValue;

    public ReflectParameter(String type, String value) {
        parameterType = type;
        parameterValue = value;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    public String GetParameterType() {
        return parameterType;
    }

    public String GetParameterValue() {
        return parameterValue;
    }
}