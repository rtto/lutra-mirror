package xyz.lutra.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osl.util.rdf.ModelEditor;
import osl.util.rdf.ModelEditorException;
import osl.util.rdf.ModelIO;
import osl.util.rdf.Models;
import osl.util.rdf.RDFLists;
import xyz.lutra.parser.ParserUtils;

@SuppressWarnings("rawtypes")
public class Substitution {
	
	public static Substitution EMPTY = null;

	private static Logger log = LoggerFactory.getLogger(Substitution.class);

	private List<Parameter> parameters;
	private List<? extends IndexedValueResource> arguments;
	private Map<Parameter, IndexedValueResource> map;

	public Substitution (List<Parameter> parameters, List<? extends IndexedValueResource> arguments) throws IllegalSubstitutionException {
		map = new HashMap<>();
		this.parameters = parameters;
		this.arguments = arguments;
		build();
	}

	public Substitution (TemplateInstance instance) throws IllegalSubstitutionException {
		this(instance.getTemplate().getParameters(), instance.getArguments());
	}

	
	private void build () throws IllegalSubstitutionException {
		String error = "Error building substitution "
				+ " with parameters " + parameters.toString() 
				+ " and arguments " + arguments.toString() + ". ";

		if (parameters.size() != arguments.size()) {
			throw new IllegalSubstitutionException (error 
					+ "Number of parameters (" + parameters.size() 
					+ ") does not match number of arguments (" + arguments.size() + ")");
		}

		for (IndexedValueResource a : arguments) {
			Parameter p;

			// check if parameter and argument match
			{try {
				// TODO this is caught with the test above.
				p = getParameter(a.getIndex());
			} catch (NoSuchParameterException ex) {
				throw new IllegalSubstitutionException (error + ex.getMessage());
			}}
			
			// type check
			{ if (a.getClass() == Argument.class && !a.isNullValued()) { // don't need to check types if argument is a variable
				try {
					ParserUtils.checkParameterType(p.getType(), (RDFNode)a.getValue());
				} catch (IllegalArgumentException ex) {
					throw new IllegalSubstitutionException (error
							+ " Error for parameter " + p.toString()
							+ " and argument " + a.toString() + ". " + 
							ex.getMessage());
				}
			}}

            map.put(p, a);
		}
	}

	private Parameter getParameter (int index) throws NoSuchParameterException {
		if (index >= 0 && index-1 < parameters.size()) {
			return parameters.get(index-1);
		} else {
			throw new NoSuchParameterException ("Found no parameter with index: " + index);
		}
	}

	public static void apply (Substitution subst, Model model) {
		if (subst != EMPTY) {
			subst.apply(model);
		}
	}
	public static Model copy (Substitution subst, Model model, Models.BlankCopy blankCopy) {
		if (subst != EMPTY) {
			model = subst.duplicate(model, blankCopy);
		}
		return model;
	}

	public Model duplicate (Model model, Models.BlankCopy blankCopy) {
		Model copy = Models.duplicate(model, blankCopy);
		apply(copy);
		return copy;
	}

	public void apply (Model model) {
		log.info("Applying substitution to model: " + model.hashCode());
		//log.info("Model " +model.hashCode()+ " before substitution: " + ModelIO.writeModel(model, ModelIO.format.TURTLE));

		for (Entry<Parameter, ? extends IndexedValueResource> m : map.entrySet()) {
			try {

				RDFNode variable = m.getKey().getValue();
				IndexedValueResource argument = m.getValue();

				boolean isVariable = argument.getClass() == VariableArgument.class;

				Node argNodeValue = isVariable ? (Node) argument.getValue() : ((RDFNode) argument.getValue()).asNode(); 

				log.info("Model " +model.hashCode()+ ": Substituting variable " 
						+ModelIO.shortForm(model, variable)+ " for value " 
						+ModelIO.shortForm(model, argNodeValue));

				if (variable.canAs(RDFList.class)) {
					RDFList varList = variable.as(RDFList.class);
					if (isVariable) { 
						RDFLists.substituteNonEmptyRDFList(model, varList, argNodeValue);
					} else if (!isVariable && ((RDFNode)argument.getValue()).canAs(RDFList.class)) {
						RDFLists.substituteNonEmptyRDFList(model, varList, ((RDFNode)argument.getValue()).as(RDFList.class));
					} else {
						throw new IllegalSubstitutionException(
								"Error applying substitution to model. Cannot substitute list variable: " 
										+ varList.asJavaList().toString() 
										+ " with value " + argument.getValue());
					}
				}
				else {
					ModelEditor.substituteNode(model, variable.asNode(), argNodeValue);
				}
			} catch (ModelEditorException e) {
				throw new IllegalSubstitutionException(
						"Error applying substitution to model. " + e.getMessage());
			}
		}
	}
}
