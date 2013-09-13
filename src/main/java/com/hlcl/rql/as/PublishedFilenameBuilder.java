package com.hlcl.rql.as;

/**
 * Diese Klasse kapselt die Logik den publizierten Dateinamen zu bestimmen.
 * 
 * @author LEJAFR
 */
public class PublishedFilenameBuilder implements PageContainer {

	private Page page;
	private ProjectVariant projectVariant;

	/**
	 * PageFilenameBuilder constructor comment. Steht nur dem Framework zu Verfügung.
	 *
	 * @param	page	Seite, für die der Dateiname bestimmt werden soll
	 * @param	projectVariant	für welche Projektvariante? bestimmt den Extender des Dateinamens
	 */
	protected PublishedFilenameBuilder(Page page, ProjectVariant projectVariant) {
		super();

		this.page = page;
		this.projectVariant = projectVariant;
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getCmsClient().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck.
	 * Erforderlich für die Ermittlung des Werts eines Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getPage().getCmsClient();
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {
		return getPage().getLogonGuid();
	}

	/**
	 * Liefert die Seite, zu der diese Bemerkung gehört.
	 */
	public Page getPage() {

		return page;
	}

	/**
	 * Liefert die RedDot GUID der Seite.
	 */
	public String getPageGuid() {
		return getPage().getPageGuid();
	}

	/**
	 * Liefert das Projekt. 
	 */
	public Project getProject() {
		return getPage().getProject();
	}

	/**
	 * Liefert den Dateinamen aus der Publizierung. Beachtet viele (aber nicht alle!) Einstellungen aus dem Projekt diesbezüglich.<p>
	 * Arbeitet nur korrekt für Seiten, deren Mainlink ein MultiLinks ist.<p>
	 * Der in den Properties der Seite definierte Dateiname darf keinen Extender haben! Der kommt immer aus der Templatevariante oder dem project default.
	 */
	public String getPublishedFilename() throws RQLException {
		// TODO was bei festem dateinamen? (mit und ohne extension!)
		Project project = getProject();

		// treat this page as default;
		Page sourcePg = getPage();
		
		// get the main link MultiLink first
		// TODO was wenn kein MainLink vorhanden ist?
		MultiLink mainMultiLink = getPage().getMainMultiLink();
		com.hlcl.rql.as.List mainLinkList = null;
		boolean isTargetContainerPg = false;
		if (mainMultiLink.isList() && (mainLinkList = (com.hlcl.rql.as.List) mainMultiLink).isTargetContainerAssigned()) {
			// change source to the page containing the target container
			sourcePg = mainLinkList.getTargetContainer().getPage();
			isTargetContainerPg = true;
		}

		// starts with the entered filename from page properties 
		// or use template prefix and page id or guid
		String filename = null;
		if (sourcePg.hasFilename()) {
			// use entered filename from page
			filename = sourcePg.getFilename();
		} else {
			// filename begins with prefix for source page
			filename = sourcePg.getTemplate().getPrefixName();

			// add page id or guid next
			filename += sourcePg.getPublishedFilenameId();
		}

		// for target container pages only add _4711 in addition
		// get the separator and the actual page id
		if (isTargetContainerPg) {
			// determine the separator string; assume target container pages did not use only her own filename
			filename += project.getPublicationSettingTargetContainerSeparator();

			// add the fragment page ID or GUID
			filename += getPage().getPublishedFilenameId();
		}

		// fix: ad the . (really?)
		filename += ".";

		// get the extension; from template variant or project default
		TemplateVariant variantOrNull = sourcePg.getTemplate().getPublishedTemplateVariantFor(projectVariant);
		if (variantOrNull == null) {
			// should not be
			throw new RQLException("The filename of page " + sourcePg.getInfoText() + " could not be determined, because the Templatevariant could not be determined for given project variant " + projectVariant.getName()+ ".");
		}
		String temp = variantOrNull.getFileExtension();
		if (temp.length() == 0) {
			temp = project.getPublicationSettingStandardExtension();
		}
		filename += temp;

		return filename;
	}

	/**
	 * Liefert die RedDot GUID des Projekts. 
	 */
	public String getProjectGuid() throws RQLException {
		return getPage().getProjectGuid();
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getPage().getSessionKey();
	}
}
