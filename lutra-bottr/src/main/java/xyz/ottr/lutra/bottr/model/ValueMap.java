package xyz.ottr.lutra.bottr.model;

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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;

import org.graalvm.compiler.core.common.SuppressFBWarnings;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.tabottr.TabOTTR;
import xyz.ottr.lutra.tabottr.parser.RDFNodeFactory;
import xyz.ottr.lutra.wottr.parser.TermFactory;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;

/**
 * ValueMapList currently only holds just the RDF types that the input data values
 * are to be cast to. I expect ValueMaps to grow to be more complex, possibly computing
 * terms based on multiple input values.
 * 
 * @author martige
 */
public class ValueMap implements Function<Record<?>, Result<ArgumentList>> {

    private final RDFNodeFactory rdfNodeFactory;
    private final TermFactory termFactory;

    private final Map<Integer, Entry> maps;

    public ValueMap(PrefixMapping prefixes, List<String> types) {
        this.rdfNodeFactory = new RDFNodeFactory(prefixes);
        this.termFactory = new TermFactory(WOTTR.theInstance);

        // convert list of types in to indexed map for easy access.
        AtomicInteger index = new AtomicInteger();
        this.maps = types.stream()
            .collect(Collectors.toMap(s -> index.getAndIncrement(), Entry::new));
    }

    public ValueMap(PrefixMapping prefixes) {
        this(prefixes, Collections.emptyList());
    }

    @Override
    public Result<ArgumentList> apply(Record<?> record) {

        List<Result<Term>> args = new LinkedList<>();
        for (int i = 0; i < record.getValues().size(); i += 1) { // need a for-loop as we are accessing records *and* optional types.
            Result<Term> term = getRDFNode(record.getValue(i), Optional.ofNullable(this.maps.get(i)))
                .flatMap(this.termFactory);
            args.add(term);
        }
        return Result.aggregate(args)
            .map(ArgumentList::new);
    }

    @SuppressFBWarnings(
        value = "UPM_UNCALLED_PRIVATE_METHOD",
        justification = "Overloading, method is called in apply(Record)")
    private Result<RDFNode> getRDFNode(RDFNode value, Optional<Entry> entry) {

        if (!entry.isPresent() || TabOTTR.TYPE_AUTO.equals(entry.get().getType())) {
            return Result.of(value);
        } else {
            return getRDFNode((Object)value, entry);
        }
    }

    private Result<RDFNode> getRDFNode(Object value, Optional<Entry> entry) {
        return getRDFNode(value.toString(), entry);
    }

    private Result<RDFNode> getRDFNode(String value, Optional<Entry> entry) {
        String type = entry.map(Entry::getType).orElse(TabOTTR.TYPE_AUTO);
        return this.rdfNodeFactory.toRDFNode(value, type);
    }

    private static class Entry {

        private final String type;

        public Entry(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }
    }

}
