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

import java.util.Objects;

import org.slf4j.Logger;

public class Message {

    public static final int FATAL   = 0;
    public static final int ERROR   = 1;
    public static final int WARNING = 2;
    public static final int INFO    = 3;

    private final int lvl;
    private final String msg;

    public Message(int lvl, String msg) {
        this.lvl = lvl;
        this.msg = msg;
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

    public int getLevel() {
        return this.lvl;
    }

    /**
     * Returns true if the first argument denotes a message level
     * that is more severe than the level denoted by the second
     * argument (e.g. Message.ERROR is more severe than Message.WARNING).
     */
    public static boolean moreSevere(int lvl1, int lvl2) {
        return lvl1 <= lvl2;
    }

    public String getMessage() {
        return this.msg;
    }

    @Override
    public int hashCode() {
        return (this.lvl + 1) * this.msg.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o
            || Objects.nonNull(o)
            && this.getClass() == o.getClass()
            && this.lvl == ((Message) o).lvl
            && Objects.equals(this.msg, ((Message) o).msg);
    }

    public void log(Logger log) {
        switch (this.lvl) {
            case WARNING:
                log.warn(this.getMessage());
                break;
            case INFO:
                log.trace(this.getMessage());
                break;
            default:
                log.error(this.getMessage());
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
        return "[" + toString(this.lvl) + "] " + this.msg;
    }
}
