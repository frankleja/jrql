package com.hlcl.rql.util.as;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * This class collects created, updated and removed pages for a whole synchronization program.
 * <p>
 * Should be used to build a combined changed status at the end and send e.g. by e-mail.
 */
public class ModificationsCollector {
	private PageArrayList createdPages;
	private PageArrayList updatedPages;
	private PageArrayList deactivatedPages;
	private PageArrayList reactivatedPages;
	private java.util.List<String> deletedPages;

	/**
	 * Constructor: Creates a new collector.
	 */
	public ModificationsCollector() {
		createdPages = new PageArrayList();
		updatedPages = new PageArrayList();
		deactivatedPages = new PageArrayList();
		reactivatedPages = new PageArrayList();
		deletedPages = new ArrayList<String>();
	}

	/**
	 * Returns all created pages sorted by headline ascending.
	 * 
	 * @throws RQLException
	 */
	public PageArrayList getCreatedPagesSortedByHeadlineAsc() throws RQLException {
		return createdPages.sortByHeadlineAsc();
	}

	/**
	 * Returns all updated pages sorted by headline ascending.
	 * 
	 * @throws RQLException
	 */
	public PageArrayList getUpdatedPagesSortedByHeadlineAsc() throws RQLException {
		return updatedPages.sortByHeadlineAsc();
	}

	/**
	 * Returns all deactivated pages sorted by headline ascending.
	 * 
	 * @throws RQLException
	 */
	public PageArrayList getDeactivatedPagesSortedByHeadlineAsc() throws RQLException {
		return deactivatedPages.sortByHeadlineAsc();
	}

	/**
	 * Returns all reactivated pages sorted by headline ascending.
	 * 
	 * @throws RQLException
	 */
	public PageArrayList getReactivatedPagesSortedByHeadlineAsc() throws RQLException {
		return reactivatedPages.sortByHeadlineAsc();
	}

	/**
	 * Returns all deleted pages info set sorted ascending.
	 * 
	 * @throws RQLException
	 */
	public SortedSet<String> getDeletedPagesSortedAsc() throws RQLException {
		SortedSet<String> sorted = new TreeSet<String>();
		sorted.addAll(deletedPages);
		return sorted;
	}

	/**
	 * Adds all sorted page information to target using given delimiter.
	 */
	public String collectDeletedPagesInfoText(String delimiter) throws RQLException {
		String result = "";
		for (String info : getDeletedPagesSortedAsc()) {
			result += info + delimiter;
		}
		return result;
	}

	/**
	 * Remember a newly created page.
	 */
	public void created(Page newPage) {
		createdPages.add(newPage);
	}

	/**
	 * Remember the deactivated page.
	 */
	public void deactivated(Page deactivatedPage) {
		deactivatedPages.add(deactivatedPage);
	}

	/**
	 * Remember all given deactivated pages.
	 */
	public void deactivated(PageArrayList deactivatedPages) {
		this.deactivatedPages.addAll(deactivatedPages);
	}

	/**
	 * Remember the updated page.
	 */
	public void updated(Page updatedPage) {
		updatedPages.add(updatedPage);
	}

	/**
	 * Remember the reactivated page.
	 */
	public void reactivated(Page reactivatedPage) {
		reactivatedPages.add(reactivatedPage);
	}

	/**
	 * Remember the given details of a deleted page.
	 */
	public void deleted(String deletedPageInfo) {
		deletedPages.add(deletedPageInfo);
	}

	/**
	 * Returns true, if at least one page was deleted.
	 */
	public boolean arePagesDeleted() {
		return deletedPages.size() > 0;
	}

	/**
	 * Returns true, if at least one page was created.
	 */
	public boolean arePagesCreated() {
		return createdPages.size() > 0;
	}

	/**
	 * Returns true, if at least one page was re-activated.
	 */
	public boolean arePagesReactivated() {
		return reactivatedPages.size() > 0;
	}

	/**
	 * Returns true, if at least one page was deactivated.
	 */
	public boolean arePagesDeactivated() {
		return deactivatedPages.size() > 0;
	}

	/**
	 * Returns true, if at least one page was updated.
	 */
	public boolean arePagesUpdated() {
		return updatedPages.size() > 0;
	}

	/**
	 * Returns true, if there was at least one modification, regardless of what type.
	 */
	public boolean anyModifications() {
		return anyModifications(true, true, true, true, true);
	}

	/**
	 * Returns true, if there was at least one modification, regardless of what type.
	 */
	public boolean anyModifications(boolean includeCreated, boolean includeReactivated, boolean includeUpdated,
			boolean includeDeleted, boolean includeDeativated) {
		return includeDeleted && arePagesDeleted() || includeCreated && arePagesCreated() || includeDeativated
				&& arePagesDeactivated() || includeReactivated && arePagesReactivated() || includeUpdated && arePagesUpdated();
	}

	/**
	 * Returns a text for a e.g. e-mail containing all types of modified pages always sorted by headline ascending.
	 * <p>
	 * All modifications are included and returned in this order: created, reactivated, updated, deleted, deactivated.
	 * 
	 * @throws RQLException
	 */
	public String buildModificationsText() throws RQLException {
		return buildModificationsText(true, true, true, true, true);
	}

	/**
	 * Returns a text for a e.g. e-mail containing all types of modified pages always sorted by headline ascending.
	 * <p>
	 * Use this order: created, reactivated, updated, deleted, deactivated.
	 * <p>
	 * Skip modification information with a given false.
	 * 
	 * @throws RQLException
	 */
	public String buildModificationsText(boolean includeCreated, boolean includeReactivated, boolean includeUpdated,
			boolean includeDeleted, boolean includeDeativated) throws RQLException {
		String text = "";
		String delimiter = "\n";

		if (includeCreated && arePagesCreated()) {
			text += "pages created:" + delimiter;
			text += getCreatedPagesSortedByHeadlineAsc().collectInfoText(delimiter);
			text += delimiter;
		}
		if (includeReactivated && arePagesReactivated()) {
			text += "pages reactivated:" + delimiter;
			text += getReactivatedPagesSortedByHeadlineAsc().collectInfoText(delimiter);
			text += delimiter;
		}
		if (includeUpdated && arePagesUpdated()) {
			text += "pages updated:" + delimiter;
			text += getUpdatedPagesSortedByHeadlineAsc().collectInfoText(delimiter);
			text += delimiter;
		}
		if (includeDeleted && arePagesDeleted()) {
			text += "pages deleted:" + delimiter;
			text += collectDeletedPagesInfoText(delimiter);
			text += delimiter;
		}
		if (includeDeativated && arePagesDeactivated()) {
			text += "pages deactivated:" + delimiter;
			text += getDeactivatedPagesSortedByHeadlineAsc().collectInfoText(delimiter);
			text += delimiter;
		}

		return text;
	}

}
