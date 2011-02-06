package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn ein Projekt einen SessionKey 
 * zum Aufrufen weiterer Funktionen benötigt, dieser aber fehlt.
 * Das Projekt-Objekt kann dann nur administrative Aufgaben ausführen.
 * 
 * @author LEJAFR
 */
public class MissingSessionKeyException extends RQLException {
	private static final long serialVersionUID = 8121039077399320539L;

/**
 * MissingProjectNameException constructor comment.
 *
 * @param s java.lang.String	Nachricht an den User
 */
public MissingSessionKeyException(String s) {
	super(s);
}
}
