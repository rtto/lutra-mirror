package xyz.ottr.lutra.bottr.util;

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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO suggest to move this to core.utils

public class ListParser {

    private final char start;
    private final char end;
    private final String sep;

    public ListParser(char start, char end, String sep) {
        this.start = start;
        this.end = end;
        this.sep = sep.trim();
    }

    public List toList(String str) {

        Deque<List> stack = new ArrayDeque<>();

        // if does not contain list markers, we add them.
        if (!str.contains(Character.toString(this.start))
            && !str.contains(Character.toString(this.end))) {
            str = this.start + str + this.end;
        }

        int readSoFarStart = 0;

        List toReturn = null;

        for (int pos = 0; pos < str.length(); pos += 1) {
            char ch = str.charAt(pos);

            if (ch == this.start || ch == this.end) {

                // we have reached the start or finish a list, so we split what ever we have read so far
                // and put on the top of the stack.

                String readSoFar = str.substring(readSoFarStart, pos).trim();
                if (!readSoFar.isEmpty()) {
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Unbalanced lists - \"dangling\" list content: " + readSoFar);
                    } else {

                        List<String> splits = Stream.of(readSoFar.split(this.sep, -1))
                            .map(String::trim)
                            .collect(Collectors
                                .toCollection(ArrayList::new)); // need remove() operation

                        // A list may contain both list and strings as elements, therefore the first and
                        // last elements of a split are empty then there "is a list there", so we remove them.

                        // if the first split is for a list, we remove it
                        if (!splits.isEmpty() && splits.get(0).isEmpty()
                            && readSoFarStart - 1 > 0 // avoid StringOutOfBounds
                            && str.charAt(readSoFarStart - 1) == this.end) {
                            splits.remove(0);
                        }

                        // if the last split is for a list, we remove it
                        if (!splits.isEmpty() && splits.get(splits.size() - 1).isEmpty()
                            && ch == this.start) {
                            splits.remove(splits.size() - 1);
                        }

                        splits.forEach(s -> stack.peek().add(s));
                    }
                }


                if (ch == this.start) {
                    // a new list starts: add it to the current stack top, and set it as new stack top
                    List list = new ArrayList<>();
                    if (!stack.isEmpty()) {
                        stack.peek().add(list);
                    }
                    stack.push(list);

                } else { // must be:  if (ch == this.end) {
                    // the list on the stack top (if it exists) ends, so we pop it.
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Unbalanced lists, more '" + this.end + "' than '" + this.start + "'.");
                    } else {
                        toReturn = stack.pop(); // we return the last list on the stack
                    }

                    if (stack.isEmpty()) {
                        // the stack should be empty only if there is nothing left to do
                        String left = str.substring(pos + 1).trim();
                        if (!left.isEmpty()) {
                            throw new IllegalArgumentException("Unbalanced lists - \"dangling\" list content: " + left);
                        }
                    }
                }
                readSoFarStart = pos + 1; // update
            }
        }
        if (!stack.isEmpty()) {
            throw new IllegalArgumentException("Unbalanced lists, more '" + this.start + "' than '" + this.end + "'.");
        }
        return toReturn;
    }
}
