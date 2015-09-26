package com.poco.PoCoRuntime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
	private static boolean successlyPromoted = false;
	private static boolean isMethodValid = true;

	public static boolean isSuccesslyPromoted() {
		return successlyPromoted;
	}

	public static void resetSuccesslyPromoted() {
		successlyPromoted = false;
	}

	/**
	 *
	 * @param pocoString
	 *            the function want to be invoked
	 * @throws Exception
	 */
	public static void Reflect(Object obj, String pocoString, Object[] objects,
							   String[] objInfos) throws Exception {
		List<String> toExecute;
		toExecute = Parse(pocoString);

		// if the method is invalid, directly return, so we can continue loading
		// the next concrete method
		if (!isMethodValid)
			return;

		String className = null;
		String methodName = null;
		// this is the case where we invoke an object's method
		if (obj != null) {
			className = obj.getClass().getName();
			methodName = objInfos[2];
		} else {
			// class we are calling
			className = toExecute.get(0).trim();
			// the name of function the to be invoked
			methodName = toExecute.get(1).trim();
		}

		ArrayList<ReflectParameter> rps = null;
		if (toExecute.size() == 3) {
			String[] params = toExecute.get(2).split(","); // #java.lang.Integer{42}
			rps = ParseParameters(params);
		}

		if (!isMethodValid)
			return;

		ReflectExecute(obj, className, methodName, rps, objects);
	}

	private static void ReflectExecute(Object obj, String className,
									   String methodName, ArrayList<ReflectParameter> params, Object[] objs)
			throws Exception {
		try {
			boolean isfound = false;
			// get rid of the return type if the string contains it
			if (className.trim().split(" ").length == 2)
				className = className.trim().split(" ")[1];
			Class cls1 = Class.forName(className);

			int paramCounts = 0;
			if (params != null)
				paramCounts = params.size();

			// the constructor case
			if (methodName.equals("new")) {

				handleConstructorCase(params, objs, isfound, cls1, paramCounts);
			} else {
				handleMethodCase(obj, methodName, params, objs, isfound, cls1,
						paramCounts);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void handleMethodCase(Object obj, String methodName,
										 ArrayList<ReflectParameter> params, Object[] objs, boolean isfound,
										 Class cls1, int paramCounts) throws IllegalAccessException,
			InvocationTargetException, InstantiationException {
		Method[] methods = cls1.getMethods();
		Method theMethod = null;

		for (Method method : methods) {
			// if find the method name
			if (method.getName().equals(methodName)) {

				Type[] methodParams = method.getGenericParameterTypes();
				if (methodParams.length == paramCounts) {
					isfound = true;
					if (paramCounts != 0) {
						int i = 0;
						for (Iterator<ReflectParameter> it = params.iterator(); it
								.hasNext();) {
							String methodParam = GetTypeName(methodParams[i++]
									.toString());
							// if the type is not the primitive type
							if (!methodParam.equals(it.next()
									.GetParameterType())) {
								isfound = false;
								break;
							}
						}
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
			successlyPromoted = true;
			if (obj != null)
				theMethod.invoke(obj, objs);
			else {
				// need check static or not
				if (Modifier.isStatic(theMethod.getModifiers()))
					theMethod.invoke(null, objs);
				else
					theMethod.invoke(cls1.newInstance(), objs);
			}
		} else {
			// return and try to locate next easy-to-find method
			return;
			// System.out
			// .println("Sorry, cannot find the right method to pomote, "
			// + "please check the policy definition!");
		}
	}

	private static void handleConstructorCase(
			ArrayList<ReflectParameter> params, Object[] objs, boolean isfound,
			Class cls1, int paramCounts) throws InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Constructor[] construs = cls1.getConstructors();
		Constructor theConstructor = null;

		for (Constructor con : construs) {
			if (con.getParameterTypes().length == paramCounts)
				isfound = true;
			if (paramCounts != 0) {
				int i = 0;
				Type[] conParams = con.getGenericParameterTypes();
				for (Iterator<ReflectParameter> it = params.iterator(); it
						.hasNext();) {
					String conParam = GetTypeName(conParams[i].toString());

					if (!conParam
							.equalsIgnoreCase(it.next().GetParameterType())) {
						isfound = false;
						break;
					}
					i++;
				}
			}
			if (isfound) {
				theConstructor = con;
				break;
			}
		}
		if (isfound) {
			successlyPromoted = true;
			theConstructor.newInstance(objs);
		} else {
			// return and try to locate next easy-to-find method
			return;
			// System.out
			// .println("Sorry, failed to find the right constructor to initialize "
			// + "the class, please check the policy definition!");
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
			int lParen = toParse.indexOf('(');
			int rParen = toParse.lastIndexOf(')');
			String methodName = toParse.substring(0, lParen);
			int dotIndex = methodName.lastIndexOf('.');
			String info = methodName.substring(0, dotIndex);
			String[] infos = info.split("\\s+");
			// package and class name
			tokens.add(infos[infos.length - 1]);
			// method name
			tokens.add(methodName.substring(dotIndex + 1));
			// parameter list if not null
			if (rParen - lParen > 1)
				tokens.add(toParse.substring(lParen + 1, rParen));
		} else {
			isMethodValid = false;
			return null;// throw new
			// Exception("the function you want to invoked is invalid!");
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
			// first check if the parameter is an object or not, if so
			// direct get the object value
			if (matcher.find()) {
				int sharp = params[i].indexOf('#');
				int lCBracet = params[i].indexOf('{');
				String paraTyp = params[i].substring(sharp + 1, lCBracet);
				rps.add(new ReflectParameter(paraTyp, null));
			} else {
				isMethodValid = false;
				// throw new Exception("the parameter you entered is invalid");
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

}