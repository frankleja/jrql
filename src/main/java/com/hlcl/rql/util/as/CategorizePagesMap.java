package com.hlcl.rql.util.as;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 * This class maps a category (simply a string) to a set of pages and provides access to categories and page sets.
 */
public class CategorizePagesMap {

	// maps a category string to a set of pages
	private Map<String, Set<Page>> categoryMap;
	private boolean freeOccupiedPageMemory;

	/**
	 * Creates a category page set map.
	 * 
	 * @param freeOccupiedPageMemory if true, all added pages will release there internal caches f 
	 */
	public CategorizePagesMap(boolean freeOccupiedPageMemory) {
		super();

		this.freeOccupiedPageMemory = freeOccupiedPageMemory;

		// initialize map
		categoryMap = new HashMap<String, Set<Page>>();
	}

	/**
	 * Adds the given page under the given categories.
	 */
	public void add(Page page, java.util.List<String> categories) throws RQLException {
		for (String category : categories) {
			add(page, category);
		}
	}
	
	/**
	 * Adds the given page under the given category.
	 */
	public void add(Page page, String category) throws RQLException {
		Set<Page> setOrNull = getPages(category);
		// lazy initialize set
		if (setOrNull == null) {
			setOrNull = new HashSet<Page>();
			categoryMap.put(category, setOrNull);
		}
		// reduce memory footprint of page
		if (freeOccupiedPageMemory) {
			page.freeOccupiedMemory();
		}

		// add page
		setOrNull.add(page);
	}

	/**
	 * Returns the Set for the given category or null, if this map did not contain pages for given category at all.
	 */
	public Set<Page> getPages(String category) throws RQLException {
		return categoryMap.get(category);
	}

	/**
	 * Returns the Set sorted by page headline for the given category or null, if this map did not contain pages for given category at all.
	 */
	public SortedSet<Page> getPagesSortedByHeadline(String category) throws RQLException {
		SortedSet<Page> result = new TreeSet<Page>(new HeadlinePageComparator());
		result.addAll(getPages(category));
		return result;
	}

	/**
	 * Returns true, if this map contains the given category, otherwise false.
	 */
	public boolean containsCategory(String category) throws RQLException {
		return categoryMap.containsKey(category);
	}

	/**
	 * Returns all added categories of this map sorted ascending. 
	 */
	public SortedSet<String> getCategories() throws RQLException {
		return new TreeSet<String>(categoryMap.keySet());
	}
}
