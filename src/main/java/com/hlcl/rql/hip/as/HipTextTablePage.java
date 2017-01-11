package com.hlcl.rql.hip.as;

import java.util.HashMap;
import java.util.Map;

import com.hlcl.rql.as.*;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.ScriptParameters;

/**
 * Diese Klasse vereinfacht die Verwaltung einer HIP text_table_block Seite.<p>
 * Änderungen an der Tabellenseite selbst müssen mit updateTable() abgeschlossen werden.<p>
 * Diese Klasse unterstützt 3 Arten der Zeilenbehandlung:<p>
 * 1. Neuerstellung aller Zeilenseiten mittels createDataRow()<p>
 * oder 2. Veränderung der existierenden Zeilenseiten mittels<p>
 *     firstRow()<p>
 *       setCurrentRow*()<p>
 *       updateCurrentRow()<p>
 *     nextRow()<p>
 * oder 3. Der Mix der beiden oberen Varianten, als Überschreiben bezeichnet. Dabei werden existierende Zeilenseiten überschrieben, aber keine Spaltenwerte gelöscht!<p>
 *    Neue Zeilenseiten werden automatisch angelegt, falls notwendig. <p>
 *    startOverwriting()<p>
 *      overwriteDataRow(..) - update inklusive<p>
 *    endOverwriting() - löscht überschüssige Zeilenseiten
 * 
 * @author lejafr
 */
public class HipTextTablePage {

	private String colspanTmpltElemNameSuffix;
	private String colTmpltElemNamePrefix;

	private String contentTmpltFldrName;

	// current row; default behavior is, that a newly created row will become the current row
	private Page currentRow;
	private String dataTmpltElemNameSuffix;

	// overwriting: remember at starting time 
	private int existingRowsSize;

	// page template element names
	private String headerTmpltElemNameSuffix;
	// overwriting: counts the overwritting rows
	private int overwrittenRowsCounter;
	// the text table block page
	private Page page;
	// parms 
	private ScriptParameters parms;

	// local cache for delayed update of bunch of fields for the current row page
	private Map rowElementValuePairs;
	private String rowHeadlinePrefix;
	// list of text rows
	private PageArrayList rows;
	// caches
	private Template rowTemplate;
	private String rowTmpltName;
	// local cache for delayed update of bunch of fields for the table page itself
	private Map tableElementValuePairs;
	private String tableGridlineTmpltElemName;

	private com.hlcl.rql.as.List textListCache;

	// row template element names
	private String textListTmpltElemName;

	private String textTmpltElemNameSuffix;
	private String widthTmpltElemNameSuffix;

	/**
	 * HipTextTablePage constructor comment.
	 */
	public HipTextTablePage(Page page) throws RQLException {
		super();

		this.page = page;

		// initialize page parms
		parms = page.getProject().getParameters(getParmPageId());
		this.headerTmpltElemNameSuffix = parms.get("headerTmpltElemNameSuffix");
		this.widthTmpltElemNameSuffix = parms.get("widthTmpltElemNameSuffix");
		this.colTmpltElemNamePrefix = parms.get("colTmpltElemNamePrefix");
		this.tableGridlineTmpltElemName = parms.get("tableGridlineTmpltElemName");
		// initialize row parms
		this.textListTmpltElemName = parms.get("textListTmpltElemName");
		this.contentTmpltFldrName = parms.get("contentTmpltFldrName");
		this.rowTmpltName = parms.get("rowTmpltName");
		this.rowHeadlinePrefix = parms.get("rowHeadlinePrefix");
		this.dataTmpltElemNameSuffix = parms.get("dataTmpltElemNameSuffix");
		this.textTmpltElemNameSuffix = parms.get("textTmpltElemNameSuffix");
		this.colspanTmpltElemNameSuffix = parms.get("colspanTmpltElemNameSuffix");

		// initialize work variables
		tableElementValuePairs = new HashMap();
		initializeRows();
	}

	/**
	 * HipTextTablePage constructor comment.
	 */
	public HipTextTablePage(Page page, String headerTmpltElemNameSuffix, String widthTmpltElemNameSuffix, String colTmpltElemNamePrefix, String tableGridlineTmpltElemName) {
		super();

		this.page = page;

		this.headerTmpltElemNameSuffix = headerTmpltElemNameSuffix;
		this.widthTmpltElemNameSuffix = widthTmpltElemNameSuffix;
		this.colTmpltElemNamePrefix = colTmpltElemNamePrefix;
		this.tableGridlineTmpltElemName = tableGridlineTmpltElemName;

		// initialize
		tableElementValuePairs = new HashMap();
	}

