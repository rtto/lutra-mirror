package xyz.lutra;

import osl.util.Strings;

public class ExpanderException extends RuntimeException {
	
	private static final long serialVersionUID = -6050773015241215349L;

	public ExpanderException (String message) {
		super(message);
	}
	
	public ExpanderException (Object... message) {
		this(Strings.toString(message, ""));
	}

}
