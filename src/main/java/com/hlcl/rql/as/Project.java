package com.hlcl.rql.as;

import java.util.*;

import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.RqlKeywordObject;
import com.hlcl.rql.util.as.ScriptParameters;

/**
 * Diese Klasse beschreibt ein RedDot Projekt.
 * 
 * @author LEJAFR
 */
public class Project extends RqlKeywordObject implements CmsClientContainer {


	/**
	 * Iterator over all MultiLinks referencing the given GUID.
	 */
	private class MultiLinksReferencingIterator implements Iterator<MultiLink> {

		private java.util.List<MultiLink> chunk;
		private Iterator<MultiLink> it;
		private String pageOrLinkGuid;

		
		/**
		 * Constructor for the given page or link GUID.
		 * @throws RQLException 
		 */
		private MultiLinksReferencingIterator(String pageOrLinkGuid) throws RQLException {
			this.pageOrLinkGuid = pageOrLinkGuid;
			readChunk();
			it = chunk.iterator();
		}

		/**
		 * Liefert die page Nodelist, die auf die gegebene Seiten- oder Link-GUID (auch Frame) verweisen.
		 */
		private RQLNodeList getNodeList() throws RQLException {
			/*
			 * V7.5 request <IODATA loginguid="50EDF8D60DFD4B36BF77D857DC010973"
			 * sessionkey="2AF3C698AC4949A4A401D2171AA84D61"> <PROJECT>
			 * <REFERENCE action="list"
			 * guid="F5E6CF4E95B64135905489D57B2D4B26"/> </PROJECT> </IODATA>
			 * V7.5 response <IODATA> <PAGES> <PAGE
			 * guid="98B96C3E009E4DBD82F3F573F9ECA67F" id="5709"
			 * headline="Brochure Thank you!" > <LINKS> <LINK
			 * guid="565B179F4D5947DCB431BFD7F07AAFAE" type="28"
			 * languagevariantguid="" languagevariantid=""
			 * languagevariantname="" value="cont_leftNavigation" /> </LINKS>
			 * </PAGE> <PAGE guid="D9E033D1EE3B439E9A11166954065584" id="5710"
			 * headline="Thank you!" > <LINKS> <LINK
			 * guid="A3528D146E9348EC9E0237A041F5E772" type="28"
			 * languagevariantguid="" languagevariantid=""
			 * languagevariantname="" value="cont_leftNavigation" /> </LINKS>
			 * </PAGE> ... </PAGES> </IODATA>
			 */

			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'><PROJECT>"
					+ " <REFERENCE action='list' guid='" + pageOrLinkGuid + "'/></PROJECT></IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			return rqlResponse.getNodes("PAGE");
		}

		
		/**
		 * Returns true if more MultiLinks are available.
		 */
		public boolean hasNext() {
			return it.hasNext();
		}

		
		/**
		 * Returns the next MultiLink referencing the given GUID at the current position.
		 */
		public MultiLink next() {
			return it.next();
		}

		
		/**
		 * Read a chunk from RD and convert into list of multi links.
		 */
		private void readChunk() throws RQLException {
			// initializing
			chunk = new ArrayList<MultiLink>();

			// call CMS
			RQLNodeList pageNodes = getNodeList();

			// filter all multilinks only and wrap them
			if (pageNodes == null) {
				return;
			}

			for (int i = 0; i < pageNodes.size(); i++) {
				RQLNode pageNode = pageNodes.get(i);
				Page page = getPageByGuid(pageNode.getAttribute("guid"));
				RQLNodeList linkNodes = pageNode.getNodes("LINK");
				for (int j = 0; j < linkNodes.size(); j++) {
					RQLNode linkNode = linkNodes.get(j);
					int type = Integer.parseInt(linkNode.getAttribute("type"));
					if (type == TemplateElement.LIST_TYPE || type == TemplateElement.CONTAINER_TYPE) {
						try {
							MultiLink multiLink = page.getMultiLinkByGuid(linkNode.getAttribute("guid"));
							chunk.add(multiLink);
						} catch (ElementNotFoundException enfe) {
							// ignore not found links in the page; database
							// inconsistency grrrhh
						}
					}
				}
			}
		}

		/**
		 * Not implemented.
		 */
		public void remove() {
			throw new UnsupportedOperationException(
					"The iterator to find all MultiLink referencing an element did not support remove operation.");
		}
	}

	// workflow page states
	// mephistopheles78
	static final String WF_LIST_STATE_MY_PAGES_IN_WORKFLOW = "pagesinworkflow";
	static final String WF_LIST_STATE_PAGES_SAVED_AS_DRAFT = "checkedout";
	static final String WF_LIST_STATE_PAGES_WAITING_FOR_CORRECTION = "waitingforcorrection";
	static final String WF_LIST_STATE_PAGES_WAITING_FOR_RELEASE = "waitingforrelease";

	static final String ASSETMANAGER_SUBFOLDER_DELIMITER = "/";

	
	private RQLNodeList affixesCache;
	private CmsClient cmsClient;
	private RQLNode detailsNodeCache;
	private RQLNodeList languageVariantsCache;
	private String projectGuid;
	private String name;
	private RQLNodeList projectTreesegmentsCache;
	private RQLNodeList projectVariantsCache;
	private RQLNode publicationSettingsCache;
	private RQLNode projectSettingsCache;
	private RQLNodeList publishingTargetsCache;
	private RQLNodeList publicationFoldersCache;
	private String sessionKey;
	private String startPageGuid = null;

	/**
	 * For direct lookups of pages.
	 */
	protected final Map<String, Page> pageCache = new TreeMap<String, Page>();

	private RQLNodeList templateFoldersCache;
	// cache
	private final Map<String, Template> templatesCache = new HashMap<String, Template>(); // maps template guids to template objects
	private Map<String, ScriptParameters> parametersCache; // maps class names to parameters, ProjectPage
	private Map<String, PageArrayList> pagesCache; // maps an id to a list of pages
	private final Map<String, Folder> foldersCache = new HashMap<String, Folder>();; // maps folder guids to folder
	// objects
	private RQLNodeList userGroupNodeListCache;
	private String clipboardTableHtml = null;
	private int clipboardTableCounter;

	/**
	 * Erzeugt ein neues Projekt.
	 * 
	 * @param cmsClient
	 *            Referenz des CmsClient, zu dem das Projekt gehört.
	 */
	public Project(CmsClient cmsClient, String projectGuid) {
		super();

		this.cmsClient = cmsClient;
		this.sessionKey = null;
		this.projectGuid = projectGuid;
	}

	/**
	 * Erzeugt ein neues Projekt.
	 * 
	 * @param cmsClient
	 *            Referenz des CMSClient, zu dem das Projekt gehört.
	 * @param sessionKey
	 *            Der Session key beinhaltet die Anmeldung eines Benutzers an dieses Projekt.
	 * @param projectGuid
	 *            GUID des Projektes
	 */
	public Project(CmsClient cmsClient, String sessionKey, String projectGuid) {
		super();

		this.cmsClient = cmsClient;
		this.sessionKey = sessionKey;
		this.projectGuid = projectGuid;
	}

	/**
	 * Aktiviert den definierten Publishing Job für die gegebene GUID und liefert den gefundenen Job zurück.
	 * 
	 * @throws ElementNotFoundException
	 */
	public PublishingJob activatePublishingJobByGuid(String publishingJobGuid) throws RQLException {
		PublishingJob job = getPublishingJobByGuid(publishingJobGuid);
		job.setIsActive(true);
		return job;
	}

	/**
	 * Fügt das gegebenen StandardFieldText Element ins Clipboard HTML Table ein.
	 * <p>
	 * 
	 * @see #startBuildClipboardHtmlTable()
	 * @see #addBuildClipboardHtmlTable(StandardFieldTextElement)
	 * @see #endBuildClipboardHtmlTable()
	 */
	void addBuildClipboardHtmlTable(StandardFieldTextElement sftElement) {
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		String row = b.getString("clipboardTableStandardFieldText");
		// replace all values
		row = StringHelper.replace(row, "{0}", sftElement.getElementGuid());
		row = StringHelper.replace(row, "{1}", sftElement.getName());
		row = StringHelper.replace(row, "{2}", Integer.toString(clipboardTableCounter));
		row = StringHelper.replace(row, "{3}", Integer.toString(clipboardTableCounter + 100));
		row = StringHelper.replace(row, "{4}", Integer.toString(clipboardTableCounter + 200));
		clipboardTableHtml += row;
		clipboardTableCounter++;
	}

