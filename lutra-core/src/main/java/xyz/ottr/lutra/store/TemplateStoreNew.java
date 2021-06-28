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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import xyz.ottr.lutra.io.FormatManager;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.store.checks.Check;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public interface TemplateStoreNew extends Consumer<Signature> {

    // former default method
    void addOTTRBaseTemplates();

    // former default method
    Set<String> getTemplateIRIs();

    /**
     * Adds the argument base template definition to this store.
     */
    boolean addBaseTemplate(BaseTemplate baseTemplate);

    // TODO do we need more than a boolean here?
    /**
     * Adds the argument template definition to this store.
     */
    boolean addTemplate(Template template);

    // TODO do we need more than a boolean here?
    /**
     * Adds the argument as a template signature to this store,
     * (that is, without a definition) even if it infact is an
     * instance of Template.
     */
    boolean addSignature(Signature signature);

    /**
     * Returns true if this store contains either a template (base or defined)
     * or a signature with the argument IRI.
     */
    boolean contains(String iri);

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
    // needed for store init
    boolean containsDefinitionOf(String iri);

    /**
     * Returns a template with the argument IRI if there is one present in the store or an empty result if there is none
     * (which includes the store only containing a signature with this IRI).
     */
    Result<Template> getTemplate(String iri);

    /**
     * Returns a signature with the argument IRI if there is one present in the store.
     */
    Result<Signature> getSignature(String iri);

    /**
     * Returns all templates in the store.
     */
    ResultStream<Template> getAllTemplates();

    /**
     * Returns all signatures (including templates and base templates) in the store.
     */
    ResultStream<Signature> getAllSignatures();

    /**
     * Returns all base templates in the store.
     */
    ResultStream<BaseTemplate> getAllBaseTemplates();

    /**
     * Returns a Result containing the IRIs of all
     * templates of the instances in the body of the argument
     * template IRI, or empty Result if argument
     * template has no definition in the store.
     */
    Result<Set<String>> getDependencies(String templateIri);

    /**
     * Returns the IRI of all non-base templates without a definition
     * in this Store.
     */
    Set<String> getMissingDependencies();

    /**
     * Returns a Result containing the IRIs of all
     * templates having an instance of the argument
     * template IRI, or empty Result if argument
     * template is not used in this store.
     */
    Result<Set<String>> getDependsOn(String template);

    /**
     * Fetches all missing dependencies (according to #getMissingDependencies())
     * iteratively based on their IRI and adds them to this Store.
     *
     * @return
     *    A MessageHandler containing all Messages obtained through fetching and
     *    parsing missing templates
     */
    // former default method
    MessageHandler fetchMissingDependencies();

    // former default method
    // TODO needed in interface?
    MessageHandler fetchMissingDependencies(Collection<String> initMissing);

    /**
     * Returns the set of IRIs of template objects contained in this store satisfying
     * the argument predicate.
     */
    // needed for store init
    Set<String> getIRIs(Predicate<String> pred);

    /**
     * Returns the IRIs of all Signatures in the store.
     */
    Set<String> getAllIRIs();

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
    MessageHandler checkTemplates();

    /**
     * Performs the same checks as #checkTemplates(), except "Missing templates".
     * This method should be used if one either wants to check single templates
     * (without having its dependencies loaded in the store) or to check templates
     * in an unfinished library where not all templates are (yet) defined.
     */
    MessageHandler checkTemplatesForErrorsOnly();

    MessageHandler checkTemplatesFor(List<Check> checks);

    // TODO  ------ check if things below stay or not

    Optional<TemplateStoreNew> getStandardLibrary();

    void registerStandardLibrary(TemplateStoreNew standardLibrary);

    FormatManager getFormatManager();

}
