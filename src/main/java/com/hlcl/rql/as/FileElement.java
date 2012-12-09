package com.hlcl.rql.as;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;

/**
 * Diese Klasse beschreibt ein File-Element (Media oder Image) einer Seite.
 * 
 * @author LEJAFR
 */
public abstract class FileElement extends Element implements FolderContainer {
	private Folder folder;

	private String folderGuid;

	/**
	 * FileElement constructor comment.
	 * 
	 * @param page
	 *            Seite, die diesen Container Link beinhaltet.
	 * @param templateElement
	 *            TemplateElement auf dem dieses Element basiert
	 * @param name
	 *            Name des Elements
	 * @param elementGuid
	 *            GUID dieses Elements
	 * @param value
	 *            Dateiname des Files
	 */
	public FileElement(Page page, TemplateElement templateElement, String name, String elementGuid, String value, String folderGuid) {
		super(page, templateElement, name, elementGuid, value);

		this.folderGuid = folderGuid;
	}

	/**
	 * Optionale Prüfmöglichkeit für Subklassen.
	 */
	protected void checkBeforeChange(String filename) throws RQLException {
		filename.toString();
	}

	/**
	 * Konvertiert ein Objekt mit dem Value dieses Elements in einen String.
	 * 
	 * @param valuObj
	 *            valueObj Objekt passend zum Elementtyp
	 */
	String convertToStringValue(Object valueObj) {
		return (String) valueObj;
	}

	/**
	 * Liefert die URL, um ein Asset aus dem CMS ImageCache herunterzuladen (parameter downloadUrl in rql_fw.properties).
	 * 
	 * @param runsOnServer
	 *            =true, if used from webapp or batch on CMS server; the domain name will be replace with localhost
	 *            <p>
	 *            =false, if used from any other client; the configured URL will be used unchanged
	 */
	public URL getDownloadUrl(boolean runsOnServer) throws RQLException {

		// get url master
		String downloadUrl = getCmsClient().getFileDownloadUrlPattern();

		// build parm array
		String filename = getFilename();
		Object[] parms = new Object[4];
		parms[0] = getProjectGuid();
		parms[1] = getFolderGuid();
		parms[2] = filename.substring(0, 2);
		parms[3] = filename;

		URL result = null;
		try {
			result = new URL(MessageFormat.format(downloadUrl, parms));

		} catch (MalformedURLException mue) {
			throw new DownloadException("Download URL is malformed.", mue);
		}
		// patch localhost
		if (runsOnServer) {
			String localhostUrl = StringHelper.replace(downloadUrl, result.getHost(), "localhost");
			try {
				result = new URL(MessageFormat.format(localhostUrl, parms));
			} catch (MalformedURLException ex) {
				throw new DownloadException("Download URL for localhost is malformed.", ex);
			}
		}
		return result;
	}

	/**
	 * Liefert die download URL als String dieses File-Elements.
	 * 
	 * @param runsOnServer
	 *            =true, if used from webapp or batch on CMS server; the domain name will be replace with localhost
	 *            <p>
	 *            =false, if used from any other client; the configured URL will be used unchanged
	 */
	public String getDownloadUrlString(boolean runsOnServer) throws RQLException {

		return getDownloadUrl(runsOnServer).toString();
	}

	/**
	 * Liefert den Extender (immer lowercase) des gegebenen Files zurück.
	 */
	public String getExtender(String filename) {

		return (filename.substring(filename.lastIndexOf(".") + 1)).toLowerCase();
	}

	/**
	 * Liefert den Extender (immer lowercase) dieses MediaElements zurück.
	 * <p>
	 * Liefert null, fall keine Datei gesetzt ist.
	 */
	public String getFilenameExtender() throws RQLException {
		String name = getFilename();
		if (name == null) {
			return null;
		}

		return getExtender(getFilename());
	}

