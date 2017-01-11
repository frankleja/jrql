package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * This class implements the compare method to sort a list of pages by last changed user name ascending.
 */
public class LastChangedUserNamePageComparator extends PageComparator  {
	/**
	 * Creates a Comparator. 
	 * Assume ascending ordering per default.
	 */
	public LastChangedUserNamePageComparator() {
		super();
	}
	/**
	 * Compare given pages by the last changed user name. 
	 */
	public int comparePages(Page p1, Page p2) {
		String name1;
		String name2;
		try {
			name1 = p1.getLastChangedByUserName();
			name2 = p2.getLastChangedByUserName();
		} catch (RQLException ex) {
			// wrap the rql exception into a valid class cast exeption
			String msg = "RQLException: "+ex.getMessage();
			Throwable re = ex.getReason();
			if (re != null) {
				msg += re.getClass().getName() + ": " + re.getMessage();
			}
			throw new ClassCastException(msg);
		}
		return name1.compareTo(name2);
	}
}
