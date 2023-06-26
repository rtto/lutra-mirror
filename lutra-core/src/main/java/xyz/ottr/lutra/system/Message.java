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
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import xyz.ottr.lutra.Space;

@EqualsAndHashCode
public class Message {

    @Setter private static boolean printStackTrace = true;

    public enum Severity {
        // the order of enums defines the result of compareTo method.
        INFO,
        WARNING,
        ERROR,
        FATAL;

        public boolean isGreaterThan(Severity other) {
            return this.compareTo(other) > 0;
        }

        public boolean isLessThan(Severity other) {
            return this.compareTo(other) < 0;
        }

        public boolean isGreaterEqualThan(Severity other) {
            return this.compareTo(other) >= 0;
        }

        public boolean isLessEqualThan(Severity other) {
            return this.compareTo(other) <= 0;
        }

        public static Severity least() {
            return INFO;
        }

        public static Severity greatest() {
            return FATAL;
        }
    }

    @Getter private final Severity severity;
    @Getter private final String message;
    private final StackTraceElement[] stackTrace;

    public Message(Severity severity, String message, StackTraceElement[] stackTrace) {
        this.severity = severity;
        this.message = message;
        this.stackTrace = stackTrace.clone();
    }

    public Message(Severity severity, String message) {
        this(severity, message, Thread.currentThread().getStackTrace());
    }

    public static Message fatal(String msg) {
        return new Message(Severity.FATAL, msg);
    }

    public static Message fatal(Exception e) {
        return new Message(Severity.FATAL, e.getMessage(), e.getStackTrace());
    }

    public static Message fatal(String message, Exception e) {
        return new Message(Severity.FATAL, message + Space.LINEBR + e.getMessage(), e.getStackTrace());
    }

    public static Message error(String msg) {
        return new Message(Severity.ERROR, msg);
    }

    public static Message error(Exception e) {
        return new Message(Severity.ERROR, e.getMessage(), e.getStackTrace());
    }

    public static Message error(String message, Exception e) {
        return new Message(Severity.ERROR, message + Space.LINEBR + e.getMessage(), e.getStackTrace());
    }

    public static Message warning(String msg) {
        return new Message(Severity.WARNING, msg);
    }

    public static Message info(String msg) {
        return new Message(Severity.INFO, msg);
    }

    public void log(Logger log) {
        switch (this.severity) {
            case WARNING:
                log.warn(this.message);
                break;
            case INFO:
                log.trace(this.message);
                break;
            default: // covers both ERROR and FATAL
                log.error(this.message);
        }
    }

    @Override
    public String toString() {

        String output = "[" + this.severity.name() + "] " + this.message;

        if (printStackTrace) {
            output += printStackTrace();
        }

        return output;
    }

    private String printStackTrace() {
        return
            System.lineSeparator()
            + Arrays.stream(this.stackTrace)
                // filter out system methods of different kinds:
                .filter(s -> !s.getClassName().startsWith("java.util"))
                .filter(s -> !s.getClassName().startsWith("java.lang.Thread"))
                .filter(s -> !s.getClassName().startsWith("java.lang.reflect"))
                .filter(s -> !s.getClassName().startsWith("jdk.internal.reflect"))
                .filter(s -> !s.getClassName().startsWith("org.junit"))
                .filter(s -> !s.getClassName().startsWith("org.apache.maven.surefire"))
                .filter(s -> !s.getClassName().startsWith("xyz.ottr.lutra.system"))
                .map(stackTraceElement -> "\t" + stackTraceElement)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
