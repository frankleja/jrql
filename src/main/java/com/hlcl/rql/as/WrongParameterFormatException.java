package com.hlcl.rql.as;

/**
 * This exception is thrown, if a parameter did not have the expected format.
 * <p>
 * E.g. to specify a AssetManager sub folder the separator / is a must.
 * 
 * @author LEJAFR
 */
public class WrongParameterFormatException extends RQLException {

	private static final long serialVersionUID = -6885723347593829419L;

	/**
	 * WrongParameterFormatException constructor comment.
	 */
	public WrongParameterFormatException(String s) {
		super(s);
	}
}
