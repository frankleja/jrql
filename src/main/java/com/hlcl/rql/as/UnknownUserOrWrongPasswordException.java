package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn ein User versucht sich anzumelden, die Anmeldung aber fehlschlägt weil. 
 * 
 * @author LEJAFR
 */
public class UnknownUserOrWrongPasswordException extends RQLException {

	private static final long serialVersionUID = -3640377003563690503L;

	/**
	 * UnknownUserOrWrongPasswordException constructor comment.
	 *
	 * @param s java.lang.String
	 */
	public UnknownUserOrWrongPasswordException(String s) {
		super(s);
	}
}
