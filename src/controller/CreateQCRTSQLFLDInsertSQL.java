package controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.MsSQL;
import model.qcrtsqlfld;

public class CreateQCRTSQLFLDInsertSQL {

	public static void main(String[] args) {
		
		StringBuilder text = new StringBuilder();
		
		String company = "liblist";
		Connection connMSSQL = null;
		MsSQL dbMSSQL = new MsSQL(company);
		qcrtsqlfld qcrt = new qcrtsqlfld();
		qcrt.setCompanyName(company);
		qcrt.setDataBase("mssql");
		qcrt.setFileName("qcrtsqlfld");
		qcrt.readJSON();
		try {
			connMSSQL = dbMSSQL.connect();
			qcrt.setConn(connMSSQL);
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		String selectSql = "Select fieldnameu, fieldnamel from " + qcrt.getFileName()
				   		  + " Order by fieldnameu";
		
		try {
			PreparedStatement selectStmt = connMSSQL.prepareStatement(selectSql);
		    ResultSet resultsSelect = selectStmt.executeQuery();
		    while (resultsSelect.next()) {
		    	String fileNameU = resultsSelect.getString(1);
		    	String fileNameL = resultsSelect.getString(2);
		    	text.append("insert into [LibList].dbo.qcrtsqlfld values('" + fileNameU.trim() + "', '" + fileNameL.trim() + "')\n");
		    }	
			qcrt.closeConnection();
			dbMSSQL.closeConnection(connMSSQL);
			connMSSQL.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try (FileOutputStream out = new FileOutputStream(new File("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\My SQL Source\\liblist\\insertQcrtsqlfld.sql"))) {
			out.write(text.toString().getBytes());
			System.out.println("Program completed normally.");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}