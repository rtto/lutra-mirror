package xyz.ottr.lutra;
import java.sql.*;

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

public class MapJDBC extends Map {


    public Result<Instance> mapToInstance(Row row) {
        return null; // TODO
    }

    public ResultStream<Row> execute() {
    	
    	 try{
    	      //Register driver
    	      Class.forName(getDriver());

    	      //Open connection
    	      conn = DriverManager.getConnection(getURL(),getUser(),getPassword());

    	      //Execute query
    	      stmt = conn.createStatement();
    	      ResultSet rs = stmt.executeQuery(getQuery());

    	      //Parse the data
    	      int colcount = rs.getMetaData().getColumnCount();
    	      
    	      private ResultStream<Row> rowStream = new ResultStream<Row>();
    	      
    	      while(rs.next()){
    	    	  List<String> rowAsList = new ArrayList<>();
    	    	  for(int i = 0; i < colcount; i++)
    	    	  {
    	    		  rowAsList.add(rs.getString(i));
    	    	  }
    	    	  rowStream.add(new Row(rowAsList));
    	      }
    	      
    	      //Clean up
    	      rs.close();
    	      stmt.close();
    	      conn.close();
    	   }catch(SQLException se){
    	      //Handle errors for JDBC
    	      se.printStackTrace();
    	   }catch(Exception e){
    	      //Handle errors for Class.forName
    	      e.printStackTrace();
    	   }finally{
    	      //finally block used to close resources
    	      try{
    	         if(stmt!=null)
    	            stmt.close();
    	      }catch(SQLException se2){
    	      }// nothing we can do
    	      try{
    	         if(conn!=null)
    	            conn.close();
    	      }catch(SQLException se){
    	         se.printStackTrace();
    	      }//end finally try
    	   }//end try
    	
    	 return rowStream;
    }
    
    
    //Returns the correct driver. Based on the type field maybe?
    private String getDriver() {
    	return null; //TODO
    }
    
    //Returns the URL. Should be contained in the source field
    public String getURL() {
    	return null; //TODO
    }
    
    //Returns the user name for accessing the database. Should be contained in the source field
    private String getUser() {
    	return null; //TODO
    }
    
    //Returns the password used to access the database. Should be contained in the source field
    private String getPassword() {
    	return null; //TODO
    }
    

}
