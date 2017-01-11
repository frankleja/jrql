package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn ein Multi-Link nicht manuell sortiert wird. 
 * Nur bei manueller Sortierung darf die Reihenfolge der Seiten an diesem Multi-Link geändert werden.
 * 
 * @author LEJAFR
 */
public class WrongSortModeException extends RQLException {
	private static final long serialVersionUID = -4912916431151504616L;

/**
 * WrongSortModeException constructor comment.
 *
 * @param s java.lang.String
 */
public WrongSortModeException(String s) {
	super(s);
}
}
