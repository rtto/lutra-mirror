package xyz.ottr.lutra.bottr.parser;

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

import java.util.List;
import java.util.function.Function;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.bottr.model.ValueMap;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.util.ModelSelector;

public class BInstanceMapParser implements Function<Model, ResultStream<InstanceMap>> {

    @Override
    public ResultStream<InstanceMap> apply(Model model) {
        List<Resource> instanceMaps = ModelSelector.getInstancesOfClass(model, BOTTR.InstanceMap);
        return parseInstanceMaps(model, instanceMaps);
    }

    private ResultStream<InstanceMap> parseInstanceMaps(Model model, List<Resource> instancesMaps) {
        return ResultStream.innerOf(instancesMaps)
            .mapFlatMap(i -> parseInstanceMap(model, i));
    }

    private Result<InstanceMap> parseInstanceMap(Model model, Resource instanceMap) {

        Result<InstanceMap.Builder> builder = Result.of(new InstanceMap.Builder());
        builder.addResult(getTemplate(model, instanceMap), InstanceMap.Builder::setTemplateIRI);
        builder.addResult(getQuery(model, instanceMap), InstanceMap.Builder::setQuery);
        builder.addResult(getSource(model, instanceMap), InstanceMap.Builder::setSource);
        builder.addResult(getValueMap(model, instanceMap), InstanceMap.Builder::setValueMap);
        return builder.map(InstanceMap.Builder::build);
    }

    private Result<String> getTemplate(Model model, Resource instanceMap) {
        return ModelSelector.getRequiredURIResourceObject(model, instanceMap, BOTTR.template)
            .map(Resource::getURI);
    }

    private Result<String> getQuery(Model model, Resource instanceMap) {
        return ModelSelector.getRequiredLiteralObject(model, instanceMap, BOTTR.query)
            .map(Literal::getLexicalForm);
    }

    private Result<Source<?>> getSource(Model model, Resource instanceMap) {
        return ModelSelector.getRequiredResourceObject(model, instanceMap, BOTTR.source)
            .flatMap(new BSourceParser(model));
    }

    private Result<ValueMap> getValueMap(Model model, Resource instanceMap) {
        return ModelSelector.getRequiredListObject(model, instanceMap, BOTTR.valueMap)
            .flatMap(new BValueMapParser(model));
    }

}
