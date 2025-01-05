package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import model.CheckTime;
import model.MsSQL;

public class CreateSQLSource {
	
	static StringBuilder create = new StringBuilder();
	static StringBuilder alterCreate = new StringBuilder();
	static StringBuilder drop = new StringBuilder();
	static StringBuilder view = new StringBuilder();
	static StringBuilder dropview = new StringBuilder();
	static StringBuilder index = new StringBuilder();
	static StringBuilder dropindex = new StringBuilder();
	
	private static Connection connMSSQL;
	private static Connection connLibrary;
	private static String fromLibrary;
	private static String company;
	private static String db;
	private static String includeLibrary;
	private static String currentLibrary;
	private static ArrayList<String> filesSoFarTables = new ArrayList<String>();
	private static String filesSoFarTableName = new String();
	private static ArrayList<String> filesSoFarViews = new ArrayList<String>();
	private static String filesSoFarViewName = new String();
	private static ArrayList<String> filesNotIncludedInViews = new ArrayList<String>();
	private static ArrayList<String> filesSoFarIndexs = new ArrayList<String>();
	private static String filesSoFarIndexName = new String();
	private static ArrayList<String> joinedPhysicalFiles = new ArrayList<String>();
	private static Collection<ArrayList<String>> allJoinedFiles = new ArrayList<ArrayList<String>>();
	private static ArrayList<String> allFields = new ArrayList<String>();
	private static ArrayList<String> joinedFields = new ArrayList<String>();
	private static ArrayList<String> originalField = new ArrayList<String>();
	private static ArrayList<String> aliasField = new ArrayList<String>();
	private static Boolean hasConcatField;
	private static Boolean hasSubStringField;
	private static Boolean isJoinFile;
	private static CheckName cn;
	private static String PhysicalFileInd = "P";
	private static String LogicalFileInd = "L";

