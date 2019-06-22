package xyz.ottr.lutra.bottr.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.jena.ext.com.google.common.io.Files;

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

public class CSVSource extends JDBCSource {

    public static File testFolder = Files.createTempDir();
    private String input;
    private String eolChar;
    private char separator;
    private char encloser;
    private boolean hasHeader;

    public CSVSource(String in, char sep, char enc, boolean header) {

        super("org.h2.Driver", "jdbc:h2:" + testFolder.getAbsolutePath() + "/db", "user", "pass");

        this.input = in;
        this.separator = sep;
        this.encloser = enc;
        this.hasHeader = header;
    }
    
    @Override
    public ResultStream<Record<String>> execute(String query) {

        // Determine EOL character
        BufferedReader reader;
             
        try {
            reader = new BufferedReader(new FileReader(input));
            String line = reader.readLine();
            if (line.substring(line.length() - 2) == "\r\n") {
                eolChar = "\\r\\n";
            } else
                if (line.substring(line.length() - 1) == "\n") {
                    eolChar = "\\n";
                }
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try (Connection conn = super.dataSource.getConnection()) {
            // Load CSV into database
            Statement stmt = conn.createStatement();
            //stmt.execute("DROP TABLE IF EXISTS " + input + ";");
            String loaderQuery = "LOAD DATA INFILE '" + input + "'"
                                 + " INTO TABLE " + input + " "
                                 + " FIELDS TERMINATED BY '" + separator + "'"
                                 + " ENCLOSED BY '" + encloser + "'"
                                 + " LINES TERMINATED BY '" + eolChar + "'";
            if (hasHeader == true) {
                loaderQuery = loaderQuery + " IGNORE 1 ROWS;";
            }
            stmt.execute(loaderQuery);
            
            List<Record<String>> rows = new QueryRunner().query(conn, query, new ArrayListHandler())
                    .stream()
                    .map(array -> Arrays.asList(array)
                            .stream()
                            .map(value -> Objects.toString(value, null)) // the string value of null is (the object) null.
                            .collect(Collectors.toList()))
                    .map(Record::new)
                    .collect(Collectors.toList());
            
            return ResultStream.innerOf(rows);

        } catch (SQLException ex) {
            ex.printStackTrace();
        } 
        return null;
    }
}