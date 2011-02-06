package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.ReddotDate;

/**
 * @author lejafr
 * 
 * Filtert alle Seiten, die vor (<, nicht =) dem gegebenen Datum geändert wurden.
 */
public class LastChangedOnBeforePageFilter extends PageFilterImpl {

	private ReddotDate marginDate;

	/**
	 * constructor comment.
	 */
	public LastChangedOnBeforePageFilter(ReddotDate marginDate) {
		super();

		this.marginDate = marginDate;
		;
	}

	/**
	 * Liefert true, falls die letze Änderung an der gegebenen Seite vor marginDate liegt.
	 */
	public boolean check(Page page) throws RQLException {
		return page.getLastChangedOn().before(marginDate);
	}

}
