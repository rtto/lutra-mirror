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

    private @NonNull String input;
    private @NonNull String inFormat;
    private @NonNull String outFormat;
    private String libFormat;
    private String library;

    private static final String CHARSET = "UTF-8";

    public String run() throws IOException {

        File inputFile = writeTempFile(this.input);

        File libraryFile = StringUtils.isNoneBlank(this.library)
            ? libraryFile = writeTempFile(this.library)
            : null;

        String command = "--mode expand"
            + " --inputFormat " + this.inFormat
            + " --outputFormat " + this.outFormat
            + (libraryFile != null ? " --library " + libraryFile.getAbsolutePath() : "")
            + (libraryFile != null && this.libFormat != null ? " --libraryFormat " + this.libFormat : "")
            + " --fetchMissing"
            + " --stdout "
            + inputFile.getAbsolutePath();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outputStream, true, CHARSET);

        new CLI(ps).run(command.split(" "));

        // clean up
        delete(inputFile);
        delete(libraryFile);

        return outputStream.toString(CHARSET);
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
