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
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.XSD;
import xyz.ottr.lutra.OTTR;

public class ModelListRenderer {

    private static final String PAV = "http://purl.org/pav/";
    private static final String PROV = "http://www.w3.org/ns/prov#";

    private static final List<String> sortingList = List.of(
        RDFS.label.getURI(),
        DCTerms.title.getURI(),
        OWL.NS + "deprecated",
        OTTR.namespace + "status",
        OWL.versionInfo.getURI(),
        OWL.getURI() + "versionIRI",
        PAV + "previousVersion",
        OWL.getURI() + "priorVersion",

        RDF.type.getURI(),
        RDFS.subClassOf.getURI(),

        DC.description.getURI(),
        RDFS.comment.getURI(),

        SKOS.scopeNote.getURI(),
        SKOS.example.getURI(),
        SKOS.note.getURI(),

        PAV + "createdOn",
        PAV + "lastUpdateOn",
        DC.creator.getURI(),
        DC.contributor.getURI(),

        DC.getURI(),
        DCTerms.getURI(),
        PAV,
        PROV,
        SKOS.getURI(),
        RDFS.uri,
        RDF.uri,
        OWL.getURI(),
        FOAF.getURI()
    );

    private Model model;

    public ModelListRenderer(Model model) {
        this.model = model;
    }

    public ContainerTag drawList(String root) {
        return drawList(ResourceFactory.createResource(root), true);
    }

    private final Predicate<Statement> refiedFilter = statement ->
        statement.getPredicate().equals(RDF.subject)
        || statement.getPredicate().equals(RDF.predicate)
        || statement.getPredicate().equals(RDF.object)
        || statement.getObject().equals(RDF.Statement) && statement.getPredicate().equals(RDF.type);

    private ContainerTag drawList(Resource root, boolean includeInverse) {

        var links = this.model.listStatements(root, null, (RDFNode) null).toList()
            .stream()
            .filter(s -> !refiedFilter.test(s))
            .map(s -> new StatementWrapper(s, false))
            .collect(Collectors.toList());

        if (includeInverse) {
            this.model.listStatements(null, null, root).toList().stream()
                .filter(s -> !refiedFilter.test(s))
                .map(s -> new StatementWrapper(s, true))
                .forEach(links::add);
        }
        return drawList(links);
    }

    private ContainerTag drawList(List<StatementWrapper> statementWrappers) {

        var propertyMap = statementWrappers.stream()
            .collect(Collectors.groupingBy(StatementWrapper::getProperty));

        var propertyList = propertyMap.keySet().stream()
            .sorted(Comparator
                .comparingInt(this::getSortIntValue)
                .thenComparing(Property::getURI));

        var list = dl();

        propertyList.forEach(p -> {
            var forward = propertyMap.get(p).stream().filter(s -> !s.isInverse()).collect(Collectors.toList());
            var backward = propertyMap.get(p).stream().filter(s -> s.isInverse()).collect(Collectors.toList());

            if (!forward.isEmpty()) {
                list.with(dt(print(p)), dd(print(forward)));
            }
            if (!backward.isEmpty()) {
                list.with(dt(text("inverse of "), print(p)), dd(print(backward)));
            }
        });

        return list;
    }

    private ContainerTag print(List<StatementWrapper> values) {

        values.sort(Comparator.comparing(a -> a.getValue().toString()));

        return values.size() == 1
            ? print(values.get(0))
            : ul(each(values, item -> li(print(item))));
    }

    private ContainerTag print(StatementWrapper value) {

        var domStatement = span(print(value.getValue()));

        if (this.model.isReified(value.getStatement())) {
            var reified = this.model.getAnyReifiedStatement(value.getStatement());
            domStatement.with(
                details(
                    summary("Says who?")
                        .withStyle("color: #ccc;"),
                    div(
                        HTMLFactory.getInfoP("Provenance information about this fact:"),
                        drawList(reified, false))
                        .withStyle("border: 1px dotted #ccc; padding: 10px;")
                ));
        }

        return domStatement;
    }

    private DomContent print(RDFNode r) {
        if (r.canAs(RDFList.class)) {
            return print(r.as(RDFList.class));
        } else if (r.isResource()) {
            return print(r.asResource());
        } else {
            return print(r.asLiteral());
        }
    }

    private DomContent print(RDFList rdfList) {
        var list = rdfList.asJavaList();
        return list.size() == 1
            ? print(list.get(0))
            : ul(each(list, item -> li(print(item))));
    }

    private DomContent print(Resource r) {
        return r.isURIResource()
            ? a(this.model.shortForm(r.getURI())).withHref(r.getURI())
            : join(span("[blank node]"),drawList(r, false));
    }

    private DomContent print(Literal r) {

        var content = r.getLexicalForm().trim().replace("\n\n", "<br/><br/>");
        var lang = r.getLanguage();
        var datatype = r.getDatatypeURI();

        if (StringUtils.isNotBlank(lang)) {
            return rawHtml(content + "@@" + lang);
        } else if (StringUtils.isNotBlank(datatype)
            && ! RDF.langString.toString().equals(datatype)
            && ! XSD.xstring.toString().equals(datatype)) {
            return rawHtml(content + "^^" + this.model.shortForm(datatype));
        } else {
            return rawHtml(content);
        }
    }

    private int getSortIntValue(Property key) {

        var uri = key.getURI();
        var ns = key.getNameSpace();

        if (sortingList.contains(uri)) {
            return sortingList.indexOf(uri);
        } else if (sortingList.contains(ns)) {
            return sortingList.indexOf(ns);
        } else {
            return sortingList.size();
        }
    }

    @AllArgsConstructor
    @Getter
    private static class StatementWrapper {

        private Statement statement;
        private boolean inverse;

        public Property getProperty() {
            return statement.getPredicate();
        }

        public RDFNode getValue() {
            return inverse
                ? statement.getSubject()
                : statement.getObject();
        }
    }


}
