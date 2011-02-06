package com.hlcl.rql.as;

import java.util.ArrayList;

/**
 * Diese Klasse beschreibt ein Verzeichnis auf dem Ausgabeserver (Publication Folder der Publication Structure).
 * 
 * @author LEJAFR
 */
public class PublicationFolder implements ProjectContainer {
	private String publicationFolderGuid;
	private String name;
	private String path; // optional, separated by \

	private Project project;
	// seems to be the same for all projects!
	protected final static String PUBLICATION_STRUCTURE_TREE_GUID = "9BBF210F7923406291BE7AE47B4CA571";
	protected final static String TREESEGMENT_TYPE = "project.6090";

	/**
	 * Folder constructor comment.
	 */
	public PublicationFolder(Project project, String name, String publicationFolderGuid) {
		super();

		this.project = project;
		this.name = name;
		this.publicationFolderGuid = publicationFolderGuid;
	}

	/**
	 * Folder constructor with path (elements separated by \).
	 */
	public PublicationFolder(Project project, String name, String publicationFolderGuid, String path) {
		super();

		this.project = project;
		this.name = name;
		this.publicationFolderGuid = publicationFolderGuid;
		this.path = path;
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
	 * Zwei Folder werden als identisch interpretiert, falls beide die gleiche GUID haben.
	 */
	public boolean equals(Object obj) {
		PublicationFolder second = (PublicationFolder) obj;
		return this.getPublicationFolderGuid().equals(second.getPublicationFolderGuid());
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	/**
	 * Liefert die RedDot GUID dieses Publicaton Folders.
	 */
	public String getPublicationFolderGuid() {

		return publicationFolderGuid;
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {
		return getProject().getLogonGuid();
	}

	/**
	 * Liefert den Namen dieses Folders.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the path of this folder starting from the root. The separator is the backslash \ per default.
	 * 
	 * @throws RQLException
	 */
	public String getPath() throws RQLException {
		if (path == null) {
			path = getProject().getPublicationFolderPathByGuid(getPublicationFolderGuid());
		}
		return path;
	}

	/**
	 * Returns true, if the path of this folder starts with the given prefix. The differences between backslash and slash are ignored.
	 * 
	 * @throws RQLException
	 */
	public boolean isPathStartsWith(String prefix) throws RQLException {
		return getPath(true).startsWith(prefix.replace('\\', '/'));
	}

	/**
	 * Returns the path of this folder. If convertToSlash=true the path separator / is delivered, other \ is delivered.
	 * 
	 * @throws RQLException
	 */
	public String getPath(boolean convertToSlash) throws RQLException {
		return convertToSlash ? getPath().replace('\\', '/') : getPath();
	}

	/**
	 * Returns the path of this folder. If convertToSlash=true the path separator / is delivered, otherwise \ is delivered. If the path
	 * starts with prefixToRemove it will be removed, otherwise the path is not shortened.
	 * <p>
	 * All root folders are delivered from CMS without a leading \.
	 * <p>
	 * Example of a realpath delivered by CMS: docRoot\images\fleet\vessels\vessel.
	 * 
	 * @throws RQLException
	 */
	public String getPathWithoutPrefix(boolean convertToSlash, String prefixToRemove) throws RQLException {
		return StringHelper.removePrefix(getPath(convertToSlash), prefixToRemove);
	}

	/**
	 * Liefert den Pfad zu diesem Folder (nur im 1.Level) unterhalb der root der Publishing Structure; beginnt immer mit pathSeparator,
	 * aber ohne / am Ende.
	 * 
	 * @throws RQLException
	 * 
	 * @see #getPath()
	 */
	public String getPublishingPathFromPublishingRoot(String pathSeparator) throws RQLException {
		return pathSeparator + StringHelper.replace(getPath(), "\\", pathSeparator);
	}

	/**
	 * Liefert das Project, zu dem dieser Publication Folder gehoert.
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
	 * Liefert den child Publication Folder mit dem gegebenen Namen dieses Publication Folders, oder null, falls keiner mit dem Namen
	 * existiert.
	 * <p>
	 * Benötigt den session key! Checks with equalsIgnoreCase().
	 */
	public PublicationFolder getChildByName(String childName) throws RQLException {
		return getProject().getPublicationFolderByName(getPublicationFolderGuid(), childName);
	}

	/**
	 * Returns all direct child publication folders of this publication folders. Returns an empty list, if no children exists.
	 * <p>
	 * Benötigt den session key! Checks with equalsIgnoreCase().
	 */
	public java.util.List<PublicationFolder> getChildren() throws RQLException {
		java.util.List<PublicationFolder> result = new ArrayList<PublicationFolder>();
		RQLNodeList nodeList = getProject().getPublicationFolderNodeList(getPublicationFolderGuid());
		// wrap into objects
		for (int i = 0; i < nodeList.size(); i++) {
			RQLNode node = nodeList.get(i);
			result.add(new PublicationFolder(getProject(), node.getAttribute("value"), node.getAttribute("guid")));
		}
		return result;
	}

	/**
	 * Ensures, that publication folders exists. Creates new one, if needed under this publication folder accordingly to the given
	 * pathToClone. The given withouPathPrefix will be removed before creation of new sub folders will start.
	 */
	public PublicationFolder ensurePath(PublicationFolder pathToClone, PublicationFolder withoutPathPrefix) throws RQLException {
		String path = pathToClone.getPath();
		String prefix = withoutPathPrefix.getPath();

		// remove prefix
		String createPath = StringHelper.replace(path, prefix + "\\", "");

		// ensure path
		String[] names = StringHelper.split(createPath, "\\");
		PublicationFolder parent = this;
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			parent = parent.getOrCreateChildFolder(name);
		}
		return parent;
	}

	/**
	 * Creates publication folders under this publication folder accordingly to the given pathToClone. The given withouPathPrefix will
	 * be removed before creation of new sub folders will start.
	 */
	public PublicationFolder createPath(PublicationFolder pathToClone, PublicationFolder withoutPathPrefix) throws RQLException {
		String path = pathToClone.getPath();
		String prefix = withoutPathPrefix.getPath();

		// remove prefix
		String createPath = StringHelper.replace(path, prefix + "\\", "");

		// create from path
		String[] names = StringHelper.split(createPath, "\\");
		PublicationFolder parent = this;
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			parent = parent.createChildFolder(name);
		}
		return parent;
	}

	/**
	 * Returns the child folder for given name childName, if folder exists. If not, create a sub folder with that name.
	 */
	public PublicationFolder getOrCreateChildFolder(String childName) throws RQLException {
		PublicationFolder result = getChildByName(childName);
		// create if not found
		if (result == null) {
			result = createChildFolder(childName);
		}
		return result;
	}

	/**
	 * Erstellt einen neuen Publication Folder unterhalb dieses Publication Folders. Der neu erstellte wird zurückgeliefert.
	 */
	public PublicationFolder createChildFolder(String childName) throws RQLException {
		/*
		 V7.5 request
		<IODATA loginguid="0B9A46F304C74A48993A7E895A6BF282" sessionkey="D0060B430D3C4C03B591A8CD9A2393C2">
		  <PROJECT>
		    <EXPORTFOLDER guid="BD1EA57A2BA249BE96D39710C54F0D2F" action="assign">
		      <EXPORTFOLDER action="addnew" name="newsletter" />
		    </EXPORTFOLDER>
		  </PROJECT>
		</IODATA>
		 V7.5 response
		<IODATA>
		<EXPORTFOLDER action="addnew" name="newsletter" parentguid="BD1EA57A2BA249BE96D39710C54F0D2F" lastguid="" guid="FD81A30E7009485488CE56CF514EA2F7"/>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<PROJECT><EXPORTFOLDER action='assign' guid='" + getPublicationFolderGuid() + "'>"
				+ "<EXPORTFOLDER action='addnew' name='" + childName + "'/>" + "</EXPORTFOLDER></PROJECT></IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		// delete cache in project
		invalidatePublicationFoldersCache();

		// wrap result into object
		return new PublicationFolder(getProject(), childName, rqlResponse.getNode("EXPORTFOLDER").getAttribute("guid"));
	}

	/**
	 * Show name for easier debugging.
	 */
	public String toString() {
		return getClass().getName() + "(" + getName() + ")";
	}

	/**
	 * Deletes the publication folders cache (in project).
	 */
	private void invalidatePublicationFoldersCache() {
		getProject().invalidatePublicationFoldersCache();
	}
}
