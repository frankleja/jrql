package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt ein Exportziel.
 * 
 * @author LEJAFR
 */
public class PublishingTarget implements ProjectContainer {

	private Project project;
	private String publishingTargetGuid;
	private String name;
	private String path;
	private String type;
	private final String TYPE_FTP = "6205";
	private final String TYPE_DIRECTORY = "6206";
	private final String TYPE_SFTP = "6207";
	private final String TYPE_LIVE_SERVER = "6208";
	private final String SEPARATOR_FTP = "/";
	private final String SEPARATOR_DIRECTORY = "\\";
	private RQLNode detailsNode;

	/**
	 * constructor comment.
	 */
	public PublishingTarget(Project project, String publishingTargetGuid, String name, String path, String type) {
		super();

		this.project = project;
		this.publishingTargetGuid = publishingTargetGuid;
		this.name = name;
		this.path = path;
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
	 * Zwei Exportziele werden als gleich betrachtet, wenn ihre GUID übereinstimmt.
	 */
	public boolean equals(Object obj) {

		if (!(obj instanceof PublishingTarget))
			return false;

		PublishingTarget target = (PublishingTarget) obj;
		return this.getPublishingTargetGuid().equals(target.getPublishingTargetGuid());
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	/**
	 * Nachlesen von Daten zu diesem publishing target.
	 */
	private RQLNode getDetailsNode() throws RQLException {
		/* 
		 V7.5 request
		<IODATA loginguid="[!guid_login!]">
		  <PROJECT sessionkey="[!key!]">
		    <EXPORT guid="[!guid_export!]" action="load"/>
		  </PROJECT>
		</IODATA>
		 V7.5 response for FTP publishing target
		<IODATA>
		  <EXPORT action="load" languagevariantid="DEU" dialoglanguageid="DEU"
		   projectguid="" guid="[!guid_export!]" name="Publizierung per FTP"
		   type="6205" username="username" password="pw" port="21"
		   asciisuffixlist="" path="ftp://www.domain.de/TestProjekt/" urlprefix="."
		   ivw="" cleanupable="1" nobom="0"/>
		</IODATA>
		V7.5. response for directory publishing target 
		<IODATA>
		  <EXPORT action="load" languagevariantid="DEU" dialoglanguageid="DEU"
		   projectguid="" guid="[!guid_export!]" name="Verzeichnis"
		   type="6206" username="" path="\\reddot\public\" urlprefix="" ivw=""
		   cleanupable="1" nobom="0"/>
		</IODATA> 
		 */

		if (detailsNode == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'><PROJECT>"
					+ "<EXPORT action='load' guid='" + getPublishingTargetGuid() + "' />" + "</PROJECT></IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			detailsNode = rqlResponse.getNode("EXPORT");
		}
		return detailsNode;
	}

	/**
	 * Liefert das Passwort des FTP Users dieses Publizierungsziels zurück.
	 * 
	 * @throws WrongPublishingTargetTypeException
	 */
	public String getFtpPassword() throws RQLException {
		if (!isFtpTarget()) {
			throw new WrongPublishingTargetTypeException("You try to get the FTP user's password from the non FTP publishing target with name "
					+ getName() + ". Correct your program.");
		}
		return getDetailsNode().getAttribute("password");
	}

	/**
	 * Liefert den Namen des FTP Users dieses Publizierungsziels zurück.
	 * 
	 * @throws WrongPublishingTargetTypeException
	 */
	public String getFtpUserName() throws RQLException {
		if (!isFtpTarget()) {
			throw new WrongPublishingTargetTypeException("You try to get the FTP user name from the non FTP publishing target with name " + getName()
					+ ". Correct your program.");
		}
		return getDetailsNode().getAttribute("username");
	}

	/**
	 * Setzt den Benutzernamen und das Passwort des FTP users auf 'unknown'.<p> Über dieses FTP Ziel kann danach nicht mehr publiziert werden. 
	 * 
	 * @throws WrongPublishingTargetTypeException	falls kein FTP publishing target vorliegt
	 */
	public void disableFtpUser() throws RQLException {
		if (!isFtpTarget()) {
			throw new WrongPublishingTargetTypeException("You try to disalbe the FTP publishing on the non FTP publishing target with name " + getName()
					+ ". Correct your program.");
		}
		save("password='unknown' username", "unknown");
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
	 * Liefert den Pfad, auf den dieses Exportziel generiert genauso, wie er in RedDot eingegeben wurde.
	 * 
	 * @throws WrongPublishingTargetTypeException
	 * @see #isDirectoryTarget()
	 * @see #isFtpTarget()
	 * @see #isLiveServerTarget()
	 */
	public String getPath() throws RQLException {
		if (isFtpTarget() || isDirectoryTarget()) {
			return path;
		}
		// cannot be used for live server targets
		throw new WrongPublishingTargetTypeException("This LiveServer publishing target has no path. Filter targets with offered methods.");
	}

	/**
	 * Liefert abhängig vom publishing target typ den Pfadseparator; / für FTP oder SFTP und \ für directory.
	 * 
	 * @throws WrongPublishingTargetTypeException
	 * @see #isDirectoryTarget()
	 * @see #isFtpTarget()
	 * @see #isLiveServerTarget()
	 */
	public String getPathSeparator() throws RQLException {
		if (isFtpTarget()) {
			return SEPARATOR_FTP;
		}
		if (isDirectoryTarget()) {
			return SEPARATOR_DIRECTORY;
		}
		// cannot be used for live server targets
		throw new WrongPublishingTargetTypeException("This LiveServer publishing target has no path. Filter targets with offered methods.");
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
	 * Liefert den Pfad, auf den dieses Exportziel generiert; immer ohne path separator(/ oder \) am Ende.
	 * <p>
	 * Funktioniert für FTP, SFTP oder directory Publizierungsziele.
	 * 
	 * @see #getPathSeparator()
	 * @throws RQLException
	 */
	public String getPublishingPath() throws RQLException {
		String path = getPath();
		if (path.endsWith(getPathSeparator())) {
			return path.substring(0, path.length() - 1);
		}
		return path;
	}

	/**
	 * Liefert den Pfad, auf den dieses Exportziel für den gegebenen Folder generiert; immer ohne path separator(/ oder \) am Ende.
	 * <p>
	 * Funktioniert für FTP, SFTP oder directory Publizierungsziele.
	 * 
	 * @see #getPathSeparator()
	 */
	public String getPublishingPath(PublicationFolder publicationFolder) throws RQLException {
		return getPublishingPath() + publicationFolder.getPublishingPathFromPublishingRoot(getPathSeparator());
	}

	/**
	 * Liefert die GUID dieses Exportzieles.
	 * 
	 * @return java.lang.String
	 */
	public String getPublishingTargetGuid() {
		return publishingTargetGuid;
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getProject().getSessionKey();
	}

	/**
	 * Liefert den internen Typ dieses Publizierungszieles.
	 */
	private String getType() {
		return type;
	}

	/**
	 * Liefert den hashcode dieses targets.
	 */
	public int hashCode() {

		return getPublishingTargetGuid().hashCode();
	}

	/**
	 * Liefert true, falls dieses publishing target ein Directory, lokal oder UNC, ist.
	 */
	public boolean isDirectoryTarget() {
		return getType().equals(TYPE_DIRECTORY);
	}

	/**
	 * Liefert true, falls dieses publishing target ein FTP oder SFTP Target ist.
	 */
	public boolean isFtpTarget() {
		String type = getType();
		return type.equals(TYPE_FTP) || type.equals(TYPE_SFTP);
	}

	/**
	 * Liefert true, falls dieses publishing target ein Live Server target ist.
	 */
	public boolean isLiveServerTarget() {
		return getType().equals(TYPE_LIVE_SERVER);
	}

	/**
	 * Speichert Änderungen an diesem publishing target.
	 */
	private void save(String attributeName, String attributeValue) throws RQLException {
		/* 
		 V7.5 request
		<IODATA loginguid="90BC9AB27FC54F47B2EA74538A171A58" sessionkey="FB5730641A9243C5954E228E6F871D2E">
		  <PROJECT>
		    <EXPORT action="save" guid="2FFF4EE2242B4A9D8100B219EB0A38C1" username="unknown" password="#FB5730641A9243C5954E228E6F871D2E" />
		  </PROJECT>
		</IODATA> 
		 V7.5 response
		<IODATA>2FFF4EE2242B4A9D8100B219EB0A38C1
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'><PROJECT>"
				+ "<EXPORT action='save' guid='" + getPublishingTargetGuid() + "' " + attributeName + "='" + attributeValue + "' />"
				+ "</PROJECT></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Ändert für dieses FTP Publizierungsziel das Passwort des verwendeten FTP users.
	 * 
	 * @throws WrongPublishingTargetTypeException
	 */
	public void setFtpPassword(String newPassword) throws RQLException {
		if (!isFtpTarget()) {
			throw new WrongPublishingTargetTypeException("You tried to change the FTP user's password at the non FTP publishing target with name "
					+ getName() + ", what is not possible. Please correct your program.");
		}
		save("password", newPassword);
	}

	/**
	 * Ändert für dieses FTP Publizierungsziel den verwendeten FTP user.
	 * 
	 * @throws WrongPublishingTargetTypeException
	 */
	public void setFtpUserName(String newUserName) throws RQLException {
		if (!isFtpTarget()) {
			throw new WrongPublishingTargetTypeException("You tried to change the FTP user name at the non FTP publishing target with name "
					+ getName() + ", what is not possible. Please correct your program.");
		}
		save("username", newUserName);
	}

	/**
	 * Show name for easier debugging.
	 */
	public String toString() {
		return getClass().getName() + "(" + getName() + ")";
	}

}
