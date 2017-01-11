package com.hlcl.rql.util.as;

import java.util.Map;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * Dieser Filer liefert true, falls die page guid der gegebenen Seite in der Map enthalten ist.
 */
public class PageGuidMapPageFilter extends PageFilterImpl {

	private Map pageGuidMap;
	
	/**
	 * constructor comment.
	 */
	public PageGuidMapPageFilter(Map pageGuidMap) {
		super();
		
		this.pageGuidMap = pageGuidMap;
	}
	/** 
	 * Liefert true, falls die page guid der gegebenen Seite in der Map enthalten ist.
	 */
	public boolean check(Page page) throws RQLException {
		return pageGuidMap.containsKey(page.getPageGuid());
	}

}
