package com.poco.PoCoCompiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by yan on 11/11/15.
 */
public class AbstractActions {

    public static ArrayList<String> handleAbcCase(String abaction) {
        String funName = PoCoUtils.getMtdName(abaction);
        ArrayList<String> concreteMtds = new ArrayList<>();
        BufferedReader bufr = null;
        try {
            bufr = new BufferedReader(new FileReader(funName + ".txt"));
            String actionStr = null;
            while ((actionStr = bufr.readLine()) != null) {
                String temp = PoCoUtils.formatFuncRetTyp(actionStr.trim());
                temp = genConcreteMtdStr(abaction, temp);
                concreteMtds.add(temp);
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to open " + funName + ".txt");
        } finally {
            if (bufr != null)
                try {
                    bufr.close();
                } catch (IOException e) {
                    throw new RuntimeException("failed to open " + funName + ".txt");
                }
        }
        return concreteMtds;
    }

    public static String genConcreteMtdStr(String abaction, String conCreteMtd) {
        //abaction: abs_creatFile(#java.lang.String{%.class})
        //conCreteMtd: java.io.FileWriter.new(java.lang.String,..)

        String absArgStr = PoCoUtils.getMethodArgLs(abaction);
        if (absArgStr == null || absArgStr.trim().length() == 0)
            return conCreteMtd;

        String retTyp = PoCoUtils.getMethodRtnTyp(conCreteMtd);
        String funName = PoCoUtils.getMtdName(conCreteMtd);

        String[] absArgs = absArgStr.split(",");
        String[] conArgs = PoCoUtils.getMethodArgLs(conCreteMtd).split(",");

        StringBuilder sb = new StringBuilder();
        int absArgIndex = 0;
        for (int i = 0; i < conArgs.length; i++) {
            // argument matching case
            if (conArgs[i].startsWith("#")) {
                if(absArgIndex>=absArgs.length)
                    throw new RuntimeException("Concrete method: " + conCreteMtd + " failed to match " + abaction + "! Please check!");
                else {
                    String val = PoCoUtils.getObjVal(absArgs[absArgIndex++]);
                    sb.append(conArgs[i] + "{" + val + "}");
                }
            } else {
                sb.append(conArgs[i]);
            }
            if (i != conArgs.length - 1)
                sb.append(",");
        }
        if (retTyp == null)
            return funName + "(" + sb.toString() + ")";
        else
            return retTyp + " " + funName + "(" + sb.toString() + ")";

    }
}
