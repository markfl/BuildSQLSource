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

public class CreateCSVFiles {

	public static void main(String[] args) {
		
		String inputCompany = args[0];
		String company = args[1];
		MsSQL dbMSSQL = new MsSQL(inputCompany);
		StringBuilder text = new StringBuilder();
		String dir = "C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\";
		
		Connection connMSSQL = null;
		try {
			connMSSQL = dbMSSQL.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String selectSql = "Select rflib, rffile from qdspfdrcdf "
						 + "Where rffatr = 'PF'"
	 			   		 + "Group by rflib, rffile "
	 			   		 + "Order by rflib, rffile";
		
		PreparedStatement checkStmt1;
		try {
			checkStmt1 = connMSSQL.prepareStatement(selectSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsSelect = checkStmt1.executeQuery();
			while (resultsSelect.next()) {
				String libraryName = resultsSelect.getString(1).trim().toLowerCase();
				String fileName = resultsSelect.getString(2).trim().toLowerCase();
				text.append(libraryName + " " + fileName);
				System.out.println(libraryName + " " + fileName);
				try (FileOutputStream out = new FileOutputStream(new File(
					dir + company + "\\data\\" + libraryName + "\\" + fileName + ".csv"))) {
					out.write(text.toString().getBytes());
					text.setLength(0);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
