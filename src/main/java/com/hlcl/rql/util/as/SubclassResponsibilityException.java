package com.hlcl.rql.util.as;

import com.hlcl.rql.as.RQLException;

/**
 * This exception is thrown, if a method has to be overridden by a subclass.
 * 
 * @author LEJAFR
 */
public class SubclassResponsibilityException extends RQLException {

	private static final long serialVersionUID = -3474032224737886821L;

	/**
	 * SubclassResponsibilityException constructor comment.
	 * 
	 * @param message
	 *            java.lang.String
	 */
	public SubclassResponsibilityException(String message) {
		super(message);
	}

	/**
	 * SubclassResponsibilityException constructor with default comment 'Method has to be overridden by subclass'.
	 */
	public SubclassResponsibilityException() {
		super("Method has to be overridden by subclass.");
	}
}
