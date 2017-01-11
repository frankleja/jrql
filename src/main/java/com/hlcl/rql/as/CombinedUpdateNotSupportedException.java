package com.hlcl.rql.as;

import java.util.Map;

/**
 * Diese Ausnahme wird geworfen, wenn ein nicht unterstützter Elementtyp beim gemeinsamen Update von Werten mit nur einem RQL verwendet wird.
 * 
 * @see Page#setElementValues(Map)
 * @author LEJAFR
 */
public class CombinedUpdateNotSupportedException extends RQLException {

	private static final long serialVersionUID = 8170820949514152954L;

	/**
	 * CombinedUpdateNotSupportedException constructor comment.
	 *
	 * @param s java.lang.String
	 */
	public CombinedUpdateNotSupportedException(String s) {
		super(s);
	}
}
