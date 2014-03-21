package com.hlcl.rql.as;

import java.util.ArrayList;

import com.hlcl.rql.util.as.PageArrayList;

/**
 * Diese Klasse kapselt eine Seitensuchanfrage (RQL xsearch). Dabei lassen sich die Kriterien dynamisch zusammenbauen.
 * 
 * @author LEJAFR
 */
public class PageSearch implements ProjectContainer {
	/**
	 * Diese Klasse kapselt die Filterkriterien für die xsearch Seitensuche.
	 * <p>
	 * Diese Klasse sollte nicht direkt, sondern nur über PageSearch genutzt werden.
	 * 
	 * @author lejafr
	 * @see PageSearch
	 */
	private class PageSearchItem {
		private String key;
		private String value;
		private String operator;
		private String additionalAttributes;

		/**
		 * constructor to create a page search item
		 */
		private PageSearchItem(String key, String value, String operator) {
			super();
			this.key = key;
			this.value = value;
			this.operator = operator;
			this.additionalAttributes = null;
		}

		/**
		 * constructor to create a page search item
		 */
		private PageSearchItem(String key, String value, String operator, String additionalAttributes) {
			super();
			this.key = key;
			this.value = value;
			this.operator = operator;
			this.additionalAttributes = additionalAttributes;
		}

		/**
		 * Erzeugt für dieses PageSearchItem das Tag <SEARCHITEM ... des xsearch RQL requests.
		 * 
		 * @see PageSearch
		 */
		private String getSearchItemTag() {
			return "<SEARCHITEM key='" + getKey() + "' value='" + getValue() + "' operator='" + getOperator() + "' " + getAdditionalAttributes()
					+ " />";
		}

		/**
		 * Returns true, if this search item has additional attributes.
		 * 
		 * @see #getAdditionalAttributes()
		 */
		public boolean hasAdditionalAttributes() {
			return additionalAttributes != null;
		}

		/**
		 * Returns the additional attributes or an empty string it not used.
		 * 
		 * @see #hasAdditionalAttributes()
		 */
		public String getAdditionalAttributes() {
			return additionalAttributes == null ? "" : additionalAttributes;
		}

		/**
		 * Returns the search item key.
		 */
		private String getKey() {
			return key;
		}

		/**
		 * Returns the search item operator.
		 */
		private String getOperator() {
			return operator;
		}

		/**
		 * Returns the search item value.
		 */
		private String getValue() {
			return value;
		}
	}

	private Project project;

	// search request attributes
	private String orderBy = "headline";
	private String orderDirection = "ASC";
	private java.util.List<PageSearchItem> searchItems;

	/**
	 * constructor to create a page search
	 */
	public PageSearch(Project project) {
		super();

		this.project = project;

		// initialize
		searchItems = new ArrayList<PageSearchItem>();
	}

	/**
	 * Fügt dieser Seitensuche ein Kriterium hinzu.
	 */
	private void add(PageSearchItem searchItem) {

		searchItems.add(searchItem);
	}

	/**
	 * Started die konfigurierte Seitensuche und liefert die RQLNodeList für die Seiten zurück.
	 * 
	 * @throws RQLException
	 */
	private RQLNodeList getPagesNodeList(int maxPages) throws RQLException {
		String rqlRequest = buildRequest(maxPages);
        return callCms(rqlRequest).getNodes("PAGE");
	}

	/**
	 * Started die konfigurierte Seitensuche und liefert alle Seiten, die diesen Kriterien entsprechen, zurück. Achtung: Es werden alle Seiten
	 * geliefert!
	 * 
	 * @throws RQLException
	 */
	public PageArrayList getPages() throws RQLException {
		return getPages(-1);
	}

