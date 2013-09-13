package com.hlcl.rql.as;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Diese Klasse beschreibt ein File eines Folders.
 * 
 * @author LEJAFR
 */
public class File implements FolderContainer {

	private Folder folder;
	private String filename;
	private ReddotDate date;

	/**
	 * FileElement constructor comment.
	 * 
	 * @param folder
	 *            Folder in dem dieses File gespeichert ist
	 * @param filename
	 *            Dateiname dieses Files
	 * @param date
	 *            Datum als String wie von RedDot geliefert
	 * @see <code>parseDate</code>
	 */
	public File(Folder folder, String filename, String date) throws RQLException {

		this.folder = folder;
		this.filename = filename;
		this.date = parseDate(date);
	}

	/**
	 * FileElement constructor comment. Date is re-read if needed.
	 * 
	 * @param folder
	 *            Folder wherein this file is stored
	 * @param filename
	 *            filename of this file
	 */
	public File(Folder folder, String filename) throws RQLException {

		this.folder = folder;
		this.filename = filename;
		this.date = null;
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getCmsClient().callCms(rqlRequest);
	}

	/**
	 * Liefert den Dateinamen dieses Files.
	 * 
	 * @return String
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Liefert den Folder aus dem das File stammt.
	 */
	public Folder getFolder() {

		return folder;
	}

	/**
	 * Returns the folder name / filename of this file as an easy to display information.
	 */
	public String getFilenameWithFolder() {
		return getFolderName() + "/" + getFilename();
	}

	/**
	 * Returns the folder name / filename of this file as an easy to display information.
	 * 
	 * @see #getFilenameWithFolder()
	 */
	public String toString() {
		return getFilenameWithFolder();
	}

	/**
	 * Returns the folder name where this file is stored.
	 */
	public String getFolderName() {

		return getFolder().getName();
	}

