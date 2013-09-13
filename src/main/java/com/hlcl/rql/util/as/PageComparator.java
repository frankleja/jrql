package com.hlcl.rql.util.as;

import java.util.Comparator;

import com.hlcl.rql.as.Page;

/**
 * @author lejafr
 *
 * This class is the root of all page comparators.
 */
public abstract class PageComparator implements Comparator<Page> {
	private int sortMode;
	
	// constants for sort mode
	private final int ASCENDING = 0;
	private final int DESCENDING = 1;

	/**
	 * Creates a Comparator default ordering mode ascending.
	 */
	public PageComparator() {
		super();
		sortMode = ASCENDING;
	}
	/**
	 * Change the sort mode from ascending (default) to descending. 
	 */
	public void forceDescendingOrdering() {
		sortMode = DESCENDING;
	}
	/**
	 * Needed method from Comparator.
     * @return a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second. 
     * @throws ClassCastException if the arguments' types prevent them from
     * 	       being compared by this Comparator.
	 */
	public final int compare(Page p1, Page p2) {
		// handle descending too
		int result = comparePages(p1, p2);
		if (sortMode == DESCENDING) {
			result *= -1;
		}
		return result;
	}
	/**
	 * Compare given pages for ascending ordering.
	 * 
     * @return a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second. 
     * @throws ClassCastException if the arguments' types prevent them from
     * 	       being compared by this Comparator.
	 */
	public abstract int comparePages(Page page1, Page page2);
}
