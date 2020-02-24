package xyz.ottr.lutra.parser;

import lombok.Builder;
import lombok.NonNull;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.system.Result;

public class ArgumentParser  {

    @Builder
    private static Result<Argument> createArgument(@NonNull Result<Term> term, Result<Boolean> listExpander) {

        listExpander = Result.nullToEmpty(listExpander);

        var builder = Result.of(Argument.builder());
        builder.addResult(term, Argument.ArgumentBuilder::term);
        builder.addResult(listExpander, Argument.ArgumentBuilder::listExpander);
        var argument = builder.map(Argument.ArgumentBuilder::build);

        validateValue(argument);

        return argument;
    }

    // Warning if value is a URI in the ottr namespace.
    private static void validateValue(Result<Argument> argument) {
        argument.ifPresent(arg -> {
            var term = arg.getTerm();
            if (term instanceof IRITerm && ((IRITerm) term).getIri().startsWith(OTTR.namespace)) {
                argument.addWarning("Suspicious argument value: " + term
                    + " is in the ottr namespace: " + OTTR.namespace);
            }
        });
    }

}
