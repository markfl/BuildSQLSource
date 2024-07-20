package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import model.MsSQL;

public class BuildFileListBat {
	
	static StringBuilder text = new StringBuilder();
	static StringBuilder textdlt = new StringBuilder();

	public static void main(String[] args) {
		
		String company = args[0];
		
		Connection connLibListMSSQL = null;
		MsSQL dbLibListMSSQL = new MsSQL("liblist");
		try {
			connLibListMSSQL = dbLibListMSSQL.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String deleteSql = "delete from librariestocopy where company = ?";
		String insertSql = "insert into librariestocopy (company, library) "
				 + "values (?, ?)";
		
		System.out.println("Clearing librariestocopy");
		try {
			PreparedStatement deleteStmt = connLibListMSSQL.prepareStatement(deleteSql);
			deleteStmt.setString(1, company.trim());
			deleteStmt.executeUpdate();
			deleteStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try (BufferedReader in = new BufferedReader(new 
				InputStreamReader(new FileInputStream("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + company + "\\data\\liblist"), "UTF-8"))) {
			String line;
			while ((line  = in.readLine()) != null ) {
				String libName = line;
				text.append("cd " + libName + "\n");
				text.append("dir /b /os *.csv > filestoread.txt\n");
				text.append("cd ..\n");
				textdlt.append("cd " + libName + "\n");
				textdlt.append("del filestoread.txt\n");
				textdlt.append("cd ..\n");
				PreparedStatement insertStmt = connLibListMSSQL.prepareStatement(insertSql);
				insertStmt.setString(1, company.trim());
				insertStmt.setString(2, libName.trim());
				insertStmt.executeUpdate();
				insertStmt.close();
		    }
			dbLibListMSSQL.closeConnection(connLibListMSSQL);
			connLibListMSSQL.close();
			System.out.println("Program completed normally.");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try (FileOutputStream out = new FileOutputStream(new File(
	    		"C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + company + "\\data\\buildread.bat"))) {
			out.write(text.toString().getBytes());
			text.setLength(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (FileOutputStream out = new FileOutputStream(new File(
	    		"C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + company + "\\data\\deleteread.bat"))) {
			out.write(textdlt.toString().getBytes());
			textdlt.setLength(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}