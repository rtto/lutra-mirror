package xyz.ottr.lutra.io;

/*-
 * #%L
 * lutra-core
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

/**
 * Utility class for working with files.
 */
public enum Files {
    ;

    private static final IOFileFilter hiddenFiles = new NotFileFilter(
            FileFilterUtils.or(
                new PrefixFileFilter("."),
                new PrefixFileFilter("#"),
                new PrefixFileFilter("~"),
                new SuffixFileFilter("~")
            ));
    private static final Function<String, IOFileFilter> extFilter = string -> FileFilterUtils.suffixFileFilter(string,
            IOCase.INSENSITIVE);

    public static Optional<Message> writeFile(String content, String filePath, String suffix) {

        try {
            java.nio.file.Files.write(Paths.get(filePath + suffix), content.getBytes(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            Message err = Message.error("Error writing output to file.", ex);
            return Optional.of(err);
        }
        return Optional.empty();
    }

    public static Optional<Message> writeTemplatesTo(String iri, String content, String folder, String suffix) {

        try {
            // TODO: cli-arg to decide extension
            String iriPath = Files.iriToPath(iri);
            java.nio.file.Files.createDirectories(Paths.get(folder, Files.iriToDirectory(iriPath)));
            java.nio.file.Files.write(Paths.get(folder, iriPath + suffix), content.getBytes(Charset.forName("UTF-8")));
        } catch (IOException | URISyntaxException ex) {
            Message err = Message.error("Error when writing output to file.", ex);
            return Optional.of(err);
        }
        return Optional.empty();
    }
    
    public static String iriToDirectory(String pathStr) {
        Path folder = Paths.get(pathStr).getParent();
        return Objects.toString(folder, null);
    }

    public static String iriToPath(String iriStr) throws URISyntaxException {

        var uri = new URI(iriStr);

        return uri.getHost()
            + (StringUtils.isNotBlank(uri.getPath()) ? uri.getPath() : "")
            + (StringUtils.isNotBlank(uri.getFragment()) ? "/" + uri.getFragment() : "");
    }

    public static Message checkFolderReadable(Path path) throws SecurityException {

        try {
            if (!java.nio.file.Files.exists(path)) {
                return Message.error("No folder with path " + path + " exists.");
            }
            if (!java.nio.file.Files.isDirectory(path)) {
                return Message.error("The path " + path + " is not a folder.");
            }
            if (!java.nio.file.Files.isReadable(path)) {
                return Message.error("The folder " + path + " is not readable.");
            }
        } catch (SecurityException ex) {
            return Message.error("Encountered security issue while reading folder metadata.", ex);
        }

        return null;
    }

    private static Message checkFolderEmpty(Path path) {
        try {
            DirectoryStream<Path> directory = java.nio.file.Files.newDirectoryStream(path);
            if (directory.iterator().hasNext()) {
                directory.close();
                return null;
            }

            directory.close();
            return Message.warning("The folder " + path + " is empty.");

        } catch (IOException ex) {
            return Message.error("I/O error occurred when opening folder", ex);
        }
    }

    private static Message checkFolder(String folder) {
        Path path = Paths.get(folder);
        Message msg = checkFolderEmpty(path);

        if (msg == null) {
            msg = checkFolderReadable(path);
        }
        return msg;
    }

    public static ResultStream<File> getFolderContents(String folder, String[] includeExtensions,
            String[] excludeExtensions) {

        Message err = checkFolder(folder);
        if (err != null) {
            return ResultStream.of(Result.empty(err));
        }

        // if there are no includes, we include all, i.e, the filter is true, else take the disjunction.
        IOFileFilter includes = (includeExtensions.length == 0)
            ? FileFilterUtils.trueFileFilter()
            : Arrays.stream(includeExtensions)
            .map(extFilter)
            .reduce(FileFilterUtils.falseFileFilter(), FileFilterUtils::or);

        // conjunction of negations
        IOFileFilter excludes = Arrays.stream(excludeExtensions)
            .map(extFilter)
            .map(FileFilterUtils::notFileFilter)
            .reduce(FileFilterUtils.trueFileFilter(), FileFilterUtils::and);

        // conjunction of all filters:
        IOFileFilter fileFilter = Stream.of(includes, excludes, hiddenFiles)
            .reduce(FileFilterUtils.trueFileFilter(), FileFilterUtils::and);

        return ResultStream.innerOf(FileUtils.listFiles(new File(folder), fileFilter, hiddenFiles));
    }

    public static ResultStream<String> loadFromFolder(String folder, String[] includeExtensions,
            String[] excludeExtensions) {

        return getFolderContents(folder, includeExtensions, excludeExtensions)
            .mapFlatMap(file -> Result.of(file.getPath()));
    }
}
