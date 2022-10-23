/*-
 * #%L
 * xyz.ottr.lutra:lutra-docttr
 * %%
 * Copyright (C) 2018 - 2022 University of Oslo
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
import static org.junit.Assert.assertNull;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.model.MutableNode;
import org.apache.jena.shared.PrefixMapping;
import org.junit.Before;
import org.junit.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.docttr.visualisation.TripleInstanceGraphVisualiser;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;

public class TripleInstanceGraphVisualiserTest {
    TripleInstanceGraphVisualiser visualiser;

    public static final String CLR_BLANK = "gray90";
    public static final String CLR_LITERAL = "gray90";
    public static final String CLR_LIST = "gray90";
    public static final String CLR_IRI = "lightskyblue";
    public static final String CLR_NONE = "pink";


    @Before
    public void setup() {
        PrefixMapping prefixes = OTTR.getDefaultPrefixes();
        visualiser = new TripleInstanceGraphVisualiser(prefixes);
    }

    @Test
    public void perform_IRITerm() {
        Term iriTerm = new IRITerm("example.org/v1");
        MutableNode mn = visualiser.perform(iriTerm);

        assertEquals("example.org/v1", mn.get("URL"));
        assertEquals("filled", mn.get("style"));
        assertEquals(CLR_IRI, mn.get("fillcolor"));
    }

    @Test
    public void perform_ListTerm() {
        Term listTerm = new ListTerm(new IRITerm("example.org/v1"), LiteralTerm.createPlainLiteral("value"));
        MutableNode mn = visualiser.perform(listTerm);

        assertEquals("<i>rdf:List</i>", ((Label)mn.get("label")).value());
        assertEquals("filled", mn.get("style"));
        assertEquals(2, mn.links().size());
        assertEquals(CLR_LIST, mn.get("fillcolor"));
    }


    @Test
    public void perform_BlankNodeTerm() {
        Term blankNodeTerm = new BlankNodeTerm();
        MutableNode mn = visualiser.perform(blankNodeTerm);
        System.out.println(mn);
        assertEquals("", ((Label) mn.get("label")).value());
        assertEquals("filled", mn.get("style"));
        assertNull(mn.get("URL"));
        assertEquals(CLR_BLANK, mn.get("fillcolor"));
    }

    @Test
    public void perform_LiteralTerm() {
        Term literalTerm = LiteralTerm.createPlainLiteral("value");
        MutableNode mn = visualiser.perform(literalTerm);

        assertEquals("xsd:string" + System.lineSeparator() + "value", mn.get("label"));
        assertEquals("rounded", mn.get("style"));
        assertEquals("Times", mn.get("fontname"));
        assertEquals(CLR_LITERAL, mn.get("fillcolor"));
    }

    @Test
    public void perform_NoneTerm() {
        Term noneTerm = new NoneTerm();
        MutableNode mn = visualiser.perform(noneTerm);

        assertEquals("filled", mn.get("style"));
        assertEquals("ottr:none", ((Label)mn.get("label")).value());
        assertEquals("http://ns.ottr.xyz/0.4/none", mn.get("URL"));
        assertEquals(CLR_NONE, mn.get("fillcolor"));
    }
}
