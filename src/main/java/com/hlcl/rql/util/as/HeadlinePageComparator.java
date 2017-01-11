package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * This class implements the compare method to sort a list of pages by page headline ascending.
 */
public class HeadlinePageComparator extends PageComparator  {
	/**
	 * Creates a Comparator for sort by page headline. 
	 * Assume ascending ordering per default.
	 */
	public HeadlinePageComparator() {
		super();
	}
	/**
	 * Compare given pages by the value of the headline. 
	 */
	public int comparePages(Page p1, Page p2) {
		String headline1;
		String headline2;
		try {
			headline1 = p1.getHeadline();
			headline2 = p2.getHeadline();
		} catch (RQLException ex) {
			// wrap the rql exception into a valid class cast exeption
			String msg = "RQLException: "+ex.getMessage();
			Throwable re = ex.getReason();
			if (re != null) {
				msg += re.getClass().getName() + ": " + re.getMessage();
			}
			throw new ClassCastException(msg);
		}
		return headline1.compareTo(headline2);
	}
}
