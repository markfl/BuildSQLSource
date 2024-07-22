package controller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import model.CheckTime;
import model.DBClassBuilder;
import model.MsSQL;

public class BuildUploadList {

	public static void main(String[] args) {
		
		CheckTime ct = new CheckTime();

		String company = args[0];
		Connection connMSSQLLibList = null;
		MsSQL dbMSSQLLibList = new MsSQL("liblist");
		DBClassBuilder cb = new DBClassBuilder();
		
		try {
			connMSSQLLibList = dbMSSQLLibList.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String fileListFile = company + "uploadfilelist";
		String liblist = company + "liblist";
		String resequence = company + "liblistreseq";
		String deleteSql1 = "delete from " + fileListFile;
		String deleteSql2 = "delete from rawdatasource where company = ?";
		String deleteSql3 = "delete from rawdatafiles where company = ?";
		String insertSql1 = "insert into " + fileListFile + " (library, filename, recordcount) "
		 		 + "values (?, ?, ?)";
		String insertSql2 = "insert into rawdatasource (company, library, longlibrary, datadirectory) "
		 		 + "values (?, ?, ?, ?)";
		String insertSql3 = "insert into rawdatafiles (company, library, longlibrary, datadirectory, filename, longfilename, recordcount) "
		 		 + "values (?, ?, ?, ?, ?, ?, ?)";
		String updateSql1 = "update " + liblist + " set runoption = ? where library = ?"; 
		String updateSql2 = "update " + resequence + " set runoption = ? where library = ?";
		
		try {
			System.out.println("Clearing " + fileListFile);
			PreparedStatement deleteStmt = connMSSQLLibList.prepareStatement(deleteSql1);
			deleteStmt.executeUpdate();
			System.out.println("Clearing rawdatasource");
			deleteStmt = connMSSQLLibList.prepareStatement(deleteSql2);
			deleteStmt.setString(1, company);
			deleteStmt.executeUpdate();
			System.out.println("Clearing rawdatafiles");
			deleteStmt = connMSSQLLibList.prepareStatement(deleteSql3);
			deleteStmt.setString(1, company);
			deleteStmt.executeUpdate();
			deleteStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try (BufferedReader dirin = new BufferedReader(new 
				InputStreamReader(new FileInputStream("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + company + "\\data\\liblist"), "UTF-8"))) {			String library;
			while ((library  = dirin.readLine()) != null ) {
				PreparedStatement insertStmt = connMSSQLLibList.prepareStatement(insertSql2);
				insertStmt.setString(1, company);
				insertStmt.setString(2, library);
				insertStmt.setString(3, company + "_" + library);
				insertStmt.setString(4, library);
				insertStmt.executeUpdate();
				try (BufferedReader in = new BufferedReader(new 
						InputStreamReader(new FileInputStream("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + company + "\\data\\" + library + "\\filestoread.txt"), "UTF-8"))) {
					
					String line;
					while ((line  = in.readLine()) != null ) {
						int a = line.indexOf(".");
						String fileName = line.substring(0, a);
						insertStmt = connMSSQLLibList.prepareStatement(insertSql1);
						insertStmt.setString(1, library);
						insertStmt.setString(2, fileName);
						String fileInputStream = new String();
						fileInputStream = "C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + company + "\\data\\" + library + "\\" + fileName + ".csv";
						double counterTotal = cb.getRecordCount(company, library, fileName, fileInputStream);
						insertStmt.setInt(3, (int) counterTotal);
						insertStmt.executeUpdate();
						insertStmt = connMSSQLLibList.prepareStatement(insertSql3);
						String dataDirectory = library;
						String setLibrary = library;
						if (company.equals("cohere")) {
							if (library.equals("qs36f1")) {
								setLibrary = "qs36f";
							} else if (library.equals("qs36f2")) {
								setLibrary = "qs36f";
							} else if (library.equals("qs36f3")) {
								setLibrary = "qs36f";
							} else if (library.equals("qs36f4")) {
								setLibrary = "qs36f";
							} else if (library.equals("qs36f5")) {
								setLibrary = "qs36f";
							}
						}
						if (company.equals("walsworth")) {
							if (library.equals("mfgdblib1")) {
								setLibrary = "mfgdblib";
							}
						}
						insertStmt.setString(1, company);
						insertStmt.setString(2, setLibrary);
						insertStmt.setString(3, company + "_" + setLibrary);
						insertStmt.setString(4, dataDirectory);
						insertStmt.setString(5, fileName);
						insertStmt.setString(6, setLibrary + "_" + fileName);
						insertStmt.setInt(7, (int) counterTotal);
						insertStmt.executeUpdate();
						insertStmt.close();
						System.out.println("File " + fileName + " in Library " + library + " added to " + fileListFile);
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
				PreparedStatement updateStmt = connMSSQLLibList.prepareStatement(updateSql1);
				updateStmt.setString(1, "y");
				updateStmt.setString(2, library);
				updateStmt.executeUpdate();
				updateStmt = connMSSQLLibList.prepareStatement(updateSql2);
				updateStmt.setString(1, "y");
				updateStmt.setString(2, library);
				updateStmt.executeUpdate();
				updateStmt.close();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		// Check for running walsworth
		if (company.equals("walsworth")) {
			String updateSql = "update rawdatasource set library = ?, longlibrary = ? "
			 		 + "where datadirectory = 'mfgdblib1'";
			try {
				PreparedStatement updateStmt = connMSSQLLibList.prepareStatement(updateSql);
				updateStmt.setString(1, "mfgdblib");
				updateStmt.setString(2, "walsworth_mfgdblib");
				updateStmt.executeUpdate();
				updateStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			dbMSSQLLibList.closeConnection(connMSSQLLibList);
			connMSSQLLibList.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String returnString = ct.calculateElapse("Build Upload File List");
		System.out.println(returnString);
	}
}