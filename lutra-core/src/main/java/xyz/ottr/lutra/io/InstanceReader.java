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

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.ResultStream;

public class InstanceReader implements Function<String, ResultStream<Instance>> {

    private Function<String, ResultStream<Instance>> instancePipeline;
    private final Logger log = LoggerFactory.getLogger(InstanceReader.class);

    public InstanceReader(Function<String, ResultStream<Instance>> instancePipeline) {
        this.instancePipeline = instancePipeline;
    }

    public <M> InstanceReader(InputReader<String, M> inputReader,
            InstanceParser<M> instanceParser) {
        // Need to explicitly assign it, to feed compiler the types.
        this.instancePipeline = ResultStream.innerFlatMapCompose(inputReader, instanceParser);
    }

    public ResultStream<Instance> readInstances(ResultStream<String> files) {
        return files.innerFlatMap(instancePipeline);
    }

    public ResultStream<Instance> apply(String filename) {
        return instancePipeline.apply(filename);
    }

    /**
     * Loads a folder of templates to be parsed when the parse-method is called.
     *
     * @param folder
     *            the folder containing templates to load
     * @param includeExtensions
     *            the file extensions of the files in the folder to include
     * @param excludeExtensions
     *            the file extensions of the files in the folder to exclude
     */
    public ResultStream<Instance> loadInstancesFromFolder(String folder, String[] includeExtensions,
            String[] excludeExtensions) throws IOException {
        log.info("Loading all template instaces from folder " + folder + " with suffix "
                + Arrays.toString(includeExtensions) + " except " + Arrays.toString(excludeExtensions));
        return readInstances(Files.loadFromFolder(folder, includeExtensions, excludeExtensions));
    }
}
