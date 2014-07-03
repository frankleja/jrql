package com.hlcl.rql.as;


/**
 * Diese Klasse beschreibt ein RedDot Standardfeld (type=1).
 * 
 * @author LEJAFR
 */
public class StandardFieldTextElement extends Element {

	/**
	 * StandardField constructor comment.
	 * 
	 * @param	page	Seite, die diesen Container Link beinhaltet.
	 * @param	templateElement		TemplateElement auf dem dieses Element basiert
	 * @param	name	Name des Elements
	 * @param	elementGuid	GUID dieses Elements
	 * @param	value Wert des Elements, auch Dateiname eines Bildes 
	 */
	public StandardFieldTextElement(Page page, TemplateElement templateElement, String name, String elementGuid, String value) {
		super(page, templateElement, name, elementGuid, value);
	}

	/**
	 * Liefert eine liste aller find strings aus findList, die in s vorkommen. Ist die zurückgegebene Liste leer, wurde nichts in s gefunden.<p>
	 * Checked case sensitive with indexOf().
	 *
	 *@param	findList	the list with strings to check for; for instance HLCL,Container Line,Container Linie
	 *@param	delimiter	the ,
	 *@param	caseSensitive		case sensitive search or not; to ignore case set to false
	 *@return	a list of elements of findList 
	 */
	public java.util.List<String> collectContainedText(String findList, String delimiter, boolean caseSensitive) throws RQLException {
		// use text as it is
		return StringHelper.collectContainedText(getText(), findList, delimiter, caseSensitive);
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
	 * Erstellt eine RD Referenz von diesem StandardFieldTextElement (als Source) zum gegebenen Element.<p> 
	 * Achtung: Nur als Administrator aufrufbar! 
	 */
	public void referenceTo(StandardFieldTextElement targetElement) throws RQLException {
		getProject().referenceElement(getElementGuid(), targetElement.getElementGuid(), "element");
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
