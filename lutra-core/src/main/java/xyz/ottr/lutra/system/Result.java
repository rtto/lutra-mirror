package xyz.ottr.lutra.system;


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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// TODO: should we move some methods to UtilityClasses?

public class Result<E> {

    private final Trace trace;
    private final Optional<E> result;

    private Result(Optional<E> result) {
        this.result = result;
        this.trace = new Trace(result);
    }

    private Result(Optional<E> result, Collection<Message> messages) {
        this(result);
        this.trace.addMessages(messages);
    }
    
    private Result(Optional<E> result, Trace trace) {
        this(result);
        this.trace.addTrace(trace);
    }
    
    private Result(Optional<E> result, Collection<Message> messages, Trace trace) {
        this(result, messages);
        this.trace.addTrace(trace);
    }

    /**
     * @see Optional#of(Object)
     */
    public static <R> Result<R> of(R val) {
        return new Result<>(Optional.of(val));
    }

    /**
     * Same as #of, but sets parsedFrom to argument Result.
     */
    public static <R> Result<R> of(R val, Result<?> parsedFrom) {
        return new Result<>(Optional.of(val), parsedFrom.trace);
    }

    /**
     * @see Optional#ofNullable(Object)
     */
    public static <R> Result<R> ofNullable(R val) {
        return new Result<>(Optional.ofNullable(val));
    }

    /**
     * Same as #ofNullable, but sets parsedFrom to argument Result.
     */
    public static <R> Result<R> ofNullable(R val, Result<?> parsedFrom) {
        return new Result<>(Optional.ofNullable(val), parsedFrom.trace);
    }
    
    public static <R> Set<Result<R>> lift(Set<R> rs) {
        return ResultStream.innerOf(rs).collect(Collectors.toSet());
    }

    public static <R> List<Result<R>> lift(List<R> rs) {
        return ResultStream.innerOf(rs).collect(Collectors.toList());
    }

    /**
     * @see Optional#empty()
     */
    public static <R> Result<R> empty() {
        return new Result<>(Optional.empty());
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
        return new Result<>(Optional.empty(), parsedFrom.trace);
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
        return new Result<>(Optional.empty(), List.of(msg));
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
        return new Result<>(Optional.empty(), List.of(msg), parsedFrom.trace);
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
        return new Result<>(Optional.empty(), msgs);
    }

    /**
     * Returns a new empty Result if input is null and is otherwise the identity on input.
     */
    public static <R> Result<R> nullToEmpty(Result<R> result) {
        return Objects.requireNonNullElse(result, empty());
    }

    public static <R> Result<R> nullToEmpty(Result<R> result, Message message) {
        return Objects.requireNonNullElse(result, empty(message));
    }

    public static <R> Result<R> fatal(String msg) {
        return empty(Message.fatal(msg));
    }

    public static <R> Result<R> fatal(String msg, Exception ex) {
        return empty(Message.fatal(msg, ex));
    }

    public static <R> Result<R> error(String msg) {
        return empty(Message.error(msg));
    }

    public static <R> Result<R> error(String msg, Exception ex) {
        return empty(Message.error(msg, ex));
    }

    public static <R> Result<R> warning(String msg) {
        return empty(Message.warning(msg));
    }

    public static <R> Result<R> info(String msg) {
        return empty(Message.info(msg));
    }

    public static boolean allIsPresent(Result<?>... results) {
        return Arrays.stream(results).allMatch(Result::isPresent);
    }

    /**
     * Returns an empty system with this system as trace and with the argument message.
     * 
     * @param msg
     *      Message to attach to resulting empty Result
     * @return 
     *      Returns an empty Result with this as trace and argument Message
     */
    public Result<E> fail(Message msg) {
        if (this.isPresent()) {
            return this.flatMap(v -> Result.empty(msg));
        } else {
            this.addMessage(msg);
            return this;
        }
    }
    
    public Result<E> addToTrace(Result<?> other) {
        if (other != null) {
            this.trace.addTrace(other.trace);
        }
        return this;
    }

    public Result<E> addToTrace(Trace other) {
        if (other != null) {
            this.trace.addTrace(other);
        }
        return this;
    }

