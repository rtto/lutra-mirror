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

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.commons.math3.util.CombinatoricsUtils;

import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.store.TemplateStore;

public abstract class QueryEngine<S extends TemplateStore> {

    S store; // Variable containing the store queries are to be evaluated over

    /**
     * Utility method for removing symmetries, such that not both (var1, var2)
     * and (var2, var1) gets compared.
     */
    public Stream<Tuple> removeSymmetry(Tuple tuple, String var1, String var2) {
        String str1 = tuple.get(var1).toString();
        String str2 = tuple.get(var2).toString();
        return str1.compareTo(str2) <= 0
            ? Stream.of(tuple)
            : Stream.empty();
    }

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable t to
     * the IRIs of templates in this' store as follows: If t is bound in tuple, then
     * the singleton stream of tuple is returned if t maps to a defined template and
     * an empty stream otherwise; or if t is not bound returns a stream of tuples
     * equal to tuple but with t bound to each template IRI defined in this' store.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param template
     *      a variable name denoting a template IRI
     * @return
     *      a Stream of tuples binding t to a template IRI
     */
    public Stream<Tuple> template(Tuple tuple, String template) {
        if (tuple.hasBound(template)) {
            if (this.store.getTemplateIRIs().contains(template)) {
                return Stream.of(tuple);
            } else {
                return Stream.empty();
            }
        } else {
            return this.store.getTemplateIRIs().stream()
                .map(iri -> tuple.bind(template, iri));
        }
    }

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable ps to
     * some elements representing the parameters of templates bound by t in tuple
     * according to this' store as follows: If ps is bound in tuple, then
     * the singleton stream of tuple is returned if t maps to a defined template with
     * ps as parameters, and an empty stream otherwise; or if ps is not bound
     * returns a stream of tuples equal to tuple but with ps bound to each template t's
     * parameters.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param template
     *      a variable name denoting a template IRI
     * @param params
     *      a variable name denoting a list of parameters
     * @return
     *      a Stream of tuples binding t to a template IRI and ps to
     *      the corresponding list of parameters
     */
    public abstract Stream<Tuple> parameters(Tuple tuple, String template, String params);

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable len to
     * the length of the list of terms represented by either a parameter list or argument list
     * bound to params in tuple according to this' store as follows: If len is bound in tuple, then
     * the singleton stream of tuple is returned if len maps to the length of params,
     * and an empty stream otherwise; or if len is not bound
     * returns a stream of the tuple equal to tuple but with len bound to length of params
     * parameters.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param params
     *      a variable name denoting a list of parameters or arguments
     * @param len
     *      a variable name denoting the length of params
     * @return
     *      a Stream of tuples binding len to an integer equal to the length of params 
     */
    public abstract Stream<Tuple> length(Tuple tuple, String params, String len);

    /**
     * Constructs a stream of maps (representing tuples) mapping the
     * variables index and val to some elements representing an index and
     * parameter value (resp.) of the list of parameters bound to params
     * in tuple according to this' store, similarly as parameters-method.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param params
     *      a variable name denoting a list of parameters
     * @param index
     *      a variable name denoting a parameter index 
     * @param val
     *      a variable name denoting a parameter value 
     * @return
     *      a Stream of tuples binding params to a list of parameters,
     *      with index bound to an index in the corresponding parameter list and
     *      val the i'th value in the list.
     */
    public abstract Stream<Tuple> index(Tuple tuple, String params, String index, String val);

    /**
     * Constructs a stream of maps (representing tuples) mapping the
     * variables inside and level to some elements representing a term occuring
     * at nesting depth denoted by level in the term denoted by the variable term.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param term
     *      a variable name denoting a term
     * @param inside
     *      a variable name denoting a term
     * @param level
     *      a variable name denoting an integer representing a nesting depth
     * @return
     *      a Stream of tuples binding inside to a term that occurs at a depth denoted by
     *      the variable level in the term denoted by the variable term.
     */
    public abstract Stream<Tuple> hasOccurenceAt(Tuple tuple, String term, String inside, String level);

