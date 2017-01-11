package com.hlcl.rql.hip.as;

import java.util.*;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.*;

/**
 * @author lejafr
 *
 * This class searches for text within physical pages (including their block childs) recursively per physical page. 
 * Double linked physical pages are scanned twice; there is no optimisation to prevent this. 
 */
public class PhysicalPageTextFinder {

	// cache physical pages ever touched and do not check these again (prevent a loop)
	private PageCache checkedPages;
	private String delimiter;
	// MVC dependent JSP interface to show progress
	private PageListener dependent;
	// cache while recursive search is running
	private String findList;

	private boolean scanFollowingPhysicalPages;
	// hold result
	private java.util.List searchResults;
	private PhysicalPage startPage;
	private boolean caseSensitive;

	/**
	 * Construct a physical page wrapping the given general page.
	 */
	public PhysicalPageTextFinder(Page page) throws RQLException {
		this.startPage = new PhysicalPage(page);
	}

	/**
	 * Glue a physical page together with the search result and add into list. 
	 */
	private void addResult(PhysicalPage physicalPage, Set found) {
		searchResults.add(new TextFinderResult(physicalPage, found));
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
	 * Scannt alle child pages der Startseite nach den gegebenen Suchbegriffen..<p> 
	 * Das zurückgegebene Set ist leer, wenn keiner der Begriffe auf der Startseite oder Ihren Kindseiten vorkommt. 
	 * @return a list of TextFinderResult
	 * @see TextFinderResult
	 */
	public java.util.List collectContainedText(String findList, String delimiter, boolean scanFollowingPhysicalPages, boolean caseSensitive)
			throws RQLException {
		// save locally
		this.findList = findList;
		this.delimiter = delimiter;
		this.scanFollowingPhysicalPages = scanFollowingPhysicalPages;
		this.caseSensitive = caseSensitive;

		// initialize scan
		searchResults = new ArrayList();
		checkedPages = new PageCache(startPage.getProject());

		// collect recursively
		doRecursive(startPage);
		return searchResults;
	}

	/**
	 * Collect recursively over physical pages.
	 */
	private void doRecursive(PhysicalPage physicalPage) throws RQLException {

		// search in the physical page and remember
		changed(physicalPage);
		Set found = physicalPage.collectContainedText(findList, delimiter, caseSensitive);
		if (!found.isEmpty()) {
			addResult(physicalPage, found);
		}
		// search on all physical childs
		if (scanFollowingPhysicalPages) {
			// treat given physical page as checked, before go deeper
			checkedPages.put(physicalPage.getPageGuid(), physicalPage.getPage());

			// for all physical pages below do
			PageArrayList physicalChilds = physicalPage.getAllPhysicalChildPages(true);
			for (int i = 0; i < physicalChilds.size(); i++) {
				Page child = (Page) physicalChilds.get(i);
				// skip ever touched pages to prevent looping
				if (checkedPages.containsKey(child.getPageGuid())) {
					continue;
				}
				// try on physical child page
				doRecursive(physicalPage.morphInto(child));
			}
		}
	}

	/**
	 * Liefert eine Liste mit allen TextFinderResult Objekten, die ab der Startpage gefunden wurden. 
	 * @return java.util.List of TextFinderResult
	 */
	public List getSearchResults() {
		return searchResults;
	}

	/**
	 * Setzt den aghängigen listener, der über jede neue physical page informiert wird, um einen fortschritt anzuzeigen. 
	 */
	public void setListener(PageListener dependentListener) {
		dependent = dependentListener;
	}
}
