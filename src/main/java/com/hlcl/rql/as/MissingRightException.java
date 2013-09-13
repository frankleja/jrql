package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn dem Autor ein Recht zur Durchführung einer Aktion (z.B. löschen) fehlt.
 *
 * @author LEJAFR
 */
public class MissingRightException extends RQLException {
	private static final long serialVersionUID = 4601039147927553447L;

/**
 * MissingRightException constructor comment.
 *
 * @param s java.lang.String
 */
public MissingRightException(String s) {
	super(s);
}
}
