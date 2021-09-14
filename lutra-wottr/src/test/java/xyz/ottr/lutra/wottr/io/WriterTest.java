package xyz.ottr.lutra.wottr.io;


/*-
 * #%L
 * xyz.ottr.lutra:lutra-wottr
 * %%
 * Copyright (C) 2018 - 2021 University of Oslo
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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.system.Message.Severity;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.wottr.writer.WInstanceWriter;

public class WriterTest {

    private static final String BR = "\n";
    private String resourcePath = "src/test/resources/WriterTests/";
    
    private PrefixMapping createPrefixes() {
        var prefixes = PrefixMapping.Factory.create();
        prefixes.withDefaultMappings(OTTR.getDefaultPrefixes());
        prefixes.setNsPrefix("my", "http://base.org/");
        return prefixes;
    }
    
    private Instance i1 = Instance.builder()
            .iri("http://base.org/T1")
            .arguments(Argument.listOf(
                LiteralTerm.createTypedLiteral("true", XSD.xboolean.getURI()),
                new NoneTerm(),
                new IRITerm("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                new IRITerm("http://some.uri/with#part"),
                LiteralTerm.createLanguageTagLiteral("hello", "no"))
            ).build();

    private Instance i2 = Instance.builder()
        .iri("http://base2.org/T2")
        .arguments(Argument.listOf(
            new BlankNodeTerm("myLabel"),
            LiteralTerm.createPlainLiteral("one"),
            LiteralTerm.createPlainLiteral("two"),
            LiteralTerm.createPlainLiteral("three"))
        ).build();

    private Instance i3 = Instance.builder()
        .iri("http://base.org/T1")
        .arguments(Argument.listOf(
            LiteralTerm.createPlainLiteral("1"),
            LiteralTerm.createPlainLiteral("2"),
            LiteralTerm.createPlainLiteral("3"))
        ).build();
        
    @Test
    public void testWrite() throws IOException {
        var instances = List.of(i1, i2, i3);                                      
        String filePath = this.resourcePath + "instances";
        WInstanceWriter writer = new WInstanceWriter(this.createPrefixes());
        writer.init(filePath, null);
        instances.forEach(writer::accept);
        writer.flush();
        MessageHandler msgs = writer.close();
        if (msgs.getMostSevere().isGreaterThan(Severity.WARNING)) { //fail if file write was not possible
            fail(msgs.getMessages().toString());
        }
        new File(filePath).delete();
    }

}
