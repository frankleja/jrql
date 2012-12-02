package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn ein Projekt ohne Name erzeugt wurde, 
 * der aber benötigt wird (z.B. für die Ermittlung der Projekt GUID).
 * 
 * @author LEJAFR
 */
public class MissingProjectNameException extends RQLException {
	private static final long serialVersionUID = -7700625293902498362L;

/**
 * MissingProjectNameException constructor comment.
 *
 * @param s java.lang.String	Nachricht an den User
 */
public MissingProjectNameException(String s) {
	super(s);
}
}
