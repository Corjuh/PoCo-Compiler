package com.poco.PoCoRuntime;

/**
 * Created by caoyan on 3/28/15.
 */
import java.util.HashMap;

public final class DataWH {
    public static HashMap<String,TypeVal> dataVal      = new HashMap<String,TypeVal>();

    public static void updateValue(String key, Object obj) {
        String  typVal  = dataVal.get(key).getType();
        dataVal.remove(key);
        dataVal.put(key, new TypeVal(typVal, obj));
    }

    public static void updateTyeVal(String key, String type, Object obj) {
        dataVal.remove(key);
        dataVal.put(key, new TypeVal(type, obj));
    }

}


class TypeVal {
    private String type;
    private Object obj;

    TypeVal() {
        this.type      = "java.lang.String";
        this.obj       = null;
    }

    TypeVal(String type) {
        this.type      = type;
        this.obj       = null;
    }

    TypeVal(String type, Object obj) {
        this.type      = type;
        this.obj       = obj;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}

class QueryResult {
    private boolean isAccept;
    private SRE  	sreResult;

    QueryResult(boolean acceptOrNot) {
        this.isAccept = acceptOrNot;
        this.sreResult   = null;
    }

    QueryResult(boolean acceptOrNot, SRE sreResult) {
        this.isAccept = acceptOrNot;
        this.sreResult   = sreResult;
    }

    public void setSREResult(SRE sreResult) {
        this.sreResult   = sreResult;
    }

    public SRE getSREResult() {
        return this.sreResult;
    }

    public boolean getAcceptVal() {
        return this.isAccept;
    }

}