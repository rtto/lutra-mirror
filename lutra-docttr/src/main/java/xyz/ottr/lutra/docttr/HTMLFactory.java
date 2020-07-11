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
import j2html.tags.DomContent;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.RDFTurtle;
import xyz.ottr.lutra.Space;

public enum HTMLFactory {
    ;

    private static final DateTimeFormatter dtf = DateTimeFormatter
        .ofPattern("yyyy/MM/dd HH:mm:ss z", Locale.ENGLISH)
        .withZone(ZoneOffset.UTC);

    static ContainerTag getHead(String title) {
        return head(
            meta().withCharset("UTF-8"),
            link().withRel("stylesheet").withHref("https://ottr.xyz/inc/docttr.css"),
            title(title))
            .withLang("en");
    }

    static DomContent getFooterDiv() {
        return div(
            p(text("This is a generated documentation page for an OTTR template library. "
                    + "For more information about Reasonable Ontology Templates (OTTR), visit "),
                a("ottr.xyz")
                    .withHref("http://ottr.xyz")
                    .withTarget("_blank"),
                text(".")),
            p(text("Generated: "), text(dtf.format(ZonedDateTime.now()))))
            .withClass("footer");
    }

    static DomContent getInfoP(String description) {
        return p(rawHtml("&#128712; "), text(description))
            .withClass("info");
    }

    static ContainerTag getPrefixDiv(PrefixMapping prefixMapping) {
        return div(
            h2("Prefixes"),
            getInfoP("Prefixes are removed from all listings on this page for readability, "
                + "but are listed here in RDF Turtle format."),
            pre(prefixMapping.getNsPrefixMap().entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(e -> RDFTurtle.prefixInit
                    + String.format(Locale.ENGLISH, "%-12s", e.getKey() + ":")
                    + RDFTurtle.fullURI(e.getValue()) + ".")
                .collect(Collectors.joining(Space.LINEBR)))
        );
    }

    static ContainerTag getColourBox(String color) {
        return span().withClass("colourbox")
            .withStyle("background-color: " + color + ";");
    }

    static ContainerTag getColourBoxNS(String iri) {
        return getColourBox(NamespaceColours.getColourByNamespace(iri));
    }

    static ContainerTag getColourBoxURI(String iri) {
        return getColourBox(NamespaceColours.getColourByIRI(iri));
    }

}
