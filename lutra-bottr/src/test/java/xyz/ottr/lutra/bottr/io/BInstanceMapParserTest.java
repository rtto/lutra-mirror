package xyz.ottr.lutra.bottr.io;

/*-
 * #%L
 * lutra-bottr
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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;

import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.wottr.util.ModelIO;

public class BInstanceMapParserTest {

    @Test
    public void shouldParse() {
        Model model = ModelIO.readModel("instanceMap1.ttl");
        BInstanceMapParser parser = new BInstanceMapParser();
        
        List<InstanceMap> maps = parser.apply(model).innerCollect(Collectors.toList());
                
        assertEquals(1, maps.size());
        assertEquals("SELECT name, age, company FROM TABLE tblEmployee", maps.get(0).getQuery());
        assertEquals("http://example.com/tpl#MyTemplate", maps.get(0).getTemplateIRI());
    }

}