	/**
	 * Liefert den Dateinamen dieses Files oder null, falls dieses Element kein Dateinamen besitzt und auch kein Defaultwert im
	 * Templateelement definiert ist.
	 * 
	 * @return String
	 */
	public String getFilename() throws RQLException {
		return super.getValue();
	}

	/**
	 * Returns the current value of this file element as file object or null, if no filename is set.
	 */
	public com.hlcl.rql.as.File getFile() throws RQLException {
		return hasFile() ? new com.hlcl.rql.as.File(getFolder(), getFilename()) : null;
	}

	/**
	 * Liefert nur den Dateinamen dieses Files; ohne Extender. Liefert null, falls kein '.' im Dateinamen gefunden werden konnte oder
	 * kein Dateiname gesetzt ist.
	 */
	public String getFilenameWithoutExtender() throws RQLException {
		String name = getFilename();
		if (name == null) {
			return null;
		}
		int pos = name.lastIndexOf(".");
		// skip invalid names
		if (pos <= 0) {
			return null;
		}
		return name.substring(0, pos);
	}

	/**
	 * Liefert den Folder dieses Dateielements.
	 * <p>
	 * Ist ein file gesetzt wird der Ordner oder auch AssetManager-Unterordner geliefert, aus dem das file zugewiesen wurde.
	 * <p>
	 * Ist kein file gesetzt wird der default Ordner aus dem template element geliefert.
	 */
	public Folder getFolder() throws RQLException {

		if (folder == null) {
			// return template folder if element is empty
			Folder templateFolder = getTemplateElementFolder();
			String filename = getFilename();
			if (filename == null) {
				folder = templateFolder;
				return folder;
			}
			// if folder guid is not equal the template folder guid, treat as an asset manager subfolder guid
			if (!getFolderGuid().startsWith("#") && !templateFolder.getFolderGuid().equals(getFolderGuid())) {
				if (!templateFolder.isAssetManagerFolder()) {
					throw new WrongTypeException(
							"Folder of file element "
									+ getName()
									+ " on page "
									+ getPage().getHeadlineAndId()
									+ " cannot be retrieved, because this file element seems to have set a file from a subfolder, but the template folder "
									+ templateFolder.getName() + " is not an asset manager at all.");
				}
				// check if sub folder or incorrect folder guid
				AssetManagerFolder templateAssetMgr = (AssetManagerFolder) templateFolder;
				if (templateAssetMgr.containsSubFolderByGuid(getFolderGuid())) {
					// get the folder guid via element again to correct folderguid bug
					String newFolderGuid = readFolderGuid();
					if (templateFolder.getFolderGuid().equals(newFolderGuid)) {
						folder = templateFolder;
					} else {
						// re-read folder guid is something different
						throw new RQLException("Impossible to find a folder for element's folder guid " + getFolderGuid()
								+ " and even re-read folder guid " + newFolderGuid + " in element " + getName() + " in page "
								+ getPage().getHeadlineAndId() + ".");
					}
				} else {
					// the folder guid has to be a subfolder of the template folder!
					folder = templateAssetMgr.getSubFolderByGuid(getFolderGuid());
				}
			} else {
				// use the template folder, because the GUIDs are the same
				folder = templateFolder;
			}
		}
		return folder;
	}

	/**
	 * Liefert die RedDot GUID des Folders, aus dem das File kommt. Kann sowohl ein AssetManager Ordner oder auch ein Unterordner sein.
	 */
	public String getFolderGuid() {

		return folderGuid;
	}

	/**
	 * Liefert den im Template-Element eingestellten Folder für dieses FileElement.
	 */
	public Folder getTemplateElementFolder() throws RQLException {
		return getTemplateElement().getFolder();
	}

	/**
	 * Liefert true, falls dieses file element eine Datei hat, sonst false;
	 */
	public boolean hasFile() throws RQLException {
		return getFilename() != null;
	}

	/**
	 * Liefert false, da alle FileElemente die gemeinsame Änderung mit nur einem RQL nicht unterstützen.
	 * <p>
	 * 
	 * @see Page#setElementValues(Map)
	 */
	public boolean isCombinedUpdateSupported() throws RQLException {
		return false;
	}

