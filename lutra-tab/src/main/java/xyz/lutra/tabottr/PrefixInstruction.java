package xyz.lutra.tabottr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefixInstruction extends Instruction {

	private int[] colIndices = { 0, 1 };
	
	public PrefixInstruction(Table table, int rowStart, int rowEnd) {
		super(table, rowStart, rowEnd);
	}

	public Map<String, String> getPrefixes() {
		Map<String, String> map = new HashMap<>();
		
		for (List<String> pair : super.getRows(this.rowStart+1, this.rowEnd, colIndices)) {
			String prefix = pair.get(0);
			String ns = pair.get(1);
			
			if (map.containsKey(prefix)) {
				throw new IllegalArgumentException("Prefix " + prefix + " already set: " + map.get(prefix));
			} 
			map.put(prefix, ns);
		}
		return Collections.unmodifiableMap(map);
	}
}
