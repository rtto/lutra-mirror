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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.ReaderRegistry;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.NoneTerm;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.store.query.Check;
import xyz.ottr.lutra.store.query.CheckFactory;
import xyz.ottr.lutra.store.query.DependencyGraphEngine;
import xyz.ottr.lutra.store.query.Query;
import xyz.ottr.lutra.store.query.QueryEngine;
import xyz.ottr.lutra.store.query.Tuple;

public class DependencyGraph implements TemplateStore {

    /**
     * Constructs a predicate that takes an edge and returns true if the edge points
     * to a template with an IRI in argument set. Can be used with the
     * expansion-methods to expand a given vocabulary.
     *
     * @param iris
     *            a set of the IRIs to expand
     * @return a predicate returning true on edges pointing to a template with IRI
     *         in argument set
     */
    public static Predicate<Dependency> vocabularyExpansionPredicate(Set<String> iris) {
        return (e) -> {
            return iris.contains(e.to.getIRI());
        };
    }

    private Set<TemplateNode> roots;
    private Map<String, TemplateNode> nodes;
    private Map<TemplateNode, Set<Dependency>> dependencies;
    private Map<String, Set<String>> instanceIndex;
    private ReaderRegistry readerRegistry;

    private final Logger log = LoggerFactory.getLogger(DependencyGraph.class);

    /**
     * Constructs a graph representing template definitions and instances.
     */
    public DependencyGraph(ReaderRegistry readerRegistry) {
        this.roots = new HashSet<>();
        this.nodes = new HashMap<>();
        this.dependencies = new HashMap<>();
        this.instanceIndex = new HashMap<>();
        this.readerRegistry = readerRegistry;
    }
    
    public DependencyGraph(ReaderRegistry readerRegistry, Template... ts) {
        this(readerRegistry);
        for (Template t : ts) {
            addTemplate(t);
        }
    }

    private void addInstanceToIndex(String instance, String template) {
        instanceIndex.putIfAbsent(instance, new HashSet<>());
        instanceIndex.get(instance).add(template);
    }

    private void addInstanceToIndex(String instance, ArgumentList args, String template) {
        if (instance.equals(OTTR.BaseURI.Triple)) {
            addInstanceToIndex(args.get(1).toString(), template);
        } else {
            addInstanceToIndex(instance, template);
        }
    }

    private void removeInstanceFromIndex(String instance, String template) {
        instanceIndex.get(instance).remove(template);
    }

    private void removeInstanceFromIndex(String instance, ArgumentList args, String template) {
        if (instance.equals(OTTR.BaseURI.Triple)) {
            removeInstanceFromIndex(args.get(1).toString(), template);
        } else {
            removeInstanceFromIndex(instance, template);
        }
    }

    private void addNode(TemplateNode n) {
        if (!nodes.containsKey(n.getIRI())) {
            this.roots.add(n);
            this.dependencies.put(n, new HashSet<Dependency>());
            this.nodes.put(n.getIRI(), n);
        }
    }

    @Override
    public void accept(TemplateSignature template) {
        this.addTemplateObject(template);
    }

    /**
     * Adds a template as a node to the graph, without knowing the parameters of the
     * template.
     *
     * @param uri
     *            the URI of the template to add
     */
    private TemplateNode addTemplateNode(String uri) {
        Result<TemplateNode> nodeRes = checkIsTemplate(uri);
        if (nodeRes.isPresent()) {
            return nodeRes.get();
        }
        TemplateNode node = new TemplateNode(uri, TemplateNode.Type.UNDEFINED);
        addNode(node);
        return node;
    }

    /**
     * Adds a template as a node to the graph.
     *
     * @param uri
     *            the URI of the template to add
     * @param params
     *            the parameters in the head of the template to add
     * @param isBaseTemplate
     *            true if the node to create represets a base template, false otherwise
     */
    private TemplateNode addTemplateSignature(String uri, ParameterList params, boolean isBaseTemplate) {
        TemplateNode node = addTemplateNode(uri);
        node.addParameters(params);
        if (isBaseTemplate) {
            node.setType(TemplateNode.Type.BASE);
        } else {
            node.setType(TemplateNode.Type.SIGNATURE);
        }
        return node;
    }

