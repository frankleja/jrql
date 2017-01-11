package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * This class implements the compare method to sort a list of pages by a standard field text element.
 * All pages in the list needs to have this text element. 
 * Very often they are instances of the same template.
 */
public class StandardFieldTextPageComparator extends PageComparator {
	private String textTmpltElemName;
	/**
	 * Creates a Comparator for text elements. 
	 * Assume ascending ordering per default.
	 */
	public StandardFieldTextPageComparator(String textTmpltElemName) {
		super();
		this.textTmpltElemName = textTmpltElemName;
	}
	/**
	 * Compare given pages by the value of the standard field date element. 
	 * 
	 */
	public int comparePages(Page p1, Page p2) {
		String t1;
		String t2;
		try {
			t1 = p1.getStandardFieldTextValue(textTmpltElemName);
			t2 = p2.getStandardFieldTextValue(textTmpltElemName);
		} catch (RQLException ex) {
			// wrap the rql exception into a valid class cast exeption
			String msg = "RQLException: "+ex.getMessage();
			Throwable re = ex.getReason();
			if (re != null) {
				msg += re.getClass().getName() + ": " + re.getMessage();
			}
			throw new ClassCastException(msg);
		}
		return t1.compareTo(t2);
	}
}
