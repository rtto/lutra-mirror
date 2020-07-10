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
                    .withTarget("main-frame")
                    .withClass("button")
                    .withStyle("float: right; padding: 5px;"),
                div(getSignatureList(root, iris)))));
    }

    ContainerTag getSignatureList(String root, Map<String, Result<Signature>> signatures) {

        var list = ul();
        var sublist = ul();
        var namespace = "";

        var keys = signatures.keySet().stream()
            .sorted()
            .collect(Collectors.toList());

        for (String iri : keys) {

            // create heading and new sublist if new namespace
            var ns = ResourceFactory.createResource(iri).getNameSpace();
            if (!ns.equals(namespace)) {
                namespace = ns;
                sublist = ul();
                list.with(li(b(ns), sublist));
            }

            var item = li(
                a(this.prefixMapping.shortForm(iri))
                    .withHref(DocttrManager.toLocalFilePath(iri, root))
                    .withTarget("main-frame") // TODO make this variable
            ).withCondClass(signatures.get(iri).isEmpty(), "error"); // mark as error if Result is empty
            sublist.with(item);
        }
        return list;
    }


}
