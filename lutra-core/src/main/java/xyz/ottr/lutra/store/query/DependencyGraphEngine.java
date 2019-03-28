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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.model.types.ComplexType;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.store.DependencyGraph;
import xyz.ottr.lutra.store.TemplateNode;

public class DependencyGraphEngine extends QueryEngine<DependencyGraph> {

    public DependencyGraphEngine(DependencyGraph store) {
        this.store = store;
    }

    ////////////////////////
    // Template Relations 
    //  
    //  The following methods describe the base relations for template relations.
    //  They are somewhat verbose due to book keeping on which variables are
    //  bound and which are not. As each lookup is somewhat different, it is difficult
    //  to extract any code common to the methods.
    //
    //  All methods are written with only ifs and no else, thus, if the method branches into an
    //  if, it will return within that block. This makes it easier to follow the program flow.
    ////////////////////////

    @Override
    public Stream<Tuple> parameters(Tuple tuple, String template, String params) {

        Result<TemplateNode> resBoundTemplate = this.store.checkIsTemplate(tuple.getAs(String.class, template));
        if (!resBoundTemplate.isPresent()) {
            return Stream.empty(); // template argument is not a template
        }
        TemplateNode boundTemplate = resBoundTemplate.get();
        if (tuple.hasBound(params)) {
            ParameterList boundParams = tuple.getAs(ParameterList.class, params);
            return boundTemplate.getParameters().equals(boundParams) ? Stream.of(tuple) : Stream.empty();
        } 
        return boundTemplate.getParameters() == null
            ? Stream.empty() : Stream.of(tuple.bind(params, boundTemplate.getParameters()));
    }

    private TermList getUnderlyingList(Tuple tuple, String params) {
        
        if (tuple.get(params) instanceof ParameterList) {
            ParameterList boundParams = tuple.getAs(ParameterList.class, params);
            return boundParams.getTermList();
        }

        ArgumentList boundArgs = tuple.getAs(ArgumentList.class, params);
        return boundArgs.getTermList();
    }

    @Override
    public Stream<Tuple> length(Tuple tuple, String params, String length) {

        TermList terms = getUnderlyingList(tuple, params);
        Integer actualLength = terms.size();

        if (tuple.hasBound(length)) {
            return tuple.getAs(Integer.class, length).equals(actualLength)
                ? Stream.of(tuple) : Stream.empty();
        }
        return Stream.of(tuple.bind(length, actualLength));
    }

    @Override
    public Stream<Tuple> index(Tuple tuple, String params, String index, String val) {

        // TODO: Make index(t, ps, i, v) be false for none in place of v

        TermList terms = getUnderlyingList(tuple, params);
        if (tuple.hasBound(index)) {
            Integer boundIndex = tuple.getAs(Integer.class, index);
            if (boundIndex < 0 || terms.asList().size() <= boundIndex
                    || terms.get(boundIndex) == null) {
                // i not an index in params
                return Stream.empty();
            } 
            if (tuple.hasBound(val)) {
                // boundIndex is an index in params and val is bound, check if boundVal is at boundIndex
                Term boundVal = tuple.getAs(Term.class, val);
                return terms.get(boundIndex).equals(boundVal) ? Stream.of(tuple) : Stream.empty();
            }
            // bind val to boundIndex'th element
            return Stream.of(tuple.bind(val, terms.get(boundIndex)));
        } 
        // bind i, but only at v terms, if v is bound
        Term boundVal = tuple.hasBound(val) ? tuple.getAs(Term.class, val) : null;
        Stream.Builder<Tuple> tuples = Stream.builder();
        List<Term> paramList = terms.asList();
        for (int boundIndex = 0; boundIndex < paramList.size(); boundIndex++) {
            if (paramList.get(boundIndex) != null
                    && (boundVal == null || paramList.get(boundIndex).equals(boundVal))) {
                tuples.accept(tuple.bind(index, Integer.valueOf(boundIndex))
                                   .bind(val, paramList.get(boundIndex)));
            }
        }
        return tuples.build();
    }

