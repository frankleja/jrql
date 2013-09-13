package com.hlcl.rql.hip.as;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StringHelper;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.ProjectPage;

/**
 * @author lejafr
 * 
 * This class represents a physical page in the HIP project to deliver block childs and page childs.
 */
public class PhysicalPage extends ProjectPage {

	private List allNonPhysicalChilds;
	private List allPhysicalChilds;
	private String isPhysicalPageTmpltElemName;
	private String shadowElementsNameSuffix;
	private String[] visibleHeadlineTemplateNameSuffixes;

	/**
	 * Construct a physical page wrapping the given general page.
	 */
	public PhysicalPage(Page page) throws RQLException {
		super(page);
		// initialize
		isPhysicalPageTmpltElemName = getParameter("isPhysicalPageTmpltElemName");
		shadowElementsNameSuffix = getParameter("shadowElementsNameSuffix");
	}

	/**
	 * Construct a physical page wrapping the given general page. Reuse template element names.
	 */
	private PhysicalPage(Page page, String isPhysicalPageTmpltElemName, String shadowElementsNameSuffix) throws RQLException {
		super(page);
		// initialize
		this.isPhysicalPageTmpltElemName = isPhysicalPageTmpltElemName;
		this.shadowElementsNameSuffix = shadowElementsNameSuffix;
	}

	/**
	 * Liefert alle in dieser physischen Seite gefundenen Begriffe.
	 * <p>
	 * Das zurückgegebene Set ist leer, wenn keiner der Begriffe auf dieser Seite und all ihren block childs vorkommt.
	 */
	public Set collectContainedText(String findList, String delimiter, boolean caseSensitive) throws RQLException {
		// start page itself
		Set result = getPage().collectContainedText(findList, delimiter, hasVisibleHeadline(), caseSensitive);
		// all block childs too
		PageArrayList childs = getAllNonPhysicalChildPages();
		for (int i = 0; i < childs.size(); i++) {
			Page child = (Page) childs.get(i);
			result.addAll(child.collectContainedText(findList, delimiter, hasVisibleHeadline(child), caseSensitive));
		}
		return result;
	}

	/**
	 * Collect and classify all childs.
	 */
	private void doRecursive(Page page) throws RQLException {

		PageArrayList childs = page.getChildPages();
		// for all childs do
		for (int i = 0; i < childs.size(); i++) {
			Page child = (Page) childs.get(i);
			// classify physical or not
			if (child.contains(isPhysicalPageTmpltElemName)) {
				// add only
				allPhysicalChilds.add(child);
			} else {
				// add and try on child
				allNonPhysicalChilds.add(child);
				doRecursive(child);
			}
		}
	}

	/**
	 * Collect and classify only physical child pagess.
	 */
	private void doRecursiveOnlyPhysicalPages(Page page) throws RQLException {

		PageArrayList childs = page.getChildPagesIgnoreShadowedMultilinks(shadowElementsNameSuffix);
		// for all childs do
		for (int i = 0; i < childs.size(); i++) {
			Page child = (Page) childs.get(i);
			// classify physical or not
			if (child.contains(isPhysicalPageTmpltElemName)) {
				// add only
				allPhysicalChilds.add(child);
			} else {
				doRecursiveOnlyPhysicalPages(child);
			}
		}
	}

	/**
	 * Start collecting and classifying all childs.
	 */
	private void gatherChilds(boolean collectNonPhysicalChildPagesAsWell) throws RQLException {
		if (collectNonPhysicalChildPagesAsWell) {
			allNonPhysicalChilds = new ArrayList();
			allPhysicalChilds = new ArrayList();
			doRecursive(getPage());
		} else {
			allPhysicalChilds = new ArrayList();
			doRecursiveOnlyPhysicalPages(getPage());
		}
	}

	/**
	 * Returns all child pages building this physical page (recursively down). All block, row and section childs are gathered.
	 */
	public PageArrayList getAllNonPhysicalChildPages() throws RQLException {
		if (allPhysicalChilds == null) {
			gatherChilds(true);
		}
		return new PageArrayList(allNonPhysicalChilds);
	}

	/**
	 * Returns all physical pages linked from this physical page.
	 * 
	 * @param collectNonPhysicalChildPagesAsWell
	 *            =true, collects all non physical pages as well =false, gets only physical pages - optimized
	 */
	public PageArrayList getAllPhysicalChildPages(boolean collectNonPhysicalChildPagesAsWell) throws RQLException {
		if (allPhysicalChilds == null) {
			gatherChilds(collectNonPhysicalChildPagesAsWell);
		}
		return new PageArrayList(allPhysicalChilds);
	}

	/**
	 * Returns true, if the encapsulated page is a physical page in HIP.
	 * <p>
	 * Returns true, if the page has an element isPhysicalPage.
	 */
	public boolean isPhysicalPage() throws RQLException {
		return getPage().contains(isPhysicalPageTmpltElemName);
	}

