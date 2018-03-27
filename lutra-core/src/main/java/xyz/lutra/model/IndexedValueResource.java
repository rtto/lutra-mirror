package xyz.lutra.model;

import java.util.Comparator;

import org.apache.jena.rdf.model.Resource;

import osl.util.rdf.vocab.Templates;

public abstract class IndexedValueResource<T> extends RDFResource {

	public static final Comparator<IndexedValueResource<?>> indexComparator = (IndexedValueResource<?> p1, IndexedValueResource<?> p2) -> p1.getIndex() - p2.getIndex();
	public static final Comparator<IndexedValueResource<?>> stringValueComparator = (IndexedValueResource<?> p1, IndexedValueResource<?> p2) -> p1.getValue().toString().compareTo(p2.getValue().toString());

	protected int index;
	protected T value;
	private boolean optional;
	
	public IndexedValueResource(Resource iri, int index, T value, boolean optional) {
		super(iri);
		this.index = index;
		this.value = value;
		this.optional = optional;
	}

	public IndexedValueResource(Resource iri, int index, T value) {
		super(iri);
		this.index = index;
		this.value = value;
		this.optional = false;
	}

	public int getIndex() {
		return index;
	}

	public T getValue() {
		return value;
	}

	public boolean isNullValued() {
		return value.equals(Templates.none);
	}
	
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public boolean isOptional () {
		return optional;
	}
}
