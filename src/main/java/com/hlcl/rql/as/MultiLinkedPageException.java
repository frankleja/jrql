package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn eine Seite an mehr als einer Seite gelinkt ist 
 * (RedDot Dialog Verlinkung/Erscheidungszeitraum), das aber nicht erwartet wird. 
 * 
 * @author LEJAFR
 */
public class MultiLinkedPageException extends RQLException {
	private static final long serialVersionUID = -1995256182292743035L;

/**
 * MultiLinkedPageException constructor comment.
 *
 * @param s java.lang.String
 */
public MultiLinkedPageException(String s) {
	super(s);
}
}
