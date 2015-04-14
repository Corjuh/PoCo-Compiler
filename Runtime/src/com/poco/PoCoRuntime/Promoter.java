package com.poco;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by caoyan on 1/30/15.
 */
public class Promoter {
    /**
     *
     * @param pocoString
     *            the function want to be invoked
     * @throws Exception
     */
    public static void Reflect(String pocoString, Object[] objects) throws Exception {
        // className.funcName(#java.lang.Integer{42},#java.lang.String{402125})
        List<String> toExecute = Parse(pocoString);
        // class we are calling
        String className = toExecute.get(0)
                .substring(0, toExecute.get(0).length() - 1).trim();
        // the name of function the to be invoked
        String methodName = toExecute.get(1).trim();
        ArrayList<ReflectParameter> rps = null;
        // if params list is not empty
        if (toExecute.size() == 3) {
            String[] params = toExecute.get(2).split(","); // #java.lang.Integer{42}
            rps = ParseParameters(params);
        }
        ReflectExecute(className, methodName, rps, objects);
    }

    private static void ReflectExecute(String className, String methodName,
                                      ArrayList<ReflectParameter> params, Object[] objs) throws Exception {
        try {
        	boolean isfound = false;
        	
        	//get rid of the return type if the string contains it
        	if(className.trim().split(" ").length == 2)
        		className = className.trim().split(" ")[1];
            Class cls1 = Class.forName(className);
            Method[] methods = cls1.getMethods();
            Method theMethod = null;
            int paramCounts = params.size();

            for (Method method : methods) {
                // if find the method name
                if (method.getName().equals(methodName)) {
                    Type[] methodParams = method.getGenericParameterTypes();
                    if (methodParams.length == paramCounts) {
                        isfound = true;
                        int i = 0;
                        for (Iterator<ReflectParameter> it = params.iterator(); it
                                .hasNext();) {
                            String methodParam = GetTypeName(methodParams[i]
                                    .toString());
                            // if the type is not the primitive type
                            if (!methodParam.equals(it.next().GetParameterType())) {
                                isfound = false;
                                break;
                            }
                            i++;
                        }
                    }
                }
                if (isfound) {
                    theMethod = method;
                    break;
                }
            }
            // found the right method that is we wanted
            if (isfound) {
                if (paramCounts > 0) {
                    /*Object[] objs = new Object[paramCounts];
                    int i = 0;
                    for (Iterator<ReflectParameter> it = params.iterator(); it
                            .hasNext();) {
                        ReflectParameter rp = it.next();

                        if (!rp.GetParameterValue().equalsIgnoreCase("null"))
                            objs[i] = newInstance4Param(rp.GetParameterType(),
                                    rp.GetParameterValue());
                        i++;
                    }
                    */
                    // need check static or not
                    if (Modifier.isStatic(theMethod.getModifiers())) {
                        theMethod.invoke(null, objs);
                    } else {
                        theMethod.invoke(cls1.newInstance(), objs);
                    }
                }
            } else {
                System.out
                        .println("Sorry, cannot find the right method to pomote, "
                                + "please check the policy definition!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param toParse
     *            the string will be parsed
     * @return
     * @throws Exception
     */
    private static ArrayList<String> Parse(String toParse) throws Exception {
        ArrayList<String> tokens = new ArrayList<>();
        String reg = "(.+\\.)*(.+)\\((.*)\\)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(toParse);
        if (matcher.find()) {
            // package and class name
            tokens.add(matcher.group(1).toString());
            // method name
            tokens.add(matcher.group(2).toString());
            // parameter list if not null
            if (matcher.group(3) != null)
                tokens.add(matcher.group(3).toString());
        } else {
            throw new Exception("the function you want to invoked is invalid!");
        }
        return tokens;
    }

    private static ArrayList<ReflectParameter> ParseParameters(String[] params)
            throws Exception {
        ArrayList<ReflectParameter> rps = new ArrayList<ReflectParameter>(
                params.length);
        String reg = "#(.+)\\{(.+)\\}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher;
        for (int i = 0; i < params.length; i++) {
            matcher = pattern.matcher(params[i]);
            if (matcher.find()) {
                String paraTyp = matcher.group(1).toString().trim();
                //String paraVal = matcher.group(2).toString().trim();
                rps.add(new ReflectParameter(paraTyp, null));
            } else {
                throw new Exception("the parameter you entered is invalid");
            }
        }
        return rps;
    }

    private static String GetTypeName(String typeName) {
        // if the type is not the primitive type
        if (typeName.length() > 6 && typeName.substring(0, 5).equals("class"))
            return typeName.substring(6, typeName.length());
        else
            return typeName;
    }

    /**
     * This function instantiate desired types with the values.
     * @param type
     * @param val
     * @return
     */
    private static Object newInstance4Param(String type, String val) {
        Object obj = null;
        try {
			/*
			 * need separate the primitive type and object type from other types.
			 * because primitive type is not class and object's constructor has no parameters
			 */
            switch (type) {
                case "int":
                    obj = new Integer(val);
                    break;
                case "byte":
                    obj = new Byte(val);
                    break;
                case "short":
                    obj = new Short(val);
                    break;
                case "long":
                    obj = new Long(val);
                    break;
                case "float":
                    obj = new Float(val);
                    break;
                case "double":
                    obj = new Double(val);
                    break;
                case "boolean":
                    obj = new Boolean(val);
                    break;
                case "char":
                    obj = new Character(val.charAt(0));
                    break;
                case "java.lang.Object":
                    obj = new String(val);
                    break;
                default:
                    Constructor constr = Class.forName(type).getConstructor(Class.forName("java.lang.String"));
                    obj = constr.newInstance(val);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj;
    }
}