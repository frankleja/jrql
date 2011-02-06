package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt eine Templatevariante, z.B. HTML, DEL_XML.
 * 
 * @author LEJAFR
 */
public class TemplateVariant implements TemplateContainer {
	// caches
	private RQLNode detailsNodeCache;
	private String templateCodeCache;

	private String name;
	private Template template;
	private String templateVariantGuid;

	/**
	 * constructor comment.
	 */
	public TemplateVariant(Template template, String templateVariantGuid, String name) {
		super();

		this.template = template;
		this.templateVariantGuid = templateVariantGuid;
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
	 * Liefert true, falls dieser TemplateCode das gegebene Element elementName verwendet.
	 */
	public boolean containsTemplateCode(String elementName) throws RQLException {
		return getTemplate().containsTemplateCode(getTemplateCode(), elementName);
	}

	/**
	 * Liefert true, falls dieser TemplateCode für das gegebene Element elementName einen roten Punkt enthält.
	 */
	public boolean containsTemplateCodeRedDot(String elementName) throws RQLException {
		return getTemplate().containsTemplateCodeRedDot(getTemplateCode(), elementName);
	}

	/**
	 * Zwei Variantenobjekte werden als identisch interpretiert, falls beide die gleiche GUID haben.
	 * 
	 * @param obj
	 *            the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
	 * @see java.lang.Boolean#hashCode()
	 * @see java.util.Hashtable
	 */
	public boolean equals(Object obj) {

		TemplateVariant second = (TemplateVariant) obj;
		return this.getTemplateVariantGuid().equals(second.getTemplateVariantGuid());
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	/**
	 * Liefert den RQLNode mit weiteren Information für diese Templatevariante zurueck.
	 * 
	 * @see #getTemplateCode()
	 */
	private RQLNode getDetailsNode() throws RQLException {

		/* 
		 V7.5 request
		 <IODATA loginguid="D43446BFDD51477EB1B8F79AC3C3FAA6" sessionkey="28D889F1770C42DC8E47349B81E412B6">
		 <PROJECT>
		 <TEMPLATE>
		 <TEMPLATEVARIANT action="load" guid="BDC2DC3DE93B44EEBB4492B50E9254A0" />
		 </TEMPLATE>
		 </PROJECT>
		 </IODATA>
		 V7.5 response
		 <IODATA>
		 <TEMPLATE languagevariantid="ENG" parentobjectname="PROJECT" useconnection="1" dialoglanguageid="ENG" guid="1E7BF9EE00EC41BAB62D1464995F6922" name="direct_linked_page" templaterights="2147483647">
		 <TEMPLATEVARIANT action="load" languagevariantid="ENG" dialoglanguageid="ENG" templaterights="2147483647" templateguid="1E7BF9EE00EC41BAB62D1464995F6922" flags="0" description="" fileextension="html" doxmlencode="0" nostartendmarkers="0" insertstylesheetinpage="0" containerpagereference="0" createdate="37858.4743055556" changeddate="39275.3884375" changeduserguid="4324D172EF4342669EAF0AD074433393" changedusername="lejafr" pdforientation="default" guid="BDC2DC3DE93B44EEBB4492B50E9254A0" name="HTML" draft="0" waitforrelease="0"><!IoRangePreExecute><!-- Bugfix nach Update auf RD 6.5 SP1 --><!/IoRangePreExecute>
		 template code
		 </TEMPLATEVARIANT>
		 </TEMPLATE>
		 </IODATA>
		 */

		// cache the node with page details information
		if (detailsNodeCache == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ "<PROJECT><TEMPLATE><TEMPLATEVARIANT action='load' guid='" + getTemplateVariantGuid() + "'/>"
					+ "</TEMPLATE></PROJECT></IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			detailsNodeCache = rqlResponse.getNode("TEMPLATEVARIANT");
		}
		return detailsNodeCache;
	}

	/**
	 * Liefert die Dateierweiterung für publizierte Dateien.
	 */
	public String getFileExtension() throws RQLException {

		return getDetailsNode().getAttribute("fileextension");
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
	public String getSessionKey() {
		return getProject().getSessionKey();
	}

	/**
	 * Liefert das Template zu dem dieses Element gehoert.
	 */
	public Template getTemplate() {

		return template;
	}

	/**
	 * Liefert den Template code für diese Templatevariante zurueck.
	 * 
	 * @see #getDetailsNode()
	 */
	public String getTemplateCode() throws RQLException {

		/* 
		 V7.5 request
		 <IODATA loginguid="D43446BFDD51477EB1B8F79AC3C3FAA6" sessionkey="28D889F1770C42DC8E47349B81E412B6">
		 <PROJECT>
		 <TEMPLATE>
		 <TEMPLATEVARIANT action="load" guid="BDC2DC3DE93B44EEBB4492B50E9254A0" />
		 </TEMPLATE>
		 </PROJECT>
		 </IODATA>
		 V7.5 response
		 <IODATA>
		 <TEMPLATE languagevariantid="ENG" parentobjectname="PROJECT" useconnection="1" dialoglanguageid="ENG" guid="1E7BF9EE00EC41BAB62D1464995F6922" name="direct_linked_page" templaterights="2147483647">
		 <TEMPLATEVARIANT action="load" languagevariantid="ENG" dialoglanguageid="ENG" templaterights="2147483647" templateguid="1E7BF9EE00EC41BAB62D1464995F6922" flags="0" description="" fileextension="html" doxmlencode="0" nostartendmarkers="0" insertstylesheetinpage="0" containerpagereference="0" createdate="37858.4743055556" changeddate="39275.3884375" changeduserguid="4324D172EF4342669EAF0AD074433393" changedusername="lejafr" pdforientation="default" guid="BDC2DC3DE93B44EEBB4492B50E9254A0" name="HTML" draft="0" waitforrelease="0"><!IoRangePreExecute><!-- Bugfix nach Update auf RD 6.5 SP1 --><!/IoRangePreExecute>
		 template code
		 </TEMPLATEVARIANT>
		 </TEMPLATE>
		 </IODATA>
		 */

		// cache the template code
		if (templateCodeCache == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ "<PROJECT><TEMPLATE><TEMPLATEVARIANT action='load' guid='" + getTemplateVariantGuid() + "'/>"
					+ "</TEMPLATE></PROJECT></IODATA>";
			String rqlResponse = callCmsWithoutParsing(rqlRequest);
			templateCodeCache = StringHelper.unescapeHTML(StringHelper.getTextBetweenTag(rqlResponse, "TEMPLATEVARIANT"));
		}
		return templateCodeCache;
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
	 * Liefert die GUID dieser Templatevariante.
	 * 
	 * @return java.lang.String
	 */
	public String getTemplateVariantGuid() {
		return templateVariantGuid;
	}

	/**
	 * Wird auf die GUID zurückgeführt.
	 */
	public int hashCode() {

		return getTemplateVariantGuid().hashCode();
	}

	/**
	 * Ändert die Dateiendung dieser Templatevariante auf den gegebenen Wert.
	 */
	public void setFileExtension(String extension) throws RQLException {

		/* 
		 V7.5 request
		<IODATA loginguid="4700BAAF790F46ABB397700D1C31F33C" sessionkey="0116FBFBD7AA44DC944383F0ABA1A3C1">
		  <PROJECT>
		    <TEMPLATE>
		      <TEMPLATEVARIANT action="save" guid="FA828890A16842C6A69A15EDCF282029" fileextension="xml"/>
		    </TEMPLATE>
		  </PROJECT>
		</IODATA>  
		 V7.5 response
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PROJECT><TEMPLATE>"
				+ "<TEMPLATEVARIANT action='save' guid='" + getTemplateVariantGuid() + "' fileextension='" + extension + "' />"
				+ "</TEMPLATE></PROJECT></IODATA>";
		callCmsWithoutParsing(rqlRequest);

		// force new read
		detailsNodeCache = null;
	}

	/**
	 * Liefert true, falls dieser TemplateCode das gegebene Element elementName verwendet oder dafür einen roten Punkt enthält.
	 */
	public boolean usesTemplateCodeElement(String elementName) throws RQLException {
		return getTemplate().usesTemplateCodeElement(getTemplateCode(), elementName);
	}

	/**
	 * Liefert true, falls dieser TemplateCode das gegebene Element element verwendet oder dafür einen roten Punkt enthält.
	 */
	public boolean usesTemplateCodeElement(TemplateElement element) throws RQLException {
		return getTemplate().usesTemplateCodeElement(getTemplateCode(), element.getName());
	}
}