	/**
	 * Started die konfigurierte Seitensuche und liefert alle Seiten, die diesen Kriterien entsprechen, zurück. Es werden maximal die Anzahl maxPages
	 * zurückgeliefert, auch wenn mehr Seiten zu dieser Suche passen.
	 * 
	 * @throws RQLException
	 */
	public PageArrayList getPages(int maxPages) throws RQLException {

		RQLNodeList pageNodes = getPagesNodeList(maxPages);
		PageArrayList result = new PageArrayList();

		// nothing found
		if (pageNodes == null) {
			return result;
		}
		// wrap into pages; use only the main data from the search
		for (int i = 0; i < pageNodes.size(); i++) {
			RQLNode pageNode = pageNodes.get(i);

			RQLNode templateNode = pageNode.getNode("CONTENTCLASS");
			RQLNode templateFolderNode = pageNode.getNode("FOLDER");

            TemplateFolder templateFolder = new TemplateFolder(getProject(), templateFolderNode.getAttribute("name"), templateFolderNode.getAttribute("guid"));
            Template template = new Template(templateFolder, templateNode.getAttribute("name"), templateNode.getAttribute("guid"), "");

            result.add(new Page(getProject(), template, pageNode.getAttribute("guid"), pageNode.getAttribute("id"), pageNode.getAttribute("headline")));
        }
		return result;
	}

	/**
	 * Liefert nur die Anzahl der passenden Seiten zurück.
	 * 
	 * @throws RQLException
	 */
	public int countOnly() throws RQLException {
		/* 
		V7.5 response
		<IODATA>
		<PAGES hits="5351" timer1="0.187000000001484" />
		</IODATA>
		 */
		String rqlRequest = buildRequest(-2);
		String number = callCms(rqlRequest).getNode("PAGES").getAttribute("hits");
		return Integer.parseInt(number);
	}

	/**
	 * Liefert nur die Anzahl der passenden Seiten zurück.
	 * 
	 * @throws RQLException
	 */
	public int size() throws RQLException {
		return countOnly();
	}
	/**
	 * Started die konfigurierte Seitensuche und liefert die maximal die gegebene Anzahl Seiten zurück.
	 * 
	 * @param maxPages >
	 *            0 begrenzt die Anzahl auf genau diesen Wert
	 * @param maxPages =
	 *            -1 liefert alle Seiten
	 * @param maxPages =
	 *            -2 liefert nur die Anzahl der Seiten
	 */
	private String buildRequest(int maxPages) {
		/*
		V7.5 request
		<IODATA loginguid="C65B6A7ADADA4A97927E5D6E79DE28FD" sessionkey="8E6BD81979F8474395C3802F34EAA2AE">
		  <PAGE action="xsearch" maxhits="12000" orderby="changedate" orderdirection="DESC">
		    <SEARCHITEMS>
		      <SEARCHITEM key="specialpages" value="unlinked" operator="eq" />
		      <SEARCHITEM key="contentclassguid" value="B2B13AC01965432C8E2CBD9EC5B406C6" operator="eq" />
		    </SEARCHITEMS>
		  </PAGE>
		</IODATA>
		
		V7.5 response
		<IODATA>
		<PAGES searchguid="50CF5C912190413C9E9AE40246E576BC" hits="12000" page="1" pagesize="13000" maxhits="12000" tasklist="" projectguid="06BE79A1D9F549388F06F6B649E27152" timer1="9.7499999999979" timer2="6.95299999999983" orderby="changedate" orderdirection="DESC" groupby="" groupdirection="" >
		<PAGE   guid="2FAF4F5D5FFB4D6FA62A878C92692993" id="729541" headline="row NW1 (41)" flags="532480" mainlink="" status="1" >
		<CREATION  date="39617.3812152778" >
		<USER  guid="672AD112D6FC4E66AC50A19994D944D3" name="ignorco" fullname="Corinne Ignorek" email="ignorco@hlag.com" />
		</CREATION>
		<CHANGE  date="39617.3812615741" >
		<USER  guid="672AD112D6FC4E66AC50A19994D944D3" name="ignorco" fullname="Corinne Ignorek" email="ignorco@hlag.com" />
		</CHANGE>
		<RELEASE  date="39617.3812615741" >
		<USER  guid="672AD112D6FC4E66AC50A19994D944D3" name="ignorco" fullname="Corinne Ignorek" email="ignorco@hlag.com" />
		</RELEASE>
		<CONTENTCLASS  guid="B2B13AC01965432C8E2CBD9EC5B406C6" name="text_table_row" >
		<FOLDER  guid="6A6740BC44F7459081BFD1F25B1BF8F6" name="content_templates" />
		</CONTENTCLASS>
		</PAGE>
		...
		</PAGES>
		</IODATA>
		 */
		// call CMS
		String count = maxPages == -2 ? " option='countresults' " : "";
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PAGE action='xsearch' " + count
				+ " maxhits='" + maxPages + "' pagesize='" + maxPages + "' orderby='" + orderBy + "' orderdirection='" + orderDirection + "'>";
		if (searchItems.size() > 0) {
			rqlRequest += "<SEARCHITEMS>";
			for (PageSearchItem searchItem : searchItems) {
				rqlRequest += searchItem.getSearchItemTag();
			}
			rqlRequest += "</SEARCHITEMS>";
		}
		rqlRequest += "</PAGE></IODATA>";
		return rqlRequest;
	}

