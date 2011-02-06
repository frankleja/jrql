package com.hlcl.rql.as;

import java.util.*;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;

import com.hlcl.rql.util.as.PageArrayList;

/**
 * Diese Klasse beschreibt einen User des Projektes.
 * 
 * @author LEJAFR
 */
public class User implements CmsClientContainer {

	// LDAP attribute name properties
	private static final String LdapCityAttributeName_KEY = "LdapCityAttributeName";
	private static final String LdapCountryAttributeName_KEY = "LdapCountryAttributeName";
	private static final String LdapDepartmentAttributeName_KEY = "LdapDepartmentAttributeName";
	// constants
	public static final int LICENSE_ADMINISTRATOR = 1;
	public static final int LICENSE_AUTHOR = 4;
	public static final int LICENSE_EDITOR = 3;
	public static final int LICENSE_SITE_BUILDER = 2;
	public static final int LICENSE_VISITOR = 5;

	private CmsClient cmsClient;
	// cache
	private RQLNode detailsNode;
	private String emailAddress;
	private String fullname;
	private java.util.List<ProjectVariant> publishableProjectVariantsCache;

	// LDAP attributes
	private String ldapCity;
	private String ldapCountry;
	private String ldapDepartment;

	private String name;
	// optional
	private String ownLoginGuid;
	private String userGuid;;
	private String userId;

	/**
	 * User constructor comment.
	 */
	public User(CmsClient cmsClient, String userGuid) {
		super();

		this.cmsClient = cmsClient;
		this.userGuid = userGuid;
		this.ownLoginGuid = null;
	}

	/**
	 * User constructor comment.
	 */
	public User(CmsClient cmsClient, String userGuid, String ownLogonGuid) {
		super();

		this.cmsClient = cmsClient;
		this.userGuid = userGuid;
		this.ownLoginGuid = ownLogonGuid;
	}

	/**
	 * User constructor comment.
	 */
	public User(CmsClient cmsClient, String name, String userGuid, String userId, String fullname, String emailAddress) {
		super();

		this.cmsClient = cmsClient;
		this.name = name;
		this.userGuid = userGuid;
		this.userId = userId;
		this.fullname = fullname;
		this.emailAddress = emailAddress;
	}

