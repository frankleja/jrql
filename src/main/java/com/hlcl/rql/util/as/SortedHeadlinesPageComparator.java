package com.hlcl.rql.util.as;

import java.util.ArrayList;
import java.util.SortedSet;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 * This class implements the compare method to sort a list of pages by headline accordingly to a list of given headlines.
 */
public class SortedHeadlinesPageComparator extends PageComparator {

	private java.util.List<String> sortedPageHeadlines;

	public SortedHeadlinesPageComparator(java.util.List<String> sortedPageHeadlines) {
		super();
		this.sortedPageHeadlines = sortedPageHeadlines;
	}

	public SortedHeadlinesPageComparator(SortedSet<String> sortedPageHeadlines) {
		super();
		this.sortedPageHeadlines = new ArrayList<String>(sortedPageHeadlines.size());
		this.sortedPageHeadlines.addAll(sortedPageHeadlines);
	}

	public int comparePages(Page page1, Page page2) {
		int result;
		try {
			// get headlines from rd pages
			String rdHeadline1 = page1.getHeadline();
			String rdHeadline2 = page2.getHeadline();
			// get index of these ids
			Integer index1 = new Integer(sortedPageHeadlines.indexOf(rdHeadline1));
			Integer index2 = new Integer(sortedPageHeadlines.indexOf(rdHeadline2));
			result = index1.compareTo(index2);
		} catch (RQLException ex) {
			// wrap the rql exception into a valid class cast exception
			String msg = "RQLException: " + ex.getMessage();
			Throwable re = ex.getReason();
			if (re != null) {
				msg += re.getClass().getName() + ": " + re.getMessage();
			}
			throw new ClassCastException(msg);
		}
		return result;
	}
}
