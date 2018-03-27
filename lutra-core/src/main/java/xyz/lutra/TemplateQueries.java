package xyz.lutra;

import java.util.Collection;
import java.util.function.Function;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateRequest;

import osl.util.rdf.Graphs;
import osl.util.rdf.Models;
import osl.util.rdf.PrefixMappings;
import osl.util.sparql.QueryBuilder;
import xyz.lutra.model.Substitution;
import xyz.lutra.model.Template;
import xyz.lutra.parser.TemplateLoader;

public abstract class TemplateQueries {
	
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
		return getSelectQuery(bodyCache.get(iri));
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
		return getSelectQuery(getBodyModel(template, subst));
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
