package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn mehr als eine Seite an einen MultiLink (List, Container) gelinkt ist.
 * 
 * @author LEJAFR
 */
public class MultiChildPagesException extends RQLException {
	private static final long serialVersionUID = 6893192785022468997L;

/**
 * MultiLinkedPageException constructor comment.
 *
 * @param s java.lang.String
 */
public MultiChildPagesException(String s) {
	super(s);
}
}
