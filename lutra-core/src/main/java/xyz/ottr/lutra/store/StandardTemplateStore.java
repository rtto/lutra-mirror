package xyz.ottr.lutra.store;

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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.FormatManager;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.store.checks.Check;
import xyz.ottr.lutra.store.checks.CheckLibrary;
import xyz.ottr.lutra.store.checks.StandardQueryEngine;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;

public class StandardTemplateStore implements TemplateStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardTemplateStore.class);

    private final Map<String, Signature> templates;
    private final Map<String, Set<String>> dependencyIndex;
    private final Set<String> missingDependencies;
    private final Set<String> failed; // Stores IRIs that failed fetching
    private TemplateStore standardLibrary;

    private final FormatManager formatManager;

    public StandardTemplateStore(FormatManager formatManager) {
        this.formatManager = formatManager;
        templates = new ConcurrentHashMap<>();
        dependencyIndex = new ConcurrentHashMap<>();
        missingDependencies = new HashSet<>();
        failed = new HashSet<>();
    }

    @Override
    public void addOTTRBaseTemplates() {
        OTTR.BaseTemplate.ALL.forEach(baseTemplate -> addBaseTemplate((BaseTemplate) baseTemplate));
    }

    @Override
    public Set<String> getTemplateIRIs() {
        return getIRIs(this::containsDefinitionOf);
    }

    @Override
    public boolean addBaseTemplate(BaseTemplate baseTemplate) {
        if (templates.containsKey(baseTemplate.getIri()) && !checkParametersMatch(baseTemplate, templates.get(baseTemplate.getIri()))) {
            LOGGER.warn("BaseTemplate {} is already in the store with different parameters - nothing will be added", baseTemplate.getIri());
            return false;
        }
        templates.put(baseTemplate.getIri(), baseTemplate);
        return true;
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
            updateDependencyIndex(template);
            updateMissingDependencies(template);
            return true;
        } else {
            LOGGER.warn("Parameters of Signature and Template {} differ: {} | {}",
                    template.getIri(), sig.getParameters(), template.getParameters());
            return false;
        }
    }

    private void updateMissingDependencies(Template template) {
        missingDependencies.remove(template.getIri());
        template.getPattern().stream()
                .map(Instance::getIri)
                .filter(Predicate.not(templates::containsKey))
                .forEach(missingDependencies::add);
    }

    private void updateDependencyIndex(Template template) {
        template.getPattern().forEach(instance -> {
            dependencyIndex.putIfAbsent(instance.getIri(), new HashSet<>());
            dependencyIndex.get(instance.getIri()).add(template.getIri());
        });
    }

    @Override
    public boolean addSignature(Signature signature) {
        Signature sig = templates.get(signature.getIri());
        if (sig == null) {
            if (signature instanceof Template) {
                return addTemplate((Template) signature);
            } else {
                templates.put(signature.getIri(), signature);
                missingDependencies.add(signature.getIri());
                return true;
            }
        } else if (signature instanceof Template && checkParametersMatch(signature, sig)) {
            return addTemplate((Template) signature);
        } else {
            LOGGER.info("Signature {} already exists", sig.getIri());
            return false;
        }
    }

    // TODO move to utility class?
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
        Signature signature = templates.get(iri);
        return signature instanceof BaseTemplate;
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
            return Result.error("Missing Template definition for IRI " + iri);
        }
    }

    @Override
    public Result<Signature> getSignature(String iri) {
        Signature signature = templates.get(iri);
        if (signature == null) {
            return Result.error("Missing Signature for IRI " + iri);
        } else {
            return Result.of(signature);
        }
    }

    @Override
    public ResultStream<Template> getAllTemplates() {
        return ResultStream.innerOf(templates.values().stream().filter(s -> s instanceof Template).map(b -> (Template)b));
    }

    @Override
    public ResultStream<Signature> getAllSignatures() {
        return ResultStream.innerOf(templates.values().stream());
    }

    @Override
    public ResultStream<BaseTemplate> getAllBaseTemplates() {
        return ResultStream.innerOf(templates.values().stream().filter(s -> s instanceof BaseTemplate).map(b -> (BaseTemplate)b));
    }

    @Override
    public Set<String> getIRIs(Predicate<String> pred) {
        return templates.keySet().stream()
                .filter(pred::test)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getAllIRIs() {
        return Collections.unmodifiableSet(templates.keySet());
    }

    @Override
    public Result<Set<String>> getDependsOn(String template) {
        return Result.ofNullable(dependencyIndex.get(template));
    }

    @Override
    public MessageHandler fetchMissingDependencies() {
        return fetchMissingDependencies(getMissingDependencies());
    }

    @Override
    public MessageHandler fetchMissingDependencies(Collection<String> initMissing) {
        Optional<TemplateStore> stdLib = getStandardLibrary();
        ResultConsumer<TemplateReader> messages = new ResultConsumer<>();

        FormatManager formatManager = getFormatManager();
        if (formatManager == null) {
            messages.accept(Result.error(
                    "Attempted fetching missing templates, but has no formats registered."));
            return messages.getMessageHandler();
        }

        Set<String> missing = new HashSet<>(initMissing);

        while (!missing.isEmpty()) {
            for (String toFetch : missing) {
                if (failed.contains(toFetch)) { // check if IRI is already know to have failed before
                    continue;
                }

                if (stdLib.isPresent() && stdLib.get().containsTemplate(toFetch)) {
                    stdLib.get().getTemplate(toFetch).ifPresent(this::addTemplate);
                } else {
                    messages.accept(formatManager.attemptAllFormats(reader -> reader.populateTemplateStore(this, toFetch)));
                }

                if (containsTemplate(toFetch)) { // Check if fetched and added to store
                    messages.accept(Result.info("Fetched template " + toFetch));
                } else {
                    failed.add(toFetch);
                    messages.accept(Result.warning("Failed fetch for template " + toFetch));
                }
            }
            missing = getMissingDependencies();
            missing.removeAll(failed); // Do not attempt to fetch IRIs that previously failed
        }
        return messages.getMessageHandler();
    }

    // TODO move to Template class later?
    @Override
    public Result<Set<String>> getDependencies(String templateIri) {
        Result<Template> result = getTemplate(templateIri);
        if (result.isEmpty()) {
            return Result.error("Template with IRI " + templateIri + " not in store");
        }

        Set<Instance> dependencies = result.get().getPattern();
        Set<String> iris = dependencies.stream().map(instance -> instance.getIri()).collect(Collectors.toSet());
        return Result.of(iris);
    }

    @Override
    public Set<String> getMissingDependencies() {
        return new HashSet<>(missingDependencies);
    }

    @Override
    public MessageHandler checkTemplatesFor(List<Check> checks) {
        StandardQueryEngine engine = new StandardQueryEngine(this);
        MessageHandler msgs = new MessageHandler();

        checks.stream()
                .flatMap(c -> c.check(engine))
                .forEach(msgs::add);

        return msgs;
    }

    @Override
    public MessageHandler checkTemplates() {
        return checkTemplatesFor(CheckLibrary.allChecks);
    }

    @Override
    public MessageHandler checkTemplatesForErrorsOnly() {
        return checkTemplatesFor(CheckLibrary.failsOnErrorChecks);
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
        } else if (signature instanceof BaseTemplate) {
            addBaseTemplate((BaseTemplate) signature);
        } else {
            addSignature(signature);
        }
    }
}
