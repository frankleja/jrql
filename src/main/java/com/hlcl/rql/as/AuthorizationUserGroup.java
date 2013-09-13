package com.hlcl.rql.as;

import java.math.BigInteger;
import java.util.ResourceBundle;

/**
 * Diese Klasse beschreibt Berechtigungen einer UserGroup einesBerechtigungspaketes.
 * 
 * @author LEJAFR
 */
public class AuthorizationUserGroup implements AuthorizationPackageContainer {

	final static int RIGHT_PAGE_ASSIGN_KEYWORDS_BIT_INDEX = 6;
	final static int RIGHT_PAGE_EDIT_HEADLINE_BIT_INDEX = 0;
	final static int RIGHT_PAGE_LINKING_APPEARANCE_SCHEDULE_BIT_INDEX = 5;
	final static int RIGHT_PAGE_PUBLISH_PAGE_BIT_INDEX = 13;
	final static int RIGHT_LINKS_CONNECT_EXISTING_PAGE_BIT_INDEX = 2;

	private AuthorizationPackage authorizationPackage;

	private String userGroupGuid;
	private String userGroupName;
	private BigInteger[] allowed;
	private BigInteger[] denied;

	/**
	 * constructor comment.
	 */
	public AuthorizationUserGroup(AuthorizationPackage authorizationPackage, String userGroupGuid, String userGroupName, String right1,
			String right2, String right3, String right4, String right5, String right6, String right7, String right8, String deny1, String deny2,
			String deny3, String deny4, String deny5, String deny6, String deny7, String deny8) {

		this.authorizationPackage = authorizationPackage;
		this.userGroupGuid = userGroupGuid;
		this.userGroupName = userGroupName;

		allowed = new BigInteger[9];
		allowed[1] = new BigInteger(right1);
		allowed[2] = new BigInteger(right2);
		allowed[3] = new BigInteger(right3);
		allowed[4] = new BigInteger(right4);
		allowed[5] = new BigInteger(right5);
		allowed[6] = new BigInteger(right6);
		allowed[7] = new BigInteger(right7);
		allowed[8] = new BigInteger(right8);

		denied = new BigInteger[9];
		denied[1] = new BigInteger(deny1);
		denied[2] = new BigInteger(deny2);
		denied[3] = new BigInteger(deny3);
		denied[4] = new BigInteger(deny4);
		denied[5] = new BigInteger(deny5);
		denied[6] = new BigInteger(deny6);
		denied[7] = new BigInteger(deny7);
		denied[8] = new BigInteger(deny8);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getAuthorizationPackage().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert das Berechtigungspaket, zu dem diese Berechtigungs-Benutzergruppe gehört.
	 */
	public AuthorizationPackage getAuthorizationPackage() {
		return authorizationPackage;
	}

	/**
	 * Liefert die GUID des Berechtigungspaket, zu dem diese Berechtigungs-Benutzergruppe gehört.
	 */
	public String getAuthorizationPackageGuid() {
		return getAuthorizationPackage().getAuthorizationPackageGuid();
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getAuthorizationPackage().getCmsClient();
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {
		return getAuthorizationPackage().getLogonGuid();
	}

	/**
	 * Liefert das Projekt.
	 */
	public Project getProject() {
		return getAuthorizationPackage().getProject();
	}

	/**
	 * Liefert die RedDot GUID des Projekts.
	 */
	public String getProjectGuid() throws RQLException {
		return getAuthorizationPackage().getProjectGuid();
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getAuthorizationPackage().getSessionKey();
	}

	/**
	 * Liefert die Benutzergruppe.
	 * <p>
	 * 
	 * ACHTUNG: Dafür sind admin Rechte erforderlich!
	 */
	public UserGroup getUserGroup() throws RQLException {
		return getProject().getUserGroupByGuid(getUserGroupGuid());
	}

	/**
	 * Liefert die RedDot GUID der Benutzergruppe.
	 */
	public String getUserGroupGuid() {
		return userGroupGuid;
	}

	/**
	 * Liefert den Namen der Benutzergruppe.
	 */
	public String getUserGroupName() {
		return userGroupName;
	}

	/**
	 * Liefert alle Benutzer, wenn dieses Berechtigungsgruppe eine Usergruppe ist.
	 * <p>
	 */
	public java.util.List<User> getUserGroupUsers() throws RQLException {
		return getUserGroup().getUsers();
	}

	/**
	 * Liefert true, falls dies die Pseudogruppe Everyone/Jeder im Berechtigungspaket ist.
	 */
	public boolean isEveryone() {
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		String everyoneDe = b.getString("PseudoUserGroupEveryoneDe");
		String everyoneEn = b.getString("PseudoUserGroupEveryoneEn");

		String name = getUserGroupName();
		return name.indexOf(everyoneDe) >= 0 || name.indexOf(everyoneEn) >= 0;
	}

	/**
	 * Liefert true, falls für diese Benutzergruppe das Seitenrecht publish page zugelassen ist.
	 */
	public boolean isPagePublisPagesAllowed() throws RQLException {

		return allowed[1].testBit(RIGHT_PAGE_PUBLISH_PAGE_BIT_INDEX);
	}

	/**
	 * Liefert true, falls für diese Benutzergruppe das Strukturelementerecht connect existing page zugelassen ist.
	 */
	public boolean isLinksConnectExistingPageAllowed() throws RQLException {

		return allowed[2].testBit(RIGHT_LINKS_CONNECT_EXISTING_PAGE_BIT_INDEX);
	}

	/**
	 * Ändert für diese Benutzergruppe das Linksrecht connect existing page auf den gegebenen Wert.
	 */
	public void setIsLinksConnectExistingPageAllowed(boolean isConnectExistingPageAllowed) throws RQLException {
		setIsAllowed(2, RIGHT_LINKS_CONNECT_EXISTING_PAGE_BIT_INDEX, isConnectExistingPageAllowed);
	}

	/**
	 * Ändert für diese Benutzergruppe das Seitenrecht publish page auf den gegebenen Wert.
	 */
	public void setIsPagePublisPagesAllowed(boolean isPublishPageAllowed) throws RQLException {
		setIsAllowed(1, RIGHT_PAGE_PUBLISH_PAGE_BIT_INDEX, isPublishPageAllowed);
	}

	/**
	 * Ändert ein Bit in einem der rightX attribute.
	 */
	private void setIsAllowed(int allowedIndex, int bitIndex, boolean isAllowed) throws RQLException {
		// get old value
		BigInteger value = allowed[allowedIndex];
		// modifiy
		if (isAllowed) {
			value = value.setBit(bitIndex);
		} else {
			value = value.clearBit(bitIndex);
		}
		// save new value
		save("right" + allowedIndex + "='" + value.toString() + "'");
		// update cache
		allowed[allowedIndex] = value;
	}

	/**
	 * Ändert die Rechte dieser Berechtigungsbenutzergruppe.
	 * <p>
	 * Es wird der cache im AuthorizationPackage zurückgesetzt. Die Rechtewerte in diesem Objekt müssen selbst gesetzt werden.
	 * 
	 * @see #setIsAllowed(int, int, boolean)
	 */
	private void save(String attributes) throws RQLException {

		/* 
		 V7.5 request
		<IODATA loginguid="806D1247175F4F339298C6EC0AF3DA73" sessionkey="9EDD2B40A0584DFCB4173F9AF89EA902">
		  <AUTHORIZATION>  
		    <AUTHORIZATIONPACKET action="save" guid="4544AD14D2C74EC492D16F2D3FD1BF5F">
		      <GROUPS>
		        <GROUP guid="B1D4F245883F4032AFBF9D209AA7DB8E" right1="97" />
		      </GROUPS>
		    </AUTHORIZATIONPACKET>
		  </AUTHORIZATION>
		</IODATA>
		 V7.5 response 
		<IODATA><AUTHORIZATIONPACKET action="save" languagevariantid="ENG" elementguid="" guid="4544AD14D2C74EC492D16F2D3FD1BF5F">
			<GROUPS>
				<GROUP guid="B1D4F245883F4032AFBF9D209AA7DB8E" right1="97"/>
			</GROUPS>
		</AUTHORIZATIONPACKET>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<AUTHORIZATION>"
				+ "<AUTHORIZATIONPACKET action='save' guid='" + getAuthorizationPackageGuid() + "'>" + "<GROUPS><GROUP guid='" + getUserGroupGuid()
				+ "' " + attributes + "/>" + "</GROUPS></AUTHORIZATIONPACKET></AUTHORIZATION></IODATA>";
		callCmsWithoutParsing(rqlRequest); // ignore result

		// force new-read on package level
		getAuthorizationPackage().clearCaches();
	}

}
