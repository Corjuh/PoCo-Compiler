package com.poco.PoCoRuntime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.JoinPoint;

public class RuntimeUtils {
    public static boolean matchFunction(String methodEventStr, String reg4Match) {
        if (isMatching(SREUtil.validateStr(reg4Match), methodEventStr))
            return true;

        // step 2: try to match method Name and each argument
        // info[0]: package name; info[1]: method name;
        // info[2]: argument string; info[3]: returnType
        String[] infos4RE = getFuncPkgMethArgInfos(reg4Match);
        String[] infos4Mtd = getFuncPkgMethArgInfos(methodEventStr);

        // a. first check whether the return types are matching
        boolean isFound = false;
        // constructor case
        if (infos4RE[3] == null && infos4Mtd[3] == null) {
            isFound = true;
        } else if (infos4RE[3] != null && infos4Mtd[3] != null) {
            isFound = isMatching(SREUtil.validateStr(infos4RE[3]), infos4Mtd[3]);
        }
        if (!isFound)
            return false;
        // b. check whether the method name are matching, need take consider of
        // the case that is to deal with catch all the subclasses

        isFound = isMatching(SREUtil.validateStr(infos4RE[0]), infos4Mtd[0])
                && isMatching(SREUtil.validateStr(infos4RE[1]), infos4Mtd[1]);
        // the case that is to deal with catch all the subclasses
        if (!isFound && infos4RE[0].endsWith("+")) {
            isFound = handleExtendClassCase(infos4RE[0], infos4Mtd[0],
                    infos4RE[1], infos4Mtd[1]);
        }
        if (!isFound)
            return false;

        // c. if method name and return type are matching, then check the
        // argument string

        // the case where the function has no argument
        if ((infos4RE[2] == null || infos4RE[2].trim().length() == 0)
                && (infos4Mtd[2] == null || infos4Mtd[2].trim().length() == 0)) {
            return true;
        } else {
            if (infos4RE[2].equals("*")) {
                return true;
            } else if (isMatching(SREUtil.validateStr(infos4RE[2]),
                    infos4Mtd[2])) {
                return true;
            } else {
                String[] argsRE = infos4RE[2].split(",");
                String[] args = infos4Mtd[2].split(",");
                if (argsRE.length != args.length)
                    return false;
                for (int i = 0; i < argsRE.length; i++) {
                    if (isRENotMatchCase(argsRE[i])) {
                        isFound = isNotMatchingCaseMatch(
                                argsRE[i].substring(1), args[i]);
                    } else {
                        isFound = isMatching(SREUtil.validateStr(argsRE[i]),
                                args[i]);

                        if (!isFound) {

                        }
                    }
                    if (!isFound)
                        break;
                }
                return isFound;
            }
        }
    }

