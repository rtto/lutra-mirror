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
import xyz.ottr.lutra.bottr.model.ArgumentMaps;
import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.parser.ModelSelector;

public class BInstanceMapParser implements Function<Model, ResultStream<InstanceMap>> {

    private final String filePath; // @Nullable. file location of the InstanceMap, used for making correct absolute paths to sources

    public BInstanceMapParser(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public ResultStream<InstanceMap> apply(Model model) {
        List<Resource> instanceMaps = ModelSelector.getInstancesOfClass(model, BOTTR.InstanceMap);
        return ResultStream.innerOf(instanceMaps)
            .mapFlatMap(i -> parseInstanceMap(model, i));
    }

    private Result<InstanceMap> parseInstanceMap(Model model, Resource instanceMap) {

        Result<InstanceMap.InstanceMapBuilder> builder = Result.of(InstanceMap.builder());
        builder.addResult(getTemplate(model, instanceMap), InstanceMap.InstanceMapBuilder::templateIRI);
        builder.addResult(getQuery(model, instanceMap), InstanceMap.InstanceMapBuilder::query);

        Result<Source<?>> source = getSource(model, instanceMap);
        builder.addResult(source, InstanceMap.InstanceMapBuilder::source);

        Result<ArgumentMaps> tupleMap = source.flatMap(s -> getArgumentMaps(model, instanceMap, s));
        builder.addResult(tupleMap, InstanceMap.InstanceMapBuilder::argumentMaps);
        return builder.map(InstanceMap.InstanceMapBuilder::build);
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
            .flatMap(new BSourceParser(model, this.filePath));
    }

    private Result<ArgumentMaps> getArgumentMaps(Model model, Resource instanceMap, Source<?> source) {
        return ModelSelector.getOptionalListObject(model, instanceMap, BOTTR.argumentMaps)
            .flatMapOrElse(
                new BArgumentMapsParser(model, source),
                Result.of(new ArgumentMaps(model, source))
            );
    }

}
