package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Diese Klasse beschreibt ein zugewiesenes Exportsetting eines Exportpaketes (Publication Combination).
 * 
 * @author LEJAFR
 */
public class PublicationSetting implements PublicationPackageContainer, ProjectVariantContainer, LanguageVariantContainer {

	private PublicationPackage publicationPackage;
	private String publicationSettingGuid;
	private PublicationFolder publishedPagesPublicationFolder;
	private ProjectVariant projectVariant;
	private LanguageVariant languageVariant;

	private RQLNodeList folderMappingsCache;

	/**
	 * Erzeugt eine Kombination des gegebenen Exportpakets.
	 * <p>
	 * Kombinationen für (Draft) Projektvarianten werden nicht unterstützt!
	 * 
	 * @param publicationPackage
	 *            Exportpaket zu dem dieses Exportsetting gehört
	 * @param publicationSettingGuid
	 *            GUID dieses Exportsettings
	 * @param publishedPagesPublicationFolder
	 *            publication folder der publishing structure; =null signalisiert publishing into root
	 * @param projectVariantGuid
	 *            GUID der Projektvariante dieses Exportsettings
	 * @param languageVariantGuid
	 *            GUID der Sprachvariante dieses Exportsettings
	 */
	public PublicationSetting(PublicationPackage publicationPackage, String publicationSettingGuid,
			PublicationFolder publishedPagesPublicationFolder, String projectVariantGuid, String languageVariantGuid)
			throws RQLException {
		super();

		this.publicationPackage = publicationPackage;
		this.publicationSettingGuid = publicationSettingGuid;
		this.publishedPagesPublicationFolder = publishedPagesPublicationFolder;
		projectVariant = getProject().getProjectVariantByGuid(projectVariantGuid);
		languageVariant = getProject().getLanguageVariantByGuid(languageVariantGuid);
	}

	/**
	 * Fügt diesem Exportsetting das gegebene Exportziel target hinzu.
	 */
	public void addPublishingTo(PublishingTarget target) throws RQLException {

		changePublishingTarget(target, 1);
	}

	/**
	 * Sendet eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getPublicationPackage().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines
	 * Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Ändert nur genau das gegebene Exportziel dieses Exportsettings. Andere zugewiesene Exportziele müssen als Tag nicht im save
	 * vorkommen!
	 * 
	 * @param selected =
	 *            0 Exportziel wird entfernt
	 *            <p> = 1 Exportziel wird zugeordnet
	 * @param target
	 *            Exportziel
	 */
	private void changePublishingTarget(PublishingTarget target, int selected) throws RQLException {

		/*
		<IODATA loginguid="A655197692CD46CD98E16F8D0FCA4D56" sessionkey="571682219Ok2u8wyh616">
		  <PROJECT>
		    <EXPORTSETTING guid="FBF0D815B03C4868BD1C3BA15915C412">
		      <EXPORTTARGETS action="save">
		        <EXPORTTARGET guid="CF15C90ABC564CFCA282CD969BF99E72" selected="0" />
		      </EXPORTTARGETS>
		    </EXPORTSETTING>
		  </PROJECT>
		</IODATA>
		V5 response
		<IODATA>
		</IODATA>
		*/

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
				+ "   <EXPORTSETTING guid='" + getPublicationSettingGuid() + "'>" + "    <EXPORTTARGETS action='save'>"
				+ "        <EXPORTTARGET guid='" + target.getPublishingTargetGuid() + "' selected='" + selected + "'/>"
				+ "    </EXPORTTARGETS>" + "   </EXPORTSETTING>" + "  </PROJECT>" + "</IODATA>";
		callCmsWithoutParsing(rqlRequest);

		// refresh cache in package
		getPublicationPackage().resetPublicationSettingsCache();
	}

