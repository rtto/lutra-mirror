package xyz.ottr.lutra.stottr.parser;

/*-
 * #%L
 * lutra-stottr
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.jena.vocabulary.XSD;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.parser.TermParser;
import xyz.ottr.lutra.stottr.STOTTR;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Result;

public class STermParserVisitor extends SBaseParserVisitor<Term> {

    private static final Pattern quotedStringPat = Pattern.compile("^\".*\"$");
    private static final Pattern quotesPat = Pattern.compile("^\"|\"$");
    private static final Pattern atPat = Pattern.compile("@");
    private static final Pattern angularPat = Pattern.compile("^<|>$");

    private final Map<String, String> prefixes;

    // Maps labels to already parsed (blank node) variable terms
    private final Map<String, Term> variables;

    STermParserVisitor(Map<String, String> prefixes) {
        this(prefixes, new HashMap<>());
    }

    STermParserVisitor(Map<String, String> prefixes, Map<String, Term> variables) {
        this.prefixes = prefixes;
        this.variables = variables;
    }

    private Result<Term> toBlankNodeTerm(String label) {

        return this.variables.containsKey(label)
            ? Result.of(this.variables.get(label).shallowClone())
            : TermParser.toBlankNodeTerm(label).map(t -> (Term)t);
    }

    public Result<Term> visitNone(stOTTRParser.NoneContext ctx) {
        return TermParser.newNoneTerm();
    }

    public Result<Term> visitTerm(stOTTRParser.TermContext ctx) {

        if (ctx.Variable() != null) {
            return toBlankNodeTerm(getVariableLabel(ctx.Variable())).map(t -> (Term)t); // return Result.of(makeBlank();
        }

        return Objects.requireNonNullElse(
            visitChildren(ctx),
            Result.error("Unrecognized term " + SParserUtils.getTextWithLineAndColumnString(ctx))
        );
    }

    public Result<Term> visitConstantTerm(stOTTRParser.ConstantTermContext ctx) {
        return Objects.requireNonNullElse(
            visitChildren(ctx),
            Result.error("Unrecognized constant term " + SParserUtils.getTextWithLineAndColumnString(ctx))
        );
    }

    String getVariableLabel(TerminalNode varLabel) {
        String label = varLabel.getSymbol().getText();
        // Need to remove variablePrefix to get label
        return label.substring(STOTTR.Terms.variablePrefix.length());
    }

    public Result<Term> visitLiteral(stOTTRParser.LiteralContext ctx) {

        if (ctx.BooleanLiteral() != null) {
            String litVal = ctx.BooleanLiteral().getSymbol().getText();
            return TermParser.toTypedLiteralTerm(litVal, XSD.xboolean.getURI()).map(t -> (Term)t);
        }
        return visitChildren(ctx);
    }

    public Result<Term> visitTermList(stOTTRParser.TermListContext ctx) {
        List<Result<Term>> termResLst = ctx.term()
            .stream()
            .map(this::visitTerm)
            .collect(Collectors.toList());

        Result<List<Term>> termLstRes = Result.aggregate(termResLst);
        return termLstRes.map(ListTerm::new);
    }

    public Result<Term> visitConstantList(stOTTRParser.ConstantListContext ctx) {
        List<Result<Term>> termResLst = ctx.constantTerm()
                .stream()
                .map(this::visitConstantTerm)
                .collect(Collectors.toList());

        Result<List<Term>> termLstRes = Result.aggregate(termResLst);
        return termLstRes.map(ListTerm::new);
    }

    public Result<Term> visitNumericLiteral(stOTTRParser.NumericLiteralContext ctx) {

        String type;
        TerminalNode valNode;

        if (ctx.INTEGER() != null) {
            type = XSD.integer.getURI();
            valNode = ctx.INTEGER();
        } else if (ctx.DECIMAL() != null) {
            type = XSD.decimal.getURI();
            valNode = ctx.DECIMAL();
        } else if (ctx.DOUBLE() != null) {
            type = XSD.xdouble.getURI();
            valNode = ctx.DOUBLE();
        } else {
            return Result.error("Unrecognized numeric type " + SParserUtils.getTextWithLineAndColumnString(ctx));
        }

        String val = valNode.getSymbol().getText();

        return TermParser.toTypedLiteralTerm(val, type).map(t -> (Term)t);
    }

    public Result<Term> visitRdfLiteral(stOTTRParser.RdfLiteralContext ctx) {

        String valStr = ctx.String().getSymbol().getText();
        // valStr might be a String containing surrounding quotes, so we remove these:
        String val = quotedStringPat.matcher(valStr).matches() // Only replace if both first and last char is \"
            ? quotesPat.matcher(valStr).replaceAll("")
            : valStr;

        if (ctx.LANGTAG() != null) { // Language tag present
            String tag = ctx.LANGTAG().getSymbol().getText();
            tag = atPat.matcher(tag).replaceFirst(""); // Remove the @-prefix
            return TermParser.toLangLiteralTerm(val, tag)
                .map(t -> (Term)t);
        }

        if (ctx.iri() != null) { // Datatype present
            Result<Term> datatype = visitIri(ctx.iri());

            if (datatype.isPresent() && !(datatype.get() instanceof IRITerm)) {
                return Result.error("Unrecognized literal datatype. Expected IRI, but found '"
                    + datatype.get() + SParserUtils.getTextWithLineAndColumnString(ctx));
            }

            return datatype
                .map(t -> (IRITerm)t)
                .map(IRITerm::getIri)
                .flatMap(iri -> TermParser.toTypedLiteralTerm(val, iri))
                .map(t -> (Term)t);
        }

        return TermParser.toPlainLiteralTerm(val)
            .map(t -> (Term)t);
    }

    public Result<Term> visitIri(stOTTRParser.IriContext ctx) {

        stOTTRParser.PrefixedNameContext prefixCtx = ctx.prefixedName();

        if (prefixCtx != null) {
            return visitPrefixedName(prefixCtx);
        } else {
            String iriBraces = ctx.IRIREF().getSymbol().getText();
            // IRIs in Lutra are always full, so do not use surrounding '<','>'
            String iri = angularPat.matcher(iriBraces).replaceAll("");

            return TermParser.toTerm(iri);
        }
    }

    public Result<Term> visitPrefixedName(stOTTRParser.PrefixedNameContext ctx) {

        // TODO can we simplify this by using Jena's PrefixMapping instead?

        String qname;
        TerminalNode onlyNS = ctx.PNAME_NS();
        // Of the form ex: (i.e. nothing after colon)
        qname = onlyNS != null
            ? onlyNS.getSymbol().getText()
            : ctx.PNAME_LN().getSymbol().getText();

        int lastColon = qname.indexOf(':'); // Cannot simply split, can e.g. have ex:local:name
        String prefixName = qname.substring(0, lastColon);
        String prefix = this.prefixes.get(prefixName);

        if (prefix == null) { // Prefix not found
            return Result.error("Unrecognized prefix '" + prefixName + "'"
                + SParserUtils.getTextWithLineAndColumnString(ctx));
        }

        String local = qname.substring(lastColon + 1);
        String iri = prefix + local;

        return TermParser.toTerm(iri);
    }

    public Result<Term> visitBlankNode(stOTTRParser.BlankNodeContext ctx) {

        if (ctx.anon() != null) { // Of the form [], i.e. no label
            return visitAnon(ctx.anon());
        }

        String label = ctx.BLANK_NODE_LABEL().getSymbol().getText();
        return toBlankNodeTerm(label);
    }

    public Result<Term> visitAnon(stOTTRParser.AnonContext ctx) {
        return TermParser.newBlankNodeTerm().map(t -> (Term)t);
    }

}

