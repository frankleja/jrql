package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn der Extender der Datei nicht im Templateelement zugelassen ist. 
 *
 * @see <code>FileElement</code>
 * @author LEJAFR
 */
public class WrongSuffixException extends RQLException {
	private static final long serialVersionUID = -2142603579801431372L;

/**
 * WrongTypeException constructor comment.
 *
 * @param s java.lang.String
 */
public WrongSuffixException(String s) {
	super(s);
}
}
