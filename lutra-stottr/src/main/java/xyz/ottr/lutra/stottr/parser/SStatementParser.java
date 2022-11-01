package xyz.ottr.lutra.stottr.parser;

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

import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Result;

public class SStatementParser<T> extends SBaseParserVisitor<T> {

    public Result visitBaseTemplate(stOTTRParser.BaseTemplateContext ctx) {
        return SParserUtils.ignoreStatement("base template", ctx);
    }

    public Result visitTemplate(stOTTRParser.TemplateContext ctx) {
        return SParserUtils.ignoreStatement("template", ctx);
    }

    public Result visitSignature(stOTTRParser.SignatureContext ctx) {
        return SParserUtils.ignoreStatement("signature", ctx);
    }

    public Result visitInstance(stOTTRParser.InstanceContext ctx) { return SParserUtils.ignoreStatement("instance", ctx); }

}
