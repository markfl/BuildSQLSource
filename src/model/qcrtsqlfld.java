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

	private String	Keyfieldnameu;

	private String	Keyfieldnamel;

	private boolean	useKeyfieldnameu;
	private boolean	useKeyfieldnamel;

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
		setCompanyName("flores_follett");

		setfieldnameu("");
		setfieldnamel("");

		setfieldnameuSav();
		setfieldnamelSav();

		setKeyfieldnameu(getfieldnameu());
		setKeyfieldnamel(getfieldnamel());

		setUseKeyfieldnameu(false);
		setUseKeyfieldnamel(false);

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
		setCompanyName("flores_follett");

		setfieldnameu(fieldnameu);
		setfieldnamel(fieldnamel);

		setfieldnameuSav();
		setfieldnamelSav();

		setKeyfieldnameu(getfieldnameu());
		setKeyfieldnamel(getfieldnamel());

		setUseKeyfieldnameu(false);
		setUseKeyfieldnamel(false);

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

	public String getKeyfieldnameu() {
		return this.Keyfieldnameu;
	}

	public void setKeyfieldnameu(String Keyfieldnameu) {
		setUseKeyfieldnameu(false);
		this.Keyfieldnameu = "";
		if (!Keyfieldnameu.isEmpty()) {
			int fldlength = 20;
			if (checkSizeString("Keyfieldnameu", Keyfieldnameu, fldlength)) {
				this.Keyfieldnameu = Keyfieldnameu;
				setUseKeyfieldnameu(true);
			} else {
				if (!supressErrorMsg) {
					System.err.println("Field Keyfieldnameu: not updated properly. Keyfieldnameu = " + Keyfieldnameu);
				}
				setUpdateOK(false);
			}
		}
	}

	public String getKeyfieldnamel() {
		return this.Keyfieldnamel;
	}

	public void setKeyfieldnamel(String Keyfieldnamel) {
		setUseKeyfieldnamel(false);
		this.Keyfieldnamel = "";
		if (!Keyfieldnamel.isEmpty()) {
			int fldlength = 20;
			if (checkSizeString("Keyfieldnamel", Keyfieldnamel, fldlength)) {
				this.Keyfieldnamel = Keyfieldnamel;
				setUseKeyfieldnamel(true);
			} else {
				if (!supressErrorMsg) {
					System.err.println("Field Keyfieldnamel: not updated properly. Keyfieldnamel = " + Keyfieldnamel);
				}
				setUpdateOK(false);
			}
		}
	}

	public boolean isUseKeyfieldnameu() {
		return this.useKeyfieldnameu;
	}

	public void setUseKeyfieldnameu(boolean useKeyfieldnameu) {
		this.useKeyfieldnameu = useKeyfieldnameu;
	}

	public boolean isUseKeyfieldnamel() {
		return this.useKeyfieldnamel;
	}

	public void setUseKeyfieldnamel(boolean useKeyfieldnamel) {
		this.useKeyfieldnamel = useKeyfieldnamel;
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

//***********Set Key FieldS Section*******************************************//

	public void setKeyFields() {
		setKeyfieldnameu(getfieldnameu());
		setKeyfieldnamel(getfieldnamel());
	}

	public void setKeyFields(String Keyfieldnameu) {
		setKeyfieldnameu(Keyfieldnameu);
	}

//***********Get A Record Section*********************************************//

	public boolean get() throws SQLException {

		setReadeOK(false);
		setUpdateOK(false);
		setRecordFound(false);

		if (connection == null) return false;

		if (Keyfieldnameu == "") {
			System.out.println("Key fields must be set.");
			return false;
		}

		String checkSql = "Select * from qcrtsqlfld ";

		int counter = 1;

		if (Keyfieldnameu != "") {
			checkSql = checkSql + " Where fieldnameu=?";
		}

		checkStmt = connection.prepareStatement(checkSql);

		if (Keyfieldnameu != "") {
			checkStmt.setString(counter++, Keyfieldnameu);
		}

		results = checkStmt.executeQuery();
		SQLWarning warning = results.getWarnings();
		printSQLWarnings(warning);
		warning = checkStmt.getWarnings();
		printSQLWarnings(warning);

		if (results.next()) {
			updateAllFromResults();
			setKeyFields();
			setRecordFound(true);
			setUpdateOK(true);
			return true;
		} else {
			setReadeOK(false);
			setUpdateOK(false);
			setRecordFound(false);
			System.err.println("Record not retrieved.");
			return false;
		}
	}

//***********Check Existence of a Record Section******************************//

	public boolean exists() throws SQLException {

		int numberOfRecords;
		setReadeOK(false);
		setUpdateOK(false);
		setRecordFound(false);

		if (connection == null) return false;

		if (Keyfieldnameu == "") {
			System.out.println("Key fields must be set.");
			return false;
		}

		String checkSql = "select count(*) as numberOfRecords from qcrtsqlfld ";
		if (Keyfieldnameu != "") {
			checkSql = checkSql + " Where fieldnameu=?";
		}

		checkStmt =connection.prepareStatement(checkSql);
		int counter = 1;

		if (Keyfieldnameu != "") {
			checkStmt.setString(counter++, Keyfieldnameu);
		}

		results = checkStmt.executeQuery();

		SQLWarning warning = results.getWarnings();
		printSQLWarnings(warning);
		warning = checkStmt.getWarnings();
		printSQLWarnings(warning);

		results.next();
		numberOfRecords = results.getInt(1);

		if (numberOfRecords > 0) return true;
		else return false;
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

	public boolean update() throws SQLException {

		boolean fieldBefore = false;
		setReadeOK(false);

		if (!getUpdateOK()) {
			System.err.println("Some fields were not updated properly.");
			return false;
		}

		if (!getRecordFound()) {
			System.err.println("Record not retrieved.");
			return false;
		}

		String checkSql = "update qcrtsqlfld ";

		int counter = 1;

		checkSql = checkSql.concat(" set ");

		if (fieldnameu != fieldnameuSav) {
			checkSql = checkSql.concat(" fieldnameu = ?");
			fieldBefore = true;
		}
		if (fieldnamel != fieldnamelSav) {
			if (fieldBefore) checkSql = checkSql.concat(",");
			checkSql = checkSql.concat(" fieldnamel = ?");
			fieldBefore = true;
		}

		if (Keyfieldnameu != "") {
			checkSql = checkSql + " Where fieldnameu=?";
		}

		checkStmt = connection.prepareStatement(checkSql);

		if (fieldnameu != fieldnameuSav) {
			checkStmt.setString(counter++, fieldnameu);
		}
		if (fieldnamel != fieldnamelSav) {
			checkStmt.setString(counter++, fieldnamel);
		}

		int record = 0;

		if (counter > 1) {

			if (Keyfieldnameu != "") {
				checkStmt.setString(counter++, Keyfieldnameu);
			}

			record = checkStmt.executeUpdate();

			SQLWarning warning = checkStmt.getWarnings();
			printSQLWarnings(warning);

		}
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

		if (Keyfieldnameu != "") {
			checkSql = checkSql + " Where fieldnameu=?";
		}

		checkStmt = connection.prepareStatement(checkSql);

		int counter = 1;

		if (Keyfieldnameu != "") {
			checkStmt.setString(counter++, Keyfieldnameu);
		}

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

//***********File Read Equal Section******************************************//

	public boolean readEqualFirst() throws SQLException {

		setReadeOK(false);
		setUpdateOK(false);
		setRecordFound(false);

		if (Keyfieldnameu == "") {
			System.err.println("Key fields must be set.");
			return false;
		}


		String checkSql = "Select * from qcrtsqlfld";
		checkSql = checkSql + " Where fieldnameu=?";
		checkSql = checkSql + " Order By fieldnameu";

		checkStmt = connection.prepareStatement(checkSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		int counter = 1;
			checkStmt.setString(counter++, Keyfieldnameu);

		results = checkStmt.executeQuery();

		SQLWarning warning = results.getWarnings();
		printSQLWarnings(warning);
		warning = checkStmt.getWarnings();
		printSQLWarnings(warning);

		if (results.first()) {
			updateAllFromResults();
			setRecordFound(true);
			setUpdateOK(true);
			setKeyFields();
			setReadeOK(true);
			return true;
		} else {
			setReadeOK(false);
			setUpdateOK(false);
			setRecordFound(false);
			return false;
		}
	}

	public boolean readEqualFirstqcrtsqlfl1 () throws SQLException {

		setReadeOK(false);
		setUpdateOK(false);
		setRecordFound(false);

		if (Keyfieldnamel == "") {
			System.err.println("Key fields must be set.");
			return false;
		}


		String checkSql = "Select * from qcrtsqlfld";
		checkSql = checkSql + " Where fieldnamel=?";
		checkSql = checkSql + " Order By fieldnamel";

		checkStmt = connection.prepareStatement(checkSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		int counter = 1;
			checkStmt.setString(counter++, Keyfieldnamel);

		results = checkStmt.executeQuery();

		SQLWarning warning = results.getWarnings();
		printSQLWarnings(warning);
		warning = checkStmt.getWarnings();
		printSQLWarnings(warning);

		if (results.first()) {
			updateAllFromResults();
			setRecordFound(true);
			setUpdateOK(true);
			setKeyFields();
			setReadeOK(true);
			return true;
		} else {
			setReadeOK(false);
			setUpdateOK(false);
			setRecordFound(false);
			return false;
		}
	}

	public boolean readEqualNext() throws SQLException {

		if (!getReadeOK()) {
			System.err.println("Record not retrieved.");
			return false;
		}

		if (results == null) return false;

		if (results.next()) {
			updateAllFromResults();
			setRecordFound(true);
			setUpdateOK(true);
			setKeyFields();
			setReadeOK(true);
			return true;
		} else {
			setReadeOK(false);
			setUpdateOK(false);
			setRecordFound(false);
			return false;
		}
	}

	public boolean readEqualLast() throws SQLException {

		setReadeOK(false);
		setUpdateOK(false);
		setRecordFound(false);

		if (Keyfieldnameu == "") {
			System.err.println("Key fields must be set.");
			return false;
		}


		String checkSql = "Select * from qcrtsqlfld";
		checkSql = checkSql + " Where fieldnameu=?";
		checkSql = checkSql + " Order By fieldnameu";

		checkStmt = connection.prepareStatement(checkSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		int counter = 1;
			checkStmt.setString(counter++, Keyfieldnameu);

		results = checkStmt.executeQuery();

		SQLWarning warning = results.getWarnings();
		printSQLWarnings(warning);
		warning = checkStmt.getWarnings();
		printSQLWarnings(warning);

		if (results.last()) {
			updateAllFromResults();
			setRecordFound(true);
			setUpdateOK(true);
			setKeyFields();
			setReadeOK(true);
			return true;
		} else {
			setReadeOK(false);
			setUpdateOK(false);
			setRecordFound(false);
			return false;
		}
	}

	public boolean readEqualLastqcrtsqlfl1 () throws SQLException {

		setReadeOK(false);
		setUpdateOK(false);
		setRecordFound(false);

		if (Keyfieldnamel == "") {
			System.err.println("Key fields must be set.");
			return false;
		}


		String checkSql = "Select * from qcrtsqlfld";
		checkSql = checkSql + " Where fieldnamel=?";
		checkSql = checkSql + " Order By fieldnamel";

		checkStmt = connection.prepareStatement(checkSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		int counter = 1;
			checkStmt.setString(counter++, Keyfieldnamel);

		results = checkStmt.executeQuery();

		SQLWarning warning = results.getWarnings();
		printSQLWarnings(warning);
		warning = checkStmt.getWarnings();
		printSQLWarnings(warning);

		if (results.last()) {
			updateAllFromResults();
			setRecordFound(true);
			setUpdateOK(true);
			setKeyFields();
			setReadeOK(true);
			return true;
		} else {
			setReadeOK(false);
			setUpdateOK(false);
			setRecordFound(false);
			return false;
		}
	}

	public boolean readEqualPrevious() throws SQLException {

		if (!getReadeOK()) {
			System.err.println("Record not retrieved.");
			return false;
		}

		if (results == null) return false;

		if (results.previous()) {
			updateAllFromResults();
			setRecordFound(true);
			setUpdateOK(true);
			setKeyFields();
			setReadeOK(true);
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

	public String toStringKey() {
		return "qcrtsqlfld [fieldnameu=" + fieldnameu + "]";
	}

	public void setKeyFieldsqcrtsqlfl1(String fieldnamel) {
		setKeyfieldnamel(fieldnamel);
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
