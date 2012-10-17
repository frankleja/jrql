package com.hlcl.rql.hip.as;

import java.util.Set;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StringHelper;
import com.hlcl.rql.util.as.PageAction;
import com.hlcl.rql.util.as.PageListener;

/**
 * @author lejafr
 * 
 *         This class checks, if the invoked page is inside or outside the given content areas. If outside, a log record is written.
 */
public class CheckContentAreasPageAction extends PageAction {

	private String expectedContentAreas; // separated by ,
	private String[] skipTemplateNameSuffixes;
	private PageListener logger;
	private String contentAreasTextTempltElemName;
	private String delimiter;

	/**
	 * CheckContentAreaPageAction constructor
	 */
	public CheckContentAreasPageAction(String expectedContentAreas, String delimiter, String[] skipTemplateNameSuffixes,
			PageListener logger, String contentAreasTextTempltElemName) throws RQLException {
		super();

		this.expectedContentAreas = expectedContentAreas;
		this.delimiter = delimiter;
		this.skipTemplateNameSuffixes = skipTemplateNameSuffixes;
		this.logger = logger;
		this.contentAreasTextTempltElemName = contentAreasTextTempltElemName;
	}

	/**
	 * Calculates the given page's not expected content areas.
	 * 
	 * @return the set of unexpected content areas.
	 */
	public Set<String> getUnexpectedContentAreas(Page page) throws RQLException {
		String currentContentAreas = page.getTextValue(contentAreasTextTempltElemName);
		// separated by ;
		currentContentAreas = currentContentAreas.replace(';', delimiter.charAt(0));
		return StringHelper.difference(currentContentAreas, expectedContentAreas, delimiter);
	}

	/**
	 * Checks the content areas of the given page against the expected content areas. Log the page as info, if it is used in a not
	 * expected content area.
	 * 
	 * @see com.hlcl.rql.as.PageAction#invoke(com.hlcl.rql.as.Page)
	 */
	public void invoke(Page page) throws RQLException {
		// skip all *_fragment pages
		if (!StringHelper.endsWithOneOf(page.getTemplateName(), skipTemplateNameSuffixes)) {
			Set<String> unexpectedContentAreas = getUnexpectedContentAreas(page);
			if (!unexpectedContentAreas.isEmpty()) {
				logger.update(page);
			}
			// do not write out skipped fragment pages
		}
	}
}
