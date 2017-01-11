package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn in den links zu einer Seite kein MainLink gefunden wird. 
 * 
 * @author LEJAFR
 */
public class MissingMainLinkException extends RQLException {
	private static final long serialVersionUID = -4272628623682056145L;

/**
 * MissingFileException constructor comment.
 *
 * @param s java.lang.String
 */
public MissingMainLinkException(String s) {
	super(s);
}
}
