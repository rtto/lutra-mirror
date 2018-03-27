package xyz.lutra.model;

import osl.util.Strings;

public class NoSuchParameterException extends IndexOutOfBoundsException {
	
	private static final long serialVersionUID = 1121710096403153444L;

	public NoSuchParameterException (String message) {
		super(message);
	}
	
	public NoSuchParameterException (Object... message) {
		this(Strings.toString(message, ""));
	}
}
