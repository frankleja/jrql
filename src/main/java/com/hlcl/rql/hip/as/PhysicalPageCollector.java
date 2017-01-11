package com.hlcl.rql.hip.as;

import java.sql.Connection;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.*;

/**
 * @author lejafr
 *
 * This class collects all physical pages in a database. Using this for org and process tree make a compare possible. 
 * Double linked physical pages are not investigated twice. 
 */
public class PhysicalPageCollector implements Runnable {

	private PageListener dependent; // MVC dependent JSP interface to show progress
	private PhysicalPage startPage;
	private PageInfoSet treePages;

	/**
	 * Constructor. Pass start page via collect() method.
	 */
	public PhysicalPageCollector(Connection connection, String setTableName) throws RQLException {
		initialize(connection, setTableName);
	}

	/**
	 * Constructor
	 */
	public PhysicalPageCollector(Page startPage, Connection connection, String setTableName) throws RQLException {
		setStartPage(startPage);
		initialize(connection, setTableName);
	}

	/**
	 * Informiert die dependent JSP darüber, dass die Seite gewechselt wurde. 
	 */
	private void changed(PhysicalPage currentPg) throws RQLException {
		if (dependent != null) {
			dependent.update(currentPg.getPage());
		}
	}

	/**
	 * Collects all physical child pages of the start page. Repeated physical child pages will not be investigated.
	 */
	public void collectPhysicalPages() throws RQLException {
		start();
	}

	/**
	 * Collects all physical child pages of the start page. Repeated physical child pages will not be investigated.
	 */
	public void collectPhysicalPages(Page startPage) throws RQLException {
		setStartPage(startPage);
		start();
	}

	/**
	 * Returns true, if given page is was collected. Searched by page ID. 
	 */
	public boolean containsPage(Page page) throws RQLException {
		return treePages.containsPage(page);
	}

	/**
	 * Returns true, if given page id is was collected. 
	 */
	public boolean containsPage(String pageId) throws RQLException {
		return treePages.containsPage(pageId);
	}

	/**
	 * Collect recursively over physical pages.
	 */
	private void doRecursive(PhysicalPage physicalPage) throws RQLException {

		// inform about changed physical page
		changed(physicalPage);

		// search on all physical childs
		// for all physical pages below do
		PageArrayList physicalChilds = physicalPage.getAllPhysicalChildPages(false);
		for (int i = 0; i < physicalChilds.size(); i++) {
			Page child = (Page) physicalChilds.get(i);
			// skip pages investigated already
			if (treePages.containsPage(child)) {
				continue;
			}
			// remember this physical page
			treePages.addWithoutCheck(child);

			// go deeper on this physical child 
			doRecursive(physicalPage.morphInto(child));
		}
	}

	/**
	 * Returns the page GUID for the current row.
	 */
	public String getCurrentPageGuid() throws RQLException {
		return treePages.getCurrentPageGuid();
	}

	/**
	 * Returns the page headline for the current row.
	 */
	public String getCurrentPageHeadline() throws RQLException {
		return treePages.getCurrentPageHeadline();
	}

	/**
	 * Returns the page ID for the current row.
	 */
	public String getCurrentPageId() throws RQLException {
		return treePages.getCurrentPageId();
	}

	/**
	 * Returns the page template name for the current row.
	 */
	public String getCurrentPageTemplateName() throws RQLException {
		return treePages.getCurrentPageTemplateName();
	}

	/**
	 * Returns the page info set.
	 */
	public PageInfoSet getPageInfoSet() {
		return treePages;
	}

	/**
	 * Informiert die dependent JSP darüber, dass die Seite gewechselt wurde. 
	 */
	private void initialize(Connection connection, String setTableName) throws RQLException {
		treePages = new PageInfoSet(connection, setTableName);
	}

	/**
	 * Forwards to next row of result set to get all information out of the set - unsorted. Returns true, if next row is available.
	 */
	public boolean nextPage() throws RQLException {
		return treePages.nextPage();
	}

	/**
	 * Forwards to next row of result set to get all information out of the set. Sorted by template name of page. Returns true, if next row is available.
	 */
	public boolean nextPageSortedByTemplateName() throws RQLException {
		return treePages.nextPageSortedByTemplateName();
	}

	/**
	 * Threadinterface
	 * @see #collectPhysicalPages() 
	 */
	public void run() {
		try {
			start();
		} catch (RQLException ex) {
			throw new RuntimeException("RQL Exception in Thread", ex);
		}
	}

	/**
	 * Setzt den aghängigen listener, der über jede neue physical page informiert wird, um einen fortschritt anzuzeigen. 
	 */
	public void setListener(PageListener dependentListener) {
		dependent = dependentListener;
	}

	/**
	 * Informiert die dependent JSP darüber, dass die Seite gewechselt wurde. 
	 */
	private void setStartPage(Page startPage) throws RQLException {
		this.startPage = new PhysicalPage(startPage);
	}

	/**
	 * Returns the number of pages in the tree.
	 */
	public int size() throws RQLException {
		return treePages.size();
	}

	/**
	 * Central method to start collecting of pages. Throws an exception if no start page was given.
	 */
	private void start() throws RQLException {

		// check for start page
		if (startPage == null) {
			throw new RQLException("This PhysicalPageCollector cannot start, because a start page is missing. Pass it via the constructor or via the collect() method.");
		}
		// collect recursively
		doRecursive(startPage);
	}
}
