package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * Filtert alle Seiten, die änderbar sind.
 */
public class ChangeablePageFilter extends PageFilterImpl {

	/**
	 * constructor comment.
	 */
	public ChangeablePageFilter() {
		super();
	}
	/** 
	 * Liefert true, falls die Seite vom angemeldeten Benutzer änderbar ist.
	 */
	public boolean check(Page page) throws RQLException {
		return page.isChangeable();
	}

}
