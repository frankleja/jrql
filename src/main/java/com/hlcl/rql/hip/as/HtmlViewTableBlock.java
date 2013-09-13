package com.hlcl.rql.hip.as;

import com.hlcl.rql.as.List;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.ProjectPage;

/**
 * @author lejafr
 * 
 * This class represents a hip html view table block.
 */
public class HtmlViewTableBlock extends ProjectPage {

	private List htmlViewList;

	public HtmlViewTableBlock(Page page) {
		super(page);
	}

	/**
	 * Links the given page to this html view table block page.
	 */
	public void add(Page page) throws RQLException {
		getHtmlViewList().connectToExistingPage(page);
	}

	/**
	 * Returns the recycling list List from this recycling table block.
	 */
	private List getHtmlViewList() throws RQLException {
		if (htmlViewList == null) {
			htmlViewList = getPage().getList(getParameter("htmlListTmpltElemName"));
		}
		return htmlViewList;
	}

}
