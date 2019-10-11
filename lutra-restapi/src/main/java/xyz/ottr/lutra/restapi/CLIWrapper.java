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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.cli.CLI;

@Setter
public class CLIWrapper {

    private @NonNull String mode;
    private Collection<File> inputFiles;
    private Collection<File> libraryFiles;
    //private Collection<File> dataFiles;
    private @NonNull String inputFormat;
    private @NonNull String outputFormat;
    private boolean fetchMissing;
    private String libraryFormat;
    private String library;

    private Path inputDirectory;
    private Path libraryDirectory;

    private static final String tempPrefix = "weblutra-";

    private static final String CHARSET = "UTF-8";
    
    private static final Logger log = LoggerFactory.getLogger(CLIWrapper.class);

    CLIWrapper() throws IOException {
        this.inputFiles = new ArrayList<>();
        this.libraryFiles = new ArrayList<>();
        this.inputDirectory = Files.createTempDirectory(tempPrefix + "input-");
        this.libraryDirectory = Files.createTempDirectory(tempPrefix + "library-");
    }

    void addInput(FileItem fileItem) throws Exception {
        addFileItem(fileItem, this.inputDirectory, this.inputFiles);
    }

    void addInput(String fileContent) throws IOException {
        addFileContent(fileContent, this.inputDirectory, this.inputFiles);
    }

    void addLibrary(FileItem fileItem) throws Exception {
        addFileItem(fileItem, this.libraryDirectory, this.libraryFiles);
    }

    void addLibrary(String fileContent) throws IOException {
        addFileContent(fileContent, this.libraryDirectory, this.libraryFiles);
    }

    private void addFileItem(FileItem fileItem, Path path, Collection<File> files) throws Exception {
        File outputFile = Files.createTempFile(path, "", "-" + fileItem.getName()).toFile();
        fileItem.write(outputFile);
        files.add(outputFile);
    }

    private void addFileContent(String content, Path path, Collection<File> files) throws IOException {
        if (StringUtils.isNoneBlank(content)) {
            File file = Files.createTempFile(path,"", ".txt").toFile();
            FileUtils.write(file, content, CHARSET);
            files.add(file);
        }
    }

    String run() throws IOException {

        String command =
            "--mode " + this.mode
            + " --inputFormat " + this.inputFormat
            + " --outputFormat " + this.outputFormat
            + (!this.libraryFiles.isEmpty() ? " --library " + this.libraryDirectory.toAbsolutePath() : "")
            + (!this.libraryFiles.isEmpty() && this.libraryFormat != null ? " --libraryFormat " + this.libraryFormat : "")
            + (this.fetchMissing ? " --fetchMissing" : "")
            + " --stdout "
            + this.inputFiles.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(" "));

        log.info(command);

        String output;
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(outStream, true, CHARSET)) {
            new CLI(out, out).run(command.split(" "));
            output = outStream.toString(CHARSET);
        }

        // clean up
        FileUtils.deleteDirectory(this.inputDirectory.toFile());
        FileUtils.deleteDirectory(this.libraryDirectory.toFile());

        return output;
    }

}
