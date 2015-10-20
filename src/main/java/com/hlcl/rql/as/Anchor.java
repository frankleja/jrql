package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Diese Klasse beschreibt einen RedDot Anchor (Textlink oder Bildlink, jeweils auch dynamisch).
 * 
 * @author LEJAFR
 */
public abstract class Anchor implements PageContainer, StructureElement {

	private String anchorGuid;
	private RQLNode anchorNode;
	private String name;

	private Page page;
	private TemplateElement templateElement;
	private boolean isReferenceSource;

	/**
	 * @deprecated
	 */
	public Anchor(Page page,TemplateElement templateElement, String name, String anchorGuid, boolean isReferenceSource) {
		super();

		this.page = page;
		this.templateElement = templateElement;
		this.name = name;
		this.anchorGuid = anchorGuid;
		this.isReferenceSource = isReferenceSource;
		this.anchorNode = null;
	}

	
	/**
	 * Use this one!
	 */
	public Anchor(Page page, TemplateElement textAnchorTemplateElement,
			RQLNode anchorNode) {
		this(page, textAnchorTemplateElement,
			 anchorNode.getAttribute("name"),
			 anchorNode.getAttribute("guid"),
			 anchorNode.getAttribute("islink").equals("10"));
		this.anchorNode = anchorNode;
	}

