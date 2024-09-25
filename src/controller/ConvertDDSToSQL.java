package controller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import model.CheckTime;

public class ConvertDDSToSQL {

	public static void main(String[] args) {
		
		CheckTime ct = new CheckTime();
		
		String company = args[0];
		String db = args[1];
		String version = args[2];

		try (BufferedReader in = new BufferedReader(new 
				InputStreamReader(new FileInputStream("C:\\Users Shared Folders\\markfl\\Documents\\My Development\\JDA Source\\" + version + "\\filestoread.txt"), "UTF-8"))) {
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
