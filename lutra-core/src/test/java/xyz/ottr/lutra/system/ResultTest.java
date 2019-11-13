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

import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class ResultTest {

    @Test
    public void testZipErrorRetainment1() {

        Message errorA = Message.error("Error A");
        Message errorB = Message.error("Error B");

        Result a = Result.empty(errorA);
        testEqualMessages(a, errorA);

        Result b = Result.empty(errorB);
        testEqualMessages(b, errorB);

        Result ab = Result.zip(a, b, (x,y) -> "noop");
        testEqualMessages(ab, errorA, errorB);
    }


    @Test
    public void testZipErrorRetainment2() {

        Message errorA = Message.error("Error A");
        Message errorB = Message.error("Error B");

        Result root = Result.of("");

        Result a = root.flatMap(_x -> Result.empty(errorA));
        testEqualMessages(a, errorA);

        Result b = root.flatMap(_x -> Result.empty(errorB));
        testEqualMessages(b, errorB);

        Result ab = Result.zip(a, b, (x,y) -> "noop");
        testEqualMessages(ab, errorA, errorB);
    }

    private void testEqualMessages(Result r, Message... messages) {
        Assert.assertThat(getSortedMessages(r.getAllMessages()), is(getSortedMessages(Arrays.asList(messages))));
    }

    private List<String> getSortedMessages(Collection<Message> messages) {
        return messages.stream()
            .map(Message::getMessage)
            .map(String::toString)
            .sorted()
            .collect(Collectors.toList());
    }
    
}
