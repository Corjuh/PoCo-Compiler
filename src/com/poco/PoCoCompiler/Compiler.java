//test
package com.poco.PoCoCompiler;

import com.poco.Extractor.Extractor;
import com.poco.Extractor.MethodSignaturesExtract;
import com.poco.Extractor.PointCutExtractor;
import com.poco.PoCoParser.PoCoLexer;
import com.poco.PoCoParser.PoCoParser;
import com.poco.Extractor.Closure;
import com.poco.StaticAnalysis.StaticAnalysis;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Compiler {
    /*
     * COMPILATION OPTIONS
     */
    /** Output verbose information to console */
    private final boolean verboseFlag;
    /** Quit compilation after a certain phase */
    private final String endAfterFlag;

    /*
     * FILES AND FOLDERS
     */
    /** Folder for compiler output */
    private Path outputDir;
    /** Path to main PoCo policy */
    private Path policyFilePath;
    /** Paths to files (jar or class) to be instrumented */
    private Path[] scanFilePaths;
    /** Used to write to the AspectJ file */
    private PrintWriter aspectWriter = null;
    /** Other policies that need to be parsed (found via "import" statements) */
    private LinkedHashSet<String> additionalPolicies = new LinkedHashSet<>();

    /*
     * COMPILATION RESULTS
     */
    /** Name of PoCo policy (e.g. CorysPolicy.poco is "CorysPolicy") */
    private String policyName;
    /** Parse tree generated by the ANTLR grammar */
    private ParseTree parseTree = null;
    /** Regular expressions from PoCo policy */
    private ArrayList<String> extractedREs = null;
    
     /** pointcut info  from PoCo policy */
    ArrayList<String> extractedPCs = new ArrayList<String>();
    LinkedHashSet<String> extractedPtCuts = null;
    //add this in order to generate the different kinds of advices for pointcuts
    LinkedHashSet<String> extractedPtCuts4Results = null;
    /** All method signatures from files in scanFilePaths */
    private LinkedHashSet<String> extractedMethodSignatures = null;
    /** Each RE from the PoCo policy mapped to all matching methods */
    private LinkedHashMap<String, ArrayList<String>> regexMethodMappings = null; 
    private LinkedHashMap<String, ArrayList<String>> pointcutMappings = null;
    /** vars and marcos value will be saved in closure*/
    private Closure closure;

    /**
     * Writes a Collection object to a file, separated by newlines.
     * @param items object adhering to the Collection interface
     * @param savePath Path pointing to the save file location
     */
    public static void writeToFile(Collection<String> items, Path savePath) {
        try (FileWriter writeStream = new FileWriter(savePath.toFile())) {
            for (String item : items) {
                writeStream.write(item);
                writeStream.write('\n');
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void addPolicy(String newPolicy) {
        additionalPolicies.add(newPolicy);
    }

    private void jOut(int indentLevel, String text, Object... args) {
        // Indent to appropriate level
        int numSpaces = indentLevel * 4;
        for (int i = 0; i < numSpaces; i++) {
            aspectWriter.format(" ");
        }

        // Output supplied format string and append newline
        aspectWriter.format(text, args);
        aspectWriter.format("\n");
    }

    /**
     * Writes a hash map to a file. Each item is separated by a newline. Keys are left-justified and their
     * values are indented 4 spaces.
     * @param map LinkedHashMap to write to file (it's much faster to iterate over LinkedHashMaps)
     * @param savePath Path pointing to the save file location
     */
    public static void writeMapToFile(LinkedHashMap<String, ArrayList<String>> map, Path savePath) {
        try (FileWriter writeStream = new FileWriter(savePath.toFile())) {
            for (String key : map.keySet()) {
                writeStream.write(key + ":\n");
                for (String value : map.get(key)) {
                    writeStream.write("    ");
                    writeStream.write(value);
                    writeStream.write('\n');
                }
                writeStream.write('\n');
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Outputs log information to console if the verbose flag has been set (-v command line option).
     * @param format printf-style format string
     * @param args arguments to printf
     */
    private void vOut(String format, Object... args) {
        if (verboseFlag) {
            System.out.printf(format, args);
        }
    }

    /**
     * Constructor. Parses command-line arguments and outputs execution information.
     * @param arguments command-line arguments
     */
    public Compiler(String[] arguments) {
        // Set up command-line option parser (see JOpts library documentation for more information)
        OptionParser optParser = new OptionParser();
        optParser.accepts("extract");
        OptionSpec<String> outputOpt = optParser.accepts("o").withRequiredArg().ofType( String.class ).defaultsTo(Paths.get("").toAbsolutePath().toString());
        OptionSpec<String> scanOpts = optParser.accepts("c").withRequiredArg().ofType( String.class );
        OptionSpec<String> policyArgs = optParser.nonOptions().ofType( String.class );
        optParser.accepts("v");
        OptionSet options = optParser.parse(arguments);

        // User wants verbose output?
        this.verboseFlag = options.has("v");

        // Configure output directory
        this.outputDir = Paths.get(outputOpt.value(options));

        // Get name for PoCo Policy
        if (policyArgs.value(options) == null) {
            System.out.println("ERROR: Please provide at least one PoCo policy file.");
            System.exit(-1);
        }

        // Set up path to policy file and get name
        this.policyFilePath = Paths.get(policyArgs.value(options));
        String policyFileName = policyFilePath .getFileName().toString();
        this.policyName = policyFileName.substring(0, policyFileName.indexOf('.'));

        // Set up list of files to instrument/scan
        this.scanFilePaths = new Path[scanOpts.values(options).size()];
        for (int i = 0; i < scanOpts.values(options).size(); i++) {
            scanFilePaths[i] = Paths.get(scanOpts.values(options).get(i));
        }

        // "--extract" option indicates that the user only wants to extract REs
        if (options.has("extract")) {
            this.endAfterFlag = "extract";
        } else {
            this.endAfterFlag = "";
        }

        // Output execution information
        vOut("PoCo Compiler starting up with the following options:\n");
        if (endAfterFlag.length() > 0) {
            vOut("%s\n  %s\n", "End After:", endAfterFlag);
        }
        vOut("%s\n  %s\n", "PoCo Policy:", policyFilePath.toString());
        vOut("%s\n  %s\n", "Output Dir:", outputDir.toString());
        vOut("%s\n", "Scan Targets:");
        for (Path scanFilePath : scanFilePaths) {
            vOut("  %s\n", scanFilePath.toString());
        }
        if (scanFilePaths.length == 0) {
            vOut("  %s\n", "(None)");
        }
        vOut("\n");
    }

    /**
     * Public-facing method to execute the compilation phases in the correct order.
     */
    public void compile() {
        // Runs through the steps of compilation (parse, extract, mapping)
        this.doParse();
        this.doGenerateClosure();
        this.doExtract();
        this.doStaticAnalysis();

        // User wants to only do extracts
        if (endAfterFlag.equals("extract")) {
            return;
        }

        //this.doMapping();
        this.doGenerateAspectJ();
    }

    /**
     * Parses the supplied PoCo Policy file, if it exists. Otherwise exits with error.
     *
     * Step #1 in compilation process as the parse tree is required by other phases.
     */
    private void doParse() {
        // Parse the specified PoCo policy
        vOut("Parsing PoCo Policy...\n");
        ANTLRInputStream antlrStream = null;

        // Open the PoCo policy file
        try {
            antlrStream = new ANTLRInputStream(new FileInputStream(policyFilePath.toFile()));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.exit(-1);
        }

        // Call lexer, get tokens, pass tokens to parser. Obtain the parseTree for the root-level rule, "policy".
        PoCoLexer lexer = new PoCoLexer(antlrStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PoCoParser parser = new PoCoParser(tokens);
        this.parseTree = parser.policy();
    }

/**
     * Extracts vars and macros that defined before executions,
     * so that the Extractor will be able to get info for generating pointcut
    */
    private void doGenerateClosure() {
        ExtractClosure extractClosure = new ExtractClosure(this.closure);
        extractClosure.visit(parseTree);
        closure =  extractClosure.getClosure();
    }

    /**
     * Extracts the REs from the PoCo policy for use by the mapping functions. Also extracts all method signatures from
     * all to-be-instrumented files.
     *
     * Step #2 in compilation process. Requires doParse() to have been called prior.
     */
    private void doExtract() {
        // Extract REs from PoCo Policy for mapping
        vOut("Extracting REs from policy...\n");
        Extractor regexExtractor = new Extractor();
        regexExtractor.visit(parseTree);
        this.extractedREs = regexExtractor.getMatchStrings();
        PointCutExtractor pcExtractor = new PointCutExtractor(this.closure);
        pcExtractor.visit(parseTree);
        this.extractedPtCuts = pcExtractor.getPCStrings();
        this.extractedPtCuts4Results = pcExtractor.getPCStrs4Results();

        for (String entry : this.extractedPtCuts) {
            extractedPCs.add(entry);
        }
        for (String entry : this.extractedPtCuts4Results) {
            extractedPCs.add(entry);
        }
        // Write REs to a file
        Path policyExtractPath = outputDir.resolve(policyName + "_extracts.txt");
        //writeToFile(extractedREs, policyExtractPath);
        writeToFile(extractedPCs, policyExtractPath);
        // Extract all method signatures from jar/class files
        vOut("Extracting method signatures from scan files...\n");
        this.extractedMethodSignatures = new LinkedHashSet<>();
        for (Path scanFilePath : scanFilePaths) {
            this.extractedMethodSignatures.addAll(new MethodSignaturesExtract(scanFilePath).getMethodSignatures());
        }

        // Write the extracted methods to a file
        Path methodExtractPath = outputDir.resolve(policyName + "_allmethods.txt");
        writeToFile(extractedMethodSignatures, methodExtractPath);
    }

    /**
     * Runs static analysis on policy. doExtract() must have already been called
     */
    private void doStaticAnalysis()
    {
        vOut("Performing static analysis...\n");
        ANTLRInputStream antlrStream = null;

        try {
            antlrStream = new ANTLRInputStream(new FileInputStream(policyFilePath.toFile()));
            StaticAnalysis sa = new StaticAnalysis();
            sa.StaticAnalysis(antlrStream, this.extractedMethodSignatures);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Maps the REs from the PoCo policy to the extracted method signatures.
     *
     * Step #3 in the compilation process. doParse() and doExtract() should be called prior.
     */
    private void doMapping() {
        // Generate mappings from extracted REs -> method signatures
        vOut("Mapping REs from policy to method signatures...\n");
        //RegexMapper mapper = new RegexMapper(extractedREs, extractedMethodSignatures);
        RegexMapper mapper = new RegexMapper(extractedPCs, extractedMethodSignatures);
        mapper.mapRegexes();
        this.regexMethodMappings = mapper.getMappings();

        // Write mappings to a text file
        Path mappingExtractPath = outputDir.resolve(policyName + "_mappings.txt");
        writeMapToFile(regexMethodMappings, mappingExtractPath);
    }

    /**
     *
     * Step #4 in the compilation process.
     */
    private void doGenerateAspectJ() {
        // Generate AspectJ pointcuts according to the mappings
        Path poincutPath = outputDir.resolve("Aspect" + policyName + ".aj");
        String aspectName = "Aspect" + policyName;
        vOut("Generating AspectJ file %s ...\n", poincutPath.getFileName());

        // Open up the AspectJ file for writing
        try {
            aspectWriter = new PrintWriter(poincutPath.toFile());
        } catch (Exception ex) {
            System.out.println("ERROR during pointcut gen");
            System.out.println(ex.getMessage());
            System.exit(-1);
        }
        // Create some class names
        String childPolicyName = policyName;
        outAspectPrologue(aspectName, childPolicyName);

        //add this paragraph for generate pointcut for reflection calls
        //only reflection calls can be made is thru PoCo
        jOut(1, "pointcut PC4Reflection():");
        jOut(2, "call (* Method.invoke(Object, Object...)) && !within(com.poco.Promoter);\n");
        jOut(1, "Object around(): PC4Reflection()   { ");
        jOut(2, "return new SRE(null,\".\"); ");
        jOut(1, "}\n");
        int pointcutNum = 0;
        for (String entry : this.extractedPtCuts) {
            //argTypeList:  Integer, String
            String argList4PC = "";
            //argList4PC :  Integer value0, String value1
            String argList4Call = "";
            //argList4Call: value0, value1
            String argTypeList = "";
            //aspect will only monitor the values that matches
            String monitorVals = "";
            String[] argsList = getArgsLstArray(entry);
            if (argsList != null) {
                int count = 0;
                for (int i = 0; i < argsList.length; i++) {
                    String str = getArgsType(argsList[i], 1);
                    if (!str.contains("..")) {
                        argTypeList += getArgsType(argsList[i], 1);
                        argList4PC += getArgsType(argsList[i], 1) + " value" + count;
                        if (getArgsMatchVal(argsList[i]) != null) {
                            monitorVals += argList4PC + getArgsMatchVal(argsList[i]);
                            if (i != argsList.length - 1) monitorVals += ",";
                        }
                        argList4Call += "value" + count++;

                        if (i != argsList.length - 1) {
                            argTypeList += ",";
                            argList4PC += ",";
                            argList4Call += ",";
                        }
                    }
                }
                argTypeList = trimLastPunctuation(argTypeList, ",");
                argList4PC = trimLastPunctuation(argList4PC, ",");
                argList4Call = trimLastPunctuation(argList4Call, ",");
                monitorVals = trimLastPunctuation(monitorVals, ",");
            }
            //if argTypeList is empty then no need for define arugment for pointcut
            if (argTypeList != null) {
                jOut(1, "pointcut PointCut%d(%s):", pointcutNum, argList4PC);
                String callStr = getPCMethodName(entry);
                if (callStr.substring(0,2).equals("$$")) {
                    if (closure.getContext(callStr.substring(2, callStr.length())) != null)
                        callStr = closure.getContext(callStr.substring(2, callStr.length()));
                }
                if (argTypeList != "") {
                    callStr += "(" + argTypeList + ")";
                    jOut(2, "call(%s) && args(%s);\n", callStr, argList4Call);
                } else {
                    if (entry.contains("(..)")) {
                        jOut(2, "call(%s);\n", callStr + "(..)");
                    } else {
                        jOut(2, "call(%s);\n", callStr);
                    }
                }
                outAdvicePrologue("PointCut" + pointcutNum, argList4PC, argList4Call, monitorVals);
                pointcutNum++;
            } else {
                jOut(1, "pointcut PointCut%d():", pointcutNum);
                String callStr = getPCMethodName(entry);
                jOut(2, "call(%s(..));\n", callStr);
                outAdvicePrologue("PointCut" + pointcutNum, argList4PC, argList4Call, monitorVals);
                pointcutNum++;
            }
        }

        for (String entry : this.extractedPtCuts4Results) {
            String argList4PC = "";
            String argList4Call = "";
            String argTypeList = "";
            String monitorVals = "";
            String[] argsList = getArgsLstArray(entry);
            if (argsList != null) {
                int count = 0;
                for (int i = 0; i < argsList.length; i++) {
                    String str = getArgsType(argsList[i], 1);
                    if (!str.contains("..")) {
                        argTypeList += getArgsType(argsList[i], 1);
                        argList4PC += getArgsType(argsList[i], 1) + " value" + count;
                        if (getArgsMatchVal(argsList[i]) != null) {
                            monitorVals += argList4PC + getArgsMatchVal(argsList[i]);
                            if (i != argsList.length - 1) monitorVals += ",";
                        }
                        argList4Call += "value" + count++;
                        if (i != argsList.length - 1) {
                            argTypeList += ",";
                            argList4PC += ",";
                            argList4Call += ",";
                        }
                    }
                }
                argTypeList = trimLastPunctuation(argTypeList, ",");
                argList4PC = trimLastPunctuation(argList4PC, ",");
                argList4Call = trimLastPunctuation(argList4Call, ",");
                monitorVals = trimLastPunctuation(monitorVals, ",");
            }
            //if argTypeList is empty then no need for define arugment for pointcut
            if (argTypeList != null) {
                jOut(1, "pointcut PointCut%d(%s):", pointcutNum, argList4PC);
                String callStr = getPCMethodName(entry);
                if (argTypeList != "") {
                    callStr += "(" + argTypeList + ")";
                    jOut(2, "call(%s) && args(%s);\n", callStr, argList4Call);
                } else {
                    jOut(2, "call(%s);\n", callStr);
                }
                outAdvicePrologue4Result("PointCut" + pointcutNum, argList4PC, argList4Call, monitorVals);
                pointcutNum++;
            } else {
                jOut(1, "pointcut PointCut%d():", pointcutNum);
                String callStr = getPCMethodName(entry);
                jOut(2, "call(%s(..));\n", callStr);
                outAdvicePrologue4Result("PointCut" + pointcutNum, argList4PC, argList4Call, monitorVals);
                pointcutNum++;
            }
        }
        // Generate policy classes
        PolicyVisitor pvisitor = new PolicyVisitor(aspectWriter, 1, this.closure);
        pvisitor.visit(parseTree);

        if (pvisitor.hasTransation()) {
            createTransUtil(pvisitor.getTransactions());
        }
        outAspectEpilogue();
        aspectWriter.close();
        aspectWriter = null;
    }

    private String trimLastPunctuation(String str, String punctuation) {
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

    private void createTransUtil(String str) {
        String fileName = policyName + "_Utils";
        Path path = outputDir.resolve(fileName + ".java");

        StringBuffer stringBuffer = new StringBuffer();
        //write the package info
        ArrayList<String> packs = getUtilPackages(str);
        if (packs != null) {
            for (Iterator<String> it = packs.iterator(); it.hasNext(); ) {
                stringBuffer = stringBuffer.append(it.next());
                stringBuffer = stringBuffer.append(System.getProperty("line.separator"));
            }
            stringBuffer = stringBuffer.append(System.getProperty("line.separator"));
        }
        //write the class name
        stringBuffer = stringBuffer.append("public class " + fileName + "{");
        stringBuffer = stringBuffer.append(System.getProperty("line.separator"));
        //add methods
        ArrayList<String> methods = getUtilMethods(str);
        if (methods != null) {
            for (Iterator<String> it = methods.iterator(); it.hasNext(); ) {
                stringBuffer = stringBuffer.append(it.next());
                stringBuffer = stringBuffer.append(System.getProperty("line.separator"));
            }
        }
        stringBuffer = stringBuffer.append("}");
        //write into "policyName_Utils.java"
        try {
            FileWriter fw = new FileWriter(path.toString());
            fw.write(stringBuffer.toString());
            fw.flush();
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ArrayList<String> getUtilPackages(String str) {
        ArrayList<String> packages = new ArrayList<>();
        String reg = "\\s*import\\s+(.+);";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            packages.add("import " + matcher.group(1).trim() + ";");
        }
        return packages;
    }

    private ArrayList<String> getUtilMethods(String str) {
        ArrayList<Integer> methodIndex = new ArrayList<>();
        ArrayList<String> methods = new ArrayList<>();
        String reg = "\\s*(public\\s+(static)?\\s+\\w+\\s+\\w+\\s*\\(.*?\\))\\s*";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find())
            methodIndex.add(matcher.start());

        if (methodIndex.size() > 0) {
            int firstIndex = methodIndex.get(0);
            methodIndex.remove(0);
            for (Iterator<Integer> it = methodIndex.iterator(); it.hasNext(); ) {
                int nextIndex = it.next();
                methods.add("\t" + str.substring(firstIndex, nextIndex).trim());
                firstIndex = nextIndex;
            }
            methods.add("\t" + str.substring(firstIndex, str.length()).trim());
        }

        return methods;
    }

    private void outAspectPrologue(String aspectName, String childName) {
        jOut(0, "import com.poco.PoCoRuntime.*;");
        jOut(0, "import java.lang.reflect.Method;\n");
        jOut(0, "public aspect %s {", aspectName);
        jOut(1, "private DummyRootPolicy root = new DummyRootPolicy( new %s() );\n", childName);
    }

    private void outAspectEpilogue() {
        jOut(0, "}");
    }

    private void outAdvicePrologue(String pointcutName, String aroundlist, String arglist, String monitorVal) {
         /* aroundlist: String value0,int value1; arglist: value0,value1; monitorVal String value0$*.class$*/
        if (monitorVal != null && monitorVal.length() > 0)
            jOut(1, "Object around(%s): %s(%s) {", aroundlist, pointcutName, arglist);
        else
            jOut(1, "Object around(): %s() {", pointcutName);

        if (monitorVal != null && monitorVal.length() > 0) {
            String conditionState = genCoditionStatements(monitorVal);
            if (conditionState != null && conditionState.length() > 0) {
                jOut(2, "if (" + conditionState + ") {");
                jOut(3, "root.queryAction(new Event(thisJoinPoint));");
                jOut(3, "return proceed(%s);", arglist);
                jOut(2, "}");
                jOut(2, "else");
                jOut(3, "return proceed(%s);", arglist);
                jOut(1, "}\n");
            } else {
                jOut(2, "root.queryAction(new Event(thisJoinPoint));");
                jOut(2, "return proceed(%s);", arglist);
                jOut(1, "}\n");
            }
        } else {
            jOut(2, "root.queryAction(new Event(thisJoinPoint));");
            jOut(2, "return proceed();");
            jOut(1, "}\n");
        }
    }

    private String genCoditionStatements(String matchStrs) {
        if (matchStrs == null || matchStrs.equals(""))
            return null;
        else {
            String returnStr = "";
            String[] typeValArray = matchStrs.split(",");
            if (typeValArray != null) {
                String reg1 = "(.+)\\$\\$(.+)\\$\\$";
                String reg2 = "(.+)\\$\\$(.+)";
                Pattern pattern1 = Pattern.compile(reg1);
                Pattern pattern2 = Pattern.compile(reg2);
                Matcher matcher1;
                Matcher matcher2;
                //String value0$*.class$ ||String value0.$ip ||String value0 ||String value0$*.class$, int value1$aa$
                for (int i = 0; i < typeValArray.length; i++) {
                    matcher1 = pattern1.matcher(typeValArray[i]);
                    matcher2 = pattern2.matcher(typeValArray[i]);
                    if (matcher1.find()) {
                        String[] typValName = matcher1.group(1).toString().trim().split(" ");
                        String typ = typValName[0];
                        String valName = typValName[1];
                        String temp = genCoditionStatement(typ, valName, matcher1.group(2).toString());
                        if (temp != null)
                            returnStr += temp + " && ";
                    } else if (matcher2.find()) {
                        String[] typValName = matcher2.group(1).toString().trim().split(" ");
                        String typ = typValName[0];
                        String valName = typValName[1];
                        //closure.getContext(matcher1.group(2).toString());
                        String str = closure.getContext(matcher2.group(2).toString());
                        if (str != null && str.equals("%"))
                            str = "\\.*";
                        String temp = genCoditionStatement(typ, valName, str);
                        if (temp != null)
                            returnStr += temp + " && ";
                    } else if (typeValArray[i].contains(" ")) {
                        String typ = typeValArray[i].substring(0, typeValArray[i].indexOf(' '));
                        String val = typeValArray[i].substring(typeValArray[i].indexOf(' ') + 1);

                        String valName = getArgsType(val, 1);
                        String matchVal = getArgsType(val, 2);
                        String temp =  genCoditionStatement(typ, valName, matchVal);
                        if (temp != null)
                            returnStr += temp + " && ";
                    }
                }
            }
            return trimLastPunctuation(returnStr, " && ");
        }
    }

    private String genCoditionStatement(String type, String valName, String matchVal) {
        if (matchVal != null && matchVal.length() > 0) {
            String str = "";
            switch (type) {
                case "int":
                case "short":
                    str = "new Integer(" + valName + ").toString()";
                    break;
                case "long":
                case "double":
                    str = "String.valueOf(" + valName + ")";
                    break;
                case "float":
                    str = "Float.toString(" + valName + ")";
                    break;
                case "boolean":
                    str = "Boolean.toString(" + valName + ")";
                    break;
                case "char":
                    str = "Character.toString(" + valName + ")";
                    break;
                default:
                    str = "String.valueOf(" + valName + ")";
            }
            return "SREUtil.StringMatch(" + str + ", \"" + matchVal + "\")";
        }
        return null;
    }

    private void outAdvicePrologue4Result(String pointcutName, String aroundlist, String arglist, String monitorVal) {
        if (monitorVal != null && monitorVal.length() > 0)
            jOut(1, "Object around(%s): %s(%s) {", aroundlist, pointcutName, arglist);
        else
            jOut(1, "Object around(): %s() {", pointcutName);
        if (monitorVal != null && monitorVal.length() > 0) {
            String conditionState = genCoditionStatements(monitorVal);
            if (conditionState.length() > 0) {
                jOut(2, "if (" + conditionState + " ) {");
                jOut(3, "Object ret = proceed(%s);", arglist);
                jOut(3, "Event event = new Event(thisJoinPoint);");
                jOut(3, "event.eventType = \"Result\";");
                jOut(3, "event.setResult(ret);");
                jOut(3, "root.queryAction(event);");
                jOut(3, "return ret;");
                jOut(2, "}");
                jOut(2, "else");
                jOut(3, "return proceed(%s);", arglist);
                jOut(1, "}\n");
            } else {
                jOut(2, "Object ret = proceed(%s);", arglist);
                outAdviceEpilogue4Result();
            }
        } else {
            jOut(2, "Object ret = proceed();");
            outAdviceEpilogue4Result();
        }
    }

    private void outAdviceEpilogue4Result() {
        jOut(2, "Event event = new Event(thisJoinPoint);");
        jOut(2, "event.eventType = \"Result\";");
        jOut(2, "event.setResult(ret);");
        jOut(2, "root.queryAction(event);");
        jOut(2, "return ret;");
        jOut(1, "}\n");
    }


    public static void main(String[] args) {
        Compiler compiler = new Compiler(args);
        compiler.compile();
    }

    private String getPCMethodName(String str) {
        int index = str.indexOf('(');
        if (index == -1)
            return str;
        else {
            return str.substring(0, index);
        }
    }

    private String[] getArgsLstArray(String str) {
        // File.new(String$*.class$)(int$1$)
        String argList = "";
        int index = str.indexOf('(', 0);
        while (index != -1) {
            int index2 = str.indexOf(')', 0);
            if (index2 > index) {
                argList += str.substring(index + 1, index2) + ",";
                if (index2 + 1 < str.length()) {
                    str = str.substring(index2 + 1, str.length());
                    index = str.indexOf('(', 0);
                } else
                    break;
            } else
                break;
        }
        if (argList.length() > 0)
            return argList.split(",");
        else return null;
    }

    private String getArgsType(String str, int index) {
        //String$*.class$ || int$1$ || String$ip ||String
        String reg = "(.+)(\\$\\$(.+)\\$\\$)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find())
            return matcher.group(index).toString().trim();
        else {
            reg = "(.+)(\\$$.+)";
            pattern = Pattern.compile(reg);
            matcher = pattern.matcher(str);
            if (matcher.find()) {
                return matcher.group(index).toString().trim();
            }
        }
        if (index == 1)
            return str;
        else
            return null;
    }

    private String getArgsMatchVal(String str) {
        //String$*.class$ || int$1$ || String$ip ||String
        String reg = "(.+)(\\$\\$(.+)\\$\\$)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(str);
        //Static match
        if (str != null) {
            if (matcher.find()) {
                return matcher.group(2).toString().trim();
            } else {
                reg = "(.+)(\\$.+)";
                pattern = Pattern.compile(reg);
                matcher = pattern.matcher(str);
                if (matcher.find()) {
                    return matcher.group(2).toString().trim();
                }
            }
        }
        return null;
    }

    private String getArgType(String str) {
        int index = str.indexOf(':');
        if (index == -1)
            return null;
        else {
            return str.substring(0, index);
        }
    }
}
