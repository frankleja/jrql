package com.hlcl.rql.as;

/**
 * This exception is thrown, if a script parameter is not set correctly.
 * <p>
 * Maybe a parameter's value is not as expected.
 * 
 * @author LEJAFR
 */
public class InvalidConfigurationException extends RQLException {

	private static final long serialVersionUID = -1084094223261870970L;

	/**
	 * InvalidConfigurationException constructor comment.
	 */
	public InvalidConfigurationException(String s) {
		super(s);
	}
}
