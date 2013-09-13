package com.hlcl.rql.util.as;

import java.util.SortedSet;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 * Represents an arbitrary table block containing only data rows.
 */
public abstract class RowTableBlock extends ProjectPage {

	private com.hlcl.rql.as.List rowsList;
	private int existingRowsSize;
	private int overwrittenRowsCounter;
	private Page currentRowPage;
	private PageArrayList rows;

	/**
	 * Creates a row table block.
	 */
	public RowTableBlock(Page page) {
		super(page);
	}

	/**
	 * Creates a new row in this table block. Subclasses has to fill all values afterwards.
	 */
	protected Page createRow(String rowHeadline) throws RQLException {
		// create page
		currentRowPage = getRowsList().createAndConnectPage(rowHeadline);
		return currentRowPage;
	}

	/**
	 * Sorts all children by headline accordingly to the given ordered headlines.
	 */
	protected void sortChildrenByGivenHeadlines(SortedSet<String> orderedHeadlines) throws RQLException {
		getRowsList().sortChildsByGivenHeadlines(orderedHeadlines);
	}

	/**
	 * Unlink all given child pages from this row table block.
	 */
	protected void disconnectChildren(PageArrayList childrenToDisconnect) throws RQLException {
		getRowsList().disconnectChildren(childrenToDisconnect);
	}

	/**
	 * Returns the list element.
	 */
	private com.hlcl.rql.as.List getRowsList() throws RQLException {
		if (rowsList == null) {
			rowsList = getPage().getList(getParameter(getRowsListTmpltElemParameterName()));
		}
		return rowsList;
	}

	/**
	 * Returns the name of the parameter which value is the name of the list template element name.
	 */
	abstract public String getRowsListTmpltElemParameterName();

	/**
	 * Returns all rows of this table page.
	 */
	protected PageArrayList getRows() throws RQLException {
		if (rows == null) {
			rows = getRowsList().getChildPages();
		}
		return rows;
	}

	/**
	 * Updates the headline on the given row page.
	 */
	private void setCurrentRowHeadline(String headline) throws RQLException {
		setRowHeadline(getCurrentRowPage(), headline);
	}

	/**
	 * Updates the headline on the given row page.
	 */
	private void setRowHeadline(Page rowPage, String headline) throws RQLException {
		rowPage.setHeadline(headline);
	}

	/**
	 * Returns the current row page.
	 * 
	 * @see #firstRow()
	 * @see #nextRow()
	 */
	protected Page getCurrentRowPage() throws RQLException {
		return currentRowPage;
	}

	/**
	 * Ends the overwriting mode on this table.
	 * 
	 * @see #startOverwriting() Call this method first to start the overwriting mode.
	 */
	public void endOverwriting() throws RQLException {
		// delete unnecessary row pages
		for (int i = overwrittenRowsCounter; i < existingRowsSize; i++) {
			getRows().getPage(i).delete();
		}
	}

	/**
	 * Overwrites the current row, always update the given headline.
	 * <p>
	 * Returns the row page which has to be filled in subclasses afterwards.
	 */
	protected Page overwriteCurrentRowPage(String rowHeadline) throws RQLException {
		Page pageToOverwrite = currentRowPage;
		// it is assumed that method firstRow() was called before
		// check, if create or update
		if (overwrittenRowsCounter < existingRowsSize) {
			// update at least given row headline
			setCurrentRowHeadline(rowHeadline);
			// only navigation, element update in subclass
			nextRow();
		} else {
			// create new row page
			pageToOverwrite = createRow(rowHeadline);
		}
		overwrittenRowsCounter++;
		return pageToOverwrite;
	}

	/**
	 * Start the overwriting mode of this table.
	 * <p>
	 * Existing row pages will be updated, new row pages will be created and to many rows at the end will be deleted.
	 * 
	 * @see #endOverwriting() Needed call to end the overwriting mode.
	 */
	public void startOverwriting() throws RQLException {
		// remember and initialize
		existingRowsSize = getRows().size();
		overwrittenRowsCounter = 0;

		// set current link row to prepare updates
		firstRow();
	}

	/**
	 * Starts an iterating over all row pages within that table.
	 */
	public void firstRow() throws RQLException {
		if (hasRows()) {
			currentRowPage = getRows().first();
		}
	}

	/**
	 * Returns true, if table has at least one row page.
	 */
	public boolean hasRows() throws RQLException {
		return !getRows().isEmpty();
	}

	/**
	 * Returns the number of row pages in this table.
	 */
	public int size() throws RQLException {
		return getRows().size();
	}

	/**
	 * Move the row page iterator to next row page. Returns false, if no further row pages are available.
	 */
	public boolean nextRow() throws RQLException {

		// no start or no row at all
		if (currentRowPage == null) {
			return false;
		}

		// move one row page down
		int currentIndex = getRows().indexOf(currentRowPage);
		boolean result = false;
		currentRowPage = null;
		if (currentIndex < getRows().size() - 1) {
			currentRowPage = getRows().getPage(currentIndex + 1);
			result = true;
		}

		return result;
	}
}