    /**
     * Constructs a stream of maps (representing tuples) mapping the
     * variable type to the type of term .
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param term
     *      a variable name denoting a term
     * @param type
     *      a variable name denoting a term type
     * @return
     *      a Stream of tuples binding type to the type of term.
     */
    public abstract Stream<Tuple> type(Tuple tuple, String term, String type);

    /**
     * Constructs a stream of maps (representing tuples) mapping the
     * variables level and inner to the level at which the type inner occurs in the
     * type denoted by the variable type.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param type
     *      a variable name denoting a type
     * @param level
     *      a variable name denoting a nesting depth
     * @param inner
     *      a variable name denoting a type
     * @return
     *      a Stream of tuples binding variables level and inner to the
     *      level at which the type inner occurs in the type denoted by the variable type.
     */
    public abstract Stream<Tuple> innerTypeAt(Tuple tuple, String type, String level, String inner);

    /**
     * Constructs a stream of maps (representing tuples) mapping the
     * variable inner to the inner type of the type denoted by type.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param type
     *      a variable name denoting a TermType 
     * @param inner
     *      a variable name denoting the inner TermType of type
     * @return
     *      a Stream of tuples binding inner to the inner type of type denoted by type
     */
    public abstract Stream<Tuple> innerType(Tuple tuple, String type, String inner);

    /**
     * Constructs a stream of maps (representing tuples) 
     * mapping type1 to a term type which is a subtype of the
     * term type denoted by type2.
     * 
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param type1
     *      a variable name denoting a term type
     * @param type2
     *      a variable name denoting a term type
     * @return
     *      a Stream of tuples binding type1 to a subtype of type2
     */
    public Stream<Tuple> isSubTypeOf(Tuple tuple, String type1, String type2) {
        TermType boundType1 = tuple.getAs(TermType.class, type1);
        TermType boundType2 = tuple.getAs(TermType.class, type2);

        return boundType1.isSubTypeOf(boundType2)
            ? Stream.of(tuple)
            : Stream.empty();
    }

    /**
     * Constructs a stream of maps (representing tuples) 
     * mapping type1 to a term type which is compatible with the
     * term type denoted by type2.
     * 
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param type1
     *      a variable name denoting a term type
     * @param type2
     *      a variable name denoting a term type
     * @return
     *      a Stream of tuples binding type1 to a compatible type of type2
     */
    public Stream<Tuple> isCompatibleWith(Tuple tuple, String type1, String type2) {
        TermType boundType1 = tuple.getAs(TermType.class, type1);
        TermType boundType2 = tuple.getAs(TermType.class, type2);

        return boundType1.isCompatibleWith(boundType2)
            ? Stream.of(tuple)
            : Stream.empty();
    }

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable i to
     * indecies of optional values in the list of parameters bound to ps in tuple
     * according to this' store, similarly as parameters-method.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param params
     *      a variable name denoting a list of parameters
     * @param index
     *      a variable name denoting a parameter index 
     * @return
     *      a Stream of tuples binding ps to a list of parameters,
     *      with i boud to an index of an optional parameter
     *      in the corresponding parameter list.
     */
    public abstract Stream<Tuple> isOptional(Tuple tuple, String params, String index);

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable i to
     * indecies of nonBlank values in the list of parameters bound to ps in tuple
     * according to this' store, similarly as parameters-method.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param params
     *      a variable name denoting a list of parameters
     * @param index
     *      a variable name denoting a parameter index 
     * @return
     *      a Stream of tuples binding ps to a list of parameters,
     *      with i bound to an index of a nonBlank parameter
     *      in the corresponding parameter list.
     */
    public abstract Stream<Tuple> isNonBlank(Tuple tuple, String params, String index);

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable i to
     * indecies of terms marked with list expanders in the list of arguments bound to ps in tuple
     * according to this' store, similarly as arguments-method.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param params
     *      a variable name denoting a list of arguments
     * @param index
     *      a variable name denoting a parameter index 
     * @return
     *      a Stream of tuples binding ps to a list of parameters,
     *      with i boud to an index of a list expander parameter
     *      in the corresponding parameter list.
     */
    public abstract Stream<Tuple> hasListExpander(Tuple tuple, String arguments, String index);

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable instance to
     * instances with a Cross expansion modifier, according to this store.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param instance
     *      a variable name denoting an instance
     * @return
     *      a Stream of tuples binding instance to an instance with a Cross expander.
     */
    public abstract Stream<Tuple> hasCrossModifier(Tuple tuple, String instance);

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable instance to
     * instances with a ZipMin expansion modifier, according to this store.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param instance
     *      a variable name denoting an instance
     * @return
     *      a Stream of tuples binding instance to an instance with a ZipMin expander.
     */
    public abstract Stream<Tuple> hasZipMinModifier(Tuple tuple, String instance);

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable instance to
     * instances with a ZipMax expansion modifier, according to this store.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param instance
     *      a variable name denoting an instance
     * @return
     *      a Stream of tuples binding instance to an instance with a ZipMax expander.
     */
    public abstract Stream<Tuple> hasZipMaxModifier(Tuple tuple, String instance);

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable instance to
     * instances with a any expansion modifier, according to this store.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param instance
     *      a variable name denoting an instance
     * @return
     *      a Stream of tuples binding instance to an instance with an expander.
     */
    public abstract Stream<Tuple> hasExpansionModifier(Tuple tuple, String instance);

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable b to
     * the template denoted by t's body according to this' store.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param template
     *      a variable name denoting a template
     * @param body
     *      a variable name denoting a template body
     * @return
     *      a Stream of tuples binding b to t's template body.
     */
    public abstract Stream<Tuple> body(Tuple tuple, String template, String body);

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable b to
     * the template denoted by t's body according to this' store.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param body
     *      a variable name denoting a template body
     * @param instance
     *      a variable name denoting a template instance
     * @return
     *      a Stream of tuples binding i to instances of the template body
     *      bound to b.
     */
    public abstract Stream<Tuple> instance(Tuple tuple, String body, String instance);

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable iri to
     * the IRI of the template instance i.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param instance
     *      a variable name denoting a template instance
     * @param iri
     *      a variable name denoting a template IRI
     * @return
     *      a Stream of tuples binding iri to the IRI of the template instance
     *      bound to i.
     */
    public abstract Stream<Tuple> instanceIRI(Tuple tuple, String instance, String iri);

