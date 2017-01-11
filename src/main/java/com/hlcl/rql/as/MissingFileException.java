package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn eine Date in einem RedDot Folder nicht vorhanden ist wie erwartet. 
 * 
 * @author LEJAFR
 */
public class MissingFileException extends RQLException {
	private static final long serialVersionUID = -1980722037278065167L;

/**
 * MissingFileException constructor comment.
 *
 * @param s java.lang.String
 */
public MissingFileException(String s) {
	super(s);
}
}
