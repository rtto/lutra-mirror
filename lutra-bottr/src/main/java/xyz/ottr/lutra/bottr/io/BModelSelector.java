package xyz.ottr.lutra.bottr.io;

/*-
 * #%L
 * lutra-bottr
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
import org.apache.jena.rdf.model.Resource;

import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.ModelSelectorException;

//TODO: Redesign ModelSelector with methods such as these.
public class BModelSelector {

    public static Result<String> getRequiredStringOfProperty(Model model, Resource subject, Property property) {
        try {
            return Result.of(ModelSelector.getRequiredLiteralOfProperty(model, subject, property).getLexicalForm());
        } catch (ModelSelectorException ex) {
            return Result.empty(Message.error(
                    "Error parsing property " + property.getLocalName() + " of " + subject.getURI() + ": " + ex.getMessage()));
        }
    }

    public static Result<Resource> getRequiredResourceOfProperty(Model model, Resource subject, Property property) {
        try {
            return Result.of(ModelSelector.getRequiredResourceOfProperty(model, subject, property));
        } catch (ModelSelectorException ex) {
            return Result.empty(Message.error("Error parsing property " + property.getLocalName()
            + " of " + subject.getURI() + ": " + ex.getMessage()));
        }
    }

}
