package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse beschreibt ein Datei Verzeichnis aus RedDot.
 * 
 * @author LEJAFR
 */
public abstract class Folder implements ProjectContainer {
	private String folderGuid;
	private String name;
	private boolean isAvailableInTextEditor;
	private String path;
	private int saveType;

	// data storage constants
	private final int SAVE_TYPE_INTERNAL_DATABASE = 0;
	private final int SAVE_TYPE_OTHER_PROJECT = 1; // folder from other project
	private final int SAVE_TYPE_FILE_SYSTEM = 2;
	private final int SAVE_TYPE_EXTERNAL_SYSTEM = 3; // e.g. document management system hummingbird

	private Project project;

	/**
	 * Folder constructor comment.
	 */
	public Folder(Project project, String name, String folderGuid, String hideInTextEditor, String saveType, String path) {
		super();

		this.project = project;
		this.name = name;
		this.folderGuid = folderGuid;

		this.isAvailableInTextEditor = "0".equals(hideInTextEditor);
		this.saveType = Integer.parseInt(saveType);
		this.path = path;
	}

	/**
	 * SubFolder constructor
	 */
	Folder(Project project, String name, String folderGuid) {
		super();

		this.project = project;
		this.name = name;
		this.folderGuid = folderGuid;

		this.isAvailableInTextEditor = false;
		this.saveType = SAVE_TYPE_FILE_SYSTEM; // only asset managers in file system can have sub-folders
		this.path = null; // unused, path come from AssetManager folder, not sub-folder
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
		Folder second = (Folder) obj;
		return this.getFolderGuid().equals(second.getFolderGuid());
	}

	/**
	 * Liefert genau dann true, wenn der gegebene Dateiname in diesem Folder existiert.
	 */
	public boolean exists(String filename) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="087F79DA22DF4EF385A7A18FDBB238CD" sessionkey="421139875y5k2iP8GF85">
		 <PROJECT>
		 <FOLDER guid="72904B4D243C4B5EA3E458FE9E019AB9">
		 <FILE action="check" sourcename="news_roundup_logo234.gif"/>
		 </FOLDER>
		 </PROJECT>
		 </IODATA>
		 V5 response
		 file exists:
		 <IODATA>
		 <FOLDER guid="72904B4D243C4B5EA3E458FE9E019AB9" languagevariantid="ENG" dialoglanguageid="ENG">
		 <FILE action="check" sourcename="news_roundup_logo.gif" dialoglanguageid="ENG" newsourcename="news_roundup_logo(1).gif"/>
		 </FOLDER>
		 </IODATA>
		 
		 file does not exist:
		 <IODATA>
		 <FOLDER guid="72904B4D243C4B5EA3E458FE9E019AB9" languagevariantid="ENG" dialoglanguageid="ENG">
		 <FILE action="check" sourcename="news_roundup_logo234.gif" dialoglanguageid="ENG" newsourcename="news_roundup_logo234.gif"/>
		 </FOLDER>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
				+ "   <FOLDER guid='" + getFolderGuid() + "'>" + "      <FILE action='check' sourcename='" + filename + "'/>"
				+ "    </FOLDER>" + "  </PROJECT>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNode fileNode = rqlResponse.getNode("FILE");

