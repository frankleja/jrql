package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;

import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.PageComparator;
import com.hlcl.rql.util.as.PageIdPageComparator;
import com.hlcl.rql.util.as.SortedHeadlinesPageComparator;
import com.hlcl.rql.util.as.StandardFieldDateDescAndHeadlineAscPageComparator;
import com.hlcl.rql.util.as.StandardFieldDatePageComparator;
import com.hlcl.rql.util.as.StandardFieldTextPageComparator;
import com.hlcl.rql.util.as.TemplatesPageFilter;

/**
 * Diese Klasse beschreibt einen RedDot MultiLink (Container or Liste).
 * 
 * @author LEJAFR
 */
public abstract class MultiLink implements PageContainer, StructureElement {
	// private static int SORT_CRITERIA_ACCORDING_TO_ELEMENT = 2;
	private static int SORT_CRITERIA_PAGE_INFORMATION = 1;
	// private static String SORT_CRITERIA_PAGE_INFORMATION_CREATION_DATE = "3";
	private static String SORT_CRITERIA_PAGE_INFORMATION_HEADLINE = "1";
	// private static String SORT_CRITERIA_PAGE_INFORMATION_LAST_EDITOR = "6";
	// private static String SORT_CRITERIA_PAGE_INFORMATION_MODIFICATION_DATE = "2";
	// private static String SORT_CRITERIA_PAGE_INFORMATION_ORIGINAL_AUTHOR = "5";
	// private static String SORT_CRITERIA_PAGE_INFORMATION_PAGE_STATUS = "4";
	private static int SORT_MODE_ASCENDING = 2;
	// private static int SORT_MODE_DESCENDING = 3;

	// constants for sort settings
	private static int SORT_MODE_MANUAL = 1;
	protected final static String TREESEGMENT_TYPE = "link";
	// true, if this MultiLink is source for a reference to another Page/Link
	// displayd in tree in green and using >>>
	private boolean isReferenceSource;

	private String linkGuid;
	private String name;

	private Page page;
	private TemplateElement templateElement;

    private AppearanceSchedule appearanceSchedule;

	/**
	 * Container constructor comment.
	 * 
	 * @param page
	 *            Seite, die diesen Multi-Link beinhaltet.
	 * @param name
	 *            Name des Links auf Seite page
	 * @param linkGuid
	 *            GUID des Links auf Seite page
	 * @param isReferenceSource
	 *            Link referenziert ein anderes Element
	 * @see Project#getMultiLinkByGuid(String)
	 */
	public MultiLink(Page page, TemplateElement templateElement, String name, String linkGuid, boolean isReferenceSource) {
		super();

		this.page = page;
		this.templateElement = templateElement;
		this.name = name;
		this.linkGuid = linkGuid;
		this.isReferenceSource = isReferenceSource;
	}

	/**
	 * Ordnet diesem MultiLink das gegebene Berechtigungspaket zu.
	 */
	public void assignAuthorizationPackage(AuthorizationPackage authorizationPackage) throws RQLException {

		// check type
		if (!authorizationPackage.isDetailedLinkAuthorizationPackage()) {
			throw new WrongTypeException("Authorization package with name " + authorizationPackage.getName()
					+ " has wrong type and cannot be linked to MultiLink " + getName() + " in page " + getPage().getHeadlineAndId()
					+ ".");
		}

		// convenience call
		getProject().assignAuthorizationPackage("LINK", getLinkGuid(), authorizationPackage);
	}

