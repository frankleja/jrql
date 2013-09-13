package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * Filtert alle Seiten ohne Überschrift (die {GUID: ...} Seiten) aus.
 */
public class NoHeadlinePageFilter extends PageFilterImpl {

	/**
	 * constructor comment.
	 */
	public NoHeadlinePageFilter() {
		super();
	}
	/** 
	 * Liefert true, falls die Seite keine Überschrift hat. Also eine GUID Seite ist.
	 */
	public boolean check(Page page) throws RQLException {
		return !page.hasHeadline();
	}

}
