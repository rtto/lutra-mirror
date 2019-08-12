package xyz.ottr.lutra.result;

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
