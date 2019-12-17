package xyz.ottr.lutra.bottr.parser;

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

import static org.hamcrest.CoreMatchers.is;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.junit.Assert;
import org.junit.Test;

public class TermFactoryTest {

    PrefixMap prefixes = PrefixMapFactory.create(PrefixMapping.Standard);

    @Test
    public void testURIs() {
        isURI("rdf:type");
        isURI("<http://example.com>");
        isURI("<http://example.com/asdf/asdf>");
        isURI("<http://example.com#asdf>");
    }

    @Test
    public void testLiterals() {
        //isLiteral("asdf");
        isLiteral("\"asdf\"^^xsd:string");
        isLiteral("\"1\"^^xsd:integer");
        isLiteral("\"asdf\"@en");
        isLiteral("\"asdf\"");
    }

    @Test
    public void testBlanks() {
        isBlank("_:asdf");
        isBlank("_:b2");
    }

    private void isURI(String value) {
        Node node = getNode(value);
        Assert.assertThat(node.isURI(), is(true));
        Assert.assertThat(node.isLiteral(), is(false));
        Assert.assertThat(node.isBlank(), is(false));
        Assert.assertThat(node.isVariable(), is(false));
    }

    private void isLiteral(String value) {
        Node node = getNode(value);
        Assert.assertThat(node.isURI(), is(false));
        Assert.assertThat(node.isLiteral(), is(true));
        Assert.assertThat(node.isBlank(), is(false));
        Assert.assertThat(node.isVariable(), is(false));
    }

    private void isBlank(String value) {
        Node node = getNode(value);
        Assert.assertThat(node.isURI(), is(false));
        Assert.assertThat(node.isLiteral(), is(false));
        Assert.assertThat(node.isBlank(), is(true));
        Assert.assertThat(node.isVariable(), is(false));
    }

    private Node getNode(String value) {
        return NodeFactoryExtra.parseNode(value, this.prefixes);
    }



}
