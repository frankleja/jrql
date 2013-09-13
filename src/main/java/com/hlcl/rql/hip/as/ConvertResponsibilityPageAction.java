package com.hlcl.rql.hip.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.Listener;
import com.hlcl.rql.util.as.PageAction;

/**
 * @author lejafr
 * 
 * Diese Klasse konvertiert alle responsible Werte für ein physical HIP Seite in den neuen stil (mit verlinkter Abteilungsseite).
 */
public class ConvertResponsibilityPageAction extends PageAction {

	private boolean submitToWorkflow;
	private Listener convertedListener;
	private Listener notConvertedListener;

	/**
	 * ConvertResponsibilityPageAction constructor
	 */
	public ConvertResponsibilityPageAction(boolean submitToWorkflow, Listener convertedListener, Listener notConvertedListener) throws RQLException {
		super();

		this.submitToWorkflow = submitToWorkflow;
		this.convertedListener = convertedListener;
		this.notConvertedListener = notConvertedListener;
	}

	/**
	 * Für jede übergebene Seite wird die Konvertierung gestartet, falls es eine physical page ist
	 * <p>
	 * und wenn notwendig und im Konstruktur gewünscht auch bestätigt.
	 * 
	 * @see com.hlcl.rql.util.as.PageAction#invoke(com.hlcl.rql.as.Page)
	 */
	@Override
	public void invoke(Page page) throws RQLException {
		boolean converted = false;
		ContentPage cp = new ContentPage(page);
		boolean hasPageFooterElements = cp.hasPageFooterElements();
		boolean inStateReleased = cp.isInStateReleased();
		if (hasPageFooterElements && inStateReleased) {
			cp.convertResponsibility();
			boolean submitNeeded = cp.isInStateSavedAsDraft(); 
			convertedListener.update("  converted " + page.getInfoText() + " submitNeeded=" + submitNeeded);
			converted = true;
			if (submitToWorkflow && submitNeeded) {
				cp.submitToWorkflow();
			}
		}

		// log reason why not converted
		if (!converted) {
			notConvertedListener.update("  not converted " + page.getInfoText() + " inStateReleased= " + inStateReleased + " hasPageFooterElements= " + hasPageFooterElements);
		}
	}
}