    private Stream<Tuple> bindIndecies(Tuple tuple, TermList terms, String index) {

        if (tuple.hasBound(index)) {
            Integer boundIndex = tuple.getAs(Integer.class, index);
            if (boundIndex < 0 || terms.size() <= boundIndex
                    || terms.get(boundIndex) == null) {
                // boundIndex not an index in params
                return Stream.empty();
            } 
            return Stream.of(tuple);
        }

        Stream.Builder<Tuple> tuples = Stream.builder();
        List<Term> paramList = terms.asList();
        for (int boundIndex = 0; boundIndex < paramList.size(); boundIndex++) {
            tuples.accept(tuple.bind(index, Integer.valueOf(boundIndex)));
        }
        return tuples.build();
    }

    @Override
    public Stream<Tuple> hasOccurenceAt(Tuple tuple, String term, String inside, String level) {

        Term boundTerm = tuple.getAs(Term.class, term);
        return findOccurences(tuple, boundTerm, inside, level, 0);
    }
            
    private Stream<Tuple> findOccurences(Tuple tuple, Term term, String inside, String level, int current) {

        if (tuple.hasBound(level)
            && current > tuple.getAs(Integer.class, level).intValue()) {

            return Stream.empty(); // Level will never match
        }

        if (term instanceof TermList) { // Match recursively on inner terms with current level +1
            Stream.Builder<Tuple> stream = Stream.builder();
            for (Term inner : ((TermList) term).asList()) {
                findOccurences(tuple, inner, inside, level, current + 1).forEach(stream);
            }
            return stream.build();
        }

        // Has non-list term, just need to check for equality of level and term
        if (tuple.hasBound(level)
            && current != tuple.getAs(Integer.class, level).intValue()) {

            return Stream.empty();
        }

        Tuple tupleWLvl = tuple.bind(level, current);

        if (tupleWLvl.hasBound(inside)) {
            return term.equals(tupleWLvl.getAs(Term.class, inside))
                ? Stream.of(tupleWLvl) : Stream.empty();
        }

        return Stream.of(tupleWLvl.bind(inside, term));
    }

    @Override
    public Stream<Tuple> type(Tuple tuple, String term, String type) {

        Term boundTerm = tuple.getAs(Term.class, term);
        TermType actType = boundTerm.getType();

        if (tuple.hasBound(type)) {
            TermType boundType = tuple.getAs(TermType.class, type);
            return boundType.equals(actType) ? Stream.of(tuple) : Stream.empty();
        }
        return Stream.of(tuple.bind(type, actType));
    }

    @Override
    public Stream<Tuple> innerTypeAt(Tuple tuple, String type, String level, String inner) {
        TermType boundType = tuple.getAs(TermType.class, type);

        if (tuple.hasBound(level)) {
            int boundLvl = tuple.getAs(Integer.class, level).intValue();
            return bindInnerTypeAt(tuple, boundType, boundLvl, inner);
        }
        return bindInnerTypes(tuple, boundType, level, inner, 0);
    }

    private Stream<Tuple> bindInnerTypeAt(Tuple tuple, TermType type, int level, String inner) {

        TermType toFind = type;

        for (int i = 0; i < level; i++) {
            if (!(toFind instanceof ComplexType)) {
                return Stream.empty();
            }
            toFind = ((ComplexType) toFind).getInner();
        }

        if (tuple.hasBound(inner)) {
            return tuple.getAs(TermType.class, inner).equals(toFind)
                ? Stream.of(tuple) : Stream.empty();
        }
        return Stream.of(tuple.bind(inner, toFind));
    }
            
    private Stream<Tuple> bindInnerTypes(Tuple tuple, TermType type, String level, String inner, int current) {

        Stream<Tuple> stream = Stream.of(tuple.bind(level, current).bind(inner, type));

        if (type instanceof ComplexType) {
            stream = Stream.concat(stream,
                bindInnerTypes(tuple, ((ComplexType) type).getInner(), level, inner, current + 1));
        }
        return stream;
    }

