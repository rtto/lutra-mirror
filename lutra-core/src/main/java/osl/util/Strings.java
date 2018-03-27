package osl.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Strings {

	public static <E> String toString (Collection<E> objects, Function<E, Object> toString, String glue) {
		return objects.stream()
				.map(object -> toString.apply(object).toString())
				.collect(Collectors.joining(glue));
	}

	public static String toString(Object[] message, String glue) {
		return toString(Arrays.asList(message), s -> s.toString(), glue);
	}
}
