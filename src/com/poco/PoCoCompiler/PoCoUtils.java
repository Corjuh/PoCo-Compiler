package com.poco.PoCoCompiler;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yan on 5/13/15.
 */
public class PoCoUtils {

    public static String getMethodName(String methodStr) {
        return getMethodInfo(methodStr,1);
    }

    public static String getMethodArgLs(String methodStr) {
        return getMethodInfo(methodStr,2);
    }

    /**
     * This method is used to get info for a method
     * @param methodStr
     * @param mode 1: method Name, 2: method arglist
     * @return
     */
    private static String getMethodInfo(String methodStr, int mode) {
        String  reg     = "(.+)\\((.*)\\)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(methodStr);
        if(matcher.find()) {
            if (mode == 1)
                return matcher.group(1).trim();
            else if (matcher.group(2).trim().length() > 0)
                return matcher.group(2).trim();
        }else if (mode == 1)
            return methodStr;

        return null;
    }

    /**
     * return the return type info of a method, if it is an initial
     * method the return type would be null
     * @param methodStr
     * @return
     */
    public static String getMethodRtnTyp(String methodStr) {
        String rtnTyp = null;
        if(getMethodInfo(methodStr, 1).split("\\s+").length ==2)
            return getMethodInfo(methodStr, 1).split("\\s+")[0];
        return rtnTyp;
    }

    /**
     * parse a method string and return the arg array
     * @param methodStr
     * @return if the arglist if empty then return null
     */
    public static String[] getArgArray(String methodStr) {
        if(getMethodInfo(methodStr, 2) != null)
            return getMethodInfo(methodStr, 2).split(",");
        return null;
    }

    public static String getObjVal(String objStr) {
        return getInfoFrmObj(objStr,2);
    }

    public static String getObjType(String objStr) {
        return getInfoFrmObj(objStr,1);
    }
    /**
    * get infomation about an object string, if an argu is * return null
    * @param objStr
    * @param mode 1: return the arg type, 2: return the argname or val
    * @return
    */
    private static String getInfoFrmObj(String objStr, int mode) {
        String reg = "#(.+)\\{(.+)\\}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(objStr);
        if (matcher.find()) {
            return matcher.group(mode).trim();
        }
        if(mode ==1 && !objStr.equals("\\*"))
            return objStr;
        else
            return null;
    }

    public static String trimEndPunc(String str, String punctuation) {
        while (str.length() > 1) {
            int x = str.length() - punctuation.length();
            if (x > 0 && str.substring(x, str.length()).contains(punctuation))
                str = str.substring(0, x);
            else if (x == 0)
                return "";
            else
                break;
        }
        return str;
    }

    /**
     * This method is used to draw parameter types of the method, so that
     * pointcut can correctly monitor the correct method.
     *
     * @param methodStr
     * @return method signature
     */
    public static String getArgsTyp4PC(String methodStr) {
        String returnStr ="";
        String[] args = getArgArray(methodStr);
        if(args != null) {
            for (int i = 0; i < args.length; i++) {
                String temp = getObjType(args[i]);
                if(temp == null)
                    temp = "..";
                returnStr +=temp;
                if(i != args.length-1)
                    returnStr +=",";
            }
        }
        return returnStr;
    }


    public static String getMethodSignature(String methodStr) {
        String methodName = getMethodInfo(methodStr, 1);
        String[] args = getArgArray(methodStr);
        if(args != null) {
            String argSig = "";
            for (int i = 0; i < args.length; i++) {
                String temp = getObjType(args[i]);
                argSig +=temp;
                if(i != args.length-1)
                    argSig +=",";
            }
            return methodName + "(" + argSig + ")";
        }
        else
            return methodName + "(" + ")";
    }

    public static String validateStr(String str) {
        if(str == null)
            return null;
        str = str.replace("\\", "");
        return str;
    }

