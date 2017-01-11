package com.hlcl.rql.as;

/**
 * Ein Container für den Usernamen und das Passwort zur Anmeldung an den CMS server.
 * 
 * @author lejafr
 */
public class PasswordAuthentication {

	private String userName;
	private String password;
/**
 * PasswordAuthentication constructor comment.
 */
public PasswordAuthentication(String userName, String password) {
	super();

	this.userName = userName;
	this.password = password;
}
/**
 * Liefert das Passwort.
 * 
 * @return java.lang.String
 */
public String getPassword() {
	return password;
}
/**
 * Liefert den Benutzernamen.
 * 
 * @return java.lang.String
 */
public String getUserName() {
	return userName;
}
}
