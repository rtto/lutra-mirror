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
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.TranslationTable;
import xyz.ottr.lutra.bottr.util.CachedResourceWrapperParser;
import xyz.ottr.lutra.bottr.util.ResourceWrapperParser;
import xyz.ottr.lutra.bottr.util.TermFactory;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;

public class BTranslationTableParser extends CachedResourceWrapperParser<TranslationTable> {

    protected final TermFactory termFactory;

    public BTranslationTableParser(Resource resource) {
        super(resource);
        this.termFactory = new TermFactory(WOTTR.theInstance, this.model);
    }

    @Override
    protected Result<TranslationTable> getResult(Resource resource) {

        Result<Map<Term, Term>> table = Result.of(new HashMap<>());

        ResultStream.of(ModelSelector.getResourceObjects(this.model, this.resource, BOTTR.entry))
            .innerMap(BTranslationEntryParserResource::new)
            .innerMap(BTranslationEntryParserResource::get)
            .innerForEach(pair -> table.addResult(pair, (t,p) -> t.put(p.getKey(), p.getValue())));

        return table.map(TranslationTable::new);
    }

    class BTranslationEntryParserResource extends ResourceWrapperParser<Map.Entry<Term, Term>> {

        BTranslationEntryParserResource(Resource resource) {
            super(resource);
        }

        @Override
        protected Result<Map.Entry<Term, Term>> getResult(Resource resource) {
            return Result.zip(
                getTermObject(BOTTR.inValue),
                getTermObject(BOTTR.outValue),
                AbstractMap.SimpleImmutableEntry::new);
        }

        private Result<Term> getTermObject(Property property) {
            return ModelSelector.getRequiredObject(this.model, this.resource, property)
                .flatMap(BTranslationTableParser.this.termFactory::createTerm);
        }
    }
}
