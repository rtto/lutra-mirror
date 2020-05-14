package xyz.ottr.lutra.store;

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

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class Query {

    //public static final Query UNIFIES2 = Query.template("T1")
    //    .and(Query.parameters("T1", "P1"))
    //    .and(Query.template("T2"))
    //    .and(Query.parameters("T2", "P2"))
    //    .and(Query.unifiesParamsUnordered("P1", "P2", "UP"))
    //    .and(Query.body("T1", "B1"))
    //    .and(Query.body("T2", "B2"))
    //    .and(Query.applyUnifier("B1", "UP", "B1U"))
    //    .and(Query.applyUnifier("B2", "UP", "B2U"))
    //    .and(Query.unifiesBody("B1U", "B2U", "UB"));

    // Query for finding all pairs of templates witch unifies
    public static final Query UNIFIES = template("T1")
        .and(template("T2"))
        .and(body("T1", "B1"))
        .and(body("T2", "B2"))
        .and(unifiesBody("B1", "B2", "UB"));

    // Query for finding all pairs of templates witch are equal
    public static final Query EQUAL = template("T1")
        .and(template("T2"))
        .and(body("T1", "B1"))
        .and(body("T2", "B2"))
        .and(unifiesBody("B1", "B2", "UB1"))
        .and(unifiesBody("B2", "B1", "UB2"));

    // rel : (QueryEngine<? extends TemplateStore>, Tuple) -> Stream<Tuple>
    private final BiFunction<QueryEngine<? extends TemplateStore>, Tuple, Stream<Tuple>> rel;

    private Query(BiFunction<QueryEngine<? extends TemplateStore>, Tuple, Stream<Tuple>> rel) {
        this.rel = rel;
    }

    public Stream<Tuple> eval(QueryEngine<? extends TemplateStore> engine) {
        return this.rel.apply(engine, new Tuple());
    }

    public Stream<Tuple> eval(QueryEngine<? extends TemplateStore> engine, Tuple constants) {
        return this.rel.apply(engine, constants.copy());
    }

    ////////////////////
    // Connectives /////
    ////////////////////

    public Query and(Query query) {
        return new Query((qe, m) -> this.rel.apply(qe, m).flatMap(n -> query.rel.apply(qe, n)));
    }

    public Query or(Query query) {
        return new Query((qe, m) -> Stream.concat(this.rel.apply(qe, m), query.rel.apply(qe, m)));
    }

    public static Query not(Query query) {

        BiPredicate<QueryEngine<? extends TemplateStore>, Tuple> shouldKeep = (qe, m) ->
            query.rel.apply(qe, m).findAny().isEmpty();

        return new Query((qe, m) -> shouldKeep.test(qe, m) ? Stream.of(m) : Stream.empty());
    }

    public static Query distinct(Query query) {
        return new Query((qe, m) -> query.rel.apply(qe, m).distinct());
    }

    //
    ////////////////////
    // Base-relations //
    ////////////////////

    public static Query template(String template) {
        return new Query((qe, m) -> qe.template(m, template));
    }

    public static Query parameters(String template, String params) {
        return new Query((qe, m) -> qe.parameters(m, template, params));
    }

    public static Query length(String params, String len) {
        return new Query((qe, m) -> qe.length(m, params, len));
    }

    public static Query index(String params, String index, String val) {
        return new Query((qe, m) -> qe.index(m, params, index, val));
    }

    public static Query hasOccurenceAt(String term, String level, String inside) {
        return new Query((qe, m) -> qe.hasOccurrenceAt(m, term, inside, level));
    }

    public static Query type(String term, String type) {
        return new Query((qe, m) -> qe.type(m, term, type));
    }

    public static Query innerTypeAt(String type, String level, String inner) {
        return new Query((qe, m) -> qe.innerTypeAt(m, type, level, inner));
    }

    public static Query innerType(String type, String inner) {
        return new Query((qe, m) -> qe.innerType(m, type, inner));
    }

    public static Query isSubTypeOf(String type1, String type2) {
        return new Query((qe, m) -> qe.isSubTypeOf(m, type1, type2));
    }

    public static Query isCompatibleWith(String type1, String type2) {
        return new Query((qe, m) -> qe.isCompatibleWith(m, type1, type2));
    }

    public static Query isOptional(String params, String index) {
        return new Query((qe, m) -> qe.isOptional(m, params, index));
    }

    public static Query isNonBlank(String params, String index) {
        return new Query((qe, m) -> qe.isNonBlank(m, params, index));
    }

    public static Query hasListExpander(String params, String index) {
        return new Query((qe, m) -> qe.hasListExpander(m, params, index));
    }

    public static Query isVariable(String argument) {
        return new Query((qe, m) -> qe.isVariable(m, argument));
    }

    public static Query hasCrossModifier(String instance) {
        return new Query((qe, m) -> qe.hasCrossModifier(m, instance));
    }

    public static Query hasZipMinModifier(String instance) {
        return new Query((qe, m) -> qe.hasZipMinModifier(m, instance));
    }

    public static Query hasZipMaxModifier(String instance) {
        return new Query((qe, m) -> qe.hasZipMaxModifier(m, instance));
    }

    public static Query hasExpansionModifier(String instance) {
        return new Query((qe, m) -> qe.hasExpansionModifier(m, instance));
    }

    public static Query body(String template, String body) {
        return new Query((qe, m) -> qe.body(m, template, body));
    }

    public static Query instance(String body, String instance) {
        return new Query((qe, m) -> qe.instance(m, body, instance));
    }

    public static Query instanceIRI(String instance, String iri) {
        return new Query((qe, m) -> qe.instanceIRI(m, instance, iri));
    }

    public static Query arguments(String instance, String args) {
        return new Query((qe, m) -> qe.arguments(m, instance, args));
    }

    public static Query unifiesVal(String val1, String val2, String unifier) {
        return new Query((qe, m) -> qe.unifiesVal(m, val1, val2, unifier));
    }

    public static Query unifiesParams(String params1, String params2, String unifier) {
        return new Query((qe, m) -> qe.unifiesParams(m, params1, params2, unifier));
    }

    public static Query unifiesParamsUnordered(String params1, String params2, String unifier) {
        return new Query((qe, m) -> qe.unifiesParamsUnordered(m, params1, params2, unifier));
    }

    public static Query unifiesBody(String body1, String body2, String unifier) {
        return new Query((qe, m) -> qe.unifiesBody(m, body1, body2, unifier));
    }

    public static Query merge(String unifier1, String unifier2, String unifier) {
        return new Query((qe, m) -> qe.merge(m, unifier1, unifier2, unifier));
    }

    public static Query applyUnifier(String elem, String unifier, String unified) {
        return new Query((qe, m) -> qe.applyUnifier(m, elem, unifier, unified));
    }

    public static Query notEquals(String elem1, String elem2) {
        return new Query((qe, m) -> m.get(elem1).equals(m.get(elem2)) ? Stream.empty() : Stream.of(m));
    }

    public static Query isDependencyOf(String instance, String template) {
        return new Query((qe, m) -> qe.isDependencyOf(m, instance, template));
    }

    public static Query dependsTransitive(String template, String instance) {
        return new Query((qe, m) -> qe.dependsTransitive(m, instance, template));
    }

    public static Query isUndefined(String template) {
        return new Query((qe, m) -> qe.isUndefined(m, template));
    }

    public static Query isSignature(String template) {
        return new Query((qe, m) -> qe.isSignature(m, template));
    }

    public static Query isBase(String template) {
        return new Query((qe, m) -> qe.isBase(m, template));
    }

    ////////////////////
    /// Short-cuts /////
    ////////////////////

    /**
     * Simply the conjunction of the #parameters(String,String) and #index(String,String) queries.
     */
    public static Query parameterIndex(String template, String index, String param) {
        String params = Tuple.freshVar();
        return parameters(template, params).and(index(params, index, param));
    }

    /**
     * Simply the conjunction of the #argument(String,String) and #index(String,String) queries.
     */
    public static Query argumentIndex(String instance, String index, String arg) {
        String args = Tuple.freshVar();
        return arguments(instance, args).and(index(args, index, arg));
    }

    /**
     * Simply the conjunction of the #body(String,String) and #instance(String,String) queries.
     */
    public static Query bodyInstance(String template, String instance) {
        String body = Tuple.freshVar();
        return body(template, body).and(instance(body, instance));
    }

    /**
     * Finds the type which argument at index is used as in instance.
     */
    public static Query usedAsType(String instance, String index, String level, String type) {

        String temp = Tuple.freshVar();
        String para = Tuple.freshVar();
        String parType = Tuple.freshVar();
        String args = Tuple.freshVar();
        String outer = Tuple.freshVar();

        return instanceIRI(instance, temp)
            .and(parameterIndex(temp, index, para))
            .and(arguments(instance, args))
            .and(type(para, parType))
            .and(hasListExpander(args, index)
                .and(innerTypeAt(parType, level, outer))
                .and(innerType(outer, type))
                .or(not(hasListExpander(args, index))
                    .and(innerTypeAt(parType, level, type))));
    }

    ////////////////////
    /// Utils //////////
    ////////////////////

    public static Query removeSymmetry(String var1, String var2) {
        return new Query((qe, m) -> qe.removeSymmetry(m, var1, var2));
    }

    public static Query bind(String var, Object val) {
        return new Query((qe, m) -> Stream.of(m.bind(var, val)));
    }
}