    @Override
    public boolean addTemplateSignature(TemplateSignature templateSignature) {
        addTemplateSignature(templateSignature.getIRI(), templateSignature.getParameters(),
                templateSignature.isBaseTemplate());
        log.info("Adding template signature " + templateSignature.getIRI());
        return true;
    }

    @Override
    public boolean addTemplate(Template template) {
        addTemplateSignature(template);
        Result<TemplateNode> tempNodeRes = checkIsTemplate(template.getIRI());
        if (!tempNodeRes.isPresent()) {
            return false;
        }
        TemplateNode tempNode = tempNodeRes.get();
        if (template.getBody() != null) {
            if (!this.dependencies.get(tempNode).isEmpty()) {
                return false;
            }
            log.info("Adding body for template " + template.getIRI());
            for (Instance i : template.getBody()) {
                TemplateNode insNode = addTemplateNode(i.getIRI());
                addDependency(tempNode, i.getArguments(), insNode);
            }
            tempNode.setType(TemplateNode.Type.DEFINITION);
        }
        return true;
    }

    /**
     * Adds an instance call to a knowledge base.
     *
     * @param knowledgeBase
     *            the URI of the knowledge base to add an instance call to
     * @param pl
     *            the parameters representing the arguments in the call
     * @param instance
     *            the URI of the template called
     */
    public void addInstance(String knowledgeBase, ArgumentList pl, String instance) {
        TemplateNode kbNode = addTemplateNode(knowledgeBase);
        TemplateNode tempNode = addTemplateNode(instance);
        addDependency(kbNode, pl, tempNode);
    }

    private void addDependency(TemplateNode fromNode, ArgumentList pl, TemplateNode toNode) {
        addDependency(new Dependency(fromNode, pl, toNode));
    }

    private void addDependency(Dependency edge) {
        this.dependencies.get(edge.from).add(edge);
        this.roots.remove(edge.to);
        addInstanceToIndex(edge.to.getIRI(), edge.argumentList, edge.from.getIRI());
    }

    private void removeDependency(Dependency dependency) {
        this.dependencies.get(dependency.from).remove(dependency);
        String instance = dependency.to.getIRI();
        removeInstanceFromIndex(instance, dependency.argumentList, dependency.from.getIRI());
        if (!instance.equals(OTTR.BaseURI.Triple) && this.instanceIndex.get(instance).isEmpty()) {
            this.roots.add(dependency.to);
        }
    }

    /**
     * Returns a Result containing the template denoted by the argument
     * IRI if the IRI has a template within this store, otherwise
     * returns empty Result.
     */
    public Result<TemplateNode> checkIsTemplate(String iri) {
        TemplateNode node = nodes.get(iri);
        if (node == null) {
            return Result.empty(Message.error("IRI not found in TemplateStore: " + iri + "."));
        }
        return Result.of(node);
    }

    @Override
    public boolean containsTemplate(String iri) {
        return this.nodes.containsKey(iri) && !this.nodes.get(iri).isUndefined();
    }

    @Override
    public boolean containsBase(String iri) {
        return this.nodes.containsKey(iri) && this.nodes.get(iri).isBase();
    }

    @Override
    public boolean containsSignature(String iri) {
        return this.nodes.containsKey(iri) && this.nodes.get(iri).isSignature();
    }

    @Override
    public boolean containsDefinitionOf(String iri) {
        return this.nodes.containsKey(iri) && this.nodes.get(iri).isDefinition();
    }

    private boolean isLeafNode(TemplateNode n) {
        return this.dependencies.get(n).isEmpty();
    }

    /**
     * Returns a Tuple with unifiers making body of template with IRI iri1
     * subset of body of template with IRI iri2, but returns empty list if
     * no unifier exists.
     */
    public Stream<Tuple> unifiesBodyConstants(String iri1, String iri2) {
        Tuple cons = new Tuple().bind("T1", iri1).bind("T2", iri2);
        Query unifies = Query.body("T1", "B1")
            .and(Query.body("T2", "B2"))
            .and(Query.unifiesBody("B1", "B2", "UB"));
        return unifies.eval(new DependencyGraphEngine(this), cons);
    }

