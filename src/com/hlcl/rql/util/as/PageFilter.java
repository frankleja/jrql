package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * Diese Klassenhierarchie beschreibt Kriterien zur Auswahl von Seiten.
 *
 * @author lejafr
 */
public interface PageFilter {
	/**
	 * Prüft die gegebenen Seite auf Gültigkeit dieses Filters.
	 * Liefert diese Methode true, wird die Seite selektiert, sonst verworfen.
	 *
	 *@see PageArrayList#select(PageFilter)
	 */
	public boolean check(Page page) throws RQLException;
}
