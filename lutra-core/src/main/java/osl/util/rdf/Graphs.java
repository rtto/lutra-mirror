package osl.util.rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.util.graph.GraphUtils;

public class Graphs {

	public static Graph empty () {
		return Models.empty().getGraph();
	}
	
	public static Model toModel (Graph graph) {
		return ModelFactory.createModelForGraph(graph);
	}
	public static Set<Triple> toTripleSet (Graph graph) {
		return GraphUtil.findAll(graph).toSet();
	}
	public static Graph toGraph (Collection<Triple> triples) {
		Graph fresh = empty();
		triples.forEach(t -> fresh.add(t));
		return fresh;
	}

	public static Set<Node> getNodes (Graph graph) {
		Set<Node> nodes = new HashSet<>();
		GraphUtils.allNodes(graph).forEachRemaining(n -> nodes.add(n));
		return nodes;
	}

	public static Collection<Triple> replaceNodes (Graph graph, Map<Node, Node> map) {
		return replaceNodes(toTripleSet(graph), map);
	}
	
	public static Collection<Triple> replaceNodes (Collection<Triple> triples, Map<Node, Node> map) {
		List<Triple> replaced = new ArrayList<>(triples);
		replaced.replaceAll(t -> replaceNodes(t, map));
		return replaced;
	}

	private static BiFunction<Map<Node,Node>,Node,Node> partialMap = (map, t) -> map.getOrDefault(t, t);
	public static Triple replaceNodes (Triple triple, Map<Node, Node> map) {
		return new Triple(
				partialMap.apply(map, triple.getSubject()),
				partialMap.apply(map, triple.getPredicate()),
				partialMap.apply(map, triple.getObject())
				);
	}

	public static Collection<Triple> replaceBlanksWithVariables (Graph graph) {

		final String varName = "_b";
		int varIndex = 0;

		Map<Node, Node> map = new HashMap<>();
		Set<Node> nodes = getNodes(graph);
		for (Node node : nodes) {
			if (node.isBlank()) {
				if (!map.containsKey(node)) {
					Node var;
					do {
						var = NodeFactory.createVariable(varName + (varIndex += 1));
					} while (nodes.contains(var));
					map.put(node, var);
				}
			}
		}
		//System.out.println("MAP: " + map.toString());
		return replaceNodes(graph, map);
	}

}
