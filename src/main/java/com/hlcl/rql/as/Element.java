package com.hlcl.rql.as;

import java.util.Map;

/**
 * Diese Klasse beschreibt ein RedDot Element einer Seite.
 * 
 * @author LEJAFR
 */
public abstract class Element implements PageContainer, ContentElement {
	private String elementGuid;
	private String name;

	private Page page;
	private TemplateElement templateElement;
	protected String value; // needs to be set in FileElement#setValue() as well

	/**
	 * Container constructor comment.
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
	public Element(Page page, TemplateElement templateElement, String name, String elementGuid, String value) {
		super();

		this.page = page;
		this.templateElement = templateElement;
		this.name = name;
		this.elementGuid = elementGuid;
		this.value = value;
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getCmsClient().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines
	 * Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Konvertiert ein Objekt mit dem Value dieses Elements in einen String.
	 * 
	 * @param valuObj
	 *            valueObj Objekt passend zum Elementtyp
	 */
	abstract String convertToStringValue(Object valueObj);

	/**
	 * Returns the content element's value converted into a string. Browse implementors to find default conversion.
	 */
	abstract public String getValueAsString() throws RQLException;

	/**
	 * Löscht den Wert dieses Elements. Funktioniert für alle StandardFelder (-text, -date, -numeric), Image- und Mediaelemente und
	 * OptionsListen.
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
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + sessionKey + "'>"
				+ "<ELT action='save' guid='" + getElementGuid() + "' value='#" + sessionKey + "'/>" + "</IODATA>";
		callCms(rqlRequest);

		// force new read with default value
		this.value = null;
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getPage().getCmsClient();
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
	 * Liefert das Template-Element auf dem dieses Seitenelement basiert.
	 */
	public TemplateElement getTemplateElement() {
		return templateElement;
	}

	/**
	 * Liefert den Namen des Template-Elemenst auf dem dieses Seitenelement basiert.
	 */
	public String getTemplateElementName() {
		return getTemplateElement().getName();
	}

	/**
	 * Liefert den Wert des Elemens (auch den Dateinamen eines Bildes). Liefert den Defaultwert aus dem Template, falls dort einer
	 * definiert ist. Liefert null, falls weder dieses Element einen Wert hat noch im Templateelement ein default gesetzt ist.
	 * 
	 * @return java.lang.String
	 */
	protected String getValue() throws RQLException {

		if (isValueEntered()) {
			return value;
		}
		return getTemplateElement().getDefaultValue();
	}

	/**
	 * Liefert true, falls für dieses Element ein Vorgabe im Template definiert ist.
	 * <p>
	 */
	public boolean hasTemplateDefaultValue() throws RQLException {
		return getTemplateElement().hasDefaultValue();
	}

	/**
	 * Liefert true, falls dieser Elementtyp die gemeinsame Änderung mit nur einem RQL unterstützt.
	 * <p>
	 * 
	 * @see Page#setElementValues(Map)
	 */
	public boolean isCombinedUpdateSupported() throws RQLException {
		return true;
	}

	/**
	 * Liefert true, falls dieses Element keinen Wert anzeigt.
	 * <p>
	 * Der Vorgabewert aus dem Template wird mit einbezogen.
	 */
	public boolean isEmpty() throws RQLException {
		String value = getValue();
		return value == null ? true : getValue().length() == 0;
	}

	/**
	 * Liefert true, falls in dieses Element eine Benutzereingabe erfolgt ist.
	 * <p>
	 */
	public boolean isValueEntered() throws RQLException {
		return value.length() != 0;
	}

	/**
	 * Aendert polymorph den Wert dieses Elements. Der Typ von valuObj muss zum Typ des Elementes passen.
	 * 
	 * @param valueObj
	 *            valueObj muss ein Object passend zum Typ dieses Elementes sein
	 */
	protected abstract void setValue(Object valueObj) throws RQLException;

	/**
	 * Aendert den Wert des Elements (auch den Bildnamen).
	 */
	protected void setValue(String value) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="087F79DA22DF4EF385A7A18FDBB238CD" sessionkey="421139875y5k2iP8GF85">
		 <ELEMENTS action="save">
		 <ELT guid="9A872444A0034A428EE1214C0BFC6EEF" value="telephone333.png"/>
		 </ELEMENTS>
		 </IODATA>
		 V5 response
		 <IODATA>9A872444A0034A428EE1214C0BFC6EEF</IODATA>
		 */

