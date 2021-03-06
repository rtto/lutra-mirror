package xyz.ottr.lutra.stottr.writer;

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

import java.util.List;
import java.util.stream.Collectors;
import xyz.ottr.lutra.Space;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.stottr.STOTTR;

public class SAnnotationInstanceWriter extends SInstanceWriter {

    protected SAnnotationInstanceWriter(STermWriter termWriter) {
        super(termWriter);
    }

    protected String writeArguments(List<Argument> args) {
        return args.stream()
            .map(this::writeArgument)
            .collect(Collectors.joining(STOTTR.Terms.annoArgSep, STOTTR.Terms.insArgStart, STOTTR.Terms.insArgEnd));
    }

    protected StringBuilder writeArgument(Argument arg) {
        return super.writeArgument(arg)
            .insert(0, Space.LINEBR + Space.INDENT);
    }
}
