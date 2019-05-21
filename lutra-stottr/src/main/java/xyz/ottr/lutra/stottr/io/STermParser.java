package xyz.ottr.lutra.stottr.io;

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
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.TerminalNode;

import org.apache.jena.vocabulary.XSD;

import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.LiteralTerm;
import xyz.ottr.lutra.model.NoneTerm;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.wottr.WOTTR;

public class STermParser extends SBaseParserVisitor<Term> {

    private final Map<String, String> prefixes;
    private final Map<String, Term> blanks;

    // Maps labels to already parsed (blank node) variable terms
    private final Map<String, Term> variables;

    public STermParser(Map<String, String> prefixes) {
        this.prefixes = prefixes;
        this.variables = new HashMap<>();
        this.blanks = new HashMap<>();
    }

    public STermParser(Map<String, String> prefixes, Map<String, Term> variables) {
        this.prefixes = prefixes;
        this.variables = variables;
        this.blanks = new HashMap<>();
    }

    private Term makeBlank(String label) {

        // Use shallow clone to keep objects distinct but
        // with same identifier
        if (this.variables.containsKey(label)) {
            return this.variables.get(label).shallowClone();
        } else if (this.blanks.containsKey(label)) {
            return this.blanks.get(label).shallowClone();
        } else {
            Term newBlank = new BlankNodeTerm();
            this.blanks.put(label, newBlank);
            return newBlank;
        }
    }

    @Override
    public Result<Term> visitTerm(stOTTRParser.TermContext ctx) {

        if (ctx.Variable() != null) {
            return Result.of(makeBlank(ctx.Variable().getSymbol().getText()));
        }

        Result<Term> trm = visitChildren(ctx);
        return trm != null // TODO: Should never happen once all methods are implemented?
            ? trm
            : Result.empty(Message.error("Expected term but found: " + ctx.toString()));
    }
    
    @Override
    public Result<Term> visitLiteral(stOTTRParser.LiteralContext ctx) {

        if (ctx.BooleanLiteral() != null) {
            String litVal = ctx.BooleanLiteral().getSymbol().getText();
            return Result.of(new LiteralTerm(litVal, XSD.xboolean.getURI()));
        }
        return visitChildren(ctx);
    }
    
    @Override
    public Result<Term> visitList(stOTTRParser.ListContext ctx) {

        List<Result<Term>> termResLst = ctx.constant()
            .stream()
            .map(cnst -> visitConstant(cnst))
            .collect(Collectors.toList());

        Result<List<Term>> termLstRes = Result.aggregate(termResLst);
        return termLstRes.map(terms -> new TermList(terms));
    }
    
    @Override
    public Result<Term> visitNumericLiteral(stOTTRParser.NumericLiteralContext ctx) {

        String type;
        TerminalNode valNode;

        if (ctx.INTEGER() != null) {
            type = XSD.integer.getURI();
            valNode = ctx.INTEGER();
        } else if (ctx.DECIMAL() != null) {
            type = XSD.decimal.getURI();
            valNode = ctx.DECIMAL();
        } else { // ctx.DOUBLE() != null
            type = XSD.xdouble.getURI();
            valNode = ctx.DOUBLE();
        }

        String val = valNode.getSymbol().getText();
        return Result.of(new LiteralTerm(val, type));
    }
    
    @Override
    public Result<Term> visitRdfLiteral(stOTTRParser.RdfLiteralContext ctx) {

        String val = ctx.String().getSymbol().getText()
            .replaceAll("^\"|\"$", ""); // Remove surronding quotes for strings

        if (ctx.LANGTAG() != null) { // Language tag present
            String tag = ctx.LANGTAG().getSymbol().getText();
            tag = tag.replace("@", ""); // Remove the @-prefix
            return Result.of(LiteralTerm.taggedLiteral(val, tag));
        }

        if (ctx.iri() != null) { // Explicit type present
            Result<Term> iriTermRes = visitIri(ctx.iri());
            return iriTermRes.flatMap(iri ->
                Result.of(LiteralTerm.typedLiteral(val, ((IRITerm) iri).getIRI())));
        }

        return Result.of(new LiteralTerm(val));
    }
    
    @Override
    public Result<Term> visitIri(stOTTRParser.IriContext ctx) {

        stOTTRParser.PrefixedNameContext prefixCtx = ctx.prefixedName();

        if (prefixCtx != null) {
            return visitPrefixedName(prefixCtx);
        } else {
            String iriBraces = ctx.IRIREF().getSymbol().getText();
            // IRIs in Lutra are always full, so do not use surrounding '<','>'
            String iri = iriBraces.replaceAll("^<|>$", ""); 
            if (iri.equals(WOTTR.none.getURI())) {
                return Result.of(new NoneTerm());
            }
            return Result.of(new IRITerm(iri));
        }
    }
    
    @Override
    public Result<Term> visitPrefixedName(stOTTRParser.PrefixedNameContext ctx) {
        String toSplit;

        TerminalNode onlyNS = ctx.PNAME_NS(); 
        if (onlyNS != null) { // Of the form ex: (i.e. nothing after colon)
            toSplit = onlyNS.getSymbol().getText();
        } else {
            toSplit = ctx.PNAME_LN().getSymbol().getText();
        }

        String[] prefixAndLocal = toSplit.split(":");
        // Note, empty string = base in this.prefixes
        String prefix = this.prefixes.get(prefixAndLocal[0]);

        if (prefix == null) { // Prefix not found
            return Result.empty(Message.error("Unrecognized prefix in qname " + toSplit + "."));
        }

        String iri = prefix + prefixAndLocal[1];
        if (iri.equals(WOTTR.none.getURI())) {
            return Result.of(new NoneTerm());
        }
        return Result.of(new IRITerm(iri));
    }
    
    @Override
    public Result<Term> visitBlankNode(stOTTRParser.BlankNodeContext ctx) {

        if (ctx.anon() != null) { // Of the form [], i.e. no label
            return visitAnon(ctx.anon());
        }

        String label = ctx.BLANK_NODE_LABEL().getSymbol().getText();
        return Result.of(makeBlank(label));
    }
    
    @Override
    public Result<Term> visitAnon(stOTTRParser.AnonContext ctx) {
        return Result.of(new BlankNodeTerm());
    }

}
