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
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.io.Files;

public class DFramesWriter {

    private static final String framesIndex = "frames/index.html";

    private final PrefixMapping prefixes;

    public DFramesWriter(PrefixMapping prefixes) {
        this.prefixes = prefixes;
    }

    @SneakyThrows(IOException.class)
    public String writeFramesIndex() {
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(framesIndex), StandardCharsets.UTF_8);
    }

    @SneakyThrows(URISyntaxException.class)
    public String writeFramesMenu(Collection<String> signatures) {

        var list = ul();
        var sublist = ul();
        var namespace = "";

        var signatureList = signatures.stream().sorted().collect(Collectors.toList());

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
                    .withHref("./" + Files.iriToPath(iri) + ".html")
                    .withTarget("main-frame")
            );
            sublist.with(item);

        }

        return document(html(
            getHead(),
            body(div(list))));
    }

    public static ContainerTag getHead() {
        return head(
            meta().withCharset("UTF-8"),
            link().withRel("stylesheet").withHref("https://ottr.xyz/inc/style.css"),
            style("div { max-width: 1200px; } p, li, pre { max-width: 100%; } p.info { color: #888; font-size: 9pt; padding: 3px; } ")
        ).withLang("en");
    }

}
