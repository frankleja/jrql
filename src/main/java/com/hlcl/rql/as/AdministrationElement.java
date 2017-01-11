package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt ein Administrationselement (Workflow Kommentare).
 * 
 * @author LEJAFR
 */
public class AdministrationElement implements TemplateContainer {

	private Template template;
	private String name;
	private String administrationElementGuid;
	private int type;

	/**
	 * Erzeugt ein neues Templateelement.
	 * 
	 * @param template
	 *            Das Template in dem dieses Element benutzt wird.
	 * @param name
	 *            Name dieses TemplateElements
	 * @param administrationElementGuid
	 *            ReDot GUID dieses AdministrationsElements
	 * @param type
	 *            RedDot Typ dieses Elements (1 oder 2)
	 */
	public AdministrationElement(Template template, String name, String administrationElementGuid, String type) {
		super();

		this.template = template;
		this.name = name;
		this.administrationElementGuid = administrationElementGuid;
		this.type = Integer.parseInt(type);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getTemplateFolder().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert die GUID des Administrations-Elements.
	 * 
	 * @return String
	 */
	public String getAdministrationElementGuid() {
		return administrationElementGuid;
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getTemplateFolder().getCmsClient();
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {
		return getTemplate().getLogonGuid();
	}

	/**
	 * Liefert den Namen des Administrations-Elements.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Liefert das Projekt.
	 */
	public Project getProject() {
		return getTemplateFolder().getProject();
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
	public java.lang.String getSessionKey() {
		return getTemplate().getSessionKey();
	}

	/**
	 * Liefert das Template zu dem dieses Element gehoert.
	 */
	public Template getTemplate() {

		return template;
	}

	/**
	 * Liefert den Template-Folder, in dem dieses Template enthalten ist.
	 * 
	 * @return TemplateFolder
	 */
	public TemplateFolder getTemplateFolder() {
		return getTemplate().getTemplateFolder();
	}

	/**
	 * Liefert den Template-Folder GUID, in dem dieses Template enthalten ist.
	 * 
	 * @return TemplateFolder
	 */
	public String getTemplateFolderGuid() {
		return getTemplate().getTemplateFolderGuid();
	}

	/**
	 * Liefert die GUID des Templates vom Container.
	 */
	public java.lang.String getTemplateGuid() {
		return getTemplate().getTemplateGuid();
	}

	/**
	 * Liefert den Typ des Administrations-Elements.
	 */
	public String getType() {

		return Integer.toString(type);
	}
}
