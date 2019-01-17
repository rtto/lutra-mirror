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

import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.result.Message;

public abstract class CheckFactory {

    public static final List<Check> defaultChecks =
        Collections.unmodifiableList(Arrays.asList(
            /* Unused parameter */
            new Check(
                template("T")
                    .and(parameters("T", "PS"))
                    .and(body("T", "B"))
                    .and(index("PS", "I", "V"))
                    .and(
                        not(
                            instance("B", "INS")
                                .and(instanceArgs("INS", "AS"))
                                .and(index("AS", "J", "V")))),
                tup -> Message.warning(
                    "Parameter with name " + tup.get("V").toString()
                    + " with index " + tup.get("I").toString()
                    + " does not occur in the body of template "
                    + tup.get("T").toString())
            ),
            /* Same variabel occurs twice in parameter list */
            new Check(
                template("T")
                    .and(parameters("T", "PS"))
                    .and(index("PS", "I", "V"))
                    .and(index("PS", "J", "V"))
                    .and(notEquals("I", "J"))
                    .and(removeSymmetry("I", "J")),
                tup -> Message.error(
                        "Parameter with name " + tup.get("V").toString()
                        + " occurs twice with indecies " + tup.get("I").toString()
                        + " and " + tup.get("J") + " in template "
                        + tup.get("T").toString())
            ),
            /* Length of argument list not equal to length of corresponding parameter list */
            new Check(
                template("T")
                    .and(body("T", "B"))
                    .and(instance("B", "I"))
                    .and(instanceIRI("I", "T2"))
                    .and(instanceArgs("I", "AS"))
                    .and(length("AS", "L1"))
                    .and(parameters("T2", "PS"))
                    .and(length("PS", "L2"))
                    .and(notEquals("L1", "L2")),
                tup -> Message.error(
                    "Argument list to template " + tup.get("T2").toString()
                    + " has length " + tup.get("L1")
                    + " but corresponding parameter list has length " + tup.get("L2")
                    + " in template " + tup.get("T").toString())
            ),
            /* Any parameter used as an argument to a non-blank is set to non-blank*/
            new Check(
                template("T1")
                    .and(parameters("T1", "PS1"))
                    .and(index("PS1", "J1", "V1"))
                    .and(not(isNonBlank("PS1", "J1")))
                    .and(body("T1", "B1"))
                    .and(instance("B1", "I1"))
                    .and(instanceArgs("I1", "AS1"))
                    .and(index("AS1", "J2", "V1"))
                    .and(instanceIRI("I1", "T2"))
                    .and(parameters("T2", "PS2"))
                    .and(isNonBlank("PS2", "J2")),
                tup -> Message.error(
                    "Parameter with name " + tup.get("V1").toString() + " is not marked as non-blank,"
                    + " but is used as argument to non-blank parameter index "
                    + tup.get("J2").toString() + " in instance of template "
                    + tup.get("T2").toString()
                    + " in template " + tup.get("T1").toString())
            ),
            /* Any template depending on itself (cyclic dependencies) */
            new Check(
                template("T")
                    .and(dependsTransitive("T", "T")),
                tup -> Message.error(
                    "Template with IRI " + tup.get("T") + " transitively depends on itself.")
            ),
            /* Type checking: consistent use of terms */
            // As our type hiearachy is tree shaped, if any pair of types a term is used as
            // is compatible (one subtype of the other) there must exist a least type subtype
            // of all the others
            new Check(
                template("Temp")
                    .and(body("Temp", "Body"))
                    .and(instance("Body", "Ins1"))
                    .and(instance("Body", "Ins2"))
                    .and(removeSymmetry("Ins1", "Ins2"))
                    .and(instanceArgs("Ins1", "Args1"))
                    .and(instanceArgs("Ins2", "Args2"))
                    .and(index("Args1", "Index1", "Val"))
                    .and(index("Args2", "Index2", "Val"))
                    .and(instanceIRI("Ins1", "Temp1"))
                    .and(parameters("Temp1", "Params1"))
                    .and(index("Params1", "Index1", "P1"))
                    .and(hasListExpander("Args1", "Index1")
                        .and(type("P1", "Outer1"))
                        .and(innerType("Outer1", "Type1"))
                        .or(not(hasListExpander("Args1", "Index1"))
                            .and(type("P1", "Type1"))))
                    .and(instanceIRI("Ins2", "Temp2"))
                    .and(parameters("Temp2", "Params2"))
                    .and(index("Params2", "Index2", "P2"))
                    .and(hasListExpander("Args2", "Index2")
                        .and(type("P2", "Outer2"))
                        .and(innerType("Outer2", "Type2"))
                        .or(not(hasListExpander("Args2", "Index2"))
                            .and(type("P2", "Type2"))))
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
                    .and(body("Temp", "Body"))
                    .and(instance("Body", "Ins"))
                    .and(instanceArgs("Ins", "Args"))
                    .and(index("Args", "I", "V"))
                    .and(type("V", "Intrinsic"))
                    .and(instanceIRI("Ins", "TempI"))
                    .and(parameters("TempI", "Params"))
                    .and(index("Params", "I", "P"))
                    .and(hasListExpander("Args", "I")
                        .and(type("P", "Outer"))
                        .and(innerType("Outer", "UsedAs"))
                        .or(not(hasListExpander("Args", "I"))
                            .and(type("P", "UsedAs"))))
                    .and(not(isCompatibleWith("Intrinsic", "UsedAs"))),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp") + " has incompatible use of term "
                        + tup.get("V").toString() + " with intrinsic type " + tup.get("Intrinsic")
                        + " is used as argument to parameter with type " + tup.get("UsedAs")
                        + " in instance " + tup.get("Ins").toString())
            ),
            /* Has expansion modifier but no arguments with list expanders set.*/
            new Check(
                template("Temp")
                    .and(body("Temp", "Body"))
                    .and(instance("Body", "Ins"))
                    .and(instanceIRI("Ins", "InsOf"))
                    .and(hasExpansionModifier("Ins"))
                    .and(not(
                        instanceArgs("Ins", "Args")
                            .and(index("Args", "Index", "Val"))
                            .and(hasListExpander("Args", "Index")))),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp").toString() + " has instance "
                        + tup.get("InsOf").toString() + " with a list expander but no arguments to expand.")
            ),
            /* Has no expansion modifier but arguments with list expanders set.*/
            new Check(
                template("Temp")
                    .and(body("Temp", "Body"))
                    .and(instance("Body", "Ins"))
                    .and(instanceIRI("Ins", "InsOf"))
                    .and(not(hasExpansionModifier("Ins")))
                    .and(instanceArgs("Ins", "Args"))
                    .and(index("Args", "Index", "Val"))
                    .and(hasListExpander("Args", "Index")),
                tup -> Message.error(
                    "Template with IRI " + tup.get("Temp").toString() + " has instance "
                        + tup.get("InsOf").toString() + " with no list expander but arguments to expand.")
            ),
            /* Has non-list argument with list expanders set.*/
            new Check(
                template("Temp")
                    .and(body("Temp", "Body"))
                    .and(instance("Body", "Ins"))
                    .and(instanceIRI("Ins", "InsOf"))
                    .and(hasExpansionModifier("Ins"))
                    .and(instanceArgs("Ins", "Args"))
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
}
