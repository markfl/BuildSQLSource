package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.MsSQL;

public class BuildDropSQL {
	
	static StringBuilder text = new StringBuilder();

	public static void main(String[] args) {
		
		String company = args[0];
		
		String selectSql = "SELECT * FROM "
				+ company
				+ ".INFORMATION_SCHEMA.TABLES"
				+ " WHERE TABLE_TYPE = 'BASE TABLE'"
				+ " order by TABLE_NAME";

		
		Connection connMSSQL = null;
		MsSQL dbMSSQL = new MsSQL(company);
		try {
			connMSSQL = dbMSSQL.connect();
			PreparedStatement checkStmtSelect = connMSSQL.prepareStatement(selectSql);
			ResultSet resultsSelect = checkStmtSelect.executeQuery();
		    while (resultsSelect.next()) {
		    	String fileName = resultsSelect.getString(3);
		    	String lineout = "drop table " + fileName + ';';
				text.append(lineout + "\n");
		    }
		    resultsSelect.close();
		    checkStmtSelect.close();
		    dbMSSQL.closeConnection(connMSSQL);
		    connMSSQL.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try (FileOutputStream out = new FileOutputStream(new File("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + company + "\\dropmy.sql"))) {
			out.write(text.toString().getBytes());
			System.out.println("Program completed normally.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}