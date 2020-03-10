package xyz.ottr.lutra.wottr.io;

/*-
 * #%L
 * lutra-wottr
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

import java.io.StringWriter;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;

public enum Models {

    ; // singleton enum

    private static final Lang defaultLang = Lang.TURTLE;

    public static Model readModel(String file) {
        return readModel(file, FileUtils.guessLang(file, defaultLang.getLabel()));
    }

    public static Model readModel(String file, Lang language) {
        return readModel(file, language.getLabel());
    }

    private static Model readModel(String file, String format) {
        return FileManager.get().loadModel(file, format);
    }

    public static String writeModel(Model model) {
        return writeRDFModel(model, defaultLang);
    }

    public static String writeModel(Model model, Lang language) {
        return writeRDFModel(model, language);
    }

    private static String writeRDFModel(Model model, Lang language) {
        StringWriter out = new StringWriter();
        RDFDataMgr.write(out, model, language);
        return out.toString();
    }

    public static void trimPrefixes(Model model) {
        Set<String> namespaces = model.listNameSpaces().toSet();
        for (String prefixNamespace : model.getNsPrefixMap().values()) {
            if (!namespaces.contains(prefixNamespace)) {
                model.removeNsPrefix(model.getNsURIPrefix(prefixNamespace));
            }
        }
    }
}
