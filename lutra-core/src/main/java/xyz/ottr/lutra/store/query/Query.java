package xyz.ottr.lutra.store.query;

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

import xyz.ottr.lutra.store.TemplateStore;

public class Query {

    private static int newId = 0;

    private static int genNewId() {
        newId++;
        return newId;
    }

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
    public static final Query UNIFIES = Query.template("T1")
        .and(Query.template("T2"))
        .and(Query.body("T1", "B1"))
        .and(Query.body("T2", "B2"))
        .and(Query.unifiesBody("B1", "B2", "UB"));

    // Query for finding all pairs of templates witch are equal
    public static final Query EQUAL = Query.template("T1")
        .and(Query.template("T2"))
        .and(Query.body("T1", "B1"))
        .and(Query.body("T2", "B2"))
        .and(Query.unifiesBody("B1", "B2", "UB1"))
        .and(Query.unifiesBody("B2", "B1", "UB2"));

    // rel : (QueryEngine<? extends TemplateStore>, Tuple) -> Stream<Tuple>
    private final BiFunction<QueryEngine<? extends TemplateStore>, Tuple, Stream<Tuple>> rel;

    private Query(BiFunction<QueryEngine<? extends TemplateStore>, Tuple, Stream<Tuple>> rel) {
        this.rel = rel;
    }

    public Stream<Tuple> eval(QueryEngine<? extends TemplateStore> engine) {
        return rel.apply(engine, new Tuple());
    }

    public Stream<Tuple> eval(QueryEngine<? extends TemplateStore> engine, Tuple constants) {
        return rel.apply(engine, constants.copy());
    }

    ////////////////////
    // Connectives /////
    ////////////////////

    public Query and(Query r) {
        return new Query((qe, m) -> this.rel.apply(qe, m).flatMap(n -> r.rel.apply(qe, n)));
    }

    public Query or(Query r) {
        return new Query((qe, m) -> Stream.concat(this.rel.apply(qe, m), r.rel.apply(qe, m)));
    }

    public static Query not(Query r) {

        BiPredicate<QueryEngine<? extends TemplateStore>, Tuple> shouldKeep = (qe, m) ->
            !r.rel.apply(qe, m).findAny().isPresent();

        return new Query((qe, m) -> shouldKeep.test(qe, m) ? Stream.of(m) : Stream.empty());
    }

    public static Query distinct(Query r) {
        return new Query((qe, m) -> r.rel.apply(qe, m).distinct());
    }

    //
    ////////////////////
    // Base-relations //
    ////////////////////

    public static Query template(String t) {
        return new Query((qe, m) -> qe.template(m, t));
    }

    public static Query parameters(String t, String ps) {
        return new Query((qe, m) -> qe.parameters(m, t, ps));
    }

    public static Query length(String ps, String len) {
        return new Query((qe, m) -> qe.length(m, ps, len));
    }

    public static Query index(String ps, String i, String v) {
        return new Query((qe, m) -> qe.index(m, ps, i, v));
    }

    public static Query hasOccurenceAt(String term, String inside, String level) {
        return new Query((qe, m) -> qe.hasOccurenceAt(m, term, inside, level));
    }

    public static Query type(String trm, String tp) {
        return new Query((qe, m) -> qe.type(m, trm, tp));
    }

    public static Query innerTypeAt(String type, String level, String inner) {
        return new Query((qe, m) -> qe.innerTypeAt(m, type, level, inner));
    }

    public static Query innerType(String type, String inner) {
        return new Query((qe, m) -> qe.innerType(m, type, inner));
    }

    public static Query isSubTypeOf(String tp1, String tp2) {
        return new Query((qe, m) -> qe.isSubTypeOf(m, tp1, tp2));
    }

    public static Query isCompatibleWith(String tp1, String tp2) {
        return new Query((qe, m) -> qe.isCompatibleWith(m, tp1, tp2));
    }

    public static Query isOptional(String ps, String i) {
        return new Query((qe, m) -> qe.isOptional(m, ps, i));
    }

    public static Query isNonBlank(String ps, String i) {
        return new Query((qe, m) -> qe.isNonBlank(m, ps, i));
    }

    public static Query hasListExpander(String ps, String i) {
        return new Query((qe, m) -> qe.hasListExpander(m, ps, i));
    }

    public static Query hasCrossModifier(String ins) {
        return new Query((qe, m) -> qe.hasCrossModifier(m, ins));
    }

    public static Query hasZipMinModifier(String ins) {
        return new Query((qe, m) -> qe.hasZipMinModifier(m, ins));
    }

    public static Query hasZipMaxModifier(String ins) {
        return new Query((qe, m) -> qe.hasZipMaxModifier(m, ins));
    }

    public static Query hasExpansionModifier(String ins) {
        return new Query((qe, m) -> qe.hasExpansionModifier(m, ins));
    }

    public static Query body(String t, String b) {
        return new Query((qe, m) -> qe.body(m, t, b));
    }

    public static Query instance(String b, String i) {
        return new Query((qe, m) -> qe.instance(m, b, i));
    }

    public static Query instanceIRI(String i, String iri) {
        return new Query((qe, m) -> qe.instanceIRI(m, i, iri));
    }

    public static Query arguments(String i, String args) {
        return new Query((qe, m) -> qe.arguments(m, i, args));
    }

    public static Query unifiesVal(String v1, String v2, String u) {
        return new Query((qe, m) -> qe.unifiesVal(m, v1, v2, u));
    }

    public static Query unifiesParams(String ps1, String ps2, String u) {
        return new Query((qe, m) -> qe.unifiesParams(m, ps1, ps2, u));
    }

    public static Query unifiesParamsUnordered(String ps1, String ps2, String u) {
        return new Query((qe, m) -> qe.unifiesParamsUnordered(m, ps1, ps2, u));
    }

    public static Query unifiesBody(String b1, String b2, String u) {
        return new Query((qe, m) -> qe.unifiesBody(m, b1, b2, u));
    }

    public static Query merge(String u1, String u2, String u) {
        return new Query((qe, m) -> qe.merge(m, u1, u2, u));
    }

    public static Query applyUnifier(String elem, String uni, String unified) {
        return new Query((qe, m) -> qe.applyUnifier(m, elem, uni, unified));
    }

    public static Query notEquals(String e1, String e2) {
        return new Query((qe, m) -> m.get(e1).equals(m.get(e2)) ? Stream.empty() : Stream.of(m));
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
        String params = "_ps" + genNewId();
        return parameters(template, params).and(index(params, index, param));
    }

    /**
     * Simply the conjunction of the #argument(String,String) and #index(String,String) queries.
     */
    public static Query argumentIndex(String instance, String index, String arg) {
        String args = "_as" + genNewId();
        return arguments(instance, args).and(index(args, index, arg));
    }

    /**
     * Simply the conjunction of the #body(String,String) and #instance(String,String) queries.
     */
    public static Query bodyInstance(String template, String instance) {
        String body = "_body" + genNewId();
        return body(template, body).and(instance(body, instance));
    }

    /**
     * Finds the type which argument at index is used as in instance.
     */
    public static Query usedAsType(String instance, String index, String type) {

        String temp = "_Temp" + genNewId();
        String para = "_Para" + genNewId();
        String args = "_Args" + genNewId();
        String outer = "_Outer" + genNewId();

        return instanceIRI(instance, temp)
            .and(parameterIndex(temp, index, para))
            .and(arguments(instance, args))
            .and(hasListExpander(args, index)
                .and(type(para, outer))
                .and(innerType(outer, type))
                .or(not(hasListExpander(args, index))
                    .and(type(para, type))));
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