	/**
	 * Deletes this publication setting from the publication package.
	 */
	public void delete() throws RQLException {
		/*
		V7.5 request
		<IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
		  <PROJECT>
		    <EXPORTSETTING action="deletesetting" guid="[!guid_exportsetting!]" 
		     packetguid="[!guid_exportpacket!]"/>
		  </PROJECT>
		</IODATA>
		V7.5 response
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'><PROJECT>"
				+ " <EXPORTSETTING action='deletesetting' guid='" + getPublicationSettingGuid() + "' packetguid='"
				+ getPublicationPackageGuid() + "' />" + "</PROJECT></IODATA>";
		callCmsWithoutParsing(rqlRequest);

		// make this object unusable
		invalidate();
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getPublicationPackage().getCmsClient();
	}

	/**
	 * Liefert alle zugeordneten FTP und SFTP Exportziele für dieses Exportsetting.
	 */
	public java.util.List<PublishingTarget> getFtpPublishingTargets() throws RQLException {
		java.util.List<PublishingTarget> result = new ArrayList<PublishingTarget>();
		for (PublishingTarget publishingTarget : getPublishingTargets()) {
			if (publishingTarget.isFtpTarget()) {
				result.add(publishingTarget);
			}
		}
		return result;
	}

	/**
	 * Liefert true, falls diese Kombination auf das gegebene Target find publiziert, sonst false.
	 * <p>
	 * Die Art des Publizierungszieles (directory, ftp) spielt dabei keine Rolle.
	 */
	public boolean hasPublishingTo(PublishingTarget find) throws RQLException {
		for (PublishingTarget publishingTarget : getPublishingTargets()) {
			if (publishingTarget.equals(find)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Liefert alle zugeordneten Verzeichnis-Publizierungsziele für dieses Exportsetting.
	 */
	public java.util.List<PublishingTarget> getDirectoryPublishingTargets() throws RQLException {
		java.util.List<PublishingTarget> result = new ArrayList<PublishingTarget>();
		for (PublishingTarget publishingTarget : getPublishingTargets()) {
			if (publishingTarget.isDirectoryTarget()) {
				result.add(publishingTarget);
			}
		}
		return result;
	}

	/**
	 * Liefert für alle zugeordneten FTP Exportziele dieses Exportsettings den FTP path; immer ohne / am Ende.
	 * <p>
	 * An den publishing target path wird der published pages ordner aus diesen setting angehängt
	 * <p>
	 */
	public java.util.List<String> getFtpPublishingTargetsPaths() throws RQLException {
		java.util.List<PublishingTarget> targets = getFtpPublishingTargets();
		java.util.List<String> result = new ArrayList<String>(targets.size());

		// add published pages publication folder to ftp path
		PublicationFolder folderOrNull = getPublishedPages();
		for (PublishingTarget target : targets) {
			String path = null;
			// handle publishing of pages into root separately
			if (pointsPublishedPagesIntoRoot()) {
				path = target.getPublishingPath();
			} else {
				// folder cannot be null
				path = target.getPublishingPath(folderOrNull);
			}
			result.add(path);
		}
		return result;
	}

	/**
	 * Liefert die Sprachvariante.
	 */
	public LanguageVariant getLanguageVariant() {
		return languageVariant;
	}

	/**
	 * Liefert die RedDot GUID der Sprachvariante.
	 */
	public String getLanguageVariantGuid() {
		return getLanguageVariant().getLanguageVariantGuid();
	}

	/**
	 * Liefert den Namen der Sprachvariante.
	 */
	public String getLanguageVariantName() {
		return getLanguageVariant().getName();
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {
		return getPublicationPackage().getLogonGuid();
	}

	/**
	 * Liefert den Namen dieses publication settings in der form: project variant name/language variant name.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getName() {
		return getProjectVariantName() + "/" + getLanguageVariantName();
	}

	/**
	 * Liefert true, falls der Name den gegebenen string enthält.
	 */
	public boolean isNameContains(String namePart) throws RQLException {
		return getName().contains(namePart);
	}

	/**
	 * Returns true, if the path of published pages of this setting starts with the given rootFolderName, e.g. docRoot.
	 */
	public boolean isPublishedPagesStartsWith(String rootFolderName) throws RQLException {
		return getPublishedPages().getPath(true).startsWith(rootFolderName);
	}

	/**
	 * Liefert das Projekt.
	 */
	public Project getProject() {
		return getPublicationPackage().getProject();
	}

	/**
	 * Liefert die RedDot GUID des Projekts.
	 */
	public String getProjectGuid() throws RQLException {
		return getPublicationPackage().getProjectGuid();
	}

	/**
	 * Liefert die Projektvariante.
	 */
	public ProjectVariant getProjectVariant() {
		return projectVariant;
	}

	/**
	 * Liefert die RedDot GUID der Projektvariante.
	 */
	public String getProjectVariantGuid() {
		return getProjectVariant().getProjectVariantGuid();
	}

	/**
	 * Liefert den Namen der Projektvariante.
	 */
	public String getProjectVariantName() {
		return getProjectVariant().getName();
	}

	/**
	 * Liefert das Exportpaket, zu dem dieses Exportsetting gehört.
	 * 
	 * @return PublicationPackage
	 */
	private PublicationPackage getPublicationPackage() {
		return publicationPackage;
	}

	/**
	 * Liefert die GUID des Exportpaketes, zu dem dieses Exportsetting gehört.
	 */
	public String getPublicationPackageGuid() {
		return getPublicationPackage().getPublicationPackageGuid();
	}

	/**
	 * Liefert die RedDot GUID dieser Exportsetting.
	 * 
	 * @return java.lang.String
	 */
	public String getPublicationSettingGuid() {
		return publicationSettingGuid;
	}

	/**
	 * Liefert den PublicationFolder in den die Seiten (Published pages) für dieses Setting publiziert werden.
	 * <p>
	 * Liefert null, wenn dieses setting in die root publiziert. Check with {@link #pointsPublishedPagesIntoRoot()}.
	 */
	public PublicationFolder getPublishedPages() throws RQLException {
		return publishedPagesPublicationFolder;
	}

	/**
	 * Liefert alle zugeordneten Exportziele für dieses Exportsetting, unabhängig vom Typ.
	 */
	public java.util.List<PublishingTarget> getPublishingTargets() throws RQLException {
		/*
		<EXPORTSETTING  default="0" guid="7013002D30A748AE8AE9C1607915458E" projectvariantguid="797D786045E04D38A02B84488EB5D843" languagevariantguid="E6FC9644A75945729B018F98C6299D50" projectvariantname="Portal PRODUCTION" languagevariantname="English" languagevariantid="ENG" codepage="">
			<EXPORTTARGETS>
			<EXPORTTARGET  guid="D2CD2946698F4510BF67A2EE42CB5682" name="khh30001"/>
			<EXPORTTARGET  guid="2FA042C944A243B1A27BE167E4B83AF4" name="localhost"/>
			<EXPORTTARGET  guid="0D3F124EE97E4CCE9C2EB206F14501DD" name="kswfip01"/>
			</EXPORTTARGETS>
		...
		 */
		RQLNode settingNode = getPublicationPackage().getPublicationSettingNode(this);

		// wrap publishing targets
		java.util.List<PublishingTarget> targets = new ArrayList<PublishingTarget>();
		RQLNodeList targetNodeList = settingNode.getNodes("EXPORTTARGET");
		for (int i = 0; i < targetNodeList.size(); i++) {
			RQLNode node = targetNodeList.get(i);
			targets.add(getProject().getPublishingTargetByGuid(node.getAttribute("guid")));
		}
		return targets;
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getPublicationPackage().getSessionKey();
	}

	/**
	 * Liefert die im SmartTree genutze GUID genau dieses Settings zurück.
	 * <p>
	 * Wird beim Ändern des folders Published pages benötigt.
	 */
	private String getTreeNodeGuid() throws RQLException {
		/*
		V7.5 request
		<IODATA dialoglanguageid="ENG" loginguid="324ACF9BA6A2424D8BDDA5EEAE6B6669" sessionkey="FCD8084DC0224D19BCFC18A314DBA22E">
		<TREESEGMENT type="project.1710" action="load" guid="5B0B9DA0D49A46DA8E712F06E517E59F" descent="project" parentguid="799D52C5682A45CAA23CEEFDE49F4C41"/>
		</IODATA>
		V7.5 response
		<IODATA>
		<SEGMENT parentguid="5B0B9DA0D49A46DA8E712F06E517E59F" guid="449A531E69E048ED981DFE21D77B3974" type="project.1720" image="FileExportPath.gif" expand="0" value="Published pages" col1value="Published pages" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"/>
		...
		<TREEELEMENT guid="5B0B9DA0D49A46DA8E712F06E517E59F" value="ARCHIVE_pages_html/Chinese" image="Projectvariants.gif" flags="0" expand="0" descent="project" type="project.1710" col1value="ARCHIVE_pages_html/Chinese" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"/>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <TREESEGMENT type='project.1710' action='load' guid='" + getPublicationSettingGuid() + "'/>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNodeList segments = rqlResponse.getNodes("SEGMENT");

		// get fix value for published pages
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		String valueDe = b.getString("PublicationSettingPublishedPagesDe");
		String valueEn = b.getString("PublicationSettingPublishedPagesEn");

		// find
		for (int i = 0; i < segments.size(); i++) {
			RQLNode segment = segments.get(i);
			String value = segment.getAttribute("value");
			if (value.equals(valueEn) || value.equals(valueDe)) {
				return segment.getAttribute("guid");
			}
		}
		// should not be happen
		throw new ElementNotFoundException("Tree node guid for publication setting " + getName() + " in publication package "
				+ getPublicationPackage().getName() + " could not be found.");
	}

	/**
	 * Liefert true, falls an diesem Setting FTP Publizierungsziele zugeordnet sind, sonst false.
	 */
	public boolean hasFtpPublishingTargets() throws RQLException {
		return getFtpPublishingTargets().size() > 0;
	}

	/**
	 * Liefert true, falls an diesem Setting Verzeichnis-Publizierungsziele zugeordnet sind, sonst false.
	 */
	public boolean hasDirectoryPublishingTargets() throws RQLException {
		return getDirectoryPublishingTargets().size() > 0;
	}

	/**
	 * Makes usage of this object impossible.
	 */
	private void invalidate() {

		// reset caches in package
		getPublicationPackage().resetPublicationSettingsCache();

		// make this object unusable
		this.publicationPackage = null;
		this.publicationSettingGuid = null;
		this.publishedPagesPublicationFolder = null;
		projectVariant = null;
		languageVariant = null;
	}

	/**
	 * Liefert true, falls dieses Setting der gegebenen Projekt- und Sprachvariante entspricht, sonst false.
	 * <p>
	 * Testet mit equalsIgnoreCase().
	 */
	public boolean matches(String projectVariantName, String languageVariantName) throws RQLException {
		return getName().equalsIgnoreCase(projectVariantName + "/" + languageVariantName);
	}

	/**
	 * Liefert true, falls die publizierten Seiten für dieses Setting in die root generiert werden, sonst false.
	 * 
	 * @see #getPublishedPages()
	 */
	public boolean pointsPublishedPagesIntoRoot() {
		return publishedPagesPublicationFolder == null;
	}

	/**
	 * Entfernt das gegebene Exportziel target von diesem Exportsetting.
	 */
	public void removePublishingTo(PublishingTarget target) throws RQLException {

		changePublishingTarget(target, 0);
	}

	/**
	 * Ersetzt das gegebene Exportziel from (falls vorhanden) durch to an diesem Exportsetting.
	 * 
	 * @return Liefert true, falls eine Ersetzung stattgefunden hat, sonst false. TODO prevent deleting the cache in publication
	 *         package between remove and add
	 */
	public boolean replacePublishingTarget(PublishingTarget from, PublishingTarget to) throws RQLException {
		if (getPublishingTargets().contains(from)) {
			removePublishingTo(from);
			addPublishingTo(to);
			return true;
		}
		// no publishing to from defined
		return false;
	}

	/**
	 * Sets the publication folder for generated pages (published pages) for this setting to the given publication folder (one from the
	 * publication structure).
	 */
	public void setPublishedPages(PublicationFolder publicationFolder) throws RQLException {
		/*
		V7.5 request
		<IODATA sessionkey="FCD8084DC0224D19BCFC18A314DBA22E">
		<PROJECT>
		<EXPORTSETTING guid="5B0B9DA0D49A46DA8E712F06E517E59F" action="save">
		<FOLDEREXPORTSETTING folderguid="53EC95932CA14DD18284B0A9F71D0570" guid="449A531E69E048ED981DFE21D77B3974"/>
		</EXPORTSETTING>
		</PROJECT>
		</IODATA>
		V7.5 response
		<IODATA>ok
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'><PROJECT>"
				+ " <EXPORTSETTING action='save' guid='" + getPublicationSettingGuid() + "'>" + " <FOLDEREXPORTSETTING guid='"
				+ getTreeNodeGuid() + "' folderguid='" + publicationFolder.getPublicationFolderGuid() + "'/>"
				+ "</EXPORTSETTING></PROJECT></IODATA>";
		callCmsWithoutParsing(rqlRequest);

		// update folder
		publishedPagesPublicationFolder = publicationFolder;

		// reset settings cache in package to force new read
		getPublicationPackage().resetPublicationSettingsCache();
	}

	/**
	 * Returns the folder mapping node guid needed for changind the folder mapping for given internal folder.
	 */
	private String getFolderMappingNodeGuid(Folder cmsFolder) throws RQLException {
		RQLNodeList nodeList = getFolderMappingsNodeList();
		for (int i = 0; i < nodeList.size(); i++) {
			RQLNode node = nodeList.get(i);
			if (node.getAttribute("value").equals(cmsFolder.getName())) {
				return node.getAttribute("guid");
			}
		}
		throw new ElementNotFoundException("The publication package " + getPublicationPackage().getName() + " setting " + getName()
				+ " did not contain a folder mapping for folder " + cmsFolder.getName() + ". Please try again with another folder.");
	}

	/**
	 * Returns the folder mapping node list to get the mapping guid (which is senseless!) needed for changind the mapping.
	 */
	private RQLNodeList getFolderMappingsNodeList() throws RQLException {
		/*
		V9 request
		<IODATA loginguid="2B7D7DE296484310917A1F04775F640C" sessionkey="16F71A56A28A49CD93CAE7E193DEC476">
		<TREESEGMENT type="project.1710" action="load" guid="A844504C1F264915A923A70EF91C3930"/>
		</IODATA>
		V9 response
		<IODATA><SEGMENT parentguid="A844504C1F264915A923A70EF91C3930" guid="0212CC3E075746AD956F73CDFBF3ADB3" type="project.1720" image="FileExportPath.gif" expand="0" value="Published pages" col1value="Published pages" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"/>
		<SEGMENT parentguid="A844504C1F264915A923A70EF91C3930" guid="BF035109EF8C436FBF7B7385A7C720C3" type="project.1720" image="Folder.gif" expand="0" value="downloads_career_application_form" col1value="downloads_career_application_form" col2fontcolor="#808080" col2value="\trash\" col1fontweight="normal" col2fontweight="normal"/>
		<SEGMENT parentguid="A844504C1F264915A923A70EF91C3930" guid="F88D729356F344B2924AAA57D85BF0C2" type="project.1720" image="Folder.gif" expand="0" value="downloads_fleet_vessel_certificates" col1value="downloads_fleet_vessel_certificates" col2fontcolor="#808080" col2value="\docRoot\downloads\fleet\vessel_certificates\" col1fontweight="normal" col2fontweight="normal"/>
		<SEGMENT parentguid="A844504C1F264915A923A70EF91C3930" guid="012A4C3C33784D19ABE87566E571CBE8" type="project.1720" image="Folder.gif" expand="0" value="downloads_labels_and_messages" col1value="downloads_labels_and_messages" col2fontcolor="#808080" col2value="\docRoot\downloads\labels_and_messages\" col1fontweight="normal" col2fontweight="normal"/>
		<SEGMENT parentguid="A844504C1F264915A923A70EF91C3930" guid="A066255646454E9DA2101776F5295C4A" type="project.1720" image="Folder.gif" expand="0" value="downloads_local_info" col1value="downloads_local_info" col2fontcolor="#808080" col2value="\docRoot\downloads\local_info\" col1fontweight="normal" col2fontweight="normal"/>
		...
		<SEGMENT parentguid="A844504C1F264915A923A70EF91C3930" guid="380DAC63BC8D4C64BDCD7E7D06E45AC9" type="project.1720" image="Folder.gif" expand="0" value="js_ext_resources_locale" col1value="js_ext_resources_locale" col2fontcolor="#808080" col2value="\docRoot\js\ext\resources\locale\" col1fontweight="normal" col2fontweight="normal"/>
		<SEGMENT parentguid="A844504C1F264915A923A70EF91C3930" guid="971F29EDE27C4CF6BABD4764F93EE70B" type="project.1720" image="Folder.gif" expand="0" value="movies_containers" col1value="movies_containers" col2fontcolor="#808080" col2value="\docRoot\movies\containers\" col1fontweight="normal" col2fontweight="normal"/>
		<TREEELEMENT guid="A844504C1F264915A923A70EF91C3930" value="ARCHIVE_page_config_XML/Chinese" image="Projectvariants.gif" flags="0" expand="0" descent="project" type="project.1710" col1value="ARCHIVE_page_config_XML/Chinese" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"/>
		</IODATA>
		 */
		if (folderMappingsCache == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ " <TREESEGMENT action='load' type='project.1710' guid='" + getPublicationSettingGuid() + "'/>" + "</IODATA>";
			folderMappingsCache = callCms(rqlRequest).getNodes("SEGMENT");
		}
		return folderMappingsCache;
	}

	/**
	 * Returns the publication folder set on this publication setting for the given cms folder.
	 */
	public PublicationFolder getPublicationFolder(Folder folder) throws RQLException {
		/*
		V9 request
		<IODATA loginguid="2B7D7DE296484310917A1F04775F640C" sessionkey="16F71A56A28A49CD93CAE7E193DEC476">
		  <PROJECT>
		    <EXPORTSETTING guid="47763904412647999C56A6AC21A79660" action="load">
		      <FOLDEREXPORTSETTING guid="BC4A214A9C474C98AC3510C3FAB9D613"/>
		    </EXPORTSETTING>
		  </PROJECT>
		</IODATA>
		V9 response
		<IODATA>
		<EXPORTSETTING guid="47763904412647999C56A6AC21A79660" action="load" languagevariantid="ENG" key="16F71A56A28A49CD93CAE7E193DEC476" parentobjectname="PROJECT" useconnection="1" dialoglanguageid="ENG">
			<FOLDEREXPORTSETTING guid="BC4A214A9C474C98AC3510C3FAB9D613" folderguid="39A0276BC49F4B6FA01A2D29C184D208" datafolderguid="DB544D7459A84AA29F7F5870E2C4B49E"/>
		</EXPORTSETTING>
		</IODATA>
		 */
		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<PROJECT><EXPORTSETTING action='load' guid='" + getPublicationSettingGuid() + "'><FOLDEREXPORTSETTING guid='"
				+ getFolderMappingNodeGuid(folder) + "'/>" + "</EXPORTSETTING></PROJECT></IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		
		return getProject().getPublicationFolderByGuid(rqlResponse.getNode("FOLDEREXPORTSETTING").getAttribute("folderguid"));
	}
	/**
	 * Maps the given cms folder to the given publication folder on this publication setting.
	 * <p>
	 * All assets stored in the cms folder will be published to the given publication folder.
	 */
	public void setPublicationFolder(Folder folder, PublicationFolder publicationFolder) throws RQLException {
		/*
		V9 request
		<IODATA loginguid="2B7D7DE296484310917A1F04775F640C" sessionkey="16F71A56A28A49CD93CAE7E193DEC476">
		<PROJECT> 
		<EXPORTSETTING action='save' guid='A844504C1F264915A923A70EF91C3930'> 
		<FOLDEREXPORTSETTING guid='BF035109EF8C436FBF7B7385A7C720C3' folderguid='A4962F9D50464C03A3BCB2F3520928FE' datafolderguid='DB544D7459A84AA29F7F5870E2C4B49E'/>
		</EXPORTSETTING>
		</PROJECT>
		</IODATA>
		V9 response
		<IODATA>ok
		</IODATA>
		 */
		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<PROJECT><EXPORTSETTING action='save' guid='" + getPublicationSettingGuid() + "'><FOLDEREXPORTSETTING guid='"
				+ getFolderMappingNodeGuid(folder) + "' folderguid='" + publicationFolder.getPublicationFolderGuid()
				+ "' datafolderguid='" + folder.getFolderGuid() + "'/>" + "</EXPORTSETTING></PROJECT></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Setzt für dieses Exportsetting genau das gegebene Exportziel. Gegebenenfalls gesetzte andere Exportziele werden dabei entfernt.
	 * 
	 * @param target
	 *            Exportziel
	 */
	public PublicationSetting setPublishingTarget(PublishingTarget target) throws RQLException {

		/*
		<IODATA loginguid="A655197692CD46CD98E16F8D0FCA4D56" sessionkey="571682219Ok2u8wyh616">
		  <PROJECT>
		    <EXPORTSETTING guid="FBF0D815B03C4868BD1C3BA15915C412">
		      <EXPORTTARGETS action="save">
		        <EXPORTTARGET guid="CF15C90ABC564CFCA282CD969BF99E72" selected="0" />
		      </EXPORTTARGETS>
		    </EXPORTSETTING>
		  </PROJECT>
		</IODATA>
		V5 response
		<IODATA>
		</IODATA>
		*/

		// start and activate given target
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
				+ "   <EXPORTSETTING guid='" + getPublicationSettingGuid() + "'>" + "    <EXPORTTARGETS action='save'>"
				+ "        <EXPORTTARGET guid='" + target.getPublishingTargetGuid() + "' selected='1'/>";

		// remove all currently active targets
		for (PublishingTarget activeTarget : getPublishingTargets()) {
			// do not deactivate given target
			if (!activeTarget.equals(target)) {
				rqlRequest += "<EXPORTTARGET guid='" + activeTarget.getPublishingTargetGuid() + "' selected='0'/>";
			}
		}

		// finish request
		rqlRequest += "    </EXPORTTARGETS>" + "   </EXPORTSETTING>" + "  </PROJECT>" + "</IODATA>";
		// call CMS
		callCmsWithoutParsing(rqlRequest);

		// refresh cache in package
		getPublicationPackage().resetPublicationSettingsCache();

		return this;
	}

	/**
	 * Show combination for easier debugging.
	 */
	public String toString() {
		return getClass().getName() + "(" + getName() + ")";
	}
}