	/**
	 * The generic RQL to assign an authorization package to links, pages and others.
	 * <p>
	 * Used only internal for convenience.
	 */
	void assignAuthorizationPackage(String requestTagName, String guid, AuthorizationPackage authorizationPackage) throws RQLException {
		/*
		 * V7.5 request LINK <IODATA
		 * loginguid="3F4926FCCBBD4E8586FA849FD581CA04"
		 * sessionkey="DF3BBAA00A964C329EE87EAD87EAD599"> <AUTHORIZATION> <LINK
		 * guid="9D39329087C443D8ACBFBAF66B6C5E9E"> <AUTHORIZATIONPACKET
		 * action="assign" guid="A4BA5C66B4A94CCFB478DE2B99349D51"/> </LINK>
		 * </AUTHORIZATION> </IODATA> V7.5 request PAGE <IODATA
		 * loginguid="3F4926FCCBBD4E8586FA849FD581CA04"
		 * sessionkey="DF3BBAA00A964C329EE87EAD87EAD599"> <AUTHORIZATION> <PAGE
		 * guid="A319960220EB42C38442B81CD1E67984"> <AUTHORIZATIONPACKET
		 * action="assign" guid="0824C9247CFD40588D05B3ABFDAAD9AE"/> </PAGE>
		 * </AUTHORIZATION> </IODATA> V7.5 response <IODATA> </IODATA>
		 */

		// call CMS
		String tag = requestTagName.toUpperCase();
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<AUTHORIZATION>"
				+ "<" + tag + " guid='" + guid + "'>" + "<AUTHORIZATIONPACKET action='assign' guid='"
				+ authorizationPackage.getAuthorizationPackageGuid() + "'/>" + "	</" + tag + ">" + "</AUTHORIZATION>" + "</IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Edit project assignment of users: Assign all given users to this project.
	 */
	public void assignUsers(java.util.List<User> newUsers) throws RQLException {
		/*
		 V5 request 
		 <IODATA loginguid="[!guid_login!]"> 
		 <ADMINISTRATION action="assign"> 
		 <PROJECT guid="[!guid_project!]"> 
		 <USER guid="[!guid_user!]"/> 
		 ... 
		 </PROJECT> 
		 ... 
		 </ADMINISTRATION> 
		 </IODATA>
		 V5 response 
		 <IODATA>ok </IODATA> 
		 ok auch bei Fehler!
		 */

		// build request for assignment
		// begin
		StringBuilder rqlRequest = new StringBuilder();
		rqlRequest.append("<IODATA loginguid='").append(getLogonGuid()).append("'>");
		rqlRequest.append("<ADMINISTRATION action='assign'>");
		rqlRequest.append("<PROJECT guid='").append(getProjectGuid()).append("'>");

		// user tags
		for (int i = 0; i < newUsers.size(); i++) {
			User newUser = (User) newUsers.get(i);
			rqlRequest.append("<USER guid='").append(newUser.getUserGuid()).append("'/>");
		}
		// end
		rqlRequest.append("</PROJECT>");
		rqlRequest.append("</ADMINISTRATION>");
		rqlRequest.append("</IODATA>");

		// call CMS
		callCmsWithoutParsing(rqlRequest.toString());
	}

	/**
	 * Remove all user currently assigned to this project from this project. Afterwards this project has no users assigned.
	 * <p>
	 * Returns the number of unlinked users.
	 */
	public int unlinkUsers() throws RQLException {
		java.util.List<User> allUsers = getAllUsers();
		int result = allUsers.size();
		unlinkUsers(getAllUsers());
		return result;
	}

	/**
	 * Edit project assignment of users: Remove all given users from this project.
	 */
	public void unlinkUsers(java.util.List<User> users) throws RQLException {
		/*
		 V5 request 
		<IODATA loginguid="8E7BB97EE04A4505AE69DC2C26D25EDB">
		  <ADMINISTRATION action="unlink">
		    <PROJECT guid="5256C671655D4CE696F663C73CE3E526">
		      <USER guid="C049FFEB1D5743DDA5DACB25F99043AF"/>
		      <USER guid="A522435F77AD4087875D66A5A468BC29"/>
		      ...
		    </PROJECT>
		  </ADMINISTRATION>
		</IODATA>
		 V5 response 
		 <IODATA>okok</IODATA> 
		 */

		// build request for unlink
		// begin
		StringBuilder rqlRequest = new StringBuilder();
		rqlRequest.append("<IODATA loginguid='").append(getLogonGuid()).append("'>");
		rqlRequest.append("<ADMINISTRATION action='unlink'>");
		rqlRequest.append("<PROJECT guid='").append(getProjectGuid()).append("'>");

		// user tags
		for (int i = 0; i < users.size(); i++) {
			User newUser = users.get(i);
			rqlRequest.append("<USER guid='").append(newUser.getUserGuid()).append("'/>");
		}
		// end
		rqlRequest.append("</PROJECT>");
		rqlRequest.append("</ADMINISTRATION>");
		rqlRequest.append("</IODATA>");

		// call CMS
		callCmsWithoutParsing(rqlRequest.toString());
	}

	/**
	 * Erzeugt einen Folder aus dem gegebenen Node.
	 * 
	 * @param folderNode
	 *            Node mit den Daten des zurueckgegebenen Folders
	 * @return Folder
	 * @see <code>Folder</code>
	 */
	private Folder buildFolder(RQLNode folderNode) throws RQLException {

		Folder folder = null;
		String guid = folderNode.getAttribute("guid");
		String name = folderNode.getAttribute("name");
		String hide = folderNode.getAttribute("hideintexteditor");
		String saveType = folderNode.getAttribute("savetype");
		String path = folderNode.getAttribute("path");

		if (folderNode.getAttribute("catalog").equals("1")) {
			folder = new AssetManagerFolder(this, name, guid, hide, saveType, path);
		} else {
			folder = new FileFolder(this, name, guid, hide, saveType, path);
		}
		return folder;
	}

	/**
	 * Erzeugt eine Sprachvariante aus dem gegebenen Node.
	 */
	private LanguageVariant buildLanguageVariant(RQLNode languageVariantNode) {

		return new LanguageVariant(this, languageVariantNode.getAttribute("guid"), languageVariantNode.getAttribute("name"),
				languageVariantNode.getAttribute("rfclanguageid"), languageVariantNode.getAttribute("ismainlanguage"),
				languageVariantNode.getAttribute("language"));
	}

	/**
	 * Erzeugt eine Projektvariante aus dem gegebenen Node.
	 */
	private ProjectVariant buildProjectVariant(RQLNode projectVariantNode) {

		return new ProjectVariant(this, projectVariantNode.getAttribute("guid"), projectVariantNode.getAttribute("name"));
	}

	/**
	 * Erzeugt ein Exportziel aus dem gegebenen Node.
	 * 
	 * @param targetNode
	 *            Node mit den Daten des zurueckgegebenen Exportzieles
	 * @return PublishingTarget
	 * @see <code>PublishingTarget</code>
	 */
	private PublishingTarget buildPublishingTarget(RQLNode targetNode) throws RQLException {

		return new PublishingTarget(this, targetNode.getAttribute("guid"), targetNode.getAttribute("name"), targetNode
				.getAttribute("path"), targetNode.getAttribute("type"));
	}

	/**
	 * Liefert den TemplateFolder mit dem gegebenen Namen vom CMS zurück.
	 * 
	 * @param templateFolderNode
	 *            Name des Template Folder.
	 * @return TemplateFolder
	 * @see TemplateFolder
	 */
	private TemplateFolder buildTemplateFolder(RQLNode templateFolderNode) throws RQLException {

		// wrap folder data
		return new TemplateFolder(this, templateFolderNode.getAttribute("name"), templateFolderNode.getAttribute("guid"));
	}

	/**
	 * Erzeugt ein Userobjekt aus dem gegebenen Usernode.
	 * 
	 * @param node
	 *            node des tags <user>
	 */
	private User buildUser(RQLNode node) {

		return getCmsClient().buildUser(node);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getCmsClient().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines
	 * Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	
	/**
	 * Make some kind of call. This method adds the IODATA node.
	 * 
	 * @param netXml netto call, without
	 * @return the unparsed result.
	 * @throws RQLException
	 */
	public String testCall(String netXml) throws RQLException {
		return getCmsClient().callCmsWithoutParsing("<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + netXml + "</IODATA>");
	}


	/**
	 * Ein Projekt kann teilweise auch ohne session key genutzt werden.
	 * <p>
	 * Ist keine Session vorhanden, können nur Administrative Funktionen dieses Projektes genutzt werden.
	 * 
	 */
	private String checkSessionKey() throws RQLException {

		if (getSessionKey() == null) {
			throw new MissingSessionKeyException("Project object try to call functions for which a session key is required.");
		}
		return sessionKey;
	}

	/**
	 * Liefert ein Seitensuch-Objekt zurück. An ihm können alle Suchkriterien definiert und die eigentliche Suche gestartet werden.
	 */
	public PageSearch definePageSearch() {
		return new PageSearch(this);
	}

	/**
	 * Delete an Element reference. Needs the target element guid.
	 */
	void deleteElementReference(String sourceElementGuid, String targetElementGuid) throws RQLException {
		/*
		V7.5 request 
		<IODATA loginguid="BBB7B38914BA471299F5958DE8B55340" dialoglanguageid="ENG" sessionkey="D2B7C2E0757240F6842A310BAC2A17FC">
		<ELEMENT action="unlink" guid="735D4F01C77B4954B630164017906153">
		<ELEMENT guid="10BC24A15B5C4BBDA11B689749A65C6D"/>
		</ELEMENT>
		</IODATA>
		V7.5 response
		<IODATA>
		<ELEMENT action="unlink" guid="735D4F01C77B4954B630164017906153" dialoglanguageid="ENG" sessionkey="D2B7C2E0757240F6842A310BAC2A17FC" languagevariantid="ENG" changed="-1" pageguid="1C8814307E844977BCE8F5DB933B79FD" useconnection="1" projectguid="73671509FA5C43ED8FC4171AD0298AD2" userguid="4324D172EF4342669EAF0AD074433393">
		<ELEMENT guid="10BC24A15B5C4BBDA11B689749A65C6D" linkguid="735D4F01C77B4954B630164017906153" targetlinkguid="" parenttable="PGE" sessionkey="D2B7C2E0757240F6842A310BAC2A17FC" languagevariantid="ENG" elementguid="735D4F01C77B4954B630164017906153"/>
		</ELEMENT>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<ELEMENT action='unlink' guid='" + sourceElementGuid + "'>" + "<ELEMENT guid='" + targetElementGuid + "'/>"
				+ "	</ELEMENT></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Löscht den Seitencache dieses Projekts vollständig.
	 */
	public void deletePageCache() throws RQLException {

		checkSessionKey();
		/*
		 * V7.5 request (changed!) <IODATA
		 * loginguid="0E961414D76A492B873CE334FDE6567D"
		 * sessionkey="055E95C92EB54517BE297B895455DCE0"> <PBCACHE> <CACHE
		 * action="delete"/> </PBCACHE> </IODATA> V7.5 response nothing
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<PBCACHE><CACHE action='delete'/></PBCACHE></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Löscht Jobreports älter als die gegebene Anzahl von Tagen.
	 * 
	 * @param daysToKeep
	 *            number of days for that job reports will not be deleted; = 2 means, that all reports older than 2 days will be
	 *            deleted
	 * @return number of deleted reports
	 */
	public int deletePublishingJobReports(int daysToKeep) throws RQLException {

		// subtract given number of days from today
		ReddotDate limit = ReddotDate.now();
		limit.rollDay(-daysToKeep);

		// loop through all existings reports
		int numberOfDeletedReports = 0;
		java.util.List<PublishingJobReport> reports = getAllPublishingJobReports();
		for (PublishingJobReport report : reports) {
			if (report.getStartDate().compareWith(limit) <= 0) {
				report.delete();
				numberOfDeletedReports += 1;
			}
		}
		return numberOfDeletedReports;
	}

	/**
	 * Setzt in allen FTP publishing targets dieses Projekts, die namePart (check with contains=case sensitive) im Namen haben den FTP
	 * user name und Passwort = unknown.
	 * <p>
	 * Eine Publizierung über diese Publizierungsziele ist danach nicht mehr möglich. Liefert die deaktivierten Ziele zurück. Benötigt
	 * den session key!
	 */
	public java.util.List<PublishingTarget> disableFtpPublishingTargetsByNameContains(String namePart) throws RQLException {
		return disableFtpPublishingTargetsByNameContains(namePart, false);
	}

	/**
	 * Setzt in allen FTP publishing targets dieses Projekts, die namePart im Namen haben den FTP user name und Passwort = unknown.
	 * <p>
	 * Eine Publizierung über diese Publizierungsziele ist danach nicht mehr möglich. Liefert die deaktivierten Ziele zurück. Benötigt
	 * den session key!
	 */
	public java.util.List<PublishingTarget> disableFtpPublishingTargetsByNameContains(String namePart, boolean ignoreCase)
			throws RQLException {
		java.util.List<PublishingTarget> result = new ArrayList<PublishingTarget>();
		for (PublishingTarget target : getPublishingTargets()) {
			// skip non ftp target
			if (!target.isFtpTarget()) {
				continue;
			}
			// check namePart case dependent on given value
			if (StringHelper.contains(target.getName(), namePart, ignoreCase)) {
				target.disableFtpUser();
				result.add(target);
			}
		}
		return result;
	}

	/**
	 * Beendet das Erzeugen des Clipboard HTML Table codes und liefert diesen zurück.
	 * <p>
	 * 
	 * @see #startBuildClipboardHtmlTable()
	 * @see #addBuildClipboardHtmlTable(StandardFieldTextElement)
	 * @see #endBuildClipboardHtmlTable()
	 */
	String endBuildClipboardHtmlTable() {
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		clipboardTableHtml += b.getString("clipboardTableEnd");
		return clipboardTableHtml;
	}

	/**
	 * Liefert eine Liste von Seiten zurück, die von einem Benutzer der gegebenen Gruppe erstellt wurde.
	 */
	private PageArrayList filterAndWrapPageNodesInWorkflow(RQLNodeList pagesInWorkflowNodeList, UserGroup userGroup)
			throws RQLException {
		PageArrayList pages = new PageArrayList();
		if (pagesInWorkflowNodeList != null) {
			for (int i = 0; i < pagesInWorkflowNodeList.size(); i++) {
				RQLNode node = pagesInWorkflowNodeList.get(i);
				if (userGroup.contains(node.getAttribute("username"))) {
					pages.add(new Page(this, node.getAttribute("guid")));
				}
			}
		}
		return pages;
	}

	/**
	 * Liefert das Berechtigunspaket des gegebenen Typs für die gegebenen GUID.
	 * 
	 * @param packageType
	 *            see possible list in class <code>AuthorizationPackage</code>
	 * @param authorizationPackageGuid
	 *            Reddot GUID des Berechtigungspaketes
	 * @see AuthorizationPackage
	 * @throws ElementNotFoundException
	 */
	private AuthorizationPackage findAuthorizationPackageByGuid(String packageType, String authorizationPackageGuid)
			throws RQLException {

		RQLNodeList packagesNodeList = getAuthorizationPackageNodeList(packageType);

		// find authorization package by GUID
		for (int i = 0; i < packagesNodeList.size(); i++) {
			RQLNode node = packagesNodeList.get(i);
			String guid = node.getAttribute("guid");
			if (guid.equalsIgnoreCase(authorizationPackageGuid)) {
				return new AuthorizationPackage(this, guid, node.getAttribute("name"), node.getAttribute("type"));
			}
		}
		throw new ElementNotFoundException("Authorization package for type " + packageType + " with GUID " + authorizationPackageGuid
				+ " could not be found in the project.");
	}

	/**
	 * Liefert das Berechtigunspaket für den gegebenen Typ und den gegebenen Namen.
	 * 
	 * @param packageType
	 *            see possible list in class <code>AuthorizationPackage</code>
	 * @param authorizationPackageName
	 *            Name des Berechtigungspaketes (case ignored!)
	 * @see AuthorizationPackage
	 * @throws ElementNotFoundException
	 */
	private AuthorizationPackage findAuthorizationPackageByName(String packageType, String authorizationPackageName)
			throws RQLException {

		RQLNodeList packagesNodeList = getAuthorizationPackageNodeList(packageType);

		// not found
		if (packagesNodeList == null) {
			throw new ElementNotFoundException("Authorization package for type " + packageType + " and named "
					+ authorizationPackageName + " could not be found in the project.");
		}

		// find authorization package
		for (int i = 0; i < packagesNodeList.size(); i++) {
			RQLNode node = packagesNodeList.get(i);
			String name = node.getAttribute("name");
			if (name.equalsIgnoreCase(authorizationPackageName)) {
				return new AuthorizationPackage(this, node.getAttribute("guid"), name, node.getAttribute("type"));
			}
		}
		throw new ElementNotFoundException("Authorization package for type " + packageType + " and named " + authorizationPackageName
				+ " could not be found in the project.");
	}

	/**
	 * Liefert den Dateiordner mit dem gegebenen Namen vom CMS zurück.
	 * 
	 * @param folderGuid
	 *            Name des Dateiordners.
	 * @return Folder
	 * @see <code>Folder</code>
	 */
	private RQLNode findFolderNodeByGuid(String folderGuid) throws RQLException {

		// find folder
		RQLNode folderNode = null;
		RQLNodeList folderList = getFoldersNodeList();

		for (int i = 0; i < folderList.size(); i++) {
			folderNode = folderList.get(i);

			if (folderNode.getAttribute("guid").equals(folderGuid)) {
				return folderNode;
			}
		}
		throw new ElementNotFoundException("Folder with guid " + folderGuid + " could not be found in the project.");
	}

	/**
	 * Liefert den FolderNode für den gegebenen Namen vom CMS zurück.
	 * 
	 * @param name
	 *            Name des Dateiordners.
	 * @return RQLNode
	 * @see <code>RQLNode</code>
	 */
	private RQLNode findFolderNodeByName(String name) throws RQLException {

		// find folder
		RQLNode folderNode = null;
		RQLNodeList folderList = getFoldersNodeList();

		for (int i = 0; i < folderList.size(); i++) {
			folderNode = folderList.get(i);

			if (folderNode.getAttribute("name").equals(name)) {
				return folderNode;
			}
		}
		throw new ElementNotFoundException("Folder named " + name + " could not be found in the project.");
	}

	/**
	 * Liefert den Node für das Exportziel mit der gegebenen GUID.
	 * 
	 * @param publishingTargetGuid
	 *            GUID des Exportzieles.
	 */
	private RQLNode findPublishingTargetNodeByGuid(String publishingTargetGuid) throws RQLException {

		// find folder
		RQLNode targetNode = null;
		RQLNodeList targets = getPublishingTargetsNodeList();

		for (int i = 0; i < targets.size(); i++) {
			targetNode = targets.get(i);

			if (targetNode.getAttribute("guid").equals(publishingTargetGuid)) {
				return targetNode;
			}
		}
		throw new ElementNotFoundException("Publishing target with guid " + publishingTargetGuid
				+ " could not be found in the project.");
	}

	/**
	 * Liefert den Node für das Exportziel, dessen Name mit dem gegebenen Prefix beginnt.
	 */
	private RQLNode findPublishingTargetNodeByNameStartsWith(String publishingTargetNamePrefix) throws RQLException {

		// find folder
		RQLNode targetNode = null;
		RQLNodeList targets = getPublishingTargetsNodeList();

		for (int i = 0; i < targets.size(); i++) {
			targetNode = targets.get(i);

			if (targetNode.getAttribute("name").startsWith(publishingTargetNamePrefix)) {
				return targetNode;
			}
		}
		throw new ElementNotFoundException("Publishing target with name prefix " + publishingTargetNamePrefix
				+ " could not be found in the project.");
	}

	/**
	 * Liefert den Node des TemplateFolder mit der gegebenen GUID vom CMS zurück.
	 * 
	 * @param templateFolderGuid
	 *            GUID des TemplateFolders
	 * @return RQLNode
	 * @see RQLNode
	 */
	private RQLNode findTemplateFolderNodeByGuid(String templateFolderGuid) throws RQLException {

		// find folder
		RQLNode folder = null;
		RQLNodeList folderList = getTemplateFoldersNodeList();

		for (int i = 0; i < folderList.size(); i++) {
			folder = folderList.get(i);

			if (folder.getAttribute("guid").equals(templateFolderGuid)) {
				return folder;
			}
		}
		throw new ElementNotFoundException("Template folder with GUID " + templateFolderGuid + " could not be found in the project.");
	}

	/**
	 * Liefert den Node des TemplateFolder mit dem gegebenen Namen vom CMS zurück.
	 * 
	 * @param name
	 *            Name des Template Folder.
	 * @return RQLNode
	 * @see RQLNode
	 */
	private RQLNode findTemplateFolderNodeByName(String name) throws RQLException {

		// find folder
		RQLNode folder = null;
		RQLNodeList folderList = getTemplateFoldersNodeList();

		for (int i = 0; i < folderList.size(); i++) {
			folder = folderList.get(i);

			if (folder.getAttribute("name").equals(name)) {
				return folder;
			}
		}
		throw new ElementNotFoundException("Template folder named " + name + " could not be found in the project.");
	}

	/**
	 * Liefert eine List mit allen aktuell an diesem Projekt angemeldeten Usern.
	 * 
	 * @see #getAllConnectedUsers()
	 */
	public java.util.List<User> getActiveUsers() throws RQLException {

		return getAllConnectedUsers();
	}

	/**
	 * Liefert den Affix aus den General Settings mit der gegebenen GUID.
	 * 
	 * @param affixGuid
	 *            GUID des Prefixes oder Suffixes
	 */
	public Affix getAffixByGuid(String affixGuid) throws RQLException {
		checkSessionKey();
		/*
		 * V7.5 request <IODATA loginguid="D43446BFDD51477EB1B8F79AC3C3FAA6"
		 * sessionkey="28D889F1770C42DC8E47349B81E412B6"> <TREESEGMENT
		 * type="project.6002" action="load"
		 * guid="A5671021AEA14EC99359FEA8DED67016" /> </IODATA> V7.5 response
		 * <IODATA> <TREESEGMENTS> <SEGMENT
		 * guid="0B07554DBB914D649F13768C3BD746C4" type="project.6003"
		 * image="point.gif" expand="0" value="pdf_gateway_"
		 * col1value="pdf_gateway_" col2fontcolor="#ff8C00" col2value=""
		 * col1fontweight="normal" col2fontweight="normal"/> <SEGMENT
		 * guid="1F1107C31B5747D681E7D285A611620D" type="project.6003"
		 * image="point.gif" expand="0" value="direct_linked_"
		 * col1value="direct_linked_" col2fontcolor="#ff8C00" col2value=""
		 * col1fontweight="normal" col2fontweight="normal"/> ... <SEGMENT
		 * guid="F60500A7CE48464EB04550A939134364" type="project.6003"
		 * image="point.gif" expand="0" value="leaf_list_"
		 * col1value="leaf_list_" col2fontcolor="#ff8C00" col2value=""
		 * col1fontweight="normal" col2fontweight="normal"/> </TREESEGMENTS>
		 * <TREEELEMENT guid="A5671021AEA14EC99359FEA8DED67016" value="Prefixes &
		 * Suffixes" image="suffix.gif" flags="0" expand="1" descent=""
		 * type="project.6002" col1value="Prefixes & Suffixes"
		 * col2fontcolor="#ff8C00" col2value="" col1fontweight="normal"
		 * col2fontweight="normal"/> </IODATA>
		 */

		// call CMS
		if (affixesCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ "<TREESEGMENT action='load' type='" + Affix.TREESEGMENT_TYPE + "'/>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			affixesCache = rqlResponse.getNodes("SEGMENT");
		}

		// wrap result into job report objects
		for (int i = 0; i < affixesCache.size(); i++) {
			RQLNode node = affixesCache.get(i);
			String guid = node.getAttribute("guid");
			if (guid.equals(affixGuid)) {
				return new Affix(this, guid, node.getAttribute("value"));
			}
		}
		// affix not found
		throw new ElementNotFoundException("The prefix or suffix with the given GUID " + affixGuid
				+ " cannot be found. Please check the GUID and try again.");
	}

	/**
	 * Liefert alle aktiven vordefinierten PublishingJobs zurück. Benötigt den session key!
	 */
	public java.util.List<PublishingJob> getAllActivePublishingJobs() throws RQLException {
		return getAllPublishingJobs(true);
	}

	/**
	 * Liefert eine List mit allen gerade am Projekt angemeldeten Usern zurück.
	 * 
	 * @see #getActiveUsers()
	 */
	public java.util.List<User> getAllConnectedUsers() throws RQLException {

		return wrapUserNodes(getAllConnectedUsersNodeList());
	}

	/**
	 * Liefert die RQLNodeList mit allen gerade angemeldeten Usern an diesem Projekt.
	 * 
	 * @return <code>RQLNodeList</code>
	 */
	private RQLNodeList getAllConnectedUsersNodeList() throws RQLException {

		/*
		 * V5 request <IODATA loginguid="[!guid_login!]"> <ADMINISTRATION>
		 * <USERS action="connectlist" projectguid="[!project_guid!]"/>
		 * </ADMINISTRATION> </IODATA> V5 response <IODATA> <USERS> <USER
		 * guid="325D4549BFF44FAB83A2E34005093DB3" id="637"
		 * name="deleteUnlinkedPages" fullname="" flags1="0" flags2="0" email=""
		 * maxlevel="1" projectlevel="1" dialoglanguageid="ENG"
		 * loginguid="0342A573A5384C4FA4A87A0E0FEE1F5B"
		 * moduleguid="37542EE2585545D188BEFB6801B1C971"
		 * logindate="39198,6189930556" lastactiondate="39198,6209606482"
		 * moduleid="smarttree" intern="0" moduledescription="SmartTree"
		 * projectname="hip.hlcl.com"
		 * projectguid="06BE79A1D9F549388F06F6B649E27152"/> <USER
		 * guid="0E785A79876647C0928D8A07D44CEEA3" id="688" name="rymkilu2"
		 * fullname="Lutz Rymkiewitsch 2" flags1="0" flags2="0"
		 * email="rymkilu@hlag.com" maxlevel="3" projectlevel="3"
		 * dialoglanguageid="ENG" loginguid="668C17162E1F4793AAD145B737204FF8"
		 * moduleguid="30265560D1E3462DB808699637681497"
		 * logindate="39198,6104282407" lastactiondate="39198,6209490741"
		 * moduleid="smartedit" intern="0" moduledescription="SmartEdit"
		 * projectname="hip.hlcl.com"
		 * projectguid="06BE79A1D9F549388F06F6B649E27152"/> </USERS> </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + " <ADMINISTRATION>"
				+ "<USERS action='connectlist' projectguid='" + getProjectGuid() + "' /></ADMINISTRATION>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		return rqlResponse.getNodes("USER");
	}

	/**
	 * Liefert alle inaktiven vordefinierten PublishingJobs zurück. Benötigt den session key!
	 */
	public java.util.List<PublishingJob> getAllInactivePublishingJobs() throws RQLException {
		return getAllPublishingJobs(false);
	}

	/**
	 * Liefert alle Sprachvarianten zurück.
	 */
	public java.util.List<LanguageVariant> getAllLanguageVariants() throws RQLException {

		RQLNodeList languageNodeList = getLanguageVariantsNodeList();
		java.util.List<LanguageVariant> result = new ArrayList<LanguageVariant>();

		// wrap into objects
		for (int i = 0; i < languageNodeList.size(); i++) {
			RQLNode node = languageNodeList.get(i);
			result.add(buildLanguageVariant(node));
		}
		return result;
	}

	/**
	 * Liefert die Rfc language id (en,de) aller Sprachvarianten zurück in der nativen Reihenfolge des MS.
	 * 
	 * @see LanguageVariant#getRfcLanguageId()
	 */
	public java.util.List<String> getAllLanguageVariantRfcLanguageIds() throws RQLException {

		java.util.List<String> result = new ArrayList<String>();
		// wrap into objects
		for (LanguageVariant lv : getAllLanguageVariants()) {
			result.add(lv.getRfcLanguageId());
		}
		return result;
	}

	/**
	 * Liefert die Rfc language id (en,de) aller Sprachvarianten zurück. Die Hauptsprache ist immer die erste, die weiteren entsprechen
	 * der nativen Reihenfolge des MSs.
	 * 
	 * @see LanguageVariant#getRfcLanguageId()
	 */
	public java.util.List<String> getAllLanguageVariantRfcLanguageIdsOrdered() throws RQLException {

		java.util.List<String> result = new ArrayList<String>();

		LanguageVariant mainLanguageVariant = getMainLanguageVariant();
		result.add(mainLanguageVariant.getRfcLanguageId());

		for (LanguageVariant lv : getAllLanguageVariants()) {
			if (!lv.equals(mainLanguageVariant)) {
				result.add(lv.getRfcLanguageId());
			}
		}
		return result;
	}

	/**
	 * Liefert alle Seitenberechtigunspakete vom Typ normal (=0) zurück.
	 * 
	 * @see AuthorizationPackage
	 */
	public java.util.List<AuthorizationPackage> getAllPageAuthorizationPackages() throws RQLException {
		return getAuthorizationPackages(AuthorizationPackage.NORMAL_TYPE);
	}

	/**
	 * Liefert alle Detailberechtigunspakete für AssetManager Attribute zurück.
	 * 
	 * @see AuthorizationPackage
	 */
	public java.util.List<AuthorizationPackage> getAllAssetManagerAttributeDetailedAuthorizationPackages() throws RQLException {
		return getAuthorizationPackages(AuthorizationPackage.DETAILED_ASSET_MANAGER_ATTRIBUT_TYPE);
	}

	/**
	 * Liefert alle Detailberechtigunspakete für Elemente zurück.
	 * 
	 * @see AuthorizationPackage
	 */
	public java.util.List<AuthorizationPackage> getAllElementDetailedAuthorizationPackages() throws RQLException {
		return getAuthorizationPackages(AuthorizationPackage.DETAILED_ELEMENT_TYPE);
	}

	/**
	 * Liefert alle Detailberechtigunspakete für Links zurück.
	 * 
	 * @see AuthorizationPackage
	 */
	public java.util.List<AuthorizationPackage> getAllLinkDetailedAuthorizationPackages() throws RQLException {
		return getAuthorizationPackages(AuthorizationPackage.DETAILED_LINK_TYPE);
	}

	/**
	 * Liefert alle Detailberechtigunspakete für Seiten zurück.
	 * 
	 * @see AuthorizationPackage
	 */
	public java.util.List<AuthorizationPackage> getAllPageDetailedAuthorizationPackages() throws RQLException {
		return getAuthorizationPackages(AuthorizationPackage.DETAILED_PAGE_TYPE);
	}

	/**
	 * Liefert alle Berechtigunspakete für Templates zurück.
	 * 
	 * @see AuthorizationPackage
	 */
	public java.util.List<AuthorizationPackage> getAllTemplateAuthorizationPackages() throws RQLException {
		return getAuthorizationPackages(AuthorizationPackage.CONTENT_CLASS_TYPE);
	}

	/**
	 * Liefert alle Berechtigunspakete für Projektvarianten zurück.
	 * 
	 * @see AuthorizationPackage
	 */
	public java.util.List<AuthorizationPackage> getAllProjectVariantAuthorizationPackages() throws RQLException {
		return getAuthorizationPackages(AuthorizationPackage.PROJECT_VARIANT_TYPE);
	}

	/**
	 * Liefert alle Berechtigunspakete für Sprachvarianten zurück.
	 * 
	 * @see AuthorizationPackage
	 */
	public java.util.List<AuthorizationPackage> getAllLanguageVariantAuthorizationPackages() throws RQLException {
		return getAuthorizationPackages(AuthorizationPackage.LANGUAGE_VARIANT_TYPE);
	}

	/**
	 * Liefert alle Berechtigunspakete für Ordner zurück.
	 * 
	 * @see AuthorizationPackage
	 */
	public java.util.List<AuthorizationPackage> getAllFolderAuthorizationPackages() throws RQLException {
		return getAuthorizationPackages(AuthorizationPackage.FOLDER_TYPE);
	}

	/**
	 * Liefert alle Detailberechtigunspakete für Kontentklassen zurück.
	 * 
	 * @see #getAllTemplateAuthorizationPackages()
	 */
	public java.util.List<AuthorizationPackage> getAllContentClassAuthorizationPackages() throws RQLException {
		return getAllTemplateAuthorizationPackages();
	}

	/**
	 * Liefert alle Detailberechtigunspakete AssetManager Attribut, Element, Link und Seite (1,2,4,8) zurück.
	 * 
	 * @see AuthorizationPackage
	 */
	public java.util.List<AuthorizationPackage> getAllDetailedAuthorizationPackages() throws RQLException {
		java.util.List<AuthorizationPackage> result = getAllAssetManagerAttributeDetailedAuthorizationPackages();
		result.addAll(getAllElementDetailedAuthorizationPackages());
		result.addAll(getAllLinkDetailedAuthorizationPackages());
		result.addAll(getAllPageDetailedAuthorizationPackages());
		return result;
	}

	/**
	 * Liefert alle Berechtigunspakete dieses Projektes zurück.
	 * <p>
	 * Das globale Berechtigungspaket und Pluginberechtigungen fehlen.
	 * <p>
	 * Kombiniert die folgenden Methoden:
	 * 
	 * @see #getAllPageAuthorizationPackages()
	 * @see #getAllDetailedAuthorizationPackages()
	 * @see #getAllContentClassAuthorizationPackages()
	 * @see #getAllProjectVariantAuthorizationPackages()
	 * @see #getAllLanguageVariantAuthorizationPackages()
	 */
	public java.util.List<AuthorizationPackage> getAllAuthorizationPackages() throws RQLException {
		java.util.List<AuthorizationPackage> result = getAllPageAuthorizationPackages();
		result.addAll(getAllDetailedAuthorizationPackages());
		result.addAll(getAllContentClassAuthorizationPackages());
		result.addAll(getAllProjectVariantAuthorizationPackages());
		result.addAll(getAllLanguageVariantAuthorizationPackages());
		return result;
	}

	/**
	 * Liefert alle Berechtigunspakete dieses Projektes, die die gegebenen userGroup beinhalten zurück.
	 * <p>
	 * Das globale Berechtigungspaket und Pluginberechtigungen werden nicht berücksichtigt.
	 * 
	 * @see #getAllAuthorizationPackages()
	 */
	public java.util.List<AuthorizationPackage> collectAuthorizationPackagesContaining(UserGroup userGroup) throws RQLException {
		java.util.List<AuthorizationPackage> result = new ArrayList<AuthorizationPackage>();
		// collect
		for (AuthorizationPackage authorizationPackage : getAllAuthorizationPackages()) {
			if (authorizationPackage.contains(userGroup)) {
				result.add(authorizationPackage);
			}
		}
		return result;
	}

	/**
	 * Liefert alle Berechtigunspakete vom gegebenen Typ zurück.
	 * 
	 * @see AuthorizationPackage constants
	 */
	private java.util.List<AuthorizationPackage> getAuthorizationPackages(String authorizationPackageType) throws RQLException {

		java.util.List<AuthorizationPackage> result = new ArrayList<AuthorizationPackage>();

		// no packages, return empty list
		RQLNodeList packagesNodeList = getAuthorizationPackageNodeList(authorizationPackageType);
		if (packagesNodeList == null) {
			return result;
		}

		// wrap all authorization packages
		for (int i = 0; i < packagesNodeList.size(); i++) {
			RQLNode node = packagesNodeList.get(i);
			String name = node.getAttribute("name");
			result.add(new AuthorizationPackage(this, node.getAttribute("guid"), name, node.getAttribute("type")));
		}
		return result;
	}

	/**
	 * Gibt alle Seiten zurück, die auf dem gegebenen Template basieren. Das Ergebnis könnte sehr groß werden. Benötigt den session
	 * key!
	 * 
	 * @param templateFolderName
	 *            content class folder name
	 * @param templateName
	 *            name of the conten class
	 * @param maxPages
	 *            maximum number of pages to return
	 */
	public PageArrayList getAllPagesBasedOn(String templateFolderName, String templateName, int maxPages) throws RQLException {
		return getAllPagesBasedOn(getTemplateByName(templateFolderName, templateName), maxPages);
	}

	/**
	 * Gibt alle Seiten zurück, die auf dem gegebenen Template basieren. Das Ergebnis könnte sehr groß werden. Benötigt den session
	 * key!
	 * 
	 * @param template
	 *            Template, dessen Instanzen gesucht sind
	 * @return List of Pages
	 */
	public PageArrayList getAllPagesBasedOn(Template template, int maxPages) throws RQLException {

		RQLNodeList pageNodes = getAllPagesBasedOnNodeList(template, maxPages);

		// check empty
		PageArrayList pages = new PageArrayList();
		if (pageNodes == null) {
			return pages;
		}

		// convert result
		for (int i = 0; i < pageNodes.size(); i++) {
			RQLNode pageNode = (RQLNode) pageNodes.get(i);
			pages.add(new Page(this, template, pageNode.getAttribute("guid"), pageNode.getAttribute("id"), pageNode
					.getAttribute("headline")));
		}

		return pages;
	}

	/**
	 * Returns the node list of all page instances for given template.
	 */
	RQLNodeList getAllPagesBasedOnNodeList(Template template, int maxPages) throws RQLException {
		checkSessionKey();
		/*
		 * V5 request <IODATA loginguid="C53DDB4FDFFA4A5CB51BF00A83AA89FB"
		 * sessionkey="925597405LF24BPTg277"> <PAGE action="search"
		 * templateguid="44E1E2EE063A46CCBEA6E297922E3AB1" maxrecords="500"/>
		 * </IODATA> V5 response <IODATA><PAGELIST> <PAGE id="12553"
		 * guid="AB92D1372EF94708B9C6B58DA9DA6814" headline="text download
		 * block" flags="525312" createdate="38000.3590856481"
		 * changedate="38279.5469212963" releasedate="38279.6184143519"/> <PAGE
		 * id="12825" guid="6D8E1B65DA16466D9774C3BA4DEFBCB1" headline="sections
		 * download block" flags="525312" createdate="38002.5836226852"
		 * changedate="38279.5469444444" releasedate="38279.6182986111"/> <PAGE
		 * id="57901" guid="0DA1FC559F9D4828975A6940CDFE16D3" headline="Gasaeco"
		 * flags="8192" createdate="38272.3792476852"
		 * changedate="38272.3794097222" releasedate="0"/> <PAGE id="58282"
		 * guid="9CD916D61CEC4CEBAC9C845ED07179FC" headline="Training Report"
		 * flags="263168" createdate="38275.8424652778"
		 * changedate="38275.8470138889" releasedate="0"/> </PAGELIST> </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<PAGE action='search' templateguid='" + template.getTemplateGuid() + "'  maxrecords='" + maxPages + "'/>"
				+ "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNodeList pageNodes = rqlResponse.getNodes("PAGE");
		return pageNodes;
	}

	/**
	 * Liefert alle Seiten dieses Projektes im Status Entwurf.
	 * 
	 * @return List of Pages
	 */
	public PageArrayList getAllPagesSavedAsDraft() throws RQLException {

		return getPagesByState(WF_LIST_STATE_PAGES_SAVED_AS_DRAFT, null);
	}

	/**
	 * Liefert alle Seiten dieses Projektes (global) im Status Entwurf und die von einem Autor der gegebenen Benutzergruppe erstellt
	 * wurde.
	 * 
	 * @return List of Pages
	 */
	public PageArrayList getAllPagesSavedAsDraftCreatedByUserGroup(UserGroup userGroup) throws RQLException {

		RQLNodeList pagesNodeList = getPagesByStateNodeList(WF_LIST_STATE_PAGES_SAVED_AS_DRAFT, null);
		return filterAndWrapPageNodesInWorkflow(pagesNodeList, userGroup);
	}

	/**
	 * Liefert alle Seiten dieses Projektes, die auf Korrektur stehen.
	 * 
	 * @return List of Pages
	 */
	public PageArrayList getAllPagesWaitingForCorrection() throws RQLException {

		return getPagesByState(WF_LIST_STATE_PAGES_WAITING_FOR_CORRECTION, null);
	}

	/**
	 * Liefert alle Seiten dieses Projektes (global), die auf Korrektur stehen und die von einem Autor der gegebenen Benutzergruppe
	 * erstellt wurde.
	 * 
	 * @return List of Pages
	 */
	public PageArrayList getAllPagesWaitingForCorrectionCreatedByUserGroup(UserGroup userGroup) throws RQLException {

		RQLNodeList pagesNodeList = getPagesByStateNodeList(WF_LIST_STATE_PAGES_WAITING_FOR_CORRECTION, null);
		return filterAndWrapPageNodesInWorkflow(pagesNodeList, userGroup);
	}

	/**
	 * Liefert alle Seiten dieses Projektes, die auf Freigabe warten.
	 * 
	 * @return List of Pages
	 */
	public PageArrayList getAllPagesWaitingForRelease() throws RQLException {

		return getPagesByState(WF_LIST_STATE_PAGES_WAITING_FOR_RELEASE, null);
	}

	/**
	 * Gibt alle Seiten zurück, die einen Dateinamen gesetzt haben. Das Ergebnis könnte sehr groß werden. Benötigt den session key!
	 * 
	 * @return List of Pages
	 */
	public PageArrayList getAllPagesWithFilename() throws RQLException {

		checkSessionKey();
		/*
		V7.5 request 
		<IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
		<PROJECT> 
		<PAGES action="listnames"/> 
		</PROJECT> 
		</IODATA>
		
		 V7.5 response 
		<IODATA> 
		<PAGES> 
		<PAGE guid="80B3AFBA6C4B46DC805A05EEF92D4E10" headline="content_styles.css" name="content_styles.css" /> 
		<PAGE guid="337077D471D14B8E93ABC684E7332F8B" headline="cpships_styles.css" name="cpships_styles.css" /> 
		<PAGE guid="C77E0205363C40168D6942C82260BF5F" headline="Delete old pages from HIP" name="delete_pages.del" /> 
		<PAGE guid="82F2160372554DD8B1F01C442E142666" headline="fulltext-result.xml" name="fulltext-result.xml" /> 
		<PAGE guid="B987F36CFBC6485BBCA914AC359BDBA8" headline="JavaScript functions" name="hip_functions.js" /> 
		<PAGE guid="5B5B0490EEE841009710AFF0D91F1167" headline="hs.xsl" name="hs.xsl" /> 
		<PAGE guid="5BDC6A0B768A4C7AB67CD1EFD5303B4C" headline="ht-fulltext-search.xsl" name="ht-fulltext-search.xsl" />
		<PAGE guid="20F813003C2C465FA4ED044B84A45FF8" headline="ht-html-copy.xsl" name="ht-html-copy.xsl" /> 
		<PAGE guid="526B45863F6C45BAB7D584E1C9D1D092" headline="ht-job-search.xsl" name="ht-job-search.xsl" /> 
		<PAGE guid="C429BA30F6B64B01A4DB0706A4C0BC0B" headline="import_replacements.py" name="import_replacements.py" />
		<PAGE guid="F940999C30204EBEB07FCE150C709EFB" headline="job_search_result.html" name="job_search_result.html" />
		<PAGE guid="2B66014A18024BB89B3A82FD3792F375" headline="job_search_result.xml" name="job_search_result.xml" />
		<PAGE guid="CA2B246BE1F3474CB3C98827B6AD62AA" headline="Organisation tree" name="navigation_organisation" /> 
		<PAGE guid="18E5D267B8164C73885FBA34B2991020" headline="Processes tree" name="navigation_processes" /> 
		...
		</PAGES> 
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PROJECT>"
				+ "<PAGES action='listnames' />" + "</PROJECT>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNodeList pageNodes = rqlResponse.getNodes("PAGE");

		// convert result
		PageArrayList pages = new PageArrayList(pageNodes.size());
		for (int i = 0; i < pageNodes.size(); i++) {
			RQLNode pageNode = (RQLNode) pageNodes.get(i);
			pages.add(new Page(this, pageNode.getAttribute("guid")));
		}
		return pages;
	}

	/**
	 * Liefert alle Projektvarianten dieses Projekts zurück. Benötigt den session key!
	 */
	public java.util.List<ProjectVariant> getAllProjectVariants() throws RQLException {

		RQLNodeList variantNodeList = getProjectVariantsNodeList();

		java.util.List<ProjectVariant> result = new ArrayList<ProjectVariant>();
		// wrap all objects
		for (int i = 0; i < variantNodeList.size(); i++) {
			RQLNode node = variantNodeList.get(i);
			result.add(buildProjectVariant(node));
		}
		return result;
	}

	/**
	 * Liefert alle PublicationPackages dieses Projektes. Benötigt den session key!
	 */
	public java.util.List<PublicationPackage> getAllPublicationPackages() throws RQLException {

		RQLNodeList packageNodeList = getPublicationPackagesNodeList();
		java.util.List<PublicationPackage> result = new ArrayList<PublicationPackage>(packageNodeList.size());

		// find export packet
		for (int i = 0; i < packageNodeList.size(); i++) {
			RQLNode node = packageNodeList.get(i);
			String name = node.getAttribute("name");
			result.add(new PublicationPackage(this, node.getAttribute("guid"), name));
		}
		return result;
	}

	/**
	 * Liefert alle Jobreports dieses Projektes. Benötigt den session key!
	 */
	public java.util.List<PublishingJobReport> getAllPublishingJobReports() throws RQLException {

		checkSessionKey();
		/*
		 * V6.5 request <IODATA loginguid="77DF15B4F32D461DBC98DFC15E842969"
		 * sessionkey="1021834323h17n0705483"> <TREESEGMENT type="project.7040"
		 * action="load" /> </IODATA> V6.5 response <IODATA> <TREESEGMENTS>
		 * <SEGMENT guid="418E4ED5DB5E44B7A88B772993AFF9CF" type="project.7045"
		 * image="exportjob.gif" expand="0" value="Business Administration /
		 * Finance (PageID: 160426)" col1value="Business Administration /
		 * Finance (PageID: 160426)" col2fontcolor="#808080"
		 * col2value="Start:27.02.2006/Status:finished" col1fontweight="normal"
		 * col2fontweight="normal"/> <SEGMENT
		 * guid="A6A0ADD02A1B4B25B0AAF213685AEC86" type="project.7045"
		 * image="exportjob.gif" expand="0" value="Business Administration /
		 * Finance (PageID: 160426)" col1value="Business Administration /
		 * Finance (PageID: 160426)" col2fontcolor="#808080"
		 * col2value="Start:27.02.2006/Status:finished" col1fontweight="normal"
		 * col2fontweight="normal"/> ... <SEGMENT
		 * guid="6A6DBE347B234A3185FCB3CAE5D5C3D8" type="project.7045"
		 * image="exportjob.gif" expand="0" value="Update Status (PageID:
		 * 74844)" col1value="Update Status (PageID: 74844)"
		 * col2fontcolor="#808080" col2value="Start:27.02.2006/Status:finished"
		 * col1fontweight="normal" col2fontweight="normal"/> <SEGMENT
		 * guid="A524ACA44DC944DE98154D05B271FFCA" type="project.7045"
		 * image="exportjob.gif" expand="0" value="Update Status (PageID:
		 * 74844)" col1value="Update Status (PageID: 74844)"
		 * col2fontcolor="#808080" col2value="Start:27.02.2006/Status:finished"
		 * col1fontweight="normal" col2fontweight="normal"/> </TREESEGMENTS>
		 * <TREEELEMENT guid="82704E96FE4D46DDA624BA1559C52781" value="Job
		 * Reports" image="menuelement.gif" flags="0" expand="1" descent=""
		 * type="project.7040" col1value="Job Reports" col2fontcolor="#ff8C00"
		 * col2value="" col1fontweight="normal" col2fontweight="normal"/>
		 * </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<TREESEGMENT action='load' type='" + PublishingJobReport.TREESEGMENT_TYPE + "'/>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNodeList nodesOrNull = rqlResponse.getNodes("SEGMENT");

		// no job report available
		java.util.List<PublishingJobReport> reports = new ArrayList<PublishingJobReport>();
		if (nodesOrNull == null) {
			return reports;
		}

		// wrap result into job report objects
		for (int i = 0; i < nodesOrNull.size(); i++) {
			RQLNode node = nodesOrNull.get(i);
			reports.add(new PublishingJobReport(this, node.getAttribute("guid"), node.getAttribute("value"), node
					.getAttribute("col2value")));
		}
		return reports;
	}

	/**
	 * Liefert alle vordefinierten PublishingJobs zurück. Benötigt den session key!
	 */
	public java.util.List<PublishingJob> getAllPublishingJobs() throws RQLException {

		RQLNodeList jobsNodeList = getPublishingJobsNodeList();
		java.util.List<PublishingJob> result = new ArrayList<PublishingJob>();

		// check for empty
		if (jobsNodeList == null) {
			return result;
		}

		// wrap into objects
		for (int i = 0; i < jobsNodeList.size(); i++) {
			RQLNode node = jobsNodeList.get(i);
			result.add(new PublishingJob(this, node.getAttribute("name"), node.getAttribute("guid")));
		}

		return result;
	}

	/**
	 * Returns all publishing jobs were job name contains given namePart.
	 * 
	 * @see String#contains(CharSequence)
	 * @see #getAllPublishingJobs()
	 */
	public java.util.List<PublishingJob> getPublishingJobsNameContains(String namePart) throws RQLException {
		java.util.List<PublishingJob> result = new ArrayList<PublishingJob>();
		for (PublishingJob publishingJob : getAllPublishingJobs()) {
			if (publishingJob.getName().contains(namePart)) {
				result.add(publishingJob);
			}
		}
		return result;
	}

	/**
	 * Liefert alle vordefinierten PublishingJobs zurück. Benötigt den session key!
	 * 
	 * @param isActive
	 *            =true, filter für alle aktiven publishing jobs =false, filter für alle inaktiven
	 */
	private java.util.List<PublishingJob> getAllPublishingJobs(boolean isActive) throws RQLException {
		java.util.List<PublishingJob> result = new ArrayList<PublishingJob>();
		for (PublishingJob publishingJob : getAllPublishingJobs()) {
			if (publishingJob.isActive(isActive)) {
				result.add(publishingJob);
			}
		}
		return result;
	}

	/**
	 * Liefert alle TemplateFolder dieses Projektes zurück.
	 * 
	 * @return list of TemplateFolder instances
	 */
	public java.util.List<TemplateFolder> getAllTemplateFolders() throws RQLException {

		RQLNodeList folderList = getTemplateFoldersNodeList();
		java.util.List<TemplateFolder> folders = new ArrayList<TemplateFolder>();
		for (int i = 0; i < folderList.size(); i++) {
			RQLNode folderNode = folderList.get(i);
			folders.add(buildTemplateFolder(folderNode));
		}
		return folders;
	}

	/**
	 * Liefert alle Templates dieses Projektes zurück.
	 * 
	 * @return list of Template instances
	 */
	public java.util.List<Template> getAllTemplates() throws RQLException {
		java.util.List<Template> result = new ArrayList<Template>();
		for (TemplateFolder folder : getAllTemplateFolders()) {
			result.addAll(folder.getTemplates());
		}
		return result;
	}

	/**
	 * Liefert alle Templates aus dem TemplateFolder mit dem gegebenen Namen.
	 */
	public java.util.List<Template> getAllTemplatesByFolderName(String templateFolderName) throws RQLException {
		checkSessionKey();
		return getTemplateFolderByName(templateFolderName).getTemplates();
	}

	/**
	 * Liefert alle Templates aus den gegebenen TemplateFoldern.
	 */
	public java.util.List<Template> getAllTemplatesByFolderNames(String... templateFolderNames) throws RQLException {
		checkSessionKey();

		java.util.List<Template> result = new ArrayList<Template>();

		// collect from all given folders
		for (String templateFolderName : templateFolderNames) {
			result.addAll(getAllTemplatesByFolderName(templateFolderName));
		}
		return result;
	}

	/**
	 * Gibt alle freien (unverlinkten) Seiten zurück. Das Ergebnis wird nach dem Modifikationsdatum (orderby=2) aufsteigend sortiert
	 * geliefert. ACHTUNG: Die Sortierung ist in V6.5.0.41 nicht korrekt. Es wird augenscheinlich nach creation date sortiert.
	 * 
	 * Das Ergebnis könnte sehr groß werden. Benötigt den session key!
	 * 
	 * @return List of Pages
	 */
	public PageArrayList getAllUnlinkedPagesSortedByModificationDateAsc(int maxPages) throws RQLException {

		checkSessionKey();
		/*
		 * V6.5 request (save query) <IODATA
		 * loginguid="A9149164E8DB42F6B885F9746C989781"
		 * sessionkey="10218343230obnqmDkr15"> <TREE> <QUERY action='save'
		 * treeguid='60B59419CCD64C05B453BBD6EDA62C93' headline='' searchtext=''
		 * maxrecords='30' orderby='2' /> </TREE> </IODATA> V6.5 response (save
		 * query) <IODATA> <QUERY action="save"
		 * treeguid="60B59419CCD64C05B453BBD6EDA62C93" headline="" searchtext=""
		 * maxrecords="30" orderby="2" sessionkey="10218343230obnqmDkr15"
		 * dialoglanguageid="ENG" languagevariantid="ENG"/> </IODATA>
		 * 
		 * V6.5 request (get segments) <IODATA
		 * loginguid="A9149164E8DB42F6B885F9746C989781"
		 * sessionkey="10218343230obnqmDkr15"> <TREESEGMENT type="app.1805"
		 * action="load" guid="60B59419CCD64C05B453BBD6EDA62C93" descent="app" />
		 * </IODATA> V6.5 response (get segments) <IODATA> <TREESEGMENTS>
		 * <SEGMENT parentguid="" guid="CD1C817088104BFCBD45A1DCE6985264"
		 * type="page" image="page.gif" pageid="612" flags="8192" expand="1"
		 * value="Backup Text" col1value="Backup Text" col2fontcolor="#808080"
		 * col2value="16.04.2003" col1fontweight="bold"
		 * col2fontweight="normal"/> <SEGMENT parentguid=""
		 * guid="21D5876DB4C548B9A26D9D8D76F73CB1" type="page" image="page.gif"
		 * pageid="611" flags="8192" expand="1" value="Backup Image"
		 * col1value="Backup Image" col2fontcolor="#808080"
		 * col2value="16.04.2003" col1fontweight="bold"
		 * col2fontweight="normal"/> ... <SEGMENT parentguid=""
		 * guid="396A980434C6437E87C3F3566D2C53AA" type="page" image="page.gif"
		 * pageid="3897" flags="524288" expand="1" value="Preview of Report
		 * Slides" col1value="Preview of Report Slides" col2fontcolor="#808080"
		 * col2value="07.10.2003" col1fontweight="bold"
		 * col2fontweight="normal"/> </TREESEGMENTS> <TREEELEMENT
		 * guid="60B59419CCD64C05B453BBD6EDA62C93" value="Unlinked Pages"
		 * image="freepages.gif" flags="0" expand="1" descent="app"
		 * type="app.1805" col1value="Unlinked Pages" col2fontcolor="#ff8C00"
		 * col2value="" col1fontweight="normal" col2fontweight="normal"/>
		 * </IODATA>
		 */

		// save query
		// 1=by headline, 2=by modification date, 3=by creation date
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<TREE><QUERY action='save' treeguid='60B59419CCD64C05B453BBD6EDA62C93' orderby='2' maxrecords='" + maxPages + "'/>"
				+ "</TREE></IODATA>";
		callCmsWithoutParsing(rqlRequest);

		// call cms to get tree segments for pages
		// guid seems to be fix in every project and installation
		rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<TREESEGMENT type='app.1805' action='load' guid='60B59419CCD64C05B453BBD6EDA62C93' descent='app' />" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNodeList pageNodes = rqlResponse.getNodes("SEGMENT");

		// convert result
		PageArrayList pages = new PageArrayList();
		if (pageNodes == null) {
			return pages;
		}
		for (int i = 0; i < pageNodes.size(); i++) {
			RQLNode segmentNode = (RQLNode) pageNodes.get(i);
			pages.add(new Page(this, segmentNode.getAttribute("guid"), segmentNode.getAttribute("pageid"), segmentNode
					.getAttribute("col1value")));
		}
		return pages;
	}

	/**
	 * Liefert alle Urls auf allen Seiten dieses Projektes. Benötigt den session key!
	 * 
	 * @return <code>java.util.List of String</code>
	 */
	public java.util.List<String> getAllUrls() throws RQLException {

		checkSessionKey();
		/*
		 * V5 request <IODATA loginguid="CBBDFEB7B65048149F8A9287A6C7A255"
		 * user="lejafr"> <PROJECT sessionkey="92570553040O3J3CGm05"
		 * projectguid="06BE79A1D9F549388F06F6B649E27152"> <URLS action="list" />
		 * </PROJECT> </IODATA> V5 response <IODATA><URLS
		 * date="38309.6363657407" > <URL
		 * guid="FF0334650A78479AAEF69C9E4B7D11E0"
		 * src="http://www.canconf.com/atlantique/en/index.htm" status="2" />
		 * <URL guid="FEB8E5B88B9B410C8E4AF759012440D6"
		 * src="http://www.bahamasmaritime.com" status="2" /> ... <URL
		 * guid="0434750B15644C2889426E621A533087" src="http://www.gaports.com"
		 * status="2" /> <URL guid="0385C65B8F2540C48936155B95042D88"
		 * src="http://www.earthcalendar.net/index.php" status="2" /> <URL
		 * guid="00D42804BA8640C29467263C0C60EFD7"
		 * src="http://www.wtsacarriers.org" status="2" /> </URLS> </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT guid='"
				+ getProjectGuid() + "'>" + "   <URLS action='list'/>" + "  </PROJECT>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNodeList urlNodeList = rqlResponse.getNodes("URL");

		// wrap into URL objects
		java.util.List<String> urls = new ArrayList<String>();
		for (int i = 0; i < urlNodeList.size(); i++) {
			RQLNode node = urlNodeList.get(i);
			urls.add(node.getAttribute("src"));
		}

		return urls;
	}

	/**
	 * Liefert eine List mit allen Usern dieses Projektes zurück.
	 */
	public java.util.List<User> getAllUsers() throws RQLException {

		return wrapUserNodes(getAllUsersNodeList());
	}

	/**
	 * Liefert alle Benutzer aus allen Gruppen dieses Projektes, deren Name mit dem prefix beginnen.
	 * 
	 * @return <code>java.util.List</code> of <code>UserGroup</code>s
	 */
	public Set<User> getAllUsersForGroupsNameStartsWith(String prefix) throws RQLException {
		java.util.List<UserGroup> groups = getUserGroupsNameStartsWith(prefix);
		Set<User> result = new HashSet<User>();
		for (UserGroup group : groups) {
			result.addAll(group.getUsers());
		}
		return result;
	}

	/**
	 * Liefert die RQLNodeList mit allen Usern dieses Projektes. Benötigt den session key!
	 * 
	 * @return <code>RQLNodeList</code>
	 */
	private RQLNodeList getAllUsersNodeList() throws RQLException {

		checkSessionKey();
		/*
		 V5 request 
		 <IODATA loginguid="A738357A554044538BCFDA1B5C9853A9">
		 <ADMINISTRATION> 
		 <PROJECT guid="094C0E751B524A83BD9707889135B5DD">
		 <USERS action="list"/> 
		 </PROJECT> 
		 </ADMINISTRATION> 
		 </IODATA> 
		 V5 response 
		 <IODATA> 
		 <PROJECT guid="094C0E751B524A83BD9707889135B5DD">
		 <USERS action="list" parentguid="094C0E751B524A83BD9707889135B5DD">
		 <USER guid="78B1031629534899AC64936B5FFFCAA8" id="283" name="beckeni" fullname="Nina Becker" email="beckeni@hlag.de" accountsystemguid="00000000000000000000000000000200" dialoglanguageid="DEU" logindate="38203.5100347222"/> 
		 <USER guid="03E8A2FEEDE2449D861AC090C31AC948" id="32" name="bergefr" fullname="Frank Bergert" email="frank.bergert@hlag.de" accountsystemguid="00000000000000000000000000000200" dialoglanguageid="DEU" logindate="0"/> 
		 ... 
		 </USERS> 
		 </PROJECT>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + " <ADMINISTRATION>"
				+ "  <PROJECT guid='" + getProjectGuid() + "'>" + "   <USERS action='list'/>" + "  </PROJECT>" + " </ADMINISTRATION>"
				+ "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		return rqlResponse.getNodes("USER");
	}

	/**
	 * Liefert den AssetManager-Ordner mit dem gegebenen Namen oder null, falls kein AssetManager für den Namen gefunden wurde.
	 * Benötigt den session key!
	 * 
	 * @param assetManagerFolderName
	 *            Name des AssetManager-Ordners.
	 * @return AssetManagerFolder
	 * @see Folder
	 */
	public AssetManagerFolder getAssetManagerByName(String assetManagerFolderName) throws RQLException {

		RQLNode folderNode = findFolderNodeByName(assetManagerFolderName);
		if (folderNode.getAttribute("catalog").equals("1")) {
			return new AssetManagerFolder(this, folderNode.getAttribute("name"), folderNode.getAttribute("guid"), folderNode
					.getAttribute("hideintexteditor"), folderNode.getAttribute("savetype"), folderNode.getAttribute("path"));
		}
		return null;
	}

	/**
	 * Liefert den AssetManager-Subfolder für die gegebenen Foldernamen. Liefert null, falls kein AssetManagerFolder gefunden wurde.
	 * Benötigt den session key!
	 * 
	 * @param assetManagerFolderSlashSubfolderName
	 *            Name des AssetManager-Ordners/Name des Unterordners; e.g. view_pdf/orga_charts_pdf
	 * @throws WrongParameterFormatException
	 *             if folder names are not separated by / (a slash)
	 * @throws ElementNotFoundException
	 *             if sub folder did not exists
	 */
	public AssetManagerSubFolder getAssetManagerSubFolderByName(String assetManagerFolderSlashSubfolderName) throws RQLException {
		// check format
		if (!assetManagerFolderSlashSubfolderName.contains(ASSETMANAGER_SUBFOLDER_DELIMITER)) {
			throw new WrongParameterFormatException(
					"The name of the asset manager folder needs to be separated from the subfolder name by / (a slash).");
		}
		// split
		String[] parts = StringHelper.split(assetManagerFolderSlashSubfolderName, ASSETMANAGER_SUBFOLDER_DELIMITER);

		// get asset manager
		AssetManagerFolder assetManagerFolder = getAssetManagerByName(parts[0]);
		// signal not found
		if (assetManagerFolder == null) {
			return null;
		}
		// return sub folder
		return assetManagerFolder.getSubFolderByName(parts[1]);
	}

	/**
	 * Liefert die geparste Antwort ffür die Anfrage nach allen asynchronen Jobs.
	 * <p>
	 * Kann nur von Administratoren verwendet werden!
	 */
	RQLNode getAsyncJobsRootNode() throws RQLException {
		/*
		 * V7.5 request 
		<IODATA loginguid="D50FDC1066964CC0940BB591EDF2495E" sessionkey="2427CFED9AFF440AAACA7255335D43BE">
		<ADMINISTRATION> 
		<ASYNCQUEUE action="list"	 project="06BE79A1D9F549388F06F6B649E27152"/> 
		</ADMINISTRATION>
		</IODATA>
		 V7.5 request 
		<IODATA>
		<PROCESSLIST1>
		<ASYNCQUEUE guid="2E6FF3E9F15F40F2A2E3F0846D07ED10" nextaction="" now="1" name="a sales tree to hip PROD" editorialserver="" editorialservername="" server="4A1FD4B9F4CF4531B7D0ED472BC17926" servername="kswfrd02" category="0" active="1" status="2" lastexecute="39924.9791666667" lastexecuteuser="" nextexecute="39924.9793055556" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="FCA44A7B64254D01810B0EF5C4375398"/>
		<ASYNCQUEUE guid="6C946950577342B88A1A841C857B955F" nextaction="" now="1" name="a operations tree to hip PROD" editorialserver="" editorialservername="" server="4A1FD4B9F4CF4531B7D0ED472BC17926" servername="kswfrd02" category="0" active="1" status="2" lastexecute="39924.9791666667" lastexecuteuser="" nextexecute="39924.9793171296" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="75CA5F911E80434AAA9340CE4E42C651"/>
		<ASYNCQUEUE guid="878D5B524ADB46C4822BC675EEF084BD" nextaction="" now="1" name="a company tree to hip PROD" editorialserver="" editorialservername="" server="4A1FD4B9F4CF4531B7D0ED472BC17926" servername="kswfrd02" category="0" active="1" status="2" lastexecute="39924.9791666667" lastexecuteuser="" nextexecute="39924.9793287037" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="6F80EF0657754726A4D8A98579D1515F"/>
		<ASYNCQUEUE guid="E3433D4036BB4A1889BFBE1ADDFA976A" nextaction="" now="1" name="a customer service tree to hip PROD" editorialserver="" editorialservername="" server="4A1FD4B9F4CF4531B7D0ED472BC17926" servername="kswfrd02" category="0" active="1" status="2" lastexecute="39924.9791666667" lastexecuteuser="" nextexecute="39924.9793402778" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="E458C204AC9A40F78F33CBB9B823FD5A"/>
		<ASYNCQUEUE guid="D861515B09C048DEAFD60D90FD7144A4" nextaction="" now="1" name="a human resources tree to PROD" editorialserver="" editorialservername="" server="4A1FD4B9F4CF4531B7D0ED472BC17926" servername="kswfrd02" category="0" active="1" status="2" lastexecute="39922.3020717593" lastexecuteuser="" nextexecute="39925.0819212963" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="0BFDB1D9A3E34F358369B7B5F1D70D2F"/>
		</PROCESSLIST1>
		<PROCESSLIST2>
		<ASYNCQUEUE guid="155E16CDE78043F1A6971617CBDD8408" nextaction="" now="0" name="FTP-Transfer for hip.hlcl.com" editorialserver="4A1FD4B9F4CF4531B7D0ED472BC17926" editorialservername="kswfrd02" server="4A1FD4B9F4CF4531B7D0ED472BC17926" servername="kswfrd02" category="17" active="1" sendstartat="39613.5928472222" automatic="1" status="0" lastexecute="39925.4282523148" lastexecuteuser="" nextexecute="39925.4291898148" nextexecuteuser="" priority="1" user="" username="" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="4A1FD4B9F4CF4531B7D0ED472BC17926"/>
		<ASYNCQUEUE guid="CADA4ACCFF414A5CA6312A84F441734D" nextaction="" now="0" name="Publishing Queue for hip.hlcl.com" editorialserver="" editorialservername="" server="" servername="" category="15" active="1" sendstartat="0" automatic="1" status="0" lastexecute="0" lastexecuteuser="" nextexecute="39925.4462037037" nextexecuteuser="" priority="1" user="" username="" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="224F67FC76A7470CBE2BBCD83B52CDBA"/>
		<ASYNCQUEUE guid="BAE1A5CFE04D478AAE1EFD51A97197BA" nextaction="" now="0" name="LiveServerCleaner hip.hlcl.com" editorialserver="" editorialservername="" server="" servername="" category="1" active="1" sendstartat="0" automatic="1" status="0" lastexecute="39925.3846875" lastexecuteuser="" nextexecute="39925.4478587963" nextexecuteuser="" priority="1" user="" username="" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="58D0963F9F3D433384A4853DD620E512"/>
		<ASYNCQUEUE guid="DCF1756886784B4F9E127E6E080B0CD6" nextaction="" now="0" name="a welcome archive to PROD" editorialserver="" editorialservername="" server="" servername="" category="0" active="1" sendstartat="39619.5600231482" automatic="1" status="0" lastexecute="39924.8144560185" lastexecuteuser="" nextexecute="39925.8125" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="627495EC49C043648D06382B729053DE"/>
		<ASYNCQUEUE guid="98323EAD1365458F92AE4441682DD805" nextaction="" now="0" name="a Start CMS gateway page to PROD" editorialserver="" editorialservername="" server="" servername="" category="0" active="1" sendstartat="39619.5599074074" automatic="1" status="0" lastexecute="39924.8139236111" lastexecuteuser="" nextexecute="39925.8125" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="EF8E09602F6647DE8D9245624C9F5040"/>
		<ASYNCQUEUE guid="2BDE3ACEAA654912BB0006C08E9EE159" nextaction="" now="0" name="a learn pages to hip PROD" editorialserver="" editorialservername="" server="" servername="" category="0" active="1" sendstartat="39619.5593865741" automatic="1" status="0" lastexecute="39924.8125694444" lastexecuteuser="" nextexecute="39925.8125" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="7E79F5DA930D455D882454111CF875DC"/>
		<ASYNCQUEUE guid="2C4AFE9A016941A2BF2D4FA057DEC6D5" nextaction="" now="0" name="a welcome page to PROD" editorialserver="" editorialservername="" server="" servername="" category="0" active="1" sendstartat="39619.5602083333" automatic="1" status="0" lastexecute="39924.8125925926" lastexecuteuser="" nextexecute="39925.8125" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="81A96E9842184E85B5483982E5D71D78"/>
		<ASYNCQUEUE guid="309B55045D5D4600B4E564B65C184102" nextaction="" now="0" name="a CPS files to PROD" editorialserver="" editorialservername="" server="" servername="" category="0" active="1" sendstartat="39422.4433449074" automatic="1" status="0" lastexecute="39924.8126041667" lastexecuteuser="" nextexecute="39925.8125" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="B4335CD965F340EDB0DDFA0F77769E65"/>
		<ASYNCQUEUE guid="41177E591B76453CB54E71B1DCC7D148" nextaction="" now="0" name="a help pages to PROD" editorialserver="" editorialservername="" server="" servername="" category="0" active="1" sendstartat="39619.5590856482" automatic="1" status="0" lastexecute="39924.812650463" lastexecuteuser="" nextexecute="39925.8125" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="C8AD26131602462BA2619F6385AE2CBC"/>
		<ASYNCQUEUE guid="606C2E54B14148E8B85B40351DC4E3D2" nextaction="" now="0" name="a local tree to PROD" editorialserver="" editorialservername="" server="" servername="" category="0" active="1" sendstartat="39619.5595023148" automatic="1" status="0" lastexecute="39924.9793171296" lastexecuteuser="" nextexecute="39925.9791666667" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="FCE29D439D15437A85CAFA17D8A6C919"/>
		<ASYNCQUEUE guid="1F24D37525734F52838E5551C7045426" nextaction="" now="0" name="a business admin tree to PROD" editorialserver="" editorialservername="" server="" servername="" category="0" active="1" sendstartat="39619.5533333333" automatic="1" status="0" lastexecute="39923.9847453704" lastexecuteuser="" nextexecute="39925.9791666667" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="0940D932D95D4DAE8EBD6060DD57D33D"/>
		<ASYNCQUEUE guid="13C8FE18222B42678E10C819AA89A395" nextaction="" now="0" name="z for external http server" editorialserver="" editorialservername="" server="" servername="" category="0" active="0" sendstartat="37893.6665509259" automatic="1" status="0" lastexecute="38513.3990162037" lastexecuteuser="4324D172EF4342669EAF0AD074433393" nextexecute="39418" nextexecuteuser="" priority="0" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="lejafr" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="32254BD6586D4B31A9103B0364DDEF93"/>
		<ASYNCQUEUE guid="AB4DF12A7A8F43EFBE1C6E3B1C3679A6" nextaction="" now="0" name="Proc tree to PROD CPS on kswfip0 (splitted night job)" editorialserver="" editorialservername="" server="" servername="" category="0" active="0" sendstartat="38541.6004976852" automatic="1" status="0" lastexecute="39422.1666666667" lastexecuteuser="" nextexecute="39422.1666666667" nextexecuteuser="" priority="0" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="1EAE0DFABEF74E958CE23F2513ED67C8"/>
		<ASYNCQUEUE guid="8F4ED48C27764603BF1CE8C380D08D18" nextaction="" now="0" name="Org tree to PROD CPS on kswfip0 (splitted night job)" editorialserver="" editorialservername="" server="" servername="" category="0" active="0" sendstartat="38541.5999421296" automatic="1" status="0" lastexecute="39422.1666666667" lastexecuteuser="" nextexecute="39422.1666666667" nextexecuteuser="" priority="0" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="C0FD5D57A88245288151861F911CEB68"/>
		<ASYNCQUEUE guid="9B1572D5A7D64885B5DEB53B78ED27BD" nextaction="" now="0" name="z DVD Export whole HIP nach \\kswfrd01\cms_data\published\hip.hlcl.com_fs" editorialserver="" editorialservername="" server="" servername="" category="0" active="0" sendstartat="39462.5089236111" automatic="1" status="0" lastexecute="39791.5461805556" lastexecuteuser="4324D172EF4342669EAF0AD074433393" nextexecute="39795.1666666667" nextexecuteuser="" priority="1" user="4324D172EF4342669EAF0AD074433393" username="lejafr" lastexecuteusername="lejafr" nextexecuteusername="" project="06BE79A1D9F549388F06F6B649E27152" projectname="hip.hlcl.com" jobguid="FF8533218EA143019D5683D288B09F67"/>
		</PROCESSLIST2>
		</IODATA>

		<ASYNCQUEUE guid= guid of the running job, not the definition
		<ASYNCQUEUE jobguid= guid of the defined job, not the running/waiting job (but also filled for not defined jobs with something!)
		 */

		// build request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<ADMINISTRATION>"
				+ "   <ASYNCQUEUE action='list' project='" + getProjectGuid() + "'/>" + "</ADMINISTRATION>" + "</IODATA>";
		// call CMS
		return callCms(rqlRequest.toString());
	}

	/**
	 * Liefert das Berechtigungspaket (Typ=Normal=0, an Seiten) für die gegebene GUID zurück.
	 * 
	 * @throws ElementNotFoundException
	 */
	public AuthorizationPackage getAuthorizationPackageForPageByGuid(String authorizationPackageGuid) throws RQLException {
		return findAuthorizationPackageByGuid(AuthorizationPackage.NORMAL_TYPE, authorizationPackageGuid);
	}

	/**
	 * Liefert das Berechtigungspaket (Typ=Normal=0, an Seiten) mit dem gegebenen Namen zurück.
	 * 
	 * @param packageName
	 *            Name des Berechtigungspaketes (case ignored!)
	 * @throws ElementNotFoundException
	 */
	public AuthorizationPackage getAuthorizationPackageForPageByName(String packageName) throws RQLException {
		return findAuthorizationPackageByName(AuthorizationPackage.NORMAL_TYPE, packageName);
	}

	/**
	 * Liefert die Nodelist für alle Berechtigungspakete des gegebenen Typs dieses Projekts zurück.
	 * 
	 * @param type
	 *            Typ des Berechtigungspaketes laut RQL Doku
	 * @see AuthorizationPackage
	 */
	private RQLNodeList getAuthorizationPackageNodeList(String type) throws RQLException {

		checkSessionKey();
		/*
		 V7.5 request (changed!) 
		 <IODATA loginguid='4B2C147944A4416BAB506F89EF553AA8' sessionkey='69E9678ECE694A8A83B125EC2770240C'>
		 <AUTHORIZATION>
		 <AUTHORIZATIONS action='list' type='1'/>
		 </AUTHORIZATION>
		 </IODATA>
		 V7.5 response <IODATA><AUTHORIZATIONS> <AUTHORIZATION
		 guid="5F2D83696BF84808B8D89B6AD7A71B80"
		 name="announcements_forbidden_functions2" projectvariantguid=""
		 languagevariantguid="" type="2"/> <AUTHORIZATION
		 guid="0DAFF46FBDE5416AA487BD52A73FEF53"
		 name="delete_pages_content_pages_list_restrictions"
		 projectvariantguid="" languagevariantguid="" type="2"/>
		 <AUTHORIZATION guid="4FEEFEBC62BE43B88020C62FCDFB0B13"
		 name="delete_pages_filename_list_restrictions" projectvariantguid=""
		 languagevariantguid="" type="2"/> ... <AUTHORIZATION
		 guid="AC6FC76AE3F7490C942AC44B67319FFD"
		 name="work_area_root_authorizations" projectvariantguid=""
		 languagevariantguid="" type="2"/> <AUTHORIZATION
		 guid="3BC413DD750749ED9351F5B4B32B3E31"
		 name="work-area-rights2_betriebsrat_hlcl+hlag" projectvariantguid=""
		 languagevariantguid="" type="2"/> </AUTHORIZATIONS> </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<AUTHORIZATION><AUTHORIZATIONS action='list' type='" + type + "'/>" + "</AUTHORIZATION></IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNodes("AUTHORIZATION");
	}

	/**
	 * Liefert das aufgebaute Clipboard Table HTML in der von RQL erwarteten Form (escaped) zurück.
	 * <p>
	 */
	String getClipboardEscaped() {
		String escapedResult = "";
		for (int i = 0; i < clipboardTableHtml.length(); i++) {
			Character c = new Character(clipboardTableHtml.charAt(i));
			if (Character.isLetterOrDigit(c) || c.equals('/') || c.equals('.') || c.equals('_') || c.equals('-')) {
				escapedResult += c;
			} else {
				escapedResult += "%" + String.format("%X", (int) c);
			}
		}
		return escapedResult;
	}

	/**
	 * Liefert den Container des Projectes.
	 * 
	 */
	public CmsClient getCmsClient() {

		return cmsClient;
	}

	/**
	 * Erzeugt einen Container für die gegebene linkGuid. Benötigt den session key!
	 * 
	 * @param linkGuid
	 *            RedDot GUID des Containers
	 * @return List or Container
	 */
	public Container getContainerByGuid(String linkGuid) throws RQLException {

		return getPageFromLinkGuid(linkGuid).getContainerByGuid(linkGuid);
	}

	/**
	 * Liefert den Namen der gerade im Projekt gewählten Sprachvariante zurück.
	 */
	public String getCurrentLanguageVariantName() throws RQLException {
		return getCurrentLanguageVariant().getName();
	}

	/**
	 * Liefert den von RD intern genutzten language code der gerade im Projekt gewählten Sprachvariante zurück.
	 */
	public String getCurrentLanguageVariantLanguageCode() throws RQLException {
		return getCurrentLanguageVariant().getLanguageCode();
	}

	/**
	 * Liefert die rfc language id (en, de, zh, es) der gerade im Projekt gewählten Sprachvariante zurück.
	 * 
	 * @see LanguageVariant#getRfcLanguageId()
	 */
	public String getCurrentLanguageVariantRfcLanguageId() throws RQLException {
		return getCurrentLanguageVariant().getRfcLanguageId();
	}

	/**
	 * Liefert die gerade im Projekt gewählte Sprachvariante zurück.
	 */
	public LanguageVariant getCurrentLanguageVariant() throws RQLException {
		RQLNodeList languageNodeList = getLanguageVariantsNodeList();
		// find checked language variant
		for (int i = 0; i < languageNodeList.size(); i++) {
			RQLNode node = languageNodeList.get(i);
			if (node.getAttribute("checked").equals("1")) {
				return buildLanguageVariant(node);
			}
		}
		throw new ElementNotFoundException(
				"There seems to be no language variant choosen in the current project, what is impossible in fact.");
	}

	/**
	 * Liefert das Detailberechtigungspaket (Typ=AssetManagerAttribute=4) mit dem gegebenen Namen zurück.
	 * 
	 * @param packageName
	 *            Name des Detailberechtigungspaketes (case ignored!)
	 * @throws ElementNotFoundException
	 */
	public AuthorizationPackage getDetailedAuthorizationPackageForAssetManagerAttributeByName(String packageName) throws RQLException {
		return findAuthorizationPackageByName(AuthorizationPackage.DETAILED_ASSET_MANAGER_ATTRIBUT_TYPE, packageName);
	}

	/**
	 * Liefert das Detailberechtigungspaket (Typ=Element=4) mit dem gegebenen Namen zurück.
	 * 
	 * @param packageName
	 *            Name des Detailberechtigungspaketes (case ignored!)
	 * @throws ElementNotFoundException
	 */
	public AuthorizationPackage getDetailedAuthorizationPackageForElementByName(String packageName) throws RQLException {
		return findAuthorizationPackageByName(AuthorizationPackage.DETAILED_ELEMENT_TYPE, packageName);
	}

	/**
	 * Liefert das Detailberechtigungspaket (Typ=Link=2) mit dem gegebenen Namen zurück.
	 * 
	 * @param packageName
	 *            Name des Detailberechtigungspaketes (case ignored!)
	 * @throws ElementNotFoundException
	 */
	public AuthorizationPackage getDetailedAuthorizationPackageForLinkByName(String packageName) throws RQLException {
		return findAuthorizationPackageByName(AuthorizationPackage.DETAILED_LINK_TYPE, packageName);
	}

	/**
	 * Liefert das Detailberechtigungspaket (Typ=Link=2) mit der angegeben guid zurueck.
	 * 
	 * @throws ElementNotFoundException
	 */
	public AuthorizationPackage getDetailedAuthorizationPackageForLinkByGuid(String packageGuid) throws RQLException {
		return findAuthorizationPackageByGuid(AuthorizationPackage.DETAILED_LINK_TYPE, packageGuid);
	}

	/**
	 * Liefert das Detailberechtigungspaket (Typ=Page=1) mit dem gegebenen Namen zurück.
	 * 
	 * @param packageName
	 *            Name des Detailberechtigungspaketes (case ignored!)
	 * @throws ElementNotFoundException
	 */
	public AuthorizationPackage getDetailedAuthorizationPackageForPageByName(String packageName) throws RQLException {
		return findAuthorizationPackageByName(AuthorizationPackage.DETAILED_PAGE_TYPE, packageName);
	}

	/**
	 * Liefert den RQLNode für dieses Projekt. Nur von Admins aufrufbar!
	 * 
	 * @return <code>RQLNode</code>
	 */
	private RQLNode getDetailsNode() throws RQLException {
		/*
		 V5 request 
		 <IODATA loginguid="7D209C6A757B44D1B5C9D183389214D6">
		 <ADMINISTRATION> <PROJECT action="load" guid="5256C671655D4CE696F663C73CE3E526"/> 
		 </ADMINISTRATION> </IODATA>
		 V5 response 
		 <IODATA> 
		 <PROJECT guid="5256C671655D4CE696F663C73CE3E526" name="hlcl_relaunch_2004" description="" versioning="0"
		 inhibitlevel="3" lockinfo="Project blocked." databasename="hlcl_relaunch_2004" servername="khh30006"
		 createallowed="1" dbtypeid="3"	 editorialserverguid="2AEE6595C20E441994F08CBF6E16067B"
		 databaseserverguid="9461EDDA4E2A4C9D94DBB0BDB3895015"	 projectversion="002.043"	 reddotstartpageguid="69060E5AA60B4AD6988FC00A68079E34"
		 lockedbysystem="0"/> 
		 </IODATA>
		 */
		if (detailsNodeCache == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "  <ADMINISTRATION>"
					+ "   <PROJECT action='load' guid='" + getProjectGuid() + "'/>" + "  </ADMINISTRATION>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);

			detailsNodeCache = rqlResponse.getNode("PROJECT");
		}

		return detailsNodeCache;
	}

	/**
	 * Liefert den Dateiordner für die GUID folderGuid vom CMS zurück. Dieser Zugriff benutzt einen Objektcache. Benötigt den session
	 * key!
	 * 
	 * @param folderGuid
	 *            GUID des Dateiordners.
	 * @return Folder
	 * @see <code>Folder</code>
	 */
	public Folder getFolderByGuid(String folderGuid) throws RQLException {
		if (foldersCache.containsKey(folderGuid)) {
			return foldersCache.get(folderGuid);
		} else {
			// retrieve new folder object
			Folder folder = buildFolder(findFolderNodeByGuid(folderGuid));
			// and cache it for reuse
			foldersCache.put(folderGuid, folder);
			return folder;
		}
	}

	/**
	 * Liefert den Dateiordner mit dem gegebenen Namen vom CMS zurück. Dieser Zugriff liefert immer neue Objekte. Benötigt den session
	 * key!
	 * 
	 * @param name
	 *            Name des Dateiordners.
	 * @return Folder
	 * @see <code>Folder</code>
	 */
	public Folder getFolderByName(String name) throws RQLException {

		return buildFolder(findFolderNodeByName(name));
	}

	/**
	 * Liefert alle Ordner (TemplateFolder, FileFolder and AssetManager) dieses Projektes. Benötigt den session key!
	 */
	public java.util.List<Folder> getFolders() throws RQLException {
		RQLNodeList nodes = getFoldersNodeList();
		java.util.List<Folder> result = new ArrayList<Folder>();

		for (int i = 0; i < nodes.size(); i++) {
			RQLNode node = nodes.get(i);
			result.add(buildFolder(node));
		}
		return result;
	}

	/**
	 * Returns all cms internal file folders of this project. Includes AssetManagers, but excludes templates folders and all other
	 * folder types.
	 * 
	 * @see #getAllTemplateFolders()
	 */
	private RQLNodeList getFoldersNodeList() throws RQLException {

		checkSessionKey();
		/*
		V7.5 request
		<IODATA loginguid="E174CB71B7934B0FA4A6DAA1FA6C7549" sessionkey="205C36CC27CC4B57A76340B79620A761">
		<PROJECT> 
		<FOLDERS action="list"/>
		</PROJECT>
		</IODATA>
		V7.5 response
		<IODATA><FOLDERS>
		<FOLDER guid="F3AE1D5712D54347B4764955CA657A67" name="1_images_TEXTEDITOR" foldertype="0" catalog="1" savetype="0" path="" description="" dms="" folderrights="2147483647" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="0407DDF4EADA4DF3962A4EBD3E00F7E6" name="admin_templates" foldertype="1" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="6A6740BC44F7459081BFD1F25B1BF8F6" name="content_templates" foldertype="1" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="32D754639EE24313920AEAB4FB163FF2" name="cps_templates" foldertype="1" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="B095BCF03ABF4D44AF932291068BF845" name="cpships_images" foldertype="0" catalog="1" savetype="0" path="" description="" dms="" folderrights="2147483647" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="011E2C5DF8A54F4AAE254A72A9D9AAE0" name="cpships_images_layout" foldertype="0" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="AFAE8458A0984621942066B11CC7DADA" name="cpships_teu_news" foldertype="0" catalog="1" savetype="2" path="\\kswfrd03\cms_data\content\hip.hlcl.com\cpships_images_teu_news" description="" dms="" folderrights="2147483647" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="EE1695282323497CA90385B4DEB96326" name="cpships_view_pdf" foldertype="0" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="5B0E9B38D7734996A43D7799FEB9906F" name="database_content_templates" foldertype="1" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="EB55CEA852F64C7099C60021171535A9" name="downloads" foldertype="0" catalog="0" savetype="2" path="\\kswfrd03\cms_data\content\hip.hlcl.com\downloads" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="72904B4D243C4B5EA3E458FE9E019AB9" name="images" foldertype="0" catalog="1" savetype="0" path="" description="" dms="" folderrights="2147483647" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="909CD85450D84DC1AF118933FB7A0AF5" name="images_article" foldertype="0" catalog="1" savetype="0" path="" description="" dms="" folderrights="2147483647" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="1A1BFD47461F4BAA9F371F1EF0CF3021" name="images_category" foldertype="0" catalog="1" savetype="0" path="" description="" dms="" folderrights="2147483647" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="C66CE50C719B4F13BE25EBF53895AC0D" name="images_filetype" foldertype="0" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="0881C762764541259266C8392018AB05" name="images_gallery" foldertype="0" catalog="1" savetype="0" path="" description="" dms="" folderrights="2147483647" hideintexteditor="1">
		</FOLDER>
		<FOLDER guid="C0E551CAD2D94DD6A418A89A4972B924" name="images_hidden" foldertype="0" catalog="1" savetype="0" path="" description="" dms="" folderrights="2147483647" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="D43C8053E2F4441B9FEF9BB94211FFB2" name="images_language" foldertype="0" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="1399FC72ABCB4E839DE8D91E83CCDCFF" name="images_layout" foldertype="0" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="A9DD33A18B334D48B560E4D95987AE69" name="images_map" foldertype="0" catalog="1" savetype="2" path="\\kswfrd03\cms_data\content\hip.hlcl.com\images_map\" description="" dms="" folderrights="2147483647" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="75DF1FFD53ED4D3A854D42B0F92B0D27" name="images_map_master" foldertype="0" catalog="1" savetype="0" path="" description="" dms="" folderrights="2147483647" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="1A9B9AE56A5449B096C547E2A0360C6C" name="images_slide" foldertype="0" catalog="1" savetype="2" path="\\kswfrd03\cms_data\content\hip.hlcl.com\images_slide" description="" dms="" folderrights="2147483647" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="4C5E6DEA5CF4424DB44553E12484B54B" name="navigation_templates" foldertype="1" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="1758C106CC0545F9B0654B633CDDCBF1" name="old_unused_cpships_templates" foldertype="1" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="E3089F2700E54968A3EA948FC0BE1B2C" name="old_unused_templates" foldertype="1" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="D981C2A5D30640D4B3EB62F5A9076EF2" name="orgcharts_pdf" foldertype="0" catalog="0" savetype="2" path="\\kswfrd03\cms_data\content\hip.hlcl.com\ftp\orgcharts_pdf" description="" dms="" hideintexteditor="1">
		</FOLDER>
		<FOLDER guid="E4B0FBDD289C43DC8BBDBC8243ED7E85" name="rql_templates" foldertype="1" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="3D87E402FFB045CEBED62C74CDB9518D" name="test" foldertype="0" catalog="1" savetype="0" path="" description="" dms="" folderrights="2147483647" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="DD8BDB6FBA954470B6F9466032653F30" name="test_templates" foldertype="1" catalog="0" savetype="0" path="" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="DB7234C07B6947EE8AACD6E271DDF32C" name="view_html" foldertype="0" catalog="0" savetype="2" path="\\kswfrd03\cms_data\content\hip.hlcl.com\ftp\view_html" description="" dms="" hideintexteditor="0">
		</FOLDER>
		<FOLDER guid="1B0280BBF0E64E70AF7D1D5575E52510" name="view_pdf" foldertype="0" catalog="1" savetype="2" path="\\kswfrd03\cms_data\content\hip.hlcl.com\ftp\view_pdf\" description="" dms="" folderrights="2147483647" hideintexteditor="0">
		</FOLDER>
		</FOLDERS>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PROJECT>"
				+ "   <FOLDERS action='list' foldertype='0'/>" + " </PROJECT>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		return rqlResponse.getNodes("FOLDER");
	}

	/**
	 * Liefert eine JobQueue mit allen jobs aus Tag ProcessList1.
	 * <p>
	 * Kann nur von Administratoren verwendet werden!
	 * 
	 * @see JobQueue
	 * @param category
	 *            filter: for possible values see JobQueue
	 * @param status
	 *            filter: for possible values see JobQueue TODO runningJobsNodeList == null auf khh31004!
	 */
	private JobQueue getJobs(String category, String status) throws RQLException {

		RQLNode rqlResponse = getAsyncJobsRootNode();

		// collect all currect jobs
		java.util.List<PublishingJob> jobs = new ArrayList<PublishingJob>();
		RQLNode list1Node = rqlResponse.getNode("PROCESSLIST1");
		if (list1Node != null) {
			RQLNodeList runningJobsNodeList = list1Node.getNodes("ASYNCQUEUE");
			if (runningJobsNodeList != null) {
				for (int i = 0; i < runningJobsNodeList.size(); i++) {
					RQLNode jobNode = runningJobsNodeList.get(i);
					if (jobNode.getAttribute("category").equals(category) && jobNode.getAttribute("status").equals(status)) {
						String name = jobNode.getAttribute("name");
						int start = name.indexOf("ID:");
						// some jobs has a page id in name, but defined not
						Page jobStartPgOrNull = null;
						if (start > 0) {
							int end = name.indexOf(")", start);
							String pageId = name.substring(start + 3, end).trim();
							jobStartPgOrNull = getPageById(pageId);
						}
						jobs.add(new PublishingJob(this, jobStartPgOrNull, name, jobNode.getAttribute("guid"), category, status,
								jobNode.getAttribute("jobguid"), jobNode.getAttribute("username")));
					}
				}
			}
		}
		return new JobQueue(this, jobs);
	}

	/**
	 * Liefert die Sprachvariante mit der gegebenen GUID zurück.
	 * 
	 * @param languageVariantGuid
	 *            GUID der gesuchten Sprachvariante
	 */
	public LanguageVariant getLanguageVariantByGuid(String languageVariantGuid) throws RQLException {

		RQLNodeList languageNodeList = getLanguageVariantsNodeList();

		// find language variant
		for (int i = 0; i < languageNodeList.size(); i++) {
			RQLNode node = languageNodeList.get(i);
			if (node.getAttribute("guid").equals(languageVariantGuid)) {
				return buildLanguageVariant(node);
			}
		}
		throw new ElementNotFoundException("Language variant with guid " + languageVariantGuid + " could not be found in the project.");
	}

	/**
	 * Liefert die Sprachvariante für die gegebene Sprache zurück.
	 * 
	 * @param languageCode
	 *            Code der Sprache, z.b. ENG, DEU aus dem RD Dialog (case ignored!)
	 */
	public LanguageVariant getLanguageVariantByLanguage(String languageCode) throws RQLException {

		RQLNodeList languageNodeList = getLanguageVariantsNodeList();

		// find language variant
		for (int i = 0; i < languageNodeList.size(); i++) {
			RQLNode node = languageNodeList.get(i);
			if (node.getAttribute("language").equalsIgnoreCase(languageCode)) {
				return buildLanguageVariant(node);
			}
		}
		throw new ElementNotFoundException("Language variant for language " + languageCode + " could not be found in the project.");
	}

	/**
	 * Liefert die Sprachvariante mit dem gegebenen Namen zurück.
	 * 
	 * @param languageVariantName
	 *            Name der Sprachvariante (case ignored!)
	 */
	public LanguageVariant getLanguageVariantByName(String languageVariantName) throws RQLException {

		RQLNodeList languageNodeList = getLanguageVariantsNodeList();

		// find language variant
		for (int i = 0; i < languageNodeList.size(); i++) {
			RQLNode node = languageNodeList.get(i);
			if (node.getAttribute("name").equalsIgnoreCase(languageVariantName)) {
				return buildLanguageVariant(node);
			}
		}
		throw new ElementNotFoundException("Language variant named " + languageVariantName + " could not be found in the project.");
	}

	/**
	 * Liefert die Sprachvariante mit der gegebenen RFC language ID (z.B. en von en-US, de von de-DE, zh oder es) zurück.
	 * 
	 * @see LanguageVariant#getRfcLanguageId()
	 */
	public LanguageVariant getLanguageVariantByRfcLanguageId(String rfcLanguageId) throws RQLException {

		RQLNodeList languageNodeList = getLanguageVariantsNodeList();

		// find language variant
		for (int i = 0; i < languageNodeList.size(); i++) {
			RQLNode node = languageNodeList.get(i);
			if (node.getAttribute("rfclanguageid").substring(0, 2).equalsIgnoreCase(rfcLanguageId)) {
				return buildLanguageVariant(node);
			}
		}
		throw new ElementNotFoundException("Language variant for RFC language ID " + rfcLanguageId
				+ " could not be found in the project.");
	}

	/**
	 * Liefert die Nodelist aller Sprachvarianten dieses Projektes zurück.
	 */
	private RQLNodeList getLanguageVariantsNodeList() throws RQLException {

		checkSessionKey();
		/*
		 V7.5 request
		<IODATA loginguid="9FE5E3E516254F45BDE783FFB584A91F" sessionkey="8C605FE429F74DC4B931F48DE58E46B4">
		<PROJECT>
		<LANGUAGEVARIANTS action="list"/>
		</PROJECT>
		</IODATA>
		 V7.5 response
		<IODATA>
		<LANGUAGEVARIANTS action="list" languagevariantid="ENG" dialoglanguageid="ENG" key="8C605FE429F74DC4B931F48DE58E46B4" guid="">
		<LANGUAGEVARIANT guid="1559299F33554073BC8E7290CE26B6B1" name="Chinese" language="CHS" codetable="65001" checked="0" ismainlanguage="0" textdirection="" rfclanguageid="zh-CN"/>
		<LANGUAGEVARIANT guid="03FF9B01B2AD4CB3B52B75D308F964B8" name="English" language="ENG" codetable="65001" checked="1" ismainlanguage="1" textdirection="" rfclanguageid="en-US"/>
		<LANGUAGEVARIANT guid="9AE7DE90A14A456683B3AE101858E51E" name="German" language="DEU" codetable="28591" checked="0" ismainlanguage="0" textdirection="" rfclanguageid="de-DE"/>
		<LANGUAGEVARIANT guid="0B0ADA2CDB384ED184B76D3E6E5E66D5" name="Spanish" language="ESN" codetable="28591" checked="0" ismainlanguage="0" textdirection="" rfclanguageid="es-MX"/>
		</LANGUAGEVARIANTS>
		</IODATA>
		 */

		// call CMS
		if (languageVariantsCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PROJECT>"
					+ "   <LANGUAGEVARIANTS action='list'/>" + " </PROJECT>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			languageVariantsCache = rqlResponse.getNodes("LANGUAGEVARIANT");
		}
		return languageVariantsCache;
	}

	/**
	 * Erzeugt eine Liste für die gegebene linkGuid. Benötigt den session key!
	 * 
	 * @param linkGuid
	 *            RedDot GUID der Liste
	 * @return List or Container
	 */
	public com.hlcl.rql.as.List getListByGuid(String linkGuid) throws RQLException {

		return getPageFromLinkGuid(linkGuid).getListByGuid(linkGuid);
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {

		return getCmsClient().getLogonGuid();
	}

	/**
	 * Liefert die Haupt-Sprachvariante im aktuellen Projekt zurück.
	 */
	public LanguageVariant getMainLanguageVariant() throws RQLException {

		RQLNodeList languageNodeList = getLanguageVariantsNodeList();

		// find language variant
		for (int i = 0; i < languageNodeList.size(); i++) {
			RQLNode node = languageNodeList.get(i);
			if (node.getAttribute("ismainlanguage").equals("1")) {
				return buildLanguageVariant(node);
			}
		}
		throw new ElementNotFoundException(
				"Main language variant not found in current project. Please correct main language variant check and try again.");
	}

	/**
	 * Liefert die GUID der Haupt-Sprachvariante im aktuellen Projekt zurück.
	 */
	public String getMainLanguageVariantGuid() throws RQLException {
		return getMainLanguageVariant().getLanguageVariantGuid();
	}

	/**
	 * Liefert den Namen der Haupt-Sprachvariante im aktuellen Projekt zurück.
	 */
	public String getMainLanguageVariantName() throws RQLException {
		return getMainLanguageVariant().getName();
	}

	/**
	 * Liefert den language code (ENG, DEU, CHS, ESN) der Haupt-Sprachvariante im aktuellen Projekt zurück.
	 */
	public String getMainLanguageVariantLanguageCode() throws RQLException {
		return getMainLanguageVariant().getLanguageCode();
	}

	/**
	 * Liefert die rfc language id (en,de,zh,es) der Haupt-Sprachvariante im aktuellen Projekt zurück.
	 */
	public String getMainLanguageVariantRfcLanguageId() throws RQLException {
		return getMainLanguageVariant().getRfcLanguageId();
	}

	/**
	 * Erzeugt einen MultiLink (Liste oder Container) für die gegebene linkGuid. Benötigt den session key!
	 * 
	 * @param linkGuid
	 *            RedDot GUID des Links
	 * @return List or Container
	 */
	public MultiLink getMultiLinkByGuid(String linkGuid) throws RQLException {

		return getPageFromLinkGuid(linkGuid).getMultiLinkByGuid(linkGuid);
	}

	/**
	 * Liefert einen Iterator über alle MultiLinks zurück, die auf die gegebene Seiten- oder Link-GUID (auch Frame) verweisen.
	 * Templateelemente, die ebenfalls diese Seite (oder Link) referenzieren werden nicht geliefert (fehlen bereits im RQL). Sie werden
	 * auch nicht durch die RD Funktion show reference list geliefert!
	 * 
	 * @see MultiLink#referenceTo(Page)
	 */
	public Iterator<MultiLink> getMultiLinksReferencingIterator(String pageOrLinkGuid) throws RQLException {
		return new MultiLinksReferencingIterator(pageOrLinkGuid);
	}

	
	/**
	 * Liefert eine Liste von MultiLinks, die auf die gegebene Seiten-/Link-GUID verweisen.
	 * Templateelemente, die ebenfalls diese Seite (oder Link) referenzieren werden nicht geliefert (fehlen bereits im RQL). Sie werden
	 * auch nicht durch die RD Funktion show reference list geliefert!
	 */
	public java.util.List<MultiLink> getMultiLinksReferencing(String pageOrLinkGuid) throws RQLException {
		MultiLinksReferencingIterator mi = new MultiLinksReferencingIterator(pageOrLinkGuid);
		return mi.chunk;
	}
	
	
	/**
	 * Frontend to REFERENCE action="list" - find from where somehting is being referred to. 
	 * 
	 * @param toHere GUID of a page OR multilink.
	 * @return a possible empty list.
	 */
	public java.util.List<Reference> getReferences(String toHere) throws RQLException {
		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'><PROJECT>"
				+ " <REFERENCE action='list' guid='" + toHere + "'/></PROJECT></IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNodeList pageNodes = rqlResponse.getNodes("PAGE");
		int num = pageNodes == null ? 0 : pageNodes.size();

		java.util.List<Reference> out = new ArrayList<Reference>(num * 2);
		if (num == 0)
			return out; // prevent NPE from iteration
		
		for (RQLNode pageNode : pageNodes) {
			for (RQLNode linkNode : pageNode.getNodes("LINK")) {
				Reference r = new Reference(this, toHere);
				r.fromResult(pageNode, linkNode);
				out.add(r);
			}
		}
		
		return out;
	}
	
	
	/**
	 * Liefert den Namen des Projekts. Nur mit Servermanager Lizenz nutzbar!
	 * 
	 * @see #validate()
	 */
	public String getName() throws RQLException {
		if (name == null) {
			name = getDetailsNode().getAttribute("name");
		}
		return name;
	}


    /**
     * {@inheritDoc}
     */
    protected void loadKeywords() {

        StringBuilder rqlRequest = new StringBuilder("");

        rqlRequest
            .append("<IODATA loginguid=\"").append(cmsClient.getLogonGuid()).append("\" sessionkey=\"").append(getSessionKey()).append("\">")
            .append("<PROJECT><CATEGORIES action=\"list\"/></PROJECT></IODATA>");


        RQLNode rqlResponse = null;

        try {
            rqlResponse = callCms(rqlRequest.toString());

            if(rqlResponse != null){

                java.util.List<KeywordCategory> categoriesFound = new ArrayList<KeywordCategory>();

                RQLNodeList categegoryNodes = rqlResponse.getNodes("CATEGORY");
                for (int i = 0; i < categegoryNodes.size(); i++) {
                    RQLNode categoryNode = categegoryNodes.get(i);
                    KeywordCategory keywordCategory = new KeywordCategory(categoryNode.getAttribute("guid"), categoryNode.getAttribute("value"));
                    categoriesFound.add(keywordCategory);
                }

                java.util.List<Keyword> keywordsFound = new ArrayList<Keyword>();
                for (KeywordCategory keywordCategory : categoriesFound) {

                    rqlRequest = new StringBuilder("");

                    rqlRequest
                        .append("<IODATA loginguid=\"").append(getLogonGuid()).append("\" sessionkey=\"").append(getSessionKey()).append("\">")
                        .append("<PROJECT><CATEGORY guid=\"").append(keywordCategory.getGuid()).append("\"><KEYWORDS action=\"load\"/>")
                        .append("</CATEGORY></PROJECT></IODATA>");


                    rqlResponse = callCms(rqlRequest.toString());
                    RQLNodeList keywordNodes = rqlResponse.getNodes("KEYWORD");
                    for (int j = 0; j < keywordNodes.size(); j++) {
                        RQLNode keywordNode = keywordNodes.get(j);

                        Keyword keyword = new Keyword(keywordNode.getAttribute("guid"), keywordCategory, keywordNode.getAttribute("value"));
                        keywordsFound.add(keyword);
                    }
                }

                this.keywords = keywordsFound;
            }

        } catch (RQLException e) {
            e.printStackTrace();
        }

    }

    /**
	 * Liefert die Anzahl aller Templates dieses Projekts zurück.
	 */
	public int getNumberOfAllTemplates() throws RQLException {

		checkSessionKey();
		/* 
		 V5 request
		 <IODATA loginguid="897F4026C87C4E9D8D2046ADEE7365BF" sessionkey="B0F9E12CEEA841D3BF1C58434F97A283">
		  <TEMPLATES action="list"/>
		</IODATA> 
		 V5 response
		<IODATA>
		<TEMPLATES>
		<TEMPLATE guid="B7B8ACF31FA54361A6CA6EF96A4E8897" name="cv_page" description="structured page with image on left side and text aside" folderguid="010A18336E374638AA3104A1A82D6B43" selectinnewpage="1">
		</TEMPLATE>
		<TEMPLATE guid="ABD0C4C0E6C747F8835D902247AA4185" name="facts_and_figures_page" description="the facts and figures page" folderguid="010A18336E374638AA3104A1A82D6B43" selectinnewpage="1">
		</TEMPLATE>
		 ...
		<TEMPLATE guid="929CDC5F08D34047B4CB80A313556CEA" name="test_range_dynlink" description="" folderguid="F421226373384D5580D6C1D11969AD68" selectinnewpage="1">
		</TEMPLATE>
		</TEMPLATES>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<TEMPLATES action='list' />" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNodes("TEMPLATE").size();
	}

	/**
	 * Liefert alle Berechtigungspaket (Typ=Normal=0, an Seiten), deren Nmae mit dem gegebenen suffix enden zurück.
	 */
	public java.util.List<AuthorizationPackage> getAuthorizationPackagesForPageByNameSuffix(String packageNameSuffix)
			throws RQLException {
		java.util.List<AuthorizationPackage> result = new ArrayList<AuthorizationPackage>();
		for (AuthorizationPackage authorizationPackage : getAllPageAuthorizationPackages()) {
			if (authorizationPackage.isNameEndsWith(packageNameSuffix)) {
				result.add(authorizationPackage);
			}
		}
		return result;
	}

	/**
	 * Liefert alle Berechtigungspaket (Typ=Normal=0, an Seiten), deren Nmae mit dem gegebenen prefix beginnen zurück.
	 */
	public java.util.List<AuthorizationPackage> getAuthorizationPackagesForPageByNamePrefix(String packageNamePrefix)
			throws RQLException {
		java.util.List<AuthorizationPackage> result = new ArrayList<AuthorizationPackage>();
		for (AuthorizationPackage authorizationPackage : getAllPageAuthorizationPackages()) {
			if (authorizationPackage.isNameStartsWith(packageNamePrefix)) {
				result.add(authorizationPackage);
			}
		}
		return result;
	}

	
	/**
	 * Erzeugt eine Page für die gegebene pageGuid. Benötigt den session key!
	 * 
	 * @param pageGuid
	 *            RedDot GUID der Seite
	 * @return Page
	 */
	public Page getPageByGuid(String pageGuid) throws RQLException {

		checkSessionKey();
		Page cached = pageCache.get(pageGuid);
		if (cached == null) {
			cached = new Page(this, pageGuid);
			pageCache.put(pageGuid, cached);
		}
		return cached;
	}

	/**
	 * Erzeugt eine Page für die gegebene page ID. Benötigt den session key!
	 * 
	 * @param pageId
	 *            RedDot ID der Seite
	 * @return Page or null (page not found)
	 */
	public Page getPageById(String pageId) throws RQLException {

		checkSessionKey();
		/*
		 * V5 request 
		 <IODATA loginguid="5F2BD3FEEB2A43659A9EE9E12AE23361" sessionkey="824484397Hsy622n48J7"> 
		 <PAGE action="search" pageidfrom="13531" pageidto="13531"/> </IODATA> 
		 V5 response 
		 <IODATA>
		 <PAGELIST> 
		 <PAGE id="13531" guid="F0F3EEAFD33E4D46A4E38DB4A36956C3"
		 headline="Author&apos;s work and training area" flags="268959744"
		 createdate="38008.557037037" changedate="38159.3075115741"
		 releasedate="38159.307662037"/> 
		 </PAGELIST> </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<PAGE action='search' pageidfrom='" + pageId + "' pageidto='" + pageId + "'/>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNode pageNodeOrNull = rqlResponse.getNode("PAGE");

		// page not found indicator
		if (pageNodeOrNull == null) {
			return null;
		}

		return new Page(this, pageNodeOrNull.getAttribute("guid"));
	}

	/**
	 * Liefert die Seite auf der der Link mit der gegebenen GUID existiert. Benötigt den session key!
	 * 
	 * @param linkGuid
	 *            RedDot GUID des Links
	 * @return Page
	 */
	public Page getPageFromLinkGuid(String linkGuid) throws RQLException {

		checkSessionKey();
		/*
		 * V6 request <IODATA loginguid="4FFE79E83EDF4CC1AB7740E26D3EC895"
		 * sessionkey="1021834323fnS64VcN0V6"> <LINK action="load"
		 * guid="87202AD919E341C6A078C3EE43B20C14" /> </IODATA> V6 response
		 * <IODATA> <LINK action="load" sessionkey="1021834323fnS64VcN0V6"
		 * dialoglanguageid="ENG" languagevariantid="ENG" ok="1"
		 * guid="87202AD919E341C6A078C3EE43B20C14"
		 * templateelementguid="4B98D010FFAA4A5E830E78A30BBED2CC"
		 * pageguid="80A6F8AFF3974BDC911B57E9F8BEEEF4" eltflags="0"
		 * eltrequired="0" eltdragdrop="0" islink="2" formularorderid="0"
		 * orderid="1" status="0" name="blocks" eltname="blocks"
		 * aliasname="blocks" variable="blocks" istargetcontainer="0" type="28"
		 * elttype="28" templateelementflags="0" templateelementislink="2"
		 * value="blocks" reddotdescription="" flags="16777216"
		 * manuallysorted="-1" useconnection="1"
		 * userguid="4324D172EF4342669EAF0AD074433393"/> </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<LINK action='load' guid='" + linkGuid + "' />" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		// wrap into page
		return getPageByGuid(rqlResponse.getNode("LINK").getAttribute("pageguid"));
	}

	/**
	 * Returns the stored pages for the given key or null, if key is not existing.
	 */
	public PageArrayList getPages(String key) {
		return getPagesCache().get(key);
	}

	/**
	 * Liefert alle Seiten des gegebenen Benutzers oder des gesamten Projektes im gegebenen Status. Benötigt den session key!
	 * 
	 * @param workflowState
	 *            eine Konstante von WF_STATE_*
	 * @param userOrNull ==
	 *            null => alle Seiten im Status für das gesamte Projekt (nur WF_STATE_PAGES_SAVED_AS_DRAFT und
	 *            WF_STATE_PAGES_WAITING_FOR_RELEASE) != null => alle Seiten des gegebenen Users im Status für dieses Projekt
	 * @return List of Pages
	 */
	PageArrayList getPagesByState(String workflowState, User userOrNull) throws RQLException {

		checkSessionKey();
		RQLNodeList pagesNodeList = getPagesByStateNodeList(workflowState, userOrNull);

		// wrap all page nodes
		PageArrayList pages = new PageArrayList();
		if (pagesNodeList != null) {
			for (int i = 0; i < pagesNodeList.size(); i++) {
				RQLNode node = pagesNodeList.get(i);
				pages.add(new Page(this, node.getAttribute("guid")));
			}
		}
		return pages;
	}

	/**
	 * Liefert eine RQL node list aller Seiten des gegebenen Benutzers oder des gesamten Projektes im gegebenen Status. Achtung: Diese
	 * Anfrage liefert nicht alle globalen Seiten (user=null) im Entwurf, einige fehlen!
	 * 
	 * @param workflowState
	 *            eine Konstante von WF_STATE_*
	 * @param userOrNull ==
	 *            null => alle Seiten im Status für das gesamte Projekt (nur WF_STATE_PAGES_SAVED_AS_DRAFT und
	 *            WF_STATE_PAGES_WAITING_FOR_RELEASE) != null => alle Seiten des gegebenen Users im Status für dieses Projekt
	 */
	private RQLNodeList getPagesByStateNodeList(String workflowState, User userOrNull) throws RQLException {
		/*
		 * V5 request <IODATA loginguid="94B1C3B490AA4C61BC57755790CAC259"
		 * sessionkey="92571089943RQ6mn4Q6i"> <AUTHORIZATION> <PAGES
		 * action="list" actionflag="262144"
		 * userguid="4324D172EF4342669EAF0AD074433393" /> </AUTHORIZATION>
		 * </IODATA> V5 response <IODATA> <PAGES> <PAGE
		 * guid="65B7B506F97D48199AD58FDCF10AD9E4" headline="row VESSEL"
		 * username="lejafr" changedate="38310.6143171296" editlinkguid=""
		 * mainlinkguid="" createdate="38310.6142592593" createuser="lejafr"
		 * releasedate="0" languagevariantid="ENG" languagevariantname="English"
		 * releaseusers="" releasegroups="" rejectiontype="0" > <RELEASEUSERS
		 * assentcount="0"></RELEASEUSERS> </PAGE> <PAGE
		 * guid="7034F62A958F4D6FA9789C8342590ED4" headline="row VESSEL"
		 * username="lejafr" changedate="38310.5542824074" editlinkguid=""
		 * mainlinkguid="" createdate="38310.554224537" createuser="lejafr"
		 * releasedate="0" languagevariantid="ENG" languagevariantname="English"
		 * releaseusers="" releasegroups="" rejectiontype="0" > <RELEASEUSERS
		 * assentcount="0"></RELEASEUSERS> </PAGE> ... </PAGES> </IODATA>
		 */

		// call CMS
		// mephistopheles78
		String users = (userOrNull != null ? "myself" : "all");
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" +
				"<PAGE action='xsearch'>" +
				"<SEARCHITEMS>" + 
				"<SEARCHITEM key='pagestate' value='" + workflowState + "' users='" + users + "' operator='eq' /> "+
				"</SEARCHITEMS>" +	
				"</PAGE>" +
				"</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNodes("PAGE");
	}

	/**
	 * Returns the pages cache map, lazy initialized.
	 */
	private Map<String, PageArrayList> getPagesCache() {
		if (pagesCache == null) {
			pagesCache = new HashMap<String, PageArrayList>();
		}
		return pagesCache;
	}

	/**
	 * Liefert ein script parameter objekt für die gegebenen Parameterseite.
	 * <p>
	 * Prefer usage of get parameters with class name to benefit of caching.
	 */
	public ScriptParameters getParameters(String parameterPageId) throws RQLException {
		return new ScriptParameters(getPageById(parameterPageId));
	}

	/**
	 * Liefert ein script parameter objekt für die gegebenen Parameterseite parmPageId.
	 * <p>
	 * Cache this object for the given parametersGroupId.
	 */
	public ScriptParameters getParameters(String parameterPageId, String parametersGroupId) throws RQLException {
		Map<String, ScriptParameters> cache = getParametersCache();
		if (cache.containsKey(parametersGroupId)) {
			return cache.get(parametersGroupId);
		} else {
			// insert first and return
			ScriptParameters parameters = getParameters(parameterPageId);
			cache.put(parametersGroupId, parameters);
			return parameters;
		}
	}

	/**
	 * Returns the parameters cache map, lazy initialized.
	 */
	private Map<String, ScriptParameters> getParametersCache() {
		if (parametersCache == null) {
			parametersCache = new HashMap<String, ScriptParameters>();
		}
		return parametersCache;
	}

	/**
	 * Liefert die GUID dieses Projektes.
	 */
	public String getProjectGuid() {
		return projectGuid;
	}

	/**
	 * Liefert den Treesegment RQLNode des Projectknotens im Baum für den gegebenen type (z.B. 'project.1700') zurück. Liefert null,
	 * falls keine globalen Pakete für den gegebenen Typ existieren. Genutzt, um die globalen Pakete lesen zu können. Attention: Did
	 * not work in V7.5.0.33. Deliver same attributes within same segment tag.
	 * 
	 * @return <code>RQLNode</code>
	 */
	RQLNode getProjectTreeSegment(String type) throws RQLException {
		/*
		 */

		// call CMS
		if (projectTreesegmentsCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ " <TREESEGMENT type='project' action='load' guid='00000000000000000000000000000001'/>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			projectTreesegmentsCache = rqlResponse.getNodes("SEGMENT");
		}
		// check, if segments exists
		if (projectTreesegmentsCache == null || projectTreesegmentsCache.size() == 0) {
			return null;
		}

		// find
		for (int i = 0; i < projectTreesegmentsCache.size(); i++) {
			RQLNode segment = projectTreesegmentsCache.get(i);
			if (segment.getAttribute("type").equals(type)) {
				return segment;
			}
		}
		// null, if not found
		return null;
	}

	/**
	 * Liefert die Projektvariante für die gegebene GUID zurück. Benötigt den session key!
	 * 
	 * @param projectVariantGuid
	 *            GUID der gesuchten Projektvariante
	 */
	public ProjectVariant getProjectVariantByGuid(String projectVariantGuid) throws RQLException {

		RQLNodeList variantNodeList = getProjectVariantsNodeList();

		// find project variant
		for (int i = 0; i < variantNodeList.size(); i++) {
			RQLNode node = variantNodeList.get(i);
			if (node.getAttribute("guid").equals(projectVariantGuid)) {
				return buildProjectVariant(node);
			}
		}
		throw new ElementNotFoundException("Project variant with guid " + projectVariantGuid + " could not be found in the project.");
	}

	/**
	 * Liefert die Projektvariante mit dem gegebenen Namen zurück. Benötigt den session key!
	 * 
	 * @param projectVariantName
	 *            Name der Projektvariante (case ignored!)
	 */
	public ProjectVariant getProjectVariantByName(String projectVariantName) throws RQLException {

		RQLNodeList variantNodeList = getProjectVariantsNodeList();

		// find project variant
		for (int i = 0; i < variantNodeList.size(); i++) {
			RQLNode node = variantNodeList.get(i);
			if (node.getAttribute("name").equalsIgnoreCase(projectVariantName)) {
				return buildProjectVariant(node);
			}
		}
		throw new ElementNotFoundException("Project variant named " + projectVariantName + " could not be found in the project.");
	}

	/**
	 * Liefert die Projektvariante mit dem gegebenen Namen zurück.
	 */
	private RQLNodeList getProjectVariantsNodeList() throws RQLException {

		checkSessionKey();
		/*
		 V5 request 
		 <IODATA loginguid="4CDC1675087A4AA0A36FE9EFC6A5F7B8" sessionkey="571683732g8m33Lo1hD4"> 
		 <PROJECT> 
		 <PROJECTVARIANTS action="list" /> 
		 </PROJECT> </IODATA> 
		 V5 response 
		 <IODATA>
		 * <PROJECTVARIANTS action="list" languagevariantid="ENG"
		 * dialoglanguageid="ENG"> <PROJECTVARIANT
		 * guid="CCA1669D6F124CED85706E0EB573EE77" name="cd_hip" checked="0"/>
		 * <PROJECTVARIANT guid="8491B46045904DA6A7F1836B06271436" name="Display
		 * (do not use for publishing)" checked="1"/> <PROJECTVARIANT
		 * guid="A333ED0B60424DBDBB2CD64B8F8BA6F6" name="interim2" checked="0"/>
		 * <PROJECTVARIANT guid="C51B659BD3D74998A4F74DB896211387"
		 * name="localhost" checked="0"/> <PROJECTVARIANT
		 * guid="7FADE58885E540ED83A046949C209FC0" name="Portal DEVELOPMENT OR"
		 * checked="0"/> <PROJECTVARIANT guid="797D786045E04D38A02B84488EB5D843"
		 * name="Portal PRODUCTION" checked="0"/> </PROJECTVARIANTS> </IODATA>
		 */

		// call CMS
		if (projectVariantsCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PROJECT>"
					+ "   <PROJECTVARIANTS action='list'/>" + " </PROJECT>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			projectVariantsCache = rqlResponse.getNodes("PROJECTVARIANT");
		}
		return projectVariantsCache;
	}

	/**
	 * Liefert den Publication Folder zur gegebenen Guid, oder null, falls keiner auf root-Ebene existiert
	 * <p>
	 * Benötigt den session key!
	 * <p>
	 */
	PublicationFolder getPublicationFolderByGuid(String parentGuid, String publicationFolderGuid) throws RQLException {
		RQLNodeList rootNodes = getPublicationFolderNodeList(parentGuid);

		// check if any at all
		if (rootNodes == null)
			return null;

		// search for given guid
		for (int i = 0; i < rootNodes.size(); i++) {
			RQLNode treeNode = rootNodes.get(i);
			String guid = treeNode.getAttribute("guid");
			if (guid.equals(publicationFolderGuid)) {
				return new PublicationFolder(this, treeNode.getAttribute("value"), guid);
			}
		}
		// signal not found
		return null;
	}

	/**
	 * Returns the publication folder for the given GUID. The publication folder has the path included.
	 * <p>
	 * Benötigt den session key!
	 * 
	 * @throws ElementNotFoundException
	 *             if no publication folder with given GUID is found
	 */
	public PublicationFolder getPublicationFolderByGuid(String publicationFolderGuid) throws RQLException {
		RQLNodeList nodes = getPublicationFolderNodeList();

		// check if any at all
		if (nodes != null) {
			// search for given guid
			for (int i = 0; i < nodes.size(); i++) {
				RQLNode folderNode = nodes.get(i);
				String guid = folderNode.getAttribute("guid");
				if (guid.equals(publicationFolderGuid)) {
					return new PublicationFolder(this, folderNode.getAttribute("name"), guid, folderNode.getAttribute("realpath"));
				}
			}
		}
		// signal not found
		throw new ElementNotFoundException("No publication folder with GUID " + publicationFolderGuid + " could be found in project "
				+ getName() + ".");
	}

	/**
	 * Returns the publication folder path for the given GUID.
	 * <p>
	 * Benötigt den session key!
	 * 
	 * @throws ElementNotFoundException
	 *             if no publication folder with given GUID is found
	 */
	String getPublicationFolderPathByGuid(String publicationFolderGuid) throws RQLException {
		RQLNodeList nodes = getPublicationFolderNodeList();

		// check if any at all
		if (nodes != null) {
			// search for given guid
			for (int i = 0; i < nodes.size(); i++) {
				RQLNode folderNode = nodes.get(i);
				String guid = folderNode.getAttribute("guid");
				if (guid.equals(publicationFolderGuid)) {
					return folderNode.getAttribute("realpath");
				}
			}
		}
		// signal not found
		throw new ElementNotFoundException("No publication folder with GUID " + publicationFolderGuid + " could be found in project "
				+ getName() + ".");
	}

	/**
	 * Returns all publication folders matching the given path prefix of the current project with the real path included.
	 * <p>
	 * Benötigt den session key!
	 */
	public java.util.List<PublicationFolder> getPublicationFoldersPathStartWith(String prefix) throws RQLException {
		java.util.List<PublicationFolder> result = new ArrayList<PublicationFolder>();
		for (PublicationFolder publicationFolder : getPublicationFolders()) {
			if (publicationFolder.isPathStartsWith(prefix)) {
				result.add(publicationFolder);
			}
		}
		return result;
	}

	/**
	 * Returns all publication folders matching the given path prefixes separated by separator of the current project with the real
	 * path included.
	 * 
	 * <p>
	 * Benötigt den session key!
	 */
	public java.util.List<PublicationFolder> getPublicationFoldersPathStartWith(String prefixList, String separator)
			throws RQLException {
		java.util.List<PublicationFolder> result = new ArrayList<PublicationFolder>();
		String[] prefixes = StringHelper.split(prefixList, separator);
		for (int i = 0; i < prefixes.length; i++) {
			String prefix = prefixes[i];
			result.addAll(getPublicationFoldersPathStartWith(prefix));
		}
		return result;
	}

	/**
	 * Returns all publication folders of the current project with the real path included.
	 * <p>
	 * Benötigt den session key!
	 */
	public java.util.List<PublicationFolder> getPublicationFolders() throws RQLException {
		RQLNodeList nodes = getPublicationFolderNodeList();
		java.util.List<PublicationFolder> result = new ArrayList<PublicationFolder>();

		// check if any at all
		if (nodes != null) {
			// wrap all
			for (int i = 0; i < nodes.size(); i++) {
				RQLNode folderNode = nodes.get(i);
				String guid = folderNode.getAttribute("guid");
				result.add(new PublicationFolder(this, folderNode.getAttribute("name"), guid, folderNode.getAttribute("realpath")));
			}
		}
		return result;
	}

	/**
	 * Liefert den Publication Folder mit dem gegebenen Namen, oder null, falls keiner mit dem Namen existiert aus den children des
	 * folder der gegebenen GUID.
	 * <p>
	 * Benötigt den session key! Checks with equalsIgnoreCase().
	 * <p>
	 */
	PublicationFolder getPublicationFolderByName(String parentGuid, String publicationFolderName) throws RQLException {
		RQLNodeList rootNodes = getPublicationFolderNodeList(parentGuid);

		// check if any at all
		if (rootNodes == null)
			return null;

		// search for given name
		for (int i = 0; i < rootNodes.size(); i++) {
			RQLNode treeNode = rootNodes.get(i);
			String name = treeNode.getAttribute("value");
			if (name.equalsIgnoreCase(publicationFolderName)) {
				return new PublicationFolder(this, name, treeNode.getAttribute("guid"));
			}
		}
		// signal not found
		return null;
	}

	/**
	 * Liefert alle Publication Folder nodes dieses Projektes. Liefert null, falls keine existieren.
	 * <p>
	 * Benötigt den session key!
	 * <p>
	 * 
	 * @param publicationFolderGuid
	 *            kann auch die feste TreeGUID des TreeNodes 'Publication Structure' sein! TODO anderen RQL verwenden, es gibt einen
	 *            eigenen: exports action=list!
	 */
	RQLNodeList getPublicationFolderNodeList(String publicationFolderGuid) throws RQLException {

		checkSessionKey();
		/*
		 V7.5 request
		<IODATA loginguid="9FE5E3E516254F45BDE783FFB584A91F" sessionkey="8C605FE429F74DC4B931F48DE58E46B4">
		<TREESEGMENT type="project.6090" action="load" guid="9BBF210F7923406291BE7AE47B4CA571" />
		</IODATA>
		 V7.5 response
		<IODATA>
		<TREESEGMENTS>
		<SEGMENT guid="60694D17B4FC497E90275BFD53A7B9EE" type="project.6095" image="exportfolder.gif" expand="1" value="docRoot" col1value="docRoot" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"/>
		<SEGMENT guid="DE52622216AC458FB4B3E9ABB930A60C" type="project.6095" image="exportfolder.gif" expand="1" value="pageConfig" col1value="pageConfig" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"/>
		<SEGMENT guid="A4962F9D50464C03A3BCB2F3520928FE" type="project.6095" image="exportfolder.gif" expand="1" value="trash" col1value="trash" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"/>
		</TREESEGMENTS>
		<TREEELEMENT guid="9BBF210F7923406291BE7AE47B4CA571" value="Publication Structure" image="exportstructure.gif" flags="0" expand="1" descent="" type="project.6090" col1value="Publication Structure" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"/>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<TREESEGMENT action='load' type='" + PublicationFolder.TREESEGMENT_TYPE + "' guid='" + publicationFolderGuid
				+ "'/>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNodes("SEGMENT");
	}

	/**
	 * Deletes the publication folder node list cache.
	 */
	void invalidatePublicationFoldersCache() {
		publicationFoldersCache = null;
	}

	/**
	 * Returns publication folders (the whole tree) with a path.
	 * <p>
	 * Benötigt den session key!
	 */
	private RQLNodeList getPublicationFolderNodeList() throws RQLException {

		checkSessionKey();
		/*
		 V9 request
		<IODATA loginguid="2B7D7DE296484310917A1F04775F640C" sessionkey="16F71A56A28A49CD93CAE7E193DEC476">
		<project>
		<EXPORTFOLDERS action="list"/>
		</project>
		</IODATA>
		 V9 response
		<IODATA><EXPORTFOLDERS>
		<EXPORTFOLDER guid="34B0AD316CD44DC08E676346EA0F0A25" name="portalConfig" realpath="portalConfig" virtualpath="portalConfig" indent="0"/>
		<EXPORTFOLDER guid="7BDE6BBA58BF4F62A0956CCBC95F8AEF" name="mailConfig" realpath="mailConfig" virtualpath="mailConfig" indent="0"/>
		<EXPORTFOLDER guid="C289D98D3BDD43C5B626EFA4F7517667" name="DEU" realpath="mailConfig\DEU" virtualpath="mailConfig\DEU" indent="1"/>
		<EXPORTFOLDER guid="DA2F352EB9D04D57AACBBCCAE800A39E" name="ENG" realpath="mailConfig\ENG" virtualpath="mailConfig\ENG" indent="1"/>
		<EXPORTFOLDER guid="E9285F17434F46D2B9AFD04000354054" name="ESN" realpath="mailConfig\ESN" virtualpath="mailConfig\ESN" indent="1"/>
		<EXPORTFOLDER guid="66BE592B97E04822A417BB1BE30BAC87" name="CHS" realpath="mailConfig\CHS" virtualpath="mailConfig\CHS" indent="1"/>
		<EXPORTFOLDER guid="1E939F54098546478C432C85DB894C73" name="helpItems" realpath="helpItems" virtualpath="helpItems" indent="0"/>
		...
		<EXPORTFOLDER guid="F721055473284890A27DEA361513D998" name="direct_booking" realpath="pageConfig\en\direct_booking" virtualpath="pageConfig\en\direct_booking" indent="2"/>
		<EXPORTFOLDER guid="3159EBBBDC114D88B65D7A2A5513F097" name="schedules" realpath="pageConfig\en\schedules" virtualpath="pageConfig\en\schedules" indent="2"/>
		...
		<EXPORTFOLDER guid="AC3B4758CD834797805282E54DE82B6F" name="local_info" realpath="pageConfig\zh\local_info" virtualpath="pageConfig\zh\local_info" indent="2"/>
		<EXPORTFOLDER guid="53233CF26557489993B4C062599F6E9F" name="products_and_services" realpath="pageConfig\zh\products_and_services" virtualpath="pageConfig\zh\products_and_services" indent="2"/>
		...
		<EXPORTFOLDER guid="444FA7BAE0E04F3B81685937556C725A" name="teaser" realpath="docRoot\images\fleet\containers\teaser" virtualpath="/images\fleet\containers\teaser" indent="4"/>
		<EXPORTFOLDER guid="357D7844109E48069D4756E4D3CAB965" name="type" realpath="docRoot\images\fleet\containers\type" virtualpath="/images\fleet\containers\type" indent="4"/>
		<EXPORTFOLDER guid="B3B9173B24A14361919C90A094F63DFF" name="vessels" realpath="docRoot\images\fleet\vessels" virtualpath="/images\fleet\vessels" indent="3"/>
		<EXPORTFOLDER guid="0A3CB43AA16346EABB1497E0A7C96361" name="vessel" realpath="docRoot\images\fleet\vessels\vessel" virtualpath="/images\fleet\vessels\vessel" indent="4"/>
		<EXPORTFOLDER guid="81D37A5F3F2544D491B49BA1C5C5CEF5" name="teu_group" realpath="docRoot\images\fleet\vessels\teu_group" virtualpath="/images\fleet\vessels\teu_group" indent="4"/>
		...
		<EXPORTFOLDER guid="522C0275D8354C2D9981536D920D0951" name="local_info" realpath="docRoot\downloads\local_info" virtualpath="/downloads\local_info" indent="2"/>
		<EXPORTFOLDER guid="1D97AD2F1B4448C7AD5069CBF273F4A1" name="pdf" realpath="docRoot\downloads\pdf" virtualpath="/downloads\pdf" indent="2"/>
		<EXPORTFOLDER guid="6E484E58563B43C09BA54C95AB4F8FDA" name="career" realpath="docRoot\downloads\career" virtualpath="/downloads\career" indent="2"/>
		<EXPORTFOLDER guid="39A0276BC49F4B6FA01A2D29C184D208" name="application_form" realpath="docRoot\downloads\career\application_form" virtualpath="/downloads\career\application_form" indent="3"/>
		...
		<EXPORTFOLDER guid="9E6FD04DDD9241C09E6B3557DD1D95C8" name="es" realpath="i18n\es" virtualpath="i18n\es" indent="1"/>
		<EXPORTFOLDER guid="E9047A12683545ABA604ECA3AD069BE1" name="de" realpath="i18n\de" virtualpath="i18n\de" indent="1"/>
		</EXPORTFOLDERS>
		</IODATA>
		 */

		// call CMS
		if (publicationFoldersCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ "<PROJECT><EXPORTFOLDERS action='list'/></PROJECT></IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			publicationFoldersCache = rqlResponse.getNodes("EXPORTFOLDER");
		}
		return publicationFoldersCache;
	}

	/**
	 * Liefert das PublicationPackage zu der gegebenen GUID. Benötigt den session key!
	 * 
	 * @param packageGuid
	 *            GUID des Exportpaketes
	 */
	public PublicationPackage getPublicationPackageByGuid(String packageGuid) throws RQLException {

		RQLNodeList packageNodeList = getPublicationPackagesNodeList();

		// find export packet
		for (int i = 0; i < packageNodeList.size(); i++) {
			RQLNode node = packageNodeList.get(i);
			String name = node.getAttribute("name");
			String guid = node.getAttribute("guid");
			if (guid.equalsIgnoreCase(packageGuid)) {
				return new PublicationPackage(this, guid, name);
			}
		}
		throw new ElementNotFoundException("Publication package for GUID " + packageGuid + " could not be found in the project.");
	}

	/**
	 * Liefert das PublicationPackage zu dem gegebenen Namen. Benötigt den session key!
	 * 
	 * @param packageName
	 *            Name des Exportpaketes (case ignored!)
	 */
	public PublicationPackage getPublicationPackageByName(String packageName) throws RQLException {

		RQLNodeList packageNodeList = getPublicationPackagesNodeList();

		// find export packet
		for (int i = 0; i < packageNodeList.size(); i++) {
			RQLNode node = packageNodeList.get(i);
			String name = node.getAttribute("name");
			if (name.equalsIgnoreCase(packageName)) {
				return new PublicationPackage(this, node.getAttribute("guid"), name);
			}
		}
		throw new ElementNotFoundException("Publication package named " + packageName + " could not be found in the project.");
	}

	/**
	 * Liefert die node list für alle PublicationPackages in diesem Projekt. Benötigt den session key!
	 */
	private RQLNodeList getPublicationPackagesNodeList() throws RQLException {

		checkSessionKey();
		/*
		 * V5 request <IODATA loginguid="1E9FFB17F81E47CC85C869D67AFA1B1C"
		 * sessionkey="5716817995if25JAL7H5"> <PROJECT> <EXPORTPACKET
		 * action="list"/> </PROJECT> </IODATA> V5 response <IODATA>
		 * <EXPORTPACKETS> <EXPORTPACKET guid="83AB8134B8514467BBA810A901D6D9C2"
		 * name="cps_export_xsl"/> <EXPORTPACKET
		 * guid="95F9353509CF4304A5CCA35A41DBB16D" name="cps_export_xml"/>
		 * <EXPORTPACKET guid="ADE892E696E844B5B83109D83ADDB210"
		 * name="exportsetting_NIKU"/> <EXPORTPACKET
		 * guid="BA80D6AB81F145B3922738CC952BF6DD" name="EXPORTSETTING
		 * generell"/> <EXPORTPACKET guid="E2B0218669A042A49394116AEAE443C2"
		 * name="cps_export_scripts"/> <EXPORTPACKET
		 * guid="F3A632F6376A4D62AEEA06FABCBE4707" name="exportsetting_css"/>
		 * </EXPORTPACKETS> </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PROJECT>"
				+ "   <EXPORTPACKET action='list'/>" + " </PROJECT>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNodes("EXPORTPACKET");
	}

	/**
	 * Liefert den Publication Folder zur gegebenen GUID, oder null, falls keiner auf root-Ebene gefunden werden konnte.
	 * <p>
	 * Benötigt den session key!
	 * <p>
	 */
	public PublicationFolder getPublicationRootFolderByGuid(String publicationFolderGuid) throws RQLException {
		return getPublicationFolderByGuid(PublicationFolder.PUBLICATION_STRUCTURE_TREE_GUID, publicationFolderGuid);
	}

	/**
	 * Liefert den Publication Folder mit dem gegebenen Namen, oder null, falls keiner mit dem Namen existiert aus den children der
	 * Publication Structure (=root folder).
	 * <p>
	 * Benötigt den session key! Checks with equalsIgnoreCase().
	 * <p>
	 */
	public PublicationFolder getPublicationRootFolderByName(String publicationFolderName) throws RQLException {
		return getPublicationFolderByName(PublicationFolder.PUBLICATION_STRUCTURE_TREE_GUID, publicationFolderName);
	}

	/**
	 * Liefert den RQLNode für die Exporteinstellungen dieses Projekt. Zu finden unter Administer Publication / Project / Edit general
	 * settings.
	 */
	private RQLNode getPublicationSettingsNode() throws RQLException {
		/*
		 * V7.5 request <IODATA loginguid="D43446BFDD51477EB1B8F79AC3C3FAA6"
		 * sessionkey="28D889F1770C42DC8E47349B81E412B6"> <PROJECT> <EXPORT
		 * action="get"/> </PROJECT> </IODATA> V7.5 response <IODATA>
		 * <EXPORTSETTINGS action="set" usepageid="1" nopaths=""
		 * generateemptylinks="0" dependingpages="0" generatefolder="0"
		 * refpages="1" createautojobs="1" generaterelativepagesdatebegin="1"
		 * generatenextpagesdatebegin="1" generaterelativepagesdateend="0"
		 * generatenextpagesdateend="1" reflinks="1" standard="html"
		 * presuffixforassignedpages="0" transfermode="0"
		 * generateallvariants="0" generaterelativepages="0"
		 * donotgenerateleadingslash="0" generatenoslashinhtml="1"
		 * dependingpagesfornamedpages="0" clearcache="0" nowarnings="0"
		 * notinthisvariant="1" queuetimer="0" soaptimeout="0" changedonly="0"
		 * cpsdeletelist="1" extramediainfo="1"
		 * uncreddottemppath="\\kswfrd02\cms\ASP\RedDotTemp\" password=""
		 * username="" overwritemedia="0" overwritepages="0"
		 * donotpublishdependentpagesoflinks="0" tcseparatorflag="0"
		 * tcseparator="" languagevariantid="ENG" dialoglanguageid="ENG"
		 * projectguid=""/> </IODATA>
		 */
		if (publicationSettingsCache == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "' >" + "  <PROJECT>"
					+ "   <EXPORT action='get'/>" + "  </PROJECT>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);

			publicationSettingsCache = rqlResponse.getNode("EXPORTSETTINGS");
		}
		return publicationSettingsCache;
	}

	/**
	 * Returns true, if the project setting activate all languages when deleting pages is true, otherwise false.
	 */
	public boolean isActivateAllLanguageVariantWhenDeletingPages() throws RQLException {
		return StringHelper.convertToBooleanFrom01(getProjectSettingsNode().getAttribute("activatealllvondelete"));
	}

	/**
	 * Liefert den RQLNode für die Projekteinstellungen dieses Projekts. Zu finden unter Administer Project Settings / Project /
	 * General Settings/ Edit settings settings.
	 */
	private RQLNode getProjectSettingsNode() throws RQLException {
		/*
		V9 request
		<IODATA loginguid='4698766CDABC4657B38D7E6D702CB821' sessionkey='4B35B0ADD528419880B1D9EEF30FEB2C'>
		  <PROJECT>
		    <SETTINGS action="load" />
		  </PROJECT>
		</IODATA>
		V9 response
		<IODATA>
		<SETTINGS action="load" languagevariantid="ENG" dialoglanguageid="ENG" borderstyle="solid" bordercolor="#e75200" excludeweekdays="" activetemplatecaching="60" contentclassversioning="3" liveserverguid="" executevirtualpath="" removefontsettings="1" executephysicalpath="" requestexterneditortext="" maxcounthomepage="10" loadoptionlists="0" activetemplateextensions="asp" externeditorurl="" allowedstyleattributes="" removestylesettings="1" borderwidth="1" donotremoveptag="1" onclosedotempsave="1" activatealllvontranslation="0" activatealllvondelete="1" parser410compactible="0" useexterneditor="0" donotreplacetop="0" setnamesonlyinmainlanguage="1" rdeditorpreferred="0" templaterelease="0" enabledirectedit="1" hidedisabledreddots="1" wordeditorallowed="0" donotloadtexteditorinform="0" showpagerange="1" navigationmanager="1" preventpageclosing="0"/>
		</IODATA>
		 */
		if (projectSettingsCache == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "' >" + "  <PROJECT>"
					+ "   <SETTINGS action='load'/>" + "  </PROJECT>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);

			projectSettingsCache = rqlResponse.getNode("SETTINGS");
		}
		return projectSettingsCache;
	}

	/**
	 * Liefert die Standard Extension aus dem Bereich File der Exporteinstellungen dieses Projekt (Administer Publication / Project /
	 * Edit general settings).
	 */
	public String getPublicationSettingStandardExtension() throws RQLException {
		return getPublicationSettingsNode().getAttribute("standard");
	}

	/**
	 * Liefert true, falls die im Dateinamen verwendet wird. Aus dem Bereich File der Exporteinstellungen dieses Projekt (Administer
	 * Publication / Project / Edit general settings).
	 * <p>
	 * Pages displayed in target containers use page file names only.
	 */
	public boolean isPublicationSettingTargetContainerPagesUsesOwnFilenameOnly() throws RQLException {
		return getPublicationSettingsNode().getAttribute("tcseparatorflag").equals("2");
	}

	/**
	 * Liefert den Separator zwischen den IDs für Targetcontainerseiten. Das kann der deafult _ sein, oder ein speziell vergebener.
	 * <p>
	 * Aus dem Bereich File der Exporteinstellungen dieses Projekt (Administer Publication / Project / Edit general settings).
	 * <p>
	 * Sollte nicht benutzt werden, wenn
	 * 
	 * @see #isPublicationSettingTargetContainerPagesUsesOwnFilenameOnly() = true ist
	 */
	public String getPublicationSettingTargetContainerSeparator() throws RQLException {
		String result = "";
		if (isPublicationSettingUseDefaultSeparatorInTargetContainers()) {
			result = "_";
		}
		if (isPublicationSettingUseThisSeparatorInTargetContainers()) {
			result = getPublicationSettingUseThisTargetContainerSeparator();
		}
		return result;
	}

	/**
	 * Liefert true, falls die im Dateinamen verwendet wird. Aus dem Bereich File der Exporteinstellungen dieses Projekt (Administer
	 * Publication / Project / Edit general settings).
	 * <p>
	 * Use default separators (_) for pages in target containers
	 */
	public boolean isPublicationSettingUseDefaultSeparatorInTargetContainers() throws RQLException {
		return getPublicationSettingsNode().getAttribute("tcseparatorflag").equals("0");
	}

	/**
	 * Liefert true, falls die GUID im Dateinamen verwendet wird. Aus dem Bereich File der Exporteinstellungen dieses Projekt
	 * (Administer Publication / Project / Edit general settings).
	 */
	public boolean isPublicationSettingUseGuidInFilename() throws RQLException {
		return getPublicationSettingsNode().getAttribute("usepageid").equals("0");
	}

	/**
	 * Liefert true, falls die page ID im Dateinamen verwendet wird. Aus dem Bereich File der Exporteinstellungen dieses Projekt
	 * (Administer Publication / Project / Edit general settings).
	 */
	public boolean isPublicationSettingUsePageIdInFilename() throws RQLException {
		return getPublicationSettingsNode().getAttribute("usepageid").equals("1");
	}

	/**
	 * Liefert true, falls die im Dateinamen verwendet wird. Aus dem Bereich File der Exporteinstellungen dieses Projekt (Administer
	 * Publication / Project / Edit general settings).
	 * <p>
	 * Use this separator for pages in target containers.
	 * <p>
	 * Get the separator from
	 * 
	 * @see #getPublicationSettingUseThisTargetContainerSeparator()
	 */
	public boolean isPublicationSettingUseThisSeparatorInTargetContainers() throws RQLException {
		return getPublicationSettingsNode().getAttribute("tcseparatorflag").equals("1");
	}

	/**
	 * Liefert den Separator zwischen den IDs für Targetcontainerseiten. Aus dem Bereich File der Exporteinstellungen dieses Projekt
	 * (Administer Publication / Project / Edit general settings). Sollte nur benutzt werden, wenn
	 * 
	 * @see #isPublicationSettingUseThisSeparatorInTargetContainers() = true ist
	 */
	public String getPublicationSettingUseThisTargetContainerSeparator() throws RQLException {
		return getPublicationSettingsNode().getAttribute("tcseparator");
	}

	/**
	 * Liefert genau die Projektvarianten zurück, die der angemeldete Benutzer auch publizieren kann.
	 */
	java.util.List<ProjectVariant> getPublishableProjectVariants() throws RQLException {
		checkSessionKey();
		/* 
		 V5 request
		<IODATA loginguid="6D17CC455FF645A4BB20260A8F46FD80" sessionkey="EA7C96BB88FA42E19728AC867A4D6E67">
		<PROJECT>
		<EXPORTJOB action="load">
		ausgelassen: <LANGUAGEVARIANTS action="listall" includerights="1" />
		<PROJECTVARIANTS action="listall" includerights="1" />
		</EXPORTJOB>
		</PROJECT>
		</IODATA>
		 V5 response
		<IODATA>
		<EXPORTJOB action="load" languagevariantid="ENG" parentguid="6A070A770D6E4DA1B5694EF25ED2E223" parenttype="PAG" notindb="1" projectguid="">
		ausgelassen: <LANGUAGEVARIANTS action="listall" includerights="1" parentguid="" generateallvariants="0" checkexisting="1">
		ausgelassen: <LANGUAGEVARIANT guid="03FF9B01B2AD4CB3B52B75D308F964B8" name="English" language="ENG" codetable="65001" checked="1" ismainlanguage="1" textdirection="" rfclanguageid="en-US"/>
		ausgelassen: <LANGUAGEVARIANT guid="9AE7DE90A14A456683B3AE101858E51E" name="German" language="DEU" codetable="65001" checked="0" ismainlanguage="0" textdirection="" rfclanguageid="de-DE"/>
		ausgelassen: </LANGUAGEVARIANTS>
		<PROJECTVARIANTS action="listall" parentguid="" type="" checkexisting="1" generateallvariants="0">
		<PROJECTVARIANT guid="860637BD08D74A47BB31EF920F0CD564" checked="0" name="TEST_page_config_xml"/>
		<PROJECTVARIANT guid="81AA826446594CF79EF261D317A97A00" checked="0" name="TEST_pages_html"/>
		</PROJECTVARIANTS>
		</EXPORTJOB>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<PROJECT><EXPORTJOB action='load' >" + "<PROJECTVARIANTS action='listall' includerights='1' />"
				+ "</EXPORTJOB></PROJECT>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		// prepare wrap
		RQLNodeList nodes = rqlResponse.getNodes("PROJECTVARIANT");
		java.util.List<ProjectVariant> result = new ArrayList<ProjectVariant>();

		// no project variant
		if (nodes == null) {
			return result;
		}
		// wrap into objects
		for (int i = 0; i < nodes.size(); i++) {
			RQLNode node = nodes.get(i);
			result.add(buildProjectVariant(node));
		}
		return result;
	}

	/**
	 * Liefert den definierten Publishing Job zur gegebenen GUID zurück.
	 */
	public PublishingJob getPublishingJobByGuid(String publishingJobGuid) throws RQLException {

		RQLNodeList jobsNodeList = getPublishingJobsNodeList();

		// find job
		for (int i = 0; i < jobsNodeList.size(); i++) {
			RQLNode node = jobsNodeList.get(i);
			if (node.getAttribute("guid").equals(publishingJobGuid)) {
				return new PublishingJob(this, node.getAttribute("name"), node.getAttribute("guid"));
			}
		}
		throw new ElementNotFoundException("Defined publishing job with guid " + publishingJobGuid
				+ " could not be found in the project.");
	}

	/**
	 * Liefert den definierten Publishing Job für den gegebenen Namen zurück.
	 */
	public PublishingJob getPublishingJobByName(String publishingJobName) throws RQLException {

		RQLNodeList jobsNodeList = getPublishingJobsNodeList();

		// find job
		for (int i = 0; i < jobsNodeList.size(); i++) {
			RQLNode node = jobsNodeList.get(i);
			if (node.getAttribute("name").equals(publishingJobName)) {
				return new PublishingJob(this, node.getAttribute("name"), node.getAttribute("guid"));
			}
		}
		throw new ElementNotFoundException("Defined publishing job with name " + publishingJobName
				+ " could not be found in the project.");
	}

	/**
	 * Liefert die RQLNodeList mit allen vordefinierten PublishinJobs dieses Projektes. Benötigt den session key!
	 * 
	 * @return <code>RQLNodeList</code>
	 */
	private RQLNodeList getPublishingJobsNodeList() throws RQLException {

		checkSessionKey();
		/*
		V7.5. request
		<IODATA loginguid="E86954A19D204CDF8AFCAFC6AF692E45" sessionkey="8B2A75E07AF74738AF1688F2567D9429">
		  <PROJECT guid="06BE79A1D9F549388F06F6B649E27152">
		    <EXPORTJOBS action="list"/>
		  </PROJECT>
		</IODATA>
		V7.5 response
		<IODATA><EXPORTJOBS>
		<EXPORTJOB guid="0940D932D95D4DAE8EBD6060DD57D33D" name="a business admin tree to PROD" reddotserver="" />
		<EXPORTJOB guid="0BFDB1D9A3E34F358369B7B5F1D70D2F" name="a human resources tree to PROD" reddotserver="" />
		<EXPORTJOB guid="32254BD6586D4B31A9103B0364DDEF93" name="z for external http server" reddotserver="" />
		<EXPORTJOB guid="37D512BB049647F99EA120D9C55A226A" name="b publish company, sales, cs, local trees to PROD" reddotserver="" />
		...
		<EXPORTJOB guid="FCE29D439D15437A85CAFA17D8A6C919" name="a local tree to PROD" reddotserver="" />
		<EXPORTJOB guid="FF8533218EA143019D5683D288B09F67" name="z DVD Export whole HIP nach \\kswfrd02\cms_data\published\hip.hlcl.com_fs" reddotserver="" />
		</EXPORTJOBS>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + " <PROJECT guid='"
				+ getProjectGuid() + "'>" + "  <EXPORTJOBS action='list'/>" + "  </PROJECT></IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		return rqlResponse.getNodes("EXPORTJOB");
	}

	/**
	 * Liefert das Exportziel fuer die gegebene Guid vom CMS zurück. Benötigt den session key!
	 * 
	 * @param publishingTargetGuid
	 *            GUID des Exportziels.
	 */
	public PublishingTarget getPublishingTargetByGuid(String publishingTargetGuid) throws RQLException {

		return buildPublishingTarget(findPublishingTargetNodeByGuid(publishingTargetGuid));
	}

	/**
	 * Liefert das 1. Exportziel fuer den gegebenen Namensprefix vom CMS zurück. Benötigt den session key!
	 */
	public PublishingTarget getPublishingTargetByNameStartsWith(String publishingTargetNamePrefix) throws RQLException {

		return buildPublishingTarget(findPublishingTargetNodeByNameStartsWith(publishingTargetNamePrefix));
	}

	/**
	 * Liefert alle Exportziele dieses Projekts zurück. Benötigt den session key!
	 */
	public java.util.List<PublishingTarget> getPublishingTargets() throws RQLException {

		RQLNodeList targetsNodeList = getPublishingTargetsNodeList();

		// wrap publishing targets
		java.util.List<PublishingTarget> targets = new ArrayList<PublishingTarget>();
		for (int i = 0; i < targetsNodeList.size(); i++) {
			RQLNode node = targetsNodeList.get(i);
			targets.add(buildPublishingTarget(node));
		}
		return targets;
	}

	/**
	 * Liefert NodeList aller Exportziele zurück.
	 * 
	 */
	private RQLNodeList getPublishingTargetsNodeList() throws RQLException {

		checkSessionKey();
		/*
		V7.5 request
		<IODATA loginguid="DCAC4B868636481B891A9DD19A0D53D6" sessionkey="624ABB35A257416D9155C9298033E35C">
		  <PROJECT>
		    <EXPORT guid="32E77E3255B143FCB46D2614B63ECDC2" action="load"/>
		  </PROJECT>
		</IODATA>
		V7.5 response
		<IODATA><EXPORTS>
		<EXPORT guid="C6170FFF36F44F6199EB9149F6D19B4D" name="ARCHIVE_cmsdata_kswfrd02_f" path="f:\cms_data\published\hlag_wm2008_archive" type="6206" archive="0" environment="" />
		<EXPORT guid="32E77E3255B143FCB46D2614B63ECDC2" name="DEVE_webdeve.ad.hl.lan_khh30007" path="ftp://khh30007.ad.hl.lan/hl/fis/deve/web/portal/publishRoot" type="6205" archive="0" environment="" />
		<EXPORT guid="88CDF1361D7B4450B250B8DC015BFDD7" name="MAILDEVE_webnewsdeve.ad.hl.lan_khh31032" path="ftp://khh31032.ad.hl.lan/hl/opt/umweb/UM/cmsbs-work/reddot/published" type="6205" archive="0" environment="" />
		<EXPORT guid="7916969D70844C44B7E555A03CF04260" name="MAILPROD_webnewsprod.hlcl.com_kswfww03" path="ftp://kswfww03.vlan08.swf.hlcl.com/hl/opt/umweb/UM/cmsbs-work/reddot/published" type="6205" archive="0" environment="" />
		<EXPORT guid="EE2B468244A64466BF29E80F0CD76674" name="MAILTEST_webnewstest.hlcl.com_kswfdp07" path="ftp://kswfdp07.vlan08.swf.hlcl.com/hl/opt/umweb/UM/cmsbs-work/reddot/published" type="6205" archive="0" environment="" />
		<EXPORT guid="2FFF4EE2242B4A9D8100B219EB0A38C1" name="PROD_www.hapag-lloyd.com_kswfww01" path="ftp://kswfww01/hl/fis/prod/web/portal/publishRoot" type="6205" archive="0" environment="" />
		<EXPORT guid="AE51B1D0C163420CADA76C7A529D2D8C" name="TEST_wwwtest.hapag-lloyd.com_kswfdp05" path="ftp://kswfdp05.hlcl.com/hl/fis/test/web/portal/publishRoot" type="6205" archive="0" environment="" />
		<EXPORT guid="FCE09C1ED4204CFE98C9C55156DBD4F5" name="WORK_fisworkweb.ad.hl.lan_khh30035" path="ftp://khh30035.ad.hl.lan/hl/fis/work/web/portal/publishRoot" type="6205" archive="0" environment="" />
		</EXPORTS>
		</IODATA>
		 */

		// call CMS
		if (publishingTargetsCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PROJECT>"
					+ "   <EXPORTS action='list'/>" + " </PROJECT>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			publishingTargetsCache = rqlResponse.getNodes("EXPORT");
		}
		return publishingTargetsCache;
	}

	/**
	 * Liefert den Papierkorb dieses Projektes zurück.
	 */
	public RecycleBin getRecycleBin() {
		return new RecycleBin(this);
	}

	/**
	 * Liefert eine JobQueue mit allen aktuell laufenden publishing Jobs.
	 * <p>
	 * Kann nur von Administratoren verwendet werden!
	 */
	public JobQueue getRunningPublishingJobs() throws RQLException {

		return getJobs(JobQueue.JOB_CATEGORY_PUBLICATION, JobQueue.JOB_STATUS_RUNNING);
	}

	/**
	 * Liefert den RedDot Session Key.
	 * <p>
	 * Ist keine Session vorhanden, können nur Administrative Funktionen dieses Projektes genutzt werden.
	 * 
	 * @see <code>checkSessionKey</code>
	 */
	public String getSessionKey() {

		return sessionKey;
	}

	/**
	 * Liefert die ungeparste Antwort für die Elemente im SmartTree beim Öffnen zurück.
	 * <p>
	 * ACHTUNG. Das gelieferte RQL ist seit V7.5 nicht mehr gültig. Es tauchen doppelte Attribute innerhalb eines Tags auf. Das parsen
	 * muss daher per Hand erfolgen.
	 * 
	 * @see StringHelper#getStartTag
	 * @see StringHelper#getAttributeValue
	 */
	String getSmartTreeSegments(String type, String guid) throws RQLException {
		/*
		 * V5 request <IODATA loginguid="7262131E70034E0B849433E2EED4B9E7"
		 * sessionkey="6920826335vSsCYlyDg8"> <TREESEGMENT type="link"
		 * action="load" guid="97FF3FAA0F554076BCE5EE028ECABDF0" /> </IODATA>
		 * 
		 * V5 response (type project.1115 identify a workflow in the tree)
		 * <IODATA> <TREESEGMENTS> <SEGMENT
		 * parentguid="97FF3FAA0F554076BCE5EE028ECABDF0"
		 * guid="82208942EB9F48EEA58854222EAFE2AA" type="project.1115"
		 * image="workflow.gif" expand="1" value="wf_old_layout_check"
		 * col1value="wf_old_layout_check" col2fontcolor="#ff8C00" col2value=""
		 * col1fontweight="normal" col2fontweight="normal" ></SEGMENT> ...
		 * </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <TREESEGMENT type='" + type + "' action='load' guid='" + guid + "'/>" + "</IODATA>";
		return callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert die Startseite dieses Projektes, falls mit Username und Passwort angemeldet wurde. Nutzt dieses Projekt den sessionKey
	 * des Users mit, wird null geliefert. Aus dem sessionKey kann ein normaler Autor die Startseite nicht ermitteln, das kann nur ein
	 * Admin.
	 * 
	 * @return Page
	 */
	public Page getStartPage() throws RQLException {

		checkSessionKey();
		if (startPageGuid == null) {
			throw new RQLException(
					"Start page of project "
							+ getProjectGuid()
							+ " cannot be determined because you used given sessionKey. You have to authenticate via user name and password to use this function.");
		}
		return new Page(this, startPageGuid);
	}

	/**
	 * Liefert die GUID der Startseite dieses Projektes, falls Anmeldung über user name and password. Wird der sessionKey mitbenutzt,
	 * wird hier null geliefert.
	 */
	public String getStartPageGuid() throws RQLException {

		checkSessionKey();
		return startPageGuid;
	}

	/**
	 * Liefert das Template mit der gegebenen RedDot GUID vom CMS zurück. Benötigt den session key!
	 * 
	 * @param templateGuid
	 *            RedDot GUID des Templates
	 * @return Template
	 * @see Template
	 */
	public Template getTemplateByGuid(String templateGuid) throws RQLException {

		/*
		 * V5 request <IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
		 * <PROJECT> <TEMPLATE action="load" guid="[!guid_template!]"/>
		 * </PROJECT> </IODATA> V5 response <IODATA> <TEMPLATE action="load"
		 * guid="[!guid_template!]" languagevariantid="DEU"
		 * parentobjectname="PROJECT" useconnection="1" dialoglanguageid="DEU"
		 * templaterights="2147483647" name="template_name" description=""
		 * approverequired="0" framesetafterlist="0" filenamerequired="0"
		 * ignoreglobalworkflow="0" webserverpreview="0" selectinnewpage="0"
		 * folderguid="[!guid_folder!]" praefixguid=""
		 * suffixguid="[!guid_suffix!]" folderrelease="0"/> </IODATA>
		 */

		Template template = null;

		// check if already in cache
		if (templatesCache.containsKey(templateGuid)) {
			template = (Template) templatesCache.get(templateGuid);
		} else {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
					+ "   <TEMPLATE action='load' guid='" + templateGuid + "' />" + "  </PROJECT>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			RQLNode templateNode = rqlResponse.getNode("TEMPLATE");

			// wrap template data
			template = new Template(getTemplateFolderByGuid(templateNode.getAttribute("folderguid")), templateNode
					.getAttribute("name"), templateNode.getAttribute("guid"), templateNode.getAttribute("description"));

			// add to cache
			templatesCache.put(templateGuid, template);
		}
		return template;
	}

	/**
	 * Liefert das Template mit dem gegebenen Namen aus dem gegebenen template folder. Benötigt den session key!
	 */
	public Template getTemplateByName(String templateFolderName, String templateName) throws RQLException {
		checkSessionKey();
		return getTemplateFolderByName(templateFolderName).getTemplateByName(templateName);
	}

	/**
	 * Liefert den TemplateFolder fuer die gegebene Guid vom CMS zurück. Benötigt den session key!
	 * 
	 * @param templateFolderGuid
	 *            GUID des Template Folder.
	 * @return TemplateFolder
	 * @see TemplateFolder
	 */
	public TemplateFolder getTemplateFolderByGuid(String templateFolderGuid) throws RQLException {

		return buildTemplateFolder(findTemplateFolderNodeByGuid(templateFolderGuid));
	}

	/**
	 * Liefert den TemplateFolder mit dem gegebenen Namen vom CMS zurück. Benötigt den session key!
	 * 
	 * @param name
	 *            Name des Template Folder.
	 * @return TemplateFolder
	 * @see TemplateFolder
	 */
	public TemplateFolder getTemplateFolderByName(String name) throws RQLException {

		return buildTemplateFolder(findTemplateFolderNodeByName(name));
	}

	/**
	 * Liefert die NodeList aller TemplateFolder dieses Projekts.
	 * 
	 * @return RQLNodeList liste der Nodes
	 * @see RQLNodeList
	 */
	private RQLNodeList getTemplateFoldersNodeList() throws RQLException {

		checkSessionKey();
		/*
		 * V5 request <IODATA loginguid="78E223D7328E45C4BAAC4B4B53D7A42E"
		 * sessionkey="10157736883NLqKJ8uK53"> <TEMPLATEGROUPS action="load" />
		 * </IODATA> V5 response <IODATA> <TEMPLATEGROUPS> <GROUP
		 * guid="0407DDF4EADA4DF3962A4EBD3E00F7E6" name="admin_templates"></GROUP>
		 * <GROUP guid="6A6740BC44F7459081BFD1F25B1BF8F6"
		 * name="content_templates"></GROUP> <GROUP
		 * guid="4C5E6DEA5CF4424DB44553E12484B54B" name="navigation_templates"></GROUP>
		 * <GROUP guid="DD8BDB6FBA954470B6F9466032653F30" name="test_templates"></GROUP>
		 * </TEMPLATEGROUPS> </IODATA>
		 */

		// call CMS
		// mephistopheles78
		if (templateFoldersCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ "<PROJECT><FOLDERS action='list' /></PROJECT>" + "</IODATA>"; // changed from + "<TEMPLATEGROUPS action='load' />" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);

			templateFoldersCache = rqlResponse.getNodes("FOLDER");  //changed from GROUP to FOLDER
		}
		return templateFoldersCache;

	}

	/**
	 * Liefert die Benutzergruppe zu der gegebenen GUID. ACHTUNG: Dafür sind admin Rechte erforderlich!
	 * 
	 * @throws ElementNotFoundException
	 */
	public UserGroup getUserGroupByGuid(String userGroupGuid) throws RQLException {

		RQLNodeList userGroupsNodeList = getUserGroupsNodeList();

		for (int i = 0; i < userGroupsNodeList.size(); i++) {
			RQLNode node = (RQLNode) userGroupsNodeList.get(i);
			String guid = node.getAttribute("guid");
			if (guid.equals(userGroupGuid)) {
				return new UserGroup(getCmsClient(), userGroupGuid, node.getAttribute("name"), node.getAttribute("email"));
			}
		}
		// group not found
		throw new ElementNotFoundException("User group with GUID " + userGroupGuid + " cannot be found in the project.");
	}

	/**
	 * Liefert die Benutzergruppe mit dem gegebenen Namen. Benötigt Admin-Rechte!
	 * 
	 * @throws ElementNotFoundException
	 */
	public UserGroup getUserGroupByName(String userGroupName) throws RQLException {

		RQLNodeList userGroupsNodeList = getUserGroupsNodeList();

		for (int i = 0; i < userGroupsNodeList.size(); i++) {
			RQLNode node = (RQLNode) userGroupsNodeList.get(i);
			String name = node.getAttribute("name");
			if (name.equals(userGroupName)) {
				return new UserGroup(getCmsClient(), node.getAttribute("guid"), userGroupName, node.getAttribute("email"));
			}
		}
		// group not found
		throw new ElementNotFoundException("User group named " + userGroupName + " cannot be found in the project.");
	}

	/**
	 * Liefert alle Benutzergruppen dieses Projektes. Benötigt Admin-Rechte!
	 */
	public java.util.List<UserGroup> getUserGroups() throws RQLException {

		RQLNodeList userGroupsNodeList = getUserGroupsNodeList();
		java.util.List<UserGroup> userGroups = new ArrayList<UserGroup>(userGroupsNodeList.size());
		RQLNode node = null;

		for (int i = 0; i < userGroupsNodeList.size(); i++) {
			node = (RQLNode) userGroupsNodeList.get(i);
			userGroups.add(new UserGroup(getCmsClient(), node.getAttribute("guid"), node.getAttribute("name"), node
					.getAttribute("email")));
		}
		return userGroups;
	}

	/**
	 * Liefert alle Benutzergruppen dieses Projektes, die mit dem prefix beginnen. Benötigt Admin-Rechte!
	 */
	public java.util.List<UserGroup> getUserGroupsNameStartsWith(String prefix) throws RQLException {

		RQLNodeList userGroupsNodeList = getUserGroupsNodeList();
		java.util.List<UserGroup> userGroups = new ArrayList<UserGroup>(userGroupsNodeList.size());
		RQLNode node = null;

		for (int i = 0; i < userGroupsNodeList.size(); i++) {
			node = (RQLNode) userGroupsNodeList.get(i);
			String name = node.getAttribute("name");
			if (name.startsWith(prefix)) {
				userGroups.add(new UserGroup(getCmsClient(), node.getAttribute("guid"), name, node.getAttribute("email")));
			}
		}
		return userGroups;
	}

	/**
	 * Liefert die RQLNodeList mit allen Benutzergruppen dieses Projektes. Benötigt Admin-Rechte!
	 * 
	 * @return <code>RQLNodeList</code>
	 */
	private RQLNodeList getUserGroupsNodeList() throws RQLException {

		checkSessionKey();
		/*
		 * V5 request <IODATA loginguid="415D151C22C94C17AD30E9BB66AA57DF"
		 * sessionkey="784760628gH4sx022rcF"> <ADMINISTRATION> <PROJECT
		 * guid="06BE79A1D9F549388F06F6B649E27152"> <GROUPS action="list"/>
		 * </PROJECT> </ADMINISTRATION> </IODATA> V5 response <IODATA> <PROJECT
		 * guid="06BE79A1D9F549388F06F6B649E27152"> <GROUPS action="list"
		 * parentguid="06BE79A1D9F549388F06F6B649E27152"> <GROUP
		 * guid="81046ED3C4F245F5BCC2F92BAF10197A"
		 * name="admin+internally-area-admins" email=""/> <GROUP
		 * guid="1F0587C33FD04874A2530269A0590BEF" name="hip all users"
		 * email="lejafr@hlcl.com"/> <GROUP
		 * guid="8D2C1969E13840AE8E48BBD3AE136B4E" name="information-area-admin"
		 * email=""/> <GROUP guid="FF8B366CAF3F4E849B64EC9E51AAA0FD"
		 * name="mail-sender" email=""/> <GROUP
		 * guid="32621BC695A5421EB84037E222799112" name="niku authors"
		 * email=""/> <GROUP guid="6026AD40E7B6464FAE56511CB9FB14BE"
		 * name="productive-portal-pages-admins" email=""/> <GROUP
		 * guid="7F35DAAB3E3F452A93E752243C883053" name="rql-script-area-admins"
		 * email=""/> <GROUP guid="352D4D389ECF47C1BF9CE690EF5D9D8E"
		 * name="tree-root-admins" email=""/> <GROUP
		 * guid="B020203886AE43A38F26A492EEEEC2F6" name="wf-authors-excluded"
		 * email=""/> <GROUP guid="B44188BBF15944489FDC30A3E0DFB71C"
		 * name="wf-authors-included" email=""/> <GROUP
		 * guid="21C7D35DD8FC4A48923433057377473F" name="wf-linking-pages"
		 * email=""/> <GROUP guid="60CDB45D66F6403E96E45316D22A24EB"
		 * name="wf-structure-controll-blocks" email=""/> <GROUP
		 * guid="B3BF4180B3184C728CBBA2FC55051CF0"
		 * name="wf-structure-controll-pages" email=""/> <GROUP
		 * guid="75D0C136086046579D444B81D3079DC3" name="work-area-admins"
		 * email=""/> <GROUP guid="29ECD50138934E47820C147184BA2611"
		 * name="work-area-business-adminstration-human-resource-region-west"
		 * email=""/> <GROUP guid="33D299FA81EC45DDB43053FCE0341CA0"
		 * name="work-area-finance-accounting" email=""/> <GROUP
		 * guid="90FF621947BD420395D2C27F333643F4" name="work-area-gbs"
		 * email=""/> <GROUP guid="49CF24F1DD654360A5DD673024A2A3F6"
		 * name="work-area-hamburg-auszubildende" email=""/> <GROUP
		 * guid="68B92A0F540940EB9387B456C7CAB4CB"
		 * name="work-area-human-resource" email=""/> <GROUP
		 * guid="71C5944605D4407CB2A485BCC03B2BAE"
		 * name="work-area-information-technology" email=""/> <GROUP
		 * guid="16132095112C4617949F4C8C28D21480" name="work-area-operation"
		 * email=""/> <GROUP guid="090CF5988C2F41C89CC26D78B1858D20"
		 * name="work-area-product-management" email=""/> <GROUP
		 * guid="2F205B29727C43BFAF1F6B8FB4135D3E" name="work-area-steercoms"
		 * email=""/> </GROUPS> </PROJECT> </IODATA>
		 */

		// call CMS
		if (userGroupNodeListCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ " <ADMINISTRATION>" + "  <PROJECT guid='" + getProjectGuid() + "'>" + "   <GROUPS action='list'/>"
					+ "  </PROJECT>" + " </ADMINISTRATION>" + "</IODATA>";
			userGroupNodeListCache = callCms(rqlRequest).getNodes("GROUP");
		}

		return userGroupNodeListCache;
	}

	/**
	 * Liefert eine JobQueue mit allen aktuell auf den start wartenden publishing Jobs.
	 * <p>
	 * Kann nur von Administratoren verwendet werden!
	 */
	public JobQueue getWaitingPublishingJobs() throws RQLException {

		return getJobs(JobQueue.JOB_CATEGORY_PUBLICATION, JobQueue.JOB_STATUS_WAITING_FOR_START);
	}

	/**
	 * Liefert den Workflow mit dem gegebenen Namen zurück.
	 * 
	 * @param workflowName
	 *            Name des Workflows (case ignored!)
	 */
	public Workflow getWorkflowByName(String workflowName) throws RQLException {

		RQLNodeList workflowNodeList = getWorkflowNodeList();

		// find workflow
		for (int i = 0; i < workflowNodeList.size(); i++) {
			RQLNode node = workflowNodeList.get(i);
			String name = node.getAttribute("name");
			if (name.equalsIgnoreCase(workflowName)) {
				return new Workflow(this, node.getAttribute("guid"), name);
			}
		}
		throw new ElementNotFoundException("Workflow named " + workflowName + " could not be found in the project.");
	}

	/**
	 * Liefert die Nodelist aller Workflows dieses Projekts zurück.
	 */
	private RQLNodeList getWorkflowNodeList() throws RQLException {

		checkSessionKey();
		/*
		 * V5 request <IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
		 * <WORKFLOWS action="list" /> </IODATA> V5 response <IODATA>
		 * <WORKFLOWS> <WORKFLOW guid="[!guid_workflow!]" name="Workflow_001" />
		 * <WORKFLOW guid="[!guid_workflow!]" name="Workflow_002" /> ...
		 * </WORKFLOWS> </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <WORKFLOWS action='list' />" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNodes("WORKFLOW");
	}

	/**
	 * Macht alle gegebenen Benutzer in diesem Projekt zu Autoren.
	 * <p>
	 * Hat ein User bereits mehr Rechte (Editor, Site Builder oder Administrator) bleiben seine Rechte unverändert.
	 */
	public void grantAuthorRight(java.util.List<User> users) throws RQLException {
		/*
		 V5 request 
		 <IODATA loginguid="D3DEF03C98E344F089832C426FEE4BBB">
		 <ADMINISTRATION> 
		 <USER guid="8898998310DD4513BB8CC1771FFD00BC">
		 <PROJECT guid="5256C671655D4CE696F663C73CE3E526"> 
		 <LICENSE action="save" level="4" /> 
		 </PROJECT> 
		 </USER> 
		 </ADMINISTRATION>
		 </IODATA> 
		 V5 response 
		 <IODATA> 
		 <LICENSE action="save" level="4" userguid="8898998310DD4513BB8CC1771FFD00BC" projectguid="5256C671655D4CE696F663C73CE3E526" guid="EF412E884B8A4546B1C55E6C60DEFE56"/> 
		 </IODATA>
		 */

		for (int i = 0; i < users.size(); i++) {
			User user = (User) users.get(i);
			if (!user.hasMoreRightsAsAnAuthor(this)) {
				// call CMS
				String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "<ADMINISTRATION>" + "<USER guid='"
						+ user.getUserGuid() + "'>" + " <PROJECT guid='" + getProjectGuid() + "'>"
						+ "  <LICENSE action='save' level='" + User.LICENSE_AUTHOR + "'/>" + " </PROJECT>" + "</USER>"
						+ "</ADMINISTRATION>" + "</IODATA>";
				callCms(rqlRequest);
			}
		}
	}

	/**
	 * Deaktivert alle (aktuell aktiven) vordefinierten Publishing jobs und liefert die geänderten Jobs zurück. Benötigt den
	 * sessionKey!
	 */
	public java.util.List<PublishingJob> inactivateAllPublishingJobs() throws RQLException {
		java.util.List<PublishingJob> result = getAllActivePublishingJobs();
		for (PublishingJob publishingJob : result) {
			publishingJob.setIsActive(false);
		}
		return result;
	}

	
	/**
	 * Macht dieses Project ungültig. D.h. es kann nach Aufruf dieser Methode nicht mehr für Zugriffe benutzt werden. Wird am
	 * <code>CmsClient</code> das Project gewechselt, wird diese Methode aufgerufen.
	 */
	void invalidate() {

		cmsClient = null;
		sessionKey = null;
		projectGuid = null;
		flushLocalCaches();
	}

	
	/**
	 * Clear/empty local caches.
	 * FIXME: This should distinguis between language-dependant and global caches,
	 * 	      but for now we are lucky to get rid of everything that may lead to
	 * 	      de-sync.
	 */
	void flushLocalCaches()
	{
		affixesCache = null;
		detailsNodeCache = null;
		languageVariantsCache = null;
		projectTreesegmentsCache = null;
		projectVariantsCache = null;
		publicationSettingsCache = null;
		projectSettingsCache = null;
		publishingTargetsCache = null;
		publicationFoldersCache = null;
		pageCache.clear();
		templateFoldersCache = null;
		templatesCache.clear();
		parametersCache = null;
		pagesCache = null;
		foldersCache.clear();
		userGroupNodeListCache = null;
	}

	
	/**
	 * Liefert true, falls die augenblickliche Sprachvariante die Hauptsprachvariante ist, sonst false.
	 */
	public boolean isCurrentLanguageVariantMainLanguage() throws RQLException {

		return getCurrentLanguageVariant().isMainLanguage();
	}

	/**
	 * Sperrt das Projekt für alle Benutzer inkl. Administratoren.
	 * 
	 * @param level
	 *            Art der Sperre -1= entire project 0 = unlock project 1 = below administrator 2 = below site builder 3 = below editor
	 *            4 = below author 5 = visitor
	 * @param message
	 *            Nachricht für Benutzer, die sich versuchen anzumelden. should be null if level = 0
	 */
	private void lock(int level, String message) throws RQLException {
		/*
		 * V5 request <IODATA loginguid="86A9760CBCFE41F78A89F37D12A448CC">
		 * <ADMINISTRATION> <PROJECT action="save"
		 * guid="50552941FD534381B12E5BCD71AD538F" inhibitlevel="-1"
		 * lockinfo="test message fle"/> </ADMINISTRATION> </IODATA> V5 response
		 * <IODATA> <PROJECT action="save" inhibitlevel="-1" lockinfo="test
		 * message fle" guid="50552941FD534381B12E5BCD71AD538F"
		 * projectguid="50552941FD534381B12E5BCD71AD538F"
		 * projectname="intranet_usa_pilot_templates"/> </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "  <ADMINISTRATION>" + "   <PROJECT action='save' guid='"
				+ getProjectGuid() + "' inhibitlevel='" + level + "' lockinfo='" + (level == 0 ? " " : message) + "'/>"
				+ "  </ADMINISTRATION>" + "</IODATA>";
		callCms(rqlRequest);
	}

	/**
	 * Sperrt dieses Projekt für alle Benutzer inkl. Administratoren.
	 * 
	 * @param lockInformationMessage
	 *            Nachricht für Benutzer, die sich versuchen anzumelden.
	 */
	public void lock(String lockInformationMessage) throws RQLException {

		lock(-1, lockInformationMessage);
	}

	/**
	 * Abmelden aller an diesem Projekt angemeldeten Benutzer außer dem User, der das Script gestartet hat.
	 */
	public void logoutActiveUsers() throws RQLException {

		java.util.List activeUsers = getActiveUsers();
		for (int i = 0; i < activeUsers.size(); i++) {
			User user = (User) activeUsers.get(i);
			// prevent logout of running user
			if (user.getOwnLogonGuid().equals(getLogonGuid())) {
				continue;
			}
			user.logout();
		}
	}

	/**
	 * Adds the given pages under the given key.
	 */
	public PageArrayList putPages(String key, PageArrayList pages) {
		return getPagesCache().put(key, pages);
	}

	/**
	 * Erstellt eine RD Referenz (action=ReferenceToElement).
	 * <p>
	 * Achtung: Nur als Administrator aufrufbar!
	 */
	void referenceElement(String sourceElementGuid, String targetElementGuid, String referenceType) throws RQLException {
		// das clipboard muss aber nicht mit diesem element gefüllt sein, sehr praktisch
		/* 
		V7.5 request
		normales standard feld text einer seite
		<IODATA loginguid="B6D7E3C76ADB4ED99D5A04732F471E33" sessionkey="76BF10A6C61C4F95B459701A8DBC97F3">
		<CLIPBOARD action="ReferenceToElement" guid="1FC94EA8535F4126AD2A0F5B17F784D1" type="element" descent="unknown">
		<ENTRY guid="4F265F725E584D999BF4DCE70C4B5B56" type="element" descent="unknown" />
		</CLIPBOARD>
		</IODATA>
		
		template element to seitenelement
		<CLIPBOARD action="ReferenceToElement" guid="145C6ACE4C824AE3AF1014E10756BCC4" type="project.4156" descent="unknown">

		V7.5 response
		<IODATA>ok
		</IODATA>
		 */

		// create the reference using the given target element guid als clipboard entry guid
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<CLIPBOARD action='ReferenceToElement' guid='" + sourceElementGuid + "' type='" + referenceType
				+ "' descent='unknown'>" + "<ENTRY guid='" + targetElementGuid + "'  type='element' descent='unknown' />"
				+ "</CLIPBOARD></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Erstellt eine RD Referenz von der gegebenen Link GUID sourceLinkGuid zu der gebebenen Link GUID targetLinkGuid.
	 * <p>
	 * Hier als zentrale Stelle für die Verwendung in Anchor und MultiLink.
	 */
	public void referenceLinkToLink(String sourceLinkGuid, String targetLinkGuid) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="1EEC6EB5414B4B86BA4301DF962EB7FC" sessionkey="10218343231Wo485iUG7s">
		 <LINK action="assign" guid="26F489CBB7FF456FB56FBFDD9C7B02A9">
		 <LINK guid="E6FFF629FF1F4DEC8289FE835E0C444F"/>
		 </LINK>
		 </IODATA> 
		 
		 V5 response
		 <IODATA>
		 <LINK action="assign" sessionkey="10218343231Wo485iUG7s" dialoglanguageid="ENG" languagevariantid="DEU" ok="1" guid="26F489CBB7FF456FB56FBFDD9C7B02A9" templateelementguid="E143EA3D3ABF485AB4E08596559A7732" pageguid="9C1B34B626454394A61D7515481D8EDB" eltflags="4194320" flags="4194320" eltrequired="0" eltdragdrop="0" islink="2" formularorderid="0" orderid="1" status="0" name="rql_server" eltname="rql_server" aliasname="rql_server" variable="rql_server" type="13" elttype="13" templateelementflags="4194320" templateelementislink="2" reddotdescription="" value="rql_server">
		 <LINK linkguid="26F489CBB7FF456FB56FBFDD9C7B02A9" targetlinkguid="" parenttable="PGE" sessionkey="10218343231Wo485iUG7s" languagevariantid="DEU" action="assign" ok="1" guid="E6FFF629FF1F4DEC8289FE835E0C444F" templateelementguid="82F8EFBB91414DDD98E874C5301E3AA9" pageguid="F1B368470EFB42CC89FACE728D07BF91" eltflags="4194320" flags="20971536" eltrequired="0" manuallysorted="-1" eltdragdrop="0" islink="2" formularorderid="0" orderid="1" status="0" name="rql_server_deve" eltname="rql_server_deve" aliasname="rql_server_deve" variable="rql_server_deve" type="13" elttype="13" templateelementflags="4194320" templateelementislink="2" target="" value="rql_server_work" reddotdescription=""/>
		 </LINK>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <LINK action='assign' guid='" + sourceLinkGuid + "'>" + "  <LINK guid='" + targetLinkGuid + "' />"
				+ "</LINK></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}
	
	
	/**
	 * Erstellt eine RD Referenz von der gegebenen Link GUID sourceLinkGuid zu der gebebenen Page GUID targetPageGuid.
	 */
	public void referenceLinkToPage(String sourceLinkGuid, String targetPageGuid) throws RQLException {
		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <LINK action='reference' guid='" + sourceLinkGuid + "'>" + "  <PAGE guid='" + targetPageGuid + "' />"
				+ "</LINK>" + "</IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	
	/**
	 * Sendet eine Mail an alle aktuall an diesem Projekt angemeldeten User.
	 */
	public void sendMailToActiveUsers(String from, String subject, String message) throws RQLException {

		getCmsClient().sendMail(getActiveUsers(), from, subject, message);
	}

	/**
	 * Sendet eine Mail an alle User dieses Projektes. Benötigt den session key!
	 */
	public void sendMailToAllUsers(String from, String subject, String message, boolean isTest) throws RQLException {

		CmsClient client = getCmsClient();
		client.sendMail(isTest ? client.getTestUsers() : getAllUsers(), from, subject, message);
	}

	/**
	 * Fügt das gegebenen StandardFieldText Element ins Clipboard ein.
	 * <p>
	 * Achtung: Alle im Clipboard vorhandenen Einträge werden überschrieben!
	 */
	public void setClipboard(StandardFieldTextElement sftElement) throws RQLException {
		startBuildClipboardHtmlTable();
		addBuildClipboardHtmlTable(sftElement);
		endBuildClipboardHtmlTable();
		String clipboard = getClipboardEscaped();

		/* 
		 V7.5 request
		<IODATA loginguid="B6D7E3C76ADB4ED99D5A04732F471E33" sessionkey="76BF10A6C61C4F95B459701A8DBC97F3">
		<ADMINISTRATION>
		<USER guid="4324D172EF4342669EAF0AD074433393">
		<CLIPBOARDDATA action="save" value="%3CTABLE%20cellSpacing%3D0%20cellPadding%3D0%3E%3CTBODY%3E%3C%21%3E%3CTR%20id%3D4F265F725E584D999BF4DCE70C4B5B56%20vAlign%3Dcenter%20elttype%3D%22element%22%3E%3CTD%3E%3CIMG%20style%3D%22CURSOR%3A%20hand%22%20onclick%3D%22GotoTreeSegment%28%274F265F725E584D999BF4DCE70C4B5B56%27%2C%27element%27%29%22%20height%3D16%20alt%3D%22Display%20Element%20in%20Tree%22%20src%3D%22Icons/jump.gif%22%20width%3D16%3E%3C/TD%3E%3CTD%3E%3CINPUT%20onclick%3DResetAllSelectCheckbox%28%29%20type%3Dcheckbox%20CHECKED%20name%3DchkClipboard_4F265F725E584D999BF4DCE70C4B5B56_element_unknown%3E%3C/TD%3E%3CTD%20width%3D250%3E%3CIMG%20id%3DIMGType34%20ondblclick%3DAddToClipboard%28%29%20style%3D%22VISIBILITY%3A%20visible%22%20src%3D%22TreeIcons/TreeType1.gif%22%20align%3Dtop%3E%3CA%20id%3DCol134%20ondblclick%3DAddToClipboard%28%29%20style%3D%22FONT-WEIGHT%3A%20normal%3B%20COLOR%3A%20%23000000%22%3E%26nbsp%3Brql_server_url_prod%3C/A%3E%3CA%20id%3DCol234%20ondblclick%3DAddToClipboard%28%29%20style%3D%22FONT-WEIGHT%3A%20normal%3B%20COLOR%3A%20%23ff8c00%22%3E%20%3C/A%3E%3C/TD%3E%3C/TR%3E%3C/TBODY%3E%3C/TABLE%3E" projectguid="73671509FA5C43ED8FC4171AD0298AD2" />
		</USER>
		</ADMINISTRATION>
		</IODATA>
		 
		 V7.5 response
		 same
		 */

		// set user's clipboard new
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<ADMINISTRATION>"
				+ " <USER guid='" + getCmsClient().getConnectedUser().getUserGuid() + "'>"
				+ "  <CLIPBOARDDATA action='save' projectguid='" + getProjectGuid() + "' value='" + clipboard + "' />"
				+ "</USER></ADMINISTRATION></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Wechselt die aktuell ausgewählte Sprachvariante des angemeldeten Benutzers und liefert diese zurück.
	 */
	public LanguageVariant setCurrentLanguageVariant(LanguageVariant languageVariant) throws RQLException {

		checkSessionKey();
		/*
		 V7.5 request
		 <IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
		  <PROJECT>
		    <LANGUAGEVARIANT action="setactive" guid="[!guid_languagevariant!]"/>
		  </PROJECT>
		</IODATA>
		 V7.5 response
		 <IODATA>ok
		</IODATA>
		 */

		// force re-read of almost everything (do it before-hand for error case)
		flushLocalCaches();

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PROJECT>"
				+ "   <LANGUAGEVARIANT action='setactive' guid='" + languageVariant.getLanguageVariantGuid() + "' />" + " </PROJECT>"
				+ "</IODATA>";
		RQLNode rs = callCms(rqlRequest);
		if (rs.isTag("IODATA") && "ok".equalsIgnoreCase(rs.getText().trim())) {
			return languageVariant;
		} else {
			throw new RQLException("Failed to switch to language" + languageVariant.toString());
		}
	}

	/**
	 * Wechselt die aktuell ausgewählte Sprachvariante des angemeldeten Benutzers und liefert die neue Sprachvariante zurück.
	 */
	public LanguageVariant setCurrentLanguageVariantByRfcLanguageId(String rfcLanguageId) throws RQLException {
		return setCurrentLanguageVariant(getLanguageVariantByRfcLanguageId(rfcLanguageId));
	}

	/**
	 * Started das Erzeugen des Clipboard HTML Table codes.
	 * <p>
	 * 
	 * @see #startBuildClipboardHtmlTable()
	 * @see #addBuildClipboardHtmlTable(StandardFieldTextElement)
	 * @see #endBuildClipboardHtmlTable()
	 */
	void startBuildClipboardHtmlTable() {
		clipboardTableCounter = 1;
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		clipboardTableHtml = b.getString("clipboardTableStart");
	}

	/**
	 * Started einen Job zum Exportieren des Projektes. Kann nur unter einem Administrator laufen!
	 * <p>
	 * ACHTUNG: RedDot liefert nicht immer Fehlermeldungen, manchmal eine Errormail, aber selten was in LOG\Export!
	 * 
	 * @param targetPath
	 *            der lokale pfad des Exportverzeichnisses. Er muss leer sein, wird, falls nicht da, angelegt! Bei einem Cluster immer
	 *            in UNC Notation, z.b. \\kswfrd01\cms_data\project_exports\
	 * @param includeUsersAndGroups
	 *            bei true werden die User und Gruppen dieses Projektes mit exportiert, sonst nicht
	 * @param sendMailToConnectedUserWhenFinished
	 *            =true, sendet dem gerade angemeldeten Benutzer eine Mail, wenn der Exporjob beendet ist; =false, kein Mailversand;
	 *            Mailadresse muss konfiguriert sein!
	 */
	public void startExportJob(String targetPath, boolean includeUsersAndGroups, boolean sendMailToConnectedUserWhenFinished)
			throws RQLException {
		/*
		 * V7.5 request <IODATA loginguid="3B08684C9CAE40BB8231AC2C4439CD45">
		 * <ADMINISTRATION> <PROJECT action="export"
		 * projectguid="E62CF0C8E4EC4D018C3E392C42A12161"
		 * targetpath="\\kswfrd01\cms_data\project_exports\test_up_and_away"
		 * logoutusers="1" includeadmindata="1" emailnotification="1"
		 * to="4324D172EF4342669EAF0AD074433393" subject="Projectexport up and
		 * away finished" message="export of project finished"/>
		 * </ADMINISTRATION> </IODATA> V7.5 response <IODATA> </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "<ADMINISTRATION>"
				+ "<PROJECT action='export' projectguid='" + getProjectGuid() + "' logoutusers='1' includeadmindata='"
				+ StringHelper.convertTo01(includeUsersAndGroups) + "' targetpath='" + targetPath + "' ";
		if (sendMailToConnectedUserWhenFinished) {
			String message = "Export of project " + getName() + " finished";
			rqlRequest += "emailnotification='1' to='" + getCmsClient().getConnectedUser().getUserGuid() + "' subject='" + message
					+ "' message='" + message + "'";
		}
		rqlRequest += "/></ADMINISTRATION></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Ändert die aktuelle Sprachvariante auf die Hauptsprache und liefert die alte Sprachvariante für den restore zurück.
	 * <p>
	 * Ist die augenblickliche Sprachvariante die Hauptsprache, wird sie nicht geändert und die Hauptsprache zurückgeliefert.
	 * 
	 * @see #isCurrentLanguageVariantMainLanguage()
	 * @see #setCurrentLanguageVariant(LanguageVariant)
	 * @return the old language variant; use it for a restore
	 */
	public LanguageVariant switchCurrentLanguageVariantToMainLanguage() throws RQLException {
		// remember current
		LanguageVariant oldLV = getCurrentLanguageVariant();

		// current is main already
		if (oldLV.isMainLanguage()) {
			return oldLV;
		}

		// change to main language variant
		setCurrentLanguageVariant(getMainLanguageVariant());

		// return old lv
		return oldLV;
	}

	/**
	 * The generic RQL to unlink an authorization package from links.
	 * <p>
	 * Used only internal for convenience.
	 */
	void unlinkAuthorizationPackage(String linkGuid, AuthorizationPackage authorizationPackage) throws RQLException {
		/*
		V7.5 request 
		<IODATA loginguid="773A7D80BE464144A9DB2A1B79B5C1DC" sessionkey="4CDCADB451954C49950F9E5713F01E70">
		<AUTHORIZATION>
		<LINK guid="6C3C6DD37EB2425EA3BC0E7692D533D0">
		<AUTHORIZATIONPACKET action="unlink" guid="95EC17A9C0624D70B5B4D62D3F65B608"/>
		</LINK></AUTHORIZATION></IODATA>
		V7.5 response
		<IODATA>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<AUTHORIZATION>"
				+ "<LINK guid='" + linkGuid + "'>" + "<AUTHORIZATIONPACKET action='unlink' guid='"
				+ authorizationPackage.getAuthorizationPackageGuid() + "'/>" + "	</LINK></AUTHORIZATION></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Entsperrt dieses Projekt.
	 */
	public void unlock() throws RQLException {

		lock(0, null);
	}

	/**
	 * Meldet den eingeloggten Benutzer am Projekt an.
	 */
	void validate() throws RQLException {
		/*
		 V5 request 
		 <IODATA loginguid="3EC1C199FEC44833A41B9B70CFF69C96">
		 <ADMINISTRATION action="validate" guid="3EC1C199FEC44833A41B9B70CFF69C96" useragent="script"> 
		 <PROJECT guid="06BE79A1D9F549388F06F6B649E27152"/> </ADMINISTRATION> </IODATA>
		 V5 response 
		 <IODATA> 
		 <PROJECT guid="06BE79A1D9F549388F06F6B649E27152" name="hip.hlcl.com" reddotstartpageguid="1408C16F8664401BA2D960B78F3FC614" flags="0"		 
		 versioning="0" useexterneditor="0" externeditorurl="" requestexterneditortext="" setnamesonlyinmainlanguage="0" mainlanguagevariantid="ENG"/> 
		 <USER guid="4324D172EF4342669EAF0AD074433393" userid="129" maxlevel="1"
		 isservermanager="-1" dialoglanguageid="ENG"
		 projectguid="06BE79A1D9F549388F06F6B649E27152" lm="0"
		 languagevariantid="ENG" country="United Kingdom" language="English"
		 languagekey="uk" lcid="2057" dialoglcid="2057"
		 languagevariantlcid="2057" rights1="-1" rights2="-1" rights3="-1"
		 rights4="-1" flags1="1040408" flags2="15948"/> 
		 <SERVER guid="DF8C1CD2998D4A65AA86E9C08CE43C0D" name="" key="824487687s5v5w1Mnl72"/> 
		 <LICENSE	 userguid="4324D172EF4342669EAF0AD074433393" projectguid="06BE79A1D9F549388F06F6B649E27152"
		 guid="F0A4E98F68E44477B4B65DA4AE5AEF20" level="1" te="-1" lm="0"/>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "<ADMINISTRATION action='validate' guid='"
				+ getLogonGuid() + "'>" + "   <PROJECT guid='" + getProjectGuid() + "'/>" + " </ADMINISTRATION>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		// store
		sessionKey = rqlResponse.getNode("SERVER").getAttribute("key");
		RQLNode projectNode = rqlResponse.getNode("PROJECT");
		startPageGuid = projectNode.getAttribute("reddotstartpageguid");
		name = projectNode.getAttribute("name");
	}

	/**
	 * Wandelt alle gegebenen user nodes in eine Liste mit User-Objekten um.
	 * 
	 * @param userNodeList
	 *            liste der umzuwandelden user nodes
	 */
	private java.util.List<User> wrapUserNodes(RQLNodeList userNodeList) {

		RQLNode node = null;
		java.util.List<User> users = new ArrayList<User>();

		if (userNodeList != null) {
			for (int i = 0; i < userNodeList.size(); i++) {
				node = userNodeList.get(i);
				users.add(buildUser(node));
			}
		}
		return users;
	}
}
