package xyz.ottr.lutra.wottr.io;

/*-
 * #%L
 * lutra-wottr
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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;

public class RDFFileReaderTest {

    private static final String ROOT = "src/test/resources/";

    private static final Logger log = LoggerFactory.getLogger(RDFFileReaderTest.class);

    @Test
    public void parseEmptyFile() {
        String file = "correct/emptyFile.ttl";
        var result = RDFIO.fileReader().parse(ROOT + file);
        Assertions.noErrors(result);
    }

    @Test
    public void parseFaultyFile() {
        String file = "incorrect/faultyRDF.ttl";
        var result = RDFIO.fileReader().parse(ROOT + file);
        Assertions.atLeast(result, Message.Severity.ERROR);
    }

    @Test
    public void parseMissingFile() {
        String file = "blablabla--this-file-not-exist";
        var result = RDFIO.fileReader().parse(ROOT + file);
        Assertions.atLeast(result, Message.Severity.ERROR);
    }

}