    /**
     * Add a system to this system by bi-consuming this and the other system. Adds the other system to the
     * context of this system.
     * @see Result#addResult(Result, BiConsumer)
     */
    public <B> void addResult(Result<B> other, BiConsumer<? super E, ? super B> consumer) {
        if (this.result.isPresent() && other.isPresent()) {
            consumer.accept(this.result.get(), other.result.get());
        }
        this.trace.addDirectTrace(other.trace);
    }

    /**
     * Applies f to the values contained in argument results if both are present, and
     * returns empty Result if not, but keeps both argument Result's messages.
     */
    public static <A,B,R> Result<R> zip(Result<A> a, Result<B> b, BiFunction<A,B,R> f) {
        return conditionalZip(a, b, (x,y) -> x.isPresent() && y.isPresent(), f);
    }

    /**
     * Applies f to the values contained in argument results (where non-present values is treated as null),
     * and keeps both argument Result's messages.
     */
    public static <A,B,R> Result<R> zipNullables(Result<A> a, Result<B> b, BiFunction<A,B,R> f) {
        return conditionalZip(a, b, (x,y) -> true, f);
    }


    /**
     * Applies f to the values (possibly null) contained in argument results if the condition is met, and
     * returns an empty Result if not, but keeps both argument Result's messages.
     */
    public static <A,B,R> Result<R> conditionalZip(Result<A> a, Result<B> b, BiPredicate<Result<A>, Result<B>> p, BiFunction<A, B, R> f) {
        Result<R> res = p.test(a, b)
            ? Result.ofNullable(f.apply(a.orElse(null), b.orElse(null)))
            : Result.empty();
        Trace fork = Trace.fork(a.trace, b.trace);
        res.addToTrace(fork);
        return res;
    }


    /**
     * Applies f to the value contained in the argument system if it is present, and
     * returns empty Result if not.
     */
    public static <A,B,R> Result<R> apply(Result<A> a, Function<A, Result<R>> f) {
        return a.flatMap(f);
    }

    /**
     * Applies f to the values contained in argument results if both are present, and
     * returns empty Result if not.
     */
    public static <A,B,R> Result<R> apply(Result<A> a, Result<B> b, BiFunction<A, B, Result<R>> f) {
        return flatten(zip(a, b, f));
    }
    
    public static <R> Result<R> flatten(Result<Result<R>> r) {
        return r.flatMap(x -> x);
    }

