package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt eine Oberflächensprache.
 * 
 * @author LEJAFR
 */
public class UserInterfaceLanguage implements CmsClientContainer {

	private CmsClient cmsClient;
	private String countryName;
	private String languageId;
	private String languageName;
	private String rfcLanguageId;

	/**
	 * Vollständiger Konstruktor mit allen Attributen.
	 * 
	 * @param client	the cms client instance
	 * @param rfcLanguageId		fr-ca, en-gb, de-de
	 * @param languageId	ENG, DEU
	 * @param countryName	Englisch, German
	 * @param languageName	United Kingdom, Germany
	 */
	public UserInterfaceLanguage(CmsClient client, String rfcLanguageId, String languageId, String countryName, String languageName) {
		super();

		this.cmsClient = client;
		this.rfcLanguageId = rfcLanguageId;
		this.languageId = languageId;
		this.countryName = countryName;
		this.languageName = languageName;
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
		return cmsClient;
	}

	/**
	 * @return Returns the countryName.
	 */
	public String getCountryName() {
		return countryName;
	}

	/**
	 * Zwei Oberflächensprachen werden als identisch interpretiert, falls beide die gleiche language ID haben.
	 *
	 * @param   obj   the reference object with which to compare.
	 * @return  <code>true</code> if this object is the same as the obj
	 *          argument; <code>false</code> otherwise.
	 */
	public boolean equals(Object obj) {

		UserInterfaceLanguage second = (UserInterfaceLanguage) obj;
		return this.getLanguageId().equals(second.getLanguageId());
	}

	/**
	 * @return Override for debugging.
	 */
	public String toString() {
		return this.getClass().getName() + "(" + getLanguageId() + ")";
	}

	/**
	 * @return Returns the languageId, e.g. DEU, ENG.
	 */
	public String getLanguageId() {
		return languageId;
	}

	/**
	 * @return Returns the languageName.
	 */
	public String getLanguageName() {
		return languageName;
	}

	/**
	 * Liefert die RedDot logon GUID des users unter dem das script läuft. 
	 *
	 */
	public String getLogonGuid() {
		return getCmsClient().getLogonGuid();
	}

	/**
	 * @return Returns the rfcLanguageId, e.g. de-de, fr-ca.
	 */
	public String getRfcLanguageId() {
		return rfcLanguageId;
	}
}
