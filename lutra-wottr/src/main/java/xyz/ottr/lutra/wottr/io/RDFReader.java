package xyz.ottr.lutra.wottr.io;

/*-
 * #%L
 * lutra-wottr
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

import static org.apache.jena.riot.SysRIOT.fmtMessage;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.Space;
import xyz.ottr.lutra.io.InputReader;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.util.PrefixValidator;

@Getter
public abstract class RDFReader<X> implements InputReader<X, Model> {

    private final PrefixMapping prefixes;
    protected final RDFParserBuilder parserBuilder;

    RDFReader() {
        this.prefixes = PrefixMapping.Factory.create();
        this.parserBuilder = RDFIO.readerBuilder();
    }

    public ResultStream<Model> apply(X input) {
        return ResultStream.of(parse(input));
    }

    public Result<Model> parse(X source) {

        setSource(source);
        var errorHandler = new RDFReaderErrorHandler();
        Model model = ModelFactory.createDefaultModel();

        List<Message> parsingMessages = new ArrayList<>();

        try {
            this.parserBuilder
                .errorHandler(errorHandler)
                .parse(model);

            this.prefixes.setNsPrefixes(model);

        } catch (RiotParseException ignored) {
            // ignore RiotParseException as this is collected by the errorHandler.
        } catch (RuntimeException ex) {
            // must catch all other exceptions since not throwing RiotParseExceptions may cause others to be thrown.
            parsingMessages.add(Message.error(ex));
        }

        var result = Result.of(model)
                .flatMap(PrefixValidator::check);

        result.addMessages(parsingMessages);
        result.addMessages(errorHandler.messages);

        if (parsingMessages.isEmpty() && errorHandler.messages.isEmpty()) {
            return result;
        } else {
            // #324 Result::mapToStream
            return Result.empty(Message.error("Error parsing " + source + " with "
                + source.getClass().getSimpleName() + "."), result);
        }
    }


    abstract void setSource(X source);


    /**
     * Error handler for collecting more errors instead of just throwing first error as exception.
     */
    private class RDFReaderErrorHandler implements ErrorHandler {

        private List<Message> messages = new ArrayList<>();

        private void addMessage(Message.Severity severity, String message, long line, long col) {
            this.messages.add(new Message(severity, fmtMessage(message, line, col) + Space.LINEBR));
        }

        @Override
        public void warning(String message, long line, long col) {
            addMessage(Message.Severity.WARNING, message, line, col);
        }

        @Override
        public void error(String message, long line, long col) {
            addMessage(Message.Severity.ERROR, message, line, col);
        }

        @Override
        public void fatal(String message, long line, long col) {
            addMessage(Message.Severity.FATAL, message, line, col);
        }
    }

}
