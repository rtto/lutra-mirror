package osl.util.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.modify.request.UpdateDeleteInsert;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public abstract class QueryBuilder {
	
	private static List<Triple> sortTriples (Collection<Triple> triples) {
		List<Triple> sorted = new ArrayList<>(triples);
		Collections.sort(sorted, (t1, t2) -> t1.toString().compareToIgnoreCase(t2.toString()));
		return sorted;
	}

	public static Query getReformattedQuery (Query query) {
		return QueryFactory.create(query.serialize());
	}
	
	public static UpdateRequest createUpdateQuery (
			Collection<Triple> deleteTriples, 
			Collection<Triple> insertTriples, 
			Collection<Triple> whereTriples) {
		
		UpdateDeleteInsert query = new UpdateDeleteInsert();
		
		sortTriples(deleteTriples).forEach(t -> query.getDeleteAcc().addTriple(t));
		sortTriples(insertTriples).forEach(t -> query.getInsertAcc().addTriple(t));
		query.setElement(getElementTriplesBlock(sortTriples(whereTriples)));
		
		return UpdateFactory.create(query.toString());
	}
	
	public static Query createConstructQuery (Collection<Triple> constructTriples, Collection<Triple> whereTriples) {
		Query query = createQuerySkeleton(whereTriples);
		query.setQueryConstructType();
		query.setConstructTemplate(getConstructTemplate(constructTriples));
		return getReformattedQuery(query);
	}

	public static Query createSelectStarQuery (Collection<Triple> whereTriples) {
		Query query = createQuerySkeleton(whereTriples);
		query.setQuerySelectType();
		query.setQueryResultStar(true);
		return getReformattedQuery(query);
	}

	public static Query createSelectQuery (List<String> resultVars, Collection<Triple> whereTriples) {
		Query query = createQuerySkeleton(whereTriples);
		query.setQuerySelectType();
		resultVars.forEach(var -> query.addResultVar(var));
		return getReformattedQuery(query);
	}

	public static Query createAskQuery (Collection<Triple> whereTriples) {
		Query query = createQuerySkeleton(whereTriples);
		query.setQueryAskType();
		return getReformattedQuery(query);
	}

	private static Query createQuerySkeleton (Collection<Triple> whereTriples) {
		Query query = QueryFactory.create();
		query.setQueryPattern(getElementTriplesBlock(whereTriples));
		return query;
	}

	private static Template getConstructTemplate (Collection<Triple> constructTriples) {
		BasicPattern bgp = new BasicPattern();
		sortTriples(constructTriples).forEach(t -> bgp.add(t));
		return new Template(bgp);
	}

	private static ElementTriplesBlock getElementTriplesBlock (Collection<Triple> whereTriples) {
		ElementTriplesBlock etp = new ElementTriplesBlock();
		sortTriples(whereTriples).forEach(t -> etp.addTriple(t));
		return etp;
	}

	
}