package edu.ncsu.csc.emerson.generics.access;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/*
 * Used for inserting MySQL SQL to Access
 */
public class SQLtoAccess {
	
	static String sqlFileLocation = "C:/Users/Adminuser/Documents/My Dropbox/workspace/Generics Analysis2/output.sql";
	
	public static void main(String[] args) throws Exception {

		Connection connection = getConnection();
		Statement s = connection.createStatement();
		
		FileInputStream fis = new FileInputStream(sqlFileLocation);
		DataInputStream dis = new DataInputStream(fis);
		BufferedReader br = new BufferedReader(new InputStreamReader(dis));
	    String line;
	    while ((line = br.readLine()) != null)   {
	    	if(!line.contains("IF EXISTS")){//have to do this manually
//	    		strLine = strLine.replace("VARCHAR(50)", "CHAR");
//	    		strLine = strLine.replace("INT", "INTEGER");
//	    		strLine = strLine.replace("TIMESTAMP", "DATE");
	    		line = line.replace("(time", "(\"time\"");
	    		try {
					s.execute(line);
				} catch (Exception e) {
					System.err.println(line);
					e.printStackTrace();
				}
	    	}
	    }
	    dis.close();
	    s.close();
	    connection.close();
		
	}

	//need to set up ODBC connection for this to work
	public static Connection getConnection() throws Exception {
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		Connection c = DriverManager
				.getConnection("jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};" +
						"DBQ=C:/Users/Adminuser/Documents/java_generics_working_copy.accdb");
		return c;
	}

}
