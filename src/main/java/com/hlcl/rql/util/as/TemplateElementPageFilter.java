package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * Filters all pages based on a given template element name.
 */
public class TemplateElementPageFilter extends PageFilterImpl {

	private String templateElementName;
	/**
	 * TemplatePageFilter constructor comment.
	 */
	public TemplateElementPageFilter(String templateElementName) {
		super();

		this.templateElementName = templateElementName;
	}
	/** 
	 * Liefert true, falls die gegebenen Seite ein Element dieser Seite besitzt.
	 * @see PageFilterImpl#check(Page)
	 */
	public boolean check(Page page) throws RQLException {
		return page.contains(templateElementName);
	}

}
