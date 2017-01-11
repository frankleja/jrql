package com.hlcl.rql.hip.as;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;

import com.hlcl.rql.as.List;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.Template;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.ProjectPage;

/**
 * @author lejafr
 * 
 * This class represents hip navigation page (leaf * list page or enhanced list table block); encapsulates mainly operations regarding
 * content_pages_list.
 */
public class NavigationPage extends ProjectPage {

	private List contentPagesList;

	public NavigationPage(Page page) {
		super(page);
	}

	/**
	 * Creates a new content page on this page's content_pages_list with page updated = today and given responsibility.
	 * 
	 * @param headline
	 *            headline of new content page
	 */
	public ContentPage createContentPage(String headline, boolean addAtBottom, String requesterUserId, boolean isResponsibleIdAPerson,
			String responsibleId, String responsibleName, String responsibleDepartmentNumber) throws RQLException {
		// create content page
		Template template = getTemplateByName(getParameter("contentTmpltFldrName"), getParameter("contentPageTmpltName"));
		ContentPage result = new ContentPage(getContentPagesList().createAndConnectPage(template, headline, addAtBottom));
		// set responsibility
		result.setResponsibility(requesterUserId, isResponsibleIdAPerson, responsibleId, responsibleName, responsibleDepartmentNumber);
		// updated today
		result.setUpdatedToday();
		return result;
	}

	/**
	 * Checks, if the given page is a children of this leaf list content_pages_list element.
	 */
	public boolean isContentPagesListChild(Page contentPagesListChildPage) throws RQLException {
		return getContentPagesList().isChild(contentPagesListChildPage);
	}

	/**
	 * Returns all children of list element content_pages_list.
	 */
	public PageArrayList getChildren() throws RQLException {
		return getContentPagesList().getChildPages();
	}

	/**
	 * Returns all children filtered by template content_page of list element content_pages_list.
	 */
	public java.util.List<ContentPage> getContentPages() throws RQLException {
		PageArrayList children = getContentPagesList().getChildPagesForTemplate(getParameter("contentPageTmpltName"));
		java.util.List<ContentPage> result = new ArrayList<ContentPage>(children.size());
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Page page = (Page) iterator.next();
			result.add(new ContentPage(page));
		}
		return result;
	}

	/**
	 * Try to disconnect the given child page from this page's content_page_list element.
	 */
	public void disconnectContentPagesListChild(Page contentPagesListChildPage) throws RQLException {
		getContentPagesList().disconnectChild(contentPagesListChildPage);
	}

	/**
	 * Sorts all children of list element content_pages_list accordingly to the given and ordered headlines.
	 * 
	 * @return the sorted list of children
	 */
	public PageArrayList sortChildsByGivenHeadlines(SortedSet<String> orderedHeadlines) throws RQLException {
		return getContentPagesList().sortChildsByGivenHeadlines(orderedHeadlines);
	}

	/**
	 * Sorts all children of list element content_pages_list accordingly to the value of standard field text with given name.
	 */
	public PageArrayList sortChildsBytandardFieldTextAsc(String standardFieldTextTmpltElemName) throws RQLException {
		return getContentPagesList().sortChildsByStandardFieldTextAsc(standardFieldTextTmpltElemName);
	}

	/**
	 * Returns the content pages list List from this leaf list page.
	 */
	protected List getContentPagesList() throws RQLException {
		if (contentPagesList == null) {
			contentPagesList = getPage().getList(getParameter("contentPagesListTmpltElemName"));
		}
		return contentPagesList;
	}

	/**
	 * Returns the content_page from the list element content_pages_list or creates a new one, if this navigation page didn't have one before.
	 */
	public ContentPage getOrCreateContentPage(String headline, boolean addAtBottom, String requesterUserId, boolean isResponsibleIdAPerson,
			String responsibleId, String responsibleName, String responsibleDepartmentNumber) throws RQLException {
		ContentPage childOrNull = findContentPagesListChildByHeadline(headline);
		// no one, create new
		if (childOrNull == null) {
			childOrNull = createContentPage(headline, addAtBottom, requesterUserId, isResponsibleIdAPerson, responsibleId, responsibleName,
					responsibleDepartmentNumber);
		}
		return childOrNull;
	}

	/**
	 * Returns the first content page (filter by template) from the list element content_pages_list with the given headline or null, if no page found.
	 * Uses {@link #equals(Object)}.
	 */
	public ContentPage findContentPagesListChildByHeadline(String headline) throws RQLException {
		return new ContentPage(getContentPagesList().getChildPagesForTemplate(getParameter("contentPageTmpltName")).findByHeadline(headline));
	}

}
