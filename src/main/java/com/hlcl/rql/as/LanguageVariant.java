package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt eine Sprachvariante.
 * 
 * @author LEJAFR
 */
public class LanguageVariant implements ProjectContainer {

    public static final String RQL_ELEMENT_NAME = "LANGUAGEVARIANT";

	private String languageVariantGuid;
	private String name;
	private String rfcLanguageCode; // e.g. en-US
	private boolean isMainLanguage;
	private String languageCode; // ENG, DEU, CHS, ESN

	private Project project;

	/**
	 * constructor comment.
	 */
	public LanguageVariant(Project project, String languageVariantGuid, String name, String rfcLanguageId, String isMainLanguage, String languageCode) {
		super();

		this.project = project;
		this.languageVariantGuid = languageVariantGuid;
		this.name = name;
		this.rfcLanguageCode = rfcLanguageId;
		this.isMainLanguage = "1".equals(isMainLanguage) ? true : false;
		this.languageCode = languageCode;
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getProject().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Zwei Sprachvariantenobjekte werden als identisch interpretiert, falls beide die gleiche GUID haben.
	 * 
	 * @param obj
	 *            the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
	 * @see java.lang.Boolean#hashCode()
	 * @see java.util.Hashtable
	 */
	public boolean equals(Object obj) {
		LanguageVariant second = (LanguageVariant) obj;
		return this.getLanguageVariantGuid().equals(second.getLanguageVariantGuid());
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	/**
	 * Liefert die GUID dieser Sprachvariante.
	 * 
	 * @return java.lang.String
	 */
	public String getLanguageVariantGuid() {
		return languageVariantGuid;
	}

	/**
	 * Liefert die RedDot logon GUID des users unter dem das script läuft. Dies ist nicht die des Users, falls er angemeldet ist!
	 * 
	 * @see <code>getOwnLoginGuid</code>
	 */
	public String getLogonGuid() {
		return getProject().getLogonGuid();
	}

	/**
	 * Liefert den Namen dieser Sprachvariante.
	 * 
	 * @return java.lang.String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Liefert den vollen RFC Language code (in RD als RFC Language ID bezeichnet), e.g. en-US
	 */
	public String getRfcLanguageCode() {
		return rfcLanguageCode;
	}

	/**
	 * Liefert die 2 Zeichen Language ID; die ersten beiden Zeichen des RFC Language codes, falls code en-US, dann wird en geliefert.
	 * 
	 * @see #getRfcLanguageCode()
	 */
	public String getRfcLanguageId() {
		return getRfcLanguageCode().split("-")[0];
	}

	/**
	 * Liefert die 2 Country code des Language codes; die letzten beiden Zeichen des RFC Language codes, falls code en-US, dann wird US geliefert.
	 * 
	 * @see #getRfcLanguageCode()
	 */
	public String getRfcLanguageCountry() {
		return getRfcLanguageCode().split("-")[1];
	}

	/**
	 * Liefert genau dann true, falls diese Sprachvariante die in RD als Main Language gekennzeichnete ist.
	 */
	public boolean isMainLanguage() {
		return isMainLanguage;
	}

	/**
	 * Liefert den von RD intern genutzten language code dieser Sprachvariante, z.B. ENG, DEU, CHS, ESN.
	 */
	public String getLanguageCode() {
		return languageCode;
	}

	/**
	 * Liefert das Project, zu dem diese Sprachvariante gehoert.
	 */
	public Project getProject() {

		return project;
	}

	/**
	 * Liefert die RedDot GUID des Projekts.
	 */
	public String getProjectGuid() throws RQLException {
		return getProject().getProjectGuid();
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getProject().getSessionKey();
	}

	public int hashCode() {

		return getLanguageVariantGuid().hashCode();
	}
	/**
	 * Show name for easier debugging.
	 */
	public String toString() {
		return getClass().getName() + "(" + getName() + ")";
	}
}
