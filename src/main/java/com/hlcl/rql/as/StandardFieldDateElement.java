package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt ein RedDot Standardfeld Date (type=5).
 * 
 * @author LEJAFR
 */
public class StandardFieldDateElement extends Element {

	// cache
	private ReddotDate date;

	/**
	 * Konvertiert ein Objekt mit dem Value dieses Elements in einen String.
	 * 
	 * @param valuObj
	 *            valueObj Objekt passend zum Elementtyp
	 */
	String convertToStringValue(Object valueObj) {
		if (valueObj == null) {
			return "";
		}
		return ((ReddotDate) valueObj).toMsDoubleString();
	}

	/**
	 * Liefert das Datum oder null, falls gar kein Datum gesetzt ist.
	 */
	public ReddotDate getDate() throws RQLException {

		if (date == null) {
			String value = super.getValue();
			if (value == null) {
				return null;
			}
			if (value.length() > 0) {
				date = new ReddotDate(value);
			}
		}
		return date;
	}

	/**
	 * Liefert das Datum in der Form 11 Sep 2006 oder null, falls gar kein Datum gesetzt ist.
	 */
	public String getDateFormatted() throws RQLException {

		ReddotDate date = getDate();
		if (date == null) {
			return null;
		}
		return ReddotDate.formatAsddMMMyyyy(date);
	}

	/**
	 * Liefert das Datum als formatierten Text oder null, falls gar kein Datum gesetzt ist.
	 */
	public String getDateFormatted(String formatPattern) throws RQLException {

		ReddotDate date = getDate();
		if (date == null) {
			return null;
		}
		return ReddotDate.format(date, formatPattern);
	}

	/**
	 * Aendert den Wert des Elements. Null löscht das Datum.
	 */
	public void setDate(ReddotDate date) throws RQLException {

		super.setValue(convertToStringValue(date));
	}

	/**
	 * Aendert polymorph den Wert dieses Elements. Der Typ von valuObj muss zum Typ des Elementes passen.
	 * 
	 * @param valueObj
	 *            valueObj muss ein ReddotDate sein
	 */
	protected void setValue(Object valueObj) throws RQLException {

		setDate((ReddotDate) valueObj);
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
	public StandardFieldDateElement(Page page, TemplateElement templateElement, String name, String elementGuid, String value) {
		super(page, templateElement, name, elementGuid, value);
	}

	/**
	 * Returns the date in format 11 Sep 2006 as content element's value.
	 * 
	 * @see #getDateFormatted()
	 */
	public String getValueAsString() throws RQLException {
		return getDateFormatted();
	}
}