	/**
	 * Aktualisiert das Thumbnail und die Dateiinformationen dieses Files in einem AssetManager.
	 */
	private void refreshAssetManagerFileInformation(String filename) throws RQLException {
		getFolder().refreshFileInformation(filename);
	}

	/**
	 * Change the filename of this image or media element in all given language variants. The current language variant is preserved.
	 * 
	 * @throws MissingFileException
	 * @throws UpdateOnlyInMainLanguageVariantAllowedException 
	 *             if a language variant independent element is tried to update in another as the main lv
	 * @see #setFilename(String)
	 */
	public void setFilename(String filename, java.util.List<LanguageVariant> languageVariants) throws RQLException {
		Project project = getProject();
		LanguageVariant currentLv = project.getCurrentLanguageVariant();

		// other than the main lv requested?
		java.util.List<LanguageVariant> tempLvs = new ArrayList<LanguageVariant>();
		tempLvs.remove(project.getMainLanguageVariant());
		try {
			// check for wrongly requested update
			if (isTemplateElementLanguageVariantIndependent() && tempLvs.size() > 0) {
				throw new UpdateOnlyInMainLanguageVariantAllowedException(
						"Try to update a language variant independent image or media element in a non main language variant. This operation is not supported. Please correct your code to only update such an element from within the main language variant. ");
			} else {
				// update an lv dependent element
				for (LanguageVariant lv : languageVariants) {
					project.setCurrentLanguageVariant(lv);
					setFilename(filename);
				}
			}
		} finally {
			// restore language variant
			project.setCurrentLanguageVariant(currentLv);
		}
	}

	/**
	 * Aendert den Namen des zugewiesenen Files. Es wird geprüft, ob das File im Ordner des Templateelements existiert.
	 * 
	 * @throws MissingFileException
	 * @see #setFilename(String, AssetManagerSubFolder)
	 */
	public void setFilename(String filename) throws RQLException {
		// use main folder set in template elemtent to start with check
		// asset manager sub folder are handled
		Folder templateFolder = getTemplateElementFolder();
		if (!templateFolder.exists(filename)) {
			throw new MissingFileException("The file with filename " + filename + " does not exists in folder "
					+ templateFolder.getName() + ".");
		}
		// change
		setFilenameWithoutCheck(filename);
	}

	/**
	 * Aendert den Namen des zugewiesenen Files aus einem AssetManager-Unterordner. Es wird geprüft, ob das File im gegebenen
	 * Unterordner existiert.
	 * 
	 * @throws MissingFileException
	 * @throws WrongTypeException
	 * @see #setFilename(String)
	 * @see #setFilename(String, String)
	 */
	public void setFilename(String filename, AssetManagerSubFolder subFolder) throws RQLException {
		// check if the given subfolder matches the template element setup
		Folder templateFolder = getTemplateElementFolder();
		if (!templateFolder.isAssetManagerFolder()) {
			throw new WrongTypeException("You cannot set a file from an asset manager subfolder to this file element with name "
					+ getName() + " on page " + getPage().getHeadlineAndId() + ", because the template folder "
					+ templateFolder.getName() + " for this element is not an asset manager at all.");
		}
		// check if given subfolder is child of template folder
		AssetManagerFolder templateAssetMgr = (AssetManagerFolder) templateFolder;
		if (!templateAssetMgr.equals(subFolder.getParentAssetManagerFolder())) {
			throw new WrongTypeException("The given subfolder " + subFolder.getName()
					+ " is not a child of the template asset manager " + templateAssetMgr.getName()
					+ ". So you cannot set a file from this subfolder to this element " + getName() + " on page "
					+ getPage().getHeadlineAndId() + ".");
		}

		// check if given file is in given subfolder
		if (!subFolder.exists(filename)) {
			throw new MissingFileException("The file with filename " + filename + " does not exists in subfolder "
					+ subFolder.getName() + ".");
		}

		// provide hook for subclasses
		checkBeforeChange(filename);

		// save filename and subfolder guid
		setValue(filename, subFolder);

		// set image cache update request
		updateImageCache();

		// refresh asset manager subfolder's thumbnail and file attributes
		refreshAssetManagerFileInformation(filename);
	}

