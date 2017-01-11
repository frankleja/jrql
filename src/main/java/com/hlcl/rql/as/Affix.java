package com.hlcl.rql.as;


/**
 * Diese Klasse beschreibt ein Prefix oder Suffix eines Projektes. <p>
 * Er wird im CMS unter Administer Project Settings / General Settings / Prefixes & Suffixes gelistet.
 * 
 * @author LEJAFR
 */
public class Affix implements ProjectContainer {

	//constants
	protected final static String TREESEGMENT_TYPE = "project.6002";
	private String name;

	private Project project;
	private String affixGuid;

	/**
	 * constructor to create a prefix or suffix
	 */
	public Affix(Project project, String affixGuid, String name) throws RQLException {
		super();

		this.project = project;
		this.affixGuid = affixGuid;
		this.name = name;
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
		return getProject().getCmsClient();
	}

	/**
	 * Liefert die RedDot logon GUID des users unter dem das script läuft. 
	 * Dies ist nicht die des Users, falls er angemeldet ist!
	 *
	 *@see <code>getOwnLoginGuid</code>
	 */
	public String getLogonGuid() {
		return getProject().getLogonGuid();
	}

	/**
	 * Liefert den Namen, also den eigentlichen Wert zurück. Z.B. list_details_ für die content page.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Liefert das Project, zu dem dieser Folder gehoert.
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
	 * Liefert die GUID dieses Affixes.
	 */
	public String getAffixGuid() {

		return affixGuid;
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getProject().getSessionKey();
	}
}