	/**
	 * Ordnet diesem Anchor das gegebene Berechtigungspaket zu.
	 */
	public void assignAuthorizationPackage(AuthorizationPackage authorizationPackage) throws RQLException {
		// check type
		if (!authorizationPackage.isDetailedLinkAuthorizationPackage()) {
			throw new WrongTypeException("Authorization package with name " + authorizationPackage.getName()
					+ " has wrong type and cannot be linked to Anchor " + getName() + " in page " + getPage().getHeadlineAndId() + ".");
		}

		// convenience call
		getProject().assignAuthorizationPackage("LINK", getAnchorGuid(), authorizationPackage);
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
	 * Erzeugt eine neue Seite basierend auf template an diesem Container. Die Templatevorbelegung wird dabei nicht geprueft!
	 * 
	 * @param template
	 *            Typ der neu erstellten Seite.
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 */
	public Page createAndConnectPage(Template template, String headline) throws RQLException {
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
		StringBuilder sb = new StringBuilder(128);
		sb.append("<IODATA loginguid='").append(getLogonGuid()).append("' sessionkey='").append(getSessionKey()).append("'>")
		.append(" <LINK action='assign' guid='").append(getAnchorGuid()).append("'>")
		.append("   <PAGE action='addnew' templateguid='").append(template.getTemplateGuid()).append("'");
		if (headline != null) {
			sb.append(" headline='").append(StringHelper.escapeHTML(headline)).append("'");
		}
		sb.append("/>")
		.append(" </LINK>")
		.append("</IODATA>");
		String rqlRequest = sb.toString();
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
	 * Löst die Verlinkung der Kindseite von diesem Link. Tut nichts, falls dieser Link keine Kindseite besitzt
	 * <p>
	 */
	public void disconnectChild() throws RQLException {
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

		// disconnect the child
		Page child = getChildPage();
		if (child != null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ " <LINK action='save' guid='" + getAnchorGuid() + "'>" + "  <PAGES>";
			rqlRequest += "<PAGE deleted ='1' guid='" + child.getPageGuid() + "' />";
			rqlRequest += "  </PAGES>" + " </LINK>" + "</IODATA>";
			callCms(rqlRequest);
		}
	}

	/**
	 * Löst die Verlinkung der gegebenen Kindseite von diesem Link. Tut nichts, falls dieser Link keine Kindseite besitzt
	 * <p>
	 * TODO what if given page is not a child?
	 */
	public void disconnectChild(Page child) throws RQLException {
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

		// disconnect the child
		if (child != null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ " <LINK action='save' guid='" + getAnchorGuid() + "'>" + "  <PAGES>";
			rqlRequest += "<PAGE deleted ='1' guid='" + child.getPageGuid() + "' />";
			rqlRequest += "  </PAGES>" + " </LINK>" + "</IODATA>";
			callCms(rqlRequest);
		}
	}

	/**
	 * Liefert die GUID dieses Links.
	 * 
	 * @return java.lang.String
	 */
	public String getAnchorGuid() {
		return anchorGuid;
	}

	/**
	 * Liefert die verlinkte Seite eines Ankers oder null, falls keine Seite angehängt ist.
	 * 
	 * @see <code>MultiLink</code>
	 */
	public Page getChildPage() throws RQLException {
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
		 </PAGES>
		 </LINK>
		 </IODATA>
		 */

		// call CMS to list all linked pages
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <LINK action='load' guid='" + getAnchorGuid() + "'>" + "   <PAGES action='list'/>" + " </LINK>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		RQLNode childNode = rqlResponse.getNode("PAGE");
		if (childNode == null) {
			return null;
		}
		// wrap into page
		return new Page(getProject(), childNode.getAttribute("guid"), childNode.getAttribute("id"), childNode.getAttribute("headline"));
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
	 * Liefert einen Iterator für alle MultiLinks zurück, die auf diesen Anchor verweisen. Das Ergebnis entspricht der RD Funktion show
	 * reference list (auch für Frameelement!). Templateelemente, die ebenfalls diesen Anchor referenzieren werden nicht geliefert
	 * (fehlen bereits im RQL). Sie werden auch nicht durch die RD Funktion show reference list geliefert!
	 */
	public Iterator<MultiLink> getMultiLinksReferencingThisLink() throws RQLException {
		return getProject().getMultiLinksReferencingIterator(getAnchorGuid());
	}

	/**
	 * Liefert den Namen dieses Containers in der Seite, die den Container beinhaltet.
	 * 
	 * @return java.lang.String
	 */
	public String getName() {
		return name;
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
	 * Liefert true, falls die gegebene URL der zugewiesen URL an diesem Anchor entspricht. Checks with equals().
	 */
	public boolean isUrlEquals(String url) throws RQLException {
		return url.equals(getUrl());
	}

	/**
	 * Liefert die zugewiesene URL oder null, falls dieser Link keine hat.
	 */
	public String getUrl() throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="5A56EC69E8AB4783A9353D14D852EFC4" sessionkey="928908857k317QL7280">
		 <LINK action="load" guid="3D9730F0151540B693B2DE1F5FD1FED3">
		 <URL action="load"/></LINK>
		 </IODATA>
		 V5 response
		 mit url
		 <IODATA><LINK action="load" sessionkey="928908857k317QL7280" dialoglanguageid="ENG" languagevariantid="ENG" ok="1" eltlanguageindependent="0" guid="9A92F10D33DE48A9A15138839CD10C39" templateelementguid="D6636A8CAC68409EA4BE1A4A23EA901C" pageguid="8274219C38C64E57998B4D17458888BF" eltflags="528" eltrequired="1" islink="10" formularorderid="0" orderid="1" status="0" name="link" eltname="link" aliasname="link" variable="link" folderguid="" type="26" elttype="26" templateelementflags="528" templateelementislink="1" value="link" reddotdescription="link" flags="16777744" manuallysorted="-1" useconnection="1" userguid="4324D172EF4342669EAF0AD074433393">
		 <URL action="load" linkguid="9A92F10D33DE48A9A15138839CD10C39" targetlinkguid="" parenttable="PGE" sessionkey="928908857k317QL7280" languagevariantid="ENG" guid="D754ED34AF014CD9A3D1C027B7D45B0C" src="http://www.hlcl.com/en/fleet/liner_services_EA1.html"/></LINK>
		 </IODATA>
		 
		 url entfernt
		 <IODATA><LINK action="load" sessionkey="928908857k317QL7280" dialoglanguageid="ENG" languagevariantid="ENG" ok="1" eltlanguageindependent="0" guid="668BDECE75204247AD8B54D5A25B54C6" templateelementguid="D6636A8CAC68409EA4BE1A4A23EA901C" pageguid="8980DB67747F4740A8C8396846E95E84" eltflags="528" eltrequired="1" islink="1" formularorderid="0" orderid="1" status="0" name="link" eltname="link" aliasname="link" variable="link" folderguid="" type="26" elttype="26" templateelementflags="528" templateelementislink="1" value="link" reddotdescription="link" flags="16777744" manuallysorted="-1" useconnection="1" userguid="4324D172EF4342669EAF0AD074433393">
		 <URL action="load" linkguid="668BDECE75204247AD8B54D5A25B54C6" targetlinkguid="" parenttable="PGE" sessionkey="928908857k317QL7280" languagevariantid="ENG"/></LINK>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <LINK action='load' guid='" + getAnchorGuid() + "'>" + "   <URL action='load'/>" + " </LINK>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		return rqlResponse.getNode("URL").getAttribute("src");
	}

	/**
	 * Liefert true, falls an diesem Anchor eine Seiten angehängt ist.
	 */
	public boolean hasChildPage() throws RQLException {
		return getChildPage() != null;
	}

	
	/**
	 * Liefert immer true. Alle subklassen sind anchor.
	 */
	@Override
	public final boolean isAnchor() {
		return true;
	}


	@Override
	public final boolean isMultiLink() {
		return false;
	}

	
	/**
	 * Erstellt eine RD Referenz von diesem Link (als Source) zu der gegebenen target Seite.
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
				+ " <LINK action='reference' guid='" + getAnchorGuid() + "'>" + "  <PAGE guid='" + targetPage.getPageGuid() + "' />"
				+ "</LINK>" + "</IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Setzt die gegebene URL an diesem Link.
	 */
	public void setUrl(String url) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="5A56EC69E8AB4783A9353D14D852EFC4" sessionkey="928908857k317QL7280">
		 <LINK action="assign" guid="9A92F10D33DE48A9A15138839CD10C39">
		 <URL src="http://www.hlcl.com/en/fleet/liner_services_EA1.html" />
		 </LINK>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <LINK action="assign" reddotcacheguid="" sessionkey="928908857k317QL7280" dialoglanguageid="ENG" languagevariantid="ENG" ok="1" eltlanguageindependent="0" guid="9A92F10D33DE48A9A15138839CD10C39" templateelementguid="D6636A8CAC68409EA4BE1A4A23EA901C" pageguid="8274219C38C64E57998B4D17458888BF" eltflags="528" flags="528" eltrequired="1" islink="10" formularorderid="0" orderid="1" status="0" name="link" eltname="link" aliasname="link" variable="link" folderguid="" type="26" elttype="26" templateelementflags="528" templateelementislink="1" value="link" reddotdescription="link">
		 <URL src="http://www.hlcl.com/en/fleet/liner_services_EA1.html" guid="D754ED34AF014CD9A3D1C027B7D45B0C" linkguid="9A92F10D33DE48A9A15138839CD10C39" targetlinkguid="" parenttable="PGE" sessionkey="928908857k317QL7280" languagevariantid="ENG" action="assign"/>
		 </LINK>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <LINK action='assign' guid='" + getAnchorGuid() + "'>" + "   <URL src='" + url + "'/>" + " </LINK>" + "</IODATA>";
		callCms(rqlRequest);
	}

	/**
	 * Erstellt eine RD Referenz von diesem Link (als Source) zu dem gegebenen target MultiLink.
	 * <p>
	 * ACHTUNG: Referenzen auf Links können zu Problemen bei der Generierung führen. Z.B. hlag.com Container referenziert Link, Seiten
	 * unter Worldwide Offices werden bei Generierung gar nicht betrachtet!
	 */
	public void referenceTo(MultiLink targetMultiLink) throws RQLException {
		getProject().referenceLinkToLink(getAnchorGuid(), targetMultiLink.getLinkGuid());
	}

	
	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public boolean isList() {
		return false;
	}

	
	/**
	 * Eine Liste mit 0 oder 1 seiten daran.
	 */
	@Override
	public List<Page> getChildPages() throws RQLException {
		List<Page> out = new ArrayList<Page>(1);
		Page p = getChildPage(); 
		if (p != null)
			out.add(p);
		return out;
	}
	
	
	@Override
	public boolean isReferenceSource() {
		return isReferenceSource;
	}
	
	@Override
	public String getGuid() {
		return anchorGuid;
	}

	@Override
	public void disconnectAllChilds() throws RQLException {
		disconnectChild();
	}

	
	/**
	 * Ignores atBottom and setMainLink as this is not implemented here.
	 */
	@Override
	public void connectToExistingPage(String targetGuid, boolean atBottom, boolean setMainLink) throws RQLException {
		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <LINKSFROM action='save' pageguid='" + targetGuid + "'>" + "   <LINK guid='" + getAnchorGuid() + "'/>"
				+ " </LINKSFROM>" + "</IODATA>";

		// ignore response
		callCms(rqlRequest);
	}

	
	public void connectToExistingPage(Page target, boolean atBottom, boolean setMainLink) throws RQLException {
		connectToExistingPage(page.getPageGuid(), atBottom, setMainLink);
	}


	@Override
	public void connectToRedirectUrl(String url, String target, String headline) throws RQLException {
		// FIXME: Ignores target/headline - should probably work like MultiLink::connectToRedirectUrl
		setUrl(url);
	}

}
