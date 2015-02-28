package com.poco.Extractor;

import org.omg.CORBA.OBJ_ADAPTER;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created by caoyan on 10/28/14.
 */
public class Closure {

    private Hashtable closures;
    // Should have reference to parent symbol table
    Closure parent;

    public Hashtable getClosures() {
        return closures;
    }

    public void setClosures(Hashtable closures) {
        this.closures = closures;
    }

    public void setParent(Closure parent) {
        this.parent = parent;
    }

    /**
     * Returns the parent symbol table of this symbol table.
     */
    public Closure getParent() {
        return parent;
    }

    /**
     * Construct a symbol table with specified parent.
     */
    public Closure(Closure parent) {
        closures = new Hashtable();
        this.parent = parent;
    }

    /**
     * Construct a symbol table with null parent.
     */
    public Closure() {
        closures = new Hashtable();
        this.parent = null;
    }

    public boolean hasParent() {
        if (this.parent == null)
            return false;
        else
            return true;
    }

    // Add new symbol with name, type, initial value
    public boolean addClosure(String varName, VarTypeVal typeVal) {
        if (varName == null)
            throw new NullPointerException("Can't put a null symbol in SymbolTable.");
        else if (typeVal == null)
            throw new NullPointerException("Can't put unnamed MathObject in SymbolTable.");
        closures.put(varName, typeVal);

        return true;
    }

    /**
     * Rebind/update typeVal to a symbol table
     */
    public void updateClosure(String varName, VarTypeVal typeVal) {
        if (varName == null)
            throw new NullPointerException("Can't put a null symbol in SymbolTable.");
        else if (typeVal == null)
            throw new NullPointerException("Can't put unnamed MathObject in SymbolTable.");
        if (closures.containsKey(varName))
            closures.put(varName, typeVal);
    }

    /**
     * Add update symbol table value with given var name
     */
    public VarTypeVal loadClosure(String varName) {
        if (varName == null)
            throw new NullPointerException("Can't put a null symbol in SymbolTable.");
        if (closures.containsKey(varName))
            return (VarTypeVal) (closures.get(varName));
        else {
            /**
             * need to check the parents policy's closure
             */
            Closure parent = this.parent;
            while (parent != null) {
                if (parent.closures.containsKey(varName))
                    return (VarTypeVal) (parent.closures.get(varName));
                else
                    parent = parent.parent;
            }
        }
        return null;
    }

    public boolean isContains(String varName) {
        if (varName == null)
            throw new NullPointerException("Can't put a null symbol in SymbolTable.");
        if (closures.containsKey(varName))
            return true;
        else {
            /**
             * need to check the parents policy's closure
             */
            Closure parent = this.parent;
            while (parent != null) {
                if (parent.closures.containsKey(varName))
                    return true;
                else
                    parent = parent.parent;
            }
        }
        return false;
    }


    public String getContext(String varName) {
        if (varName == null)
            throw new NullPointerException("Can't put a null symbol in SymbolTable.");
        if (closures.containsKey(varName)) {
            VarTypeVal vals = (VarTypeVal)closures.get(varName);
            return vals.getVarContext();
        }
        else {
            /**
             * need to check the parents policy's closure
             */
            Closure parent = this.parent;
            while (parent != null) {
                if (parent.closures.containsKey(varName)) {
                    VarTypeVal vals = (VarTypeVal)parent.closures.get(varName);
                    return vals.getVarContext();
                }
                else
                    parent = parent.parent;
            }
        }
        return null;
    }

    public void updateLinkedVal(String text, String objVal) {
        if (closures != null) {
            for (Object varName : closures.keySet()) {
                String id = (String) varName;
                if(!id.equals(text)) {
                    VarTypeVal vals = (VarTypeVal) closures.get(id);
                    if(vals.getVarLink() != null && vals.getVarLink().getText().contains("$"+text)) {
                        if(objVal.indexOf('(') != -1)
                            objVal = objVal.substring(0, objVal.indexOf('('));
                        String context =  vals.getVarContext().replace("$"+text,objVal);
                        closures.put(varName, new VarTypeVal(vals.getVarType(),vals.getVarLink(),context));
                    }
                }

            }
        }
    }
    /**
     * just for debug
     */
    public void printClosure() {
        if (closures != null) {
            for (Object varname : closures.keySet()) {
                System.out.println(varname + ": ");
                System.out.println("Type    : " + loadClosure((String) varname).getVarType());
                if(loadClosure((String) varname).getVarLink()!= null)
                    System.out.println("Link    : " + loadClosure((String) varname).getVarLink().getText());
                else
                    System.out.println("Link    : NULL");
                System.out.println("Context : " + loadClosure((String) varname).getVarContext());
                System.out.println("==========================================================");
            }
        }
    }
}