	/**
	 * Ordnet diesem MultiLink das gegebene Exportpaket zu.
	 * 
	 * @param inherit
	 *            =true, Exportpaket wird an alle Unterstrukturen vererbt.
	 */
	public void assignPublicationPackage(PublicationPackage publicationPackage, boolean inherit) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="D146BD4786B4467F96D2E587701EEB3C" sessionkey="1021834323A168rr1234y">
		 <PROJECT>
		 <EXPORTPACKET action="save" guid="BA80D6AB81F145B3922738CC952BF6DD" inherit="1" linkguid="ACB0C9CD09664176AC57004F76B4DBE2"/>
		 </PROJECT>
		 </IODATA> 
		 V5 response
		 <IODATA><EXPORTPACKET action="save" guid="BA80D6AB81F145B3922738CC952BF6DD" inherit="1" linkguid="ACB0C9CD09664176AC57004F76B4DBE2" languagevariantid="ENG" key="1021834323A168rr1234y" parentobjectname="PROJECT" useconnection="1" dialoglanguageid="ENG"/>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + " 	<PROJECT>"
				+ "		<EXPORTPACKET action='save' guid='" + publicationPackage.getPublicationPackageGuid() + "' linkguid='"
				+ getLinkGuid() + "'" + (inherit ? " inherit='1'" : "") + " />" + "	</PROJECT>" + "</IODATA>";
		callCms(rqlRequest);
	}

	/**
	 * Ordnet diesem MultiLink den Workflow mit der gegebenen GUID zu.
	 */
	public void assignWorkflow(Workflow workflow, boolean inherit) throws RQLException {
		/* 
		 V5 request (without language variant)
		 <IODATA loginguid="70873D469F7549D1A312CDDA0E7CABA4">
		 <WORKFLOW sessionkey="69175087813744xXf750">
		 <LINK action="assign" guid="AD9CEA3B7E6C4B3DBAF4FF702C8975F6">
		 <WORKFLOW guid="AEDD9522F8194B1389FCE9700C8C3271" />
		 </LINK>
		 </WORKFLOW>
		 </IODATA>
		 V5 request (with language variant - not used here)
		 <IODATA loginguid="70873D469F7549D1A312CDDA0E7CABA4">
		 <WORKFLOW sessionkey="69175087813744xXf750">
		 <LINK action="assign" guid="AD9CEA3B7E6C4B3DBAF4FF702C8975F6">
		 <WORKFLOW guid="AEDD9522F8194B1389FCE9700C8C3271">
		 <LANGUAGEVARIANTS><LANGUAGEVARIANT language="XXXX"/></LANGUAGEVARIANTS>
		 </WORKFLOW>
		 </LINK>
		 </WORKFLOW>
		 </IODATA>
		 
		 V5 response
		 <IODATA>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "	<WORKFLOW sessionkey='" + getSessionKey() + "'>"
				+ "   	<LINK action='assign' guid='" + getLinkGuid() + "'>" + "			<WORKFLOW guid='" + workflow.getWorkflowGuid()
				+ "'/>" + "		</LINK>" + "	</WORKFLOW>" + "</IODATA>";
		callCms(rqlRequest);

		// inherit
		if (inherit) {
			workflow.inherit(this);
		}
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
	 * Ändert die Seitensortierung der angehängten Kindseiten entsprechend der gegebenen Liste.
	 */
	private void changeOrder(PageArrayList childs) throws RQLException {
		/* 
		 V5 request (save page order)
		 <IODATA loginguid="FE4D2C44BAEC43DD9CAB3D4DC262763E" sessionkey="42113976861d16oBRlOJ">
		 <LINK action="save" guid="A71E9253472845629DBA1C5C813A953E" orderby="5">
		 <PAGES  action="saveorder">
		 <PAGE guid="8CE42BC44CDE4BAB9F6BE54C377A6A93" />
		 <PAGE guid="202ECB7C834542C582F6A30410195664" />
		 <PAGE guid="F45146658C6347CDBB4A2311902985E3" />
		 <PAGE guid="9CE9B1A9C1AB462E8B96963548D191FF" />
		 </PAGES>
		 </LINK>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <LINK action="save" sessionkey="421139565e46x3Bm8C61" dialoglanguageid="ENG" languagevariantid="ENG" defaultlanguagevariantid="ENG">
		 <PAGES linkguid="" targetlinkguid="" parenttable="PGE" sessionkey="421139565e46x3Bm8C61" languagevariantid="ENG" orderby="" action="saveorder">
		 <PAGE guid="8CE42BC44CDE4BAB9F6BE54C377A6A93"/>
		 <PAGE guid="202ECB7C834542C582F6A30410195664"/>
		 <PAGE guid="F45146658C6347CDBB4A2311902985E3"/>
		 <PAGE guid="9CE9B1A9C1AB462E8B96963548D191FF"/>
		 </PAGES>
		 </LINK>
		 </IODATA>
		 */
		// check if MultiLink is manually sorted!
		if (!isManuallySorted()) {
			throw new WrongSortModeException("The Multi-Link with the GUID " + getLinkGuid() + " on page "
					+ getPage().getHeadlineAndId() + " is not manually sorted, therefore you cannot sort childs.");
		}

		// do noting, if no or only one page linked
		if (childs.size() <= 1)
			return;

		// build new request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "  <LINK action='save' guid='" + getLinkGuid() + "' orderby='5'>" + "	<PAGES action='saveorder'>";

		// add all childs in given order
		Page child = null;
		for (int i = 0; i < childs.size(); i++) {
			child = (Page) childs.get(i);
			rqlRequest = rqlRequest + "<PAGE guid='" + child.getPageGuid() + "'/>";
		}

		// add request tail
		rqlRequest = rqlRequest + "</PAGES>" + "</LINK>" + "</IODATA>";

		callCms(rqlRequest);
	}

    /**
     * Ordnet eine Liste child pages nach der Reihenfolge der übergebenen pages
     *
     * @param pages
     * @throws RQLException
     */
    public void applyChildPageOrderByPages(java.util.List<Page> pages) throws RQLException {
        this.changeOrder(new PageArrayList(pages));
    }

    /**
     * Ordnet eine Liste child pages nach der Reihenfolge der übergebenen page GUIDS
     *
     * @param pageGuids
     * @throws RQLException
     */
    public void applyChildPageOrderByPageGuids(java.util.List<String> pageGuids) throws RQLException {

        java.util.List<Page> pages = new ArrayList<Page>(pageGuids.size());
        for (String pageGuid : pageGuids) {
            pages.add(new Page(this.getProject(), pageGuid));
        }

        this.changeOrder(new PageArrayList(pages));
    }

	/**
	 * Ändert den Sortierungsmodus dieses Multilinks (nur für Liste getestet!).
	 * 
	 * @param maxCount
	 *            maximale Anzahl angezeigter Seiten (0=unbegrenzt)
	 * @param sortMode
	 *            1=manual, 2=ascending, 3=descending
	 * @param sortCriteriaType
	 *            1=internal page information, 2=according to element
	 * @param sortCriteriaValue
	 *            falls sortCriteriaType=1: eine Zahl auf SORT_CRITERIA_PAGE_INFORMATION_* falls sortCriteriaType=2: ein Elementname
	 *            der hier angehängten Seiten
	 * @throws RQLException
	 */
	private void changeSortMode(int maxCount, int sortMode, int sortCriteriaType, String sortCriteriaValue) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="2B2D9227CD704459809D56073EC405CA">
		 <PROJECT sessionkey="1021834323DI6n4308Km7">
		 <PAGE>
		 <ELEMENT guid="4B857EC6EED34B2C8CCB8129B7755ACF" >
		 <SORTSETTING action="save" maxcount="#1021834323DI6n4308Km7" sortmode="2" sortelement="1" sortelementname="1" />
		 <DISPLAYFILTERS action="save" perkeyword="0" />
		 </ELEMENT>
		 </PAGE>
		 </PROJECT>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <ELEMENT guid="4B857EC6EED34B2C8CCB8129B7755ACF" languagevariantid="ENG" dialoglanguageid="ENG" action="" parentguid="" parenttable="PAG">
		 <SORTSETTING action="save" maxcount="#1021834323DI6n4308Km7" sortmode="2" sortelement="1" sortelementname="1" linkguid="4B857EC6EED34B2C8CCB8129B7755ACF" targetlinkguid="" parenttable="PGE" sessionkey="" languagevariantid="ENG" guid="7A8E13C8FCEC416EA47118A4C1392F9F"/>
		 <DISPLAYFILTERS action="save" perkeyword="0" linkguid="4B857EC6EED34B2C8CCB8129B7755ACF" targetlinkguid="" parenttable="PGE" sessionkey="" languagevariantid="ENG"/>
		 </ELEMENT>
		 </IODATA>
		 */

		// call CMS
		String maxCountAttribute = maxCount == 0 ? "maxCount='#" + getSessionKey() + "' " : "maxCount='" + Integer.toString(maxCount)
				+ "'";
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "	<PROJECT sessionkey='" + getSessionKey() + "'>"
				+ "		<PAGE>" + "   		<ELEMENT guid='" + getLinkGuid() + "'>" + "				<SORTSETTING action='save' " + maxCountAttribute
				+ " sortmode='" + sortMode + "' sortelement='" + sortCriteriaType + "' sortelementname='" + sortCriteriaValue + "'/>"
				+ "				<DISPLAYFILTERS action='save' perkeyword='0' />" + " 			</ELEMENT>" + " 		</PAGE>" + "	</PROJECT>"
				+ "</IODATA>";

		// ignore result
		callCms(rqlRequest);
	}

	/**
	 * Verlinkt alle Kindseiten dieses Links an den gegebenen Ziellink. Hängt alle Seiten unten an.
	 * <p>
	 * Alle Kindseiten bleiben an diesem Link erhalten. Der Mainlink wird nicht verändert.
	 * 
	 * @param targetMultiLink
	 *            Link, an alle Kindseiten dieses Links ebenfalls angehängt werden sollen
	 */
	public void connectAllChildsTo(MultiLink targetMultiLink) throws RQLException {

		// first connect to new multi link
		PageArrayList childs = getChildPages();
		for (int i = 0; i < childs.size(); i++) {
			Page child = (Page) childs.get(i);
			targetMultiLink.connectToExistingPage(child, true);
		}
	}

	/**
	 * Linkt die gegebenen Seite an diese Liste oder Container. Die targetPage wird an das Ende dieses MultiLinks verschoben. Der
	 * Hauptlink bleibt unverändert.
	 * 
	 * @param targetPage
	 *            Seite, die an diesen MultiLink angehängt wird
	 */
	public void connectToExistingPage(Page targetPage) throws RQLException {

		// page will be added at bottom
		connectToExistingPage(targetPage, true);
	}

	/**
	 * Linkt die gegebenen Seite an diese Liste oder Container.
	 * 
	 * @param targetPage
	 *            Seite, die an diesen MultiLink angehängt wird
	 * @param addAtBottom
	 *            verschiebt die targetPage an das Ende aller Kindseiten
	 */
	public void connectToExistingPage(Page targetPage, boolean addAtBottom) throws RQLException {
		connectToExistingPage(targetPage, addAtBottom, false);
	}

	/**
	 * Linkt die gegebenen Seite an diese Liste oder Container. Das RQL tut nichts (und liefert keinen Fehler), falls der Autor auf der
	 * targetPage keine Rechte hat.
	 * 
	 * @param targetPage
	 *            Seite, die an diesen MultiLink angehängt wird
	 * @param addAtBottom
	 *            verschiebt die targetPage an das Ende aller Kindseiten
	 * @param setMainLink
	 *            Ändert den Mainlink der targetPage auf diesen Link
	 */
	public void connectToExistingPage(Page targetPage, boolean addAtBottom, boolean setMainLink) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="ACE152EE25AF45D68E0E43DD22FB4C3D" sessionkey="784733788GX2Fr1e1F20">
		 <LINKSFROM action="save" pageguid="D5719843A97B460D8E86A20555CBF153" >
		 <LINK guid="DC3BF43339514CCCBD28E4C3F4EFE209" />
		 </LINKSFROM>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <LINKSFROM action="save" pageguid="D5719843A97B460D8E86A20555CBF153" sessionkey="784733788GX2Fr1e1F20" dialoglanguageid="ENG" linkednew="1" useconnection="1" userguid="4324D172EF4342669EAF0AD074433393" languagevariantid="DEU">
		 <LINK ok="1" guid="DC3BF43339514CCCBD28E4C3F4EFE209" templateelementguid="0FAE6A79FF664B11844428241EA8943A" pageguid="430AC5C25CA4406F9B7D2A12DE3BF071" eltflags="33555456" flags="33555456" eltrequired="0" islink="2" formularorderid="0" orderid="1" status="0" name="inhalts_container" eltname="inhalts_container" aliasname="inhalts_container" variable="inhalts_container" folderguid="" istargetcontainer="1" hastargetcontainerreferences="0" type="28" elttype="28" templateelementflags="33555456" templateelementislink="2" reddotdescription="" rights2="2147483647" value="inhalts_container" linkednew="1" languagevariantid="DEU" elementguid=""/>
		 </LINKSFROM>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <LINKSFROM action='save' pageguid='" + targetPage.getPageGuid() + "'>" + "   <LINK guid='" + getLinkGuid() + "'/>"
				+ " </LINKSFROM>" + "</IODATA>";

		// ignore response
		callCms(rqlRequest);

		// move new page to bottom, if requested and possible
		if (addAtBottom && isManuallySorted()) {
			moveFirstPageToLastPosition();
		}

		// adjust main link
		if (setMainLink) {
			targetPage.setMainLink(this);
		}
	}

	
	
	public void connectToExistingPage(String targetPageGuid) throws RQLException {
		connectToExistingPage(new Page(this.getProject(), targetPageGuid));
	}
	
	
	public void connectToExistingPage(String targetPageGuid, boolean addAtBottom) throws RQLException {
		connectToExistingPage(new Page(this.getProject(), targetPageGuid), addAtBottom);
	}
	
	
	public void connectToExistingPage(String targetPageGuid, boolean addAtBottom, boolean setMainLink) throws RQLException {
		connectToExistingPage(new Page(this.getProject(), targetPageGuid), addAtBottom, setMainLink);
	}
	
	
	/**
	 * Linkt die gegebenen Seiten (die Reihenfolge wird beibehalten) an diese Liste oder Container.
	 * 
	 * @param targetPages
	 *            Seiten, die an diesen MultiLink angehängt wird
	 * @param addAtBottom
	 *            die gegebenen Seiten kommen (in ihrer Reihenfolge unverändert) unter oder über die bereits gelinkten Seiten
	 * @param setMainLink
	 *            Ändert den Mainlink für alle Seiten in targetPages auf diesen Link
	 */
	public void connectToExistingPages(java.util.List<Page> targetPages, boolean addAtBottom, boolean setMainLink) throws RQLException {
		if (addAtBottom) {
			for (Page targetPg : targetPages) {
				// move it
				connectToExistingPage(targetPg, true, setMainLink);
			}
		} else {
			// connect all at top
			for (int i = targetPages.size() - 1; i >= 0; i--) {
				Page targetPg = targetPages.get(i);
				connectToExistingPage(targetPg, false, setMainLink);
			}
		}
	}

    /**
     * Linkt die gegebenen Seiten GUIDS (die Reihenfolge wird beibehalten) an diese Liste oder Container.
     *
     * @param targetPageGuids
     *            Seiten GUIDS, die an diesen MultiLink angehängt wird
     * @param addAtBottom
     *            die gegebenen Seiten kommen (in ihrer Reihenfolge unverändert) unter oder über die bereits gelinkten Seiten
     * @param setMainLink
     *            Ändert den Mainlink für alle Seiten in targetPages auf diesen Link
     */
    public void connectToExistingPages(String[] targetPageGuids, boolean addAtBottom, boolean setMainLink) throws RQLException {

        java.util.List<Page> pages = new ArrayList<Page>(targetPageGuids.length);

        for (int i = 0; i < targetPageGuids.length; i++) {
            String targetPageGuid = targetPageGuids[i];
            pages.add(new Page(this.getProject(), targetPageGuid));
        }

        connectToExistingPages(pages, addAtBottom, setMainLink);
    }
    
    
    /**
     * Create a URL mini-page and put add it to this container. 
     * 
     * @param url actual link target
     * @param target frame target (_top, _blank etc)
     * @param headline name of this link, headine of the mini-page
     */
    public void connectToRedirectUrl(String url, String target, String headline) throws RQLException
    {
    	StringBuilder sb = new StringBuilder(128);
    	
		sb.append("<IODATA loginguid='").append(getLogonGuid()).append("' sessionkey='").append(getSessionKey()).append("'>")
		  .append("<LINK action='assign' guid='").append(getLinkGuid()).append("'>")
		  .append("<PAGE action='addnew' target='")
		  	.append(StringHelper.escapeHTML(target)).append("' headline='")
		  	.append(StringHelper.escapeHTML(headline)).append("'>")
		  .append("<URL action='assign' src='")
		  	.append(StringHelper.escapeHTML(url)).append("' />")
		  .append("</PAGE></LINK></IODATA>");
		callCms(sb.toString()); // check result?
    }
    
    

	/**
	 * Kopiert alle Kindseiten (nur der 1. level) von sourceMultiLink an diese Liste. Die Werte der content elements werden mit
	 * kopiert.
	 * 
	 * @param ignoreElementNames
	 *            Liste of template element names, deren Werte nicht kopiert werden sollen
	 * @param separator
	 *            Trennzeichen der Namen
	 * @param copyAllContainersChildren
	 *            falls true werden kindseiten von containerelement der source children mit kopiert
	 */
	protected void  copyChildrenWithContentFrom(MultiLink sourceMultiLink, String ignoreElementNames, String separator,
			boolean copyAllContainersChildren) throws RQLException {

		for (Page sourceChild : sourceMultiLink.getChildrenReversed()) {
			Template sourceTemplate = sourceChild.getTemplate();
			Page targetChild = createAndConnectPage(sourceTemplate, sourceChild.getHeadline(), false);
			// exclude referenced content elements and headline
			targetChild.copyContentElementValuesFrom(sourceChild, false, false, ignoreElementNames, separator);

			// check of containers needs to be handled
			if (copyAllContainersChildren) {
				// for all containers of source child do
				for (Container childSourceCtr : sourceChild.getContainerElements()) {
					// get source and target ctr
					Container childTargetCtr = targetChild.getContainer(childSourceCtr.getTemplateElementName());
					// use this copy function for only 1st level copy
					childTargetCtr.copyChildrenWithContentFrom(childSourceCtr, ignoreElementNames, separator);
				}
			}
		}
	}

	/**
	 * Erzeugt eine neue Seite basierend auf dem einzig zugelassen Template. Die Seite wird an diesem MultiLink nach unten verschoben.
	 * 
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 * @throws AmbiguousTemplateException
	 */
	public Page createAndConnectPage(String headline) throws RQLException {
		return createAndConnectPage(headline, true);
	}

	/**
	 * Erzeugt eine neue Seite basierend auf dem einzig zugelassen Template.
	 * 
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 * @param addAtBottom
	 *            true=>seite wird nach unten verschoben, false=>seite wird nicht verschoben und RD default is oben anlegen
	 * @throws AmbiguousTemplateException
	 */
	public Page createAndConnectPage(String headline, boolean addAtBottom) throws RQLException {
		// get the only one template
		java.util.List<Template> templates = getAllowedTemplates();
		if (templates.size() != 1) {
			throw new AmbiguousTemplateException("Create of new page on link " + this.getName() + " in page "
					+ this.getPage().getHeadlineAndId()
					+ " failed, because template cannot be determined. Maybe more than one preassigned.");
		}

		// new page will be added at bottom
		return createAndConnectPage((Template) templates.get(0), headline, addAtBottom);
	}

	/**
	 * Erzeugt eine neue Seite basierend auf template an diesem Container. Die neue Seite wird per default unten angehängt. Die
	 * Templatevorbelegung wird dabei nicht geprueft!
	 * 
	 * @param template
	 *            Typ der neu erstellten Seite.
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 */
	public Page createAndConnectPage(Template template, String headline) throws RQLException {

		// new page will be added at bottom
		return createAndConnectPage(template, headline, true);
	}

	/**
	 * Erzeugt eine neue Seite basierend auf template an diesem Container. Die Templatevorbelegung wird dabei nicht geprueft!
	 * 
	 * @param template
	 *            Typ der neu erstellten Seite.
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 * @param addAtBottom
	 *            true=>seite wird nach unten verschoben, false=>seite wird nicht verschoben und RD default is oben anlegen
	 */
	public Page createAndConnectPage(Template template, String headline, boolean addAtBottom) throws RQLException {
		// create
		Page page = createAndConnectPagePrimitive(template, headline);

		// move new page to bottom, if requested and possible
		if (addAtBottom && isManuallySorted()) {
			moveFirstPageToLastPosition();
		}

		return page;
	}

	/**
	 * Erzeugt eine neue Seite basierend auf template an diesem MultiLink. Die Templatevorbelegung wird dabei nicht geprueft!
	 * 
	 * @param template
	 *            Typ der neu erstellten Seite.
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 * @param positionChild
	 *            the page after the new page will be created; has to be a child of this MultiLink!
	 */
	public Page createAndConnectPageBehind(Template template, String headline, Page positionChild) throws RQLException {
		// check sort mode first
		if (!isManuallySorted()) {
			throw new WrongSortModeException("The Multi-Link with name " + getName() + " on page " + getPage().getHeadlineAndId()
					+ " is not manually sorted, therefore you cannot create a page at a specific position.");
		}
		// create the page
		Page page = createAndConnectPagePrimitive(template, headline);

		// move new page after the given page
		movePageBehind(getChildPagesNodeList(), page.getPageGuid(), positionChild.getPageGuid());

		return page;
	}

	/**
	 * Erzeugt eine neue Seite basierend auf template an diesem MultiLink. Die Templatevorbelegung wird dabei nicht geprueft!
	 * 
	 * @param template
	 *            Typ der neu erstellten Seite.
	 * @param headline
	 *            Überschrift der neu erstellten Seite
	 */
	private Page createAndConnectPagePrimitive(Template template, String headline) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="C2FFCC097AA64EE594B583FD0BF4A8F5" sessionkey="4211388671o4w56GcjW4">
		 <LINK action="assign" guid="A71E9253472845629DBA1C5C813A953E">
		 <PAGE action="addnew" templateguid="1951E69D65264B4EB73539D96AE35349" headline="test page"/>
		 </LINK>
		 </IODATA> 
		 V5 response
		 <IODATA>
		 <LINK ok="1" sessionkey="4211388671o4w56GcjW4" dialoglanguageid="ENG" languagevariantid="ENG" guid="A71E9253472845629DBA1C5C813A953E" templateelementguid="3DED4381C1314A348138D73E97346803" pageguid="28457BD66F9B45A18B9E69F8F9DABAD3" eltflags="0" flags="0" eltrequired="0" islink="2" formularorderid="0" orderid="1" status="0" name="container" eltname="container" aliasname="container" variable="container" folderguid="" istargetcontainer="0" type="28" elttype="28" templateelementflags="0" templateelementislink="2" value="container" reddotdescription="" action="assignnewpage" useconnection="1" userguid="1B9A283122B741809F02C456CB77C43B">
		 <PAGE headline="name" pageid="1087" guid="3A674B3ABC7C42D5A61926D3B42E60B1" tmpeditlinkguid="A71E9253472845629DBA1C5C813A953E" dialoglanguageid="ENG" emailreceiver="" emailsubject="" templateguid="1951E69D65264B4EB73539D96AE35349" mainlinkguid="A71E9253472845629DBA1C5C813A953E" id="1087" attributeid="2" changed="-1" useconnection="1" userguid="1B9A283122B741809F02C456CB77C43B" action="assumetemplatedefaults" linkednew="1" pgeguid="9D44ECC90D734F93854DB77451DF27F8" newguid="3A674B3ABC7C42D5A61926D3B42E60B1" elementguid="A71E9253472845629DBA1C5C813A953E" linkguid="A71E9253472845629DBA1C5C813A953E" targetlinkguid="" parenttable="PGE" sessionkey="4211388671o4w56GcjW4" languagevariantid="ENG">
		 <ELEMENTS pageguid="3A674B3ABC7C42D5A61926D3B42E60B1" action="load" childnodesasattributes="-1" parentguid="1951E69D65264B4EB73539D96AE35349" parenttable="TPL" editlinkguid="A71E9253472845629DBA1C5C813A953E" languagevariantid="ENG" dialoglanguageid="ENG">
		 <ELEMENT languagevariantid="ENG" eltformatbutton="2" eltparentelementguid="A1745B8215FB4345BE191E79132715F0" eltformatno="0" templateguid="1951E69D65264B4EB73539D96AE35349" eltname="back_link_text" elttype="1" eltid="24" eltislink="0" eltfolderguid="" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="1" eltinvisibleinpage="0" eltdonothtmlencode="0" eltuserdefinedallowed="1" eltextendedlist="0" eltdefaultvalue="Back to tree" guid="13A8F978F2AA4FC2B14C2FF888EB7AB5" pageguid="3A674B3ABC7C42D5A61926D3B42E60B1" templateelementguid="928F3CE929294E399840866156AABE71" islink="0"/>
		 ....
		 <ELEMENT languagevariantid="ENG" templateguid="1951E69D65264B4EB73539D96AE35349" eltname="blocks_bottom" elttype="28" eltid="19" eltislink="2" eltfolderguid="" eltistargetcontainer="0" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0" guid="7777525857E543D1B2778568EB9B8A6A" pageguid="3A674B3ABC7C42D5A61926D3B42E60B1" templateelementguid="C33AF18345E34448A48D09B991695125" islink="2"/>
		 </ELEMENTS>
		 </PAGE>
		 </LINK>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <LINK action='assign' guid='" + getLinkGuid() + "'>" + "   <PAGE action='addnew' templateguid='"
				+ template.getTemplateGuid() + "' headline='" + StringHelper.escapeHTML(headline) + "'/>" + " </LINK>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		// check link ok status
		RQLNode linkNode = rqlResponse.getNode("LINK");
		if (!linkNode.getAttribute("ok").equals("1")) {
			throw new RQLException(
					"<LINK ok<>'1'...>: Daten konnten nicht gelesen werden, da das Linkelement nicht für die Dialoganzeige vorgesehen ist.");
		}

		// wrap page data
		RQLNode pageNode = rqlResponse.getNode("PAGE");
		return new Page(getProject(), template, pageNode.getAttribute("guid"), pageNode.getAttribute("pageid"), headline);
	}

	/**
	 * Löst die Verlinkung aller Kindseiten von diesem Link.
	 * <p>
	 * Danach besitzt dieser Link keine Kindseiten mehr.
	 */
	public void disconnectAllChilds() throws RQLException {
		disconnectAllChilds(getChildPages());
	}

	/**
	 * Löst die Verlinkung der gegebenen Kindseiten von diesem Link.
	 * <p>
	 * Zur Performancesteigerung nur intern genutzt.
	 */
	private void disconnectAllChilds(java.util.List<Page> childPages) throws RQLException {
		/* 
		 V5 request (disconnect pages)
		 <IODATA loginguid="78905ACA0C614D15BC6DB1F6748405E4" sessionkey="925818745bOSRp7gXR85">
		 <LINK action="save" guid="20467D958D7743488A2356B6CC1099D0">
		 <PAGES>
		 <PAGE deleted ="1" guid="4BB16A9C4BDF423F8CB9A631B5F59BC2" />
		 ...
		 </PAGES>
		 </LINK>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <LINK action="save" reddotcacheguid="6EF8582D8DA6425D8FA620FD2A617116" sessionkey="925818745bOSRp7gXR85" dialoglanguageid="ENG" languagevariantid="ENG" defaultlanguagevariantid="ENG" guid="20467D958D7743488A2356B6CC1099D0" type="13" pageguid="EF0B9745F3774A0DAA3D7EF4819F6428" useconnection="1" userguid="4324D172EF4342669EAF0AD074433393">
		 <PAGES linkguid="20467D958D7743488A2356B6CC1099D0" targetlinkguid="" parenttable="PGE" sessionkey="925818745bOSRp7gXR85" orderby="" languagevariantid="ENG" elementguid="20467D958D7743488A2356B6CC1099D0" action="unlink" guid="20467D958D7743488A2356B6CC1099D0">
		 <PAGE deleted="1" guid="FBB4B76094F8472CBA543C28E2FB2EDF" action="" linkguid="20467D958D7743488A2356B6CC1099D0" mainlinkguid="20467D958D7743488A2356B6CC1099D0" pageguid="FBB4B76094F8472CBA543C28E2FB2EDF" languagevariantid="ENG"/>
		 <PAGE deleted="1" guid="FBD05727F00042FDBA671F90743B5D3B" action="" linkguid="20467D958D7743488A2356B6CC1099D0" mainlinkguid="20467D958D7743488A2356B6CC1099D0" pageguid="FBD05727F00042FDBA671F90743B5D3B" languagevariantid="ENG"/>
		 </PAGES>
		 </LINK>
		 </IODATA>
		 */

		// only needed if children given
		if (childPages.size() > 0) {
			// disconnect all given childs from here
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ " <LINK action='save' guid='" + getLinkGuid() + "'>" + "  <PAGES>";
			for (int i = 0; i < childPages.size(); i++) {
				Page child = (Page) childPages.get(i);
				rqlRequest += "<PAGE deleted ='1' guid='" + child.getPageGuid() + "' />";
			}
			rqlRequest += "  </PAGES>" + " </LINK>" + "</IODATA>";
			callCms(rqlRequest);
		}
	}

	/**
	 * Löst die Verlinkung der gegebenen Kindseite von diesem Link.
	 * <p>
	 * Es findet keine Prüfung statt, ob die gegebenen Kindseite ein Child dieses MultiLinks ist.
	 */
	public void disconnectChild(Page childPage) throws RQLException {

		// wrap into list to reuse method
		java.util.List<Page> childs = new java.util.ArrayList<Page>();
		childs.add(childPage);
		disconnectAllChilds(childs);
	}

	/**
	 * Löst die Verlinkung der Kindseite mit der gegebenen PageId von diesem Link.
	 * <p>
	 * Es findet keine Prüfung statt, ob die gegebenen Kindseite ein Child dieses MultiLinks ist.
	 */
	public void disconnectChildByPageId(String pageId) throws RQLException {

		Page childPage = getProject().getPageById(pageId);
		if (childPage == null) {
			throw new RQLException("Disconnect child page fails, because no page with page id " + pageId + " found.");
		}
		disconnectChild(childPage);
	}

	/**
	 * Löst die Verlinkung der gegebenen Kindseiten von diesem Link.
	 * <p>
	 * Es findet keine Prüfung statt, ob die gegebenen Kindseite ein Child dieses MultiLinks ist.
	 */
	public void disconnectChildren(PageArrayList childrenToDisconnect) throws RQLException {
		disconnectAllChilds(childrenToDisconnect);
	}

	/**
	 * Löst die Verlinkung der Kindseiten von diesem Link.
	 * <p>
	 * Es findet keine Prüfung statt, ob die gegebenen Kindseite ein Child dieses MultiLinks ist. Solch falsche requests werden dann
	 * einfach irgnoriert.
	 */
	public void disconnectChilds(String[] pageGuids) throws RQLException {

		// try only if not empty
		if (pageGuids.length != 0) {
			// wrap into page list
			java.util.List<Page> childs = new java.util.ArrayList<Page>();
			for (int i = 0; i < pageGuids.length; i++) {
				String pageGuid = pageGuids[i];
				childs.add(getProject().getPageByGuid(pageGuid));
			}
			// disconnect all with only one RQL
			disconnectAllChilds(childs);
		}
	}

	/**
	 * Gleiche CMS Links werden als identisch betrachtet.
	 */
	public boolean equals(Object obj) {

		MultiLink second = (MultiLink) obj;
		return this.getLinkGuid().equals(second.getLinkGuid());
	}

	/**
	 * Sucht in den Childseiten die Seite mit der gegebenen headline. Liefert null, falls keine Seite mit der gegebenen headline
	 * gefunden wurde.
	 */
	public Page findChildPageByHeadline(String headline) throws RQLException {

		PageArrayList childs = getChildPages();

		for (int i = 0; i < childs.size(); i++) {
			Page child = (Page) childs.get(i);
			if (child.getHeadline().equals(headline)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Liefert eine String-Liste mit allen hier anlinkbaren Template Namen.
	 */
	public String getAllowedTemplateNames(String delimiter) throws RQLException {

		java.util.List<Template> templates = getAllowedTemplates();
		StringBuilder buffer = new StringBuilder(64);

		for (int i = 0; i < templates.size(); i++) {
			Template t = (Template) templates.get(i);
			buffer.append(t.getName()).append(delimiter);
		}
		return buffer.toString();
	}

	/**
	 * Liefert eine Liste mit allen hier anlinkbaren Templates.
	 */
	public java.util.List<Template> getAllowedTemplates() throws RQLException {

		return getTemplateElement().getPreassignedTemplates();
	}

	/**
	 * Liefert alle an diesen Multilink angehängten Seiten zurück, die für den angemeldeten User änderbar sind.
	 * 
	 * @return PageArrayList list of Pages; Liste ist leer falls keine Seiten angehängt sind (oder auf einen anderen Link verwiesen
	 *         wird)
	 */
	public PageArrayList getChangeableChildPages() throws RQLException {

		return getChildPages().selectAllChangeablePages();
	}

	/**
	 * Liefert die Anzahl aller an diesen Multilink angehängten Seiten zurück, die für den angemeldeten User änderbar sind.
	 */
	public int getChangeableChildPagesSize() throws RQLException {

		return getChildPages().selectAllChangeablePages().size();
	}

	/**
	 * Liefert die einzige an diesen MultiLink angehängte Seite.
	 */
	public Page getChildPage() throws RQLException {

		PageArrayList children = getChildPages();

		// check number of childs
		if (children.size() > 1) {
			throw new MultiChildPagesException("The MultiLink " + getName() + "(guid=" + getLinkGuid() + ") in page with id "
					+ getPage().getPageId() + " has more than one child page linked.");
		}

		return (Page) children.get(0);
	}


	/**
	 * Liefert alle an diesen Multilink angehängten Seiten zurück.
	 * 
	 * @return PageArrayList list of Pages; Liste ist leer falls keine Seiten angehängt sind (oder auf einen anderen Link verwiesen
	 *         wird)
	 */
	public PageArrayList getChildPages() throws RQLException {

		PageArrayList pages = new PageArrayList();

		RQLNodeList pageNodeList = getChildPagesNodeList();

		// no childs found; could be empty or referenced link
		if (pageNodeList == null) {
			return pages;
		}

		for (int i = 0; i < pageNodeList.size(); i++) {
			RQLNode pageNode = pageNodeList.get(i);
			String headline = pageNode.getAttribute("headline");

			// For redirect="1" there is an additional " (URL)" at the end of the headline ...
			if ("1".equals(pageNode.getAttribute("redirect"))) {
				String suffix = " (URL)";
				if (headline != null && headline.endsWith(suffix)) {
					headline = headline.substring(0, headline.length() - suffix.length());
				}
			}
			Page p = new Page(getProject(), pageNode.getAttribute("guid"),
					          pageNode.getAttribute("id"), headline);
			pages.add(p);
		}

		return pages;
	}


	/**
	 * Liefert alle an diesen Multilink angehängten Seiten zurück deren Template dem gegebenen entspricht.
	 * 
	 * @return PageArrayList list of Pages; Liste kann leer sein
	 */
	public PageArrayList getChildPagesForTemplate(String templateName) throws RQLException {

		return getChildPages().selectAllTemplateNamed(templateName);
	}

	/**
	 * Liefert einen Iterator über die Liste aller an diesen Multilink angehängten Seiten zurück.
	 */
	public Iterator<Page> getChildPagesIterator() throws RQLException {

		return getChildPages().iterator();
	}

	/**
	 * Liefert die NodeList aller an diesen MultiLink angehängten Seiten.
	 */
	private RQLNodeList getChildPagesNodeList() throws RQLException {
		/* 
		 V5 request (get linked pages)
		 <IODATA loginguid="28669C31B4274B7785D99D52D7E32FA1" sessionkey="421139565e46x3Bm8C61">
		 <LINK action="load" guid="A71E9253472845629DBA1C5C813A953E">
		 <PAGES action="list"/>
		 </LINK>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <LINK action="load" name="container" guid="A71E9253472845629DBA1C5C813A953E" sessionkey="421139565e46x3Bm8C61" dialoglanguageid="ENG" languagevariantid="ENG" ok="1" templateelementguid="3DED4381C1314A348138D73E97346803" pageguid="28457BD66F9B45A18B9E69F8F9DABAD3" eltflags="0" eltrequired="0" islink="2" formularorderid="0" orderid="1" status="0" eltname="container" aliasname="container" variable="container" folderguid="" istargetcontainer="0" type="28" elttype="28" templateelementflags="0" templateelementislink="2" value="container" reddotdescription="" flags="16777216" manuallysorted="-1" useconnection="1" userguid="1B9A283122B741809F02C456CB77C43B">
		 <PAGES action="list">
		 <PAGE id="1536" guid="9CE9B1A9C1AB462E8B96963548D191FF" headline="test konstruktoren" connectedbykeyword="0" datebegin="0" dateend="0"/>
		 <PAGE id="1528" guid="8CE42BC44CDE4BAB9F6BE54C377A6A93" headline="test frank RQL fw" connectedbykeyword="0" datebegin="0" dateend="0"/>
		 <PAGE id="1449" guid="202ECB7C834542C582F6A30410195664" headline="block sample list frank" connectedbykeyword="0" datebegin="0" dateend="0"/>
		 <PAGE id="1057" guid="F45146658C6347CDBB4A2311902985E3" headline="Daily News Roundups" connectedbykeyword="0" datebegin="0" dateend="0"/>
		 </PAGES>
		 </LINK>
		 </IODATA>
		 */

		// call CMS to list all linked pages
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <LINK action='load' guid='" + getLinkGuid() + "'>" + "   <PAGES action='list'/>" + " </LINK>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		return rqlResponse.getNodes("PAGE");
	}

	/**
	 * Liefert die Anzahl der an diesen Multilink angehängten Seiten zurück.
	 */
	public int getChildPagesSize() throws RQLException {

		RQLNodeList pageNodeList = getChildPagesNodeList();

		// no childs found; could be empty or referenced link
		if (pageNodeList == null) {
			return 0;
		}
		return pageNodeList.size();
	}

	/**
	 * Liefert die Templatenamen aller an diesen Multilink angehängten Seiten zurück.
	 */
	public java.util.List<String> getChildPagesTemplateNames() throws RQLException {

		java.util.List<String> result = new ArrayList<String>();
		PageArrayList childs = getChildPages();

		// no childs found; could be empty or referenced link
		if (childs.size() == 0) {
			return result;
		}

		for (int i = 0; i < childs.size(); i++) {
			Page child = (Page) childs.get(i);
			result.add(child.getTemplateName());
		}

		return result;
	}

	/**
	 * Liefert die Templatenamen aller an diesen Multilink angehängten Seiten zurück.
	 * 
	 * @return namesSeparator trennt die zurückgegebenen templatenamen
	 */
	public String getChildPagesTemplateNames(String namesSeparator) throws RQLException {

		StringBuilder names = new StringBuilder(128);
		PageArrayList childs = getChildPages();

		// no childs found; could be empty or referenced link
		if (childs.size() == 0) {
			return names.toString();
		}

		for (int i = 0; i < childs.size(); i++) {
			Page child = (Page) childs.get(i);
			names.append(child.getTemplateName()).append(namesSeparator);
		}

		return names.toString();
	}

	/**
	 * Liefert alle an diesen Multilink angehängten Seiten in umgekehrter Reihenfolge zurück.
	 * 
	 * @return PageArrayList list of Pages; Liste ist leer falls keine Seiten angehängt sind (oder auf einen anderen Link verwiesen
	 *         wird)
	 */
	public PageArrayList getChildrenReversed() throws RQLException {
		PageArrayList result = getChildPages();
		Collections.reverse(result);
		return result;
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getPage().getCmsClient();
	}

	/**
	 * Returns the first child page from this multi link or null, if less or more children are linked.
	 */
	public Page getFirstChildPage() throws RQLException {

		PageArrayList children = getChildPages();

		// check number of childs
		if (children.size() != 1) {
			return null;
		}

		return (Page) children.get(0);
	}

	/**
	 * Liefert die page GUID der ersten an diesem Multilink angehängten Seiten zurück oder null, falls dieser Link keine Seite hat.
	 */
	public String getFirstChildPageGuid() throws RQLException {

		RQLNodeList pageNodeList = getChildPagesNodeList();

		// no childs found; could be empty or referenced link
		if (pageNodeList == null || pageNodeList.size() == 0) {
			return null;
		}
		return pageNodeList.get(0).getAttribute("guid");
	}

	/**
	 * Liefert die GUID dieses Links.
	 */
	public String getLinkGuid() {
		return linkGuid;
	}

	
	/**
	 * Liefert die GUID dieses Links.
	 */
	public String getGuid() {
		return linkGuid;
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {
		return getPage().getLogonGuid();
	}

	/**
	 * Liefert die an diesem Multilink über Hauptlink angehängte Seite zurück.
	 * 
	 * @return Page null, falls keine Kind-Seiten vorhanden
	 */
	public Page getMainLinkChildPage() throws RQLException {

		RQLNodeList pageNodeList = getChildPagesNodeList();

		// no childs found; could be empty or referenced link
		if (pageNodeList == null) {
			return null;
		}

		for (int i = 0; i < pageNodeList.size(); i++) {
			RQLNode pageNode = pageNodeList.get(i);
			Page page = new Page(getProject(), pageNode.getAttribute("guid"), pageNode.getAttribute("id"), pageNode
					.getAttribute("headline"));
			if (getLinkGuid().equals(page.getMainLinkGuid())) {
				// this child page is linked via main link
				return page;
			}
		}
		return null;
	}

	/**
	 * Liefert einen Iterator für alle MultiLinks zurück, die auf diesen MultiLink verweisen. Das Ergebnis entspricht der RD Funktion
	 * show reference list für diesen MultiLink. Templateelemente, die ebenfalls diesen Multilink referenzieren werden nicht geliefert
	 * (fehlen bereits im RQL). Sie werden auch nicht durch die RD Funktion show reference list geliefert!
	 */
	public Iterator<MultiLink> getMultiLinksReferencingThisLink() throws RQLException {
		return getProject().getMultiLinksReferencingIterator(getLinkGuid());
	}

	/**
	 * Liefert den Namen dieses MultiLinks in der Seite.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getName() {
		return name;
	}

	/**
	 * Liefert eine Liste von Kindseiten, deren Template an diesem MultiLink nicht vorbelegt sind.
	 */
	public PageArrayList getNotAllowedChildPages() throws RQLException {

		PageArrayList childs = getChildPages();
		TemplatesPageFilter filter = new TemplatesPageFilter(getAllowedTemplateNames(","), ",", true);
		return childs.select(filter);
	}

	/**
	 * Liefert die Seite, die diesen MultiLink beinhaltet.
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

	/**
	 * Liefert den Sortierungsmodus dieses Multilinks zurück.
	 */
	private int getSortMode() throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="FE4D2C44BAEC43DD9CAB3D4DC262763E">
		 <PROJECT sessionkey="42113976861d16oBRlOJ">
		 <PAGE>
		 <ELEMENT guid="A71E9253472845629DBA1C5C813A953E" >
		 <SORTSETTING action="load" />
		 </ELEMENT>
		 </PAGE>
		 </PROJECT>
		 </IODATA>
		 V5 response (sortmode + maxcount)
		 <IODATA>
		 <ELEMENT guid="A71E9253472845629DBA1C5C813A953E" languagevariantid="ENG" dialoglanguageid="ENG" action="" parentguid="" parenttable="PAG">
		 <SORTSETTING action="load" sortmode="1" sortelement="1" sortelementname="1" linkguid="A71E9253472845629DBA1C5C813A953E" targetlinkguid="" parenttable="PGE" sessionkey="" languagevariantid="ENG" guid="CB4B2336C46A4DA291A40D57ADB72008" maxcount="0" />
		 </ELEMENT>
		 </IODATA>
		 V5 wrong response (no sort* attributes)
		 <IODATA>
		 <ELEMENT guid="CA866019FF014E2ABAD2CDE241B8D616" languagevariantid="ENG" dialoglanguageid="ENG" action="" parentguid="" parenttable="PAG">
		 <SORTSETTING action="load" linkguid="CA866019FF014E2ABAD2CDE241B8D616" targetlinkguid="" parenttable="PGE" sessionkey="" languagevariantid="ENG"/>
		 </ELEMENT>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "	<PROJECT sessionkey='" + getSessionKey() + "'>"
				+ "		<PAGE>" + "   		<ELEMENT guid='" + getLinkGuid() + "'>" + "				<SORTSETTING action='load' />" + " 			</ELEMENT>"
				+ " 		</PAGE>" + "	</PROJECT>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		// get sortmode
		RQLNode sortNode = rqlResponse.getNode("SORTSETTING");
		String sortModeOrNull = sortNode.getAttribute("sortmode");
		// prevent NumberFormatException
		if (sortModeOrNull != null)
			return Integer.parseInt(sortModeOrNull);
		else
			// treat as manually sorted
			return SORT_MODE_MANUAL;
	}

	/**
	 * Liefert das Template-Element, auf dem dieser Link basiert.
	 */
	public TemplateElement getTemplateElement() {

		return templateElement;
	}

	/**
	 * Liefert den Namen des Template-Elements, auf dem dieser Link basiert.
	 */
	public String getTemplateElementName() {
		return getTemplateElement().getName();
	}

	/**
	 * Returns the template element type name, e.g. List or Container
	 */
	public String getTypeName() throws RQLException {
		return getTemplateElement().getTypeName();
	}

	/**
	 * Returns the template element type name converted to lower case, e.g. list or container
	 */
	public String getTypeNameLowerCase() throws RQLException {
		return getTypeName().toLowerCase();
	}

	/**
	 * Liefert den an diesem Link angehängten Workflow, aber niemals den globalen. Liefert null, falls kein Workflow angehängt ist.
	 */
	public Workflow getWorkflow() throws RQLException {

		String response = getProject().getSmartTreeSegments("link", getLinkGuid());
		// catch from "xml"
		String tag = StringHelper.getStartTag(response, Workflow.TREESEGMENT_TYPE);
		if (tag == null) {
			// no workflow found
			return null;
		}
		return new Workflow(getProject(), StringHelper.getAttributeValue(tag, "guid"), StringHelper
				.getAttributeValue(tag, "col1value"));
	}

	/**
	 * Liefert true, falls dieser MultiLink mindestens eine Kindseite besitzt.
	 */
	public boolean hasChildPages() throws RQLException {

		return getChildPagesSize() > 0;
	}

	/**
	 * Gleiche CMS Links werden als identisch betrachtet.
	 */
	public int hashCode() {

		return getLinkGuid().hashCode();
	}

	/**
	 * Liefert true, falls es mindestens einen Link in targetPage gibt an den eine Kindseite diese Links verschoben werden kann, sonst
	 * false. Es werden die TemplateVorbelegungen dieses Links und der targetPage ausgewertet.
	 * 
	 * @param includeReferences
	 *            =true, auch TemplateElemente, die Referenzquelle sind werden geliefert (haben keine Childs!) =false, ohne
	 *            TemplateElemente, die Referenzquelle sind (nur diese haben Childs!)
	 */
	public boolean isAtLeastOneChildMoveableToTarget(Page targetPage, boolean includeReferences) throws RQLException {

		java.util.Set<Template> targetTemplates = targetPage.getTemplate().collectPreassignedTemplatesOfAllMultiLinkElements(includeReferences);
		java.util.List<Template> ownTemplates = getAllowedTemplates();

		// try to find at least one match
		for (int i = 0; i < ownTemplates.size(); i++) {
			Template ownTmplt = (Template) ownTemplates.get(i);
			// one found; end
			if (targetTemplates.contains(ownTmplt)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Liefert true, falls die gegebene Seite ein Child dieses Links ist.
	 */
	public boolean isChild(Page page) throws RQLException {

		return getChildPages().contains(page);
	}

	/**
	 * Liefert true, gdw das Template von targetPage an diesem MultiLink zugelassen ist.
	 */
	public boolean isConnectToExistingPageAllowed(Page targetPage) throws RQLException {

		return templateElement.getPreassignedTemplates().contains(targetPage.getTemplate());
	}

	/**
	 * Liefert immer false. Nur die Subklasse Container liefert true.
	 */
	public boolean isContainer() {

		return false;
	}

	/**
	 * Liefert immer false. Nur die Subklasse List liefert true.
	 */
	public boolean isList() {

		return false;
	}

	
	/**
	 * Liefert immer false. Nur das Sub-Interface Anchro liefert true.
	 */
	public boolean isAnchor() {

		return false;
	}

	/**
	 * Liefert true genau dann, wenn dieser Multilink manuell sortiert werden kann.
	 */
	public boolean isManuallySorted() throws RQLException {

		return getSortMode() == 1;
	}

	/**
	 * Liefert immer true, da alle Subklassen Links sind.
	 */
	public boolean isMultiLink() {

		return true;
	}

	/**
	 * Liefert true, falls dieser MultiLink einen anderen Link referenziert. Liefert true, falls dieser MultiLink im Baum als Referenz
	 * in grün dargestellt ist, und den Ziellink hinter >>> darstellt.
	 */
	public boolean isReferenceSource() {

		return isReferenceSource;
	}

	/**
	 * Liefert immer false. Zur generischen Behandling von Liste und Container genutzt, obwohl sinnlos für einen Container.
	 */
	public boolean isTargetContainerAssigned() {
		return false;
	}

	/**
	 * Liefert true, falls das gegebenen Template an diesem MultiLink vorbelegt ist.
	 */
	public boolean isTemplateAllowed(Template template) throws RQLException {

		return getTemplateElement().isTemplatePreassigned(template);
	}

	/**
	 * Verlinkt alle Kindseiten dieses Links an den gegebenen Ziellink und löst die Verlinkung von diesem Link.
	 * <p>
	 * Danach besitzt dieser Link keine Kindseiten mehr. Vorgabe ist: addAtBottom = true
	 * 
	 * @param targetMultiLink
	 *            Link, der alle Kindseiten bekommen soll
	 */
	public void moveAllChildsTo(MultiLink targetMultiLink) throws RQLException {
		moveAllChildsTo(targetMultiLink, true);
	}

	/**
	 * Verlinkt alle Kindseiten dieses Links an den gegebenen Ziellink und löst die Verlinkung von diesem Link.
	 * <p>
	 * Danach besitzt dieser Link keine Kindseiten mehr.
	 * 
	 * @param targetMultiLink
	 *            Link, der alle Kindseiten bekommen soll
	 * @param addAtBottom
	 *            verschiebt jede child page an das Ende der Liste (nicht für automatisch sortierte verwendbar)
	 */
	public void moveAllChildsTo(MultiLink targetMultiLink, boolean addAtBottom) throws RQLException {

		// first connect to new multi link
		PageArrayList childs = getChildPages();
		for (int i = 0; i < childs.size(); i++) {
			Page child = (Page) childs.get(i);
			targetMultiLink.connectToExistingPage(child, addAtBottom);
		}

		// disconnect all childs from here
		disconnectAllChilds(childs);
	}

	/**
	 * Verschiebt die gegebenen Kindseite von diesem Link zum targetMultiLink.
	 * <p>
	 * 
	 * @param childPage
	 *            die zu verschiebene Kindseite
	 * @param targetMultiLink
	 *            Link, der alle Kindseiten bekommen soll
	 * @throws WrongSortModeException
	 */
	public void moveChildPage(Page childPage, MultiLink targetMultiLink) throws RQLException {

		// connect childPage at targetMuiltiLink at the bottom
		moveChildPage(childPage, targetMultiLink, true);
	}

	/**
	 * Verschiebt die gegebenen Kindseite von diesem Link zum targetMultiLink.
	 * <p>
	 * 
	 * @param childPage
	 *            die zu verschiebene Kindseite
	 * @param targetMultiLink
	 *            Link, der alle Kindseiten bekommen soll
	 * @param addAtBottom
	 *            true=>seite wird nach unten verschoben, false=>seite wird nicht verschoben und RD default is oben anlegen
	 * @param setMainLink
	 *            if true, after the move the targetMultiLink will be the main link of childPage
	 * @throws WrongSortModeException,NoChildException
	 */
	public void moveChildPage(Page childPage, MultiLink targetMultiLink, boolean addAtBottom, boolean setMainLink) throws RQLException {

		// check is really child
		PageArrayList checkChilds = getChildPages();
		if (!checkChilds.contains(childPage)) {
			throw new NoChildException("The page " + childPage.getHeadlineAndId() + " is not a child of this multi link " + name
					+ " on page " + getPage().getHeadlineAndId() + ".");
		}
		// do it
		moveChildPagePrimitive(childPage, targetMultiLink, addAtBottom);
		// main link
		if (setMainLink) {
			childPage.setMainLink(targetMultiLink);
		}
	}

	/**
	 * Verschiebt die gegebenen Kindseite von diesem Link zum targetMultiLink.
	 * <p>
	 * 
	 * @param childPage
	 *            die zu verschiebene Kindseite
	 * @param targetMultiLink
	 *            Link, der alle Kindseiten bekommen soll
	 * @param addAtBottom
	 *            true=>seite wird nach unten verschoben, false=>seite wird nicht verschoben und RD default is oben anlegen
	 * @throws WrongSortModeException,NoChildException
	 */
	public void moveChildPage(Page childPage, MultiLink targetMultiLink, boolean addAtBottom) throws RQLException {

		// check is really child
		PageArrayList checkChilds = getChildPages();
		if (!checkChilds.contains(childPage)) {
			throw new NoChildException("The page " + childPage.getHeadlineAndId() + " is not a child of this multi link " + name
					+ " on page " + getPage().getHeadlineAndId() + ".");
		}
		// do it
		moveChildPagePrimitive(childPage, targetMultiLink, addAtBottom);
	}

	/**
	 * Verschiebt die gegebenen Kindseite von diesem Link zum targetMultiLink.
	 * <p>
	 * 
	 * @param childPage
	 *            die zu verschiebene Kindseite
	 * @param targetMultiLink
	 *            Link, der alle Kindseiten bekommen soll
	 * @param addAtBottom
	 *            true=>seite wird nach unten verschoben, false=>seite wird nicht verschoben und RD default is oben anlegen
	 * @throws WrongSortModeException
	 */
	private void moveChildPagePrimitive(Page childPage, MultiLink targetMultiLink, boolean addAtBottom) throws RQLException {

		// first connect to new multi link
		targetMultiLink.connectToExistingPage(childPage, addAtBottom);

		// disconnect child from here
		disconnectChild(childPage);
	}

	/**
	 * Verschiebt die gegebenen Kindseite von diesem Link zum targetMultiLink.
	 * <p>
	 * Achtung: Keine Prüfung, ob die gegebene Seite Kindseite an dieser Liste ist.
	 * 
	 * @param childPage
	 *            die zu verschiebene Kindseite
	 * @param targetMultiLink
	 *            Link, der alle Kindseiten bekommen soll
	 * @param addAtBottom
	 *            true=>seite wird nach unten verschoben, false=>seite wird nicht verschoben und RD default is oben anlegen
	 * @throws WrongSortModeException
	 */
	public void moveChildPageWithoutCheck(Page childPage, MultiLink targetMultiLink, boolean addAtBottom) throws RQLException {
		moveChildPagePrimitive(childPage, targetMultiLink, addAtBottom);
	}

	/**
	 * Verschiebt alle Seiten dieses Multilinks, deren Datumswert im Feld standardFieldDateTmpltElemName < (before) marginDate ist an
	 * den targetMultiLink. Liefert die Anzahl der verschobenen Seiten.
	 * <p>
	 * Der Seitenblock wird am targetMultiLink oben angefügt; typische Verschiebeoperation bei Archiven.
	 * <p>
	 * Die Seiten werden erst an targetMultiLink mit Mainlink angehängt und dann von diesem MultiLink entfernt.
	 * <p>
	 * Die Seiten an targetMultiLink werden nicht absteigend sortiert!
	 */
	public int moveChildrenWithStandardFieldDateValueBeforTo(ReddotDate marginDate, String standardFieldDateTmpltElemName,
			MultiLink targetMultiLink) throws RQLException {
		// get
		PageArrayList toMove = getChildPages().selectAllPagesWithStandardFieldDateValueBefore(marginDate,
				standardFieldDateTmpltElemName);
		// connect
		targetMultiLink.connectToExistingPages(toMove, false, true);
		// disconnect
		disconnectChildren(toMove);

		return toMove.size();
	}

	/**
	 * Verlinkt alle gegebenen Kindseiten dieses Links an den gegebenen Ziellink und löst die Verlinkung von diesem Link.
	 * <p>
	 * Die gegebenen Seiten müssen Kinder dieses Links sein.
	 * 
	 * @param targetMultiLink
	 *            Link, der alle Kindseiten bekommen soll
	 * @param childsToMove
	 *            Liste mit den Kindseiten, die verschoben werden sollen
	 */
	public void moveChildsTo(PageArrayList childsToMove, MultiLink targetMultiLink) throws RQLException {

		// first connect to new multi link
		for (int i = 0; i < childsToMove.size(); i++) {
			Page child = (Page) childsToMove.get(i);
			targetMultiLink.connectToExistingPage(child, false);
		}

		// disconnect all childs from here
		disconnectAllChilds(childsToMove);
	}

	/**
	 * Verschiebt die oberste Seite an diesem Multi-Link an die letzte Position. Dies korrigiert das Standardverhalten von RedDot, dass
	 * eine neue Seite immer oben (als Newsletter) erstellt wird.
	 */
	public void moveFirstPageToLastPosition() throws RQLException {
		// get top page
		RQLNodeList pageNodes = getChildPagesNodeList();

		// do noting, if no or only one page linked
		if (pageNodes == null || pageNodes.size() <= 1)
			return;

		// change
		movePageBehind(pageNodes, pageNodes.first().getAttribute("guid"), pageNodes.last().getAttribute("guid"));
	}

	/**
	 * Verschiebt die unterste Seite an diesem Multi-Link auf die 1. Position.
	 */
	public void moveLastPageToFirstPosition() throws RQLException {
		// get top page
		RQLNodeList pageNodes = getChildPagesNodeList();

		// do noting, if no or only one page linked
		if (pageNodes == null || pageNodes.size() <= 1)
			return;

		// change
		movePageBefore(pageNodes, pageNodes.last().getAttribute("guid"), pageNodes.first().getAttribute("guid"));
	}

	/**
	 * Verschiebt die gegebenen Seite childToMovePageGuid an diesem Multi-Link vor die Seite positionChildPageGuid. ACHTUNG: Beide Page
	 * GUIDs müssen Kinder an diesem MultiLink gelinkt sein!
	 */
	private void movePageBefore(RQLNodeList allChilds, String childToMovePageGuid, String positionChildPageGuid) throws RQLException {
		/* 
		 V5 request (save page order)
		 <IODATA loginguid="FE4D2C44BAEC43DD9CAB3D4DC262763E" sessionkey="42113976861d16oBRlOJ">
		 <LINK action="save" guid="A71E9253472845629DBA1C5C813A953E" orderby="5">
		 <PAGES  action="saveorder">
		 <PAGE guid="8CE42BC44CDE4BAB9F6BE54C377A6A93" />
		 <PAGE guid="202ECB7C834542C582F6A30410195664" />
		 <PAGE guid="F45146658C6347CDBB4A2311902985E3" />
		 <PAGE guid="9CE9B1A9C1AB462E8B96963548D191FF" />
		 </PAGES>
		 </LINK>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <LINK action="save" sessionkey="421139565e46x3Bm8C61" dialoglanguageid="ENG" languagevariantid="ENG" defaultlanguagevariantid="ENG">
		 <PAGES linkguid="" targetlinkguid="" parenttable="PGE" sessionkey="421139565e46x3Bm8C61" languagevariantid="ENG" orderby="" action="saveorder">
		 <PAGE guid="8CE42BC44CDE4BAB9F6BE54C377A6A93"/>
		 <PAGE guid="202ECB7C834542C582F6A30410195664"/>
		 <PAGE guid="F45146658C6347CDBB4A2311902985E3"/>
		 <PAGE guid="9CE9B1A9C1AB462E8B96963548D191FF"/>
		 </PAGES>
		 </LINK>
		 </IODATA>
		 */
		// check if MultiLink is manually sorted!
		if (!isManuallySorted()) {
			throw new WrongSortModeException("The Multi-Link with name " + getName() + " on page " + getPage().getHeadlineAndId()
					+ " is not manually sorted, therefore you cannot sort childs.");
		}

		// do noting, if no or only one page linked
		if (allChilds == null || allChilds.size() <= 1)
			return;

		// build new request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "  <LINK action='save' guid='" + getLinkGuid() + "' orderby='5'>" + "	<PAGES action='saveorder'>";

		// build new order
		RQLNode pageNode = null;
		for (int i = 0; i < allChilds.size(); i++) {
			pageNode = allChilds.get(i);
			String guid = pageNode.getAttribute("guid");
			// ignore the pages which should be moved
			if (childToMovePageGuid.equals(guid)) {
				continue;
			}
			// position page found
			if (positionChildPageGuid.equals(guid)) {
				rqlRequest = rqlRequest + "<PAGE guid='" + childToMovePageGuid + "'/>";
				rqlRequest = rqlRequest + "<PAGE guid='" + positionChildPageGuid + "'/>";
				continue;
			}
			// keep all other pages
			rqlRequest = rqlRequest + "<PAGE guid='" + pageNode.getAttribute("guid") + "'/>";
		}

		// add request tail
		rqlRequest = rqlRequest + "</PAGES>" + "</LINK>" + "</IODATA>";
		callCms(rqlRequest);
	}

	/**
	 * Verschiebt die gegebenen Seite childToMove an diesem Multi-Link hinter die Seite positionChild. ACHTUNG: Beide Seiten müssen
	 * Kinder an diesem MultiLink sein!
	 */
	public void movePageBehind(Page childToMove, Page positionChild) throws RQLException {
		// get top page
		RQLNodeList pageNodes = getChildPagesNodeList();

		// do noting, if no or only one page linked
		if (pageNodes == null || pageNodes.size() <= 1)
			return;

		// change
		movePageBehind(pageNodes, childToMove.getPageGuid(), positionChild.getPageGuid());
	}

	/**
	 * Verschiebt die gegebenen Seite childToMovePageGuid an diesem Multi-Link hinter die Seite positionChildPageGuid. ACHTUNG: Beide
	 * Page GUIDs müssen Kinder an diesem MultiLink gelinkt sein!
	 */
	private void movePageBehind(RQLNodeList allChilds, String childToMovePageGuid, String positionChildPageGuid) throws RQLException {
		/* 
		 V5 request (save page order)
		 <IODATA loginguid="FE4D2C44BAEC43DD9CAB3D4DC262763E" sessionkey="42113976861d16oBRlOJ">
		 <LINK action="save" guid="A71E9253472845629DBA1C5C813A953E" orderby="5">
		 <PAGES  action="saveorder">
		 <PAGE guid="8CE42BC44CDE4BAB9F6BE54C377A6A93" />
		 <PAGE guid="202ECB7C834542C582F6A30410195664" />
		 <PAGE guid="F45146658C6347CDBB4A2311902985E3" />
		 <PAGE guid="9CE9B1A9C1AB462E8B96963548D191FF" />
		 </PAGES>
		 </LINK>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <LINK action="save" sessionkey="421139565e46x3Bm8C61" dialoglanguageid="ENG" languagevariantid="ENG" defaultlanguagevariantid="ENG">
		 <PAGES linkguid="" targetlinkguid="" parenttable="PGE" sessionkey="421139565e46x3Bm8C61" languagevariantid="ENG" orderby="" action="saveorder">
		 <PAGE guid="8CE42BC44CDE4BAB9F6BE54C377A6A93"/>
		 <PAGE guid="202ECB7C834542C582F6A30410195664"/>
		 <PAGE guid="F45146658C6347CDBB4A2311902985E3"/>
		 <PAGE guid="9CE9B1A9C1AB462E8B96963548D191FF"/>
		 </PAGES>
		 </LINK>
		 </IODATA>
		 */
		// check if MultiLink is manually sorted!
		if (!isManuallySorted()) {
			throw new WrongSortModeException("The Multi-Link with name " + getName() + " on page " + getPage().getHeadlineAndId()
					+ " is not manually sorted, therefore you cannot sort childs.");
		}

		// do noting, if no or only one page linked
		if (allChilds == null || allChilds.size() <= 1)
			return;

		// build new request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "  <LINK action='save' guid='" + getLinkGuid() + "' orderby='5'>" + "	<PAGES action='saveorder'>";

		// build new order
		RQLNode pageNode = null;
		for (int i = 1; i < allChilds.size(); i++) {
			pageNode = allChilds.get(i);
			String guid = pageNode.getAttribute("guid");
			// ignore the pages which should be moved
			if (childToMovePageGuid.equals(guid)) {
				continue;
			}
			// position page found
			if (positionChildPageGuid.equals(guid)) {
				rqlRequest = rqlRequest + "<PAGE guid='" + positionChildPageGuid + "'/>";
				rqlRequest = rqlRequest + "<PAGE guid='" + childToMovePageGuid + "'/>";
				continue;
			}
			// keep all other pages
			rqlRequest = rqlRequest + "<PAGE guid='" + pageNode.getAttribute("guid") + "'/>";
		}

		// add request tail
		rqlRequest = rqlRequest + "</PAGES>" + "</LINK>" + "</IODATA>";
		callCms(rqlRequest);
	}

	/**
	 * Verschiebt alle Seiten deren Überschrift mit dem gegebenen Prefix beginnen ans Ende dieser Liste.
	 */
	public void movePagesHeadlineStartsWithToLastPosition(String prefix) throws RQLException {
		PageArrayList allChildren = getChildPages();
		PageArrayList prefixPages = allChildren.selectAllPagesHeadlineStartsWith(prefix);

		// build new order of collection
		allChildren.removeAll(prefixPages);
		allChildren.addAll(prefixPages);
		changeOrder(allChildren);
	}

	/**
	 * Erstellt eine RD Referenz von diesem MultiLink (als Source) zu dem gegebenen target Anchor.
	 */
	public void referenceTo(Anchor targetAnchor) throws RQLException {
		getProject().referenceLinkToLink(getLinkGuid(), targetAnchor.getAnchorGuid());
	}

	/**
	 * Erstellt eine RD Referenz von diesem MultiLink (als Source) zu dem gegebenen target MultiLink.
	 * <p>
	 * ACHTUNG: Referenzen auf Links können zu Problemen bei der Generierung führen. Z.B. hlag.com Container referenziert Link, Seiten
	 * unter Worldwide Offices werden bei Generierung gar nicht betrachtet!
	 */
	public void referenceTo(MultiLink targetMultiLink) throws RQLException {
		getProject().referenceLinkToLink(getLinkGuid(), targetMultiLink.getLinkGuid());
	}

	/**
	 * Erstellt eine RD Referenz von diesem MultiLink (als Source) zu der gegebenen target Seite. Dieser MultiLink sollte
	 * sinnvollerweise ein Container sein!
	 */
	public void referenceTo(Page targetPage) throws RQLException {
		/* 
		 V6.5 request
		 <IODATA loginguid="98D333D0CABF448E97FD54806F13184C" sessionkey="1021834323136p3X742U8">
		 <LINK action="reference" guid="794B433AC82044AFA185088E9A33504B">
		 <PAGE guid="80B3AFBA6C4B46DC805A05EEF92D4E10" />
		 </LINK>
		 </IODATA> 
		 
		 V6.5 response
		 <IODATA>
		 <LINK action="reference" sessionkey="1021834323136p3X742U8" dialoglanguageid="ENG" languagevariantid="ENG" ok="1" guid="794B433AC82044AFA185088E9A33504B" templateelementguid="BCCEB7F466D743678B315F56A6B97377" pageguid="EE7FE1F062624E0AB95ADE337A6DC49C" eltflags="0" flags="0" eltrequired="0" eltdragdrop="0" islink="2" formularorderid="0" orderid="1" status="0" name="pdf_styles_container" eltname="pdf_styles_container" aliasname="pdf_styles_container" variable="pdf_styles_container" istargetcontainer="0" type="28" elttype="28" templateelementflags="0" templateelementislink="2" target="" reddotdescription="" value="pdf_styles_container">
		 <PAGE guid="80B3AFBA6C4B46DC805A05EEF92D4E10" linkednew="1" linkguid="794B433AC82044AFA185088E9A33504B" targetlinkguid="" parenttable="PGE" sessionkey="1021834323136p3X742U8" languagevariantid="ENG"/>
		 </LINK>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <LINK action='reference' guid='" + getLinkGuid() + "'>" + "  <PAGE guid='" + targetPage.getPageGuid() + "' />"
				+ "</LINK>" + "</IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Ändert die Sortierungseinstellungen dieses MultiLinks. Es werden alle Seiten angezeigt und die Liste wird aufsteigend nach
	 * Headline der angelinkten Seiten geordnet.
	 */
	public void setSortModeAllAscendingByHeadline() throws RQLException {

		changeSortMode(0, SORT_MODE_ASCENDING, SORT_CRITERIA_PAGE_INFORMATION, SORT_CRITERIA_PAGE_INFORMATION_HEADLINE);
	}

	/**
	 * Ändert die Sortierungseinstellungen dieses MultiLinks auf manuelle Sortierung.
	 */
	public void setSortModeManually() throws RQLException {

		changeSortMode(0, SORT_MODE_MANUAL, 0, null);
	}

	/**
	 * Sortiert alle änderbaren Kindseiten dieses MultiLinks nach dem gegebenen Datumselement absteigend.
	 * <p>
	 * Achtung! Alle Kindseiten müssen dieses Element besitzen.
	 * 
	 * @param standardFieldDateTmpltElemName
	 *            Name des Datumselements (muss in jeder Kindseite vorhanden sein)
	 * @return returns die sortierte Liste der Kindseiten
	 */
	public PageArrayList sortChangeableChildrenByDateDesc(String standardFieldDateTmpltElemName) throws RQLException {

		// create comparator and sort
		StandardFieldDatePageComparator comparator = new StandardFieldDatePageComparator(standardFieldDateTmpltElemName);
		comparator.forceDescendingOrdering();
		PageArrayList sortedChilds = getChangeableChildPages().sort(comparator);

		// update cms
		changeOrder(sortedChilds);

		return sortedChilds;
	}

	/**
	 * Sortiert alle Kindseiten dieses MultiLinks nach dem gegebenen comparator.
	 * 
	 * @return returns die sortierte Liste der Kindseiten
	 */
	public PageArrayList sortChilds(PageComparator comparator) throws RQLException {
		PageArrayList sortedChilds = getChildPages().sort(comparator);
		changeOrder(sortedChilds);
		return sortedChilds;
	}

	/**
	 * Sortiert alle Kindseiten dieses MultiLinks nach dem gegebenen Datumselement aufsteigend. Achtung! Alle Kindseiten müssen dieses
	 * Element besitzen.
	 * 
	 * @param standardFieldDateTmpltElemName
	 *            Name des Datumselements (muss in jeder Kindseite vorhanden sein)
	 * @return returns die sortierte Liste der Kindseiten
	 */
	public PageArrayList sortChildsByDateAsc(String standardFieldDateTmpltElemName) throws RQLException {

		// create comparator and sort
		StandardFieldDatePageComparator comparator = new StandardFieldDatePageComparator(standardFieldDateTmpltElemName);
		PageArrayList sortedChilds = getChildPages().sort(comparator);

		// update cms
		changeOrder(sortedChilds);

		return sortedChilds;
	}

	/**
	 * Sortiert alle Kindseiten dieses MultiLinks nach dem gegebenen Datumselement absteigend. Achtung! Alle Kindseiten müssen dieses
	 * Element besitzen.
	 * 
	 * @param standardFieldDateTmpltElemName
	 *            Name des Datumselements (muss in jeder Kindseite vorhanden sein)
	 * @return returns die sortierte Liste der Kindseiten
	 */
	public PageArrayList sortChildsByDateDesc(String standardFieldDateTmpltElemName) throws RQLException {

		// create comparator and sort
		StandardFieldDatePageComparator comparator = new StandardFieldDatePageComparator(standardFieldDateTmpltElemName);
		comparator.forceDescendingOrdering();
		PageArrayList sortedChilds = getChildPages().sort(comparator);

		// update cms
		changeOrder(sortedChilds);

		return sortedChilds;
	}

	/**
	 * Sortiert alle Kindseiten dieses MultiLinks nach dem gegebenen Datumselement absteigend und der Seitenüberschrift aufsteigend.
	 * Achtung! Alle Kindseiten müssen dieses Element besitzen.
	 * 
	 * @param standardFieldDateTmpltElemName
	 *            Name des Datumselements (muss in jeder Kindseite vorhanden sein)
	 * @return returns die sortierte Liste der Kindseiten
	 */
	public PageArrayList sortChildsByDateDescAndHeadlineAsc(String standardFieldDateTmpltElemName) throws RQLException {

		// create comparator and sort in default order
		StandardFieldDateDescAndHeadlineAscPageComparator comparator = new StandardFieldDateDescAndHeadlineAscPageComparator(
				standardFieldDateTmpltElemName);
		PageArrayList sortedChilds = getChildPages().sort(comparator);

		// update cms
		changeOrder(sortedChilds);

		return sortedChilds;
	}

	/**
	 * Sorts all children of this multi link accordingly to the given and ordered headlines.
	 * 
	 * @return the sorted list of children
	 */
	public PageArrayList sortChildsByGivenHeadlines(SortedSet<String> orderedHeadlines) throws RQLException {

		// create comparator and sort
		SortedHeadlinesPageComparator comparator = new SortedHeadlinesPageComparator(orderedHeadlines);
		PageArrayList sortedChilds = getChildPages().sort(comparator);

		// update cms
		changeOrder(sortedChilds);

		return sortedChilds;
	}

	/**
	 * Sortiert alle Kindseiten dieses MultiLinks aufsteigend nach Überschrift.
	 * 
	 * @return returns die sortierte Liste der Kindseiten
	 */
	public PageArrayList sortChildsByHeadlineAsc() throws RQLException {

		// sort
		PageArrayList sortedChilds = getChildPages().sortByHeadlineAsc();

		// update cms
		changeOrder(sortedChilds);

		return sortedChilds;
	}

	/**
	 * Sortiert alle Kindseiten dieses MultiLinks absteigend nach Überschrift.
	 * 
	 * @return returns die sortierte Liste der Kindseiten
	 */
	public PageArrayList sortChildsByHeadlineDesc() throws RQLException {

		// sort
		PageArrayList sortedChilds = getChildPages().sortByHeadlineDesc();

		// update cms
		changeOrder(sortedChilds);

		return sortedChilds;
	}

	/**
	 * Sortiert alle Kindseiten dieses MultiLinks nach der Page ID aufsteigend.
	 * 
	 * @return returns die sortierte Liste der Kindseiten
	 */
	public PageArrayList sortChildsByIdAsc() throws RQLException {

		// create comparator and sort
		PageIdPageComparator comparator = new PageIdPageComparator();
		PageArrayList sortedChilds = getChildPages().sort(comparator);

		// update cms
		changeOrder(sortedChilds);

		return sortedChilds;
	}

	/**
	 * Sortiert alle Kindseiten dieses MultiLinks nach der Page ID absteigend.
	 * 
	 * @return returns die sortierte Liste der Kindseiten
	 */
	public PageArrayList sortChildsByIdDesc() throws RQLException {

		// create comparator and sort
		PageIdPageComparator comparator = new PageIdPageComparator();
		comparator.forceDescendingOrdering();
		PageArrayList sortedChilds = getChildPages().sort(comparator);

		// update cms
		changeOrder(sortedChilds);

		return sortedChilds;
	}

	/**
	 * Sortiert alle Kindseiten dieses MultiLinks nach dem gegebenen StandardFieldText Element aufsteigend. Achtung! Alle Kindseiten
	 * müssen dieses Element besitzen.
	 * 
	 * @param standardFieldTextTmpltElemName
	 *            Name des SFT Elements (muss in jeder Kindseite vorhanden sein)
	 * @return returns die sortierte Liste der Kindseiten
	 */
	public PageArrayList sortChildsByStandardFieldTextAsc(String standardFieldTextTmpltElemName) throws RQLException {

		// create comparator and sort
		StandardFieldTextPageComparator comparator = new StandardFieldTextPageComparator(standardFieldTextTmpltElemName);
		PageArrayList sortedChilds = getChildPages().sort(comparator);

		// update cms
		changeOrder(sortedChilds);

		return sortedChilds;
	}

	/**
	 * Sortiert alle Kindseiten dieses MultiLinks nach dem gegebenen Standardfieldtext Element absteigend. Achtung! Alle Kindseiten
	 * müssen dieses Element besitzen.
	 * 
	 * @param standardFieldTextTmpltElemName
	 *            Name des SFT elements (muss in jeder Kindseite vorhanden sein)
	 * @return returns die sortierte Liste der Kindseiten
	 */
	public PageArrayList sortChildsByStandardFieldTextDesc(String standardFieldTextTmpltElemName) throws RQLException {

		// create comparator and sort
		StandardFieldTextPageComparator comparator = new StandardFieldTextPageComparator(standardFieldTextTmpltElemName);
		comparator.forceDescendingOrdering();
		PageArrayList sortedChilds = getChildPages().sort(comparator);

		// update cms
		changeOrder(sortedChilds);

		return sortedChilds;
	}

	/**
	 * Entfernt das gegebene Detailberechtigungspaket von diesem MultiLink.
	 */
	public void unlinkAuthorizationPackage(AuthorizationPackage authorizationPackage) throws RQLException {

		// check type
		if (!authorizationPackage.isDetailedLinkAuthorizationPackage()) {
			throw new WrongTypeException("Authorization package with name " + authorizationPackage.getName()
					+ " has wrong type and cannot be unlinked to MultiLink " + getName() + " in page " + getPage().getHeadlineAndId()
					+ ".");
		}

		// convenience call
		getProject().unlinkAuthorizationPackage(getLinkGuid(), authorizationPackage);
	}

	/**
	 * Entfernt den angelinkten Workflow von diesem MultiLink. Liefert true zurück, wenn ein Workflow abgehängt wurde, false, falls
	 * dort keiner angehängt war.
	 */
	public boolean unlinkWorkflow() throws RQLException {
		/* 
		 V5 request (language variant is needed!)
		 <IODATA loginguid="37B36D2128A1436D8C7EFD1743E24528" sessionkey="692084693571Ad873QsS">
		 <WORKFLOW>
		 <LINK action="unlink" guid="97FF3FAA0F554076BCE5EE028ECABDF0">
		 <WORKFLOW guid="82208942EB9F48EEA58854222EAFE2AA">
		 <LANGUAGEVARIANTS>     <LANGUAGEVARIANT language="ENG"/>    </LANGUAGEVARIANTS>
		 </WORKFLOW>
		 </LINK>
		 </WORKFLOW>
		 </IODATA>
		 V5 response
		 <IODATA>
		 </IODATA>
		 */

		Workflow workflowOrNull = getWorkflow();
		if (workflowOrNull == null) {
			return false;
		}

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + " <WORKFLOW>"
				+ "  <LINK action='unlink' guid='" + getLinkGuid() + "'>" + "   <WORKFLOW guid='" + workflowOrNull.getWorkflowGuid()
				+ "'>" + "    <LANGUAGEVARIANTS><LANGUAGEVARIANT language='" + workflowOrNull.getLanguageCode()
				+ "'/></LANGUAGEVARIANTS>" + "   </WORKFLOW>" + "  </LINK>" + " </WORKFLOW>" + "</IODATA>";
		callCms(rqlRequest);

		return true;
	}

    /**
     * Liefert den appearance schedule, falls einer existiert.
     *
     * @return appearanceSchedule oder null
     */
    public AppearanceSchedule getAppearanceSchedule() {
        return appearanceSchedule;
    }

    /**
     * Setzt einen {@link com.hlcl.rql.as.AppearanceSchedule} auf diesem Link. <b>Es wird dadurch nicht persistiert!</b>
     * Um einen schedule auch zu speichern bitte {@link com.hlcl.rql.as.Page#assignMainLinkAppearanceSchedule(AppearanceSchedule)} benutzen.
     *
     * @param appearanceSchedule
     * @throws RQLException
     */
    public void setAppearanceSchedule(AppearanceSchedule appearanceSchedule) throws RQLException {
        this.appearanceSchedule = appearanceSchedule;
    }
}
