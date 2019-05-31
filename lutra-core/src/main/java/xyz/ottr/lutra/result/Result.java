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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
//import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Result<E> {

    // Used to construct a printable context, which is stored within a Result
    // object in the parsedFrom-pointer. This is used by a ResultConsumer
    // to give a context to the Messages printed. The default is just the
    // toString-representation of the original object, but one can override
    // this to get a different context object.
    private static Function<Object, ?> deriveContext = obj -> {
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
    public static void setDeriveContextFunction(Function<Object, ?> fun) {
        deriveContext = fun;
    }

    private Result<?> parsedFrom;
    private final List<Message> messages;
    private final Optional<E> result;

    // Used to determine of this is a context Result (deriveContext has been applied)
    private boolean isContext = false; 

    private Result(Optional<E> result, List<Message> messages, Result<?> parsedFrom) {
        this.result = result;
        this.messages = messages;
        addParsedFrom(parsedFrom);
    }

    private Result(Optional<E> result) {
        this(result, new LinkedList<>(), null);
    }

    private Result(Optional<E> result, Message msg, Result<?> parsedFrom) {
        this(result, new LinkedList<>(), parsedFrom);
        this.messages.add(msg);
    }

    private Result(Result<E> other, Result<?> newParsedFrom) {
        this(other == null ? Optional.empty() : other.result,
            other == null ? new LinkedList<>() : other.messages,
            other == null ? null : other.parsedFrom);
        addParsedFrom(newParsedFrom);
    }

    /**
     * @see Optional#of(R)
     */
    public static <R> Result<R> of(R val) {
        return new Result<R>(Optional.of(val));
    }

    /**
     * Same as #of, but sets parsedFrom to argument Result.
     */
    public static <R> Result<R> of(R val, Result<?> parsedFrom) {
        return new Result<R>(Optional.of(val), new LinkedList<>(), parsedFrom);
    }

    /**
     * @see Optional#ofNullable(R)
     */
    public static <R> Result<R> ofNullable(R val) {
        return new Result<R>(Optional.ofNullable(val));
    }

    /**
     * Same as #ofNullable, but sets parsedFrom to argument Result.
     */
    public static <R> Result<R> ofNullable(R val, Result<?> parsedFrom) {
        return new Result<R>(Optional.ofNullable(val), new LinkedList<>(), parsedFrom);
    }

    /**
     * @see Optional#empty()
     */
    public static <R> Result<R> empty() {
        return new Result<R>(Optional.empty());
    }

    /**
     * Returns an empty Result, but sets argument to be the Result's
     * parsed from.
     * 
     * @param parsedFrom
     *      A Result which the returned Result is resulting from.
     *
     * @return
     *      An empty Result
     */
    public static <R> Result<R> empty(Result<?> parsedFrom) {
        return new Result<R>(Optional.empty(), new LinkedList<>(), parsedFrom);
    }

    /**
     * Returns an empty Result, with argument as Message (typically an error giving
     * the reason for this Result being empty).
     * 
     * @param msg
     *      A Message describing the reason for the emptiness of this Result
     *
     * @return
     *      An empty Result
     */
    public static <R> Result<R> empty(Message msg) {
        return new Result<R>(Optional.empty(), msg, null);
    }

    /**
     * Returns an empty Result, which is a combination of Result#empty(Result)
     * and Result#empty(Message).
     * 
     * @param parsedFrom
     *      A Result which the returned Result is resulting from.
     *
     * @param msg
     *      A Message describing the reason for the emptiness of this Result
     *
     * @return
     *      An empty Result
     */
    public static <R> Result<R> empty(Message msg, Result<?> parsedFrom) {
        return new Result<R>(Optional.empty(), msg, parsedFrom);
    }

    /**
     * Returns an empty Result, with argument as Message-s (typically errors giving
     * the reasons for this Result being empty).
     * 
     * @param msgs
     *      A list of Message-s describing the reasons for the emptiness of this Result
     *
     * @return
     *      An empty Result
     */
    public static <R> Result<R> empty(List<Message> msgs) {
        return new Result<R>(Optional.empty(), msgs, null);
    }

    protected Result<?> deriveContext() {
        if (!this.isContext) {
            Result<?> context = this.map(deriveContext);
            context.isContext = true;
            return context;
        } else {
            return this;
        }
    }

    private void addParsedFrom(Result<?> other) {
        if (other == null) {
            return;
        }
        Result<?> context = other.deriveContext();
        Result<?> r = this;
        while (r.parsedFrom != null && !r.parsedFrom.equals(context)) {
            r = r.parsedFrom;
        }
        r.parsedFrom = context;
    }

    /**
     * Applies f to the values contained in argument results if both are present, and
     * returns empty Result if not, but keeps both argument Result's messages.
     */
    public static <A,B,R> Result<R> zip(Result<A> a, Result<B> b, BiFunction<A,B,R> f) {
        // TODO: Implement using flatMap to get parsedFrom pointers instead of addMessage
        R r = a.isPresent() && b.isPresent() ? f.apply(a.get(), b.get()) : null;
        Result<R> res = Result.ofNullable(r);
        res.addParsedFrom(a);
        res.addParsedFrom(b);
        return res;
    }

    /**
     * Applies f to the values contained in argument results (where non-present values is treated as null),
     * and keeps both argument Result's messages.
     */
    public static <A,B,R> Result<R> zipNullables(Result<A> a, Result<B> b, BiFunction<A,B,R> f) {
        // TODO: Implement using flatMap to get parsedFrom pointers instead of addMessage
        A aval = a.isPresent() ? a.get() : null;
        B bval = b.isPresent() ? b.get() : null;
        R r = f.apply(aval, bval);
        Result<R> res = Result.ofNullable(r);
        res.addParsedFrom(a);
        res.addParsedFrom(b);
        return res;
    }

    /**
     * Unpacks all Results in argument list, and packs the unpacked
     * objects in a list, which is empty if not all
     * results of the original list were present, inside one Result containing all
     * the original Results messages.
     */
    public static <R> Result<List<R>> aggregate(List<Result<R>> lst) {
        return new ResultStream<>(lst.stream())
            .aggregate()
            .map(terms -> terms.collect(Collectors.toList()));
    }

    /**
     * Unpacks all Results in argument set, and packs the unpacked
     * objects in a set, which is empty if not all
     * results of the original set were present, inside one Result containing all
     * the original Results messages.
     */
    public static <R> Result<Set<R>> aggregate(Set<Result<R>> lst) {
        return new ResultStream<>(lst.stream())
            .aggregate()
            .map(terms -> terms.collect(Collectors.toSet()));
    }

    /**
     * Unpacks all Results in argument list, and packs the unpacked
     * objects in a list, which is present even if not all
     * results of the original list were present, inside one Result containing all
     * the original Results messages.
     */
    public static <R> Result<List<R>> aggregateNullable(List<Result<R>> lst) {
        return new ResultStream<>(lst.stream())
            .aggregateNullable()
            .map(terms -> terms.collect(Collectors.toList()));
    }

    /**
     * Unpacks all Results in argument set, and packs the unpacked
     * objects in a set, which is present even if not all
     * results of the original set were present, inside one Result containing all
     * the original Results messages.
     */
    public static <R> Result<Set<R>> aggregateNullable(Set<Result<R>> lst) {
        return new ResultStream<>(lst.stream())
            .aggregateNullable()
            .map(terms -> terms.collect(Collectors.toSet()));
    }

    /**
     * @see Optional#isPresent()
     */
    public boolean isPresent() {
        return this.result.isPresent();
    }

    /**
     * @see Optional#ifPresent(Consumer)
     */
    public void ifPresent(Consumer<? super E> consumer) {
        result.ifPresent(consumer);
    }

    /**
     * @see Optional#get()
     */
    public E get() {
        return this.result.get();
    }

    /**
     * Returns the list of Message-s on this Result.
     * To retrieve all Message-s including Message-s on the Result-s
     * this is parsed from, use a ResultConsumer or Result#getAllMessages().
     */
    public List<Message> getMessages() {
        return this.messages;
    }

    /**
     * Returns the list of all Message-s on this Result and the Results is was parsed from.
     * To retrieve only Message-s on this Result, use Result#getMessages().
     */
    public List<Message> getAllMessages() {
        ResultConsumer<E> msgs = new ResultConsumer<>();
        msgs.accept(this);
        return msgs.getMessageHandler().getMessages();
    }

    /**
     * Returns the Result which this was derived from, via some form of computation.
     */
    public Result<?> getParsedFrom() {
        return this.parsedFrom;
    }

    /**
     * Similar to Optional#filter(Predicate), but result retains Message-s and the pointer
     * to the parsed from Result.
     */
    public Result<E> filter(Predicate<E> pred) {
        return new Result<>(this.result.filter(pred), this.messages, this.parsedFrom);
    }

    /**
     * Similar to Optional#map(Function), but result retains Message-s and the pointer
     * to the parsed from Result.
     */
    public <R> Result<R> map(Function<? super E, ? extends R> fun) {
        return new Result<R>(this.result.map(fun), this.messages, this.parsedFrom);
    }

    /**
     * Similar to Optional#flatMap(Function), but result's parsed from becomes this.
     */
    public <R> Result<R> flatMap(Function<? super E, ? extends Result<R>> fun) {

        Result<R> newResult = result.isPresent() ? fun.apply(result.get()) : Result.empty();
        return new Result<R>(newResult, this);
    }

    /**
     * If this is present, returns the result of mapping argument function over this with
     * every Result in resulting stream having this as parsed from, otherwise
     * returns a ResultStream containing a single empty Result with this as parsed from.
     */
    public <R> ResultStream<R> mapToStream(Function<? super E, ? extends ResultStream<R>> fun) {
        Result<ResultStream<R>> mapped = this.map(fun);
        if (mapped.isPresent()) {
            // Return a stream of results with parsedFrom pointers to this
            // TODO: Fix loss of messages if mapped contains an empty ResultStream
            return mapped.get().map(r -> new Result<>(r, this));
        } else {
            // Return a stream of an empty Result, containing parsedFrom pointer to this
            return ResultStream.of(Result.empty(this));
        }
    }

    /**
     * Adds the argument Message to this' list of Message-s.
     */
    public void addMessage(Message newMsg) {
        this.messages.add(newMsg);
    }

    /**
     * Adds the argument Message-s to this' list of Message-s.
     */
    public void addMessages(Collection<Message> moreMsgs) {
        this.messages.addAll(moreMsgs);
    }

    @Override
    public String toString() {
        return (this.result.isPresent()
                ? "Result(" + this.result.get().toString() + ")"
                : "Empty") + this.messages.toString();
    }

    public String getContext() {
        if (isContext) {
            return get().toString();
        } else {
            return deriveContext.apply(get()).toString();
        }
    }

    /**
     * Returns a new function that is the composition of the two argument functions, under
     * flatMap, that is, the function applies the first to the argument then flatMaps
     * the second over the result.
     */
    public static <A,B,C> Function<A, Result<C>> flatMapCompose(Function<A, Result<B>> fun1,
            Function<? super B, Result<C>> fun2) {
        return a -> fun1.apply(a).flatMap(fun2);
    }
}