	/**
	 * Returns the name suffixes for all templates having a visible reddot page headline.
	 */
	public String[] getVisibleHeadlineTemplateNameSuffixes() throws RQLException {
		if (visibleHeadlineTemplateNameSuffixes == null) {
			visibleHeadlineTemplateNameSuffixes = StringHelper.split(getParameter("visibleHeadlineTemplateNameSuffixes"), ",");
		}
		return visibleHeadlineTemplateNameSuffixes;
	}

	/**
	 * Returns true, if the reddot page headline of the wrapped page is visible.
	 */
	public boolean hasVisibleHeadline() throws RQLException {
		return hasVisibleHeadline(getPage());
	}

	/**
	 * Returns true, if the reddot page headline of the given page is visible/searchable.
	 */
	public boolean hasVisibleHeadline(Page page) throws RQLException {
		return StringHelper.endsWithOneOf(page.getTemplateName(), getVisibleHeadlineTemplateNameSuffixes());
	}

	/**
	 * Returns a new physical page for the given page. Reuse the parameters.
	 */
	public PhysicalPage morphInto(Page page) throws RQLException {
		return new PhysicalPage(page, isPhysicalPageTmpltElemName, shadowElementsNameSuffix);
	}

	/**
	 * Liefert eine Liste mit allen über MainLink verknüpften Seiten (nur physical pages, keine Blockseiten, aber inkl. nodes!).
	 * <p>
	 * Erste Seite ist eine der gegebenen Startseiten und letzte ist diese Seite selbst.
	 * <p>
	 * Falls die gegebenen Startseite nicht im Pfad enthalten ist, wird dieser auch nicht gekürzt und endet damit an der Projektstartseite.
	 */
	public PageArrayList getMainLinkPath(String startPageIds, String separator) throws RQLException {
		MainLinkPagePathBuilder pathBuilder = new MainLinkPagePathBuilder(getPage());
		return pathBuilder.getMainLinkPath(startPageIds, separator, false, true);
	}

	/**
	 * Liefert eine Liste mit allen über MainLink verknüpften Seiten (nur physical pages, keine Blockseiten, aber inkl. nodes!).
	 * <p>
	 * Erste Seite ist die Projektstartseite und letzte ist diese Seite selbst.
	 */
	public PageArrayList getMainLinkPath() throws RQLException {
		MainLinkPagePathBuilder pathBuilder = new MainLinkPagePathBuilder(getPage());
		return pathBuilder.getMainLinkPath(false, true);
	}
	/**
	 * Liefert eine Liste mit allen über MainLink verknüpften Seiten (nur physical pages, Blockseiten optional, nodes optional).
	 * <p>
	 * Erste Seite ist eine der gegebenen Startseiten und letzte ist diese Seite selbst.
	 * <p>
	 * Falls die gegebenen Startseite nicht im Pfad enthalten ist, wird dieser auch nicht gekürzt und endet damit an der Projektstartseite.
	 */
	public PageArrayList getMainLinkPath(String startPageIds, String separator, boolean includeBlocks, boolean includeNodes) throws RQLException {
		MainLinkPagePathBuilder pathBuilder = new MainLinkPagePathBuilder(getPage());
		return pathBuilder.getMainLinkPath(startPageIds, separator, includeBlocks, includeNodes);
	}

	/**
	 * Zwei Seitenobjekte werden als identisch interpretiert, falls beide die gleiche GUID haben.
	 * 
	 * @param obj
	 *            the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
	 * @see java.lang.Boolean#hashCode()
	 * @see java.util.Hashtable
	 */
	public boolean equals(Object obj) {
		PhysicalPage second = (PhysicalPage) obj;
		return this.getPageGuid().equals(second.getPageGuid());
	}

	/**
	 * Returns a hash code value for the object. This method is supported for the benefit of hashtables such as those provided by
	 * <code>java.util.Hashtable</code>.
	 * <p>
	 * The general contract of <code>hashCode</code> is:
	 * <ul>
	 * <li>Whenever it is invoked on the same object more than once during an execution of a Java application, the <tt>hashCode</tt>
	 * method must consistently return the same integer, provided no information used in <tt>equals</tt> comparisons on the object is
	 * modified. This integer need not remain consistent from one execution of an application to another execution of the same application.
	 * <li>If two objects are equal according to the <tt>equals(Object)</tt> method, then calling the <code>hashCode</code> method on
	 * each of the two objects must produce the same integer result.
	 * <li>It is <em>not</em> required that if two objects are unequal according to the {@link java.lang.Object#equals(java.lang.Object)}
	 * method, then calling the <tt>hashCode</tt> method on each of the two objects must produce distinct integer results. However, the
	 * programmer should be aware that producing distinct integer results for unequal objects may improve the performance of hashtables.
	 * </ul>
	 * <p>
	 * As much as is reasonably practical, the hashCode method defined by class <tt>Object</tt> does return distinct integers for distinct
	 * objects. (This is typically implemented by converting the internal address of the object into an integer, but this implementation
	 * technique is not required by the Java<font size="-2"><sup>TM</sup></font> programming language.)
	 * 
	 * @return a hash code value for this object.
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @see java.util.Hashtable
	 */
	public int hashCode() {
	
		return getPageGuid().hashCode();
	}
}
