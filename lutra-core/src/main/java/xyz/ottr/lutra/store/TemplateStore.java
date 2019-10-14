package xyz.ottr.lutra.store;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.ReaderRegistry;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.MessageHandler;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.result.ResultStream;

public interface TemplateStore extends Consumer<TemplateSignature> {

    default void addOTTRBaseTemplates() {
        addTemplateSignature(OTTR.BaseTemplate.Triple);
        addTemplateSignature(OTTR.BaseTemplate.NullableTriple);
    }

    /**
     * Adds the argument template definition to this store.
     */
    boolean addTemplate(Template template);

    /**
     * Adds the argument as a template signature to this store,
     * (that is, without a definition) even if it infact is an
     * instance of Template.
     */
    boolean addTemplateSignature(TemplateSignature templateSignature);

    /**
     * If the argument object is instance of Template, then 
     * adds it as template (with addTemplate-method) to this store,
     * otherwise adds it as TemplateSignature with addTemplateSignature-method.
     */
    default boolean addTemplateObject(TemplateSignature templateObj) {
        if (templateObj instanceof Template) {
            return addTemplate((Template) templateObj);
        } else {
            return addTemplateSignature(templateObj);
        }
    }

    /**
     * Returns true if this store contains either a template (base or defined)
     * or a signature with the argument IRI.
     */
    boolean containsTemplate(String iri);

    /**
     * Returns true if this store contains a base template
     * with the argument IRI.
     */
    boolean containsBase(String iri);

    /**
     * Returns true if this store contains signature
     * with the argument IRI.
     */
    boolean containsSignature(String iri);

    /**
     * Returns true if this store contains a template (with definition)
     * with the argument IRI.
     */
    boolean containsDefinitionOf(String iri);

    Result<Template> getTemplate(String iri);

    Result<TemplateSignature> getTemplateSignature(String iri);

    /**
     * Returns the set of IRIs of template objects contained in this store satifiying
     * the argument predicate.
     */
    Set<String> getIRIs(Predicate<String> pred);

    default Set<String> getTemplateIRIs() {
        return getIRIs(this::containsDefinitionOf);
    }

    default Set<String> getTemplateSignatureIRIs() {
        return getIRIs(iri ->
            containsSignature(iri) || containsBase(iri) && !iri.equals(OTTR.BaseURI.Triple));
    }

    /**
     * Returns a Result containing the IRIs of all
     * templates having an instance of the argument
     * template IRI, or empty Result if argument
     * template is not used in this store.
     */
    Result<Set<String>> getDependsOn(String template);

    /**
     * Returns a Result containing the IRIs of all
     * templates of the instances in the body of the argument
     * template IRI, or empty Result if argument
     * template has no definition in the store.
     */
    Result<Set<String>> getDependencies(String template);

    /**
     * Refactors the template having the second argument as IRI
     * to instantiate the template having the first argument as IRI.
     */
    boolean refactor(String toUse, String toChange);

    /**
     * Performs all checks on all templates in this library, and returns
     * errors or warnings if checks fail. The following is checked:
     * - Type correctness, non-blank flags, and consistent use of resources
     * - Correct calling of templates in instances
     * - Cycles in template definitions
     * - Unused variables, reused variables in different parameters
     * - Use of lists and expansion modifiers
     * - Missing template 
     */
    List<Message> checkTemplates();

    /**
     * Performs the same checks as #checkTemplates(), except "Missing templates".
     * This method should be used if one either wants to check single templates
     * (without having its dependencies loaded in the store) or to check templates
     * in an unfinished library where not all templates are (yet) defined.
     */
    List<Message> checkTemplatesForErrorsOnly();

    /**
     * Expands all nodes without losing information, that is, it does not expand
     * nodes with non-optional possible null valued parameters, and does not alter
     * this TemplareStore.
     *
     * @return
     *          a new TemplateStore containing the expansion of this graph
     */
    Result<? extends TemplateStore> expandAll();

    /**
     * Expands all templates in argument set without altering this TemplateStore.
     *
     * @param iris
     *          the set of IRIs of templates which instances to expand
     *
     * @return
     *          a new TemplateStore containing the expansion of this graph
     */
    Result<? extends TemplateStore> expandVocabulary(Set<String> iris);

