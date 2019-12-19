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

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.bottr.util.ListParser;
import xyz.ottr.lutra.bottr.util.TermFactory;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.ComplexType;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;

// TODO use lombok's @SuperBuilder once it has support in IDE, and make fields final. now we simply use setter methods.
@Setter
public abstract class ArgumentMap<V> implements Function<V, Result<Term>> {

    protected TermType type;
    protected String literalLangTag;

    //private TranslationTable translationTable;
    private TranslationSettings translationSettings;

    protected final TermFactory termFactory;
    //protected final PrefixMapping prefixMapping;

    protected ArgumentMap(PrefixMapping prefixMapping) {
        //this.prefixMapping = prefixMapping;
        this.termFactory = new TermFactory(WOTTR.theInstance, prefixMapping);
        this.translationSettings = TranslationSettings.builder().build();
        //this.translationTable = new TranslationTable();
    }

    protected ArgumentMap(PrefixMapping prefixMapping, TermType type) {
        this(prefixMapping);
        this.type = type;
    }

    public Result<Term> apply(V value) {
        return getTerm(value, this.type);
    }

    protected String toString(V value) {
        return Objects.toString(value);
    }

    private String getBlankNodeLabel(V value) {
        String prefix = this.translationSettings.getLabelledBlankPrefix();
        String stringValue = toString(value);

        return StringUtils.startsWith(stringValue, prefix)
            ? StringUtils.removeStart(stringValue, prefix)
            : StringUtils.EMPTY;
    }

    private Result<Term> getTerm(V value, TermType type) {

        String blankNodeLabel = getBlankNodeLabel(value);

        Result<Term> term;

        if (Objects.isNull(value)) {
            return Result.of(this.translationSettings.getNullValue());
        } else if (StringUtils.isNotEmpty(blankNodeLabel)) {
            term = this.termFactory.createBlankNode(blankNodeLabel).map(t -> (Term)t);
        } else if (type.isListType()) {
            term = getListTerm(toString(value), (ComplexType)type);
        } else {
            term = getBasicTerm(value, (BasicType)type);
        }

        return term;//.flatMap(this.translationTable);
    }

    protected abstract Result<Term> getBasicTerm(V value, BasicType type);

    protected abstract Result<Term> getListElementTerm(String value, BasicType type);

    private Result<Term> getListTerm(String value, ComplexType type) {

        // get list markers:
        char listStart = this.translationSettings.getListStart();
        char listEnd = this.translationSettings.getListEnd();
        String listSep = this.translationSettings.getListSep();

        // parse string to list
        ListParser listParser = new ListParser(listStart, listEnd, listSep);
        List parsedList = listParser.toList(value);

        BasicType basicType = type.getInnermost();

        return getListTerm(parsedList, basicType);
    }

    private Result<Term> getListTerm(List<?> valueList, BasicType type) {

        return ResultStream.innerOf(valueList)
            .mapFlatMap(element -> {
                // element is a string
                return element instanceof List
                    ? getListTerm((List<?>) element, type)
                    : getListElementTerm((String) element, type);
            })
            .aggregate()
            .map(stream -> stream.collect(Collectors.toList()))
            .map(ListTerm::new);
    }
}
