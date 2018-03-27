package xyz.lutra.tabottr.io;

import java.io.File;

public abstract class AbstractFileTableReader implements TableReader {
	
	protected File file;
		
	private AbstractFileTableReader(File file) {
		this.file = file;
	}
	public AbstractFileTableReader(String filename) {
		this(new File(filename));
	}
	
}