    @Override
    public Stream<Tuple> innerType(Tuple tuple, String type, String inner) {

        String lvl = Tuple.freshVar();
        return innerTypeAt(tuple.bind(lvl, 1), type, lvl, inner);
    }

    @Override
    public Stream<Tuple> isOptional(Tuple tuple, String params, String index) {
        ParameterList boundParams = tuple.getAs(ParameterList.class, params);
        return bindIndecies(tuple, boundParams.getTermList(), index)
            .filter(tup -> boundParams.isOptional(tup.getAs(Integer.class, index)));
    }

    @Override
    public Stream<Tuple> isNonBlank(Tuple tuple, String params, String index) {
        ParameterList boundParams = tuple.getAs(ParameterList.class, params);
        return bindIndecies(tuple, boundParams.getTermList(), index)
            .filter(tup -> boundParams.isNonBlank(tup.getAs(Integer.class, index)));
    }

    @Override
    public Stream<Tuple> hasListExpander(Tuple tuple, String args, String index) {
        ArgumentList boundArgs = tuple.getAs(ArgumentList.class, args);
        return bindIndecies(tuple, boundArgs.getTermList(), index)
            .filter(tup -> boundArgs.hasListExpander(tup.getAs(Integer.class, index)));
    }

    @Override
    public Stream<Tuple> hasCrossModifier(Tuple tuple, String instance) {
        Instance boundIns = tuple.getAs(Instance.class, instance);
        return boundIns.getArguments().hasCrossExpander()
            ? Stream.of(tuple)
            : Stream.empty();
    }

    @Override
    public Stream<Tuple> hasZipMinModifier(Tuple tuple, String instance) {
        Instance boundIns = tuple.getAs(Instance.class, instance);
        return boundIns.getArguments().hasZipMinExpander()
            ? Stream.of(tuple)
            : Stream.empty();
    }

    @Override
    public Stream<Tuple> hasZipMaxModifier(Tuple tuple, String instance) {
        Instance boundIns = tuple.getAs(Instance.class, instance);
        return boundIns.getArguments().hasZipMaxExpander()
            ? Stream.of(tuple)
            : Stream.empty();
    }

    @Override
    public Stream<Tuple> hasExpansionModifier(Tuple tuple, String instance) {
        Instance boundIns = tuple.getAs(Instance.class, instance);
        return boundIns.getArguments().hasListExpander()
            ? Stream.of(tuple)
            : Stream.empty();
    }

    @Override
    public Stream<Tuple> body(Tuple tuple, String template, String body) {

        Result<TemplateNode> resBoundTemplate = this.store.checkIsTemplate(tuple.getAs(String.class, template));
        if (!resBoundTemplate.isPresent()) {
            return Stream.empty(); //  argument is not a template
        }
        Template boundTemplate = this.store.getTemplate(resBoundTemplate.get().getIRI()).get();
        Set<Instance> deps = boundTemplate.getBody();

        if (tuple.hasBound(body)) {
            return deps.equals(tuple.get(body)) ? Stream.of(tuple) : Stream.empty();
        }

        return Stream.of(tuple.bind(body, deps));
    }

    @Override
    public Stream<Tuple> instance(Tuple tuple, String body, String instance) {
        
        Set<Instance> boundBody = tuple.getAs(HashSet.class, body);

        if (tuple.hasBound(instance)) {

            Instance boundInstance = tuple.getAs(Instance.class, instance);

            if (!boundBody.contains(boundInstance)) {
                return Stream.empty();
            }
            return Stream.of(tuple);
        }
        return boundBody.stream().map(boundInstance -> tuple.bind(instance, boundInstance));
    }

    @Override
    public Stream<Tuple> instanceIRI(Tuple tuple, String instance, String iri) {

        Instance boundInstance = tuple.getAs(Instance.class, instance);

        if (tuple.hasBound(iri)) {
            return tuple.getAs(String.class, iri).equals(boundInstance.getIRI())
                ? Stream.of(tuple) : Stream.empty();
        }
        return Stream.of(tuple.bind(iri, boundInstance.getIRI()));
    }