	/**
	 * Sendet eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
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
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	/**
	 * Liefert das Project, den Container der Seite.
	 * 
	 * @see Project
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
	 * 
	 */
	public String getSessionKey() {

		return getProject().getSessionKey();
	}

	/**
	 * Liefert die Logon GUID vom Container.
	 * 
	 * @see Project
	 */
	public String getLogonGuid() {

		return getProject().getLogonGuid();
	}

	/**
	 * Konfiguriert eine Sortierung nach Änderungsdatum.
	 */
	public void orderByLastChangedOnDate() {
		orderBy = "changedate";
	}

	/**
	 * Konfiguriert eine Sortierung nach Seiten ID.
	 */
	public void orderByPageId() {
		orderBy = "pageid";
	}

	/**
	 * Konfiguriert eine Sortierung nach Seitenüberschrift.
	 */
	public void orderByPageHeadline() {
		orderBy = "headline";
	}

	/**
	 * Konfiguriert eine Sortierung nach Erstellungsdatum.
	 */
	public void orderByCreatedOnDate() {
		orderBy = "createdate";
	}

	/**
	 * Konfiguriert eine Sortierung nach Kontentklasse.
	 */
	public void orderByContentClass() {
		orderBy = "contentclass";
	}

	/**
	 * Fordert eine aufsteigende Sortierung.
	 */
	public void orderAscending() {
		orderDirection = "ASC";
	}

