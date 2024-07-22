package model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;

public class copy_qcrtsqlfld extends qcrtsqlfld {

	public String runqcrtsqlfld() {

		String company = "liblist";
		String returnString = new String();
		CheckTime ct = new CheckTime();
		Connection connMSSQL = null;
		MsSQL dbMSSQL = new MsSQL(company);
		setCompanyName(company);
		setDataBase("mssql");
		setFileName("qcrtsqlfld");
		readJSON();
		try {
			connMSSQL = dbMSSQL.connect();
			setConn(connMSSQL);
			dbMSSQL.closeConnection(connMSSQL);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		String fileInputStream = new String();
		fileInputStream = "C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\liblist\\qcrtsqlfld.csv";
		double counterTotal = getRecordCount(getCompanyName(), "liblist", getFileName(), fileInputStream);
		System.out.println((int) counterTotal + " record(s) to copy to qcrtsqlfld.");

		setsupressErrorMsg(true);
		double counter = 0.0;
		int errorCounter = 0;
		try (BufferedReader in = new BufferedReader(new
			InputStreamReader(new FileInputStream(fileInputStream), "UTF-8"))) {
			String line;
			String splitBy = "\\t";
			setConn(dbMSSQL.connect());
			setUpdateOK(true);
			readFirst();
			System.out.println("Clearing qcrtsqlfld");
			delete();
			System.out.println("Copying data to qcrtsqlfld");
			while ((line  = in.readLine()) != null ) {
				String records[] = line.split(splitBy);
				if (records.length >= 1)
					setfieldnameu(records[0]);
				if (records.length >= 2)
					setfieldnamel(records[1]);
				try {
					setUpdateOK(true);
					add();
					counter++;
					int m = (int) counter % 100000;
					if (m == 0) {
						double counterDiff = counter / counterTotal;
						int counterPercent = (int) (counterDiff * 100);
						int printCounterTotal = (int) counterTotal;						System.out.println((int) counter + " records of " + printCounterTotal + " written to qcrtsqlfld. " + counterPercent + "% complete.");
					}
				} catch (SQLException e) {
					e.printStackTrace();
					errorCounter++;
				}
			}
			returnString = ct.calculateElapse("Copy", "qcrtsqlfld", (int) counter);
			dbMSSQL.closeConnection(connMSSQL);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			errorCounter++;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			errorCounter++;
		} catch (IOException e) {
			e.printStackTrace();
			errorCounter++;
		} catch (SQLException e1) {
			e1.printStackTrace();
			errorCounter++;
		}

		if (errorCounter == 0) {
			return returnString;
		} else {
			return returnString + errorCounter + " errors occured.";
		}
	}
}