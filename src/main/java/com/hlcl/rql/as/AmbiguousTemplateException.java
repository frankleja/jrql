package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird bei Erstellung einer Seite geworfen, wenn nicht genau ein Template vorbelegt ist. 
 * 
 * @author LEJAFR
 */
public class AmbiguousTemplateException extends RQLException {
	private static final long serialVersionUID = 776646808672567874L;

/**
 * AmbiguousTemplateException constructor comment.
 *
 * @param s java.lang.String
 */
public AmbiguousTemplateException(String s) {
	super(s);
}
}