		return !filename.equals(fileNode.getAttribute("newsourcename"));
	}

	/**
	 * Liefert für normale folder und subfolder immer true, damit die exists() prüfung für diese ordner positiv ausfällt.
	 * <p>
	 * 
	 * @see AssetManagerFolder#existsInSubFolder(String)
	 * @see #exists(String)
	 * @see Folder#exists(String)
	 */
	public boolean existsInSubFolder(String filename) throws RQLException {
		return true;
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	/**
	 * Liefert den Pfad dieses Folder im Dateisystem, falls die Inhalte im Dateisystem abgelegt werden.
	 * <p>
	 * Der gelieferte Pfad endet immer auf \.
	 * 
	 * @throws WrongStorageTypeException
	 *             falls die Inhalte dieses Folders nicht im Dateisystem abgelegt werden
	 */
	public String getFileSystemPath() throws RQLException {
		if (isStoredInFileSystem()) {
			return StringHelper.ensureEnding(path, "\\");
		}
		throw new WrongStorageTypeException("Path cannot be delivered, because this file folder " + getName()
				+ " is not stored in file system.");
	}

	/**
	 * Ersetzt im Pfad dieses Folders im Dateisystem den gegebenen String find mit replace, falls find gefunden wurde.
	 * <p>
	 * Achtung: Es wird keine Prüfung vorgenommen, ob der neue Pfad auch im Dateisystem vorhanden ist!
	 * 
	 * @throws WrongStorageTypeException
	 *             falls die Inhalte dieses Folders nicht im Dateisystem abgelegt werden
	 * @return replaced path
	 */
	public String replaceFileSystemPath(String find, String replace) throws RQLException {
		// save new path
		String newPath = StringHelper.replace(getFileSystemPath(), find, replace);
		save("path", newPath);

		// update cache
		this.path = newPath;

		return path;
	}

	/**
	 * Speichert Änderungen an diesem Folder.
	 */
	private void save(String attributeName, String attributeValue) throws RQLException {
		/* 
		 V7.5 request
		<IODATA loginguid="A7BEE759DB9D44AE972AD85BF0F64292" sessionkey="12AE245720854344BB518178A5E795E5">
		  <PROJECT>
		    <FOLDER action="save" guid="DB544D7459A84AA29F7F5870E2C4B49E" path="\\khh31004.ad.hl.lan\cms_data\content\hlag.com\downloads\career\application_form" />
		 </PROJECT>
		</IODATA>
		 V7.5 response
		<IODATA>
		<FOLDER action="save" path="\\khh31004.ad.hl.lan\cms_data\content\hlag.com\downloads\career\application_form" languagevariantid="ENG" dialoglanguageid="ENG" guid="DB544D7459A84AA29F7F5870E2C4B49E"/>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'><PROJECT>"
				+ "<FOLDER action='save' guid='" + getFolderGuid() + "' " + attributeName + "='" + attributeValue + "' />"
				+ "</PROJECT></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert die RedDot GUID dieses Folders.
	 */
	public String getFolderGuid() {

		return folderGuid;
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
	 * Liefert per default immer true. Wird in Unterklasse asset manager überschrieben.
	 * 
	 * @see AssetManagerFolder#hasNoSubFolders()
	 */
	public boolean hasNoSubFolders() throws RQLException {
		return true;
	}

	/**
	 * Liefert immer per default immer false. Wird in Unterklasse asset manager überschrieben.
	 * 
	 * @see AssetManagerFolder#hasSubFolders()
	 */
	public boolean hasSubFolders() throws RQLException {
		return false;
	}

	/**
	 * Liefert true, falls dieser folder ein AssetManager ist. Wird von der Subklasse mit true überschrieben.
	 */
	public boolean isAssetManagerFolder() {

		return false;
	}

	/**
	 * Liefert true, falls dieser folder ein Unterordner eines AssetManagers ist. Wird von der Subklasse mit true überschrieben.
	 */
	public boolean isAssetManagerSubFolder() {

		return false;
	}

	/**
	 * Liefert true, falls dieser FileFolder oder AssetManager im TextEditor nutzbar ist, sonst false;
	 */
	public boolean isAvailableInTextEditor() throws RQLException {
		return isAvailableInTextEditor;
	}

	/**
	 * Liefert true, falls dieser folder ein einfacher FileFolder ist. Wird von der Subklasse mit true überschrieben.
	 */
	public boolean isFileFolder() {

		return false;
	}

	/**
	 * Liefert true, falls die Inhalte dieses FileFolders oder AssetManagers außerhalb RedDots gespeichert werden, z.B. in einem
	 * Dokumentenmanagementsystems (DMS).
	 */
	public boolean isStoredInExternalSystem() throws RQLException {
		return saveType == SAVE_TYPE_EXTERNAL_SYSTEM;
	}

	/**
	 * Liefert true, falls die Inhalte dieses FileFolders oder AssetManagers im Dateisystem gespeichert werden.
	 */
	public boolean isStoredInFileSystem() throws RQLException {
		return saveType == SAVE_TYPE_FILE_SYSTEM;
	}

	/**
	 * Liefert true, falls die Inhalte dieses FileFolders oder AssetManagers im Dateisystem über einen UNC Pfad gespeichert werden,
	 * also aus dem Netzwerk erreichbar sind. An UNC path is starting always with \\.
	 * Returns false, if folder is not stored in external file system at all. 
	 */
	public boolean isStoredInFileSystemUnc() throws RQLException {
		return isStoredInFileSystem() && getFileSystemPath().startsWith("\\\\");
	}

	/**
	 * Liefert true, falls die Inhalte dieses FileFolders oder AssetManagers innerhalb eines anderen RedDot Projektes gespeichert
	 * werden.
	 */
	public boolean isStoredInOtherProject() throws RQLException {
		return saveType == SAVE_TYPE_OTHER_PROJECT;
	}

	/**
	 * Liefert true, falls die Inhalte dieses FileFolders oder AssetManagers innerhalb der RedDot Projektdatenbank gespeichert werden.
	 */
	public boolean isStoredInternal() throws RQLException {
		return saveType == SAVE_TYPE_INTERNAL_DATABASE;
	}

	/**
	 * Liefert alle Files dieses Ordners, die dem pattern genügen. Achtung diese Liste kann sehr groß werden.
	 * 
	 * @param pattern
	 *            Teil des Names incl. Wildcards (*)
	 */
	public java.util.List<File> searchFiles(String pattern) throws RQLException {

		return searchFiles(pattern, "", null);
	}

	/**
	 * Returns the file from this folder with the given filename or null, if no file with given filename is found.
	 * <p>
	 * If you still use a wildcard, you will get the first file found.
	 * 
	 * @param filename
	 *            complete filename incl. extender of the file you are looking for, e.g. 9820_Umweltflyer_dt_rev.gif
	 */
	public File getFile(String filename) throws RQLException {

		List<File> files = searchFiles(filename);
		return files.size() > 0 ? files.get(0) : null;
	}

	/**
	 * Liefert alle Files dieses Ordners, deren extender gleich dem gegebenen ist. Achtung diese Liste kann sehr groß werden.
	 */
	public java.util.List<File> searchFilesByExtender(String extender) throws RQLException {

		return searchFiles("*", extender, null);
	}

	/**
	 * Liefert alle Files dieses Ordners, die dem pattern genügen und deren Extender in der Liste der suffixes ist.
	 * 
	 * @param pattern
	 *            Teil des Names incl. Wildcards (*)
	 * @param suffixes
	 *            zugelassene Extender aus dem FileElement
	 */
	public java.util.List<File> searchFiles(String pattern, String suffixes) throws RQLException {

		return searchFiles(pattern, suffixes, null);
	}

	/**
	 * Liefert alle Files dieses Ordners, die dem pattern genügen und deren Extender in der Liste der suffixes ist.
	 * 
	 * @param pattern
	 *            Teil des Names incl. Wildcards (*)
	 * @param suffixes
	 *            zugelassene Extender aus dem FileElement
	 * @param exceptionSuffix
	 *            Dateien mit disem Extender werden nicht zurückgegeben.
	 * @see <code>FileElement</code>
	 * @see <code>File</code>
	 */
	public java.util.List<File> searchFiles(String pattern, String suffixes, String exceptionSuffix) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="00B4D13475E645F1B0BC6AD013D95364">
		 <PROJECT sessionkey="351990199ra5D1w22S1S">
		 <TEMPLATE>
		 <ELEMENT folderguid="F3AE1D5712D54347B4764955CA657A67">
		 <FILES action="list" searchtext="3_" maxfilesize="0" pattern="gif;jpg;png" startcount="1" orderby="name"/>
		 </ELEMENT>
		 </TEMPLATE>
		 </PROJECT>
		 </IODATA>
		 V5 response (media catalog stored in database)
		 <IODATA>
		 <ELEMENT folderguid="F3AE1D5712D54347B4764955CA657A67" languagevariantid="ENG" dialoglanguageid="ENG" action="" parentguid="" parenttable="TPL">
		 <FILES startcount="1" overflow="0">
		 <FILE name="3_START_AND__DISTRIBUTION__OF_img1.gif" guid="9836F427CC1942298B10C18CEE54BF0B" date="37924.6611342593"/>
		 <FILE name="3_START_AND_DISTRIBUTION_OF_img2.gif" guid="854FD26AE38D4BDB82423B13107E76DA" date="37924.6696412037"/>
		 <FILE name="3_START_AND_DISTRIBUTION_OF_img3.gif" guid="AABC9DEF6134442F80CE2FD84B9F8BDC" date="37924.6757407407"/>
		 </FILES>
		 </ELEMENT>
		 </IODATA>
		 V5 file respone from a folder saved in file system
		 <FILE name="EH_10.11.03_filelist.xml" date="12/16/2003 8:53:17 AM"></FILE>
		 V6.5 file respone from a folder saved in file system
		 <FILE name="xls_fle_PM_NC_WEC_MINGDL_image004.gif" date="01.09.2005 12:11:35" folderguid="DB7234C07B6947EE8AACD6E271DDF32C" type="0" href="xls_fle_PM_NC_WEC_MINGDL_image004.gif"/>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "<PROJECT sessionkey='" + getSessionKey() + "'>"
				+ "<TEMPLATE>" + "<ELEMENT folderguid='" + folderGuid + "'>" + "<FILES action='list' searchtext='" + pattern;
		if (suffixes != null && suffixes.trim().length() > 0) {
			rqlRequest = rqlRequest + "' pattern='" + suffixes;
		}
		rqlRequest = rqlRequest + "' maxfilesize='0' startcount='1' orderby='name'/>" + "</ELEMENT>" + "</TEMPLATE>" + "</PROJECT>"
				+ "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNodeList fileNodeList = rqlResponse.getNodes("FILE");

		// prepare result list and check if empty
		java.util.List<File> files = new ArrayList<File>();
		if (fileNodeList == null) {
			return files;
		}

		// filter rql nodes and wrap
		for (int i = 0; i < fileNodeList.size(); i++) {
			RQLNode fileNode = fileNodeList.get(i);
			String filename = fileNode.getAttribute("name");
			if (exceptionSuffix != null && filename.substring(filename.lastIndexOf(".") + 1).equals(exceptionSuffix)) {
				continue;
			}
			files.add(new File(this, filename, fileNode.getAttribute("date")));
		}
		return files;
	}

	/**
	 * Liefert alle Files dieses Ordners, die dem pattern genügen. Achtung diese Liste kann sehr groß werden.
	 * 
	 * @param pattern
	 *            Teil des Names incl. Wildcards (*)
	 * @param templateElement
	 *            bestimmt als zusätzliche Einschränkung die möglichen Extender
	 */
	public java.util.List<File> searchFiles(String pattern, TemplateElement templateElement) throws RQLException {

		return searchFiles(pattern, templateElement.getFolderSuffixesStr(), null);
	}

	/**
	 * Liefert alle Files dieses Ordners, die dem pattern genügen. Achtung diese Liste kann sehr groß werden.
	 * 
	 * @param pattern
	 *            Teil des Names incl. Wildcards (*)
	 * @param templateElement
	 *            bestimmt als zusätzliche Einschränkung die möglichen Extender
	 * @param exceptionSuffix
	 *            Dateien mit disem Extender werden nicht zurückgegeben.
	 */
	public java.util.List<File> searchFiles(String pattern, TemplateElement templateElement, String exceptionSuffix)
			throws RQLException {

		return searchFiles(pattern, templateElement.getFolderSuffixesStr(), exceptionSuffix);
	}

	/**
	 * Refreshs the thumbnail and file attributes for the given file in this asset manager folder. Ignore this command on a normal file
	 * folder, but force it on an asset manager folder.
	 */
	public void refreshFileInformation(String filename) throws RQLException {
	}
	/**
	 * Refreshs the thumbnail and file attributes for the given file in this asset manager folder. Ignore this command on a normal file
	 * folder, but force it on an asset manager folder.
	 */
	public void refreshFileInformation(File file) throws RQLException {
	}
}
