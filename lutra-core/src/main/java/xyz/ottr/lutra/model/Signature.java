package xyz.ottr.lutra.model;

/*-
 * #%L
 * lutra-core
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.Type;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.system.Result;

@Getter
@Builder(builderMethodName = "superbuilder")
public class Signature implements ModelElement {

    private final @NonNull String iri;
    private final @NonNull @Singular List<Parameter> parameters;
    private final @NonNull @Singular Set<Instance> annotations;


    @Override
    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
    }

    @Override
    public String toString(PrefixMapping prefixes) {
        return prefixes.shortForm(this.iri)
            + this.parameters.stream()
                .map(t -> t.toString(prefixes))
                .collect(Collectors.joining(", ", "[ ", " ]"));
    }

    @Override
    public int hashCode() {
        return this.iri.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o 
                || Objects.nonNull(o)
                        && getClass() == o.getClass()
                        && Objects.equals(this.iri, ((Signature) o).iri);
    }

    @Override
    public Result<? extends Signature> validate() {

        var result = Result.of(this);

        // check for duplicate names and variables
        var duplicateVarNames = getDuplicates(this.getParameters(), p -> p.getName());
        if (!duplicateVarNames.isEmpty()) {
            result.addError("Parameter variable names must be unique. Signature contains multiple occurrences "
                    + "of the same variable name: " + duplicateVarNames);
        }
        var duplicateVars = getDuplicates(this.getParameters(), p -> p.getTerm().getIdentifier());
        if (!duplicateVars.isEmpty()) {
            result.addError("Parameter variables must be unique. Signature contains multiple occurrences "
                + "of the same variable: " + duplicateVars);
        }

        return result;
    }

    /**
     * Generic method for getting duplicate values of *non-null* function calls applied to a collection.
     * Returns the duplicate function call values.
     */
    public <X,V> Collection<V> getDuplicates(Collection<X> collection, Function<X,V> function) {
        return collection.stream()
                .map(function)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Instance asInstance() {

        var builder = Instance.builder().iri(this.getIri());
        for (Parameter param : this.getParameters()) {
            builder.argument(Argument.builder().term(param.getTerm()).build());
        }
        return builder.build();
    }

    public Instance getExampleInstance() {

        var builder = Instance.builder().iri(this.getIri());

        int index = 1;
        for (Parameter param : this.getParameters()) {
            builder.argument(
                Argument.builder()
                    .term(getExampleTerm(param.getTerm().getType(), "argument", index))
                    .build());
            index += 1;
        }
        return builder.build();
    }

    private Term getExampleTerm(Type type, String name, int index) {

        if (type.isSubTypeOf(TypeRegistry.IRI)) {
            return new IRITerm(OTTR.ns_example_arg + name + index);
        } else if (type instanceof ListType) {
            var list = (ListType) type;
            return new ListTerm(
                getExampleTerm(list.getInner(), name + index + "-", 1),
                getExampleTerm(list.getInner(), name + index + "-", 2));
        } else {
            return new BlankNodeTerm(name + index);
        }
    }

    public boolean isOptional(int index) {
        return Objects.nonNull(this.parameters)
                && this.parameters.get(index).isOptional();
    }

    public boolean isOptional(Term parameterTerm) {
        return Objects.nonNull(this.parameters)
                && this.parameters.stream()
                .filter(p -> p.getTerm().equals(parameterTerm))
                .findFirst()
                .get()
                .isOptional();
    }

}
