package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn ein Element nicht in einer Liste gefunden werden konnte. 
 * Wahrscheinlich ist dann mit dem falschen Namen gesucht worden.
 * 
 * @author LEJAFR
 */
public class ElementNotFoundException extends RQLException {
	private static final long serialVersionUID = 5660048985075998850L;

/**
 * ElementNotFoundException constructor comment.
 * @param s java.lang.String
 */
public ElementNotFoundException(String s) {
	super(s);
}
}
