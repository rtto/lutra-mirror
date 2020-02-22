package xyz.ottr.lutra.parser;

import lombok.Builder;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.system.Result;

public class ParameterParser {

    @Builder
    private static Result<Parameter> createParameter(Result<Term> term, Result<Boolean> nonBlank, Result<Boolean> optional, Result<Term> defaultValue) {

        var builder = Result.of(Parameter.builder());
        builder.addResult(term, Parameter.ParameterBuilder::term);
        builder.addResult(nonBlank, Parameter.ParameterBuilder::nonBlank);
        builder.addResult(optional, Parameter.ParameterBuilder::optional);
        builder.addResult(defaultValue, Parameter.ParameterBuilder::defaultValue);
        var parameter = builder.map(Parameter.ParameterBuilder::build);

        return parameter;
    }

}
