package com.hlcl.rql.hip.as;

import com.hlcl.rql.as.List;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.ProjectPage;

/**
 * This class represents all pages of template edi_status_table_block and offer edit functions for this page.
 * 
 * @author lejafr
 */
public class EdiStatusTableBlock extends ProjectPage {

	private List businessFunctionsList;
	// overwriting: counts the overwritting rows
	private int overwrittenRowsCounter;
	// list of text rows
	private PageArrayList rows;
	// current row; default behavior is, that a newly created row will become the current row
	private Page currentRow;
	// overwriting: remember at starting time
	private int existingRowsSize;

	/**
	 * Creates an edi status table block page.
	 * 
	 * @throws RQLException
	 */
	public EdiStatusTableBlock(Page page) throws RQLException {
		super(page);
		initializeRows();
	}

	/**
	 * Returns the valid at time stamp from this edit status table block.
	 * 
	 * @throws RQLException
	 */
	public String getValidAt() throws RQLException {
		return getStandardFieldTextValue(getParameter("validAtTmpltElemName"));
	}

	/**
	 * Changes the valid at time stamp on this edi status table block.
	 * 
	 * @throws RQLException
	 */
	public void setValidAt(String validAt) throws RQLException {
		setStandardFieldTextValue(getParameter("validAtTmpltElemName"), validAt);
	}

	/**
	 * Returns the business functions list from this edi status table block.
	 * 
	 * @throws RQLException
	 */
	private List getBusinessFunctionsList() throws RQLException {
		if (businessFunctionsList == null) {
			businessFunctionsList = getList(getParameter("businessFunctionsListTmpltElemName"));
		}
		return businessFunctionsList;
	}

	/**
	 * Beendet das Überschreiben von Zeilenseiten (nicht notwendige werden gelöscht) mit StandardFieldText Werten.
	 * <p>
	 * Muss mit startOverwriting() begonnen werden!
	 * 
	 * @see #startOverwriting()
	 */
	public void endOverwriting() throws RQLException {
		// delete unnecessary row pages
		for (int i = overwrittenRowsCounter; i < existingRowsSize; i++) {
			rows.getPage(i).delete();
		}
	}

	/**
	 * Liest alle Zeilenseiten der Tabelle und setzt den Pointer auf die erste Zeile der Tabelle, falls möglich.
	 */
	private void initializeRows() throws RQLException {
		// get all childs
		rows = getListChildPages(getParameter("businessFunctionsListTmpltElemName"));
		if (rows.size() != 0) {
			firstRow();
		}
	}

	/**
	 * Macht die nächste Zeilenseite zur current row. Liefert true, falls möglich, false, falls keine Seite mehr vorhanden.
	 */
	public boolean nextRow() {

		// no start or no row at all
		if (currentRow == null) {
			return false;
		}

		// move one row page down
		int currentIndex = rows.indexOf(currentRow);
		boolean result = false;
		currentRow = null;
		if (currentIndex < rows.size() - 1) {
			currentRow = (Page) rows.get(currentIndex + 1);
			result = true;
		}

		return result;
	}

	/**
	 * Überschreibt alle gegebenen Zeilenwerte
	 * <p>
	 */
	public Page overwriteRow(String businessFunction, String inOut, String state, String messagesInQueue, String waitingTime,
			String estimatedFurtherDuration) throws RQLException {
		Page overwrittenPg = currentRow;
		// it is assumed that method firstRow() was called before
		// check, if create or update
		if (overwrittenRowsCounter < existingRowsSize) {
			// update
			updateCurrentRow(businessFunction, inOut, state, messagesInQueue, waitingTime, estimatedFurtherDuration);
			// go forward
			nextRow();
		} else {
			// create new row page
			overwrittenPg = addNewRow(businessFunction, inOut, state, messagesInQueue, waitingTime, estimatedFurtherDuration);
		}
		overwrittenRowsCounter++;
		return overwrittenPg;
	}

	/**
	 * Started das Überschreiben von Zeilenseiten (neue werden bei Bedarf angelegt) mit StandardFieldText Werten.
	 * <p>
	 * Muss mit endOverwriting() abgeschlossen werden!
	 * 
	 * @see #endOverwriting()
	 */
	public void startOverwriting() throws RQLException {
		// remember and initialize
		existingRowsSize = rows.size();
		overwrittenRowsCounter = 0;
	}

	/**
	 * Ändert die aktuelle Zeile auf die gegebenen Werte.
	 */
	public void updateCurrentRow(String businessFunction, String inOut, String state, String messagesInQueue, String waitingTime,
			String estimatedFurtherDuration) throws RQLException {
		currentRow.setHeadline(businessFunction);

		currentRow.startSetElementValues();
		currentRow.setStandardFieldTextValue(getParameter("inOutTmpltElemName"), inOut);
		currentRow.setOptionListValue(getParameter("stateTmpltElemName"), state);
		currentRow.setStandardFieldTextValue(getParameter("messagesInQueueTmpltElemName"), messagesInQueue);
		currentRow.setStandardFieldTextValue(getParameter("waitingTimeTmpltElemName"), waitingTime);
		currentRow.setStandardFieldTextValue(getParameter("estimatedFurtherDurationTmpltElemName"), estimatedFurtherDuration);
		currentRow.endSetElementValues();
	}

	/**
	 * Erstellt eine neue Zeile mit den gegebenen Werte. Die Zeile wird unten angehängt.
	 */
	public Page addNewRow(String businessFunction, String inOut, String state, String messagesInQueue, String waitingTime,
			String estimatedFurtherDuration) throws RQLException {

		Page row = getBusinessFunctionsList().createAndConnectPage(businessFunction);
		row.startSetElementValues();
		row.setStandardFieldTextValue(getParameter("inOutTmpltElemName"), inOut);
		row.setOptionListValue(getParameter("stateTmpltElemName"), state);
		row.setStandardFieldTextValue(getParameter("messagesInQueueTmpltElemName"), messagesInQueue);
		row.setStandardFieldTextValue(getParameter("waitingTimeTmpltElemName"), waitingTime);
		row.setStandardFieldTextValue(getParameter("estimatedFurtherDurationTmpltElemName"), estimatedFurtherDuration);
		row.endSetElementValues();
		return row;
	}

	/**
	 * Macht die erste Kindseite zur current row, wenn mindestens eine Kindseite vorhanden ist.
	 * 
	 * @return true, if this table has row, other false is returned
	 */
	public boolean firstRow() throws RQLException {
		if (rows.isEmpty()) {
			return false;
		}
		// possible
		currentRow = (Page) rows.get(0);
		return true;
	}
}
