package xyz.ottr.lutra.bottr.model;

import java.util.Objects;
import java.util.function.Supplier;

import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;

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

public class InstanceMap implements Supplier<ResultStream<Instance>> {

    private final Source<?> source;
    private final String query;
    private final String templateIRI;
    private final ValueMap valueMap;

    public InstanceMap(Source<?> source, String query, String templateIRI, ValueMap valueMap) {
        this.source = source;
        this.query = query;
        this.templateIRI = templateIRI;
        this.valueMap = valueMap;        
    }
    
    @Override
    public ResultStream<Instance> get() {
        return this.source.execute(this.query)
            .mapFlatMap(this::createInstance);
    }

    private Result<Instance> createInstance(Record<?> record) {
        return Result.zip(
            Result.of(this.templateIRI),
            Result.of(record).flatMap(this.valueMap),
            Instance::new
        );
    }
    
    public String getQuery() {
        return this.query;
    }
        
    public String getTemplateIRI() {
        return this.templateIRI;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " - " + this.templateIRI + " - " + this.query;
    }

    public static class Builder {
        private Source<?> source;
        private String query;
        private String templateIRI;
        private ValueMap valueMap;

        private String nullError(String variableName) {
            return "InstanceMap's " + variableName + " cannot be null.";
        }

        public void setSource(Source<?> source) {
            this.source = Objects.requireNonNull(source, nullError("source"));
        }

        public void setQuery(String query) {
            this.query = Objects.requireNonNull(query, nullError("query"));
        }

        public void setTemplateIRI(String templateIRI) {
            this.templateIRI = Objects.requireNonNull(templateIRI, nullError("template"));
        }

        public void setValueMap(ValueMap valueMap) {
            this.valueMap = Objects.requireNonNull(valueMap, nullError("valueMap"));
        }

        public InstanceMap build() {
            return new InstanceMap(this.source, this.query, this.templateIRI, this.valueMap);
        }
    }

}
