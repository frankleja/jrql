package com.hlcl.rql.util.as;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hlcl.rql.as.MissingRightException;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.ReddotDate;
import com.hlcl.rql.as.UserGroup;

/**
 * Diese Klasse bietet Selektionen auf Listen von Seiten.
 * 
 * @author lejafr
 */
public class PageArrayList extends ArrayList implements java.util.List {
	private static final long serialVersionUID = -1289686304913077174L;

	/**
	 * Constructs an empty list.
	 */
	public PageArrayList() {
		super();
	}

	/**
	 * Constructs a list containing the elements of the specified collection, in the order they are returned by the collection's
	 * iterator. The <tt>ArrayList</tt> instance has an initial capacity of 110% the size of the specified collection.
	 */
	public PageArrayList(Collection c) {
		super(c);
	}

	/**
	 * Constructs an empty list with the specified initial capacity.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the list.
	 */
	public PageArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Returns a HashMap where key=page guid and value=page itself.
	 */
	public Map asPageGuidMap() throws RQLException {
		Map result = new HashMap(size());

		// insert all
		for (int i = 0; i < size(); i++) {
			Page p = getPage(i);
			result.put(p.getPageGuid(), p);
		}
		return result;
	}

	/**
	 * Liefert eine Liste mit allen Überschriften der Seiten dieser Liste.
	 */
	public java.util.List<String> collectHeadlines() throws RQLException {

		java.util.List<String> result = new ArrayList<String>(this.size());
		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			result.add(page.getHeadline());
		}
		return result;
	}

	/**
	 * Calls for all pages in this list {@link Page#getInfoText()} and adds the result together with delimiter to target.
	 * <p>
	 * The delimiter at the end will not be removed!
	 * 
	 * @see Page#getInfoText()
	 */
	public String collectInfoText(String delimiter) throws RQLException {
		String result = "";
		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			result += page.getInfoText() + delimiter;
		}
		return result;
	}

	/**
	 * Liefert eine Liste mit allen Templatenamen der Seiten dieser Liste.
	 */
	public java.util.List<String> collectTemplateNames() throws RQLException {

		java.util.List<String> result = new ArrayList<String>(this.size());
		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			result.add(page.getTemplateName());
		}
		return result;
	}

	/**
	 * Liefert eine Liste mit allen Überschriften und IDs der Seiten dieser Liste.
	 */
	public java.util.List<String> collectHeadlinesAndIds() throws RQLException {

		java.util.List<String> result = new ArrayList<String>(this.size());
		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			result.add(page.getHeadlineAndId());
		}
		return result;
	}

	/**
	 * Returns a Set of users who last changed the pages in this list.
	 */
	public Set collectLastChangedByUsers() throws RQLException {
		Set result = new HashSet();

		// insert all
		for (int i = 0; i < size(); i++) {
			Page p = getPage(i);
			result.add(p.getLastChangedByUser());
		}
		return result;
	}

	/**
	 * Liefert truel, falls eine Seite mit dem gegebenen Dateinamen in dieser Liste enthalten ist.
	 * <p>
	 * Prüft mit #startsWith, da in RD häufig der Extender nicht im filename auftaucht.
	 */
	public boolean containsPageWithFilename(String filename) throws RQLException {

		return findByFilename(filename) != null;
	}

	/**
	 * Released alle Seiten in dieser Liste.
	 * 
	 * @see Page#release()
	 */
	public void releaseAll() throws RQLException {
		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			page.release();
		}
	}

	/**
	 * Bestätigt alle Seiten in dieser Liste.
	 * 
	 * @see Page#submitToWorkflow()
	 */
	public void submitAllToWorkflow() throws RQLException {
		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			page.submitToWorkflow();
		}
	}

	/**
	 * Löscht alle Seiten in dieser Liste. Diese Liste ist danach leer. Auch mehrfach verlinkte Seiten werden gelöscht!
	 * 
	 * @return number of deleted pages
	 */
	public int deleteAll(boolean ignoreReferences) throws RQLException {

		int result = 0;
		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			try {
				page.delete(ignoreReferences);
				result += 1;
			} catch (MissingRightException mre) {
				// ignore
			}
		}
		clear();
		return result;
	}

	/**
	 * Löscht alle Seiten inklusive der Containerkindseiten in dieser Liste. Diese Liste ist danach leer. Auch mehrfach verlinkte
	 * Seiten werden gelöscht!
	 */
	public void deleteAllWithContainerChilds(String containerTemplateElementName, boolean ignoreReferences) throws RQLException {

		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			page.deleteWithContainerChilds(containerTemplateElementName, ignoreReferences);
		}
		clear();
	}

	/**
	 * Liefert die (erste) Seite mit dem gegebenen Dateinamen aus dieser Liste oder null, falls keine Seite mit diesem Dateinamen
	 * enthalten ist.
	 * <p>
	 * Prüft mit #startsWith, da in RD häufig der Extender nicht im filename auftaucht.
	 */
	public Page findByFilename(String filename) throws RQLException {

		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			if (filename.startsWith(page.getFilename())) {
				return page;
			}
		}
		// indicate not found
		return null;
	}

	/**
	 * Liefert die (erste) Seite deren Dateiname mit filenameSuffix endet aus dieser Liste oder null, falls nicht gefunden.
	 * <p>
	 * Prüft mit #endsWith().
	 */
	public Page findByFilenameEndsWith(String filenameSuffix) throws RQLException {

		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			if (page.getFilename().endsWith(filenameSuffix)) {
				return page;
			}
		}
		// indicate not found
		return null;
	}

	/**
	 * Liefert die (erste) Seite mit der gegebenen Überschrift aus dieser Liste oder null, falls keine Seite mit der Überschrift
	 * enthalten ist.
	 * <p>
	 * Prüft mit #equals().
	 */
	public Page findByHeadline(String headline) throws RQLException {

		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			if (headline.equals(page.getHeadline())) {
				return page;
			}
		}
		// indicate not found
		return null;
	}

	/**
	 * Liefert true, falls es eine erste Seite gibt, deren Überschrift mit headlinePrefix beginnt, sont false.
	 * <p>
	 * @see #findByHeadlineStartsWith(String)
	 */
	public boolean containsByHeadlineStartsWith(String headlinePrefix) throws RQLException {
		return findByHeadlineStartsWith(headlinePrefix) != null;
	}
	/**
	 * Liefert die (erste) Seite deren Überschrift mit headlinePrefix beginnt aus dieser Liste oder null, falls nicht gefunden.
	 * <p>
	 * Prüft mit #startsWith().
	 */
	public Page findByHeadlineStartsWith(String headlinePrefix) throws RQLException {

		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			if (page.getHeadline().startsWith(headlinePrefix)) {
				return page;
			}
		}
		// indicate not found
		return null;
	}

	/**
	 * Liefert die (erste) Seite deren erster Gruppenwert der Überschrift der RegEx regexWithGroup aus dieser Liste gleich groupValue
	 * ist oder null, falls nicht gefunden.
	 * <p>
	 * Prüft den ersten Gruppenwert group(1) der RegEx mit #equals(groupValue).
	 */
	public Page findByHeadlineRegExFirstGroupEquals(String regexWithGroup, String groupValue) throws RQLException {
		Pattern pattern = Pattern.compile(regexWithGroup);
		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			Matcher matcher = pattern.matcher(page.getHeadline());
			if (matcher.find() && matcher.group(1).equals(groupValue)) {
				return page;
			}
		}
		// indicate not found
		return null;
	}

	/**
	 * Liefert die (erste) Seite mit der gegebenen pageId aus dieser Liste oder null, falls keine Seite mit der Überschrift enthalten
	 * ist.
	 * <p>
	 * Prüft mit #equals().
	 */
	public Page findByPageId(String pageId) throws RQLException {

		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			if (pageId.equals(page.getPageId())) {
				return page;
			}
		}
		// indicate not found
		return null;
	}

	/**
	 * Liefert die (erste) Seite mit der gegebenen pageGuid aus dieser Liste oder null, falls keine Seite mit der page GUID enthalten
	 * ist.
	 * <p>
	 * Prüft mit #equals().
	 */
	public Page findByPageGuid(String pageGuid) throws RQLException {

		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			if (pageGuid.equals(page.getPageGuid())) {
				return page;
			}
		}
		// indicate not found
		return null;
	}

	/**
	 * Liefert true, falls diese Liste eine Seite mit der gegebenen Page ID enthält, sonst false.
	 * <p>
	 * Prüft mit #equals().
	 */
	public boolean containsByPageId(String pageId) throws RQLException {

		return findByPageId(pageId) != null ? true : false;
	}

	/**
	 * Liefert die (erste) Seite, deren StandartFieldDate Element templateElementName den Wert value hat oder null, falls nicht
	 * vorhanden.
	 * <p>
	 * Prüft das Datum per String mit dem pattern yyyyMMdd. TODO werden hier auch draft seiten anderer autoren mit gefunden?
	 */
	public Page findByStandardFieldDateValue(String templateElementName, ReddotDate dateValue) throws RQLException {

		String value = dateValue.getAsyyyyMMdd();

		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			if (value.equals(page.getStandardFieldDateValue(templateElementName).getAsyyyyMMdd())) {
				return page;
			}
		}
		// indicate not found
		return null;
	}

	/**
	 * Liefert die (erste) Seite, deren StandartFieldText Element templateElementName hat den Wert value hat oder null, falls nicht
	 * vorhanden.
	 * <p>
	 * Prüft mit #equals(). Ist freeOccupiedMemory true, wird der Speicher verworfener Seiten freigegeben, um einen Überlauf zu
	 * verhindern. TODO werden hier auch draft seiten anderer autoren mit gefunden?
	 */
	public Page findByStandardFieldTextValue(String templateElementName, String value, boolean freeOccupiedMemory) throws RQLException {

		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			if (value.equals(page.getStandardFieldTextValue(templateElementName))) {
				return page;
			}
			// free occupied memory
			if (freeOccupiedMemory) {
				page.freeOccupiedMemory();
			}
		}
		// indicate not found
		return null;
	}

	/**
	 * Liefert die (erste) Seite, deren StandartFieldText Element templateElementName hat den Wert value hat oder null, falls nicht
	 * vorhanden.
	 * <p>
	 * Prüft mit #equals().
	 */
	public Page findByStandardFieldTextValue(String templateElementName, String value) throws RQLException {
		return findByStandardFieldTextValue(templateElementName, value, false);
	}

	/**
	 * Liefert die erste Seite aus dieser Liste
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if index is out of range <tt>(index  &lt; 0 || index &gt;= size())</tt>.
	 */
	public Page first() throws RQLException {
		return getPage(0);
	}

	/**
	 * Liefert die Seite am gegebenen index.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if index is out of range <tt>(index
	 * 		  &lt; 0 || index &gt;= size())</tt>.
	 */
	public Page getPage(int index) throws RQLException {

		return (Page) super.get(index);
	}

	/**
	 * Liefert die letzte Seite aus dieser Liste
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if index is out of range <tt>(index  &lt; 0 || index &gt;= size())</tt>.
	 */
	public Page last() throws RQLException {
		return getPage(size() - 1);
	}

	/**
	 * Liefert alle Seiten aus pageList, für die der gegebenen filter true liefert. Diese Liste selbst wird dabei nicht verändert!
	 * 
	 * @param filter
	 *            ein Seitenfilter
	 * @see <code>PageFiler</code>
	 */
	public PageArrayList select(PageFilter filter) throws RQLException {

		PageArrayList selected = new PageArrayList();
		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			if (filter.check(page)) {
				selected.add(page);
			}
		}
		return selected;
	}

	/**
	 * Liefert eine neue Liste von Seiten, die vom angemeldeten Benutzer änderbar sind.
	 */
	public PageArrayList selectAllChangeablePages() throws RQLException {
		return this.select(new ChangeablePageFilter());
	}

	/**
	 * Liefert eine neue Liste von Seiten, die von den Benutzern der gegebenen Gruppe erstellt wurden.
	 */
	public PageArrayList selectAllCreatedBy(UserGroup userGroup) throws RQLException {
		return this.select(new CreatedByPageFilter(userGroup));
	}

	/**
	 * Liefert eine neue Liste von Seiten, deren letztes Änderungsdatum vor dem gegebenen Datum margin liegt.
	 */
	public PageArrayList selectAllLastChangedOnBefore(ReddotDate margin) throws RQLException {
		return this.select(new LastChangedOnBeforePageFilter(margin));
	}

	/**
	 * Liefert eine neue Liste von Seiten, deren letztes Änderungsdatum marginDays vor heute liegt.
	 */
	public PageArrayList selectAllLastChangedOnBefore(int marginDays) throws RQLException {
		ReddotDate margin = new ReddotDate();
		margin.rollDay(-marginDays);
		return this.select(new LastChangedOnBeforePageFilter(margin));
	}

	/**
	 * Liefert eine neue Liste von Seiten, die vom gegebenen Benutzer zuletzt geändert wurden.
	 */
	public PageArrayList selectAllLastChangedBy(String userName) throws RQLException {
		return this.select(new LastChangedByPageFilter(userName));
	}

	/**
	 * Liefert eine neue Liste von Seiten deren Template den gegebenen Namen hat.
	 */
	public PageArrayList selectAllPagesBasedOn(String templateName) throws RQLException {
		return this.select(new TemplatePageFilter(templateName));
	}

	/**
	 * Liefert eine neue Liste von Seiten die ein Element mit dem gegebenen Namen besitzen.
	 */
	public PageArrayList selectAllPagesContaining(String templateElementName) throws RQLException {
		return this.select(new TemplateElementPageFilter(templateElementName));
	}

	/**
	 * Liefert eine neue Liste von Seiten, die alle im Status draftChanged or draftNew sind.
	 */
	public PageArrayList selectAllPagesInStateDraft() throws RQLException {
		return this.select(StatePageFilter.getDraftStatePageFilter());
	}

	/**
	 * Liefert eine neue Liste von Seiten, die alle im Status draftChanged sind.
	 */
	public PageArrayList selectAllPagesInStateDraftChanged() throws RQLException {
		return this.select(StatePageFilter.getDraftChangedStatePageFilter());
	}

	/**
	 * Liefert eine neue Liste von Seiten, die alle im Status draftNew sind.
	 */
	public PageArrayList selectAllPagesInStateDraftNew() throws RQLException {
		return this.select(StatePageFilter.getDraftNewStatePageFilter());
	}

	/**
	 * Liefert eine neue Liste von Seiten, die alle im Status released sind.
	 */
	public PageArrayList selectAllPagesInStateReleased() throws RQLException {
		return this.select(StatePageFilter.getReleasedStatePageFilter());
	}

	/**
	 * Liefert eine neue Liste von Seiten, die in einem der gegebenen Stati sind. Entweder draft oder draftNew and draftChanged für
	 * bessere performance.
	 */
	public PageArrayList selectAllPagesInStates(boolean draft, boolean draftNew, boolean draftChanged, boolean waitingForRelease,
			boolean waitingForCorrection, boolean released) throws RQLException {
		return this.select(new StatePageFilter(draft, draftNew, draftChanged, waitingForRelease, waitingForCorrection, released));
	}

	/**
	 * Liefert eine neue Liste von Seiten, die alle im Status waitingForCorrection sind.
	 */
	public PageArrayList selectAllPagesInStateWaitingForCorrection() throws RQLException {
		return this.select(StatePageFilter.getWaitingForCorrectionStatePageFilter());
	}

	/**
	 * Liefert eine neue Liste von Seiten, die alle im Status waitingForRelease sind.
	 */
	public PageArrayList selectAllPagesInStateWaitingForRelease() throws RQLException {
		return this.select(StatePageFilter.getWaitingForReleaseStatePageFilter());
	}

	/**
	 * Liefert eine neue Liste von Seiten die das gegebene Element nicht haben.
	 */
	public PageArrayList selectAllPagesNotContaining(String templateElementName) throws RQLException {
		return this.select(new NotPageFilter(new TemplateElementPageFilter(templateElementName)));
	}

	/**
	 * Liefert alle Seiten aus dieser Liste, deren headline in der gegebenen Liste enthalten ist. Diese Liste selbst wird dabei nicht
	 * verändert!
	 */
	public PageArrayList selectAllPagesWithHeadlineIn(Collection<String> headlines) throws RQLException {

		PageArrayList selected = new PageArrayList();
		for (int i = 0; i < size(); i++) {
			Page page = (Page) get(i);
			if (headlines.contains(page.getHeadline())) {
				selected.add(page);
			}
		}
		return selected;
	}

	/**
	 * Liefert eine neue Liste von Seiten ohne Überschrift (liefert nur die GUID-Seiten).
	 */
	public PageArrayList selectAllPagesWithoutHeadline() throws RQLException {
		return this.select(new NoHeadlinePageFilter());
	}

	/**
	 * Liefert eine neue Liste von Seiten deren Überschrift mit dem gegebenen prefix beginnen.
	 */
	public PageArrayList selectAllPagesHeadlineStartsWith(String prefix) throws RQLException {
		return this.select(new HeadlineStartWithPageFilter(prefix));
	}

	/**
	 * Liefert eine neue Liste von Seiten, deren Template den gegebenen Namen hat.
	 */
	public PageArrayList selectAllTemplateNamed(String templateName) throws RQLException {
		return this.select(new TemplatePageFilter(templateName));
	}

	/**
	 * Liefert eine neue Liste von Seiten, in denen der Wert im StandardFieldDate standardFieldDateTmpltElemName < (before) marginDate
	 * ist.
	 */
	public PageArrayList selectAllPagesWithStandardFieldDateValueBefore(ReddotDate marginDate, String standardFieldDateTmpltElemName)
			throws RQLException {
		return this.select(new StandardFieldDateBeforePageFilter(marginDate, standardFieldDateTmpltElemName));
	}

	/**
	 * Sortiert diese Liste mittels des gegebenen Comparators.
	 * 
	 * @param comparator
	 *            a java standard comparator
	 */
	public PageArrayList sort(PageComparator comparator) {

		java.util.Collections.sort(this, comparator);
		return this;
	}

	/**
	 * Returns a new page list sorted by headline ascending.
	 */
	public PageArrayList sortByHeadlineAsc() throws RQLException {
		HeadlinePageComparator comparator = new HeadlinePageComparator();
		return sort(comparator);
	}

	/**
	 * Returns a new page list sorted by headline descending.
	 */
	public PageArrayList sortByHeadlineDesc() throws RQLException {
		HeadlinePageComparator comparator = new HeadlinePageComparator();
		comparator.forceDescendingOrdering();
		return sort(comparator);
	}
}
