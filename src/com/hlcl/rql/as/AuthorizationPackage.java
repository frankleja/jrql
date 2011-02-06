package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Diese Klasse beschreibt Berechtigungspakete unterschiedlichen Typs.
 * 
 * @author LEJAFR
 */
public class AuthorizationPackage implements ProjectContainer {
	final static String CONTENT_CLASS_TYPE = "16";
	final static String DETAILED_ASSET_MANAGER_ATTRIBUT_TYPE = "8";
	final static String DETAILED_ELEMENT_TYPE = "4";
	final static String DETAILED_LINK_TYPE = "2";
	final static String DETAILED_PAGE_TYPE = "1";
	final static String NORMAL_TYPE = "0";
	final static String PROJECT_VARIANT_TYPE = "32";
	final static String FOLDER_TYPE = "64";
	final static String LANGUAGE_VARIANT_TYPE = "128";

	private String authorizationPackageGuid;
	private String name;
	private Project project;
	private String type;

	// cache
	private List<AuthorizationUserGroup> authorizationUserGroups;

	/**
	 * Liefert eine Map mit den Berechtigungspakettypen und dem Namen, alphabetisch sortiert.
	 */
	public static Map<String, String> getTypeMap() {
		// fill the map in same order then types array
		Map<String, String> t2n = new HashMap<String, String>();
		t2n.put(DETAILED_ASSET_MANAGER_ATTRIBUT_TYPE, "AssetManager attribute detailed authorization package");
		t2n.put(CONTENT_CLASS_TYPE, "Content class authorization package");
		t2n.put(DETAILED_ELEMENT_TYPE, "Element detailed authorization package");
		t2n.put(FOLDER_TYPE, "Folder authorization package");
		t2n.put(LANGUAGE_VARIANT_TYPE, "Language variant authorization package");
		t2n.put(DETAILED_LINK_TYPE, "Link detailed authorization package");
		t2n.put(NORMAL_TYPE, "Page authorization package");
		t2n.put(DETAILED_PAGE_TYPE, "Page detailed authorization package");
		t2n.put(PROJECT_VARIANT_TYPE, "Project variant authorization package");
		return t2n;
	}

	/**
	 * Liefert für die gegebene Berechtigungspakettypnummer einen Anzeigenamen.
	 */
	public static String getTypeName(String type) {
		return AuthorizationPackage.getTypeMap().get(type);
	}

