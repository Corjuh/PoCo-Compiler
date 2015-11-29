package com.poco.PoCoCompiler;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yan on 5/13/15.
 */
public class PoCoUtils {

    public static String getMtdName(String methodStr) {
        String nameStr = getMethodInfos(methodStr, 1);
        if (nameStr != null) {
            String reg = "^((public|private|protected)\\s+)?(static\\s+)?(.+\\s+)?(.+\\.)(.+)+$";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(nameStr);
            if (matcher.find()) {
                if (matcher.group(5) != null)
                    return matcher.group(5).concat(matcher.group(6));
                else
                    return matcher.group(6);
            }else {
                //abs_action case
                reg = "^((public|private|protected)\\s+)?(static\\s+)?(.+\\s+)?(.+)+$";
                pattern = Pattern.compile(reg);
                matcher = pattern.matcher(nameStr);
                if (matcher.find())
                    return matcher.group(5);
            }
        }
        return null;
    }

    public static String getMtdNmInfo(String methodStr) {
        return getMethodInfos(methodStr, 1);
    }

    public static String getMethodArgLs(String methodStr) {
        return getMethodInfos(methodStr, 2);
    }

    /**
     * This method is used to get info for a method
     *
     * @param methodStr
     * @param mode      1: method Name along with other infos, 2: method arglist
     * @return
     */
    private static String getMethodInfos(String methodStr, int mode) {
        String reg = "(.+)\\((.*)\\)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(methodStr);
        if (matcher.find()) {
            if (mode == 1)
                return matcher.group(1).trim();
            else if (matcher.group(2).trim().length() > 0)
                return matcher.group(2).trim();
        } else if (mode == 1)
            return methodStr;

        return null;
    }

    /**
     * return the return type info of a method, if it is an initial
     * method the return type would be null
     *
     * @param methodStr
     * @return
     */
    public static String getMethodRtnTyp(String methodStr) {
        String nameStr = getMethodInfos(methodStr, 1);
        if (nameStr != null) {
            String reg = "^((public|private|protected)\\s+)?(static\\s+)?(.+\\s+)?(.+\\.)(.+)+$";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(nameStr);
            if (matcher.find())
                return matcher.group(4);
        }
        return null;
    }

    /**
     * parse a method string and return the arg array
     *
     * @param methodStr
     * @return if the arglist if empty then return null
     */
    public static String[] getArgArray(String methodStr) {
        if (getMethodInfos(methodStr, 2) != null)
            return getMethodInfos(methodStr, 2).split(",");
        return null;
    }

    public static boolean isPoCoObject(String objStr) {
        if (objStr == null)
            return false;
        Pattern pattern = Pattern.compile("#(.+)\\{(.+)\\}");
        Matcher matcher = pattern.matcher(objStr);
        return matcher.find();
    }

    public static String getObjVal(String objStr) {
        return getInfoFrmObj(objStr, 2);
    }

    public static String getObjType(String objStr) {
        return getInfoFrmObj(objStr, 1);
    }

