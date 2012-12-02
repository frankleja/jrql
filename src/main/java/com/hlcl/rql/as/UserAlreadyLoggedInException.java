package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn ein User versucht sich anzumelden, dieser User aber bereits angemeldet ist. 
 * 
 * @author LEJAFR
 */
public class UserAlreadyLoggedInException extends RQLException {
	private static final long serialVersionUID = 3147147358643896859L;

/**
 * UserAlreadyLoggedInException constructor comment.
 *
 * @param s java.lang.String
 */
public UserAlreadyLoggedInException(String s) {
	super(s);
}
}
