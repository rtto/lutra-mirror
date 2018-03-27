package xyz.lutra;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Cache<T> {

	private Map<String, T> cache = new HashMap<>();
	
	private boolean isEnabled; // use the cache only if enabled.
	private Function<String, T> getFunction; // function to get T if not in cache.

	public Cache (boolean isEnabled, Function<String, T> getFunction) {
		this.isEnabled = isEnabled;
		this.getFunction = getFunction;
	}
	
	public Cache (Function<String, T> getFunction) {
		this (true, getFunction);
	}

	public void put (String iri, T model) {
		if (isEnabled) {
			if (cache.containsKey(iri) && !cache.get(iri).equals(model)) {
				// TODO, different exception.
				throw new RuntimeException ("Cache error: key " +iri+ " already exists.");
			}
			cache.put(iri, model);
		}
	}

	public T get (String iri) {
		T model;
		if (isCached(iri)) {
			model = cache.get(iri);
		} else {
			model = getFunction.apply(iri);	
			put(iri, model);
		}
		return model;
	}

	public void clear () {
		cache.clear();
	}

	private boolean isCached (String iri) {
		return cache.containsKey(iri);
	}

}
