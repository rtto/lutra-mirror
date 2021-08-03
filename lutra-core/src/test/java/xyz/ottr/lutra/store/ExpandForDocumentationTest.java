package xyz.ottr.lutra.store;

/*-
 * #%L
 * lutra-core
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.store.expansion.NonCheckingExpander;
import xyz.ottr.lutra.store.graph.StandardTemplateStore;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.ResultConsumer;

public class ExpandForDocumentationTest {

    private void expandAndCheck(Signature toExpand, Set<Signature> templates, int shouldHaveSize) {

        StandardTemplateStore store = new StandardTemplateStore(null);
        templates.forEach(store);

        Set<Instance> expanded = new HashSet<>();
        ResultConsumer<Instance> consumer = new ResultConsumer<>(expanded::add);
        Expander expander = new NonCheckingExpander(store); // TODO check expander type
        expander.expandInstance(toExpand.getExampleInstance())
            .forEach(consumer);

        Assertions.noErrors(consumer);

        assertEquals(expanded.size(), shouldHaveSize);
    }

    @Test
    public void expandSimpleInstance() {

        Signature base = BaseTemplate.builder()
            .iri("base")
            .parameter(Parameter.builder().term(new BlankNodeTerm("x")).nonBlank(true).type(TypeRegistry.LITERAL).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("y")).type(TypeRegistry.IRI).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("z")).nonBlank(true).type(TypeRegistry.IRI).build())
            .build();

        Term var1 = new BlankNodeTerm("x");
        Term var2 = new BlankNodeTerm("y");
        Term var3 = new BlankNodeTerm("z");

        Signature temp = Template.builder()
            .iri("temp")
            .parameter(Parameter.builder().term(var1).nonBlank(true).type(TypeRegistry.LITERAL).build())
            .parameter(Parameter.builder().term(var2).type(TypeRegistry.IRI).build())
            .parameter(Parameter.builder().term(var3).nonBlank(true).type(TypeRegistry.IRI).build())
            .instance(Instance.builder().iri("base")
                .argument(Argument.builder().term(var1).build())
                .argument(Argument.builder().term(var2).build())
                .argument(Argument.builder().term(var3).build())
                .build())
            .build();

        expandAndCheck(temp, Set.of(base, temp), 1);
    }
}
