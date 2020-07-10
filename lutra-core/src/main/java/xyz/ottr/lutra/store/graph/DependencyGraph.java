package xyz.ottr.lutra.store.graph;

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
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.FormatManager;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ListExpander;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.store.Query;
import xyz.ottr.lutra.store.QueryEngine;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.store.Tuple;
import xyz.ottr.lutra.store.checks.Check;
import xyz.ottr.lutra.store.checks.CheckLibrary;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

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
    public static Predicate<DependencyEdge> vocabularyExpansionPredicate(Set<String> iris) {
        return (e) -> iris.contains(e.to.getIri());
    }

    private final Set<TemplateNode> roots;
    private final Map<String, TemplateNode> nodes;
    private final Map<TemplateNode, Set<DependencyEdge>> dependencies;
    private final Map<String, Set<String>> instanceIndex;
    private final FormatManager formatManager;
    private Optional<TemplateStore> standardLibrary;

    private static final Logger log = LoggerFactory.getLogger(DependencyGraph.class);

    /**
     * Constructs a graph representing template definitions and instances.
     */
    public DependencyGraph(FormatManager formatManager) {
        this.roots = new HashSet<>();
        this.nodes = new HashMap<>();
        this.dependencies = new HashMap<>();
        this.instanceIndex = new HashMap<>();
        this.formatManager = formatManager;
        this.standardLibrary = Optional.empty();
    }
    
    public DependencyGraph(FormatManager formatManager, Template... ts) {
        this(formatManager);
        for (Template t : ts) {
            addTemplate(t);
        }
    }

    private void addInstanceToIndex(String instance, String template) {

        this.instanceIndex.putIfAbsent(instance, new HashSet<>());
        this.instanceIndex.get(instance).add(template);
    }

    private void addInstanceToIndex(String instance, List<Argument> args, String template) {
        if (instance.equals(OTTR.BaseURI.Triple)) {
            addInstanceToIndex(args.get(1).toString(), template);
        } else {
            addInstanceToIndex(instance, template);
        }
    }

    private void removeInstanceFromIndex(String instance, String template) {

        this.instanceIndex.get(instance).remove(template);
    }

    private void removeInstanceFromIndex(String instance, List<Argument> args, String template) {
        if (instance.equals(OTTR.BaseURI.Triple)) {
            removeInstanceFromIndex(args.get(1).toString(), template);
        } else {
            removeInstanceFromIndex(instance, template);
        }
    }

    private void addNode(TemplateNode n) {
        if (!this.nodes.containsKey(n.getIri())) {
            this.roots.add(n);
            this.dependencies.put(n, new HashSet<>());
            this.nodes.put(n.getIri(), n);
        }
    }

    @Override
    public void accept(Signature template) {
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

    @Override
    public boolean addTemplateSignature(Signature signature) {
        log.info("Adding signature " + signature.getIri());
        TemplateNode node = addTemplateNode(signature.getIri());
        node.setParameters(signature.getParameters());
        node.setType(TemplateNode.getTemplateNodeType(signature));
        return true;
    }

    @Override
    public boolean addTemplate(Template template) {
        addTemplateSignature(template);
        Result<TemplateNode> tempNodeRes = checkIsTemplate(template.getIri());
        if (!tempNodeRes.isPresent()) {
            return false;
        }
        TemplateNode tempNode = tempNodeRes.get();
        if (template.getPattern() != null) {
            if (!this.dependencies.get(tempNode).isEmpty()) {
                return false;
            }
            log.info("Adding pattern for template " + template.getIri());
            for (Instance i : template.getPattern()) {
                TemplateNode insNode = addTemplateNode(i.getIri());
                addDependency(tempNode, i.getArguments(), i.getListExpander(), insNode);
            }
            tempNode.setType(TemplateNode.Type.TEMPLATE);
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
    public void addInstance(String knowledgeBase, List<Argument> pl, ListExpander expander, String instance) {
        TemplateNode kbNode = addTemplateNode(knowledgeBase);
        TemplateNode tempNode = addTemplateNode(instance);
        addDependency(kbNode, pl, expander, tempNode);
    }

    private void addDependency(TemplateNode fromNode, List<Argument> pl, ListExpander expander, TemplateNode toNode) {
        addDependency(new DependencyEdge(fromNode, pl, expander, toNode));
    }

    private void addDependency(DependencyEdge edge) {
        this.dependencies.get(edge.from).add(edge);
        this.roots.remove(edge.to);
        addInstanceToIndex(edge.to.getIri(), edge.argumentList, edge.from.getIri());
    }

    private void removeDependency(DependencyEdge dependency) {
        this.dependencies.get(dependency.from).remove(dependency);
        String instance = dependency.to.getIri();
        removeInstanceFromIndex(instance, dependency.argumentList, dependency.from.getIri());
        if (!instance.equals(OTTR.BaseURI.Triple) && this.instanceIndex.get(instance).isEmpty()) {
            this.roots.add(dependency.to);
        }
    }

    /**
     * Returns a Result containing the template denoted by the argument
     * IRI if the IRI has a template within this store, otherwise
     * returns empty Result.
     */
    // TODO: should it be "Template" or "Signature", both method name and error message
    public Result<TemplateNode> checkIsTemplate(String iri) {
        TemplateNode node = this.nodes.get(iri);
        if (node == null) {
            return Result.error("Unknown template " + iri + ".");
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
        if (optUnifier.isEmpty()) {
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

        for (DependencyEdge toRemove : new HashSet<>(this.dependencies.get(useNode))) {
            DependencyEdge toRemoveUnifd = new DependencyEdge(changeNode,
                    unifier.apply(toRemove.argumentList), toRemove.listExpander, toRemove.to);
            removeDependency(toRemoveUnifd);
        }
         
        // Add dependency of toUse
        List<Argument> newArgs = useNode.getParameters().stream()
            .map(Parameter::getTerm)
            .map(term -> term.apply(unifier))
            .map(term -> Argument.builder().term(term).build())
            .collect(Collectors.toList());

        addDependency(changeNode, newArgs, null, useNode); // TODO: is it correct to pass null as expander here?
        return true;
    }

    private Map<TemplateNode, Integer> getIndegrees() {
        Map<TemplateNode, Integer> indegrees = new HashMap<>(this.dependencies.keySet().size());
        for (Map.Entry<TemplateNode, Set<DependencyEdge>> es : this.dependencies.entrySet()) {
            for (DependencyEdge e : es.getValue()) {
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

                for (DependencyEdge e : this.dependencies.get(from)) {
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
    private void expandEdges(Set<Result<DependencyEdge>> toExpand, Set<Result<DependencyEdge>> expanded,
            Set<Result<DependencyEdge>> unexpanded, Predicate<DependencyEdge> shouldExpand) {

        for (Result<DependencyEdge> edgeRes : toExpand) {

            // Check that we can and should expand
            Result<DependencyEdge> checkedEdge = edgeRes.flatMap(this::checkForExpansionErrors);
            if (!checkedEdge.isPresent()) {
                unexpanded.add(checkedEdge);
                continue;
            } else if (!checkedEdge.filter(shouldExpand).isPresent()) {
                unexpanded.add(checkedEdge);
                continue; 
            } else if (checkedEdge.filter(DependencyEdge::shouldDiscard).isPresent()) {
                continue;
            }

            // Then expand instance
            DependencyEdge edge = checkedEdge.get();
            if (edge.hasListExpander()) {
                if (edge.canExpandExpander()) {
                    Set<Result<DependencyEdge>> exp = edge.expandListExpander();
                    exp.forEach(e -> e.addToTrace(checkedEdge));
                    expandEdges(exp, expanded, unexpanded, shouldExpand); 
                } else {
                    unexpanded.add(checkedEdge);
                }
            } else if (edge.canExpand()) {
                Set<Result<DependencyEdge>> exp = expandEdgeWithChecks(edge);
                exp.forEach(e -> e.addToTrace(checkedEdge));
                expanded.addAll(exp);
            } else {
                unexpanded.add(checkedEdge);
            }
        }
    }

    private Result<DependencyEdge> checkForExpansionErrors(DependencyEdge edge) {

        Result<DependencyEdge> res = Result.of(edge);

        if (edge.hasListExpander()
            && !edge.canExpandExpander()
            && edge.isInstance()) {

            res = Result.empty(Message.error(
                "List expansion error for instance " + edge.to.getIri() + edge.argumentList + ". "
                + "A blank node is marked for list expansion."), res);
            // TODO: Could the error message be: "Expected list expansion argument to be a list, but found [arg] of type [type].
            // TODO contd: however the canExpandExpander method checks only if the arg is a variable or blank node.
        }

        if (edge.to.isUndefined() || edge.isInstance() && edge.to.isSignature()) {
            res = Result.empty(Message.error(
                "Error expanding instance " + edge.to.getIri() + edge.argumentList
                    + (edge.from != null ? " in the pattern of template " + edge.from.getIri() : "") + ". "
                    + " Unknown signature or template " + edge.to.getIri() + "."), res);
        }

        return res;
    }

    /**
     * Used for expanding instances: Expands edges if template used correctly, and gives
     * empty Result with error message otherwise.
     */
    private Set<Result<DependencyEdge>> expandEdgeWithChecks(DependencyEdge edge) {

        Set<Result<DependencyEdge>> expanded = new HashSet<>();

        Result<Substitution> resSubs = checkAndMakeSubstitution(edge.argumentList, edge.to.getParameters());
        if (!resSubs.isPresent()) {
            expanded.add(Result.empty(resSubs));
            return expanded;
        }
        
        for (DependencyEdge edgeEdge : this.dependencies.get(edge.to)) {
            DependencyEdge newDep = new DependencyEdge(
                edge.from, resSubs.get().apply(edgeEdge.argumentList), edgeEdge.listExpander, edgeEdge.to);
            expanded.add(Result.of(newDep, resSubs));
        }
        return expanded;
    }

    private Result<Substitution> checkAndMakeSubstitution(List<Argument> args, List<Parameter> params) {
        // TODO: Check types
        return Substitution.resultOf(args, params);
    }

    @Override
    public Set<String> getIRIs(Predicate<String> pred) {
        return this.nodes.keySet().stream()
            .filter(pred::test)
            .collect(Collectors.toSet());
    }

    @Override
    public Result<Template> getTemplate(String iri) {
        // TODO BUG? there is no check if the iri is of type template.
        // Will this return signatures and base templates as templates with no pattern?
        return checkIsTemplate(iri).map(this::buildTemplate);
    }


    @Override
    public Result<Signature> getTemplateSignature(String iri) {

        // TODO is this correct: the method is called getSignature, but returns a BaseTemplate in some cases.

        Result<TemplateNode> resTemplate = checkIsTemplate(iri);

        return resTemplate.map(template ->
            template.isBase()
                ? buildBaseTemplate(template)
                : buildSignature(template));
    }

    @Override
    public Result<Signature> getTemplateObject(String iri) {

        Result<TemplateNode> resTemplate = checkIsTemplate(iri);
        return resTemplate.map(template -> {
            if (template.isDefinition()) {
                return buildTemplate(template);
            } else if (template.isBase()) {
                return buildBaseTemplate(template);
            } else {
                return buildSignature(template);
            }
        });
    }


    private Signature buildSignature(TemplateNode templateNode) {
        return Signature.superbuilder()
            .iri(templateNode.getIri())
            .parameters(templateNode.getParameters())
            .build();
    }

    private BaseTemplate buildBaseTemplate(TemplateNode templateNode) {
        return BaseTemplate.builder()
            .iri(templateNode.getIri())
            .parameters(templateNode.getParameters())
            .build();
    }

    private Template buildTemplate(TemplateNode templateNode) {

        var instances = this.dependencies.get(templateNode).stream()
            .map(dependencyEdge -> Instance.builder()
                .iri(dependencyEdge.to.getIri())
                .arguments(dependencyEdge.argumentList)
                .listExpander(dependencyEdge.listExpander)
                .build())
            .collect(Collectors.toList());

        return Template.builder()
            .iri(templateNode.getIri())
            .parameters(templateNode.getParameters())
            .instances(instances)
            .isEmptyPattern(instances.isEmpty())
            .build();
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
            .forEach(dep -> res.add(dep.to.getIri()));
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

    private Set<Result<DependencyEdge>> toResultDependencies(Set<Instance> instances) {
        return instances.stream()
            .map(i -> checkIsTemplate(i.getIri()).map(template ->
                        new DependencyEdge(null, i.getArguments(), i.getListExpander(), template)))
            .collect(Collectors.toSet());
    }

    /**
     * Expands all instances in argument set recursively according to this graph
     * when shouldExpand holds, and adds the resulting leaf-nodes to argument
     * writer.
     */
    private ResultStream<Instance> expandInstances(Set<Instance> instances,
            Predicate<DependencyEdge> shouldExpand, boolean performChecks) {

        Set<Result<DependencyEdge>> finalExpansion = new HashSet<>();
        Set<Result<DependencyEdge>> toExpandRes = toResultDependencies(instances);
        
        // Check arguments (number of arguments and types)
        if (performChecks) {
            toExpandRes = toExpandRes.stream()
                   .map(ins -> ins.flatMap(this::checkArguments))
                   .collect(Collectors.toSet());
        }

        while (!toExpandRes.isEmpty()) {

            Set<Result<DependencyEdge>> expanded = new HashSet<>();
            expandEdges(toExpandRes, expanded, finalExpansion, shouldExpand);
            toExpandRes = expanded;
        }

        return new ResultStream<>(finalExpansion)
            .innerMap(dep -> Instance.builder()
                .iri(dep.to.getIri())
                .arguments(dep.argumentList)
                .listExpander(dep.listExpander)
                .build());
    }

    private Result<DependencyEdge> checkArguments(DependencyEdge ins) {
        
        List<Argument> args = ins.argumentList;
        List<Parameter> params = ins.to.getParameters();

        // TODO: suggestion, if this method finds one or more problems, should we collect all errors to one message?

        // First check correct number of arguments
        if (params == null) {
            return Result.error("Unknown signature or template " + ins.to.getIri() + ".");
        } else if (params.size() != args.size()) {
            return Result.error("Wrong number of arguments. Instance " + ins.to.getIri() + args
                + " has " + args.size() + " arguments, but template expects " + params.size() + ".");
        }

        // Then check types and non-blanks

        Result<DependencyEdge> insRes = Result.of(ins);

        for (int i = 0; i < args.size(); i++) {
            Argument arg = args.get(i);
            Parameter param = params.get(i);

            if (!arg.getTerm().getType().isCompatibleWith(param.getTerm().getType())) {
                String err = "Argument type error. Argument " + arg + " (index " + i + ") "
                    + "in instance " + ins.to.getIri() + args + " has a type " + arg.getTerm().getType()
                    + " which is incompatible with the type of the corresponding parameter " + param + ".";
                insRes = insRes.fail(Message.error(err));
            }
            if (arg.getTerm() instanceof BlankNodeTerm && param.isNonBlank()) {
                String err = "Argument non-blank error. Argument " + arg + " (index " + i + ") "
                    + "in instance" + ins.to.getIri() + args + " is a blank node, but "
                    + " the corresponding parameter " + params.get(i) + " is marked as non-blank.";
                insRes = insRes.fail(Message.error(err));
            }
        }
        return insRes;
    }

    @Override
    public ResultStream<Instance> expandInstance(Instance instance) {
        Set<Instance> instanceSet = new HashSet<>();
        instanceSet.add(instance);
        return expandInstances(instanceSet, e -> true, true);
    }

    @Override
    public ResultStream<Instance> expandInstanceWithoutChecks(Instance instance) {
        Set<Instance> instanceSet = new HashSet<>();
        instanceSet.add(instance);
        return expandInstances(instanceSet, e -> true, false);
    }

    /**
     * Expands all nodes except where argument predicate holds, where arguments to
     * predicate is one of its outgoing edges.
     */
    public Result<DependencyGraph> expandOnly(Predicate<DependencyEdge> shouldExpand) {

        log.info("Expanding definitions.");
        List<TemplateNode> sorted = topologicallySort();

        DependencyGraph ngraph = new DependencyGraph(this.formatManager);
        Result<DependencyGraph> graphRes = Result.of(ngraph);

        for (TemplateNode n : sorted) {

            ngraph.addNode(n);
            if (!isLeafNode(n)) {
                Set<Result<DependencyEdge>> expanded = new HashSet<>(); // Used for both expanded and unexpanded
                ngraph.expandEdges(Result.lift(this.dependencies.get(n)), expanded, expanded, shouldExpand);
                Result<Set<DependencyEdge>> resExpanded = Result.aggregate(expanded);

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

    private MessageHandler checkTemplatesFor(List<Check> checks) {

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(this);
        MessageHandler msgs = new MessageHandler();

        checks.stream()
            .flatMap(c -> c.check(engine))
            .forEach(msgs::add);

        return msgs;
    }

    @Override
    public MessageHandler checkTemplates() {
        return checkTemplatesFor(CheckLibrary.allChecks);
    }

    @Override
    public MessageHandler checkTemplatesForErrorsOnly() {
        return checkTemplatesFor(CheckLibrary.failsOnErrorChecks);
    }

    @Override
    public String toString() {
        
        StringBuilder str = new StringBuilder();

        for (Map.Entry<TemplateNode, Set<DependencyEdge>> ens : this.dependencies.entrySet()) {
            TemplateNode node = ens.getKey();
            str.append(node).append(":\n");
            Map<TemplateNode, Set<List<Argument>>> deps = new HashMap<>();
            for (DependencyEdge e : ens.getValue()) {
                deps.putIfAbsent(e.to, new HashSet<>());
                deps.get(e.to).add(e.argumentList);
            }
            for (Map.Entry<TemplateNode, Set<List<Argument>>> dep : deps.entrySet()) {
                str.append("  ").append(dep.getKey()).append("\n");
                for (List<Argument> args : dep.getValue()) {
                    str.append("    => ").append(args).append("\n");
                }
            }
            str.append("\n\n");
        }
        return str.toString();
    }

    @Override
    public FormatManager getFormatManager() {
        return this.formatManager;
    }

    @Override
    public Optional<TemplateStore> getStandardLibrary() {
        return this.standardLibrary;
    }

    @Override
    public void registerStandardLibrary(TemplateStore standardLibrary) {
        this.standardLibrary = Optional.of(standardLibrary);
        
    }
}
