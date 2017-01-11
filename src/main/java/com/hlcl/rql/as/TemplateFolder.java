package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Diese Klasse beschreibt einen RedDot Template Folder.
 * 
 * @author LEJAFR
 */
public class TemplateFolder implements ProjectContainer {
	private String name;

	private Project project;
	private String templateFolderGuid;

	// cache
	private RQLNodeList templateNodeList;

	/**
	 * Erzeugt einen neuen TemplateFolder für das gegebenen Projekt.
	 *
	 * @param project	Projekt, zu dem dieser TemplateFolder gehört
	 * @param name		Name dieses TemplateFolders
	 * @param templateFolderGuid	RedDot GUID dieses Verzeichnises
	 */
	public TemplateFolder(Project project, String name, String templateFolderGuid) {
		super();

		this.project = project;
		this.name = name;
		this.templateFolderGuid = templateFolderGuid;
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getProject().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck.
	 * Erforderlich für die Ermittlung des Werts eines Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert true, wenn das gegebene Template in diesem Folder existiert, sonst false.
	 * Berücksichtigt Berechtigungen an Templates.
	 */
	public boolean contains(Template template) throws RQLException {

		return getTemplates().contains(template);
	}

	/**
	 * Liefert true, wenn es in diesem Ordner ein Template mit dem gegebenen Namen gibt, sonst false.<p>
	 * Berücksichtigt Berechtigungen an Templates: Ist ein Template für den connected user nicht zugelassen, wird false geliefert auch wenn es in diesem folder enthalten ist.
	 */
	public boolean containsByName(String templateName) throws RQLException {
		return getTemplateNames().contains(templateName);
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	/**
	 * Liefert die Logon GUID vom Container.
	 * 
	 * @see		Project
	 */
	public String getLogonGuid() {

		return getProject().getLogonGuid();
	}

	/**
	 * Liefert den Namen des Template folders.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Liefert das Project, den Container des Folders.
	 *
	 * @see		Project
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

	/**
	 * Liefert das Template mit dem gegebenen Namen vom CMS zurück.
	 *  
	 * @param	name Name des Templates.
	 * @return	Template	
	 * @see		Template  
	 */
	public Template getTemplateByName(String name) throws RQLException {

		RQLNodeList templateList = getTemplateNodeList();
		
		if (templateList != null) {
			for (RQLNode templateNode : templateList) {
				if (templateNode.getAttribute("name").equals(name)) {
					// wrap template data
					return new Template(this, name, templateNode.getAttribute("guid"), templateNode.getAttribute("description"));
				}
			}
		}

		throw new ElementNotFoundException("Template named " + name + " could not be found in the template folder " + getName() + ".");
	}

	/**
	 * Liefert die GUID dieses TemplateFolders.
	 * 
	 * @return String
	 */
	public String getTemplateFolderGuid() {
		return templateFolderGuid;
	}

	
	private RQLNodeList getTemplateNodeList() throws RQLException {
		// check cache first
		if (templateNodeList == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
			+ "<TEMPLATES action='list' folderguid='" + getTemplateFolderGuid() + "' />"
			+ "</IODATA>"; // FIXME: all='1' is not sure
			RQLNode rqlResponse = callCms(rqlRequest);
			templateNodeList = rqlResponse.getNodes("TEMPLATE");
		}
		return templateNodeList;
	}


	/**
	 * Liefert die RQLNodeList mit allen Templates dieses Folders zurück.
	 *  
	 * @return	RQLNodeList
	 * @see		RQLNodeList
	 */
	private RQLNodeList getTemplateNodeList_old() throws RQLException {

		/* 
		 V5 request
		 <IODATA loginguid="2A51E6531D1A4D02935BBCF4CD05A3EA" sessionkey="421138853e8oC524a665">
		 <TEMPLATELIST action="load" folderguid="6A6740BC44F7459081BFD1F25B1BF8F6"/>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <TEMPLATELIST>
		 <TEMPLATE guid="B7D1E958FA1F4987BA51A6273CC8A497" name="data_table_block" description="a table with up to 10 columns for data" folderguid="6A6740BC44F7459081BFD1F25B1BF8F6" selectinnewpage="0" />
		 <TEMPLATE guid="79D69F29A4394B71BC3A8930C689D7D8" name="data_table_row" description="one row of the data table" folderguid="6A6740BC44F7459081BFD1F25B1BF8F6" selectinnewpage="0" />
		 <TEMPLATE guid="3ABD3D04DB8745069AE2BCEBD18C239E" name="download_block" description="a table block with downloadable files" folderguid="6A6740BC44F7459081BFD1F25B1BF8F6" selectinnewpage="0" />
		 ...
		 <TEMPLATE guid="1E7BF9EE00EC41BAB62D1464995F6922" name="welcome_page" description="for the Hip Welcome Page." folderguid="6A6740BC44F7459081BFD1F25B1BF8F6" selectinnewpage="0" />
		 </TEMPLATELIST>
		 </IODATA>
		 */

		// check cache first
		if (templateNodeList == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<TEMPLATELIST action='load' folderguid='" + getTemplateFolderGuid() + "' />"
					+ "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			templateNodeList = rqlResponse.getNodes("TEMPLATE");
		}
		return templateNodeList;
	}

	/**
	 * Liefert ein Set mit allen Templatenamen dieses Ordners. Berücksichtigt Berechtigungen an Templates.
	 */
	public Set<String> getTemplateNames() throws RQLException {
		Set<String> result = new HashSet<String>();
		for (Template template : getTemplates()) {
			result.add(template.getName());
		}
		return result;
	}
	/**
	 * Liefert eine Liste mit allen Templates dieses Ordners. Berücksichtigt Berechtigungen an Templates.
	 */
	public java.util.List<Template> getTemplates() throws RQLException {

		RQLNodeList templateNodeList = getTemplateNodeList();
		java.util.List<Template> templates = new ArrayList<Template>();
		if (templateNodeList == null) {
			return templates;
		}

		for (int i = 0; i < templateNodeList.size(); i++) {
			RQLNode templateNode = templateNodeList.get(i);
			templates.add(new Template(this, templateNode.getAttribute("name"), templateNode.getAttribute("guid"), templateNode.getAttribute("description")));
		}
		return templates;
	}
}