    /**
     * Constructs a stream of maps (representing tuples) mapping the variable args to
     * the arguments of the template instance i.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     * @param instance
     *      a variable name denoting a template instance
     * @param args
     *      a variable name denoting argument list to a template instance
     * @return
     *      a Stream of tuples binding args to argument lists of the template body
     *      bound to i.
     */
    public abstract Stream<Tuple> arguments(Tuple tuple, String instance, String args);

    /**
     * Constructs a a stream containing the argument tuple if the template
     * denoted by the IRI bound to the argument string is undefined in the store,
     * and an empty stream otherwise.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     *@param template
     *      a string denoting a variable bound to an IRI of a template
     *@return
     *      a Stream containin tuple if the IRI bound to argument string is undefined
     *      in the store, and an empty Stream otherwise.
     */
    public abstract Stream<Tuple> isUndefined(Tuple tuple, String template);

    /**
     * Constructs a a stream containing the argument tuple if the template
     * denoted by the IRI bound to the argument string is a signature in the store,
     * and an empty stream otherwise.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     *@param template
     *      a string denoting a variable bound to an IRI of a template
     *@return
     *      a Stream containin tuple if the IRI bound to argument string is a signature
     *      in the store, and an empty Stream otherwise.
     */
    public abstract Stream<Tuple> isSignature(Tuple tuple, String template);

    /**
     * Constructs a a stream containing the argument tuple if the template
     * denoted by the IRI bound to the argument string is a base template in the store,
     * and an empty stream otherwise.
     *
     * @param tuple
     *      a Map representing a tuple from variables to values
     *@param template
     *      a string denoting a variable bound to an IRI of a template
     *@return
     *      a Stream containin tuple if the IRI bound to argument string is a base
     *      template in the store, and an empty Stream otherwise.
     */
    public abstract Stream<Tuple> isBase(Tuple tuple, String template);

