/**
 * 
 */
package com.hlcl.rql.hip.as;

import java.util.Iterator;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.PageArrayList;

/**
 * This class represents an page based on content class enhanced list table block.
 * 
 * @author lejafr
 */
public class EnhancedListTableBlock extends NavigationPage {

	public EnhancedListTableBlock(Page page) {
		super(page);
	}

	/**
	 * Moves the given child page from the content_pages_list to the given recycling table block. Returns true, if page is moved.
	 * <p>
	 * Given page is disconnected from all other links! Afterwards its linked only to the recycling table block.
	 * <p> 
	 * Didn't link the given contentPagesChildPage to the recycling block, if it's not a child of this page's content_pages_list element.
	 */
	public boolean deactivate(Page contentPagesListChildPage, RecyclingTableBlock targetBlock) throws RQLException {
		if (isContentPagesListChild(contentPagesListChildPage)) {
			// disconnect from all other links 
			contentPagesListChildPage.disconnectFromAllMultiLinks(getContentPagesList());
			// connect first
			targetBlock.add(contentPagesListChildPage);
			// disconnect 2nd, safety
			super.disconnectContentPagesListChild(contentPagesListChildPage);
			// moved
			return true;
		}
		// not moved
		return false;
	}

	/**
	 * Try to move all given child pages from the content_pages_list to the given recycling table block. Returns number of moved pages.
	 * <p>
	 * Given pages are disconnected from all other links! Afterwards they are linked only to the recycling table block.
	 * <p>
	 * Didn't link the given contentPagesChildPage to the recycling block, if it's not a child of this page's content_pages_list element.
	 */
	public int deactivate(PageArrayList contentPagesListChildren, RecyclingTableBlock targetBlock) throws RQLException {
		int result = 0;
		for (Iterator iterator = contentPagesListChildren.iterator(); iterator.hasNext();) {
			Page page = (Page) iterator.next();
			if (deactivate(page, targetBlock)) {
				result++;
			}
		}
		return result;
	}

}
