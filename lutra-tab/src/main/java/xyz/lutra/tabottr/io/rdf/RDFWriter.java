package xyz.lutra.tabottr.io.rdf;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;

import osl.util.rdf.Models;
import osl.util.rdf.vocab.Templates;
import xyz.lutra.tabottr.Instruction;
import xyz.lutra.tabottr.PrefixInstruction;
import xyz.lutra.tabottr.Table;
import xyz.lutra.tabottr.TemplateInstruction;
import xyz.lutra.tabottr.io.TableWriter;

public class RDFWriter implements TableWriter<Model> {

	private Model model;

	public RDFWriter() {
		model = Models.empty();
		model.setNsPrefixes(PrefixMapping.Standard);
		model.setNsPrefix("ottr", Templates.namespace);
	}

	@Override
	public void process(List<Table> tables) {

		// collect all instructions
		List<Instruction> instructions = tables.stream()
				.map(Table::getInstructions)
				.flatMap(List::stream)
				.collect(Collectors.toList());

		// process all prefixes first
		instructions.stream()
			.filter(i -> i instanceof PrefixInstruction)
			.forEach(i -> this.process((PrefixInstruction)i));

		// process template instances
		instructions.stream()
			.filter(i -> i instanceof TemplateInstruction)
			.forEach(i -> this.process((TemplateInstruction)i));
	}

	@Override
	public void process(PrefixInstruction instruction) {
		for (Entry<String, String> p : instruction.getPrefixes().entrySet()) {
			model.setNsPrefix(p.getKey(), p.getValue());
		}
	}

	@Override
	public void process(TemplateInstruction instruction) {
		TemplateInstanceFactory builder = new TemplateInstanceFactory(
				model,
				instruction.getTemplateIRI(), 
				instruction.getArgumentTypes());
		
		for (List<String> arguments : instruction.getTemplateInstances()) {
			builder.addTemplateInstance(arguments);
		}
	}

	@Override
	public Model write() {
		return model;
	}
}
