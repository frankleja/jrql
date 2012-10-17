package com.hlcl.rql.hip.as;

import java.sql.Connection;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.PageAction;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.PageInfoSet;
import com.hlcl.rql.util.as.PageListener;

/**
 * @author lejafr
 * 
 * This class invokes a page action on all physical pages found in the tree below the given start page.
 * <p>
 * Double linked physical pages are not investigated twice.
 */
public class PhysicalPagesWalker {

	private PageListener dependent; // MVC dependent JSP interface to show
	// progress
	private PhysicalPage physicalStartPage;
	private PageInfoSet treePages;
	private PageAction pageAction;

	/**
	 * Constructor. Feed with the page info set to be sure not to investigate same page twice.
	 */
	public PhysicalPagesWalker(Connection connection, String setTableName) throws RQLException {
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
	 * For the start page (should be a physical page as well) and all physical pages below invoke the page action.
	 */
	public void walk(Page startPage, PageAction physicalPageAction) throws RQLException {
		// remember
		this.physicalStartPage = new PhysicalPage(startPage);
		this.pageAction = physicalPageAction;

		// walk through and invoke page action for every physical page
		doRecursive(physicalStartPage);
	}

	/**
	 * Collect recursively over physical pages.
	 */
	private void doRecursive(PhysicalPage physicalPage) throws RQLException {

		// inform about changed physical page
		changed(physicalPage);

		// invoke configured page action
		pageAction.invoke(physicalPage.getPage());

		// search on all physical children
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
			
			// preserve heap space
			child.freeOccupiedMemory();
		}
	}

	/**
	 * Bereitet das set vor, das verhindert, dass Teilbäume doppelt untersucht werden.
	 */
	private void initialize(Connection connection, String setTableName) throws RQLException {
		treePages = new PageInfoSet(connection, setTableName);
	}

	/**
	 * Setzt den abhängigen listener, der über jede neue physical page informiert wird, um einen fortschritt anzuzeigen.
	 */
	public void setListener(PageListener dependentListener) {
		dependent = dependentListener;
	}

	/**
	 * Returns the number of pages in the tree.
	 */
	public int size() throws RQLException {
		return treePages.size();
	}

}
