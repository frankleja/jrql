package com.hlcl.rql.util.as;

import java.util.*;

import com.hlcl.rql.as.*;

/**
 * @author lejafr
 *
 * This class is a memory optimized cache for pages.
 */
public class PageCache {
	
	Project project;
	
	// maps an object to a page guid (string) as proxy for the page object
	private Map pageCache;
	 
	/**
	 * Construct a page cache.
	 */
	public PageCache(Project project) {
		super();
		
		this.project = project;
		
		// initialize
		pageCache = new HashMap();
	}
	/**
	 * Associates the specified value with the specified page in this map.
	 * If the map previously contained a mapping for this key, the old
	 * page is replaced.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param page value to be associated with the specified key.
	 */
	public void put(Object key, Page page) throws RQLException {
		pageCache.put(key, page.getPageGuid());		
	}
	/**
	 * Returns the Page to which the specified key is mapped in this identity
	 * hash map, or <tt>null</tt> if the map contains no mapping for this key.
	 * A return value of <tt>null</tt> does not <i>necessarily</i> indicate
	 * that the map contains no mapping for the key; it is also possible that
	 * the map explicitly maps the key to <tt>null</tt>. The
	 * <tt>containsKey</tt> method may be used to distinguish these two cases.
	 *
	 * @param   key the key whose associated value is to be returned.
	 * @return  the page to which this map maps the specified key, or
	 *          <tt>null</tt> if the map contains no mapping for this key.
	 * @see #put(Object, Page)
	 */
	public Page get(Object key) throws RQLException {
		String guid = (String) pageCache.get(key);
		if (guid != null) {
			return new Page(project, guid);
		} else {
			return null;
		}
	}
	/**
	 * Returns <tt>true</tt> if this page cache contains a mapping for the
	 * specified key.
	 *
	 * @param   key   The key whose presence in this map is to be tested
	 * @return <tt>true</tt> if this map contains a mapping for the specified key.
	 */
	public boolean containsKey(Object key) {
		return pageCache.containsKey(key);
	}
	/**
	 * Returns all cached pages.
	 *
	 * @return <code>PageArrayList</code>
	 */
	public PageArrayList getAllPages() throws RQLException {
		Collection guids = pageCache.values();
		
		// convert 
		PageArrayList pages = new PageArrayList(guids.size());
		for (Iterator iter = guids.iterator(); iter.hasNext();) {
			String guid = (String) iter.next();
			pages.add(new Page(project, guid));
		}
		return pages;
	}
}