	/**
	 * Prüft, ob vor dem Zugriff auf Zeilenfunktionen, auch die erforderlichen Templatenamen gesetzt wurden.
	 */
	private void checkRowTemplateNames() throws RQLException {

		// use one row name only
		if (textListTmpltElemName == null) {
			throw new MissingTemplateNamesException("Row Template names missing. Use function setRowTemplateNames() before.");
		}
	}

	/**
	 * Erstellt eine Zeile und setzt den Standard-Text in Spalte 1.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page createDataRow(String data1) throws RQLException {

		return createDataRow(data1, null, null, null, null, null, null, null, null, null);
	}

	/**
	 * Erstellt eine Zeile und setzt den Standard-Text bis Spalte 2.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page createDataRow(String data1, String data2) throws RQLException {

		return createDataRow(data1, data2, null, null, null, null, null, null, null, null);
	}

	/**
	 * Erstellt eine Zeile und setzt den Standard-Text bis Spalte 3.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page createDataRow(String data1, String data2, String data3) throws RQLException {

		return createDataRow(data1, data2, data3, null, null, null, null, null, null, null);
	}

	/**
	 * Erstellt eine Zeile und setzt den Standard-Text bis Spalte 4.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page createDataRow(String data1, String data2, String data3, String data4) throws RQLException {

		return createDataRow(data1, data2, data3, data4, null, null, null, null, null, null);
	}

	/**
	 * Erstellt eine Zeile und setzt den Standard-Text bis Spalte 5.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page createDataRow(String data1, String data2, String data3, String data4, String data5) throws RQLException {

		return createDataRow(data1, data2, data3, data4, data5, null, null, null, null, null);
	}

	/**
	 * Erstellt eine Zeile und setzt den Standard-Text bis Spalte 6.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page createDataRow(String data1, String data2, String data3, String data4, String data5, String data6) throws RQLException {

		return createDataRow(data1, data2, data3, data4, data5, data6, null, null, null, null);
	}

	/**
	 * Erstellt eine Zeile und setzt den Standard-Text bis Spalte 7.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page createDataRow(String data1, String data2, String data3, String data4, String data5, String data6, String data7) throws RQLException {

		return createDataRow(data1, data2, data3, data4, data5, data6, data7, null, null, null);
	}

	/**
	 * Erstellt eine Zeile und setzt den Standard-Text bis Spalte 8.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page createDataRow(String data1, String data2, String data3, String data4, String data5, String data6, String data7, String data8) throws RQLException {

		return createDataRow(data1, data2, data3, data4, data5, data6, data7, data8, null, null);
	}

	/**
	 * Erstellt eine Zeile und setzt den Standard-Text bis Spalte 9.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page createDataRow(String data1, String data2, String data3, String data4, String data5, String data6, String data7, String data8, String data9) throws RQLException {

		return createDataRow(data1, data2, data3, data4, data5, data6, data7, data8, data9, null);
	}

	/**
	 * Erstellt eine Zeile und setzt in dieser Zeile die Standardfelder-Text beginnend bei 1.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page createDataRow(String data1, String data2, String data3, String data4, String data5, String data6, String data7, String data8, String data9, String data10) throws RQLException {

		// needs given row template names 
		checkRowTemplateNames();

		// create row
		String rowHeadline = rowHeadlinePrefix + StringHelper.getFirstWord(data1);
		Page rowPg = createRow(rowHeadline);

		// prepare for loop handling
		String[] dataValues = new String[10];
		dataValues[0] = data1;
		dataValues[1] = data2;
		dataValues[2] = data3;
		dataValues[3] = data4;
		dataValues[4] = data5;
		dataValues[5] = data6;
		dataValues[6] = data7;
		dataValues[7] = data8;
		dataValues[8] = data9;
		dataValues[9] = data10;

		// set all row values
		Map elementValuePairs = new HashMap();
		for (int i = 0; i < dataValues.length; i++) {
			if (dataValues[i] != null) {
				Element elem = rowPg.getStandardFieldTextElement(colTmpltElemNamePrefix + (i + 1) + dataTmpltElemNameSuffix);
				elementValuePairs.put(elem, dataValues[i]);
			} else {
				// stop at first null
				break;
			}
		}
		rowPg.setElementValues(elementValuePairs);
		return rowPg;
	}

	/**
	 * Erstellt eine neue Zeile und fügt sie der Liste der Seiten hinzu.
	 *
	 *@param	rowName		a name for the row page headline; without prefix 'row '
	 */
	public Page createRow(String rowName) throws RQLException {

		// create
		com.hlcl.rql.as.List textList = getTextList();
		Page rowPg = textList.createAndConnectPage(getRowTemplate(), rowName);

		// remember
		rows.add(rowPg);

		// change current row
		currentRow = rowPg;

		return rowPg;
	}

