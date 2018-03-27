package xyz.lutra.tabottr.io.rdf;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.jena.rdf.model.Model;

import xyz.lutra.tabottr.TabOTTR;

public class DataValidator {

	private Model model;

	private static UrlValidator urlValidator = new UrlValidator();

	public DataValidator (Model model) {
		this.model = model;
	}

	public static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	public static boolean isBlank(String value) {
		return isFreshBlank(value) || isNamedBlank(value);
	}

	public static boolean isFreshBlank(String value) {
		return TabOTTR.VALUE_FRESH_BLANK.equals(value);
	}

	public static boolean isNamedBlank(String value) {
		return value.startsWith("_:");
	}

	public boolean isQName(String value) {
		return !value.equals(model.expandPrefix(value));
	}

	public static boolean isURL(String value) {
		return urlValidator.isValid(value);
	}

	public boolean isIRI(String value) {
		return isQName(value) || isURL(value);
	}

	public static boolean isBoolean(String value) {
		return "TRUE".equals(value) 
				|| "true".equals(value)
				|| "FALSE".equals(value) 
				|| "false".equals(value);
	}

	public static boolean isInteger(String value) {
		return isInteger(value, true);
	}

	public static boolean isDecimal(String value) {
		int dot = value.indexOf(".");
		return 
				// there must be a dot:
				dot != -1
				// an integer before the dot:
				&& isInteger(value.substring(0, dot), true) 
				// a positive integer after the dot:
				&& isInteger(value.substring(dot+1), false); 
	}

	private static boolean isInteger(String value, boolean allowNegative) {
		if (isEmpty(value)) {
			return false;
		}
		int length = value.length();
		int i = 0;
		// possibly a minus
		if (allowNegative && value.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		// only digits allowed:
		for (; i < length; i++) {
			char c = value.charAt(i);
			if (!isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isLanguageTag(String value) {
		if (isEmpty(value)) {
			return false;
		}
		// first character must be a letter:
		if (!isAlpha(value.charAt(0))) {
			return false;
		}
		// only alphanumerics or '-' allowed:
		for (int i = 1; i < value.length(); i++) {
			char c = value.charAt(i);
			if (!(isAlphaNumeric(c) || c == '-')) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9'; 
	}
	
	private static boolean isLowercase(char c) {
		return c >= 'a' && c <= 'z';
	}
	
	private static boolean isUppercase(char c) {
		return c >= 'A' && c <= 'Z';
	}
	
	private static boolean isAlpha(char c) {
		return isLowercase(c) || isUppercase(c);
	}
	
	private static boolean isAlphaNumeric(char c) {
		return isDigit(c) || isAlpha(c);
	}

}
