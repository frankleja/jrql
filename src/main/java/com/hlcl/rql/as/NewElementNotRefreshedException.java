package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird einer Seite geworfen, wenn ein Element, das im Template hinzugefügt wurde, noch nicht auf dieser Seite aktiviert wurde.
 * Das RQL elements action=load liefert diese neuen Elemente nicht zurück. Das Script bricht unerwartet ab.
 * Zur Behebung muss diese Seite nur einmal im SmartEdit angezeigt werden. Dadurch erkennt RD das fehlende Seitenelement und fügt es der Seite hinzu.
 * Dieses Problem tritt z.B. bei TextElementen. Im Template hinzugefügt Standardfelder werden sofort auf allen Seiten aktiviert.
 *
 * @see <code>Page#findElementNode()</code>
 * @author LEJAFR
 */
public class NewElementNotRefreshedException extends RQLException {
	private static final long serialVersionUID = 5327364477124809313L;

/**
 * NewElementNotRefreshedException constructor comment.
 * @param s java.lang.String
 */
public NewElementNotRefreshedException(String s) {
	super(s);
}
}