	/**
	 * Hängt alle Zeilenseiten von dieser Tabelle ab. 
	 */
	public void deleteAllRows() throws RQLException {
		page.getListChildPages(textListTmpltElemName).deleteAll(true);
	}

	/**
	 * Hängt alle Zeilenseiten von dieser Tabelle ab. 
	 */
	public void disconnectAllRows() throws RQLException {
		page.getList(textListTmpltElemName).disconnectAllChilds();
	}

	/**
	 * Beendet das Überschreiben von Zeilenseiten (nicht notwendige werden gelöscht) mit StandardFieldText Werten.<p>
	 * Muss mit startOverwriting() begonnen werden!
	 * @see #startOverwriting()
	 */
	public void endOverwriting() throws RQLException {
		// delete unnecessary row pages
		for (int i = overwrittenRowsCounter; i < existingRowsSize; i++) {
			rows.getPage(i).delete();
		}
	}

	/**
	 * Liefert die Zeilen-Seite, dessen plain text Wert in Spalte column dem Wert dataValue gleich ist.
	 */
	private Page findRowByDataValue(int column, String dataValue) throws RQLException {

		for (int i = 0; i < rows.size(); i++) {
			Page row = (Page) rows.get(i);
			if (row.getStandardFieldTextValue(colTmpltElemNamePrefix + column + dataTmpltElemNameSuffix).equals(dataValue)) {
				return row;
			}
		}
		throw new ElementNotFoundException("No row page found, where plain text value in column " + column + " is equal value " + dataValue + ".");
	}

	/**
	 * Macht die erste Seite aus rows zur current row.
	 * @throws NoChildException	wird geworfen, falls Tabelle keine Zeilenseiten hat
	 */
	public void firstRow() throws RQLException {
		if (rows.isEmpty()) {
			throw new NoChildException("Text table page " + page.getHeadlineAndId() + " cannot set to first row, because there are no rows at all.");
		}
		currentRow = (Page) rows.get(0);
	}

	/**
	 * Liefert den Wert des plain text fields der gegebenen Spalte in der current row.
	 */
	public String getCurrentRowData(int column) throws RQLException {

		return currentRow.getStandardFieldTextValue(colTmpltElemNamePrefix + column + dataTmpltElemNameSuffix);
	}

	/**
	 * Liefert den formatted text aus der gegebenen Spalte für die current row.
	 */
	public String getCurrentRowText(int column) throws RQLException {

		return currentRow.getTextValue(colTmpltElemNamePrefix + column + textTmpltElemNameSuffix);
	}

	/**
	 * Liefert eine neue Instanz für die gegebenen Seite. Es werden die gleichen Templatenamen benutzt.
	 */
	public HipTextTablePage getNewInstanceFor(Page newPage) throws RQLException {

		HipTextTablePage tablePg = new HipTextTablePage(newPage, headerTmpltElemNameSuffix, widthTmpltElemNameSuffix, colTmpltElemNamePrefix, tableGridlineTmpltElemName);
		tablePg.setRowTemplateNames(textListTmpltElemName, contentTmpltFldrName, rowTmpltName, rowHeadlinePrefix, dataTmpltElemNameSuffix, textTmpltElemNameSuffix, colspanTmpltElemNameSuffix);

		return tablePg;
	}

	/**
	 * Liefert die Parameterseite aus hip.hlcl.com für alle Templatenamen.
	 */
	public String getParmPageId() throws RQLException {
		return "321080";
	}

	/**
	 * Liefert das Template für die Zeile zurück.
	 */
	private Template getRowTemplate() throws RQLException {

		if (rowTemplate == null) {
			TemplateFolder contentTpltFldr = page.getProject().getTemplateFolderByName(contentTmpltFldrName);
			rowTemplate = contentTpltFldr.getTemplateByName(rowTmpltName);
		}

		return rowTemplate;
	}

	/**
	 * Liefert das Listenelement an dem alle Zeilenseiten hängen.
	 */
	private List getTextList() throws RQLException {

		if (textListCache == null) {
			textListCache = page.getList(textListTmpltElemName);
		}

		return textListCache;
	}

	/**
	 * Liest alle Zeilenseiten der Tabelle und setzt den Pointer auf die erste Zeile der Tabelle, falls möglich.
	 */
	private void initializeRows() throws RQLException {
		rowElementValuePairs = new HashMap();
		// get all childs 
		rows = page.getListChildPages(textListTmpltElemName);
		if (rows.size() != 0) {
			firstRow();
		}
	}

