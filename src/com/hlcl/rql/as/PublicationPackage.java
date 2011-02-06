package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Diese Klasse beschreibt ein Exportpaket.
 * 
 * @author LEJAFR
 */
public class PublicationPackage implements ProjectContainer {
	// constants
	protected final static String TREESEGMENT_TYPE = "project.1700";
	private final static String PUBLISHED_PAGES_FOLDER_GUID = "00000000000000000000000000000100";
	private String name;

	private Project project;
	private String publicationPackageGuid;
	private java.util.List<PublicationSetting> publicationSettings;
	private RQLNode detailsNodeCache;

	/**
	 * constructor comment.
	 */
	public PublicationPackage(Project project, String publicationPackageGuid, String name) {
		super();

		this.project = project;
		this.publicationPackageGuid = publicationPackageGuid;
		this.name = name;
	}

	/**
	 * constructor with better performance.
	 */
	public PublicationPackage(Project project, String publicationPackageGuid, String name, RQLNode exportPacketNode) {
		super();

		this.project = project;
		this.publicationPackageGuid = publicationPackageGuid;
		this.name = name;
		this.detailsNodeCache = exportPacketNode;
	}

	/**
	 * Erzeugt ein neues ExportSetting für die gegebene Kombination newProjectVariant und languageVariant.
	 * <p>
	 * Kopiert die Einstellungen für die neue Kombination (alle Folder) von der Kombination copyProjectVariant/newLanguageVariant.
	 * 
	 * @throws RQLException
	 *             falls es bereits ein setting für die anzulegende Kombination gibt.
	 * @throws RQLException
	 *             falls es kein setting gibt, von dem die Foldereinstellungen kopiert werden sollen
	 */
	public PublicationSetting addSetting(ProjectVariant newProjectVariant, LanguageVariant languageVariant, ProjectVariant copyProjectVariant)
			throws RQLException {
		/* 
		 V7.5 request
		<IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
		  <PROJECT>
		    <EXPORTSETTING action="save" guid="[!guid_exportpacket!]"
		     projectvariantguid="[!guid_projectvariant!]"
		     languagevariantguid="[!guid_languagevariant!]"
		     copyguid="[!guid_projectvariant!][!guid_languagevariant!]"/>
		  </PROJECT>
		</IODATA>
		 V7.5 response
		<IODATA>ok
		</IODATA>
		 */

		// check that new combination does not exist
		if (getSettingFor(newProjectVariant, languageVariant) != null) {
			throw new RQLException("Setting not added, because a setting for the combination " + newProjectVariant.getName() + " and "
					+ languageVariant.getName() + " already exists in publication package " + getName() + ".");
		}
		// check that old combination exist
		PublicationSetting copySetting = getSettingFor(copyProjectVariant, languageVariant);
		if (copySetting == null) {
			throw new RQLException("Setting not added, because a setting you want to copy from does not exist for the combination "
					+ copyProjectVariant.getName() + " and " + languageVariant.getName() + " in publication package " + getName() + ".");
		}
		// create new setting and copy folders
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PROJECT>"
				+ "   <EXPORTSETTING action='save' guid='" + getPublicationPackageGuid() + "' projectvariantguid='"
				+ newProjectVariant.getProjectVariantGuid() + "' languagevariantguid='" + languageVariant.getLanguageVariantGuid() + "' copyguid='"
				+ copySetting.getProjectVariantGuid() + copySetting.getLanguageVariantGuid() + "' />" + " </PROJECT>" + "</IODATA>";
		callCmsWithoutParsing(rqlRequest);
		// clear caches
		resetPublicationSettingsCache();

		// deliver the newly created setting
		return getSettingFor(newProjectVariant, languageVariant);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getCmsClient().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert true genau dann, wenn dieses Exportpaket ein Exportsetting entsprechend dem gegebenen enthält.
	 */
	public boolean contains(PublicationSetting publicationSetting) throws RQLException {

		java.util.List<PublicationSetting> settings = getPublicationSettings();
		for (int i = 0; i < settings.size(); i++) {
			PublicationSetting setting = (PublicationSetting) settings.get(i);
			if (setting.getPublicationSettingGuid().equals(publicationSetting.getPublicationSettingGuid())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Liefert true genau dann, wenn dieses Exportpaket ein Exportsetting für die gegebene Kombination besitzt.
	 */
	public boolean containsSettingFor(ProjectVariant projectVariant, LanguageVariant languageVariant) throws RQLException {

		PublicationSetting settingOrNull = findPublicationSetting(projectVariant, languageVariant);
		return settingOrNull == null ? false : true;
	}

	/**
	 * Löscht alle ExportSettings für alle Projektvarianten, deren Name projectVariantNamePart enthält.
	 * 
	 * @param projectVariantNamePart
	 *            Teil des Namens der Projektvariante (check with contains)
	 */
	public void deleteSettingsByProjectVariantName(String projectVariantNamePart) throws RQLException {

		for (PublicationSetting setting : getSettingsByProjectVariantName(projectVariantNamePart)) {
			setting.delete();
			// reset settings cache to force new read
			resetPublicationSettingsCache();
		}
	}

	/**
	 * Liefert ein konfiguriertes (used) Exportsetting für die gegebenen Varianten falls vorhanden, sonst null.
	 * 
	 * @param projectVariantName
	 *            Name der Projektvariante
	 * @param languageVariantName
	 *            Name der Sprachvariante
	 * @return PublicationSetting or null
	 */
	private PublicationSetting findPublicationSetting(ProjectVariant projectVariant, LanguageVariant languageVariant) throws RQLException {

		java.util.List<PublicationSetting> settings = getPublicationSettings();
		String projectVariantName = projectVariant.getName();
		String languageVariantName = languageVariant.getName();

		// find setting
		for (int i = 0; i < settings.size(); i++) {
			PublicationSetting setting = (PublicationSetting) settings.get(i);
			if (setting.matches(projectVariantName, languageVariantName)) {
				return setting;
			}
		}
		// not found
		return null;
	}

	/**
	 * Liefert ein ExportSetting für die gegebene Kombination, wenn das Exportpaket sie besitzt. Sonst wird null geliefert.
	 * 
	 * @param projectVariantName
	 *            Name der Projektvariante
	 * @param languageCode
	 *            internal Language ID, e.g. ENG, CHS, DEU
	 * @return ExportSetting or null, if not exists
	 */
	public PublicationSetting findSettingByProjectVariantNameAndLanguageCode(String projectVariantName, String languageCode) throws RQLException {

		return getSettingFor(getProject().getProjectVariantByName(projectVariantName), getProject().getLanguageVariantByLanguage(languageCode));
	}

	/**
	 * Liefert ein ExportSetting für die gegebene Kombination, wenn das Exportpaket sie besitzt. Sonst wird null geliefert.
	 * 
	 * @param projectVariantGuid
	 *            GUID der Projektvariante
	 * @param rfcLanguageId
	 *            RFC Language ID, e.g. en, zh, de
	 * @return ExportSetting or null, if not exists
	 */
	public PublicationSetting findSettingByProjectVarianGuidAndRfcLanguageId(String projectVariantGuid, String rfcLanguageId) throws RQLException {

		return getSettingFor(getProject().getProjectVariantByGuid(projectVariantGuid), getProject().getLanguageVariantByRfcLanguageId(rfcLanguageId));
	}

	/**
	 * Liefert ein ExportSetting für die gegebene Kombination, wenn das Exportpaket sie besitzt. Sonst wird null geliefert.
	 * 
	 * @param projectVariantName
	 *            Name der Projektvariante
	 * @param rfcLanguageId
	 *            RFC Language ID, e.g. en, zh, de
	 * @return ExportSetting or null, if not exists
	 */
	public PublicationSetting findSettingByProjectVariantNameAndRfcLanguageId(String projectVariantName, String rfcLanguageId) throws RQLException {

		return getSettingFor(getProject().getProjectVariantByName(projectVariantName), getProject().getLanguageVariantByRfcLanguageId(rfcLanguageId));
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	/**
	 * Liefert alle Details dieses Exportpaketes. Draft project variants are not delivered.
	 * <p>
	 */
	private RQLNode getDetailsNode() throws RQLException {
		/*
		<IODATA loginguid="58C6B3A2B35E4C71AD95800752DC418F" sessionkey="571696016O1sWPv3lP00">
		  <PROJECT>
		    <EXPORTPACKET action="loadpacket" guid="BA80D6AB81F145B3922738CC952BF6DD" />
		  </PROJECT>
		</IODATA>
		V5 response
		<IODATA>
		<EXPORTPACKET  guid="BA80D6AB81F145B3922738CC952BF6DD" name="EXPORTSETTING generell">
		<EXPORTSETTINGS>
		<EXPORTSETTING  default="0" guid="7013002D30A748AE8AE9C1607915458E" projectvariantguid="797D786045E04D38A02B84488EB5D843" languagevariantguid="E6FC9644A75945729B018F98C6299D50" projectvariantname="Portal PRODUCTION" languagevariantname="English" languagevariantid="ENG" codepage="">
			<EXPORTTARGETS>
			<EXPORTTARGET  guid="D2CD2946698F4510BF67A2EE42CB5682" name="khh30001"/>
			<EXPORTTARGET  guid="2FA042C944A243B1A27BE167E4B83AF4" name="localhost"/>
			<EXPORTTARGET  guid="0D3F124EE97E4CCE9C2EB206F14501DD" name="kswfip01"/>
			</EXPORTTARGETS>
			<FOLDEREXPORTSETTINGS>
			<FOLDEREXPORTSETTING  folderguid="00000000000000000000000000000100" foldername="Published pages" path="962C5D014AA04F2B8E1A9455DB7BBB51" htmlpath="962C5D014AA04F2B8E1A9455DB7BBB51" name="pages" realname="pages" realvirtualname="" virtualname="pages"/>
			<FOLDEREXPORTSETTING  folderguid="1399FC72ABCB4E839DE8D91E83CCDCFF" foldername="images_layout" path="6C16304BE65F4D198ABBB65861108039" htmlpath="6C16304BE65F4D198ABBB65861108039" name="images_layout" realname="images_layout" realvirtualname="" virtualname="images_layout"/>
			<FOLDEREXPORTSETTING  folderguid="1A9B9AE56A5449B096C547E2A0360C6C" foldername="images_slide" path="4683B338343441F0882F7A1A5FA4C096" htmlpath="4683B338343441F0882F7A1A5FA4C096" name="images_slide" realname="images_slide" realvirtualname="" virtualname="images_slide"/>
			<FOLDEREXPORTSETTING  folderguid="1B0280BBF0E64E70AF7D1D5575E52510" foldername="view_pdf" path="222E654EE5EB42FDA974D7A045323ADF" htmlpath="222E654EE5EB42FDA974D7A045323ADF" name="view_pdf" realname="view_pdf" realvirtualname="" virtualname="view_pdf"/>
			<FOLDEREXPORTSETTING  folderguid="63D8B1B73E854B4AA6A2041784C41C9C" foldername="imageCache" path="" htmlpath="" name="" realname="" realvirtualname="" virtualname=""/>
			<FOLDEREXPORTSETTING  folderguid="72904B4D243C4B5EA3E458FE9E019AB9" foldername="images" path="DC5F0CBD188242F9A01F1B447E61C804" htmlpath="DC5F0CBD188242F9A01F1B447E61C804" name="images" realname="images" realvirtualname="" virtualname="images"/>
			<FOLDEREXPORTSETTING  folderguid="8BF0F5A053864F798F354D2323E99EDA" foldername="newsletter_pdf" path="09E9B19BA40F4E9D81C66D0B05728EE3" htmlpath="09E9B19BA40F4E9D81C66D0B05728EE3" name="newsletter_pdf" realname="newsletter_pdf" realvirtualname="" virtualname="newsletter_pdf"/>
			<FOLDEREXPORTSETTING  folderguid="909CD85450D84DC1AF118933FB7A0AF5" foldername="images_article" path="58DDB4E3CADD493C8D6F1EC591702700" htmlpath="58DDB4E3CADD493C8D6F1EC591702700" name="images_article" realname="images_article" realvirtualname="" virtualname="images_article"/>
			<FOLDEREXPORTSETTING  folderguid="C0E551CAD2D94DD6A418A89A4972B924" foldername="images_hidden" path="9747455B2CAE48FA994EC157399DB8C5" htmlpath="9747455B2CAE48FA994EC157399DB8C5" name="images_hidden" realname="images_hidden" realvirtualname="" virtualname="images_hidden"/>
			<FOLDEREXPORTSETTING  folderguid="C66CE50C719B4F13BE25EBF53895AC0D" foldername="images_filetype" path="4932908EBEAF4608967B4AF42E44E55F" htmlpath="4932908EBEAF4608967B4AF42E44E55F" name="images_filetype" realname="images_filetype" realvirtualname="" virtualname="images_filetype"/>
			<FOLDEREXPORTSETTING  folderguid="D43C8053E2F4441B9FEF9BB94211FFB2" foldername="images_language" path="54E8413EF0324A6FA57646FD6FCDD465" htmlpath="54E8413EF0324A6FA57646FD6FCDD465" name="images_language" realname="images_language" realvirtualname="" virtualname="images_language"/>
			<FOLDEREXPORTSETTING  folderguid="DB7234C07B6947EE8AACD6E271DDF32C" foldername="view_html" path="B1F3562065094ACAA047E499032569BC" htmlpath="B1F3562065094ACAA047E499032569BC" name="view_html" realname="view_html" realvirtualname="" virtualname="view_html"/>
			<FOLDEREXPORTSETTING  folderguid="EB55CEA852F64C7099C60021171535A9" foldername="downloads" path="A6B0A68DDCB24E8CBFB745C282D05A6F" htmlpath="A6B0A68DDCB24E8CBFB745C282D05A6F" name="downloads" realname="downloads" realvirtualname="" virtualname="downloads"/>
			<FOLDEREXPORTSETTING  folderguid="F3AE1D5712D54347B4764955CA657A67" foldername="1_images_TEXTEDITOR" path="AA1745C07A8A416899CB5647082E35E5" htmlpath="AA1745C07A8A416899CB5647082E35E5" name="images_texteditor" realname="images_texteditor" realvirtualname="" virtualname="images_texteditor"/>
			</FOLDEREXPORTSETTINGS>
		</EXPORTSETTING>
		...
		</EXPORTSETTINGS>
		</EXPORTPACKET>
		
		</IODATA>
		*/

		// call CMS
		if (detailsNodeCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
					+ "   <EXPORTPACKET action='loadpacket' guid='" + getPublicationPackageGuid() + "'/>" + "  </PROJECT>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			detailsNodeCache = rqlResponse.getNode("EXPORTPACKET");
		}
		return detailsNodeCache;
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
	 * Return true, if the name ends with the given suffix.
	 */
	public boolean isNameEndsWith(String suffix) {
		return getName().endsWith(suffix);
	}

	/**
	 * Return true, if the name starts with the given prefix.
	 */
	public boolean isNameStartsWith(String prefix) {
		return getName().startsWith(prefix);
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
	 * Liefert die GUID des PublicationFolders für die gegebenen folderGuid aus dem gegebenen node für das PublicationSetting. <p>
	 * Liefert null, falls für die folderGuid ins root Verzeichnis publiziert wird.<p>
	 * 
	 * @throws ElementNotFoundException if folderGuidToFind not found within publicationSettingNode
	 */
	private String getPublicationFolderGuid(RQLNode publicationSettingNode, String folderGuidToFind) throws RQLException {
		RQLNodeList nodes = publicationSettingNode.getNodes("FOLDEREXPORTSETTING");
		for (int i = 0; i < nodes.size(); i++) {
			RQLNode folderSettingNode = nodes.get(i);
			// filter first
			if (folderSettingNode.getAttribute("folderguid").equals(folderGuidToFind)) {
				// check for root
				if (folderSettingNode.getAttribute("name").length() == 0) {
					// no name value means root folder
					return null;
				} else {
					// return guid of publication folder
					return folderSettingNode.getAttribute("path");
				}
			}
		}
		// signal not found
		throw new ElementNotFoundException("Folder GUID " + folderGuidToFind + " could not found in RQL response node with guid " + publicationSettingNode.getAttribute("guid") + " project variant " + publicationSettingNode.getAttribute("projectvariantname") + " language variant " + publicationSettingNode.getAttribute("languagevariantname") + " in publication package " + getName());
	}

	/**
	 * Liefert die GUID dieses Exportpaketes.
	 * 
	 * @return java.lang.String
	 */
	public String getPublicationPackageGuid() {
		return publicationPackageGuid;
	}

	/**
	 * Liefert für das gegebene Exportsetting den passenden RQLNode zurück. Ignoriert die (Draft) Kombinationen.
	 * <p>
	 * 
	 * @throws ElementNotFoundException
	 */
	RQLNode getPublicationSettingNode(PublicationSetting publicationSetting) throws RQLException {

		RQLNodeList settingNodeList = getPublicationSettingsNodeList();

		// wrap all setting nodes
		for (int i = 0; i < settingNodeList.size(); i++) {
			RQLNode node = settingNodeList.get(i);
			// filter
			if (node.getAttribute("guid").equals(publicationSetting.getPublicationSettingGuid())) {
				return node;
			}
		}
		// should not happen
		throw new ElementNotFoundException("RQLNode for publication setting " + publicationSetting.getName() + " in publication package "
				+ this.getName() + " not found. Maybe cached values are not in synch.");
	}

	/**
	 * Liefert alle Exportsettings die an diesem Exportpaket aktiv sind, aber nicht die (Draft) Kombinationen.
	 */
	public java.util.List<PublicationSetting> getPublicationSettings() throws RQLException {
		/*
		<EXPORTSETTINGS>
		<EXPORTSETTING  default="0" guid="7013002D30A748AE8AE9C1607915458E" projectvariantguid="797D786045E04D38A02B84488EB5D843" 
		languagevariantguid="E6FC9644A75945729B018F98C6299D50" projectvariantname="Portal PRODUCTION" languagevariantname="English" 
		languagevariantid="ENG" codepage="">
			<EXPORTTARGETS>
		 */

		// get constant suffixes
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		String pvNameSuffixes = b.getString("DraftProjectVariantNameSuffixes");

		// convert from nodes to objects
		if (publicationSettings == null) {
			publicationSettings = new ArrayList<PublicationSetting>();
			RQLNodeList settingNodeList = getPublicationSettingsNodeList();

			// wrap all setting nodes
			for (int i = 0; i < settingNodeList.size(); i++) {
				RQLNode node = settingNodeList.get(i);

				// skip (Draft) project variants
				if (StringHelper.endsWithOneOf(node.getAttribute("projectvariantname"), pvNameSuffixes, ",")) {
					continue;
				}
				// add normal setting
				publicationSettings.add(buildPublicationSetting(node));
			}
		}
		return publicationSettings;
	}

	/**
	 * Creates a publication setting object from the RQLNode.
	 * TODO Funktioniert nicht, falls der publication folder für published pages nicht direkt unter der root liegt.
	 * @throws RQLException
	 */
	private PublicationSetting buildPublicationSetting(RQLNode node) throws RQLException {
		String publicationFolderGuidOrNull = getPublicationFolderGuid(node, PublicationPackage.PUBLISHED_PAGES_FOLDER_GUID);
		
		// try to get publication folder from GUID
		PublicationFolder folder = null;
		if (publicationFolderGuidOrNull != null) {
			folder = getProject().getPublicationFolderByGuid(publicationFolderGuidOrNull);
		}
		// create object
		return new PublicationSetting(this, node.getAttribute("guid"), folder, node
				.getAttribute("projectvariantguid"), node.getAttribute("languagevariantguid"));
	}

	/**
	 * Liefert die RQLNodeList aller PublicationSettings dieses Exportpaketes. Draft project variants are not delivered.
	 */
	private RQLNodeList getPublicationSettingsNodeList() throws RQLException {

		return getDetailsNode().getNodes("EXPORTSETTING");
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getProject().getSessionKey();
	}

	/**
	 * Liefert ein ExportSetting für die gegebene Kombination, wenn das Exportpaket sie besitzt. Sonst wird null geliefert.
	 * <p>
	 */
	public PublicationSetting getSettingFor(ProjectVariant projectVariant, LanguageVariant languageVariant) throws RQLException {

		return findPublicationSetting(projectVariant, languageVariant);
	}

	/**
	 * Liefert ein ExportSetting für die gegebene Kombination, wenn das Exportpaket sie besitzt. Sonst wird null geliefert.
	 * <p>
	 */
	public PublicationSetting getSettingFor(String projectVariantGuid, String languageVariantGuid) throws RQLException {

		return getSettingFor(getProject().getProjectVariantByGuid(projectVariantGuid), getProject().getLanguageVariantByGuid(languageVariantGuid));
	}

	/**
	 * Liefert alle ExportSettings für alle Projektvarianten, deren Name projectVariantNamePart enthält.
	 * 
	 * @param projectVariantNamePart
	 *            Teil des Namens der Projektvariante (check with contains, case sensitive)
	 */
	public java.util.List<PublicationSetting> getSettingsByProjectVariantName(String projectVariantNamePart) throws RQLException {
		java.util.List<PublicationSetting> result = new ArrayList<PublicationSetting>();

		// select settings
		for (PublicationSetting setting : getPublicationSettings()) {
			if (setting.getProjectVariantName().contains(projectVariantNamePart)) {
				result.add(setting);
			}
		}
		return result;
	}

	/**
	 * Liefert true, falls dieses Exportpaket ein globales ist.
	 * 
	 * @deprecated Did not work in V7.5.0.33. Deliver same attributes within same segment tag what causes the xml parser to stop with an error.
	 */
	public boolean isGlobal() throws RQLException {
		RQLNode segment = getProject().getProjectTreeSegment(TREESEGMENT_TYPE);
		if (segment == null) {
			return false;
		}
		// check name
		return segment.getAttribute("value").equals(getName());
	}

	/**
	 * Erzwingt ein Neulesen der PublicationSettings vom CMS. Notwendig nach Änderungen des Foldermappings und Anlage neuer settings.
	 * 
	 * @return java.lang.String
	 */
	void resetPublicationSettingsCache() {
		this.detailsNodeCache = null;
		this.publicationSettings = null;
	}

	/**
	 * Gibt den Speicher aller Caches wieder frei für die GC. Dieses Exportpaket bleibt trotzdem voll funktionsfähig! 
	 */
	public void freeOccupiedMemory() {
		resetPublicationSettingsCache();
	}

	/**
	 * Show name for easier debugging.
	 */
	public String toString() {
		return getClass().getName() + "(" + getName() + ")";
	}
}
