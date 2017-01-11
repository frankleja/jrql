package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn per Programm die Sprachvariante im Hintergrund gewechselt wird und es dabei eine RQLException gab. Der Autor
 * muss sich dann vom RedDot im browser abmelden, um die angezeigte Sprachvariante wieder mit seinen Sessiondaten zu synchronisieren.
 * 
 * @author LEJAFR
 */
public class LanguageVariantNotSynchronizedException extends RQLException {

	private static final long serialVersionUID = 9080655922403096387L;
	private LanguageVariant usersLanguageVariant;

	/**
	 * LanguageVariantNotSynchronizedException constructor comment.
	 * 
	 * @param s	message
	 */
	public LanguageVariantNotSynchronizedException(String s) {
		super(s);
	}
	/**
	 * LanguageVariantNotSynchronizedException constructor comment.
	 * 
	 * @param s message
	 * @param reason the wrapped exception
	 */
	public LanguageVariantNotSynchronizedException(String s, Throwable reason, LanguageVariant usersLanguageVariant) {
		super(s, reason);
		
		this.usersLanguageVariant = usersLanguageVariant;
	}
	/**
	 * @return the usersLanguageVariant
	 */
	public LanguageVariant getUsersLanguageVariant() {
		return usersLanguageVariant;
	}
}
