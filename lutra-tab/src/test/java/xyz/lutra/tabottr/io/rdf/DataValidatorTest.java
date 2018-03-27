package xyz.lutra.tabottr.io.rdf;

import static org.junit.Assert.assertTrue;

import java.util.function.Predicate;

import org.junit.Test;

import xyz.lutra.tabottr.io.rdf.DataValidator;

public class DataValidatorTest {
	
	private void accept(Predicate<String> func, String value) {
		boolean result = func.test(value);
		if (!result) {
			System.out.println("Error testing value: " + value);
		}
		assertTrue(result);
	}
	
	private void reject(Predicate<String> func, String value) {
		accept(func.negate(), value);
	}

	@Test
	public void shouldAcceptBooleans() {
		for (String value : new String[] { "TRUE", "FALSE", "true", "false" } ) {
			accept(DataValidator::isBoolean, value);
		}
	}
	
	@Test
	public void shouldRejectBooleans() {
		for (String value : new String[] { "True", "yes", "1", "0", "", "asdf" } ) {
			reject(DataValidator::isBoolean, value);
		}
	}
	
	@Test
	public void shouldAcceptIntegers() {
		for (String value : new String[] { "1", "-1234", "00000", "0", "91234" } ) {
			accept(DataValidator::isInteger, value);
		}
	}
	
	@Test
	public void shouldRejectIntegers() {
		for (String value : new String[] { "", "1.1", "asdf", "--123", "1-2", "12-" } ) {
			reject(DataValidator::isInteger, value);
		}
	}
	
	@Test
	public void shouldAcceptDecimals() {
		for (String value : new String[] { "1.0", "-1.1", "0.2", "-0.4", "91234.123" } ) {
			accept(DataValidator::isDecimal, value);
		}
	}
	
	@Test
	public void shouldRejectDecimals() {
		for (String value : new String[] { "", "1.1-", "asdf", "--123", "1-2", "12-", "1" } ) {
			reject(DataValidator::isDecimal, value);
		}
	}
	
	@Test
	public void shouldAcceptLanguageTag() {
		for (String value : new String[] { "no", "en-GB", "asdf", "aa"} ) {
			accept(DataValidator::isLanguageTag, value);
		}
	}
	
	@Test
	public void shouldRejectLanguageTag() {
		for (String value : new String[] { "", ".com", "@", "--123", "1-2", "12-", "1" , "a a", " ", "91234.123"} ) {
			reject(DataValidator::isLanguageTag, value);
		}
	}
}
