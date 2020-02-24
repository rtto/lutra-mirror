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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.stottr.STOTTR;
import xyz.ottr.lutra.wottr.WOTTR;

public class STermWriter {

    private final Map<String, String> prefixes;
    private final Set<String> usedPrefixes;
    private final Set<Term> variables;

    public STermWriter(Map<String, String> prefixes, Set<Term> variables) {
        this.prefixes = prefixes;
        this.variables = variables;
        this.usedPrefixes = new HashSet<>();
    }

    public STermWriter(Map<String, String> prefixes) {
        this(prefixes, new HashSet<>());
    }

    public Map<String, String> getPrefixes() {
        return Collections.unmodifiableMap(this.prefixes);
    }

    public Set<String> getUsedPrefixes() {
        return Collections.unmodifiableSet(this.usedPrefixes);
    }

    public String write(Term term) {

        if (term instanceof NoneTerm) {
            return STOTTR.Terms.none;
        } else if (term instanceof IRITerm) {
            return writeIRI(((IRITerm) term).getIri());
        } else if (term instanceof LiteralTerm) {
            return writeLiteral((LiteralTerm) term);
        } else if (term instanceof BlankNodeTerm) {
            return writeBlank((BlankNodeTerm) term);
        } else if (term instanceof ListTerm) {
            return writeList((ListTerm) term);
        } else {
            return null; // TODO: Maybe use Result?
        }
    }

    public String writeIRI(String iri) {

        if (iri.equals(WOTTR.none.getURI())) {
            return STOTTR.Terms.none;
        }

        String out = iri;
        // Shorten to qname if possible
        for (Map.Entry<String, String> nsln : this.prefixes.entrySet()) {
            if (out.startsWith(nsln.getValue())) {
                String suffix = out.substring(nsln.getValue().length());
                out = nsln.getKey() + ":" + suffix;
                this.usedPrefixes.add(nsln.getKey());
                break;
            }
        }
        return out;
    }

    public String writeLiteral(LiteralTerm literal) {

        String val = "\"" + literal.getValue() + "\"";
        if (literal.getDatatype() != null) {
            val += "^^" + writeIRI(literal.getDatatype());
        } else if (literal.getLanguageTag() != null) {
            val += "@" + literal.getLanguageTag();
        }
        return val;
    }
    
    public String writeBlank(BlankNodeTerm blank) {
        String label = blank.getLabel();
        String prefix = this.variables.contains(blank)
            ? STOTTR.Terms.variablePrefix
            : "_:";
        return prefix + label;
    }

    public String writeList(ListTerm list) {
        return list.asList()
            .stream()
            .map(this::write)
            .collect(Collectors.joining(STOTTR.Terms.listSep, STOTTR.Terms.listStart, STOTTR.Terms.listEnd));
    }
}
