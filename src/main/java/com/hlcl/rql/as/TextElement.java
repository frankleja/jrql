package com.hlcl.rql.as;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Diese Klasse beschreibt ein RedDot Textelement (type=31 oder type=32). Folgende Konvertierungsmöglichkeiten werden nicht unterstützt:
 * <ul>
 * <li>Do not convert characters to HTML</li>
 * <li>CRLF -> &lt;BR&gt;</li>
 * </ul>
 * 
 * @author LEJAFR
 */
public class TextElement implements PageContainer, ContentElement {
	private String elementGuid;

	// this string is set, if the given string is empty
	// it has to be double!
	private final String EMPTY_VALUE = "&nbsp;&nbsp;";
	// pattern zur Suche nach verlinkten page GUIDs im Source; matcher.group(1) liefert nur die GUID
	private final String LINK_GUID_PATTERN = "\\[ioID\\](\\w+)[\"#]";

	private String name;
	private Page page;
	private TemplateElement templateElement;
	// {0}=guid of target page, {1}=anchor name(page id of block or row), {2}=visible link text
	private final String TEXT_EDITOR_LINK = "<a href=\"[ioID]{0}#{1}\">{2}</a>";
	private String textValue;

	/**
	 * Textelement (ASCII) constructor comment.
	 * 
	 * @param page
	 *            Seite, die dieses Textelement beinhaltet.
	 * @param templateElement
	 *            TemplateElement auf dem dieses Element basiert
	 * @param name
	 *            Name des Elements
	 * @param elementGuid
	 *            GUID dieses Elements
	 */
	public TextElement(Page page, TemplateElement templateElement, String name, String elementGuid) {

		super();

		this.page = page;
		this.templateElement = templateElement;
		this.name = name;
		this.elementGuid = elementGuid;
		this.textValue = null;
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getCmsClient().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert eine liste aller find strings aus findList, die in s vorkommen. Ist die zurückgegebene Liste leer, wurde nichts in s gefunden.
	 * <p>
	 * Checked case sensitive with indexOf().
	 * 
	 * @param findList
	 *            the list with strings to check for; for instance HLCL,Container Line,Container Linie
	 * @param delimiter
	 *            the ,
	 * @param caseSensitive
	 *            case sensitive search or not; to ignore case set to false
	 * @return a list of elements of findList
	 */
	public java.util.List<String> collectContainedText(String findList, String delimiter, boolean caseSensitive) throws RQLException {
		// because searched in HTML source code, find list needs to be html encoded
		return StringHelper.collectContainedText(getText(), findList, delimiter, caseSensitive, true);
	}

	/**
	 * Konvertiert einen durch linkMarkerStart und linkMarkerEnd markierten Text in einen Texteditor-Link.\n Der Link zeigt auf die targetPage an den
	 * Anchor targetAnchorPage.
	 */
	public void convertToLink(String linkMarkerStart, String linkMarkerEnd, Page targetPg, Page targetAnchorPg) throws RQLException {

		// build link
		Object args[] = new Object[3];
		args[0] = targetPg.getPageGuid();
		args[1] = targetAnchorPg.getPageId();
		args[2] = targetAnchorPg.getHeadline();
		String link = MessageFormat.format(TEXT_EDITOR_LINK, args);

		// fill in link HTML source code + save it
		String newValue = StringHelper.replaceText(getText(), StringHelper.escapeHTML(linkMarkerStart), StringHelper.escapeHTML(linkMarkerEnd), link);
		setText(newValue);
	}

	/**
	 * Löscht den Wert dieses TextElements.
	 */
	public void deleteValue() throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="FACCF252F3164BA0AAEB7F53120792B7" sessionkey="1021834323bMB6Qx16e48">
		 <ELT action="save" guid="DFF26D0E47E04D7F91549CE32AA6C9B5" value="#1021834323bMB6Qx16e48"/>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <ELT action="save" value="#1021834323bMB6Qx16e48" sessionkey="1021834323bMB6Qx16e48" dialoglanguageid="ENG" languagevariantid="ENG" defaultlanguagevariantid="ENG" tleflags="2228224" eltconvertmode="" eltconvert="" guid="DFF26D0E47E04D7F91549CE32AA6C9B5" type="1" pageguid="429E9E4C4DBA42F0885EA751A010440F"/>
		 </IODATA>
		 */

		// call CMS
		String sessionKey = getSessionKey();
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + sessionKey + "'>" + "<ELT action='save' guid='"
				+ getElementGuid() + "' value='#" + sessionKey + "'/>" + "</IODATA>";
		callCms(rqlRequest);

		// force new read with default value
		this.textValue = null;
	}

