package controller;

import model.copy_qcrtsqlfld;

public class run_Copy_mssql_qcrtsqlfld {

	public static void main(String[] args) {

		String returnString = new String();
		copy_qcrtsqlfld runcopy_qcrtsqlfld = new copy_qcrtsqlfld();
		returnString = runcopy_qcrtsqlfld.runqcrtsqlfld();
		System.out.println(returnString);
	}
}