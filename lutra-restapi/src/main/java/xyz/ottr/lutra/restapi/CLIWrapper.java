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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import xyz.ottr.lutra.cli.CLI;

@Setter
@NoArgsConstructor
public class CLIWrapper {

    private @NonNull String mode;
    private @NonNull String input;
    private @NonNull String inputFormat;
    private @NonNull String outputFormat;
    private boolean fetchMissing;
    private String libraryFormat;
    private String library;

    private static final String CHARSET = "UTF-8";

    public String run() throws IOException {

        File inputFile = writeTempFile(this.input);

        File libraryFile = StringUtils.isNoneBlank(this.library)
            ? writeTempFile(this.library)
            : null;

        String command =
            "--mode " + this.mode
            + " --inputFormat " + this.inputFormat
            + " --outputFormat " + this.outputFormat
            + (libraryFile != null ? " --library " + libraryFile.getAbsolutePath() : "")
            + (libraryFile != null && this.libraryFormat != null ? " --libraryFormat " + this.libraryFormat : "")
            + (this.fetchMissing ? " --fetchMissing" : "")
            + " --stdout "
            + inputFile.getAbsolutePath();

        String output;
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(outStream, true, CHARSET)) {
            new CLI(out, out).run(command.split(" "));
            output = outStream.toString(CHARSET);
        }

        // clean up
        delete(inputFile);
        delete(libraryFile);

        return output;
    }

    private static File writeTempFile(String contents) throws IOException {
        File tempInput = Files.createTempFile("lutra-", ".tmp").toFile();
        FileUtils.write(tempInput, contents, CHARSET);
        return tempInput;
    }

    private static void delete(File file) throws IOException {
        if (file != null) {
            Files.deleteIfExists(file.toPath());
        }
    }

}
