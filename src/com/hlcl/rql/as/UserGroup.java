package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Diese Klasse beschreibt eine Benutzergruppe.
 * 
 * @author LEJAFR
 */
public class UserGroup implements CmsClientContainer {

	private CmsClient cmsClient;
	private String email;
	private String name;
	private String userGroupGuid;
	private final String RQL_ACTION_UNLINK = "unlink";
	private final String RQL_ACTION_ASSIGN = "assign";

	// cache
	private RQLNodeList userNodeListCache;

	/**
	 * Vollständiger Konstruktor mit e-mail adresse.
	 */
	public UserGroup(CmsClient client, String userGroupGuid, String name, String email) {
		super();

		this.cmsClient = client;
		this.userGroupGuid = userGroupGuid;
		this.name = name;
		this.email = email;
	}

	/**
	 * Konstruktor ohne e-mail adresse.
	 */
	public UserGroup(CmsClient client, String userGroupGuid, String name) {
		this(client, userGroupGuid, name, null);
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
	 * Liefert true, falls der gegebene Benutzer zu dieser Gruppe gehört. Liefert false, falls diese Gruppe keine Benutzer hat.
	 * 
	 * Für den Vergleich wird der Benutzername verwendet.
	 */
	public boolean contains(String userName) throws RQLException {

		RQLNodeList userNodeList = getUserNodeList();

		// no users at all
		if (userNodeList == null) {
			return false;
		}

		for (int i = 0; i < userNodeList.size(); i++) {
			RQLNode userNode = (RQLNode) userNodeList.get(i);
			if (userNode.getAttribute("name").equals(userName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Liefert true, falls der gegebene Benutzer zu dieser Gruppe gehört.
	 * 
	 * Für den Vergleich wird der Benutzername verwendet.
	 */
	public boolean contains(User user) throws RQLException {

		return contains(user.getName());
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return cmsClient;
	}

	/**
	 * Liefert die e-Mailadresse dieser Benutzergruppe.
	 * 
	 * @return java.lang.String
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Liefert true, falls diese Gruppe keine Benutzer zugeordnet hat.
	 */
	public boolean isEmpty() throws RQLException {
		RQLNodeList list = getUserNodeList();
		return (list == null);
	}

	/**
	 * Liefert die RedDot logon GUID des users unter dem das script läuft.
	 * 
	 */
	public String getLogonGuid() {
		return getCmsClient().getLogonGuid();
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
	 * Liefert die GUID dieser Projektvariante.
	 * 
	 * @return java.lang.String
	 */
	public String getUserGroupGuid() {
		return userGroupGuid;
	}

	/**
	 * Liefert die RQL node list für die Benutzer in dieser Gruppe. Liefert null, falls diese Gruppe keine Benutzer hat.
	 * 
	 * @return java.lang.String
	 */
	private RQLNodeList getUserNodeList() throws RQLException {
		/* 
		V5 request
		ohne session key, da gruppen projektunabhängig existieren
		<IODATA loginguid="415D151C22C94C17AD30E9BB66AA57DF">
		<ADMINISTRATION>
		<GROUP guid="75D0C136086046579D444B81D3079DC3">
		<USERS action="list" />
		</GROUP>
		</ADMINISTRATION>
		</IODATA>
		V5 response
		<IODATA>
		<GROUP guid="75D0C136086046579D444B81D3079DC3">
		<USERS action="list" parentguid="75D0C136086046579D444B81D3079DC3">
		<USER guid="67154D5E9E88489188EB82A89BA0FC06" id="173" name="chiact" fullname="CHIA CHONG THAI STEVEN" email="chiact@HLCL.COM"/>
		<USER guid="A591A89A4A564E65A0CB214B5E6EB4DE" id="12" name="chiamhy" fullname="Heng Yeong Chiam " email="chiamhy@hlcl.com"/>
		<USER guid="6B9C41C04B6F4953A7DBBDCF86654965" id="265" name="heinsra" fullname="Ralf Heinsohn" email="heinsra@hlcl.com"/>
		<USER guid="5F80AC4E6C464FEBA474A5E7DCC4EA36" id="133" name="ioriojo" fullname="Joseph P. Iorio" email="ioriojo@HLCL.COM"/>
		<USER guid="047E236471B846CD8C2BA10FDC57198B" id="184" name="karstuw" fullname="Uwe Karstens" email="karstuw@hlcl.com"/>
		<USER guid="AF459095C7F54D79888D8A8CC5B380E2" id="154" name="keysesa" fullname="Keyser, Sabrina" email="keysesa@hlcl.com"/>
		<USER guid="F1714603A20343A4B9F5942CF4C6FF30" id="1" name="kiessan" fullname="Antje Kiessig" email="kiessan@hlcl.com"/>
		<USER guid="E39A07D6B3EE4B7FA5F568E22F9CE039" id="172" name="leesf" fullname="LEE SOON FATT" email="leesf@HLCL.COM"/>
		<USER guid="4324D172EF4342669EAF0AD074433393" id="129" name="lejafr" fullname="Frank Leja" email="lejafr@hlcl.com"/>
		<USER guid="EA20FA3B06624733AEBEA3283BE389F1" id="242" name="ramirma" fullname="Martha Ramirez/Lebron" email="ramirma@hlcl.com"/>
		<USER guid="11D003FC3B584AA0BBE88D72140E6F3D" id="98" name="retzkwd" fullname="Retzko, Wolf-Dietrich" email="wolf-dietrich.retzko@hlcl.com"/>
		<USER guid="BCD4CF70A04049A0AA42709F0E84745B" id="96" name="rymkilu" fullname="Rymkiewitsch, Lutz" email="rymkilu@hlcl.com"/>
		</USERS>
		</GROUP>
		</IODATA>
		*/
		if (userNodeListCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + " <ADMINISTRATION>" + "  <GROUP guid='"
					+ getUserGroupGuid() + "'>" + "   <USERS action='list'/>" + "  </GROUP>" + " </ADMINISTRATION>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			userNodeListCache = rqlResponse.getNodes("USER");
		}
		return userNodeListCache;
	}

	/**
	 * Liefert alle Benutzer, die dieser Gruppe zugeordnet sind.
	 * 
	 * @return <code>java.util.List</code> of <code>User</code>s
	 */
	public java.util.List<User> getUsers() throws RQLException {

		// wrap users
		RQLNodeList userNodeList = getUserNodeList();
		RQLNode node = null;
		java.util.List<User> users = new ArrayList<User>();
		if (userNodeList != null) {
			for (int i = 0; i < userNodeList.size(); i++) {
				node = (RQLNode) userNodeList.get(i);
				users.add(new User(getCmsClient(), node.getAttribute("name"), node.getAttribute("guid"), node.getAttribute("id"), node
						.getAttribute("fullname"), node.getAttribute("email")));
			}
		}
		return users;
	}

	/**
	 * Fügt alle Benutzer der gegebenen sourceGroup dieser Gruppe hinzu.
	 */
	public void addUsers(UserGroup sourceGroup) throws RQLException {
		editUsers(RQL_ACTION_ASSIGN, sourceGroup.getUsers());
	}

	/**
	 * Fügt alle gegebenen Benutzer dieser Gruppe hinzu.
	 */
	public void addUsers(java.util.List<User> usersToAdd) throws RQLException {
		editUsers(RQL_ACTION_ASSIGN, usersToAdd);
	}

	/**
	 * Entfernt alle gegebenen Benutzer von dieser Gruppe.
	 */
	public void removeUsers(java.util.List<User> usersToRemove) throws RQLException {
		editUsers(RQL_ACTION_UNLINK, usersToRemove);
	}

	/**
	 * Entfernt alle Benutzer von dieser Gruppe. Diese Gruppe hat danach keine Benutzer mehr zugeordnet.
	 */
	public void removeUsers() throws RQLException {
		removeUsers(getUsers());
	}

	/**
	 * Entfernt alle Benutzer der gegebenen sourceGroup von dieser Gruppe.
	 */
	public void removeUsers(UserGroup sourceGroup) throws RQLException {
		editUsers(RQL_ACTION_UNLINK, sourceGroup.getUsers());
	}

	/**
	 * Fügt dieser Gruppe den gegebenen User hinzu.
	 */
	public void addUser(User user) throws RQLException {
		editUser(RQL_ACTION_ASSIGN, user);
	}

	/**
	 * Fügt dieser Gruppe den gegebenen User hinzu.
	 */
	public void removeUser(User user) throws RQLException {
		editUser(RQL_ACTION_UNLINK, user);
	}

	/**
	 * Liefert alle Benutzer dieser Gruppe zurück, die nicht in subtrahend sind.
	 * <p>
	 * Für die gilt: this.getUsers() - subtrahend.getUsers().
	 */
	public List<User> difference(UserGroup subtrahend) throws RQLException {
		List<User> difference = new ArrayList<User>(getUsers());
		difference.removeAll(subtrahend.getUsers());
		return difference;
	}

	/**
	 * Fügt dieser Gruppe den gegebenen User hinzu (action=assign) oder entfernt ihn aus dieser Gruppe (action=unlink).
	 */
	private void editUser(String action, User user) throws RQLException {
		editUsers(action, Arrays.asList(user));
	}

	/**
	 * Fügt dieser Gruppe die gegebenen User hinzu (action=assign) oder entfernt sie aus dieser Gruppe (action=unlink).
	 */
	private void editUsers(String action, java.util.List<User> users) throws RQLException {
		/* 
		V7.5 request
		assign or unlink
		<IODATA loginguid="[!guid_login!]">
		  <ADMINISTRATION action="assign">
		    <GROUP guid="[!guid_group!]">
		      <USER guid="[!guid_user!]"/>
		      ...
		    </GROUP>
		  </ADMINISTRATION>
		</IODATA>
		V7.5 response
		<IODATA>ok
		</IODATA>
		*/
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "<ADMINISTRATION action='" + action + "'>"
				+ "<GROUP guid='" + getUserGroupGuid() + "'>";
		for (User user : users) {
			rqlRequest += "<USER guid='" + user.getUserGuid() + "'/>";
		}
		rqlRequest += "</GROUP></ADMINISTRATION></IODATA>";
		callCmsWithoutParsing(rqlRequest);

		// empty cache to force new read
		userNodeListCache = null;
	}
}