    /**
     * Retrieves the definitions of all templates, according to this store.
     *
     * @return
     *          a ResultStream of templates
     */ 
    default ResultStream<Template> getAllTemplates() {
        return getTemplates(getTemplateIRIs());
    }

    /**
     * Retrieves all signatures and base templates (except the ottr:Triple base)
     * in this store.
     *
     * @return
     *          a ResultStream of signatures
     */ 
    default ResultStream<TemplateSignature> getAllTemplateSignatures() {
        return getTemplateSignatures(getTemplateSignatureIRIs());
    }

    /**
     * Retrieves all template objects, both definitions and
     * signatures (except the ottr:Triple base) in this graph.
     *
     * @return
     *          a ResultStream of templates
     */ 
    default ResultStream<TemplateSignature> getAllTemplateObjects() {
        return ResultStream.concat(
            getAllTemplateSignatures(),
            getAllTemplates().innerMap(tmp -> (TemplateSignature) tmp));
    }

    /**
     * Retrieves the definitions of the templates of the given iris,
     * according to this graph.
     *
     * @param iris
     *          the IRIs of the template to return the definitions of
     * @return
     *          a ResultStream of templates where missing templates
     *          results in empty Result-objects
     */ 
    default ResultStream<Template> getTemplates(Set<String> iris) {
        return new ResultStream<>(iris.stream().map(iri -> getTemplate(iri)));
    }

    /**
     * Retrieves the signatures with the given iris,
     * according to this graph.
     *
     * @param iris
     *          the IRIs of the signatures to return
     * @return
     *          a ResultStream of signatures where missing signatures
     *          results in empty Result-objects
     */ 
    default ResultStream<TemplateSignature> getTemplateSignatures(Set<String> iris) {
        return new ResultStream<>(iris.stream().map(iri -> getTemplateSignature(iri)));
    }

    /**
     * Expands the argument template instance according to the definitions in this
     * store, and returns empty Result-instances if the instance is using
     * a template wrongly (e.g.~wrong number of arguments or wrong types, optionals).
     */
    ResultStream<Instance> expandInstance(Instance instance);

    /**
     * Expands the argument template instance according to the definitions in this
     * store, but fetches misisng templates, and returns empty Result-instances if the instance is using
     * a template wrongly (e.g.~wrong number of arguments or wrong types, optionals).
     *
     * @param instance
     *     the template instance to expand
     *
     * @return
     *     a ResultStream of expanded template instances
     */
    default ResultStream<Instance> expandInstanceFetch(Instance instance) {

        if (!containsTemplate(instance.getIRI())) {
            // Need to fetch missing template
            MessageHandler messages = fetchMissingDependencies(Arrays.asList(instance.getIRI()));
            Result<Instance> insWithMsgs = Result.of(instance);
            messages.toSingleMessage("Fetch missing template: " + instance.getIRI())
                .ifPresent(insWithMsgs::addMessage);
            return insWithMsgs.mapToStream(this::expandInstance);
        }

        return expandInstance(instance);
    }

    /**
     * Returns the IRI of all non-base templates without a definition
     * in this Store.
     */
    Set<String> getMissingDependencies();

    /**
     * Fetches all missing dependencies (according to #getMissingDependencies())
     * iteratively based on their IRI and adds them to this Store.
     *
     * @return
     *    A MessageHandler containing all Messages obtained through fetching and
     *    parsing missing templates
     */
    default MessageHandler fetchMissingDependencies() {
        return fetchMissingDependencies(getMissingDependencies());
    }
    
    default MessageHandler fetchMissingDependencies(Collection<String> initMissing) {

        ResultConsumer<TemplateReader> messages = new ResultConsumer<>();
        
        ReaderRegistry readerRegistry = getReaderRegistry();
        if (readerRegistry == null) {
            messages.accept(Result.error(
                    "Attempted fetching missing templates, but has no ReaderRegistry provided."));
            return messages.getMessageHandler();
        }

        Set<String> missing = new HashSet<>(initMissing);
        while (!missing.isEmpty()) {
            String toFetch = missing.iterator().next();
            messages.accept(readerRegistry.attemptAllReaders(reader -> reader.populateTemplateStore(this, toFetch)));
            missing = getMissingDependencies(); // TODO: Make more efficient check for missing
        }
        return messages.getMessageHandler();
    }

    ReaderRegistry getReaderRegistry();
}