    @Override
    public Stream<Tuple> arguments(Tuple tuple, String instance, String args) {

        Instance boundInstance = tuple.getAs(Instance.class, instance);

        if (tuple.hasBound(args)) {
            return tuple.getAs(ArgumentList.class, args).equals(boundInstance.getArguments())
                ? Stream.of(tuple) : Stream.empty();
        }
        return Stream.of(tuple.bind(args, boundInstance.getArguments()));
    }

    @Override
    public Stream<Tuple> isUndefined(Tuple tuple, String template) {
        String iri = tuple.getAs(String.class, template);
        return !this.store.containsTemplate(iri)
            ? Stream.of(tuple)
            : Stream.empty();
    }

    @Override
    public Stream<Tuple> isSignature(Tuple tuple, String template) {
        String iri = tuple.getAs(String.class, template);
        return this.store.containsSignature(iri)
            ? Stream.of(tuple)
            : Stream.empty();
    }

    @Override
    public Stream<Tuple> isBase(Tuple tuple, String template) {
        String iri = tuple.getAs(String.class, template);
        return this.store.containsBase(iri)
            ? Stream.of(tuple)
            : Stream.empty();
    }

    @Override
    public Stream<Tuple> unifiesVal(Tuple tuple, String val1, String val2, String unifier) {
        
        Term boundVal1 = tuple.getAs(Term.class, val1);
        Term boundVal2 = tuple.getAs(Term.class, val2);

        if (tuple.hasBound(unifier)) {
            Substitution boundUnifier = tuple.getAs(Substitution.class, unifier);
            return boundUnifier.get(boundVal1).equals(boundUnifier.get(boundVal2))
                ? Stream.of(tuple) : Stream.empty();
        }

        Optional<Term> unified = boundVal1.unify(boundVal2);

        if (!unified.isPresent()) {
            return Stream.empty();
        }

        Map<Term, Term> boundUnifier = new HashMap<>();
        boundUnifier.put(boundVal1, unified.get());
        boundUnifier.put(boundVal2, unified.get());
        return Stream.of(tuple.bind(unifier, new Substitution(boundUnifier)));
    }

    @Override
    public Stream<Tuple> isDependencyOf(Tuple tuple, String instanceIRI, String templateIRI) {
        String boundInstanceIRI = tuple.getAs(String.class, instanceIRI);
        Result<Set<String>> dependsOnIns = this.store.getDependsOn(boundInstanceIRI);
        if (!dependsOnIns.isPresent()) {
            return Stream.empty();
        }
        if (tuple.hasBound(templateIRI)) {
            String boundTemplateIRI = tuple.getAs(String.class, templateIRI);
            return dependsOnIns.get().contains(boundTemplateIRI)
                ? Stream.of(tuple) : Stream.empty();
        }
        return dependsOnIns.get().stream().map(iri -> tuple.bind(templateIRI, iri));
    }

    @Override
    public Stream<Tuple> dependsTransitive(Tuple tuple, String templateIRI, String instanceIRI) {

        String boundTemplateIRI = tuple.getAs(String.class, templateIRI);

        Set<String> visited = new HashSet<>(); // For cycle detection

        Result<Set<String>> nextRes = this.store.getDependencies(boundTemplateIRI);
        if (!nextRes.isPresent()) {
            return Stream.empty();
        }
        Set<String> next = nextRes.get();
        Stream.Builder<Tuple> tuples = Stream.builder();

        while (!next.isEmpty()) {
            Set<String> nextNext = new HashSet<>();
            for (String ins : next) {
                if (tuple.hasBound(instanceIRI)) {
                    String boundInstanceIRI = tuple.getAs(String.class, instanceIRI);
                    if (boundInstanceIRI.equals(ins)) {
                        return Stream.of(tuple);
                    }
                }
                if (!visited.contains(ins)) {
                    tuples.add(tuple.bind(instanceIRI, ins));
                    this.store.getDependencies(ins).ifPresent(iris -> nextNext.addAll(iris));
                }
            }
            visited.addAll(next);
            next = nextNext;
        }

        return tuple.hasBound(instanceIRI)
            ? Stream.empty()
            : tuples.build();
    }
}
