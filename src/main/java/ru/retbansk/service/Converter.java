/*
 * Copyright 2012 the original author.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.retbansk.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

import ru.retbansk.DefaultParams;
import ru.retbansk.domain.Contact;

/**
 * This class is a great worker. He works so badly that i think to raise his salary.
 *  
 * 
 * <p><code>contactsToString</code>
 * Converts Set of Contacts into String with default line separator.
 * Default line separator is CR\LR
 * 
 * <p><code>getContactsFromListOfStringArrays</code>
 * Return a Set of Contacts.
 * Converting a List of Arrays of Strings from CSV file into the Set of Contacts.
 * 
 * <p><code>convert</code>
 * Asks user for path to input and output file.
 * Unites all key methods and does entire job. Read, parse and write to new file.
 * 
 * <p><code>writeStringToFile</code>
 *  JDK 1.7 NIO writer. try-with-resources usage.
 * 
 * @author Siarhei Yanusheuski
 * @since 02.12.2012
 * @see ru.retbansk.service.Converter#contactsToString(Set)
 * @see ru.retbansk.service.Converter#contactsToString(Set, String)
 * @see ru.retbansk.service.Converter#convert()
 * @see ru.retbansk.service.Converter#convert(String, String)
 * @see ru.retbansk.service.Converter#getContactsFromListOfStringArrays(List)
 * @see ru.retbansk.service.Converter#getNewContact(String)
 * 
 */

public class Converter implements DefaultParams {
	protected static Logger logger = Logger.getLogger("service");
	private static volatile Converter instance;
	public static String JUST_FAMILYNAME_WITH_DOTS_REGEX = "\\.[_A-Za-z0-9-]+@";
	public static String EMAIL_REGEX = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";
	public static String PATH_TO_FILE_REGEX = "[a-zA-Z0-9_-]*[/\\\\]";
	private String separator;
	private String encoding;
	private String lineEnd;
	
	private Converter(String separator, String encoding, String lineEnd) {
		this.separator = separator;
		this.encoding = encoding;
		this.lineEnd = lineEnd;
	}

		public static Converter getInstance(){
			if (instance == null) {
				synchronized (Converter.class) {
					if (instance == null) {
						return new Converter(DEFAULT_CSV_SEPARATOR, DEFAULT_ENCODING, DEFAULT_LINE_SEPARATOR);
					}
				}
			}
			return instance;
		}
		

	/**
	 * Converts Set of Contacts into String with default line separator.
	 * Default line separator is CR\LR
	 * 
	 * @param contacts Set of Contacts
	 * @return Contacts as a String
	 */
	 
	public String contactsToString(Set<Contact> contacts) {
		return contactsToString(contacts, this.lineEnd);
	}
	/**
	 * Converts Set of Contacts into String
	 * 
	 * @param contacts Set of Contacts
	 * @param lineSeparator desired separator between lines
	 * @return Contacts as a String
	 */
	public String contactsToString(Set<Contact> contacts, String lineSeparator) {
		String contactsString;
		StringBuilder builder = new StringBuilder();
		builder.append(HEADER);
		Contact.setSEPARATOR(this.separator);
		try {
			for (Contact contact: contacts) {
				builder.append(lineSeparator);
				builder.append(contact);
			}
			contactsString = builder.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Contact list is empty or null");
			contactsString = null;
		}
		
		return contactsString;
	}
	/**
	 * Return a Set of Contacts.
	 * Converting a List of Arrays of Strings from CSV file into the Set of Contacts.
	 * 
	 * @param list List of Arrays of Strings from CSV file
	 * @return Set of Contacts
	 */

