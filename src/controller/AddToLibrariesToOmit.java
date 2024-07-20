package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.MsSQL;

public class AddToLibrariesToOmit {

	public static void main(String[] args) {

		Connection connLibListMSSQL = null;
		String company = args[0];
		String liblistFile = company.trim() + "liblist";
		
		MsSQL dbLibListMSSQL = new MsSQL("liblist");
		try {
			connLibListMSSQL = dbLibListMSSQL.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String selectSql = "Select library from " + liblistFile
				+ " Where sequence = 999"
				+ " Order by library";
		String insertSql = "insert into librariestoomit (company, library) "
				 + "values (?, ?)";
		
		PreparedStatement checkStmt;
		try {
			checkStmt = connLibListMSSQL.prepareStatement(selectSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsSelect = checkStmt.executeQuery();
			while (resultsSelect.next()) {
				String libraryName = resultsSelect.getString(1).trim().toLowerCase();
				PreparedStatement insertStmt = connLibListMSSQL.prepareStatement(insertSql);
				insertStmt.setString(1, company.trim());
				insertStmt.setString(2, libraryName.trim());
				insertStmt.executeUpdate();
				System.out.println(libraryName);
			}
			resultsSelect.close();
			checkStmt.close();
			dbLibListMSSQL.closeConnection(connLibListMSSQL);
			connLibListMSSQL.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}