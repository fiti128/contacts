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

package ru.retbansk.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.retbansk.DefaultParams;

/**
 * Main domain class. Everything about contact is here.
 * It is a form of the Builder pattern[Gamma95,p.97]. 
 * Instead of making the desired object directly, the client calls
 * a constructor with required parameter (primaryEmail) and gets a 
 * builder object. Then you can call setter-like methods on the buider 
 * object to set each optional parameter of interest.
 * 
 * <p><code>Builder.firstName</code>
 * Returns builder with new first name. If parameter is blank and 
 * primary email suits for generating new first name method generates
 * it.
 * <pre>
 * primaryEmail = siarhei.yanusheuski@mail.ru
 * generated first name will be Siarhei          
 * </pre>
 * 
 * <p><code>Builder.lastName</code>
 * Returns builder with new last name. If parameter is blank and 
 * primary email suits for generating new last name method generates 
 * it.
 * <pre>
 * primaryEmail = yanusheuski@mail.ru
 * generated last name will be Yanusheuski          
 * </pre>
 * @param lastName
 * @return Builder with new last name
 * 
 * <p><code>Builder.capitalize</code>
 * <p>Capitalizes all the whitespace separated words in a String.
 * Only the first letter of each word is changed.
 * <pre>
 * capitalize(null)        = null
 * capitalize("")          = ""
 * capitalize("siarhei is future java guru") = "Siarhei Is Future Java Guru"
 * </pre>
 * @param str Any string
 * @return String with Upper case first letter in each word
 * 
 * 
 * @author Siarhei Yanusheuski
 * @since 02.12.2012
 * @see ru.retbansk.service.Converter
 * @see #toString()
 * @see Builder#firstName(String)
 * @see Builder#lastName(String)
 * @see Builder#capitalize(String)
 */
public class Contact implements Comparable<Contact> {
	

	public static String EMAIL_REGEX = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";
	public static String FULLNAME_REGEX = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+){1}@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";
	public static String JUST_FAMILYNAME = "[_A-Za-z0-9-]+@";
	public static String SEPARATOR = DefaultParams.DEFAULT_CSV_SEPARATOR;
	private String firstName;
	private String lastName;
	private String nickName;
	private final String primaryEmail;
	
	private Contact(Builder builder) {
		this.primaryEmail = builder.primaryEmail;
		this.firstName = builder.firstName;
		this.lastName = builder.lastName;
		this.nickName = builder.nickName;
	}
	public static class Builder {
		private String firstName = "";
		private String lastName = "";
		private String nickName = "";
		private final String primaryEmail;
		
		public Builder(String primaryEmail) {
			this.primaryEmail = primaryEmail;
		}
		public boolean isBlank (String string) {
			return (string == null || string.trim().equals(""));
		}
		public boolean isSuitForFullName(String email) {
			return email.matches(FULLNAME_REGEX);
		}
		/**
		 * Returns builder with new first name. If parameter is blank and 
		 * primary email suits for generating new first name method generates
		 * it.
		 * <pre>
	     * primaryEmail = siarhei.yanusheuski@mail.ru
	     * generated first name will be Siarhei          
	     * </pre>
		 * @param lastName
		 * @return Builder with new last name
		 */
		public Builder firstName(String firstName) {
			this.firstName = (isBlank(firstName) && isSuitForFullName(this.primaryEmail)) ?
					capitalize(this.primaryEmail.split("[\\.@]")[0]) : firstName;
			return this;
		}
		/**
		 * Returns builder with new last name. If parameter is blank and 
		 * primary email suits for generating new last name method generates 
		 * it.
		 * <pre>
	     * primaryEmail = yanusheuski@mail.ru
	     * generated last name will be Yanusheuski          
	     * </pre>
		 * @param lastName
		 * @return Builder with new last name
		 */
		public Builder lastName(String lastName) {
			if (isBlank(lastName)) {
				Pattern justFamilyNamePattern = Pattern.compile(JUST_FAMILYNAME);
				Matcher justFamilyNameMatcher = justFamilyNamePattern.matcher(this.primaryEmail);
					if (justFamilyNameMatcher.find()) {
						lastName = justFamilyNameMatcher.group().split("@")[0];
						this.lastName = capitalize(lastName);
					}
			} else {
				this.lastName = lastName;
			}
			return this;
			}
		public Builder nickName(String nickName) {
			this.nickName = nickName;
			return this;
		}
		/**
		 * <p>Capitalizes all the whitespace separated words in a String.
		 * Only the first letter of each word is changed.
		 * <pre>
	     * capitalize(null)        = null
	     * capitalize("")          = ""
	     * capitalize("siarhei is future java guru") = "Siarhei Is Future Java Guru"
	     * </pre>
		 * @param str Any string
		 * @return String with Upper case first letter in each word
		 */
		public String capitalize(String str) {
	        
	        if (str == null || str.length() == 0) {
	            return str;
	        }
	        int strLen = str.length();
	        StringBuilder builder = new StringBuilder(strLen);
	        boolean capitalizeNext = true;
	        for (int i = 0; i < strLen; i++) {
	            char ch = str.charAt(i);

	            if (Character.isWhitespace(ch)) {
	                builder.append(ch);
	                capitalizeNext = true;
	            } else if (capitalizeNext) {
	                builder.append(Character.toTitleCase(ch));
	                capitalizeNext = false;
	            } else {
	                builder.append(ch);
	            }
	        }
	        return builder.toString();
	    } 
		
		public Contact build() {
			return new Contact(this);
		}
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((primaryEmail == null) ? 0 : primaryEmail.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contact other = (Contact) obj;
		if (primaryEmail == null) {
			if (other.primaryEmail != null)
				return false;
		} else if (!primaryEmail.equals(other.primaryEmail))
			return false;
		return true;
	}


	@Override
	public int compareTo(Contact o) {
		return primaryEmail.compareTo(o.getPrimaryEmail());
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}


	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getPrimaryEmail() {
		return primaryEmail;
	}
	public static void setSEPARATOR(String separator) {
		SEPARATOR = separator;
	}



	@Override
	public String toString() {
		String displayName = firstName + " " + lastName;
		return firstName + SEPARATOR + displayName.trim()+ SEPARATOR +lastName +SEPARATOR + nickName
				+ SEPARATOR + primaryEmail;
	}

}
