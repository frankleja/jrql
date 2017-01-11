package com.hlcl.rql.util.as;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * This class encapsulates the union operations needed to synchronize between a list of pages within cms and a given set of external data.
 * <p>
 * This comparison is implemented by using a page attribute adapter.
 */
public class TargetPerformanceComparison {

	private PageArrayList currentPages;
	private List<String> currentAttributes;
	private SortedSet<String> targets;

	private String adapterMethodName;
	private String adapterMethodParameterValue; // null supported

	/**
	 * Creates a comparison. Given currentPages has to be different attributes each
	 */
	public TargetPerformanceComparison(PageArrayList currentPages, SortedSet<String> targets, String adapterMethodName,
			String adapterMethodParameterValue) throws RQLException {
		this.currentPages = currentPages;
		this.targets = targets;
		this.adapterMethodName = adapterMethodName;
		this.adapterMethodParameterValue = adapterMethodParameterValue;

		// initialize attribute values
		initializeCurrentAttributes();
	}

	/**
	 * Creates a comparison. Given currentPages has to be different attributes each
	 */
	public TargetPerformanceComparison(PageArrayList currentPages, SortedSet<String> targets, String adapterMethodName) throws RQLException {
		this.currentPages = currentPages;
		this.targets = targets;
		this.adapterMethodName = adapterMethodName;
		this.adapterMethodParameterValue = null;

		// initialize attribute values
		initializeCurrentAttributes();
	}

	/**
	 * Creates a comparison. Given currentPages has to be different attributes each
	 */
	public TargetPerformanceComparison(PageArrayList currentPages, String[] targets, String adapterMethodName, String adapterMethodParameterValue)
			throws RQLException {
		this.currentPages = currentPages;

		this.targets = new TreeSet<String>();
		this.targets.addAll(Arrays.asList(targets));

		this.adapterMethodName = adapterMethodName;
		this.adapterMethodParameterValue = adapterMethodParameterValue;

		// initialize attribute values
		initializeCurrentAttributes();
	}

	/**
	 * Creates a comparison. Given currentPages has to be different attributes each
	 */
	public TargetPerformanceComparison(PageArrayList currentPages, String[] targets, String adapterMethodName) throws RQLException {
		this.currentPages = currentPages;

		this.targets = new TreeSet<String>();
		this.targets.addAll(Arrays.asList(targets));

		this.adapterMethodName = adapterMethodName;
		this.adapterMethodParameterValue = null;

		// initialize attribute values
		initializeCurrentAttributes();
	}

	/**
	 * 1. Step: Returns all pages that has to be removed; are not in targets anymore.
	 * 
	 * @throws RQLException
	 */
	public PageArrayList get1PagesWhichHaveToBeRemoved() throws RQLException {
		return getCurrentPagesWithAttributeIn(difference(currentAttributes, targets));
	}

	/**
	 * Return all pages from current pages for which the configured attribute is contained in given list.
	 */
	private PageArrayList getCurrentPagesWithAttributeIn(Collection<String> attributes) throws RQLException {
		PageArrayList result = new PageArrayList();
		for (int i = 0; i < currentPages.size(); i++) {
			Page page = (Page) currentPages.get(i);
			PageAttributeAdapter adapter = buildAdapter(page);
			if (attributes.contains(adapter.getValue())) {
				result.add(page);
			}
		}
		return result;
	}

	/**
	 * 2. Step: Returns all pages that has to be updated.
	 */
	public PageArrayList get2PagesWhichHasToBeUpdated() throws RQLException {
		return getCurrentPagesWithAttributeIn(targets);
	}

	/**
	 * 3. Step: Returns all page attributes that has to be added.
	 */
	public List<String> get3PageAttributesWhichHasToBeAdded() {
		return difference(targets, currentAttributes);
	}

	/**
	 * 4. Step: Returns a list with all sorted page attributes, which should be used to order the pages accordingly.
	 */
	public SortedSet<String> get4PagesSortOrder() {
		// simply return the already sorted targets
		return targets;
	}

	/**
	 * Returns true, if the used method has a parameter.
	 */
	private boolean hasMethodAParemeter() throws RQLException {
		return adapterMethodParameterValue != null;
	}

	/**
	 * Builds the adapter for the given page and the configured method.
	 */
	private PageAttributeAdapter buildAdapter(Page page) throws RQLException {
		PageAttributeAdapter adapter = null;
		if (hasMethodAParemeter()) {
			adapter = new PageAttributeAdapter(page, adapterMethodName, adapterMethodParameterValue);
		} else {
			adapter = new PageAttributeAdapter(page, adapterMethodName);
		}
		return adapter;
	}

	/**
	 * Sets the current attributes by applying the adapter to all current pages.
	 */
	private void initializeCurrentAttributes() throws RQLException {
		currentAttributes = new ArrayList<String>(currentPages.size());
		for (Iterator iterator = currentPages.iterator(); iterator.hasNext();) {
			Page page = (Page) iterator.next();
			PageAttributeAdapter adapter = buildAdapter(page);
			currentAttributes.add(adapter.getValue());
		}
	}

	/**
	 * Returns the difference between the both collections.
	 */
	private List<String> difference(Collection<String> minuend, Collection<String> subtrahend) {
		List<String> difference = new ArrayList<String>(minuend);
		difference.removeAll(subtrahend);
		return difference;
	}
}