	/**
	 * Liefert die RedDot GUID des Folders, aus dem das File kommt.
	 */
	public String getFolderGuid() {

		return getFolder().getFolderGuid();
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {
		return getFolder().getLogonGuid();
	}

	/**
	 * Liefert das Projekt.
	 */
	public Project getProject() {
		return getFolder().getProject();
	}

	/**
	 * Liefert die RedDot GUID des Projekts.
	 */
	public String getProjectGuid() throws RQLException {
		return getFolder().getProjectGuid();
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getFolder().getSessionKey();
	}

	/**
	 * Konvertiert den Datums-String dateStr in ein ReddotDate
	 * 
	 * @param dateStr
	 *            Format: "12/16/2003 8:53:17 AM" oder "37924.6757407407" oder "01.09.2005 12:11:35"
	 * @see <code>searchFiles</code>
	 */
	private ReddotDate parseDate(String dateTimeStr) throws RQLException {

		ReddotDate rdDate = null;
		if (dateTimeStr.indexOf(" ") >= 0) { // us or german style
			Locale locale = Locale.GERMAN;
			if (dateTimeStr.indexOf("AM") >= 0 || dateTimeStr.indexOf("PM") >= 0) {
				locale = Locale.US;
			}
			StringTokenizer t = new StringTokenizer(dateTimeStr, " ");
			String dateStr = t.nextToken();
			DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			try {
				rdDate = new ReddotDate(dateFormat.parse(dateStr));
			} catch (ParseException pe) {
				throw new RQLException(
						"A file object could not be created, because the date " + dateTimeStr + " could not be parsed.", pe);
			}
		} else { // ms number style
			rdDate = new ReddotDate(dateTimeStr);
		}

		return rdDate;
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines
	 * Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Returns the date of this file, lazy initialised.
	 * 
	 * @return com.hlcl.rql.as.ReddotDate
	 * @throws RQLException
	 */
	public ReddotDate getDate() throws RQLException {
		if (date == null) {
			date = getFolder().getFile(getFilename()).getDate();
		}
		return date;
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getFolder().getCmsClient();
	}

	/**
	 * Returns true, if this file can be renamed. This works if this file is stored in an folder which is stored on a network location.
	 * All usage references, if existing, are checked as well. Rename works only in main language variant, in other LVs not!
	 * 
	 * @throws RQLException
	 * 
	 * @see #isFolderStoredInFileSystemUnc()
	 */
	public boolean canBeRenamed() throws RQLException {
		// check folder storage first
		if (!isFolderStoredInFileSystemUnc()) {
			return false;
		}

		// only in main lv possible, in non main lvs the lv independent elements could not be retrieved!
		if (!getProject().isCurrentLanguageVariantMainLanguage()) {
			return false;
		}
		
		// check all usage references as well
		for (UsageReference ur : getUsageReferences()) {
			if (!ur.canBeRenamed()) {
				return false;
			}
		}
		// this file and all usage references can be renamed
		return true;
	}

	/**
	 * Returns true, if this file was renamed. This works if this file is stored in an folder which is stored on a network location.
	 * All usage references, if existing, are renamed as well. Therefore you might get a bunch of pages in several language variants
	 * into draft state. If a template element default file needs to be updated, you need the template editor right to succeed.
	 * 
	 * @throws RQLException
	 * 
	 * @see #isFolderStoredInFileSystemUnc()
	 * @see #canBeRenamed()
	 */
	public boolean rename(String newFilename) throws RQLException {
		if (!canBeRenamed()) {
			return false;
		}
		// remember usage refs of old file
		List<UsageReference> oldUsageRefs = getUsageReferences();

		// rename in file system and refresh attributes
		String oldFilename = getFilename();
		java.io.File file = new java.io.File(getFileSystemPath());
		String folderFileSystemPath = getFolderFileSystemPath();
		String newPath = folderFileSystemPath + newFilename;
		boolean success = file.renameTo(new java.io.File(newPath));
		// update local cache and refresh file attributes
		this.filename = newFilename;
		getFolder().refreshFileInformation(this);
		if (!success) {
			// if refs are existing, these are not corrected
			if (oldUsageRefs.size() > 0) {
				throw new RQLException("Renaming of file from " + oldFilename + " to " + newFilename + " in folder storage "
						+ folderFileSystemPath + " not successfully finished. " + oldUsageRefs.size()
						+ " usage references not updated, please correct possible inconsistencies manually.");
			}
			return false;
		}

		// propagate renaming to old file refs
		for (UsageReference ur : oldUsageRefs) {
			ur.renameTo(newFilename);
		}
		return true;
	}

	/**
	 * Liefert true, falls dieses file im Dateisystem über einen UNC Pfad aus dem Netzwerk erreichbar ist. An UNC path is starting
	 * always with \\.
	 * 
	 * @see Folder#isStoredInFileSystemUnc()
	 */
	public boolean isFolderStoredInFileSystemUnc() throws RQLException {
		return getFolder().isStoredInFileSystemUnc();
	}

	/**
	 * Returns the file system path under which this file is stored. It combines the folder file system path with the filename of this
	 * file. This could be a UNC like \\kswfrd03\cms_data\content\hlag.com\images\all_areas9009_Organigramm_Englisch.gif or local path
	 * like d:\... depending on your folder settings.
	 * 
	 * @see Folder#getFileSystemPath()
	 * @throws WrongStorageTypeException
	 *             if the folder is not stored in file system
	 */
	public String getFileSystemPath() throws RQLException {
		return getFolderFileSystemPath() + getFilename();
	}

	/**
	 * Returns this file folder's file system path.
	 * 
	 * @see Folder#getFileSystemPath()
	 * @throws WrongStorageTypeException
	 *             if the folder is not stored in file system
	 */
	public String getFolderFileSystemPath() throws RQLException {
		return getFolder().getFileSystemPath();
	}

	/**
	 * Returns all referencing elements using this file. Returns an empty list, if file is not used at all. Could be template elements,
	 * page image elements or page media elements.
	 * <p>
	 * Attention: References from other projects are just ignored. Works only in main language variant, because in other LVs the lv
	 * independent file elements could not be retrieved.
	 */
	public java.util.List<UsageReference> getUsageReferences() throws RQLException {

		/* 
		 V9 request
		<IODATA loginguid="5CF390551B254C8F9733891B3E0E4EA2" sessionkey="AA535441E776427C926FF4339586B534">
		<PROJECT sessionkey="AA535441E776427C926FF4339586B534">
		<TRANSFER action="checkimage" getreferences="1" sync="1" folderguid="7410F0AFB7654B9D9ADCEC2A0AA73CC9" filename="Dias_libres_y_manifiestos_de_impo-expo_4th_quater(4).pdf"/>
		</PROJECT>
		</IODATA>
		 V9 response page image and media elements
		 <IODATA>
		<REFERENCES>
		<REFERENCE id="17952" name="Dias libres y manifiestos de impo - expo 4th. quater" imagetype="page" treetype="page" typename="Page" guid="C47DFEDEF6614507A9AC0F0C0AA83159">
		<CHILDREFERENCE name="language_independent_pdf_media" imagetype="TreeType38" treetype="element" typename="Media" guid="2EE554CD7D4540F988647751A57B555F" languagevariantids="ENG, ESN, CHS, DEU"/>
		</REFERENCE>
		</REFERENCES>
		</IODATA>
		 V9 response content class elements
		<IODATA>
		<REFERENCES>
		<REFERENCE name="olb_page" foldername="templates_olb" folderguid="C0D4AD9E5AD247F1ACE4AEE9EB47D917" imagetype="contentclass" treetype="app.4015" typename="Content class" guid="0B496BBEF7764906A7286C0757415F05">
		 <CHILDREFERENCE name="bookmark_current" imagetype="TreeType2" treetype="project.4148" typename="Image" guid="2B0C70A512CA48CF8F8AEA58A385A80F" languagevariantids="ENG, ESN, CHS, DEU"/></REFERENCE>
		 <REFERENCE name="olb_my_account_page" foldername="templates_olb" folderguid="C0D4AD9E5AD247F1ACE4AEE9EB47D917" imagetype="contentclass" treetype="app.4015" typename="Content class" guid="E37529F196444079B2C763EDDD23C510">
		  <CHILDREFERENCE name="bookmark_current" imagetype="TreeType2" treetype="project.4148" typename="Image" guid="58023BA934FD4C28AF74CD6559D36EF3" languagevariantids="ENG, ESN, CHS, DEU"/></REFERENCE>
		  ...
		 */

		// call CMS
		java.util.List<UsageReference> result = new ArrayList<UsageReference>();
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
				+ "<TRANSFER action='checkimage' getreferences='1' sync='1' folderguid='" + getFolderGuid() + "' filename='"
				+ getFilename() + "'/>" + "</PROJECT></IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNodeList refNodes = rqlResponse.getNodes("REFERENCE");
		// file not used at all
		if (refNodes == null) {
			return result;
		}
		// wrap result
		for (int i = 0; i < refNodes.size(); i++) {
			RQLNode refNode = refNodes.get(i);
			// get values
			String treetype = refNode.getAttribute("treetype");
			String refGuid = refNode.getAttribute("guid");
			RQLNodeList childNodes = refNode.getChildren();
			// check by parent type
			if (treetype.equals(UsageReference.PARENT_TREETYPE_CONTENTCLASS)) {
				// for all template elements
				for (int j = 0; j < childNodes.size(); j++) {
					RQLNode childNode = childNodes.get(j);
					String elemName = childNode.getAttribute("name");
					Template t = getProject().getTemplateByGuid(refGuid);
					TemplateElement templateElement = t.getTemplateElementByName(elemName);
					result.add(new TemplateElementUsageReference(this, childNode.getAttribute("languagevariantids"), templateElement));
				}
			} else if (treetype.equals(UsageReference.PARENT_TREETYPE_PAGE)) {
				// treat as page
				// for all page elements
				for (int j = 0; j < childNodes.size(); j++) {
					RQLNode childNode = childNodes.get(j);
					String elemName = childNode.getAttribute("name");
					Page page = getProject().getPageByGuid(refGuid);
					TemplateElement templateElement = page.getTemplate().getTemplateElementByName(elemName);
					// image element
					if (templateElement.isImage()) {
						result.add(new PageImageElementUsageReference(this, childNode.getAttribute("languagevariantids"), page
								.getImageElement(elemName)));
						continue;
					}
					// media element
					if (templateElement.isMedia()) {
						result.add(new PageMediaElementUsageReference(this, childNode.getAttribute("languagevariantids"), page
								.getMediaElement(elemName)));
						continue;
					}
					// unsupported page element type
					throw new WrongTypeException("Unsupported page element type for file usage reference on page "
							+ page.getInfoText() + " on element with name " + elemName + " of type " + templateElement.getTypeName()
							+ ". Please contact jRQL vendor.");
				}
			}
		}
		return result;
	}

	/**
	 * Refreshs the thumbnail and file attributes for this file in it's folder (only on asset manager folders, ignored on file
	 * folders). Can be called on downloads, e.g. *.pdf as well.
	 */
	public void refreshThumbnailAndFileInformation() throws RQLException {
		getFolder().refreshFileInformation(this);
	}

}
