package controller;

import model.copy_follett_qcrtsqlfld;

public class run_follett_Copy_mssql_qcrtsqlfld {

	public static void main(String[] args) {

		String returnString = new String();
		copy_follett_qcrtsqlfld runcopy_follett_qcrtsqlfld = new copy_follett_qcrtsqlfld();
		returnString = runcopy_follett_qcrtsqlfld.runqcrtsqlfld();
		System.out.println(returnString);
	}
}