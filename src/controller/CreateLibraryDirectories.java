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

public class CreateLibraryDirectories {

	public static void main(String[] args) {
		
		String inputCompany = args[0];
		String company = args[1];
		MsSQL dbMSSQL = new MsSQL(inputCompany);
		StringBuilder text = new StringBuilder();
		
		Connection connMSSQL = null;
		try {	
			connMSSQL = dbMSSQL.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String selectSql1 = "Select rflib from qdspfdrcdf "
						  + "Where rffatr = 'PF' "
	 			   	  	  + "Group by rflib "
	 			   	 	  + "Order by rflib";
		
		String selectSql2 = "Select count(*) as numberOfRecords"
						  + " From qdspfdrcdf"
						  + " Where rflib = ? And rffatr = 'PF'"
		   		 	  	  + " Group by rflib"
		   		 	  	  + " Order by rflib";
		
		PreparedStatement checkStmt1;
		try {
			checkStmt1 = connMSSQL.prepareStatement(selectSql1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsSelect1 = checkStmt1.executeQuery();
			while (resultsSelect1.next()) {
				String libraryName = resultsSelect1.getString(1).trim().toLowerCase();
				try {
					PreparedStatement checkStmt2 = connMSSQL.prepareStatement(selectSql2);
					checkStmt2.setString(1, libraryName.trim());
					ResultSet resultsSelect2 = checkStmt2.executeQuery();
					resultsSelect2.next();
					int count = resultsSelect2.getInt(1);
					if (count > 1) {
						text.append("md " + libraryName + "\n");
						System.out.println(libraryName);
					} else {
						System.out.println(libraryName + " not added");
					}
					resultsSelect2.close();
					checkStmt2.close();
				} catch (SQLException e) {
					System.out.println(libraryName + " not added");
				}
			}
			resultsSelect1.close();
			checkStmt1.close();
		    dbMSSQL.closeConnection(connMSSQL);
		    connMSSQL.close();
			try (FileOutputStream out = new FileOutputStream(new File(
		    		"C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + company + "\\data\\createdirectories.bat"))) {
				out.write(text.toString().getBytes());
				text.setLength(0);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}