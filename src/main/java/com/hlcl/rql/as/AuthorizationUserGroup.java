package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

/**
 * Diese Klasse beschreibt Berechtigungen einer UserGroup einesBerechtigungspaketes.
 * 
 * @author LEJAFR
 */
public class AuthorizationUserGroup implements AuthorizationPackageContainer {

	private final AuthorizationPackage authorizationPackage;
	private final String userGroupGuid;
	private final String userGroupName;
	private final long[] allowed = new long[9];  // unsigned int
	private final long[] denied  = new long[9];  // unsigned int

	public AuthorizationUserGroup(AuthorizationPackage authorizationPackage,
			String userGroupGuid, String userGroupName,
			String right1, String right2, String right3, String right4, String right5, String right6, String right7, String right8,
			String deny1, String deny2, String deny3, String deny4, String deny5, String deny6, String deny7, String deny8)
	{
		this.authorizationPackage = authorizationPackage;
		this.userGroupGuid = userGroupGuid;
		this.userGroupName = userGroupName;

		allowed[1] = Long.parseLong(right1);
		allowed[2] = Long.parseLong(right2);
		allowed[3] = Long.parseLong(right3);
		allowed[4] = Long.parseLong(right4);
		allowed[5] = Long.parseLong(right5);
		allowed[6] = Long.parseLong(right6);
		allowed[7] = Long.parseLong(right7);
		allowed[8] = Long.parseLong(right8);

		denied[1] = Long.parseLong(deny1);
		denied[2] = Long.parseLong(deny2);
		denied[3] = Long.parseLong(deny3);
		denied[4] = Long.parseLong(deny4);
		denied[5] = Long.parseLong(deny5);
		denied[6] = Long.parseLong(deny6);
		denied[7] = Long.parseLong(deny7);
		denied[8] = Long.parseLong(deny8);
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
		return isAllowed(Authorization.Page.PublishPages);
	}

	/**
	 * Liefert true, falls für diese Benutzergruppe das Strukturelementerecht connect existing page zugelassen ist.
	 */
	public boolean isLinksConnectExistingPageAllowed() throws RQLException {
		return isAllowed(Authorization.StructureElement.ConnectToExistingPages);
	}

	/**
	 * Ändert für diese Benutzergruppe das Linksrecht connect existing page auf den gegebenen Wert.
	 */
	public void setIsLinksConnectExistingPageAllowed(boolean isConnectExistingPageAllowed) throws RQLException {
		setIsAllowedDirect(Authorization.StructureElement.ConnectToExistingPages, isConnectExistingPageAllowed);
	}

	/**
	 * Ändert für diese Benutzergruppe das Seitenrecht publish page auf den gegebenen Wert, direkt.
	 */
	public void setIsPagePublisPagesAllowed(boolean isPublishPageAllowed) throws RQLException {
		setIsAllowedDirect(Authorization.Page.PublishPages, isPublishPageAllowed);
	}


	/**
	 * Set a single authorization.
	 * 
	 * @param a bit identifier
	 * @param isAllowed set or clear this bit
	 * @throws RQLException
	 */
	public void setIsAllowedDirect(Authorization a, boolean isAllowed) throws RQLException {
		long value = allowed[a.getOffset()];
		if (isAllowed) {
			value |= a.getBitmask();
		} else {
			value &= (~ a.getBitmask()) & 0xFFFF;
		}
		save("right" + a.getOffset() + "='" + value + "'");
		allowed[a.getOffset()] = value;
	}
	
	
	
	/**
	 * Change some bits in this set, without direct saving.
	 */
	public void applyAuthorization(AuthorizationSet set) {
		int offset = set.authorization.getOffset();
		int mask = set.authorization.getBitmask();
		
		if (set.allowed) {
			allowed[offset] |= mask;
		} else {
			allowed[offset] &= (~mask) & 0xFFFF;
		}

		if (set.denied) {
			denied[offset] |= mask;
		} else {
			denied[offset] &= (~mask) & 0xFFFF;
		}
	}
	

	/**
	 * Ändert die Rechte dieser Berechtigungsbenutzergruppe.
	 * <p>
	 * Es wird der cache im AuthorizationPackage zurückgesetzt. Die Rechtewerte in diesem Objekt müssen selbst gesetzt werden.
	 */
	public void save(String attributes) throws RQLException {

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
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<AUTHORIZATION>"
				+ "<AUTHORIZATIONPACKET action='save' guid='" + getAuthorizationPackageGuid() + "'>"
				+ "<GROUPS><GROUP guid='" + getUserGroupGuid() + "' " + attributes + "/>"
				+ "</GROUPS></AUTHORIZATIONPACKET></AUTHORIZATION></IODATA>";
		callCmsWithoutParsing(rqlRequest); // ignore result

		// force new-read on package level
		getAuthorizationPackage().clearCaches();
	}


	public void save() throws RQLException {
		StringBuilder rqlRequest = new StringBuilder(128);
		rqlRequest.append("<IODATA loginguid='").append(getLogonGuid()).append("' sessionkey='").append(getSessionKey())
		.append("'>")
		.append("<AUTHORIZATION>")
		.append("<AUTHORIZATIONPACKET action='save' guid='").append(getAuthorizationPackageGuid()).append("'>")
		.append("<GROUPS><GROUP guid='").append(getUserGroupGuid()).append("' ");

		//+ attributes +
		for (int offset = 1; offset < allowed.length; offset++) {
			rqlRequest.append("right").append(offset).append("='").append(allowed[offset]).append("' ");
			rqlRequest.append("deny").append(offset).append("='").append(denied[offset]).append("' ");
		}
		rqlRequest.append(" /></GROUPS></AUTHORIZATIONPACKET></AUTHORIZATION></IODATA>");
		callCmsWithoutParsing(rqlRequest.toString()); // ignore result

		// force new-read on package level
		getAuthorizationPackage().clearCaches();
	}

	public boolean isAllowed(Authorization a) {
		return (allowed[a.getOffset()] & a.getBitmask()) != 0; 
	}


	public boolean isDenied(Authorization a) {
		return (denied[a.getOffset()] & a.getBitmask()) != 0; 
	}


	public void collectAuthorizations(Collection<AuthorizationSet> outList, Authorization[] values, boolean filterMode) {
		outer:
		for (Authorization a : values) {
			if (a.getOffset() == 0) continue; // dummy, FIXME: sollte irgendwann nicht mehr auftreten
			
			AuthorizationSet out = new AuthorizationSet();
			out.authorization = a;
			out.allowed = isAllowed(a);
			out.denied = isDenied(a);
			if (out.allowed || out.denied) {
				if (filterMode) { // filter: only if there isnt anything other, yet
					for (AuthorizationSet o : outList) {
						if (Authorization.Fun.equals(o.authorization, a)) {
							// this one is known already, do not add it again
							continue outer;
						}
					}
				}
				
				outList.add(out);
			}
			// else: no data
		}
	}
	
	
	public Collection<AuthorizationSet> listAuthorizations() {
		ArrayList<AuthorizationSet> out = new ArrayList<AuthorizationSet>(32);

		collectAuthorizations(out, Authorization.Page.values(), false);
		collectAuthorizations(out, Authorization.StructureElement.values(), false);
		collectAuthorizations(out, Authorization.Dummy.values(), true); // anonymous
		
		return out;
	}
	
	
}
