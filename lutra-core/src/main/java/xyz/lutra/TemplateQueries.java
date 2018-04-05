package xyz.lutra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.update.UpdateRequest;

import osl.util.rdf.Graphs;
import osl.util.rdf.ModelEditor;
import osl.util.rdf.Models;
import osl.util.rdf.PrefixMappings;
import osl.util.sparql.QueryBuilder;
import xyz.lutra.model.Parameter;
import xyz.lutra.model.Substitution;
import xyz.lutra.model.Template;
import xyz.lutra.parser.TemplateLoader;

public abstract class TemplateQueries {
	
	public static final String PARAM_PREFIX = "param";
	public static final String LISTITEM_POSTFIX = "item";
	
	private static final Path RDFListContentPath = PathParser.parse("rdf:rest*/rdf:first", PrefixMapping.Standard);
	
	private static Cache<Model> bodyCache = new Cache<>(
			Settings.enableTemplateQueriesCache,
			iri -> getBodyModel(TemplateLoader.getTemplate(iri), Substitution.EMPTY));

	private static Cache<Model> headCache = new Cache<>(
			Settings.enableTemplateQueriesCache,
			iri -> getHeadModel(TemplateLoader.getTemplate(iri), Substitution.EMPTY));

	
	///////////////////////////////////////////
	// Get queries from template IRI
	// lifting:  head -> body
	// lowering: body -> head
	
	public static UpdateRequest getLiftingUpdateQuery (String iri) {		
		return getUpdateRequest(headCache.get(iri), bodyCache.get(iri));
	}
	public static UpdateRequest getLoweringUpdateQuery (String iri) {		
		return getUpdateRequest(bodyCache.get(iri), headCache.get(iri));
	}
	public static Query getLiftingConstructQuery (String iri) {		
		return getConstructQuery(bodyCache.get(iri), headCache.get(iri));
	}
	public static Query getLoweringConstructQuery (String iri) {		
		return getConstructQuery(headCache.get(iri),bodyCache.get(iri));
	}
	public static Query getBodySelectQuery (String iri) {
		return getBodySelectQuery(TemplateLoader.getTemplate(iri), Substitution.EMPTY);
	}
	public static Query getHeadSelectQuery (String iri) {		
		return getSelectQuery(headCache.get(iri));
	}
	
	///////////////////////////////////////////
	// Get queries from template object + substitution
	// Cannot do caching as expansion is tail-recursive, i.e, must substitute first.
	
	public static UpdateRequest getLiftingUpdateQuery (Template template, Substitution subst) {		
		return getUpdateRequest(getHeadModel(template, subst),getBodyModel(template, subst));
	}
	public static UpdateRequest getLoweringUpdateQuery (Template template, Substitution subst) {		
		return getUpdateRequest(getBodyModel(template, subst),getHeadModel(template, subst));
	}
	public static Query getLiftingConstructQuery (Template template, Substitution subst) {		
		return getConstructQuery(getHeadModel(template, subst),getBodyModel(template, subst));
	}
	public static Query getLoweringConstructQuery (Template template, Substitution subst) {		
		return getConstructQuery(getBodyModel(template, subst),getHeadModel(template, subst));
	}
	public static Query getBodySelectQuery (Template template, Substitution subst) {
		// TODO: use WhereHandler in java 3.7.x?
		Query query = QueryFactory.create();
		Model body = getBodyModel(template, subst);
		List<TriplePath> itemPaths = new ArrayList<>();
		// for list item
		for (Parameter p : template.getParameters()) {
			if (p.getValue().canAs(RDFList.class)) {
				Node var = NodeFactory.createVariable(PARAM_PREFIX + p.getIndex());
				Node varList = NodeFactory.createVariable(PARAM_PREFIX + p.getIndex() + LISTITEM_POSTFIX);
				TriplePath tp = new TriplePath(var, RDFListContentPath, varList);
				itemPaths.add(tp);
				
				// replace mode expanded instances with listitem
				for (RDFNode item : p.getValue().as(RDFList.class).asJavaList()) {
					ModelEditor.substituteNode(body, item.asNode(), varList);
				}
			}
		}
		
		ElementPathBlock etp = new ElementPathBlock();
		Set<Triple> whereTriples = Models.toTripleSet(body);
		// add triples
		QueryBuilder.sortTriples(whereTriples).forEach(t -> etp.addTriple(t));
		// add list item paths
		itemPaths.forEach(t -> etp.addTriplePath(t));
		
		ElementGroup group = new ElementGroup();
		group.addElement(etp);
		//optionalElements.forEach(o -> group.addElement(o));
		
		query.setPrefixMapping(body);
		query.setQueryPattern(group);
		query.setQuerySelectType();
		query.setQueryResultStar(true);
		return QueryBuilder.getReformattedQuery(query);
	}
	public static Query getHeadSelectQuery (Template template, Substitution subst) {		
		return getSelectQuery(getHeadModel(template, subst));
	}
	
	
	////////////////////////////////////////////////
	// template to body and head model
	
	private static Model getHeadModel (Template template, Substitution paramSubst) {
		return _getTemplateModel(template, t -> t.getInstance(), paramSubst);  
	}
	
	private static Model getBodyModel (Template template, Substitution paramSubst) {
		return _getTemplateModel(template, t -> Expander.expand(t.getBody()), paramSubst); 
	}
	
	private static Model _getTemplateModel (Template template, Function<Template, Model> func, Substitution paramSubst) {
		Model model = Substitution.copy(paramSubst, func.apply(template), Models.BlankCopy.KEEP);
		Substitution.apply(template.getVariableSubstitution(), model);
		model.setNsPrefixes(template.getSourceModel());
		return model; 
	}

	
	//////////////////////////////////////////
	// Models to queries

	private static UpdateRequest getUpdateRequest (Model source, Model target) {
		Collection<Triple> sourceTriples = Graphs.replaceBlanksWithVariables(source.getGraph());	
		UpdateRequest update = QueryBuilder.createUpdateQuery(
				sourceTriples, // delete
				Models.toTripleSet(target), // insert
				sourceTriples  // where
				);
		update.setPrefixMapping(PrefixMappings.merge(source, target));
		return update;
	}

	private static Query getConstructQuery (Model source, Model target) {
		Query query = QueryBuilder.createConstructQuery(
				Models.toTripleSet(target),
				Models.toTripleSet(source));
		query.setPrefixMapping(PrefixMappings.merge(target, source));
		return QueryBuilder.getReformattedQuery(query);
	}

	private static Query getSelectQuery (Model source) {
		Query query = QueryBuilder.createSelectStarQuery(
				Models.toTripleSet(source));
		query.setPrefixMapping(PrefixMappings.merge(source));
		return QueryBuilder.getReformattedQuery(query);
	}
}
