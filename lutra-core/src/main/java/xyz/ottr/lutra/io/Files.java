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
//import java.io.IOException;
//import java.util.Collection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;

import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;

public abstract class Files {
    // TODO is this correct
    private static IOFileFilter hiddenFiles = new NotFileFilter(
            FileFilterUtils.or(new PrefixFileFilter("."), new PrefixFileFilter("#")));
    private static Function<String, IOFileFilter> extFilter = string -> FileFilterUtils.suffixFileFilter(string,
            IOCase.INSENSITIVE);

    public static Message checkIsReadableFolder(String folder) throws SecurityException {
        Path path = Paths.get(folder);

        try {
            if (!java.nio.file.Files.exists(path)) {
                return Message.error("No folder with path " + folder + " exists.");
            }
            if (!java.nio.file.Files.isDirectory(path)) {
                return Message.error("The path " + folder + " does not denote a folder.");
            }
            if (!java.nio.file.Files.isReadable(path)) {
                return Message.error("The folder " + folder + " is not readable.");
            }
        } catch (SecurityException ex) {
            return Message.error("Encountered security error when attempting to read"
                + " folder's metadata -- " + ex.getMessage());
        }

        return null;
    }

    public static ResultStream<File> getFolderContents(String folder, String[] includeExtensions,
            String[] excludeExtensions) {

        Message err = checkIsReadableFolder(folder);
        if (err != null) {
            return ResultStream.of(Result.empty(err));
        }
            
        IOFileFilter ext = null;

        for (int i = 0; i < includeExtensions.length; i += 1) {
            if (i == 0 && ext == null) {
                ext = extFilter.apply(includeExtensions[i]);
            }
            ext = FileFilterUtils.or(ext, extFilter.apply(includeExtensions[i]));
        }

        for (int i = 0; i < excludeExtensions.length; i += 1) {
            if (i == 0 && ext == null) {
                ext = FileFilterUtils.notFileFilter(extFilter.apply(excludeExtensions[i]));
            } else {
                ext = FileFilterUtils.and(ext, FileFilterUtils.notFileFilter(extFilter.apply(excludeExtensions[i])));
            }
        }

        if (ext == null) {
            ext = FileFilterUtils.trueFileFilter();
        }

        return ResultStream.innerOf(
            FileUtils.listFiles(new File(folder),
                FileFilterUtils.and(hiddenFiles, ext),
                hiddenFiles));
    }

    public static ResultStream<String> loadFromFolder(String folder, String[] includeExtensions,
            String[] excludeExtensions) {

        Message err = checkIsReadableFolder(folder);
        if (err != null) {
            return ResultStream.of(Result.empty(err));
        }
            
        return Files.getFolderContents(folder, includeExtensions, excludeExtensions)
            .mapFlatMap(file -> Result.of(file.getPath()));
    }
}
