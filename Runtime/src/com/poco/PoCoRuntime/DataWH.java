package com.poco.PoCoRuntime;

/**
 * Created by caoyan on 3/28/15.
 */

import java.util.HashMap;

public final class DataWH {
    public static HashMap<String,TypeVal> dataVal = new HashMap<String,TypeVal>();
    
    public static void updateValue(String key, Object obj) {
    	String  typVal  = dataVal.get(key).getType();
    	String  typeAttr = dataVal.get(key).getAttribute();
        dataVal.remove(key);
        DataWH.dataVal.put(key, new TypeVal(typVal, typeAttr, obj));
    }
    
    public static void updateAttr(String key, String typeAttr) {
    	String  typVal = dataVal.get(key).getType();
    	Object  obj    = dataVal.get(key).getObj();
        dataVal.remove(key);
        DataWH.dataVal.put(key, new TypeVal(typVal, typeAttr, obj));
    }
}


class TypeVal {
    private String type;
    private String attribute; //use to store the attribute, such as method signature
    private Object obj;

    TypeVal() {
        this.type      = "java.lang.String";
        this.attribute = null;
        this.obj       = null;
    }

    TypeVal(String type) {
    	this.type      = type;
        this.attribute = null;
        this.obj       = null;
    }

    TypeVal(String type, Object obj) {
    	this.type      = type;
        this.attribute = null;
        this.obj       = obj;
    }

    TypeVal(String type, String attribute, Object obj) {
    	this.type      = type;
        this.attribute = attribute;
        this.obj       = obj;
    }
    
    public String getType() {
        return type;
    }
    
    public String getAttribute() {
        return this.attribute;
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