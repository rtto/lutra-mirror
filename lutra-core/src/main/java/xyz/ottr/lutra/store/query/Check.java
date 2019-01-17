package xyz.ottr.lutra.store.query;

/*-
 * #%L
 * lutra-core
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

import java.util.function.Function;
import java.util.stream.Stream;

import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.store.TemplateStore;

public class Check {

    private final Query query;
    private final Function<Tuple, Message> toMessage;

    public Check(Query query, Function<Tuple, Message> toMessage) {
        this.query = query;
        this.toMessage = toMessage;
    }

    public Stream<Message> check(QueryEngine<? extends TemplateStore> engine) {
        return query.eval(engine).map(toMessage).distinct();
    }
}