	/**
	 * User constructor comment.
	 */
	public User(CmsClient cmsClient, String name, String userGuid, String userId, String fullname, String emailAddress, String ownLoginGuid) {
		super();

		this.cmsClient = cmsClient;
		this.name = name;
		this.userGuid = userGuid;
		this.userId = userId;
		this.fullname = fullname;
		this.emailAddress = emailAddress;
		this.ownLoginGuid = ownLoginGuid;
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort
	 * zurueck. Erforderlich für die Ermittlung des Werts eines Textelements.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {

		return getCmsClient().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort
	 * zurueck. Erforderlich für die Ermittlung des Werts eines Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {

		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Hebt für alle Seiten dieses Benutzers im Status Entwurf diesen Status
	 * auf.
	 * <p>
	 * Löscht neue Seiten oder macht Änderung rückgängig.
	 * Returns true, if at least one page was submitted.
	 */
	public boolean cancelPagesSavedAsDraft(Project project) throws RQLException {

		PageArrayList draftPages = getPagesSavedAsDraft(project);
		for (int i = 0; i < draftPages.size(); i++) {
			Page draftPg = (Page) draftPages.get(i);
			draftPg.cancelDraftState();
		}
		return draftPages.size() > 0;
	}

	/**
	 * Löscht den detailsNode cache. Erzwingt so ein neulesen.
	 */
	private void clearDetailsNodeCache() {
		detailsNode = null;
	}

	/**
	 * Treat users the equals if they have the same name (act as ID).
	 */
	public boolean equals(Object obj) {
		User second = (User) obj;
		try {
			return this.getName().equals(second.getName());
		} catch (RQLException e) {
			// treat as different
			return false;
		}
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {

		return cmsClient;
	}

	/**
	 * Liefert das aktuell gewählte Projekt des CmsServer Objektes.
	 */
	private Project getCurrentProject() {

		return getCmsClient().getCurrentProject();
	}

	/**
	 * Liefert genau die Projektvarianten zurück, die der angemeldete Benutzer auch publizieren kann.
	 */
	public java.util.List<ProjectVariant> getPublishableProjectVariants() throws RQLException {
		if (publishableProjectVariantsCache == null) {
			publishableProjectVariantsCache = getCurrentProject().getPublishableProjectVariants(); 
		}
		return publishableProjectVariantsCache;
	}

	/**
	 * Liefert die Beschreibung dieses Benutzers zurück.
	 */
	public String getDescription() throws RQLException {

		return getDetailsNode().getAttribute("description");
	}

	/**
	 * Liefert die aktuell eingestellt lcid (locale ID) dieses Benutzers.
	 */
	private String getDetailsLcid() throws RQLException {
		return getDetailsNode().getAttribute("lcid");
	}

	/**
	 * Liefert den RQLNode mit weiteren Information für diesen User zurueck.
	 * <p>
	 * Kann nur als admin ausgeführt werden.
	 */
	private RQLNode getDetailsNode() throws RQLException {

		/*
		 V5 request 
		 <IODATA loginguid="[!guid_login!]"> 
		 <ADMINISTRATION> 
		 <USER	action="load" guid="[!guid_user!]" /> 
		 </ADMINISTRATION> 
		 </IODATA> 
		 V5 response 
		 <IODATA> 
		 <USER action="load" guid="[!guid_user!]" id="3" name="name" fullname="Vorname Nachname" description="Beschreibung" flags1="0" flags2="0" maxlevel="4" email="name@company.de"	 acs="[!guid_accountsystem!]" accountsystemguid="[!guid_accountsystem!]" dialoglanguageid="DEU"	 userlanguage="DEU" isservermanager="-1" logindate="37077,6798611111"		 te="-1" lm="0"/> 
		 </IODATA>
		 */

		// call CMS
		if (detailsNode == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "<ADMINISTRATION>" + "<USER action='load' guid='" + getUserGuid() + "'/>" + "</ADMINISTRATION>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			detailsNode = rqlResponse.getNode("USER");
			// set all local values
			emailAddress = detailsNode.getAttribute("email");
			fullname = detailsNode.getAttribute("fullname");
			name = detailsNode.getAttribute("name");
			userId = detailsNode.getAttribute("id");
		}

		return detailsNode;
	}

	/**
	 * Liefert die aktuell eingestellte OberflächensprachenID (ENG, DEU) dieses Benutzers.
	 */
	private String getDetailsDialogLanguageId() throws RQLException {
		return getDetailsNode().getAttribute("dialoglanguageid");
	}

	/**
	 * Liefert die e-Mail Adresse des Users zurück.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getEmailAddress() throws RQLException {

		if (emailAddress == null) {
			getDetailsNode();
		}

		return emailAddress;
	}

	/**
	 * Liefert den Langnamen dieses Users zurück, z.B Frank Leja.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getFullname() throws RQLException {

		if (fullname == null) {
			getDetailsNode();
		}

		return fullname;
	}

	/**
	 * @return Returns the city of this user from LDAP.
	 */
	public String getLdapCity() throws RQLException {
		if (ldapCity == null) {
			getLdapValues();
		}
		return ldapCity;
	}

	/**
	 * @return Returns the country of this user from LDAP.
	 */
	public String getLdapCountry() throws RQLException {
		if (ldapCountry == null) {
			getLdapValues();
		}
		return ldapCountry;
	}

	/**
	 * @return Returns the department (LDAP attribute departmentnumber) of this user from LDAP (shown in Outlook as Department: as well).
	 */
	public String getLdapDepartment() throws RQLException {
		if (ldapDepartment == null) {
			getLdapValues();
		}
		return ldapDepartment;
	}

	/**
	 * Holt alle Werte aus LDAP und speichert sie lokal.
	 */
	private void getLdapValues() throws RQLException {
		// initialize
		ldapCountry = "";
		ldapDepartment = "";
		ldapCity = "";

		// get the centry ldap context
		DirContext context = getCmsClient().openLdapContext();

		// LDAP-Anfrage definieren:
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration namingEnumeration = context.search("dc=hlcl,dc=com", "uid=" + getName(), constraints); // filter = uid=lejafr

			if (namingEnumeration.hasMore()) {
				// get search result attributes
				SearchResult sr = (SearchResult) namingEnumeration.next();
				Attributes attributes = sr.getAttributes();

				// get attribute name properties
				PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
				String countryAttributeName = bundle.getString(LdapCountryAttributeName_KEY).trim();
				String departmentAttributeName = bundle.getString(LdapDepartmentAttributeName_KEY).trim();
				String cityAttributeName = bundle.getString(LdapCityAttributeName_KEY).trim();

				// take care, not all attributes are stored within ldap!
				Attribute attributeOrNull = attributes.get(countryAttributeName);
				if (attributeOrNull != null) {
					ldapCountry = (String) attributeOrNull.get();
				}
				attributeOrNull = attributes.get(departmentAttributeName);
				if (attributeOrNull != null) {
					ldapDepartment = (String) attributeOrNull.get();
				}
				attributeOrNull = attributes.get(cityAttributeName);
				if (attributeOrNull != null) {
					ldapCity = (String) attributeOrNull.get();
				}
				namingEnumeration.close();
			}
		} catch (NamingException ex) {
			throw new RQLException("LDAP search for user attributes of user " + getName() + " failed.", ex);
		} finally {
			// close the ldap context
			getCmsClient().closeLdapContext();
		}
	}

	/**
	 * Liefert die höchste Lizenz dieses Users im gegebenen Projekt zurück.
	 * 
	 * @param project
	 * @return eine der LICENSE_* constants
	 */
	private int getLicense(Project project) throws RQLException {
		/*
		 * V5 request <IODATA loginguid="D3DEF03C98E344F089832C426FEE4BBB">
		 * <ADMINISTRATION> <USER guid="4324D172EF4342669EAF0AD074433393">
		 * <PROJECT guid="5256C671655D4CE696F663C73CE3E526" action="load"/>
		 * </USER> </ADMINISTRATION> </IODATA> V5 response <IODATA> <PROJECT
		 * guid="5256C671655D4CE696F663C73CE3E526" name="hlcl_relaunch_2004"
		 * description="" versioning="0" inhibitlevel="3" lockinfo="Project
		 * blocked." inhibit="0" userlevel="1" templateeditorright="-1"
		 * languagemanagerright="0" projectversion="002.043"
		 * reddotstartpageguid="69060E5AA60B4AD6988FC00A68079E34"
		 * lockedbysystem="0"/> </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "  <ADMINISTRATION>" + "   <USER guid='" + getUserGuid() + "'>" + "    <PROJECT action='load' guid='"
				+ project.getProjectGuid() + "'/>" + "   </USER>" + "  </ADMINISTRATION>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		return Integer.parseInt(rqlResponse.getNode("PROJECT").getAttribute("userlevel"));
	}

	/**
	 * Liefert die Locale dieses Benutzers zurück.
	 */
	public Locale getLocale() throws RQLException {

		return getCmsClient().getLocaleByLcid(getDetailsLcid());
	}

	/**
	 * Liefert die RedDot logon GUID des users unter dem das script läuft. Dies
	 * ist nicht die des Users, falls er angemeldet ist!
	 * 
	 * @see <code>getOwnLoginGuid</code>
	 */
	public String getLogonGuid() {

		return getCmsClient().getLogonGuid();
	}

	/**
	 * Liefert alle Seiten dieses Benutzers die noch auf Freigabe warten.
	 * 
	 * @return List of Pages
	 */
	public PageArrayList getMyPagesInWorkflow(Project project) throws RQLException {

		return project.getPagesByState(Project.WF_LIST_STATE_MY_PAGES_IN_WORKFLOW, this);
	}

	/**
	 * Liefert den Benutzernamen zurück, z.B. lejefr.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getName() throws RQLException {

		if (name == null) {
			getDetailsNode();
		}

		return name;
	}

	/**
	 * Liefert die login guid dieses Benutzers, falls er angemeldet ist.
	 * 
	 * @see <code>getLoginGuid</code>
	 */
	public String getOwnLogonGuid() {
		return ownLoginGuid;
	}

	/**
	 * Liefert alle Seiten dieses Benutzers im Status Entwurf.
	 * 
	 * @return List of Pages
	 */
	public PageArrayList getPagesSavedAsDraft(Project project) throws RQLException {

		return project.getPagesByState(Project.WF_LIST_STATE_PAGES_SAVED_AS_DRAFT, this);
	}

	/**
	 * Bestätigt alle Seiten dieses Benutzers im Status Entwurf.
	 * 
	 * @return Number of submitted pages
	 */
	public int submitPagesSavedAsDraft(Project project) throws RQLException {
		PageArrayList draftPages = project.getPagesByState(Project.WF_LIST_STATE_PAGES_SAVED_AS_DRAFT, this);
		draftPages.submitAllToWorkflow();
		return draftPages.size();
	}

	/**
	 * Liefert alle Seiten dieses Benutzers, die er zu korrigieren hat.
	 * 
	 * @return List of Pages
	 */
	public PageArrayList getPagesWaitingForCorrection(Project project) throws RQLException {

		return project.getPagesByState(Project.WF_LIST_STATE_PAGES_WAITING_FOR_CORRECTION, this);
	}

	/**
	 * Liefert alle Seiten, die von diesem Benutzer zu prüfen sind.
	 * 
	 * @return List of Pages
	 */
	public PageArrayList getPagesWaitingForRelease(Project project) throws RQLException {

		return project.getPagesByState(Project.WF_LIST_STATE_PAGES_WAITING_FOR_RELEASE, this);
	}

	/**
	 * Liefert alle Seiten, die für diesen Benutzer (ein Translator) auf Übersetzung warten; von der Hauptsprachvariante in die aktuelle Sprachvariante.
	 */
	public PageArrayList getPagesWaitingForTranslation(Project project) throws RQLException {
		PageSearch search = project.definePageSearch();
		search.addStateCriteriaWaitingForTranslation(project.getMainLanguageVariant());
		return search.getPages();
	}

	/**
	 * Liefert eine Liste aller für diesen User zugelassenen Projekte.
	 */
	public java.util.List getProjects() throws RQLException {

		CmsClient client = getCmsClient();

		return client.wrapProjectNodes(client.getProjectsNodeList(this));
	}

	/**
	 * Liefert alle Benutzergruppen passend zum gegebenen Prefix, denen dieser Benutzer im aktuellen Projekt zugeordnet ist.
	 */
	public List<UserGroup> getUserGroupsInCurrentProject(String groupPrefix) throws RQLException {

		/*
		 V6.5 request
		 <IODATA loginguid="[!guid_login!]">
		 <ADMINISTRATION>
		 <USER guid="[!guid_user!]">
		 <PROJECTS action="listgroups"/>
		 </USER>
		 </ADMINISTRATION>
		 </IODATA>  
		 V6.5 response
		 <IODATA>
		 <PROJECTS>
		 <PROJECT guid="06BE79A1D9F549388F06F6B649E27152" name="hip.hlcl.com">
		 <GROUPS>
		 <GROUP guid="81046ED3C4F245F5BCC2F92BAF10197A" name="admin+internally-area-admins" checked="0"/>
		 <GROUP guid="C7DC42F676E6481BA0645DCA6A7B255B" name="archiv publisher" checked="0"/>
		 <GROUP guid="A99543873A424FCA8962C959CB45FE4D" name="work-area-transition-hq-authors" checked="0"/>
		 <GROUP guid="07F341D915DE429AB1742FCB249FCA5F" name="work-area-transition-hq-checkers" checked="0"/>
		 </GROUPS>
		 </PROJECT>
		 <PROJECT guid="5256C671655D4CE696F663C73CE3E526" name="www.hapag-lloyd.com">
		 <GROUPS>
		 <GROUP guid="1EEEC4831CF24917BC08896F247FC862" name="all authors HLAG" checked="0"/>
		 <GROUP guid="FB52D532789340B48A11709717A104B7" name="Recht_hlcl" checked="0"/>
		 <GROUP guid="5F49D5F58CDC4224B08E25C8DADEC2ED" name="useres with exaptions" checked="0"/>
		 </GROUPS>
		 </PROJECT>
		 </PROJECTS>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "<ADMINISTRATION>" + "<USER guid='" + getUserGuid() + "' >" + "<PROJECTS action='listgroups' />" + "</USER>"
				+ "</ADMINISTRATION>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		// get first node for current project
		RQLNodeList projectNodes = rqlResponse.getNodes("PROJECT");
		RQLNode projectNode = null;
		for (int i = 0; i < projectNodes.size(); i++) {
			projectNode = projectNodes.get(i);
			if (projectNode.getAttribute("guid").equals(getCurrentProject().getProjectGuid())) {
				break;
			}
		}
		// no groups in current project return empty
		List<UserGroup> groups = new ArrayList<UserGroup>();
		if (projectNode == null) {
			return groups;
		}

		// return empty if no groups
		RQLNodeList groupNodes = projectNode.getNodes("GROUP");
		if (groupNodes == null) {
			return groups;
		}

		// select all checked and wrap into user groups
		for (int i = 0; i < groupNodes.size(); i++) {
			RQLNode node = groupNodes.get(i);
			String name = node.getAttribute("name");
			if (node.getAttribute("checked").equals("1") && name.startsWith(groupPrefix)) {
				UserGroup group = new UserGroup(getCmsClient(), node.getAttribute("guid"), name);
				groups.add(group);
			}
		}
		return groups;
	}

	/**
	 * Liefert
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getUserGuid() {
		return userGuid;
	}

	/**
	 * Liefert die interne ID dieses Benutzers zurück.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getUserId() throws RQLException {

		if (userId == null) {
			getDetailsNode();
		}

		return userId;
	}

	/**
	 * Liefert die Oberflächensprache dieses Benutzers zurück.
	 */
	public UserInterfaceLanguage getUserInterfaceLanguage() throws RQLException {
	
		return getCmsClient().getUserInterfaceLanguageByLanguageId(getDetailsDialogLanguageId());
	}

	/**
	 * Liefert den HashCode dieses Users.
	 * 
	 * @return java.lang.String
	 */
	public int hashCode() {
		return getUserGuid().hashCode();
	}

	/**
	 * Liefert true genau dann, wenn dieser User Editor, Site Builder oder
	 * Administrator im gegebenen Projekt ist.
	 */
	public boolean hasMoreRightsAsAnAuthor(Project project) throws RQLException {

		return getLicense(project) < LICENSE_AUTHOR;
	}

	/**
	 * Liefert true genau dann, wenn dieser User Editor, Site Builder oder
	 * Administrator im aktuellen Projekt ist.
	 */
	public boolean hasMoreRightsAsAnAuthorInCurrentProject() throws RQLException {

		return hasMoreRightsAsAnAuthor(getCurrentProject());
	}

	/**
	 * Liefert true, wenn dieser Benutzer gerade am CMS angemeldet ist.
	 */
	public boolean isActive() {

		return ownLoginGuid != null;
	}

	/**
	 * Liefert true genau dann, wenn dieser User Administrator im gegebenen
	 * Projekt ist.
	 */
	public boolean isAdministrator(Project project) throws RQLException {

		return getLicense(project) == LICENSE_ADMINISTRATOR;
	}

	/**
	 * Liefert true genau dann, wenn dieser User Administrator im aktuellen
	 * Projekt ist.
	 */
	public boolean isAdministratorInCurrentProject() throws RQLException {

		return isAdministrator(getCurrentProject());
	}

	/**
	 * Liefert true genau dann, wenn dieser User Author im gegebenen Projekt
	 * ist.
	 */
	public boolean isAuthor(Project project) throws RQLException {

		return getLicense(project) == LICENSE_AUTHOR;
	}

	/**
	 * Liefert true genau dann, wenn dieser User Author im aktuellen Projekt
	 * ist.
	 */
	public boolean isAuthorInCurrentProject() throws RQLException {

		return isAuthor(getCurrentProject());
	}

	/**
	 * Liefert true, falls dieser User den DirectEdit mode in seinen Benutzereinstellungen auf CTL und Maus gesetzt hat.
	 */
	public boolean isDirectEditCtlAndMouse() throws RQLException {

		return getDetailsNode().getAttribute("invertdirectedit").equals("0");
	}

	/**
	 * Liefert true, falls dieser User den DirectEdit mode in seinen Benutzereinstellungen auf Maus gesetzt hat.
	 */
	public boolean isDirectEditMouseOnly() throws RQLException {

		return getDetailsNode().getAttribute("invertdirectedit").equals("1");
	}

	/**
	 * Liefert true genau dann, wenn dieser User Site Builder im gegebenen
	 * Projekt ist.
	 */
	public boolean isSiteBuilder(Project project) throws RQLException {

		return getLicense(project) == LICENSE_SITE_BUILDER;
	}

	/**
	 * Liefert true genau dann, wenn dieser User Site Builder im aktuellen
	 * Projekt ist.
	 */
	public boolean isSiteBuilderInCurrentProject() throws RQLException {

		return isSiteBuilder(getCurrentProject());
	}

	/**
	 * Liefert true genau dann, wenn dieser User Visitor im gegebenen Projekt
	 * ist.
	 */
	public boolean isVisitor(Project project) throws RQLException {

		return getLicense(project) == LICENSE_VISITOR;
	}

	/**
	 * Liefert true genau dann, wenn dieser User Visitor im aktuellen Projekt ist.
	 */
	public boolean isVisitorInCurrentProject() throws RQLException {

		return isVisitor(getCurrentProject());
	}

	/**
	 * Meldet diesen Benutzer vom RD CMS ab, falls er angemeldet ist.
	 */
	public void logout() throws RQLException {

		getCmsClient().logout(getOwnLogonGuid());
	}

	/**
	 * Ändert, wie der Direct Edit mode aktiviert wird. Bei false wird er auf nur Maus gestellt.
	 */
	public void setDirectEditMode(boolean withCtlAndMouse) throws RQLException {
		update("invertdirectedit='" + (withCtlAndMouse ? "0" : "1") + "' ");
	}

	/**
	 * Ändert die e-mail Adresse dieses Users.
	 */
	public void setEmailAddress(String emailAddress) throws RQLException {

		update("email='" + emailAddress + "' ");
		this.emailAddress = emailAddress;
	}

	/**
	 * Ändert die Locale dieses Benutzers.
	 */
	public void setLocale(Locale locale) throws RQLException {
		update("lcid='" + locale.getLcid() + "'");
	}

	/**
	 * Ändert die Oberflächensprache dieses Benutzers.
	 */
	public void setUserInterfaceLanguage(UserInterfaceLanguage userInterfaceLanguage) throws RQLException {
		update("dialoglanguageid='" + userInterfaceLanguage.getLanguageId() + "'");
	}

	/**
	 * Überschreiben zum Debuggen.
	 */
	public String toString() {
		String result = super.toString();
		try {
			result = "User (" + getName() + ")";
		} catch (RQLException e) {
			// ignore
		}
		return result;
	}

	/**
	 * Ändert Userattribute. Kann nur als admin ausgeführt werden.
	 * 
	 * ATTENTION: Do not forget to update the instance variables accordingly!
	 * 
	 * @param attributeAndValue
	 *            like email="name@company.de"
	 */
	private void update(String attributeAndValue) throws RQLException {

		/*
		 V7.5 request 
		 <IODATA loginguid="[!guid_login!]"> 
		 <ADMINISTRATION>
		 <USER action="save" name="name" fullname="Vorname Nachname"		 description="Beschreibung" email=name@company.de		 accountsystemguid="[!guid_accountsystem!]" pw="pw" userlanguage="DEU"		 lcid="1031" guid="[!guid_user!]" navigationtype="0" ssoactive="1"/>
		 </ADMINISTRATION> 
		 </IODATA> 
		 V7.5 response 
		 <IODATA>
		 <USER action="load" guid="[!guid_user!]" id="3" name="name"  fullname="Vorname Nachname" description="Beschreibung"	   flags1="0" flags2="0" maxlevel="4" 
		 email="name@company.de" acs="[!guid_accountsystem!]"   accountsystemguid="[!guid_accountsystem!]" 
		 dialoglanguageid="DEU" userlanguage="DEU"	   isservermanager="-1" logindate="37077,6798611111"
		 te="-1" lm="0" navigationtype="0" lcid="1031" navigationtype="0"	   maxlogin="4" preferrededitor="0" invertdirectedit="0"	   disablepassword="0" userlimits="139"/>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "<ADMINISTRATION>" + "<USER action='save' guid='" + getUserGuid() + "' " + attributeAndValue + "/>" + "</ADMINISTRATION>"
				+ "</IODATA>";
		callCms(rqlRequest);

		// force new read
		clearDetailsNodeCache();
	}
}
