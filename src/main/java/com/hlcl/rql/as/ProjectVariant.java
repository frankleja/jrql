package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt eine Projektvariante.
 * 
 * @author LEJAFR
 */
public class ProjectVariant implements ProjectContainer {
	private String name;

	private Project project;
	private String projectVariantGuid;

	/**
	 * constructor comment.
	 */
	public ProjectVariant(Project project, String projectVariantGuid, String name) {
		super();

		this.project = project;
		this.projectVariantGuid = projectVariantGuid;
		this.name = name;
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
	 * Zwei Projektvariantenobjekte werden als identisch interpretiert, falls beide die gleiche GUID haben.
	 * 
	 * @param obj
	 *            the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
	 * @see java.lang.Boolean#hashCode()
	 * @see java.util.Hashtable
	 */
	public boolean equals(Object obj) {

		ProjectVariant second = (ProjectVariant) obj;
		return this.getProjectVariantGuid().equals(second.getProjectVariantGuid());
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
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
	 * Liefert den Namen dieses Exportpaketes.
	 * 
	 * @return java.lang.String
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
	 * Liefert die GUID dieser Projektvariante.
	 * 
	 * @return java.lang.String
	 */
	public String getProjectVariantGuid() {
		return projectVariantGuid;
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getProject().getSessionKey();
	}

	public int hashCode() {
		return getProjectVariantGuid().hashCode();
	}

	/**
	 * Show name for easier debugging.
	 */
	public String toString() {
		return getClass().getName() + "(" + getName() + ")";
	}
}
