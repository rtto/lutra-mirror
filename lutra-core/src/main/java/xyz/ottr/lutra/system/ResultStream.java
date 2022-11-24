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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ResultStream<E> {

    private final Stream<Result<E>> results;

    //
    // CONSTRUCTORS
    //

    public ResultStream(Stream<Result<E>> results) {
        this.results = results;
    }

    //
    // STATIC CONSTRUCTORS
    //

    /**
     * @see Stream#empty()
     */
    public static <R> ResultStream<R> empty() {
        return new ResultStream<>(Stream.empty());
    }

    /**
     * Returns a ResultStream containing a single element
     * which is the result of applying Result#ofNullable(R)
     * to argument.
     */
    public static <R> ResultStream<R> ofNullable(R val) {
        return of(Result.ofNullable(val));
    }


    /**
     * @see Stream#of(Object)
     */
    public static <R> ResultStream<R> of(Result<R> r) {
        return new ResultStream<>(Stream.of(r));
    }


    public static <R> ResultStream<R> of(Stream<Result<R>> results) {
        return new ResultStream<R>(results);
    }

    public static <R> ResultStream<R> of(Collection<Result<R>> results) {
        return of(results.stream());
    }

    public static <E> ResultStream<E> of(Iterator<Result<E>> iterator) {
        Stream<Result<E>> stream = StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
            false);
        return of(stream);
    }

    /**
     * Returns a ResultStream containing a single element
     * which is a Result containing the argument.
     */
    public static <R> ResultStream<R> innerOf(R val) {
        return of(Result.of(val));
    }

    /**
     * Returns a ResultStream consisting of one Result per element
     * in argument Collection.
     */
    public static <R> ResultStream<R> innerOf(Collection<R> col) {
        return innerOf(col.stream());
    }

    /**
     * Returns a ResultStream consisting of one Result per element
     * in argument Stream.
     */
    public static <R> ResultStream<R> innerOf(Stream<R> stream) {
        return new ResultStream<>(stream.map(Result::of));
    }

    /**
     * Returns a ResultStream consisting of one Result per element
     * in argument iterator.
     */
    public static <R> ResultStream<R> innerOf(Iterator<R> iterator) {
        Stream<R> stream = StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
            false);
        return innerOf(stream);
    }

    /**
     * Returns a ResultStream consisting of one Result per element
     * in argument array.
     */
    public static <R> ResultStream<R> innerOf(R[] array) {
        return innerOf(Arrays.stream(array));
    }

    //
    // METHODS "COPIED" FROM Stream
    //

    /**
     * Returns the underlying stream of Results.
     */
    public Stream<Result<E>> getStream() {
        return this.results;
    }

    /**
     * @see Stream#parallel()
     */
    public ResultStream<E> parallel() {
        return new ResultStream<>(this.results.parallel());
    }

    /**
     * @see Stream#sequential()
     */
    public ResultStream<E> sequential() {
        return new ResultStream<>(this.results.sequential());
    }

    /**
     * @see Stream#concat(Stream, Stream)
     */
    public static <R> ResultStream<R> concat(ResultStream<R> a, ResultStream<R> b) {
        return new ResultStream<>(Stream.concat(a.results, b.results));
    }

    /**
     * Returns a new ResultStream which contains all Results of
     * the list of argument streams.
     */
    public static <R> ResultStream<R> concat(List<ResultStream<R>> streams) {
        return streams.stream().reduce(empty(), ResultStream::concat);
    }

    /**
     * @see Stream#peek(Consumer)
     */
    public ResultStream<E> peek(Consumer<? super Result<E>> consumer) {
        return new ResultStream<>(this.results.peek(consumer));
    }

    /**
     * @see Stream#findFirst()
     */
    public Result<E> findFirst() {
        return this.results.findFirst().orElse(Result.empty());
    }

    /**
     * @see Stream#filter(Predicate)
     */
    public ResultStream<E> filter(Predicate<Result<E>> pred) {
        return new ResultStream<>(this.results.filter(pred));
    }

    /**
     * @see Stream#collect(Collector)
     */
    public <R, A> R collect(Collector<Result<E>, A, R> collector) {
        return this.results.collect(collector);
    }

    /**
     * @see Stream#forEach(Consumer)
     */
    public void forEach(Consumer<? super Result<E>> consumer) {
        this.results.forEach(consumer);
    }

    // MAPs

    /**
     * @see Stream#map(Function)
     */
    public <R> ResultStream<R> map(Function<? super Result<E>, ? extends Result<R>> f) {
        return new ResultStream<>(this.results.map(f));
    }

    /**
     * @see Stream#flatMap(Function)
     */
    public <R> ResultStream<R> flatMap(Function<? super Result<E>, ? extends ResultStream<R>> f) {
        return new ResultStream<>(this.results.flatMap(f.andThen(ResultStream::getStream)));
    }

    /**
     * Returns a new ResultStream where the argument function is flatMap'ed
     * over each Result in this stream.
     */
    public <R> ResultStream<R> mapFlatMap(Function<? super E, ? extends Result<R>> f) {
        return new ResultStream<>(this.results.map(r -> r.flatMap(f)));
    }

    //
    // "INNER" METHODS
    //

    /**
     * Similar to #filter(Predicate) except that the filter
     * is applied to the inner values, instead of to each Result.
     */
    public ResultStream<E> innerFilter(Predicate<E> pred) {
        return new ResultStream<>(this.results.filter(res ->
            res.isPresent() && pred.test(res.get())));
    }

    /**
     * Applies the argument Consumer to each value
     * of each present Result in this stream.
     */
    public void innerForEach(Consumer<? super E> consumer) {
        this.results.forEach(r -> r.ifPresent(consumer));
    }

    /**
     * Returns a new ResultStream consisting of all this' Results,
     * but with argument function mapped over each.
     */
    public <R> ResultStream<R> innerMap(Function<? super E, R> f) {
        return new ResultStream<>(this.results.map(r -> r.map(f)));
    }


    /**
     * Returns a new ResultStream consisting of the concatenation of all streams
     * resulting from applying each Result's mapToStream on argument function.
     */
    public <R> ResultStream<R> innerFlatMap(Function<? super E, ? extends ResultStream<R>> f) {
        return new ResultStream<>(this.results.flatMap(r -> r.mapToStream(f).results));
    }

    /**
     * Similar to Result#flatMapCompose(Function), that is, returns a new function
     * which applies the first function to the argument, and then innerFlatMap-s the
     * second function over the result.
     */
    public static <A,B,C> Function<A, ResultStream<C>> innerFlatMapCompose(Function<A, ResultStream<B>> f,
                                                                           Function<? super B, ResultStream<C>> g) {
        return a -> f.apply(a).innerFlatMap(g);
    }

    /**
     * Unpacks all Results in stream, and packs the unpacked
     * objects in a stream, which is present even if not all
     * results of the original stream were present, inside one Result containing all
     * the original Results messages.
     */
    public Result<Stream<E>> aggregateNullable() {

        Stream.Builder<E> unpacked = Stream.builder();
        List<Trace> traces = new LinkedList<>();
        this.results.forEach(r -> {
            r.ifPresent(unpacked);
            traces.add(r.getTrace()); 
        });

        Result<Stream<E>> res = Result.of(unpacked.build());
        res.addToTrace(Trace.fork(traces));
        return res;
    }

    /**
     * Unpacks all Results in stream, and packs the unpacked
     * objects in a possible stream, which is empty if not all
     * results of the original stream were present, inside one Result containing all
     * the original Results messages.
     */
    // TODO implement this as a Collector?
    public Result<Stream<E>> aggregate() {
        
        List<E> unpacked = new LinkedList<>();

        List<Trace> traces = new LinkedList<>();
        for (Result<E> r : this.results.collect(Collectors.toList())) {
            if (unpacked != null && r != null && r.isPresent()) {
                unpacked.add(r.get());
            } else {
                // No elements should be kept
                unpacked = null;
            }
            if (r != null) {
                traces.add(r.getTrace());
            }
        }

        Result<Stream<E>> res = Result.ofNullable(unpacked).map(Collection::stream);
        res.addToTrace(Trace.fork(traces));
        return res;
    }
    

}
