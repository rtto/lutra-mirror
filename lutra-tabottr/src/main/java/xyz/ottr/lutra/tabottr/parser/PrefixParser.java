package xyz.ottr.lutra.tabottr.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.tabottr.model.Instruction;
import xyz.ottr.lutra.tabottr.model.PrefixInstruction;


public final class PrefixParser {

    private static final PrefixMapping stdPrefixes = PrefixMapping.Standard; // TODO use OTTR standard

    // TODO must be a java method for this?
    private static <T> Set<T> union (Set<T> a, Set<T> b) {
        Set<T> union = new HashSet<>();
        union.addAll(a);
        union.addAll(b);
        return union;
    }

    private static Set<Map.Entry<String, String>> getPrefixPairs(PrefixMapping prefixes) {
        return prefixes.getNsPrefixMap().entrySet();
    }

    private static Result<PrefixMapping> mergePrefixResults(Result<PrefixMapping> base, Result<PrefixMapping> add) {
        Result<Set<Map.Entry<String, String>>> pairs = Result.zip(base, add, (px1, px2) -> union(getPrefixPairs(px1), getPrefixPairs(px2)));
        Result<PrefixMapping> prefixes = pairs.flatMap(PrefixParser::buildPrefixMapping);
        return prefixes;
    }

    /**
     * Builds a PrefixMapping containing the given prefix pairs, returns an empty Result if there is a
     * conflict in the prefix pairs, i.e,. if one prefix has two different namespaces.
     * @param pairs a list of pairs of strings (prefix, namespace)
     * @return a Result containing the PrefixMapping or an empty Result with an error message.
     */
    private static Result<PrefixMapping> buildPrefixMapping(Collection<Map.Entry<String,String>> pairs) {

        List<Message> errors = new ArrayList<>();
        // build a map of the pairs, while collect conflicts if there are
        Map<String, String> pxMap = pairs.stream()
                .collect(Collectors.toMap(
                        pair -> pair.getKey(),
                        pair -> pair.getValue(),
                        (ns1, ns2) -> {  // run merge function on values if identical keys
                            if (!ns1.equals(ns2)) {
                                errors.add(Message.error("Conflicting prefix instruction: "
                                        + ns1 + " and " + ns2 + " share the same prefix."));
                            }
                            return ns1;
                        }));

        // NOTE: we keep the result even though there are errors in other to collect more possible errors.
        Result<PrefixMapping> prefixes = Result.of(PrefixMapping.Factory.create().setNsPrefixes(pxMap));
        prefixes.addMessages(errors);
        return prefixes;
    }

    static Result<PrefixMapping> processPrefixInstructions(Collection<Instruction> instructions) {
        return instructions.stream()
                .filter(ins -> ins instanceof PrefixInstruction)
                .map(ins -> (PrefixInstruction) ins)
                .map(ins -> ins.getPrefixPairs())
                .map(PrefixParser::buildPrefixMapping) // checks for local conflicts
                .map(prefixes -> PrefixParser.mergePrefixResults(Result.of(stdPrefixes), prefixes)) // checks for standard prefix conflicts
                .reduce(Result.of(PrefixMapping.Factory.create()), PrefixParser::mergePrefixResults); // checks for conflicts across prefix instructions
    }
}
