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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import xyz.ottr.lutra.result.Message;

@Command(
    name = "lutra",
    descriptionHeading = "%n@|bold DESCRIPTION:|@%n",
    parameterListHeading = "%n@|bold PARAMETERS:|@%n",
    optionListHeading = "%n@|bold OPTIONS:|@%n",
    footerHeading = "%n",
    description = "Tool for working with OTTR Templates, for expanding instances and template definition,"
        + " translating between different formats and for checking the integrity of a template library.",
    footer = "@|bold EXAMPLES:|@%n"
        + "The following command reads all .ttl and .owl-files in ./lib as a template library and checks its intergrity:%n%n"
        + "    lutra -L wottr -m lint -l ./lib -e \"ttl,owl\"%n%n"
        + "The following translates all template files (with .ttl-extension) in ./lib from the legacy format to wottr,"
        + " and writes them to ./wottr:%n%n"
        + "    lutra -L legacy -O wottr -m formatLibrary -l ./lib -o ./wottr%n%n"
        + "The following expands all instances in ins1.xlsx and ins2.xlsx in tabOTTR using the templates in ./lib and writes"
        + " the expanded instances to exp.ttl in the wOTTR format:%n%n"
        + "    lutra -I tabottr -O wottr -m expand -l ./lib -o exp.ttl ins1.xlsx ins2.xlsx"
        + "%n%n@|bold DISCUSSION:|@%n"
        + "Note that with -O wottr all triple-instances outside of template definitions are written as normal RDF triples,"
        + " thus to expand a set of instances into an RDF graph this is what should be used."
        + "%n%nWhen a set of template definitions are written with -o <fpath>,"
        + " each template will be writen to a folder path of the form <fpath>/<tpath>/<name>.ttl, where"
        + " <tpath> is the path-part of the template's IRI, and <name> is the fragment of the IRI."
        + " E.g. with -o ./templates, the template with IRI"
        + "%n    http://example.org/draft/owl/SubclassOf%n"
        + "will be written to the path"
        + "%n    ./templates/draft/owl/SubclassOf.ttl."
        + "%n%n@|bold FURTHER INFORMATION:|@%n"
        + "Website: https://ottr.xyz%n"
        + "Source:  https://gitlab.com/ottr/lutra/lutra"
        + "%n%n@|bold REPORTING BUGS:|@%n"
        + "Please report any bugs as issues to our Git repository at"
        + "%n    https://gitlab.com/ottr/lutra/lutra/issues.",
    mixinStandardHelpOptions = true, 
    versionProvider = Settings.JarFileVersionProvider.class)
public class Settings {

    public enum Format { legacy, wottr, stottr, tabottr, bottr }

    @Option(names = {"--extension", "-e"}, split = ",",
        description = {"File extension of files to use as input to template library.%n"
                       + "(default: ${DEFAULT-VALUE})"})
    public String[] extensions = { };

    @Option(names = {"--ignoreExtension", "-E"}, split = ",",
        description = {"File extensions of files to ignore as input to template library.%n"
                       + "(default: ${DEFAULT-VALUE})"})
    public String[] ignoreExtensions = { };

    @Option(names = {"-I", "--inputFormat"}, completionCandidates = InsInputFormat.class,
        description = {"Input format of instances.%n"
                       + "(legal values: ${COMPLETION-CANDIDATES}; "
                       + "default: ${DEFAULT-VALUE})"})
    public Format inputFormat = Format.wottr;

    @Option(names = {"-O", "--outputFormat"}, completionCandidates = OutputFormat.class,
        description = {"Output format of output of operation defined by the mode.%n"
                       + "(legal values: ${COMPLETION-CANDIDATES}; "
                       + "default: ${DEFAULT-VALUE})"})
    public Format outputFormat = Format.wottr;

    @Option(names = {"-L", "--libraryFormat"}, completionCandidates = TplInputFormat.class,
        description = {"The input format of the library.%n"
                       + "(legal values: ${COMPLETION-CANDIDATES}; "
                       + "default: ${DEFAULT-VALUE})"})
    public Format libraryFormat = Format.wottr;


    @Option(names = {"-f", "--fetchMissing"},
        description = {"Fetch missing template dependencies. It is here assumed that"
                       + " templates' definitions are accessible via their IRI, that is, the IRI is"
                       + " either a path to a file, a URL, or similar.%n"
                       + "(default: ${DEFAULT-VALUE})"})
    public boolean fetchMissingDependencies = false;

    @Option(names = {"-F", "--fetchFormat"},
        description = {"The input format of the templates fetched via the -f flag."})
    public Format fetchFormat;

    @Option(names = {"-l", "--library"}, description = {"Folder containing templates to use as library."})
    public String library;

    @Parameters(description = {"Files of instances to which operations are to be applied."})
    public List<String> inputs = new LinkedList<>();

    @Option(names = {"-o", "--output"}, description = {"Path to which output from operations are to be written."})
    public String out;

    @Option(names = {"--stdout"},
        description = {"Print result of operations to standard out.%n"
                       + "(default: ${DEFAULT-VALUE})"})
    public boolean stdout = false;

    @Option(names = {"--quiet"},
        description = {"Suppress all messages, including errors and warnings.%n"
                       + "(default: ${DEFAULT-VALUE})"})
    public boolean quiet = false;

    @Option(names = {"--haltOn"},
        description = {"Halt on messages with a severity equal to or below the flag.%n"
                       + "(legal values: 3=INFO, 2=WARNING, 1=ERROR, 0=FATAL; "
                       + "default: ${DEFAULT-VALUE})"})
    public int haltOn = Message.ERROR;


    public enum Mode { expand, expandLibrary, format, formatLibrary, lint }

    @Option(names = {"-m", "--mode"},
        description = {"The mode of operation to be applied to input.%n"
                       + "(legal values: ${COMPLETION-CANDIDATES}; "
                       + "default: ${DEFAULT-VALUE})"})
    public Mode mode = Mode.expand;
    
    /* The following classes restrict the selections of Format to supported formats. */
    private static class InsInputFormat extends ArrayList<String> {

        static final long serialVersionUID = 0L; // Not correct!

        InsInputFormat() {
            super(Arrays.asList(
                    Format.legacy.toString(),
                    Format.wottr.toString(),
                    Format.stottr.toString(),
                    Format.tabottr.toString()));
        }
    }

    private static class TplInputFormat extends ArrayList<String> {

        static final long serialVersionUID = 0L; // Not correct!

        TplInputFormat() {
            super(Arrays.asList(
                    Format.stottr.toString(),
                    Format.legacy.toString(),
                    Format.wottr.toString()));
        }
    }

    private static class OutputFormat extends ArrayList<String> {

        static final long serialVersionUID = 0L; // Not correct!

        OutputFormat() {
            super(Arrays.asList(
                    Format.legacy.toString(),
                    Format.stottr.toString(),
                    Format.wottr.toString()));
        }
    }
    
    /**
     * This gets the version from the pom.xml file. Works only for jar file.
     */
    static class JarFileVersionProvider implements IVersionProvider {
        
        public String[] getVersion() {
            return new String[] { Settings.class.getPackage().getImplementationVersion() };
        }
    }

}
