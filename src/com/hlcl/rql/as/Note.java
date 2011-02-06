package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt eine Bemerkung zu einer Seite.
 * 
 * @author LEJAFR
 */
public class Note implements PageContainer {

	private Page page;
	private String noteGuid;
	private String type;
	private String name;
	private String value;
	
	// constants
	private final String LINE_BREAK = "<BR>";
/**
 * Note constructor comment.
 *
 * @param	page	Seite, zu der diese Bemerkung gehört
 * @param	name	Name der Bemerkung
 * @param	type	Typ der Bemerkung (2=Text)
 * @param	noteGuid	GUID dieser Bemerkung
 * @param	value 	Text dieser Bemerkung
 */
public Note(Page page, String name, String type, String noteGuid, String value) {
	super();

	this.page = page;
	this.name = name;
	this.type = type;
	this.noteGuid= noteGuid;
	this.value = value;
}
/**
 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
 */
public RQLNode callCms(String rqlRequest) throws RQLException {
	return getCmsClient().callCms(rqlRequest);
}
/**
 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck.
 * Erforderlich für die Ermittlung des Werts eines Textelements.
 */
public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
	return getCmsClient().callCmsWithoutParsing(rqlRequest);
}
/**
 * Liefert den CmsClient.
 */
public CmsClient getCmsClient() {
	return getPage().getCmsClient();
}
/**
 * Liefert die RedDot logon GUID.
 */
public String getLogonGuid() {
	return getPage().getLogonGuid();
}
/**
 * Liefert den Namen dieser Bemerkung
 * 
 * @return java.lang.String
 */
public java.lang.String getName() {
	return name;
}
/**
 * Liefert die RedDot GUID dieser Bemerkung.
 * 
 * @return java.lang.String
 */
public String getNoteGuid() {
	return noteGuid;
}
/**
 * Liefert die Seite, zu der diese Bemerkung gehört.
 */
public Page getPage() {
	
	return page;
}
/**
 * Liefert die RedDot GUID der Seite.
 */
public String getPageGuid() {
	return getPage().getPageGuid();
}
/**
 * Liefert das Projekt. 
 */
public Project getProject() {
	return getPage().getProject();
}
/**
 * Liefert die RedDot GUID des Projekts. 
 */
public String getProjectGuid() throws RQLException {
	return getPage().getProjectGuid();
}
/**
 * Liefert den RedDot Session key.
 */
public String getSessionKey() {
	return getPage().getSessionKey();
}
/**
 * Liefert den Wert der Bemerkung mit <BR> für den Zeilenumbruch.
 * 
 * @return java.lang.String
 */
public String getValue() {
	return value;
}
/**
 * Liefert den Wert der Bemerkung. Zeilenumbrüche werden als \n konvertiert geliefert.
 * 
 * @return java.lang.String
 */
public String getEnteredText() {
	return getValue().replaceAll(LINE_BREAK, "\n");
}
/**
 * Aendert den Wert dieser Bemerkung. Value kann <BR> zur formatierung enhalten.
 */
public void setValue(String value) throws RQLException {
	/* 
	V5 request
	<IODATA loginguid="A1CF49EECD3B4496AB1EC82C55818CFE" sessionkey="37163615908l72364M28">
	<PAGE guid="792F72BCCE13424FA151B12DFDF203C2">
	<SUPPLEMENTS action="save">
	<SUPPLEMENT guid="412656C9F03C426CA2BB78C7A184D447" type="2" value="test" />
	</SUPPLEMENTS>
	</PAGE>
	</IODATA>
	V5 response
	<IODATA>ok
	</IODATA>
	*/

	// call CMS
	String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" +
						"<PAGE guid='" + getPageGuid() + "'>" +
						"<SUPPLEMENTS action='save'>" +
						"<SUPPLEMENT guid='" + getNoteGuid() + "' type='" + type + "' value='" + StringHelper.escapeHTML(value) + "'/>" +
						"</SUPPLEMENTS>" +
						"</PAGE>" +
						"</IODATA>";
	callCms(rqlRequest);

	// change local copy too
	this.value = value;
}
/**
 * Aendert den Wert dieser Bemerkung. Die Zeilenumbrüche werden zu <BR> konvertiert.
 */
public void enterText(String value) throws RQLException {
	setValue(value.replaceAll("\n", LINE_BREAK));
}
}