    private static boolean handleExtendClassCase(String classRE,
                                                 String classVal, String methodName, String methodReg) {
        classRE = classRE.substring(0, classRE.length() - 1); // delete \+
        try {
            // e.g., com.poco.AClassLoader
            Class cls1 = Class.forName(classVal);
            // e.g., java.lang.ClassLoader+
            Class cls2 = Class.forName(classRE);

            if (!cls2.isAssignableFrom(cls1))
                return false;
            else {
                // constructor case
                if (methodReg == null && methodName == null)
                    return true;
                else if (methodReg == null || methodName == null)
                    return false;

                return isMatching(SREUtil.validateStr(methodReg), methodName);
            }
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * This function returns proper information about a method
     *
     * @param methodString
     * @return classInfo[0]: package name; classInfo[1]: method name;
     * classInfo[2]: argument string; classInfo[3]: returnType
     */
    public static String[] getFuncPkgMethArgInfos(String methodString) {
        String packageNam = "";
        String methodName = getMethodName(methodString);
        String methodArgs = getfunArgstr(methodString);
        String returnType = getMethodRetTyp(methodString);

        // the constructor case
        if (methodName.endsWith(".new")) {
            // package class name
            packageNam = methodName.substring(0, methodName.length() - 4);
            returnType = null;
            methodName = "";
        } else {
            if (methodName.indexOf('.') != -1
                    && methodName.indexOf('.') + 1 < methodName.length()) {
                // package class name
                packageNam = methodName.substring(0,
                        methodName.lastIndexOf('.'));
                // method name, null for constructor case
                methodName = methodName
                        .substring(methodName.lastIndexOf('.') + 1);
            } else {
                packageNam = methodName;
                methodName = "";
            }
        }
        return new String[]{packageNam, methodName, methodArgs, returnType};
    }

    public static String getMethodSignature(String methodString) {
        if (methodString == null || methodString.equals("null"))
            return null;

        // classInfo[0]: package name; classInfo[1]: method name;
        // classInfo[2]: argument string; classInfo[3]: returnType
        String[] infos = getFuncPkgMethArgInfos(methodString);
        infos[1] = concatClsMethod(infos[0], infos[1]);

        if (infos[2] != null && infos[2].trim().length() > 0) {
            String[] argArr = infos[2].split(",");
            for (int i = 0; i < argArr.length; i++) {
                if (isPoCoObject(argArr[i]))
                    argArr[i] = getPoCoObjTyp(argArr[i]);
            }
            infos[2] = strArrJoin(argArr, ",");
        } else
            infos[2] = "";

        if (infos[3] != null)
            return infos[3] + " " + infos[1] + "(" + infos[2] + ")";
        else
            return infos[1] + "(" + infos[2] + ")";
    }

    public static String concatClsMethod(String className, String methodName) {
        if (methodName == null || methodName.trim().length() == 0)
            return className.trim();
        else
            return className.trim().concat(".").concat(methodName.trim());
    }

    public static boolean isMethod(String str) {
        if (str == null)
            return false;
        return isMatching("^(.+)\\((.*)\\)$", str);
    }

    public static boolean isPoCoObject(String str) {
        if (str == null)
            return false;
        return (!isMethod(str)) && isMatching("#(.+)\\{(.+)\\}", str);
    }

    public static String getPoCoObjTyp(String str) {
        return getPoCoObjInfo(str, 1);
    }

    public static String getPoCoObjVal(String str) {
        return getPoCoObjInfo(str, 2);
    }

    private static String getPoCoObjInfo(String str, int mode) {
        if (str == null)
            return null;

        Pattern pattern = Pattern.compile("#(.+)\\{(.+)\\}");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            int sharp = str.indexOf('#');
            int lCBracet = str.indexOf('{');
            int rCBracet = str.lastIndexOf('}');
            if (mode == 1)
                return str.substring(sharp + 1, lCBracet);
            else
                return str.substring(lCBracet + 1, rCBracet);
        }
        return null;
    }

    public static boolean isPrimitiveType(String varType) {
        switch (varType) {
            // primitive types
            case "byte":
            case "int":
            case "short":
            case "long":
            case "double":
            case "float":
            case "boolean":
            case "char":
            case "String":
            case "java.lang.String":
                return true;
            // Non-primitive types
            default:
                return false;
        }
    }

    public static String getNameFrmJonPiont(JoinPoint joinPoint) {
        String methodStr = joinPoint.getSignature().toString();
        String methodName = getMethodName(methodStr);
        if (joinPoint.getKind().equals("constructor-call"))
            methodName += ".new";
        return methodName;
    }

    public static String getMethodName(String funStr) {
        return getMethodInfo(funStr, 0);
    }

    public static String getMethodInfos(String funStr) {
        return getMethodInfo(funStr, 1);
    }

    public static String getfunArgstr(String funStr) {
        if (funStr == null)
            return "";

        String returnStr = getMethodInfo(funStr, 2);
        // delete empty spaces between all parameters
        // joinPoint.getSignature().toLongString() will insert an empty
        // space between each parameter
        // such as java.io.FileWriter.new(java.lang.String, boolean)
        if (returnStr == null || returnStr.trim().length() == 0)
            return "";

        String[] args = returnStr.split(",");
        if (args.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                sb.append(args[i].trim());
                if (i != args.length - 1)
                    sb.append(",");
            }
            returnStr = sb.toString();
        }
        return returnStr;
    }

    /**
     * This function get the requested information of a function
     *
     * @param functionStr
     * @param mode        0: return only methodName info; 1: return method Name along
     *                    with other information, such as Access Modifiers
     * @return
     */
    private static String getMethodInfo(String functionStr, int mode) {
        String reg = "^(.+)\\((.*)\\)$";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(functionStr);
        if (matcher.find()) {
            int lParen = functionStr.indexOf('(');
            int rParen = functionStr.lastIndexOf(')');
            switch (mode) {
                // 0: return only methodName info
                case 0:
                    String temp = functionStr.substring(0, lParen);
                    int length = temp.split("\\s+").length;
                    if (length > 1) {
                        return temp.split("\\s+")[length - 1];
                    } else
                        return temp;
                    // 1: return method Name along with other information, such as
                    // Access Modifiers
                case 1:
                    return functionStr.substring(0, lParen);
                default: // mode = 2, return argstr
                    return functionStr.substring(lParen + 1, rParen);
            }
        } else {
            switch (mode) {
                // 0: return only methodName info
                case 0:
                    int length = functionStr.split("\\s+").length;
                    if (length > 1)
                        return functionStr.trim().split("\\s+")[length - 1];
                    else
                        return functionStr.trim();
                case 1:
                    return functionStr;
                default: // mode = 2, return argstr
                    return null;
            }
        }
    }