	/**
	 * Aendert den Namen des zugewiesenen Files aus einem AssetManager-Unterordner. Es wird geprüft, ob das File im gegebenen
	 * Unterordner existiert.
	 * 
	 * @throws MissingFileException
	 * @throws WrongTypeException
	 * @see #setFilename(String, AssetManagerSubFolder)
	 * @see #setFilename(String)
	 */
	public void setFilename(String filename, String subFolderName) throws RQLException {
		// fail is template folder is not an asset manager (only them can have subfolders)
		Folder templateFolder = getTemplateElementFolder();
		if (!templateFolder.isAssetManagerFolder()) {
			throw new WrongTypeException("You cannot set a file from an asset manager subfolder to this file element with name "
					+ getName() + " on page " + getPage().getHeadlineAndId() + ", because the template folder "
					+ templateFolder.getName() + " for this element is not an asset manager at all.");
		}
		// cast and forward
		AssetManagerFolder templateAssetMgr = (AssetManagerFolder) templateFolder;
		setFilename(filename, templateAssetMgr.getSubFolderByName(subFolderName));
	}

	/**
	 * Aendert den Namen des zugewiesenen Files. Es wird NICHT geprüft, ob das File im Templateornder existiert, um die Performance zu
	 * steigern. Sprachvariantenunabhängige Elemente übertragen die Werte erst bei submit to workflow.
	 * 
	 * @throws MissingFileException
	 * @see #setFilename(String)
	 */
	public void setFilenameWithoutCheck(String filename) throws RQLException {
		// provide hook for subclasses
		checkBeforeChange(filename);

		// save filename
		super.setValue(filename);

		// set image cache update request
		updateImageCache();

		// if in asset manager folder refresh thumbnail and file attributes
		if (getFolder().isAssetManagerFolder()) {
			refreshAssetManagerFileInformation(filename);
		}
	}

	/**
	 * Changes the folder guid and force a new retrieval of the folder.
	 */
	private void setFolderGuid(String folderGuid) {
		this.folderGuid = folderGuid;
		this.folder = null; // force new retrieval
	}

	/**
	 * Returns the folder guid read by separate RQL command ELT action=load.
	 * 
	 * @throws RQLException
	 */
	private String readFolderGuid() throws RQLException {
		return readElementNode().getAttribute("folderguid");
	}

	/**
	 * Aendert polymorph den Wert dieses Elements. Der Typ von valuObj muss zum Typ des Elementes passen.
	 * 
	 * @param valueObj
	 *            valueObj muss ein String mit dem Dateinamen sein
	 */
	protected void setValue(Object valueObj) throws RQLException {

		setFilename(convertToStringValue(valueObj));
	}

