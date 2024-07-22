package model;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

public class qcrtsqlfld extends DBClassBuilder {

//***********Field Definition Section*****************************************//

	private String	fieldnameu;
	private String	fieldnamel;

	private String	fieldnameuSav;
	private String	fieldnamelSav;




	private Collection<ArrayList<String>> allFields = new ArrayList<ArrayList<String>>();
	private ArrayList<String> allKeyFiles = new ArrayList<String>();
	private Collection<ArrayList<String>> allKeyFields = new ArrayList<ArrayList<String>>();
	private ArrayList<String> allPhysicalKeyFieldNames = new ArrayList<String>();
	private ArrayList<String> allLogicalKeyFieldNames = new ArrayList<String>();
	private Collection<ArrayList<String>> allLogicalKeyFields = new ArrayList<ArrayList<String>>();
	private ArrayList<String> allLogicalFieldList = new ArrayList<String>();
	private Collection<ArrayList<String>> allLogicalKeyFieldList = new ArrayList<ArrayList<String>>();

	private Connection connection;
	private ResultSet results;
	private PreparedStatement checkStmt;

	private boolean recordFound;
	private boolean updateOK;
	private boolean readeOK;
	private boolean supressErrorMsg;



//***********Constructor Section**********************************************//

	public qcrtsqlfld() {

		super();

		setsupressErrorMsg(true);

		setDataBase("mssql");
		setFileName("qcrtsqlfld");
		setCompanyName("flores_walsworth");

		setfieldnameu("");
		setfieldnamel("");

		setfieldnameuSav();
		setfieldnamelSav();



		getFields();

		setsupressErrorMsg(false);
		setReadeOK(false);
		setUpdateOK(false);
		setRecordFound(false);
	}

	public qcrtsqlfld(String fieldnameu, String fieldnamel) {

		super();

		setsupressErrorMsg(true);

		setDataBase("mssql");
		setFileName("qcrtsqlfld");
		setCompanyName("flores_walsworth");

		setfieldnameu(fieldnameu);
		setfieldnamel(fieldnamel);

		setfieldnameuSav();
		setfieldnamelSav();

		getFields();

		setReadeOK(false);
		setUpdateOK(false);
		setRecordFound(false);
	}

//***********Getter Setter Section********************************************//

	public String getfieldnameu() {
		return this.fieldnameu;
	}

	public void setfieldnameu(String fieldnameu) {
		this.fieldnameu = "";
		if (!fieldnameu.isEmpty()) {
			int fldlength = 20;
			if (checkSizeString("fieldnameu", fieldnameu, fldlength)) {
				this.fieldnameu = fieldnameu;
			} else {
				if (!supressErrorMsg) {
					System.err.println("Field fieldnameu: not updated properly. fieldnameu = " + fieldnameu);
				}
				setUpdateOK(false);
			}
		}
	}

	public String getfieldnamel() {
		return this.fieldnamel;
	}

	public void setfieldnamel(String fieldnamel) {
		this.fieldnamel = "";
		if (!fieldnamel.isEmpty()) {
			int fldlength = 20;
			if (checkSizeString("fieldnamel", fieldnamel, fldlength)) {
				this.fieldnamel = fieldnamel;
			} else {
				if (!supressErrorMsg) {
					System.err.println("Field fieldnamel: not updated properly. fieldnamel = " + fieldnamel);
				}
				setUpdateOK(false);
			}
		}
	}

	public String getfieldnameuSav() {
		return this.fieldnameuSav;
	}

	private void setfieldnameuSav() {
		this.fieldnameuSav = getfieldnameu();
	}

	public String getfieldnamelSav() {
		return this.fieldnamelSav;
	}

	private void setfieldnamelSav() {
		this.fieldnamelSav = getfieldnamel();
	}

	public Connection getConn() {
		return this.connection;
	}

	public void setConn(Connection connection) {
		this.connection = connection;
	}

	private boolean getRecordFound() {
		return this.recordFound;
	}

	private void setRecordFound(boolean recordFound) {
		this.recordFound = recordFound;
	}

	public boolean getUpdateOK() {
		return this.updateOK;
	}

	public void setUpdateOK(boolean updateOK) {
		this.updateOK = updateOK;
	}

	public boolean getReadeOK() {
		return this.readeOK;
	}

	public void setReadeOK(boolean readeOK) {
		this.readeOK = readeOK;
	}

