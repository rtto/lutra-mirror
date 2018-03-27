package osl.util.sparql;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osl.util.rdf.ModelIO;
import osl.util.rdf.ModelIOException;

public abstract class QueryExecutor {

	private static Logger log = LoggerFactory.getLogger(QueryExecutor.class);
	
	public static void printQueryModelResult (String sparql, String modelPath) throws ModelIOException {
		log.info("Querying with SPARQL query: " + sparql + "against model.");
		printQueryExecutionResult(QueryExecutionFactory.create(sparql, ModelIO.readModel(modelPath)));
	}

	public static void printQueryModelResult (String sparql, Model model) throws ModelIOException {
		log.info("Querying with SPARQL query: " + sparql + "against model.");
		printQueryExecutionResult(QueryExecutionFactory.create(sparql, model));
	}

	public static void printQueryEndpointResult (String sparql, String endpoint) throws ModelIOException {
		log.info("Querying with SPARQL query: " + sparql + "against endpoint: " + endpoint);
		try (QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, sparql)) {
			printQueryExecutionResult(qexec);
		}
	}

	private static void printQueryExecutionResult (QueryExecution qexec) throws ModelIOException {
		log.info("Printing SPARQL query results");
		Query query = qexec.getQuery();
		if (query.isSelectType()) {
			ResultSet results = qexec.execSelect();
			if (!results.hasNext()) {
				System.out.println("No results.");
			}
			else {
				ResultSetFormatter.out(System.out, results);
			}
		}
		else if (query.isAskType()) {
			boolean result = qexec.execAsk();
			System.out.println(result);
		}
		else if (query.isConstructType()) {
			Model results = qexec.execConstruct();
			ModelIO.printModel(results, ModelIO.format.TURTLE);			
		}
		else if (query.isDescribeType()) {
			Model results = qexec.execDescribe();
			ModelIO.printModel(results, ModelIO.format.TURTLE);
		}
	}
}
