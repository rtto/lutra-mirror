package xyz.ottr.lutra.cli;

/*-
 * #%L
 * lutra-cli
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

import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;

import xyz.ottr.lutra.result.Message;

@Command(
    name = "lutra",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    description = "Tool for working with OTTR Templates, for expanding instances and template definition,"
        + " translating between different formats and for checking the integrity of a template library.%n%n"
        + "Examples:%n%n"
        + "The following command reads all .ttl and .owl-files in ./lib as a template library and checks its intergrity:%n%n"
        + "    lutra -l wottr -m lint -L ./lib -e \"ttl,owl\"%n%n"
        + "The following translates all template files (with .ttl-endings) in ./lib from the legacy format to wottr,"
        + " and writes translated templates to ./wott:%n%n"
        + "    lutra -l legacy -O wottr -m formatLibrary -L ./lib -o /wottr%n%n"
        + "The following expands all instances in instances.xlsx in tabOTTR using the templates in ./lib and writes"
        + " the expanded instances to exp.ttl in the wOTTR format:%n%n"
        + "    lutra -I tabottr -O wottr -m expand -L ./lib -o exp.ttl -f instances.xlsx%n",
    mixinStandardHelpOptions = true, 
    versionProvider = Settings.JarFileVersionProvider.class)
public class Settings {

    public enum Format { legacy, wottr, stottr, tabottr, qottr }

    @Option(names = {"--endings", "-e"}, description = {"File endings of files to use as template library.%n"
                                                        + "(default: ${DEFAULT-VALUE})"})
    public String[] endings = new String[]{ "ttl" };

    @Option(names = {"--ignoreEndings", "-E"}, split = ",",
        description = {"File endings of files to ignore as template library.%n"
                       + "(default: ${DEFAULT-VALUE})"})
    public String[] ignoreEndings = new String[]{ };

    @Option(names = {"-I", "--inputFormat"}, description = {"Input format.%n"
                                                            + "(legal values: ${COMPLETION-CANDIDATES}; "
                                                            + "default: ${DEFAULT-VALUE})"})
    public Format inputFormat = Format.wottr;

    @Option(names = {"-O", "--outputFormat"}, description = {"Output format.%n"
                                                             + "(legal values: ${COMPLETION-CANDIDATES}; "
                                                             + "default: ${DEFAULT-VALUE})"})
    public Format outputFormat = Format.wottr;

    @Option(names = {"-l", "--libraryFormat"}, description = {"The input format of the library.%n"
                                                              + "(legal values: ${COMPLETION-CANDIDATES}; "
                                                              + "default: ${DEFAULT-VALUE})"})
    public Format libraryFormat = Format.wottr;


    @Option(names = {"-F", "--fetchMissing"}, description = {"Fetch missing template dependencies. It is here assumed that"
                                                             + " templates' definitions are accessible via their IRI, that is, the IRI is"
                                                             + " either a path to a file, a URL, or similar.%n"
                                                             + "(default: ${DEFAULT-VALUE})"})
    public boolean fetchMissingDependencies = false;

    @Option(names = {"-L", "--library"}, description = {"Folder containing templates to use as library."})
    public String library;

    @Option(names = {"-f", "--file"}, description = {"Path to content to which operations are to be applied."})
    public String input;

    @Option(names = {"-o", "--out"}, description = {"Path to which output from operations are to be written."})
    public String out;

    @Option(names = {"--stdout"}, description = {"Print result of operations to standard out.%n"
                                                 + "(default: ${DEFAULT-VALUE})"})
    public boolean stdout = false;

    @Option(names = {"--quiet"}, description = {"Suppress all messages, including errors and warnings.%n"
                                                + "(default: ${DEFAULT-VALUE})"})
    public boolean quiet = false;

    @Option(names = {"--haltOn"}, description = {"Halt on messages with a severity equal to or below the flag.%n"
                                                 + "(legal values: 3=INFO, 2=WARNING, 1=ERROR, 0=FATAL; "
                                                 + "default: ${DEFAULT-VALUE})"})
    public int haltOn = Message.ERROR;


    public enum Mode { expand, expandLibrary, contract, format, formatLibrary, lint, analyse }

    @Option(names = {"-m", "--mode"}, description = {"The mode of operation to be applied to input.%n"
                                                     + "(legal values: ${COMPLETION-CANDIDATES}; "
                                                     + "default: ${DEFAULT-VALUE})"})
    public Mode mode = Mode.expand;
    
    
    /**
     * This gets the version from the pom.xml file. Works only for jar file.
     */
    static class JarFileVersionProvider implements IVersionProvider {
        
        public String[] getVersion() {
            return new String[] { Settings.class.getPackage().getImplementationVersion() };
        }
    }
}
