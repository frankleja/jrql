package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * Filters all pages using the logical negation of the given page filter.
 */
public class NotPageFilter extends PageFilterImpl {

	private PageFilter pageFilter;
	/**
	 * TemplatePageFilter constructor comment.
	 */
	public NotPageFilter(PageFilter pageFilter) {
		super();

		this.pageFilter = pageFilter;
	}
	/** 
	 * Liefert true genau dann, wenn der page filter für die gegebene Seite false ist.
	 * 
	 * @see PageFilter#check(Page)
	 */
	public boolean check(Page page) throws RQLException {
		return !pageFilter.check(page);
	}

}
