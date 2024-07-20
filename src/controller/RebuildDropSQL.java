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
import java.sql.SQLException;

import model.CheckTime;
import model.MsSQL;

public class RebuildDropSQL {
	
	static StringBuilder text = new StringBuilder();
	
	public static void main(String[] args) {
		
		CheckTime ct = new CheckTime();

		String companyDir = args[0];
		String line;
		String inputFileName = "F:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + companyDir + "\\create.sql";
		MsSQL db = new MsSQL(companyDir);
		Connection connMsSQL = null;
		String fileName = new String();
		int count = 0;
		boolean isFirst = true;
		
		try {
			connMsSQL = db.connect();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		try (BufferedReader in = new BufferedReader(new 
					InputStreamReader(new FileInputStream(inputFileName), "UTF-8"))) {
			while ((line  = in.readLine()) != null ) {
				if (isFirst) {
					line = "USE [" + companyDir + "]\nGO";
					text.append(line + "\n");
					isFirst = false;
				}
				int a = line.indexOf("create");
				if (a >= 0) {
					fileName = line.substring(13);
					line = "drop table " + fileName;
					text.append(line + "\n");
					count++;
				}
			}
			try {
				db.closeConnection(connMsSQL);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try (FileOutputStream out = new FileOutputStream(new File("F:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\" + companyDir + "\\drop.sql"))) {
				out.write(text.toString().getBytes());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Program completed normally: " + count);
			String returnString = ct.calculateElapse("Rebuild");
			System.out.println(returnString);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}