	/**
	 * constructor comment.
	 */
	public AuthorizationPackage(Project project, String authorizationPackageGuid, String name, String type) {
		super();

		this.project = project;
		this.authorizationPackageGuid = authorizationPackageGuid;
		this.name = name;
		this.type = type;
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
	 * Liefert die GUID dieses Berechtigungspakets.
	 * 
	 * @return java.lang.String
	 */
	public String getAuthorizationPackageGuid() {
		return authorizationPackageGuid;
	}

	/**
	 * Liefert die Benutzergruppe dieses Berechtigungspaktes für die gegebene GUID.
	 * 
	 * @throws ElementNotFoundException
	 */
	public AuthorizationUserGroup getAuthorizationUserGroupByGuid(String userGroupGuid) throws RQLException {

		for (Iterator iterator = getAuthorizationUserGroups().iterator(); iterator.hasNext();) {
			AuthorizationUserGroup authorizationUserGroup = (AuthorizationUserGroup) iterator.next();
			if (authorizationUserGroup.getUserGroupGuid().equals(userGroupGuid)) {
				return authorizationUserGroup;
			}
		}
		// group not found
		throw new ElementNotFoundException("User group with GUID " + userGroupGuid + " cannot be found in the authorization package " + getName()
				+ ".");
	}

	/**
	 * Liefert die Berechtigungsbenutzergruppe dieses Berechtigungspaketes.
	 * 
	 * @throws ElementNotFoundException
	 */
	public AuthorizationUserGroup getAuthorizationUserGroup(UserGroup userGroup) throws RQLException {
		AuthorizationUserGroup result = findAuthorizationUserGroup(userGroup);
		if (result == null) {
			// group not found
			throw new ElementNotFoundException("User group " + userGroup.getName() + " cannot be found in the authorization package " + getName() + ".");
		}
		return result;
	}

	/**
	 * Liefert true, falls die gegebene Benutzergruppe in diesem Berechtigungspakte verwendet wird.
	 */
	public boolean contains(UserGroup userGroup) throws RQLException {
		return findAuthorizationUserGroup(userGroup) != null;
	}
	/**
	 * Liefert die Berechtigungsbenutzergruppe dieses Berechtigungspaktes, oder null, falls nicht gefunden.
	 */
	private AuthorizationUserGroup findAuthorizationUserGroup(UserGroup userGroup) throws RQLException {

		for (Iterator iterator = getAuthorizationUserGroups().iterator(); iterator.hasNext();) {
			AuthorizationUserGroup authorizationUserGroup = (AuthorizationUserGroup) iterator.next();
			if (authorizationUserGroup.getUserGroupGuid().equals(userGroup.getUserGroupGuid())) {
				return authorizationUserGroup;
			}
		}
		// not found
		return null;
	}

	/**
	 * Liefert die Benutzergruppe dieses Berechtigungspaktes für den gegebenen Namen.
	 * 
	 * @throws ElementNotFoundException
	 */
	public AuthorizationUserGroup getAuthorizationUserGroupByName(String userGroupName) throws RQLException {

		for (Iterator iterator = getAuthorizationUserGroups().iterator(); iterator.hasNext();) {
			AuthorizationUserGroup authorizationUserGroup = (AuthorizationUserGroup) iterator.next();
			if (authorizationUserGroup.getUserGroupName().equals(userGroupName)) {
				return authorizationUserGroup;
			}
		}
		// group not found
		throw new ElementNotFoundException("User group " + userGroupName + " cannot be found in the authorization package " + getName() + ".");
	}

	/**
	 * Liefert den Namen dieses Berechtigungspaketes ohne den gegebenen Prefix. Liefert den Namen unverändert, falls prefix nicht vorhanden.
	 */
	public String getNameWithoutPrefix(String prefix) throws RQLException {
		return StringHelper.removePrefix(getName(), prefix);
	}

	/**
	 * Liefert die Benutzergruppe passend zum Namen dieses Berechtigungspaktes. If the suffixes is not matching null is returned.
	 * <p>
	 * Is this name work_area_rights_business_administration the user group authorization for user group work-area-business-administration will be
	 * returned.
	 * <p>
	 * Automatically replaces _ from package name suffix to - in user group suffix
	 * 
	 * @param packageNameprefix =
	 *            to be removed to calculate user group suffix, e.g. work-area-rights-
	 */
	public AuthorizationUserGroup getAuthorizationUserGroupMatchingThisName(String packageNameprefix) throws RQLException {
		String suffix = getNameWithoutPrefix(packageNameprefix).replace('_', '-');
		try {
			return getAuthorizationUserGroupByNameSuffix(suffix);
		} catch (ElementNotFoundException ex) {
			return null;
		}
	}

	/**
	 * Liefert alle Berechtigungs-Benutzergruppen dieses Berechtigungspaktes, die mit dem gegebenen Prefix beginnen.
	 * <p>
	 * Liefert eine leere Liste, falls gar keine gefunden wurden. Der Vergleich ist case sensitiv!
	 */
	public List<AuthorizationUserGroup> getAuthorizationUserGroupByNamePrefix(String userGroupNamePrefix) throws RQLException {

		List<AuthorizationUserGroup> result = new ArrayList<AuthorizationUserGroup>();

		for (Iterator iterator = getAuthorizationUserGroups().iterator(); iterator.hasNext();) {
			AuthorizationUserGroup authorizationUserGroup = (AuthorizationUserGroup) iterator.next();

			if (authorizationUserGroup.getUserGroupName().startsWith(userGroupNamePrefix)) {
				result.add(authorizationUserGroup);
			}
		}
		return result;
	}

	/**
	 * Liefert die Berechtigungs-Benutzergruppe dieses Berechtigungspaktes, das mit dem gegebenen Suffix endet.
	 * <p>
	 * 
	 * @throws ElementNotFoundException
	 *             falls keine Benutzergruppe mit dem gegebenen suffix gefunden werden kann.
	 */
	public AuthorizationUserGroup getAuthorizationUserGroupByNameSuffix(String userGroupNameSuffix) throws RQLException {

		List<AuthorizationUserGroup> result = new ArrayList<AuthorizationUserGroup>();

		for (AuthorizationUserGroup authorizationUserGroup : getAuthorizationUserGroups()) {
			if (authorizationUserGroup.getUserGroupName().endsWith(userGroupNameSuffix)) {
				return authorizationUserGroup;
			}
		}
		throw new ElementNotFoundException("No authorization user group for suffix " + userGroupNameSuffix + " in package " + getName() + " found.");
	}

	/**
	 * Liefert die Anzahl aller Benutzergruppenberechtigungen dieses Paketes.
	 */
	public int getAuthorizationUserGroupsSize() throws RQLException {
		return getAuthorizationUserGroups().size();
	}

	/**
	 * Liefert die liste aller Benutzergruppenberechtigungen dieses Paketes.
	 */
	public List<AuthorizationUserGroup> getAuthorizationUserGroups() throws RQLException {
		if (authorizationUserGroups == null) {
			authorizationUserGroups = new ArrayList<AuthorizationUserGroup>();
			// wrap
			RQLNodeList authorizationUserGroupNodeList = getDetailsNode().getNodes("GROUP");
			// check for no groups at all
			if (authorizationUserGroupNodeList == null) {
				return authorizationUserGroups;
			}
			for (int i = 0; i < authorizationUserGroupNodeList.size(); i++) {
				RQLNode node = (RQLNode) authorizationUserGroupNodeList.get(i);
				authorizationUserGroups.add(buildAuthorizationUserGroup(node));
			}
		}
		return authorizationUserGroups;
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	/**
	 * Erzwingt ein neulesen nach änderungen an diesem Paket.
	 */
	void clearCaches() {
		authorizationUserGroups = null;
	}

	/**
	 * Erzeugt ein AuthorizationUserGroup objekt aus dem gegebenen node.
	 */
	private AuthorizationUserGroup buildAuthorizationUserGroup(RQLNode node) {
		return new AuthorizationUserGroup(this, node.getAttribute("guid"), node.getAttribute("name"), node.getAttribute("right1"), node
				.getAttribute("right2"), node.getAttribute("right3"), node.getAttribute("right4"), node.getAttribute("right5"), node
				.getAttribute("right6"), node.getAttribute("right7"), node.getAttribute("right8"), node.getAttribute("deny1"), node
				.getAttribute("deny2"), node.getAttribute("deny3"), node.getAttribute("deny4"), node.getAttribute("deny5"), node
				.getAttribute("deny6"), node.getAttribute("deny7"), node.getAttribute("deny8"));
	}

	/**
	 * Liefert den root node mit allen Details (action=load) dieses paketes.
	 */
	private RQLNode getDetailsNode() throws RQLException {

		/* 
		 V7.5 request
		 <IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
		 <AUTHORIZATION>
		 <AUTHORIZATIONPACKET action="load" guid="[!guid_authorization!]"/>
		 </AUTHORIZATION>
		 </IODATA>
		 V7.5 response
		<IODATA>
		<AUTHORIZATIONPACKET action="load" languagevariantid="ENG" elementguid="" guid="7C3EFE2D84BB48DCBEF757BBC36EE6A0" name="work_area_rights_business_administration_hq" projectvariantguid="" languagevariantguid="" donotinherit="0" type="0">
		<USERS>
		<USER guid="C2F334B097B44B728EE1373D471833BF" name="sefrali" right1="570179295" right2="16777151" right3="31" right4="0" right5="0" right6="0" right7="0" right8="0" deny1="0" deny2="64" deny3="0" deny4="0" deny5="0" deny6="0" deny7="0" deny8="0"/>
		<USER guid="BCD4CF70A04049A0AA42709F0E84745B" name="rymkilu" right1="570179295" right2="16777151" right3="31" right4="0" right5="0" right6="0" right7="0" right8="0" deny1="0" deny2="64" deny3="0" deny4="0" deny5="0" deny6="0" deny7="0" deny8="0"/>
		<USER guid="B248DCF058AE480888AA9FE795BC01A8" name="Unknown" right1="570179295" right2="16777151" right3="31" right4="0" right5="0" right6="0" right7="0" right8="0" deny1="0" deny2="64" deny3="0" deny4="0" deny5="0" deny6="0" deny7="0" deny8="0"/>
		<USER guid="3AA3510C3E084768B9E46348505441E3" name="reissdi" right1="570179295" right2="16777151" right3="31" right4="0" right5="0" right6="0" right7="0" right8="0" deny1="0" deny2="64" deny3="0" deny4="0" deny5="0" deny6="0" deny7="0" deny8="0"/>
		<USER guid="7DC03DED86234BC8A1F35752262B61F5" name="medinel" right1="570179295" right2="16777151" right3="31" right4="0" right5="0" right6="0" right7="0" right8="0" deny1="0" deny2="64" deny3="0" deny4="0" deny5="0" deny6="0" deny7="0" deny8="0"/>
		<USER guid="A591A89A4A564E65A0CB214B5E6EB4DE" name="chiamhy" right1="570179295" right2="16777151" right3="31" right4="0" right5="0" right6="0" right7="0" right8="0" deny1="0" deny2="64" deny3="0" deny4="0" deny5="0" deny6="0" deny7="0" deny8="0"/>
		<USER guid="198C466E5362482EBBD0AEE77BF141C3" name="lejafr4" right1="570179295" right2="16777151" right3="31" right4="0" right5="0" right6="0" right7="0" right8="0" deny1="0" deny2="64" deny3="0" deny4="0" deny5="0" deny6="0" deny7="0" deny8="0"/>
		</USERS>
		<GROUPS>
		<GROUP guid="0ECF12BB815344B0835E457440703327" name="work-area-business-administration-hq-authors" right1="570178783" right2="12582847" right3="31" right4="0" right5="0" right6="0" right7="0" right8="0" deny1="512" deny2="4194368" deny3="0" deny4="0" deny5="0" deny6="0" deny7="0" deny8="0"/>
		<GROUP guid="C7AC2D3B968B483DAC0F3EEA9F315A9E" name="work-area-business-administration-hq-checkers" right1="570178783" right2="12582847" right3="31" right4="0" right5="0" right6="0" right7="0" right8="0" deny1="512" deny2="4194368" deny3="0" deny4="0" deny5="0" deny6="0" deny7="0" deny8="0"/>
		<GROUP guid="75D0C136086046579D444B81D3079DC3" name="work-area-admins" right1="570179551" right2="16777151" right3="31" right4="0" right5="0" right6="0" right7="0" right8="0" deny1="0" deny2="64" deny3="0" deny4="0" deny5="0" deny6="0" deny7="0" deny8="0"/>
		<GROUP guid="359DB51116354F57BD4B8D0CF8648786" name="Unknown" right1="570179583" right2="16777151" right3="31" right4="0" right5="0" right6="0" right7="0" right8="0" deny1="0" deny2="64" deny3="0" deny4="0" deny5="0" deny6="0" deny7="0" deny8="0"/>
		<GROUP guid="608D7F3D07E94C3E99A187E9DACE46CA" name="Everyone" right1="0" right2="0" right3="1" right4="0" right5="0" right6="0" right7="0" right8="0" deny1="570179551" deny2="16777215" deny3="30" deny4="0" deny5="0" deny6="0" deny7="0" deny8="0"/>
		</GROUPS>
		</AUTHORIZATIONPACKET>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<AUTHORIZATION>"
				+ "<AUTHORIZATIONPACKET action='load' guid='" + getAuthorizationPackageGuid() + "'/>" + "</AUTHORIZATION></IODATA>";
		return callCms(rqlRequest).getNode("IODATA");
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
	 * Liefert den Namen dieses Berechtigungspaketes.
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
	 * Startet einen asynchronen Job, der dieses Berechtigungspaket an der gegebenen Seite an alle Unterseiten vererbt. Es wird keine Mail an den
	 * Autor versendet!
	 */
	public void inherit(Page page) throws RQLException {

		/* 
		 V7.5 request
		 <IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
		 <AUTHORIZATION>
		 <PAGE guid="[!guid_page!]">
		 <AUTHORIZATIONPACKET action="inherit" guid="[!guid_authorization!]" sendmail="1" emailreceiver="[!guid_user!]" emailsubject="eMail Text"/>
		 </PAGE>
		 </AUTHORIZATION>
		 </IODATA> 
		 V7.5 response 
		 <IODATA>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<AUTHORIZATION>" + "<PAGE guid='"
				+ page.getPageGuid() + "'>" + "<AUTHORIZATIONPACKET action='inherit' guid='" + getAuthorizationPackageGuid() + "'/>"
				+ " </PAGE></AUTHORIZATION></IODATA>";
		callCmsWithoutParsing(rqlRequest); // no result
	}

	/**
	 * Liefert einen Anzeigenamen für den Typ dieses Berechtigungspaketes.
	 */
	public String getTypeName() {
		return AuthorizationPackage.getTypeName(type);
	}

	/**
	 * Liefert true genau dann, wenn dieses Berechtigungspaket ein Detailberechtigungspaket für Asset Manager Attribute ist.
	 */
	public boolean isDetailedAssetManagerAttributeAuthorizationPackage() {
		return type.equals(DETAILED_ASSET_MANAGER_ATTRIBUT_TYPE);
	}

	/**
	 * Liefert true genau dann, wenn der Name dieses Berechtigungspaket mit dem gegebenen Suffix endet.
	 */
	public boolean isNameEndsWith(String suffix) {
		return getName().endsWith(suffix);
	}

	/**
	 * Liefert true genau dann, wenn der Name dieses Berechtigungspaket mit dem gegebenen Prefix beginnt.
	 */
	public boolean isNameStartsWith(String prefix) {
		return getName().startsWith(prefix);
	}

	/**
	 * Liefert true genau dann, wenn dieses Berechtigungspaket ein Detailberechtigungspaket ist.
	 */
	public boolean isDetailedAuthorizationPackage() {
		return type.equals(DETAILED_ELEMENT_TYPE) || type.equals(DETAILED_LINK_TYPE) || type.equals(DETAILED_PAGE_TYPE)
				|| type.equals(DETAILED_ASSET_MANAGER_ATTRIBUT_TYPE);
	}

	/**
	 * Liefert true genau dann, wenn dieses Berechtigungspaket ein Detailberechtigungspaket für Elemente ist.
	 */
	public boolean isDetailedElementAuthorizationPackage() {
		return type.equals(DETAILED_ELEMENT_TYPE);
	}

	/**
	 * Liefert true genau dann, wenn dieses Berechtigungspaket ein Detailberechtigungspaket für Links ist.
	 */
	public boolean isDetailedLinkAuthorizationPackage() {
		return type.equals(DETAILED_LINK_TYPE);
	}

	/**
	 * Liefert true genau dann, wenn dieses Berechtigungspaket ein Detailberechtigungspaket für Seiten ist.
	 */
	public boolean isDetailedPageAuthorizationPackage() {
		return type.equals(DETAILED_PAGE_TYPE);
	}

	/**
	 * Liefert true genau dann, wenn dieses Berechtigungspaket ein Ordnerberechtigungspaket ist.
	 */
	public boolean isFolderAuthorizationPackage() {
		return type.equals(FOLDER_TYPE);
	}

	/**
	 * Liefert true genau dann, wenn dieses Berechtigungspaket ein Templateberechtigungspaket ist.
	 */
	public boolean isContentClassAuthorizationPackage() {
		return type.equals(CONTENT_CLASS_TYPE);
	}

	/**
	 * Liefert true genau dann, wenn dieses Berechtigungspaket ein Templateberechtigungspaket ist.
	 */
	public boolean isTemplateAuthorizationPackage() {
		return isContentClassAuthorizationPackage();
	}

	/**
	 * Liefert true genau dann, wenn dieses Berechtigungspaket ein Sprachvariantenberechtigungspaket ist.
	 */
	public boolean isLanguageVariantAuthorizationPackage() {
		return type.equals(LANGUAGE_VARIANT_TYPE);
	}

	/**
	 * Liefert true genau dann, wenn dieses Berechtigungspaket ein Projektvariantenberechtigungspaket ist.
	 */
	public boolean isProjectVariantAuthorizationPackage() {
		return type.equals(PROJECT_VARIANT_TYPE);
	}

	/**
	 * Liefert true genau dann, wenn dieses Berechtigungspaket ein normales Berechtigungspaket oder Detailberechtigungspaket für Seiten ist.
	 */
	public boolean isPageAuthorizationPackage() {
		return type.equals(NORMAL_TYPE) || type.equals(DETAILED_PAGE_TYPE);
	}

	/**
	 * Liefert true, falls die gegebene Benutzergruppe in diesem Paket das Recht hat (allowed) die Seite zu publizieren.
	 * <p>
	 * Nur für normale Berechtigungspakete (Typ=0).
	 */
	public boolean isPublishPageAllowed(UserGroup userGroup) throws RQLException {

		// check type
		if (!isPageAuthorizationPackage()) {
			throw new WrongTypeException("The authorization package " + getName()
					+ " is not of type normal (type=0), therefore this check is not supported.");
		}
		// check right
		return getAuthorizationUserGroup(userGroup).isPagePublisPagesAllowed();
	}
}
