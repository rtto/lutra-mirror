package xyz.ottr.lutra.stottr.writer;

/*-
 * #%L
 * lutra-stottr
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.RDFTurtle;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.stottr.STOTTR;
import xyz.ottr.lutra.wottr.WOTTR;

public class STermWriter {

    private final PrefixMapping prefixes;
    private final Set<String> usedPrefixNS;
    /**
     * The variables map has two functions:
     * 1. Its keySet contains the blank node terms that are used as variables.
     * 2. The mapped value contains the name of the variable, which might be null, in case a variable name is not set.
     */
    private final Map<Term, String> variables;

    STermWriter(PrefixMapping prefixes, Map<Term, String> variables) {
        this.prefixes = prefixes;
        this.variables = variables;
        this.usedPrefixNS = new HashSet<>();
    }

    STermWriter(PrefixMapping prefixes) {
        this(prefixes, new HashMap<>());
    }

    public PrefixMapping getPrefixes() {
        return this.prefixes;
    }

    /**
     * Get the prefixes which this was initialised with, but trimmed to those used until method call.
     */
    PrefixMapping getUsedPrefixes() {
        PrefixMapping used = PrefixMapping.Factory.create();
        this.usedPrefixNS.forEach(ns -> used.setNsPrefix(this.prefixes.getNsURIPrefix(ns), ns));
        used.lock();
        return used;
    }

    public String write(Term term) {

        if (term instanceof NoneTerm) {
            return STOTTR.Terms.none;
        } else if (term instanceof IRITerm) {
            return writeIRI((IRITerm) term);
        } else if (term instanceof LiteralTerm) {
            return writeLiteral((LiteralTerm) term);
        } else if (term instanceof BlankNodeTerm) {
            return writeBlank((BlankNodeTerm) term);
        } else if (term instanceof ListTerm) {
            return writeList((ListTerm) term);
        } else {
            throw new IllegalArgumentException("Unknown term of class " + term.getClass().getName());
        }
    }

    private String writeIRI(IRITerm iri) {
        return writeIRI(iri.getIri());
    }

    String writeIRI(String iri) {

        if (iri.equals(WOTTR.none.getURI())) {
            return STOTTR.Terms.none;
        }

        String qname = this.prefixes.qnameFor(iri);

        if (qname != null) {
            String prefix = qname.split(RDFTurtle.qnameSep)[0];
            this.usedPrefixNS.add(this.prefixes.getNsPrefixURI(prefix));
            return qname;
        }

        return RDFTurtle.fullURI(iri);
    }

    private String writeLiteral(LiteralTerm literal) {

        String val = RDFTurtle.literal(literal.getValue());
        if (literal.getLanguageTag() != null) {
            val += RDFTurtle.literalLangSep + literal.getLanguageTag();
        } else if (!literal.getDatatype().equals(RDFTurtle.plainLiteralDatatype)) {
            val += RDFTurtle.literalTypeSep + writeIRI(literal.getDatatype());
        }
        return val;
    }

    /**
     * A blank node may represent a variable or a regular blank node.
     */
    private String writeBlank(BlankNodeTerm blank) {
        if (this.variables.containsKey(blank)) {
            return STOTTR.Terms.variablePrefix
                + Objects.requireNonNullElse(this.variables.get(blank), blank.getLabel()); // use variable name, if set.
        } else {
            return STOTTR.Terms.blankPrefix + blank.getLabel();
        }
    }

    private String writeList(ListTerm list) {
        return list.asList()
            .stream()
            .map(this::write)
            .collect(Collectors.joining(STOTTR.Terms.listSep, STOTTR.Terms.listStart, STOTTR.Terms.listEnd));
    }
}
