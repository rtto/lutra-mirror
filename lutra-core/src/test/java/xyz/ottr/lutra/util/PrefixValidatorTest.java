package xyz.ottr.lutra.util;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-core
 * %%
 * Copyright (C) 2018 - 2023 University of Oslo
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.jena.shared.PrefixMapping;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public class PrefixValidatorTest {

    @Test
    public void same_prefix_different_namespaces() {
        List<Map.Entry<String,String>> prefixes = new LinkedList<>();
        prefixes.add(Map.entry("foaf", "http://xmlns.com/foaf/0.1/"));
        prefixes.add(Map.entry("foaf", "http://faulty-xmlns.com/foaf/0.1/"));

        Result<PrefixMapping> result = PrefixValidator.buildPrefixMapping(prefixes);
        Assertions.atLeast(result, Message.Severity.ERROR);
    }

    @Disabled
    @Test
    public void same_namespace_different_prefixes() {
        List<Map.Entry<String,String>> prefixes = new LinkedList<>();
        prefixes.add(Map.entry("foaf", "http://xmlns.com/foaf/0.1/"));
        prefixes.add(Map.entry("my-foaf", "http://xmlns.com/foaf/0.1/"));

        PrefixMapping expected = PrefixMapping.Factory.create();
        expected.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        expected.setNsPrefix("my-foaf", "http://xmlns.com/foaf/0.1/");

        Result<PrefixMapping> result = PrefixValidator.buildPrefixMapping(prefixes);
        Assertions.atLeast(result, Message.Severity.WARNING);
        assertTrue(result.get().samePrefixMappingAs(expected));
    }

    @Test
    public void check_standard_namespace() {
        PrefixMapping prefixes = PrefixMapping.Factory.create();
        prefixes.setNsPrefix("rdf", "http://faulty-rdf-syntax-ns#");
        prefixes.setNsPrefix("ottr", "http://faulty-ns.ottr.xyz/0.4/");
        prefixes.setNsPrefix("xsd", "http://faulty/XMLSchema#");

        Result<PrefixMapping> result =  PrefixValidator.check(prefixes);
        Assertions.atLeast(result, Message.Severity.WARNING);
        assertTrue(result.getAllMessages().size() >= 3);
    }

    @Test
    public void check_standard_prefix() {
        PrefixMapping prefixes = PrefixMapping.Factory.create();
        prefixes.setNsPrefix("my-rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        prefixes.setNsPrefix("my-rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prefixes.setNsPrefix("my-xsd", "http://www.w3.org/2001/XMLSchema#");

        Result<PrefixMapping> result =  PrefixValidator.check(prefixes);
        Assertions.atLeast(result, Message.Severity.WARNING);
        assertTrue(result.getAllMessages().size() >= 3);
    }
}
