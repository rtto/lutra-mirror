package xyz.ottr.lutra.docttr;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-docttr
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.TemplateManager;
import xyz.ottr.lutra.io.Files;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.Result;

public class DocttrManager {

    public static final String HTML_EXT = ".html";

    public static final String FILENAME_MENU = "menu.html";
    public static final String FILENAME_FRONTPAGE = "frontpage.html";
    public static final String FILENAME_FRAMESET = "index.html";

    public static final String FRAMENAME_MAIN = "main-frame";

    //private final TemplateManager manager;
    private final PrefixMapping prefixMapping;
    private final TemplateStore templateStore;

    public DocttrManager(TemplateManager manager) {
        //this.manager = manager;
        this.prefixMapping = manager.getPrefixes();
        this.templateStore = manager.getTemplateStore();
    }

    private Map<String, Result<Signature>> getSignatureMap() {
        return this.templateStore.getAllTemplateObjectIRIs().stream()
            .collect(
                Collectors.toMap(Function.identity(),
                    this.templateStore::getTemplateObject));
    }

    public void write(Path outputFolder) {

        var signatures = getSignatureMap();

        // write all templates
        writeTemplates(signatures, outputFolder);

        // write frame page for all templates
        writeFrames(signatures, outputFolder, null);

        // write frame pages for all namespaces
        for (String domain : getDomains(signatures.keySet())) {
            writeFrames(filter(signatures, domain), outputFolder, domain);
        }

        // write frame pages for all namespaces
        for (String namespace : getNamespaces(signatures.keySet())) {
            writeFrames(filter(signatures, namespace), outputFolder, namespace);
        }
    }

    private static <V> Map<String, V> filter(Map<String, V> map, String uriStart) {
        return map.keySet().stream()
            .filter(key -> key.startsWith(uriStart))
            .collect(Collectors.toMap(Function.identity(), map::get));
    }

    static List<String> getNamespaces(Collection<String> iris) {
        return iris.stream()
            .map(ResourceFactory::createResource)
            .filter(Resource::isURIResource)
            .map(Resource::getNameSpace)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    static List<String> getDomains(Collection<String> iris) {
        return getNamespaces(iris).stream()
            .map(iri -> {
                try {
                    var host = new URI(iri).getHost();
                    int startHost = iri.indexOf(host);
                    return iri.substring(0, startHost + host.length());
                } catch (URISyntaxException e) {
                    return null;
                }
            })
            .filter(StringUtils::isNotBlank)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    private void writeFrames(Map<String, Result<Signature>> signatures, Path outputFolder, String rootIRI) {

        var folder = StringUtils.isNoneBlank(rootIRI)
            ? outputFolder.resolve(toLocalPath(rootIRI))
            : outputFolder;

        writeFile(new HTMLMenuWriter(this.prefixMapping).write(rootIRI, signatures),
            folder.resolve(FILENAME_MENU));

        writeFile(new HTMLIndexWriter(this.prefixMapping, this.templateStore).write(rootIRI, signatures),
            folder.resolve(FILENAME_FRONTPAGE));

        writeFile(new HTMLFramesetWriter().write(),
            folder.resolve(FILENAME_FRAMESET));
    }

    private void writeTemplates(Map<String, Result<Signature>> signatures, Path outputFolder) {

        var templateWriter = new HTMLTemplateWriter(this.prefixMapping, this.templateStore);

        signatures.forEach((iri, signatureResult) -> {
            var content = templateWriter.write(iri, signatureResult);
            writeFile(content, outputFolder.resolve(toLocalFilePath(iri)));
        });
    }

    @SneakyThrows(URISyntaxException.class)
    public static String toLocalPath(String iri) {
        return Files.iriToPath(iri);
    }

    /**
     * Create a local path from iri relative to the relativeTo input. Both iri and relativeTo must be IRIs.
     * Use the parents int to move the relativeTo to a parent. This can be necessary if relativeTo is a file.
     * @param iri
     * @param relativeTo
     * @param parents number of parent skips to move relativeTo.
     * @return
     */
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    static String toLocalPath(String iri, String relativeTo, int parents) {

        if (relativeTo == null) {
            return toLocalPath(iri);
        }

        var relativePath = Path.of(toLocalPath(relativeTo));
        while (parents > 0 && relativePath.getParent() != null) {
            parents -= 1;
            relativePath = relativePath.getParent();
        }

        return relativePath.relativize(Path.of(toLocalPath(iri))).toString();
    }

    static String toLocalPath(String iri, String relativeTo) {
        return toLocalPath(iri, relativeTo, 0);
    }

    public static String toLocalFilePath(String iri, String relativeTo, int parents) {
        return toLocalPath(iri, relativeTo, parents) + HTML_EXT;
    }

    public static String toLocalFilePath(String iri, String relativeTo) {
        return toLocalFilePath(iri, relativeTo, 0);
    }

    private static String toLocalFilePath(String iri) {
        return toLocalPath(iri) + HTML_EXT;
    }

    // TODO Align this with core's Files
    @SneakyThrows(IOException.class)
    private static void writeFile(String content, Path filePath) {

        // Need to get parent object to avoid spotbug's NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE
        var parent = filePath.getParent();
        if (Objects.nonNull(parent)) {
            java.nio.file.Files.createDirectories(parent);
        }
        java.nio.file.Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
    }
}