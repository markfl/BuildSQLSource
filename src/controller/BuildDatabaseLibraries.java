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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.CheckTime;
import model.MsSQL;

public class BuildDatabaseLibraries {

	public static void main(String[] args) {
		
		StringBuilder createText = new StringBuilder();
		StringBuilder deleteText = new StringBuilder();
		
		CheckTime ct = new CheckTime();
		
		String company = args[0];
		String libraryList = company + "liblistreseq";

		Connection connLibrary = null;
		MsSQL dbLibrary = new MsSQL("liblist");
		try {
			connLibrary = dbLibrary.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String companySql = new String();
		companySql = "Select * from " + libraryList
				   + " Where runoption = 'y'"
				   + " Order by library";
		ArrayList<String> createdb = new ArrayList<String>();
		
		PreparedStatement checkStmt;
		try {
			try (BufferedReader in = new BufferedReader(new 
					InputStreamReader(new FileInputStream("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\createdb.sql"), "UTF-8"))) {
				String line;
				while ((line  = in.readLine()) != null ) {
					createdb.add(line);
				}
			} 
			checkStmt = connLibrary.prepareStatement(companySql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsSelect = checkStmt.executeQuery();
			String fromScan = "libraryname";
			while (resultsSelect.next()) {
				String libraryName = resultsSelect.getString(3).trim().toLowerCase();
				String newLibrary = company + "_" + libraryName;
				for (String element : createdb) {
					element = element.replace(fromScan, newLibrary);
					createText.append(element + "\n");
				}
				deleteText.append("DROP DATABASE " + newLibrary + "\n");
			}
			resultsSelect.close();
			checkStmt.close();
			dbLibrary.closeConnection(connLibrary);
			connLibrary.close();
			for (String element : createdb) {
				element = element.replace(fromScan, company);
				createText.append(element + "\n");
			}
			deleteText.append("DROP DATABASE " + company + "\n");
			try (FileOutputStream createOut = new FileOutputStream(new File("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + company + "\\createdball.sql"))) {
				createOut.write(createText.toString().getBytes());
				createText.setLength(0);
			}
			try (FileOutputStream deleteOut = new FileOutputStream(new File("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + company + "\\deletedball.sql"))) {
				deleteOut.write(deleteText.toString().getBytes());
				deleteText.setLength(0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String returnString = ct.calculateElapse("Build Database Libraries");
		System.out.println(returnString);
	}
}