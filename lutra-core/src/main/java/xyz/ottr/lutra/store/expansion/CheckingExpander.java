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
        Result<Instance> errorMessage = checkInstance(instance);
        if (errorMessage.isEmpty()) {
            return ResultStream.of(errorMessage);
        }

        return super.expandInstance(instance);
    }

    private Result<Instance> checkInstance(Instance instance) {
        Result<Signature> signature = getTemplateStore().getSignature(instance.getIri());
        if (signature.isEmpty()) {
            return signature.map(x -> null);
        }
        if (isSignature(signature)) {
            return Result.error("Missing pattern definition of: " + instance.getIri());
        }

        Message error = checkParametersMatch(instance, signature.get());
        if (error != null) {
            return Result.empty(error);
        }

        return Result.of(instance);
    }

    // TODO return messages
    private Message checkParametersMatch(Instance instance, Signature signature) {

        if (instance.getArguments().size() != signature.getParameters().size()) {
            return Message.error("Number of arguments do not match number of paramters in instance " + instance.toString());
        }

        for (int i = 0; i < instance.getArguments().size(); i++) {
            Argument argument = instance.getArguments().get(i);
            Parameter parameter = signature.getParameters().get(i);
            Message error = checkNonCompatibleArgument(argument, parameter);

            if (error != null) {
                return error;
            }
        }

        return null;
    }

    private Message checkNonCompatibleArgument(Argument argument, Parameter parameter) {
        
        Type paramType = parameter.getType();
        Type argType = argument.getTerm().getType();

        if (argument.isListExpander()) {
            if (argType instanceof ListType) {
                argType = ((ListType) argType).getInner();
            } else {
                return Message.error("List expander applied to non-list argument: "
                        + argument.toString());
            }
        }

        if (!argType.isCompatibleWith(paramType)) {
            return Message.error("Incompatible argument in instance: "
                    + argument.toString() + " given to parameter "
                    + parameter.toString() + " - incompatible types.");
        }

        if (argument.getTerm() instanceof BlankNodeTerm && parameter.isNonBlank()) {
            return Message.error("Incompatible argument in instance:"
                    + " blank node " + argument.toString() + " given to non-blank"
                    + " parameter " + parameter.toString());
        }

        return null;
    }

    // TODO should go somewhere else where is can be reused
    private boolean isSignature(Result<Signature> signature) {
        return !(signature.get() instanceof Template || signature.get() instanceof BaseTemplate);
    }
}