    /**
     * get infomation about an object string, if an argu is * return null
     *
     * @param objStr
     * @param mode   1: return the arg type, 2: return the argname or val
     * @return
     */
    private static String getInfoFrmObj(String objStr, int mode) {
        Pattern pattern = Pattern.compile("#(.+)\\{(.+)\\}");
        Matcher matcher = pattern.matcher(objStr);
        if (matcher.find()) {
            return matcher.group(mode).trim();
        }
        if (mode == 1 && !objStr.equals("\\*"))
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
        String returnStr = "";
        String[] args = getArgArray(methodStr);
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String temp = getObjType(args[i]);
                if (temp == null)
                    temp = "..";
                returnStr += temp;
                if (i != args.length - 1)
                    returnStr += ",";
            }
        }
        return returnStr;
    }


    public static String getMethodSignature(String methodStr) {
        String methodName = getMethodInfos(methodStr, 1);
        String[] args = getArgArray(methodStr);
        if (args != null) {
            String argSig = "";
            for (int i = 0; i < args.length; i++) {
                String temp = getObjType(args[i]);
                argSig += temp;
                if (i != args.length - 1)
                    argSig += ",";
            }
            return methodName + "(" + argSig + ")";
        } else
            return methodName + "(" + ")";
    }

    public static String validateStr(String str) {
        if (str == null)
            return null;
        str = str.replace("\\|", "|").replace("\\","\\\\");
        return str.replace("%", "*");
    }

    public static String getVariableName(String str) {
        String reg = "(.*)\\$(\\w+)(.*)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    public static String[] objMethodCall(String str) {
        String[] returnVal = new String[2];
        String reg = "^(.+\\.)(.+)+\\((.*)\\)$";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            returnVal[0] = matcher.group(1).substring(0, matcher.group(1).length() - 1);
            returnVal[1] = matcher.group(2);
        } else {
            reg = "(.+\\.)(.+)+";
            pattern = Pattern.compile(reg);
            matcher = pattern.matcher(str);
            if (matcher.find()) {
                returnVal[0] = matcher.group(1).substring(0, matcher.group(1).length() - 1);
                returnVal[1] = matcher.group(2);
            } else
                returnVal = null;
        }
        return returnVal;
    }


    /**
     * this function is used to format the function signature
     * function name included return type;
     *
     * @return
     */
    public static String formatFuncRetTyp(String funcStr) {
        String funName = getMtdName(funcStr);
        String retTyp = getMethodRtnTyp(funcStr);
        String argStr = getMethodArgLs(funcStr);

        if (retTyp == null)
            retTyp = "* ";

        if (argStr == null)
            argStr = "";

        //abs_case need also handle the init case
        if (funName.endsWith(".<init>"))
            funName = funName.replace(".<init>", ".new");

        if (funName.endsWith(".new"))
            return (funName + "(" + argStr + ")").replace("%", "*");
        else
            return (retTyp + funName + "(" + argStr + ")").replace("%", "*");
    }

    public static String attachPolicyName(String policyName, String str) {
        str = str.replace("$", "$" + policyName+"_");
        String reg = "(.*)\\$(\\w+\\(\\))(.*)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            if (matcher.group(2).endsWith("()")) {
                String temp = matcher.group(2).substring(0, matcher.group(2).length() - 2);
                str = str.replace(matcher.group(2), temp);
                matcher = pattern.matcher(str);
            }
        }
        return str;
    }

    public static void throwNoSuchVarExpection(String varName) {
        throw new NullPointerException("No such var named " + varName + " exist.");
    }

    public static boolean isVariable(String varName) {
        return varName.startsWith("$");
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

    public static boolean clousrFunc(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 1);
    }

    public static boolean closurFunwUop(Stack<Integer> stack) {
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

    //check if the parsing Arg is Result
    public static boolean hasNone(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 7);
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

    public static boolean isExchMatch(Stack<Integer> stack) {
        return PoCoUtils.checkStackFlag(stack, 14);
    }


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
        if (!stack.empty() && stack.peek() == flag)
            return true;
        else
            return false;
    }

    /**
     * take care the case where the single pointcut contained more than one function
     * cannot direct use string.split("|") due to the fact that the function parameters
     * may also contains "|", such as [`#java.lang.String{.exe|.vbs|.hta|.mdb|.bad}']
     *
     * @param methodStr
     * @return
     */
    public static String[] parseFunStr(String methodStr) {
        ArrayList<String> methodStrs = new ArrayList<String>();
        String restStr = methodStr;

        int lParenIndex = methodStr.indexOf('(', 0);
        int rParenIndex = restStr.indexOf(')', lParenIndex);

        //no | case
        if (rParenIndex == methodStr.length() - 1) {
            methodStrs.add(methodStr);
        } else {
            while (lParenIndex != -1 && rParenIndex != -1) {
                String left = restStr.substring(0, lParenIndex + 1);
                String args = restStr.substring(lParenIndex + 1, rParenIndex);
                restStr = restStr.substring(rParenIndex,
                        restStr.length());

                methodStrs.add(left + args + ")");

                if (restStr.trim().equals(")")) {
                    break;
                } else {
                    //delete the ")"
                    restStr = restStr.substring(1);
                    if (restStr.startsWith("|"))
                        restStr = restStr.substring(1);
                    lParenIndex = restStr.indexOf('(', 0);
                    if (lParenIndex != -1)
                        rParenIndex = restStr.indexOf(')', lParenIndex);
                    else
                        rParenIndex = -1;
                }
            }
        }
        return methodStrs.toArray(new String[methodStrs.size()]);
    }

    /**
     * In PoCo, "|" can be within a SRE to separate each individual event or
     * event pattern, but nested alternation will not be allowed.
     * -`java.io.File.<init>(#String{%})|java.io.File.<init>(\*,#String{%})'
     * And, within each individual event or event pattern, alternation (but no
     * nested alternation) can also be used. For example, the
     * "FileWriter.<init>(#String{%.mdb|%.bad})|FileWriter.<init>(#String{%.mdb|%.bad},boolean)"
     *
     * @param str
     * @return
     */
    public static String[] splitSreStr(String str) {
        ArrayList<String> retStrs = new ArrayList<String>();
        int splitIndex = str.indexOf('|');

        // step 1, get all the index for the "|"s (indexes for "|" literals are
        // excluded)
        int[] barIndexes = getSplitIndexes(str, "|");
        // this is the case that alternation is non-existing.
        if (barIndexes == null) {
            retStrs.add(str);
        } else {
            int startIndex = 0;
            for (int index : barIndexes) {
                String leftStr = str.substring(startIndex, index);
                if (isMethod(leftStr)
                        || isPoCoObject(leftStr)
                        || isVariableCase(leftStr)) {
                    // add the leftStr to the array list first, then locate for
                    // next alternation
                    retStrs.add(str.substring(startIndex, index));
                    if (index == str.length() - 1) {
                        inValidSREstr(str);
                    } else
                        startIndex = index + 1;
                } else {
                    if (index == str.length() - 1) {
                        inValidSREstr(str);
                    }
                }
            }
            if (startIndex + 1 < str.length())
                retStrs.add(str.substring(startIndex));
        }
        return retStrs.toArray(new String[retStrs.size()]);
    }

    /**
     * This function returns the indexes of all occurrences of specific
     * character (not escaped) in a string,
     *
     * @param str
     * @param split
     * @return
     */
    private static int[] getSplitIndexes(String str, String split) {
        if (!str.contains(split))
            return null;

        ArrayList<Integer> indexes = new ArrayList<Integer>();
        int index = str.indexOf(split);
        while (index != -1) {
            if (index - 1 >= 0 && str.substring(index - 1, index).equals("\\")) {
                if (index + 1 < str.length())
                    index = str.indexOf(split, index + 1);
                else
                    break;
            } else {
                indexes.add(index);
                index = str.indexOf(split, index + 1);
            }
        }

        int[] indexs = null;
        if (indexes.size() > 0) {
            int count = indexes.size();
            indexs = new int[count];
            for (int i = 0; i < count; i++) {
                indexs[i] = Integer.valueOf(indexes.get(i));
                if (i > 0 && (indexs[i - 1] + 1) == indexs[i])
                    inValidSREstr(str);
            }
        }
        return indexs;
    }

    private static void inValidSREstr(String str) {
        System.err.println("The specified SRE: \"" + str + "\" is invalid!");
        System.exit(-1);
    }

    public static boolean reContainNotMatch(String str) {
        if (str == null)
            return false;
        // We assume that there will be no nested PoCo object
        return isMatching("!#(.+)\\{(.+)\\}", str);
    }

    public static boolean isMethod(String str) {
        if (str == null)
            return false;
        return isMatching("^(.+)\\((.*)\\)$", str);
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

    public static boolean isMatching(String reg, String str4Match) {
        if (str4Match == null)
            return false;

        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str4Match);
        return matcher.find();
    }
}
