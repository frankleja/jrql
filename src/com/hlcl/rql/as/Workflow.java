package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt einen Workflow. 
 * 
 * @author LEJAFR
 */
public class Workflow implements ProjectContainer {
	
	//constants
	protected final static String TREESEGMENT_TYPE = "project.1115";
	
	// cache
	private RQLNode detailsNode;

	private String name;
	private Project project;
	private String workflowGuid;
/**
 * constructor comment.
 */
public Workflow(Project project, String workflowGuid, String name) {
	super();

	this.project = project;
	this.workflowGuid = workflowGuid;
	this.name = name;
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
 * Liefert den CmsClient.
 */
public CmsClient getCmsClient() {
	return getProject().getCmsClient();
}
/**
 * Liefert den RQLNode mit weiteren Information für diesen Workflow zurueck. 
 * Unklare Beziehung zwischen Workflow und Language Variant (durch Language Code identifiziert).
 *
 */
private RQLNode getDetailsNode() throws RQLException {

    /* 
    V5 request
	<IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
	  <WORKFLOW action="load" guid="[!guid_workflow!]"/>
	</IODATA>
	V5 response 
	<IODATA>
	<WORKFLOW action="load" sessionkey="692084693571Ad873QsS" dialoglanguageid="ENG" languagevariantid="ENG" name="wf_old_layout_check" guid="82208942EB9F48EEA58854222EAFE2AA" inherit="1" global="0"/>
	</IODATA>
	*/

	// cache the node with details information
	if (detailsNode == null) {
	    // call CMS
	    String rqlRequest =
	        "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" +
	        " <WORKFLOW action='load' guid='" + getWorkflowGuid() + "'/>" + 
	        "</IODATA>";
	    RQLNode rqlResponse = callCms(rqlRequest);
		detailsNode = rqlResponse.getNode("WORKFLOW");
	}
	return detailsNode;
}
/**
 * Liefert den Language Code der Sprachvariante des Workflows.
 * 
 * @return java.lang.String
 */
public String getLanguageCode() throws RQLException {
	return getDetailsNode().getAttribute("languagevariantid");
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
 * Liefert den Namen dieses Workflows.
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
 * Liefert den RedDot Session key.
 */
public String getSessionKey() {
	return getProject().getSessionKey();
}
/**
 * Liefert die GUID dieses Workflows.
 * 
 * @return java.lang.String
 */
public String getWorkflowGuid() {
	return workflowGuid;
}
/**
 * Startet einen asynchronen Job, der diesen Workflow am gegebenen Link an alle Unter-MultiLinks vererbt. 
 * Es wird keine Mail an den Autor versendet!
 * 
 * ATTENTION: Dieses RQL funktioniert nur, falls in den Workflow properties die Funktion 'Inherit workflow' aktiviert wurde. 
 * Falls nicht, gibt es keinen Fehler, sondern der request wird einfach ignoriert. Sehr unschön!
 */
public void inherit(MultiLink multiLink) throws RQLException {

    /* 
    V6 request
	<IODATA loginguid="D146BD4786B4467F96D2E587701EEB3C" sessionkey="1021834323f26hNvlNvcN" component="workflow">
	<LINK guid="F45B174721B3483FA471B5E539706625">
	<WORKFLOW sendmail="0" guid="82208942EB9F48EEA58854222EAFE2AA" action="inherit"/>
	</LINK>
	</IODATA>
	V6 response 
	<IODATA>
	</IODATA>
	*/

    // call CMS
    String rqlRequest =
        "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "' component='workflow'>" +
		" <LINK guid='" + multiLink.getLinkGuid() + "'>" + 
        " <WORKFLOW action='inherit' guid='" + getWorkflowGuid() + "' sendmail='0'/>" + 
		" </LINK>" + 
        "</IODATA>";
    callCms(rqlRequest); // no result
}
}
