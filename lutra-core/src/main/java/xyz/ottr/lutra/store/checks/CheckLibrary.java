package xyz.ottr.lutra.store.checks;

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

import java.util.List;

import org.apache.commons.collections4.ListUtils;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.store.Query;
import xyz.ottr.lutra.system.Message;

public enum CheckLibrary {
    ;

    /* Undefined template */
    /**
     * Checks that require information to be present, e.g.
     * check for missing dependencies.
     */
    public static final List<Check> failsOnMissingInformationChecks =
        List.of(new Check(
            Query.template("Temp")
                .and(Query.bodyInstance("Temp", "Ins"))
                .and(Query.instanceIRI("Ins", "Temp2"))
                .and(Query.isUndefined("Temp2")),
            tup -> Message.error(
                "Template with IRI " + tup.get("Temp")
                    + " depends on undefined template "
                    + tup.get("Temp2"))
        ));

    /* Length of argument list not equal to length of corresponding parameter list */
    // not(A) and not(B) = not(A or B)
    /**
     * Checks that does not depend on having all definitions present.
     * Type and parameter checks are included here, but only fails if a concrete
     * error/inconsistency is found (thus does not fail on missing information).
     */
    public static final List<Check> failsOnErrorChecks =
        List.of(new Check(
            Query.template("Temp")
                .and(Query.bodyInstance("Temp", "Ins"))
                .and(Query.instanceIRI("Ins", "Temp2"))
                .and(Query.arguments("Ins", "Args"))
                .and(Query.length("Args", "Len1"))
                .and(Query.parameters("Temp2", "Params"))
                .and(Query.length("Params", "Len2"))
                .and(Query.notEquals("Len1", "Len2")),
            tup -> Message.error(
                "Argument list to template " + tup.get("Temp2")
                    + " has length " + tup.get("Len1")
                    + " but corresponding parameter list has length " + tup.get("Len2")
                    + " in template " + tup.get("Temp"))
            ),
            /* Any parameter used as an argument to a non-blank is set to non-blank*/
            new Check(
                Query.template("Temp1")
                    .and(Query.parameters("Temp1", "Params1"))
                    .and(Query.index("Params1", "Index1", "Val"))
                    .and(Query.not(Query.isNonBlank("Params1", "Index1")))
                    .and(Query.bodyInstance("Temp1", "Ins"))
                    .and(Query.argumentIndex("Ins", "Index2", "Val"))
                    .and(Query.instanceIRI("Ins", "Temp2"))
                    .and(Query.parameters("Temp2", "Params2"))
                    .and(Query.isNonBlank("Params2", "Index2")),
                tup -> Message.error(
                    "Parameter with name " + tup.get("Val") + " is not marked as non-blank,"
                        + " but is used as argument to non-blank parameter index "
                        + tup.getAsEndUserIndex("Index2") + " in instance of template "
                        + tup.get("Temp2")
                        + " in template " + tup.get("Temp1"))
            ),
            /* Any template depending on itself (cyclic dependencies) */
            new Check(
                Query.template("Temp")
                    .and(Query.dependsTransitive("Temp", "Temp")),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp") + " transitively depends on itself.")
            ),
            /* Unused parameter */
            new Check(
                Query.template("Temp")
                    .and(Query.parameterIndex("Temp", "Index", "Val"))
                    .and(
                        Query.not(
                            Query.bodyInstance("Temp", "Ins")
                                .and(Query.argumentIndex("Ins", "Index2", "Arg"))
                                .and(Query.hasOccurenceAt("Arg", "Lvl", "Val")))),
                tup -> Message.warning(
                    "Parameter with name " + tup.get("Val")
                        + " with index " + tup.getAsEndUserIndex("Index")
                        + " does not occur in the body of template "
                        + tup.get("Temp"))
            ),
            /* Same variable occurs twice in parameter list */
            new Check(
                Query.template("Temp")
                    .and(Query.parameters("Temp", "Params"))
                    .and(Query.index("Params", "Index1", "Val"))
                    .and(Query.index("Params", "Index2", "Val"))
                    .and(Query.notEquals("Index1", "Index2"))
                    .and(Query.removeSymmetry("Index1", "Index2")),
                tup -> Message.error(
                    "Parameter with name " + tup.get("Val")
                        + " occurs twice with indices " + tup.getAsEndUserIndex("Index1")
                        + " and " + tup.getAsEndUserIndex("Index2") + " in template "
                        + tup.get("Temp"))
            ),
            /* Type checking: consistent use of terms */
            // As our type hierarchy is tree shaped, if any pair of types a term is used as
            // is compatible (one subtype of the other) there must exist a least type subtype
            // of all the others
            new Check(
                Query.template("Temp")
                    .and(Query.bodyInstance("Temp", "Ins1"))
                    .and(Query.bodyInstance("Temp", "Ins2"))
                    .and(Query.removeSymmetry("Ins1", "Ins2"))
                    .and(Query.argumentIndex("Ins1", "Index1", "Arg1"))
                    .and(Query.hasOccurenceAt("Arg1", "Lvl1", "Val"))
                    .and(Query.argumentIndex("Ins2", "Index2", "Arg2"))
                    .and(Query.hasOccurenceAt("Arg2", "Lvl2", "Val"))
                    .and(Query.usedAsType("Ins1", "Index1", "Lvl1", "Type1"))
                    .and(Query.usedAsType("Ins2", "Index2", "Lvl2", "Type2"))
                    .and(Query.not(Query.isSubTypeOf("Type1", "Type2")) // not(A) and not(B) = not(A or B)
                        .and(Query.not(Query.isSubTypeOf("Type2", "Type1")))),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp") + " has incompatible use of term "
                        + tup.get("Val") + " in instances " + tup.get("Ins1")
                        + " and " + tup.get("Ins2") + ", with corresponding parameters typed as "
                        + tup.get("Type1") + " and " + tup.get("Type2") + " respectively.")
            ),
            /* Type checking: intrinsic and inferred types incompatible */
            new Check(
                Query.template("Temp")
                    .and(Query.bodyInstance("Temp", "Ins"))
                    .and(Query.argumentIndex("Ins", "Index", "Arg"))
                    .and(Query.hasOccurenceAt("Arg", "Lvl", "Val"))
                    .and(Query.type("Val", "Intrinsic"))
                    .and(Query.usedAsType("Ins", "Index", "Lvl", "UsedAs"))
                    .and(Query.not(Query.isCompatibleWith("Intrinsic", "UsedAs"))),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp") + " has incompatible use of term "
                        + tup.get("Val") + " with intrinsic type " + tup.get("Intrinsic")
                        + " is used as argument to parameter with type " + tup.get("UsedAs")
                        + " in instance " + tup.get("Ins"))
            ),
            /* Has expansion modifier but no arguments with list expanders set.*/
            new Check(
                Query.template("Temp")
                    .and(Query.bodyInstance("Temp", "Ins"))
                    .and(Query.instanceIRI("Ins", "InsOf"))
                    .and(Query.hasExpansionModifier("Ins"))
                    .and(Query.not(
                        Query.arguments("Ins", "Args")
                            .and(Query.index("Args", "Index", "Val"))
                            .and(Query.hasListExpander("Args", "Index")))),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp") + " has instance "
                        + tup.get("InsOf") + " with a listExpander but no arguments to expand.")
            ),
            /* Has no expansion modifier but arguments with list expanders set.*/
            new Check(
                Query.template("Temp")
                    .and(Query.bodyInstance("Temp", "Ins"))
                    .and(Query.instanceIRI("Ins", "InsOf"))
                    .and(Query.not(Query.hasExpansionModifier("Ins")))
                    .and(Query.arguments("Ins", "Args"))
                    .and(Query.index("Args", "Index", "Val"))
                    .and(Query.hasListExpander("Args", "Index")),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp") + " has instance "
                        + tup.get("InsOf") + " with no listExpander but arguments to expand.")
            ),
            /* Has non-list argument with list expanders set.*/
            new Check(
                Query.template("Temp")
                    .and(Query.bodyInstance("Temp", "Ins"))
                    .and(Query.instanceIRI("Ins", "InsOf"))
                    .and(Query.hasExpansionModifier("Ins"))
                    .and(Query.arguments("Ins", "Args"))
                    .and(Query.index("Args", "Index", "Val"))
                    .and(Query.hasListExpander("Args", "Index"))
                    .and(Query.type("Val", "Type"))
                    .and(Query.bind("ListType", new ListType(TypeRegistry.TOP)))
                    .and(Query.not(Query.isSubTypeOf("Type", "ListType"))),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp") + " has instance "
                        + tup.get("InsOf") + " with listExpander on non-list argument.")
            ));

    public static final List<Check> allChecks = ListUtils.union(failsOnErrorChecks, failsOnMissingInformationChecks);
}
