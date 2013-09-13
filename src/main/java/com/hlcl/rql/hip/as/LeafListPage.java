package com.hlcl.rql.hip.as;

import java.util.Iterator;

import com.hlcl.rql.as.Container;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.Template;
import com.hlcl.rql.util.as.PageArrayList;

/**
 * @author lejafr
 * 
 * This class represents a hip leaf list page.
 */
public class LeafListPage extends NavigationPage {

	private Container blocksBottomCtr;

	public LeafListPage(Page page) {
		super(page);
	}

	/**
	 * Moves the given leaf list child page from the content_pages_list to a recycling table block at blocks_bottom. Returns true, if
	 * page is moved.
	 * <p>
	 * Didn't link the given contentPagesChildPage to the recycling block, if it's not a child of this page's content_pages_list
	 * element.
	 * <p>
	 * Creates a new recycling block automatically if it doesn't exists already.
	 */
	public boolean deactivate(Page contentPagesListChildPage) throws RQLException {
		if (isContentPagesListChild(contentPagesListChildPage)) {
			// connect first
			RecyclingTableBlock tableBlock = getOrCreateBottomRecyclingTableBlock();
			tableBlock.add(contentPagesListChildPage);
			// disconnect 2nd, safety
			super.disconnectContentPagesListChild(contentPagesListChildPage);
			// moved
			return true;
		}
		// not moved
		return false;
	}

	/**
	 * Try to move all given leaf list child pages from the content_pages_list to a recycling table block at blocks_bottom. Returns
	 * number of moved pages.
	 * <p>
	 * Didn't link the given contentPagesChildPage to the recycling block, if it's not a child of this page's content_pages_list
	 * element.
	 * <p>
	 * Creates a new recycling block automatically if it doesn't exists already.
	 */
	public int deactivate(PageArrayList contentPagesListChildren) throws RQLException {
		int result = 0;
		for (Iterator iterator = contentPagesListChildren.iterator(); iterator.hasNext();) {
			Page page = (Page) iterator.next();
			if (deactivate(page)) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Returns the first recycling table block from the blocks_bottom container or null, if it doesn't have a recycling table block
	 * page at all.
	 */
	public RecyclingTableBlock getBottomRecyclingTableBlock() throws RQLException {
		Container bottomCtr = getBottomBlocksContainer();
		PageArrayList children = bottomCtr.getChildPagesForTemplate(getParameter("recyclingBlockTmpltName"));
		// no one found
		if (children.size() == 0) {
			return null;
		}
		// wrap
		return new RecyclingTableBlock(children.first());
	}

	/**
	 * Returns the first link table block from the blocks_bottom container or null, if it doesn't have a link table block page at all.
	 */
	public LinkTableBlock getBottomLinkTableBlock() throws RQLException {
		Container bottomCtr = getBottomBlocksContainer();
		PageArrayList children = bottomCtr.getChildPagesForTemplate(getParameter("linkTableBlockTmpltName"));
		// no one found
		if (children.size() == 0) {
			return null;
		}
		// wrap
		return new LinkTableBlock(children.first());
	}

	/**
	 * Returns the number of html view table blocks linked at the blocks_bottom container.
	 */
	public int getBottomHtmlViewTableBlocksSize() throws RQLException {
		Container bottomCtr = getBottomBlocksContainer();
		PageArrayList children = bottomCtr.getChildPagesForTemplate(getParameter("htmlViewTableBlockTmpltName"));
		return children.size();
	}

	/**
	 * Returns the first html view table block from the blocks_bottom container or null, if it doesn't have a html view table block page at all.
	 */
	public HtmlViewTableBlock getBottomHtmlViewTableBlock() throws RQLException {
		Container bottomCtr = getBottomBlocksContainer();
		PageArrayList children = bottomCtr.getChildPagesForTemplate(getParameter("htmlViewTableBlockTmpltName"));
		// no one found
		if (children.size() == 0) {
			return null;
		}
		// wrap
		return new HtmlViewTableBlock(children.first());
	}

	/**
	 * Returns the recycling table block from the blocks_bottom container or creates a new one, if this leaf list page didn't have one
	 * before.
	 */
	public RecyclingTableBlock getOrCreateBottomRecyclingTableBlock() throws RQLException {
		RecyclingTableBlock blockPg = getBottomRecyclingTableBlock();
		// no one, create new
		if (blockPg == null) {
			blockPg = createBottomRecyclingTableBlock();
		}
		return blockPg;
	}

	/**
	 * Creates a new recycling table block on the blocks_bottom container of this leaf list page.
	 * 
	 * @throws RQLException
	 */
	public RecyclingTableBlock createBottomRecyclingTableBlock() throws RQLException {
		Container ctr = getBottomBlocksContainer();
		String templateName = getParameter("recyclingBlockTmpltName");
		Template template = getTemplateByName(getParameter("contentTmpltFldrName"), templateName);
		return new RecyclingTableBlock(ctr.createAndConnectPage(template, getHeadline() + " " + templateName, true));
	}

	/**
	 * Creates a new link table block on the blocks_bottom container of this leaf list page.
	 * 
	 * @throws RQLException
	 */
	public LinkTableBlock createBottomLinkTableBlock() throws RQLException {
		Container ctr = getBottomBlocksContainer();
		String templateName = getParameter("linkTableBlockTmpltName");
		Template template = getTemplateByName(getParameter("contentTmpltFldrName"), templateName);
		return new LinkTableBlock(ctr.createAndConnectPage(template, getHeadline() + " " + templateName, true));
	}

	/**
	 * Returns the bottom blocks container from this leaf list page.
	 */
	public Container getBottomBlocksContainer() throws RQLException {
		if (blocksBottomCtr == null) {
			blocksBottomCtr = getPage().getContainer(getParameter("blocksBottomTmpltElemName"));
		}
		return blocksBottomCtr;
	}

	/**
	 * Returns the first link table block from the blocks_bottom container or creates a new one, if this leaf list page didn't have one
	 * before.
	 */
	public LinkTableBlock getOrCreateBottomLinkTableBlock() throws RQLException {
		LinkTableBlock blockPg = getBottomLinkTableBlock();
		// no one, create new
		if (blockPg == null) {
			blockPg = createBottomLinkTableBlock();
		}
		return blockPg;
	}
}
