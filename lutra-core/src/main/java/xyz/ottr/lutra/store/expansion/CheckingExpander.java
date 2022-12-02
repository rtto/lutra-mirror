package xyz.ottr.lutra.store.expansion;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-core
 * %%
 * Copyright (C) 2018 - 2021 University of Oslo
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

import java.util.LinkedList;
import java.util.List;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.Type;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public class CheckingExpander extends NonCheckingExpander {

    public CheckingExpander(TemplateStore templateStore) {
        super(templateStore);
    }

    @Override
    public ResultStream<Instance> expandInstance(Instance instance) {
        var errors = checkInstance(instance);
        if (!errors.isEmpty()) {
            return ResultStream.of(Result.empty(errors));
        }
        return super.expandInstance(instance);
    }

    private List<Message> checkInstance(Instance instance) {
        Result<Signature> signature = getTemplateStore().getSignature(instance.getIri());
        if (signature.isEmpty()) {
            return signature.getMessageHandler().getMessages();
        }
        if (isSignature(signature)) {
            return List.of(Message.error("No template (only signature) found for instance " + instance));
        }

        return checkArgumentList(instance, signature.get());
    }

    private List<Message> checkArgumentList(Instance instance, Signature signature) {

        var messages = new LinkedList<Message>();

        var noArgs = instance.getArguments().size();
        var noParams = signature.getParameters().size();

        if (noArgs != noParams) {
            messages.add(Message.error("Wrong number of arguments. Expected "
                    + noParams + " arguments, but found " + noArgs
                    + " in instance: " + instance));
        }

        for (int i = 0; i < noArgs && i < noParams; i++) {
            Argument argument = instance.getArguments().get(i);
            Parameter parameter = signature.getParameters().get(i);
            messages.addAll(checkArgument(argument, parameter));
        }

        return messages;
    }

    private List<Message> checkArgument(Argument argument, Parameter parameter) {

        var messages = new LinkedList<Message>();
        
        Type paramType = parameter.getType();
        Type argType = argument.getTerm().getType();

        if (argument.isListExpander()) {
            if (argType instanceof ListType) {
                argType = ((ListType) argType).getInner();
            } else {
                messages.add(Message.error("List expander applied to non-list argument: " + argument));
            }
        }

        if (!argType.isCompatibleWith(paramType)) {
            messages.add(Message.error("Incompatible argument type. Argument " + argument
                    + " with type " + argType
                    + " given to parameter " + parameter));
        }

        if (argument.getTerm() instanceof BlankNodeTerm && parameter.isNonBlank()) {
            messages.add(Message.error("Incompatible blank node argument. Blank node " + argument
                    + " given to non-blank parameter " + parameter));
        }

        return messages;
    }

    // TODO should go somewhere else where is can be reused
    private boolean isSignature(Result<Signature> signature) {
        return !(signature.get() instanceof Template || signature.get() instanceof BaseTemplate);
    }
}
