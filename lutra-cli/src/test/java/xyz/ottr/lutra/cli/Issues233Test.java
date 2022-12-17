package xyz.ottr.lutra.cli;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-cli
 * %%
 * Copyright (C) 2018 - 2021 University of Oslo
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.io.RDFIO;
import xyz.ottr.lutra.wottr.parser.WInstanceParser;


@Isolated
public class Issues233Test {

    private static final String ROOT = "src/test/resources/issues/233/";
    
    /*
     * Issue#233, Expansion mode type error.
     * Expanding cross  | ottr:Triple( ++(ex:a1, ex:b1, ex:c1), ex:r1, ++(ex:A1, ex:B1)) . gives the following error.
     * [ERROR] Argument type error. Argument ++<http://example.com/ns#a1 : LUB<ottr:IRI>, http://example.com/ns#b1 : LUB<ottr:IRI>, http://example.com/ns#c1 : LUB<ottr:IRI>>(id: 2) (index 0) in instance http://ns.ottr.xyz/0.4/Triple[++<http://example.com/ns#a1 : LUB<ottr:IRI>, http://example.com/ns#b1 : LUB<ottr:IRI>, http://example.com/ns#c1 : LUB<ottr:IRI>>(id: 2), http://example.com/ns#r1 : LUB<ottr:IRI>, ++<http://example.com/ns#A1 : LUB<ottr:IRI>, http://example.com/ns#B1 : LUB<ottr:IRI>>(id: 4)] has a type NEList<LUB<rdfs:Resource>> which is incompatible with the type of the corresponding parameter ?_:s : ottr:IRI.
     * 
     * 
     * Remarks: Test passed successfully, error not reproduced.
     */
    
    @Test
    public void test() {
        CLIRunner.run(" "
                + " --library " + ROOT + "template.stottr"
                + " --libraryFormat stottr"
                + " -O wottr"
                + " -o " + ROOT + "output.ttl"
                + " --inputFormat stottr"
                + " " + ROOT + "instance.stottr");
        
        //Parse generated output file, number of triples should be equal to cross product, here it is 6
                
        InstanceReader instanceReader = new InstanceReader(RDFIO.fileReader(), new WInstanceParser());            
        List<Result<Instance>> ins = instanceReader
                .apply(ROOT + "/output.ttl")
                .collect(Collectors.toList());
        
        int expectedNumberOfTripels = 6;
        int actualNumberOfTriples = ins.size();
        assertEquals(expectedNumberOfTripels, actualNumberOfTriples);

    }
    
    @Test
    public void testWithoutTemplate() {
        /**Test with instance not in template,
         *  
         *  @prefix ottr: <http://ns.ottr.xyz/0.4/> .
         *  @prefix ex: <http://example.org/> .
         *
         *  cross  | ottr:Triple( ++(ex:a1, ex:b1, ex:c1), ex:r1, ++(ex:A1, ex:B1)) .
         *  
         */
        
        CLIRunner.run(" "
                + " -O wottr"
                + " -o " + ROOT + "outputWithoutTemplate.ttl"
                + " --inputFormat stottr"
                + " " + ROOT + "instanceWithoutTemplate.stottr");
        
        //Parse generated output file, number of triples should be equal to cross product, here it is 6
                
        InstanceReader instanceReader = new InstanceReader(RDFIO.fileReader(), new WInstanceParser());            
        List<Result<Instance>> ins = instanceReader
                .apply(ROOT + "/outputWithoutTemplate.ttl")
                .collect(Collectors.toList());
        
        int expectedNumberOfTripels = 6;
        int actualNumberOfTriples = ins.size();
        assertEquals(expectedNumberOfTripels, actualNumberOfTriples);
        
    }

}
