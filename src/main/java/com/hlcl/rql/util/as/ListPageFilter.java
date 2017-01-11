package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * Dieser Filer liefert true, falls die gegebene Seite in einer Liste enthalten ist.
 */
public class ListPageFilter extends PageFilterImpl {

	private PageArrayList pageList;
	
	/**
	 * constructor comment.
	 */
	public ListPageFilter(PageArrayList pageList) {
		super();
		
		this.pageList = pageList;
	}
	/** 
	 * Liefert true, falls page in pageList enthalten ist.
	 */
	public boolean check(Page page) throws RQLException {
		return pageList.contains(page);
	}

}
