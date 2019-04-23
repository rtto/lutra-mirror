package xyz.ottr.lutra.bottr.source;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

import xyz.ottr.lutra.bottr.model.Record;
import xyz.ottr.lutra.bottr.model.Source;
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

        List<Record<String>> rows = new ArrayList<Record<String>>();

        try {

            Processor proc = new Processor(false);
            XQueryCompiler comp = proc.newXQueryCompiler();
            XQueryExecutable exp;

            exp = comp.compile(query);

            XQueryEvaluator qe = exp.load();
            XdmValue result = qe.evaluate();

            for (XdmItem item : result) {
                rows.add(new Record<String>(nodeToList(item)));
            }

        } catch (SaxonApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ResultStream.innerOf(rows);
    }

    private List<String> nodeToList(XdmItem node) {

        List<String> output = new ArrayList<String>();

        for (XdmItem item : node) {
            if (item.isAtomicValue() == true) {
                output.add(item.getStringValue());
            } else {
                output.addAll(nodeToList(item));
            }
        }
        return output;
    }
}