	public void setsupressErrorMsg(boolean supressErrorMsg) {
		this.supressErrorMsg = supressErrorMsg;
	}

//***********File Updating Section********************************************//

	public boolean add() throws SQLException {

		setReadeOK(false);

		if (!getUpdateOK()) {
			System.err.println("Some fields were not updated properly.");
			return false;
		}

		String checkSql = "insert into qcrtsqlfld (fieldnameu, fieldnamel) " +
						"values (?, ?);";

		checkStmt = connection.prepareStatement(checkSql);

		checkStmt.setString(1, fieldnameu);
		checkStmt.setString(2, fieldnamel);

		int record = checkStmt.executeUpdate();

		SQLWarning warning = checkStmt.getWarnings();
		printSQLWarnings(warning);

		if (record > 0) return true;
		else return false;
	}

	public boolean delete() throws SQLException {

		setReadeOK(false);

		if (!getRecordFound()) {
			System.err.println("Record not retrieved.");
			return false;
		}

		String checkSql = "delete from qcrtsqlfld ";

		checkStmt = connection.prepareStatement(checkSql);


		int record = checkStmt.executeUpdate();

		SQLWarning warning = checkStmt.getWarnings();
		printSQLWarnings(warning);

		if (record > 0) return true;
		else return false;
	}

//***********File Read Random Section*****************************************//

	public boolean readFirst() throws SQLException {

		setReadeOK(false);
		setUpdateOK(false);
		setRecordFound(false);

		String checkSql = "Select * from qcrtsqlfld";

		Statement Stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		results = Stmt.executeQuery(checkSql);

		SQLWarning warning = results.getWarnings();
		printSQLWarnings(warning);
		warning = Stmt.getWarnings();
		printSQLWarnings(warning);

		if (results.first()) {
			updateAllFromResults();
			setRecordFound(true);
			setUpdateOK(true);
			return true;
		} else {
			setReadeOK(false);
			setUpdateOK(false);
			setRecordFound(false);
			return false;
		}
	}

	public boolean readNext() throws SQLException {

		setReadeOK(false);

		if (!getRecordFound()) {
			System.err.println("Record not retrieved.");
			return false;
		}

		if (results == null) return false;

		if (results.next()) {
			updateAllFromResults();
			setRecordFound(true);
			setUpdateOK(true);
			return true;
		} else {
			setReadeOK(false);
			setUpdateOK(false);
			setRecordFound(false);
			return false;
		}
	}

	public boolean readLast() throws SQLException {

		setReadeOK(false);
		setUpdateOK(false);
		setRecordFound(false);

		String checkSql = "Select * from qcrtsqlfld";

		Statement Stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		results = Stmt.executeQuery(checkSql);

		SQLWarning warning = results.getWarnings();
		printSQLWarnings(warning);
		warning = Stmt.getWarnings();
		printSQLWarnings(warning);

		if (results.last()) {
			updateAllFromResults();
			setRecordFound(true);
			setUpdateOK(true);
			return true;
		} else {
			setReadeOK(false);
			setUpdateOK(false);
			setRecordFound(false);
			return false;
		}
	}	

	public boolean readPrevious() throws SQLException {

		setReadeOK(false);

		if (!getRecordFound()) {
			System.err.println("Record not retrieved.");
			return false;
		}

		if (results == null) return false;

		if (results.previous()) {
			updateAllFromResults();
			setRecordFound(true);
			setUpdateOK(true);
			return true;
		} else {
			setReadeOK(false);
			setUpdateOK(false);
			setRecordFound(false);
			return false;
		}
	}

//***********Record Set Results Section***************************************//

	private void updateAllFromResults() throws SQLException {

		if (results == null) return;

		setfieldnameu(results.getString(1));
		setfieldnamel(results.getString(2));

		getfieldnameuSav();
		getfieldnamelSav();
	}

//***********Print to String Section*******************************************//

	public String toString() {
		return "qcrtsqlfld [fieldnameu=" + fieldnameu.trim() + ", fieldnamel=" + fieldnamel.trim() + "]";
	}

//***********Utility Section**************************************************//

	public boolean checkSizeDouble(double field, int length, int decimal) {

		int leftInt = 0;
		int leftIntSize;
		String doubleString, leftString;
		char checkString = '.';

		doubleString = String.valueOf(field);
		int stringLength = doubleString.length();
		for (int i = 0; i < stringLength; i++) {
			char newString = doubleString.charAt(i);
			if (newString == checkString) {
				leftInt = i;
				break;
			}
		}
		leftString = doubleString.substring(0, leftInt);
		leftIntSize = leftString.length();

		if (leftIntSize > leftInt)
			return false;

		return true;
	}

