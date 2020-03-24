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
import org.slf4j.Logger;

@EqualsAndHashCode
public class Message {

    private static final boolean debug = true;

    public static final int FATAL   = 0;
    public static final int ERROR   = 1;
    public static final int WARNING = 2;
    public static final int INFO    = 3;

    @Getter private final int level;
    @Getter private final String message;
    private final StackTraceElement[] stackTrace;

    public Message(int level, String message) {
        this.level = level;
        this.message = message;
        this.stackTrace = debug
            ? Thread.currentThread().getStackTrace()
            : null;
    }

    public static Message fatal(String msg) {
        return new Message(FATAL, msg);
    }

    public static Message error(String msg) {
        return new Message(ERROR, msg);
    }

    public static Message warning(String msg) {
        return new Message(WARNING, msg);
    }

    public static Message info(String msg) {
        return new Message(INFO, msg);
    }

    /**
     * Returns true if the first argument denotes a message level
     * that is more severe than the level denoted by the second
     * argument (e.g. Message.ERROR is more severe than Message.WARNING).
     */
    public static boolean moreSevere(int lvl1, int lvl2) {
        return lvl1 <= lvl2;
    }

    public void log(Logger log) {
        switch (this.level) {
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

    public static String toString(int level) {
        switch (level) {
            case FATAL:   return "FATAL";
            case ERROR:   return "ERROR";
            case WARNING: return "WARNING";
            case INFO:    return "INFO";
            default:      return "UNRECOGNIZED LEVEL";
        }
    }

    @Override
    public String toString() {

        String output = "[" + toString(this.level) + "] " + this.message;

        if (debug) {
            output += printStackTrace();
        }

        return output;
    }

    private String printStackTrace() {
        return
            System.lineSeparator()
            + Arrays.stream(this.stackTrace)
                .filter(s -> s.getClassName().startsWith("xyz.ottr.lutra"))
                .skip(2)
                .map(stackTraceElement -> "\t" + stackTraceElement)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
