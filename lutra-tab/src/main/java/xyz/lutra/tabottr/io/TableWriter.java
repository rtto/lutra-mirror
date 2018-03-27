package xyz.lutra.tabottr.io;

import java.util.List;

import xyz.lutra.tabottr.PrefixInstruction;
import xyz.lutra.tabottr.Table;
import xyz.lutra.tabottr.TemplateInstruction;

public interface TableWriter<Output> {
	
	public void process(List<Table> tables);
	public void process(PrefixInstruction instruction);
	public void process(TemplateInstruction instruction);
	
	public Output write();
}
