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

import java.io.PrintStream;
import java.util.function.Consumer;

public class ResultConsumer<T> implements Consumer<Result<T>> {

    private MessageHandler messageHandler;
    private Consumer<T> valueConsumer;

    public ResultConsumer(Consumer<T> valueConsumer, PrintStream output) {
        this.valueConsumer = valueConsumer;
        if (output == null) {
            this.messageHandler = new MessageHandler();
        } else {
            this.messageHandler = new MessageHandler(output);
        }
    }

    public ResultConsumer(Consumer<T> valueConsumer) {
        this(valueConsumer, null);
    }

    public ResultConsumer() {
        this(null);
    }

    @Override
    public void accept(Result<T> result) {

        if (valueConsumer != null && result != null) {
            result.ifPresent(valueConsumer);
        }

        this.messageHandler.add(result);
    }

    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }
}
