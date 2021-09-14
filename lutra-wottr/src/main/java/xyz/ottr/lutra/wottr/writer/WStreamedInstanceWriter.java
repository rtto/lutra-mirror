package xyz.ottr.lutra.wottr.writer;

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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.writer.InstanceWriter;

public class WStreamedInstanceWriter implements InstanceWriter {

    private boolean prefixWriteFlag = true; //flag to ensure prefixes are written only once    

    // Set by constructor
    private PrefixMapping prefixes;

    // Set by call to init
    private StreamRDF out;              // File output (wraps below stream)
    private FileOutputStream outStream; // File output (pointer for closing)
    private StreamRDF console;          // Stdout (wraps below stream)
    private PrintStream consoleStream;  // Stdout (pointer for closing)
    private MessageHandler msgs;

    public WStreamedInstanceWriter() {
        this(PrefixMapping.Factory.create());
    }

    public WStreamedInstanceWriter(PrefixMapping prefixes) {
        this.prefixes = prefixes;
    }

    private StreamRDF makeStreamRDF(OutputStream output) {
        return StreamRDFWriter.getWriterStream(output, Lang.TURTLE);
    }

    @Override
    public MessageHandler init(String filePath, PrintStream consoleStream) {

        this.msgs = new MessageHandler();
        
        if (consoleStream != null) {
            this.consoleStream = consoleStream;
            this.console = makeStreamRDF(this.consoleStream);
            this.msgs.add(Message.info("Output will be written to console"));
        }
        
        if (filePath != null) {
            try {
                Path parentDirs = Paths.get(filePath).getParent(); 
                if (parentDirs != null) {
                    Files.createDirectories(parentDirs); //create parent directories
                }
                this.outStream = new FileOutputStream(filePath);
                this.out = makeStreamRDF(this.outStream);
            } catch (IOException ex) {
                Message err = Message.error("Error opening file " + filePath + ": " + ex.getMessage());
                this.msgs.add(err);
            }
        }

        return this.msgs;
    }

    @Override
    public MessageHandler write(String contents) {

        if (this.consoleStream != null) {
            this.consoleStream.println(contents);
        }

        if (this.outStream != null) {
            try {
                this.outStream.write(contents.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                Message err = Message.error("Error writing file: " + ex.getMessage());
                this.msgs.add(err);
            }
        }
        return this.msgs;
    }

    private void writePrefixes() {

        if (this.prefixWriteFlag) {
            if (this.out != null) {
                this.prefixes.getNsPrefixMap().forEach(this.out::prefix);
            }

            if (this.console != null) {
                this.prefixes.getNsPrefixMap().forEach(this.console::prefix);
            }
            this.prefixWriteFlag = false;
        }
    }

    @Override
    public void accept(Instance instance) {

        writePrefixes();
            
        Model model = ModelFactory.createDefaultModel();
        List<Triple> triples = new LinkedList<>();

        if (WTripleWriter.isTriple(instance)) {
            triples.add(WTripleWriter.write(model, instance).asTriple());
            // Also need to add other statements, in case an argument is a list-term
            triples.addAll(model.listStatements().mapWith(Statement::asTriple).toList());
        } else {
            WriterUtils.createInstanceNode(model, instance);
            triples.addAll(model.listStatements().mapWith(Statement::asTriple).toList());
        }

        if (this.out != null) {
            triples.forEach(this.out::triple);
        }

        if (this.console != null) {
            triples.forEach(this.console::triple);
        }
    }
    
    @Override
    public MessageHandler flush() {
        return this.msgs;
    }

    @Override
    public MessageHandler close() {

        try {
            if (this.out != null) {
                this.out.finish();
                this.outStream.close();
            }
            // Do not close this.console as it will close console output for rest
            // of execution!
        } catch (Exception ex) {
            this.msgs.add(Message.error("Error closing streams: " + ex.getMessage()));
        }

        return this.msgs;
    }
}