    /**
     * Consumes the values contained in argument results if both are present.
     */
    public static <A,B> void consume(Result<A> a, Result<B> b, BiConsumer<A,B> f) {
        if (a.isPresent() && b.isPresent()) {
            f.accept(a.get(), b.get());
        }
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
     * @see Optional#isEmpty()
     */
    public boolean isEmpty() {
        return this.result.isEmpty();
    }

    /**
     * @see Optional#ifPresent(Consumer)
     */
    public void ifPresent(Consumer<? super E> consumer) {

        this.result.ifPresent(consumer);
    }

    /**
     * @see Optional#get()
     */
    public E get() {

        // overwrite NoSuchElementException to contain error message trace.
        if (this.result.isEmpty()) {
            throw new java.util.NoSuchElementException("No value present. Message trace: " + this.trace.getMessages());
        }

        return this.result.get();
    }
    
    protected Optional<E> getOptional() {
        return this.result;
    }

    /**
     * @see Optional#orElse(Object)
     */
    public E orElse(E other) {
        return this.result.orElse(other);
    }

    /**
     * @see Optional#or(Supplier)
     */
    public Result<E> or(Supplier<? extends Result<? extends E>> supplier) {
        Objects.requireNonNull(supplier);
        if (this.isPresent()) {
            return this;
        } else {
            Result<E> r = (Result<E>) supplier.get();
            return Objects.requireNonNull(r);
        }
    }

    protected Trace getTrace() {
        return this.trace;
    }

    /**
     * Returns the list of Message-s on this Result.
     * To retrieve all Message-s including Message-s on the Result-s
     * this is parsed from, use a ResultConsumer or Result#getAllMessages().
     */
    protected Collection<Message> getMessages() {
        return this.trace.getMessages();
    }

    /**
     * Returns the list of all Message-s on this Result and the Results is was parsed from.
     * To retrieve only Message-s on this Result, use Result#getMessages().
     */
    public List<Message> getAllMessages() {
        return getMessageHandler().getMessages();
    }

    public MessageHandler getMessageHandler() {
        var messageHandler = new MessageHandler();
        messageHandler.add(this);
        return messageHandler;
    }

    /**
     * Similar to Optional#filter(Predicate), but system retains Message-s and the pointer
     * to the parsed from Result.
     */
    public Result<E> filter(Predicate<E> pred) {
        return new Result<>(this.result.filter(pred), this.trace);
    }

    /**
     * Similar to Optional#map(Function), but system retains Message-s and the pointer
     * to the parsed from Result.
     */
    public <R> Result<R> map(Function<? super E, ? extends R> fun) {
        return new Result<>(this.result.map(fun), this.trace);
    }

    /**
     * Similar to Result#map(Function), but returns the Result of orElse if this' system is empty -- with a
     * pointer to this.
     */
    public <R> Result<R> mapOrElse(Function<? super E, ? extends R> fun, R orElse) {
        return this.isPresent()
            ? this.map(fun)
            : Result.of(orElse, this);
    }

    /**
     * Similar to Optional#flatMap(Function), but system's parsed from becomes this.
     */
    public <R> Result<R> flatMap(Function<? super E, ? extends Result<R>> fun) {
        return flatMapOrElse(fun, Result.empty());
    }

    /**
     * Similar to Result#flatMap(Function), but system becomes orElse if this' system is empty.
     */
    public <R> Result<R> flatMapOrElse(Function<? super E, ? extends Result<R>> fun, Result<R> orElse) {

        Result<R> newResult = this.result.isPresent() ? fun.apply(this.result.get()) : orElse;
        return newResult.addToTrace(this);
    }

    /**
     * If this is present, returns the system of mapping argument function over this with
     * every Result in resulting stream having this as parsed from, otherwise
     * returns a ResultStream containing a single empty Result with this as parsed from.
     */
    public <R> ResultStream<R> mapToStream(Function<? super E, ? extends ResultStream<R>> fun) {
        Result<ResultStream<R>> mapped = this.map(fun);
        if (mapped.isPresent()) {
            // Return a stream of results with parsedFrom pointers to this
            // TODO: Fix loss of messages if mapped contains an empty ResultStream
            return mapped.get().filter(Objects::nonNull).map(r -> r.addToTrace(this));
        } else {
            // Return a stream of an empty Result, containing parsedFrom pointer to this
            return ResultStream.of(Result.empty(this));
        }
    }

    /**
     * Adds the argument Message to this' list of Message-s.
     */
    public void addMessage(Message newMsg) {
        this.trace.addMessage(newMsg);
    }

    public void addFatal(String newMsg) {
        this.trace.addMessage(Message.fatal(newMsg));
    }

    public void addError(String newMsg) {
        this.trace.addMessage(Message.error(newMsg));
    }

    public void addWarning(String newMsg) {
        this.trace.addMessage(Message.warning(newMsg));
    }

    public void addInfo(String newMsg) {
        this.trace.addMessage(Message.info(newMsg));
    }

    /**
     * Adds the argument Message-s to this' list of Message-s.
     */
    public void addMessages(Collection<Message> moreMsgs) {
        this.trace.addMessages(moreMsgs);
    }

    @Override
    public String toString() {
        return (this.result.isPresent()
                ? "Result(" + this.result.get() + ")"
                : "Empty") + this.trace.getMessages();
    }

    /**
     * Returns a new function that is the composition of the two argument functions, under
     * flatMap, that is, the function applies the first to the argument then flatMaps
     * the second over the system.
     */
    public static <A,B,C> Function<A, Result<C>> flatMapCompose(Function<A, Result<B>> fun1,
            Function<? super B, Result<C>> fun2) {
        return a -> fun1.apply(a).flatMap(fun2);
    }
}