    /**
     * Constructs a stream of tuples mapping the variable uni to
     * a unifier, according to this' store, such that the value
     * in val1 becomes equal to val2
     *
     * @param tuple
     *      a Tuple mapping variables to values
     * @param val1
     *      a variable name denoting a value
     * @param val2
     *      a variable name denoting a value
     * @param uni
     *      a variable name denoting a unifier
     * @return
     *      a Stream of tuples binding u to a unifier between
     *      the values bound to v and w.
     */
    public abstract Stream<Tuple> unifiesVal(Tuple tuple, String val1, String val2, String uni);

    
    /**
     * Constructs a stream of tuples mapping the variable
     * denoted by the templateIRI argument to IRIs of templates
     * having value denoted by instanceIRI as instance.
     *
     * @param tuple
     *      a Tuple mapping variables to values
     * @param instanceIRI
     *      a variable name denoting a value of an IRI
     * @param templateIRI
     *      a variable name which will denote IRIs of templates
     *      having instanceIRI as dependency
     * @return
     *      a Stream of tuples binding variable denoted by templateIRI
     *      to IRIs of templates with value denoted by instanceIRI
     *      as dependency
     */
    public abstract Stream<Tuple> isDependencyOf(Tuple tuple, String instanceIRI, String templateIRI);

    /**
     * Constructs a stream of tuples mapping the variable
     * denoted by the instanceIRI argument to IRIs of templates
     * that are dependencies (transetively closed) of template
     * denoted by templateIRI
     *
     * @param tuple
     *      a Tuple mapping variables to values
     * @param instanceIRI
     *      a variable name denoting a value of an IRI
     * @param templateIRI
     *      a variable name which will denote IRIs of templates
     *      having instanceIRI as dependency
     * @return
     *      a Stream of tuples binding variable denoted by templateIRI
     *      to IRIs of templates with value denoted by instanceIRI
     *      as dependency
     */
    public abstract Stream<Tuple> dependsTransitive(Tuple tuple, String templateIRI, String instanceIRI);

    /**
     * Returns the unifier denoted by argument string in tuple, if it is bound and is
     * a unifier.
     */
    public static Map<Object, Object> asUnifier(Tuple tuple, String uni) {
        if (!tuple.hasBound(uni)) {
            return null; // TODO: Raise exception
        }
        Object obj = tuple.get(uni);
        if (!(obj instanceof Map)) {
            return null; // TODO: Raise exception
        }
        return (Map<Object, Object>) obj;
    }

    /**
     * Constructs a stream of tuples mapping the variable u to
     * a unifier, according to this' store, such that the list of
     * values bound to ps1 becomes equal to
     * the list of values bound to ps2.
     *
     * @param tuple
     *      a Tuple mapping variables to values
     * @param ps1
     *      a variable name denoting a list of values
     * @param ps2
     *      a variable name denoting a list of values
     * @param uni
     *      a variable name denoting a unifier
     * @return
     *      a Stream of tuples binding u to a unifier between
     *      the list of values bound to ps1 and ps2.
     */
    public Stream<Tuple> unifiesParams(Tuple tuple, String ps1, String ps2, String uni) {
        return unifiesParamsAccOrd(tuple, ps1, ps2, uni, true);
    }

    /**
     * Constructs a stream of tuples mapping the variable u to
     * a unifier, according to this' store, such that the list
     * of values bound to ps1 becomes equal to
     * the list of values bound to ps2, disregarding order.
     *
     * @param tuple
     *      a Tuple mapping variables to values
     * @param ps1
     *      a variable name denoting a list of values
     * @param ps2
     *      a variable name denoting a list of values
     * @param uni
     *      a variable name denoting a unifier
     * @return
     *      a Stream of tuples binding u to a unifier between
     *      the list of values bound to ps1 and ps2.
     */
    public Stream<Tuple> unifiesParamsUnordered(Tuple tuple, String ps1, String ps2, String uni) {
        return unifiesParamsAccOrd(tuple, ps1, ps2, uni, false);
    }

