package xyz.ottr.lutra.restapi;

/*-
 * #%L
 * lutra-restapi
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class CLIWrapperTest {

    @Test
    public void pizzatest1() throws IOException {

        String instances = "@prefix o-pizza: <http://tpl.ottr.xyz/pizza/0.1/> .\n"
            + "@prefix ex: <http://example.com/ns#> .\n"
            + "o-pizza:NamedPizza(ex:Grandiosa, ex:Norge, (ex:Tomat, ex:Ost, ex:Paprika, ex:Kjøttdeig)) .";

        String instanceFormat = "stottr";

        String outputFormat = "wottr";

        CLIWrapper cliWrapper = new CLIWrapper();

        cliWrapper.setInput(instances);
        cliWrapper.setInFormat(instanceFormat);
        cliWrapper.setOutFormat(outputFormat);

        String out = cliWrapper.run();

        assertThat(StringUtils.isNoneBlank(out), is(true));

    }

    @Test
    public void pizzatest2() throws IOException {

        // Set template to user-input template
        String instances = "@prefix o-pizza: <http://tpl.ottr.xyz/pizza/0.1-test/> .\n"
            + "@prefix ex: <http://example.com/ns#> .\n"
            + "o-pizza:NamedPizza(ex:Grandiosa, ex:Norge, (ex:Tomat, ex:Ost, ex:Paprika, ex:Kjøttdeig)) .";


        // Verbatim copy of NamedPizza template, except namespace to test user-input of library
        String library = "@prefix o-pizza: <http://tpl.ottr.xyz/pizza/0.1-test/> .\n"
            + "@prefix pav:   <http://purl.org/pav/> .\n"
            + "@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n"
            + "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
            + "@prefix ottr:  <http://ns.ottr.xyz/0.4/> .\n"
            + "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n"
            + "@prefix skos:  <http://www.w3.org/2004/02/skos/core#> .\n"
            + "@prefix pz:    <http://www.co-ode.org/ontologies/pizza/pizza.owl#> .\n"
            + "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n"
            + "@prefix o-owl-ax: <http://tpl.ottr.xyz/owl/axiom/0.1/> .\n"
            + "\n"
            + "o-pizza:PizzaTopping  a  ottr:Template ;\n"
            + "        ottr:parameters  ( [ ottr:type      owl:Class ;\n"
            + "                             ottr:variable  _:b0\n"
            + "                           ]\n"
            + "                           [ ottr:type      ( ottr:NEList xsd:string ) ;\n"
            + "                             ottr:variable  _:b1\n"
            + "                           ]\n"
            + "                           [ ottr:type      xsd:string ;\n"
            + "                             ottr:variable  _:b2\n"
            + "                           ]\n"
            + "                           [ ottr:modifier  ottr:optional ;\n"
            + "                             ottr:type      ( ottr:NEList xsd:string ) ;\n"
            + "                             ottr:variable  _:b3\n"
            + "                           ]\n"
            + "                           [ ottr:modifier  ottr:optional ;\n"
            + "                             ottr:type      owl:Class ;\n"
            + "                             ottr:variable  _:b4\n"
            + "                           ]\n"
            + "                         ) ;\n"
            + "        ottr:pattern     [ ottr:arguments  ( [ ottr:value  _:b0 ]\n"
            + "                                             [ ottr:value  skos:prefLabel ]\n"
            + "                                             [ ottr:value  _:b2 ]\n"
            + "                                           ) ;\n"
            + "                           ottr:of         ottr:Triple\n"
            + "                         ] ;\n"
            + "        ottr:pattern     [ ottr:arguments  ( [ ottr:value  _:b0 ]\n"
            + "                                             [ ottr:value  rdfs:comment ]\n"
            + "                                             [ ottr:modifier  ottr:listExpand ;\n"
            + "                                               ottr:value     _:b3\n"
            + "                                             ]\n"
            + "                                           ) ;\n"
            + "                           ottr:modifier   ottr:cross ;\n"
            + "                           ottr:of         ottr:Triple\n"
            + "                         ] ;\n"
            + "        ottr:pattern     [ ottr:arguments  ( [ ottr:value  _:b0 ]\n"
            + "                                             [ ottr:value  pz:PizzaTopping ]\n"
            + "                                           ) ;\n"
            + "                           ottr:of         o-owl-ax:SubClassOf\n"
            + "                         ] ;\n"
            + "        ottr:pattern     [ ottr:arguments  ( [ ottr:value  _:b0 ]\n"
            + "                                             [ ottr:value  pz:hasSpiciness ]\n"
            + "                                             [ ottr:value  _:b4 ]\n"
            + "                                           ) ;\n"
            + "                           ottr:of         o-owl-ax:SubObjectSomeValuesFrom\n"
            + "                         ] ;\n"
            + "        ottr:pattern     [ ottr:arguments  ( [ ottr:value  _:b0 ]\n"
            + "                                             [ ottr:value  rdfs:label ]\n"
            + "                                             [ ottr:modifier  ottr:listExpand ;\n"
            + "                                               ottr:value     _:b1\n"
            + "                                             ]\n"
            + "                                           ) ;\n"
            + "                           ottr:modifier   ottr:cross ;\n"
            + "                           ottr:of         ottr:Triple\n"
            + "                         ] ;\n"
            + "        pav:version      \"0.1.0\" .";

        CLIWrapper cliWrapper = new CLIWrapper();

        cliWrapper.setInput(instances);
        cliWrapper.setInFormat("stottr");
        cliWrapper.setLibrary(library);
        cliWrapper.setLibFormat("wottr");
        cliWrapper.setOutFormat("wottr");

        String out = cliWrapper.run();

        assertThat(StringUtils.isNoneBlank(out), is(true));
        //System.out.println(out);

    }

}
