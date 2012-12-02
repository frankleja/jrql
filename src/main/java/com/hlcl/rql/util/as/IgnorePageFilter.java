package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * Ein Dummy Seitenfilter, der jede Seite ablehnt.
 */
public class IgnorePageFilter extends PageFilterImpl {

	/**
	 * constructor comment.
	 */
	public IgnorePageFilter() {
		super();
	}
	/** 
	 * Liefert false für jede Seite.
	 */
	public boolean check(Page page) throws RQLException {
		return false;
	}

}
