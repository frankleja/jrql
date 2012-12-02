package com.hlcl.rql.util.as;

import com.hlcl.rql.as.*;

/**
 * @author lejafr
 *
 * This class implements comparision logic to compare first by a date element desc and than headline asc.
 * This comparision logic fits perfect for a news list. 
 * The <code>forceDescendingOrdering</code> makes no sense here.
 * 
 * All pages in the list needs to have this date element. 
 * Very often they are instances of the same template.
 */
public class StandardFieldDateDescAndHeadlineAscPageComparator extends PageComparator {
	private String dateTmpltElemName;
	/**
	 * Creates a Comparator for date elements. 
	 * Assume ascending ordering per default.
	 */
	public StandardFieldDateDescAndHeadlineAscPageComparator(String dateTmpltElemName) {
		super();
		this.dateTmpltElemName = dateTmpltElemName;
	}
	/**
	 * Compare given pages by the value of the standard field date element. 
	 */
	public int comparePages(Page p1, Page p2) {
		String d1,d2,h1,h2;
		try {
			d1 = ReddotDate.formatAsyyyyMMdd(p1.getStandardFieldDateValue(dateTmpltElemName));
			h1 = p1.getHeadline();
			d2 = ReddotDate.formatAsyyyyMMdd(p2.getStandardFieldDateValue(dateTmpltElemName));
			h2 = p2.getHeadline();
		} catch (RQLException ex) {
			// wrap the rql exception into a valid class cast exeption
			String msg = "RQLException: "+ex.getMessage();
			Throwable re = ex.getReason();
			if (re != null) {
				msg += re.getClass().getName() + ": " + re.getMessage();
			}
			throw new ClassCastException(msg);
		}
		// compare 
		int dateResult = d1.compareTo(d2);
		// if same date headline asc 
		if (dateResult == 0) {
			return h1.compareTo(h2);
		}
		// date desc
		return -dateResult; 
	}
}
