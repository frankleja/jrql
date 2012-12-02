package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * Filters all pages based on the given template name.
 */
public class TemplatePageFilter extends PageFilterImpl {

	private String templateName;
	/**
	 * TemplatePageFilter constructor comment.
	 */
	public TemplatePageFilter(String templateName) {
		super();

		this.templateName = templateName;
	}
	/* (non-Javadoc)
	 * @see com.hlcl.rql.as.PageFilter#check(com.hlcl.rql.as.Page)
	 */
	public boolean check(Page page) throws RQLException {
		return page.isBasedOnTemplate(templateName);
	}

}
