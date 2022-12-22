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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.ottr.lutra.api.StandardFormat;
import xyz.ottr.lutra.api.StandardTemplateManager;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public class FormatEquivalenceTest {

    public static Stream<Arguments> data() {

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
            .getAllSignatures()
            .getStream()
            .map(Result::get)
            .collect(Collectors.toList());

        // combine collected templates with collected formats
        Stream.Builder<Arguments> data = Stream.builder();
        for (Format f : formats) {
            for (Signature s : signatures) {
                data.add(arguments(f, s));
            }
        }

        return data.build();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void test(Format format, Signature signature, @TempDir Path tmpFolder) throws Exception {

        assumeTrue(format.supportsTemplateReader());
        assumeTrue(format.supportsTemplateWriter());

        var writer = format.getTemplateWriter().get();

        BiFunction<String, String, Optional<Message>> writerFunc = (iri, str)
                -> xyz.ottr.lutra.io.Files.writeTemplatesTo(iri, str, tmpFolder.toString(), format.getDefaultFileSuffix());

        writer.setWriterFunction(writerFunc);
        writer.accept(signature); //write file

        // read file
        String iriFilePath = xyz.ottr.lutra.io.Files.iriToPath(signature.getIri()) + "" + format.getDefaultFileSuffix();
        String absFilePath = tmpFolder.resolve(iriFilePath).toAbsolutePath().toString();

        var reader = format.getTemplateReader().get();
        var ioSignatures = reader.apply(absFilePath)
            .getStream()
            .collect(Collectors.toList());

        assertThat(ioSignatures.size(), is(1));
        assertThat(ioSignatures.get(0).get(), is(signature));
    }
}
