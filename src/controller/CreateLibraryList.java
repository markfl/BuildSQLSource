package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.CheckTime;
import model.MsSQL;

public class CreateLibraryList {

	public static void main(String[] args) {
		
		CheckTime ct = new CheckTime();
		
		String fromLibrary = args[0];
		String companyliblist = fromLibrary + "liblist";
		String resequence = fromLibrary + "liblistreseq";
		String companyFile = fromLibrary + "file";
		String companyFileList = fromLibrary + "filelist";
		int currentSequence = 0;
		Boolean currentRunOption = false;
		
		Connection connLibListMSSQL = null;
		Connection connFloresCompanyMSSQL = null;
		MsSQL dbLibListMSSQL = new MsSQL("liblist");
		MsSQL dbFloresCompanyMSSQL = new MsSQL("flores_" + fromLibrary);
		try {
			connLibListMSSQL = dbLibListMSSQL.connect();
			connFloresCompanyMSSQL = dbFloresCompanyMSSQL.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		CheckName cn = new CheckName(connLibListMSSQL);
		
		String deleteSql1 = "delete from " + companyliblist;
		String deleteSql2 = "delete from " + resequence;
		String selectSql1 = "Select * from " + fromLibrary + " Order by library";
		String insertSql1 = "insert into " + companyliblist + " (sequence, runoption, library, origlibrary) "
						 + "values (?, ?, ?, ?)";
		String selectSql2 = "select count(*) as numberOfRecords from qdspfdbas"
				 + " Where atlib = ? And atfila = '*PHY' And atdtat = 'D'";
		String selectSql3 = "select count(*) as numberOfRecords from LibrariesToOmit"
				 + " Where company = ? And library = ?";
		String selectSql4 = "select count(*) as numberOfRecords from LibrariesToCopy"
				 + " Where company = ? And library = ?";
		try {
			PreparedStatement deleteStmt = connLibListMSSQL.prepareStatement(deleteSql1);
			deleteStmt.executeUpdate();
			deleteStmt = connLibListMSSQL.prepareStatement(deleteSql2);
			deleteStmt.executeUpdate();
			deleteStmt.close();
			PreparedStatement checkStmt = connLibListMSSQL.prepareStatement(selectSql1, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsSelect = checkStmt.executeQuery();
			while (resultsSelect.next()) {
				String libraryName = resultsSelect.getString(1).trim().toLowerCase();
				PreparedStatement insertStmt = connLibListMSSQL.prepareStatement(insertSql1);
				String origLibraryName = libraryName;
				libraryName = cn.checkFieldName(libraryName);
				insertStmt.setString(3, libraryName.trim());
				insertStmt.setString(4, origLibraryName.trim());
				if (libraryName.length() >= 0) {
					PreparedStatement checkStmt2 = connFloresCompanyMSSQL.prepareStatement(selectSql2);
					checkStmt2.setString(1, libraryName.toUpperCase());
					ResultSet resultsSelect2 = checkStmt2.executeQuery();
					if (resultsSelect2.next()) {
						int numberOfRecords = resultsSelect2.getInt(1);
						if (numberOfRecords == 0) {
							insertStmt.setString(2, "n");
							currentRunOption = false;	
						} else {
							insertStmt.setString(2, "y");
							currentRunOption = true;
						}
					}
					resultsSelect2.close();
					checkStmt2.close();
					PreparedStatement checkStmt3;
					checkStmt3 = connLibListMSSQL.prepareStatement(selectSql3);
					checkStmt3.setString(1, fromLibrary);
					checkStmt3.setString(2, libraryName);
					ResultSet resultsSelect3 = checkStmt3.executeQuery();
					if (resultsSelect3.next()) {
						int numberOfRecords = resultsSelect3.getInt(1);
						if (numberOfRecords >= 1) {
							insertStmt.setString(2, "n");
							currentRunOption = false;	
						}	
					}
					resultsSelect3.close();
					checkStmt3.close();
					// Follett specific
					if (libraryName.equals("mm4r6lib")) {
						insertStmt.setInt(1, 2);
						currentSequence = 2;
					} else if(libraryName.equals("mm4r6itl")) {
						insertStmt.setInt(1, 1);
						currentSequence = 1;
					} else if(libraryName.equals("mm4r6dvl")) {
						insertStmt.setInt(1, 3);
						currentSequence = 3;
					} else if(libraryName.equals("apvtax")) {
						insertStmt.setInt(1, 12);
						currentSequence = 12;
					} else if(libraryName.equals("wtmapvtax")) {
						insertStmt.setInt(1, 13);
						currentSequence = 13;
					} else if(libraryName.equals("avpzip")) {
						insertStmt.setInt(1, 14);
						currentSequence = 14;
					} else if(libraryName.equals("avptax")) {
						insertStmt.setInt(1, 15);
						currentSequence = 15;
					} else if(libraryName.equals("fastfaxl")) {
						insertStmt.setInt(1, 16);
						currentSequence = 16;
					} else if(libraryName.equals("folutillib")) {
						insertStmt.setInt(1, 17);
						currentSequence = 17;
					} else if(libraryName.equals("jaiwk")) {
						insertStmt.setInt(1, 40);
						currentSequence = 40;
					} else if(libraryName.equals("xaload")) {
						insertStmt.setInt(1, 40);
						currentSequence = 40;
					} else if(libraryName.equals("xaprod")) {
						insertStmt.setInt(1, 41);
						currentSequence = 41;
					} else if(libraryName.equals("ed405106")) {
						insertStmt.setInt(1, 999);
						currentSequence = 999;
						insertStmt.setString(2, "n");
						currentRunOption = false;	
					} else if(libraryName.equals("ed405107")) {
						insertStmt.setInt(1, 999);
						insertStmt.setString(2, "n");
						currentSequence = 999;
						currentRunOption = false;	
					} else if(libraryName.length() >= 6 && libraryName.substring(0, 6).equals("jaiwrk")) {
						insertStmt.setInt(1, 41);
						insertStmt.setString(2, "n");
						currentSequence = 41;
						currentRunOption = false;	
					} else if(libraryName.equals("\"testdb2\"")) {
						insertStmt.setInt(1, 999);
						insertStmt.setString(2, "n");
						currentSequence = 999;
						currentRunOption = false;	
					} else if(libraryName.length() >= 5 && libraryName.substring(0, 5).equals("mmfhg")) {
						insertStmt.setInt(1, 4);
						currentSequence = 4;
					} else if(libraryName.length() >= 5 && libraryName.substring(0, 5).equals("mm4r6")) {
						insertStmt.setInt(1, 5);
						currentSequence = 5;
					} else if(libraryName.length() >= 5 && libraryName.substring(0, 5).equals("mm4r5")) {
						insertStmt.setInt(1, 6);
						insertStmt.setString(2, "n");
						currentSequence = 6;
						currentRunOption = false;	
					} else if(libraryName.length() >= 2 && libraryName.substring(0, 2).equals("mm")) {
						insertStmt.setInt(1, 8);
						insertStmt.setString(2, "n");
						currentSequence = 8;
						currentRunOption = false;	
					} else if(libraryName.length() >= 2 && libraryName.substring(0, 2).equals("n0")) {		
						insertStmt.setInt(1, 999);
						currentSequence = 999;
						insertStmt.setString(2, "n");
						currentRunOption = false;
					} else if (libraryName.length() >= 7 && libraryName.substring(0, 7).equals("mflores")) {
						insertStmt.setInt(1, 11);
						currentSequence = 11;
					} else if (libraryName.length() >= 3 && libraryName.substring(0, 3).equals("jda")) {
						insertStmt.setInt(1, 18);
						currentSequence = 18;
					} else if (libraryName.length() >= 3 && libraryName.substring(0, 3).equals("oms")) {
						insertStmt.setInt(1, 19);
						currentSequence = 19;
					} else if (libraryName.length() >= 3 && libraryName.substring(0, 3).equals("rxs")) {
						insertStmt.setInt(1, 20);
						currentSequence = 20;
					} else if (libraryName.length() >= 3 && libraryName.substring(0, 3).equals("ats")) {
						insertStmt.setInt(1, 999);
						currentSequence = 999;
						insertStmt.setString(2, "n");
						currentRunOption = false;	
					} else if (libraryName.length() >= 3 && libraryName.substring(0, 3).equals("arc")) {
						insertStmt.setInt(1, 999);
						currentSequence = 999;
						insertStmt.setString(2, "n");
						currentRunOption = false;	
					} else if(libraryName.length() >= 2 && libraryName.substring(0, 2).equals("#0")) {
						insertStmt.setInt(1, 1001);
						insertStmt.setString(2, "n");
						currentSequence = 1001;
						currentRunOption = false;	
					} else if(libraryName.length() >= 2 && libraryName.substring(0, 2).equals("o#")) {
						insertStmt.setInt(1, 1002);
						currentSequence = 1002;
						insertStmt.setString(2, "n");
						currentRunOption = false;	
					} else if(libraryName.length() >= 2 && libraryName.substring(0, 2).equals("a0")) {
						insertStmt.setInt(1, 1003);
						currentSequence = 1003;
						insertStmt.setString(2, "n");
						currentRunOption = false;	
					} else if(libraryName.length() >= 2 && libraryName.substring(0, 2).equals("r0")) {
						insertStmt.setInt(1, 1009);
						currentSequence = 1009;
						insertStmt.setString(2, "n");
						currentRunOption = false;	
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("al0")) {
						insertStmt.setInt(1, 1004);
						currentSequence = 1004;
						currentRunOption = false;	
						insertStmt.setString(2, "n");
					} else if (libraryName.length() >= 5 && libraryName.substring(0, 5).equals("nfedx")) {
						insertStmt.setInt(1, 1005);
						insertStmt.setString(2, "n");
						currentSequence = 1005;
						currentRunOption = false;
					} else if (libraryName.length() >= 5 && libraryName.substring(0, 5).equals("nnone")) {
						insertStmt.setInt(1, 1006);
						insertStmt.setString(2, "n");
						currentSequence = 1006;
						currentRunOption = false;	
					} else if (libraryName.length() >= 4 && libraryName.substring(0, 4).equals("nshp")) {
						insertStmt.setInt(1, 1007);
						insertStmt.setString(2, "n");
						currentSequence = 1007;
						currentRunOption = false;	
					} else if (libraryName.length() >= 4 && libraryName.substring(0, 4).equals("mone")) {
						insertStmt.setInt(1, 1008);
						insertStmt.setString(2, "n");
						currentSequence = 1008;
						currentRunOption = false;	
					} else if (libraryName.length() >= 5 && libraryName.substring(0, 5).equals("dfedx")) {
						insertStmt.setInt(1, 1010);
						insertStmt.setString(2, "n");
						currentSequence = 1010;
						currentRunOption = false;	
					} else if (libraryName.length() >= 4 && libraryName.substring(0, 4).equals("mite")) {
						insertStmt.setInt(1, 1011);
						insertStmt.setString(2, "n");
						currentSequence = 1011;
						currentRunOption = false;	
					} else if (libraryName.length() >= 4 && libraryName.substring(0, 4).equals("ntbd")) {
						insertStmt.setInt(1, 1012);
						insertStmt.setString(2, "n");
						currentSequence = 1012;
						currentRunOption = false;	
					} else if (libraryName.length() >= 4 && libraryName.substring(0, 4).equals("npur")) {
						insertStmt.setInt(1, 1013);
						insertStmt.setString(2, "n");
						currentSequence = 1013;
						currentRunOption = false;	
					} else if (libraryName.length() >= 4 && libraryName.substring(0, 4).equals("nups")) {
						insertStmt.setInt(1, 1014);
						insertStmt.setString(2, "n");
						currentSequence = 1014;
						currentRunOption = false;	
					} else if (libraryName.length() >= 5 && libraryName.substring(0, 5).equals("nusps")) {
						insertStmt.setInt(1, 1015);
						insertStmt.setString(2, "n");
						currentSequence = 1015;
						currentRunOption = false;	
					} else if (libraryName.length() >= 3 && libraryName.substring(0, 3).equals("ptf")) {
						insertStmt.setInt(1, 1016);
						insertStmt.setString(2, "n");
						currentSequence = 1016;
						currentRunOption = false;	
					} else if (libraryName.length() >= 3 && libraryName.substring(0, 3).equals("xl_")) {
							insertStmt.setInt(1, 1017);
							insertStmt.setString(2, "n");
							currentSequence = 1017;
							currentRunOption = false;	
					} else if (libraryName.length() >= 3 && libraryName.substring(0, 3).equals("zz_")) {
						insertStmt.setInt(1, 1018);
						insertStmt.setString(2, "n");
						currentSequence = 1018;
						currentRunOption = false;	
					} else if (libraryName.length() >= 2 && libraryName.substring(0, 2).equals("xa")) {
						insertStmt.setInt(1, 1019);
						insertStmt.setString(2, "n");
						currentSequence = 1019;
						currentRunOption = false;	
					} else if (libraryName.length() >= 4 && libraryName.substring(0, 4).equals("xx4n")) {
						insertStmt.setInt(1, 1020);
						insertStmt.setString(2, "n");
						currentSequence = 1020;
						currentRunOption = false;	
					} else if (libraryName.length() >= 5 && libraryName.substring(0, 5).equals("xaobj")) {
						insertStmt.setInt(1, 1021);
						insertStmt.setString(2, "n");
						currentSequence = 1021;
						currentRunOption = false;	
					} else if (libraryName.length() >= 3 && libraryName.substring(0, 3).equals("x2e")) {
						insertStmt.setInt(1, 1022);
						insertStmt.setString(2, "n");
						currentSequence = 1022;
						currentRunOption = false;	
					} else if (libraryName.length() >= 3 && libraryName.substring(0, 3).equals("upd")) {
						insertStmt.setInt(1, 1023);
						insertStmt.setString(2, "n");
						currentSequence = 1023;
						currentRunOption = false;	
					} else if (libraryName.length() >= 4 && libraryName.substring(0, 4).equals("user")) {
						insertStmt.setInt(1, 1024);
						insertStmt.setString(2, "n");
						currentSequence = 1024;
						currentRunOption = false;	
					} else if (libraryName.length() >= 4 && libraryName.substring(0, 4).equals("usps")) {
						insertStmt.setInt(1, 1025);
						insertStmt.setString(2, "n");
						currentSequence = 1025;
						currentRunOption = false;

					// Walsworth specific
					} else if(libraryName.equals("mfgdblib")) {
						insertStmt.setInt(1, 2);
						currentSequence = 2;
					} else if(libraryName.equals("objlib")) {
						insertStmt.setInt(1, 3);
						currentSequence = 3;
					} else if(libraryName.equals("comp97")) {
						insertStmt.setInt(1, 1);
						currentSequence = 1;
					} else if(libraryName.equals("upslib")) {
						insertStmt.setInt(1, 4);
						currentSequence = 4;
					} else if(libraryName.equals("sipdata")) {
						insertStmt.setInt(1, 5);
						currentSequence = 5;
					} else if(libraryName.equals("wpcgpl")) {
						insertStmt.setInt(1, 8);
						currentSequence = 8;
					} else if(libraryName.equals("yearend")) {
						insertStmt.setInt(1, 9);
						currentSequence = 9;
					} else if(libraryName.equals("com_brkfld")) {
						insertStmt.setInt(1, 10);
						currentSequence = 10;
					} else if(libraryName.equals("oracle")) {
						insertStmt.setInt(1, 11);
						currentSequence = 11;
					} else if(libraryName.equals("oldlib")) {
						insertStmt.setInt(1, 16);
						insertStmt.setString(2, "n");
						currentSequence = 16;
					} else if(libraryName.equals("mfgproj")) {
						insertStmt.setInt(1, 11);
						currentSequence = 11;
					} else if(libraryName.equals("mfgdbrst")) {
						insertStmt.setInt(1, 19);
						currentSequence = 19;
					} else if(libraryName.equals("allxml100")) {
						insertStmt.setInt(1, 12);
						currentSequence = 12;
					} else if(libraryName.length() >= 6 && libraryName.substring(0, 6).equals("allxml")) {
						insertStmt.setInt(1, 99);
						currentSequence = 99;
						insertStmt.setString(2, "n");
					} else if(libraryName.length() >= 6 && libraryName.substring(0, 6).equals("fix400")) {
						insertStmt.setInt(1, 999);
						insertStmt.setString(2, "n");
						currentSequence = 999;
					} else if(libraryName.equals("libhttp23")) {
						insertStmt.setInt(1, 99);
						currentSequence = 9;
					} else if(libraryName.equals("rbtreplib")) {
						insertStmt.setInt(1, 16);
						currentSequence = 16;
					} else if(libraryName.equals("acom")) {
						insertStmt.setInt(1, 66);
						insertStmt.setString(2, "y");
						currentSequence = 99;
					} else if(libraryName.length() >= 4 && libraryName.substring(0, 4).equals("acom")) {
						insertStmt.setInt(1, 999);
						insertStmt.setString(2, "n");
						insertStmt.setInt(1, 999);
						currentRunOption = false;	
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("rbt")) {
						insertStmt.setInt(1, 17);
						currentSequence = 17;
					} else if(libraryName.length() >= 7 && libraryName.substring(0, 7).equals("adtslab")) {
						insertStmt.setInt(1, 12);
						currentSequence = 12;
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("est")) {
						insertStmt.setInt(1, 13);
						insertStmt.setString(2, "n");
						currentSequence = 13;
						currentRunOption = false;	
					} else if(libraryName.length() >= 4 && libraryName.substring(0, 4).equals("sp41")) {
						insertStmt.setInt(1, 41);
						insertStmt.setString(2, "n");
						currentSequence = 41;
						currentRunOption = false;	
					} else if(libraryName.length() >= 4 && libraryName.substring(0, 4).equals("sp50")) {
						insertStmt.setInt(1, 41);
						insertStmt.setString(2, "n");
						currentSequence = 41;
						currentRunOption = false;	
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("sp5")) {
						insertStmt.setInt(1, 14);
						insertStmt.setString(2, "n");
						currentSequence = 14;
						currentRunOption = false;	
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("sp4")) {
						insertStmt.setInt(1, 15);
						insertStmt.setString(2, "n");
						currentSequence = 15;
						currentRunOption = false;	
					} else if(libraryName.length() >= 8 && libraryName.substring(0, 8).equals("mfgdbqry")) {
						insertStmt.setInt(1, 31);
						insertStmt.setString(2, "n");
						currentSequence = 31;
						currentRunOption = false;	
					} else if(libraryName.length() >= 8 && libraryName.substring(0, 8).equals("mfgdbqryt")) {
						insertStmt.setInt(1, 32);
						insertStmt.setString(2, "n");
						currentSequence = 32;
						currentRunOption = false;	
					} else if(libraryName.length() >= 8 && libraryName.substring(0, 8).equals("mfgdbrst")) {
						insertStmt.setInt(1, 33);
						insertStmt.setString(2, "n");
						currentSequence = 33;
						currentRunOption = false;	
					} else if(libraryName.length() >= 8 && libraryName.substring(0, 8).equals("mfgdbtest")) {
						insertStmt.setInt(1, 34);
						insertStmt.setString(2, "n");
						currentSequence = 34;
						currentRunOption = false;	
					} else if(libraryName.length() >= 8 && libraryName.substring(0, 8).equals("mfgproj")) {
						insertStmt.setInt(1, 35);
						currentSequence = 35;
						currentRunOption = false;	
					} else if(libraryName.length() >= 8 && libraryName.substring(0, 8).equals("mfgtrain")) {
						insertStmt.setInt(1, 36);
						insertStmt.setString(2, "n");
						currentSequence = 36;
						currentRunOption = false;	
					} else if(libraryName.length() >= 4 && libraryName.substring(0, 4).equals("time")) {
							insertStmt.setInt(1, 37);
							currentSequence = 37;
					} else if(libraryName.length() >= 5 && libraryName.substring(0, 5).equals("total")) {
						insertStmt.setInt(1, 37);
						currentSequence = 37;
					} else if(libraryName.length() >= 6 && libraryName.substring(0, 6).equals("weblib")) {
							insertStmt.setInt(1, 38);
							currentSequence = 38;
					} else if(libraryName.length() >= 6 && libraryName.substring(0, 6).equals("wiplib")) {
						insertStmt.setInt(1, 39);
						currentSequence = 39;
					} else if(libraryName.length() >= 7 && libraryName.substring(0, 7).equals("isphere")) {
						insertStmt.setInt(1, 40);
						currentSequence = 40;
					} else if(libraryName.length() >= 7 && libraryName.substring(0, 7).equals("code400")) {
						insertStmt.setInt(1, 999);
						insertStmt.setString(2, "n");
						currentSequence = 999;
						currentRunOption = false;
					} else if (libraryName.equals("qs36f")) {
						if (fromLibrary.equals("cohere")) {
							insertStmt.setInt(1, 1);
							currentSequence = 1;
							insertStmt.setString(2, "y");
							currentRunOption = true;	
						} else {
							insertStmt.setInt(1, 21);
							currentSequence = 21;
						}
					} else if (libraryName.length() >= 5 && libraryName.substring(0, 5).equals("qs36f") && fromLibrary.equals("follett")) {
							insertStmt.setInt(1, 21);
							currentSequence = 21;
					} else if(libraryName.length() >= 2 && libraryName.substring(0, 2).equals("y2")) {
						insertStmt.setInt(1, 9999);
						insertStmt.setString(2, "n");
						currentSequence = 9999;
						currentRunOption = false;	
						
					// DM Bowman specific
					} else if(libraryName.length() >= 4 && libraryName.substring(0, 4).equals("iesf")) {
						insertStmt.setInt(1, 1);
						currentSequence = 1;
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("ies")) {
						insertStmt.setInt(1, 2);
						currentSequence = 2;
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("dmb")) {
						insertStmt.setInt(1, 3);
						currentSequence = 3;
					} else if(libraryName.length() >= 7 && libraryName.substring(0, 7).equals("utsiesf")) {
						insertStmt.setInt(1, 11);
						currentSequence = 11;
					} else if(libraryName.length() >= 6 && libraryName.substring(0, 6).equals("utsies")) {
						insertStmt.setInt(1, 12);
						currentSequence = 12;
					} else if(libraryName.length() >= 4 && libraryName.substring(0, 4).equals("udmb")) {
						insertStmt.setInt(1, 13);
						currentSequence = 13;
					} 

					// Cohere specific
					else if (libraryName.length() >= 5 && libraryName.substring(0, 5).equals("qs36f") && fromLibrary.equals("cohere")) {
						if (!libraryName.equals("qs36fqa")) {
							insertStmt.setInt(1, 1);
							currentSequence = 1;
						} else {
							insertStmt.setInt(1, 32);
							insertStmt.setString(2, "n");
							currentSequence = 32;
							currentRunOption = false;	
						}
					} else if(libraryName.length() >= 4 && libraryName.substring(0, 4).equals("dist")) {
						insertStmt.setInt(1, 2);
						currentSequence = 2;
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("adp")) {
						insertStmt.setInt(1, 4);
						currentSequence = 4;
					} else if(libraryName.length() >= 7 && libraryName.substring(0, 7).equals("libhttp")) {
						insertStmt.setInt(1, 99);
						currentSequence = 9;
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("edi")) {
						insertStmt.setInt(1, 8);
						currentSequence = 8;
					} else if(libraryName.length() >= 5 && libraryName.substring(0, 5).equals("dbu11")) {
						if (fromLibrary.equals("cohere")) {
							insertStmt.setInt(1, 8);
							currentSequence = 8;
						} else if (fromLibrary.equals("res")) {
							insertStmt.setInt(1, 21);
							currentSequence = 21;
						} else { 
							insertStmt.setInt(1, 41);
							currentSequence = 41;
						}
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("dbu")) {
						if (fromLibrary.equals("cohere")) {
							insertStmt.setInt(1, 9);
							currentSequence = 9;
						} else if (fromLibrary.equals("res")) {
							insertStmt.setInt(1, 22);
							currentSequence = 22;
						} else { 
							insertStmt.setInt(1, 42);
							insertStmt.setString(2, "n");
							currentSequence = 42;
							currentRunOption = false;	
						}
					} else if(libraryName.length() >= 4 && libraryName.substring(0, 4).equals("qusr")) {
						insertStmt.setInt(1, 99);
						insertStmt.setString(2, "n");
						currentSequence = 99;
						currentRunOption = false;	
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("bcd")) {
						insertStmt.setInt(1, 12);
						currentSequence = 12;
					} else if(libraryName.length() >= 6 && libraryName.substring(0, 6).equals("crypto")) {
						insertStmt.setInt(1, 13);
						currentSequence = 13;
					} else if(libraryName.length() >= 8 && libraryName.substring(0, 8).equals("debugger")) {
						insertStmt.setInt(1, 14);
						currentSequence = 14;
					} else if(libraryName.length() >= 4 && libraryName.substring(0, 4).equals("menu")) {
						insertStmt.setInt(1, 15);
						currentSequence = 15;
					} else if(libraryName.length() >= 6 && libraryName.substring(0, 6).equals("a99lib")) {
						insertStmt.setInt(1, 16);
						currentSequence = 16;
					} else if(libraryName.length() >= 6 && libraryName.substring(0, 6).equals("b99lib")) {
						insertStmt.setInt(1, 16);
						currentSequence = 16;
					} else if(libraryName.length() >= 10 && libraryName.substring(0, 10).equals("emlordmntr")) {
						insertStmt.setInt(1, 8);
						currentSequence = 8;
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("rjs")) {
						insertStmt.setInt(1, 17);
						currentSequence = 17;
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("tax")) {
						insertStmt.setInt(1, 18);
						currentSequence = 18;
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("rpg")) {
						insertStmt.setInt(1, 18);
						currentSequence = 18;
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("sql")) {
						insertStmt.setInt(1, 19);
						currentSequence = 19;
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("seq")) {
						insertStmt.setInt(1, 19);
						currentSequence = 19;
					} else if(libraryName.length() >= 4 && libraryName.substring(0, 4).equals("seal")) {
						insertStmt.setInt(1, 20);
						currentSequence = 20;
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("sys")) {
						if (currentRunOption) {
							insertStmt.setInt(1, 21);
							currentSequence = 21;
						} else {
							insertStmt.setInt(1, 921);
							currentSequence = 921;
						}
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("rtv")) {
						insertStmt.setInt(1, 22);
						currentSequence = 22;
					} else if(libraryName.length() >= 3 && libraryName.substring(0, 3).equals("rdb")) {
						insertStmt.setInt(1, 23);
						currentSequence = 23;
					} else if(libraryName.equals("kathylib")) {
						insertStmt.setInt(1, 60);
						insertStmt.setString(2, "n");
						currentSequence = 60;
						currentRunOption = false;	
					
					// res specific
					} else if(libraryName.equals("resobj")) {
						insertStmt.setInt(1, 1);
						currentSequence = 1;
					} else if(libraryName.equals("res400")) {
						insertStmt.setInt(1, 2);
						currentSequence = 2;
					} else if(libraryName.equals("archlib")) {
						insertStmt.setInt(1, 3);
						currentSequence = 3;
					} else if(libraryName.equals("dbitools")) {
						insertStmt.setInt(1, 4);
						currentSequence = 4;
					} else if(libraryName.equals("ddslib")) {
						insertStmt.setInt(1, 5);
						currentSequence = 5;
					} else if(libraryName.equals("devlib")) {
						insertStmt.setInt(1, 6);
						currentSequence = 6;
					} else if(libraryName.equals("fwps")) {
						insertStmt.setInt(1, 7);
						currentSequence = 7;
					} else if(libraryName.equals("pcsfile")) {
						insertStmt.setInt(1, 8);
						currentSequence = 8;
					} else if(libraryName.equals("pcspgm")) {
						insertStmt.setInt(1, 9);
						currentSequence = 9;
					} else if(libraryName.equals("pete53")) {
						insertStmt.setInt(1, 10);
						currentSequence = 10;
					} else if(libraryName.equals("swma")) {
						insertStmt.setInt(1, 11);
						currentSequence = 11;
					} else if(libraryName.equals("testqt")) {
						insertStmt.setInt(1, 12);
						currentSequence = 12;
					} else if(libraryName.equals("wi")) {
						insertStmt.setInt(1, 13);
						currentSequence = 13;
					} else if(libraryName.equals("xfer")) {
						insertStmt.setInt(1, 14);
						currentSequence = 14;
					} else if(libraryName.length() >= 7 && libraryName.substring(0, 7).equals("jcerbin")) {
						insertStmt.setInt(1, 25);
						currentSequence = 25;
					} else if(libraryName.equals("lisaha1")) {
						insertStmt.setInt(1, 21);
						currentSequence = 21;
					} else if(libraryName.equals("meppsa1")) {
						insertStmt.setInt(1, 22);
						currentSequence = 22;
					} else if(libraryName.length() >= 8 && libraryName.substring(0, 8).equals("rgriffin")) {
						insertStmt.setInt(1, 23);
						currentSequence = 23;
					} else if(libraryName.length() >= 6 && libraryName.substring(0, 6).equals("qpadev")) {
						insertStmt.setInt(1, 24);
						currentSequence = 24;
						insertStmt.setString(2, "n");
						currentRunOption = false;	
					} else if(libraryName.length() >= 6 && libraryName.substring(0, 6).equals("waynew")) {
						insertStmt.setInt(1, 26);
						currentSequence = 26;
						insertStmt.setString(2, "n");
						currentRunOption = false;	
						
						// west end specific
					} else if(libraryName.equals("jcccwhs")) {
						insertStmt.setInt(1, 1);
						currentSequence = 1;
					} else if(libraryName.equals("jccc230623")) {
						insertStmt.setInt(1, 999);
						currentSequence = 999;
						insertStmt.setString(2, "n");
						currentRunOption = false;	
					} else if(libraryName.length() >= 4 && libraryName.substring(0, 4).equals("jccc")) {
						insertStmt.setInt(1, 2);
						insertStmt.setString(2, "n");
						currentSequence = 2;
						currentRunOption = false;
						
					// banner specific
					} else if(libraryName.equals("idsdata")) {
						insertStmt.setInt(1, 1);
						currentSequence = 1;
					} else if(libraryName.equals("idsuser")) {
						insertStmt.setInt(1, 2);
						currentSequence = 2;
					} else if(libraryName.equals("idsilledi")) {
						insertStmt.setInt(1, 3);
						currentSequence = 3;
					} else if(libraryName.equals("idsmods")) {
						insertStmt.setInt(1, 4);
						currentSequence = 4;
					} else if(libraryName.equals("idspgmr")) {
						insertStmt.setInt(1, 5);
						currentSequence = 5;
					} else if(libraryName.equals("idspruapp")) {
						insertStmt.setInt(1, 6);
						currentSequence = 6;
					} else if(libraryName.equals("idsurdta")) {
						insertStmt.setInt(1, 7);
						currentSequence = 7;
						
					// Misc
					} else if(libraryName.equals("qgpl")) {
						insertStmt.setInt(1, 30);
						currentSequence = 30;
					} else if(libraryName.equals("qsys2")) {
						if (fromLibrary.equals("walsworth")) {
							insertStmt.setInt(1, 999);
							currentSequence = 999;
							insertStmt.setString(2, "n");
							currentRunOption = false;	
						} else {
							insertStmt.setInt(1, 31);
							currentSequence = 31;
						}
					} else if(libraryName.equals("zendphp7")) {
						insertStmt.setInt(1, 99);
						currentSequence = 99;
					} else {
						insertStmt.setInt(1, 999);
						currentSequence = 999;
					}
				} else {
					insertStmt.setInt(1, 998);
					currentSequence = 998;
				}
				PreparedStatement checkStmt4;
				checkStmt4 = connLibListMSSQL.prepareStatement(selectSql4);
				checkStmt4.setString(1, fromLibrary);
				checkStmt4.setString(2, libraryName);
				ResultSet resultsSelect4 = checkStmt4.executeQuery();
				if (resultsSelect4.next()) {
					int numberOfRecords = resultsSelect4.getInt(1);
					if (numberOfRecords >= 1) {
						if (currentSequence == 999) {
							insertStmt.setInt(1, 40);
						}
						insertStmt.setString(2, "y");
					} else {
						if (fromLibrary.equals("res") || fromLibrary.equals("walsworth")) {
							insertStmt.setInt(1, 999);
							insertStmt.setString(2, "n");
						}
						if (fromLibrary.equals("westend")) {
							if(libraryName.length() >= 4 && libraryName.substring(0, 4).equals("jccc")) {
								insertStmt.setInt(1, 999);
								insertStmt.setString(2, "n");
							}
						}
						if (currentSequence <100) {
							if (fromLibrary.equals("cohere") && libraryName.equals("qs36f")) {
							} else {
								currentSequence += 100;
								insertStmt.setInt(1, currentSequence);
							}
						}
					}
				}
				resultsSelect4.close();
				checkStmt4.close();
				insertStmt.executeUpdate();
				System.out.println(libraryName);
				insertStmt.close();
			} // end while
			resultsSelect.close();
			checkStmt.close();
			BuildCompanyFileList(companyliblist, companyFile, companyFileList, connLibListMSSQL);
			dbLibListMSSQL.closeConnection(connLibListMSSQL);;
			dbFloresCompanyMSSQL.closeConnection(connFloresCompanyMSSQL);
			connLibListMSSQL.close();
			connFloresCompanyMSSQL.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String returnString = ct.calculateElapse("Build Library List");
		System.out.println(returnString);
	}
	
	private static void BuildCompanyFileList(String companyliblist, String companyFile, String companyFileList, Connection connLibListMSSQL ) {
		
		String selectSql = "Select * from " + companyFile + " Order by library, filename";
		String deleteSql = "delete from " + companyFileList;
		String insertSql = "Insert into " + companyFileList + " (library, filename, runoption) "
						  + "values (?, ?, ?)";

		try {
			PreparedStatement deleteStmt = connLibListMSSQL.prepareStatement(deleteSql);
			deleteStmt.executeUpdate();
			deleteStmt.close();
			String runOption = new String();
			String chainLibrary = new String();
			String libraryName = new String();
			String fileName = new String();
			PreparedStatement checkStmt = connLibListMSSQL.prepareStatement(selectSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsSelect = checkStmt.executeQuery();
			String saveLibraryName = new String();
			while (resultsSelect.next()) {
				libraryName = resultsSelect.getString(1).trim().toLowerCase();
				if (libraryName.trim().equals("acom")) {
					System.out.println(libraryName);
				}
				fileName = resultsSelect.getString(2).trim().toLowerCase();

				if (saveLibraryName.isEmpty() || !saveLibraryName.equals(libraryName)) {
					runOption = "n";
					String checkSql = new String();				
					checkSql = "Select * from " + companyliblist
							 + " Where library = ?";
					chainLibrary = libraryName;
					PreparedStatement checkStmt4 = connLibListMSSQL.prepareStatement(checkSql);
					checkStmt4.setString(1, chainLibrary);
					ResultSet resultsSelect4 = checkStmt4.executeQuery();
					if (resultsSelect4.next()) {
						runOption = resultsSelect4.getString(2);
					}
					resultsSelect4.close();
					checkStmt4.close();
				}
				if (runOption.equals("y")) {
					PreparedStatement insertStmt = connLibListMSSQL.prepareStatement(insertSql);
					insertStmt.setString(1, libraryName);
					insertStmt.setString(2, fileName);
					insertStmt.setString(3, runOption);
					insertStmt.executeUpdate();
					insertStmt.close();
					System.out.println(libraryName + " " + fileName + " " + runOption);
				}
				saveLibraryName = libraryName;
			}
			resultsSelect.close();
			checkStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
}