package xyz.ottr.lutra.store.graph;

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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.io.FormatManager;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.store.TemplateStoreNew;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public class TemplateManager implements TemplateStore, TemplateStoreNew {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateManager.class);

    private final Map<String, Signature> templates;
    private TemplateStore standardLibrary;

    private final FormatManager formatManager;

    public TemplateManager(FormatManager formatManager) {
        this.formatManager = formatManager;
        templates = new ConcurrentHashMap<>();
    }

    @Override
    public boolean addTemplate(Template template) {
        Signature sig = templates.get(template.getIri());
        if (sig instanceof Template && !((Template)sig).getPattern().isEmpty()) {
            LOGGER.warn("Signature {} is a template and has dependencies set - nothing will be added", sig.getIri());
            return false;
        }

        if (sig == null || checkParametersMatch(template, sig)) {
            templates.put(template.getIri(), template);
            return true;
        } else {
            LOGGER.warn("Parameters of Signature and Template {} differ: {} | {}",
                    template.getIri(), sig.getParameters(), template.getParameters());
            return false;
        }
    }

    @Override
    public boolean addTemplateSignature(Signature signature) {
        return addSignature(signature);
    }

    @Override
    public boolean addSignature(Signature signature) {
        Signature sig = templates.get(signature.getIri());
        if (sig == null) {
            templates.put(signature.getIri(), signature);
            return true;
        } else if (signature instanceof Template && checkParametersMatch(signature, sig)) {
            return addTemplate((Template) signature);
        } else {
            LOGGER.info("Signature {} already exists", sig.getIri());
            return false;
        }
    }

    private boolean checkParametersMatch(Signature sig1, Signature sig2) {
        boolean result = sig1.getParameters().size() == sig2.getParameters().size();

        if (result) {
            for (int i = 0; i < sig1.getParameters().size(); i++) {
                Parameter p1 = sig1.getParameters().get(i);
                Parameter p2 = sig2.getParameters().get(i);
                result = checkParametersEqual(p1, p2);

                if (!result) {
                    return result;
                }
            }
        }

        return result;
    }

    private boolean checkParametersEqual(Parameter p1, Parameter p2) {
        // TODO parameter match check is missing Type and default value (Term) checks
        //      - to be decided how this is best integrated in these interfaces

        // TODO type equality check
        if (p1.isNonBlank() == p2.isNonBlank()) {
            if (p1.isOptional() == p2.isOptional()) {
                // TODO default value match check

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean contains(String iri) {
        return templates.containsKey(iri);
    }

    @Override
    public boolean containsTemplate(String iri) {
        Signature signature = templates.get(iri);
        return signature instanceof Template;
    }

    @Override
    public boolean containsBase(String iri) {
        return false;
    }

    @Override
    public boolean containsSignature(String iri) {
        return templates.containsKey(iri);
    }

    @Override
    public boolean containsDefinitionOf(String iri) {
        boolean result = templates.containsKey(iri);
        if (result) {
            return templates.get(iri) instanceof Template;
        }
        return result;
    }

    @Override
    public Result<Template> getTemplate(String iri) {
        Signature signature = templates.get(iri);
        if (signature instanceof Template) {
            return Result.of((Template) signature);
        } else {
            return Result.empty();
        }
    }

    @Override
    public Result<Signature> getTemplateSignature(String iri) {
        return getSignature(iri);
    }

    @Override
    public Result<Signature> getSignature(String iri) {
        Signature signature = templates.get(iri);
        if (signature == null) {
            return Result.empty();
        } else {
            return Result.of(signature);
        }
    }

    @Override
    public Result<Signature> getTemplateObject(String iri) {
        return null;
    }

    @Override
    public Set<String> getIRIs(Predicate<String> pred) {
        return templates.keySet().stream()
                .filter(pred::test)
                .collect(Collectors.toSet());
    }

    @Override
    public Result<Set<String>> getDependsOn(String template) {
        return null;
    }

    @Override
    public Result<Set<String>> getDependencies(String template) {
        return null;
    }

    @Override
    public MessageHandler checkTemplates() {
        return null;
    }

    @Override
    public MessageHandler checkTemplatesForErrorsOnly() {
        return null;
    }

    @Override
    public Result<? extends TemplateStore> expandAll() {
        return null;
    }

    @Override
    public ResultStream<Instance> expandInstance(Instance instance) {
        return null;
    }

    @Override
    public ResultStream<Instance> expandInstanceWithoutChecks(Instance instance) {
        return null;
    }

    @Override
    public Set<String> getMissingDependencies() {
        return null;
    }

    @Override
    public Optional<TemplateStore> getStandardLibrary() {
        return standardLibrary == null ? Optional.empty() : Optional.of(standardLibrary);
    }

    @Override
    public void registerStandardLibrary(TemplateStore standardLibrary) {
        this.standardLibrary = standardLibrary;
    }

    @Override
    public FormatManager getFormatManager() {
        return formatManager;
    }

    // coming from Consumer interface - needed at least for store init
    @Override
    public void accept(Signature signature) {
        if (signature instanceof Template) {
            addTemplate((Template) signature);
        } else {
            addSignature(signature);
        }
    }
}