	public boolean checkSizeInt(String fieldName, int field,  int length) {

		double testInt;
		String stringInt = "";

		for (int i = 0; i < length; i++) {
			stringInt = stringInt.concat("9");
		}

		testInt = Double.parseDouble(stringInt);
		if (field > testInt) {
			System.err.println("In File qcrtsqlfld number field to long for field size for field " + fieldName + " size " + length + " value " + field +".");
			return false;
		} else return true;
	}

	public boolean checkSizeLong(String fieldName, Long field, int length) {

		double testInt;
		String stringLong = "";

		for (int i = 0; i < length; i++) {
			stringLong = stringLong.concat("9");
		}

		testInt = Double.parseDouble(stringLong);
		if (field > testInt) {
			System.err.println("In File qcrtsqlfld number field to long for field size for field " + fieldName + " size " + length + " value " + field +".");
			return false;
		} else return true;
	}

	public boolean checkSizeString(String fieldName, String field, int length) {

		String overflow;

		if (field.length() < length) return true;
		overflow = field.substring(length);
		String trim = overflow.trim();
		if (!trim.isEmpty()) {
			System.err.println("In File qcrtsqlfld string field to long for field size for field " + fieldName + " size " + length + " value " + field +".");
			return false;
		} else return true;
	}

	private void printSQLWarnings(SQLWarning warning) {

		while (warning != null) {

			System.out.println(warning);
			String message = warning.getMessage();
			String sqlState = warning.getSQLState();
			int errorCode = warning.getErrorCode();
			System.err.println(message + sqlState + errorCode);
			warning = warning.getNextWarning();
		}
	}

	public void getFields() {

		readJSON();

		setAllFields(getAllFields());
		setAllKeyFiles(getAllKeyFiles());
		setAllKeyFields(getAllKeyFields());
		setAllPhysicalKeyFieldNames(getAllPhysicalKeyFieldNames());
		setAllLogicalKeyFieldNames(getAllLogicalKeyFieldNames());
		setAllLogicalKeyFields(getAllLogicalKeyFields());
		setAllLogicalKeyFieldList(getAllLogicalKeyFieldList());
	}

	public Collection<ArrayList<String>> getAllFields() {
		return allFields;
	}

	public void setAllFields(Collection<ArrayList<String>> allFields) {
		this.allFields = allFields;
	}

	public ArrayList<String> getAllKeyFiles() {
		return allKeyFiles;
	}

	public void setAllKeyFiles(ArrayList<String> allKeyFiles) {
		this.allKeyFiles = allKeyFiles;
	}

	public Collection<ArrayList<String>> getAllKeyFields() {
		return allKeyFields;
	}

	public void setAllKeyFields(Collection<ArrayList<String>> allKeyFields) {
		this.allKeyFields = allKeyFields;
	}
	public ArrayList<String> getAllLogicalKeyFieldNames() {
		return allLogicalKeyFieldNames;
	}

	public void setAllLogicalKeyFieldNames(ArrayList<String> allLogicalKeyFieldNames) {
		this.allLogicalKeyFieldNames = allLogicalKeyFieldNames;
	}

	public Collection<ArrayList<String>> getallLogicalKeyFields() {
		return allLogicalKeyFields;
	}

	public void setAllLogicalKeyFields(Collection<ArrayList<String>> allLogicalKeyFields) {
		this.allLogicalKeyFields = allLogicalKeyFields;
	}

	public ArrayList<String> getAllPhysicalKeyFieldNames() {
		return allPhysicalKeyFieldNames;
	}

	public void setAllPhysicalKeyFieldNames(ArrayList<String> allPhysicalKeyFieldNames) {
		this.allPhysicalKeyFieldNames = allPhysicalKeyFieldNames;
	}

	public ArrayList<String> getAllLogicalFieldList() {
		return allLogicalFieldList;
	}

	public void setAllLogicalFieldList(ArrayList<String> allLogicalFieldList) {
		this.allLogicalFieldList = allLogicalFieldList;
	}

	public Collection<ArrayList<String>> getallLogicalKeyFieldList() {
		return allLogicalKeyFieldList;
	}

	public void setAllLogicalKeyFieldList(Collection<ArrayList<String>> allLogicalKeyFieldList) {
		this.allLogicalKeyFieldList = allLogicalKeyFieldList;
	}
}
