package com.hlcl.rql.hip.as;

import java.util.ArrayList;
import java.util.Iterator;

import com.hlcl.rql.as.*;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.PageFilter;

/**
 * @author lejafr
 *
 * This class collect all child pages (block pages) with their multi links. 
 * The deepness of each link and child will be calculated as well. 
 */
public class PageStructureCollector {

	private String isPhysicalPageTmpltElemName;
	private String ignoreTmpltElemNameSuffix;
	private java.util.List structure;

	// remember start page
	private Page startPage;

	/**
	 * Construct a page structure collector.
	 * 
	 *@param	 isPhysicalPageTmpltElemName if template of page contains an element with this name the page is a real HTML page 
	 *@param	 ignoreTmpltElemNameSuffix skip child pages of multi links if a shadowed element for this suffix exists, e.g. _onlyPhysicalPages 
	 */
	public PageStructureCollector(String isPhysicalPageTmpltElemName, String ignoreTmpltElemNameSuffix) {
		super();
		this.isPhysicalPageTmpltElemName = isPhysicalPageTmpltElemName;
		this.ignoreTmpltElemNameSuffix = ignoreTmpltElemNameSuffix;

		// initialize an ordered collection
		structure = new ArrayList();
	}
	/**
	 * Startet das Suchen und linken ab der gegebenen Startseite.
	 * Liefert die geordnete Liste mit der vollen Struktur zurück.
	 */
	public java.util.List collectStructure(Page startPage) throws RQLException {
		// remember
		this.startPage = startPage;

		// initialize
		int deepness = 0;
		
		// handle target container constructions two levels up
		MultiLink mainMultiLink = startPage.getMainMultiLink();
		if (mainMultiLink.isTargetContainerAssigned()) {
			Page mainParentPage = mainMultiLink.getPage();
			while (!mainParentPage.contains(isPhysicalPageTmpltElemName)) {
				addFirst(mainParentPage, deepness);
				// try another level up
				mainParentPage = mainParentPage.getMainLinkParentPage(); 
			}
			// add physical page too
			addFirst(mainParentPage, deepness);
		}
		
		// proceed with start page
		addLast(startPage, deepness);
		doRecursive(startPage, deepness);
		return structure;
	}
	/**
	 * Fügt den link oder die seite ans Ende der Liste hinzu.
	 */
	private void addLast(Object multiLinkOrPage, int deepness) throws RQLException {
		structure.add(new ObjectPageStructureElement(deepness, multiLinkOrPage));
	}
	/**
	 * Fügt den link oder die seite an den Anfang der Liste hinzu.
	 */
	private void addFirst(Object multiLinkOrPage, int deepness) throws RQLException {
		structure.add(0, new ObjectPageStructureElement(deepness, multiLinkOrPage));
	}
	/**
	 * Startet das sammeln der childs und links.
	 */
	private int doRecursive(Page page, int deepness) throws RQLException {

		java.util.List multiLinks = page.getMultiLinksWithoutShadowedOnesSorted(ignoreTmpltElemNameSuffix, false);
		// end condition - no childs at all
		if (multiLinks.isEmpty()) {
			return deepness;
		}
		for (Iterator iter = multiLinks.iterator(); iter.hasNext();) {
			MultiLink multiLink = (MultiLink) iter.next();
			PageArrayList childs = multiLink.getChildPages();
			if (childs.isEmpty()) {
				// ignore empty multi links (do not add them)
				continue;
			}
			if (childs.selectAllPagesNotContaining(isPhysicalPageTmpltElemName).isEmpty()) {
				// ignore all multi links with only physical pages behind
				continue;
			}
			// collect link, because it has childs
			addLast(multiLink, deepness);
			// try on every child
			deepness -= 1;
			for (int i = 0; i < childs.size(); i++) {
				Page child = (Page) childs.get(i);
				if (child.contains(isPhysicalPageTmpltElemName)) {
					// stop recursion because of next physical page reached
					continue;
				}
				// block child found
				addLast(child, deepness);
				deepness = doRecursive(child, deepness);
			}
			// restore for next multilink
			deepness += 1;
		}
		return deepness;
	}
	/**
	 * Liefert die Seitenstruktur als geordnete Liste. 
	 * Das erste Element ist immer die Startseite selbst.
	 */
	public java.util.List getStructure() {
		return structure;
	}
	/**
	 * Fasst childs in media files und text table rows zusammen. Ist optional.
	 * 
	 * @param	groupTempltElemNameList	a list of multi link names for which child pages should be grouped, e.g. text_list,term_list
	 * @param	delimiter the delimiter for this list, e.g. ,
	 * @param	seperateFilter	pages for which this filter returns true stay always outside the group, e.g. a ListPageFilter with all draft pages of author
	 */
	public java.util.List compact(String groupTempltElemNameList, String delimiter, PageFilter seperateFilter) throws RQLException {

		// build a new one		
		java.util.List compactedStructure = new ArrayList(structure.size());

		// for all elements do
		for (int i = 0; i < structure.size(); i++) {
			ObjectPageStructureElement element = (ObjectPageStructureElement) structure.get(i);
			// add page unchanged
			if (element.isPage()) {
				compactedStructure.add(element);
			}
			if (element.isMultiLink()) {
				// is multi link
				MultiLink link = element.getMultiLink();
				compactedStructure.add(element);
				String name = link.getName();
				if (StringHelper.contains(groupTempltElemNameList, delimiter, name, true)) {
					// needs to be grouped
					// position to first link child
					int g = i + 1;
					element = (ObjectPageStructureElement) structure.get(g); // cannot be the end because empty links not allowed
					// for all childs of this multi link
					int childDeepness = element.getDeepness();
					GroupPageStructureElement groupElement = new GroupPageStructureElement(childDeepness);;
					while (g < structure.size() && element.getDeepness() == childDeepness) {
						// this is a child; increase outer iteration
						i++;
						// check element type
						if (element.isPage()) {
							Page child = element.getPage();
							// leave all pages for the given filter allone
							if (seperateFilter.check(child)) {
								// add group element if needed
								if (groupElement.isNotEmpty()) {
									compactedStructure.add(groupElement);
									groupElement = new GroupPageStructureElement(childDeepness);
								}
								compactedStructure.add(element);
							} else {
								// increment group
								groupElement.incrementSize();
							}
						} else {
							// another element than a page element at same deepnes should not be
							throw new RQLException("Wrong page structure for page " + startPage.getHeadlineAndId() + " detected while compacting the page structure. Only pages as childs of MultiLinks "+groupTempltElemNameList+" expected.");
						}
						// try next element
						g++;
						if (g < structure.size()) {
							element = (ObjectPageStructureElement) structure.get(g);
						}
					}
					// add last group element if needed
					if (groupElement.isNotEmpty()) {
						compactedStructure.add(groupElement);
					}
				}
			}
		}
		// replace with compacted one and return
		structure = compactedStructure;
		return structure;
	}
	/**
	 * Liefert eine geordnete Liste mit den deepness information für die vollständige Strutur.
	 */
	public int[] getStructureDeepnesses() {
		int size = structure.size();
		int[] result = new int[structure.size()];
		for (int i = 0; i < size; i++) {
			PageStructureElement element = (PageStructureElement) structure.get(i);
			result[i] = element.getDeepness();
		}
		return result;
	}
}
