package xyz.ottr.lutra.bottr.model;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.tabottr.io.rdf.RDFNodeFactory;
import xyz.ottr.lutra.wottr.WTermFactory;

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


/**
 * ValueMapList currently only holds just the RDF types that the input data values
 * are to be cast to. I expect ValueMaps to grow to be more complex, possibly computing
 * terms based on multiple input values.
 * 
 * @author martige
 */
public class ValueMap implements Function<Record<?>, Result<ArgumentList>> {

    private final RDFNodeFactory dataFactory;
    private final WTermFactory termFactory;

    private final List<ValueMap.Entry> maps;

    public ValueMap(PrefixMapping prefixes, List<String> types) {
        this.dataFactory = new RDFNodeFactory(prefixes);
        this.termFactory = new WTermFactory();
        this.maps = types.stream()
                .map(ValueMap.Entry::new)
                .collect(Collectors.toList());
    }

    @Override
    public Result<ArgumentList> apply(Record<?> record) {
        List<Result<Term>> args = new LinkedList<>();
        for (int i = 0; i < record.getValues().size(); i += 1) {
            Result<RDFNode> rdfNode = getRDFNode(record.getValue(i), this.maps.get(i).getType());
            args.add(rdfNode.flatMap(termFactory));
        }
        return Result.aggregate(args).map(ArgumentList::new);
    }

    private Result<RDFNode> getRDFNode(Object value, String type) {
        return dataFactory.toRDFNode(getStringValue(value), type);
    }

    // TODO: can we do this better, without instanceof?
    private String getStringValue (Object value) {
        if (value instanceof RDFNode && ((RDFNode) value).isLiteral()) {
            return ((Literal) value).getLexicalForm();
        }
        return value.toString();
    }

    private static class Entry {

        private String type;

        public Entry(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }
    }

}