    public static String getVariableName(String str) {
        String  reg     = "(.+)\\$(\\w+)(.*)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    public static String[] objMethodCall(String str) {
        String[] returnVal = new String[2];
        String reg = "(.+\\.)(.+)+\\((.*)\\)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()) {
            returnVal[0] = matcher.group(1).substring(0,matcher.group(1).length() - 1);
            returnVal[1] = matcher.group(2);
        }
        else {
            reg = "(.+\\.)(.+)+";
            pattern = Pattern.compile(reg);
            matcher = pattern.matcher(str);
            if(matcher.find()) {
                returnVal[0] = matcher.group(1).substring(0,matcher.group(1).length() - 1);
                returnVal[1] = matcher.group(2);
            } else
                returnVal=null;
        }
        return returnVal;
    }


    /**
     * this function is used to format the function signature
     * function name included return type;
     * @return
     */
    public static String formatFuncRetTyp(String funcStr) {
        funcStr = reStrRectify(funcStr);
        String funName = getMethodInfo(funcStr, 1);
        if (!funName.endsWith(".new") && funName.split("\\s+").length == 1)
            return  "* " + funcStr;
        else
            return funcStr;

    }

    private static String reStrRectify(String reStr) {
        return reStr.replace("%", "..");
    }

    public static String attachPolicyName(String policyName, String str) {
        str = str.replace("$", "$" + policyName);
        String  reg     = "(.*)\\$(\\w+\\(\\))(.*)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            if(matcher.group(2).endsWith("()")) {
                String temp = matcher.group(2).substring(0,matcher.group(2).length()-2);
                str = str.replace(matcher.group(2),temp);
                matcher = pattern.matcher(str);
            }
        }
        return str;
    }

    public static void throwNoSuchVarExpection(String varName) {
        throw new NullPointerException("No such var named" + varName + " exist.");
    }

    public static boolean isVariable(String varName) {
        return  varName.startsWith("$");
    }

    /**
     * functions for checking parsing flags on stack
     */
    public static boolean isParsingArg(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 0);
    }
    public static boolean notParsingArgs(Stack<Integer> stack) {
        return !isParsingArg(stack);
    }

    public static boolean clousrFunc(Stack<Integer> stack)
    {
        return PoCoUtils.checkStackFlag(stack, 1);
    }
    public static boolean closurFunwUop(Stack<Integer> stack)
    {
        return PoCoUtils.checkStackFlag(stack, 2);
    }

    //check if the parsing Arg is Action
    public static boolean isActionFlag(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 3);
    }

    //check if the parsing Arg is Result
    public static boolean isResultFlag(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 4);
    }

    //check if the parsing Arg is Result
    public static boolean hasAsterisk(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 5);
    }

    //check if the parsing Arg is Result
    public static boolean hasPlus(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 6);
    }


    public static boolean isMapSreFlag(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 11);
    }

    public static boolean isExchLHSWild(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 12);
    }

    public static boolean isExchRHSSre(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 13);
    }

    public static boolean isExchMatch(Stack<Integer> stack) {  return PoCoUtils.checkStackFlag(stack, 14);}


    public static boolean isReBopFlag(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 21);
    }

    public static boolean isIREMatch(Stack<Integer> stack) {
        return isIREAction(stack) || isIREResult(stack) || isIREResMatch(stack);
    }

    public static boolean isIREAction(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 22);
    }

    public static boolean isIREResult(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 23);
    }

    public static boolean isIREResMatch(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 24);
    }

    public static boolean isPosNegRe4SRE(Stack<Integer> stack) {
        return isSrePosRE(stack) || isSreNegRE(stack);
    }
    public static boolean isSrePosRE(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 25);
    }

    public static boolean isSreNegRE(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 26);
    }

    public static boolean isSREBop1(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 31);
    }

    public static boolean isSREBop2(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 32);
    }

    public static boolean isSREBopCase(Stack<Integer> stack) {
        return isSREBop1(stack) || isSREBop2(stack);
    }

    private static boolean checkStackFlag(Stack<Integer> stack, int flag) {
        if(!stack.empty() && stack.peek()==flag)
            return true;
        else
            return false;
    }
}
