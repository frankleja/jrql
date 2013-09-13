package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn eine Seite nicht gelöscht wird, da noch Referenzen auf diese Seite verweisen.
 * Solche Referenzen können sein: 
 * 1. Container in Seite ist Ziel einer Targetcontainerzuweisung an einer/m Liste/Container 
 * 2. Links anderer Seiten verweisen (reference) auf Links in dieser Seite. 
 * 
 * @author LEJAFR
 */
public class DeletionReferencesToThisPageException extends RQLException {
	private static final long serialVersionUID = -209759912792061615L;

/**
 * DeletionReferencesToThisPageException constructor comment.
 *
 * @param s java.lang.String
 */
public DeletionReferencesToThisPageException(String s) {
	super(s);
}
}
