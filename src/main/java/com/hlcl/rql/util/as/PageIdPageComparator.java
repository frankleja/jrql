package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * This class implements the compare method to sort a list of pages by page ID.
 */
public class PageIdPageComparator extends PageComparator  {
	/**
	 * Creates a Comparator for sort by page ID. 
	 * Assume ascending ordering per default.
	 */
	public PageIdPageComparator() {
		super();
	}
	/**
	 * Compare given pages by the page ID. 
	 */
	public int comparePages(Page p1, Page p2) {
		Integer pageId1;
		Integer pageId2;
		try {
			pageId1 = p1.getPageIdAsInteger();
			pageId2 = p2.getPageIdAsInteger();
		} catch (RQLException ex) {
			// wrap the rql exception into a valid class cast exeption
			String msg = "RQLException: "+ex.getMessage();
			Throwable re = ex.getReason();
			if (re != null) {
				msg += re.getClass().getName() + ": " + re.getMessage();
			}
			throw new ClassCastException(msg);
		}
		return pageId1.compareTo(pageId2);
	}
}
