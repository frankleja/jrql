/*
 * Created on Sep 6, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.hlcl.rql.hip.as;

import java.util.*;

import com.hlcl.rql.as.*;
import com.hlcl.rql.util.as.*;

/**
 * @author lejafr
 *
 * This class is the model for the check workflow pages JSP.
 */
public class WorkflowCheckModel {
	// childed items of selected user
	private Map items;
	// childed items of selected user
	private Map allItemsCache;
	// suffix for note element when rejecting a page
	String noteNameSuffix;
	// all pages which needs the check
	private PageArrayList pages;
	// actual selected user 
	private User selectedUser;
	private String suffixesSeparator;
	// needed filter criteria
	private String templateNameSuffixes;
	// a set of users (by name) waiting for the check;
	private java.util.List users;

	/**
	 * Creates a new workflow check model.
	 */
	public WorkflowCheckModel(PageArrayList pages, String templateNameSuffixes, String suffixesSeparator, String noteNameSuffix) throws RQLException {
		super();
		// initialise
		this.pages = pages;
		this.templateNameSuffixes = templateNameSuffixes;
		this.suffixesSeparator = suffixesSeparator;
		this.noteNameSuffix = noteNameSuffix;
		// automatically select first user and adjust model accordingly
		buildUsers();
		selectUser();
	}
	/**
	 * Adds an item to the list. Update the set of users as well.
	 */
	private WorkflowCheckItem addItem(Page page) throws RQLException {
		// add as child item
		WorkflowCheckItem item = new WorkflowCheckItem(this, page);
		items.put(page.getPageId(), item);
		
		// remember 
		rememberItem(item);
		return item;
	}
	/**
	 * Remember all items for later confirmation/rejection.
	 */
	void rememberItem(WorkflowCheckItem item) throws RQLException {
		allItemsCache.put(item.getId(), item);
	}
	/** 
	 * Return true, if there are pages left for check.
	 */
	public boolean areTherePagesToCheck() {
		return !pages.isEmpty();
	}
	/**
	 * Build a childed structure for the pages to check.
	 */
	private void buildItemsForSelectedUser() throws RQLException {
		// initialize used collections
		items = new HashMap();
		allItemsCache = new HashMap();
		
		// filter pages 
		PageArrayList userPages = pages.select(new UserPageFilter(getSelectedUserName()));
		PageArrayList userMainPages = userPages.select(new TemplateNameSuffixesPageFilter(templateNameSuffixes, suffixesSeparator)); 

		// convert all main pages to check items
		for (int i = 0; i < userMainPages.size(); i++) {
			Page aPg = (Page) userMainPages.get(i);
			addItem(aPg);
			userPages.remove(aPg);
		}

		// try to child all remaining pages
		PageArrayList clonedList = (PageArrayList) userPages.clone();
		for (int i = 0; i < clonedList.size(); i++) {
			Page aPg = (Page) clonedList.get(i);
			Page parentOrNull = null;
			try {
				parentOrNull = aPg.getMainLinkParentPage();
			} catch (UnlinkedPageException upe) {
				// ignore
			} catch (MissingMainLinkException mmle) {
				// ignore
			}
			// add aPg as child
			if (parentOrNull != null) {
				WorkflowCheckItem itemOrNull = getItem(parentOrNull.getPageId());
				if (itemOrNull != null) {
					itemOrNull.addChild(aPg);
					userPages.remove(aPg);
				}
			}
		}

		// add all now remaining pages 
		for (int i = 0; i < userPages.size(); i++) {
			Page aPg = (Page) userPages.get(i);
			addItem(aPg);
		}
	}
	/**
	 * Build the set of user names who submitted pages for the check.
	 */
	private void buildUsers() throws RQLException {
		users = new ArrayList();
		for (int i = 0; i < pages.size(); i++) {
			Page aPg = (Page) pages.get(i);
			User u = aPg.getCreatedByUser();
			if (!users.contains(u)) {
				users.add(u);
			}
		}
	}
	/**
	 * Confirm the page for the given page id.
	 * The list of items and pages are not reduced!
	 */
	public void confirm(String pageId) throws RQLException {

		getRememberedItem(pageId).confirm();
	}
	/**
	 * Returns the user for the given name.
	 */
	private User findUserByName(String userName) throws RQLException {
		Iterator iter = users.iterator();
		while (iter.hasNext()) {
			User u = (User) iter.next();
			if (u.getName().equals(userName)) {
				return u;
			}
		}
		throw new ElementNotFoundException("The user with name " + userName + "could not be found in the list of users to check.");
	}
	/**
	 * Returns the main workflow check item (with childs) for the given page id.
	 */
	private WorkflowCheckItem getItem(String pageId) throws RQLException {
		return (WorkflowCheckItem) items.get(pageId);
	}
	/**
	 * Returns the workflow check item for the given page id to confirm/reject it.
	 */
	private WorkflowCheckItem getRememberedItem(String pageId) throws RQLException {
		return (WorkflowCheckItem) allItemsCache.get(pageId);
	}
	/**
	 * Returns an iterator over the list of workflow check items.
	 */
	public Iterator getItemsIterator() {
		// sort list of items by headline
		java.util.List list = new ArrayList(items.values());
		java.util.Collections.sort(list);
		return list.iterator();
	}
	/**
	 * Returns the actual selected user.
	 */
	public User getSelectedUser() {
		return selectedUser;
	}
	/**
	 * Returns the name of the actual selected user.
	 */
	public String getSelectedUserName() throws RQLException {
		return selectedUser.getName();
	}
	/**
	 * Returns the iterator for the set of all users, who send pages to check.
	 */
	public Iterator getUsersIterator() {
		return users.iterator();
	}
	/**
	 * Reject the page for the given page id and set the comment at the page.
	 * The list of items and pages are not reduced!
	 */
	public void reject(String pageId, String comment) throws RQLException {

		getRememberedItem(pageId).reject(comment);
	}
	/**
	 * Sets this model accordingly to the first found user, if possible.
	 * Otherwise ignore this request.
	 */
	private void selectUser() throws RQLException {
		if (users.size() != 0) {
			selectedUser = (User) users.iterator().next();
			// update items for this user
			buildItemsForSelectedUser();
		}
	}
	/**
	 * Sets this model accordingly to the given user.
	 * Otherwise ignore this request.
	 */
	public void selectUser(String userName) throws RQLException {
		// remember
		this.selectedUser = findUserByName(userName);

		// update items for this user
		buildItemsForSelectedUser();
	}
	/**
	 * Returns the workflow check item from within items with the given pages id.
	 * Returns null, if the workflow check item could not be found.
	private WorkflowCheckItem findItemByPageId(String pageId) throws RQLException {
		for (int i = 0; i < items.size(); i++) {
			WorkflowCheckItem item = (WorkflowCheckItem) items.get(i);
			if (item.getId().equals(pageId)) {
				return item;
			}
		}
		return null;
	}
	 */
}