	public static void main(String[] args) {
		
		CheckTime ct = new CheckTime();
		
		String fromLibrary = args[0];
		String company = args[1];
		String db = args[2];
		includeLibrary = new String();
		if (args.length >= 4) {
			includeLibrary = args[3];
		}
		String libraryList = company + "liblistreseq";
		
		int tablesCreated = 0;
		int viewsCreated = 0;
		int indexesCreated = 0;
		int libCount = 0;
		int currentCount = 0;
		
		setFromLibrary(fromLibrary);
		setCompany(company);
		setDb(db);
		setIsJoinFile(false);

		connMSSQL = null;
		connLibrary = null;
		MsSQL dbMSSQL = new MsSQL(fromLibrary);
		MsSQL dbLibrary = new MsSQL("liblist");
		try {
			connMSSQL = dbMSSQL.connect();
			connLibrary = dbLibrary.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		cn = new CheckName(connLibrary);
		
		String companySql = new String();
		String countSQL = new String();
		if (includeLibrary.isEmpty()) {
			companySql = "Select * from " + libraryList
					   + " Where runoption = 'y'"
					   + " Order by sequence, library";
			countSQL = "Select count(*) as numberOfRecords from " + libraryList
					   + " Where runoption = 'y'";
		} else {
			companySql = "Select * from " + libraryList
					   + " Where library = '" + includeLibrary + "'";
		}
		try {
			if (includeLibrary.isEmpty()) {
				PreparedStatement checkStmt1 = connLibrary.prepareStatement(countSQL);;
				ResultSet resultsSelect1 = checkStmt1.executeQuery();
				resultsSelect1.next();
				libCount = resultsSelect1.getInt(1);
			} else libCount = 1;
			
			if (libCount > 0) {
				System.out.println(libCount + " libraries to build." );
			}
			PreparedStatement checkStmt2 = connLibrary.prepareStatement(companySql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsSelect2 = checkStmt2.executeQuery();
			while (resultsSelect2.next()) {
				Boolean firstFile = true;
				filesSoFarTables = new ArrayList<String>();
				filesSoFarViews = new ArrayList<String>();
				filesSoFarIndexs = new ArrayList<String>();
				String libraryName = resultsSelect2.getString(3).trim().toLowerCase();
				String origLibraryName = resultsSelect2.getString(4).trim().toLowerCase();
				libraryName = cn.checkFieldName(libraryName);
				currentLibrary = libraryName;
				tablesCreated += createTable(origLibraryName, firstFile);
				viewsCreated += createView(origLibraryName, viewsCreated, firstFile);
				if (viewsCreated == 0)
					viewsCreated += createJoinView(origLibraryName, viewsCreated, true);
				else
					viewsCreated += createJoinView(origLibraryName, viewsCreated, false);
				if (filesNotIncludedInViews.size() > 0) 
					viewsCreated += createNewView(origLibraryName, viewsCreated, firstFile);
				indexesCreated += createIndex(origLibraryName, firstFile);
				firstFile = false;
				currentCount += 1;
				System.out.println(currentCount + " libraries created. " + (libCount - currentCount) + " to go." );
			}
			WriteCreateSQLTable("Create", "Drop");
			WriteAlterCreateSQLTable();
			WriteCreateSQLView("View", "DropView");
			WriteCreateSQLIndex("Index");
			System.out.println("Program completed normally, " + tablesCreated + " create table scripts created.");
			System.out.println("Program completed normally, " + viewsCreated + " create view scripts created.");
			System.out.println("Program completed normally, " + indexesCreated + " create index scripts created.");
			dbMSSQL.closeConnection(connMSSQL);
			dbLibrary.closeConnection(connLibrary);
			connMSSQL.close();
			connLibrary.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String returnString = ct.calculateElapse("Build SQL function complete");
		System.out.println(returnString);
	}
	
	static public int createTable(String selectLibrary, Boolean firstFile) {
	
		Collection<ArrayList<String>> fields = new ArrayList<ArrayList<String>>();
		String selectSql1 = new String();
		String selectSql2 = new String();
		
		selectSql1 = "Select atfile, atlib from qdspfdbas "
				   + "Where atlib = '" + selectLibrary + "' "
				   + "And atfila = '*PHY' And atdtat = 'D' "
				   + "And atfile <> 'evfevent' "
				   + "And substring(atfile, 1, 4) <> 'qhst' "
				   + "Order by atlib, atfile";
		selectSql2 = "Select rflen from qdspfdrcdf "
	 			   + "Where rflib = ? "
	 			   + "And rffile = ?";

		String physicalFileName = new String();
		String library = new String();
		int sqlScriptsCreated = 0;
		
		try {	
			PreparedStatement checkStmt1 = connMSSQL.prepareStatement(selectSql1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			PreparedStatement checkStmt2 = connMSSQL.prepareStatement(selectSql2, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsSelect1 = checkStmt1.executeQuery();
			while (resultsSelect1.next()) {
		   		String origPhysicalFileName = resultsSelect1.getString(1).toLowerCase();
		   		
		   		allFields = new ArrayList<String>();
		   		 if (checkFileName(origPhysicalFileName)) {
					physicalFileName = resultsSelect1.getString(1).trim().toLowerCase();
					physicalFileName = cn.checkFieldName(physicalFileName);
					library = resultsSelect1.getString(2).trim().toLowerCase();
					filesSoFarTableName = getCompany() + "_" + library + "." + physicalFileName; 
					checkStmt2 = connMSSQL.prepareStatement(selectSql2, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					checkStmt2.setString(1, library.trim());
					checkStmt2.setString(2, origPhysicalFileName);
					ResultSet resultsSelect2 = checkStmt2.executeQuery();
					while (resultsSelect2.next()) {
						Boolean ableTORun = true;
			   			if (!filesSoFarTables.contains(filesSoFarTableName.trim())) {
			   				int recordLength = resultsSelect2.getInt(1);
			   				if (getDb().equals("mssql")) {
			   					if (recordLength > 8000) {
			   						ableTORun = false;
			   					}
			   				}
			   				if (ableTORun) {
					   			fields = getFieldData(origPhysicalFileName, library, fields, PhysicalFileInd);
					   			if (fields.size() > 0) {
						    		buildSQL(physicalFileName, library, fields, firstFile);
						    		fields = new ArrayList<ArrayList<String>>();
						    		System.out.println("Create SQL Script from library " + library.trim() + " file " + physicalFileName.trim() + " added.");
						    		sqlScriptsCreated++;
						    		filesSoFarTables.add(filesSoFarTableName.trim());
						    		firstFile = false;
				   				}
				   			}
			   			}
					}
					resultsSelect2.close();
					checkStmt2.close();
		   		}
			}
			resultsSelect1.close();
			checkStmt1.close();
			if (sqlScriptsCreated > 0)
				System.out.println(sqlScriptsCreated + " Create SQL scripts created.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return sqlScriptsCreated;
	}
	
	static public int createView(String selectLibrary, int viewSoFar, Boolean firstFile) {
		
		Collection<ArrayList<String>> fields = new ArrayList<ArrayList<String>>();
		int sqlScriptsCreated = 0;

		String selectSql1 = "Select atfile, atlib from qdspfdbas "
				 + "Where atfila = '*LGL' "
				 + "And atlib = '" + selectLibrary + "' "
				 + "And atfile <> 'vrshpa2n' "
				 + "And atfile <> 'fmpfcn4' "
				 + "And atnofm = 1 "
				 + "Order by atlib, atfile";
		String selectSql2 = "Select whrfi from qdspdbr "
				 + "Where whrefi = ? And whreli = ?";
		String selectSql3 = "select count(*) as numberOfRecords from qdspfdjoin "
				 + "Where jnfile = ? And jnlib = ?";
		String selectSql4 = "select count(*) as numberOfRecords from qdspfdsel "
				 + "Where sofile = ? And solib = ?";

		try {	
			PreparedStatement checkStmt1 = connMSSQL.prepareStatement(selectSql1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsSelect1 = checkStmt1.executeQuery();
			while (resultsSelect1.next()) {
				String fileName = resultsSelect1.getString(1).trim().toLowerCase();
				allFields = new ArrayList<String>();
				if (checkFileName(fileName)) {
					filesSoFarViewName = getCompany() + "_" + selectLibrary + "." + fileName.trim();
					if (!filesSoFarViews.contains(filesSoFarViewName)) {
						String libraryName = resultsSelect1.getString(2).trim();
						fileName = cn.checkFieldName(fileName.trim().toLowerCase());
			   			libraryName = libraryName.trim().toLowerCase();
			   			PreparedStatement checkStmt2 = connMSSQL.prepareStatement(selectSql2, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
						checkStmt2.setString(1, fileName);
						checkStmt2.setString(2, libraryName);
						ResultSet resultsSelect2 = checkStmt2.executeQuery();
						if (resultsSelect2.next()) {
							String physicalFileName = resultsSelect2.getString(1).trim().toLowerCase();
							physicalFileName = cn.checkFieldName(physicalFileName);
							filesSoFarTableName = getCompany() + "_" + selectLibrary + "." + physicalFileName.trim();
							if (filesSoFarTables.contains(filesSoFarTableName)) {	
								PreparedStatement checkStmt3;
								checkStmt3 = connMSSQL.prepareStatement(selectSql3);
								checkStmt3.setString(1, fileName);
								checkStmt3.setString(2, libraryName);
								ResultSet resultsSelect3 = checkStmt3.executeQuery();
								if (resultsSelect3.next()) {
									int numberOfRecords = resultsSelect3.getInt(1);
									if (numberOfRecords <= 1) {
										PreparedStatement checkStmt4;
										checkStmt4 = connMSSQL.prepareStatement(selectSql4);
										checkStmt4.setString(1, fileName);
										checkStmt4.setString(2, libraryName);
										ResultSet resultsSelect4 = checkStmt4.executeQuery();
										if (resultsSelect4.next()) {
											numberOfRecords = resultsSelect4.getInt(1);
											if (numberOfRecords > 1) {
												setHasConcatField(false);
												setHasSubStringField(false);
												fields = getFieldData(fileName, libraryName, fields, PhysicalFileInd);
												if (buildSelectView(physicalFileName, fileName, libraryName, fields, firstFile)) {
													System.out.println("Select View SQL Script from library " + libraryName.trim() + " file " + fileName.trim() + " added.");
													firstFile = false;
													sqlScriptsCreated++;
													filesSoFarViews.add(fileName.trim());
												}
												fields = new ArrayList<ArrayList<String>>();
											}
										}
										resultsSelect4.close();
										checkStmt4.close();
									}
								}
								resultsSelect3.close();
								checkStmt3.close();
							}
						}
						resultsSelect2.close();
						checkStmt2.close();
					}
				}
			}
			resultsSelect1.close();
			checkStmt1.close();
			if (sqlScriptsCreated > 0)
				System.out.println(sqlScriptsCreated + " Select View SQL scripts created.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return sqlScriptsCreated;
	}
	
	static public int createJoinView(String selectLibrary, int viewSoFar, Boolean firstFile) {
		
		Collection<ArrayList<String>> fields = new ArrayList<ArrayList<String>>();
		int sqlScriptsCreated = 0;

		String selectSql1 = "Select atfile, atlib from qdspfdbas "
				 + "Where atfila = '*LGL' "
				 + "And atlib = '" + selectLibrary + "' "
				 + "And atfile <> 'vrshpa2n' "
				 + "And atfile <> 'fmpfcn4' "
				 + "Order by atlib, atfile";
		String selectSql2 = "Select whrfi from qdspdbr "
				 + "Where whrefi = ? And whreli = ?";
		String selectSql3 = "select count(*) as numberOfRecords from qdspfdjoin "
				 + "Where jnfile = ? And jnlib = ?";

		try {	
			PreparedStatement checkStmt1 = connMSSQL.prepareStatement(selectSql1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsSelect1 = checkStmt1.executeQuery();
			while (resultsSelect1.next()) {
				setIsJoinFile(false);
				joinedFields = new ArrayList<String>();
				allFields = new ArrayList<String>();
				String fileName = resultsSelect1.getString(1).trim().toLowerCase();
				if (checkFileName(fileName.trim())) {
					filesSoFarViewName = getCompany() + "_" + selectLibrary + "." + fileName.trim();
					if (!filesSoFarViews.contains(fileName.trim())) {
						String libraryName = resultsSelect1.getString(2).trim();
						fileName = cn.checkFieldName(fileName.trim().toLowerCase());
			   			libraryName = libraryName.trim().toLowerCase();
			   			PreparedStatement checkStmt2 = connMSSQL.prepareStatement(selectSql2, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
						checkStmt2.setString(1, fileName);
						checkStmt2.setString(2, libraryName);
						ResultSet resultsSelect2 = checkStmt2.executeQuery();
						if (resultsSelect2.next()) {
							String physicalFileName = resultsSelect2.getString(1).trim().toLowerCase();
							filesSoFarTableName = getCompany() + "_" + selectLibrary + "." + physicalFileName.trim();
							if (filesSoFarTables.contains(filesSoFarTableName)) {	
								PreparedStatement checkStmt3;
								checkStmt3 = connMSSQL.prepareStatement(selectSql3);
								checkStmt3.setString(1, fileName);
								checkStmt3.setString(2, libraryName);
								ResultSet resultsSelect3 = checkStmt3.executeQuery();
								if (resultsSelect3.next()) {
									int numberOfRecords = resultsSelect3.getInt(1);
									if (numberOfRecords > 1) {
										setIsJoinFile(true);
										setHasConcatField(false);
										setHasSubStringField(false);
										fields = getJoinFieldData(fileName, libraryName, fields);
										if (buildSelectJoinView(physicalFileName, fileName, libraryName, fields, firstFile)) {
											System.out.println("Join View SQL Script from library " + libraryName.trim() + " file " + fileName.trim() + " added.");
											firstFile = false;
											sqlScriptsCreated++;
											filesSoFarViews.add(fileName.trim());
										}
										fields = new ArrayList<ArrayList<String>>();
									} else {
										filesNotIncludedInViews.add(fileName.trim());
									}
								}
								resultsSelect3.close();
								checkStmt3.close();
							}
						}
						resultsSelect2.close();
						checkStmt2.close();
					}
				}
			}
			resultsSelect1.close();
			checkStmt1.close();
			if (sqlScriptsCreated > 0)
				System.out.println(sqlScriptsCreated + " Join View SQL scripts created.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return sqlScriptsCreated;
	}
	
	static public int createNewView(String libraryName, int viewSoFar, Boolean firstFile) {
		
		Collection<ArrayList<String>> fields = new ArrayList<ArrayList<String>>();
		int sqlScriptsCreated = 0;
		
		String selectSql1 = "Select whrfi from qdspdbr "
				 + "Where whrefi = ? And whreli = ?";
		String countSQL = "Select count(*) as numberOfRecords from qdspdbr "
				 + "Where whrefi = ? And whreli = ?";
		
		for (String fileName : filesNotIncludedInViews) {
			PreparedStatement checkStmt1;
			try {
				PreparedStatement countStmt = connMSSQL.prepareStatement(countSQL);
				countStmt.setString(1, fileName);
				countStmt.setString(2, libraryName);
				ResultSet resultsCount = countStmt.executeQuery();
				resultsCount.next();
				int libCount = resultsCount.getInt(1);
				if (libCount == 1) {
					checkStmt1 = connMSSQL.prepareStatement(selectSql1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					checkStmt1.setString(1, fileName);
					checkStmt1.setString(2, libraryName);
					ResultSet resultsSelect1 = checkStmt1.executeQuery();
					if (resultsSelect1.next()) {
						String physicalFileName = resultsSelect1.getString(1).trim().toLowerCase();
						filesSoFarTableName = getCompany() + "_" + libraryName + "." + physicalFileName.trim();
						if (filesSoFarTables.contains(filesSoFarTableName)) {
							if (checkFileName(fileName.trim())) {
								filesSoFarViewName = getCompany() + "_" + libraryName + "." + fileName.trim();
								if (!filesSoFarViews.contains(fileName.trim())) {
									fields = new ArrayList<ArrayList<String>>();
									fields = getFieldData(fileName, libraryName, fields, PhysicalFileInd);
									setHasConcatField(false);
									setHasSubStringField(false);
									if (buildNewView(physicalFileName, fileName, libraryName, fields, firstFile)) {
										System.out.println("Logical View SQL Script from library " + libraryName.trim() + " file " + fileName.trim() + " added.");
										firstFile = false;
										sqlScriptsCreated++;
										filesSoFarViews.add(fileName.trim());
									}
								}
							}
						}
					}
					resultsSelect1.close();
					checkStmt1.close();
				} else {
					checkStmt1 = connMSSQL.prepareStatement(selectSql1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					checkStmt1.setString(1, fileName);
					checkStmt1.setString(2, libraryName);
					ResultSet resultsSelect1 = checkStmt1.executeQuery();
					if (resultsSelect1.next()) {
						String physicalFileName = resultsSelect1.getString(1).trim().toLowerCase();
						filesSoFarTableName = getCompany() + "_" + libraryName + "." + physicalFileName.trim();
						if (filesSoFarTables.contains(filesSoFarTableName)) {
							if (checkFileName(fileName.trim())) {
								filesSoFarViewName = getCompany() + "_" + libraryName + "." + fileName.trim();
								if (!filesSoFarViews.contains(fileName.trim())) {
									fields = new ArrayList<ArrayList<String>>();
									fields = getFieldData(fileName, libraryName, fields, LogicalFileInd);
									setHasConcatField(false);
									setHasSubStringField(false);
									if (buildNewView(physicalFileName, fileName, libraryName, fields, firstFile)) {
										System.out.println("Logical View SQL Script from library " + libraryName.trim() + " file " + fileName.trim() + " added.");
										firstFile = false;
										sqlScriptsCreated++;
										filesSoFarViews.add(fileName.trim());
									}
								}
							}
						}
					}
					resultsSelect1.close();
					checkStmt1.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}	
		}
		if (sqlScriptsCreated > 0)
			System.out.println(sqlScriptsCreated + " Logical View SQL scripts created.");
		
		return sqlScriptsCreated;
	}

	static public int createIndex(String selectLibrary, Boolean firstFile) {
		int sqlScriptsCreated = 0;

		String selectSql1 = "Select atfile, atlib, atfila from qdspfdbas "
					 	  + "Where atfila = '*PHY' And atlib = '"  + selectLibrary + "' "
					 	  + "Or atfila = '*LGL' And atlib = '" + selectLibrary + "' "
					 	  + "And atnofm = 1"
					 	  + "Order by atlib, atfile";
		String selectSql2 = "Select whrfi from qdspdbr "
						  + "Where whrefi = ? And whreli = ?";
						  
		try {
			PreparedStatement checkStmt1 = connMSSQL.prepareStatement(selectSql1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsSelect1 = checkStmt1.executeQuery();
			while (resultsSelect1.next()) {
				String fileName = resultsSelect1.getString(1).trim();
				if (checkFileName(fileName.trim())) {
					fileName = cn.checkFieldName(fileName.trim().toLowerCase());
					filesSoFarIndexName = getCompany() + "_" + selectLibrary + "." + fileName.trim();
					if (!filesSoFarIndexs.contains(fileName.trim())) {
						String libraryName = resultsSelect1.getString(2).trim();
			   			libraryName = resultsSelect1.getString(2);
			   			libraryName = libraryName.trim().toLowerCase();
			   			String attribute = resultsSelect1.getString(3);
			   			String physicalFileName = new String();
			   			if (attribute.equals("*LGL")) {
			   				PreparedStatement checkStmt2 = connMSSQL.prepareStatement(selectSql2, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			   				checkStmt2.setString(1, fileName);
			   				checkStmt2.setString(2, libraryName);
			   				ResultSet resultsSelect2 = checkStmt2.executeQuery();
			   				if (resultsSelect2.next()) {
			   					physicalFileName = resultsSelect2.getString(1).trim().toLowerCase();
			   					physicalFileName = cn.checkFieldName(physicalFileName);
			   				}
			   				resultsSelect2.close();
			   				checkStmt2.close();
			   			} else physicalFileName = fileName;
			   			filesSoFarTableName = getCompany() + "_" + selectLibrary + "." + physicalFileName.trim();
			   			if (filesSoFarTables.contains(filesSoFarTableName)) {
			   				filesSoFarViewName = getCompany() + "_" + selectLibrary + "." + fileName.trim();
			   				if (filesSoFarViews.contains(filesSoFarViewName)) {
			   					physicalFileName = fileName.trim();
			   				}
				   			if (!filesSoFarIndexs.contains(filesSoFarIndexName.trim())) {
					   			if (buildIndex(physicalFileName, fileName, libraryName, firstFile)) {
					   				firstFile = false;
									System.out.println("Index SQL Script from library " + libraryName.trim() + " file " + fileName.trim() + " added.");
									sqlScriptsCreated++;
									filesSoFarIndexs.add(filesSoFarIndexName.trim());
					   			}
				   			}
			   			}
					}
				}
			}
			resultsSelect1.close();
			checkStmt1.close();
			if (sqlScriptsCreated > 0)
				System.out.println(sqlScriptsCreated + " Index SQL script created.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return sqlScriptsCreated;
	}
	
	static public Boolean checkFileName(String fileName) {
		if (fileName.contains(".")) return false;
		return true;
	}
	
	static public String checkFieldName(String fieldName) {
		
		String search = fieldName.substring(0, 1);
		Boolean i = search.equals("#");
		if (i) {
			fieldName = "a_" + fieldName.substring(1);
		}
		if (!i) i = search.equals("@");
		if (i) {
			fieldName = "b_" + fieldName.substring(1);
		}
		if (!i) i = search.equals("$");
		if (i) {
			fieldName = "c_" + fieldName.substring(1);
		}
		if (!i) i = search.equals("%");
		if (i) {
			fieldName = "d_" + fieldName.substring(1);
		}
		if (!i) i = search.equals("&");
		if (i) {
			fieldName = "e_" + fieldName.substring(1);
		}
		if (!i) i = search.equals("*");
		if (i) {
			fieldName = "f_" + fieldName.substring(1);
		}
		if (!i) i = search.equals("_");
		if (i) {
			fieldName = "g_" + fieldName.substring(1);
		}
		
		fieldName = fieldName.replace("#", "_a");
		fieldName = fieldName.replace("@", "_b");
		fieldName = fieldName.replace("$", "_c");
		fieldName = fieldName.replace("%", "_d");
		fieldName = fieldName.replace("&", "_e");
		fieldName = fieldName.replace("*", "_f");
		
		try {
			String checkSql = "select count(*) as numberOfRecords from qcrtsqlfld "
					+ "Where fieldnamel = ?";
			PreparedStatement checkStmt;
			checkStmt = connLibrary.prepareStatement(checkSql);
			checkStmt.setString(1, fieldName);
			ResultSet results = checkStmt.executeQuery();
			if (results.next()) {
				int numberOfRecords = results.getInt(1);
				if (numberOfRecords > 0) {
					fieldName = fieldName + "_";
				}
			}
			results.close();
			checkStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return fieldName;
	}
	
	static public Collection<ArrayList<String>> getFieldData(String fileName, String libraryName, Collection<ArrayList<String>> fields, String fileType) {
		
		Collection<String> currentFields = new ArrayList<String>();
		String selectSql1 = "Select whfile, whlib, whflde, whfldb, whfldd, whfldt, whfldp, whftxt, whjref, concat, whfldi, whmap, whmaps, whmapl, whname "
						  + "From qdbasedict Where whfile = ? And whlib = ? "
						  + "Order by whname, whfobo";
		String selectSql2 = "Select apbof From qdspfdacc Where aplib = ? And apbolf = ?";
		
		try {
			PreparedStatement checkStmt1 = connMSSQL.prepareStatement(selectSql1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		    checkStmt1.setString(1, fileName);
		    checkStmt1.setString(2, libraryName);
		    ResultSet resultsSelect1 = checkStmt1.executeQuery();
		    boolean firstRecord = true;
		    while (resultsSelect1.next()) {
		    	fileName = resultsSelect1.getString(1);
		    	libraryName = resultsSelect1.getString(2);
		    	String fieldName = resultsSelect1.getString(3);
				firstRecord = false;
				if (!fieldName.trim().equals("QZG0000031")) {
			    	libraryName = resultsSelect1.getString(2);
			    	fieldName = resultsSelect1.getString(3);
			    	fieldName = cn.checkFieldName(fieldName.trim().toLowerCase());
			    	int fieldSizeAlpha = resultsSelect1.getInt(4);
			    	int fieldSizeNumeric = resultsSelect1.getInt(5);
					String fieldType = resultsSelect1.getString(6);
					int decimal = resultsSelect1.getInt(7);
					String fieldText = resultsSelect1.getString(8);
					int joinReference = resultsSelect1.getInt(9);
					String concat = resultsSelect1.getString(10);
					String interalFieldName = resultsSelect1.getString(11);
					interalFieldName = cn.checkFieldName(interalFieldName.trim().toLowerCase());
					String map = resultsSelect1.getString(12);
					int substringStart = resultsSelect1.getInt(13);
					int substringLength = resultsSelect1.getInt(14);
					String recordFormat = resultsSelect1.getString(15);
					PreparedStatement checkStmt2 = null;
					ResultSet resultsSelect2 = null;
					if (fileType.equals(LogicalFileInd)) {
						checkStmt2 = connMSSQL.prepareStatement(selectSql2, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					    checkStmt2.setString(1, libraryName);
					    checkStmt2.setString(2, recordFormat);
					    resultsSelect2 = checkStmt2.executeQuery();
					}
					currentFields.add(fileName);
					currentFields.add(libraryName);
					currentFields.add(fieldName);
					currentFields.add(Integer.toString(fieldSizeAlpha));
					currentFields.add(Integer.toString(fieldSizeNumeric));
					currentFields.add(fieldType);
					currentFields.add(Integer.toString(decimal));
					currentFields.add(fieldText);
					currentFields.add(Integer.toString(joinReference));
					currentFields.add(concat.trim());
					currentFields.add(interalFieldName);
					currentFields.add(map.trim());
					currentFields.add(Integer.toString(substringStart));
					currentFields.add(Integer.toString(substringLength));
					currentFields.add(recordFormat.trim());
					if (fileType.equals(LogicalFileInd)) {
						if (resultsSelect2.next()) {
							String physicalFileName = resultsSelect2.getString(1);
							currentFields.add(physicalFileName.trim());
						}
						resultsSelect2.close();
					    checkStmt2.close();
					}
					fields = getFileFields(firstRecord, currentFields, fields);
					currentFields = new ArrayList<String>();
				}
		    }
		    resultsSelect1.close();
		    checkStmt1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return fields;
	}
	
	static public Collection<ArrayList<String>> getJoinFieldData(String fileName, String libraryName, Collection<ArrayList<String>> fields) {
		
		Collection<String> currentFields = new ArrayList<String>();
		ArrayList<String> filesJoined = new ArrayList<String>();
		ArrayList<String> joinedFile = new ArrayList<String>();
		Collection<ArrayList<String>> joinedFiles = new ArrayList<ArrayList<String>>();

		String selectSql1 = "Select jndnam, jnjfnm, jnjtnm, jnjfd1, jnjfd2 "
						  + "from qdspfdjoin "
						  + "Where jnfile = ? And jnlib = ? "
						  + "And jnjdsq = ' ' And jnjfrm > 0";
		String selectSql2 = "Select whfile, whlib, whflde, whfldb, whfldd, whfldt, whfldp, whftxt, whjref, concat, whfldi, whmap, whmaps, whmapl, whname "
						  + "from qdbasedict Where whfile = ? And whlib = ? "
						  + "Order by whname, whfobo";
		try {
			PreparedStatement checkStmt1 = connMSSQL.prepareStatement(selectSql1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		    checkStmt1.setString(1, fileName);
		    checkStmt1.setString(2, libraryName);
		    ResultSet resultsSelect1 = checkStmt1.executeQuery();
		    while (resultsSelect1.next()) {
		    	String file = resultsSelect1.getString(1);
		    	file = cn.checkFieldName(file.trim().toLowerCase());
		    	
		    	String jnjfnm = resultsSelect1.getString(2).trim().toLowerCase();
		    	if (!jnjfnm.isEmpty()) {
		    		String jnjtnm = resultsSelect1.getString(3).trim().toLowerCase();
			    	String jnjfd1 = resultsSelect1.getString(4).trim().toLowerCase();
			    	String jnjfd2 = resultsSelect1.getString(5).trim().toLowerCase();
			    	jnjfnm = cn.checkFieldName(jnjfnm);
			    	jnjtnm = cn.checkFieldName(jnjtnm);
			    	jnjfd1 = cn.checkFieldName(jnjfd1);
			    	jnjfd2 = cn.checkFieldName(jnjfd2);
			    	joinedFile.add(jnjfnm);
			    	joinedFile.add(jnjtnm);
			    	joinedFile.add(jnjfd1);
			    	joinedFile.add(jnjfd2);
			    	joinedFiles.add(joinedFile);
			    	joinedFile = new ArrayList<String>();
			    	if (!filesJoined.contains(jnjfnm)) filesJoined.add(jnjfnm);
			    	if (!filesJoined.contains(jnjtnm)) filesJoined.add(jnjtnm);
		    	}
		    }
		    resultsSelect1.close();
		    checkStmt1.close();
		    setAllJoinedFiles(joinedFiles);
		    setJoinedPhysicalFiles(filesJoined);
			PreparedStatement checkStmt2 = connMSSQL.prepareStatement(selectSql2, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		    checkStmt2.setString(1, fileName);
		    checkStmt2.setString(2, libraryName);
		    ResultSet resultsSelect2 = checkStmt2.executeQuery();
		    boolean firstRecord = true;
		    while (resultsSelect2.next()) {
		    	fileName = resultsSelect2.getString(1).trim().toLowerCase();
		    	libraryName = resultsSelect2.getString(2).trim().toLowerCase();
		    	String fieldName = resultsSelect2.getString(3).trim().toLowerCase();
				firstRecord = false;
				if (!fieldName.trim().equals("QZG0000031")) {
			    	fieldName = cn.checkFieldName(fieldName);
			    	int fieldSizeAlpha = resultsSelect2.getInt(4);
			    	int fieldSizeNumeric = resultsSelect2.getInt(5);
					String fieldType = resultsSelect2.getString(6);
					int decimal = resultsSelect2.getInt(7);
					String fieldText = resultsSelect2.getString(8);
					int joinReference = resultsSelect2.getInt(9);
					String concat = resultsSelect2.getString(10);
					String interalFieldName = resultsSelect2.getString(11).trim().toLowerCase();
					if (!interalFieldName.isEmpty()) 
						interalFieldName = cn.checkFieldName(interalFieldName);
					String map = resultsSelect2.getString(12);
					int substringStart = resultsSelect2.getInt(13);
					int substringLength = resultsSelect2.getInt(14);
					String recordFormat = resultsSelect2.getString(15);
					if (joinReference <= filesJoined.size())
						fileName = filesJoined.get(joinReference-1);
					currentFields.add(fileName.trim());
					currentFields.add(libraryName.trim().toLowerCase());
					currentFields.add(fieldName);
					currentFields.add(Integer.toString(fieldSizeAlpha));
					currentFields.add(Integer.toString(fieldSizeNumeric));
					currentFields.add(fieldType.trim());
					currentFields.add(Integer.toString(decimal));
					currentFields.add(fieldText.trim());
					currentFields.add(Integer.toString(joinReference));
					currentFields.add(concat.trim());
					currentFields.add(interalFieldName);
					currentFields.add(map.trim());
					currentFields.add(Integer.toString(substringStart));
					currentFields.add(Integer.toString(substringLength));
					currentFields.add(recordFormat.trim());
					fields = getFileFields(firstRecord, currentFields, fields);
					currentFields = new ArrayList<String>();
				}
		    }
		    resultsSelect2.close();
		    checkStmt2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return fields;
	}

    static public Collection<ArrayList<String>> getFileFields(boolean firstRecord, Collection<String> results, Collection<ArrayList<String>> fields) {
    	
    	Collection<String> fieldList = new ArrayList<String>();
    	
		String fileName = new String();
		String fieldName = new String();
		String fieldType = new String();
		int fieldSizeAlpha = 0;
		int fieldSizeNumeric = 0;
		int decimal = 0;
		String fieldText = new String();
		int joinReference = 0;
		String fieldConcat = new String();
		String interalFieldName = new String();
		String map = new String();
		int substringStart = 0;
		int substringLength = 0;
		String recordFormat = new String();
		String physicalFile = new String();
		int count = 0;
		for (String field : results) {
			count++;
			switch (count) {
				case 1:
					fileName = field.trim();
					break;
				case 3:
					fieldName = field.trim();
					break;
				case 4:
					fieldSizeAlpha = Integer.parseInt(field);
					break;
				case 5:
					fieldSizeNumeric = Integer.parseInt(field);
					break;
				case 6:
					fieldType = field.trim();
					break;
				case 7:
					decimal = Integer.parseInt(field);
					break;
				case 8:
					fieldText = field.trim();
					break;
				case 9:
					joinReference = Integer.parseInt(field);
					break;
				case 10:
					fieldConcat = field.trim();
					if (!fieldConcat.isEmpty())
						setHasConcatField(true);
					break;
				case 11:
					interalFieldName = field.trim();
					break;
				case 12:
					map = field.trim();
					if (!map.equals("Y"))
						setHasSubStringField(true);
					break;
				case 13:
					substringStart = Integer.parseInt(field);
					break;
				case 14:
					substringLength = Integer.parseInt(field);
					break;
				case 15:
					recordFormat = field.trim();
					break;
				case 16:
					physicalFile = field.trim();
					break;
			}
		}
		
		fileName = fileName.toLowerCase();
		fieldName = fieldName.toLowerCase();
		fieldList.add(fileName);
		fieldList.add(fieldName);
		if (db.equals("oracle")) {
			if (fieldSizeNumeric > 4000) {
				fieldList.add("long varchar");
			} else {
				fieldList.add("varchar2");
			}
		} else if (db.equals("mysql")) {
			if (fieldSizeAlpha > 255) {
				fieldList.add("text");
			} else {
				fieldList.add("char");
			}
		}
		if (fieldType.equals("B")) {
			if (db.equals("oracle")) {
				fieldList.add("number");
			}	else if (db.equals("mssql")) {
					if (decimal == 0) fieldList.add("int");
					else fieldList.add("double");
			} else {
				fieldList.add("numeric");
			}
		} else if (fieldType.equals("P")) {
			if (decimal == 0) fieldList.add("int");
			else fieldList.add("double");
		} else if (fieldType.equals("S")) {
			if (db.equals("mssql")) {
				if (decimal == 0) fieldList.add("int");
				else fieldList.add("double");
			} else {
				fieldList.add("numeric");
			}
		} else if (fieldType.equals("A")) {
			fieldList.add("String");
		} else if (fieldType.equals("L")) {
			if (db.equals("mssql")) {
				fieldList.add("char");
			} else {
				fieldList.add("char");
			}
		} else if (fieldType.equals("Z")) {
			if (db.equals("mssql")) {
				fieldList.add("String");
			} else {
				fieldList.add("char");
			}
		} else if (fieldType.equals("T")) {
			if (db.equals("mssql")) {
				fieldList.add("String");
			} else {
				fieldList.add("char");
			}
		} else if (fieldType.equals("E")) {
			if (db.equals("mssql")) {
				fieldList.add("String");
			} else {
				fieldList.add("char");
			}
		} else if (fieldType.equals("O")) {
			if (db.equals("mssql")) {
				fieldList.add("String");
			} else {
				fieldList.add("char");
			}
		} else if (fieldType.equals("H")) {
			if (db.equals("mssql")) {
				fieldList.add("String");
			} else {
				fieldList.add("char");
			}
		} else if (fieldType.equals("F")) {
			if (db.equals("mssql")) {
				fieldList.add("double");
			} else {
				fieldList.add("float");
			}
		} else if (fieldType.equals("G")) {
			if (db.equals("mssql")) {
				fieldList.add("String");
			} else {
				fieldList.add("char");
			}
		} else if (fieldType.equals("1")) {
			if (db.equals("mssql")) {
				fieldList.add("String");
			} else {
				fieldList.add("char");
			}
		} else if (fieldType.equals("3")) {
			if (db.equals("mssql")) {
				fieldList.add("String");
			} else {
				fieldList.add("char");
			}
		} else if (fieldType.equals("5")) {
			if (db.equals("mssql")) {
				fieldList.add("String");
			} else {
				fieldList.add("char");
			}
		} else if (fieldType.equals("6")) {
			if (db.equals("mssql")) {
				fieldList.add("String");
			} else {
				fieldList.add("char");
			}
		} else if (fieldType.isEmpty()) {
			if (db.equals("mssql")) {
				fieldList.add("String");
			} else {
				fieldList.add("char");
			}
		}
		fieldList.add(Integer.toString(fieldSizeAlpha));
		fieldList.add(Integer.toString(fieldSizeNumeric));
		fieldList.add(Integer.toString(decimal));
		fieldList.add(fieldText);
		fieldList.add(Integer.toString(joinReference));
		fieldList.add(fieldConcat);
		fieldList.add(interalFieldName);
		fieldList.add(map);
		fieldList.add(Integer.toString(substringStart));
		fieldList.add(Integer.toString(substringLength));
		fieldList.add(recordFormat);
		fieldList.add(physicalFile);
		fields.add((ArrayList<String>) fieldList);
    	return fields;
    }

	static public void buildSQL(String physicalFileName, String physicalLibraryName, Collection<ArrayList<String>> fields, Boolean firstFile) {
		
		String lineCreate = new String();
		String lineAlter = new String();
		String lineDrop = new String();
		
		if (firstFile) {
			if (db.equals("mssql")) {
				lineCreate += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
				lineAlter += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
				lineDrop += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
			}
		}
		
		lineCreate += "create table " + physicalFileName.trim() + " -- " + physicalLibraryName + "\n(\n";
		lineDrop += "drop table " + physicalFileName.trim()+ "\ngo\n";
		
		int count1 = 0;
		int numberOfFields = fields.size();
		for (ArrayList<String> element : fields) {
			String fieldName = new String();
			String fieldType = new String();
			int fieldSizeAlpha = 0;
			int fieldSizeNumeric = 0;
			int decimal = 0;
			String fieldText = new String();
			count1++;
			int count2 = 0;
			for (String field : element) {
				count2++;
				switch (count2) {
					case 2:
						fieldName = field.trim();
						break;
					case 3:
						fieldType = field.trim();
						break;
					case 4:
						fieldSizeAlpha = Integer.parseInt(field);
						if (db.equals("mssql")) {
							if (fieldType.equals("int") || fieldType.equals("double")) {
								if (fieldSizeAlpha > 38) fieldSizeAlpha = 38;
							}
						}
						break;
					case 5:
						fieldSizeNumeric = Integer.parseInt(field);
						if (db.equals("mssql")) {
							if (fieldType.equals("int")  || fieldType.equals("double")) {
								if (fieldSizeNumeric > 38) fieldSizeNumeric = 38;
							}
						}
						break;
					case 6:
						decimal = Integer.parseInt(field);
						break;
					case 7:
						fieldText = field.trim();
						break;
				}
			}

			if (fieldType.equals("String")) {
				lineCreate += fieldName.trim() + " char(" + Integer.toString(fieldSizeAlpha) + ")";
			} else if (fieldType.equals("char")) {
					lineCreate += fieldName.trim() + " char(" + Integer.toString(fieldSizeAlpha) + ")";
			} else if (fieldType.equals("int")) {
				lineCreate += fieldName.trim() + " numeric(" + Integer.toString(fieldSizeNumeric) + "," + Integer.toString(decimal) + ")";
			} else if (fieldType.equals("double")) {
				lineCreate += fieldName.trim() + " numeric(" + Integer.toString(fieldSizeNumeric) + "," + Integer.toString(decimal) + ")";
			} else {
				lineCreate += fieldName.trim() + " " + fieldType.trim();
			}
			
			fieldText = fieldText.replace("'", " ");
			fieldText = fieldText.replace("\\", " ");
			if (count1 < numberOfFields) {
				if (fieldText.isEmpty()) {
					lineCreate += ",\n";
				} else {
					lineCreate += ", -- " + fieldText + "\n";
					lineAlter += "EXEC sp_addextendedproperty 'MS_Description',  '" + fieldText.trim() + "', 'schema', dbo, 'table', '" + physicalFileName.trim() + "', 'column', " + fieldName.trim() + "\ngo\n";
				}
			} else {
				if (fieldText.isEmpty()) {
					lineCreate += "\n";
				} else {
					lineCreate += " -- " + fieldText + "\n";
					lineAlter += "EXEC sp_addextendedproperty 'MS_Description',  '" + fieldText.trim() + "', 'schema', dbo, 'table', '" + physicalFileName.trim() + "', 'column', " + fieldName.trim() + "\ngo\n";
				}
			}
		}
		lineCreate += ");\n";
		if (db.equals("mssql")) {
			lineCreate += "go";
		}
		
		if (!lineCreate.isEmpty()) WriteJavaSourceLineCreate(lineCreate);
		if (!lineAlter.isBlank()) WriteJavaSourceLineAlter(lineAlter);
		if (!lineDrop.isEmpty()) WriteJavaSourceLineDrop(lineDrop);
    }

	static public Boolean buildSelectView(String physicalFileName, String fileName, String libraryName, Collection<ArrayList<String>> fields, Boolean firstFile) {
		
		String lineCreate = new String();
		String lineAlter = new String();
		String lineDrop = new String();
		originalField = new ArrayList<String>();
		aliasField = new ArrayList<String>();
		
		if (firstFile) {
			if (db.equals("mssql")) {
				lineCreate += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
				lineAlter += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
				lineDrop += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
			}
		}
		
		String selectSql
		= "Select sofile, solib, sofld, sorule, socomp, sonval, sovall, sovalu "
		+ "from qdspfdsel "
		+ "Where sofile = ? And solib = ? And sorfmt <> \' \' "
		+ "Order by solib, sofile";
		
		try {
			PreparedStatement checkStmt = connMSSQL.prepareStatement(selectSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			checkStmt.setString(1, fileName);
			checkStmt.setString(2, libraryName);
			ResultSet resultsSelect = checkStmt.executeQuery();
			if (resultsSelect.first()) {
				lineCreate = setViewFields(lineCreate, libraryName, physicalFileName, fileName, fields);
				lineAlter = setAlterFields(lineAlter, libraryName, physicalFileName, fileName, fields);
				if (db.equals("mssql")) {
					lineDrop += "drop view dbo." + fileName.trim()+ "\ngo\n";
				} else {
					lineDrop += "drop view " + fileName.trim()+ "\n";
				}
				lineCreate += physicalFileName + "\nwhere\n";
				String lastField = new String();
				Boolean needsParan = false;
				String compareField = new String();
				String ruleField = new String();
				lineCreate = setViewValues(resultsSelect, lineCreate, lastField, compareField, ruleField, false);
				lastField = resultsSelect.getString(3).trim().toLowerCase();
				if (!lastField.isEmpty()) lastField = cn.checkFieldName(lastField);
				ruleField = resultsSelect.getString(4).trim(); 
				compareField = resultsSelect.getString(5).trim(); 
				if (compareField.equals("VA")) needsParan = true;
				while (resultsSelect.next()) {
					lineCreate = setViewValues(resultsSelect, lineCreate, lastField, compareField, ruleField, needsParan);
					needsParan = false;
					
					lastField = resultsSelect.getString(3).trim().toLowerCase();
					if (!lastField.isEmpty()) {
						ruleField = resultsSelect.getString(4).trim(); 
						compareField = resultsSelect.getString(5).trim();
						if (compareField.equals("VA")) needsParan = true;
					}
				}
				lineCreate += ";\n";
				if (db.equals("mssql")) {
					lineCreate += "go";
				}				
				WriteJavaSourceLineView(lineCreate);
				WriteJavaSourceLineDropView(lineDrop);
			} else if (getHasConcatField() || getHasSubStringField()) {
				lineCreate = setViewFields(lineCreate, libraryName, physicalFileName, fileName, fields);
				lineCreate += physicalFileName + ";\n";
				if (db.equals("mssql")) {
					lineCreate += "go";
					lineDrop += "drop view dbo." + fileName.trim()+ "\ngo\n";
				} else {
					lineDrop += "drop view " + fileName.trim()+ "\n";
				}
				if (!lineCreate.isBlank()) WriteJavaSourceLineView(lineCreate);
				if (!lineAlter.isBlank()) WriteJavaSourceLineAlter(lineAlter);
				if (!lineDrop.isBlank()) WriteJavaSourceLineDropView(lineDrop);
			} else {
				resultsSelect.close();
			    checkStmt.close();
				return false;
			}
			resultsSelect.close();
		    checkStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	static public Boolean buildNewView(String physicalFileName, String fileName, String libraryName, Collection<ArrayList<String>> fields, Boolean firstFile) {
		
		String lineCreate = new String();
		String lineAlter = new String();
		String lineDrop = new String();
		originalField = new ArrayList<String>();
		aliasField = new ArrayList<String>();
		
		if (firstFile) {
			if (db.equals("mssql")) {
				lineCreate += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
				lineAlter += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
				lineDrop += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
			}
		}
		
		lineCreate = setViewFields(lineCreate, libraryName, physicalFileName, fileName, fields);
		lineAlter = setAlterFields(lineAlter, libraryName, physicalFileName, fileName, fields);
		if (db.equals("mssql")) {
			lineDrop += "drop view dbo." + fileName.trim()+ "\ngo\n";
			lineCreate += physicalFileName + ";\ngo\n";
		} else {
			lineDrop += "drop view " + fileName.trim()+ "\n";
		}
		if (!lineCreate.isBlank()) WriteJavaSourceLineView(lineCreate);
		if (!lineAlter.isBlank()) WriteJavaSourceLineAlter(lineAlter);
		if (!lineDrop.isBlank()) WriteJavaSourceLineDropView(lineDrop);
		
		return true;
	}

	static public Boolean buildSelectJoinView(String physicalFileName, String fileName, String libraryName, Collection<ArrayList<String>> fields, Boolean firstFile) {
		
		String lineCreate = new String();
		String lineAlter = new String();
		String lineDrop = new String();
		String joinedField = new String();
		
		if (firstFile) {
			if (db.equals("mssql")) {
				lineCreate += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
				lineAlter += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
				lineDrop += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
			}
		}
		if (db.equals("mssql")) {
			lineCreate += "create view dbo." + fileName.trim() + " -- " + libraryName + "\n";
			lineCreate += "with schemabinding as\n";
			lineDrop += "drop view " + fileName.trim()+ "\ngo\n";
		} else {
			lineCreate += "create view " + fileName.trim() + " -- " + libraryName + "\n";
			lineCreate += "as\n";
			lineDrop += "drop view " + fileName.trim()+ "\n";
		}
		lineCreate += "Select\n";
		int count1 = 0;
		int numberOfFields = fields.size();
		for (ArrayList<String> element : fields) {
			String joinFileName = new String();
			String joinFieldName = new String();
			String joinConcat = new String();
			String map = new String();
			String mapField = new String();
			String substringStart = new String();
			String substringLength = new String();
			count1++;
			int count2 = 0;
			for (String field : element) {
				count2++;
				switch (count2) {
					case 1:
						joinFileName = field.trim();
						break;
					case 2:
						joinFieldName = field.trim();
						break;
					case 10:
						joinConcat = field.trim();
						break;
					case 11:
						mapField = field.trim();
					case 12:
						map = field.trim();
					case 13:
						substringStart = field.trim();
						break;
					case 14:
						substringLength = field.trim();
						break;
				}
			}
			if (count1 < numberOfFields) {
				if (!joinConcat.isEmpty()) {
					lineCreate += " " + joinFileName + "." + joinConcat + ",\n";
				} else if (map.equals("Y")) {
						lineCreate += ",substring(" + mapField.trim() + "," + substringStart + "," + substringLength + " as " + joinFieldName + "\n";
				} else {
					lineCreate += " " + joinFileName + "." + joinFieldName + ",\n";
				}
			} else {
				if (!joinConcat.isEmpty()) {
					lineCreate += " " + joinFileName + "." + joinConcat + "\n";
				} else {
					lineCreate += " " + joinFileName + "." + joinFieldName + "\n";
				}
			}
			joinedField = joinFileName + "." + joinFieldName;
			joinedFields.add(joinedField);
			allFields.add(joinFieldName);
		}

		ArrayList<String> filesJoined = getJoinedPhysicalFiles();
		String lastJoinedFromFile = new String();
		String lastJoinedToFile = new String();
		if (db.equals("mssql")) {
			lineCreate += "From [dbo].[" + filesJoined.get(0);
			lineCreate += "] inner join [dbo].[" + filesJoined.get(1) + "]";
		} else {
			lineCreate += "From inner join [" + filesJoined.get(1) + "]";
		}
		lineCreate += " On\n";
		Collection<ArrayList<String>>joinedFiles = getAllJoinedFiles();
		count1 = 0;
		String jnjfnm = new String();
		String jnjtnm = new String();
    	String jnjfd1 = new String();
    	String jnjfd2 = new String();
		for (ArrayList<String> element : joinedFiles) {
			count1++;
			int count2 = 0;
			for (String field : element) {
				count2++;
				switch (count2) {
					case 1:
						jnjfnm = field.trim();
						break;
					case 2:
						jnjtnm = field.trim();
						break;
					case 3:
						jnjfd1 = field.trim();
						break;
					case 4:
						jnjfd2 = field.trim();
						break;
				}
			}
			if (!jnjfnm.equals(lastJoinedFromFile) && !lastJoinedFromFile.isBlank()
			|| (!jnjtnm.equals(lastJoinedToFile) && !lastJoinedToFile.isBlank())) {
				if (db.equals("mssql")) {
					lineCreate += "inner join [dbo].[" + jnjtnm + "] On\n";
				} else {
						lineCreate += "inner join [" + jnjtnm + "] On\n";
				}
				count1 = 1;
			}
			if (count1 == 1)
				lineCreate += "\t " + jnjfnm + "." +jnjfd1 + " = " + jnjtnm + "." +jnjfd2 + "\n";
			else
				lineCreate += " and " + jnjfnm + "." +jnjfd1 + " = " + jnjtnm + "." +jnjfd2 + "\n";
			lastJoinedFromFile = jnjfnm;
			lastJoinedToFile = jnjtnm;
		}
		
		String selectSql
		= "Select sofile, solib, sofld, sorule, socomp, sonval, sovall, sovalu "
		+ "from qdspfdsel "
		+ "Where sofile = ? And solib = ? And sorfmt <> \' \' "
		+ "Order by solib, sofile";
		
		try {
			PreparedStatement checkStmt = connMSSQL.prepareStatement(selectSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			checkStmt.setString(1, fileName);
			checkStmt.setString(2, libraryName);
			ResultSet resultsSelect = checkStmt.executeQuery();
			if (resultsSelect.first()) {
				
				lineCreate += "where\n";
				String lastField = new String();
				Boolean needsParan = false;
				String compareField = new String();
				String ruleField = new String();
				lineCreate = setViewValues(resultsSelect, lineCreate, lastField, compareField, ruleField, false);
				lastField = resultsSelect.getString(3).trim().toLowerCase();
				if (!lastField.isEmpty()) lastField = cn.checkFieldName(lastField);
				ruleField = resultsSelect.getString(4).trim(); 
				compareField = resultsSelect.getString(5).trim(); 
				if (compareField.equals("VA")) needsParan = true;
				while (resultsSelect.next()) {
					lineCreate = setViewValues(resultsSelect, lineCreate, lastField, compareField, ruleField, needsParan);
					lastField = resultsSelect.getString(3).trim().toLowerCase();
					if (!lastField.isEmpty()) {
						ruleField = resultsSelect.getString(4).trim(); 
						compareField = resultsSelect.getString(5).trim();
						if (compareField.equals("VA")) needsParan = true;
					}
				}
				lineCreate += ";\n";
				if (db.equals("mssql")) {
					lineCreate += "go";
				}
				if (!lineCreate.isBlank()) WriteJavaSourceLineView(lineCreate);
				if (!lineAlter.isBlank()) WriteJavaSourceLineAlter(lineAlter);
				if (!lineDrop.isBlank()) WriteJavaSourceLineDropView(lineDrop);
			} else {
				lineCreate += ";\n";
				if (db.equals("mssql")) {
					lineCreate += "go";
				}
				if (!lineCreate.isBlank()) WriteJavaSourceLineView(lineCreate);
				if (!lineAlter.isBlank()) WriteJavaSourceLineAlter(lineAlter);
				if (!lineDrop.isBlank()) WriteJavaSourceLineDropView(lineDrop);
			}
			resultsSelect.close();
		    checkStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private static String setViewFields(String lineCreate, String libraryName, String physicaFileName, String fileName, Collection<ArrayList<String>> fields) {
		
		if (fileName.equals("achkeftl0")) {
			System.out.println(fileName);		}
		
		if (db.equals("mssql")) {
			lineCreate += "create view dbo." + fileName.trim() + " -- " + libraryName + "\n";
		} else {
			lineCreate += "create view " + fileName.trim() + " -- " + libraryName + "\n";
		}
		
		if (db.equals("mssql")) {
			lineCreate += "with schemabinding as\n";
		} else {
			lineCreate += "as\n";
		}
		
		lineCreate += "Select\n";
		String savePhysicalFile = new String();
		
		int count1 = 0;
		int count3 = 0;
		int numberOfFields = fields.size();
		for (ArrayList<String> element : fields) {
			String fieldName = new String();
			String fieldDesc = new String();
			String joinConcat = new String();
			String map = new String();
			String mapField = new String();
			String substringStart = new String();
			String substringLength = new String();
			String multiPhysicalFileName = new String();
			count1++;
			count3++;
			int count2 = 0;
			for (String field : element) {
				count2++;
				switch (count2) {
					case 2:
						fieldName = field.trim().toLowerCase();
						break;
					case 7:
						fieldDesc = field.trim();
						break;
					case 9:
						if (field.contains("CONCAT"))
							joinConcat = field.trim();
						break;
					case 10:
						mapField = field.trim();
					case 11:
						map = field.trim();
					case 12:
						substringStart = field.trim();
						break;
					case 13:
						substringLength = field.trim();
						break;
					case 15:
						multiPhysicalFileName = field.trim();
						break;
				}
			}
			
			if (count1 < numberOfFields) {
				if (!multiPhysicalFileName.isEmpty()) {
					if (savePhysicalFile.isEmpty()) {
						savePhysicalFile = multiPhysicalFileName;
					} else {
						if (!multiPhysicalFileName.equals(savePhysicalFile)) {
							count3 = 1;
							if (db.equals("mssql")) lineCreate += "From dbo." + savePhysicalFile.toLowerCase() + "\n";
							else lineCreate += "From " + savePhysicalFile.toLowerCase() + "\n";
							lineCreate += "Union\nSelect\n";
							savePhysicalFile = multiPhysicalFileName;
						}
					}
				}
				allFields.add(fieldName);
				if (!joinConcat.isEmpty()) {
					int fi = joinConcat.indexOf("CONCAT(");
					int ft = joinConcat.indexOf(")");
					if ((fi > 0) && (ft > 0)) {
						String concatString = joinConcat.substring(fi +7, ft);
						String records[] = concatString.split(" ");
						if (records.length < 3) {
							lineCreate += "\n concat_ws(' ', ";
						} else {
							lineCreate += "\n concat_ws(";
						}
						int counter = 0;
						for (String record : records) {
							counter++;
							lineCreate += record.trim().toLowerCase();
							if (counter < records.length) lineCreate += ", ";
						}
						lineCreate += ") as " + fieldName + " -- " + fieldDesc + "\n";
					}
				} else if (map.equals("Y")) {
						lineCreate += ",substring(" + mapField.trim() + "," + substringStart + "," + substringLength + ") as " + fieldName + "\n";
				} else {
					if (fieldName.equals(mapField)) {
						if (count3 == 1) {
							if (fieldDesc.isEmpty()) {
								lineCreate += " " + fieldName + "\n";
							} else {
								lineCreate += " " + fieldName + " -- " + fieldDesc + "\n";
							}

						} else {
							if (fieldDesc.isEmpty()) {
								lineCreate += "," + fieldName + "\n";
							} else {
								lineCreate += "," + fieldName + " -- " + fieldDesc + "\n";
							}
						}
						
					} else {
						if (count3 == 1)
							lineCreate += " " + mapField + " as " + fieldName + " -- " + fieldDesc + "\n";
						else
							lineCreate += "," + mapField + " as " + fieldName + " -- " + fieldDesc + "\n";
						originalField.add(mapField);
						aliasField.add(fieldName);
					}
	 			}
			} else {
				if (!joinConcat.isEmpty()) {
					int fi = joinConcat.indexOf("CONCAT(");
					int ft = joinConcat.indexOf(")");
					if (fi >= 0 && ft > 0) {
						String concatString = joinConcat.substring(fi +7, ft);
						String records[] = concatString.split(" ");
						if (records.length < 3) {
							lineCreate += " concat_ws(' ', ";
						} else {
							lineCreate += " concat_ws(";
						}
						int counter = 0;
						for (String record : records) {
							counter++;
							record = record.trim().toLowerCase();
							record = cn.checkFieldName(record);
							lineCreate += record.trim().toLowerCase();
							if (counter < records.length) lineCreate += ", ";
						}
						lineCreate += ") as " + fieldName + "\n";
					}
				} else if (map.equals("Y")) {
					lineCreate += " substring(" + mapField.trim() + "," + substringStart + "," + substringLength + ") as " + fieldName + "\n";
				} else {
					if (fieldName.equals(mapField)) {
						lineCreate += "," + fieldName + "\n";
					} else {
						lineCreate += "," + mapField + " as " + fieldName + "\n";
						originalField.add(mapField);
						aliasField.add(fieldName);
					}	
				}
			}
		}

		if (db.equals("mssql")) lineCreate += "From dbo.";
		else lineCreate += "From ";
		
		return lineCreate;
	}
	
	private static String setAlterFields(String lineAlter, String libraryName, String physicaFileName, String fileName, Collection<ArrayList<String>> fields) {
		
		for (ArrayList<String> element : fields) {
			String fieldName = new String();
			String fieldDesc = new String();
			int count = 0;
			for (String field : element) {
				count++;
				switch (count) {
					case 2:
						fieldName = field.trim().toLowerCase();
						break;
					case 7:
						fieldDesc = field.trim();
						break;
				}
			}
			lineAlter += "EXEC sp_addextendedproperty 'MS_Description',  '" + fieldDesc.trim() + "', 'schema', dbo, 'view', '" + fileName.trim() + "', 'column', " + fieldName.trim() + "\ngo\n";
		}
		
		return lineAlter;
	}

	private static String setViewValues(ResultSet resultsSelect, String lineCreate, String lastField, String lastComp, String lastRule, Boolean needsParam) {
		
	try {		
		String fieldName = resultsSelect.getString(3).trim().toLowerCase();
		String saveFieldName = fieldName;
		String rule = resultsSelect.getString(4).trim();
		String compare = resultsSelect.getString(5).trim();
		String values = resultsSelect.getString(8).trim();
		if (!fieldName.isEmpty()) fieldName = cn.checkFieldName(fieldName);
		if (fieldName.isEmpty() && (lastField.isEmpty())) {
		} else {
			if (getIsJoinFile()) {
				int i = allFields.indexOf(fieldName.trim());
				if (i >= 0) {
					fieldName = joinedFields.get(i);
				}
			} else {
				int i = aliasField.indexOf(fieldName.trim());
				if (i >= 0) {
					fieldName = originalField.get(i);
				}
			}
			if (!compare.equals("VA")) {
				if (lastComp.equals("VA") && needsParam) lineCreate += ")\n";  
				if (rule.equals("S") && (!fieldName.isEmpty())) {
					if (lastRule.equals("S")) lineCreate += "  or " + fieldName;
					if (lastRule.equals("A")) lineCreate += "\n   or " + fieldName;
					if (lastRule.equals("O")) lineCreate += " and " + fieldName;
					if (lastRule.isEmpty())   lineCreate += "\t " + fieldName;
					if (compare.equals("NE")) lineCreate += " <> ";
					if (compare.equals("EQ")) lineCreate += " = ";
					if (compare.equals("GT")) lineCreate += " > ";
					if (compare.equals("LT")) lineCreate += " < ";
					if (compare.equals("GE")) lineCreate += " >= ";
					if (compare.equals("LE")) lineCreate += " <= ";
					lineCreate += values + "\n";
				}
				if (rule.equals("A")) {
					if (!lastRule.equals("A") && lastComp.equals("VA")) lineCreate += " and " + fieldName;
					else if (!lastRule.equals("O")) lineCreate += " and " + fieldName;
					else lineCreate += " and " + fieldName;
					if (compare.equals("NE")) lineCreate += " <> ";
					if (compare.equals("EQ")) lineCreate += " = ";
					if (compare.equals("GT")) lineCreate += " > ";
					if (compare.equals("LT")) lineCreate += " < ";
					if (compare.equals("GE")) lineCreate += " >= ";
					if (compare.equals("LE")) lineCreate += " <= ";
					lineCreate += values + "\n";
				}
				if (rule.equals("O") && (!fieldName.isEmpty())) {
					if (lastRule.equals("O")) lineCreate += "\n or " + fieldName;
					else if (lastRule.equals("A")) lineCreate += "\n or  " + fieldName;
					else if (lastRule.equals("S")) lineCreate += "\n and " + fieldName;
					if (lastRule.isEmpty()) lineCreate += "\t " + fieldName;
					if (compare.equals("NE")) lineCreate += " = ";
					if (compare.equals("EQ")) lineCreate += " <> ";
					if (compare.equals("GT")) lineCreate += " <= ";
					if (compare.equals("LT")) lineCreate += " >= ";
					if (compare.equals("GE")) lineCreate += " <  ";
					if (compare.equals("LE")) lineCreate += " >  ";
					if (values.substring(0, 1).equals("+")) {
						values = values.substring(1);
					}
					lineCreate += values;
				}

			} else {
				if (!saveFieldName.equals(lastField)) {
					if (rule.equals("S")) {
						if (lastField.isEmpty()) {
							lineCreate += "\t " + fieldName + " in (" + values;
						} else lineCreate += ")\n";
					} else if (rule.equals("O")) {
						lineCreate += "\t " + fieldName + " not in (" + values;
					}
					if (rule.equals("A")) {
						if (needsParam) lineCreate += ")\n";
						lineCreate += " and " + fieldName + " in (" + values;;
					}
				} else {
					lineCreate += "," + values;
				}
			}
			lastField = fieldName;
		}
	} catch (SQLException e) {
		e.printStackTrace();
	}
	
	return lineCreate;
	}
	
	private static Boolean buildIndex(String physicalFileName, String fileName, String libraryName, Boolean firstFile) {
		
		ArrayList<String> allKeyFields = new ArrayList<String>();
		
		String lineCreate = new String();
		String lineDrop = new String();
		
		if (firstFile) {
			if (db.equals("mssql")) {
				lineCreate += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
				lineDrop += "USE [" + company + "_" + currentLibrary + "]\ngo\n";
			}
		}

		String selectSql = "Select apfile, apkeyf, apkseq, apuniq from qdspfdacc "
		 		  		 + "Where apfile = ? And aplib = ? And apkeyn > 0 "
		 		  		 + "Order by aplib, apfile, apkeyn";

		ResultSet resultsSelect;
		try {
			PreparedStatement checkStmt = connMSSQL.prepareStatement(selectSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			checkStmt.setString(1, fileName);
			checkStmt.setString(2, libraryName);
			resultsSelect = checkStmt.executeQuery();
			if (resultsSelect.first()) {
				String keyField = resultsSelect.getString(2).trim().toLowerCase();
				keyField = cn.checkFieldName(keyField);
				String sequence = resultsSelect.getString(3).trim();
				String uniqueID = resultsSelect.getString(4).trim();
				if (uniqueID.equals("Y")) {
					if(getDb().equals("mssql")) {
						if (physicalFileName.equals(fileName)) {
							lineCreate += "create unique clustered index " + fileName + " -- " + libraryName + "\n";
						} else {
							lineCreate += "create unique index " + fileName + " -- " + libraryName + "\n";
						}
					} else {
						lineCreate += "create unique index " + fileName + " -- " + libraryName + "\n";
					}
				} else if (physicalFileName.equals(fileName)) {
					if(getDb().equals("mssql")) {
						lineCreate += "create unique clustered index " + fileName + " -- " + libraryName + "\n";
					} else {
						lineCreate += "create index " + fileName + " -- " + libraryName + "\n";
					}
				} else if (filesSoFarViews.contains(fileName.trim())) {
					if(getDb().equals("mssql")) {
						lineCreate += "create index " + fileName + " -- " + libraryName + "\n";
					} else {
						lineCreate += "create unique index " + fileName + " -- " + libraryName + "\n";
					}
				} else {
					lineCreate += "create index " + fileName + " -- " + libraryName + "\n";
				}
				lineCreate += "on " + physicalFileName + " (\n";
				if (sequence.equals("D")) {
					lineCreate += keyField + " desc\n";
				} else {
					lineCreate += keyField + "\n";
				}

				while (resultsSelect.next()) {
					keyField = resultsSelect.getString(2).trim().toLowerCase();
					keyField = cn.checkFieldName(keyField);
					sequence = resultsSelect.getString(3).trim();
					if (!allKeyFields.contains(keyField.trim())) {
						allKeyFields.add(keyField.trim());
						if (sequence.equals("D")) {
							lineCreate += "," + keyField + " desc\n";
						} else {
							 lineCreate += "," + keyField + "\n";
						}
					}
				}
				lineCreate += ");\n";
				if (db.equals("mssql")) {
					lineCreate += "go";
				}
			}
			WriteJavaSourceLineIndex(lineCreate);
			lineDrop += "drop index " + fileName + "\n";
			if (db.equals("mssql")) {
				lineDrop += "go\n";
			}
			WriteJavaSourceLineDropIndex(lineDrop);
			resultsSelect.close();
		    checkStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return true;
	}
	
	private static void WriteJavaSourceLineCreate(String line) {
		create.append(line + "\n");
	}

	private static void WriteJavaSourceLineAlter(String line) {
		alterCreate.append(line + "\n");
	}
	
	private static void WriteJavaSourceLineDrop(String line) {
		drop.append(line);
	}
	
	private static void WriteJavaSourceLineView(String line) {
		view.append(line + "\n");
	}
	
	private static void WriteJavaSourceLineDropView(String line) {
		dropview.append(line);
	}
	
	private static void WriteJavaSourceLineIndex(String line) {
		index.append(line + "\n");
	}
	
	private static void WriteJavaSourceLineDropIndex(String line) {
		dropindex.append(line);
	}
	
	private static void WriteCreateSQLTable(String createString, String dropString) {
		
		try (FileOutputStream outCreate = new FileOutputStream(new File("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + getCompany() + "\\sql\\" + createString + ".sql"))) {
			outCreate.write(create.toString().getBytes());
			create.setLength(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (FileOutputStream outDrop = new FileOutputStream(new File("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + getCompany() + "\\sql\\" + dropString + ".sql"))) {
			outDrop.write(drop.toString().getBytes());
			drop.setLength(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private static void WriteAlterCreateSQLTable() {
		
		try (FileOutputStream outCreate = new FileOutputStream(new File("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + getCompany() + "\\sql\\alter.sql"))) {
			outCreate.write(alterCreate.toString().getBytes());
			alterCreate.setLength(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void WriteCreateSQLView(String createString, String dropString) {
		
		try (FileOutputStream outCreate = new FileOutputStream(new File("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + getCompany() + "\\sql\\" + createString + ".sql"))) {
			outCreate.write(view.toString().getBytes());
			view.setLength(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (FileOutputStream outDrop = new FileOutputStream(new File("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + getCompany() + "\\sql\\" + dropString + ".sql"))) {
			outDrop.write(dropview.toString().getBytes());
			dropview.setLength(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void WriteCreateSQLIndex(String createString) {
		
		try (FileOutputStream outCreate = new FileOutputStream(new File("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + getCompany() + "\\sql\\" + createString + ".sql"))) {
			outCreate.write(index.toString().getBytes());
			index.setLength(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static StringBuilder getCreate() {
		return create;
	}

	public static void setCreate(StringBuilder create) {
		CreateSQLSource.create = create;
	}

	public static StringBuilder getDrop() {
		return drop;
	}

	public static void setDrop(StringBuilder drop) {
		CreateSQLSource.drop = drop;
	}

	public static Connection getConnMSSQL() {
		return connMSSQL;
	}

	public static void setConnMSSQL(Connection connMSSQL) {
		CreateSQLSource.connMSSQL = connMSSQL;
	}

	public static String getFromLibrary() {
		return fromLibrary;
	}

	public static void setFromLibrary(String fromLibrary) {
		CreateSQLSource.fromLibrary = fromLibrary;
	}

	public static String getCompany() {
		return company;
	}

	public static void setCompany(String company) {
		CreateSQLSource.company = company;
	}

	public static String getDb() {
		return db;
	}

	public static void setDb(String db) {
		CreateSQLSource.db = db;
	}

	public static ArrayList<String> getJoinedPhysicalFiles() {
		return joinedPhysicalFiles;
	}

	public static void setJoinedPhysicalFiles(ArrayList<String> joinedPhysicalFiles) {
		CreateSQLSource.joinedPhysicalFiles = joinedPhysicalFiles;
	}

	public static Collection<ArrayList<String>> getAllJoinedFiles() {
		return allJoinedFiles;
	}

	public static void setAllJoinedFiles(Collection<ArrayList<String>> allJoinedFiles) {
		CreateSQLSource.allJoinedFiles = allJoinedFiles;
	}

	public static Boolean getHasConcatField() {
		return hasConcatField;
	}

	public static void setHasConcatField(Boolean hasConcatField) {
		CreateSQLSource.hasConcatField = hasConcatField;
	}

	public static Boolean getHasSubStringField() {
		return hasSubStringField;
	}

	public static void setHasSubStringField(Boolean hasSubStringField) {
		CreateSQLSource.hasSubStringField = hasSubStringField;
	}

	public static Boolean getIsJoinFile() {
		return isJoinFile;
	}

	public static void setIsJoinFile(Boolean isJoinFile) {
		CreateSQLSource.isJoinFile = isJoinFile;
	}
}	