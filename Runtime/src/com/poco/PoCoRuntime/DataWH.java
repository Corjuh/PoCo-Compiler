package com.poco.PoCoRuntime;

/**
 * Created by caoyan on 3/28/15.
 */

        import java.util.HashMap;

public final class DataWH {
    public static HashMap<String,TypeVal> dataVal = new HashMap<String,TypeVal>();

    public static void updateValue(String key, Object obj) {
        String  typVal  = dataVal.get(key).getType();
        dataVal.remove(key);
        DataWH.dataVal.put(key, new TypeVal(typVal, obj));
    }

    public static void updateTyeVal(String key, String type, Object obj) {
        dataVal.remove(key);
        DataWH.dataVal.put(key, new TypeVal(type, obj));
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