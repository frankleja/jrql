package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn eine GUID eines RedDot Items erforderlich ist, aber fehlt. 
 * 
 * @author LEJAFR
 */
public class MissingGuidException extends RQLException {
	private static final long serialVersionUID = 8015169511829294245L;

/**
 * MissingFileException constructor comment.
 *
 * @param s java.lang.String
 */
public MissingGuidException(String s) {
	super(s);
}
}
