package xyz.ottr.lutra.wottr.vocabulary;

/*-
 * #%L
 * lutra-wottr
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.hamcrest.CoreMatchers.is;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.wottr.WOTTR;

public class WottrVocabTest {

    @Test
    public void testWOTTR04() {

        // read spec RDF file
        syncTest(
            WOTTR.class,
            "src/test/resources/spec/core-vocabulary.owl.ttl",
            WOTTR.ns);

    }

    private void syncTest(Class<?> clazz, String vocabularyURL, String namespaceURL) {

        Model spec = ModelFactory.createDefaultModel();
        spec.read(vocabularyURL);

        // get all IRIs
        Set<Resource> specResources = getResources(spec);

        // Filter to namespace
        if (StringUtils.isNoneBlank(namespaceURL)) {
            specResources.removeIf(r -> !r.getNameSpace().equals(namespaceURL));
        }

        // collect all constants in clazz
        List<Resource> clazzResources = getConstantsOfType(
            clazz, List.of(Resource.class, Property.class), ResourceFactory.createResource());

        // we want all lutraResources to be contained in specResources, i.e. soundness, not completeness
        clazzResources.removeAll(specResources);
        MatcherAssert.assertThat("Some resources are not defined in vocabulary spec. ", clazzResources, is(Collections.emptyList()));
    }

    private Set<Resource> getResources(Model model) {

        Set<Resource> resources = new HashSet<>();

        // OTTR resource collector
        Consumer<RDFNode> addURIResource = node -> {
            if (node.isURIResource()) {
                resources.add(node.asResource());
            }
        };

        model.listStatements()
            .forEachRemaining(s -> {
                addURIResource.accept(s.getSubject());
                addURIResource.accept(s.getPredicate());
                addURIResource.accept(s.getObject());
            });

        return resources;
    }

    private static <R> List<R> getConstantsOfType(Class<?> clazz, Collection<Class<? extends R>> types, R instance) {
        Predicate<Field> filter = f -> true;
        filter = filter.and(f -> Modifier.isStatic(f.getModifiers()));
        filter = filter.and(f -> Modifier.isFinal(f.getModifiers()));
        filter = filter.and(f -> Modifier.isPublic(f.getModifiers()));
        filter = filter.and(f -> types.contains(f.getType()));

        return getConstantsOfType(clazz, filter, instance);
    }

    @SuppressWarnings("unchecked")
    private static <R> List<R> getConstantsOfType(Class<?> clazz, Predicate<Field> filter, R instance) {
        return (List<R>) Arrays.stream(clazz.getDeclaredFields())
            .filter(filter)
            .map(f -> {
                try {
                    return f.get(instance);
                } catch (IllegalArgumentException | IllegalAccessException e) {

                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());
    }
}
