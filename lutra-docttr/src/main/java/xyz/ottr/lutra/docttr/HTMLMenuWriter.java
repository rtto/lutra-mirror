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
                    getSignatureList(root, iris)))
                .withStyle("margin: 20px;"),
            HTMLFactory.getScripts()
        ));
    }

    ContainerTag getSignatureList(String rootPath, Map<String, Result<Signature>> signatures) {

        // map namespace to signatures in the namespace
        var namespaceMap = signatures.keySet().stream()
            .collect(Collectors.groupingBy(iri -> ResourceFactory.createResource(iri).getNameSpace()));

        // Convert expansion tree to html list
        var indexTreeViewWriter = new StringTreeViewWriter(namespaceMap, rootPath, signatures);

        var nsTreeMap = DocttrManager.getNamespaceTrees(signatures.keySet());

        return div(each(nsTreeMap.keySet(), path -> indexTreeViewWriter.write(nsTreeMap.get(path))));
    }


    private static class StringTreeViewWriter extends TreeViewWriter<String> {
        private final Map<String, List<String>> namespaceMap;
        private final String rootPath;
        private final Map<String, Result<Signature>> signatures;

        StringTreeViewWriter(Map<String, List<String>> namespaceMap, String rootPath, Map<String, Result<Signature>> signatures) {

            this.namespaceMap = namespaceMap;
            this.rootPath = rootPath;
            this.signatures = signatures;
        }

        @Override
        protected ContainerTag writeRoot(Tree<String> root) {
            var uri = root.getRoot();

            var shortName = uri;
            if (root.getParent() != null) {
                shortName = shortName.replaceFirst(root.getParent().getRoot(), "");
            }

            var link = a(shortName).withTarget(DocttrManager.FRAMENAME_MAIN);
            var container = span().withTitle(uri);

            if (namespaceMap.keySet().contains(uri)) {
                link.withHref(Path.of(DocttrManager.toLocalPath(uri, rootPath), DocttrManager.FILENAME_FRONTPAGE).toString());
                container.with(link, HTMLFactory.getColourBoxNS(uri));
            } else if (signatures.containsKey(uri)) {
                link.withHref(DocttrManager.toLocalFilePath(uri, rootPath));
                container.with(link)
                    .withCondClass(signatures.get(uri).isEmpty(), "error");
            } else {
                link.withHref(Path.of(DocttrManager.toLocalPath(uri, rootPath), DocttrManager.FILENAME_FRONTPAGE).toString());
                container.with(link);
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
