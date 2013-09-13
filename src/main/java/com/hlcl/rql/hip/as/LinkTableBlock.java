package com.hlcl.rql.hip.as;

import java.util.SortedSet;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StringHelper;
import com.hlcl.rql.as.TextAnchor;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.RowTableBlock;

/**
 * @author lejafr
 * 
 * Represents a link table block page in hip.
 */
public class LinkTableBlock extends RowTableBlock {

	/**
	 * Creates a hip link table block.
	 */
	public LinkTableBlock(Page page) {
		super(page);
	}

	/**
	 * Returns the name of the parameter which value is the name of the list template element name.
	 */
	public String getRowsListTmpltElemParameterName() {
		return "linkListTmpltElemName";
	}

	/**
	 * Sort all link rows by given headlines (headline=clickable text).
	 */
	public void sortLinkRowsByGivenHeadlines(SortedSet<String> orderedHeadlines) throws RQLException {
		sortChildrenByGivenHeadlines(orderedHeadlines);
	}

	/**
	 * Remove all given link rows from this table block
	 */
	public void removeLinkRows(PageArrayList childrenToBeRemoved) throws RQLException {
		disconnectChildren(childrenToBeRemoved);
	}

	/**
	 * Returns all link row pages on this link table block.
	 * 
	 * @throws RQLException
	 */
	public PageArrayList getAllLinkRows() throws RQLException {
		return getRows();
	}

	/**
	 * Returns the first link row for the given URL from this table block or creates a new link row.
	 * 
	 * @see #getLinkRowByUrl(String)
	 * @see #createLinkRow(String, String, boolean)
	 * @throws RQLException
	 */
	public Page getOrCreateLinkRowByUrl(String clickableText, String url, boolean isLinkPasswordProtected) throws RQLException {
		Page result = getLinkRowByUrl(url);
		if(result == null) {
			result = createLinkRow(clickableText, url, isLinkPasswordProtected);
		}
		return result;
	}
	/**
	 * Creates a new link row in this block and fill all given row values. Updated is set to today.
	 * 
	 * @throws RQLException
	 */
	public Page createLinkRow(String clickableText, String url, boolean isLinkPasswordProtected) throws RQLException {
		Page currentLinkRow = super.createRow(clickableText);
		updateCurrentLinkRow(url, isLinkPasswordProtected);
		return currentLinkRow;
	}

	/**
	 * Returns the first link row of this link table block matching the given URL (using equals) or null, if no row page could be
	 * found.
	 */
	public Page getLinkRowByUrl(String url) throws RQLException {
		PageArrayList linkRows = getRows();
		for (int i = 0; i < linkRows.size(); i++) {
			Page rowPg = linkRows.getPage(i);
			TextAnchor anchor = rowPg.getTextAnchor(getParameter("linkTmpltElemName"));
			if (anchor.isUrlEquals(url)) {
				return rowPg;
			}
		}
		// signal not found
		return null;
	}
	/**
	 * Updates the current link row in this table with given values.
	 */
	public void updateCurrentLinkRow(String clickableText, String url, boolean isLinkPasswordProtected) throws RQLException {
		getCurrentRowPage().setHeadline(clickableText);
		updateCurrentLinkRow(url, isLinkPasswordProtected);
	}

	/**
	 * Updates the current link row in this table with given values.
	 */
	public void updateCurrentLinkRow(String url, boolean isLinkPasswordProtected) throws RQLException {
		updateLinkRow(getCurrentRowPage(), url, isLinkPasswordProtected);
	}

	/**
	 * Updates the given link row in this table with given values.
	 */
	private void updateLinkRow(Page rowPage, String url, boolean isLinkPasswordProtected) throws RQLException {
		// update content elements
		rowPage.startSetElementValues();
		rowPage.addSetStandardFieldDateValueToToday(getParameter("linkRowPageUpdatedTmpltElemName"));
		rowPage.addSetOptionListValue(getParameter("linkRowPasswordTmpltElemName"), StringHelper.convertToYesNo(isLinkPasswordProtected));
		rowPage.endSetElementValues();
		// assign url
		rowPage.getTextAnchor(getParameter("linkTmpltElemName")).setUrl(url);
	}

	/**
	 * Überschreibt an der augenblicklichen Linkzeile die gegeben Werte. Mit {@link #nextRow()} kann zur nächsten Zeile navigiert
	 * werden.
	 */
	public void overwriteLinkRow(String clickableText, String url, boolean isLinkPasswordProtected) throws RQLException {
		Page pageToOverwrite = overwriteCurrentRowPage(clickableText);
		updateLinkRow(pageToOverwrite, url, isLinkPasswordProtected);
	}

	/**
	 * Save the given version code within RedDot link table block.
	 */
	public void setRdVersionCode(String versionCode) throws RQLException {
		setStandardFieldTextValue(getParameter("versionCodeTmpltElemName"), versionCode);
	}

	/**
	 * Returns the saved version code from this link table block.
	 */
	public String getRdVersionCode() throws RQLException {
		return getStandardFieldTextValue(getParameter("versionCodeTmpltElemName"));
	}

}
