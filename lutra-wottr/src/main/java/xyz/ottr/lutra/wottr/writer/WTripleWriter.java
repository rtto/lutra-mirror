package xyz.ottr.lutra.wottr.writer;

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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Instance;

class WTripleWriter {

    static boolean isTriple(Instance instance) {
        String templateIRI = instance.getIri();
        return OTTR.BaseURI.Triple.equals(templateIRI)
            || OTTR.BaseURI.NullableTriple.equals(templateIRI);
    }

    static Statement write(Model model, Instance instance) {

        var arguments = instance.getArguments();

        RDFNode s = WTermWriter.term(model, arguments.get(0).getTerm());
        RDFNode p = WTermWriter.term(model, arguments.get(1).getTerm());
        RDFNode o = WTermWriter.term(model, arguments.get(2).getTerm());

        // TODO: these checks should be superfluous once instance type checking is in place.
        checkSubject(s, instance);
        checkPredicate(p, instance);

        return model.createStatement(s.asResource(), p.as(Property.class), o);
    }

    private static void checkSubject(RDFNode subject, Instance instance) {
        if (!subject.isResource()) {
            throw new IllegalArgumentException("Error creating a triple of the instance " + instance
                + ". Expected a resource on subject position, but found "
                + (subject.isLiteral() ? "a literal: " + subject.asLiteral() : subject));
        }
    }

    private static void checkPredicate(RDFNode predicate, Instance instance) {

        if (!predicate.canAs(Property.class)) {

            String error;
            if (predicate.isLiteral()) {
                error = "a literal: " + predicate.asLiteral();
            } else if (predicate.isAnon()) {
                error = "a blank node: " + predicate;
            } else {
                error = predicate.toString();
            }

            throw new IllegalArgumentException("Error creating a triple of the instance " + instance
                + ". Expected a property on predicate position, but found " + error);
        }
    }
}
