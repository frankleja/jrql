package com.hlcl.rql.util.as;

import com.hlcl.rql.as.*;

/**
 * @author lejafr
 *
 * This class collect all physical pages from a tree part and link onto a target list.
 * This function is used for deleting all pages from HIP under a tree part linking all physical pages to the delete list. 
 */
public class TreePartPhysicalPagesLinker {

	private MultiLink targetLink;
	private String pageIndicatorTmpltElemName;
	private String skipChildTmpltElemName; 

	/**
	 * Construct a linker with markers to identify physical pages.
	 */
	public TreePartPhysicalPagesLinker(String pageIndicatorTmpltElemName, String skipChildTmpltElemName) {
		super();
		this.pageIndicatorTmpltElemName = pageIndicatorTmpltElemName;
		this.skipChildTmpltElemName = skipChildTmpltElemName;
	}
	/**
	 * Startet das Suchen und linken ab der gegebenen Startseite.
	 */
	public int startFromPage(Page startPage, MultiLink targetLink) throws RQLException {
		// remember only
		this.targetLink = targetLink;
		// prepare and start
		int numberOfPagesLinked = 0;
		return doRecursive(startPage, numberOfPagesLinked);
	}
	/**
	 * Startet das Suchen und Erstellen der PublishingJobs ab der gegebenen Navigationsseite.
	 */
	private int doRecursive(Page page, int numberOfPagesLinked) throws RQLException {

		java.util.List childPages = page.getChildPages();

		// check for end of recursion
		if (childPages.size() == 0) {
			return numberOfPagesLinked;
		} else {
			for (int i = 0; i < childPages.size(); i++) {
				Page child = (Page) childPages.get(i);
				// stop investigating this child further; improve performance
				if (child.contains(skipChildTmpltElemName)) {
					continue;
				}
				// only physical pages
				if (child.contains(pageIndicatorTmpltElemName)) {
					// link child to target link without order constraints and without change of main link
					targetLink.connectToExistingPage(child, false, false); 
					numberOfPagesLinked += 1;
				}
				// try on child
				numberOfPagesLinked = doRecursive(child, numberOfPagesLinked);
			}
		}
		return numberOfPagesLinked;
	}
}
