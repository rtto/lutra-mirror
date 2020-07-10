package xyz.ottr.lutra.docttr;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-docttr
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import static j2html.TagCreator.*;

import j2html.tags.ContainerTag;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.docttr.visualisation.DependencyGraphVisualiser;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.writer.RDFNodeWriter;

public class HTMLIndexWriter extends HTMLMenuWriter {

    private final TemplateStore store;

    public HTMLIndexWriter(PrefixMapping prefixMapping, TemplateStore store) {
        super(prefixMapping);
        this.store = store;
    }

    public String write(String root, Map<String, Result<Signature>> iris) {

        var graphViz = new DependencyGraphVisualiser(this.prefixMapping);

        return document(html(
            HTMLFactory.getHead("OTTR template library " + Objects.toString(root, "")),
            body(
                img().withClass("logo").withSrc("https://ottr.xyz/logo/lOTTR.jpg"),
                iffElse(root != null,
                    h1(join("Library: ", code(root))),
                    h1(join("OTTR template library "))
                ),
                h2("Metrics"),
                writeMetrics(root, iris),
                h2("Dependency graph"),
                HTMLFactory.getInfoP("Each node is linked to its documentation page."),
                rawHtml(graphViz.drawGraph(root, iris.keySet(), this.store)),
                h2("List of templates"),
                HTMLFactory.getInfoP("These are the templates in this library, grouped by their namespace."),
                div(getSignatureList(root, iris))),
                HTMLFactory.getPrefixDiv(this.prefixMapping),
                HTMLFactory.getFooterDiv()
            ));
    }

    private ContainerTag writeMetrics(String root, Map<String, Result<Signature>> iris) {

        var list = ul();

        list.with(li(
            "Number of templates: " + iris.size()));

        {
            var domains = DocttrManager.getDomains(iris.keySet());
            list.with(li(
                join("Template domains: " + domains.size(),
                    ul(each(domains, iri -> li(
                        a(code(iri)).withTarget("_top")
                            .withHref(Path.of(DocttrManager.toLocalPath(iri, root, 0), DocttrManager.FILENAME_FRAMESET).toString()))
                    )))
            ));
        }

        {
            var namespaces = DocttrManager.getNamespaces(iris.keySet());
            list.with(li(
                join("Template namespaces: " + namespaces.size(),
                    ul(each(namespaces, iri -> li(
                        a(code(iri)).withTarget("_top")
                            .withHref(Path.of(DocttrManager.toLocalPath(iri, root), DocttrManager.FILENAME_FRAMESET).toString()))
                    )))
            ));
        }

        {
            var nonRootTemplates = new HashSet<>();
            iris.keySet().forEach(iri -> this.store.getDependencies(iri).ifPresent(nonRootTemplates::addAll));

            var rootTemplates = iris.keySet().stream()
                .filter(iri -> !nonRootTemplates.contains(iri))
                .sorted()
                .collect(Collectors.toList());

            list.with(li(
                join("Root templates: " + rootTemplates.size(),
                    ul(each(rootTemplates, iri -> li(
                        a(RDFNodeWriter.toString(this.prefixMapping, iri)).withHref(DocttrManager.toLocalFilePath(iri, root))
                    ))))
            ));
        }


        /*
        var leafTemplates = iris.stream()
            .filter(iri -> {
                var deps = store.getDependencies(iri);
                return !deps.isPresent() || deps.get().isEmpty();
            }).collect(Collectors.toList());

        list.with(li(
            join("Number of leaf templates: " + leafTemplates.size(),
                ul(each(leafTemplates, iri -> li(
                    a(code(RDFNodeWriter.toString(this.prefixes, iri))).withHref(toLocalPath(iri))
                ))))
        ));
         */

        return list;
    }

}
