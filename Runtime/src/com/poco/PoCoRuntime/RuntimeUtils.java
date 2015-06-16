package com.poco.PoCoRuntime;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.JoinPoint;

public class RuntimeUtils {
	public static boolean matchFunction(String eventSig, String matchStr) {
		//if (eventSig.contains(matchStr) || matchStr.contains(eventSig))  
		//	return true;
		matchStr = getMethodSignature(matchStr);
		
		Pattern pattern = Pattern.compile(validateStr(matchStr));
		Matcher matcher = pattern.matcher(eventSig);
		
		if (matcher.find())  
			return true;
		 
		// if not match, need check the case of dealing with catch all the
		// subclasses
		String[] matchInfo = getClassInfo(matchStr);

		if (!matchInfo[0].endsWith("+"))
			return false;
		matchInfo[0] = matchInfo[0].replace("+", ""); // delete \+

		String[] eventInfo = getClassInfo(eventSig);
		try {
			// e.g., com.poco.AClassLoader
			Class cls1 = Class.forName(eventInfo[0]);
			// e.g., java.lang.ClassLoader+
			Class cls2 = Class.forName(matchInfo[0]);

			if (!cls2.isAssignableFrom(cls1)) {
				return false;
			} else {
				// check method match first or not
				if (eventInfo[1] == null && matchInfo[1] == null) // constructor case
					return true;
				if (eventInfo[1] == null || matchInfo[1] == null)
					return false;

				if (!eventInfo[1].equals(matchInfo[1]))
					return false;

				// if names also match then check args
				if (eventInfo[2] == null && matchInfo[2] == null)
					return true;
				if (eventInfo[2] == null || matchInfo[2] == null)
					return false;

				String[] eventParas = eventInfo[2].split(",");
				String[] matchParas = matchInfo[2].split(",");

				if (eventParas.length != matchParas.length)
					return false;

				boolean isFound = true;
				for (int j = 0; j < eventParas.length; j++) {
					if (!matchInfo[j].contains(eventParas[j])) {
						isFound = false;
						break;
					}
				}
				return isFound;
			}
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static String[] getClassInfo(String classStr) {
		String[] classInfo = new String[3];

		String fullMethodName = classStr;
		int leftParen = classStr.indexOf('(');
		int righParen = classStr.indexOf(')');
		if (leftParen != -1 && righParen != -1)
			fullMethodName = fullMethodName.substring(0, leftParen).trim();

		// the constructor case
		if (fullMethodName.split("\\s+").length == 1) {
			classInfo[0] = fullMethodName.replace(".new", "");
			classInfo[1] = null;
			classInfo[2] = null;
		} else {
			String reg = "(.+\\.)*(.+)\\((.*)\\)";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher1 = pattern.matcher(classStr);

			if (matcher1.find()) {
				// package and class name
				classInfo[0] = matcher1.group(1).toString();
				// method name
				classInfo[1] = matcher1.group(2).toString();
				// parameter list if not null
				if (matcher1.group(3) != null
						&& matcher1.group(3).trim().length() > 0)
					classInfo[2] = matcher1.group(3).toString();
			} else { // no arginfo
				String regNoArg = "(.+\\.)*(.+)";
				Pattern patNoArg = Pattern.compile(regNoArg);
				Matcher matNoArg = patNoArg.matcher(classStr);
				if (matNoArg.find()) {
					classInfo[0] = matNoArg.group(1).toString();
					classInfo[1] = matNoArg.group(2).toString();
					classInfo[2] = null;
				} else {
					System.out.println("the method name is invalid, the program will end abnormally!!!");
					System.exit(-1);
				}
			}
			classInfo[0] = classInfo[0].substring(0, classInfo[0].length()-1); // delete end  dot
		}
		return classInfo;
	}

	public static String[] getfunTypName(String funStr) {
		String[] returnStr = new String[2];
		String[] temp = funStr.trim().split("\\s+");
		if (temp.length == 2) {
			returnStr[0] = temp[0].trim();
			returnStr[1] = temp[1].trim();
			if (temp[1].indexOf('(') != -1)
				returnStr[1] = temp[1].substring(0, temp[1].indexOf('('));
		} else {
			returnStr[0] = "*";
			returnStr[1] = funStr.trim();
			if (funStr.indexOf('(') != -1)
				returnStr[1] = funStr.substring(0, funStr.indexOf('('));
		}
		return returnStr;
	}

	public static String getfunArgstr(String funStr) {
		String returnStr = null;
		if (funStr.indexOf('(') != -1 && funStr.indexOf(')') != -1)
			returnStr = funStr.substring(funStr.indexOf('(') + 1,
					funStr.indexOf(')')).trim();
		if (returnStr != null && returnStr.length() == 0)
			return null;
		return returnStr;
	}

	/**
	 * trim the first six letters from the full class name (e.g., trim
	 * "class java.lang.reflect.Method" to "java.lang.reflect.Method")
	 * 
	 * @param fullClassName
	 * @return
	 */
	public static String trimClassName(String fullClassName) {
		if (fullClassName != null && fullClassName.length() >= 6)
			if (fullClassName.startsWith("class "))
				return fullClassName.substring(6, fullClassName.length());
		return fullClassName;

	}

	public static String concatClsMethod(String className, String methodName) {
		return className.trim().concat(".").concat(methodName.trim());
	}

	public static String getMethodSignature(String methodString) {
		int lParenIndex = methodString.indexOf('(', 0);

		if (lParenIndex == -1)  
			return methodString;

		String objReg = "#(.+)\\{(.+)\\}";
		Pattern objPtn = Pattern.compile(objReg);

		String validateStr = methodString;
		String returnstr = "";

		lParenIndex = validateStr.indexOf('(', 0);
		int rParenIndex = validateStr.indexOf(')', lParenIndex);

		while (lParenIndex != -1 && rParenIndex != -1) {
			String left = validateStr.substring(0, lParenIndex + 1);
			String args = validateStr.substring(lParenIndex + 1, rParenIndex);
			validateStr = validateStr.substring(rParenIndex,validateStr.length());

			String argSig = "";
			if (args.trim().length() > 0) {
				String[] argArr = args.split(",");
				for (int i = 0; i < argArr.length; i++) {
					Matcher objMth = objPtn.matcher(argArr[i]);
					if (objMth.find()) 
						argSig += argArr[i].replace(objMth.group(0), objMth.group(1));
					else
						argSig += argArr[i];
					
					if(i != argArr.length-1)
							argSig +=",";
				}
			}
			if (validateStr.trim().equals(")")) {
				returnstr += left + argSig + validateStr;
				break;
			} else {
				returnstr += left + argSig;
				lParenIndex = validateStr.indexOf('(', 0);
				if (lParenIndex != -1)
					rParenIndex = validateStr.indexOf(')', lParenIndex);
				else
					rParenIndex = -1;
			}
		}
		return returnstr;
	}
	
	public static String getMethodName(String functionStr) {
		String reg = "(.+)\\((.*)\\)";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(functionStr);
		if (matcher.find()) {
			String temp = matcher.group(1).trim();
			if (temp.split(" ").length == 2)
				return temp.split(" ")[1];
			else
				return temp;
		} else {
			if (functionStr.trim().split(" ").length == 2)
				return functionStr.trim().split(" ")[1];
			else
				return functionStr.trim();
		}
	}

	public static boolean StringMatch(String matchingVal, String matchingRegex) {
		//String regex = matchingRegex.replace(".", "\\.").replace("*", "(.*)");
		String regex = validateStr(matchingRegex);
		
		//Pattern pattern = Pattern.compile(strictifyMatch(regex));
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(matchingVal);
		return  matcher.find();
	}
	
	public static String validateStr(String str) {
		return str.replaceAll("(%|\\$[a-zA-Z0-9\\.\\-_]+)", "").replaceAll(
				"#|\\{|\\}", "").replace("(", "\\(").replace(")", "\\)").replace("*", "(.*)");
	}
	
	public static String strictifyMatch(String str) {
		String returnStr = "";
		while(str.contains("*")) {
			int leftIndex = str.indexOf("*");
			if(leftIndex >0) 
				returnStr += "(" + str.substring(0, leftIndex) + ")$";
			returnStr += "(.*)";
			if(leftIndex+1 < str.length()-1)
				str = str.substring(leftIndex+1, str.length());
			else 
				break;
		}
		returnStr += "(" +str+ ")$";
		return returnStr;
	}
	
	public static String[] objMethodCall(String str) {
        String[] returnVal = new String[3];
        String reg = "(.+\\.)(.+)+\\((.*)\\)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()) {
            returnVal[0] = matcher.group(1).substring(0,matcher.group(1).length() - 1);
            returnVal[1] = matcher.group(2);
            returnVal[2] = matcher.group(3).trim();
        } 
        else
        	returnVal=null;
        
        return returnVal;
    }

	/**
	 * this method used to check if a method has return value or not
	 * if the method is constructor or the method return type is void
	 * then it will return false, otherwise return true
	 * @return
	 */
	public static boolean hasReturnValue(String methodStr){
		methodStr = methodStr.trim();
		if (!methodStr.startsWith("void ")
				|| methodStr.split("\\s+").length > 1)
			return true;
		else
			return false;
	}
	
	public static boolean matchingStack(Stack<String> events, String runningMethod) {
        if (events!=null && events.peek().contains(runningMethod)) 
        	return true;
            
        return false;
    }
	
	public static String getInvokeMethoSig(Method run) {
		String methodName = run.getReturnType().getName() + " ";
		methodName += run.getDeclaringClass().getName() +"."+run.getName();
		//String methodName = run.getDeclaringClass().toString()+"."+run.getName();
        if(run.getTypeParameters().length ==0) {
        	methodName +="()";
        }else {
        	String paraStr = "(";
        	Type[] paraTypes = run.getParameterTypes();
        	for(int i=0; i<paraTypes.length; i++) {
        		paraStr += paraTypes[i].toString();
        		if(i != paraTypes.length-1)
        			paraStr += ",";
        	}
        	methodName = methodName + paraStr +")";
        }
		return methodName;
	}
	
}