package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn eine Seite nicht verlinked ist (freie Seite). 
 * 
 * @author LEJAFR
 */
public class UnlinkedPageException extends RQLException {
	private static final long serialVersionUID = 8589807506336347143L;

/**
 * UnlinkedPageException constructor comment.
 *
 * @param s java.lang.String
 */
public UnlinkedPageException(String s) {
	super(s);
}
}