    private Stream<Tuple> unifiesParamsAccOrd(Tuple tuple, String ps1, String ps2, String uni, boolean order) {

        List<Object> ps1Vals = index(tuple.copy(), ps1, "_i1", "_v1")
            .map(t -> t.get("_v1")).collect(Collectors.toList());
        List<Object> ps2Vals = index(tuple.copy(), ps2, "_i2", "_v2")
            .map(t -> t.get("_v2")).collect(Collectors.toList());

        if (ps1Vals.size() != ps2Vals.size()) {
            return Stream.empty();
        }

        Stream.Builder tuples = Stream.builder();
        if (order) {
            unifyOrdered(tuple, ps1Vals, ps2Vals, uni).ifPresent(tuples);
        } else {
            // TODO: rather use k choose n from apache.commons.math3 (see Cluster-class) for efficiency
            (new PermutationIterator<>(ps2Vals)).forEachRemaining(ps2Perm ->
                    unifyOrdered(tuple, ps1Vals, ps2Perm, uni).ifPresent(tuples));
        }

        return tuples.build();
    }

    public Optional<Tuple> unifyOrdered(Tuple m, List<Object> ps1Vals, List<Object> ps2Vals, String u) {

        Optional<Tuple> newTuple = Optional.of(m.copy());
        if (!m.hasBound(u)) {
            newTuple = newTuple.map(t -> t.bind(u, new Substitution()));
        }

        for (int i = 0; i < ps1Vals.size() && newTuple.isPresent(); i++) {
            Object ps1Val = ps1Vals.get(i);
            Object ps2Val = ps2Vals.get(i);
            newTuple = newTuple.map(t -> t.bind("_v1", ps1Val).bind("_v2", ps2Val))
                               .flatMap(t -> unifiesVal(t, "_v1", "_v2", "_paramsUni").findAny());
            newTuple = newTuple.flatMap(t -> merge(t, "_paramsUni", u, u).findAny())
                               .map(t -> t.unbind("_v1", "_v2", "_paramsUni"));
        }
        return newTuple;
    }

    /**
     * Constructs a stream of tuples mapping the variable u to
     * a unifier, according to this' store, such that the list of instances bound to b1
     * becomes a subset of the list of instances bound to b2.
     *
     * @param m
     *      a Tuple mapping variables to values
     * @param b1
     *      a variable name denoting a list of instances (i.e. a body)
     * @param b2
     *      a variable name denoting a list of instances (i.e. a body)
     * @param u
     *      a variable name denoting a unifier
     * @return
     *      a Stream of tuples binding u to a unifier between
     *      the the bodies b1 and b2
     */
    public Stream<Tuple> unifiesBody(Tuple m, String b1, String b2, String u) {
        // TODO: Split into smaller methods
        List<Tuple> b1Ins = instance(m.copy(), b1, "_ins")
            .flatMap(mc -> instanceIRI(mc, "_ins", "_iri"))
            .flatMap(mc -> arguments(mc, "_ins", "_args"))
            .collect(Collectors.toList());
        List<Tuple> b2Ins = instance(m.copy(), b2, "_ins")
            .flatMap(mc -> instanceIRI(mc, "_ins", "_iri"))
            .flatMap(mc -> arguments(mc, "_ins", "_args"))
            .collect(Collectors.toList());
        List<Tuple> biggestBody;
        List<Tuple> smallestBody;

        Comparator<Tuple> comp = new TupleComparator();
        if (b1Ins.size() > b2Ins.size()) {
            return Stream.empty();
        } else {
            biggestBody = b2Ins;
            Collections.sort(biggestBody, comp);
            smallestBody = b1Ins;
            Collections.sort(smallestBody, comp);
        }

        if (smallestBody.size() == 0) {
            return Stream.empty();
        }

        Stream.Builder tuples = Stream.builder();
        // TODO: rather use k choose n from apache.commons.math3 (see Cluster-class) for efficiency
        Iterator<int[]> combIter = CombinatoricsUtils
            .combinationsIterator(biggestBody.size(), smallestBody.size());
        //(new PermutationIterator<>(biggestBody)).forEachRemaining(biggestPerm -> { 
        while (combIter.hasNext()) {
            int[] comb = combIter.next();
            List<Tuple> biggestPerm = makeSelection(biggestBody, comb);
            Optional<Tuple> newTuple = Optional.of(m.copy());
            if (!m.hasBound(u)) {
                newTuple = newTuple.map(t -> t.bind(u, new Substitution()));
            }

            for (int i = 0; i < smallestBody.size() && newTuple.isPresent(); i++) {
                Tuple t1 = smallestBody.get(i);
                Tuple t2 = biggestPerm.get(i);
                if (!t1.get("_iri").equals(t2.get("_iri"))) {
                    newTuple = Optional.empty();
                    break;
                }
                // TODO: Check that list expanders are equal
                List<Tuple> t1ListExpanders = hasListExpander(t1.copy(), "_args", "_ind")
                    .collect(Collectors.toList());
                List<Tuple> t2ListExpanders = hasListExpander(t2.copy(), "_args", "_ind")
                    .collect(Collectors.toList());
                if (t1ListExpanders.size() != t2ListExpanders.size()) {
                    newTuple = Optional.empty();
                    break;
                }

                newTuple = newTuple.map(t -> t.bind("_args1", t1.get("_args")).bind("_args2", t2.get("_args")))
                                   .flatMap(t -> unifiesParams(t, "_args1", "_args2", "_bodyUni").findAny());
                newTuple = newTuple.flatMap(t -> merge(t, "_bodyUni", u, u).findAny())
                                   .map(t -> t.unbind("_args1", "_args2", "_bodyUni"));
            }
            newTuple.ifPresent(tuples);
        }
        return tuples.build();
    }

