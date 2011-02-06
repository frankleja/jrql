package com.hlcl.rql.util.as;

import com.hlcl.rql.as.*;

/**
 * @author lejafr
 *
 * This class collects page filenames, find pages with same filename and count page for different aspects. 
 */
public class DoubleFilenameCollector {

	private PageCache filenameMap; // maps the filename to the page itself
	private int pagesAddedCounter;
	private int pagesWithoutFilenameCounter;
	private PageArrayList pagesWithRepeatedFilename;

	/**
	 * Constructor.
	 */
	public DoubleFilenameCollector(Project project) {
		super();

		// initialize
		filenameMap = new PageCache(project);
		pagesWithRepeatedFilename = new PageArrayList();
		pagesAddedCounter = 0;
		pagesWithoutFilenameCounter = 0;
	}

	/**
	 * Adds a physical page and update all counters accordingly. Check for pages with same filename, but do not remember pages with empty filenames.<p>
	 * Pages without filename will not be remembered. It is assumed that the page id is used in filename of such pages, so repeating filename is impossible.
	 */
	public void addPage(Page physicalPage) throws RQLException {
		pagesAddedCounter++;
		// empty filename?
		String filename = physicalPage.getFilename();
		if (filename.length() == 0) {
			pagesWithoutFilenameCounter++;
			return;
		}
		// filename already found?
		String key = buildKey(physicalPage);
		Page found = filenameMap.get(key);
		if (found != null && !found.equals(physicalPage)) {
			pagesWithRepeatedFilename.add(physicalPage);
		} else {
			// save filename for further checks
			filenameMap.put(key, physicalPage);
		}
	}

	/**
	 * @return Returns the key for the given page.
	 */
	private String buildKey(Page page) throws RQLException {
		return page.getFilename();
	}

	/**
	 * @return Returns the pagesAddedCounter.
	 */
	public int getPagesAddedCounter() {
		return pagesAddedCounter;
	}

	/**
	 * @return Returns the pagesWithoutFilenameCounter.
	 */
	public int getPagesWithoutFilenameCounter() {
		return pagesWithoutFilenameCounter;
	}

	/**
	 * @return Returns the pagesWithRepeatedFilename.
	 */
	public PageArrayList getPagesWithRepeatedFilename() {
		return pagesWithRepeatedFilename;
	}

	/**
	 * @return Returns the pagesWithRepeatedFilenameCounter.
	 */
	public int getPagesWithRepeatedFilenameCounter() {
		return pagesWithRepeatedFilename.size();
	}

	/**
	 * @return Returns the page with same filename as the given page, which should be identified as page repeating a filename. <p>
	 * Returns null, if page is not found in map of remembered pages.
	 * 
	 * @see #getPagesWithRepeatedFilename()
	 */
	public Page getPageWithSameFilename(Page sameFilenamePage) throws RQLException {
		return filenameMap.get(buildKey(sameFilenamePage));
	}
}
