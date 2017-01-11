package com.hlcl.rql.hip.as;

import java.net.URLEncoder;
import java.util.*;

import com.hlcl.rql.as.*;

/**
 * @author lejafr
 *
 * This class encapsulates results from PhysicalPageTextFinder; glue the physical page to the search texts found on it. 
 */
public class TextFinderResult {
	private Set found;
	private PhysicalPage physicalPage;

	/**
	 * Construct a text finder result combining the page together with the search results.
	 */
	public TextFinderResult(PhysicalPage physicalPage, Set found) {
		this.physicalPage = physicalPage;
		this.found = found;
	}

	/**
	 * Liefert die Page zu den Suchbegriffen (aus der physical page).
	 */
	public Page getFoundPage() {
		return physicalPage.getPage();
	}

	/**
	 * Liefert die GUID der Page zu den Suchbegriffen (aus der physical page).
	 */
	public String getFoundPageGuid() {
		return getFoundPage().getPageGuid();
	}

	/**
	 * Liefert die PhysicalPage zu den Suchbegriffen.
	 */
	public PhysicalPage getFoundPhysicalPage() {
		return physicalPage;
	}

	/**
	 * Liefert die gefundenen Suchbegriffe in der PhysicalPage als sortierte Liste.
	 */
	public java.util.List getFoundTexts() {
		java.util.List sorted = new ArrayList(found);
		Collections.sort(sorted);
		return sorted;
	}

	/**
	 * Liefert die gefundenen Suchbegriffe in der PhysicalPage als String, getrennt durch den gegebenen String.
	 */
	public String getFoundTexts(String delimiter) {
		return StringHelper.toString(getFoundTexts(), delimiter);
	}

	/**
	 * Liefert die gefundenen Suchbegriffe in der PhysicalPage als URL Parameter (tlws. encoded), getrennt durch den gegebenen String.
	 */
	public String getFoundTextsAsUrlParm(String delimiter) {
		java.util.List foundTexts = getFoundTexts();
		String result = "";
		for (Iterator iter = foundTexts.iterator(); iter.hasNext();) {
			String found = (String) iter.next();
			// add html encoded first
			result += URLEncoder.encode(StringHelper.encodeHtml(found)) + ",";
			// add as found too
			result += URLEncoder.encode(found) + ",";
		}
		// remove last delimiter
		result = result.substring(0, result.length()-1);
		return result;
	}

	/**
	 * Liefert die URL für die Seitenvorschau. Im Status draft funktioniert page preview nicht. 
	 */
	public String getPagePreviewUrl() throws RQLException {
		return getFoundPage().getPagePreviewUrl();
	}
}
