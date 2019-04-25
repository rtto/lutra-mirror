package xyz.ottr.lutra.bottr.source;

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

import java.util.List;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.io.WFileReader;

public class RDFSource extends AbstractSPARQLSource {

    private List<String> modelURIs;

    public RDFSource(List<String> modelURIs) {
        this.modelURIs = modelURIs;
    }

    private Result<Model> loadModels() {
        return ResultStream.innerOf(this.modelURIs)
                .innerFlatMap(new WFileReader())
                .aggregate()
                .map(stream -> stream.reduce(
                    ModelFactory.createDefaultModel(), 
                    (m1, m2) -> m1.add(m2)));
    }

    @Override
    protected Result<QueryExecution> getQueryExecution(String query) {
        return loadModels().map(models -> QueryExecutionFactory.create(query, models));
    }

}
