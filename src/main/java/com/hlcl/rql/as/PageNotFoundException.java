package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn eine Seite nicht gefunden werden kann.
 * 
 * @author LEJAFR
 */
public class PageNotFoundException extends RQLException {

	private static final long serialVersionUID = -2100431268931899035L;

/**
 * PageNotFoundException constructor comment.
 *
 * @param s java.lang.String
 */
public PageNotFoundException(String s) {
	super(s);
}
}