    @Override
    public boolean refactor(String toUse, String toChange) {

        // Compute unifier
        Stream<Tuple> ans = unifiesBodyConstants(toUse, toChange);
        Optional<Tuple> optUnifier = ans.findAny();
        if (!optUnifier.isPresent()) {
            return false;
        }
        Substitution unifier = optUnifier.get().getAs(Substitution.class, "UB");
        
        // Remove body parts
        Result<TemplateNode> changeNodeR = checkIsTemplate(toChange);
        Result<TemplateNode> useNodeR = checkIsTemplate(toUse);
        if (!changeNodeR.isPresent() || !useNodeR.isPresent()) {
            return false;
        }
        TemplateNode changeNode = changeNodeR.get();
        TemplateNode useNode = useNodeR.get();

        for (Dependency toRemove : new HashSet<>(this.dependencies.get(useNode))) {
            Dependency toRemoveUnifd = new Dependency(changeNode,
                    unifier.apply(toRemove.argumentList), toRemove.to);
            removeDependency(toRemoveUnifd);
        }
         
        // Add dependency of toUse
        ArgumentList newArgs = new ArgumentList(unifier.apply(useNode.getParameters().getTermList()));
        addDependency(changeNode, newArgs, useNode);
        return true;
    }

    private Map<TemplateNode, Integer> getIndegrees() {
        Map<TemplateNode, Integer> indegrees = new HashMap<>(this.dependencies.keySet().size());
        for (Map.Entry<TemplateNode, Set<Dependency>> es : this.dependencies.entrySet()) {
            for (Dependency e : es.getValue()) {
                indegrees.put(e.to, indegrees.getOrDefault(e.to, 0) + 1);
            }
        }
        return indegrees;
    }

    private List<TemplateNode> topologicallySort() {

        Map<TemplateNode, Integer> indegrees = getIndegrees();
        List<TemplateNode> sorted = new ArrayList<>();
        Set<TemplateNode> next = new HashSet<>(this.roots);

        while (!next.isEmpty()) {
            Set<TemplateNode> nextNext = new HashSet<>();

            for (TemplateNode from : next) {
                sorted.add(0, from);

                if (isLeafNode(from)) {
                    continue;
                }

                for (Dependency e : this.dependencies.get(from)) {
                    indegrees.put(e.to, indegrees.get(e.to) - 1);
                    if (indegrees.get(e.to) == 0) {
                        nextNext.add(e.to);
                    }
                }
            }
            next = nextNext;
        }
        return sorted;
    }

    /**
     * Expands the edges in toExpand once (but includes an expansion of list expanders)
     * according to argument predicate and adds all expanded dependencies into expanded
     * set and all unexpanded into the unexpanded set.
     */
    private void expandEdges(Set<Result<Dependency>> toExpand, Set<Result<Dependency>> expanded,
            Set<Result<Dependency>> unexpanded, Predicate<Dependency> shouldExpand) {

        for (Result<Dependency> edgeRes : toExpand) {

            // Check that we can and should expand
            final Result<Dependency> checkedEdge = edgeRes.flatMap(edge -> checkForExpansionErrors(edge));
            // TODO: May loose messages on checkedEdge if present, but contains messages
            if (!checkedEdge.isPresent()) {
                unexpanded.add(checkedEdge);
                continue;
            } else if (!checkedEdge.filter(shouldExpand).isPresent()) {
                unexpanded.add(checkedEdge);
                continue; 
            } else if (checkedEdge.filter(edge -> edge.shouldDiscard()).isPresent()) {
                continue;
            }

            // Then expand instance
            Dependency edge = checkedEdge.get();
            if (edge.argumentList.hasListExpander()) {
                if (edge.canExpandExpander()) {
                    Set<Result<Dependency>> exp = edge.expandListExpander();
                    exp.forEach(e -> e.addToTrace(checkedEdge));
                    expandEdges(exp, expanded, unexpanded, shouldExpand); 
                } else {
                    unexpanded.add(checkedEdge);
                }
            } else if (edge.canExpand()) {
                Set<Result<Dependency>> exp = expandEdgeWithChecks(edge);
                exp.forEach(e -> e.addToTrace(checkedEdge));
                expanded.addAll(exp);
            } else {
                unexpanded.add(checkedEdge);
            }
        }
    }

