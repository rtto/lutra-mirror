package xyz.ottr.lutra.wottr.legacy;

/*-
 * #%L
 * lutra-wottr
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
import java.util.Set;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.result.Result;

@SuppressWarnings("CPD-START")
public abstract class WTemplateFactory {

    // TODO Possible generalisation a a generic TemplateFactory provided with with a
    // generic TermFactory


    public static Result<Instance> createTripleInstance(Statement stmt) {

        WTermFactory rdfTermFactory = new WTermFactory();
        Result<Term> sub = rdfTermFactory.apply(stmt.getSubject());
        Result<Term> pred = rdfTermFactory.apply(stmt.getPredicate());
        Result<Term> obj = rdfTermFactory.apply(stmt.getObject());

        ArgumentList as = sub.isPresent() && pred.isPresent() && obj.isPresent()
            ? new ArgumentList(sub.get(), pred.get(), obj.get()) : null;
        Result<ArgumentList> asRes = Result.ofNullable(as);
        asRes.addMessages(sub.getMessages());
        asRes.addMessages(pred.getMessages());
        asRes.addMessages(obj.getMessages());

        return asRes.map(asVal -> new Instance(OTTR.Bases.Triple, asVal));
    }

    /**
     * Returns true if the argument is a redundant list-triple, that is,
     * on one of the forms "(:a :b) rdf:first :a" or "(:a :b) rdf:rest (:b)".
     */
    public static boolean isRedundant(Statement s) {
        return s.getSubject().canAs(RDFList.class)
            && (s.getPredicate().equals(RDF.first)
                || s.getPredicate().equals(RDF.rest));
    }

    public static TemplateSignature createTripleTemplateHead() {
        Term sub = new BlankNodeTerm("_:s"); // TODO: fix iri
        sub.setType(TypeFactory.getByName("IRI"));
        Term pred = new BlankNodeTerm("_:p"); // TODO: fix iri
        pred.setType(TypeFactory.getByName("IRI"));
        Term obj = new BlankNodeTerm("_:o"); // TODO: fix iri
        obj.setType(TypeFactory.getVariableType(obj));
        Set<Term> nonBlanks = new HashSet<>();
        nonBlanks.add(pred);
        return new TemplateSignature(
            OTTR.Bases.Triple,
            new ParameterList(new TermList(sub, pred, obj), nonBlanks, null, null),
            true);
    }
}
