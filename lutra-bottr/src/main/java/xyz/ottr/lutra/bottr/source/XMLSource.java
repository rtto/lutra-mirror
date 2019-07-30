package xyz.ottr.lutra.bottr.source;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sf.saxon.lib.Feature;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import xyz.ottr.lutra.bottr.model.Record;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;

/*-
 * #%L
 * lutra-bottr
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

public class XMLSource implements Source<String> {

    public ResultStream<Record<String>> execute(String query) {

        Stream.Builder<Result<Record<String>>> rows = Stream.builder();

        try {
            
            Processor proc = new Processor(false);
            proc.setConfigurationProperty(Feature.STRIP_WHITESPACE, "all"); // Remove whitespace nodes
            XQueryCompiler comp = proc.newXQueryCompiler();
            XQueryExecutable exp =  comp.compile(query);
            XQueryEvaluator qe = exp.load();
            XdmValue result = qe.evaluate();

            for (XdmItem item : result) {
                rows.accept(Result.of(new Record<String>(nodeToList(item))));
            }

        } catch (SaxonApiException e) {
            Result<Record<String>> err = Result.empty(Message.error(
                    "Error when executing XML query: " + query
                    + "\n Recieved error: " + e.getMessage()));
            return ResultStream.of(err);
        }

        return new ResultStream<>(rows.build());
    }

    protected List<String> nodeToList(XdmItem node) {

        if (!(node instanceof XdmNode)) {
            return Arrays.asList(node.getStringValue());
        }

        XdmNode xnode = ((XdmNode) node);
        return xnode.axisIterator(Axis.CHILD).stream()
            .map(c -> c.getStringValue())
            .collect(Collectors.toList());
    }
}
