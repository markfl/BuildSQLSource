package model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;

public class copy_follett_qcrtsqlfld extends qcrtsqlfld {

	public String runqcrtsqlfld() {

		String company = "flores_follett";
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

		int counterTotal = getRecordCount(getCompanyName(), "follett", getFileName());
		System.out.println(counterTotal + " record(s) to copy to qcrtsqlfld.");

		setsupressErrorMsg(true);
		int counter = 0;
		try (BufferedReader in = new BufferedReader(new
			InputStreamReader(new FileInputStream("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\flores_follett\\data\\follett\\qcrtsqlfld.csv"), "UTF-8"))) {
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
					int m = counter % 100000;
					if (m == 0)
						System.out.println(counter + " records of " + counterTotal + " written to qcrtsqlfld");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			returnString = ct.calculateElapse("Copy", "qcrtsqlfld", counter);
			dbMSSQL.closeConnection(connMSSQL);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return returnString;
	}
}