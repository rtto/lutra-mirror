package osl.util.rdf;

import java.util.Collection;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrefixMappings {

	private static Logger log = LoggerFactory.getLogger(PrefixMappings.class);

	private static final String NS = "ns";
	private static final String defaultNSPrefix = "";

	public static Collection<String> getPrefixes (PrefixMapping mapping) {
		return mapping.getNsPrefixMap().keySet();
	}
	public static Collection<String> getNamespaces (PrefixMapping mapping) {
		return mapping.getNsPrefixMap().values();
	}

	public static String getNamespace (PrefixMapping mapping, String prefix) {
		return mapping.getNsPrefixURI(prefix);
	}

	public static String getPrefix (PrefixMapping mapping, String namespace) {
		return mapping.getNsURIPrefix(namespace);
	}

	public static boolean containsNamespace (PrefixMapping mapping, String namespace) {
		return getPrefix(mapping, namespace) != null; 
	}

	public static boolean containsPrefix (PrefixMapping mapping, String prefix) {
		return getNamespace(mapping, prefix) != null; 
	}

	public static void trim (Model model) {
		Set<String> namespaces = ModelSelector.getNamespaces(model);
		for (String prefixNamespace : model.getNsPrefixMap().values()) {
			if (!namespaces.contains(prefixNamespace)) {
				model.removeNsPrefix(model.getNsURIPrefix(prefixNamespace));
			}
		}
	}

	public static PrefixMapping merge (PrefixMapping... maps) {
		PrefixMapping pmap = PrefixMapping.Factory.create();
		for (PrefixMapping map : maps) {
			replacePrefix(map, defaultNSPrefix);
			addPrefixes(pmap, map);
		}
		return pmap;
	}

	public static void addPrefixes (PrefixMapping target, PrefixMapping source) {
		if (target.samePrefixMappingAs(source)) {
			//no-op
		}
		else if (getPrefixes(target).isEmpty()) {
			target.setNsPrefixes(source);
		}
		// copy only nonexistent
		else {
			for (String ns : getNamespaces(source)) {
				// only add new namespaces
				if (!containsNamespace(target, ns)) { 
					String prefix = getPrefix(source, ns);
					// check that prefix is not already in target, or if default ns
					if (!containsPrefix(target, prefix) && !prefix.equals(defaultNSPrefix)) {
						target.setNsPrefix(prefix, ns);
						log.info("Setting ns: " + prefix + " - " + ns);
					} else {
						addNamespace(target, ns);
					}
				}
			}
		}
	}

	/**
	 * Add a namespace to mapping, giving it some fresh prefix.
	 * @param mapping
	 * @param namespace
	 */
	public static void addNamespace (PrefixMapping mapping, String namespace) {
		if (!containsNamespace(mapping, namespace)) {
			int i = 1;
			// find an unused prefix:
			while (containsPrefix(mapping, NS + i)) {
				i += 1;
			}
			mapping.setNsPrefix(NS + i, namespace);
			log.info("Adding ns: " + NS + i + " - " + namespace);
		}
	}

	private static void replacePrefix (PrefixMapping mapping, String prefix) {
		if (containsPrefix(mapping, prefix)) {
			String namespace = getNamespace(mapping, prefix);
			mapping.removeNsPrefix(prefix);
			addNamespace(mapping, namespace);
		}
	}

}