		// call CMS
		String sessionKey = getSessionKey();
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + sessionKey + "'>"
				+ "  <ELEMENTS action='save'>" + "      <ELT guid='" + getElementGuid() + "' value='"
				+ (value.length() == 0 ? "#" + sessionKey : StringHelper.escapeHTML(value)) + "'/>" + "  </ELEMENTS>" + "</IODATA>";
		callCms(rqlRequest);

		// change local copy too
		this.value = value;
	}

	/**
	 * Liefert den RQLNode für dieses Elements mit dem eigenen RQL ELT action=load.
	 */
	protected RQLNode readElementNode() throws RQLException {
		/* 
		 V9 request
		<IODATA loginguid="7D45996D34B9448EBF429336F5D1157D" sessionkey="7F7D8BC07E334ABFB68AE57A894B05A5">
		<ELT action="load" guid="2947CE7E59ED435AB259E4FB511A4E48" />
		</IODATA>
		 V9 response
		<IODATA>
		<ELT action="load" sessionkey="7F7D8BC07E334ABFB68AE57A894B05A5" dialoglanguageid="ENG" languagevariantid="ENG" ok="1" 
		eltlanguageindependent="0" suffix="pdf" guid="2947CE7E59ED435AB259E4FB511A4E48" 
		templateelementguid="EB51BA8773074AC9A1E32C9AA2C312CB" pageguid="D901C0DF6F734A2CBB968C445AA2F70D" eltflags="64" 
		eltrequired="0" eltdragdrop="1" islink="0" formularorderid="0" orderid="1" status="0" name="language_dependent_pdf_media" 
		eltname="language_dependent_pdf_media" aliasname="language_dependent_pdf_media" variable="language_dependent_pdf_media" 
		folderguid="E3FAC52B4C0A4D89840F47305833850A" type="38" elttype="38" templateelementflags="64" templateelementislink="0" 
		value="BFF_TPEB_2010_Q1.pdf" altname="" subdirguid="E3FAC52B4C0A4D89840F47305833850A" 
		reddotdescription="language dependent PDF; could be different for English, German, Chinese, Spanish " eltsrc="" 
		eltsrcsubdirguid="26B25BE612894941BBD4E075071E513E" editfileattributes="0" flags="16777280" manuallysorted="-1" 
		useconnection="1" projectguid="73671509FA5C43ED8FC4171AD0298AD2" userguid="226895CB642D475784CE8D3838CBD74F" pagestatus="1"/>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "  <ELT action='load' guid='" + getElementGuid() + "' /></IODATA>";
		return callCms(rqlRequest).getNode("ELT");
	}

	/**
	 * Aendert den Wert des Elementes nach einer Veränderung.
	 * 
	 * @param newValueObj
	 *            newValueObj neuer Wert des Elementes.
	 */
	void updateValue(Object newValueObj) {

		value = convertToStringValue(newValueObj);
	}

	/**
	 * Überschreibt den Standardwert für bequemes Debugging.
	 */
	public String toString() {
		return this.getClass().getName() + " (" + getTemplateElementName() + ")";
	}

	/**
	 * Liefert true, falls das Templateelement, auf dem dieses Element basiert, sprachvariantenabhängig ist, sonst false.
	 * <p>
	 * Attention: Returns default value true, if the attribute eltlanguageindependent is not found in RQL response at all. In old
	 * projects I discovered this problem, which (without this default value) could be solved only to open end save the resprective
	 * template element (which is inconvenient).
	 * <p>
	 * Attention: This property is available in headline elementes only in versions before 9.0.1.49. Moved to setting in content class.
	 * 
	 * @see TemplateElement#isLanguageVariantDependent()
	 */
	public boolean isTemplateElementLanguageVariantDependent() throws RQLException {
		return getTemplateElement().isLanguageVariantDependent();
	}

	/**
	 * Liefert true, falls das Templatelement, auf dem dieses Element basiert, sprachvariantenunabhängig ist, sonst false.
	 * <p>
	 * Attention: This property is available in headline elementes only in versions before 9.0.1.49. Moved to setting in content class.
	 * 
	 * @see TemplateElement#isLanguageVariantIndependent()
	 */
	public boolean isTemplateElementLanguageVariantIndependent() throws RQLException {
		return getTemplateElement().isLanguageVariantIndependent();
	}

}
