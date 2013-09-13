package com.hlcl.rql.as;


/**
 * Diese Klasse beschreibt ein RedDot Standardfeld user defined (type=999).
 * 
 * @author LEJAFR
 */
public class StandardFieldUserDefinedElement extends Element {

	/**
	 * StandardField constructor comment.
	 * 
	 * @param	page	Seite, die dieses Element beinhaltet.
	 * @param	templateElement		TemplateElement auf dem dieses Element basiert
	 * @param	name	Name des Elements
	 * @param	elementGuid	GUID dieses Elements
	 * @param	value Wert des Elements, auch Dateiname eines Bildes 
	 */
	public StandardFieldUserDefinedElement(Page page, TemplateElement templateElement, String name, String elementGuid, String value) {
		super(page, templateElement, name, elementGuid, value);
	}

	/**
	 * Konvertiert ein Objekt mit dem Value dieses Elements in einen String.
	 *
	 * @param valuObj valueObj Objekt passend zum Elementtyp
	 */
	String convertToStringValue(Object valueObj) {

		String value = (String) valueObj;
		return value;
	}

	/**
	 * Liefert den Wert des Feldes oder einen leeren String,
	 * falls weder dieses Element einen Wert hat noch im TemplateElement ein default definiert ist.
	 */
	public String getText() throws RQLException {

		String value = super.getValue();
		if (value == null) {
			return "";
		}
		return getValue();
	}

	/**
	 * Liefert den Wert des Feldes oder null,
	 * falls weder dieses Element einen Wert hat noch im TemplateElement ein default definiert ist.
	 */
	protected String getValue() throws RQLException {

		return super.getValue();
	}

	/**
	 * Aendert den Wert des Elements.
	 * Achtung: Die festgelegte JavaScript RegEx kann hier nicht geprüft werden!
	 * Achtung: Ist value.length() = 0, wird der Wert nicht gelöscht! Tatsächlich ignoriert RD diesen RQL request.
	 * @see	Element#deleteValue()
	 */
	public void setText(String value) throws RQLException {

		setValue(value);
	}

	/**
	 * Aendert polymorph den Wert dieses Elements. 
	 * Der Typ von valuObj muss zum Typ des Elementes passen.
	 *
	 * @param valueObj valueObj muss ein String sein
	 */
	protected void setValue(Object valueObj) throws RQLException {

		setValue(convertToStringValue(valueObj));
	}

	/**
	 * Aendert den Wert des Elements.
	 */
	protected void setValue(String value) throws RQLException {

		super.setValue(value);
	}

	/**
	 * Returns the text value as content element's value.
	 * 
	 * @see #getText()
	 */
	public String getValueAsString() throws RQLException {
		return getText();
	}
}
