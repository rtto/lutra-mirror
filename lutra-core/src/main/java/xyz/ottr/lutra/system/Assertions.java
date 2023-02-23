package xyz.ottr.lutra.system;

/*-
 * #%L
 * lutra-core
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum Assertions {
    ;

    // If size is < 0, it means *any* size > 0.
    private static void assertSeverity(MessageHandler messageHandler, Predicate<Message.Severity> severityFilter, int size) {

        var messages = messageHandler.getMessages().stream()
            .filter(message -> severityFilter.test(message.getSeverity()))
            .collect(Collectors.toList());

        if (size == 0) {
            assertThat(messages, is(Collections.EMPTY_LIST));
        } else if (size < 0) {
            assertThat("Expected matching messages, but got none", messages.size() > 0, is(true));
        } else {
            assertThat(messages.size(), is(size));
        }
    }

    public static void noWarnings(MessageHandler messageHandler) {
        assertSeverity(messageHandler, s -> s.isGreaterEqualThan(Message.Severity.WARNING), 0);
    }

    public static void noFatals(MessageHandler messageHandler) {
        assertSeverity(messageHandler, s -> s.isGreaterEqualThan(Message.Severity.FATAL), 0);
    }

    public static void noFatals(ResultConsumer consumer) {
        noFatals(consumer.getMessageHandler());
    }

    public static void noFatals(Result result) {
        noFatals(result.getMessageHandler());
    }

    public static void noErrors(MessageHandler messageHandler) {
        assertSeverity(messageHandler, s -> s.isGreaterEqualThan(Message.Severity.ERROR), 0);
    }

    public static void noErrors(ResultConsumer consumer) {
        noErrors(consumer.getMessageHandler());
    }

    public static void noErrors(Result result) {
        noErrors(result.getMessageHandler());
    }

    public static void atLeast(MessageHandler messageHandler, Message.Severity severity) {
        assertSeverity(messageHandler, s -> s.isGreaterEqualThan(severity), -1);
    }

    public static void atLeast(ResultConsumer consumer, Message.Severity severity) {
        atLeast(consumer.getMessageHandler(), severity);
    }

    public static void atLeast(Result result, Message.Severity severity) {
        atLeast(result.getMessageHandler(), severity);
    }

    /**
     * Checks if the messageHandler contains a message of severity Error or worse
     * which contains the given string.
     * @param messageHandler handler to check
     * @param expected string that at least one error message must contain
     */

    public static void containsMessageFragment(MessageHandler messageHandler, Message.Severity severity, String expected) {
        var test = messageHandler.getMessages().stream()
                .filter(m -> m.getSeverity().equals(severity))
                .map(Message::getMessage)
                .map(s -> s.toLowerCase(Locale.ENGLISH))
                .anyMatch(s -> s.contains(expected.trim().toLowerCase(Locale.ENGLISH)));

        assertThat(test, is(true));
    }

}
