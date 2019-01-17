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

//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.Queue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.result.MessageHandler;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.store.TemplateStore;

public class TemplateReader implements Function<String, ResultStream<TemplateSignature>> {

    private Function<String, ResultStream<TemplateSignature>> templatePipeline;
    private final Logger log = LoggerFactory.getLogger(TemplateReader.class);

    public TemplateReader(Function<String, ResultStream<TemplateSignature>> templatePipeline) {
        this.templatePipeline = templatePipeline;
    }

    public <M> TemplateReader(InputReader<String, M> templateInputReader, TemplateParser<M> templateParser) {
        this.templatePipeline = ResultStream.innerFlatMapCompose(templateInputReader, templateParser);
    }

    public ResultStream<TemplateSignature> apply(String file) {
        return templatePipeline.apply(file);
    }

    public MessageHandler populateTemplateStore(TemplateStore store, Set<String> iris) {
        return populateTemplateStore(store, ResultStream.innerOf(iris));
    }

    public MessageHandler populateTemplateStore(TemplateStore store, ResultStream<String> iris) {
        ResultConsumer<TemplateSignature> consumer = new ResultConsumer<TemplateSignature>(store);
        iris.innerFlatMap(templatePipeline).forEach(consumer);
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
            String[] includeExtensions, String[] excludeExtensions) throws IOException {
        log.info("Loading all templates from folder " + folder + " with suffix "
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
    public ResultStream<TemplateSignature> loadTemplatesFromFolder(String folder,
            String[] includeExtensions, String[] excludeExtensions) throws IOException {
        log.info("Loading all templates from folder " + folder + " with suffix "
                + Arrays.toString(includeExtensions) + " except " + Arrays.toString(excludeExtensions));
        return Files.loadFromFolder(folder, includeExtensions, excludeExtensions)
            .innerFlatMap(this.templatePipeline);
    }
}
