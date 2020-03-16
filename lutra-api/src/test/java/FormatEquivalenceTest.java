/*-
 * #%L
 * lutra-api
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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import xyz.ottr.lutra.api.StandardFormat;
import xyz.ottr.lutra.api.StandardTemplateManager;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.system.Result;

@RunWith(Parameterized.class)
public class FormatEquivalenceTest {

    private Format format;
    private Signature signature;
    private static StandardTemplateManager manager;

    @BeforeClass
    public static void setup() {
        manager = new StandardTemplateManager();
    }

    @AfterClass
    public static void destroy() {
        manager = null;
    }

    public FormatEquivalenceTest(Signature signature, String uri, Format format, String formatName) {
        this.format = format;
        this.signature = signature;
    }

    @Parameterized.Parameters(name = "{index}: {3}: {1} ")
    public static List<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        // collect formats relevant for templates
        var formats = Arrays.stream(StandardFormat.values())
            .map(std -> std.format)
            .filter(Format::supportsTemplateReader)
            .filter(Format::supportsTemplateWriter)
            .collect(Collectors.toList());

        var stdLib = new StandardTemplateManager();
        stdLib.loadStandardTemplateLibrary();
        // collect signatures
        var signatures = stdLib.getStandardLibrary()
            .getAllTemplateObjects()
            .getStream()
            .map(Result::get)
            .collect(Collectors.toList());

        // combine collected templates with collected formats
        for (Format f : formats) {
            for (Signature s : signatures) {
                data.add(new Object[] { s, s.getIri(), f, f.getFormatName() });
            }
        }
        return data;
    }

    @Test
    public void test() throws IOException {

        assumeTrue(this.format.supportsTemplateReader());
        assumeTrue(this.format.supportsTemplateWriter());

        // write signature to string
        var writer = this.format.getTemplateWriter().get();
        writer.accept(this.signature);
        String coreString = writer.write(this.signature.getIri());

        // write string to file
        Path file = Files.createTempFile("template", this.format.getDefaultFileSuffix());
        Files.write(file, coreString.getBytes(Charset.forName("UTF-8")));

        // read file
        var reader = this.format.getTemplateReader().get();
        var ioSignatures = reader.apply(file.toAbsolutePath().toString())
            .getStream()
            .collect(Collectors.toList());

        assertThat(ioSignatures.size(), is(1));
        assertThat(ioSignatures.get(0).get(), is(this.signature));

    }
}