	/**
	 * Schreibt für ASCII Elemente genau den gegebenen Wert; genau gleich wie <code>setText</code>. Erhält für HTML Elemente alle eingegebenen
	 * Zeichen (< wird zu &lt;). Ein einzelnes blank (space) wird als Textwert geschrieben (zu &nbsp;).
	 * 
	 * @see #setText(String)
	 */
	public void enterText(String value) throws RQLException {

		// ascii do not need any conversion
		if (getTemplateElement().isAsciiText()) {
			setText(value);
		}

		// url encode before write
		if (getTemplateElement().isHtmlText()) {
			setText(value, true);
		}
	}

	/**
	 * Liefert eine Liste von GUIDs der Seiten, die im Quelltext als Ziel im href auftauchen, z.B. href="[ioID]0C5BFE26441D437599F613195538CC67"
	 * 
	 * @return java.util.List of Page GUIDs
	 */
	public java.util.List<String> getAllHrefPageGuids() throws RQLException {
		Pattern pattern = Pattern.compile(LINK_GUID_PATTERN);
		Matcher matcher = pattern.matcher(getText());

		java.util.List<String> guids = new ArrayList<String>();
		while (matcher.find()) {
			String guid = matcher.group(1);
			if (guid.length() != Page.PAGE_GUID_LENGTH) {
				throw new RQLException("Found page GUID " + guid + " has wrong length " + guid.length() + ", but should be " + Page.PAGE_GUID_LENGTH
						+ ". Text element is " + getName() + " in page  " + getPage().getHeadlineAndId() + ".");
			}
			guids.add(guid);
		}
		return guids;
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getPage().getCmsClient();
	}

	/**
	 * Liefert den ersten der markierten LinkTexte oder null, falls kein Linktext markiert ist.
	 */
	public String getConvertableLinkText(String linkMarkerStart, String linkMarkerEnd) throws RQLException {

		return StringHelper.getTextBetween(getText(), StringHelper.escapeHTML(linkMarkerStart), StringHelper.escapeHTML(linkMarkerEnd));
	}

