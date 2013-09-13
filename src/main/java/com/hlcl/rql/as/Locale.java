package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt ein Gebietsschema.
 * 
 * @author LEJAFR
 */
public class Locale implements CmsClientContainer {

	private CmsClient cmsClient;
	private String countryName;
	private String languageId;
	private String languageName;
	private String lcid;

	/**
	 * Vollständiger Konstruktor mit allen Attributen.
	 * 
	 * @param client	the cms client instance
	 * @param localeId		locale ID, z.B. 2057, 1031
	 * @param languageId	ENG, DEU
	 * @param countryName	Englisch, German
	 * @param languageName	United Kingdom, Germany
	 */
	public Locale(CmsClient client, String localeId, String languageId, String countryName, String languageName) {
		super();

		this.cmsClient = client;
		this.lcid = localeId;
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
	 * @return Returns the languageId.
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
	 * @return Returns the lcid = locale ID.
	 */
	public String getLcid() {
		return lcid;
	}

	/**
	 * @return Returns the locale ID.
	 */
	public String getLocaleId() {
		return getLcid();
	}

	/**
	 * Liefert die RedDot logon GUID des users unter dem das script läuft. 
	 */
	public String getLogonGuid() {
		return getCmsClient().getLogonGuid();
	}

	/**
	 * Liefert alle relevanten infos für diese locale.
	 */
	public String toString() {
		return getLcid() + " " + getLanguageId() + " " + getLanguageName();
	}
}
