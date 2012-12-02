package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt eine die Zuordnung zwischen TemplateVariante und ProjektVariante für das Publishing.
 * Entspricht einer Zeile (pro Projektvariante) im Dialog Assign Project variants auf dem Templates Knoten der Content-Klasse.
 * 
 * @author LEJAFR
 */
public class TemplatePublishAssignment implements TemplateContainer {

	private ProjectVariant projectVariant;
	private boolean publish;
	private TemplateVariant templateVariant;
	/**
	 * constructor comment.
	 */
	public TemplatePublishAssignment(ProjectVariant projectVariant, TemplateVariant templateVariant, boolean publish) {
		super();

		this.projectVariant = projectVariant;
		this.templateVariant = templateVariant;
		this.publish = publish;
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
	 * Zwei TemplatePublishAssignment Objekte werden als identisch interpretiert, falls beide die gleiche GUID haben.
	 *
	 * @param   obj   the reference object with which to compare.
	 * @return  <code>true</code> if this object is the same as the obj
	 *          argument; <code>false</code> otherwise.
	 * @see     java.lang.Boolean#hashCode()
	 * @see     java.util.Hashtable
	 */
	public boolean equals(Object obj) {

		TemplatePublishAssignment second = (TemplatePublishAssignment) obj;
		return this.getTemplateVariantGuid().equals(second.getTemplateVariantGuid());
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
	 * Liefert die GUID der zugehörigen Projektvariante.
	 * 
	 * @return java.lang.String
	 */
	public String getProjectVariantGuid() {
		return projectVariant.getProjectVariantGuid();
	}
	/**
	 * Liefert den Namen der zugehörigen Projektvariante.
	 * 
	 * @return java.lang.String
	 */
	public String getProjectVariantName() {
		return projectVariant.getName();
	}
	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getProject().getSessionKey();
	}
	/**
	 * Liefert das Template zu dem dieses Element gehoert.
	 */
	public Template getTemplate() {

		return templateVariant.getTemplate();
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
	 * @return Returns the templateVariant.
	 */
	public TemplateVariant getTemplateVariant() {
		return templateVariant;
	}
	/**
	 * Liefert die GUID der zugehörigen Templatevariante.
	 * 
	 * @return java.lang.String
	 */
	public String getTemplateVariantGuid() {
		return templateVariant.getTemplateVariantGuid();
	}
	public int hashCode() {

		return getTemplateVariantGuid().hashCode();
	}
	/**
	 * Liefert true, falls die Kombination von Projektvariante und Sprachvariante publiziert werden soll.  
	 */
	public boolean isPublish() {
		return publish;
	}
	/**
	 * Ändert die Eigenschaft publish? für dieses Assignment.
	 */
	public void setPublish( boolean publish) throws RQLException {
		/* 
		V6.5 request
		<IODATA loginguid="8601812988C14A58BD35090DCFAAFE1E">
		<PROJECT sessionkey="1021834323P8KPp744v0A">
		<TEMPLATE guid="CE92011D1E7E407FAD74BE4E2E6C6C2F">
		<TEMPLATEVARIANTS>
		<TEMPLATEVARIANT guid="D597B5959E15497ABD79181603B25DD6">
		<PROJECTVARIANTS action="assign">
		<PROJECTVARIANT donotgenerate="0" donotusetidy="0" guid="9F922A0194F648C580367248A7C901A6" />
		<PROJECTVARIANT donotgenerate="0" donotusetidy="0" guid="A333ED0B60424DBDBB2CD64B8F8BA6F6" />
		</PROJECTVARIANTS>
		</TEMPLATEVARIANT>
		</TEMPLATEVARIANTS>
		</TEMPLATE>
		</PROJECT>
		</IODATA>
		V6.5 response
		<IODATA>
		</IODATA>
		*/

		// call CMS
		String rqlRequest =
			"<IODATA loginguid='"
				+ getLogonGuid()
				+ "' sessionkey='"
				+ getSessionKey()
				+ "'>"
				+ "<PROJECT>"
				+ "<TEMPLATE guid='"
				+ getTemplateGuid()
				+ "'>"
				+ "<TEMPLATEVARIANTS>"
				+ "<TEMPLATEVARIANT guid='"
				+ getTemplateVariantGuid()
				+ "'>"
				+ "<PROJECTVARIANTS action='assign'>"
				+ "<PROJECTVARIANT guid='"
				+ getProjectVariantGuid()
				+ "' donotgenerate='"
				+ StringHelper.convertTo01(!publish)
				+ "'/>"
				+ "</PROJECTVARIANTS>"
				+ "</TEMPLATEVARIANT>"
				+ "</TEMPLATEVARIANTS>"
				+ "</TEMPLATE>"
				+ "</PROJECT>"
				+ "</IODATA>";
		callCmsWithoutParsing(rqlRequest);
		
		// update local cache
		this.publish = publish;
	}
}
