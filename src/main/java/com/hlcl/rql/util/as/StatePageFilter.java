package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * Filter all pages accordingly the choosen page state. 
 * Combinations are possible, but for every configured check a uncached re-read of page state is used.
 */
public class StatePageFilter extends PageFilterImpl {
	/**
	 * Returns a state page filter for only pages in state waitingForRelease.
	 */
	public static StatePageFilter getWaitingForReleaseStatePageFilter() {
		return new StatePageFilter(false, false, false, true, false, false);
	}

	/**
	 * Returns a state page filter for only pages in state waitingForCorrection.
	 */
	public static StatePageFilter getWaitingForCorrectionStatePageFilter() {
		return new StatePageFilter(false, false, false, false, true, false);
	}

	/**
	 * Returns a state page filter for only pages in state released.
	 */
	public static StatePageFilter getReleasedStatePageFilter() {
		return new StatePageFilter(false, false, false, false, false, true);
	}

	/**
	 * Returns a state page filter for only pages in state draft.
	 */
	public static StatePageFilter getDraftStatePageFilter() {
		return new StatePageFilter(true, false, false, false, false, false);
	}

	/**
	 * Returns a state page filter for only pages in state draftNew.
	 */
	public static StatePageFilter getDraftNewStatePageFilter() {
		return new StatePageFilter(false, true, false, false, false, false);
	}

	/**
	 * Returns a state page filter for only pages in state draftChanged.
	 */
	public static StatePageFilter getDraftChangedStatePageFilter() {
		return new StatePageFilter(false, false, true, false, false, false);
	}

	private boolean draft;
	private boolean draftChanged;
	private boolean draftNew;
	private boolean released;
	private boolean waitingForCorrection;
	private boolean waitingForRelease;

	/**
	 * Constructor comment.
	 * Set a true for every state you want to select.
	 * Use draft or draftNew and draftChanged, not both.
	 */
	public StatePageFilter(boolean draft, boolean draftNew, boolean draftChanged, boolean waitingForRelease, boolean waitingForCorrection, boolean released) {
		super();
		this.draft = draft;
		this.draftNew = draftNew;
		this.draftChanged = draftChanged;
		this.waitingForCorrection = waitingForCorrection;
		this.released = released;
		this.waitingForRelease = waitingForRelease;
	}

	/** 
	 * Returns true only if page is in one of the configured states.
	 */
	public boolean check(Page page) throws RQLException {

		// end checks, if one fit found
		if (released && page.isInStateReleased()) {
			return true;
		}
		if (draft && page.isInStateSavedAsDraft()) {
			return true;
		}
		if (draftNew && page.isInStateSavedAsDraftNew()) {
			return true;
		}
		if (draftChanged && page.isInStateSavedAsDraftChanged()) {
			return true;
		}
		if (waitingForRelease && page.isInStateWaitingForRelease()) {
			return true;
		}
		if (waitingForCorrection && page.isInStateWaitingForCorrection()) {
			return true;
		}
		// all tried, but no fit found
		// this include misconfiguration (all false) too!
		return false;
	}
}
