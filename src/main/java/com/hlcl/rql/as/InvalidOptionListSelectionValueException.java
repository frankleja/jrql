package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn versucht wird auf einen Eintrag in einer Optionsliste zuzugreifen, den es nicht in der Liste gibt. 
 * 
 * @author LEJAFR
 */
public class InvalidOptionListSelectionValueException extends RQLException {
	private static final long serialVersionUID = 1151427758634838658L;

/**
 * InvalidOptionListSelectionValueException constructor comment.
 *
 * @param s java.lang.String
 */
public InvalidOptionListSelectionValueException(String s) {
	super(s);
}
}
