package com.poco.PoCoCompiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that generates a mapping of regular expressions to method signatures.
 */
public class RegexMapper {
    private final Collection<String> regexes;
    private final Collection<String> signatures;
    private final LinkedHashMap<String, ArrayList<String>> mappings;
    private boolean mapGenerated = false;

    public RegexMapper(Collection<String> regexes, Collection<String> signatures) {
        this.regexes = regexes;
        this.signatures = signatures;
        this.mappings = new LinkedHashMap<>(regexes.size());
    }

    public void mapRegexes() {
        if (mapGenerated) {
            return;
        }

        for(String regex : regexes) {
            Pattern pat = Pattern.compile(regex);
            ArrayList<String> mappedSignatures = new ArrayList<>();

            for(String signature : signatures) {
                Matcher match = pat.matcher(signature);
                if(match.find()) {
                    mappedSignatures.add(signature);
                }
            }

            mappings.put(regex, mappedSignatures);
        }

        mapGenerated = true;
    }

    public LinkedHashMap<String, ArrayList<String>> getMappings() {
        return mappings;
    }


}
