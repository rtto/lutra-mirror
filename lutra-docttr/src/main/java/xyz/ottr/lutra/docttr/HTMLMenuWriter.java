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
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.ResourceFactory;
import xyz.ottr.lutra.TemplateManager;

public class HTMLMenuWriter extends HTMLDocWriter {

    public HTMLMenuWriter(TemplateManager manager) {
        super(manager);
    }

    public String write() {
        return document(html(
            getHead("OTTR template library frames menu"),
            body(
                a("index")
                    .withHref("./index-noframes.html") // TODO make this variable
                    .withTarget("main-frame")
                    .withStyle("float: right; padding: 5px;"),
                div(getSignatureList()))));
    }

    protected ContainerTag getSignatureList() {

        var list = ul();
        var sublist = ul();
        var namespace = "";

        var signatureList = this.manager.getTemplateStore().getAllTemplateObjectIRIs()
            .stream().sorted().collect(Collectors.toList());

        for (String iri : signatureList) {

            // create heading and new sublist if new namespace
            var ns = ResourceFactory.createResource(iri).getNameSpace();
            if (!ns.equals(namespace)) {
                namespace = ns;
                sublist = ul();
                list.with(li(b(ns), sublist));
            }

            var item = li(
                a(this.prefixes.shortForm(iri))
                    .withHref(toLocalPath(iri))
                    .withTarget("main-frame") // TODO make this variable
            );
            sublist.with(item);
        }
        return list;
    }


}
