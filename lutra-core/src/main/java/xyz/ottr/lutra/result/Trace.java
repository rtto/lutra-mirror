package xyz.ottr.lutra.result;

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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class Trace {

    // Used to construct a printable context, which is stored within a Result
    // object in the parsedFrom-pointer. This is used by a ResultConsumer
    // to give a context to the Messages printed. The default is just the
    // toString-representation of the original object, but one can override
    // this to get a different context object.
    private static Function<Object, ?> toLocation = obj -> {
        if (obj == null) {
            return null;
        }
        String str = obj.toString();
        String prefix = "(" + obj.getClass().getName() + ") ";
        if (str.length() <= 60) {
            return prefix + str;
        } else {
            return prefix + str.substring(0, 60) + "...";
        }
    };

    /** 
     * Sets the argument function to be the method to construct a printable context, which is
     * stored within a Result object in the parsedFrom-pointer.
     * This context is then used by a ResultConsumer to give a context to the Messages printed. 
     * The default is just Object#toString()
     */
    public static void setToLocationFunction(Function<Object, ?> fun) {
        toLocation = fun;
    }

    private final Optional<?> location;
    private final Set<Trace> trace;
    private final List<Message> messages;
    
    private Trace(Optional<?> location, Set<Trace> trace, List<Message> messages) {
        this.location = location;
        this.trace = trace;
        this.messages = messages;
    }
    
    public static Trace from(Result<?> result) {
        Optional<?> location = result.getOptional().map(toLocation);
        return new Trace(location, result.getTraces(), result.getMessages());
    }
    
    public boolean hasLocation() {
        return this.location.isPresent();
    }
    
    public Object getLocation() {
        return this.location.get();
    }

    public Set<Trace> getTrace() {
        return trace;
    }

    public List<Message> getMessages() {
        return messages;
    }

}
