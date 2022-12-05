package xyz.ottr.lutra.stottr.parser;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-stottr
 * %%
 * Copyright (C) 2018 - 2022 University of Oslo
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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.ResultConsumer;

public class ParserIgnoringTest {

    private String getMessage(ResultConsumer consumer, String statementType) {

        return consumer.getMessageHandler().getMessages().stream()
                .filter(msg -> msg.getMessage().contains("Ignoring"))
                .filter(msg -> msg.getMessage().contains(statementType))
                .findAny()
                .map(Message::toString)
                .orElse("");
    }

    @Test
    public void testInstanceParserIgnoringBaseTemplate() {

        var parser = new SInstanceParser();
        ResultConsumer<Instance> consumer = new ResultConsumer<>();

        parser.apply(
                "@prefix ex: <http://example.com/> .\n"
                        + "ex:BaseTemplate[?x, ?y] :: BASE .")
                .forEach(consumer);

        assertThat(getMessage(consumer, "base template"), not(""));
    }

    @Test
    public void testInstanceParserIgnoringTemplate() {

        var parser = new SInstanceParser();
        ResultConsumer<Instance> consumer = new ResultConsumer<>();

        parser.apply(
                "@prefix ex: <http://example.com/> .\n"
                        + "ex:Template[?x, ?y] :: { ottr:Triple(?x, ex:P, ?y) } .")
                .forEach(consumer);

        assertThat(getMessage(consumer, "template"), not(""));
    }

    @Test
    public void testInstanceParserIgnoringSignature() {

        var parser = new SInstanceParser();
        ResultConsumer<Instance> consumer = new ResultConsumer<>();

        parser.apply(
                "@prefix ex: <http://example.com/> .\n"
                        + "ex:Template[?x, ?y]  .")
                .forEach(consumer);

        assertThat(getMessage(consumer, "signature"), not(""));
    }

    @Test
    public void testTemplateParserIgnoringInstance() {

        var parser = new STemplateParser();
        var consumer = new ResultConsumer<Signature>();

        parser.apply(
                "@prefix ex: <http://example.com/> .\n"
                        + "ex:Template(ex:A, ex:B)  .")
                .forEach(consumer);

        assertThat(getMessage(consumer, "instance"), not(""));
    }

}