	/**
	 * Macht die nächste Zeilenseite zur current row.
	 * Liefert true, falls möglich, false, falls keine Seite mehr vorhanden.
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
	 * Überschreibt eine Zeile und setzt den Standard-Text in Spalte 1.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page overwriteDataRow(String data1) throws RQLException {
		return overwriteDataRow(data1, null, null, null, null, null, null, null, null, null);
	}

	/**
	 * Überschreibt eine Zeile und setzt den Standard-Text bis Spalte 2.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page overwriteDataRow(String data1, String data2) throws RQLException {
		return overwriteDataRow(data1, data2, null, null, null, null, null, null, null, null);
	}

	/**
	 * Überschreibt eine Zeile und setzt den Standard-Text bis Spalte 3.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page overwriteDataRow(String data1, String data2, String data3) throws RQLException {
		return overwriteDataRow(data1, data2, data3, null, null, null, null, null, null, null);
	}

	/**
	 * Überschreibt eine Zeile und setzt den Standard-Text bis Spalte 4.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page overwriteDataRow(String data1, String data2, String data3, String data4) throws RQLException {
		return overwriteDataRow(data1, data2, data3, data4, null, null, null, null, null, null);
	}

	/**
	 * Überschreibt eine Zeile und setzt den Standard-Text bis Spalte 5.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page overwriteDataRow(String data1, String data2, String data3, String data4, String data5) throws RQLException {
		return overwriteDataRow(data1, data2, data3, data4, data5, null, null, null, null, null);
	}

	/**
	 * Überschreibt eine Zeile und setzt den Standard-Text bis Spalte 6.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page overwriteDataRow(String data1, String data2, String data3, String data4, String data5, String data6) throws RQLException {
		return overwriteDataRow(data1, data2, data3, data4, data5, data6, null, null, null, null);
	}

	/**
	 * Überschreibt eine Zeile und setzt den Standard-Text bis Spalte 7.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page overwriteDataRow(String data1, String data2, String data3, String data4, String data5, String data6, String data7) throws RQLException {
		return overwriteDataRow(data1, data2, data3, data4, data5, data6, data7, null, null, null);
	}

	/**
	 * Überschreibt eine Zeile und setzt den Standard-Text bis Spalte 8.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page overwriteDataRow(String data1, String data2, String data3, String data4, String data5, String data6, String data7, String data8) throws RQLException {
		return overwriteDataRow(data1, data2, data3, data4, data5, data6, data7, data8, null, null);
	}

	/**
	 * Überschreibt eine Zeile und setzt den Standard-Text bis Spalte 9.
	 * Die neu erstellt Zeilenseite wird current row.
	 */
	public Page overwriteDataRow(String data1, String data2, String data3, String data4, String data5, String data6, String data7, String data8, String data9) throws RQLException {
		return overwriteDataRow(data1, data2, data3, data4, data5, data6, data7, data8, data9, null);
	}

	/**
	 * Überschreibt die Standardfelder Text beginnend bei 1 und endet beim ersten null-Wert.<p>
	 */
	public Page overwriteDataRow(String data1, String data2, String data3, String data4, String data5, String data6, String data7, String data8, String data9, String data10) throws RQLException {
		Page overwrittenPg = currentRow;
		// it is assumed that method firstRow() was called before
		// check, if create or update
		if (overwrittenRowsCounter < existingRowsSize) {
			// update only
			// prepare for loop handling
			String[] dataValues = new String[10];
			dataValues[0] = data1;
			dataValues[1] = data2;
			dataValues[2] = data3;
			dataValues[3] = data4;
			dataValues[4] = data5;
			dataValues[5] = data6;
			dataValues[6] = data7;
			dataValues[7] = data8;
			dataValues[8] = data9;
			dataValues[9] = data10;

			// collect column values
			for (int i = 0; i < dataValues.length; i++) {
				String dataValue = dataValues[i];
				if (dataValue != null) {
					setCurrentRowData(i + 1, dataValue);
				} else {
					// stop at first null
					break;
				}
			}
			// update row, call cms
			updateCurrentRow();
			nextRow();
		} else {
			// create new row page
			overwrittenPg = createDataRow(data1, data2, data3, data4, data5, data6, data7, data8, data9, data10);
		}
		overwrittenRowsCounter++;
		return overwrittenPg;
	}

	/**
	 * Markiert die Änderung des Tabellen-Grids. 
	 */
	public void selectTableGridline(String optionListValue) throws RQLException {

		Element elem = page.getOptionList(tableGridlineTmpltElemName);
		tableElementValuePairs.put(elem, optionListValue);
	}

