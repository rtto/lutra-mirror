package xyz.ottr.lutra.stottr.io;

/*-
 * #%L
 * lutra-stottr
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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.result.Message;

public class PrototypeTest {

    private static final Logger log = LoggerFactory.getLogger(PrototypeTest.class);

    @Test
    public void test() {

        SInstanceParser parser = new SInstanceParser();
        parser.parseString(
            "@prefix ex: <http://example.com/> .\n"
                + "@prefix : <http://base.org/> .\n"
                + "@prefix xsd: <http://xsd.org/> .\n"
                + "<https://ex.com/T0>(:a, false) .\n"
                + "ex:H1(?c, :d) .\n"
                + "ex:H2(shouldProduceError, ?c, ?variable) .\n"
                + "ex:H3(:x, (:lst, 1, :val)) .\n"
                + "cross | ex:H35(:x, ++(:lst, 1, :val)) .\n"
                + "ex:H4(1, 2.32, .45) .\n"
                + "ex:H5(\"1\"^^xsd:int, \"hello\"@en) .\n"
                + "ex:T6([], _:blank) .")
            .forEach(res -> Message.info("Parsed :" + res.toString()).log(log));
    }
}
