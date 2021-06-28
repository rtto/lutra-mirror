package xyz.ottr.lutra.store.checks;

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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import xyz.ottr.lutra.store.Query;
import xyz.ottr.lutra.store.Tuple;
import xyz.ottr.lutra.store.graph.QueryEngineNew;
import xyz.ottr.lutra.system.Message;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Check {

    private final Query query;
    private final Function<Tuple, Message> toMessage;

    public Stream<Message> check(QueryEngineNew engine) {
        return this.query.eval(engine).map(this.toMessage).distinct();
    }
}
