package xyz.lutra.parser;

import osl.util.Strings;

public class ParserException extends IllegalArgumentException {

	private static final long serialVersionUID = 7219262944876847248L;
	
	public ParserException (String message) {
		super(message);
	}
	
	public ParserException (Object... message) {
		this(Strings.toString(message, ""));
	}

}
