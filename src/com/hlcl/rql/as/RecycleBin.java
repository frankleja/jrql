package com.hlcl.rql.as;

import java.util.ResourceBundle;

import com.hlcl.rql.util.as.PageArrayList;

/**
 * Diese Klasse beschreibt den Papierkorb mit gelöschten Seiten eines Projektes.
 * <p>
 * 
 * @author LEJAFR
 */
public class RecycleBin implements ProjectContainer {
	// fix for all projects
	private final String TREEGUID = "A05B9630A9E247BFB646540D61C3A2E2";
	private final String TREESEGMENT_TYPE = "app.1820";

	private Project project;

	/**
	 * constructor to create the recycle bin
	 */
	public RecycleBin(Project project) {
		super();

		this.project = project;
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
	 * Liefert true, falls die Seite mit der gegebenen Page ID im Papierkorb enthalten ist, sonst false.
	 */
	public boolean containsPageById(String pageId) throws RQLException {
		return searchForPageById(pageId) != null;
	}
	/**
	 * Returns the page for the given page ID or null, if no page for this page id found within recycle bin.
	 */
	public Page getPageById(String pageId) throws RQLException {

		PageSearch pageSearch = getPageSearch();
		pageSearch.addPageIdCriteriaEqual(pageId);
		// max 1 page in result, so get all
		PageArrayList result = pageSearch.getPages();
		return result.size() != 1 ? null : result.first();
	}


	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
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
	 * Erzeugt bei jedem Aufruf eine neue Suchanfrage für Seiten im recycle bin des Projekts.
	 */
	private PageSearch getPageSearch() {
		PageSearch pageSearch = project.definePageSearch();
		pageSearch.addTypeCriteriaOnlyPagesInRecycleBin();
		return pageSearch;
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
	 * Re-aktiviert die Seite mit der gegebenen Seiten ID aus dem Papierkorb und liefert diese Seite zurück.
	 * <p>
	 * 
	 * @throws WrongStateException
	 *             thrown, if no page with given page ID is in recycle bin
	 */
	public Page restorePageById(String pageId) throws RQLException {
		String guidOrNull = searchForPageById(pageId);
		// page not found in recycle bin
		if (guidOrNull == null) {
			throw new WrongStateException("Page with ID " + pageId + " cannot be restored, because it is not in the recycle bin.");
		}
		// wrap into page and restore
		Page result = getProject().getPageByGuid(guidOrNull);
		result.restore();
		// return for further access
		return result;
	}

	/**
	 * Liefert die Seiten GUID, falls die Seite mit der gegebenen SeitenID im Papierkorb enthalten ist, sonst null.
	 * 
	 * @throws RQLException
	 */
	private String searchForPageById(String pageId) throws RQLException {

		PageSearch pageSearch = getPageSearch();
		pageSearch.addPageIdCriteriaEqual(pageId);
		// max 1 page in result, so get all
		PageArrayList result = pageSearch.getPages();
		return result.size() != 1 ? null : result.first().getPageGuid();
	}

	/**
	 * Setzt auf dem recycle bin tree node im Smart Tree die Suchanfrage.
	 */
	private void setQueryByHeadline(String headline) throws RQLException {
		/*
		V9 request
		<IODATA loginguid="917E6A0D553542D4B627C5DAA9514CCB" sessionkey="164B53F2DD204CA196998F57D035CFED">
		<TREE>
		<QUERY action="save" treeguid="A05B9630A9E247BFB646540D61C3A2E2" headline="test" maxrecords="200" orderby="2" headlinelike="1" />
		</TREE>
		</IODATA>
		V9 response
		<IODATA>
		<QUERY action="save" treeguid="A05B9630A9E247BFB646540D61C3A2E2" headline="del test" maxrecords="200" orderby="2" headlinelike="1" sessionkey="02576AC4E9D249FD980655217223D261" dialoglanguageid="ENG" languagevariantid="ENG"/>
		</IODATA>
		 */
		
		String maxRecords = getRecycleBinPageScanByHeadlineChunkSize();

		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'><TREE>"
				// call CMS
				+ "<QUERY action='save' treeguid='" + TREEGUID + "' headline='" + StringHelper.escapeHTML(headline) + "'  maxrecords='" + maxRecords + "' orderby='2' headlinelike='1' />"
				+ "</TREE></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * @return the recycleBinPageScanByHeadlineChunkSize config parameter value from com/hlcl/rql/as/rql_fw.properties.
	 */
	public String getRecycleBinPageScanByHeadlineChunkSize() {
		// get scan chunk size
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		String maxRecords = b.getString("recycleBinPageScanByHeadlineChunkSize");
		return maxRecords;
	}

	/**
	 * Returns the tree segments for the pages in recycle bin.
	 */
	private RQLNodeList getSegmentNodes() throws RQLException {
		/*
		V9 request
		<IODATA loginguid="917E6A0D553542D4B627C5DAA9514CCB" sessionkey="164B53F2DD204CA196998F57D035CFED">
		<TREESEGMENT type="app.1820" action="load" />
		</IODATA>
		oder 
		<IODATA loginguid="917E6A0D553542D4B627C5DAA9514CCB" sessionkey="164B53F2DD204CA196998F57D035CFED">
		<TREESEGMENT type="app.1820" action="load" guid="A05B9630A9E247BFB646540D61C3A2E2" />
		</IODATA>
		V9 response
		<IODATA>
		<TREESEGMENTS>
		<SEGMENT parentguid="" guid="1528FB743BE2451C8A31E454740869F6" type="page" image="wastebasketpage.gif" pageid="195897" flags="524288" expand="1" value="test script draw points" col1value="test script draw points" col2fontcolor="#808080" col2value="12/14/2010 9:56:39 AM" col1fontweight="bold" col2fontweight="normal"/>
		<SEGMENT parentguid="" guid="69EC43F454B4423BAFCAC0F7B8CAFC3E" type="page" image="wastebasketpage.gif" pageid="204639" flags="524288" expand="1" value="test draw point default values leaf" col1value="test draw point default values leaf" col2fontcolor="#808080" col2value="12/14/2010 9:56:58 AM" col1fontweight="bold" col2fontweight="normal"/>
		<SEGMENT parentguid="" guid="62CC3C0EBCEF4D79A0715715C2B72FFC" type="page" image="wastebasketpage.gif" pageid="728138" flags="524288" expand="1" value="test antje leaf" col1value="test antje leaf" col2fontcolor="#808080" col2value="12/14/2010 9:57:15 AM" col1fontweight="bold" col2fontweight="normal"/>
		<SEGMENT parentguid="" guid="FF44566E0E324298ADF7C0E1765A16F0" type="page" image="wastebasketpage.gif" pageid="973077" flags="524288" expand="1" value="del test" col1value="del test" col2fontcolor="#808080" col2value="12/16/2010 11:50:07 AM" col1fontweight="bold" col2fontweight="normal"/>
		</TREESEGMENTS>
		<TREEELEMENT guid="A05B9630A9E247BFB646540D61C3A2E2" value="Recycle Bin" image="wastebasket.gif" flags="0" expand="1" descent="" type="app.1820" col1value="Recycle Bin" col2fontcolor="#808080" col2value="  Filtered" col1fontweight="normal" col2fontweight="normal"/>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<TREESEGMENT action='load' type='" + TREESEGMENT_TYPE + "' />" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNodes("SEGMENT");
	}

	/**
	 * Returns the deleted date for the given page, or null if given page could not be found in recycle bin. Uses a query to the
	 * recycle bin in SmartTree to get it from tree nodes. 
	 * 
	 * MS requires to search for the page by headline. This method uses the query in SmartTree on Recycle Bin node. 
	 * 
	 * @throws RecycleBinPageNotFoundByHeadlineException
	 *             if the page is in recycle bin, but could not be found with the smart tree query on recycle bin node. This query uses
	 *             the page headline, which might cause the problem.
	 */
	ReddotDate getDeletedDate(Page page) throws RQLException {
		// set query
		setQueryByHeadline(page.getHeadline());

		// get segment node for page by guid
		RQLNode pageNode = null;
		RQLNodeList nodes = getSegmentNodes();
		if (nodes == null) {
			if (page.isInRecycleBin()) {
				throw new RecycleBinPageNotFoundByHeadlineException(
						"Page "
								+ page.getInfoText()
								+ " is in recycle bin, but could not be found with smart tree query to get the deleted on date. This query uses the page headline, which might be a problem here.");
			}
			return null;
		}
		for (int i = 0; i < nodes.size(); i++) {
			RQLNode n = nodes.get(i);
			if (n.getAttribute("guid").equals(page.getPageGuid())) {
				pageNode = n;
				break;
			}
		}
		// signal not found
		if (pageNode == null) {
			if (page.isInRecycleBin()) {
				throw new RecycleBinPageNotFoundByHeadlineException(
						"Page "
								+ page.getInfoText()
								+ " is in recycle bin, but could not be found with smart tree query to get the deleted on date. This query uses the page headline, which might be a problem here.");
			}
			return null;
		}

		// parse date
		String timestamp = pageNode.getAttribute("col2value");
		String[] parts = StringHelper.split(timestamp, " ");
		// ignore time, only date
		return ReddotDate.parseSmartTreeDate(parts[0]);
	}
}
