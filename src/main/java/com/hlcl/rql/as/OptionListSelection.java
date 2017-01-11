package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt einen moeglichen Wert einer Optionsliste.
 * 
 * @author LEJAFR
 */
public class OptionListSelection implements ElementContainer {

	private OptionList optionList;
	private String selectionGuid;
	private String value;
	private String description;
	private boolean isDefault;
/**
 * OptionListEntry constructor comment.
 */
public OptionListSelection(OptionList optionList, String selectionGuid, String description, String value, boolean isDefault) {
    super();

    this.optionList = optionList;
    this.selectionGuid = selectionGuid;
    this.description = description;
    this.value = value;
    this.isDefault = isDefault;
}
/**
 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
 */
public RQLNode callCms(String rqlRequest) throws RQLException {
	return getOptionList().callCms(rqlRequest);
}
/**
 * Liefert das Element..
 */
public Element getElement() {
	return getOptionList();
}
/**
 * Liefert die RedDot GUID der OptionList zu der diese Selection gehoert.
 */
public String getElementGuid() {
	return getOptionList().getElementGuid();
}
/**
 * Liefert die RedDot logon GUID.
 */
public String getLogonGuid() {
	return getOptionList().getLogonGuid();
}
/**
 * Liefert die OptionList zu der dieses Selection Object gehört.
 */
private OptionList getOptionList() {

	return optionList;
}
/**
 * Liefert die Seite zu der das Element gehoert.
 */
public Page getPage() {
	return null;
}
/**
 * Liefert die RedDot GUID der Seite.
 */
public String getPageGuid() {
	return getOptionList().getPageGuid();
}
/**
 * Liefert das Projekt. 
 */
public Project getProject() {
	return getOptionList().getProject();
}
/**
 * Liefert die RedDot GUID dieser OptionList Selection zurueck.
 */
public String getSelectionGuid() {

	return selectionGuid;
}
/**
 * Liefert den RedDot Session key.
 */
public String getSessionKey() {
	return getOptionList().getSessionKey();
}

/**
 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck.
 * Erforderlich für die Ermittlung des Werts eines Textelements.
 */
public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
	return getCmsClient().callCmsWithoutParsing(rqlRequest);
}

/**
 * Liefert den dem Autor angezeigten Wert dieses Eintrags (Name). 
 * 
 * @return java.lang.String
 */
public java.lang.String getDescription() {
	return description;
}

/**
 * Liefert die RedDot GUID des Projekts. 
 */
public String getProjectGuid() throws RQLException {
	return getOptionList().getProjectGuid();
}

/**
 * Liefert den im HTML Code eingesetzten Wert dieses Eintrags (Value).
 * 
 * @return java.lang.String
 */
public String getValue() {
	return value;
}

/**
 * Liefert true genau dann, wenn dieses Selection Objekt der Default ist.
 * 
 * @return boolean
 */
public boolean isDefault() {
	return isDefault;
}

/**
 * Liefert den CmsServer.
 */
public CmsClient getCmsClient() {
	return getOptionList().getCmsClient();
}
}
