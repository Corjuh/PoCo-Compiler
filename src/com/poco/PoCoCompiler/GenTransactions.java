package com.poco.PoCoCompiler;

import com.poco.PoCoParser.PoCoParser;
import com.poco.PoCoParser.PoCoParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yan on 10/4/15.
 */
public class GenTransactions extends PoCoParserBaseVisitor<Void> {
    //use to save the transactions that need to be added into Util
    private String policName = "";
    private Path transPath = null;

    public GenTransactions(Path outputDir) {
        this.transPath = outputDir;
    }

    @Override
    public Void visitPocopol(PoCoParser.PocopolContext ctx) {
        policName = ctx.id().getText();
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitTransaction(PoCoParser.TransactionContext ctx) {
        String transaction = ctx.transbody().getText();
        if (transaction != null && transaction.length() > 15) {
            transaction = transaction.substring(0, transaction.length() - 15);
            genPolicyTranFile(transaction);
        }
        return null;
    }

    private void genPolicyTranFile(String trans) {
        //System.out.println("Generating policy's transactions file: "+ policName + "_Trans.java...");
        Path path = transPath.resolve(policName + "_Trans.java");

        StringBuffer stringBuffer = new StringBuffer();
        //write the package info
        HashSet<String> packs = getUtilPackages(trans);
        if (packs != null) {
            for (Iterator<String> it = packs.iterator(); it.hasNext(); ) {
                stringBuffer = stringBuffer.append(it.next());
                stringBuffer = stringBuffer.append(System.getProperty("line.separator"));
            }
            stringBuffer = stringBuffer.append(System.getProperty("line.separator"));
        }

        //write the class name
        stringBuffer = stringBuffer.append("public class " +policName + "_Trans {");
        stringBuffer = stringBuffer.append(System.getProperty("line.separator"));

        //add methods
        ArrayList<String> methods = getUtilMethods(trans);
        if (methods != null) {
            for (Iterator<String> it = methods.iterator(); it.hasNext(); ) {
                stringBuffer = stringBuffer.append(it.next());
                stringBuffer = stringBuffer.append(System.getProperty("line.separator"));
            }
        }
        stringBuffer = stringBuffer.append("}");
        try {
            FileWriter fw = new FileWriter(path.toString());
            fw.write(stringBuffer.toString());
            fw.flush();
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private HashSet<String> getUtilPackages(String trans) {
        HashSet<String> packages = new HashSet<String>();
        String reg = "\\s*import\\s+(.+);";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(trans);
        while (matcher.find())
            packages.add("import " + matcher.group(1).trim() + ";");
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