    private Result<Dependency> checkForExpansionErrors(Dependency edge) {

        Result<Dependency> res = Result.of(edge);

        if (edge.argumentList.hasListExpander()
            && !edge.canExpandExpander()
            && edge.isInstance()) {

            res = Result.empty(Message.error(
                    "Cannot expand expander on instance of template " + edge.to.getIRI()
                    + " with arguments " + edge.argumentList.toString()
                    + ": it contains blank nodes."), res);
        }

        if (edge.to.isUndefined() || edge.isInstance() && edge.to.isSignature()) {
            res = Result.empty(Message.error(
                    "Cannot expand instance of template " + edge.to.getIRI()
                    + " with arguments " + edge.argumentList.toString()
                    + (edge.from == null ? "" : " in body of " + edge.from.getIRI())
                    + ": missing definition."), res);
        }

        return res;
    }

    /**
     * Used for expanding instances: Expands edges if template used correctly, and gives
     * empty Result with error message othewise.
     */
    private Set<Result<Dependency>> expandEdgeWithChecks(Dependency edge) {

        Set<Result<Dependency>> expanded = new HashSet<>();

        Result<Substitution> resSubs = checkAndMakeSubstitution(edge.argumentList, edge.to.getParameters());
        if (!resSubs.isPresent()) {
            expanded.add(Result.empty(resSubs));
            return expanded;
        }
        
        for (Dependency edgeEdge : this.dependencies.get(edge.to)) {
            Dependency newDep = new Dependency(edge.from, resSubs.get().apply(edgeEdge.argumentList), edgeEdge.to);
            expanded.add(Result.of(newDep, resSubs));
        }
        return expanded;
    }

    private Result<Substitution> checkAndMakeSubstitution(ArgumentList args, ParameterList params) {
        // TODO: Check types
        return Substitution.makeSubstitution(args, params);
    }

    @Override
    public Set<String> getIRIs(Predicate<String> pred) {
        return nodes.keySet().stream()
            .filter(iri -> pred.test(iri))
            .collect(Collectors.toSet());
    }

    @Override
    public Result<Template> getTemplate(String iri) {

        Result<TemplateNode> resTemplate = checkIsTemplate(iri);
        Set<Instance> body = new HashSet<Instance>();

        resTemplate.ifPresent(template -> {
            for (Dependency d : dependencies.get(template)) {
                body.add(new Instance(d.to.getIRI(), d.argumentList));
            }
        });
        return resTemplate.map(template ->
                new Template(template.getIRI(), template.getParameters(), body));
    }

    @Override
    public Result<TemplateSignature> getTemplateSignature(String iri) {

        Result<TemplateNode> resTemplate = checkIsTemplate(iri);

        return resTemplate.map(template ->
            new TemplateSignature(template.getIRI(), template.getParameters(), template.isBase()));
    }

    @Override
    public Result<Set<String>> getDependsOn(String template) {
        return this.instanceIndex.containsKey(template)
            ? Result.of(this.instanceIndex.get(template)) : Result.empty();
    }

    @Override
    public Result<Set<String>> getDependencies(String template) {
        if (!this.nodes.containsKey(template)) {
            return Result.empty();
        }

        Set<String> res = new HashSet<>();
        this.dependencies
            .get(this.nodes.get(template))
            .stream()
            .forEach(dep -> res.add(dep.to.getIRI()));
        return Result.of(res);
    }

    @Override
    public Set<String> getMissingDependencies() {

        Set<String> missing = new HashSet<>();
        
        for (Map.Entry<String, TemplateNode> iriNode : this.nodes.entrySet()) {
            TemplateNode node = iriNode.getValue();
            if (node.isUndefined() || node.isSignature()) {
                missing.add(iriNode.getKey());
            }
        }
        return missing;
    }

    private Set<Result<Dependency>> toResultDependencies(Set<Instance> instances) {
        return instances.stream()
            .map(i -> checkIsTemplate(i.getIRI()).map(template ->
                        new Dependency(null, i.getArguments(), template)))
            .collect(Collectors.toSet());
    }

