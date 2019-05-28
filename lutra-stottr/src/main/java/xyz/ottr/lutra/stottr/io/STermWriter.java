package xyz.ottr.lutra.stottr.io;

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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.LiteralTerm;
import xyz.ottr.lutra.model.NoneTerm;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
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
        return this.prefixes;
    }

    public Set<String> getUsedPrefixes() {
        return this.usedPrefixes;
    }

    public String write(Term term) {

        if (term instanceof NoneTerm) {
            return STOTTR.Terms.none;
        } else if (term instanceof IRITerm) {
            return writeIRI(((IRITerm) term).getIRI());
        } else if (term instanceof LiteralTerm) {
            return writeLiteral((LiteralTerm) term);
        } else if (term instanceof BlankNodeTerm) {
            return writeBlank((BlankNodeTerm) term);
        } else if (term instanceof TermList) {
            return writeList((TermList) term);
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

        String val = "\"" + literal.getPureValue() + "\"";
        if (literal.getDatatype() != null) {
            val += "^^" + writeIRI(literal.getDatatype());
        } else if (literal.getLangTag() != null) {
            val += "@" + literal.getLangTag();
        }
        return val;
    }
    
    public String writeBlank(BlankNodeTerm blank) {
        String label = blank.getLabel();
        String prefix = this.variables.contains(blank) ? STOTTR.Terms.variablePrefix : "_:";
        return  prefix + label;
    }

    public String writeList(TermList list) {

        List<String> terms = list.asList()
            .stream()
            .map(trm -> write(trm))
            .collect(Collectors.toList());

        return STOTTR.Terms.listStart
            + String.join(STOTTR.Terms.listSep, terms)
            + STOTTR.Terms.listEnd;
    }
}
