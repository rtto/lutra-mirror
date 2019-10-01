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

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.TranslationSettings;
import xyz.ottr.lutra.bottr.util.CachedResourceWrapperParser;
import xyz.ottr.lutra.bottr.util.DataParser;
import xyz.ottr.lutra.bottr.util.TermFactory;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;

public class BTranslationSettingsParser extends CachedResourceWrapperParser<TranslationSettings> {

    private final TermFactory termFactory;

    public BTranslationSettingsParser(Resource resource) {
        super(resource);
        this.termFactory = new TermFactory(WOTTR.theInstance, this.model);
    }

    @Override
    protected Result<TranslationSettings> getResult(Resource resource) {

        Result<TranslationSettings.TranslationSettingsBuilder> builder = Result.of(TranslationSettings.builder());

        builder.addResult(getNullValue(), TranslationSettings.TranslationSettingsBuilder::nullValue);
        builder.addResult(getOptionalString(BOTTR.labelledBlankPrefix),
            TranslationSettings.TranslationSettingsBuilder::labelledBlankPrefix);
        builder.addResult(getOptionalString(BOTTR.listSep), TranslationSettings.TranslationSettingsBuilder::listSep);
        builder.addResult(getOptionalChar(BOTTR.listStart), TranslationSettings.TranslationSettingsBuilder::listStart);
        builder.addResult(getOptionalChar(BOTTR.listEnd), TranslationSettings.TranslationSettingsBuilder::listEnd);

        return builder.map(TranslationSettings.TranslationSettingsBuilder::build);
    }

    private Result<Term> getNullValue() {
        return ModelSelector.getOptionalObject(this.model, this.resource, BOTTR.nullValue)
            .flatMap(this.termFactory::createTerm);
    }

    private Result<String> getOptionalString(Property property) {
        return ModelSelector.getOptionalLiteralObject(this.model, this.resource, property)
            .map(Literal::getLexicalForm);
    }

    private Result<Character> getOptionalChar(Property property) {
        return getOptionalString(property)
            .flatMap(DataParser::asChar);
    }
}