    /**
     * Expands all instances in argument set recursively according to this graph
     * when shouldExpand holds, and adds the resulting leaf-nodes to argument
     * writer.
     */
    private ResultStream<Instance> expandInstances(Set<Instance> instances,
            Predicate<Dependency> shouldExpand) {

        Set<Result<Dependency>> finalExpansion = new HashSet<>();
        Set<Result<Dependency>> toExpandRes = toResultDependencies(instances);
        
        // Check arguments (number of arguments and types)
        toExpandRes = toExpandRes.stream()
            .map(ins -> ins.flatMap(this::checkArguments))
            .collect(Collectors.toSet());

        while (!toExpandRes.isEmpty()) {

            Set<Result<Dependency>> expanded = new HashSet<>();
            expandEdges(toExpandRes, expanded, finalExpansion, shouldExpand);
            toExpandRes = expanded;
        }

        ResultStream<Instance> expandedInstances = new ResultStream<>(finalExpansion)
            .innerMap(dep -> new Instance(dep.to.getIRI(), dep.argumentList));
        return expandedInstances;
    }

    private Result<Dependency> checkArguments(Dependency ins) {
        
        ArgumentList args = ins.argumentList;
        ParameterList params = ins.to.getParameters();

        // First check correct number of arguments
        if (params == null) {
            return Result.error("Missing definition of template with IRI " + ins.to.getIRI() + ".");
        } else if (params.size() != args.size()) {
            return Result.error("Instance of template with IRI " + ins.to.getIRI() 
                + " with arguments " + args.toString() + " has " + args.size() 
                + " arguments, but template expects " + params.size() + ".");
        }

        // Then check types and non-blanks

        Result<Dependency> insRes = Result.of(ins);

        for (int i = 0; i < args.size(); i++) {
            Term arg = args.get(i);
            Term param = params.get(i);

            if (!arg.getType().isCompatibleWith(param.getType())) {
                String err = "Argument " + arg.toString() + " with index " + i 
                    + " to template with IRI " + ins.to.getIRI() + " has incompatible type: "
                    + "Expected type compatible with " + param.getType().toString() + " but got " + arg.getType().toString() + ".";
                insRes = insRes.fail(Message.error(err));
            }
            if (arg instanceof BlankNodeTerm && params.isNonBlank(i)) {
                String err = "Argument " + arg.toString() + " with index " + i 
                    + " to template with IRI " + ins.to.getIRI() + " is a blank node, but "
                    + " corresponding parameter " + params.get(i).toString() + " is marked as non-blank.";
                insRes = insRes.fail(Message.error(err));
            }
        }
        return insRes;
    }

    @Override
    public ResultStream<Instance> expandInstance(Instance instance) {
        Set<Instance> instanceSet = new HashSet<>();
        instanceSet.add(instance);
        return expandInstances(instanceSet, e -> true);
    }

    /**
     * Expands all nodes except where argument predicate holds, where arguments to
     * predicate is one of its outgoing edges.
     */
    public Result<DependencyGraph> expandOnly(Predicate<Dependency> shouldExpand) {

        log.info("Expanding definitions.");
        List<TemplateNode> sorted = topologicallySort();

        DependencyGraph ngraph = new DependencyGraph(this.readerRegistry);
        Result<DependencyGraph> graphRes = Result.of(ngraph);

        for (TemplateNode n : sorted) {

            ngraph.addNode(n);
            if (!isLeafNode(n)) {
                Set<Result<Dependency>> expanded = new HashSet<>(); // Used for both expanded and unexpanded
                ngraph.expandEdges(Result.lift(this.dependencies.get(n)), expanded, expanded, shouldExpand);
                Result<Set<Dependency>> resExpanded = Result.aggregate(expanded);

                graphRes.addToTrace(resExpanded);
                resExpanded.ifPresent(deps -> deps.forEach(ngraph::addDependency));
            }
        }
        return graphRes;
    }

    @Override
    public Result<DependencyGraph> expandAll() {
        return expandOnly(e -> true);
    }

    @Override
    public Result<DependencyGraph> expandVocabulary(Set<String> iris) {
        return expandOnly(vocabularyExpansionPredicate(iris));
    }

