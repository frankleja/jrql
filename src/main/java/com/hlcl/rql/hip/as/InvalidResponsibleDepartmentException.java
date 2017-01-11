package com.hlcl.rql.hip.as;

import com.hlcl.rql.util.as.ProjectException;

/**
 * This is the super class for all project page exceptions.
 * 
 * @author LEJAFR
 */
public class InvalidResponsibleDepartmentException extends ProjectException {

	private static final long serialVersionUID = 5462062941371936334L;

	/**
	 * InvalidResponsibleDepartmentException constructor comment.
	 * 
	 * @param message
	 *            java.lang.String
	 */
	public InvalidResponsibleDepartmentException(String message) {
		super(message);
	}

	/**
	 * InvalidResponsibleDepartmentException constructor comment.
	 * 
	 * @param message
	 *            Beschreibung der Exception.
	 * @param reason
	 *            Ursache des Problems, evtl. auch null.
	 */
	public InvalidResponsibleDepartmentException(String message, Throwable reason) {
		super(message, reason);
	}

}
