package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 * Filtert alle Seiten deren Überschrift mit dem gegebenen Prefix beginnen aus.
 */
public class HeadlineStartWithPageFilter extends PageFilterImpl {

	private String prefix;

	/**
	 * constructor comment.
	 */
	public HeadlineStartWithPageFilter(String prefix) {
		super();
		this.prefix = prefix;
	}

	/**
	 * Liefert true, falls die Überschrift der gegebenen Seite mit dem prefix beginnt.
	 */
	public boolean check(Page page) throws RQLException {
		return page.getHeadline().startsWith(prefix);
	}

}
