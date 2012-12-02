package com.hlcl.rql.as;


/**
 * Diese Ausnahme wird geworfen, wenn ein Element angelegt wird, das bereits existiert, zB. ein TemplateElement. 
 * 
 * @author LEJAFR
 */
public class ElementAlreadyExistingException extends RQLException {

	private static final long serialVersionUID = 4813048511224550530L;

	/**
	 * ElementAlreadyExistingException constructor comment.
	 *
	 * @param s java.lang.String
	 */
	public ElementAlreadyExistingException(String s) {
		super(s);
	}
}
