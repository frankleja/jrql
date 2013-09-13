package com.hlcl.rql.hip.as;

import com.hlcl.rql.as.*;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.PageCache;

/**
 * @author lejafr
 *
 * This class caches (memory optimized) a whole glossary for fast access while creating cross links within glossary.
 */
public class GlossaryCache {
	
	// maps a one char string (lowercase) to the glossar page of this letter
	private PageCache letterPagesCache;
	
	Project project;
	// maps the term (lowercase) to its row page
	private PageCache termPagesCache;
	 
	/**
	 * Construct a glossary cache.
	 */
	public GlossaryCache(Project project) {
		super();
		
		this.project = project;
		
		// initialize
		letterPagesCache = new PageCache(project);
		termPagesCache = new PageCache(project);
	}
	/**
	 * Konvertiert den term zum Key für den term page cache. 
	 */
	private String convertTerm(String term) {
		return term.toLowerCase();
	}
	/**
	 * Liefert den ersten Buchstaben des Terms, lowercase.
	 */
	private String getFirstLetter(String term) {
		return StringHelper.getFirstLetter(term).toLowerCase();
	}
	/**
	 * Erstellt den Cache - mappt den term zu beiden seiten.  
	 */
	public void add(String term, Page letterPage, Page termPage) throws RQLException {
		// do not add same letter page again
		String firstLetter = getFirstLetter(term);
		if (!letterPagesCache.containsKey(firstLetter)) {
			letterPagesCache.put(firstLetter, letterPage);
		} 
		// add term page always
		termPagesCache.put(convertTerm(term), termPage);
	}
	/**
	 * Liefert die Seite für den ersten Buchstaben des Terms.
	 * Liefert null, falls keine Seite vorhanden ist. 
	 */
	public Page getLetterPage(String term) throws RQLException {
		return letterPagesCache.get(getFirstLetter(term));
	}
	/**
	 * Liefert alle gecacheten Buchstabenseiten zurück.
	 */
	public PageArrayList getAllLetterPages() throws RQLException {
		return letterPagesCache.getAllPages();
	}
	/**
	 * Liefert die Zeilenseite für den gegebenen Term.
	 * Liefert null, falls keine Seite vorhanden ist. 
	 */
	public Page getTermPage(String term) throws RQLException {
		return termPagesCache.get(convertTerm(term));
	}
}