	/**
	 * Markiert die Änderung des column spannings auf den gegebenen Wert für die current row.
	 */
	public void setCurrentRowColspan(int column, int colspanValue) throws RQLException {

		Element elem = currentRow.getStandardFieldNumericElement(colTmpltElemNamePrefix + column + colspanTmpltElemNameSuffix);
		rowElementValuePairs.put(elem, new Integer(colspanValue));
	}

	/**
	 * Markiert die Änderung der plain text fields auf den gegebenen Wert für die current row.
	 * Write a blank instead of an empty string, to clear the value, but keep the cell visible. 
	 */
	public void setCurrentRowData(int column, String dataValue) throws RQLException {

		Element elem = currentRow.getStandardFieldTextElement(colTmpltElemNamePrefix + column + dataTmpltElemNameSuffix);
		if (dataValue.length() == 0) {
			dataValue = " ";
		}
		rowElementValuePairs.put(elem, dataValue);
	}

	/**
	 * Ändert den formatted text auf den gegebenen Wert für die current row.
	 */
	public void setCurrentRowText(int column, String textValue) throws RQLException {

		currentRow.setTextValue(colTmpltElemNamePrefix + column + textTmpltElemNameSuffix, textValue);
	}

	/**
	 * Markiert die Änderung der Spaltenüberschrift auf den gegebenen Wert.
	 */
	public void setHeader(int column, String header) throws RQLException {

		Element elem = page.getStandardFieldTextElement(colTmpltElemNamePrefix + column + headerTmpltElemNameSuffix);
		tableElementValuePairs.put(elem, header);
	}

	/**
	 * Vor dem Zugriff auf Zeilen, muss mit dieser Methode die Template- und Templateelement-Namen gesetzt werden.
	 */
	public void setRowTemplateNames(String textListTmpltElemName, String contentTmpltFldrName, String rowTmpltName, String rowHeadlinePrefix, String dataTmpltElemNameSuffix,
			String textTmpltElemNameSuffix, String colspanTmpltElemNameSuffix) throws RQLException {

		this.textListTmpltElemName = textListTmpltElemName;
		this.contentTmpltFldrName = contentTmpltFldrName;
		this.rowTmpltName = rowTmpltName;
		this.rowHeadlinePrefix = rowHeadlinePrefix;
		this.dataTmpltElemNameSuffix = dataTmpltElemNameSuffix;
		this.textTmpltElemNameSuffix = textTmpltElemNameSuffix;
		this.colspanTmpltElemNameSuffix = colspanTmpltElemNameSuffix;

		// initialize rows collection
		initializeRows();
	}

	/**
	 * Markiert die Änderung der Spaltenbreite auf den gegebenen Wert.
	 */
	public void setWidth(int column, int width) throws RQLException {

		Element elem = page.getStandardFieldNumericElement(colTmpltElemNamePrefix + column + widthTmpltElemNameSuffix);
		tableElementValuePairs.put(elem, new Integer(width));
	}

	/**
	 * Markiert die Änderung der Spaltenbreite auf den gegebenen Wert.
	 */
	public void setWidth(int column, String width) throws RQLException {

		setWidth(column, Integer.parseInt(width));
	}

	/**
	 * Started das Überschreiben von Zeilenseiten (neue werden bei Bedarf angelegt) mit StandardFieldText Werten.<p>
	 * Muss mit endOverwriting() abgeschlossen werden!
	 * @see #endOverwriting()
	 */
	public void startOverwriting() throws RQLException {
		// remember and initialize
		existingRowsSize = rows.size();
		overwrittenRowsCounter = 0;
	}

	/**
	 * Schreibt die vorgesehenen Änderungen an der current row auf den RD Server.
	 */
	public void updateCurrentRow() throws RQLException {

		currentRow.setElementValues(rowElementValuePairs);
		rowElementValuePairs.clear();
	}

	/**
	 * Ändert in der Zeilenseite, den plain text value in Spalte column von oldValue auf newValue.
	 */
	public void updateRowFindByDataValue(int column, String oldValue, String newValue) throws RQLException {

		Page row = findRowByDataValue(column, oldValue);
		row.setStandardFieldTextValue(colTmpltElemNamePrefix + column + dataTmpltElemNameSuffix, newValue);
	}

	/**
	 * Schreibt die vorgesehenen Änderungen an der tabelle auf den RD Server.
	 */
	public void updateTable() throws RQLException {

		page.setElementValues(tableElementValuePairs);
		tableElementValuePairs.clear();
	}
}
