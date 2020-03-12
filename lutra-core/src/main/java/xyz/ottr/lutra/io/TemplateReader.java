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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.parser.TemplateParser;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;

public class TemplateReader implements Function<String, ResultStream<Signature>> {

    private final Function<String, ResultStream<Signature>> templatePipeline;
    private final TemplateParser<?> parser; // Needed for retrieving used prefixes
    private static final Logger log = LoggerFactory.getLogger(TemplateReader.class);

    public <M> TemplateReader(InputReader<String, M> templateInputReader,
            TemplateParser<M> templateParser) {
        this.templatePipeline = ResultStream.innerFlatMapCompose(templateInputReader, templateParser);
        this.parser = templateParser;
    }

    public Map<String, String> getPrefixes() {
        return this.parser.getPrefixes();
    }

    public ResultStream<Signature> apply(String file) {
        return this.templatePipeline.apply(file);
    }

    public MessageHandler populateTemplateStore(TemplateStore store, String iri) {
        return populateTemplateStore(store, ResultStream.innerOf(iri));
    }

    public MessageHandler populateTemplateStore(TemplateStore store, Set<String> iris) {
        return populateTemplateStore(store, ResultStream.innerOf(iris));
    }

    public MessageHandler populateTemplateStore(TemplateStore store, ResultStream<String> iris) {
        ResultConsumer<Signature> consumer = new ResultConsumer<>(store);
        iris.innerFlatMap(this.templatePipeline).forEach(consumer);
        return consumer.getMessageHandler();
    }

    public MessageHandler loadTemplatesFromFile(TemplateStore store, String file) {
        ResultConsumer<Signature> consumer = new ResultConsumer<>(store);
        this.templatePipeline.apply(file).forEach(consumer);
        return consumer.getMessageHandler();
    }

    /**
     * Loads a folder of templates to be parsed when the parse-method is called.
     *
     * @param store
     *       the TemplateStore the templates should be loaded into
     * @param folder
     *       the folder containing templates to load
     * @param includeExtensions
     *       the file extensions of the files in the folder to include
     * @param excludeExtensions
     *       the file extensions of the files in the folder to exclude
     * @return
     *       a MessageHandler containing possible Message-s with Warnings, Errors, etc.
     */
    public MessageHandler loadTemplatesFromFolder(TemplateStore store, String folder,
            String[] includeExtensions, String[] excludeExtensions) {

        this.log.info("Loading all templates from folder " + folder + " with suffix "
                + Arrays.toString(includeExtensions) + " except " + Arrays.toString(excludeExtensions));

        return populateTemplateStore(store,
                                     Files.loadFromFolder(folder,
                                                          includeExtensions,
                                                          excludeExtensions));
    }

    /**
     * Loads a folder of templates to be parsed when the parse-method is called.
     *
     * @param folder
     *       the folder containing templates to load
     * @param includeExtensions
     *       the file extensions of the files in the folder to include
     * @param excludeExtensions
     *       the file extensions of the files in the folder to exclude
     * @return
     *       a ResultStream containing the parsed TemplateSignatures 
     */
    public ResultStream<Signature> loadTemplatesFromFolder(String folder,
                                                           String[] includeExtensions, String[] excludeExtensions) {
        log.info("Loading all templates from folder " + folder + " with suffix "
                + Arrays.toString(includeExtensions) + " except " + Arrays.toString(excludeExtensions));
        return Files.loadFromFolder(folder, includeExtensions, excludeExtensions)
            .innerFlatMap(this.templatePipeline);
    }
    
    @Override
    public String toString() {
        return this.parser.toString();
    }
}
