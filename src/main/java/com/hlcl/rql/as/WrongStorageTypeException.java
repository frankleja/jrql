package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn ein FileFolder nicht den erwarteten Typ hat.
 * 
 * @author LEJAFR
 */
public class WrongStorageTypeException extends RQLException {

	private static final long serialVersionUID = 929148602752098129L;

	/**
	 * WrongStorageTypeException constructor comment.
	 * 
	 * @param s
	 *            java.lang.String
	 */
	public WrongStorageTypeException(String s) {
		super(s);
	}
}
