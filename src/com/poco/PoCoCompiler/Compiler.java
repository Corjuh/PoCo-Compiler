package com.poco.PoCoCompiler;

import com.poco.Extractor.*;
import com.poco.PoCoParser.PoCoLexer;
import com.poco.PoCoParser.PoCoParser;
import com.poco.StaticAnalysis.StaticAnalysis;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compiler {
    /*
     * COMPILATION OPTIONS
     */
    /**
     * Output verbose information to console
     */
    private final boolean verboseFlag;
    /**
     * Quit compilation after a certain phase
     */
    private final String endAfterFlag;

    /*
     * FILES AND FOLDERS
     */
    /**
     * Folder for compiler output
     */
    private Path outputDir;
    /**
     * Path to main PoCo policy
     */
    private Path policyFilePath;
    /**
     * Paths to files (jar or class) to be instrumented
     */
    private Path[] scanFilePaths;
    /**
     * Used to write to the AspectJ file
     */
    private PrintWriter aspectWriter = null;
    /**
     * Other policies that need to be parsed (found via "import" statements)
     */
    private LinkedHashSet<String> additionalPolicies = new LinkedHashSet<>();

    /**
     * Name of PoCo policy (e.g. CorysPolicy.poco is "CorysPolicy")
     */
//    private String policyName;
//    private String poRootName;
    /**
     * Parse tree generated by the ANTLR grammar
     */
    private ParseTree parseTree = null;
    /**
     * Regular expressions from PoCo policy
     */
    private ArrayList<String> extractedREs = null;

    private PointCutExtractor pcExtractor;

    //add this in order to generate the different kinds of advices for pointcuts
//    private HashMap<String, HashMap<String, String>> extractedPts;
//    private HashMap<String, HashMap<String, String>> extractedPts4Prom;
//    private HashMap<String, HashMap<String, String>> extractedPts4Res;
//
//    private HashMap<String, PolicyTreeNode> policy2Properities;

    /**
     * All method signatures from files in scanFilePaths
     */
    private LinkedHashSet<String> extractedMethodSignatures = null;
    /**
     * Each RE from the PoCo policy mapped to all matching methods
     */
    private LinkedHashMap<String, ArrayList<String>> regexMethodMappings = null;
    private LinkedHashMap<String, ArrayList<String>> pointcutMappings = null;
    /**
     * vars and marcos value will be saved in closure
     */
    private Closure closure;
    private Hashtable<String, String> monitoredPC = new Hashtable<String, String>();
    private HashSet<String> varNeedBind;

    public static void main(String[] args) {
        Compiler compiler = new Compiler(args);
        compiler.compile();
    }

    /**
     * Writes a Collection object to a file, separated by newlines.
     *
     * @param items    object adhering to the Collection interface
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
     *
     * @param map      LinkedHashMap to write to file (it's much faster to iterate over LinkedHashMaps)
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
     *
     * @param format printf-style format string
     * @param args   arguments to printf
     */
    private void vOut(String format, Object... args) {
        if (verboseFlag) {
            System.out.printf(format, args);
        }
    }

    /**
     * Constructor. Parses command-line arguments and outputs execution information.
     *
     * @param arguments command-line arguments
     */
    public Compiler(String[] arguments) {
        // Set up command-line option parser (see JOpts library documentation for more information)
        OptionParser optParser = new OptionParser();
        optParser.accepts("extract");
        OptionSpec<String> outputOpt = optParser.accepts("o").withRequiredArg().ofType(String.class).defaultsTo(Paths.get("").toAbsolutePath().toString());
        OptionSpec<String> scanOpts = optParser.accepts("c").withRequiredArg().ofType(String.class);
        OptionSpec<String> policyArgs = optParser.nonOptions().ofType(String.class);
        optParser.accepts("v");
        OptionSet options = optParser.parse(arguments);

        // User wants verbose output?  Yan modified
        this.verboseFlag = true; //options.has("v");

        // Configure output directory
        this.outputDir = Paths.get(outputOpt.value(options));

        // Get name for PoCo Policy
        //Yan disabled these three lines of code temporarily
        /*if (policyArgs.value(options) == null) {
            System.out.println("ERROR: Please provide at least one PoCo policy file.");
            System.exit(-1);
        }*/

        // Set up path to policy file and get name
        //Yan disabled these three lines of code temporarily
        /*this.policyFilePath = Paths.get(policyArgs.value(options));
        String policyFileName = policyFilePath .getFileName().toString();
        this.policyName = policyFileName.substring(0, policyFileName.indexOf('.'));*/

        //Yan add these three lines of code temporarily
        this.policyFilePath = Paths.get("/Users/yan/Desktop/DisSysCalls.poco");
        String policyFileName = "DisSysCalls.poco";
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

        this.doGenerateAspectJ();
    }

    /**
     * <p/>
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
        try {
            ExtractClosure extractClosure = new ExtractClosure();
            extractClosure.visit(parseTree);
            closure = extractClosure.getClosure();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Extracts the REs from the PoCo policy for use by the mapping functions. Also extracts all method signatures from
     * all to-be-instrumented files.
     * <p/>
     * Step #2 in compilation process. Requires doParse() to have been called prior.
     */
    private void doExtract() {
        // Extract REs from PoCo Policy for mapping
        vOut("Extracting REs from policy...\n");
        Extractor regexExtractor = new Extractor();
        regexExtractor.visit(parseTree);
        this.extractedREs = regexExtractor.getMatchStrings();
        pcExtractor = new PointCutExtractor(this.closure);
        pcExtractor.visit(parseTree);
        this.extractedMethodSignatures = new LinkedHashSet<>();
        //Modify temporarily
        /*for (Path scanFilePath : scanFilePaths) {
            this.extractedMethodSignatures.addAll(new MethodSignaturesExtract(scanFilePath).getMethodSignatures());
        }*/
        Path myPath = Paths.get("/Users/yan/Desktop/Test/RuntimeDemo.class");
        //this.extractedMethodSignatures.addAll(new MethodSignaturesExtract(myPath).getMethodSignatures());
        // Write the extracted methods to a file
        //Path methodExtractPath = outputDir.resolve(policyName + "_allmethods.txt");
        //writeToFile(extractedMethodSignatures, methodExtractPath);
    }

    /**
     * Runs static analysis on policy. doExtract() must have already been called
     */
    private void doStaticAnalysis() {
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
     * Step #4 in the compilation process.
     */
    private void doGenerateAspectJ() {
        String rootName = pcExtractor.getRoot();

        //if the root is not explicitly declared, then output the error message and terminate.
//        if(rootName == null) {
//            System.err.println("There is not root to be delcared in your policy, please check");
//            System.exit(-11);
//        }
//
        rootName = "root";

        // Generate AspectJ pointcuts according to the mappings
        Path poincutPath = outputDir.resolve("Aspect" + rootName + ".aj");
        vOut("Generating AspectJ file %s ...\n", poincutPath.getFileName());

        // Open up the AspectJ file for writing
        try {
            aspectWriter = new PrintWriter(poincutPath.toFile());
        } catch (Exception ex) {
            System.out.println("ERROR during pointcut gen");
            System.out.println(ex.getMessage());
            System.exit(-1);
        }

        // Generate policy classes
        GenAspectJFile genAJfile = new GenAspectJFile(aspectWriter, 0, this.closure, pcExtractor);
        genAJfile.visit(parseTree);

        PointCutGen pointCutGen = new PointCutGen(aspectWriter, 0, this.closure, pcExtractor);
        pointCutGen.GenAspectJ();

        // Generate policy classes
        PolicyVisitor pvisitor = new PolicyVisitor(aspectWriter, 1, this.closure);
        pvisitor.visit(parseTree);
        if (pvisitor.hasTransation())
            createTransUtil(pvisitor.getTransactions());
        outAspectEpilogue();
        aspectWriter.close();
        aspectWriter = null;
    }

    private void outAspectEpilogue() {
        jOut(0, "}");
    }

    private void createTransUtil(String str) {
        //String fileName = policyName + "_Utils";
        String fileName = "_Utils";
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

}