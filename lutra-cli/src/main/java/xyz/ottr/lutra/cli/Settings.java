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
import java.util.stream.Collectors;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import xyz.ottr.lutra.api.StandardFormat;
import xyz.ottr.lutra.system.Message;

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
        + "The following expands all instances in ins1.xlsx and ins2.xlsx in tabOTTR using the templates"
        + " in ./baselib and ./domain and writes the expanded instances to exp.ttl in the wOTTR format:%n%n"
        + "    lutra -I tabottr -O wottr -m expand -l ./baselib -l ./domain -o exp.ttl ins1.xlsx ins2.xlsx"
        + "%n%n@|bold DISCUSSION:|@%n"
        + "Note that with -O wottr all triple-instances outside of template definitions are written as normal RDF triples,"
        + " thus to expand a set of instances into an RDF graph this is what should be used."
        + "%n%nWhen a set of template definitions are written with -o <fpath>,"
        + " each template will be writen to a folder path of the form <fpath>/<tpath>/<name>.ttl, where"
        + " <tpath> is the path-part of the template's IRI, and <name> is the fragment of the IRI."
        + " E.g. with -o ./templates, the template with IRI"
        + "%n    http://example.org/draft/owl/SubclassOf%n"
        + "will be written to the path"
        + "%n    ./templates/draft/owl/SubclassOf.ttl.%n%n"
        + "Note that one can omit giving a format for libraries. In this case all possible formats are attempted,"
        + " and the first to succeed for each library is used for that library. However, all files within one"
        + " library needs to be of the same format, but"
        + " different libraries can have files of different formats."
        + "%n%n@|bold FURTHER INFORMATION:|@%n"
        + "Website: https://ottr.xyz%n"
        + "Source:  https://gitlab.com/ottr/lutra/lutra"
        + "%n%n@|bold REPORTING BUGS:|@%n"
        + "Please report any bugs as issues to our Git repository at"
        + "%n    https://gitlab.com/ottr/lutra/lutra/issues.",
    mixinStandardHelpOptions = true, 
    versionProvider = Settings.JarFileVersionProvider.class)
public class Settings {

    @Option(names = {"--extension", "-e"}, split = ",",
        description = {"File extension of files to use as input to template library.%n"
                       + "(default: ${DEFAULT-VALUE})"})
    public String[] extensions = { };

    @Option(names = {"--ignoreExtension", "-E"}, split = ",",
        description = {"File extensions of files to ignore as input to template library.%n"
                       + "(default: ${DEFAULT-VALUE})"})
    public String[] ignoreExtensions = { };

    @Option(names = {"-I", "--inputFormat"}, completionCandidates = InstanceInputFormat.class,
        description = {"Input format of instances.%n"
                       + "(legal values: ${COMPLETION-CANDIDATES}"
                       + " default: ${DEFAULT-VALUE})"})
    public StandardFormat inputFormat = StandardFormat.wottr;

    @Option(names = {"-O", "--outputFormat"}, completionCandidates = TemplateOutputFormat.class,
        description = {"Output format of output of operation defined by the mode.%n"
                       + "(legal values: ${COMPLETION-CANDIDATES}; "
                       + "default: ${DEFAULT-VALUE})"})
    public StandardFormat outputFormat = StandardFormat.wottr;

    @Option(names = {"-L", "--libraryFormat"}, completionCandidates = TemplateInputFormat.class,
        description = {"The input format of the libraries. If omitted, all available formats are attempted.%n"
                       + "(legal values: ${COMPLETION-CANDIDATES})"})
    public StandardFormat libraryFormat;


    @Option(names = {"-f", "--fetchMissing"},
        description = {"Fetch missing template dependencies. It is here assumed that"
                       + " templates' definitions are accessible via their IRI, that is, the IRI is"
                       + " either a path to a file, a URL, or similar.%n"
                       + "(default: ${DEFAULT-VALUE})"})
    public boolean fetchMissingDependencies = false;

    @Option(names = {"-p", "--prefixes"},
        description = "Path to RDF file containing prefix declarations to be used when rendering output. "
            + "   Any other data in the file is read, but ignored.")
    public String prefixes;

    @Option(names = {"-F", "--fetchFormat"},
        description = {"The input format of the templates fetched via the -f flag."})
    public StandardFormat fetchFormat;

    @Option(names = {"-l", "--library"}, 
        description = {"Folder containing templates to use as library."
                       + " Can be used multiple times for multiple libraries."})
    public String[] library;

    @Parameters(description = {"Files of instances to which operations are to be applied."})
    public List<String> inputs = new LinkedList<>();

    @Option(names = {"-o", "--output"}, description = {"Path for writing output."})
    public String out;

    @Option(names = {"--stdout"},
        description = {"Print system of operations to standard out.%n"
                       + "(default: ${DEFAULT-VALUE})"})
    public boolean stdout = false;

    @Option(names = {"--quiet"},
        description = {"Suppress all messages, including errors and warnings.%n"
                       + "(default: ${DEFAULT-VALUE})"})
    public boolean quiet = false;

    @Option(names = {"--haltOn"},
        description = {"Halt execution upon receiving messages with a severity equal to or greater than this value.%n"
                        + "(legal values: ${COMPLETION-CANDIDATES}; "
                        + "default: ${DEFAULT-VALUE})"})
    public Message.Severity haltOn = Message.Severity.ERROR;


    public enum Mode { expand, expandLibrary, format, formatLibrary, lint, checkSyntax, docttrLibrary }

    @Option(names = {"-m", "--mode"},
        description = {"The mode of operation to be applied to input.%n"
                       + "(legal values: ${COMPLETION-CANDIDATES}; "
                       + "default: ${DEFAULT-VALUE})"})
    public Mode mode = Mode.expand;

    @Option(names = {"--debugFullTrace"},
        description = {"This enables tracing such that printed messages get a stack trace "
                       + "giving more information on the location of the concerned objects. "
                       + "NB! Enabling this flag will deteriorate performance.%n"
                       + "default: ${DEFAULT-VALUE})"})
    public boolean debugFullTrace = false;

    @Option(names = {"--debugStackTrace"},
        description = {"This enables printing a regular java stack trace for error messages."
            + "Enabling this flag will not deteriorate performance.%n"
            + "default: ${DEFAULT-VALUE})"})
    public boolean debugStackTrace = false;
    
    /* The following classes restrict the selections of FormatName to supported formats. */
    private static class InstanceInputFormat extends ArrayList<String> {

        private static final long serialVersionUID = 0L; // TODO Not correct!

        InstanceInputFormat() {
            super(Arrays.stream(StandardFormat.values())
                .filter(f -> f.format.supportsInstanceReader())
                .map(StandardFormat::name)
                .collect(Collectors.toList())
            );
        }
    }

    private static class TemplateInputFormat extends ArrayList<String> {

        private static final long serialVersionUID = 0L; // TODO Not correct!

        TemplateInputFormat() {
            super(Arrays.stream(StandardFormat.values())
                .filter(f -> f.format.supportsTemplateReader())
                .map(StandardFormat::name)
                .collect(Collectors.toList())
            );
        }
    }

    private static class TemplateOutputFormat extends ArrayList<String> {

        private static final long serialVersionUID = 0L; // TODO Not correct!

        TemplateOutputFormat() {
            super(Arrays.stream(StandardFormat.values())
                .filter(f -> f.format.supportsTemplateWriter())
                .map(StandardFormat::name)
                .collect(Collectors.toList())
            );
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
