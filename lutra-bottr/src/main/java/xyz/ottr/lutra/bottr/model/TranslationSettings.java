package xyz.ottr.lutra.bottr.model;

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

import lombok.Builder;
import lombok.Getter;
import xyz.ottr.lutra.bottr.util.TermFactory;
import xyz.ottr.lutra.model.Term;

@Getter
@Builder
public class TranslationSettings {

    @Builder.Default private final Term nullValue = TermFactory.createNone().get();

    @Builder.Default private final String labelledBlankPrefix = "_:";

    @Builder.Default private final String listSep = ",";
    @Builder.Default private final char listStart = '(';
    @Builder.Default private final char listEnd = ')';
}
