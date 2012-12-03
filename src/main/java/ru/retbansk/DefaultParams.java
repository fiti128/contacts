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
package ru.retbansk;

public interface DefaultParams {
	public static String HEADER = "First Name,Display Name,Last Name,Nickname,Primary Email";
	public static String DEFAULT_LINE_SEPARATOR = "\r\n";
	public static String DEFAULT_CSV_SEPARATOR = ",";
	public static String DEFAULT_ENCODING = "UTF-8";
	public static String INPUT_FILE = "inputContacts.csv";
	public static String OUTPUT_FILE = "output/output.csv";
	public static String HEADER_EMAIL = "E-mail Address";
	public static String HEADER_FIRST_NAME = "First Name";
	public static String HEADER_LAST_NAME = "Last Name";
}