	/**
	 * Fordert eine absteigende Sortierung.
	 */
	public void orderDescending() {
		orderDirection = "DESC";
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden nur verlinkte Seiten gesucht.
	 */
	public void addTypeCriteriaOnlyLinkedPages() {
		add(new PageSearchItem("specialpages", "linked", "eq"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden nur unverlinkte Seiten gesucht.
	 */
	public void addTypeCriteriaOnlyUnlinkedPages() {
		add(new PageSearchItem("specialpages", "unlinked", "eq"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden nur Seiten gesucht, deren letztes Änderungsdatum gleich dem gegebenen ist. Achtung: Da die Zeitangabe
	 * in diesem Datum immer mit eingeschlossen ist, kann man nicht einfach nur einen Tag finden.
	 */
	public void addLastChangedOnCriteriaEqual(ReddotDate lastChangedOn) {
		add(new PageSearchItem("changedate", lastChangedOn.toMsDoubleString(), "eq"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden nur Seiten gesucht, deren letztes Änderungsdatum ungleich dem gegebenen ist. Achtung: Gleiche
	 * Problematik wie bei equal.
	 * 
	 * @see #addLastChangedOnCriteriaEqual(ReddotDate)
	 */
	public void addLastChangedOnCriteriaNotEqual(ReddotDate lastChangedOn) {
		add(new PageSearchItem("changedate", lastChangedOn.toMsDoubleString(), "ne"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden nur Seiten gesucht, deren letztes Änderungsdatum größer dem gegebenen ist (=jünger).
	 */
	public void addLastChangedOnCriteriaGreaterThan(ReddotDate lastChangedOn) {
		add(new PageSearchItem("changedate", lastChangedOn.toMsDoubleString(), "gt"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden nur Seiten gesucht, deren letztes Änderungsdatum kleiner dem gegebenen ist (=älter).
	 */
	public void addLastChangedOnCriteriaLowerThan(ReddotDate lastChangedOn) {
		add(new PageSearchItem("changedate", lastChangedOn.toMsDoubleString(), "lt"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden nur Seiten gesucht, deren letztes Änderungsdatum größer oder gleich dem gegebenen ist (=jünger).
	 */
	public void addLastChangedOnCriteriaGreaterEqual(ReddotDate lastChangedOn) {
		add(new PageSearchItem("changedate", lastChangedOn.toMsDoubleString(), "ge"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es wird die Seite für die gegebenen Page ID gesucht.
	 */
	public void addPageIdCriteriaEqual(String pageId) {
		add(new PageSearchItem("pageid", pageId, "eq"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden nur Seiten gesucht, deren letztes Änderungsdatum kleiner oder gleich als dem gegebenen ist (=älter).
	 */
	public void addLastChangedOnCriteriaLowerEqual(ReddotDate lastChangedOn) {
		add(new PageSearchItem("changedate", lastChangedOn.toMsDoubleString(), "le"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden verlinkte und unverlinkte Seiten gesucht, aber keine Seiten im Papierkorb.
	 */
	public void addTypeCriteriaLinkedAndUnlinkedPages() {
		add(new PageSearchItem("specialpages", "active", "eq"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden verlinkte, unverlinkte und Seiten im Papierkorb gesucht.
	 * <p>
	 * Muss man wahrscheinlich nicht angeben.
	 */
	public void addTypeCriteriaAllPages() {
		add(new PageSearchItem("specialpages", "all", "eq"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden nur Seiten gesucht, die sich im Papierkorb.
	 */
	public void addTypeCriteriaOnlyPagesInRecycleBin() {
		add(new PageSearchItem("specialpages", "recyclebin", "eq"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden nur Seiten dieses Templates gesucht.
	 */
	public void addContentClassCriteriaEqual(Template contentClass) {
		add(new PageSearchItem("contentclassguid", contentClass.getTemplateGuid(), "eq"));
	}

	/**
	 * Fügt ein Suchkriterium hinzu. Es werden nur Seiten gesucht, die nicht auf dem gegebenen Template basieren.
	 */
	public void addContentClassCriteriaNotEqual(Template contentClass) {
		add(new PageSearchItem("contentclassguid", contentClass.getTemplateGuid(), "ne"));
	}

	/**
	 * Fügt ein Suchkriterium nach Seitenstatus hinzu. Es werden nur Seiten gesucht, die sich im Übersetzungsworkflow für die gegebenen sourceLanguage befinden.
	 */
	public void addStateCriteriaWaitingForTranslation(LanguageVariant sourceLanguage) {
		/*
		<SEARCHITEM sourcelanguage="ENG" key="pagestate" value="waitingfortranslation" operator="eq" displayvalue="" users="myself"></SEARCHITEM>
		 */
		add(new PageSearchItem("pagestate", "waitingfortranslation", "eq", "sourcelanguage='" + sourceLanguage.getLanguageCode()
				+ "' users='myself'"));
	}

    public void addKeywordCriteriaEqual(Keyword keyword){
        add(new PageSearchItem("keyword", keyword.getGuid(), "eq"));
    }

    public void addKeywordCriteriaNotEqual(Keyword keyword){
        add(new PageSearchItem("keyword", keyword.getGuid(), "ne"));
    }

}
