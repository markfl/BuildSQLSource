package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.CheckTime;
import model.MsSQL;

public class ResequenceLibraryList {

	public static void main(String[] args) {
		
		CheckTime ct = new CheckTime();
		
		String fromLibrary = args[0];
		String company = fromLibrary + "liblist";
		String resequence = fromLibrary + "liblistreseq";
		
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
		
		String step1 = "delete from " + resequence;
		String step2 = "Select * from " + company + " Order by sequence, runoption, library";
		String step3 = "insert into " + resequence + " (sequence, runoption, library, origlibrary, filecount, viewcount, copyfilecount) "
		 		 		 + "values (?, ?, ?, ?, ?, ?, ?)";
		String step4 = "delete from " + company;
		String step5 = "Select sequence, runoption, library, origlibrary from " + resequence + " Order by sequence, runoption, library";
		String step6 = "select count(*) as numberOfRecords from qdspfdbas "
					 + "Where atlib = ? And atfila = '*PHY' And atdtat = 'D'";
		String step7 = "select count(*) as numberOfRecords from qdspfdbas "
				     + "Where atlib = ? And atfila = '*LGL' And atdtat = 'D'";
		String step8 = "Select company, library, count(*) as recordCount "
					 + "From rawdatafiles " 
					 + "Where company = ? And library = ? "
					 + "Group by company, library "
					 + "Order by company, library";
		String step9 = "insert into " + company + " (sequence, runoption, library, origlibrary) "
						 + "values (?, ?, ?, ?)";
		try {
			System.out.println("Clearing " + resequence);
			PreparedStatement stepstmt1 = connLibListMSSQL.prepareStatement(step1);
			stepstmt1.executeUpdate();
			int sequence = 0;
			System.out.println("Copying " + company);
			PreparedStatement stepstmt2 = connLibListMSSQL.prepareStatement(step2, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsStep2 = stepstmt2.executeQuery();
			while (resultsStep2.next()) {
				int filecount = 0;
				String runoption = resultsStep2.getString(2);
				String libraryName = resultsStep2.getString(3).trim();
				String origLibraryName = resultsStep2.getString(4).trim();
				PreparedStatement stepstmt6 = connFloresCompanyMSSQL.prepareStatement(step6);
				stepstmt6.setString(1, origLibraryName);
				ResultSet resultsstep6 = stepstmt6.executeQuery();
				if (resultsstep6.next()) {
					filecount = resultsstep6.getInt(1);
				}
				resultsstep6.close();
				stepstmt6.close();
				int viewscount = 0;
				PreparedStatement stepstmt7 = connFloresCompanyMSSQL.prepareStatement(step7);
				stepstmt7.setString(1, origLibraryName);
				ResultSet resultsstep7 = stepstmt7.executeQuery();
				if (resultsstep7.next()) {
					viewscount = resultsstep7.getInt(1);
				}
				resultsstep7.close();
				stepstmt7.close();
				int filestocopy = 0;
				PreparedStatement stepstmt8 = connLibListMSSQL.prepareStatement(step8);
				stepstmt8.setString(1, fromLibrary);
				stepstmt8.setString(2, libraryName);
				ResultSet resultsstep8 = stepstmt8.executeQuery();
				if (resultsstep8.next()) {
					filestocopy = resultsstep8.getInt(3);
				}
				resultsstep8.close();
				stepstmt8.close();
				PreparedStatement stepstmt3 = connLibListMSSQL.prepareStatement(step3);
				if (!fromLibrary.equals("integrative")) {
					if (filestocopy == 0) runoption = "n";
				} else {
					runoption = "y";
				}
				sequence += 10;
				stepstmt3.setInt(1, sequence);
				stepstmt3.setString(2, runoption);
				stepstmt3.setString(3, libraryName.trim());
				stepstmt3.setString(4, origLibraryName.trim());
				stepstmt3.setInt(5, filecount);
				stepstmt3.setInt(6, viewscount);
				stepstmt3.setInt(7, filestocopy);
				stepstmt3.executeUpdate();
				stepstmt3.close();
			}
			resultsStep2.close();
			stepstmt2.close();
			System.out.println("Clearing " + company);
			PreparedStatement stepstmt4 = connLibListMSSQL.prepareStatement(step4);
			stepstmt4.executeUpdate();
			stepstmt4.close();
			PreparedStatement stepstmt5 = connLibListMSSQL.prepareStatement(step5, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultsStep5 = stepstmt5.executeQuery();
			System.out.println("Resequencing " + company);
			while (resultsStep5.next()) {
				PreparedStatement stepstmt9 = connLibListMSSQL.prepareStatement(step9);
				stepstmt9.setInt(1, resultsStep5.getInt(1));
				stepstmt9.setString(2, resultsStep5.getString(2));
				stepstmt9.setString(3, resultsStep5.getString(3));
				stepstmt9.setString(4, resultsStep5.getString(4));
				stepstmt9.executeUpdate();
				stepstmt9.close();
			}
			resultsStep5.close();
			stepstmt5.close();
			dbLibListMSSQL.closeConnection(connLibListMSSQL);
			connLibListMSSQL.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String returnString = ct.calculateElapse("Resequencing complete");
		System.out.println(returnString);
	}
}