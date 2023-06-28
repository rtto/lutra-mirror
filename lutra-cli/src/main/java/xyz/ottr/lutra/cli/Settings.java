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
    description = "Reference implementation for OTTR Templates. Use for expanding template instances and template definitions,"
        + " translating between different formats and for checking the integrity of template libraries.",
    footer = "@|bold LINKS:|@%n"
        + "Website:  https://ottr.xyz%n"
        + "Primers:  https://primer.ottr.xyz%n"
        + "Git repo: https://gitlab.com/ottr/lutra/lutra",
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

    @Option(names = {"-I", "--inputFormat"}, completionCandidates = Settings.InstanceInputFormat.class,
        description = {"Input format of instances.%n"
                       + "(registered formats: ${COMPLETION-CANDIDATES}"
                       + " default: ${DEFAULT-VALUE})"})
    public String inputFormat = StandardFormat.wottr.toString();

    @Option(names = {"-O", "--outputFormat"}, completionCandidates = Settings.AllFormats.class,
        description = {"Output format of output of operation defined by the mode.%n"
                       + "(registered formats: ${COMPLETION-CANDIDATES}; "
                       + "default: ${DEFAULT-VALUE})"})
    public String outputFormat = StandardFormat.wottr.toString();

    @Option(names = {"-L", "--libraryFormat"}, completionCandidates = Settings.TemplateInputFormat.class,
        description = {"The input format of the libraries. If omitted, all available formats are attempted.%n"
                       + "(registered formats: ${COMPLETION-CANDIDATES})"})
    public String libraryFormat;


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

    @Option(names = {"-F", "--fetchFormat"}, completionCandidates = Settings.TemplateInputFormat.class,
        description = {"The input format of the templates fetched via the -f flag (registered formats: ${COMPLETION-CANDIDATES})"})
    public String fetchFormat;

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

    @Option(names = {"--disableFullTrace"},
        description = {"This disable tracing such that printed messages get a stack trace "
                       + "giving less information on the location of the concerned objects. "
                       + "default: ${DEFAULT-VALUE})"})
    public boolean disableFullTrace = false;

    @Option(names = {"--debugStackTrace"},
        description = {"This enables printing a regular java stack trace for error messages."
                + "Enabling this flag will not deteriorate performance.%n"
                + "default: ${DEFAULT-VALUE})"})
    public boolean debugStackTrace = false;

    /* The following classes *informs* the CLI about registered formats. It does not *validate* the input format name. */
    private static class AllFormats extends ArrayList<String> {

        private static final long serialVersionUID = 0L; // TODO Not correct!

        AllFormats() {
            super(Arrays.stream(StandardFormat.values())
                .map(StandardFormat::name)
                .collect(Collectors.toList())
            );
        }
    }

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
