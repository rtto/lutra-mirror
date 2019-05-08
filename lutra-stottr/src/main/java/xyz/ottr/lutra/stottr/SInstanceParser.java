package xyz.ottr.lutra.stottr;

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
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.stottr.antlr.stOTTRBaseVisitor;
import xyz.ottr.lutra.stottr.antlr.stOTTRLexer;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser.IriContext;

public class SInstanceParser extends stOTTRBaseVisitor<Result<Instance>> {

    // TODO: Should first parse all prefixes and make a PrefixMapping
    //       Maybe need to make a SInstanceFileParser that parses all instances
    //       and prefixes?

    public SInstanceParser() {
    }

    public Result<Instance> parseString(String str) {
        return parseStream(CharStreams.fromString(str));
    }

    public Result<Instance> parseStream(CharStream in) {
        stOTTRLexer lexer = new stOTTRLexer(in);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        stOTTRParser parser = new stOTTRParser(commonTokenStream);
 
        stOTTRParser.InstanceContext insContext = parser.instance();
        return visitInstance(insContext);
    }

    @Override
    public Result<Instance> visitInstance(stOTTRParser.InstanceContext ctx) {

        IriContext iriCtx = ctx.templateRef().templateName().iri();
        String iri;

        if (iriCtx.prefixedName() != null) {
            // TODO: Use prefixes to expand to full name
            iri = iriCtx.prefixedName().PNAME_LN().getSymbol().getText();
        } else {
            iri = iriCtx.IRIREF().getSymbol().getText();
        }

        if (iri == null) {
            return Result.empty(Message.error(ctx.toString()));
        } else {
            return Result.empty(Message.error(iri));
        }
    }
}
