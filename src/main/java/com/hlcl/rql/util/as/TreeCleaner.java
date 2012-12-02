package com.hlcl.rql.util.as;

import java.util.Iterator;

import com.hlcl.rql.as.*;

/**
 * @author lejafr
 *
 * This class frees up a tree part from pages which are multi linked.<p>
 * What about page linked twice into this tree part?<p>
 * This class will remove all links of such a page except the last one! So, at the end, this page will be still connected once within this tree part. Other connections into this tree part are lost! 
 */
public class TreeCleaner {

	private boolean infoModeOnly;
	private ConnectionListener pageAboutToDisconnectListener;
	private PageListener treePageFoundListener;

	/**
	 * Construct a cleaner.
	 */
	public TreeCleaner(boolean infoModeOnly) {
		super();
		this.infoModeOnly = infoModeOnly;
	}

	/**
	 * Startet Untersuchung und das ablinken mehrfach verlinkter Seiten von diesem Tree part.
	 */
	private void doRecursive(Page page) throws RQLException {

		// for all multi links of the given page in correct order do
		java.util.List<MultiLink> multiLinks = page.getMultiLinksSorted(false); // ignore the referenced multi links - they do not have childs!
		for (MultiLink multiLink : multiLinks) {

			// for all childs of this multi link do
			PageArrayList childs = multiLink.getChildPages();
			for (Iterator iter = childs.iterator(); iter.hasNext();) {
				Page child = (Page) iter.next();

				int howOftenConnected = child.howOftenIsThisPageConnected();
				// decide depending how many parent this child have
				if (howOftenConnected > 1) {
					// inform dependent that this child is about to disconnect
					pageAboutToDisconnectEvent(child, multiLink);
					// child is used somewhere else too
					if (isDisconnectingAllowed()) {
						multiLink.disconnectChild(child);
					}
				} else {
					// child is used only within three
					treePageFoundEvent(child);
					// investigate further
					doRecursive(child);
				}
			}
		}
	}

	/**
	 * Liefert true, wenn der Cleaner die Seiten wirklich von diesem tree part abhängen soll. 
	 */
	public boolean isDisconnectingAllowed() {
		return !infoModeOnly;
	}

	/**
	 * Informiert den Listener über eine abgehängte Seite. 
	 */
	private void pageAboutToDisconnectEvent(Page page, MultiLink multiLink) throws RQLException {
		if (pageAboutToDisconnectListener != null) {
			pageAboutToDisconnectListener.update(page, multiLink);
		}
	}

	/**
	 * Setzt die beiden Dependents (according to MVC pattern), um ein logging und eine Fortschrittsanzeige zu ermöglichen. 
	 */
	public void setListeners(ConnectionListener pageAboutToDisconnectListener, PageListener treePageFoundListener) {
		this.pageAboutToDisconnectListener = pageAboutToDisconnectListener;
		this.treePageFoundListener = treePageFoundListener;
	}

	/**
	 * Startet das ablinken mehrfach verlinkter Seiten ab der gegebenen Startseite rekursiv nach unten.
	 */
	public void startFromPage(Page startPage) throws RQLException {
		// echo the tree start page
		treePageFoundEvent(startPage);
		
		// investigate deeper and deeper
		doRecursive(startPage);
	}

	/**
	 * Informiert den Listener über eine gefundene Baumseite. 
	 */
	private void treePageFoundEvent(Page page) throws RQLException {
		if (treePageFoundListener != null) {
			treePageFoundListener.update(page);
		}
	}
}
