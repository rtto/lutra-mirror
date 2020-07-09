package xyz.ottr.lutra.docttr.writer;

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
import java.util.HashSet;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import xyz.ottr.lutra.TemplateManager;
import xyz.ottr.lutra.writer.RDFNodeWriter;

public class DNoFramesIndexWriter extends DFramesMenuWriter {

    public DNoFramesIndexWriter(TemplateManager manager) {
        super(manager);
    }

    @Override
    public String write() {

        var graphViz = new DependencyGraphVisualiser(this.prefixes);

        return document(html(
            getHead("OTTR template library"),
            body(
                img().withClass("logo").withSrc("https://ottr.xyz/logo/lOTTR.jpg"),
                h1("OTTR template library"),
                h2("Metrics"),
                writeMetrics(),
                h2("Dependency graph"),
                getInfoP("Each node is linked to its documentation page."),
                rawHtml(graphViz.drawGraph(this.store)),
                h2("List of templates"),
                getInfoP("These are the templates in this library, grouped by their namespace."),
                div(getSignatureList())),
                getPrefixDiv(this.prefixes)
            ));
    }

    private ContainerTag writeMetrics() {

        var list = ul();

        var templateIRIs = this.store.getTemplateIRIs();

        list.with(li(
            "Number of templates: " + templateIRIs.size()));

        var namespaces = templateIRIs.stream()
            .map(ResourceFactory::createResource)
            .map(Resource::getNameSpace)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        list.with(li(
            join("Template namespaces: " + namespaces.size(),
            ul(each(namespaces, ns -> li(code(ns)))))
        ));


        var nonRootTemplates = new HashSet<>();
        templateIRIs.forEach(iri -> this.store.getDependencies(iri).ifPresent(nonRootTemplates::addAll));

        var rootTemplates = templateIRIs.stream()
            .filter(iri -> !nonRootTemplates.contains(iri))
            .sorted()
            .collect(Collectors.toList());

        list.with(li(
            join("Root templates: " + rootTemplates.size(),
                ul(each(rootTemplates, iri -> li(
                    a(code(RDFNodeWriter.toString(this.prefixes, iri))).withHref(toLocalPath(iri))
                ))))
        ));


        /*
        var leafTemplates = templateIRIs.stream()
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
