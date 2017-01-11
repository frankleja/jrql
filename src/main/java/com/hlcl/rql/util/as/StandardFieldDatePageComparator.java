package com.hlcl.rql.util.as;

import com.hlcl.rql.as.*;

/**
 * @author lejafr
 *
 * This class implements the compare method to sort a list of pages by a standard field date element.
 * All pages in the list needs to have this date element. 
 * Very often they are instances of the same template.
 */
public class StandardFieldDatePageComparator extends PageComparator {
	private String dateTmpltElemName;
	/**
	 * Creates a Comparator for date elements. 
	 * Assume ascending ordering per default.
	 */
	public StandardFieldDatePageComparator(String dateTmpltElemName) {
		super();
		this.dateTmpltElemName = dateTmpltElemName;
	}
	/**
	 * Compare given pages by the value of the standard field date element. 
	 * 
	 */
	public int comparePages(Page p1, Page p2) {
		ReddotDate d1;
		ReddotDate d2;
		try {
			d1 = p1.getStandardFieldDateValue(dateTmpltElemName);
			d2 = p2.getStandardFieldDateValue(dateTmpltElemName);
		} catch (RQLException ex) {
			// wrap the rql exception into a valid class cast exeption
			String msg = "RQLException: "+ex.getMessage();
			Throwable re = ex.getReason();
			if (re != null) {
				msg += re.getClass().getName() + ": " + re.getMessage();
			}
			throw new ClassCastException(msg);
		}
		return d1.compareTo(d2);
	}
}
