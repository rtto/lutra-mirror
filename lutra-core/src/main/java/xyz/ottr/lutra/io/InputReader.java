package xyz.ottr.lutra.io;

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
import xyz.ottr.lutra.system.ResultStream;

/**
 * An <code>InputReader</code> should come before a reader 
 * ({@link xyz.ottr.lutra.parser.TemplateParser} or 
 * {@link xyz.ottr.lutra.parser.InstanceParser}) to translate 
 * some pointer to an input (e.g. a filename or URL) to an 
 * output consumable by a reader. An <code>InputReader<T, R></code> 
 * can be paired with a parser, e.g. a <code>InstanceParser<R, O></code>
 * in order to produce a reader, e.g. <code>InstanceReader<T, O></code>.
 * Thus, for any parser consuming something of type <code>R</code> one 
 * needs to make an <code>InputReader</code> with output of type <code>R</code>.
 * 
 * @param <T> The type of a pointer to an input to a reader (e.g. <code>String</code> for filenames)
 * @param <R> The type of the result of the consumption of the input to a representation consumable by a parser.
 */
public interface InputReader<T, R> extends Function<T, ResultStream<R>> {

}
