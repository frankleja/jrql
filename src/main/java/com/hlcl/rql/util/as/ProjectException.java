package com.hlcl.rql.util.as;

import com.hlcl.rql.as.RQLException;

/**
 * This is the super class for all project page exceptions.
 * 
 * @author LEJAFR
 */
public class ProjectException extends RQLException {

	private static final long serialVersionUID = 5462062941371936334L;

	/**
	 * ProjectException constructor comment.
	 * 
	 * @param message
	 *            java.lang.String
	 */
	public ProjectException(String message) {
		super(message);
	}

	/**
	 * ProjectException constructor comment.
	 * 
	 * @param message
	 *            Beschreibung der Exception.
	 * @param reason
	 *            Ursache des Problems, evtl. auch null.
	 */
	public ProjectException(String message, Throwable reason) {
		super(message, reason);
	}

}
