package com.hlcl.rql.hip.as;

import com.hlcl.rql.as.List;
import com.hlcl.rql.as.MultiLinkedPageException;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.UnlinkedPageException;
import com.hlcl.rql.util.as.ProjectPage;

/**
 * @author lejafr
 * 
 * This class represents a hip recycling table block.
 */
public class RecyclingTableBlock extends ProjectPage {

	private List recyclingList;

	public RecyclingTableBlock(Page page) {
		super(page);
	}

	/**
	 * Links the given content page to this recycling table block page.
	 */
	public void add(ContentPage contentPage) throws RQLException {
		add(contentPage.getPage());
	}

	/**
	 * Links the given page to this recycling table block page.
	 */
	public void add(Page page) throws RQLException {
		getRecyclingList().connectToExistingPage(page);
	}

	/**
	 * Moves the given page needed again to the target html view table block, if it is linked to this recycling table block, otherwise
	 * not.
	 * 
	 * @return true, if pageNeededAgain is moved successfully false, if pageNeededAgain is not a child of this recycling table block
	 */
	public boolean reactivate(Page pageNeededAgain, HtmlViewTableBlock targetBlockPage) throws RQLException {
		if (!getRecyclingList().isChild(pageNeededAgain)) {
			return false;
		}

		// add first
		targetBlockPage.add(pageNeededAgain);
		// disconnect second
		getRecyclingList().disconnectChild(pageNeededAgain);
		return true;
	}

	/**
	 * Returns the recycling list List from this recycling table block.
	 */
	private List getRecyclingList() throws RQLException {
		if (recyclingList == null) {
			recyclingList = getPage().getList(getParameter("recyclingListTmpltElemName"));
		}
		return recyclingList;
	}

}