    private List<Message> checkTemplatesFor(List<Check> checks) {

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(this);
        return checks
            .stream()
            .flatMap(c -> c.check(engine))
            .collect(Collectors.toList());
    }

    @Override
    public List<Message> checkTemplates() {
        return checkTemplatesFor(CheckFactory.allChecks);
    }

    @Override
    public List<Message> checkTemplatesForErrorsOnly() {
        return checkTemplatesFor(CheckFactory.failsOnErrorChecks);
    }

    @Override
    public String toString() {
        
        StringBuilder str = new StringBuilder();

        for (Map.Entry<TemplateNode, Set<Dependency>> ens : this.dependencies.entrySet()) {
            TemplateNode node = ens.getKey();
            str.append(node.toString() + ":" + "\n");
            Map<TemplateNode, Set<ArgumentList>> deps = new HashMap<>();
            for (Dependency e : ens.getValue()) {
                deps.putIfAbsent(e.to, new HashSet<ArgumentList>());
                deps.get(e.to).add(e.argumentList);
            }
            for (Map.Entry<TemplateNode, Set<ArgumentList>> dep : deps.entrySet()) {
                str.append("  " + dep.getKey().toString() + "\n");
                for (ArgumentList args : dep.getValue()) {
                    str.append("    => " + args.toString() + "\n");
                }
            }
            str.append("\n\n");
        }
        return str.toString();
    }

    @Override
    public ReaderRegistry getReaderRegistry() {
        return this.readerRegistry;
    }

    static class Dependency {
        public final TemplateNode from;
        public final ArgumentList argumentList;
        public final TemplateNode to;

        public Dependency(TemplateNode from, ArgumentList argumentList, TemplateNode to) {
            this.from = from;
            this.argumentList = argumentList;
            this.to = to;
        }

        public boolean shouldDiscard() {

            // Should discard this instance if it contains none at a non-optional position
            for (int i = 0; i < this.argumentList.size(); i++) {
                if (this.argumentList.get(i) instanceof NoneTerm
                    && !this.to.isOptional(i)
                    && !this.to.getParameters().hasDefaultValue(i)) { 
                    return true;
                }
            }
            return false;
        }

        public boolean isInstance() {
            return this.from == null;
        }

        /**
         * Checks if this edge can be expanded (i.e. not base and no optional variables),
         * but does not check for missing definitions.
         */
        public boolean canExpand() {
            if (this.to.isBase()) {
                return false;
            }
            if (this.isInstance()) {
                return true;
            }
            for (int i = 0; i < this.argumentList.size(); i++) {
                Term arg = this.argumentList.get(i);
                if (arg.isVariable() && this.from.isOptional(arg) && !this.to.isOptional(i)) { 
                    return false;
                }
            }
            return true;
        }

        /**
         * Checks if this edge's expander can be expanded (i.e. no variable or blank marked for expansion),
         * but does not check for missing definitions.
         */
        public boolean canExpandExpander() {
            for (int i = 0; i < this.argumentList.size(); i++) {
                Term arg = this.argumentList.get(i);
                if (this.argumentList.hasListExpander(arg)
                    && (arg.isVariable() || arg instanceof BlankNodeTerm)) { 
                    return false;
                }
            }
            return true;
        }

        public Set<Result<Dependency>> expandListExpander() {
            Set<Result<Dependency>> expanded = new HashSet<>();
            for (ArgumentList args : this.argumentList.expandListExpander()) {
                expanded.add(Result.of(new Dependency(this.from, args, this.to)));
            }
            return expanded;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.from, this.argumentList, this.to);
        }

        @Override
        public boolean equals(Object o) {
            return this == o
                || o != null
                    && this.getClass().equals(o.getClass())
                    && Objects.equals(this.from, ((Dependency) o).from)
                    && Objects.equals(this.argumentList, ((Dependency) o).argumentList)
                    && Objects.equals(this.to, ((Dependency) o).to);
        }

        @Override
        public String toString() {
            String fromStr = this.from == null ? "" : this.from.toString();
            String argsStr = this.argumentList == null ? "" : this.argumentList.toString();
            String toStr = this.to == null ? "" : this.to.toString();
            return fromStr + "--" + argsStr + "--> " + toStr;
        }

    }
}