	public Set<Contact> getContactsFromListOfStringArrays(List<String[]> list) {
		Set<Contact> contacts = new HashSet<>();
		if (list == null|| list.size() == 0) {
			logger.error("List of Arrays of Strings from CSV file is impty or null");
			return null;
		}
		String[] headerLine = list.get(0);
		int primaryEmailIndex = Arrays.asList(headerLine).indexOf(HEADER_EMAIL);
		int firstNameIndex = 	Arrays.asList(headerLine).indexOf(HEADER_FIRST_NAME);
		int lastNameIndex = 	Arrays.asList(headerLine).indexOf(HEADER_LAST_NAME);
		if (primaryEmailIndex == -1) {
			logger.error(HEADER_EMAIL+ " header was not detected");
			
		}
		String firstName, lastName, primaryEmail;
		try {
			for (String[] line : list) {
				if (line.length < 5 ) continue;
				primaryEmail = line [primaryEmailIndex];
				if (primaryEmail.matches(EMAIL_REGEX)) {
					lastName = (lastNameIndex == -1)? "" :line[lastNameIndex];
					firstName = (firstNameIndex == -1) ? "" :line[firstNameIndex];
					Contact contact = new Contact.Builder(primaryEmail)
						.firstName(firstName).lastName(lastName).build();
					contacts.add(contact);
					
					}
				}
		} catch (Exception e) {
			logger.error("Not a valid csv");
			e.printStackTrace();
		}
				
		return contacts;
	}
	/**
	 * This unites all key methods and does entire job. Read, parse and write to new file.
	 * 
	 * @param inputFilePath Input File
	 * @param outputFilePath Output File
	 * @see CSVReader#readAll()
	 * @see Converter#getContactsFromListOfStringArrays(List)
	 * @see Converter#contactsToString(Set)
	 * @see ReadAndWriteIO#writeStringToFile(String, String)
	 * 
	 */
	public void convert(String inputFilePath, String outputFilePath) {
		List<String[]> list = null;
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(inputFilePath));
			list = reader.readAll();
		} catch (IOException e) {
			logger.error("Something goes wrong on reading file " + inputFilePath);;
		}
		finally {
			if (reader != null ) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("error on closing stream");
					
				}
			}
	
		}
		Set<Contact> contacts = getContactsFromListOfStringArrays(list);
		String newData = contactsToString(contacts);
		writeStringToFile(newData, outputFilePath);

	}
	/**
	 * Asks user for path to input and output file.
	 * Unites all key methods and does entire job. Read, parse and write to new file.
	 * 
	 * @see #convert(String,String)
	 * @see CSVReader#readAll()
	 * @see Converter#getContactsFromListOfStringArrays(List)
	 * @see Converter#contactsToString(Set)
	 * @see ReadAndWriteIO#writeStringToFile(String, String)
	 */
	public void convert() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter name of input file");
		String input = scanner.next();
		System.out.println("Please enter name of outputfile");
		String output = scanner.next();
		if (scanner != null) scanner.close();
		convert(input,output);
	}
	/**
	 * 	Create or rewrite file from the string
	 *  JDK 1.7 NIO writer. try-with-resources usage.
	 * 
	 * @param data String to write
	 * @param path Path of File to create or rewrite
	 */
	
	public void writeStringToFile(String data, String filePath) {
		if (data == null) {
			logger.error("There is no data to write");
			return;
		}
		Pattern pattern = Pattern.compile(PATH_TO_FILE_REGEX);
		Matcher matcher = pattern.matcher(filePath.trim());
		matcher.find();
		String path = matcher.group();
		if (path != null) {
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		try (BufferedWriter writer = Files
				.newBufferedWriter(Paths.get(filePath), Charset.forName(this.encoding))) {
			writer.append(data);
			writer.flush();
			logger.info("File was created: " + filePath);
		} catch (IOException exception) {
			logger.error("Error writing to file");
		}
	}
	
	public String getSeparator() {
		return separator;
	}

	public Converter setSeparator(String separator) {
		this.separator = separator;
		return this;
	}

	public String getEncoding() {
		return encoding;
	}

	public Converter setEncoding(String encoding) {
		this.encoding = encoding;
		return this;
	}

	public String getLineEnd() {
		return lineEnd;
	}

	public Converter setLineEnd(String lineEnd) {
		this.lineEnd = lineEnd;
		return this;
	}
	
	
}
