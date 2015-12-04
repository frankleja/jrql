package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Diese Klasse beschreibt das Seitenelement Optionsliste.
 * 
 * @author lejafr
 */
public class OptionList extends Element {

	private String defaultSelectionGuid;
	private Map<String, OptionListSelection> selections = new HashMap<String, OptionListSelection>(); // maps editable values to selection objects

	/**
	 * OptionList constructor comment.
	 * 
	 * @param page
	 *            Seite, die diesen Container Link beinhaltet.
	 * @param templateElement
	 *            TemplateElement auf dem dieses Element basiert
	 * @param name
	 *            Name des Elements
	 * @param elementGuid
	 *            GUID dieses Elements
	 * @param value
	 *            GUID eines möglichen OptionListEntry
	 * @param defaultSelectionGuid
	 *            GUID des default OptionListEntry
	 */
	public OptionList(Page page, TemplateElement templateElement, String name, String elementGuid, String value, String defaultSelectionGuid) {
		super(page, templateElement, name, elementGuid, value);

		this.defaultSelectionGuid = defaultSelectionGuid;
	}

	/**
	 * Liefert
	 */
	void addSelection(String selectionGuid, String description, String value) {

		// create Selection object
		OptionListSelection selection = new OptionListSelection(this, selectionGuid, description, value, selectionGuid.equals(defaultSelectionGuid));

		// remember selection
		getSelections().put(value, selection);
	}

	/**
	 * Prüft die möglichen Werte an dieser Optionsliste.
	 * 
	 * @return -1, if size of possible selections and given values doesn't match
	 *         <p>
	 *         0, if all given values are exactly the possible values of this option list (values checked with equals)
	 *         <p>
	 *         1, if size matches, but values didn't
	 * 
	 */
	public int checkValues(java.util.List<String> expectedPossibleValues) throws RQLException {
		List<String> selectionValues = getSelectionValuesSorted();
		int svSize = selectionValues.size();
		int epvSize = expectedPossibleValues.size();

		// size based checks first
		if (svSize != epvSize) {
			return -1;
		}

		// check values using sorted lists
		Collections.sort(expectedPossibleValues);
		// check value by value
		for (int i = 0; i < selectionValues.size(); i++) {
			String sv = selectionValues.get(i);
			String epv = expectedPossibleValues.get(i);
			if (!sv.equals(epv)) {
				return 1;
			}
		}
		// size and all values equal
		return 0;
	}

	/**
	 * Konvertiert ein Objekt mit dem Value dieses Elements in einen String.
	 * 
	 * @param valuObj
	 *            der editierbare Wert des Eintrages (nicht die selectionGuid)
	 */
	String convertToStringValue(Object valueObj) {
		String v = (String) valueObj;
		OptionListSelection s = getSelection(v); 
		return s == null ? null : s.getSelectionGuid();
	}

	/**
	 * Liefert die aktuell gewählte Selection zurueck oder null, falls diese Optionsliste keine Selektion hat und auch im TemplateElement kein default
	 * definiert ist.
	 */
	public OptionListSelection getCurrentSelection() throws RQLException {
		String selectionGuid = super.getValue(); 
		if (selectionGuid == null) {
			return null;
		}
		Collection<OptionListSelection> values = getSelections().values();
		Iterator<OptionListSelection> iter = values.iterator();
		while (iter.hasNext()) {
			OptionListSelection selection = iter.next();
			if (selection.getSelectionGuid().equals(selectionGuid)) {
				return selection;
			}
		}
		throw new ElementNotFoundException("The selection with GUID " + selectionGuid + " for option list " + getName() + " in page "
				+ getPage().getHeadlineAndId() + " cannot be found. Maybe a page uses a value, which was removed from the list of possible values.");
	}

