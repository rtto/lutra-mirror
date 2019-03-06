package xyz.ottr.lutra;

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

public abstract class Map implements Supplier<ResultStream<Instance>> {

    private final String source; // TODO: Decide how to incorporate DB drivers, etc.
    private final String query;
    private final TermMapping mapping;
    private final String templateIRI;

    @Override
    public ResultStream<Instance> get() {
        return execute().innerFlatMap(row -> mapToInstance(row));
    }

    public Result<Instance> mapToInstance(Row row) {
        return null; // TODO
    }

    public abstract ResultStream<Row> execute();


    private class Row {

        private final List<String> elements;

        public Row(List<String> elements) {
            this.elements = elements;
        }

        public List<String> getElements() {
            return this.elements;
        }
    }
}