    private List<Tuple> makeSelection(List<Tuple> body, int[] selection) {
        List<Tuple> selected = new LinkedList<>();
        for (int i = 0; i < selection.length; i++) {
            selected.add(body.get(selection[i]));
        }
        return selected;
    }

    /**
     * Constructs a stream of tuples mapping the variable u to
     * a unifier which represents a merge between the
     * unifiers bound to u1 and u2, according to this' store's unifiesVal.
     *
     * @param m
     *      a Tuple mapping variables to values
     * @param u1
     *      a variable name denoting a unifier
     * @param u2
     *      a variable name denoting a unifier
     * @param u
     *      a variable name denoting a unifier
     * @return
     *      a Stream of tuples binding u to a unifier which is the
     *      merge of the unifiers bound to u1 and u2.
     */
    public Stream<Tuple> merge(Tuple m, String u1, String u2, String u) {

        Substitution mu1 = m.getAs(Substitution.class, u1);
        Substitution mu2 = m.getAs(Substitution.class, u2);

        Optional<Substitution> newUni = mu1.mergeWithUnification(mu2);

        return newUni.isPresent() ? Stream.of(m.bind(u, newUni.get())) : Stream.empty();
    }

    public Stream<Tuple> applyUnifier(Tuple m, String elem, String u, String unified) {
        Substitution boundSubs = m.getAs(Substitution.class, u);

        Object boundElem = m.get(elem);
        Object boundUnified;

        if (boundElem instanceof ArgumentList) {
            ArgumentList boundArgs = (ArgumentList) boundElem;
            boundUnified = boundSubs.apply(boundArgs);
        } else if (boundElem instanceof Instance) {
            Instance boundInstance = (Instance) boundElem;
            boundUnified = boundSubs.apply(boundInstance);
        } else  if (boundElem instanceof Set) {
            Set<Instance> boundBody = (Set<Instance>) boundElem;
            boundUnified = boundSubs.apply(boundBody);
        } else {
            throw new VariableNotBoundException("Variable " + elem
                    + " not bound to type a unifier can be applied to.");
        }

        if (m.hasBound(unified)) {
            return m.get(unified).equals(boundUnified) ? Stream.of(m) : Stream.empty();
        }
        return Stream.of(m.bind(unified, boundUnified));
    }


    /**
     * Utility class used in unification above.
     */
    static class TupleComparator implements Comparator<Tuple>, Serializable {

        public static final long serialVersionUID = 19013L; // TODO: Not correct

        @Override
        public int compare(Tuple t1, Tuple t2) {
            // TODO: Fix these explicit strings
            return t1.getAs(String.class, "_iri").compareTo(t2.getAs(String.class, "_iri")); 
        }
    }
}
