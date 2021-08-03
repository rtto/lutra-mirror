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
import xyz.ottr.lutra.store.TemplateStore;
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
        if (checkParametersMatch(instance, signature.get())) {
            return Result.error("Arguments do not match parameters");
        }

        return null;
    }

    // TODO return messages
    private boolean checkParametersMatch(Instance instance, Signature signature) {
        boolean result = instance.getArguments().size() == signature.getParameters().size();

        if (result) {
            for (int i = 0; i < instance.getArguments().size(); i++) {
                Argument argument = instance.getArguments().get(i);
                Parameter parameter = signature.getParameters().get(i);
                result = checkParametersEqual(argument, parameter);

                if (!result) {
                    return result;
                }
            }
        }

        return result;
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private boolean checkParametersEqual(Argument argument, Parameter parameter) {
        // TODO implement with messages wht is not OK
        return false;
    }

    // TODO should go somewhere else where is can be reused
    private boolean isSignature(Result<Signature> signature) {
        return !(signature.get() instanceof Template || signature.get() instanceof BaseTemplate);
    }
}
