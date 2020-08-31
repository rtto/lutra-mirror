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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.system.Result;

public class HTMLMenuWriter {

    protected final PrefixMapping prefixMapping;

    public HTMLMenuWriter(PrefixMapping prefixMapping) {
        this.prefixMapping = prefixMapping;
    }

    public String write(String root, Map<String, Result<Signature>> iris) {
        return document(html(
            HTMLFactory.getHead("OTTR template library frames menu"),
            body(
                a("index")
                    .withHref(DocttrManager.FILENAME_FRONTPAGE)
                    .withTarget(DocttrManager.FRAMENAME_MAIN)
                    .withClass("button")
                    .withStyle("float: right; padding: 5px;"),
                div(
                    h3("Contents"),
                    HTMLFactory.getInfoP("Signatures are organised according to their namespace. "
                        + "Click arrow to expand list. "
                        + "Click text to display page in right window. "
                        + "Items in the list with a colour box represent a namespace. "
                        + "A package is a set of templates constructed for a particular purpose, "
                            + "often as part of a specific project."),
                    getSignatureList(root, iris))).withClass("menulist")
                .withStyle("margin: 20px;"),
            HTMLFactory.getScripts()
        ));
    }

    ContainerTag getSignatureList(String rootPath, Map<String, Result<Signature>> signatures) {

        var nsPackages = DocttrManager.filter(signatures, DocttrManager.NS_TPL_PACKAGE);
        var nsNonPackages = signatures.entrySet().stream()
            .filter(x -> !nsPackages.keySet().contains(x.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return div(getTreeList(rootPath, nsNonPackages),
                iff(!nsPackages.isEmpty(),
                    div(
                        h4("Packages"),
                        getTreeList(rootPath, nsPackages)))
        );
    }

    private ContainerTag getTreeList(String rootPath, Map<String, Result<Signature>> signatures) {

        var indexTreeViewWriter = new StringTreeViewWriter(rootPath, signatures);
        var nsTreeMap = DocttrManager.getNamespaceTrees(signatures.keySet());
        return div(
            each(DocttrManager.getNamespaceTrees(signatures.keySet()).keySet(), path -> indexTreeViewWriter.write(nsTreeMap.get(path))));
    }

    // Inner class for formatting path tree

    private static class StringTreeViewWriter extends TreeViewWriter<String> {
        private final String rootPath;
        private final Map<String, Result<Signature>> signatures;

        StringTreeViewWriter(String rootPath, Map<String, Result<Signature>> signatures) {
            this.rootPath = rootPath;
            this.signatures = signatures;
        }

        @Override
        protected ContainerTag writeRoot(Tree<String> root) {

            // get namespaces for adding colourboxes
            var namespaces = signatures.keySet().stream()
                .map(iri -> ResourceFactory.createResource(iri).getNameSpace())
                .collect(Collectors.toSet());

            var uri = root.getRoot();

            var shortName = uri;
            if (root.getParent() != null) {
                shortName = shortName.replaceFirst(root.getParent().getRoot(), "");
            }

            var container = span().withTitle(uri);
            var link = a(shortName).withTarget(DocttrManager.FRAMENAME_MAIN);

            if (signatures.containsKey(uri)) {
                link.withHref(DocttrManager.toLocalFilePath(uri, rootPath));
                container.with(link)
                    .withCondClass(signatures.get(uri).isEmpty(), "error");
            } else {
                link.withHref(Path.of(DocttrManager.toLocalPath(uri, rootPath), DocttrManager.FILENAME_FRONTPAGE).toString());
                container.with(link, iff(namespaces.contains(uri), HTMLFactory.getColourBoxNS(uri)));
            }
            return container;
        }

        @Override
        protected Collection<Tree<String>> prepareChildren(List<Tree<String>> children) {
            children.sort(Comparator.comparing(a -> a.getRoot()));
            return children;
        }
    }
}
