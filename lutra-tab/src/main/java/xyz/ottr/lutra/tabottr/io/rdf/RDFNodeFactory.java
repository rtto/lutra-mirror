package xyz.ottr.lutra.tabottr.io.rdf;

/*-
 * #%L
 * lutra-tab
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.XSD;

import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.tabottr.TabOTTR;
import xyz.ottr.lutra.wottr.parser.v04.WOTTR;

public class RDFNodeFactory {

    private Model model;
    private DataValidator validator;
    
    public RDFNodeFactory(PrefixMapping prefixes) {
        this.model = ModelFactory.createDefaultModel();
        this.model.setNsPrefixes(prefixes);
        this.validator = new DataValidator(model);
    }

    public Result<RDFNode> toRDFNode(String value, String type) {
        // TODO: Wrap return value in Result and make empty Results
        //       with error message on Exception
        
        // if is a list, split in to values and parse into RDF nodes with a recursive call.
        if (type.endsWith(TabOTTR.TYPE_LIST_POSTFIX)) {
            type = type.substring(0, type.length() - 1).trim(); // remove list operator from type
            List<Result<RDFNode>> nodes = new ArrayList<>();
            for (String item : value.split(Pattern.quote(TabOTTR.VALUE_LIST_SEPARATOR))) {
                item = item.trim();
                nodes.add(toRDFNode(item, type));
            }
            Result<List<RDFNode>> resNodes = Result.aggregate(nodes);
            return resNodes.map(this::toList);
        } else if (DataValidator.isEmpty(value)) { // if value == empty -> ottr:none
            return Result.of(WOTTR.none);    
        } else if (TabOTTR.TYPE_IRI.equals(type)) {
            return Result.of(toResource(value));
        } else if (TabOTTR.TYPE_BLANK.equals(type)) {
            return Result.of(toBlank(value));
        } else if (TabOTTR.TYPE_TEXT.equals(type)) { // string, e.g, untyped literal
            return Result.of(toUntypedLiteral(value));
        } else if (TabOTTR.TYPE_AUTO.equals(type)) { // auto, get type
            // if type is explicitly set:
            int typeIndex = value.lastIndexOf(TabOTTR.VALUE_DATATYPE_TAG_PREFIX);
            if (typeIndex != -1) {
                String explicitType = value.substring(typeIndex + TabOTTR.VALUE_LANGUAGE_TAG_PREFIX.length());
                value = value.substring(0, typeIndex);
                return toRDFNode(value, explicitType);
            } else { // auto-get datatype:
                return toRDFNode(value, getAutoType(value));
            }
        } else { // literal
            if (!validator.isIRI(type)) {
                Message msg = Message.error("Type " + type + " is not a recognised type.");
                return Result.empty(msg);
            }
            return Result.of(toTypedLiteral(value, type));
        }
    }

    private String getAutoType(String value) {
        if (DataValidator.isBoolean(value)) {
            return XSD.xboolean.toString();
        } else if (DataValidator.isInteger(value)) {
            return XSD.integer.toString();
        } else if (DataValidator.isDecimal(value)) {
            return XSD.decimal.toString();
        } else if (DataValidator.isBlank(value)) {
            return TabOTTR.TYPE_BLANK;
        } else if (validator.isIRI(value)) {
            return TabOTTR.TYPE_IRI;
        } else { // default
            return TabOTTR.TYPE_TEXT;
        }
    }
    
    public RDFList toList(List<RDFNode> nodes) {
        return model.createList(nodes.iterator());
    }

    public Resource toResource(String qname) {
        return model.createResource(model.expandPrefix(qname));
    }

    private Resource toBlank(String value) {
        if (DataValidator.isFreshBlank(value)) {
            return model.createResource();
        } else {
            // remove trailing "_:"
            if (value.startsWith(TabOTTR.VALUE_BLANK_NODE_PREFIX)) {
                value = value.substring(TabOTTR.VALUE_BLANK_NODE_PREFIX.length());
            }
            return model.createResource(AnonId.create(value));
        }
    }

    private Literal toTypedLiteral(String value, String type) {
        // default:
        Literal literal = model.createTypedLiteral(value, model.expandPrefix(type));

        // overwrite default if ...
        // xsd:boolean and value is "1" or any capitalisation of "TRUE" (and similar for false):
        if (XSD.xboolean.toString().equals(model.expandPrefix(type))) {
            if (value.equals("1") || Boolean.parseBoolean(value) == true) {
                literal = model.createTypedLiteral(true);
            } else if (value.equals("0") || Boolean.parseBoolean(value) == false) {
                literal = model.createTypedLiteral(false);
            }
        }
        return literal;
    }
    
    private Literal toUntypedLiteral(String value) {
        // default:
        Literal literal = model.createLiteral(value);
        
        // overwrite default if ...
        // has language tag:
        int langTagIndex = value.lastIndexOf(TabOTTR.VALUE_LANGUAGE_TAG_PREFIX);
        if (langTagIndex != -1) {
            String lang = value.substring(langTagIndex + TabOTTR.VALUE_LANGUAGE_TAG_PREFIX.length());
            if (DataValidator.isLanguageTag(lang)) {
                literal = model.createLiteral(value.substring(0, langTagIndex), lang);
            }
        }
        return literal;
    }
}
