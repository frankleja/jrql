package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt ein RedDot Standardfeld Numeric (type=48).
 * 
 * @author LEJAFR
 */
public class StandardFieldNumericElement extends Element {

	/**
	 * Konvertiert ein Objekt mit dem Value dieses Elements in einen String.
	 * 
	 * @param valuObj
	 *            valueObj Objekt passend zum Elementtyp
	 */
	String convertToStringValue(Object valueObj) {

		return ((Integer) valueObj).toString();
	}

	/**
	 * Liefert den Wert dieses Feldes als String oder 0, falls weder dieses Element einen Wert hat, noch im TemplateElement ein default gesetzt ist.
	 */
	public String getIntAsString() throws RQLException {
		return Integer.toString(getInt());
	}

	/**
	 * Liefert den Wert dieses Feldes oder 0, falls weder dieses Element einen Wert hat, noch im TemplateElement ein default gesetzt ist.
	 */
	public int getInt() throws RQLException {

		String value = super.getValue();
		if (value == null) {
			return 0;
		}
		return Integer.parseInt(value);
	}

	/**
	 * Liefert den Defaultwert dieses Feldes aus dem Templateelement oder null, falls kein defaultwert vorhanden.
	 */
	public int getDefaultInt() throws RQLException {

		String value = getTemplateElement().getDefaultValue();
		if (value == null) {
			return 0;
		}
		return Integer.parseInt(value);
	}

	/**
	 * Aendert den Wert des Elements.
	 */
	public void setInt(int value) throws RQLException {

		super.setValue(Integer.toString(value));
	}

	/**
	 * Aendert den Wert des Elements.
	 */
	public void setInt(Integer value) throws RQLException {

		setInt(value.intValue());
	}

	/**
	 * Aendert polymorph den Wert dieses Elements. Der Typ von valuObj muss zum Typ des Elementes passen.
	 * 
	 * @param valueObj
	 *            valueObj muss ein Integerobjekt sein
	 */
	protected void setValue(Object valueObj) throws RQLException {

		setInt((Integer) valueObj);
	}

	/**
	 * StandardField constructor comment.
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
	 *            Wert des Elements, auch Dateiname eines Bildes
	 */
	public StandardFieldNumericElement(Page page, TemplateElement templateElement, String name, String elementGuid, String value) {
		super(page, templateElement, name, elementGuid, value);
	}

	/**
	 * Returns the int value converted into a string as content element's value.
	 * 
	 * @see #getIntAsString()
	 */
	public String getValueAsString() throws RQLException {
		return getIntAsString();
	}
}
