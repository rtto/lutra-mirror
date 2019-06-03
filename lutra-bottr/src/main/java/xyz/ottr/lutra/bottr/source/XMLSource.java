package xyz.ottr.lutra.bottr.source;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;

//import javax.xml.transform.sax.SAXSource;

//import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
//import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

//import org.xml.sax.InputSource;

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

    //private final String uri;
    
    //public XMLSource(String source) {
    //    this.uri = source;
    //}

    public ResultStream<Record<String>> execute(String query) {

        List<Record<String>> rows = new ArrayList<Record<String>>();

        try {
            
            Processor proc = new Processor(false);
            XQueryCompiler comp = proc.newXQueryCompiler();
            XQueryExecutable exp =  comp.compile(query);
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

    protected List<String> nodeToList(XdmItem node) {

        List<String> output = new ArrayList<String>();

        for (XdmItem item : node) {
            
            // Must do some formatting on the XQuery result, might want to comment these in order to debug the JSON issues

            // Get the result
            String itemString = item.getStringValue();

            // Get rid of tab characters and indentation spacing
            itemString = itemString.replace("\t", "");
            itemString = itemString.replace("  ", "");
            
            // Split the result using the line breaks as separators
            List<String> row = Arrays.asList(itemString.split("\\R"));
            output.add(itemString);
            
            // Remove the first element, which is empty and results from a line break
            output.remove(0);
        }
        return output;
    }
}
