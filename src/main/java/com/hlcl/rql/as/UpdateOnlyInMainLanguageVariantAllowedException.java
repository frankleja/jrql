package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn eine Änderung in einer Sprachvariante für ein sprachunabhängiges Element versucht wird, die nicht
 * die Hauptsprachvariante ist.
 * <p>
 * Das CMS ermöglicht für sprachunabhängige Elemente nur updates in der Hauptsprachvariante.
 * 
 * @author LEJAFR
 */
public class UpdateOnlyInMainLanguageVariantAllowedException extends RQLException {

	private static final long serialVersionUID = 4260029631506390539L;

	/**
	 * UpdateOnlyInMainLanguageVariantAllowedException constructor comment.
	 * 
	 * @param s
	 *            java.lang.String
	 */
	public UpdateOnlyInMainLanguageVariantAllowedException(String s) {
		super(s);
	}
}
