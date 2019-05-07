package xyz.ottr.lutra.bottr.source;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import xyz.ottr.lutra.bottr.model.Record;
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

public class JSONSource extends XMLSource {

    private final String uri;
    
    public JSONSource(String source) {
        this.uri = source;
    }

    public ResultStream<Record<String>> execute(String query) {
        
        // Load JSON file
        // Convert JSON to XML
        //String json = "json-thingy";
    
        // the Saxon processor object
        Processor saxon = new Processor(false);
        
        List<Record<String>> rows = new ArrayList<Record<String>>();
        
        try {
            // compile the query
            XQueryCompiler compiler = saxon.newXQueryCompiler();
            XQueryExecutable exec = compiler.compile(query);


            // parse the string as a document node
            DocumentBuilder builder = saxon.newDocumentBuilder();
            Source src = new StreamSource(new StringReader(uri));
            XdmNode doc = builder.build(src);
            //XdmNode doc = builder.build(src);

            // instantiate the query, bind the input and evaluate
            XQueryEvaluator xquery = exec.load();
            xquery.setContextItem(doc);
            XdmValue result = xquery.evaluate();
            
            for (XdmItem item : result) {
                rows.add(new Record<String>(nodeToList(item)));
            }

        } catch (SaxonApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return ResultStream.innerOf(rows);
    }
}
