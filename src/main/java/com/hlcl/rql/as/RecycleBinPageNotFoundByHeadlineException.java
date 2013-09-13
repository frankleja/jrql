package com.hlcl.rql.as;

/**
 * This exception is thrown, if a page is in recycle bin (determined through PageSearch), but could not be found in SmartTree RQL on
 * recycle bin node searching by page headline.
 * 
 * @author LEJAFR
 */
public class RecycleBinPageNotFoundByHeadlineException extends RQLException {

	private static final long serialVersionUID = 2309589597776934126L;

	/**
	 * RecycleBinPageNotFoundByHeadlineException constructor comment.
	 * 
	 * @param s
	 *            java.lang.String
	 */
	public RecycleBinPageNotFoundByHeadlineException(String s) {
		super(s);
	}
}