	/**
	 * Liefert die RedDot GUID dieses Elements.
	 * 
	 * @return java.lang.String
	 */
	public String getElementGuid() {
		return elementGuid;
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {
		return getPage().getLogonGuid();
	}

	/**
	 * Liefert den Namen des Elements auf der Seite.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getName() {
		return name;
	}

	/**
	 * Liefert die Seite, die diesen MultiLink beinhaltet.
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
	 * Liefert das Template-Element zurück, auf dem dieses Element basiert.
	 */
	public TemplateElement getTemplateElement() {

		return templateElement;
	}

	/**
	 * Liefert den Namen des Template-Elemenst auf dem dieses Textelement basiert.
	 */
	public String getTemplateElementName() {
		return getTemplateElement().getName();
	}

	/**
	 * Liefert den Quellcode des Textelements für HTML Texte. Liefert genau den eingegebenen Text für ASCII Texte zurück.
	 */
	public String getText() throws RQLException {

		String result = getTextValue();
		if (!isValueEntered(result)) {
			result = getTemplateElement().getDefaultValue();
		}
		return result == null ? "" : result;
	}

	/**
	 * Liefert den Quellcode des Textelements für HTML Texte. Eingegebene <> werden als &lt;&gt; geliefert (wie gespeichert), Klammern wie < für Tags
	 * als <. Liefert genau den eingegebenen Text für ASCII Texte zurück.
	 * 
	 * @return java.lang.String
	 */
	private String getTextValue() throws RQLException {
		/* 
		 V5 request 
		 <IODATA loginguid="3C3E27E618F54168B815CA14FDEBBE3B" sessionkey="100825549Y683ccmbR41" format="1">
		 <ELT action="load" guid="A2FBF27B37294248B603BBDBF031F4B6" />
		 </IODATA> 
		 V5 response
		 main,hlinternal,manager,hlit,hlag,hlexternal,itexternal,customer,subcontractor
		 */

		if (textValue == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "' format='1'>"
					+ " <ELT action='load' guid='" + getElementGuid() + "'/>" + "</IODATA>";
			textValue = callCmsWithoutParsing(rqlRequest);
		}
		return textValue;
	}

	/**
	 * Liefert true, falls für dieses Textelement ein Vorgabe im Template definiert ist.
	 * <p>
	 */
	public boolean hasTemplateDefaultValue() throws RQLException {
		return getTemplateElement().hasDefaultValue();
	}

	/**
	 * Liefert true, falls dieses Textelement nur ASCII Text enthält.
	 */
	public boolean isAsciiText() {
		return getTemplateElement().isAsciiText();
	}

	/**
	 * Liefert sicherheitshalber false, da TextElemente die gemeinsame Änderung mit nur einem RQL nicht unterstützen.
	 * <p>
	 * Diese Methode sollte nicht aufgerufen werden, da TextElemente nicht in Page#setElementValues() verwendet werden.
	 * 
	 * @see Page#setElementValues(Map)
	 */
	public boolean isCombinedUpdateSupported() throws RQLException {
		return false;
	}

	/**
	 * Liefert true, falls dieses Textelement keinen Wert anzeigt.
	 * <p>
	 * Der Vorgabewert aus dem Template wird mit einbezogen.
	 */
	public boolean isEmpty() throws RQLException {
		return getTextValue().length() == 0;
	}

	/**
	 * Liefert true, falls dieses Textelement HTML Text enthält.
	 */
	public boolean isHtmlText() {
		return getTemplateElement().isHtmlText();
	}

	/**
	 * Liefert true, falls in dieses Textelement eine Benutzereingabe erfolgt ist.
	 * <p>
	 */
	public boolean isValueEntered() throws RQLException {
		return isValueEntered(getTextValue());
	}

	/**
	 * Liefert true, falls in dieses Textelement eine Benutzereingabe erfolgt ist.
	 * <p>
	 * Reduced reading value twice
	 */
	private boolean isValueEntered(String value) throws RQLException {
		return value.length() != 0;
	}

	/**
	 * Erstellt eine RD Referenz von diesem TextElement (als Source) zum gegebenen targetElement.
	 * <p>
	 * Achtung: Nur als Administrator aufrufbar!
	 */
	public void referenceTo(TextElement targetElement) throws RQLException {
		getProject().referenceElement(getElementGuid(), targetElement.getElementGuid(), "element");
	}

	/**
	 * Ersetzt den Text im Tag mit dem gegebenen Namen mit einem neuen Text, der auch Tags mit Formatierungen enthalten kann. Wir das gegebene Tag
	 * nicht gefunden, bleibt der Text unverändert.
	 * 
	 * @see StringHelper#replaceTagValue(String, String, String)
	 */
	public void replaceTagValue(String tagName, String newSourceCode) throws RQLException {
		setText(StringHelper.replaceTagValue(getText(), tagName, newSourceCode));
	}

	/**
	 * Schreibt für ASCII Elemente genau den gegebenen Wert.
	 * <p>
	 * Schreibt für HTML Elemente den gegebenen Sourcecode weg. Ein einzelnes blank (space) wird als Textwert geschrieben (zu &nbsp;).
	 * 
	 * @see #enterText(String)
	 */
	public void setText(String value) throws RQLException {

		setText(value, getTemplateElement().isAsciiText());
	}

	/**
	 * Schreibt für ASCII Elemente genau den gegebenen Wert.
	 * <p>
	 * Schreibt für HTML Elemente den gegebenen Sourcecode weg. Ein einzelnes blank (space) wird als Textwert geschrieben (zu &nbsp;).
	 */
	private void setText(String value, boolean urlEncode) throws RQLException {

		/* 
		 V5 request 
		 <IODATA loginguid="0FAB838BB44041B2A0DB59C39E35A868" sessionkey="100839556323c61L8X5Q">
		 <ELT action="save" guid="A2FBF27B37294248B603BBDBF031F4B6">
		 main,hlinternal,manager,hlit,hlag,hlexternal,itexternal,customer,subcontractor
		 </ELT>
		 </IODATA> 
		 V5 response
		 <IODATA>
		 <ELT action="save" sessionkey="100839556323c61L8X5Q" dialoglanguageid="ENG" languagevariantid="ENG" defaultlanguagevariantid="ENG" guid="A2FBF27B37294248B603BBDBF031F4B6" type="31" pageguid="CF703A87BC0D4824B1957BAA5C94F36F">
		 main,hlinternal,manager,hlit,hlag,hlexternal,itexternal,customer,subcontractor
		 </ELT>
		 </IODATA>
		 */

		// prepare request values
		// default is ascii text
		String rdValue = value;
		String formatAttribut = "";

		// treat an empty string as a blank, normally ignored from RD via RQL
		if (value.length() == 0) {
			rdValue = EMPTY_VALUE;
		}

		// escape html text
		if (getTemplateElement().isHtmlText()) {
			formatAttribut = "' format='1";
			rdValue = StringHelper.escapeHTML(rdValue);
		}

		// url encode ascii
		if (urlEncode) {
			rdValue = urlEncode(rdValue);
		}

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + formatAttribut + "'>"
				+ " <ELT action='save' guid='" + getElementGuid() + "'>" + rdValue + "</ELT>" + "</IODATA>";
		callCms(rqlRequest);

		// force re-read of text
		textValue = null;
	}

	/**
	 * URL encoding, wobei + wieder gegen space ersetzt wird.
	 */
	private String urlEncode(String s) throws RQLException {
		return URLEncoder.encode(s).replace('+', ' ');
	}

	/**
	 * Liefert den Quellcode des Textelements für HTML Texte. Liefert genau den eingegebenen Text für ASCII Texte zurück.
	 * @throws RQLException 
	 * @see #getText()
	 */
	public String getValueAsString() throws RQLException {
		return getText();
	}
	/**
	 * Überschreibt den Standardwert für bequemes Debugging.
	 * 
	 * @return String
	 */
	public String toString() {
		return this.getClass().getName() + " (" + getTemplateElementName() + ")";
	}
}
