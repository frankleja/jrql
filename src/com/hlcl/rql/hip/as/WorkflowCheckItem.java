/*
 * Created on Sep 6, 2005
 *
*/
package com.hlcl.rql.hip.as;

import java.util.ArrayList;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * This class represents one page to check.
 *  
 * @see WorkflowCheckModel
 */
public class WorkflowCheckItem implements Comparable {
	private java.util.List childs;
	private WorkflowCheckModel model;
	private Page page;

	/** 
	 * Creates an item, wrapping the given page. 
	 */
	public WorkflowCheckItem(WorkflowCheckModel model, Page page) {
		super();
		this.model = model;
		this.page = page;
	}
	/** 
	 * Add given page as new workflow check item child. 
	 */
	public WorkflowCheckItem addChild(Page childPage) throws RQLException {
		//lazy initialisation
		if (childs == null) {
			childs = new ArrayList();
		}
		// adding
		WorkflowCheckItem item = new WorkflowCheckItem(model, childPage);
		childs.add(item);
		
		// remember child too for confirm/reject
		model.rememberItem(item);
		return item;
	}
	/** 
	 * Implement to sort by page headline. 
	 */
	public int compareTo(Object o) {
		WorkflowCheckItem second = (WorkflowCheckItem) o;
		int result = 0;
		try {
			result = this.getHeadlineAndId().compareToIgnoreCase(second.getHeadlineAndId());
		} catch (RQLException e) {
			// ignore
		}
		return result;
	}
	/** 
	 * Confirm the page
	 */
	public void confirm() throws RQLException {
		page.release();
	}
	/** 
	 * Return true, if the pages are equal.
	 */
	public boolean equals(Object obj) {
		return page.equals(obj);
	}
	/** 
	 * Return the childs (a list of workflow check items) or null, if no items exists.
	 */
	public java.util.List getChilds() {
		return childs;
	}
	/** 
	 * Return the name of the user who created the page.
	 */
	public String getCreatedByUserName() throws RQLException {
		return page.getCreatedByUserName();
	}
	/** 
	 * Return the headline of the page.
	 */
	public String getHeadline() throws RQLException {
		return page.getHeadline();
	}
	/** 
	 * Return the headline and the ID of the page.
	 */
	public String getHeadlineAndId() throws RQLException {
		return page.getHeadlineAndId();
	}
	/** 
	 * Return the page ID as the ID of this check item too.
	 */
	public String getId() throws RQLException {
		return page.getPageId();
	}
	/** 
	 * Return the name of administrative note where to save the rejection comment.
	 */
	private String getNoteName() throws RQLException {
		return page.getTemplateName() + model.noteNameSuffix;
	}
	/** 
	 * Return the value of the administrative note where the rejection comment is saved.
	 */
	public String getNoteValue() throws RQLException {
		return page.getNoteValue(getNoteName());
	}
	/** 
	 * Return the page GUID of the page.
	 */
	public String getPageGuid() throws RQLException {
		return page.getPageGuid();
	}
	/** 
	 * Return the name of the template of the page.
	 */
	public String getTemplateName() throws RQLException {
		return page.getTemplateName();
	}
	/** 
	 * Reject the page with the given comment.
	 */
	public void reject(String comment) throws RQLException {
		page.reject(getNoteName(), comment);
	}
}
