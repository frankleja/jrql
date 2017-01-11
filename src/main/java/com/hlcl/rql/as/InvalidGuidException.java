package com.hlcl.rql.as;


/**
 * Diese Ausnahme wird geworfen, wenn eine GUID eines RedDot Items ungültig ist.. 
 * 
 * @author LEJAFR
 */
public class InvalidGuidException extends RQLException {
	private static final long serialVersionUID = 8015169511829294245L;

	/**
	 * InvalidGuidException constructor comment.
	 *
	 * @param s java.lang.String
	 */
	public InvalidGuidException(String s) {
		super(s);
	}
}