    public static boolean strValMatch(String matchingVal, String matchRegex) {
        if (isVariable(matchRegex)) {
            Object x = DataWH.dataVal.get(matchRegex.substring(1)).getObj();
            matchRegex = DataWH.dataVal.get(matchRegex.substring(1)).getObj()
                    .toString();
        }
        if (matchRegex == null || matchRegex.trim().length() == 0)
            return true;
        else
            return isMatching("^" + SREUtil.validateSREStr(matchRegex) + "$",
                    matchingVal);
    }

    public static boolean objMatch(Object obj1, Object obj2) {
        if (obj2 != null && obj2.toString().equals("%"))
            return true;
        return obj1.equals(obj2);
    }

    public static String[] objMethodCall(String str) {
        String[] returnVal = new String[3];
        String reg = "(.+\\.)(.+)+\\((.*)\\)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            returnVal[0] = matcher.group(1).substring(0,
                    matcher.group(1).length() - 1);
            returnVal[1] = matcher.group(2);
            returnVal[2] = matcher.group(3).trim();
        } else {
            reg = "(.+\\.)(.+)";
            pattern = Pattern.compile(reg);
            matcher = pattern.matcher(str);
            if (matcher.find()) {
                returnVal[0] = matcher.group(1).substring(0,
                        matcher.group(1).length() - 1);
                returnVal[1] = matcher.group(2);
                returnVal[2] = "";
            } else
                returnVal = null;
        }
        return returnVal;
    }

    /**
     * this method used to check if a method has return value or not if the
     * method is constructor or the method return type is void then it will
     * return false, otherwise return true
     *
     * @return
     */
    public static String getMethodRetTyp(String methodStr) {
        String methodName = getMethodInfos(methodStr.trim());
        // constructors have no return type
        if (methodName.endsWith(".new"))
            return null;

        if (methodName.split("\\s+").length > 1) {
            String[] infos = methodName.split("\\s+");
            return infos[infos.length - 2];
        } else
            return "*";
    }

    public static boolean matchingStack(Stack<String> events, Method run) {
        String className = run.getDeclaringClass().getName();
        className = concatClsMethod(className, run.getName());
        if (events != null && events.peek().contains(className))
            return true;

        return false;
    }

    public static String getInvokeMethoSig(Method run) {
        StringBuilder methodName = new StringBuilder();
        methodName.append(run.getReturnType().getName() + " ");
        methodName.append(run.getDeclaringClass().getName() + "."
                + run.getName() + "(");

        if (run.getParameterTypes().length > 0) {
            Type[] paraTypes = run.getParameterTypes();
            for (int i = 0; i < paraTypes.length; i++) {
                methodName.append(paraTypes[i].getClass().getName());
                if (i != paraTypes.length - 1)
                    methodName.append(",");
            }
        }
        return methodName.append(")").toString();
    }

    public static boolean isVariable(String varName) {
        if (varName == null || varName.trim().length() == 0)
            return false;

        return isMatching("^\\$\\w+$", varName);
    }

    public static boolean isVariableCase(String varName) {
        if (varName == null || varName.trim().length() == 0)
            return false;

        // $variable case
        if (isMatching("^\\$\\w+$", varName))
            return true;
            // $obj.method() | $obj.value case
        else if (isMatching("^\\$\\w+\\.\\w+(\\(.*\\))?$", varName))
            return true;
            // $policyVar case
        else
            return isMatching("^\\$\\w+\\(\\)$", varName);
    }

    /**
     * apply all the variable value into the final SRE value
     *
     * @param str
     * @return
     */
    public static String applyVarValues(String str) {
        if (str == null)
            return null;
        // for action cases:
        // a) $methodVar
        // b) $obj.method($arg1, #int{$arg2});
        // c) method($arg1, #int{$arg2});
        // For result cases:
        // a) $variable
        // b) $obj.value;
        // c) #type{$variable};

        // =====================VAR TYPE======OBJECT
        // $method_Variable=====STRING =======METHOD STRIN
        // $result_Variable=====ANY TYPE======ANY VALUE BUT NOT AN METHOD
        // $obj.method(...)=====OBJECT =======OBJECT VAL =>LOAD ADDRESS
        // $obj.value ==========OBJECT =======OBJECT VAL =>LOAD ADDRESS
        if (str.startsWith("$")) {
            String varName = getVariableName(str);
            // $obj.method(...) | $obj.value ==> load address
            if (varIsObjCase(str)) { // varName includes"$"
                if (varName != null
                        && DataWH.dataVal.containsKey(varName.substring(1))) {
                    Object obj = DataWH.dataVal.get(varName.substring(1))
                            .getObj();
                    if (obj == null) {
                        return null;
                    } else {
                        str = str.replace(varName,
                                Integer.toString(System.identityHashCode(obj)));
                    }
                }
            } else {
                String varType = DataWH.dataVal.get(varName.substring(1))
                        .getType();
                if (varType.equals("String")
                        || varType.equals("java.lang.String")) {
                    String varVal = DataWH.dataVal.get(varName.substring(1))
                            .getObj().toString();
                    if (isMethod(varVal))
                        str = str.replace(varName, varVal);
                    else
                        str = str.replace(
                                varName,
                                getVar(varType,
                                        DataWH.dataVal
                                                .get(varName.substring(1))
                                                .getObj(), false));
                } else {
                    str = str.replace(
                            varName,
                            getVar(varType,
                                    DataWH.dataVal.get(varName.substring(1))
                                            .getObj(), false));
                }
            }
        }
        // SRE string still contains variables, then just locate the variable
        // and load the value
        // method($arg1, #int{$arg2}) || #type{$variable};
        if (SREUtil.isContaintheChar(str, "$")) {
            if (isMethod(str)) {
                String methodName = getMethodInfo(str, 1);
                String[] args = getfunArgstr(str).split(",");
                for (int i = 0; i < args.length; i++) {
                    // $arg1 case
                    if (args[i].startsWith("$")) {
                        String val = loadVariable(args[i], 0, false);
                        String typ = null;
                        if (DataWH.dataVal.containsKey(args[i].substring(1)))
                            typ = DataWH.dataVal.get(args[i].substring(1))
                                    .getType();
                        args[i] = "#" + typ + "{" + val + "}";
                    }
                    // #int{$arg2} case
                    if (SREUtil.isContaintheChar(args[i], "$")) {
                        args[i] = loadVariable(args[i], 1, false);
                    }
                }
                str = methodName + "(" + strArrJoin(args, ",") + ")";
            } else {
                while (str.contains("$"))
                    str = loadVariable(str, 1, false);
            }
        }
        return str;
    }

    private static boolean varIsObjCase(String str) {
        return isMatching("^\\$\\w+\\.\\w+(\\(.*\\))?$", str);
    }

    public static String getVariableName(String str) {
        if (str == null || str.trim().length() == 0)
            return null;

        Pattern pattern = Pattern.compile("(\\$\\w+)(.*)");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find())
            return matcher.group(1);
        return null;
    }

    private static String loadVariable(String arg, int mode,
                                       boolean isMethodName) {
        if (arg == null || arg.trim().length() == 0)
            return null;

        String[] regex = {"(\\$\\w+)(.*)", "(.*)(\\$\\w+)(.*)"};
        Pattern pattern = Pattern.compile(regex[mode]);
        Matcher matcher = pattern.matcher(arg);
        if (matcher.find()) {
            String varName;
            if (mode == 0)
                varName = matcher.group(1).trim();
            else
                varName = matcher.group(2).trim();
            if (!DataWH.dataVal.containsKey(varName.substring(1))) {
                try {
                    throw new Exception("The variable \""
                            + varName.substring(1) + "\" does not exist!");
                } catch (Exception e) {
                }
            }
            String varType = DataWH.dataVal.get(varName.substring(1)).getType();
            Object varVal = DataWH.dataVal.get(varName.substring(1)).getObj();
            if (!isMethodName) {
                if (isPrimitiveType(varType)) {
                    arg = arg.replace(varName,
                            varVal.toString().replace(",", ";"));
                } else {
                    int varAddress = System.identityHashCode(varVal);
                    arg = arg.replace(varName, Integer.toString(varAddress));
                }
            } else
                arg = arg.replace(varName,
                        getPoCoObjVal(getVar(varType, varVal, isMethodName)));
        }
        return arg;
    }

    public static String getVar(String varType, Object varVal,
                                boolean isMethodName) {
        if (isPrimitiveType(varType)) {
            if (isMethodName)
                return varVal.toString();
            else
                return "#" + varType + "{" + varVal.toString() + "}";
            // Non-primitive types
        } else {
            if (isMethodName)
                return Integer.toString(System.identityHashCode(varVal));
            else
                return "#" + varType + "{" + System.identityHashCode(varVal)
                        + "}";
        }
    }

    public static boolean matchStack4Constr(Stack<String> events,
                                            Constructor run) {
        String className = run.getDeclaringClass().getName();
        if (events != null && events.peek().contains(className))
            return true;

        return false;
    }

    public static boolean matchSig(String matchingVal, Constructor run) {
        String regex = SREUtil.validateStr(getConstruSig(run));
        return isMatching(regex, matchingVal);
    }

    public static boolean matchSig(String matchingVal, Method run) {
        String methodSig = getInvokeMethoSig(run);
        String methodName = getMethodName(methodSig);
        String argStr = getfunArgstr(methodSig);
        if (argStr == null || argStr.trim().length() == 0)
            argStr = "";
        methodSig = methodName + "(" + argStr + ")";

        return isMatching(SREUtil.validateStr(methodSig), matchingVal);
    }

    public static String getConstruSig(Constructor run) {
        String className = run.getDeclaringClass().getName() + ".new";
        if (run.getTypeParameters().length > 0) {
            StringBuilder argStr = new StringBuilder();
            Type[] paras = run.getGenericParameterTypes();
            for (int i = 0; i < paras.length; i++) {
                argStr.append(paras[i].getClass().getName());
                if (i != paras.length - 1)
                    argStr.append(",");
            }
            className += "(" + argStr.toString() + ")";
        }
        return className;
    }

    public static String getStrValFrmDataWH(String sreStrVal) {
        if (sreStrVal == null)
            return null;

        String varVal = DataWH.dataVal.get(sreStrVal.substring(1)).getObj()
                .toString();
        return varVal;
    }

    public static Object getObjFrmDbWH(String varName) {
        if (varName == null)
            return null;

        if (DataWH.dataVal.containsKey(varName))
            return DataWH.dataVal.get(varName).getObj();
        return null;
    }

    /**
     * Join a string array with a separator
     *
     * @param strArr
     * @param separator
     * @return
     */
    public static String strArrJoin(String[] strArr, String separator) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0; i < strArr.length; i++) {
            sbStr.append(strArr[i]);
            if (i != strArr.length - 1)
                sbStr.append(separator);
        }
        return sbStr.toString();
    }

    private static boolean isRENotMatchCase(String str) {
        if (str == null)
            return false;
        // We assume that there will be no nested PoCo object
        return isMatching("^!#(.+)\\{(.+)\\}$", str);
    }

    private static boolean isNotMatchingCaseMatch(String reg, String str4Match) {
        String typRE = getPoCoObjTyp(reg);
        String typStr = getPoCoObjTyp(str4Match);
        // the argument types are not matching
        if (!isMatching("^(" + SREUtil.validateStr(typRE) + ")$", typStr)) {
            return false;
        } else {
            String valRE = getPoCoObjVal(reg);
            String valStr = getPoCoObjVal(str4Match);
            return !isMatching("^(" + SREUtil.validateStr(valRE) + ")$", valStr);
        }
    }

    public static boolean isObjCall(String methodName) {
        return isMatching("\\d+(\\.\\w+)+", methodName);
    }

    public static String[] getObjAddr4Call(String methodName) {
        Pattern pattern = Pattern.compile("(\\d+)(\\.\\w+)?\\.(\\w+)");
        Matcher matcher = pattern.matcher(methodName);
        if (matcher.find())
            return new String[]{matcher.group(1), matcher.group(2), matcher.group(3)};
        else
            return null;
    }

    public static boolean isMatching(String reg, String str4Match) {
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str4Match);
        return matcher.find();
    }

    public static String genValueofStr(int value0) {
        return new Integer(value0).toString();
    }

    public static String genValueofStr(byte value0) {
        return Byte.toString(value0);
    }

    public static String genValueofStr(short value0) {
        return new Integer(value0).toString();
    }

    public static String genValueofStr(long value0) {
        return Long.toString(value0);
    }

    public static String genValueofStr(double value0) {
        return Double.toString(value0);
    }

    public static String genValueofStr(float value0) {
        return Float.toString(value0);
    }

    public static String genValueofStr(boolean value0) {
        return Boolean.toString(value0);
    }

    public static String genValueofStr(char value0) {
        return Character.toString(value0);
    }
}