	/**
	 * Aendert den Wert dieses Fileelements, wenn es aus einem Unterordner stammt.
	 * 
	 * @see Element#setValue(String)
	 */
	protected void setValue(String filename, AssetManagerSubFolder subFolder) throws RQLException {
		/* 
		 V7.5 request
		 <IODATA loginguid="254803B361D74CABB2077D90392BBFAC" sessionkey="FD04927A897D4F1AB9E76A39E73BB34F">
		 <ELT action="save" reddotcacheguid="" guid="CE17781A1B974A95BE1D808714299D06" value="2_Average_Salaries.pdf" subdirguid="1B0280BBF0E64E70AF7D1D5575E52510" extendedinfo="">
		 </ELT></IODATA>
		 V7.5 response
		 <IODATA>
		 <ELT action="save" reddotcacheguid="" value="2_Average_Salaries.pdf" extendedinfo="" sessionkey="FD04927A897D4F1AB9E76A39E73BB34F" dialoglanguageid="ENG" languagevariantid="ENG" defaultlanguagevariantid="ENG" tleflags="64" eltconvertmode="NO" eltconvert="0" guid="CE17781A1B974A95BE1D808714299D06" type="38" pageguid="1485D7978E8A42AF80E022151C4E8ED8" available="1" pagestatuschanged="1" changed="0" saveauthorization="-1">
		 </ELT></IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "  <ELEMENTS action='save'>" + "      <ELT guid='" + getElementGuid() + "' value='"
				+ StringHelper.escapeHTML(filename) + "' subdirguid='" + subFolder.getFolderGuid() + "'/>" + "  </ELEMENTS>"
				+ "</IODATA>";
		callCms(rqlRequest);

		// change instance vars too
		this.value = filename;
		setFolderGuid(subFolder.getFolderGuid());
	}

	/**
	 * Setzt eine Anforderung in der session, dass der ImageCache für dieses File angepasst werden soll. Das eigentliche Update des
	 * ImageCaches wie über die URL http://reddot.hlcl.com/cms/ImageCache/* erfolgt verzögert bei Anzeige einer Seite im SmartEdit.
	 */
	private void updateImageCache() throws RQLException {
		/* first step; delete file from image cache
		 V5 request
		 <IODATA loginguid="FA8F77B3DF6845A88E13055751FE5B7A" sessionkey="3519994622X20qFhD2s4">
		 <IMAGECACHE action="execute" projectguid="06BE79A1D9F549388F06F6B649E27152">
		 <IMAGE action="delete" folderguid="63D8B1B73E854B4AA6A2041784C41C9C" filetitle="EH_10.11.03.zip"/>
		 </IMAGECACHE>
		 </IODATA>
		 V5 response
		 <IMAGECACHE action="execute" projectguid="06BE79A1D9F549388F06F6B649E27152">
		 <IMAGE action="delete" folderguid="63D8B1B73E854B4AA6A2041784C41C9C" filetitle="EH_10.11.03.zip"/>
		 </IMAGECACHE>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "  <IMAGECACHE action='execute' projectguid='" + getProjectGuid() + "'>"
				+ "      <IMAGE action='delete' folderguid='" + getFolderGuid() + "' filetitle='" + getFilename() + "'/>"
				+ "  </IMAGECACHE>" + "</IODATA>";
		callCms(rqlRequest);

		/* second step
		 add image to cache transfer request; delayed until the page is accessed next time!
		 V5 request
		 <IODATA loginguid="FA8F77B3DF6845A88E13055751FE5B7A" sessionkey="3519994622X20qFhD2s4">
		 <IMAGECACHE action="transfer" projectguid="06BE79A1D9F549388F06F6B649E27152">
		 <IMAGE action="delete" folderguid="63D8B1B73E854B4AA6A2041784C41C9C" filetitle="EH_10.11.03.zip"/>
		 </IMAGECACHE>
		 </IODATA>
		 V5 response
		 <IMAGECACHE action="transfer" projectguid="06BE79A1D9F549388F06F6B649E27152">
		 <IMAGE action="delete" folderguid="63D8B1B73E854B4AA6A2041784C41C9C" filetitle="EH_10.11.03.zip"/>
		 </IMAGECACHE>
		 */

		// call CMS
		rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "  <IMAGECACHE action='transfer' projectguid='" + getProjectGuid() + "'>"
				+ "      <IMAGE action='delete' folderguid='" + getFolderGuid() + "' filetitle='" + getFilename() + "'/>"
				+ "  </IMAGECACHE>" + "</IODATA>";
		callCms(rqlRequest);
	}

	/**
	 * Lädt für dieses FileElement die Datei aus dem RD ImageCache und speichert sie in der gegebenen Datei targetFile ab.
	 * <p>
	 * Achtung: Die Extender sollten zueinander passen! Sie werden hierbei nicht geprüft.
	 * 
	 * @param runsOnServer
	 *            =true, if used from webapp or batch on CMS server; the domain name will be replace with localhost
	 *            <p>
	 *            =false, if used from any other client; the configured URL will be used unchanged
	 */
	public void downloadToFile(java.io.File targetFile, boolean runsOnServer) throws RQLException {

		URL url = getDownloadUrl(runsOnServer);
		try {
			FileUtils.copyURLToFile(url, targetFile);
		} catch (FileNotFoundException fnfe) {
			// file maybe not in image cache
			// preview the page to fill the cache and try again
			getPage().simulateSmartEditUsage();
			// try download again
			try {
				FileUtils.copyURLToFile(url, targetFile);
			} catch (IOException ioe) {
				throw new DownloadException("Download of file " + url.toString() + " does not work on element " + getName()
						+ " on page " + getPage().getHeadlineAndId() + " even after a simulated page preview.", ioe);
			}
		} catch (IOException ioe) {
			throw new DownloadException("Download of file " + url.toString() + " does not work on element " + getName() + " on page "
					+ getPage().getHeadlineAndId() + ".", ioe);
		}
	}

	/**
	 * Lädt für dieses FileElement die Datei aus dem RD ImageCache und speichert sie unter dem gegebenen Pfad ab.
	 * <p>
	 * Achtung: Die Extender sollten zueinander passen! Sie werden hierbei nicht geprüft.
	 * 
	 * @param runsOnServer
	 *            =true, if used from webapp or batch on CMS server; the domain name will be replace with localhost
	 *            <p>
	 *            =false, if used from any other client; the configured URL will be used unchanged
	 */
	public void downloadToFile(String targetPathName, boolean runsOnServer) throws RQLException {

		try {
			File targetFile = new File(targetPathName);
			targetFile.createNewFile();
			downloadToFile(targetFile, runsOnServer);
		} catch (IOException ioe) {
			throw new DownloadException("A new file under path " + targetPathName + " could not be created.", ioe);
		}
	}

	/**
	 * Schreibt den aktuellen Bildnamen dieses Dateielements neu und aktualisiert dabei den neuen templatefolder in diesem Element.
	 */
	public void updateFolderChange() throws RQLException {
		/* 
		 V9 request
		<IODATA loginguid="7D45996D34B9448EBF429336F5D1157D" sessionkey="7F7D8BC07E334ABFB68AE57A894B05A5">
		<ELT action="save" reddotcacheguid="" guid="DE159F58527D498A87C3091A1857787C" value="BAF_NAOC_Dec15_2009.pdf" 
		subdirguid="E3FAC52B4C0A4D89840F47305833850A" extendedinfo="">
		</ELT>
		</IODATA>
		 V9 response
		 <IODATA>9A872444A0034A428EE1214C0BFC6EEF</IODATA>
		 */

		// call CMS
		if (!isEmpty()) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ "<ELT action='save' guid='" + getElementGuid() + "' value='" + StringHelper.escapeHTML(getFilename())
					+ "' subdirguid='" + getTemplateElementFolder().getFolderGuid() + "' >" + "  </ELT>" + "</IODATA>";
			callCmsWithoutParsing(rqlRequest);
		}
	}

	/**
	 * Returns the filename as content element's value. Return an empty string, if no filename is set.
	 * 
	 * @see #getFilename()
	 */
	public String getValueAsString() throws RQLException {
		String fn = getFilename();
		return fn == null ? "" : fn;
	}

	/**
	 * Liefert true, falls die Inhalte des Folders des Templateelements im Dateisystem über einen UNC Pfad gespeichert werden, also aus
	 * dem Netzwerk erreichbar sind. An UNC path is starting always with \\.
	 */
	public boolean isTemplateElementFolderStoredInFileSystemUnc() throws RQLException {
		return getTemplateElement().isFolderStoredInFileSystemUnc();
	}

}
