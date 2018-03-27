package xyz.lutra.model;

import osl.util.Strings;

public class IllegalSubstitutionException extends RuntimeException {

	private static final long serialVersionUID = 8543686911166481602L;

	public IllegalSubstitutionException (String message) {
		super(message);
	}
	
	public IllegalSubstitutionException (Object... message) {
		this(Strings.toString(message, ""));
	}
	
}
