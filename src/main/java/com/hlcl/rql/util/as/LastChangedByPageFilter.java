package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * Filtert alle Seiten, die zuletzt vom gegebenen Benutzer geändert wurden.
 */
public class LastChangedByPageFilter extends PageFilterImpl {

	private String userName;

	/**
	 * constructor comment.
	 */
	public LastChangedByPageFilter(String userName) {
		super();
		
		this.userName = userName;;
	}
	/** 
	 * Liefert true, falls die gegebene Seite vom Benutzer zuletzt geändert wurde.
	 */
	public boolean check(Page page) throws RQLException {
		return page.getLastChangedByUserName().equals(userName);
	}

}
