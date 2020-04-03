package xyz.ottr.lutra.bottr.parser;

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

import java.util.AbstractMap;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.TranslationTable;
import xyz.ottr.lutra.bottr.util.CachedResourceWrapperParser;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.parser.ModelSelector;

public class BTranslationTableParser extends CachedResourceWrapperParser<TranslationTable> {

    BTranslationTableParser(Resource resource) {
        super(resource);
    }

    @Override
    protected Result<TranslationTable> getResult(Resource resource) {
        return ResultStream.of(ModelSelector.getResourceObjects(this.model, this.resource, BOTTR.entry))
            .mapFlatMap(r -> Result.zip(getInValue(r), getOutValue(r), AbstractMap.SimpleEntry::new))
            .aggregate()
            .map(stream -> stream.collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)))
            .map(TranslationTable::new);
    }

    private Result<RDFNode> getInValue(Resource entry) {
        return ModelSelector.getRequiredObject(this.model, entry, BOTTR.inValue);
    }

    private Result<RDFNode> getOutValue(Resource entry) {
        return ModelSelector.getRequiredObject(this.model, entry, BOTTR.outValue);
    }

}
