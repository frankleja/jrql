package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn eine Seite, entgegen der Erwartung, keine Kindseite ist.
 * 
 * @author LEJAFR
 */
public class NoChildException extends RQLException {
	private static final long serialVersionUID = 6996997909683709305L;

/**
 * NoChildException constructor comment.
 *
 * @param s java.lang.String
 */
public NoChildException(String s) {
	super(s);
}
}
