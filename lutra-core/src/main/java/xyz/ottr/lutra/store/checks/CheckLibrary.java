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
import xyz.ottr.lutra.system.Message;

public enum CheckLibrary {
    ;

    /* Undefined template */
    /**
     * Checks that require information to be present, e.g. check for missing dependencies.
     */
    private static final Check undefinedTemplate = new Check(
        Query.template("Temp")
            .and(Query.bodyInstance("Temp", "Ins"))
            .and(Query.instanceIRI("Ins", "Temp2"))
            .and(Query.isUndefined("Temp2")),
        tup -> Message.error(
            "Undefined template used in " + tup.get("Temp") + ". The template"
                + " depends on an undefined signature or template " + tup.get("Temp2"))
    );

    /* Length of argument list not equal to length of corresponding parameter list */
    // not(A) and not(B) = not(A or B)
    /**
     * Checks that does not depend on having all definitions present.
     * Type and parameter checks are included here, but only fails if a concrete
     * error/inconsistency is found (thus does not fail on missing information).
     */
    private static final Check wrongNumberOfArguments = new Check(
        Query.template("Temp")
            .and(Query.bodyInstance("Temp", "Ins"))
            .and(Query.instanceIRI("Ins", "Temp2"))
            .and(Query.arguments("Ins", "Args"))
            .and(Query.length("Args", "Len1"))
            .and(Query.parameters("Temp2", "Params"))
            .and(Query.length("Params", "Len2"))
            .and(Query.notEquals("Len1", "Len2")),
        tup -> Message.error(
            "Wrong number of arguments in instance " + tup.get("Ins") + "."
                + "An instance of template " + tup.get("Temp")
                + " has " + tup.get("Len1") + " arguments "
                + "(" + tup.get("Args") + ")"
                + ", but the signature of " + tup.get("Temp2")
                + " expects " + tup.get("Len2") + " arguments "
                + "(" + tup.get("Params") + ")")
    );
    /* Any parameter used as an argument to a non-blank is set to non-blank*/
    private static final Check inconsistentNonBlankFlags = new Check(
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
            "Inconsistent non-blank parameter flags in template " + tup.get("Temp1")
                + ". The template contains a parameter "
                + tup.get("Val") + " which is not non-blank," + " but the parameter is used as argument "
                + "(arg no. " + tup.getAsEndUserIndex("Index2") + ")"
                + " to an instance of template " + tup.get("Temp2")
                + " where the corresponding parameter " + tup.get("Params2") + " is non-blank.")
    );
    /* Any template depending on itself (cyclic dependencies) */

    private static final Check cyclicDependency = new Check(
        Query.template("Temp")
            .and(Query.dependsTransitive("Temp", "Temp")),
        tup -> Message.error(
            "Cyclic dependency in template " + tup.get("Temp") + "."
                + " The template has a cyclic dependency.")
    );


    /* Unused parameter */
    private static final Check unusedParameter = new Check(
        Query.template("Temp")
            .and(Query.parameterIndex("Temp", "Index", "Val"))
            .and(
                Query.not(
                    Query.bodyInstance("Temp", "Ins")
                        .and(Query.argumentIndex("Ins", "Index2", "Arg"))
                        .and(Query.hasOccurenceAt("Arg", "Lvl", "Val")))),
        tup -> Message.warning(
            "Unused parameter in template " + tup.get("Temp") + ". "
                + "The template has a parameter " + tup.get("Val")
                + " (arg no. " + tup.getAsEndUserIndex("Index") + ")"
                + " which does not occur in the pattern of the template.")
    );

    /* Undefined parameter */
    private static final Check undefinedParameter = new Check(
        Query.template("Temp")
            .and(Query.bodyInstance("Temp", "Ins")
                .and(Query.argumentIndex("Ins", "Index2", "Arg"))
                .and(Query.isVariable("Arg"))
                .and(Query.hasOccurenceAt("Arg", "Lvl", "Val")))
            .and(Query.not(Query.parameterIndex("Temp", "Index", "Val"))),
        tup -> Message.error(
            "Undefined parameter in template " + tup.get("Temp") + ". "
                + "The template pattern contains the variable " + tup.get("Val")
                + " (used in the instance " + tup.get("Ins") + ")"
                + ", but this does not occur in the template's parameter list.")
    );

    /* Type checking: consistent use of terms */
    // As our type hierarchy is tree shaped, if any pair of types a term is used as
    // is compatible (one subtype of the other) there must exist a least type subtype
    // of all the others
    private static final Check conflictingParameterTypes = new Check(
        Query.template("Temp")
            .and(Query.bodyInstance("Temp", "Ins1"))
            .and(Query.bodyInstance("Temp", "Ins2"))
            .and(Query.removeSymmetry("Ins1", "Ins2"))
            .and(Query.argumentIndex("Ins1", "Index1", "Arg1"))
            .and(Query.hasOccurenceAt("Arg1", "Lvl1", "Val"))
            .and(Query.isNotNone("Val")) // Do not check type-correctness of ottr:none
            .and(Query.argumentIndex("Ins2", "Index2", "Arg2"))
            .and(Query.hasOccurenceAt("Arg2", "Lvl2", "Val"))
            .and(Query.usedAsType("Ins1", "Index1", "Lvl1", "Type1"))
            .and(Query.usedAsType("Ins2", "Index2", "Lvl2", "Type2"))
            .and(Query.not(Query.isSubTypeOf("Type1", "Type2")) // not(A) and not(B) = not(A or B)
                .and(Query.not(Query.isSubTypeOf("Type2", "Type1")))),
        tup -> Message.error(
            "Type error in template " + tup.get("Temp") + ": incompatible parameter types. "
                + " The template contains an argument " + tup.get("Val") + " to different parameters with incompatible types: "
                + " instance " + tup.get("Ins1")
                + " (arg no. " + tup.getAsEndUserIndex("Index1") + ") with parameter type " + tup.get("Type1")
                + " instance " + tup.get("Ins2")
                + " (arg no. " + tup.getAsEndUserIndex("Index2") + ") with parameter type " + tup.get("Type2"))
    );

    /* Type checking: intrinsic and inferred types incompatible */
    private static final Check conflictingIntrinsicInferredTypes = new Check(
        Query.template("Temp")
            .and(Query.bodyInstance("Temp", "Ins"))
            .and(Query.argumentIndex("Ins", "Index", "Arg"))
            .and(Query.hasOccurenceAt("Arg", "Lvl", "Val"))
            .and(Query.type("Val", "Intrinsic"))
            .and(Query.usedAsType("Ins", "Index", "Lvl", "UsedAs"))
            .and(Query.not(Query.isCompatibleWith("Intrinsic", "UsedAs"))),
        tup -> Message.error(
            "Type error in template " + tup.get("Temp") + ": incompatible argument and parameter type."
                + " The template contains a value "
                + tup.get("Val") + " which has the type " + tup.get("Intrinsic")
                + " and which is used as argument to a parameter with the incompatible type " + tup.get("UsedAs")
                + " in instance " + tup.get("Ins") + " (arg no. " + tup.getAsEndUserIndex("Index") + ").")
    );

    public static final List<Check> failsOnMissingInformationChecks = List.of(
        undefinedTemplate
    );

    public static final List<Check> failsOnErrorChecks = List.of(
        inconsistentNonBlankFlags,
        wrongNumberOfArguments,
        cyclicDependency,
        unusedParameter,
        undefinedParameter,
        conflictingParameterTypes,
        conflictingIntrinsicInferredTypes
    );

    public static final List<Check> allChecks = ListUtils.union(
        failsOnMissingInformationChecks,
        failsOnErrorChecks
    );


}
