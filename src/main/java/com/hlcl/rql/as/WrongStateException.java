package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn eine Seite nicht den erwarteten Status (im Workflow) hat. 
 * 
 * @author LEJAFR
 */
public class WrongStateException extends RQLException {
	private static final long serialVersionUID = 5647056626234271855L;

/**
 * WrongStateException constructor comment.
 *
 * @param s java.lang.String
 */
public WrongStateException(String s) {
	super(s);
}
}
