package xyz.ottr.lutra.bottr.source;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import xyz.ottr.lutra.bottr.model.Record;
import xyz.ottr.lutra.result.ResultStream;

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

public class XMLSourceTest {
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void prototypeTest() {
        
        
        String root = "C:\\";
        String url = root + "books.xml";
        
        XMLSource source = new XMLSource(url);

        //Create expected result
        Set<Record<String>> expected = new HashSet<>();
        expected.add(new Record<>(Arrays.asList("Everyday Italian", "Giada De Laurentiis", "2005", "30.00")));
        expected.add(new Record<>(Arrays.asList("Harry Potter", "J K. Rowling", "2005", "29.99")));
        
        ResultStream<Record<String>> rowStream = source.execute("doc(\"" + url + "\")/bookstore/book[price<35]");
        Set<Record<String>> dbOutput = rowStream.innerCollect(Collectors.toSet());

        //Compare dbOutput to expected result
        Assert.assertEquals(dbOutput, expected);
    }
}