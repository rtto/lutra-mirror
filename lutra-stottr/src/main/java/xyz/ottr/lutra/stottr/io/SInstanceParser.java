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

import org.antlr.v4.runtime.CharStream;

import xyz.ottr.lutra.io.InstanceParser;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;

public class SInstanceParser extends SParser<Instance> implements InstanceParser<CharStream> {

    public ResultStream<Instance> apply(CharStream in) {
        return parseDocument(in);
    }

    @Override
    public Result<Instance> visitStatement(stOTTRParser.StatementContext ctx) {

        if (ctx.instance() == null) { // Not an instance
            return Result.empty(); // TODO: Decide on error or ignore?
        }

        return visitInstance(ctx.instance());
    }

    @Override
    public Result<Instance> visitInstance(stOTTRParser.InstanceContext ctx) {

        // Parse template name
        Result<String> iriRes = getTermParser()
            .visitIri(ctx.templateName().iri())
            .map(iri -> ((IRITerm) iri).getIRI());

        // Parse arguments and possible list expander
        SArgumentListParser argumentListParser = new SArgumentListParser(getTermParser());
        Result<ArgumentList> argsRes = argumentListParser.visitInstance(ctx);

        return Result.zip(iriRes, argsRes, (iri, args) -> new Instance(iri, args));
    }
}
