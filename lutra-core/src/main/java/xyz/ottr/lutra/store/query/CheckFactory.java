package xyz.ottr.lutra.store.query;

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

import static xyz.ottr.lutra.store.query.Query.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.ListUtils;

import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.result.Message;

public abstract class CheckFactory {

    /**
     * Checks that require information to be present, e.g.
     * check for missing dependencies.
     */
    public static final List<Check> failsOnMissingInformationChecks =
        Collections.unmodifiableList(Arrays.asList(
            /* Undefined template */
            new Check(
                template("Temp")
                    .and(bodyInstance("Temp", "Ins"))
                    .and(instanceIRI("Ins", "Temp2"))
                    .and(isUndefined("Temp2")),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp").toString()
                    + " depends on undefined template "
                    + tup.get("Temp2").toString())
            )
        ));

    /**
     * Checks that does not depend on having all definitions present.
     * Type and parameter checks are included here, but only fails if a concrete
     * error/inconsitency is found (thus does not fail on missing information).
     */
    public static final List<Check> failsOnErrorChecks =
        Collections.unmodifiableList(Arrays.asList(
            /* Length of argument list not equal to length of corresponding parameter list */
            new Check(
                template("Temp")
                    .and(bodyInstance("Temp", "Ins"))
                    .and(instanceIRI("Ins", "Temp2"))
                    .and(arguments("Ins", "Args"))
                    .and(length("Args", "Len1"))
                    .and(parameters("Temp2", "Params"))
                    .and(length("Params", "Len2"))
                    .and(notEquals("Len1", "Len2")),
                tup -> Message.error(
                    "Argument list to template " + tup.get("Temp2").toString()
                    + " has length " + tup.get("Len1")
                    + " but corresponding parameter list has length " + tup.get("Len2")
                    + " in template " + tup.get("Temp").toString())
            ),
            /* Any parameter used as an argument to a non-blank is set to non-blank*/
            new Check(
                template("Temp1")
                    .and(parameters("Temp1", "Params1"))
                    .and(index("Params1", "Index1", "Val"))
                    .and(not(isNonBlank("Params1", "Index1")))
                    .and(bodyInstance("Temp1", "Ins"))
                    .and(argumentIndex("Ins", "Index2", "Val"))
                    .and(instanceIRI("Ins", "Temp2"))
                    .and(parameters("Temp2", "Params2"))
                    .and(isNonBlank("Params2", "Index2")),
                tup -> Message.error(
                    "Parameter with name " + tup.get("Val").toString() + " is not marked as non-blank,"
                    + " but is used as argument to non-blank parameter index "
                    + tup.get("Index2").toString() + " in instance of template "
                    + tup.get("Temp2").toString()
                    + " in template " + tup.get("Temp1").toString())
            ),
            /* Any template depending on itself (cyclic dependencies) */
            new Check(
                template("Temp")
                    .and(dependsTransitive("Temp", "Temp")),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp") + " transitively depends on itself.")
            ),
            /* Unused parameter */
            new Check(
                template("Temp")
                    .and(parameterIndex("Temp", "Index", "Val"))
                    .and(
                        not(
                            bodyInstance("Temp", "Ins")
                                .and(argumentIndex("Ins", "Index2", "Arg"))
                                .and(hasOccurenceAt("Arg", "Val", "Lvl")))),
                tup -> Message.warning(
                    "Parameter with name " + tup.get("Val").toString()
                    + " with index " + tup.get("Index").toString()
                    + " does not occur in the body of template "
                    + tup.get("Temp").toString())
            ),
            /* Same variabel occurs twice in parameter list */
            new Check(
                template("Temp")
                    .and(parameters("Temp", "Params"))
                    .and(index("Params", "Index1", "Val"))
                    .and(index("Params", "Index2", "Val"))
                    .and(notEquals("Index1", "Index2"))
                    .and(removeSymmetry("Index1", "Index2")),
                tup -> Message.error(
                        "Parameter with name " + tup.get("Val").toString()
                        + " occurs twice with indecies " + tup.get("Index1").toString()
                        + " and " + tup.get("Index2") + " in template "
                        + tup.get("Temp").toString())
            ),
            /* Type checking: consistent use of terms */
            // As our type hiearachy is tree shaped, if any pair of types a term is used as
            // is compatible (one subtype of the other) there must exist a least type subtype
            // of all the others
            new Check(
                template("Temp")
                    .and(bodyInstance("Temp", "Ins1"))
                    .and(bodyInstance("Temp", "Ins2"))
                    .and(removeSymmetry("Ins1", "Ins2"))
                    .and(argumentIndex("Ins1", "Index1", "Val"))
                    .and(argumentIndex("Ins2", "Index2", "Val"))
                    .and(usedAsType("Ins1", "Index1", "Type1"))
                    .and(usedAsType("Ins2", "Index2", "Type2"))
                    .and(not(isSubTypeOf("Type1", "Type2")) // not(A) and not(B) = not(A or B)
                        .and(not(isSubTypeOf("Type2", "Type1")))),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp") + " has incompatible use of term "
                        + tup.get("Val").toString() + " in instances " + tup.get("Ins1").toString()
                        + " and " + tup.get("Ins2") + ", with corresponding parameters typed as "
                        + tup.get("Type1").toString() + " and " + tup.get("Type2") + " respectively.")
            ),
            /* Type checking: intrinsic and inferred types incompatible */
            new Check(
                template("Temp")
                    .and(bodyInstance("Temp", "Ins"))
                    .and(argumentIndex("Ins", "Index", "Val"))
                    .and(type("Val", "Intrinsic"))
                    .and(usedAsType("Ins", "Index", "UsedAs"))
                    .and(not(isCompatibleWith("Intrinsic", "UsedAs"))),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp") + " has incompatible use of term "
                        + tup.get("Val").toString() + " with intrinsic type " + tup.get("Intrinsic")
                        + " is used as argument to parameter with type " + tup.get("UsedAs")
                        + " in instance " + tup.get("Ins").toString())
            ),
            /* Has expansion modifier but no arguments with list expanders set.*/
            new Check(
                template("Temp")
                    .and(bodyInstance("Temp", "Ins"))
                    .and(instanceIRI("Ins", "InsOf"))
                    .and(hasExpansionModifier("Ins"))
                    .and(not(
                        arguments("Ins", "Args")
                            .and(index("Args", "Index", "Val"))
                            .and(hasListExpander("Args", "Index")))),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp").toString() + " has instance "
                        + tup.get("InsOf").toString() + " with a list expander but no arguments to expand.")
            ),
            /* Has no expansion modifier but arguments with list expanders set.*/
            new Check(
                template("Temp")
                    .and(bodyInstance("Temp", "Ins"))
                    .and(instanceIRI("Ins", "InsOf"))
                    .and(not(hasExpansionModifier("Ins")))
                    .and(arguments("Ins", "Args"))
                    .and(index("Args", "Index", "Val"))
                    .and(hasListExpander("Args", "Index")),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp").toString() + " has instance "
                        + tup.get("InsOf").toString() + " with no list expander but arguments to expand.")
            ),
            /* Has non-list argument with list expanders set.*/
            new Check(
                template("Temp")
                    .and(bodyInstance("Temp", "Ins"))
                    .and(instanceIRI("Ins", "InsOf"))
                    .and(hasExpansionModifier("Ins"))
                    .and(arguments("Ins", "Args"))
                    .and(index("Args", "Index", "Val"))
                    .and(hasListExpander("Args", "Index"))
                    .and(type("Val", "Type"))
                    .and(bind("ListType", new ListType(TypeFactory.getTopType())))
                    .and(not(isSubTypeOf("Type", "ListType"))),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp").toString() + " has instance "
                        + tup.get("InsOf").toString() + " with list expander on non-list argument.")
            )
        ));

    public static final List<Check> allChecks = ListUtils.union(failsOnErrorChecks, failsOnMissingInformationChecks);
}
