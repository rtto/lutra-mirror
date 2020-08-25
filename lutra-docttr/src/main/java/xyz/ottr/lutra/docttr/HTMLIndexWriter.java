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
import xyz.ottr.lutra.docttr.visualisation.ModuleDependencyGraphVisualiser;
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

        var iriDepViz = new DependencyGraphVisualiser(this.prefixMapping);
        var modDepViz = new ModuleDependencyGraphVisualiser(this.prefixMapping);

        return document(html(
            HTMLFactory.getHead("OTTR template library " + Objects.toString(root, "")),
            body(
                img().withClass("logo").withSrc("https://ottr.xyz/logo/lOTTR.jpg"),
                iffElse(root != null,
                    h1(code(root)),
                    h1(join("OTTR template library"))
                ),
                h2("Metrics"),
                writeMetrics(root, iris),
                h2("Dependencies"),
                HTMLFactory.getInfoP("Each graph is visualised using different layouts."
                    + "Each node is linked to its documentation page. "
                    + "The colour of the node indicates its namespace. "),
                h3("Modules and packages"),
                HTMLFactory.getInfoP("Dependencies between modules and packages."),
                modDepViz.drawGraph(root, iris.keySet(), this.store),
                h3("Templates"),
                HTMLFactory.getInfoP("Dependencies between templates."),
                iriDepViz.drawGraph(root, iris.keySet(), this.store),
                h2("List of templates"),
                HTMLFactory.getInfoP(join(
                    text("These are the templates in this library"),
                    iff(root != null, join(text(" those IRI starts with "), code(root))),
                    text(", grouped by their namespace."))),
                div(getSignatureList(root, iris))),
                HTMLFactory.getPrefixDiv(this.prefixMapping),
                HTMLFactory.getFooterDiv(),
                HTMLFactory.getScripts()
            ));
    }

    private ContainerTag writeMetrics(String root, Map<String, Result<Signature>> iris) {

        var list = ul();

        list.with(li("Number of templates: " + iris.size()));

        {
            var domains = DocttrManager.getDomains(iris.keySet());
            list.with(li(
                join("Template domains: " + domains.size(),
                    ul(each(domains, iri ->
                        li(
                            a(code(iri))
                                .withHref(Path.of(DocttrManager.toLocalPath(iri, root), DocttrManager.FILENAME_FRONTPAGE).toString())
                        )
                    )))));
        }

        {
            var namespaces = DocttrManager.getNamespaces(iris.keySet());
            list.with(li(
                join("Template namespaces: " + namespaces.size(),
                    ul(each(namespaces, iri ->
                        li(
                            a(code(iri))
                                .withHref(Path.of(DocttrManager.toLocalPath(iri, root), DocttrManager.FILENAME_FRONTPAGE).toString()),
                            HTMLFactory.getColourBoxNS(iri)
                        )
                    )))));
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
                        a(RDFNodeWriter.toString(this.prefixMapping, iri)).withHref(DocttrManager.toLocalFilePath(iri, root)),
                        HTMLFactory.getColourBoxURI(iri)
                    ))))));
        }

        return list;
    }

    /*

    private ContainerTag writeVocabularyUse(String root, Map<String, Result<Signature>> iris) {


        iris.values().stream()
            .filter(Result::isPresent)
            .map(Result::get)
            .filter(signature -> signature instanceof Template)
            .map(template -> ((Template) template).getPattern())
            .collect(Coll)


    }

     */

}
