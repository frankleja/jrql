package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.Iterator;

import com.hlcl.rql.util.as.PageArrayList;

/**
 * Diese Klasse beschreibt einen RedDot Dynamic Anchor (Text oder Bild).
 * 
 * @author LEJAFR
 */
public abstract class DynamicAnchor implements PageContainer {
	
	private java.util.List<Anchor> anchors;

	private Page page;

	private TemplateElement templateElement;

	/**
	 * Erzeugt einen dynamischen Anker.
	 * 
	 * @param page
	 *            Seite, die diesen Dynamischen Anker beinhaltet.
	 * @param size
	 *            Anzahl der Links in diesem Set.
	 */
	public DynamicAnchor(Page page, TemplateElement templateElement, int size) {
		super();

		this.page = page;
		this.templateElement = templateElement;
		this.anchors = new ArrayList<Anchor>(size);

		// init the number of elements
		for (int i = 0; i < size; i++) {
			anchors.add(null);
		}

	}

	/**
	 * Fügt einen Textanker der Liste hinzu.
	 */
	private TextAnchor add(String name, String anchorGuid) {

		// create new anchor
		TextAnchor textAnchor = new TextAnchor(getPage(), templateElement, name, anchorGuid, false); // FIXME: most probably wrong TemplateElement
		// add to anchors list
		anchors.add(textAnchor);

		return textAnchor;

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
	 * Führt die Funktion "Expand dynamic Link" immer vom letzten Anker der dynamic List aus.
	 */
	private void expandAtBottom(String name) throws RQLException {
		/* Expand always at bottom 1 link
		V5 request
		<IODATA loginguid="C2FFCC097AA64EE594B583FD0BF4A8F5" sessionkey="4211388671o4w56GcjW4">
		  <LINK action="expand" guid="CBC247CED4994D31BF6F7CA9780B3D6D" count="1" insertpos="1" autoname="1"/>
		</IODATA> 
		V5 response
		<IODATA>
			<LINK action="expand" count="1" insertpos="1" autoname="1" sessionkey="351980766Y0gY71V4G5q" dialoglanguageid="ENG" languagevariantid="DEU" ok="1" templateelementguid="1BB634828C1246D5A4AB7CBF637EA955" eltflags="1026" flags="1026" eltrequired="0" islink="1" formularorderid="0" orderid="26" status="0" name="dynLink" eltname="dynLink" aliasname="dynLink" variable="dynLink" folderguid="" elttype="26" templateelementflags="1026" templateelementislink="1" target="" value="e8020.icw1.bare.chassis.workorder.da.exec.complete.flag" reddotdescription="" guid="6C92B793EB4846D8831CCA7950A4DCD1" type="26" pageguid="13856E8A92A84FB5B2EDEB47A62A2620" changed="-1" available="1" oldguid="CBC247CED4994D31BF6F7CA9780B3D6D"/>
		</IODATA>
		*/

		// call CMS to expand
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + " <LINK action='expand' guid='"
				+ getLastAnchorGuid() + "' count='1' insertpos='1' autoname='0'/>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		String newAnchorGuid = rqlResponse.getNode("LINK").getAttribute("guid");

		/* change name of link
		V5 request
		<IODATA loginguid="21A9DDB2BCB9488FAD361911E7312E12" sessionkey="351985347bC155DHy5t2">
			<LINK action="save" guid="3660014AEFC349258B500749AAE3223F" value="frank test name"/>
		</IODATA>
		V5 response
		<IODATA>
			<LINK action="save" value="frank test name" reddotcacheguid="" description="" prefixguid="" target="#351985347bC155DHy5t2" frameset="" page="" dialoglanguageid="ENG" sessionkey="351985347bC155DHy5t2" languagevariantid="DEU" defaultlanguagevariantid="DEU" guid="3660014AEFC349258B500749AAE3223F" type="26" pageguid="016E13862737477EA25E076BE21C09E7" available="1" changed="0" saveauthorization="-1"/>
		</IODATA>
		*/

		// call CMS to name link
		rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + " <LINK action='save' guid='"
				+ newAnchorGuid + "' value='" + name + "'/>" + "</IODATA>";
		rqlResponse = callCms(rqlRequest);

		// add a TextAnchor to the list
		add(name, newAnchorGuid);
	}

	/**
	 * Erweitert die Liste der dynamischen Link am Ende um einen Eintrag und erstellt daran eine neue Seite.
	 * <p>
	 * Der erste Anchor bleibt unbenutzt!
	 * 
	 * @param anchorName
	 *            Name des neuen Links
	 * @param template
	 *            Template mit dem die neue Seite erstellt werden soll.
	 * @param headline
	 *            Überschrift der neu erstellten Seite
	 */
	public Page expandCreateAndConnect(String anchorName, Template template, String headline) throws RQLException {

		// expand and name link first and than create the new page
		expandAtBottom(anchorName);
		return getLastAnchor().createAndConnectPage(template, headline);
	}

	/**
	 * Liefert den ersten Anchor mit dem gegebenen Namen zurück.
	 * 
	 * @param anchorName
	 *            Name des Anchor
	 * @return null, falls kein Anker mit dem gegebenen namen gefunden werden kann
	 */
	protected Anchor findAnchorByName(String anchorName) {

		Anchor anchor = null;

		// loop through and return first found
		Iterator<Anchor> iterator = getAnchors().iterator();
		while (iterator.hasNext()) {
			anchor = iterator.next();
			if (anchor.getName().equals(anchorName)) {
				return anchor;
			}
		}

		return null;
	}

	/**
	 * Liefert die Liste der Anchors
	 */
	private java.util.List<Anchor> getAnchors() {

		return anchors;
	}

	/**
	 * Liefert den ersten Anchor zurück.
	 */
	public Anchor first() {
		return anchors.get(0);
	}

	/**
	 * Liefert alle Kindseiten, die an allen Links dieses DynLink-Sets angehängt sind.
	 */
	public PageArrayList getChildPages() throws RQLException {

		java.util.List<Anchor> anchors = getAnchors();
		PageArrayList result = new PageArrayList(anchors.size());

		// collect all anchors child pages
		for (Iterator<Anchor> iter = anchors.iterator(); iter.hasNext();) {
			Anchor anchor = iter.next();
			Page childOrNull = anchor.getChildPage();
			if (childOrNull != null) {
				result.add(childOrNull);
			}
		}
		return result;
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getPage().getCmsClient();
	}

	/**
	 * Liefert den letzen Anchor aus der Liste.
	 */
	private Anchor getLastAnchor() {

		java.util.List<Anchor> list = getAnchors();

		return list.get(list.size() - 1);
	}

	/**
	 * Liefert die link Guid des letzten Ankers in der Liste.
	 */
	private String getLastAnchorGuid() {

		return getLastAnchor().getAnchorGuid();
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {
		return getPage().getLogonGuid();
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
	 * Fügt einen Textanker an der gegebenen position hinzu.<p>
	 * Achtung: Auf der Seite noch nicht aktivierte Dyn Links führen in dieser Methode zu einem Problem, da position dann 0 ist.<p>
	 * Eine automatische Aktivierung, wie es für TemplateElemente stattfindet, muss hier noch implementiert werden.
	 * 
	 * @param position
	 *            Position des Links in der Liste.
	 * @param name
	 *            Bezeichnung des Links
	 * @param anchorGuid
	 *            guid des Ankers
	 */
	void set(int position, String name, String anchorGuid) {

		// create new anchor
		TextAnchor textAnchor = new TextAnchor(getPage(), templateElement, name, anchorGuid, false); // FIXME: wild guesses

		// add to anchors list
		anchors.set(position - 1, textAnchor);
	}
}
