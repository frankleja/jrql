package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn 
 * - der Typ des Templateelementes nicht mit dem angefragten Element zusammenpasst oder 
 * - das Berechtigungspaket nicht den korrekten Typ hat.
 * 
 * @author LEJAFR
 */
public class WrongTypeException extends RQLException {
	private static final long serialVersionUID = 4851905345082719503L;

/**
 * WrongTypeException constructor comment.
 *
 * @param s java.lang.String
 */
public WrongTypeException(String s) {
	super(s);
}
}