	/**
	 * Liefert den Wert des OptionList Elements dieser Seite, das auf dem gegebenen templateElement basiert oder null, falls weder diese Optionsliste
	 * einen Wert hat noch im Templateelement ein default gesetzt ist.
	 */
	public String getCurrentSelectionValue() throws RQLException {

		OptionListSelection selectionOrNull = getCurrentSelection();
		if (selectionOrNull == null) {
			return null;
		}
		return selectionOrNull.getValue();
	}

	/**
	 * Liefert die GUID des Vorgabewertes der Optonsliste.
	 */
	private String getDefaultSelectionGuid() {

		return defaultSelectionGuid;
	}

	/**
	 * Liefert die Selection mit dem Wert value oder null, falls nicht in der Map.
	 */
	private OptionListSelection getSelection(String value) {

		return (OptionListSelection) getSelections().get(value);
	}

	/**
	 * Liefert true, falls der gegebene Wert ein möglicher in dieser OptionsListe ist, checked with equals().
	 * 
	 * @see Map#containsKey(Object)
	 */
	public boolean containsValue(String value) {
		return getSelections().containsKey(value);
	}

	/**
	 * Liefert alle an dieser Optionsliste möglichen Anzeigewerte aus den selection objects.
	 */
	public java.util.List<String> getSelectionDescriptions() {
		Collection<OptionListSelection> selections = getSelections().values();
		java.util.List<String> result = new ArrayList<String>(getSelectionsSize());
		for (OptionListSelection selection : selections) {
			result.add(selection.getDescription());
		}
		return result;
	}

	/**
	 * Liefert die Collection mit den Selections zurueck.
	 */
	private Map<String, OptionListSelection> getSelections() {

		return selections;
	}

	/**
	 * Liefert die Anzahl der möglicher Auswahlen.
	 */
	public int getSelectionsSize() {
		return getSelections().size();
	}

	/**
	 * Liefert alle an dieser Optionsliste möglichen Werte aus den selection objects.
	 */
	public java.util.List<String> getSelectionValues() {
		Collection<OptionListSelection> selections = getSelections().values();
		java.util.List<String> result = new ArrayList<String>(getSelectionsSize());
		for (OptionListSelection selection : selections) {
			result.add(selection.getValue());
		}
		return result;
	}

	/**
	 * Liefert alle an dieser Optionsliste möglichen Werte aus den selection objects sortiert zurück.
	 */
	public java.util.List<String> getSelectionValuesSorted() {
		List<String> selectionValues = getSelectionValues();
		Collections.sort(selectionValues);
		return selectionValues;
	}

	/**
	 * Aendert den Wert der OptionList auf die Selection GUID des Selection-Objektes mit dem Wert selectionValue.
	 * <p>
	 * Der value wird in den HTML Source eingesetzt.
	 * 
	 * @param selectionValue
	 *            Wert der OptionListSelection auf den die OptionsListe geaendert werden soll (weder die GUID noch der dem Autor angezeigt Wert).
	 */
	public void select(String selectionValue) throws RQLException {

		OptionListSelection selectionOrNull = getSelection(selectionValue);

		if (selectionOrNull == null) {
			throw new InvalidOptionListSelectionValueException("There is no selection with value " + selectionValue + " for this option list "
					+ getName() + ".");
		}

		super.setValue(selectionOrNull.getSelectionGuid());
	}

	/**
	 * Aendert den Wert der Optionsliste auf den default Wert.
	 */
	public void selectDefault() throws RQLException {

		super.setValue(getDefaultSelectionGuid());
	}

	/**
	 * Aendert polymorph den Wert dieses Elements. Der Typ von valuObj muss zum Typ des Elementes passen.
	 * 
	 * @param valueObj
	 *            valueObj muss ein String mit einem möglichen Wert sein
	 */
	protected void setValue(Object valueObj) throws RQLException {

		select(convertToStringValue(valueObj));
	}

	/**
	 * Returns the value of the current selection as content element's value. Return an empty string, if no selection is choosen.
	 * 
	 * @see #getCurrentSelectionValue()
	 */
	public String getValueAsString() throws RQLException {
		String v = getCurrentSelectionValue();
		return v == null ? "" : v;
	}
}
