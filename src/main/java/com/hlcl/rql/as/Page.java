package com.hlcl.rql.as;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.RqlKeywordObject;

/**
 * Diese Klasse repräsentiert eine RedDot Seite.
 * 
 * @author LEJAFR
 */
public class Page extends RqlKeywordObject implements ProjectContainer {
	private static final String PAGE_ACTION_REJECT = "16384"; // 2^14
	private static final String PAGE_ACTION_RELEASE = "4096"; // 2^12

	// change page state; action flags to use
	private static final String PAGE_ACTION_SUBMIT_TO_WORKFLOW = "32768"; // 2^15
	public final static int PAGE_GUID_LENGTH = 32; // fix length of page GUID
	private static final int PAGE_STATE_RELEASED_BIT_INDEX = 19; // 2^19=524288
	private static final int PAGE_STATE_LOCKED_BIT_INDEX = 28; // 2^28=268435456
	// page states
	private static final int PAGE_STATE_SAVED_AS_DRAFT_BIT_INDEX = 18; // 2^18=262144
	private static final int PAGE_STATE_WAITING_FOR_CORRECTION_BIT_INDEX = 17; // 2^17=131072
	private static final int PAGE_STATE_WAITING_FOR_RELEASE_BIT_INDEX = 6; // 2^6=64
	protected final static String TREESEGMENT_TYPE = "page";

	// caches
	private RQLNode detailsNode;
	private RQLNodeList elementsNodeList;
	private RQLNode urlNode; // redirect
	private String headline;
	private RQLNodeList linksNodeList;
	private String pageGuid;
	private String pageId;
	private StringBuilder deleteElementValuesRequest = null;
	private HashMap<Element, Object> setElementValuesMap = null;
	private String databaseQueryCache = null;
	private PublicationPackage publicationPackage = null;


	private Project project;

	// lazy initialized
	private Template template;

	/**
	 * Erzeugt eine Seite aus den gegebenen Daten. Die pageId wird nicht gesetzt, da sie nicht für RQL requests benoetigt wird.
	 * 
	 * @param project
	 *            Projekt zu dem diese Seite gehört
	 * @param pageGuid
	 *            RedDot page guid dieser Seite
	 * @see <code>getPageId</code>
	 */
	public Page(Project project, String pageGuid) throws RQLException {

        this(project, null, pageGuid, null, null);
	}

	/**
	 * Erzeugt eine Seite aus den gegebenen Daten.
	 * 
	 * @param project
	 *            Projekt zu dem diese Seite gehört
	 * @param pageGuid
	 *            RedDot page guid dieser Seite
	 * @param pageId
	 *            RedDot page id
	 * @param headline
	 *            headline of this page
	 */
	public Page(Project project, String pageGuid, String pageId, String headline) throws RQLException{

        this(project, null, pageGuid, pageId, headline);
	}

	/**
	 * Erzeugt eine Seite aus den gegebenen Daten.
	 * 
	 * @param project
	 *            Projekt zu dem diese Seite gehört
	 * @param template
	 *            Template auf dem diese Seite basiert
	 * @param pageGuid
	 *            RedDot page guid dieser Seite
	 * @param pageId
	 *            RedDot page id
	 * @param headline
	 *            headline of this page
	 */
	public Page(Project project, Template template, String pageGuid, String pageId, String headline) throws RQLException {

        // check if given
        if (pageGuid.trim().length() == 0) {
            throw new MissingGuidException("Page could not be created because the page guid is not delivered.");
        }

		this.project = project;
		this.template = template;
		this.pageGuid = pageGuid;
		this.pageId = pageId;
		this.headline = headline;
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * <p>
	 * Der gegebenen Wert value wird dem Autor angezeigt. Es ist nicht die GUID der OptionListSelection.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addCopyOptionListValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		OptionList sourceElem = sourcePage.getOptionList(templateElementName);
		if (sourceElem.isValueEntered()) {
			setElementValuesMap.put(getOptionList(templateElementName), sourceElem.getCurrentSelection().getValue());
		}
	}

	/**
	 * Adds the given page element for the given template elements to the list of elements which value will be changed on this page.
	 * <p>
	 * Der gegebenen Wert value wird dem Autor angezeigt. Es ist nicht die GUID der OptionListSelection.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addCopyOptionListValuesFrom(String templateElementNamePrefix, int first, int last, String templateElementNameSuffix,
			Page sourcePage) throws RQLException {
		for (int i = first; i <= last; i++) {
			String templateElementName = templateElementNamePrefix + i + templateElementNameSuffix;
			addCopyOptionListValueFrom(templateElementName, sourcePage);
		}
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addCopyStandardFieldDateValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		StandardFieldDateElement sourceElem = sourcePage.getStandardFieldDateElement(templateElementName);
		if (sourceElem.isValueEntered()) {
			setElementValuesMap.put(getStandardFieldDateElement(templateElementName), sourceElem.getDate());
		}
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addCopyStandardFieldNumericValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		StandardFieldNumericElement sourceElem = sourcePage.getStandardFieldNumericElement(templateElementName);
		if (sourceElem.isValueEntered()) {
			setElementValuesMap.put(getStandardFieldNumericElement(templateElementName), sourceElem.getInt());
		}
	}

	/**
	 * Adds the given page element for the given template elements to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addCopyStandardFieldNumericValuesFrom(String templateElementNamePrefix, int first, int last,
			String templateElementNameSuffix, Page sourcePage) throws RQLException {
		for (int i = first; i <= last; i++) {
			String templateElementName = templateElementNamePrefix + i + templateElementNameSuffix;
			addCopyStandardFieldNumericValueFrom(templateElementName, sourcePage);
		}
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addCopyStandardFieldTextValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		StandardFieldTextElement sourceElem = sourcePage.getStandardFieldTextElement(templateElementName);
		if (sourceElem.isValueEntered()) {
			setElementValuesMap.put(getStandardFieldTextElement(templateElementName), sourceElem.getText());
		}
	}

	/**
	 * Adds the given page element for the given template elements to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addCopyStandardFieldTextValuesFrom(String templateElementNamePrefix, int first, int last,
			String templateElementNameSuffix, Page sourcePage) throws RQLException {
		for (int i = first; i <= last; i++) {
			String templateElementName = templateElementNamePrefix + i + templateElementNameSuffix;
			addCopyStandardFieldTextValueFrom(templateElementName, sourcePage);
		}
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addCopyStandardFieldUserDefinedValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		StandardFieldUserDefinedElement sourceElem = sourcePage.getStandardFieldUserDefinedElement(templateElementName);
		if (sourceElem.isValueEntered()) {
			setElementValuesMap.put(getStandardFieldUserDefinedElement(templateElementName), sourceElem.getText());
		}
	}

	/**
	 * Adds the given Element to the list of elements which value will be deleted.
	 * 
	 * @see #startDeleteElementValues()
	 * @see #endDeleteElementValues()
	 * @see Element#deleteValue()
	 * @see TextElement#deleteValue()
	 */
	public void addDeleteElementValue(Element element) throws RQLException {
		addDeleteElementValue(element.getElementGuid());
	}

	/**
	 * Adds the given Element GUID to the list of elements which value will be deleted.
	 * 
	 * @see #startDeleteElementValues()
	 * @see #addDeleteElementValue(Element)
	 * @see #endDeleteElementValues()
	 * @see Element#deleteValue()
	 * @see TextElement#deleteValue()
	 */
	public void addDeleteElementValue(String elementGuid) throws RQLException {
		if (deleteElementValuesRequest == null) {
			throw new RQLException(
					"You try to delete element values with one request, but you have to use the method #startDeleteElementValues() first.");
		}

		deleteElementValuesRequest.append("<ELT action='save' guid='").append(elementGuid)
		.append("' value='#").append(getSessionKey()).append("'/>");
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be deleted on this page.
	 * 
	 * @see #startDeleteElementValues()
	 * @see #endDeleteElementValues()
	 * @see Element#deleteValue()
	 * @see TextElement#deleteValue()
	 */
	public void addDeleteStandardFieldTextValue(String templateElementName) throws RQLException {
		addDeleteElementValue(getStandardFieldTextElement(templateElementName));
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be deleted on this page.
	 * 
	 * @see #startDeleteElementValues()
	 * @see #endDeleteElementValues()
	 * @see Element#deleteValue()
	 * @see TextElement#deleteValue()
	 */
	public void addDeleteStandardFieldUserDefinedValue(String templateElementName) throws RQLException {
		addDeleteElementValue(getStandardFieldUserDefinedElement(templateElementName));
	}

	/**
	 * Adds the given Element to the list of elements which value will be deleted.
	 * 
	 * @see #startDeleteElementValues()
	 * @see #endDeleteElementValues()
	 * @see Element#deleteValue()
	 * @see TextElement#deleteValue()
	 */
	public void addDeleteTextValue(TextElement textElement) throws RQLException {
		addDeleteElementValue(textElement.getElementGuid());
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * <p>
	 * Der gegebenen Wert value wird dem Autor angezeigt. Es ist nicht die GUID der OptionListSelection.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addSetOptionListValue(String templateElementName, String value) throws RQLException {
		setElementValuesMap.put(getOptionList(templateElementName), value);
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addSetStandardFieldDateValue(String templateElementName, ReddotDate value) throws RQLException {
		setElementValuesMap.put(getStandardFieldDateElement(templateElementName), value);
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addSetStandardFieldDateValueToToday(String templateElementName) throws RQLException {
		setElementValuesMap.put(getStandardFieldDateElement(templateElementName), ReddotDate.now());
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addSetStandardFieldNumericValue(String templateElementName, int value) throws RQLException {
		setElementValuesMap.put(getStandardFieldNumericElement(templateElementName), value);
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addSetStandardFieldNumericValue(String templateElementName, String value) throws RQLException {
		setElementValuesMap.put(getStandardFieldNumericElement(templateElementName), value);
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addSetStandardFieldTextValue(String templateElementName, String value) throws RQLException {
		setElementValuesMap.put(getStandardFieldTextElement(templateElementName), value);
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addSetStandardFieldEmailValue(String templateElementName, String value) throws RQLException {
		setElementValuesMap.put(getStandardFieldEmailElement(templateElementName), value);
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addSetStandardFieldUrlValue(String templateElementName, String value) throws RQLException {
		setElementValuesMap.put(getStandardFieldUrlElement(templateElementName), value);
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addSetStandardFieldUserDefinedValue(String templateElementName, String value) throws RQLException {
		setElementValuesMap.put(getStandardFieldUserDefinedElement(templateElementName), value);
	}

	/**
	 * Ordnet dieser Seite das gegebene Berechtigungspaket zu.
	 */
	public void assignAuthorizationPackage(AuthorizationPackage authorizationPackage, boolean inherit) throws RQLException {

		// check type
		if (!authorizationPackage.isPageAuthorizationPackage()) {
			throw new WrongTypeException("Authorization package with name " + authorizationPackage.getName()
					+ " has wrong type and cannot be linked to page " + getHeadlineAndId() + ".");
		}

		// call CMS
		getProject().assignAuthorizationPackage("PAGE", getPageGuid(), authorizationPackage);

		// inherit
		if (inherit) {
			authorizationPackage.inherit(this);
		}
	}

	/**
	 * Ordnet dem Element dieser Seite, das auf dem gegebenen TemplateElement basiert, das gegebene Detail-Berechtigungspaket zu.
	 */
	public void assignAuthorizationPackage(AuthorizationPackage authorizationPackage, TemplateElement templateElement)
			throws RQLException {
		// check content elements
		if (templateElement.isContentElement() && !authorizationPackage.isDetailedElementAuthorizationPackage()) {
			throw new WrongTypeException("Authorization package with name " + authorizationPackage.getName()
					+ " has wrong type and cannot be linked to content element " + templateElement.getName() + " in page "
					+ getHeadlineAndId() + ".");
		}
		// check structural elements
		if (templateElement.isStructuralElement() && !authorizationPackage.isDetailedLinkAuthorizationPackage()) {
			throw new WrongTypeException("Authorization package with name " + authorizationPackage.getName()
					+ " has wrong type and cannot be linked to structural element " + templateElement.getName() + " in page "
					+ getHeadlineAndId() + ".");
		}
		// get element or link node
		RQLNode node = null;
		if (templateElement.isContentElement()) {
			node = findElementNode(templateElement);
		}
		if (templateElement.isStructuralElement()) {
			node = findLinkNode(templateElement);
		}

		// assign to page element
		getProject().assignAuthorizationPackage("ELEMENT", node.getAttribute("guid"), authorizationPackage);
	}

	/**
	 * Erstellt einen Container aus dem LinkNode.
	 * 
	 * @param containerLinkNode
	 *            muss vom Typ 28 (Container) sein.
	 * @return <code>Container</code>
	 * @see <code>Container</code>
	 */
	private Container buildContainer(RQLNode containerLinkNode, TemplateElement templateElement, Page existsOnPage)
			throws RQLException {

		return new Container(existsOnPage, templateElement, containerLinkNode.getAttribute("name"), containerLinkNode
				.getAttribute("guid"), containerLinkNode.getAttribute("islink").equals("10"));
	}

	/**
	 * Erstellt eine Liste aus einem LinkNode mit Type = 13.
	 * 
	 * @param listLinkNode
	 *            muss vom Typ 13 (Liste) sein.
	 * @return <code>List</code>
	 * @see <code>List</code>
	 */
	private com.hlcl.rql.as.List buildList(RQLNode listLinkNode, TemplateElement templateElement, Page existsOnPage)
			throws RQLException {

		return new com.hlcl.rql.as.List(existsOnPage, templateElement, listLinkNode.getAttribute("name"), listLinkNode
				.getAttribute("guid"), listLinkNode.getAttribute("islink").equals("10"), listLinkNode
				.getAttribute("targetcontainerguid"));
	}

	/**
	 * Erstellt einen MultiLink dieser Seite (Liste oder Container).
	 */
	private MultiLink buildMultiLink(RQLNode linkNode) throws RQLException {

		MultiLink multiLink = null;

		int type = Integer.parseInt(linkNode.getAttribute("elttype"));
		// determine the page where the link is element of
		// prefer existing page object
		String existsOnPageGuid = linkNode.getAttribute("pageguid");
		Page existsOnPage = this;
		if (!existsOnPageGuid.equals(getPageGuid())) {
			existsOnPage = getProject().getPageByGuid(existsOnPageGuid);
		}
		TemplateElement templateElement = existsOnPage.getTemplateElementByGuid(linkNode.getAttribute("templateelementguid"));
		if (type == 13) {
			multiLink = buildList(linkNode, templateElement, existsOnPage);

            String datebegin = linkNode.getAttribute("datebegin");
            String dateend = linkNode.getAttribute("dateend");
            if(datebegin != null && !datebegin.equals("0") && dateend != null && !dateend.equals("0")){
                multiLink.setAppearanceSchedule(AppearanceSchedule.forPeriod(datebegin, dateend));
            }

		} else if (type == 28) {
			multiLink = buildContainer(linkNode, templateElement, existsOnPage);
        } else {
			throw new WrongTypeException("Creation of a List(13) or Container(28) fails, because given link node has type " + type
					+ ".");
		}
		return multiLink;
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
	 * Macht den daft-Status dieser Seite rückgängig:
	 * <p> - löscht eine neu angelegte Seite oder
	 * <p> - macht die Änderungen daran rückgängig
	 */
	public void cancelDraftState() throws RQLException {

		if (isInStateSavedAsDraftNew()) {
			delete();
		} else if (isInStateSavedAsDraftChanged()) {
			undoChanges();
		}
	}

	/**
	 * Ändert die Überschrift dieser Seite, um sie wieder publizieren zu können.
	 * <p> - hängt ein blank an oder
	 * <p> - entfernt an bereits angehängtes blank von der Überschrift wieder
	 * <p>
	 * Seite wird nicht geändert, falls diese Seite ein GUID page ist, also gar keine Überschrift hat.
	 */
	public void changeHeadline() throws RQLException {

		if (hasHeadline()) {
			String headline = getHeadline();
			// change
			int length = headline.length();
			if (headline.charAt(length - 1) == ' ') {
				headline = headline.substring(0, length - 2);
			} else {
				headline += " ";
			}
			// update
			setHeadline(headline);
		}
	}

	/**
	 * Ändert den Status dieser Seite; gibt diese Seite frei oder schickt sie zur Korrektur zurück.
	 * 
	 * Lt. RQL Doku können folgende Stati gesetzt werden: 32768 = Seitenbearbeitung abschließen (submit to workflow); im Workflow:
	 * Seite wartet auf Freigabe 16384 = Seite zur Korrektur zurücksenden (reject) 4096 = Seite freigeben (release)
	 * 
	 * @param newActionFlag
	 *            berechneter neuer Seitenstatus
	 */
	private RQLNode changeState(String newActionFlag) throws RQLException {

		/* 
		 V6.5 request
		 <IODATA loginguid="FC795754C7754BDCA68170D1B8DD94CF" sessionkey="925833953ar10vo61yBI">
		 <PAGE action="save" guid="792F72BCCE13424FA151B12DFDF203C2" actionflag="4096" globalsave="0"/>
		 </IODATA>
		 V6.5 response (all fields filled)
		 <IODATA>
		 <PAGE action="save" guid="792F72BCCE13424FA151B12DFDF203C2" sessionkey="925833953ar10vo61yBI" dialoglanguageid="ENG" useconnection="1" userguid="4324D172EF4342669EAF0AD074433393" changeuserguid="8898998310DD4513BB8CC1771FFD00BC" mainlinkguid="20467D958D7743488A2356B6CC1099D0" glrights1="-1" glrights2="-1" glrights3="-1" glrights5="-1" glrights6="-1" gldenys1="0" gldenys2="0" gldenys3="0" gldenys5="0" gldenys6="0" rights1="-1" rights2="-1" rights3="-1" rights4="-1" rights5="-1" rights6="-1" saveexplicituserguid="8898998310DD4513BB8CC1771FFD00BC" actionflag="32768" languagevariantid="ENG"/>
		 </IODATA>
		 V6.5 response (missing mandatory fields)
		 <IODATA>
		 <EMPTYELEMENTS  pageguid="C1BAC7FA66734D1EB00223D7D58BA6F6" pageheadline="or - de: Points of Entry">
		 <ELEMENT guid="A28560E00D434A70A8AC87E223B2333E" name="responsible_id" type="1"></ELEMENT>
		 <ELEMENT guid="CB508F4828BD409E893AF3963C87F5B4" name="responsible_name" type="1"></ELEMENT>
		 </EMPTYELEMENTS>
		 </IODATA>
		 
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <PAGE action='save' guid='" + getPageGuid() + "' actionflag='" + newActionFlag + "' globalsave='0'/>"
				+ "</IODATA>";
		RQLNode response = callCms(rqlRequest);

		// force new read of details node
		deleteDetailsNodeCache();

		// for further checks
		return response;
	}

	/**
	 * Check if the page node has an attribute id. if not throw an exception, because the page guid is missing.
	 * 
	 * @throws InvalidGuidException
	 */
	private void checkDetailsNode() throws InvalidGuidException {
		// page GUID invalid if page ID attribute not found
		if (detailsNode.getAttribute("id") == null) {
			throw new InvalidGuidException("No page can be found for page GUID " + pageGuid
					+ ". Maybe the page was deleted in between");
		}
	}

	/**
	 * Erzwingt nach dem Wechsel der Sprachvariante das erneute auslesen, durch löschen aller Caches mit Seitendaten.
	 * 
	 * @see #freeOccupiedMemory()
	 */
	public void clearLanguageVariantDependentCaches() {

		headline = null;
		deleteDetailsNodeCache();
		deleteElementsNodeListCache();
		// links node list
		// keep template
	}

	/**
	 * Liefert alle in dieser seite gefundenen Begriffe. Das zurückgegebene Set ist leer, wenn keiner der Begriffe auf dieser Seite
	 * vorkommt.
	 * 
	 * @param findList
	 *            the list with strings to check for; for instance HLCL,Container Line,Container Linie
	 * @param delimiter
	 *            the ,
	 * @param caseSensitive
	 *            case sensitive search or not; to ignore case set to false
	 */
	public Set<String> collectContainedText(String findList, String delimiter, boolean searchHeadline, boolean caseSensitive)
			throws RQLException {

		Set<String> result = new HashSet<String>();
		// check only filled StandardFieldText elements first
		java.util.List<StandardFieldTextElement> sftElems = getFilledStandardFieldTextElements();
		for (Iterator<StandardFieldTextElement> iter = sftElems.iterator(); iter.hasNext();) {
			StandardFieldTextElement element = iter.next();
			result.addAll(element.collectContainedText(findList, delimiter, caseSensitive));
		}

		// check only filled TextElements too
		java.util.List<TextElement> textElems = getFilledTextElements();
		for (Iterator<TextElement> iter = textElems.iterator(); iter.hasNext();) {
			TextElement element = iter.next();
			result.addAll(element.collectContainedText(findList, delimiter, caseSensitive));
		}

		// search headline
		if (searchHeadline) {
			result.addAll(StringHelper.collectContainedText(getHeadline(), findList, delimiter, caseSensitive));
		}
		return result;
	}

	/**
	 * Liefert eine Liste mit allen über MainLink verknüpften Seiten und den Links dazwischen. Erste Seite ist die Projektstartseite
	 * und letzte ist diese Seite selbst. Die Anzahl Elemente in der Liste ist immer ungerade!
	 * 
	 * ACHTUNG: Funktioniert nicht, da das links action=load für die Projektstartseite nichts liefert.
	 */
	public java.util.List<ProjectContainer> collectMainLinkChainUntilRoot() throws RQLException {
		/* 
		 V6.5 request
		 <IODATA loginguid="E572DB5C192D47DF87B6A91D1DF77B54" sessionkey="1021834323qeTOO46l1Ir">
		 <TREESEGMENT action="gettreenodepath" guid="43E0C4B9C16340229A8DE8289269C7DA" type="page"/>
		 </IODATA>
		 V5 response (verlinkt)
		 <IODATA>
		 <TREESEGMENTS>
		 <SEGMENT guid="00000000000000000000000000000001" parentguid="" value="Start" image="" type="start" flags="0" expand="1" close="1" col1value="Start" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="6AAC3B14535511D4BDAB004005312B7C" parentguid="00000000000000000000000000000001" value="Administer Project Settings" image="MenuElement.gif" position="1" type="app.mnuProjectPreferences" flags="0" expand="1" close="0" descent="app" col1value="Administer Project Settings" col2fontcolor="#ff8C00" col2value="" col1fontweight="BOLD" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="4AF89E44535511D4BDAB004005312B7C" parentguid="00000000000000000000000000000001" value="Administer Content Classes" image="MenuElement.gif" position="3" type="project.4000" flags="0" expand="1" close="0" descent="app" col1value="Administer Content Classes" col2fontcolor="#ff8C00" col2value="" col1fontweight="BOLD" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="290C8FB4535511D4BDAB004005312B7C" parentguid="00000000000000000000000000000001" value="Administer Publication" image="MenuElement.gif" position="4" type="app.7000" flags="0" expand="1" close="0" descent="app" col1value="Administer Publication" col2fontcolor="#ff8C00" col2value="" col1fontweight="BOLD" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="B015D262080E474C9EAE5A70EEE70EDA" parentguid="00000000000000000000000000000001" value="Browse Navigation" image="MenuElement.gif" position="5" type="project.6500" flags="0" expand="1" close="0" descent="app" col1value="Browse Navigation" col2fontcolor="#ff8C00" col2value="" col1fontweight="BOLD" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="04C7F874535511D4BDAB004005312B7C" parentguid="00000000000000000000000000000001" value="Administer Project Structure" image="MenuElement.gif" position="6" type="app.mnuProjectStructure" flags="0" expand="1" close="1" descent="app" col1value="Administer Project Structure" col2fontcolor="#ff8C00" col2value="" col1fontweight="BOLD" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="6C8A3094541911D4BDAB004005312B7C" parentguid="04C7F874535511D4BDAB004005312B7C" value="Project" image="Project.gif" position="1" type="project" flags="0" expand="0" close="1" descent="app" col1value="Project" col2fontcolor="#ff8C00" col2value="" col1fontweight="BOLD" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="1408C16F8664401BA2D960B78F3FC614" parentguid="6C8A3094541911D4BDAB004005312B7C" close="1" type="page" image="page.gif" pageid="42004" flags="0" expand="1" value2=" " value="HIP - RedDot start page" col1value="HIP - RedDot start page" col2fontcolor="#808080" col2value=" " col1fontweight="bold" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="9F2C3B5C2D094BD2AF6EC29443C55ACF" parentguid="1408C16F8664401BA2D960B78F3FC614" close="1" islink="2" name="list" type="link" value="list" imagetype="13" expand="3" col1value="list" col1fontcolor="#0000ff" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="D2EF596D9A0E4FC0BF5C05E8BCDD2274" parentguid="9F2C3B5C2D094BD2AF6EC29443C55ACF" close="1" type="page" image="page.gif" pageid="127290" flags="524288" expand="1" value2=" " value="HIP Portal pages (all pages found in hip.hlcl.com production)" col1value="HIP Portal pages (all pages found in hip.hlcl.com production)" col2fontcolor="#808080" col2value=" " col1fontweight="bold" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="C1E60F7960FB4C24ABD7339CA487F819" parentguid="D2EF596D9A0E4FC0BF5C05E8BCDD2274" close="1" islink="2" name="list" type="link" value="list" imagetype="13" expand="3" col1value="list" col1fontcolor="#0000ff" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="18E5D267B8164C73885FBA34B2991020" parentguid="C1E60F7960FB4C24ABD7339CA487F819" close="1" type="page" image="page.gif" pageid="2" flags="0" expand="1" value2=" " value="Processes tree " col1value="Processes tree " col2fontcolor="#808080" col2value=" " col1fontweight="bold" col2fontweight="normal"></SEGMENT>
		 ...
		 <SEGMENT guid="CE9E3057342C4DC38D506FE0027EDCE2" parentguid="F886231363904F76882CBD03974F271D" close="1" islink="2" name="content_pages_list" type="link" value="following_rows_list" imagetype="13" expand="3" col1value="following_rows_list" col1fontcolor="#0000ff" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="43E0C4B9C16340229A8DE8289269C7DA" parentguid="CE9E3057342C4DC38D506FE0027EDCE2" close="0" type="page" image="page.gif" pageid="27006" flags="524288" expand="1" value2=" " value="Details to all block content classes (Template Samples)" col1value="Details to all block content classes (Template Samples)" col2fontcolor="#808080" col2value=" " col1fontweight="bold" col2fontweight="normal"></SEGMENT>
		 </TREESEGMENTS>
		 </IODATA>
		 
		 V6.5 response (not linked)
		 <IODATA>
		 <TREESEGMENTS>
		 <SEGMENT guid="00000000000000000000000000000001" parentguid="" value="Start" image="" type="start" flags="0" expand="1" close="1" col1value="Start" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="6AAC3B14535511D4BDAB004005312B7C" parentguid="00000000000000000000000000000001" value="Administer Project Settings" image="MenuElement.gif" position="1" type="app.mnuProjectPreferences" flags="0" expand="1" close="0" descent="app" col1value="Administer Project Settings" col2fontcolor="#ff8C00" col2value="" col1fontweight="BOLD" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="4AF89E44535511D4BDAB004005312B7C" parentguid="00000000000000000000000000000001" value="Administer Content Classes" image="MenuElement.gif" position="3" type="project.4000" flags="0" expand="1" close="0" descent="app" col1value="Administer Content Classes" col2fontcolor="#ff8C00" col2value="" col1fontweight="BOLD" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="290C8FB4535511D4BDAB004005312B7C" parentguid="00000000000000000000000000000001" value="Administer Publication" image="MenuElement.gif" position="4" type="app.7000" flags="0" expand="1" close="0" descent="app" col1value="Administer Publication" col2fontcolor="#ff8C00" col2value="" col1fontweight="BOLD" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="B015D262080E474C9EAE5A70EEE70EDA" parentguid="00000000000000000000000000000001" value="Browse Navigation" image="MenuElement.gif" position="5" type="project.6500" flags="0" expand="1" close="0" descent="app" col1value="Browse Navigation" col2fontcolor="#ff8C00" col2value="" col1fontweight="BOLD" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="04C7F874535511D4BDAB004005312B7C" parentguid="00000000000000000000000000000001" value="Administer Project Structure" image="MenuElement.gif" position="6" type="app.mnuProjectStructure" flags="0" expand="1" close="1" descent="app" col1value="Administer Project Structure" col2fontcolor="#ff8C00" col2value="" col1fontweight="BOLD" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="71FF2D7290CB4FD080D35813C56125F2" parentguid="04C7F874535511D4BDAB004005312B7C" value="Edit Special Pages" image="PageOverview.gif" position="2" type="app.1006" flags="0" expand="0" descent="app" col1value="Edit Special Pages" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="60B59419CCD64C05B453BBD6EDA62C93" parentguid="71FF2D7290CB4FD080D35813C56125F2" value="Unlinked Pages" image="freepages.gif" position="1" type="app.1805" flags="0" expand="0" close="1" descent="app" col1value="Unlinked Pages" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="8B65922311D34DD0BE75CD8D46E00F0C" parentguid="71FF2D7290CB4FD080D35813C56125F2" value="Tree Segments" image="partialtree.gif" position="3" type="app.1810" flags="0" expand="0" close="1" descent="app" col1value="Tree Segments" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="24F3997B2A77465BACC2BB7ABCCDC825" parentguid="8B65922311D34DD0BE75CD8D46E00F0C" close="1" type="page" image="page.gif" pageid="2218" flags="532480" expand="1" value2=" " value="Entwicklungshilfsmittel TOC" col1value="Entwicklungshilfsmittel TOC" col2fontcolor="#808080" col2value=" " col1fontweight="bold" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="6E731C45EE5F46F6BA65CDB9DE2B6DEC" parentguid="24F3997B2A77465BACC2BB7ABCCDC825" close="1" islink="2" name="chapter_list" type="link" value="chapter_list" imagetype="13" expand="3" col1value="chapter_list" col1fontcolor="#0000ff" col2fontcolor="#ff8C00" col2value="" col1fontweight="normal" col2fontweight="normal"></SEGMENT>
		 <SEGMENT guid="14EABB3EB6DE46E7A38943218A7CECB7" parentguid="6E731C45EE5F46F6BA65CDB9DE2B6DEC" close="0" type="page" image="page.gif" pageid="2257" flags="524288" expand="1" value2=" " value="Blockmode Testrahmen" col1value="Blockmode Testrahmen" col2fontcolor="#808080" col2value=" " col1fontweight="bold" col2fontweight="normal"></SEGMENT>
		 </TREESEGMENTS>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <TREESEGMENT action='gettreenodepath' guid='" + getPageGuid() + "' type='page' />" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		// collect and build pages and multi links
		java.util.List<ProjectContainer> result = new ArrayList<ProjectContainer>();
		RQLNodeList segmentNodes = rqlResponse.getNodes("SEGMENT");
		for (int i = 0; i < segmentNodes.size(); i++) {
			RQLNode node = (RQLNode) segmentNodes.get(i);
			String type = node.getAttribute("type");
			if (type.equals(TREESEGMENT_TYPE)) {
				result.add(getProject().getPageByGuid(node.getAttribute("guid")));
			}
			if (type.equals(MultiLink.TREESEGMENT_TYPE)) {
				Page linkPg = getProject().getPageByGuid(node.getAttribute("parentguid"));
				result.add(linkPg.getMultiLinkByGuid(node.getAttribute("guid")));
			}
		}
		return result;
	}

	/**
	 * Liefert eine Liste mit allen über MainLink verknüpften Seiten. Erste Seite ist die Projektstartseite und letzte ist diese Seite
	 * selbst.
	 */
	public PageArrayList collectMainLinkParentPagesUntilRoot() throws RQLException {

		PageArrayList result = new PageArrayList();

		// collect going backwards until project start page
		Page page = this;
		try {
			while (true) {
				result.add(page);
				page = page.getMainLinkParentPage();
			}
		} catch (UnlinkedPageException upe) {
			// simply end iteration
		}

		// reverse and return
		Collections.reverse(result);
		return result;
	}

	/**
	 * Liefert true genau dann, wenn die Seite ein Element hat, das auf dem gegebenen TemplateElement basiert.
	 */
	public boolean contains(String templateElementName) throws RQLException {

		return getTemplate().contains(templateElementName);
	}

	/**
	 * Kopiert die Werte aller content elemente aus sourcePage auf diese Seite.
	 * <p>
	 * Es werden nur die folgende TemplateElement Typen unterstützt: all subclasses of {@link Element} and {@link TextElement}.
	 */
	public void copyContentElementValuesFrom(Page sourcePage, boolean includeHeadline, boolean includeReferences) throws RQLException {
		copyContentElementValuesFrom(sourcePage, includeHeadline, includeReferences, "", ",");
	}

	/**
	 * Kopiert die Werte aller content elemente aus sourcePage auf diese Seite. Die Elementwerte in der liste ignoreElementNames werden
	 * nicht mit kopiert.
	 * <p>
	 * Es werden nur die folgende TemplateElement Typen unterstützt: all subclasses of {@link Element} and {@link TextElement}.
	 */
	public void copyContentElementValuesFrom(Page sourcePage, boolean includeHeadline, boolean includeReferences,
			String ignoreElementNames, String separator) throws RQLException {

		// copy headline if needed
		if (includeHeadline) {
			setHeadline(sourcePage.getHeadline());
		}
		// start combined update
		startSetElementValues();

		// for all template elements do
		java.util.List<TemplateElement> sourceTemplateElements = getTemplate().getContentElements(false, includeReferences,
				ignoreElementNames, separator);
		for (TemplateElement sourceTmpltElem : sourceTemplateElements) {
			String templateElementName = sourceTmpltElem.getName();

			// never copy given elements
			if (ignoreElementNames.trim().length() != 0 && StringHelper.contains(ignoreElementNames, separator, templateElementName)) {
				continue;
			}

			// special handling depending on type
			// immediate update on this page
			if (sourceTmpltElem.isText()) {
				copyTextValueFrom(templateElementName, sourcePage);
			} else if (sourceTmpltElem.isImage()) {
				copyImageValueFrom(templateElementName, sourcePage);
			} else if (sourceTmpltElem.isMedia()) {
				copyMediaValueFrom(templateElementName, sourcePage);
			} else
			// use combined update possibility
			if (sourceTmpltElem.isOptionList()) {
				addCopyOptionListValueFrom(templateElementName, sourcePage);
			} else if (sourceTmpltElem.isStandardFieldDate()) {
				addCopyStandardFieldDateValueFrom(templateElementName, sourcePage);
			} else if (sourceTmpltElem.isStandardFieldNumeric()) {
				addCopyStandardFieldNumericValueFrom(templateElementName, sourcePage);
			} else if (sourceTmpltElem.isStandardFieldText()) {
				addCopyStandardFieldTextValueFrom(templateElementName, sourcePage);
			} else if (sourceTmpltElem.isStandardFieldUserDefined()) {
				addCopyStandardFieldUserDefinedValueFrom(templateElementName, sourcePage);
			} else {
				// type hot handled appropriately
				throw new RQLException("You try to copy an element of the unsupported type " + sourceTmpltElem.getTypeName()
						+ ". Allowed are only all subclasses of Element and TextElement.");
			}
		}
		// update combined
		endSetElementValues();
	}

	/**
	 * Add the change of given string value to page element with given templateElementName. All string value (and numeric) element
	 * types are supported, but not date (missing parse pattern).
	 * <p>
	 * Text elements are immediately stored; the value is the source code of a HTML text element.
	 * <p>
	 * Es werden nur die folgende TemplateElement Typen unterstützt: all subclasses of {@link Element} and {@link TextElement}.
	 */
	public void addSetContentElementStringValue(String templateElementName, String value) throws RQLException {
		TemplateElement tmpltElem = getTemplateElementByName(templateElementName);
		// special handling depending on type
		// immediate update on this page
		if (tmpltElem.isText()) {
			setTextValue(templateElementName, value);
		} else if (tmpltElem.isImage()) {
			setImageValue(templateElementName, value);
		} else if (tmpltElem.isMedia()) {
			setMediaValue(templateElementName, value);
		} else
		// use combined update possibility
		if (tmpltElem.isOptionList()) {
			addSetOptionListValue(templateElementName, value);
		} else if (tmpltElem.isStandardFieldNumeric()) {
			addSetStandardFieldNumericValue(templateElementName, value);
		} else if (tmpltElem.isStandardFieldText()) {
			addSetStandardFieldTextValue(templateElementName, value);
		} else if (tmpltElem.isStandardFieldUserDefined()) {
			addSetStandardFieldUserDefinedValue(templateElementName, value);
		} else {
			// type hot handled appropriately
			throw new RQLException("You try to set an element of the unsupported type " + tmpltElem.getTypeName()
					+ ". Allowed are only all subclasses of Element (except standard field date) and TextElement.");
		}
	}

	/**
	 * Kopiert den Wert des ImageElementes der gegebenen Seite sourcePage, das auf dem gegebenen templateElement basiert in das
	 * gleichnamige Element dieser Seite.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyImageValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		sourcePage.copyImageValueTo(templateElementName, this);
	}

	/**
	 * Kopiert den Wert des ImageElementes dieser Seite, das auf dem gegebenen templateElement basiert in das targetElement.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyImageValueTo(String sourceTemplateElementName, ImageElement targetElement) throws RQLException {
		ImageElement sourceElem = getImageElement(sourceTemplateElementName);
		if (sourceElem.isValueEntered()) {
			targetElement.setFilename(sourceElem.getFilename());
		}
	}

	/**
	 * Kopiert den Wert des ImageElementes dieser Seite, das auf dem gegebenen templateElement basiert in das gleichnamige Element in
	 * targetPage.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyImageValueTo(String templateElementName, Page targetPage) throws RQLException {
		copyImageValueTo(templateElementName, targetPage.getImageElement(templateElementName));
	}

	/**
	 * Kopiert den Wert des MediaElementes der gegebenen Seite sourcePage, das auf dem gegebenen templateElement basiert in das
	 * gleichnamige Element dieser Seite.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyMediaValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		sourcePage.copyMediaValueTo(templateElementName, this);
	}

	/**
	 * Kopiert den Wert des MediaElementes dieser Seite, das auf dem gegebenen templateElement basiert in das targetElement.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyMediaValueTo(String sourceTemplateElementName, MediaElement targetElement) throws RQLException {
		MediaElement sourceElem = getMediaElement(sourceTemplateElementName);
		if (sourceElem.isValueEntered()) {
			targetElement.setFilename(sourceElem.getFilename());
		}
	}

	/**
	 * Kopiert den Wert des MediaElementes dieser Seite, das auf dem gegebenen templateElement basiert in das gleichnamige Element in
	 * targetPage.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyMediaValueTo(String templateElementName, Page targetPage) throws RQLException {
		copyMediaValueTo(templateElementName, targetPage.getMediaElement(templateElementName));
	}

	/**
	 * Kopiert den Wert des Verwaltungseintrages mit dem gegebenen Namen noteName der gegebenen Seite sourcePage in das gleichnamige
	 * Element dieser Seite.
	 */
	public void copyNoteValueFrom(String noteName, Page sourcePage) throws RQLException {
		sourcePage.copyNoteValueTo(noteName, this);
	}

	/**
	 * Kopiert den Wert des Verwaltungseintrages dieser Seite mit dem gegebenen Namen in das targetElement.
	 */
	public void copyNoteValueTo(String noteName, Note targetElement) throws RQLException {
		targetElement.setValue(getNoteValue(noteName));
	}

	/**
	 * Kopiert den Wert des Verwaltungseintrages dieser Seite mit dem gegebenen Namen in das gleichnamige Element in targetPage.
	 */
	public void copyNoteValueTo(String noteName, Page targetPage) throws RQLException {
		targetPage.setNoteValue(noteName, getNoteValue(noteName));
	}

	/**
	 * Kopiert den Wert der Optionsliste der gegebenen Seite sourcePage, das auf dem gegebenen templateElement basiert in das
	 * gleichnamige Element dieser Seite.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyOptionListValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		sourcePage.copyOptionListValueTo(templateElementName, this);
	}

	/**
	 * Kopiert die Werte der Optionsliste passend zu namePattern (muss ein {0} enthalten) der gegebenen Seite sourcePage in
	 * gleichnamige Elemete dieser Seite.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyOptionListValuesFrom(Page sourcePage, String namePattern) throws RQLException {
		List<OptionList> sourceOls = sourcePage.getOptionLists(namePattern);
		for (OptionList sourceOl : sourceOls) {
			copyOptionListValueFrom(sourceOl.getName(), sourcePage);
		}
	}

	/**
	 * Kopiert den Wert der Optionsliste dieser Seite, das auf dem gegebenen templateElement basiert in das targetElement. TODO geht
	 * das so überhaupt?
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyOptionListValueTo(String sourceTemplateElementName, OptionList targetElement) throws RQLException {
		OptionList sourceElem = getOptionList(sourceTemplateElementName);
		if (sourceElem.isValueEntered()) {
			targetElement.select(sourceElem.getCurrentSelection().getValue());
		}
	}

	/**
	 * Kopiert den Wert der Optionsliste dieser Seite, das auf dem gegebenen templateElement basiert in das gleichnamige Element in
	 * targetPage.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyOptionListValueTo(String templateElementName, Page targetPage) throws RQLException {
		copyOptionListValueTo(templateElementName, targetPage.getOptionList(templateElementName));
	}

	/**
	 * Kopiert den Wert des Standardfeld Date Elements der gegebenen Seite sourcePage, das auf dem gegebenen templateElement basiert in
	 * das gleichnamige Element dieser Seite.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyStandardFieldDateValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		sourcePage.copyStandardFieldDateValueTo(templateElementName, this);
	}

	/**
	 * Kopiert den Wert des Standardfeld Date Elements dieser Seite, das auf dem gegebenen templateElement basiert in das gleichnamige
	 * Element in targetPage.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyStandardFieldDateValueTo(String templateElementName, Page targetPage) throws RQLException {
		copyStandardFieldDateValueTo(templateElementName, targetPage.getStandardFieldDateElement(templateElementName));
	}

	/**
	 * Kopiert den Wert des Standardfeld Date Elements dieser Seite, das auf dem gegebenen templateElement basiert in das
	 * targetElement.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyStandardFieldDateValueTo(String sourceTemplateElementName, StandardFieldDateElement targetElement)
			throws RQLException {
		StandardFieldDateElement sourceElem = getStandardFieldDateElement(sourceTemplateElementName);
		if (sourceElem.isValueEntered()) {
			targetElement.setDate(sourceElem.getDate());
		}
	}

	/**
	 * Kopiert den Wert des Standardfeld Numeric Elements der gegebenen Seite sourcePage, das auf dem gegebenen templateElement basiert
	 * in das gleichnamige Element dieser Seite.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyStandardFieldNumericValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		sourcePage.copyStandardFieldNumericValueTo(templateElementName, this);
	}

	/**
	 * Kopiert den Wert des Standardfeld Numeric Elements dieser Seite, das auf dem gegebenen templateElement basiert in das
	 * gleichnamige Element in targetPage.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyStandardFieldNumericValueTo(String templateElementName, Page targetPage) throws RQLException {
		copyStandardFieldNumericValueTo(templateElementName, targetPage.getStandardFieldNumericElement(templateElementName));
	}

	/**
	 * Kopiert den Wert des Standardfeld Numeric Elements dieser Seite, das auf dem gegebenen templateElement basiert in das
	 * targetElement.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyStandardFieldNumericValueTo(String sourceTemplateElementName, StandardFieldNumericElement targetElement)
			throws RQLException {
		StandardFieldNumericElement sourceElem = getStandardFieldNumericElement(sourceTemplateElementName);
		if (sourceElem.isValueEntered()) {
			targetElement.setInt(sourceElem.getInt());
		}
	}

	/**
	 * Kopiert den Wert des Standardfeld Text Elements der gegebenen Seite sourcePage, das auf dem gegebenen templateElement basiert in
	 * das gleichnamige Element dieser Seite.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyStandardFieldTextValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		sourcePage.copyStandardFieldTextValueTo(templateElementName, this);
	}

	/**
	 * Kopiert den Wert des Standardfeld Text Elements dieser Seite, das auf dem gegebenen templateElement basiert in das gleichnamige
	 * Element in targetPage.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyStandardFieldTextValueTo(String templateElementName, Page targetPage) throws RQLException {
		copyStandardFieldTextValueTo(templateElementName, targetPage.getStandardFieldTextElement(templateElementName));
	}

	/**
	 * Kopiert den Wert des Standardfeld Text Elements dieser Seite, das auf dem gegebenen templateElement basiert in das
	 * targetElement.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyStandardFieldTextValueTo(String sourceTemplateElementName, StandardFieldTextElement targetElement)
			throws RQLException {
		StandardFieldTextElement sourceElem = getStandardFieldTextElement(sourceTemplateElementName);
		if (sourceElem.isValueEntered()) {
			targetElement.setText(sourceElem.getText());
		}
	}

	/**
	 * Kopiert den Wert des Standardfeld user defined Elements der gegebenen Seite sourcePage , das auf dem gegebenen templateElement
	 * basiert in das gleichnamige Element dieser Seite.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyStandardFieldUserDefinedValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		sourcePage.copyStandardFieldUserDefinedValueTo(templateElementName, this);
	}

	/**
	 * Kopiert den Wert des Standardfeld user defined Elements dieser Seite, das auf dem gegebenen templateElement basiert in das
	 * gleichnamige Element in targetPage.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyStandardFieldUserDefinedValueTo(String templateElementName, Page targetPage) throws RQLException {
		copyStandardFieldUserDefinedValueTo(templateElementName, targetPage.getStandardFieldUserDefinedElement(templateElementName));
	}

	/**
	 * Kopiert den Wert des Standardfeld user defined Elements dieser Seite, das auf dem gegebenen templateElement basiert in das
	 * targetElement.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyStandardFieldUserDefinedValueTo(String sourceTemplateElementName, StandardFieldUserDefinedElement targetElement)
			throws RQLException {
		StandardFieldUserDefinedElement sourceElem = getStandardFieldUserDefinedElement(sourceTemplateElementName);
		if (sourceElem.isValueEntered()) {
			targetElement.setText(sourceElem.getText());
		}
	}

	/**
	 * Kopiert den gegebenen textValue auf alle Kind-Seiten (nur einfach verlinkte), die ein Text-Element basierend dem
	 * valueTemplateElementName haben.
	 * 
	 * Es wird eine Liste aller Seiten zurückgeliefert, die außer unter dieser Seite noch woanders angelinkt sind. Diese Seiten müssen
	 * manuell gepflegt werden, da der Konflikt nicht automatisch gelöst werden kann.
	 * 
	 * @param valueTemplateElementName
	 *            Name eines TextElementes im Template, in das der textValue geschrieben wird
	 * @param textValue
	 *            Text, der auf alle Kind-Seiten verteilt werden soll
	 * @param pageIndicatorTmpltElemName
	 *            Vorhandensein dieses Elementes selektiert eine Seite, in die die SecuRoles geschrieben werden muss
	 * @param skipChildTmpltElemName
	 *            Bei vorhandensein dieses Elementes in einer Seite, werden keine Kinder dieser Seite untersucht
	 */
	public java.util.List<Page> copyTextToAllChilds(String valueTemplateElementName, String textValue, String pageIndicatorTmpltElemName,
			String skipChildTmpltElemName) throws RQLException {

		// set to this page
		setTextValue(valueTemplateElementName, textValue);

		// copy to all childs, if possible
		java.util.List<Page> multiLinkedPages = new ArrayList<Page>();
		return copyTextToAllChilds(valueTemplateElementName, textValue, pageIndicatorTmpltElemName, skipChildTmpltElemName,
				multiLinkedPages);
	}

	/**
	 * Kopiert den gegebenen textValue auf alle Kind-Seiten (nur einfach verlinkte), die ein Text-Element basierend dem
	 * templateElementName haben.
	 * 
	 * Es wird eine Liste aller Seiten zurückgeliefert, die außer unter dieser Seite noch woanders angelinkt sind. Diese Seiten müssen
	 * manuell gepflegt werden, da der Konflikt nicht automatisch gelöst werden kann.
	 * 
	 * @param valueTemplateElementName
	 *            Name eines TextElementes im Template, in das der textValue geschrieben wird
	 * @param textValue
	 *            Text, der auf alle Kind-Seiten verteilt werden soll
	 * @param pageIndicatorTmpltElemName
	 *            Vorhandensein dieses Elementes selektiert eine Seite, in die die SecuRoles
	 *            <p>
	 *            geschrieben werden muss (=hasSecuContentRoles)
	 * @param skipChildTmpltElemName
	 *            Bei vorhandensein dieses Elementes in einer Seite, werden keine Kinder
	 *            <p>
	 *            dieser Seite untersucht (=hasNoSecuChilds)
	 * @param multiLinkedPages
	 *            Liste aller Seiten, die nicht beschrieben werden konnten
	 */
	private java.util.List<Page> copyTextToAllChilds(String valueTemplateElementName, String textValue, String pageIndicatorTmpltElemName,
			String skipChildTmpltElemName, java.util.List<Page> multiLinkedPages) throws RQLException {

		java.util.List childPages = getChildPages();

		// check for end of recursion
		if (childPages.size() == 0) {
			return multiLinkedPages;
		} else {
			for (int i = 0; i < childPages.size(); i++) {
				Page child = (Page) childPages.get(i);
				// stop investigating this child further; improve performance
				if (child.contains(skipChildTmpltElemName)) {
					continue;
				}
				// only pages tagged
				if (child.contains(pageIndicatorTmpltElemName)) {
					// ignore multilinked pages
					if (child.isMultiLinked()) {
						multiLinkedPages.add(child);
						continue;
					} else {
						// set text to child
						child.setTextValue(valueTemplateElementName, textValue);
					}
				}
				// try on child
				child.copyTextToAllChilds(valueTemplateElementName, textValue, pageIndicatorTmpltElemName, skipChildTmpltElemName,
						multiLinkedPages);
			}
		}
		return multiLinkedPages;
	}

	/**
	 * Kopiert den Wert des Textelements dieser Seite, das auf dem gegebenen templateElement basiert in das targetElement.
	 */
	public void copyTextValue(String sourceTemplateElementName, TextElement targetElement) throws RQLException {
		TextElement sourceElem = getTextElement(sourceTemplateElementName);

		// check for same type
		if (sourceElem.isAsciiText() != targetElement.isAsciiText()) {
			throw new WrongTypeException("Text value could not be copied on page " + getHeadlineAndId() + " element "
					+ sourceTemplateElementName
					+ ", because of not matching type of TextElements. Both needs either ASCII or HTML, but not mixed.");
		}
		// copy
		targetElement.setText(sourceElem.getText());
	}

	/**
	 * Kopiert den Wert des Textelements der gegebenen Seite sourcePage, das auf dem gegebenen templateElement basiert in das
	 * gleichnamige Element dieser Seite.
	 */
	public void copyTextValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		sourcePage.copyTextValueTo(templateElementName, this);
	}

	/**
	 * Kopiert den Wert des Textelements dieser Seite, das auf dem gegebenen templateElement basiert in das gleichnamige Element in
	 * targetPage.
	 */
	public void copyTextValueTo(String templateElementName, Page targetPage) throws RQLException {
		TextElement sourceElem = getTextElement(templateElementName);
		TextElement targetElem = targetPage.getTextElement(templateElementName);

		// check for same type
		if (sourceElem.isAsciiText() != targetElem.isAsciiText()) {
			throw new WrongTypeException("Text value could not be copied on page " + getHeadlineAndId() + " element "
					+ templateElementName
					+ ", because of not matching type of TextElements. Both needs either ASCII or HTML, but not mixed.");
		}
		// copy only if value entered
		if (sourceElem.isValueEntered()) {
			targetElem.setText(sourceElem.getText());
		}
	}

	/**
	 * Erzeugt eine neue Seite des einzig möglichen templates am Container mit dem gegebenen Namen in dieser Seite.
	 * <p>
	 * Die Seite wird am Container unten angehängt.
	 * 
	 * @param containerTemplateElementName
	 *            muss vom Typ 28 (Container) sein.
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 * @throws AmbiguousTemplateException
	 */
	public Page createAndConnectPageAtContainer(String containerTemplateElementName, String headline) throws RQLException {

		return getContainer(containerTemplateElementName).createAndConnectPage(headline);
	}

	/**
	 * Erzeugt eine neue Seite basierend auf template am Container in dieser Seite. Die Seitenüberschrift der Containerseite wird aus
	 * der Überschrift dieser Seite und dem Templatenamen zusammengesetzt. Die Templatevorbelegung wird dabei nicht geprueft!
	 * 
	 * @param containerTemplateElementName
	 *            muss vom Typ 28 (Container) sein.
	 * @param template
	 *            Typ der neu erstellten Seite.
	 */
	public Page createAndConnectPageAtContainer(String containerTemplateElementName, Template template) throws RQLException {

		return getContainer(containerTemplateElementName)
				.createAndConnectPage(template, this.getHeadline() + " " + template.getName());
	}

	/**
	 * Erzeugt eine neue Seite basierend auf template am Container mit dem gegebenen Namen in dieser Seite. Die Seitenüberschrift der
	 * Containerseite wird aus der Überschrift dieser Seite und dem Templatenamen zusammengesetzt. Die Templatevorbelegung wird dabei
	 * nicht geprueft!
	 * 
	 * @param containerTemplateElementName
	 *            muss vom Typ 28 (Container) sein.
	 * @param template
	 *            Typ der neu erstellten Seite.
	 * @param addAtBottom
	 *            true=>seite wird nach unten verschoben, false=>seite wird nicht verschoben und RD default is oben anlegen
	 */
	public Page createAndConnectPageAtContainer(String containerTemplateElementName, Template template, boolean addAtBottom)
			throws RQLException {

		return getContainer(containerTemplateElementName).createAndConnectPage(template,
				this.getHeadline() + " " + template.getName(), addAtBottom);
	}

	/**
	 * Erzeugt eine neue Seite basierend auf template am Container mit dem gegebenen Namen in dieser Seite. Die Templatevorbelegung
	 * wird dabei nicht geprueft!
	 * 
	 * @param containerTemplateElementName
	 *            muss vom Typ 28 (Container) sein.
	 * @param template
	 *            Typ der neu erstellten Seite.
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 */
	public Page createAndConnectPageAtContainer(String containerTemplateElementName, Template template, String headline)
			throws RQLException {

		return getContainer(containerTemplateElementName).createAndConnectPage(template, headline);
	}

	/**
	 * Erzeugt eine neue Seite basierend auf template am Container mit dem gegebenen Namen in dieser Seite. Die Templatevorbelegung
	 * wird dabei nicht geprueft!
	 * 
	 * @param containerTemplateElementName
	 *            muss vom Typ 28 (Container) sein.
	 * @param template
	 *            Typ der neu erstellten Seite.
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 * @param addAtBottom
	 *            true=>seite wird nach unten verschoben, false=>seite wird nicht verschoben und RD default is oben anlegen
	 */
	public Page createAndConnectPageAtContainer(String containerTemplateElementName, Template template, String headline,
			boolean addAtBottom) throws RQLException {

		return getContainer(containerTemplateElementName).createAndConnectPage(template, headline, addAtBottom);
	}

	/**
	 * Erzeugt eine neue Seite des einzig möglichen templates an der Liste mit dem gegebenen Namen in dieser Seite.
	 * 
	 * @param listTemplateElementName
	 *            TemplateElement muss vom Typ 13 (Liste) sein.
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 * @param addAtBottom
	 *            true=>seite wird nach unten verschoben, false=>seite wird nicht verschoben und RD default is oben anlegen
	 * @throws AmbiguousTemplateException
	 */
	public Page createAndConnectPageAtList(String listTemplateElementName, String headline, boolean addAtBottom) throws RQLException {

		return getList(listTemplateElementName).createAndConnectPage(headline, addAtBottom);
	}

	/**
	 * Erzeugt eine neue Seite basierend auf template an der Liste mit dem gegebenen Namen in dieser Seite. Die Templatevorbelegung
	 * wird dabei nicht geprueft!
	 * 
	 * @param listTemplateElementName
	 *            TemplateElement muss vom Typ 13 (Liste) sein.
	 * @param template
	 *            Typ der neu erstellten Seite.
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 * @param addAtBottom
	 *            true=>seite wird nach unten verschoben, false=>seite wird nicht verschoben und RD default is oben anlegen
	 */
	public Page createAndConnectPageAtList(String listTemplateElementName, Template template, String headline, boolean addAtBottom)
			throws RQLException {

		return getList(listTemplateElementName).createAndConnectPage(template, headline, addAtBottom);
	}
	
	
	
	/**
	 * Erzeugt eine neue Seit und hängt sie an einen TextAnchor an.
	 * 
	 * @param textAnchorTemplateElementName name des anchor elements
	 * @param template fuer die neu anzulegende seite
	 * @param headline der neuen seite.
	 */
	public Page createAndConnectPageAtAnchor(String textAnchorTemplateElementName, Template template, String headline)
			throws RQLException {
		return getTextAnchor(textAnchorTemplateElementName).createAndConnectPage(template, headline);
	}

	
	/**
	 * Compat: Someone made a typo once.
	 */
	public Page createAndConnextPageAtAnchor(String textAnchorTemplateElementName, Template template, String headline)
			throws RQLException {
		return createAndConnectPageAtAnchor(textAnchorTemplateElementName, template, headline);
	}
	

	/**
	 * Löscht diese Seite. Dieses Page Objekt darf danach nicht mehr benutzt werden.
	 * 
	 * @throws DeletionReferencesToThisPageException
	 *             wird geworfen, falls Referenzen auf diese Seite zeigen.
	 * @throws MissingRightException
	 */
	public void delete() throws RQLException {
		delete(false);
	}

	/**
	 * Löscht diese Seite. Dieses Page Objekt darf danach nicht mehr benutzt werden.
	 * 
	 * @param ignoreReferences
	 *            =true, delete the page even if references from other pages/elements points to this page/elements =false, page will
	 *            not deleted and an exception is thrown, if such references exists
	 * @throws DeletionReferencesToThisPageException
	 * @throws MissingRightException
	 */
	public void delete(boolean ignoreReferences) throws RQLException {
		/* 
		 V5 request (no deletion, if references exists)
		 <IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
		 <PAGE action="delete" guid="[!guid_page!]"/>
		 ...
		 </IODATA> 
		 V5 request (force deletion, even if references exists)
		 <IODATA sessionkey="7619097620FgQWqUd14X">
		 <PAGE action="delete" forcedelete2910="1" forcedelete2911="1" guid="65A01DDCD6744CF3B0D94467032107CB">
		 </PAGE>
		 </IODATA>
		 
		 V5 response 
		 <IODATA>ok
		 </IODATA>
		 */

		// call CMS
		try {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ " <PAGE action='delete' guid='" + getPageGuid()
					+ (ignoreReferences ? "' forcedelete2910='1' forcedelete2911='1" : "") + "'/>" + "</IODATA>";
			callCms(rqlRequest);
		} catch (RQLException rqle) {
			// signal existing references to this page
			if (rqle.getMessage().indexOf("#RDError2910") > 0 || rqle.getMessage().indexOf("#RDError2911") > 0) {
				throw new DeletionReferencesToThisPageException(
						"The page "
								+ getHeadlineAndId()
								+ "could not be deleted, because some references to this page (or it elements) exists. Remove this references or force deletion.");
			}
			// signal missing right
			if (rqle.getMessage().indexOf("#RDError15805") > 0) {
				throw new MissingRightException("The page " + getHeadlineAndId()
						+ "could not be deleted, because you did not have the rights to delete this page.");
			}
			// signal any other problem
			throw rqle;
		}
        invalidatePage();
    }

    private void invalidatePage() {
        // make this object "invalid"
        // state pattern?
        project = null;
        pageGuid = null;
        template = null;
        pageId = null;
        headline = null;
        detailsNode = null;
        elementsNodeList = null;
        linksNodeList = null;
    }


    /**
     * Löscht eine Seite für eine angegebene Sprachvariante, auch wenn es noch weitere Referenzen auf diese Seite gibt.
     *
     * @param languageVariant
     * @throws RQLException
     */
    public void deleteForLanguageVariant(LanguageVariant languageVariant) throws RQLException {

        if(languageVariant == null){
            throw new IllegalArgumentException("lanuageVariant must not be null");
        }

        StringBuilder rqlRequest = new StringBuilder("");
        rqlRequest.append("<IODATA loginguid=\"").append(getLogonGuid()).append("\" sessionkey=\"").append(getSessionKey()).append("\">")
          .append("<PAGE action=\"delete\" guid=\"" + getPageGuid() + "\" forcedelete2910=\"1\" forcedelete2911=\"1\"").append(" languagevariantid=\"").append(languageVariant.getLanguageCode()).append("\">")
          .append("<LANGUAGEVARIANTS elementguid=\"").append(getPageGuid()).append("\">")
          .append("<LANGUAGEVARIANT language=\"").append(languageVariant.getLanguageCode()).append("\"/>")
          .append("</LANGUAGEVARIANTS></PAGE></IODATA>");

        getCmsClient().callCmsWithoutParsing(rqlRequest.toString());
    }

	/**
	 * Löscht den cache für den detailsNode. Bei erneuten Zugriff wird der aktuelle Wert wieder vom CMS Server gelesen.
	 */
	private void deleteDetailsNodeCache() {

		detailsNode = null;
	}

	/**
	 * Löscht den cache für die elementsNodeList. Bei erneuten Zugriff wird der aktuelle Wert wieder vom CMS Server gelesen.
	 */
	private void deleteElementsNodeListCache() {

		elementsNodeList = null;
	}

	/**
	 * Löscht diese Seite für alle Sprachvarianten aus dem Papierkorb. Dieses Page Objekt darf danach nicht mehr benutzt werden.
	 * Achtung: Diese Seite kann nicht wieder hergestellt werden. Vielleicht noch aus alten Versionen?
	 */
	public void deleteFromRecycleBin() throws RQLException {
		deleteFromRecycleBin(getProject().getAllLanguageVariants());
	}

	/**
	 * Löscht diese Seite in der augenblicklichen Sprachvariante aus dem Papierkorb. Dieses Page Objekt darf danach nicht mehr benutzt
	 * werden. Achtung: Diese Seite kann nicht wieder hergestellt werden. Vielleicht noch aus alten Versionen?
	 */
	public void deleteFromRecycleBinInCurrentLanguageVariant() throws RQLException {
		deleteFromRecycleBin(Arrays.asList(getProject().getCurrentLanguageVariant()));
	}

	/**
	 * Löscht diese Seite für die gegebenen Sprachvarianten aus dem Papierkorb. Dieses Page Objekt darf danach nicht mehr benutzt
	 * werden. Achtung: Diese Seite kann nicht wieder hergestellt werden. Vielleicht noch aus alten Versionen?
	 */
	public void deleteFromRecycleBin(java.util.List<LanguageVariant> languageVariants) throws RQLException {
		/* 
		 V7.5 request
		<IODATA loginguid="AD630F1DA7E44ABD92CB05F198424D6D" sessionkey="2FDBC3D5EC33448C87B93CED894C2CDD">
		  <PAGE action="deletefinally" guid="2909318174A14779ADD5C7EB91D97F07" languages="ENG,CHS,DEU,"/>
		</IODATA>
		 V7.5 response 
		 <IODATA>
		 </IODATA>
		 */

		// convert lvs
		String lvCodes = "";
		for (LanguageVariant languageVariant : languageVariants) {
			lvCodes += languageVariant.getLanguageCode() + ",";
		}

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <PAGE action='deletefinally' guid='" + getPageGuid() + "' languages='" + lvCodes + "'/></IODATA>";
		callCmsWithoutParsing(rqlRequest);

		// make this object "invalid"
		// state pattern?
        invalidatePage();
	}

	/**
	 * Löscht die Referenz des Seitenelements, das auf dem gegebenen templateElementName basiert.
	 * <p>
	 * Ist keine Referenz gesetzt, wird der Befehl ignoriert.
	 * <p>
	 * Liefert true, falls die Referenz entfernt wurde, sonst false.
	 * 
	 * @param templateElementName
	 *            das TemplateElement muss vom Typ = 2 sein
	 */
	public boolean deleteImageElementReference(String templateElementName) throws RQLException {
		TemplateElement templateElement = getTemplateElementByName(templateElementName);
		// check type of template element
		if (!templateElement.isImage()) {
			throw new WrongTypeException("Template element " + templateElement.getName() + " is not of type Image.");
		}

		// call CMS
		RQLNode elementNode = findElementNode(templateElement);
		String refElemGuid = elementNode.getAttribute("refelementguid");

		// delete reference if existing
		if (refElemGuid != null && refElemGuid != "") {
			getProject().deleteElementReference(elementNode.getAttribute("guid"), refElemGuid);
			return true;
		}
		// reference not removed
		return false;
	}

	/**
	 * Löscht den Wert des ImageElementes dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            das TemplateElement muss vom Typ = 2 sein
	 */
	public void deleteImageValue(String templateElementName) throws RQLException {

		getImageElement(templateElementName).deleteValue();
	}

	/**
	 * Löscht den Wert des MediaElementes dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            das TemplateElement muss vom Typ = 38 sein
	 */
	public void deleteMediaValue(String templateElementName) throws RQLException {

		getMediaElement(templateElementName).deleteValue();
	}

	/**
	 * Löscht den Wert der OptionsListe dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            Name der OptionList im Template
	 */
	public void deleteOptionListValue(String templateElementName) throws RQLException {

		getOptionList(templateElementName).deleteValue();
	}

	/**
	 * Löscht den Seitencache dieser Seite für die gegebenen Sprachvarianten.
	 */
	public void deletePageCache(java.util.List<LanguageVariant> languageVariants) throws RQLException {

		/*
		V9 request
		<IODATA loginguid="D0EF5D1C8FCB41CDAE1E6CF03C971A5D" sessionkey="229026613C9D428A850F4DE6864647B1">
		  <PAGEBUILDER languagevariantid="ENG">
		    <PAGES action="pagevaluesetdirty">
		      <PAGE guid="4C5907C29BFB4616B12D39B941BE8323"/>
		    </PAGES>
		  </PAGEBUILDER>
		</IODATA> 
		V9 response
		nothing!
		 */

		for (LanguageVariant languageVariant : languageVariants) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ "<PAGEBUILDER languagevariantid='" + languageVariant.getLanguageCode() + "'><PAGES action='pagevaluesetdirty'>"
					+ "<PAGE guid='" + getPageGuid() + "'/>" + "</PAGES></PAGEBUILDER></IODATA>";
			callCmsWithoutParsing(rqlRequest);
		}
	}

	/**
	 * Löscht den Seitencache dieser Seite für die gegebenen Sprachvarianten GUIDs.
	 */
	public void deletePageCache(String[] languageVariantGuids) throws RQLException {
		// collect language variants
		java.util.List<LanguageVariant> lvs = new ArrayList<LanguageVariant>();
		for (int i = 0; i < languageVariantGuids.length; i++) {
			String lvGuid = languageVariantGuids[i];
			lvs.add(project.getLanguageVariantByGuid(lvGuid));
		}
		// del page cache
		deletePageCache(lvs);
	}

	/**
	 * Löscht den Seitencache dieser Seite allen Sprachvarianten des aktuellen Projekts.
	 */
	public void deletePageCacheinAllLanguageVariant() throws RQLException {
		deletePageCache(getProject().getAllLanguageVariants());
	}

	/**
	 * Löscht den Seitencache dieser Seite in der aktuellen Sprachvariante.
	 */
	public void deletePageCacheinCurrentLanguageVariant() throws RQLException {
		deletePageCache(Arrays.asList(getProject().getCurrentLanguageVariant()));
	}

	/**
	 * Löscht den Wert des Standardfeld Date Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 5 sein
	 */
	public void deleteStandardFieldDateValue(String templateElementName) throws RQLException {

		getStandardFieldDateElement(templateElementName).deleteValue();
	}

	/**
	 * Löscht den Wert des Standardfeld Numeric Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 48 sein
	 */
	public void deleteStandardFieldNumericValue(String templateElementName) throws RQLException {

		getStandardFieldNumericElement(templateElementName).deleteValue();
	}

	/**
	 * Löscht den Wert des Standardfeld Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 1 sein
	 */
	public void deleteStandardFieldTextValue(String templateElementName) throws RQLException {

		getStandardFieldTextElement(templateElementName).deleteValue();
	}

	/**
	 * Löscht den Wert des Standardfeld user defined elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 999 sein
	 */
	public void deleteStandardFieldUserDefinedValue(String templateElementName) throws RQLException {

		getStandardFieldUserDefinedElement(templateElementName).deleteValue();
	}

	/**
	 * Löscht den Wert des Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 31 sein
	 */
	public void deleteTextValue(String templateElementName) throws RQLException {

		getTextElement(templateElementName).deleteValue();
	}

	/**
	 * Löscht diese Seite mit allen Kindseiten des gegebenen Containers. Dieses Page Objekt darf danach nicht mehr benutzt werden.
	 */
	public void deleteWithContainerChilds(String containerTemplateElementName, boolean ignoreReferences) throws RQLException {

		PageArrayList blockPages = getContainerChildPages(containerTemplateElementName);
		for (int j = 0; j < blockPages.size(); j++) {
			Page blockPg = (Page) blockPages.get(j);
			blockPg.delete(ignoreReferences);
		}
		delete(ignoreReferences);
	}

	/**
	 * Entfernt das gegebene Berechtigungspaket von dieser Seite.
	 */
	public void disconnectAuthorizationPackage(AuthorizationPackage authorizationPackage) throws RQLException {
		/* 
		 V7.5 request
		 <IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
		 <AUTHORIZATION>
		 <ELEMENT guid="[!guid_element!]">
		 <AUTHORIZATIONPACKET action="unlink" guid="[!guid_authorization!]"/>
		 </ELEMENT>
		 </AUTHORIZATION>
		 </IODATA>
		 V7.5 response
		 <IODATA>
		 </IODATA>
		 */

		// check type
		if (!authorizationPackage.isPageAuthorizationPackage()) {
			throw new WrongTypeException("Authorization package with name " + authorizationPackage.getName()
					+ " has wrong type and cannot be disconnected from page " + getHeadlineAndId() + ".");
		}

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<AUTHORIZATION>"
				+ " <ELEMENT guid='" + getPageGuid() + "'>" + "  <AUTHORIZATIONPACKET action='unlink' guid='"
				+ authorizationPackage.getAuthorizationPackageGuid() + "'/>" + " </ELEMENT></AUTHORIZATION></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Hängt diese Seite von allen MultiLinks ab, an denen diese Seite gelinkt ist. Diese Seite ist danach unverlinkt!
	 * 
	 * @return Anzahl der MultiLinks, an denen diese Seite gelinkt war.
	 */
	public int disconnectFromAllMultiLinks() throws RQLException {
		return disconnectFromMultiLinks(getMultiLinksToThisPage());
	}

	/**
	 * Hängt diese Seite von allen MultiLinks außer dem gegebenen ab, an denen diese Seite gelinkt ist.
	 * <p>
	 * Danach ist diese Seite nur noch an dem gegebenen gelinkt.
	 * 
	 * @return Anzahl der MultiLinks, von denen diese Seite abgelinkt wurde
	 */
	public int disconnectFromAllMultiLinks(MultiLink linkToKeep) throws RQLException {
		List<MultiLink> linksToDisconnect = getMultiLinksToThisPage();
		linksToDisconnect.remove(linkToKeep);
		return disconnectFromMultiLinks(linksToDisconnect);
	}

	/**
	 * Hängt diese Seite von allen gegebenen MultiLinks ab.
	 * 
	 * @return Anzahl der MultiLinks, an denen diese Seite gelinkt war.
	 */
	private int disconnectFromMultiLinks(java.util.List<MultiLink> links) throws RQLException {
		int result = links.size();

		// disconnect from given links
		for (MultiLink multiLink : links) {
			multiLink.disconnectChild(this);
		}
		return result;
	}

	/**
	 * Ändert den Status dieser Seite im Übersetzungsworkflow; macht keine Änderungen an dieser Seite, sondern nimmt sie nur aus den zu
	 * übersetzenden Seiten. Löst die Funktion 'Do not translate' im Translation Editor für eine Übersetzung von sourceLanguageVariant
	 * nach targetLanguageVariant aus. TODO mehrere Seiten mit einem Step bearbeiten scheint möglich!
	 */
	public void doNotTranslate(LanguageVariant sourceLanguageVariant, LanguageVariant targetLanguageVariant) throws RQLException {

		/* 
		 V9 request
		<IODATA loginguid="369527148B5847AD8184EF3656412E13" sessionkey="4E033CC0E293497999BA814EFBF3E3D7">
		<WORKFLOW>
		<PAGES action="donottranslate" sourcelanguageid="ENG" targetlanguageid="CHS">
		<PAGE guid="2B3BB28A1BCE4FC9AAFEFB28FF1F03C0"/>
		</PAGES>
		</WORKFLOW>
		</IODATA>
		 V9 response
		<IODATA>
		</IODATA>
		 */

		// check prerequisite state
		if (!isInStateWaitingToBeTranslated()) {
			throw new WrongStateException("Page " + getHeadlineAndId() + " is in unexpected state '" + getStateInfo()
					+ "'. To do not translate a page in translation workflow it has to be in state 'waiting to be translated'.");
		}

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'><WORKFLOW>"
				+ " <PAGES action='donottranslate' sourcelanguageid='" + sourceLanguageVariant.getLanguageCode()
				+ "' targetlanguageid='" + targetLanguageVariant.getLanguageCode() + "'>" + " <PAGE guid='" + getPageGuid() + "' />"
				+ "</PAGES></WORKFLOW></IODATA>";
		callCmsWithoutParsing(rqlRequest);

		// force new read of details node
		deleteDetailsNodeCache();
	}

	/**
	 * Ändert den Status dieser Seite im Übersetzungsworkflow; macht keine Änderungen an dieser Seite, sondern nimmt sie nur aus den zu
	 * übersetzenden Seiten. Löst die Funktion 'Do not translate' im Translation Editor für die Kombination Hauptsprachvariante nach
	 * targetLanguageVariant aus.
	 */
	public void doNotTranslateFromMainTo(LanguageVariant targetLanguageVariant) throws RQLException {
		doNotTranslate(getProject().getMainLanguageVariant(), targetLanguageVariant);
	}

	/**
	 * Ändert den Status dieser Seite im Übersetzungsworkflow; macht keine Änderungen an dieser Seite, sondern nimmt sie nur aus den zu
	 * übersetzenden Seiten. Löst die Funktion 'Do not translate' im Translation Editor für die Kombination Hauptsprachvariante nach
	 * aktueller Sprachvariante aus.
	 */
	public void doNotTranslateFromMainToCurrent() throws RQLException {
		doNotTranslate(getProject().getMainLanguageVariant(), getProject().getCurrentLanguageVariant());
	}

	/**
	 * Lädt das Bild in die Datei targetFile aus dem RD ImageCache.
	 * <p>
	 * Achtung: Die Extender sollten zueinander passen! Sie werden hierbei nicht geprüft.
	 * 
	 * @param runsOnServer
	 *            =true, if used from webapp or batch on CMS server; the domain name will be replace with localhost
	 *            <p>
	 *            =false, if used from any other client; the configured URL will be used unchanged
	 */
	public void downloadImage(String templateElementName, java.io.File targetFile, boolean runsOnServer) throws RQLException {

		getImageElement(templateElementName).downloadToFile(targetFile, runsOnServer);
	}

	/**
	 * Lädt das Bild in eine Datei in dem gegebenen targetPathName aus dem RD ImageCache.
	 * <p>
	 * Achtung: Die Extender sollten zueinander passen! Sie werden hierbei nicht geprüft.
	 * 
	 * @param runsOnServer
	 *            =true, if used from webapp or batch on CMS server; the domain name will be replace with localhost
	 *            <p>
	 *            =false, if used from any other client; the configured URL will be used unchanged
	 */
	public void downloadImage(String templateElementName, String targetPathName, boolean runsOnServer) throws RQLException {

		getImageElement(templateElementName).downloadToFile(targetPathName, runsOnServer);
	}

	/**
	 * Lädt das Asset in die Datei targetFile aus dem RD ImageCache.
	 * <p>
	 * Achtung: Die Extender sollten zueinander passen! Sie werden hierbei nicht geprüft.
	 * 
	 * @param runsOnServer
	 *            =true, if used from webapp or batch on CMS server; the domain name will be replace with localhost
	 *            <p>
	 *            =false, if used from any other client; the configured URL will be used unchanged
	 */
	public void downloadMedia(String templateElementName, java.io.File targetFile, boolean runsOnServer) throws RQLException {
		getImageElement(templateElementName).downloadToFile(targetFile, runsOnServer);
	}

	/**
	 * Lädt das Asset in eine Datei in dem gegebenen targetPathName aus dem RD ImageCache.
	 * <p>
	 * Achtung: Die Extender sollten zueinander passen! Sie werden hierbei nicht geprüft.
	 * 
	 * @param runsOnServer
	 *            =true, if used from webapp or batch on CMS server; the domain name will be replace with localhost
	 *            <p>
	 *            =false, if used from any other client; the configured URL will be used unchanged
	 */
	public void downloadMedia(String templateElementName, String targetPathName, boolean runsOnServer) throws RQLException {
		getMediaElement(templateElementName).downloadToFile(targetPathName, runsOnServer);
	}

	/**
	 * Ends the mode to delete element values. This methods really delete the element values.
	 * 
	 * @see #startDeleteElementValues()
	 * @see Element#deleteValue()
	 * @see TextElement#deleteValue()
	 * @see #addDeleteElementValue(Element)
	 */
	public void endDeleteElementValues() throws RQLException {
		if (deleteElementValuesRequest == null) {
			throw new RQLException(
					"You tried to delete element values with one request, but you have to use the method #startDeleteElementValues() first.");
		}
		int cmdLen = deleteElementValuesRequest.length();
		
		// no actual delete-requests were queued, do nothing
		if (cmdLen == 0) {
			deleteElementValuesRequest = null;
			return;
		}
		
		StringBuilder cmd = new StringBuilder(128 + cmdLen);
		cmd.append("<IODATA loginguid='").append(getLogonGuid()).append("' sessionkey='").append(getSessionKey()).append("'>");
		cmd.append(deleteElementValuesRequest);
		cmd.append("</IODATA>");
		callCmsWithoutParsing(cmd.toString());
		deleteElementValuesRequest = null;
	}

	/**
	 * Returns the number of changed elements. Call immediately before end set elements.
	 * <p>
	 * Even it returns 0 call {@link #endSetElementValues()}
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public int endNumberOfSetElements() throws RQLException {
		return setElementValuesMap.size();
	}

	/**
	 * Stops the mode to add elements which values should be changed. Updates the page with the values for all added elements.
	 * <p>
	 * Ändert Inhaltselemente dieser Seite mit nur einem RQL request. Es werden nur die folgenden Elementtypen unterstützt:
	 * <p>
	 * StandardFieldText, StandardFieldNumeric, StandardFieldDate, StandardFieldUserDefined, OptionsList
	 * <p>
	 * 
	 * Folgende Elementtypen werden nicht unterstützt, da für diese spezielle Updatemethoden benutzt werden müssen:
	 * <p>
	 * ImageElement, MediaElement, TextElement
	 * <p>
	 * 
	 * @see #startSetElementValues()
	 */
	public void endSetElementValues() throws RQLException {
		if (setElementValuesMap == null) {
			throw new RQLException(
					"You tried to change element values with one request, but you have to use the method #startSetElementValues() first.");
		}
		// change the page elem values with only one request (if any)
		if (setElementValuesMap.size() > 0)
			setElementValues(setElementValuesMap);
		// prepare for next start
		setElementValuesMap = null;
	}

	/**
	 * Erhält für HTML Elemente alle eingegebenen Zeichen (< wird zu &lt;). Ein einzelnes blank (space) wird als Textwert geschrieben
	 * (zu &nbsp;).
	 */
	public void enterText(String templateElementName, String value) throws RQLException {

		getTextElement(templateElementName).enterText(value);
	}

	/**
	 * Zwei Seitenobjekte werden als identisch interpretiert, falls beide die gleiche GUID haben.
	 * 
	 * @param obj
	 *            the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
	 * @see java.lang.Boolean#hashCode()
	 * @see java.util.Hashtable
	 */
	public boolean equals(Object obj) {
		Page second = (Page) obj;
		return this.getPageGuid().equals(second.getPageGuid());
	}

	/**
	 * Liefert den RQLNode mit dem Element dieser Seite, das auf dem gegebenen templateElement basiert, oder null, falls das Element
	 * nicht in dieser Seite vorhanden ist.
	 * 
	 * @param templateElement
	 *            kann ein Element dieser Seite sein
	 * @return <code>RQLNode</code>
	 * @throws NewElementNotRefreshedException
	 * @throws ElementNotFoundException
	 */
	private RQLNode findElementNode(TemplateElement templateElement) throws RQLException {

		RQLNode result = findElementNodePrimitive(templateElement);

		if (result != null && result.getAttribute("guid").length() != 0) {
			return result;
		}

		// element was not found in page
		// use an internal page preview to update missing text elements in page
		if (getTemplate().contains(templateElement)) {
			simulateSmartEditUsage();
			// try again
			elementsNodeList = null;
			result = findElementNodePrimitive(templateElement);
			if (result != null && result.getAttribute("guid").length() != 0) {
				return result;
			} else {
				throw new NewElementNotRefreshedException(
						"Element named "
								+ templateElement.getName()
								+ " is new in template "
								+ getTemplate().getName()
								+ " but not activated on page "
								+ getHeadlineAndId()
								+ ". Please use it in SmartEdit HTML code or navigate to this page within RedDot CMS. Afterwards try the script again.");
			}
		} else {
			throw new ElementNotFoundException("Element named " + templateElement.getName() + " could not be found in page "
					+ getHeadlineAndId() + " and template.");
		}
	}

	/**
	 * Liefert den RQLNode mit dem Element dieser Seite, das auf dem gegebenen templateElement basiert, oder null, falls das Element
	 * nicht gefunden werden konnte.
	 * 
	 * inklusive der 'hide in project structure=true' felder (die rd aber nicht in der form anzeigt!) aber ohne die 'hide in form=true'
	 * fields
	 * 
	 * @param templateElement
	 *            muss ein Element dieser Seite sein
	 * @return <code>RQLNode</code>
	 */
	private RQLNode findElementNodePrimitive(TemplateElement templateElement) throws RQLException {
		elementsNodeList = getElementNodeList();
		// find element
		RQLNode xmlNode = null;
		String guid = templateElement.getTemplateElementGuid();

		// handle no elementes at all
		if (elementsNodeList == null) {
			return null;
		}

		// search by template elemenet guid
		for (int i = 0; i < elementsNodeList.size(); i++) {
			xmlNode = elementsNodeList.get(i);

			if (xmlNode.getAttribute("templateelementguid").equals(guid)) {
				return xmlNode;
			}
		}
		return null;
	}

	/**
	 * Liefert den RQLNode mit dem Link dieser Seite, der die gegebenen GUID besitzt.
	 * 
	 * @param linkGuid
	 *            muss ein Link dieser Seite sein
	 * @return <code>RQLNode</code>
	 */
	private RQLNode findLinkNode(String linkGuid) throws RQLException {

		// find container
		RQLNodeList elementList = getLinksNodeList();
		RQLNode elementNode = null;

		for (int i = 0; i < elementList.size(); i++) {
			elementNode = elementList.get(i);

			if (elementNode.getAttribute("guid").equals(linkGuid)) {
				return elementNode;
			}
		}
		throw new ElementNotFoundException("Link with GUID " + linkGuid + " could not be found in page " + getHeadlineAndId() + ".");
	}

	/**
	 * Liefert den RQLNode mit dem Link dieser Seite, der auf dem gegebenen templateElement basiert. Falls für den gesuchten Link keine
	 * GUID geliefert wird, wird ein Aufruf im SmartEdit simuliert, um die Seite korrekt zu aktualisieren.
	 * 
	 * @param linkTemplateElement
	 *            muss zum Template dieser Seite gehoeren
	 * @return <code>RQLNode</code>
	 */
	private RQLNode findLinkNode(TemplateElement linkTemplateElement) throws RQLException {

		RQLNode linkNode = findLinkNodePrimitive(linkTemplateElement);

		if (linkNode != null && linkNode.getAttribute("guid").length() != 0) {
			return linkNode;
		}

		// workaround for missing guid for new links in templates
		if (getTemplate().contains(linkTemplateElement)) {
			simulateSmartEditUsage();
			// force re-read from cms
			linksNodeList = null;
			// and try again
			linkNode = findLinkNodePrimitive(linkTemplateElement);
			if (linkNode != null && linkNode.getAttribute("guid").length() != 0) {
				return linkNode;
			} else {
				throw new NewElementNotRefreshedException(
						"Link element named "
								+ linkTemplateElement.getName()
								+ " is new in template "
								+ getTemplate().getName()
								+ " but not activated on page "
								+ getHeadlineAndId()
								+ ". Please use it in SmartEdit HTML code or navigate to this page within RedDot CMS. Afterwards try the script again.");
			}
		} else {
			throw new ElementNotFoundException("Link element named " + linkTemplateElement.getName() + " could not be found in page "
					+ getHeadlineAndId() + " and template.");
		}
	}

	/**
	 * Liefert den RQLNode mit dem Link dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param linkTemplateElement
	 *            muss zum Template dieser Seite gehoeren
	 * @return <code>RQLNode</code>
	 */
	private RQLNode findLinkNodePrimitive(TemplateElement linkTemplateElement) throws RQLException {

		// find container
		RQLNodeList elementList = getLinksNodeList();
		RQLNode elementNode = null;
		String guid = linkTemplateElement.getTemplateElementGuid();

		for (int i = 0; i < elementList.size(); i++) {
			elementNode = elementList.get(i);

			if (elementNode.getAttribute("templateelementguid").equals(guid)) {
				return elementNode;
			}
		}
		return null;
		// throw new ElementNotFoundException("Link named " + linkTemplateElement.getName() + " could not be found in page "
		// + getHeadlineAndId() +
		// ".");
	}

	/**
	 * Gibt den Speicher aller Caches wieder frei für die GC. Dieses Seitenobjekt bleibt voll funktionsfähig! Folgende Zugriffe auf
	 * diese Seite füllen die Caches einfach wieder.
	 * 
	 * @see #clearLanguageVariantDependentCaches()
	 */
	public void freeOccupiedMemory() {

		deleteDetailsNodeCache();
		deleteElementsNodeListCache();
        keywords = null;
		linksNodeList = null;
		template = null;
		publicationPackage = null;
		project.pageCache.remove(pageGuid);
	}

	/**
	 * Liefert das AdministrationElement aus dem Template auf dem diese Seite basiert.
	 */
	private AdministrationElement getAdministrationElementByName(String administrationElementName) throws RQLException {

		return getTemplate().getAdministrationElementByName(administrationElementName);
	}

	/**
	 * Liefert das Berechtigungspaket (vom Typ=normal=page) dieser Seite, niemals das globale. Liefert null, falls diese Seite kein
	 * Berechtigungspaket hat.
	 */
	public AuthorizationPackage getAuthorizationPackage() throws RQLException {
		String name = getAuthorizationPackageNamePrim();
		if (name != null) {
			return getProject().getAuthorizationPackageForPageByName(name);
		}
		return null;
	}

	/**
	 * Liefert den Namen des Berechtigungspaket (vom Typ=normal=page) dieser Seite, niemals das globale.
	 * <p>
	 * Liefert einen leeren String, falls diese Seite kein Berechtigungspaket hat.
	 */
	public String getAuthorizationPackageName() throws RQLException {
		AuthorizationPackage pckg = getAuthorizationPackage();
		if (pckg == null) {
			return "";
		} else {
			return pckg.getName();
		}
	}

	/**
	 * Liefert den Namen des Berechtigungspaketes dieser Seite, niemals das globale. Liefert null, falls diese Seite kein
	 * Berechtigungspaket hat.
	 */
	private String getAuthorizationPackageNamePrim() throws RQLException {

		String name = getExtendedDetails("AUTHORIZATION").getAttribute("standardpagerightname");
		return name.length() == 0 ? null : name;
	}

	/**
	 * Liefert alle Kind-Seiten zurück, die an allen MultiLinks dieser Seite angehängt sind.
	 */
	public PageArrayList getChildPages() throws RQLException {

		// collect from all multilinks, which are not reference sources
		java.util.List multiLinks = getMultiLinks(false);
		PageArrayList childPages = new PageArrayList();
		for (int i = 0; i < multiLinks.size(); i++) {
			MultiLink multiLink = (MultiLink) multiLinks.get(i);
			childPages.addAll(multiLink.getChildPages());
		}
		return childPages;
	}

	/**
	 * Liefert alle Kind-Seiten zurück, die an den MultiLinks dieser Seite angehängt sind, die kein Schattenelement
	 * _workflow_unlinked_flag besitzen. Es werden daher keine Kindseiten geliefert, die eh keine Physical pages haben können, z.B. an
	 * text blöcken, sections, term table blocks.
	 */
	public PageArrayList getChildPagesIgnoreShadowedMultilinks(String shadowElementsNameSuffix) throws RQLException {

		// collect from all multilinks, which are not reference sources
		java.util.List<MultiLink> multiLinks = getMultiLinksWithoutShadowedOnes(shadowElementsNameSuffix, false);
		PageArrayList childPages = new PageArrayList();
		for (int i = 0; i < multiLinks.size(); i++) {
			MultiLink multiLink = multiLinks.get(i);
			childPages.addAll(multiLink.getChildPages());
		}
		return childPages;
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	/**
	 * Liefert die GUID des angemeldeten Users vom CMS zurück.
	 */
	String getConnectedUserGuid() throws RQLException {

		return getDetailsNode().getAttribute("userguid");
	}

	/**
	 * Liefert den Container aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param containerTemplateElementName
	 *            muss vom Typ 28 (Container) sein.
	 * @return <code>Container</code>
	 * @see <code>Container</code>
	 */
	public Container getContainer(String containerTemplateElementName) throws RQLException {

		return getContainer(getTemplateElementByName(containerTemplateElementName));
	}

	/**
	 * Liefert den Container aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param containerTemplateElement  muss vom Typ 28 (Container) sein.
	 * @return <code>Container</code>
	 * @see <code>Container</code>
	 */
	private Container getContainer(TemplateElement containerTemplateElement) throws RQLException {

		// check type of template element
		if (!containerTemplateElement.isContainer()) {
			throw new WrongTypeException("Template element " + containerTemplateElement.getName() + " is not of type container.");
		}

		// call CMS to find the link element of this page
		RQLNode linkNode = findLinkNode(containerTemplateElement);

		return buildContainer(linkNode, containerTemplateElement, this);
	}

	/**
	 * Liefert einen Container dieser Seite für die gegebenen Link GUID.
	 */
	public Container getContainerByGuid(String containerLinkGuid) throws RQLException {

		RQLNode linkNode = findLinkNode(containerLinkGuid);

		return (Container) buildMultiLink(linkNode);
	}

	/**
	 * Liefert die Kindseiten des Containers aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param containerTemplateElementName
	 *            TemplateElement muss vom Typ 28 (Container) sein.
	 * @return <code>PageArrayList</code>
	 * @see <code>PageArrayList</code>
	 */
	public PageArrayList getContainerChildPages(String containerTemplateElementName) throws RQLException {

		return getContainer(getTemplateElementByName(containerTemplateElementName)).getChildPages();
	}

	/**
	 * Liefert die Kindseiten des Containers aus dieser Seite, der auf dem gegebenen templateElement basiert und dem gegebenen Template
	 * entspricht.
	 * 
	 * @param containerTemplateElementName
	 *            TemplateElement muss vom Typ 28 (Container) sein.
	 * @return <code>PageArrayList</code>
	 * @see <code>PageArrayList</code>
	 */
	public PageArrayList getContainerChildPages(String containerTemplateElementName, String childTemplateName) throws RQLException {

		return getContainer(getTemplateElementByName(containerTemplateElementName)).getChildPages().selectAllTemplateNamed(
				childTemplateName);
	}

	
	/**
	 * Liefert eine Liste mit allen Containerelementen dieser Seite. Orientiert sich am Template.
	 */
	public java.util.List<Container> getContainerElements() throws RQLException {
		java.util.List<Container> result = new ArrayList<Container>(32);
		for (TemplateElement e : getTemplate().getContainerTemplateElements()) {
			result.add(getContainer(e));
		}
		return result;
	}

	
	/**
	 * Liefert eine Liste mit allen List-Elementen dieser Seite. Orientiert sich am Template.
	 */
	public java.util.List<com.hlcl.rql.as.List> getListElements() throws RQLException {
		java.util.List<com.hlcl.rql.as.List> result = new ArrayList<com.hlcl.rql.as.List>(32);
		
		for (TemplateElement e : getTemplate().getListTemplateElements()) {
			result.add(getList(e));
		}
		
		return result;
	}
	
	
	/**
	 * Liefert eine Liste mit allen TextAnchor-Elementen dieser Seite. Orientiert sich am Template.
	 */
	public List<TextAnchor> getTextAnchorElements() throws RQLException {
		java.util.List<TextAnchor> result = new ArrayList<TextAnchor>(32);
		for (TemplateElement e : getTemplate().getTextAnchorTemplateElements()) {
			result.add(getTextAnchor(e));
		}
		return result;
	}


	/**
	 * Liefert alle Anchor-, Container- und List-Elemente, die sich im Template finden lassen.
	 * Note: Frame Elements are not supported, yet.
	 * 
	 * {@see #getContainerElements()}
	 * {@see #getListElements()}
	 * {@see #getTextAnchorElements()}
	 */
	public java.util.List<StructureElement> getChildElements() throws RQLException {
		java.util.List<Container> l1 = getContainerElements();
		java.util.List<com.hlcl.rql.as.List> l2 = getListElements();
		java.util.List<TextAnchor> l3 = getTextAnchorElements();
		
		java.util.List<StructureElement> result = new ArrayList<StructureElement>(l1.size() + l2.size() + l3.size());
		result.addAll(l1);
		result.addAll(l2);
		result.addAll(l3);
		return result;
	}


	/**
	 * Returns a list of all content element of this page. Headline elements are not returned.
	 * <p>
	 * Es werden nur die folgende TemplateElement Typen unterstützt: all subclasses of {@link Element} and {@link TextElement}.
	 */
	public java.util.List<ContentElement> getContentElements(boolean includeReferences) throws RQLException {
		return getContentElements(includeReferences, null, null);
	}

	/**
	 * Returns a list of all content element of this page. Headline elements are not returned.
	 * <p>
	 * Es werden nur die folgende TemplateElement Typen unterstützt: all subclasses of {@link Element} and {@link TextElement}.
	 */
	public java.util.List<ContentElement> getContentElements(boolean includeReferences, String ignoreElementNames, String separator)
			throws RQLException {
		java.util.List<ContentElement> result = new ArrayList<ContentElement>();

		// for all template elements do
		java.util.List<TemplateElement> templateElements = getTemplate().getContentElements(false, includeReferences,
				ignoreElementNames, separator);
		for (TemplateElement templateElement : templateElements) {
			// skip all lv independent elements if not in main language variant, because these page elements can't be read by
			// rql
			if (!getProject().isCurrentLanguageVariantMainLanguage() && templateElement.isLanguageVariantIndependent()) {
				continue;
			}
			String templateElementName = templateElement.getName();

			// never copy given elements
			if (ignoreElementNames != null && StringHelper.contains(ignoreElementNames, separator, templateElementName)) {
				continue;
			}

			// special handling depending on type
			if (templateElement.isText()) {
				result.add(getTextElement(templateElement));
			} else if (templateElement.isImage()) {
				result.add(getImageElement(templateElement));
			} else if (templateElement.isMedia()) {
				result.add(getMediaElement(templateElement));
			} else if (templateElement.isOptionList()) {
				result.add(getOptionList(templateElement));
			} else if (templateElement.isStandardFieldEmail()) {
				result.add(getStandardFieldEmailElement(templateElement));
			} else if (templateElement.isStandardFieldUrl()) {
				result.add(getStandardFieldUrlElement(templateElement));
			} else if (templateElement.isStandardFieldDate()) {
				result.add(getStandardFieldDateElement(templateElement));
			} else if (templateElement.isStandardFieldNumeric()) {
				result.add(getStandardFieldNumericElement(templateElement));
			} else if (templateElement.isStandardFieldText()) {
				result.add(getStandardFieldTextElement(templateElement));
			} else if (templateElement.isStandardFieldUserDefined()) {
				result.add(getStandardFieldUserDefinedElement(templateElement));
			} else {
				// type hot handled appropriately
				throw new RQLException("You try to collect an element of the unsupported type " + templateElement.getTypeName()
						+ ". Allowed are only all subclasses of Element and TextElement.");
			}
		}
		return result;
	}
	
	
	/**
	 * Very stupid implementation: Iterates all content elements and find the one with the given name.
	 * 
	 * @param name element name to look for
	 * @return null if not found.
	 */
	public ContentElement getContentElement(String name) throws RQLException {
		for (ContentElement e : getContentElements(true)) {
			if (e.getTemplateElementName().equals(name))
				return e;
		}
		return null;
	}
	
	
	/**
	 * Liefert den User, der diese Seite erstellt hat.
	 * 
	 * @see Page#hasCreatedUser()
	 */
	public User getCreatedByUser() throws RQLException {

		return new User(getCmsClient(), getDetailsCreatedByUserGuid());
	}

	/**
	 * Liefert den Namen des User, der diese Seite erstellt hat. Liefert 'Unknown author', falls der User bereits gelöscht wurde.
	 * <p>
	 * Scheint nicht zu funktionieren, falls die Seite im draft steht oder nur in einer anderen Sprachvariante existiert.
	 * 
	 * @see Page#hasCreatedUser()
	 */
	public String getCreatedByUserName() throws RQLException {
		return getDetailsNode().getAttribute("createusername");
	}

	/**
	 * Liefert den Zeitpunkt der Erstellung dieser Seite.
	 */
	public ReddotDate getCreatedOn() throws RQLException {
		return new ReddotDate(getDetailsNode().getAttribute("createdate"));
	}

	/**
	 * Returns the date (no time), when this page was deleted. Returns null, if page is not deleted.
	 * 
	 * @throws RecycleBinPageNotFoundByHeadlineException
	 *             if the page is in recycle bin, but could not be found via the headline in recycle bin to determine the deleted date
	 */
	public ReddotDate getDeletedOn() throws RQLException {
		return getProject().getRecycleBin().getDeletedDate(this);
	}

	/**
	 * Returns true, if the given date when is after the deletion date of this page.
	 * 
	 * @see ReddotDate#after(java.util.Date)
	 * @throws RecycleBinPageNotFoundByHeadlineException
	 *             if the page is in recycle bin, but could not be found via the headline in recycle bin to determine the deleted date
	 */
	public boolean isDeletedOnAfter(ReddotDate when) throws RQLException {
		return getDeletedOn().after(when);
	}

	/**
	 * Liefert den Zeitpunkt der Erstellung dieser Seite im Format 27 Aug 1967.
	 */
	public String getCreatedOnAsddMMyyyy() throws RQLException {
		return getCreatedOn().getAsddMMyyyy();
	}

	/**
	 * Liefert den Zeitpunkt der Erstellung dieser Seite im Format 20100824.
	 */
	public String getCreatedOnAsyyyyMMdd() throws RQLException {
		return getCreatedOn().getAsyyyyMMdd();
	}

	/**
	 * Liefert das Select statement für den Datenbankzugriff für diese Seite oder null, falls diese Seite kein sql statement besitzt.
	 */
	public DatabaseQuery getDatabaseQuery() throws RQLException {
		if (hasDatabaseQuery()) {
			return new DatabaseQuery(getDatabaseQueryPrim());
		} else {
			return null;
		}
	}

	/**
	 * Liefert das SQL Statement dieser Seite, oder null, falls diese Seite keins hat.
	 */
	private String getDatabaseQueryPrim() throws RQLException {
		/* 
		 V7.5 request
		<IODATA loginguid="9507A4A8186D4171A275722D6E124008" sessionkey="296105C195104B17A037A38444F075D1" >
		<PAGE guid="9620411723354B86AD52D9698EC40A6F" >
		<SQL action="load" />
		</PAGE>
		</IODATA>
		 V7.5 response
		<IODATA>
		<SQL action="load" languagevariantid="DEU" dialoglanguageid="ENG" guid="BF4C79987D5546E18835D8681E9FA548" pageguid="9620411723354B86AD52D9698EC40A6F" 
		value="select main_location, short_name, address_line_1,  address_line_2, address_line_3, address_line_4, address_line_5, address_line_6, address_line_7, address_line_8, address_line_9, address_line_10, address_line_11, address_line_12 from ti0110 where matchcode_name = 'HAPAGL' and matchcode_suppl = 043"
		/>
		</IODATA>
		 */

		// call CMS
		if (databaseQueryCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PAGE guid='"
					+ getPageGuid() + "'>" + "<SQL action='load' />" + "</PAGE></IODATA>";
			databaseQueryCache = callCms(rqlRequest).getNode("SQL").getAttribute("value");
		}
		return databaseQueryCache;
	}

	/**
	 * Liefert die GUID des Users, der diese Seite zuletzt geändert hat.
	 */
	private String getDetailsChangedByUserGuid() throws RQLException {

		return getDetailsNode().getAttribute("changeuserguid");
	}

	/**
	 * Liefert die GUID des Users, der diese Seite erstellt hat.
	 */
	private String getDetailsCreatedByUserGuid() throws RQLException {

		return getDetailsNode().getAttribute("createuserguid");
	}

	/**
	 * Liefert die Ueberschrift dieser Seite vom CMS zurück.
	 */
	private String getDetailsHeadline() throws RQLException {

		return getDetailsNode().getAttribute("headline");
	}

	/**
	 * Liefert den RQLNode mit weiteren Information für diese Seite zurueck.
	 * 
	 */
	private RQLNode getDetailsNode() throws RQLException {

		/* 
		 V5 request (reqeust started from page with id 2281)
		 <IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
		 <PAGE action="load" guid="[!guid_page!]"/>
		 </IODATA> 
		 V5 response 
		 <IODATA>
		 <PAGE action="load" sessionkey="371636884K08x31EcF82" dialoglanguageid="ENG" 
		 languagevariantid="ENG" guid="47026A8A0ECD46AEB5C42B5D23B73D53" id="77800" 
		 templateguid="5C4E160F28C04BB999108028554AA596" templaterights="2147483647" 
		 mainlinkguid="20467D958D7743488A2356B6CC1099D0" 
		 editlinkguid="20467D958D7743488A2356B6CC1099D0" 
		 parentguid="20467D958D7743488A2356B6CC1099D0" headline="after christmas wf test " 
		 name="" hassupplements="1" templatepath="content_templates" templatetitle="content_page" 
		 templateflags="0" headlinedescription="page HEADLINE of content_page" flags="132096" 
		 breadcrumbstartpoint="0" breadcrumbdonotuse="0" releaseguid="" btflags="0" 
		 createdate="38348.5955671296" createuserguid="8898998310DD4513BB8CC1771FFD00BC" 
		 createusername="lejafr2" changedate="38349.4285185185" 
		 changeuserguid="8898998310DD4513BB8CC1771FFD00BC" changeusername="lejafr2" 
		 releasedate="38349.4249652778" releaseusername="lejafr" 
		 releaseuserguid="4324D172EF4342669EAF0AD074433393" lockdate="38348.6125694444" 
		 lastchangesincelocked="38349.4285185185" lockuserguid="8898998310DD4513BB8CC1771FFD00BC" 
		 lockusername="lejafr2" lockuseremail="lejafr@hlcl.com" checkindate="38349.4249652778" 
		 useconnection="1" userguid="4324D172EF4342669EAF0AD074433393"/>
		 </IODATA>
		 */

		// cache the node with page details information
		if (detailsNode == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ " <PAGE action='load' guid='" + getPageGuid() + "'>" + "<ELEMENTS action='load'/></PAGE></IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			// cache page details
			setDetailsNode(rqlResponse.getNode("PAGE"));
			setElementsNodeList(rqlResponse.getNodes("ELEMENT"));
			
			// TBI: URLs are different then proper pages
			RQLNode urlNode = rqlResponse.getNode("URL");
			if (urlNode != null) {
				// <URL guid="E2E8F9B47C0542518111D9366780260B" src="http://www.pinuts.de/"/>
				this.urlNode = urlNode;
			}
			
		}

		return detailsNode;
	}

	/**
	 * Liefert die page id dieser Seite vom CMS zurück.
	 */
	private String getDetailsPageId() throws RQLException {
		return getDetailsNode().getAttribute("id");
	}

	/**
	 * Liefert das Statusflag dieser Seite vom CMS zurück.
	 * 
	 * Für die Übergänge wurden folgende Änderungen analysiert: submit to workflow = -262144 +64 = -2^18 +2^6 = -(als Entwurf
	 * gespeichert) +(wartet auf Freigabe) reject page = -64 +131072 = -2^6 +2^17 = -(wartet auf Freigabe) +(Seite muss korrigiert
	 * werden) confirm = -64 +524288 = -2^6 +2^19 = -(wartet auf Freigabe) +(Seite ist von einem Benutzer bereits freigegeben worden)
	 * Diese Änderung werden aber nicht beim Speichern eines neuen Seitenstatus verwendet!
	 * 
	 * @see #changeState
	 * @throw InvalidGuidException if page has no flags (probably does not exist).
	 */
	private BigInteger getDetailsStateFlag() throws RQLException {

		// force re-read of details node to get current state
		deleteDetailsNodeCache();
		String flags = getDetailsNode().getAttribute("flags");
		if (flags == null)
			throw new InvalidGuidException("No flags, page does not exist in current language.");
		return new BigInteger(flags);
	}

	/**
	 * Liefert die Template GUID des Template auf dem diese Seite basiert.
	 */
	private String getDetailsTemplateGuid() throws RQLException {

		return getDetailsNode().getAttribute("templateguid");
	}

	/**
	 * Liefert eine liste von Linkelementnamen der Seite, die fälschlicherweise doppelt an der Seite existieren.
	 * <p>
	 * Dynamic links will be skip in this investigation!
	 */
	public java.util.Set<String> getDoubleLinkElements() throws RQLException {
		java.util.Set<String> result = new HashSet<String>();

		// for all page elements
		Set<String> check = new HashSet<String>();
		RQLNodeList linkNodes = getLinksNodeList();

		// ignore for no links on page
		if (linkNodes == null) {
			return result;
		}

		for (int i = 0; i < linkNodes.size(); i++) {
			RQLNode linkNode = linkNodes.get(i);
			String name = linkNode.getAttribute("name");

			// skip dynamic links, because they are in the rql result repeate for every added link
			TemplateElement tmpltElem = getTemplate().getTemplateElementByGuid(linkNode.getAttribute("templateelementguid"));
			if (tmpltElem.isDynamicTextAnchor()) {
				continue;
			}
			if (check.contains(name)) {
				// already found, worth to mention
				result.add(name);
			} else {
				// add in check set
				check.add(name);
			}
		}
		return result;
	}

	/**
	 * Liefert den dynamischen Textanker dieser Seite, der auf dem Template-Element mit dem gegebenen Namen basiert.
	 * 
	 * @param dynamicTextAnchorTemplateElementName
	 *            TemplateElement muss vom Typ 26 (Anchor Text) und dynamisch sein.
	 * @return <code>DynamicTextAnchor</code>
	 * @see <code>DynamicTextAnchor</code>
	 */
	public DynamicTextAnchor getDynamicTextAnchor(String dynamicTextAnchorTemplateElementName) throws RQLException {

		return getDynamicTextAnchor(getTemplateElementByName(dynamicTextAnchorTemplateElementName));
	}

	/**
	 * Liefert den dynamischen Textanker aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param dynamicTextAnchorTemplateElement
	 *            muss vom Typ 26 (Anchor Text) und dynamisch sein.
	 * @return <code>DynamicTextAnchor</code>
	 * @see <code>DynamicTextAnchor</code>
	 */
	private DynamicTextAnchor getDynamicTextAnchor(TemplateElement dynamicTextAnchorTemplateElement) throws RQLException {

		// check type of template element
		if (!dynamicTextAnchorTemplateElement.isDynamicTextAnchor()) {
			throw new WrongTypeException("Template element " + dynamicTextAnchorTemplateElement.getName()
					+ " is not a dynamic text anchor. Maybe it is a normal text anchor.");
		}

		// call CMS
		RQLNodeList linkNodes = getLinksNodeList();

		// calculate size of dynamic text anchor
		int size = 0;
		RQLNode linkNode = null;
		String guid = dynamicTextAnchorTemplateElement.getTemplateElementGuid();
		for (int i = 0; i < linkNodes.size(); i++) {
			linkNode = linkNodes.get(i);
			if (linkNode.getAttribute("templateelementguid").equals(guid) && linkNode.getAttribute("elttype").equals("26")) {
				size += 1;
			}
		}

		// wrap list data
		DynamicTextAnchor dynTextAnchor = new DynamicTextAnchor(this, dynamicTextAnchorTemplateElement, size);
		for (int i = 0; i < linkNodes.size(); i++) {
			linkNode = linkNodes.get(i);
			if (linkNode.getAttribute("templateelementguid").equals(guid) && linkNode.getAttribute("elttype").equals("26")) {
				dynTextAnchor.set(Integer.parseInt(linkNode.getAttribute("orderid")), linkNode.getAttribute("value"), linkNode
						.getAttribute("guid"));
			}
		}

		return dynTextAnchor;
	}

	/**
	 * Liefert den RQLNode mit dem Element dieser Seite, das auf dem gegebenen templateElement basiert, oder null, falls das Element
	 * nicht gefunden werden konnte.
	 * <p>
	 * Inklusive der 'hide in project structure=true' felder (die rd aber nicht in der form anzeigt!) aber ohne die 'hide in form=true'
	 * fields.
	 * <p>
	 * Es werden nur Elemente geliefert, die auch in der Sprachvariante geändert werden können. D.h., dass sprachvariantenunabhängige
	 * Elemente werden nur in der Hauptsprachvariante geliefert.
	 * 
	 * @return <code>RQLNode</code>
	 */
	private RQLNodeList getElementNodeList() throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="5AA9CC8EB68E42D8A1C6392D47BA68A3" sessionkey="421138897ay278DE41I6">
		 <PAGE guid="E762DC3918AB4BACA568D80D1B597CA9 " >
		 <ELEMENTS action="load"/>
		 </PAGE>
		 </IODATA>
		 V5 response (leaf list page)
		 <IODATA>
		 <PAGE guid="E762DC3918AB4BACA568D80D1B597CA9 " sessionkey="421138897ay278DE41I6" dialoglanguageid="ENG" languagevariantid="ENG">
		 <ELEMENTS action="load" languagevariantid="ENG" dialoglanguageid="ENG" pageguid="E762DC3918AB4BACA568D80D1B597CA9 " parenttable="PAG">
		 <ELEMENT name="back_link_text" value="" guid="7FB20800F2D0460A8A312F29B091C985" templateelementguid="928F3CE929294E399840866156AABE71" languagevariantid="ENG" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="2097160" eltrequired="0" formularorderid="0" orderid="1" islink="0" eltname="back_link_text" aliasname="back_link_text" variable="back_link_text" type="1" elttype="1" templateelementflags="2097160" templateelementislink="0" />
		 <ELEMENT languagevariantid="ENG" guid="890DCD98C67D45D9B241DC420C3B1F20" templateelementguid="77EC145F35CA4E1D952CAB9081348BC2" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="24" eltrequired="0" formularorderid="0" orderid="1" islink="0" name="img_back" eltname="img_back" aliasname="img_back" variable="img_back" type="2" elttype="2" templateelementflags="24" templateelementislink="0" value="" folderguid="1399FC72ABCB4E839DE8D91E83CCDCFF"/>
		 <ELEMENT guid="E0E33151FB69478C83C3699FB37CDC87" templateelementguid="C4F3C519E72A4B65BF8625319AFF8C71" name="roles" type="8" languagevariantid="ENG" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="0" eltrequired="0" formularorderid="0" orderid="1" islink="0" eltname="roles" aliasname="roles" variable="roles" elttype="8" templateelementflags="0" templateelementislink="0" value="" eltdefaultselectionguid="">
		 <SELECTIONS>
		 <SELECTION guid="1687689F34DC44C891392EE0A27E3938" description="Manager" value="default;manager"/>
		 <SELECTION guid="DBD39FCFDBD74503B0D8BE3CB8723775" description="Agent" value="agent"/>
		 </SELECTIONS>
		 </ELEMENT>
		 <ELEMENT languagevariantid="ENG" guid="7DD6A8DFF4DF44C39FCC4D22C3C532B1" templateelementguid="48DA9994015E487F86283319E21D75F6" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="0" eltrequired="0" formularorderid="0" orderid="1" islink="0" name="table_caption" eltname="table_caption" aliasname="table_caption" variable="table_caption" type="1" elttype="1" templateelementflags="0" templateelementislink="0" value="" reddotdescription="table caption"/>
		 <ELEMENT languagevariantid="ENG" guid="B9BD3A237C414EBBB32F90414A2FBF35" templateelementguid="F0359ECF4AE04B69802A472CA1BC1F11" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="2097160" eltrequired="0" formularorderid="0" orderid="1" islink="0" name="rdSEBorderColor" eltname="rdSEBorderColor" aliasname="rdSEBorderColor" variable="rdSEBorderColor" type="1" elttype="1" templateelementflags="2097160" templateelementislink="0" value=""/>
		 <ELEMENT languagevariantid="ENG" guid="CEDF2DC1E45741C9A0B966552EF59FD0" templateelementguid="51927B70A08844EC8BA97EF973D30177" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="2097152" eltrequired="0" formularorderid="0" orderid="1" islink="0" name="column_created_header" eltname="column_created_header" aliasname="column_created_header" variable="column_created_header" type="1" elttype="1" templateelementflags="2097152" templateelementislink="0" value="" reddotdescription="header of column CREATED"/>
		 <ELEMENT languagevariantid="ENG" guid="9B0455374D0C4A04A11701C2EFAB0AAA" templateelementguid="D51209B06756418A84145F3210A64532" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="0" eltrequired="0" formularorderid="0" orderid="1" islink="0" name="column_created_align" eltname="column_created_align" aliasname="column_created_align" variable="column_created_align" type="8" elttype="8" templateelementflags="0" templateelementislink="0" value="" reddotdescription="alignment of column CREATED" eltdefaultselectionguid="F9620CB74EC0459CBAD3A32580E007E1">
		 <SELECTIONS>
		 <SELECTION guid="5AC21CA6F8564DD1AD111014CE6780DB" description="right" value="right"/>
		 <SELECTION guid="73F03D996217409BB8A248B3612455F6" description="center" value="center"/>
		 <SELECTION guid="F9620CB74EC0459CBAD3A32580E007E1" description="left" value="left"/>
		 </SELECTIONS>
		 </ELEMENT>
		 <ELEMENT name="column_created_width" value="" maxsize="2" languagevariantid="ENG" guid="5A9BECEEAB724573A93A69A3383E481B" templateelementguid="AE1C1B6F97CB4FA18FF78096F221AAA9" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="2097152" eltrequired="0" formularorderid="0" orderid="1" islink="0" eltname="column_created_width" aliasname="column_created_width" variable="column_created_width" type="48" elttype="48" templateelementflags="2097152" templateelementislink="0" reddotdescription="width (%) of column CREATED. Attention! Width of all columns has to be 100%."/>
		 <ELEMENT languagevariantid="ENG" guid="EB7F1DC1C3FF45788EB0FD4C9442B3DF" templateelementguid="F5C42CF315E74B9D8CCAB290AFCE0777" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="2097152" eltrequired="0" formularorderid="0" orderid="1" islink="0" name="column_items_header" eltname="column_items_header" aliasname="column_items_header" variable="column_items_header" type="1" elttype="1" templateelementflags="2097152" templateelementislink="0" value="" reddotdescription="header of column ITEMS"/>
		 <ELEMENT name="column_items_align" value="" eltdefaultselectionguid="2404F55E71374B1AB1C8613664717713" guid="2F109D70CBEB42E0B533579B1607E898" languagevariantid="ENG" templateelementguid="4C176485D3044AB7A886C75E340B31D5" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="0" eltrequired="0" formularorderid="0" orderid="1" islink="0" eltname="column_items_align" aliasname="column_items_align" variable="column_items_align" type="8" elttype="8" templateelementflags="0" templateelementislink="0" reddotdescription="alignment of column ITEMS" >
		 <SELECTIONS>
		 <SELECTION guid="2404F55E71374B1AB1C8613664717713" description="left" value="left"/>
		 <SELECTION guid="86B5F2E0DD0643BA9ECC3E3EABBA6606" description="center" value="center"/>
		 <SELECTION guid="EB3FC864B0634BA8B37AF55D57049C03" description="right" value="right"/>
		 </SELECTIONS>
		 </ELEMENT>
		 <ELEMENT languagevariantid="ENG" maxsize="2" guid="F7031C457CF34E34A09A6FE0F5EA6434" templateelementguid="20666B6EF91C4EE984B227196C53F28F" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="2097152" eltrequired="0" formularorderid="0" orderid="1" islink="0" name="column_items_width" eltname="column_items_width" aliasname="column_items_width" variable="column_items_width" type="48" elttype="48" templateelementflags="2097152" templateelementislink="0" value="" reddotdescription="width (%) of  column ITEMS. Attention! Width of all columns has to be 100%."/>
		 <ELEMENT languagevariantid="ENG" guid="B13C5B760BA543E1BD013DBA8C82C675" templateelementguid="A1745B8215FB4345BE191E79132715F0" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="2097160" eltrequired="0" formularorderid="0" orderid="1" islink="0" name="headline" eltname="headline" aliasname="headline" variable="headline" type="1" elttype="1" templateelementflags="2097160" templateelementislink="0" value=""/>
		 <ELEMENT languagevariantid="ENG" guid="F106145498314901B30D5C463196909F" templateelementguid="7440DD859DC14BE6AF45F07EA3683C9B" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="24" eltrequired="0" formularorderid="0" orderid="1" islink="0" name="img_bottom" eltname="img_bottom" aliasname="img_bottom" variable="img_bottom" type="2" elttype="2" templateelementflags="24" templateelementislink="0" value="" folderguid="1399FC72ABCB4E839DE8D91E83CCDCFF"/>
		 <ELEMENT languagevariantid="ENG" guid="051049DC943D466BBB7EE842F52E972A" templateelementguid="C273B9438CC14D56B24A1822479F555B" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="24" eltrequired="0" formularorderid="0" orderid="1" islink="0" name="img_top" eltname="img_top" aliasname="img_top" variable="img_top" type="2" elttype="2" templateelementflags="24" templateelementislink="0" value="" folderguid="1399FC72ABCB4E839DE8D91E83CCDCFF"/>
		 <ELEMENT languagevariantid="ENG" guid="7E37797CC1DC49A581E8A50035AB7636" templateelementguid="E50FAD416BBB4F1CB625F65496C433B8" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="2097160" eltrequired="0" formularorderid="0" orderid="1" islink="0" name="page_created" eltname="page_created" aliasname="page_created" variable="page_created" type="1" elttype="1" templateelementflags="2097160" templateelementislink="0" value=""/>
		 <ELEMENT name="responsible_id" type="1" maxsize="7" value="" eltrequired="1" guid="CF89C5EFD17B4097AD32E03EE3DDF8C6" templateelementguid="DC4404B501CC4A27B72608A231BBFFDE" languagevariantid="ENG" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="2097664" formularorderid="0" orderid="1" islink="0" eltname="responsible_id" aliasname="responsible_id" variable="responsible_id" elttype="1" templateelementflags="2097664" templateelementislink="0" reddotdescription="MEMO ID of responsible person"/>
		 <ELEMENT languagevariantid="ENG" guid="527ACFAC86824483920F884228863C53" templateelementguid="12FA59828DA74D71867D14A960559921" pageguid="E762DC3918AB4BACA568D80D1B597CA9" eltflags="0" flags="2097664" eltrequired="1" formularorderid="0" orderid="1" islink="0" name="responsible_name" eltname="responsible_name" aliasname="responsible_name" variable="responsible_name" type="1" elttype="1" templateelementflags="2097664" templateelementislink="0" value="" reddotdescription="NAME of responsible person"/>
		 </ELEMENTS>
		 </PAGE>
		 </IODATA>
		 // image element von image_block
		 <ELEMENT name="image" value="news_roundup_logo.gif" folderguid="72904B4D243C4B5EA3E458FE9E019AB9" languagevariantid="ENG" guid="9A872444A0034A428EE1214C0BFC6EEF" templateelementguid="E6E2F70A3BCD41638ACAE9981232530E" pageguid="8035652A9A3C40C697581FB9FEBBFDDA" eltflags="0" flags="512" eltrequired="1" formularorderid="0" orderid="1" islink="0" name="image" eltname="image" aliasname="image" variable="image" type="2" elttype="2" templateelementflags="512" templateelementislink="0" />
		 // block_style option list of image_block page
		 <ELEMENT name="block_style" value="" eltdefaultselectionguid="5BBCE2ACCB134B168956E494964950CC" guid="0AD15ACD8A0041FE984B3BBBE8E26CA9" languagevariantid="ENG" templateelementguid="52601CCBFADF4C7091B99CC47F85E373" pageguid="165CB69E06F64225BB79BBAF1FFFB146" eltflags="0" flags="0" eltrequired="0" formularorderid="0" orderid="1" islink="0" eltname="block_style" aliasname="block_style" variable="block_style" type="8" elttype="8" templateelementflags="0" templateelementislink="0" reddotdescription="bottom space below block?" >
		 <SELECTIONS>
		 <SELECTION guid="5BBCE2ACCB134B168956E494964950CC" description="yes (default)" value="block"/>
		 <SELECTION guid="F8CCABE6C2CB4B279008FF73BC17BB71" description="no" value="blockEmbedded"/>
		 </SELECTIONS>
		 // issue_date von manual_toc_block
		 // 37776 = 04 Jun 2003 
		 <ELEMENT name="issue_date" type="5" value="37776" guid="22A3F6599F5D4BE18D323357DF716607" templateelementguid="A4EAF8FDF35743B5A7A2EFA32C85BCD6" languagevariantid="ENG" pageguid="D176A434D8964C4EA33A76410A2549E4" eltflags="0" flags="2097152" eltrequired="0" formularorderid="0" orderid="1" islink="0" eltname="issue_date" aliasname="issue_date" variable="issue_date" type="5" elttype="5" templateelementflags="2097152" templateelementislink="0" value="37776" reddotdescription="ISSUE DATE of manual"/>
		 // media_file von html_view_gateway_row
		 <ELEMENT name="media_file" value="EH_10.11.03_filelist.xml" suffix="htm;css;xml" folderguid="DB7234C07B6947EE8AACD6E271DDF32C" languagevariantid="ENG" guid="589606D3CFDB47D5A4A0AF77236CA7E1" templateelementguid="8369E7139AEB48038DB8BFF218C3BDB9" pageguid="12B6A68250234FD2A68C54ED040C6A15" eltflags="0" flags="512" eltrequired="1" formularorderid="0" orderid="1" islink="0" eltname="media_file" aliasname="media_file" variable="media_file" type="38" elttype="38" templateelementflags="512" templateelementislink="0"/>
		 // text element mit style
		 <ELEMENT languagevariantid="ENG" eltstylesheetdata="<STYLE>   link styles  a:link     {font-weight:bold; text-decoration:underline; color:#111565; background-color:transparent;} a:visited  {font-weight:bold; text-decoration:underline; color:#6165b5; background-color:transparent;} a:hover    {font-weight:bold; text-decoration:underline; color:#e75200; background-color:transparent;} a:active   {font-weight:bold; text-decoration:underline; color:#e75200; background-color:transparent;}  special text style  p    {margin:0px 0px 10px 0px;}     remove default top margin from paragraphs  font tag would be ignored yet   font  {font-family:Helvetica,Arial,sans-serif; font-size:100% ;}  hr       {color:#111565; background-color:#ffffff; height:1px; margin:10px 0px 10px 0px; padding:0px 0px 0px 0px;}  img      {border:0; margin:0px 3px 0px 3px;}  </STYLE>" editoroptions="7681020" guid="1E5FA657C0B846659B8CD4946682B52B" templateelementguid="3CE7A5BF2156435180DD687943CCFC8D" pageguid="761434E3EA494658B52822688DC2AB42" eltflags="0" flags="2097152" eltrequired="0" formularorderid="0" orderid="1" islink="0" name="column_1_text" eltname="column_1_text" aliasname="column_1_text" variable="column_1_text" type="32" elttype="32" templateelementflags="2097152" templateelementislink="0" value="" reddotdescription="data of column 1"/>
		 
		 */
		if (elementsNodeList == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PAGE guid='"
					+ getPageGuid() + "' action='load'>" + "<ELEMENTS action='load'/>" + " </PAGE>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);

			// cache page details
			setElementsNodeList(rqlResponse.getNodes("ELEMENT"));
			setDetailsNode(rqlResponse.getNode("PAGE"));
		}
		return elementsNodeList;
	}

	/**
	 * Liefert den RQLNode für das gegebene Tag der extended Info für diese Seite zurueck.
	 * 
	 * @param tagName
	 *            Tagname der Antwort, für die der RQLNode geliefert wird
	 */
	private RQLNode getExtendedDetails(String tagName) throws RQLException {

		/* 
		 V5 request (reqeust started from page with id 2281)
		 <IODATA loginguid="[!guid_login!]" sessionkey="[!key!]">
		 <PAGE action="load" guid="[!guid_page!]" option="extendedinfo"/>
		 </IODATA>  
		 V5 response 
		 <IODATA>
		 <PAGE action="load" option="extendedinfo" sessionkey="1021834323f26hNvlNvcN" dialoglanguageid="ENG" languagevariantid="ENG" guid="92BC2B92745148E5A1049A6EED0DD82A" id="44071" templateguid="1951E69D65264B4EB73539D96AE35349" templaterights="2147483647" mainlinkguid="373159EBEDB8465EA953707B6C03BA50" editlinkguid="373159EBEDB8465EA953707B6C03BA50" parentguid="373159EBEDB8465EA953707B6C03BA50" headline="W4C pages" hassupplements="1" templatepath="content_templates" templatetitle="leaf_list_page" templateflags="536870912" headlinedescription="page HEADLINE of leaf_list_page" flags="268960768" breadcrumbstartpoint="0" breadcrumbdonotuse="0" releaseguid="" btflags="0" createdate="38203.5955555556" createuserguid="4324D172EF4342669EAF0AD074433393" createusername="lejafr" changedate="38630.6565046296" changeuserguid="3291FBDE296847848C9E2731F785CD58" changeusername="grapean" releasedate="38630.6589351852" releaseusername="grapean" releaseuserguid="3291FBDE296847848C9E2731F785CD58" lockdate="38687.7042824074" lastchangesincelocked="38687.7042824074" lockuserguid="4324D172EF4342669EAF0AD074433393" lockusername="lejafr" lockuseremail="lejafr@hlcl.com" checkindate="38630.6589351852" drafttemplates="0" templateswaitforrelease="0" templatelockuserguid="" templatelockdate="" templatelockusername="" templatelock="0" previousreleasedversionexists="1" useconnection="1" userguid="4324D172EF4342669EAF0AD074433393">
		 <ELEMENTS type="obligatory">
		 <ELEMENT guid="11927646B0A44712A67D5AF93B457913" name="responsible_id" type="1" filled="1"/>
		 <ELEMENT guid="665CDD2250AD48C88EDACFCCC3DCDE80" name="page_updated" type="5" filled="1"/>
		 <ELEMENT guid="A5764310B3DD49969A7264DD08E13483" name="responsible_name" type="1" filled="1"/>
		 </ELEMENTS>
		 <AUTHORIZATION  standardpagerightsassigned="0" standardpagerightname="work_area_rigths_information_technology" detailpagerightsassigned="0" detailpagerightname=""/>
		 <LANGUAGEVARIANTS>
		 <LANGUAGEVARIANT guid="E6FC9644A75945729B018F98C6299D50" name="English" language="ENG" codetable="" charsetlabel="Server (Server)">
		 <WORKFLOW guid="82208942EB9F48EEA58854222EAFE2AA" name="wf_old_layout_check" statustext="Released" lastreleasedby="grapean" lastreleasedat="38630.6589351852"/>
		 <RELEASEUSERS>
		 <USER guid="3291FBDE296847848C9E2731F785CD58" name="grapean" date="38630.6589351852"/>
		 </RELEASEUSERS>
		 <EXPORTSETTINGS>
		 <PROJECTVARIANTS>
		 <PROJECTVARIANT guid="8491B46045904DA6A7F1836B06271436" name="Display_do_not_use_for_publishing" exportname="khh30001-hipw">
		 <EXPORTFOLDERS>
		 <EXPORTFOLDER folderguid="00000000000000000000000000000100" foldername="Published pages" folderpath=""/>
		 ...
		 </EXPORTFOLDERS>
		 </PROJECTVARIANT>
		 ...
		 </PROJECTVARIANTS>
		 </EXPORTSETTINGS>
		 </LANGUAGEVARIANT>
		 </LANGUAGEVARIANTS>
		 <MAINLINK guid="373159EBEDB8465EA953707B6C03BA50" name="pseudo_list" pageguid="AE60A6F4A2294653BFDBE3FB141AEC76" pageheadline="W4C pages leaf" wkfguid="82208942EB9F48EEA58854222EAFE2AA" wkfname="wf_old_layout_check"/>
		 
		 <PROJECTVARIANTS>
		 <PROJECTVARIANT guid="8491B46045904DA6A7F1836B06271436" name="Display_do_not_use_for_publishing" checked="1">
		 <TEMPLATEVARIANTS>
		 <TEMPLATEVARIANT guid="DF1017AA49F94934AD1341EC487297C7" name="HTML" generatorstatustext="Will not be published"/>
		 </TEMPLATEVARIANTS>
		 </PROJECTVARIANT>
		 ...
		 </PROJECTVARIANTS>
		 </PAGE>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <PAGE action='load' guid='" + getPageGuid() + "' option='extendedinfo'/>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNode(tagName);
	}

	/**
	 * Liefert den Dateinamen dieser Seite unter Properties vom CMS zurück. Liefert null oder leeren string, falls keiner gesetzt ist.
	 * <p>
	 * Dies ist nicht der generierte Dateiname auf der site.
	 * 
	 * @see #getPublishedFilename(String)
	 */
	public String getFilename() throws RQLException {
		return getDetailsNode().getAttribute("name");
	}

	/**
	 * Liefert eine Liste mit allen gefüllten HTML Textelementen dieser Seite. Leere Textelemente werden nicht geliefert. Orientiert
	 * sich an der Seite, nicht am Template. Auch werden neue, noch nicht aktivierte, Elemente nicht gefunden.
	 * 
	 * @see #getFilledTextElements()
	 */
	public java.util.List<TextElement> getFilledHtmlTextElements() throws RQLException {
		return getTextElements(false, true, true);
	}

	/**
	 * Liefert eine Liste mit allen gefüllten Standardfeld Textelementen dieser Seite. Leere Elemente werden nicht geliefert.
	 * Orientiert sich an der Seite, nicht am Template. Auch werden neue, noch nicht aktivierte, Elemente nicht gefunden.
	 * 
	 * @see #getStandardFieldTextElements()
	 */
	public java.util.List<StandardFieldTextElement> getFilledStandardFieldTextElements() throws RQLException {

		RQLNodeList nodes = getElementNodeList();
		java.util.List<StandardFieldTextElement> elements = new ArrayList<StandardFieldTextElement>();

		if (nodes != null) {
			// for all template elements do
			for (int i = 0; i < nodes.size(); i++) {
				RQLNode node = (RQLNode) nodes.get(i);
				// skip not standard field text elements
				String value = node.getAttribute("value");
				if (Integer.parseInt(node.getAttribute("type")) == TemplateElement.STANDARDFIELD_TEXT_TYPE && value.length() > 0) {
					elements.add(new StandardFieldTextElement(this,
							getTemplateElementByGuid(node.getAttribute("templateelementguid")), node.getAttribute("name"), node
									.getAttribute("guid"), value));
				}
			}
		}
		return elements;
	}

	/**
	 * Liefert eine Liste mit allen gefüllten Textelementen dieser Seite. Leere Textelemente werden nicht geliefert. Orientiert sich an
	 * der Seite, nicht am Template. Auch werden neue, noch nicht aktivierte, Elemente nicht gefunden.
	 * 
	 * @see #getTextElements()
	 */
	public java.util.List<TextElement> getFilledTextElements() throws RQLException {
		return getTextElements(true, true, true);
	}

	/**
	 * Liefert den Frame aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param frameTemplateElementName
	 *            TemplateElement muss vom Typ 3 (Frame) sein.
	 * @return <code>Frame</code>
	 * @see <code>Frame</code>
	 */
	public Frame getFrame(String frameTemplateElementName) throws RQLException {

		return getFrame(getTemplateElementByName(frameTemplateElementName));
	}

	/**
	 * Liefert den Frame aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param frameTemplateElement
	 *            muss vom Typ 3 (Frame) sein.
	 * @return <code>Frame</code>
	 * @see <code>Frame</code>
	 */
	private Frame getFrame(TemplateElement frameTemplateElement) throws RQLException {

		// check type of template element
		if (!frameTemplateElement.isFrame()) {
			throw new WrongTypeException("Template element " + frameTemplateElement.getName() + " is not of type frame.");
		}

		// call CMS
		RQLNode linkNode = findLinkNode(frameTemplateElement);

		// wrap list data
		// return new Frame(this, frameTemplateElement, linkNode.getAttribute("name"), linkNode.getAttribute("guid"), linkNode.getAttribute("islink").equals("10"));
		return new Frame(this, frameTemplateElement, linkNode);
	}

    protected void loadKeywords() {

        StringBuilder rqlRequest = new StringBuilder("");

        rqlRequest.append("<IODATA loginguid=\"").append(getLogonGuid()).append("\" sessionkey=\"").append(getSessionKey()).append("\">")
            .append("<PROJECT sessionkey=\"").append(getSessionKey())
            .append("\"><PAGE guid=\"").append(getPageGuid()).append("\"><KEYWORDS action=\"load\" /></PAGE></PROJECT></IODATA>");

        RQLNode rqlResponse = null;
        try {
            rqlResponse = callCms(rqlRequest.toString());

            if(rqlResponse != null){

                List<Keyword> keywordsFound = new ArrayList<Keyword>();
                
                RQLNodeList categegoryNodes = rqlResponse.getNodes("CATEGORY");

                if(categegoryNodes != null){
                    for (int i = 0; i < categegoryNodes.size(); i++) {
                        RQLNode categoryNode = categegoryNodes.get(i);

                        KeywordCategory keywordCategory = new KeywordCategory(categoryNode.getAttribute("guid"), categoryNode.getAttribute("value"));

                        RQLNodeList keywordNodes = categoryNode.getNodes("KEYWORD");
                        for (int j = 0; j < keywordNodes.size(); j++) {
                            RQLNode keywordNode = keywordNodes.get(j);

                            Keyword keyword = new Keyword(keywordNode.getAttribute("guid"), keywordCategory ,keywordNode.getAttribute("value"));
                            keywordsFound.add(keyword);
                        }
                    }
                }

                this.keywords = keywordsFound;
            }

        } catch (RQLException e) {
            e.printStackTrace();
        }
    }


	/**
	 * Returns the headline of this page. Is empty, if page is created by guid and in recycle bin. In that case use PageSearch to get
	 * even the headline from a page in recycle bin.
	 * 
	 * @see PageSearch#addTypeCriteriaOnlyPagesInRecycleBin()
	 */
	public String getHeadline() throws RQLException {

		if (headline == null) {
			headline = getDetailsHeadline();
		}

		return headline;
	}

	/**
	 * Liefert die Überschrift gemeinsam mit der Page ID zurück.
	 */
	public String getHeadlineAndId() throws RQLException {

		return getHeadline() + " (ID " + getPageId() + ")";
	}

	/**
	 * Liefert den page preview HTML code dieser Seite.
	 * <p>
	 */
	public String getHtmlPagePreview() throws RQLException {
		/* 
		 V6.5 request
		 <IODATA loginguid="1B600CDCAC23438BAE6099AD6204B799" sessionkey="10218343237k60g56oi71">
		 <PREVIEW mode="" translationmode="0" projectguid="06BE79A1D9F549388F06F6B649E27152" loginguid="1B600CDCAC23438BAE6099AD6204B799" url="/reddot5/ioRD.asp" querystring="Action=RedDot&amp;Mode=0&amp;pageguid=1BA94C4F6F2444FEB8B72AB7FE47BB07&amp;islink=2"/>
		 </IODATA>		
		 V5 response 
		 <rde-dm:import>
		 <constraints>
		 <constraint mode="condition" attribute="profile.roles" op="containsany" value="MAIN" description=".">
		 </constraint>
		 </constraints>
		 </rde-dm:import>
		 <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
		 <html>
		 
		 <head><!-- PageID 326015 - published by RedDot 6.5 - 6.5.0.41 - 14962 -->
		 <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
		 <meta name="author" content="Frank Leja (lejafr)">
		 <title>Web Online Business Administrators for all regions</title>
		 <link rel="stylesheet" type="text/css" href="http://hip.hlcl.com/css/content_styles.css">
		 </head>
		 
		 <body style="background-color:#ffffff;">
		 ...
		 */

		// build the reddot tag
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		String reddotTag = b.getString("rqlPreviewHtmlTag");
		Object parms[] = new Object[3];
		parms[0] = getProjectGuid();
		parms[1] = getLogonGuid();
		parms[2] = getPageGuid();
		reddotTag = MessageFormat.format(reddotTag, parms);

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>";
		rqlRequest += reddotTag;
		rqlRequest += "</IODATA>";
		return callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert den HTML code dieser Seite für die Darstellung im Smart-Edit (page closed). Funktioniert nicht, falls die Seite im
	 * Status draft ist!?
	 */
	public String getHtmlSmartEditPageClosed() throws RQLException {
		return simulateSmartEditUsage();
	}

	/**
	 * Liefert das Image Element dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            das TemplateElement muss vom Typ = 2 sein
	 */
	public ImageElement getImageElement(String templateElementName) throws RQLException {

		return getImageElement(getTemplateElementByName(templateElementName));
	}

	/**
	 * Liefert das Image Element dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElement
	 *            muss vom Typ = 2 sein
	 */
	private ImageElement getImageElement(TemplateElement templateElement) throws RQLException {

		// check type of template element
		if (!templateElement.isImage()) {
			throw new WrongTypeException("Template element " + templateElement.getName() + " is not of type Image.");
		}

		// call CMS
		RQLNode elementNode = findElementNode(templateElement);

		// wrap page element data
		return new ImageElement(this, templateElement, elementNode.getAttribute("name"), elementNode.getAttribute("guid"), elementNode
				.getAttribute("value"), elementNode.getAttribute("folderguid"));
	}

	/**
	 * Liefert den Wert des ImageElementes dieser Seite, das auf dem gegebenen templateElement basiert. Liefert den Dateinamen dieses
	 * Files oder null, falls dieses Element kein Dateinamen besitzt und auch kein Defaultwert im Templateelement definiert ist.
	 * 
	 * @param templateElementName
	 *            das TemplateElement muss vom Typ = 2 sein
	 */
	public String getImageValue(String templateElementName) throws RQLException {

		return getImageElement(templateElementName).getFilename();
	}

	/**
	 * Returns the value of the image element on this page which is based on the content class element with the given name. Returns
	 * null, if this image element did not have a filename set at all.
	 * 
	 * @param templateElementName
	 *            das TemplateElement muss vom Typ = 2 sein
	 */
	public File getImageValueFile(String templateElementName) throws RQLException {

		return getImageElement(templateElementName).getFile();
	}

	/**
	 * Liefert Detailinformation zu dieser Seite in dern Form: pageId headline (template name).
	 */
	public String getInfoText() throws RQLException {

		return getInfoText(" ");
	}

	/**
	 * Liefert Detailinformation zu dieser Seite in dern Form: pageId<trenner>headline<trenner>(template name).
	 */
	public String getInfoText(String trenner) throws RQLException {

		return getPageId() + trenner + getHeadline() + trenner + "(" + getTemplateName() + ")";
	}

	/**
	 * Liefert den User, der diese Seite zuletzt geändert hat.
	 */
	public User getLastChangedByUser() throws RQLException {

		return new User(getCmsClient(), getDetailsChangedByUserGuid());
	}

	/**
	 * Liefert die E-Mailadresse des User, der diese Seite zuletzt geändert hat.
	 */
	public String getLastChangedByUserEmailAddress() throws RQLException {

		return getLastChangedByUser().getEmailAddress();
	}

	/**
	 * Liefert den Namen des User, der diese Seite zuletzt geändert hat.
	 */
	public String getLastChangedByUserName() throws RQLException {

		return getDetailsNode().getAttribute("changeusername");
	}

	/**
	 * Liefert den Zeitpunkt der letzten Änderung dieser Seite.
	 */
	public ReddotDate getLastChangedOn() throws RQLException {

		return new ReddotDate(getDetailsNode().getAttribute("changedate"));
	}

	/**
	 * Liefert den Zeitpunkt der letzten Änderung dieser Seite im Format 27 Aug 2009.
	 */
	public String getLastChangedOnAsddMMyyyy() throws RQLException {
		return getLastChangedOn().getAsddMMyyyy();
	}

	/**
	 * Liefert den Zeitpunkt der letzten Änderung dieser Seite im Format 26 Aug 2009 6:15 pm.
	 */
	public String getLastChangedOnAsddMMyyyyHmma() throws RQLException {

		return getLastChangedOn().getAsddMMyyyyHmma();
	}

	/**
	 * Liefert den Zeitpunkt der letzten Änderung dieser Seite im Format 20091120.
	 */
	public String getLastChangedOnAsyyyyMMdd() throws RQLException {
		return getLastChangedOn().getAsyyyyMMdd();
	}

	/**
	 * Liefert die RQLNodeList der Links zurueck an die diese Seite gelinkt ist. Wird null zurückgegeben, ist diese Seite unverlinkt,
	 * frei.
	 * @return "LINK" nodes or null if not linked.
	 */
	private RQLNodeList getLinkedFromNodeList() throws RQLException {
		/* 
		 V5 request (request started from page with id 2281)
		 <IODATA loginguid="8F79F379DF6A4B9A8576705727669ED1" sessionkey="42114046225S3JUHx111">
		 <PAGE guid="036DCFFF773B438B8AB7BE058589A3B1">
		 <LINKSFROM action="load" />
		 </PAGE>
		 </IODATA>
		 V5 response 
		 <IODATA>
		 <LINKSFROM action="load" languagevariantid="ENG" dialoglanguageid="ENG" pageguid="036DCFFF773B438B8AB7BE058589A3B1" guid="036DCFFF773B438B8AB7BE058589A3B1">
		 <LINK pageheadline="Daily News Roundups" pageguid="8BE954C420484400A6061F3FE9B38B5F" ismainlink="1" 
		 relationguid="2B1E95C56108487F824D8EB6CB399C6D" datebegin="0" dateend="0" datestate="1" 
		 connectedbykeyword="0" ok="1" guid="1B7EE21BC6654BC7B6DDEF1CD8000F61" 
		 templateelementguid="4B98D010FFAA4A5E830E78A30BBED2CC" eltflags="0" flags="0" eltrequired="0" 
		 islink="2" formularorderid="0" orderid="1" status="0" name="blocks" eltname="blocks" 
		 aliasname="blocks" variable="blocks" folderguid="" istargetcontainer="0" type="28" elttype="28" 
		 templateelementflags="0" templateelementislink="2" value="blocks" reddotdescription="" workflowname=""/>
		 </LINKSFROM>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + " <PAGE guid='"
				+ getPageGuid() + "'>" + "   <LINKSFROM action='load' />" + " </PAGE>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNodes("LINK");
	}

	/**
	 * Liefert die RQLNodeList mit den Links dieser Seite. basieren.
	 * 
	 * @return <code>RQLNodeList</code>
	 */
	private RQLNodeList getLinksNodeList() throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="C2FFCC097AA64EE594B583FD0BF4A8F5" sessionkey="4211388671o4w56GcjW4">
		 <PAGE guid="28457BD66F9B45A18B9E69F8F9DABAD3" >
		 <LINKS action="load"/>
		 </PAGE>
		 </IODATA> 
		 V5 response (container)
		 <IODATA>
		 <PAGE guid="28457BD66F9B45A18B9E69F8F9DABAD3" sessionkey="4211388671o4w56GcjW4" dialoglanguageid="ENG" languagevariantid="ENG">
		 <LINKS action="load" languagevariantid="ENG" dialoglanguageid="ENG" pageguid="28457BD66F9B45A18B9E69F8F9DABAD3" parenttable="PAG">
		 <LINK islink="2" name="container" guid="A71E9253472845629DBA1C5C813A953E" templateelementguid="3DED4381C1314A348138D73E97346803" languagevariantid="ENG" pageguid="28457BD66F9B45A18B9E69F8F9DABAD3" eltflags="0" flags="0" eltrequired="0" formularorderid="0" orderid="1" eltname="container" aliasname="container" variable="container" istargetcontainer="0" type="28" elttype="28" templateelementflags="0" templateelementislink="2" value="container"/>
		 </LINKS>
		 </PAGE>
		 </IODATA>
		 V5 response (list)
		 <IODATA>
		 <PAGE guid="F45146658C6347CDBB4A2311902985E3" sessionkey="4211388671o4w56GcjW4" dialoglanguageid="ENG" languagevariantid="ENG">
		 <LINKS action="load" languagevariantid="ENG" dialoglanguageid="ENG" pageguid="F45146658C6347CDBB4A2311902985E3" parenttable="PAG">
		 <LINK name="pseudo_list" guid="03D1B69EE20A417E95BA2D593D4CCA77" type="13" templateelementguid="048965A22B164BF8971DFED13A81F43B" islink="2" languagevariantid="ENG" pageguid="F45146658C6347CDBB4A2311902985E3" eltflags="0" flags="4194320" eltrequired="0" formularorderid="0" orderid="1" eltname="pseudo_list" aliasname="pseudo_list" variable="pseudo_list" elttype="13" templateelementflags="4194320" templateelementislink="2" value="pseudo_list"/>
		 </LINKS>
		 </PAGE>
		 </IODATA>
		 V5 response (anchor)
		 <IODATA>
		 <PAGE guid="13856E8A92A84FB5B2EDEB47A62A2620" sessionkey="3519839256vl5msmP05T" dialoglanguageid="ENG" languagevariantid="DEU">
		 <LINKS action="load" languagevariantid="DEU" dialoglanguageid="ENG" pageguid="13856E8A92A84FB5B2EDEB47A62A2620" parenttable="PAG">
		 <LINK eltname="dynLink" elttype="26" value="e8020.creation.date" islink="1" guid="0176F5CE870F417BAC90842341571888" templateelementguid="1BB634828C1246D5A4AB7CBF637EA955" languagevariantid="DEU" pageguid="13856E8A92A84FB5B2EDEB47A62A2620" eltflags="0" flags="1026" eltrequired="0" formularorderid="0" orderid="19" name="dynLink" eltname="dynLink" aliasname="dynLink" variable="dynLink" type="26" elttype="26" templateelementflags="1026" templateelementislink="1"/>
		 </LINKS>
		 </PAGE>
		 </IODATA>
		 V6.5 response (list reference list)
		 <LINK type="13" guid="5EF1125CF59941BDA42E538FCFBEAFBE" eltname="rql_server" islink="10" templateelementguid="E98598EB177C4755AF373263CE80379A" languagevariantid="ENG" defaultlanguagevariantid="" pageguid="14C926798F934B98BF49D61DB9796B5E" eltflags="0" flags="37748752" eltrequired="0" formularorderid="0" orderid="1" name="rql_server" aliasname="rql_server" variable="rql_server" elttype="13" templateelementflags="37748752" templateelementislink="2" value="rql_server"/>
		 */

		// call CMS
		if (linksNodeList == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + " <PAGE guid='"
					+ getPageGuid() + "'>" + "   <LINKS action='load'/>" + " </PAGE>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			linksNodeList = rqlResponse.getNodes("LINK");
		}

		return linksNodeList;
	}

	/**
	 * Liefert die Liste aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param listTemplateElementName
	 *            TemplateElement muss vom Typ 13 (Liste) sein.
	 * @return <code>List</code>
	 * @see <code>List</code>
	 */
	public com.hlcl.rql.as.List getList(String listTemplateElementName) throws RQLException {

		return getList(getTemplateElementByName(listTemplateElementName));
	}

	/**
	 * Liefert die Liste aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param listTemplateElement
	 *            muss vom Typ 13 (Liste) sein.
	 * @return <code>List</code>
	 * @see <code>List</code>
	 */
	private com.hlcl.rql.as.List getList(TemplateElement listTemplateElement) throws RQLException {

		// check type of template element
		if (!listTemplateElement.isList()) {
			throw new WrongTypeException("Template element " + listTemplateElement.getName() + " is not of type list.");
		}

		// call CMS
		RQLNode linkNode = findLinkNode(listTemplateElement);

		return buildList(linkNode, listTemplateElement, this);
	}

	/**
	 * Liefert eine Liste dieser Seite für die gegebenen Link GUID.
	 */
	public com.hlcl.rql.as.List getListByGuid(String listLinkGuid) throws RQLException {

		RQLNode linkNode = findLinkNode(listLinkGuid);

		return (com.hlcl.rql.as.List) buildMultiLink(linkNode);
	}

	/**
	 * Liefert die Kindseiten der Liste aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param listTemplateElementName
	 *            TemplateElement muss vom Typ 13 (Liste) sein.
	 * @return <code>PageArrayList</code>
	 * @see <code>PageArrayList</code>
	 */
	public PageArrayList getListChildPages(String listTemplateElementName) throws RQLException {

		return getList(getTemplateElementByName(listTemplateElementName)).getChildPages();
	}

	/**
	 * Liefert die Kindseiten der Liste aus dieser Seite, der auf dem gegebenen templateElement basiert und dem gegebenen Template
	 * entspricht.
	 * 
	 * @param listTemplateElementName
	 *            TemplateElement muss vom Typ 13 (Liste) sein.
	 * @return <code>PageArrayList</code>
	 * @see <code>PageArrayList</code>
	 */
	public PageArrayList getListChildPages(String listTemplateElementName, String childTemplateName) throws RQLException {

		return getList(getTemplateElementByName(listTemplateElementName)).getChildPages().selectAllTemplateNamed(childTemplateName);
	}

	/**
	 * Liefert einen Iterator auf die Kindseiten der Liste aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param listTemplateElementName
	 *            TemplateElement muss vom Typ 13 (Liste) sein.
	 * @return <code>Iterator</code>
	 * @see <code>Iterator</code>
	 */
	public Iterator getListChildPagesIterator(String listTemplateElementName) throws RQLException {

		return getList(getTemplateElementByName(listTemplateElementName)).getChildPagesIterator();
	}

	/**
	 * Liefert den User, der die Seite sperrt oder zuletzt gesperrt hat. Kann auch der angemeldete sein.
	 * <p>
	 * Benötigt Servermanager-Lizenz!
	 * 
	 * @see #isLocked()
	 */
	public User getLockedByUser() throws RQLException {
		return getCmsClient().getUserByName(getLockedByUserName());
	}

	/**
	 * Liefert die E-Mailadresse des Users, der die Seite sperrt oder zuletzt gesperrt hat. Kann auch der angemeldete sein.
	 * 
	 * @see #isLocked()
	 */
	public String getLockedByUserEmailAddress() throws RQLException {
		return getDetailsNode().getAttribute("lockuseremail");
	}

	/**
	 * Liefert den Usernamen des Autors, der die Seite sperrt oder zuletzt gesperrt hat. Kann auch der angemeldete sein.
	 * 
	 * @see #isLocked()
	 */
	public String getLockedByUserName() throws RQLException {
		return getDetailsNode().getAttribute("lockusername");
	}

	/**
	 * Liefert den Zeitpunkt seitdem diese Seite gesperrt ist.
	 * 
	 * @see #isLocked()
	 */
	public ReddotDate getLockedSince() throws RQLException {
		return new ReddotDate(getDetailsNode().getAttribute("lockdate"));
	}

	/**
	 * Liefert den Zeitpunkt seitdem diese Seite gesperrt ist im Format: 27 Aug 2008 1:41 PM.
	 * 
	 * @see #isLocked()
	 */
	public String getLockedSinceAsddMMyyyyHmma() throws RQLException {
		return getLockedSince().getAsddMMyyyyHmma();
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
	 * Liefert die GUID des Hauptlinks zurück an der diese Seite hängt.
	 * 
	 * @throws UnlinkedPageException,
	 *             MissingMainLinkException
	 */
	public String getMainLinkGuid() throws RQLException {

		return getMainLinkNode().getAttribute("guid");
	}

	/**
	 * Liefert den Link-Node des Hauptlinks zurück an der diese Seite hängt.
	 * 
	 * @throws UnlinkedPageException,
	 *             MissingMainLinkException
	 */
	private RQLNode getMainLinkNode() throws RQLException {

		// get links to this page
		RQLNodeList linkNodeListOrNull = getLinkedFromNodeList();
		if (linkNodeListOrNull == null) {
			throw new UnlinkedPageException("Page " + getPageId() + " (" + getHeadlineAndId() + ") is unlinked. Script does not work on unlinked pages.");
		}

		// find main link
		RQLNode linkNode = null;
		for (int i = 0; i < linkNodeListOrNull.size(); i++) {
			linkNode = linkNodeListOrNull.get(i);
			if (linkNode.getAttribute("ismainlink").equals("1")) {
				return linkNode;
			}
		}

		// no main link found
		throw new MissingMainLinkException("Page " + getPageId() + " has no main link.");
	}

	/**
	 * Liefert die Parent-Seite zurueck an die diese Seite über den MainLink gelinkt ist.
	 * 
	 * @throws UnlinkedPageException
	 * @throws MissingMainLinkException
	 */
	public Page getMainLinkParentPage() throws RQLException {

		return new Page(getProject(), getMainLinkNode().getAttribute("pageguid"));
	}

	/**
	 * Liefert die Liste oder den Container an der dieser Seite über Hauptlink hängt.
	 * 
	 * @throws UnlinkedPageException
	 * @throws MissingMainLinkException
	 */
	public MultiLink getMainMultiLink() throws RQLException {

		return buildMultiLink(getMainLinkNode());
	}

	
	/**
	 * Subclasses of FileElement.
	 *  
	 * @return MediaElement or ImageElement
	 * @throws RQLException
	 */
	public FileElement getFileElement(String templateElementName) throws RQLException {
		TemplateElement e = getTemplateElementByName(templateElementName);
		
		if (e.isImage()) {
			return getImageElement(e);
		} else if (e.isMedia()) {
			return getMediaElement(e);
		} else {
			throw new WrongTypeException("Template element " + templateElementName + " is not of type Media or Image.");
		}
	}
	
	
	/**
	 * Liefert das Media Element dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            das TemplateElement muss vom Typ = 38 sein
	 */
	public MediaElement getMediaElement(String templateElementName) throws RQLException {
		return getMediaElement(getTemplateElementByName(templateElementName));
	}

	/**
	 * Liefert das Media Element dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElement
	 *            muss vom Typ = 38 sein
	 */
	private MediaElement getMediaElement(TemplateElement templateElement) throws RQLException {

		// check type of template element
		if (!templateElement.isMedia()) {
			throw new WrongTypeException("Template element " + templateElement.getName() + " is not of type Media.");
		}

		// call CMS
		RQLNode elementNode = findElementNode(templateElement);

		// wrap page element data
		// add templateElement too?
		return new MediaElement(this, templateElement, elementNode.getAttribute("name"), elementNode.getAttribute("guid"), elementNode
				.getAttribute("value"), elementNode.getAttribute("folderguid"), elementNode.getAttribute("suffix"));
	}

	/**
	 * Liefert den Wert des MediaElementes dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            das TemplateElement muss vom Typ = 38 sein
	 */
	public String getMediaValue(String templateElementName) throws RQLException {

		return getMediaElement(templateElementName).getFilename();
	}

	/**
	 * Returns the value of the media element on this page which is based on the content class element with the given name. Returns
	 * null, if this image element did not have a filename set at all.
	 * 
	 * @param templateElementName
	 *            das TemplateElement muss vom Typ = 38 sein
	 */
	public File getMediaValueFile(String templateElementName) throws RQLException {

		return getMediaElement(templateElementName).getFile();
	}

	/**
	 * Liefert den MultiLink aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param multiLinkTemplateElementName
	 *            muss vom Typ 28 (Container) or Typ 13 (Liste) sein.
	 * @return <code>Container</code>
	 * @see <code>Container</code>
	 */
	public MultiLink getMultiLink(String multiLinkTemplateElementName) throws RQLException {

		return getMultiLink(getTemplateElementByName(multiLinkTemplateElementName));
	}

	/**
	 * Liefert den MultiLink (Container oder Liste) aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param multiLinkTemplateElement
	 *            muss vom Typ 13 (Liste) oder Typ 28 (Container) sein.
	 * @return <code>MultiLink</code>
	 * @see <code>getList()</code>
	 * @see <code>getContainer()</code>
	 */
	private MultiLink getMultiLink(TemplateElement multiLinkTemplateElement) throws RQLException {

		// check type of template element
		if (!multiLinkTemplateElement.isList() & !multiLinkTemplateElement.isContainer()) {
			throw new WrongTypeException("Template element " + multiLinkTemplateElement.getName()
					+ " is neither a Container nor a List.");
		}

		// call CMS
		RQLNode linkNode = findLinkNode(multiLinkTemplateElement);

		return buildMultiLink(linkNode);
	}

	/**
	 * Liefert eine Liste oder einen Container dieser Seite für die gegebenen Link GUID.
	 */
	public MultiLink getMultiLinkByGuid(String multiLinkGuid) throws RQLException {

		RQLNode linkNode = findLinkNode(multiLinkGuid);

		return buildMultiLink(linkNode);
	}

	/**
	 * Liefert alle MultiLinks (Type=13 oder Type=28) dieser Seite zurück. MultiLinks, die Referenzquellen sind werden mitgeliefert.
	 * 
	 * @return java.util.List mit List oder Container Objekten
	 */
	public java.util.List getMultiLinks() throws RQLException {
		return getMultiLinks(true);
	}

	/**
	 * Liefert alle MultiLinks (Type=13 oder Type=28) dieser Seite zurück.
	 * 
	 * @param includeReferences
	 *            =true, auch Links, die Referenzquelle sind werden geliefert (haben keine Childs!) =false, ohne Links, die
	 *            Referenzquelle sind (nur diese haben Childs!)
	 * @return java.util.List mit List oder Container Objekten
	 */
	public java.util.List<MultiLink> getMultiLinks(boolean includeReferences) throws RQLException {

		RQLNodeList linkNodes = getLinksNodeList();
		java.util.List<MultiLink> multiLinks = new ArrayList<MultiLink>();

		// treat as empty
		if (linkNodes == null) {
			return multiLinks;
		}

		// collect all MultiLinks
		for (int i = 0; i < linkNodes.size(); i++) {
			RQLNode linkNode = linkNodes.get(i);

			// filter only lists or container
			int type = Integer.parseInt(linkNode.getAttribute("elttype"));
			if (type == TemplateElement.LIST_TYPE || type == TemplateElement.CONTAINER_TYPE) {
				// skip references if requested
				MultiLink link = buildMultiLink(linkNode);
				if (!includeReferences && link.isReferenceSource()) {
					continue;
				}
				// collect link to return
				multiLinks.add(link);
			}
		}
		return multiLinks;
	}

	/**
	 * Liefert einen Iterator für alle MultiLinks zurück, die auf diese Seite verweisen. Das Ergebnis entspricht der RD Funktion show
	 * reference list dieser Seite.
	 * <p>
	 * Templateelemente, die ebenfalls diese Seite referenzieren werden nicht geliefert (fehlen bereits im RQL). Sie werden auch nicht
	 * durch die RD Funktion show reference list geliefert!
	 * <p>
	 */
	public Iterator<MultiLink> getMultiLinksReferencingThisPage() throws RQLException {
		return getProject().getMultiLinksReferencingIterator(getPageGuid());
	}

	/**
	 * Liefert alle MultiLinks (Type=13 oder Type=28) dieser Seite in der Reihenfolge im Tree (blocks_top vor blocks_bottom) zurück.
	 * 
	 * @param includeReferences
	 *            =true, auch Links, die Referenzquelle sind werden geliefert (haben keine Childs!) =false, ohne Links, die
	 *            Referenzquelle sind (nur diese haben Childs!)
	 * @return java.util.List mit List oder Container Objekten
	 */
	public java.util.List<MultiLink> getMultiLinksSorted(boolean includeReferences) throws RQLException {

		java.util.List<MultiLink> links = getMultiLinks(includeReferences);

		return sortLinks(links, includeReferences);
	}

	/**
	 * Liefert alle MultiLinks (Type=13 oder Type=28) die zu dieser Seite führen. Dies entspricht der Anzeige im Linking Dialog.
	 * 
	 * @return java.util.List mit List oder Container Objekten
	 */
	public java.util.List<MultiLink> getMultiLinksToThisPage() throws RQLException {

		RQLNodeList linkNodes = getLinkedFromNodeList();
		java.util.List<MultiLink> multiLinks = new ArrayList<MultiLink>();

		// treat as empty
		if (linkNodes == null) {
			return multiLinks;
		}

		// collect all MultiLinks
		for (int i = 0; i < linkNodes.size(); i++) {
			RQLNode linkNode = linkNodes.get(i);

			// filter only lists or container
			int type = Integer.parseInt(linkNode.getAttribute("elttype"));
			if (type == TemplateElement.LIST_TYPE || type == TemplateElement.CONTAINER_TYPE) {
				multiLinks.add(buildMultiLink(linkNode));
			}
		}
		return multiLinks;
	}

	/**
	 * Liefert alle MultiLinks (Type=13 oder Type=28) dieser Seite zurück, für die es kein Schattenelement mit dem gegebenen suffix
	 * gibt.
	 * 
	 * @param includeReferences
	 *            =true, auch Links, die Referenzquelle sind werden geliefert (haben keine Childs!) =false, ohne Links, die
	 *            Referenzquelle sind (nur diese haben Childs!)
	 * @return java.util.List mit List oder Container Objekten
	 */
	public java.util.List<MultiLink> getMultiLinksWithoutShadowedOnes(String shadowElementsNameSuffix, boolean includeReferences)
			throws RQLException {

		RQLNodeList linkNodes = getLinksNodeList();
		java.util.List<MultiLink> multiLinks = new ArrayList<MultiLink>();

		// treat as empty
		if (linkNodes == null) {
			return multiLinks;
		}

		// collect all MultiLinks except the shadowed ones
		String delimiter = ",";
		String excludeGuids = getTemplate().getShadowedTemplateElementGuids(shadowElementsNameSuffix, delimiter);
		for (int i = 0; i < linkNodes.size(); i++) {
			RQLNode linkNode = linkNodes.get(i);

			// filter only lists or container and skip the shadowed ones
			int type = Integer.parseInt(linkNode.getAttribute("elttype"));
			if (type == TemplateElement.LIST_TYPE || type == TemplateElement.CONTAINER_TYPE) {
				// skip a shadowed multi link
				if (excludeGuids.length() > 0
						&& StringHelper.contains(excludeGuids, delimiter, linkNode.getAttribute("templateelementguid"))) {
					continue;
				}
				// skip references
				MultiLink link = buildMultiLink(linkNode);
				if (link.isReferenceSource() && !includeReferences) {
					continue;
				}
				// collect link to return
				multiLinks.add(link);
			}
		}
		return multiLinks;
	}

	/**
	 * Liefert alle MultiLinks (Type=13 oder Type=28) dieser Seite in der Reihenfolge im Tree (blocks_top vor blocks_bottom) zurück,
	 * für die es kein Schattenelement mit dem gegebenen suffix gibt.
	 * 
	 * @param includeReferences
	 *            =true, auch Links, die Referenzquelle sind werden geliefert (haben keine Childs!) =false, ohne Links, die
	 *            Referenzquelle sind (nur diese haben Childs!)
	 * @return java.util.List mit List oder Container Objekten
	 */
	public java.util.List getMultiLinksWithoutShadowedOnesSorted(String shadowElementsNameSuffix, boolean includeReferences)
			throws RQLException {

		// get links
		java.util.List<MultiLink> links = getMultiLinksWithoutShadowedOnes(shadowElementsNameSuffix, includeReferences);

		return sortLinks(links, includeReferences);
	}

	/**
	 * Liefert die Bemerkung zu dieser Seite, das auf dem gegebenen Administrations-Element basiert. ACHTUNG: Die GUID des gegebenen
	 * Administrations-Elementes und der Note in der Seite sind gleich!
	 */
	private Note getNote(AdministrationElement administrationElement) throws RQLException {

		/* 
		 V5 request
		 <IODATA loginguid="F1AC9D2C51E94EF09399C80160ADBEB9" sessionkey="371636152EnyV868i458">
		 <PAGE guid="792F72BCCE13424FA151B12DFDF203C2">
		 <SUPPLEMENTS action="load"/>
		 </PAGE>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <SUPPLEMENTS>
		 <SUPPLEMENT guid="FB06DFFF65FD434CA09A6CF628A95905" name="linking information" type="2" value="test linking info"/>
		 <SUPPLEMENT guid="412656C9F03C426CA2BB78C7A184D447" name="workflow information" type="2" value=" 2test info"/>
		 </SUPPLEMENTS>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PAGE guid='"
				+ getPageGuid() + "'>" + "<SUPPLEMENTS action='load'/>" + "</PAGE>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		// find note
		RQLNodeList noteList = rqlResponse.getNodes("SUPPLEMENT");
		RQLNode noteNode = null;

		// no note in this page
		if (noteList == null) {
			throw new ElementNotFoundException("The page " + getHeadlineAndId()
					+ " has no notes. Maybe you try on the wrong page or you need to create a note element in the content class.");
		}

		// find the right note
		for (int i = 0; i < noteList.size(); i++) {
			noteNode = noteList.get(i);

			if (noteNode.getAttribute("guid").equals(administrationElement.getAdministrationElementGuid())) {
				// create note
				return new Note(this, noteNode.getAttribute("name"), noteNode.getAttribute("type"), noteNode.getAttribute("guid"),
						noteNode.getAttribute("value"));
			}
		}
		throw new ElementNotFoundException("Note with name " + administrationElement.getName() + " could not be found in page "
				+ getHeadlineAndId() + ".");
	}

	/**
	 * Liefert das Note-Objekt dieser Seite mit dem gegebenen Namen basiert.
	 * 
	 * @param administrationElementName
	 *            Name des Administrations-Elementes aus dem Template
	 */
	public Note getNote(String administrationElementName) throws RQLException {

		return getNote(getAdministrationElementByName(administrationElementName));
	}

	/**
	 * Liefert den Wert des Verwaltungseintrages mit dem gegebenen Namen.
	 * <p>
	 * Die Länge scheint nicht wie im CMS auf 255 Zeichen begrenzt.
	 * 
	 * @param name
	 *            Name des Verwaltungseintrages
	 */
	public String getNoteValue(String name) throws RQLException {

		return getNote(name).getValue();
	}

	/**
	 * Liefert die OptionsListe aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param listTemplateElementName
	 *            TemplateElement muss vom Typ 8 (OptionList) sein.
	 * @return <code>OptionList</code>
	 * @see <code>OptionList</code>
	 */
	public OptionList getOptionList(String listTemplateElementName) throws RQLException {

		return getOptionList(getTemplateElementByName(listTemplateElementName));

	}

	/**
	 * Liefert alle OptionsListen aus dieser Seite, deren Templateelementname zu dem gegebenen namePattern (muss {0} enthalten) passt.
	 * 
	 * @see #getOptionList(String)
	 */
	public java.util.List<OptionList> getOptionLists(String templateElementNamePattern) throws RQLException {
		List<TemplateElement> templateElements = getTemplate().getOptionListTemplateElements(templateElementNamePattern);
		java.util.List<OptionList> result = new ArrayList<OptionList>(templateElements.size());
		// collect page elements
		for (TemplateElement te : templateElements) {
			result.add(getOptionList(te));
		}
		return result;
	}

	/**
	 * Liefert die OptionsListe aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param listTemplateElement
	 *            muss vom Typ 8 (OptionList) sein.
	 * @return <code>OptionList</code>
	 * @see <code>OptionList</code>
	 */
	private OptionList getOptionList(TemplateElement listTemplateElement) throws RQLException {

		// check type of template element
		if (!listTemplateElement.isOptionList()) {
			throw new WrongTypeException("Template element " + listTemplateElement.getName() + " is not of type option list.");
		}

		// call CMS
		RQLNode elementNode = findElementNode(listTemplateElement);

		// wrap option list data
		// add listTemplateElement too?
		OptionList optionList = new OptionList(this, listTemplateElement, elementNode.getAttribute("name"), elementNode
				.getAttribute("guid"), elementNode.getAttribute("value"), elementNode.getAttribute("eltdefaultselectionguid"));

		// add possible selections to the option list
		RQLNodeList selectionNodeList = elementNode.getNodes("SELECTION");

		for (int i = 0; selectionNodeList != null && i < selectionNodeList.size(); i++) {
			RQLNode selectionNode = selectionNodeList.get(i);
			optionList.addSelection(selectionNode.getAttribute("guid"), selectionNode.getAttribute("description"), selectionNode
					.getAttribute("value"));
		}
		return optionList;
	}

	/**
	 * Liefert den Wert des OptionList Elements dieser Seite, das auf dem gegebenen templateElement basiert oder null, falls weder
	 * diese Optionsliste einen Wert hat noch im Templateelement ein default gesetzt ist.
	 * 
	 * @param templateElementName
	 *            Name der OptionList im Template
	 */
	public String getOptionListValue(String templateElementName) throws RQLException {

		return getOptionList(templateElementName).getCurrentSelectionValue();
	}

	/**
	 * Returns a list of option list element values of this page.
	 * 
	 * @param templateElementNamePattern
	 *            Pattern of the template elementn name containing exactly one argument {0}
	 * @param arguments
	 *            each argument number will be converted into a string and inserted into {0} and the value of this option list element
	 *            will be collected
	 * @param skipEmptyValues
	 *            if true, values of empty option list elements (=null) are not included into result
	 *            <p>
	 *            if false, null values are included into result (size of arguments is equal to size of result list)
	 * 
	 * @see #getOptionListValue(String)
	 */
	public java.util.List<String> getOptionListValues(String templateElementNamePattern, boolean skipEmptyValues, int... arguments)
			throws RQLException {
		return getOptionListValues(templateElementNamePattern, skipEmptyValues, StringHelper.convertArray(arguments));
	}

	/**
	 * Returns a list of option list element values of this page.
	 * 
	 * @param templateElementNamePattern
	 *            Pattern of the template elementn name containing exactly one argument {0}
	 * @param arguments
	 *            each argument string will be inserted into {0} and the value of this option list element will be collected
	 * @param skipEmptyValues
	 *            if true, values of empty option list elements (=null) are not included into result
	 *            <p>
	 *            if false, null values are included into result (size of arguments is equal to size of result list)
	 * 
	 * @see #getOptionListValue(String)
	 */
	public java.util.List<String> getOptionListValues(String templateElementNamePattern, boolean skipEmptyValues, String... arguments)
			throws RQLException {
		java.util.List<String> result = new ArrayList<String>(arguments.length);
		// collect values
		for (String argument : arguments) {
			String templateElementName = MessageFormat.format(templateElementNamePattern, argument);
			OptionList ol = getOptionList(templateElementName);
			// skip empty values
			if (skipEmptyValues && ol.isEmpty()) {
				continue;
			}
			// otherwise add
			OptionListSelection currentSelection = ol.getCurrentSelection();
			result.add(currentSelection == null ? null : currentSelection.getValue());
		}
		return result;
	}

	/**
	 * Returns a list of option list element values of this page. Some entries might be null.
	 * <p>
	 * Size of returned list is equal to size of given arguments.
	 * 
	 * @param templateElementNamePattern
	 *            Pattern of the template element name containing exactly one argument {0}
	 * @param arguments
	 *            each argument string will be inserted into {0} and the value of this option list element will be collected
	 * 
	 * @see #getOptionListValue(String)
	 */
	public java.util.List<String> getOptionListValues(String templateElementNamePattern, String... arguments) throws RQLException {
		return getOptionListValues(templateElementNamePattern, false, arguments);
	}

	/**
	 * Liefert die RedDot GUID der Seite.
	 */
	public String getPageGuid() {

		return pageGuid;
	}

	/**
	 * Liefert die RedDot page ID dieser Seite.
	 * 
	 * @return String
	 */
	public String getPageId() throws RQLException {

		if (pageId == null) {
			pageId = getDetailsPageId();
		}

		return pageId;
	}

	/**
	 * Liefert die RedDot page ID dieser Seite als int.
	 * 
	 * @return String
	 */
	public int getPageIdAsInt() throws RQLException {

		return Integer.parseInt(getPageId());
	}

	/**
	 * Liefert die RedDot page ID dieser Seite als Integer.
	 * 
	 * @return String
	 */
	public Integer getPageIdAsInteger() throws RQLException {

		return Integer.valueOf(getPageId());
	}

	/**
	 * Liefert die URL für die Seitenvorschau. Im Status draft funktioniert page preview nicht.
	 * Can be used to activate page elements which are added into the template.
	 */
	public String getPagePreviewUrl() throws RQLException {
		
		String pagePreviewUrl = getCmsClient().getPagePreviewUrlPattern();
		Object[] parms = new Object[1];
		parms[0] = getPageGuid();
		return MessageFormat.format(pagePreviewUrl, parms);
	}

	/**
	 * Liefert die Seite zurueck an die diese Seite gelinkt ist. Dieser einzige Link sollte dann der Hauptlink sein, was hier aber
	 * nicht geprüft wird.
	 * 
	 * @throws MultiLinkedPageException
	 * @throws UnlinkedPageException
	 */
	public Page getParentPage() throws RQLException {

		RQLNodeList linkNodeList = getLinkedFromNodeList();

		// page is unlinked
		if (linkNodeList == null) {
			throw new UnlinkedPageException("Page " + getPageId() + " is unlinked. Script does not work on unlinked pages.");
		}

		// should be linked only once
		if (linkNodeList.size() > 1) {
			throw new MultiLinkedPageException("Could not return one page which links to this page with ID=" + getPageId() + ".");
		}

		// wrap page data
		RQLNode linkNode = linkNodeList.get(0);
		return new Page(getProject(), linkNode.getAttribute("pageguid"));
	}

	/**
	 * Liefert alle Elternseiten (nur über alle MultiLinks (Type=13 oder Type=28) die zu dieser Seite führen). Dies entspricht der
	 * Anzeige der Seiten im Linking Dialog.
	 * 
	 * @return an empty collection, if this page is unlinked
	 * 
	 * @see #getMultiLinksToThisPage()
	 * @see #getParentPage()
	 */
	public PageArrayList getParentPages() throws RQLException {

		java.util.List<MultiLink> links = getMultiLinksToThisPage();

		// collect all pages where these links are on
		PageArrayList result = new PageArrayList(links.size());
		for (Iterator<MultiLink> iter = links.iterator(); iter.hasNext();) {
			MultiLink link = iter.next();
			result.add(link.getPage());
		}
		return result;
	}

	/**
	 * Liefert diese oder die nächste Vorgängerseite zurück, die ein Element mit gegebenem Namen enthällt.
	 * 
	 * @param templateElementName
	 *            Name des Templateelementes das gesucht wird
	 */
	public Page getPredecessorPageContainingElement(String templateElementName) throws RQLException {

		return getPredecessorPageContainingElement(templateElementName, true);
	}

	/**
	 * Liefert diese oder die nächste Vorgängerseite (über MainLink) zurück, die ein Element mit gegebenem Namen enthällt.
	 * 
	 * @param templateElementName
	 *            Name des Templateelementes das gesucht wird
	 * @param startOnThisPage =
	 *            false beginnt Suche erst im Parent dieser Seite startOnThisPage = true beginnt Suche bereits mit dieser Seite
	 */
	public Page getPredecessorPageContainingElement(String templateElementName, boolean startOnThisPage) throws RQLException {

		if (startOnThisPage && contains(templateElementName)) {
			return this;
		}

		return getMainLinkParentPage().getPredecessorPageContainingElement(templateElementName);
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
	 * Liefert das Exportpaket über das diese Seite generiert werden muss. Liefert gegebenenfalls das globale Exportpaket zurück.
	 * Liefert null, falls kein Exportpaket bestimmt werden kann. This method is quite slow, because of the underlaying RQL, if the
	 * publicatoin packet is comprehensive.
	 * <p>
	 * The main link publication package is cached within this page to speed up publishing path calculation.
	 * <p>
	 * <b>Attention:</b> Do not use this method in same program together with functions to update/change the publication package. The
	 * cache in this page might not be reset accordingly. So it is possible to get an publication package object with the old
	 * (unchanged) data!
	 */
	public PublicationPackage getPublicationPackage() throws RQLException {

		/* 
		 V5 request
		 <IODATA loginguid="5F804A2F2B6D4459B9C21F3F0C40524C" sessionkey="571684951b361671imP4"> 
		 <PROJECT>
		 <EXPORTPACKET action="loadpacket" linkguid="8D672CAB0A5E4B4F8B505D312D617B79" />
		 </PROJECT>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <EXPORTPACKET  guid="80C6EA4C6F6F4CFD9FF1D6B50DD325E4" name="general export">
		 <EXPORTSETTINGS>
		 <EXPORTSETTING  inherit="" default="0" guid="252A68CD883B44868C9568B022C99A94" projectvariantguid="18B4C61D1C514E6B8E8E1BB9669B1109" languagevariantguid="95BBAD2CCB53405E8B303BA7FFF7B753" projectvariantname="HTML" languagevariantname="Deutsch" languagevariantid="DEU" codepage="">
		 <EXPORTTARGETS>
		 <EXPORTTARGET  guid="9200CE56615E4429919E1B72D9475825" name="wwwtest.hlcl.com (khhdp09)"/>
		 </EXPORTTARGETS>
		 <FOLDEREXPORTSETTINGS>
		 <FOLDEREXPORTSETTING  folderguid="00000000000000000000000000000100" foldername="Published pages" path="8757E4B654ED4DCF9D054D6027267DBB" htmlpath="8757E4B654ED4DCF9D054D6027267DBB" name="pages" realname="pages" realvirtualname="" virtualname="pages"/>
		 <FOLDEREXPORTSETTING  folderguid="263CD34373AF404597A603951D3F3CBE" foldername="head" path="E0A9EE6810C54DE3A02A14DD471E6B30" htmlpath="E0A9EE6810C54DE3A02A14DD471E6B30" name="images\head" realname="head" realvirtualname="" virtualname="images\head"/>
		 <FOLDEREXPORTSETTING  folderguid="57CA93258CC9433F85C6EFE9F1075C47" foldername="media_pages" path="8757E4B654ED4DCF9D054D6027267DBB" htmlpath="8757E4B654ED4DCF9D054D6027267DBB" name="pages" realname="pages" realvirtualname="" virtualname="pages"/>
		 <FOLDEREXPORTSETTING  folderguid="5FC9174AADCE463299047443A17071A0" foldername="fotos" path="2E5FC1FC8E1D4700A27D17780A850587" htmlpath="2E5FC1FC8E1D4700A27D17780A850587" name="images\fotos" realname="fotos" realvirtualname="" virtualname="images\fotos"/>
		 <FOLDEREXPORTSETTING  folderguid="8BE00E383A754C54A0DA695D339F4233" foldername="other" path="7D502F58BD654ED9B1DC927F34DCCB9B" htmlpath="7D502F58BD654ED9B1DC927F34DCCB9B" name="images\other" realname="other" realvirtualname="" virtualname="images\other"/>
		 <FOLDEREXPORTSETTING  folderguid="910149CEF6C845A4918EE9BE0C25A5F9" foldername="diagramme" path="2E5FC1FC8E1D4700A27D17780A850587" htmlpath="2E5FC1FC8E1D4700A27D17780A850587" name="images\fotos" realname="fotos" realvirtualname="" virtualname="images\fotos"/>
		 <FOLDEREXPORTSETTING  folderguid="A4B3584E09B2406C809A522E1A7943F4" foldername="background" path="7D502F58BD654ED9B1DC927F34DCCB9B" htmlpath="7D502F58BD654ED9B1DC927F34DCCB9B" name="images\other" realname="other" realvirtualname="" virtualname="images\other"/>
		 <FOLDEREXPORTSETTING  folderguid="A7C28526939A41B8925D0BC23CF499D1" foldername="nav" path="19B83346818846DC910F10516104A882" htmlpath="19B83346818846DC910F10516104A882" name="images\nav" realname="nav" realvirtualname="" virtualname="images\nav"/>
		 <FOLDEREXPORTSETTING  folderguid="BD0DC62CE52749138A3825D628148447" foldername="logos" path="450869462C4641D1B3A09D01D025FA68" htmlpath="450869462C4641D1B3A09D01D025FA68" name="images\logos" realname="logos" realvirtualname="" virtualname="images\logos"/>
		 <FOLDEREXPORTSETTING  folderguid="CCF546941AFB4D27B3355C4280481830" foldername="images" path="5B7C4B49932A489FA6718DAA5EE05594" htmlpath="5B7C4B49932A489FA6718DAA5EE05594" name="images" realname="images" realvirtualname="" virtualname="images"/>
		 <FOLDEREXPORTSETTING  folderguid="D2353EBC90CE48FAAABF6A1A40B68553" foldername="media" path="B85166C079FA48D98675EEF751126F02" htmlpath="B85166C079FA48D98675EEF751126F02" name="media" realname="media" realvirtualname="" virtualname="media"/>
		 <FOLDEREXPORTSETTING  folderguid="EAC64F2B4B9B4616A4FE5E60C39D5138" foldername="maps" path="2E5FC1FC8E1D4700A27D17780A850587" htmlpath="2E5FC1FC8E1D4700A27D17780A850587" name="images\fotos" realname="fotos" realvirtualname="" virtualname="images\fotos"/>
		 <FOLDEREXPORTSETTING  folderguid="F7F5E3D58B27403F9E282F0E5D939C8F" foldername="buttons" path="BB1EF03381C847CB84024F21A6FF980D" htmlpath="BB1EF03381C847CB84024F21A6FF980D" name="images\buttons" realname="buttons" realvirtualname="" virtualname="images\buttons"/>
		 <FOLDEREXPORTSETTING  folderguid="FDBD6CBF795C41E597E6B7AD3C5BFFFF" foldername="dailydata" path="DAD15BDF17404914A4E91A0961AEB7F6" htmlpath="DAD15BDF17404914A4E91A0961AEB7F6" name="daily" realname="daily" realvirtualname="" virtualname="daily"/>
		 </FOLDEREXPORTSETTINGS>
		 </EXPORTSETTING>
		 </EXPORTSETTINGS>
		 </EXPORTPACKET>
		 </IODATA>
		 V6 response, if no package
		 <EXPORTPACKET  guid="00000000000000000000000000000001" name="empty">
		 */

		// call CMS
		if (publicationPackage == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PROJECT>"
					+ "   <EXPORTPACKET action='loadpacket' linkguid='" + getMainLinkGuid() + "'/>" + " </PROJECT>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			RQLNode packetNode = rqlResponse.getNode("EXPORTPACKET");

			// check, if no package assigned
			String guid = packetNode.getAttribute("guid");
			String name = packetNode.getAttribute("name");
			if (guid.equals("00000000000000000000000000000001") && name.equals("empty")) {
				return null;
			}
			publicationPackage = new PublicationPackage(getProject(), guid, name, packetNode);
		}

		// wrap results
		return publicationPackage;
	}

	/**
	 * Liefert den publizierten Dateinamen dieser Seite für die gegebene Projektvariante vom CMS zurück. Attention, this method is
	 * quite slow, because a lot of settings spread around needs to be investigated.
	 * <p>
	 * 
	 * @see #getFilename()
	 */
	public String getPublishedFilename(String projectVariantGuid) throws RQLException {
		return new PublishedFilenameBuilder(this, getProject().getProjectVariantByGuid(projectVariantGuid)).getPublishedFilename();
	}

	/**
	 * Returns the published path (incl. the filename and extender) of this page for the given project variant and language.
	 * <p>
	 * FTP Server, determined by the publishing target, is not included.
	 * 
	 * @see #getPublishedPath(String, String, String)
	 */
	public String getPublishedPath(String projectVariantGuid, LanguageVariant languageVariant, String folderSeparator)
			throws RQLException {
		return getPublishedPath(projectVariantGuid, languageVariant.getRfcLanguageId(), folderSeparator);
	}

	/**
	 * Returns the published path (incl. the filename and extender) of this page for the given project variant and language.
	 * <p>
	 * FTP Server, determined by the publishing target, is not included.
	 * 
	 * @see #getPublishedPath(String, String, String)
	 */
	public String getPublishedPath(ProjectVariant projectVariant, LanguageVariant languageVariant, String folderSeparator)
			throws RQLException {
		return getPublishedPath(projectVariant.getProjectVariantGuid(), languageVariant.getRfcLanguageId(), folderSeparator);
	}

	/**
	 * Returns the published path (incl. the filename and extender) of this page for the given project variant and language.
	 * <p>
	 * Attention: This method is quite slow, because of {@link #getPublicationPackage()} and {@link #getPublishedFilename(String)}.
	 * <p>
	 * FTP Server, determined by the publishing target, is not included.
	 * 
	 * @param rfcLanguageId
	 *            determine the language variant by RFC code, like en,de,zh
	 * @param folderSeparator
	 *            separates the folder names in the returned path; usually \ or /
	 * @see #getFilename()
	 * @see #getPublishedFilename(String)
	 */
	public String getPublishedPath(String projectVariantGuid, String rfcLanguageId, String folderSeparator) throws RQLException {
		PublicationPackage publicationPackage = getPublicationPackage();
		PublicationSetting setting = publicationPackage.findSettingByProjectVarianGuidAndRfcLanguageId(projectVariantGuid,
				rfcLanguageId);
		PublicationFolder publicationFolder = setting.getPublishedPages();
		return publicationFolder.getPublishingPathFromPublishingRoot(folderSeparator) + folderSeparator
				+ getPublishedFilename(projectVariantGuid);
	}

	/**
	 * Returns the published path (incl. the filename and extender) of this page for the given project variant and language.
	 * <p>
	 * Attention: There is no check, if the given publication setting is one from the publication package of this' page.
	 * <p>
	 * FTP Server, determined by the publishing target, is not included.
	 * 
	 * @param publicationSetting
	 *            the publication setting from the publication package on the main link of this page for which the published path
	 *            should be determined; 
	 * @param folderSeparator
	 *            separates the folder names in the returned path; usually \ or /
	 * @see #getFilename()
	 * @see #getPublishedFilename(String)
	 */
	public String getPublishedPath(PublicationSetting publicationSetting, String folderSeparator) throws RQLException {
		PublicationFolder publicationFolder = publicationSetting.getPublishedPages();
		return publicationFolder.getPublishingPathFromPublishingRoot(folderSeparator) + folderSeparator
				+ getPublishedFilename(publicationSetting.getProjectVariantGuid());
	}

	/**
	 * Liefert für diese Seite die Page ID oder die GUID, je nach Einstellung der PublicationSettings.
	 */
	protected String getPublishedFilenameId() throws RQLException {
		Project project = getProject();
		if (project.isPublicationSettingUsePageIdInFilename()) {
			return getPageId();
		}
		if (project.isPublicationSettingUseGuidInFilename()) {
			return getPageGuid();
		}
		return null;
	}

	/**
	 * Liefert den Usernamen des Autors, der die Seite released hat. Falls diese Seite noch nicht released wurde, wird null
	 * zurückgegeben!
	 */
	public String getReleasedByUserName() throws RQLException {
		return getDetailsNode().getAttribute("releaseusername");
	}

	/**
	 * Liefert das Datum zurück, an dem diese Seite im Workflow freigegeben wurde. Falls diese Seite noch nicht released wurde, wird
	 * null zurückgegeben! Danach wird das Redlining Icon angezeigt.
	 */
	public ReddotDate getReleasedOn() throws RQLException {
		String released = getDetailsNode().getAttribute("releasedate");
		return released.equals("0") ? null : new ReddotDate(released);
	}

	/**
	 * Liefert das Freigabedatum im Format 29 Aug 2009 zurück, an dem diese Seite im Workflow freigegeben wurde.
	 * <p>
	 * Falls diese Seite noch nicht released wurde, wird 'never' zurückgegeben!
	 */
	public String getReleasedOnAsddMMyyyy() throws RQLException {
		ReddotDate released = getReleasedOn();
		return released == null ? "never" : released.getAsddMMyyyy();
	}

	/**
	 * Liefert das Freigabedatum im Format 29 Aug 2009 6:15 pm zurück, an dem diese Seite im Workflow freigegeben wurde.
	 * <p>
	 * Falls diese Seite noch nicht released wurde, wird 'never' zurückgegeben!
	 */
	public String getReleasedOnAsddMMyyyyHmma() throws RQLException {
		ReddotDate released = getReleasedOn();
		return released == null ? "never" : released.getAsddMMyyyyHmma();
	}
	
	
	/**
	 * Eine "redirect"-Seite ist in Wirklichkeit nur eine Mini-Seite (ohne Template, Workflow etc)
	 */
	public boolean isRedirect() throws RQLException {
		return "1".equals(getDetailsNode().getAttribute("redirect"));
	}

	
	/**
	 * Redirect: The target frame.
	 * @return possibly null.
	 */
	public String getRedirectTarget() throws RQLException {
		return getDetailsNode().getAttribute("target");
	}
	
	
	public String getRedirectUrl() throws RQLException {
		return urlNode.getAttribute("src");
	}
	
	
	/**
	 * Liefert den RedDot Session key.
	 * 
	 */
	public String getSessionKey() {

		return getProject().getSessionKey();
	}

	/**
	 * Liefert Standardfeld Date Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 5 sein
	 */
	public StandardFieldDateElement getStandardFieldDateElement(String templateElementName) throws RQLException {

		return getStandardFieldDateElement(getTemplateElementByName(templateElementName));
	}

	/**
	 * Liefert Standardfeld E-Mail Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 50 sein
	 */
	public StandardFieldEmailElement getStandardFieldEmailElement(String templateElementName) throws RQLException {

		return getStandardFieldEmailElement(getTemplateElementByName(templateElementName));
	}

	public StandardFieldUrlElement getStandardFieldUrlElement(String templateElementName) throws RQLException {

		return getStandardFieldUrlElement(getTemplateElementByName(templateElementName));
	}

	/**
	 * Liefert Standardfeld Date Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElement
	 *            muss vom Typ 5 sein
	 */
	private StandardFieldDateElement getStandardFieldDateElement(TemplateElement templateElement) throws RQLException {

		// check type of template element
		if (!templateElement.isStandardFieldDate()) {
			throw new WrongTypeException("Template element " + templateElement.getName() + " is not of type Standard-Field Date.");
		}

		// call CMS
		RQLNode elementNode = findElementNode(templateElement);

		// wrap page element data
		return new StandardFieldDateElement(this, templateElement, elementNode.getAttribute("name"), elementNode.getAttribute("guid"),
				elementNode.getAttribute("value"));
	}

	/**
	 * Liefert Standardfeld E-Mail Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElement
	 *            muss vom Typ 50 sein
	 */
	private StandardFieldEmailElement getStandardFieldEmailElement(TemplateElement templateElement) throws RQLException {

		// check type of template element
		if (!templateElement.isStandardFieldEmail()) {
			throw new WrongTypeException("Template element " + templateElement.getName() + " is not of type Standard-Field E-Mail.");
		}

		// call CMS
		RQLNode elementNode = findElementNode(templateElement);

		// wrap page element data
		return new StandardFieldEmailElement(this, templateElement, elementNode.getAttribute("name"), elementNode.getAttribute("guid"),
				elementNode.getAttribute("value"));
	}

	/**
	 * Standardfeld URL.
	 * 
	 * @param templateElement muss vom Typ 51 sein.
	 */
	private StandardFieldUrlElement getStandardFieldUrlElement(TemplateElement templateElement) throws RQLException {

		// check type of template element
		if (!templateElement.isStandardFieldUrl()) {
			throw new WrongTypeException("Template element " + templateElement.getName() + " is not of type Standard-Field URL.");
		}

		// call CMS
		RQLNode elementNode = findElementNode(templateElement);

		// wrap page element data
		return new StandardFieldUrlElement(this, templateElement, elementNode.getAttribute("name"), elementNode.getAttribute("guid"),
				elementNode.getAttribute("value"));
	}

	
	/**
	 * Liefert den Wert des Standardfeld Date Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 5 sein
	 */
	public ReddotDate getStandardFieldDateValue(String templateElementName) throws RQLException {

		return getStandardFieldDateElement(templateElementName).getDate();
	}

	/**
	 * Liefert den Wert des Standardfeld Date Elements dieser Seite, das auf dem gegebenen templateElement basiert in der Formatierung
	 * 11 Sep 2006.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 5 sein
	 */
	public String getStandardFieldDateValueFormatted(String templateElementName) throws RQLException {

		return getStandardFieldDateElement(templateElementName).getDateFormatted();
	}

	/**
	 * Liefert den Wert des Standardfeld Date Elements dieser Seite, das auf dem gegebenen templateElement basiert in der gegebenen
	 * Formatierung.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 5 sein
	 */
	public String getStandardFieldDateValueFormatted(String templateElementName, String formatPattern) throws RQLException {

		return getStandardFieldDateElement(templateElementName).getDateFormatted(formatPattern);
	}

	/**
	 * Liefert Standardfeld Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 1 sein
	 */
	public StandardFieldNumericElement getStandardFieldNumericElement(String templateElementName) throws RQLException {

		return getStandardFieldNumericElement(getTemplateElementByName(templateElementName));
	}

	/**
	 * Liefert Standardfeld Numeric dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElement
	 *            muss vom Typ 48 sein
	 */
	private StandardFieldNumericElement getStandardFieldNumericElement(TemplateElement templateElement) throws RQLException {

		// check type of template element
		if (!templateElement.isStandardFieldNumeric()) {
			throw new WrongTypeException("Template element " + templateElement.getName() + " is not of type Standard-Field Numeric.");
		}

		// call CMS
		RQLNode elementNode = findElementNode(templateElement);

		// wrap page element data
		// add templateElement too?
		return new StandardFieldNumericElement(this, templateElement, elementNode.getAttribute("name"), elementNode
				.getAttribute("guid"), elementNode.getAttribute("value"));
	}

	/**
	 * Liefert den Wert des Standardfeld Numeric Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 48 sein
	 */
	public int getStandardFieldNumericValue(String templateElementName) throws RQLException {

		return getStandardFieldNumericElement(templateElementName).getInt();
	}

	/**
	 * Liefert Standardfeld Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 1 sein
	 */
	public StandardFieldTextElement getStandardFieldTextElement(String templateElementName) throws RQLException {

		return getStandardFieldTextElement(getTemplateElementByName(templateElementName));
	}

	/**
	 * Liefert Standardfeld Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElement
	 *            muss vom Typ 1 sein
	 */
	private StandardFieldTextElement getStandardFieldTextElement(TemplateElement templateElement) throws RQLException {

		// check type of template element
		if (!templateElement.isStandardFieldText()) {
			throw new WrongTypeException("Template element " + templateElement.getName() + " is not of type Standard-Field Text.");
		}

		// call CMS
		RQLNode elementNode = findElementNode(templateElement);

		// wrap page element data
		return new StandardFieldTextElement(this, templateElement, elementNode.getAttribute("name"), elementNode.getAttribute("guid"),
				elementNode.getAttribute("value"));
	}

	/**
	 * Liefert eine Liste mit allen Standardfeld Textelementen dieser Seite, gefüllt oder nicht spielt keine Rolle. Orientiert sich an
	 * der Seite, nicht am Template. Auch werden neue, noch nicht aktivierte, Elemente nicht gefunden.
	 */
	public java.util.List<StandardFieldTextElement> getStandardFieldTextElements() throws RQLException {
		RQLNodeList nodes = getElementNodeList();

		// for all template elements do
		java.util.List<StandardFieldTextElement> elements = new ArrayList<StandardFieldTextElement>();
		for (int i = 0; i < nodes.size(); i++) {
			RQLNode node = (RQLNode) nodes.get(i);
			// skip not standard field text elements
			if (Integer.parseInt(node.getAttribute("type")) != TemplateElement.STANDARDFIELD_TEXT_TYPE) {
				continue;
			}
			elements.add(new StandardFieldTextElement(this, getTemplateElementByGuid(node.getAttribute("templateelementguid")), node
					.getAttribute("name"), node.getAttribute("guid"), node.getAttribute("value")));
		}
		return elements;
	}

	/**
	 * Liefert eine Liste mit Standardfeld Textelements dieser Seite, deren Templatename mit dem gegebenen Suffix endet. Orientiert
	 * sich am Template, so dass auch Elemente mit Form=false geliefert werden sollten. Nicht aktivierte Elemente erzeugen eine
	 * <code>NewElementNotRefreshedException</code>.
	 */
	public java.util.List<StandardFieldTextElement> getStandardFieldTextElementsBySuffix(String templateNameSuffix) throws RQLException {

		java.util.List templateElements = getTemplate().getTemplateElementsBySuffix(templateNameSuffix);

		// for all template elements do
		java.util.List<StandardFieldTextElement> elements = new ArrayList<StandardFieldTextElement>();
		for (int i = 0; i < templateElements.size(); i++) {
			TemplateElement templateElement = (TemplateElement) templateElements.get(i);
			// use only standard field text elements
			if (!templateElement.isStandardFieldText()) {
				continue;
			}
			RQLNode elementNode = findElementNode(templateElement);
			elements.add(new StandardFieldTextElement(this, templateElement, elementNode.getAttribute("name"), elementNode
					.getAttribute("guid"), elementNode.getAttribute("value")));
		}
		return elements;
	}

	/**
	 * Liefert den Wert des Standardfeld Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 1 sein
	 */
	public String getStandardFieldTextValue(String templateElementName) throws RQLException {

		return getStandardFieldTextElement(templateElementName).getText();
	}

	/**
	 * Liefert Standardfeld user defined elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 999 sein
	 */
	public StandardFieldUserDefinedElement getStandardFieldUserDefinedElement(String templateElementName) throws RQLException {

		return getStandardFieldUserDefinedElement(getTemplateElementByName(templateElementName));
	}

	/**
	 * Liefert Standardfeld user defined elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElement
	 *            muss vom Typ 999 sein
	 */
	private StandardFieldUserDefinedElement getStandardFieldUserDefinedElement(TemplateElement templateElement) throws RQLException {

		// check type of template element
		if (!templateElement.isStandardFieldUserDefined()) {
			throw new WrongTypeException("Template element " + templateElement.getName()
					+ " is not of type Standard-Field user defined.");
		}

		// call CMS
		RQLNode elementNode = findElementNode(templateElement);

		// wrap page element data
		return new StandardFieldUserDefinedElement(this, templateElement, elementNode.getAttribute("name"), elementNode
				.getAttribute("guid"), elementNode.getAttribute("value"));
	}

	/**
	 * Liefert den Wert des Standardfeld user defined elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 999 sein
	 */
	public String getStandardFieldUserDefinedValue(String templateElementName) throws RQLException {

		return getStandardFieldUserDefinedElement(templateElementName).getText();
	}

	/**
	 * Liefert einen Text, der den Status dieser Seite beschreibt. Der Translation Editor status waiting to be translated wird
	 * angehängt.
	 */
	public String getStateInfo() throws RQLException {

		String result = getDetailsStateFlag().toString();
		if (isInStateReleased()) {
			result = "released";
		} else if (isInStateSavedAsDraftNew()) {
			result = "saved as draft (new)";
		} else if (isInStateSavedAsDraftChanged()) {
			result = "saved as draft (changed)";
		} else if (isInStateWaitingForCorrection()) {
			result = "waiting for correction";
		} else if (isInStateWaitingForRelease()) {
			result = "waiting for release";
		}
		
		try {
			// TODO check, if Translation Editor licencse is available
			if (isInStateWaitingToBeTranslated()) {
				result += ", waiting to be translated";
			}
		} catch (RQLException e) {
			result += " (translation state unavailabe, CMS bug?)"; 
		}
		return result;
	}

	/**
	 * Liefert das Template auf dem diese Seite basiert.
	 */
	public Template getTemplate() throws RQLException {

		if (template == null) {
			template = getProject().getTemplateByGuid(getDetailsTemplateGuid());
		}

		return template;
	}

	/**
	 * Liefert das TemplateElement aus dem Template auf dem diese Seite basiert.
	 */
	public TemplateElement getTemplateElementByGuid(String templateElementGuid) throws RQLException {

		return getTemplate().getTemplateElementByGuid(templateElementGuid);
	}

	/**
	 * Liefert das TemplateElement aus dem Template auf dem diese Seite basiert.
	 */
	public TemplateElement getTemplateElementByName(String templateElementName) throws RQLException {

		return getTemplate().getTemplateElementByName(templateElementName);
	}

	/**
	 * Liefert die GUID des Folders des Templates, auf dem diese Seite basiert.
	 */
	public String getTemplateFolderGuid() throws RQLException {

		return getTemplate().getTemplateFolderGuid();
	}

	/**
	 * Liefert den Namen des Folders des Templates, auf dem diese Seite basiert.
	 */
	public String getTemplateFolderName() throws RQLException {

		return getTemplate().getTemplateFolderName();
	}

	/**
	 * Liefert die GUID des Templates auf dem diese Seite basiert.
	 */
	public String getTemplateGuid() throws RQLException {

		return getTemplate().getTemplateGuid();
	}

	/**
	 * Liefert true, falls die gegebenen content class GUID gleich des Templates dieser Seite ist.
	 */
	public boolean isTemplateGuidEquals(String templateGuid) throws RQLException {
		return getTemplateGuid().equals(templateGuid);
	}

	/**
	 * Liefert true, falls der Name des content class folders des Templates dieser Seite den gegebenen namePart beinhaltet, case
	 * sensitive.
	 */
	public boolean isTemplateFolderNameContains(String namePart) throws RQLException {
		return getTemplate().isTemplateFolderNameContains(namePart);
	}

	/**
	 * Liefert true, falls der Name der content class dieser Seite auf den gegebenen suffix endet, case sensitive.
	 */
	public boolean isTemplateNameEndsWith(String suffix) throws RQLException {
		return getTemplate().isNameEndsWith(suffix);
	}

	/**
	 * Liefert true, falls der Name des content class folders des Templates dieser Seite den gegebenen namePart beinhaltet.
	 */
	public boolean isTemplateFolderNameContains(String namePart, boolean ignoreCase) throws RQLException {
		return getTemplate().isTemplateFolderNameContains(namePart, ignoreCase);
	}

	/**
	 * Liefert den Namen des Templates auf diese Seite basiert.
	 */
	public String getTemplateName() throws RQLException {

		return getTemplate().getName();
	}

	/**
	 * Liefert den Textanker dieser Seite, der auf dem Template-Element mit dem gegebenen Namen basiert.
	 * 
	 * @param textAnchorTemplateElementName
	 *            TemplateElement muss vom Typ 26 (Anchor Text) sein.
	 * @return <code>TextAnchor</code>
	 * @see <code>TextAnchor</code>
	 */
	public TextAnchor getTextAnchor(String textAnchorTemplateElementName) throws RQLException {

		return getTextAnchor(getTemplateElementByName(textAnchorTemplateElementName));
	}

	/**
	 * Liefert den Textanker aus dieser Seite, der auf dem gegebenen templateElement basiert.
	 * 
	 * @param textAnchorTemplateElement
	 *            muss vom Typ 26 (Anchor Text) sein.
	 * @return <code>TextAnchor</code>
	 * @see <code>TextAnchor</code>
	 */
	private TextAnchor getTextAnchor(TemplateElement textAnchorTemplateElement) throws RQLException {

		// check type of template element
		if (!textAnchorTemplateElement.isTextAnchor()) {
			throw new WrongTypeException("Template element " + textAnchorTemplateElement.getName()
					+ " is not a normal text anchor. Maybe it is a dynamic text anchor.");
		}

		// call CMS
		RQLNode anchorNode = findLinkNode(textAnchorTemplateElement);

		// note: attribute "value" contains local name
		//return new TextAnchor(this, textAnchorTemplateElement, anchorNode.getAttribute("name"), anchorNode.getAttribute("guid"), anchorNode.getAttribute("islink").equals("10"));
		return new TextAnchor(this, textAnchorTemplateElement, anchorNode);
	}

	/**
	 * Liefert Textelement dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 32 sein
	 */
	public TextElement getTextElement(String templateElementName) throws RQLException {

		return getTextElement(getTemplateElementByName(templateElementName));
	}

	/**
	 * Liefert das Textelement dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElement
	 *            muss vom Typ 31 sein
	 */
	private TextElement getTextElement(TemplateElement templateElement) throws RQLException {

		// check type of template element
		if (!templateElement.isAsciiText() && !templateElement.isHtmlText()) {
			throw new WrongTypeException("Template element " + templateElement.getName()
					+ " is neither an ASCII text nor a HTML text.");
		}

		// call CMS
		RQLNode elementNode = findElementNode(templateElement);

		// wrap page element data
		return new TextElement(this, templateElement, elementNode.getAttribute("name"), elementNode.getAttribute("guid"));
	}

	/**
	 * Liefert eine Liste mit allen Textelementen dieser Seite; unabhängig ob diese einen Wert enhalten oder nicht. Orientiert sich an
	 * der Seite, nicht am Template. Auch werden neue, noch nicht aktivierte, Elemente nicht gefunden.
	 * 
	 * @see #getFilledTextElements()
	 */
	public java.util.List<TextElement> getTextElements() throws RQLException {
		return getTextElements(true, true, false);
	}

	/**
	 * Liefert eine Liste mit allen Textelementen dieser Seite entsprechend der gegebenen Parameter. Orientiert sich an der Seite,
	 * nicht am Template. Auch werden neue, noch nicht aktivierte, Elemente nicht gefunden.
	 * 
	 * @param includeAscii
	 *            if true, add ascii text elements to returned list
	 * @param includeHtml
	 *            if true, add html text elements to returned list
	 * @param onlyFilled
	 *            if true, add only ascii or html text elements with content (skip empty ones)
	 */
	private java.util.List<TextElement> getTextElements(boolean includeAscii, boolean includeHtml, boolean onlyFilled)
			throws RQLException {

		RQLNodeList nodes = getElementNodeList();

		// for all template elements do
		java.util.List<TextElement> elements = new ArrayList<TextElement>();
		if (nodes != null) {
			for (int i = 0; i < nodes.size(); i++) {
				RQLNode node = (RQLNode) nodes.get(i);
				// get attributes
				int type = Integer.parseInt(node.getAttribute("type"));
				String templateElemGuid = node.getAttribute("templateelementguid");
				String elemName = node.getAttribute("name");
				String elemGuid = node.getAttribute("guid");
				String value = node.getAttribute("value");
				// check ascii elements
				if (includeAscii && type == TemplateElement.TEXT_ASCII_TYPE && onlyFilled && value.length() > 0) {
					elements.add(new TextElement(this, getTemplateElementByGuid(templateElemGuid), elemName, elemGuid));
				}
				// check html elements
				if (includeHtml && type == TemplateElement.TEXT_HTML_TYPE && onlyFilled && value.length() > 0) {
					elements.add(new TextElement(this, getTemplateElementByGuid(templateElemGuid), elemName, elemGuid));
				}
			}
		}
		return elements;
	}

	/**
	 * Liefert den Wert des Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 31 sein
	 */
	public String getTextValue(String templateElementName) throws RQLException {

		return getTextElement(templateElementName).getText();
	}

	/**
	 * Liefert den Workflow, der am MainLink dieser Seite definiert ist.
	 * <p>
	 * Liefert null, falls an dieser Seite kein Workflow hängt.
	 * <p>
	 */
	public Workflow getWorkflow() throws RQLException {
		RQLNode mainLinkNode = getExtendedDetails("MAINLINK");
		// no main link = no workflow
		if (mainLinkNode == null) {
			return null;
		}
		// no workflow name
		String wfName = mainLinkNode.getAttribute("wkfname");
		if (wfName == null) {
			return null;
		}
		return getProject().getWorkflowByName(wfName);
	}

	/**
	 * Liefert true genau dann, wenn der Container mit dem gegebenen Templatenamen dieser Seite Kindseiten besitzt.
	 * 
	 * @param containerTemplateElementName
	 *            Name des Containers im Template
	 */
	public boolean hasContainerChildPages(String containerTemplateElementName) throws RQLException {

		return getContainer(containerTemplateElementName).hasChildPages();
	}

	/**
	 * Liefert true genau dann, wenn der User, der diese Seite erstellt hat, noch existiert, sonst false.
	 */
	public boolean hasCreatedUser() throws RQLException {
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		String unknownAuthorDe = b.getString("unknownAuthorDe");
		String unknownAuthorEn = b.getString("unknownAuthorEn");
		String name = getCreatedByUserName();

		return name.equalsIgnoreCase(unknownAuthorDe) || name.equalsIgnoreCase(unknownAuthorEn);
	}

	/**
	 * Liefert true, falls diese Seite ein sql statement besitzt, sonst false.
	 */
	public boolean hasDatabaseQuery() throws RQLException {
		return getDatabaseQueryPrim() != null;
	}

	/**
	 * Liefert true, falls diese Seite einen Dateinamen ungleich null und length > 0 besitzt.
	 * <p>
	 */
	public boolean hasFilename() throws RQLException {
		String filename = getFilename();
		return filename != null && filename.length() > 0;
	}

	/**
	 * Returns a hash code value for the object. This method is supported for the benefit of hashtables such as those provided by
	 * <code>java.util.Hashtable</code>.
	 * <p>
	 * The general contract of <code>hashCode</code> is:
	 * <ul>
	 * <li>Whenever it is invoked on the same object more than once during an execution of a Java application, the <tt>hashCode</tt>
	 * method must consistently return the same integer, provided no information used in <tt>equals</tt> comparisons on the object is
	 * modified. This integer need not remain consistent from one execution of an application to another execution of the same
	 * application.
	 * <li>If two objects are equal according to the <tt>equals(Object)</tt> method, then calling the <code>hashCode</code> method
	 * on each of the two objects must produce the same integer result.
	 * <li>It is <em>not</em> required that if two objects are unequal according to the
	 * {@link java.lang.Object#equals(java.lang.Object)} method, then calling the <tt>hashCode</tt> method on each of the two objects
	 * must produce distinct integer results. However, the programmer should be aware that producing distinct integer results for
	 * unequal objects may improve the performance of hashtables.
	 * </ul>
	 * <p>
	 * As much as is reasonably practical, the hashCode method defined by class <tt>Object</tt> does return distinct integers for
	 * distinct objects. (This is typically implemented by converting the internal address of the object into an integer, but this
	 * implementation technique is not required by the Java<font size="-2"><sup>TM</sup></font> programming language.)
	 * 
	 * @return a hash code value for this object.
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @see java.util.Hashtable
	 */
	public int hashCode() {

		return getPageGuid().hashCode();
	}

	/**
	 * Liefert true genau dann, wenn diese Seite eine Überschrift hat. Liefert false für alle '{GUID: ...} Seiten.
	 */
	public boolean hasHeadline() throws RQLException {

		String headline = getHeadline();
		return headline.length() != 0 && !headline.startsWith("{GUID:");
	}

	/**
	 * Liefert true genau dann, wenn die Liste mit dem gegebenen Templatenamen dieser Seite Kindseiten besitzt.
	 * 
	 * @param listTemplateElementName
	 *            Name der Liste im Template
	 */
	public boolean hasListChildPages(String listTemplateElementName) throws RQLException {

		return getList(listTemplateElementName).hasChildPages();
	}

	/**
	 * Liefert true, falls diese Seite ein AdministrationsElement im Template besitzt.
	 * 
	 * @param administrationElementName
	 *            Name des Administrations-Elementes aus dem Template
	 */
	public boolean hasNote(String administrationElementName) throws RQLException {

		return getTemplate().hasAdministrationElement(administrationElementName);
	}

	/**
	 * Liefert die Anzahl der aller Links, an der diese Seite verlinkt ist (connected to).
	 * <p>
	 * Wird 0 geliefert, ist diese Seite unverlinkt, frei.
	 */
	public int howOftenIsThisPageConnected() throws RQLException {

		RQLNodeList result = getLinkedFromNodeList();
		if (result == null) {
			return 0;
		}
		return result.size();
	}

	/**
	 * Liefert true genau dann, wenn diese Seite auf einem der gegebenen Templatenamen basiert.
	 * 
	 * @param listOfTemplateNames
	 *            liste von templatenamen, getrennt mit separator: text_row;faq_section;gateway_section
	 * @param separator
	 *            Trenner der Liste: ;
	 */
	public boolean isBasedOnOneTemplateOf(String listOfTemplateNames, String separator) throws RQLException {

		java.util.List list = StringHelper.split(listOfTemplateNames, separator.charAt(0));

		return list.contains(getTemplateName());
	}

	/**
	 * Liefert true genau dann, wenn das Template auf dem diese Seite basiert den Namen templateName hat.
	 * 
	 * @param templateName
	 *            Name eines Templates
	 */
	public boolean isBasedOnTemplate(String templateName) throws RQLException {

		return getTemplateName().equals(templateName);
	}

	/**
	 * Liefert true, falls diese Seite durch den angegebenen User änderbar ist. Liefert true, falls diese Seite freigegeben ist.
	 * Liefert true, falls diese Seite im Draft oder auf Korrektur des angemeldeten Users steht.
	 * <p>
	 * Achtung: Berücksichtigt nicht, ob diese Seite durch einen anderen Benutzer gerade gesperrt ist.
	 * 
	 * @see #isLocked()
	 */
	public boolean isChangeable() throws RQLException {

		if (isInStateReleased()) {
			return true;
		} else if (isInStateSavedAsDraft() || isInStateWaitingForCorrection()) {
			return getCmsClient().getConnectedUser().getName().equals(getLastChangedByUserName());
		}
		return false;
	}

	/**
	 * Liefert true, falls diese Seite in allen gegebenen Sprachvarianten durch den angegebenen User änderbar ist. Liefert true, falls
	 * diese Seite freigegeben ist. Liefert true, falls diese Seite im Draft oder auf Korrektur des angemeldeten Users steht.
	 * <p>
	 * Achtung: Berücksichtigt nicht, ob diese Seite durch einen anderen Benutzer gerade gesperrt ist.
	 * 
	 * @see #isChangeable()
	 */
	public boolean isChangeable(java.util.List<LanguageVariant> languageVariants) throws RQLException {
		LanguageVariant currentLv = getProject().getCurrentLanguageVariant();
		try {
			for (LanguageVariant lv : languageVariants) {
				project.setCurrentLanguageVariant(lv);
				if (!isChangeable()) {
					return false;
				}
			}
		} finally {
			// restore lv
			getProject().setCurrentLanguageVariant(currentLv);
		}
		return true;
	}

	/**
	 * Liefert true, falls die Überschrift dieser Seite mit dem gegebenen Suffix endet.
	 */
	public boolean isHeadlineEndsWith(String suffix) throws RQLException {

		return getHeadline().endsWith(suffix);
	}

	/**
	 * Liefert true, falls das Image Element dieser Seite, das auf dem gegebenen templateElementName basiert, auf ein anderes Element
	 * referenziert, also dessen Wert nutzt.
	 * 
	 * @param templateElementName
	 *            das TemplateElement muss vom Typ = 2 sein
	 */
	public boolean isImageElementReferenceSource(String templateElementName) throws RQLException {
		TemplateElement templateElement = getTemplateElementByName(templateElementName);
		// check type of template element
		if (!templateElement.isImage()) {
			throw new WrongTypeException("Template element " + templateElement.getName() + " is not of type Image.");
		}

		// call CMS
		RQLNode elementNode = findElementNode(templateElement);

		// TODO sollte eigentlich in Element implementiert werden; über den konstruktur mitgeben
		return elementNode.getAttribute("refelementguid") != null;
	}

	/**
	 * Liefert true, falls das Imageelement leer ist, das auf dem gegebenen templateElement basiert.
	 */
	public boolean isImageEmpty(String templateElementName) throws RQLException {

		return getImageElement(templateElementName).isEmpty();
	}

	/**
	 * Liefert true, falls der Benutzer in dieses Imageelement etwas eingegeben hat.
	 */
	public boolean isImageValueEntered(String templateElementName) throws RQLException {

		return getImageElement(templateElementName).isValueEntered();
	}

	/**
	 * Liefert true genau dann, wenn diese Seite im Papierkorb enthalten ist, sonst false.
	 */
	public boolean isInRecycleBin() throws RQLException {

		return getProject().getRecycleBin().containsPageById(getPageId());
	}

	/**
	 * Returns true, if this page exists in the current language variant.
	 * <p>
	 * Returns false, if this page does not exists in the language variant so far.
	 */
	public boolean existsInCurrentLanguageVariant() throws RQLException {
		String na = getDetailsNode().getAttribute("notavailable");
		if (na == null) {
			return true;
		}
		return na.equals("0");
	}

	/**
	 * Liefert true, falls diese Seite freigegeben ist.
	 */
	public boolean isInStateReleased() throws RQLException {

		return getDetailsStateFlag().testBit(PAGE_STATE_RELEASED_BIT_INDEX);
	}

	/**
	 * Liefert true, falls diese Seite im Status draft ist (neu erstellt oder geändert).
	 * 
	 * Dieser Test wirkt global. Er liefert auch true, falls die Seite bei einem anderen als dem gerade angemeldeten in draft steht.
	 * 
	 * @see User#getPagesSavedAsDraft(Project)
	 */
	public boolean isInStateSavedAsDraft() throws RQLException {

		return getDetailsStateFlag().testBit(PAGE_STATE_SAVED_AS_DRAFT_BIT_INDEX);
	}

	/**
	 * Liefert true, falls diese nach mind. einer Freigabe im Workflow nochmals geändert wurde. In diesem Status wird auch das
	 * RedLining Icon angezeigt.
	 * 
	 * Dieser Test wirkt global. Er liefert auch true, falls die Seite bei einem anderen als dem gerade angemeldeten in draft steht.
	 * 
	 * @see User#getPagesSavedAsDraft(Project)
	 */
	public boolean isInStateSavedAsDraftChanged() throws RQLException {

		return isInStateSavedAsDraft() && getReleasedOn() != null;
	}

	/**
	 * Liefert true, falls diese Seite neu erstellt wurde und noch im Status draft ist. D.h. diese Seite wurde noch niemals im Workflow
	 * freigegeben.
	 * 
	 * Dieser Test wirkt global. Er liefert auch true, falls die Seite bei einem anderen als dem gerade angemeldeten in draft steht.
	 * 
	 * @see User#getPagesSavedAsDraft(Project)
	 */
	public boolean isInStateSavedAsDraftNew() throws RQLException {

		return isInStateSavedAsDraft() && getReleasedOn() == null;
	}

	/**
	 * Liefert true, falls diese Seite auf Korrektur wartet.
	 * 
	 * Dieser Test wirkt global. Er liefert auch true, falls die Seite bei einem anderen als dem gerade angemeldeten steht.
	 * 
	 * @see User#getPagesWaitingForCorrection(Project)
	 */
	public boolean isInStateWaitingForCorrection() throws RQLException {

		return getDetailsStateFlag().testBit(PAGE_STATE_WAITING_FOR_CORRECTION_BIT_INDEX);
	}

	/**
	 * Liefert true, falls diese Seite auf Freigabe wartet.
	 * 
	 * Dieser Test wirkt global. Er liefert auch true, falls die Seite bei einem anderen als dem gerade angemeldeten steht.
	 * 
	 * @see User#getPagesWaitingForRelease(Project)
	 */
	public boolean isInStateWaitingForRelease() throws RQLException {
		return getDetailsStateFlag().testBit(PAGE_STATE_WAITING_FOR_RELEASE_BIT_INDEX);
	}

	/**
	 * Liefert true, falls diese Seite auf eine Übersetzung in der aktuellen Sprachvariante wartet. Dieser Test wirkt global.
	 * <p>
	 * Unklar, wie hierbei die Kombination der von/nach Sprachvariante Einfluss hat.
	 * <p>
	 * Waiting for translation kann zusätzlich zu state released auftreten.
	 * 
	 * @see #isInStateReleased()
	 */
	public boolean isInStateWaitingToBeTranslated() throws RQLException {
		return getCmsClient().getConnectedUser().getPagesWaitingForTranslation(getProject()).contains(this);
	}

	/**
	 * Liefert den String 'true', falls diese Seite auf eine Übersetzung in der aktuellen Sprachvariante wartet. Dieser Test wirkt
	 * global.
	 * <p>
	 * Unklar, wie hierbei die Kombination der von/nach Sprachvariante Einfluss hat.
	 * <p>
	 * Waiting for translation kann zusätzlich zu state released auftreten.
	 * 
	 * @see #isInStateWaitingToBeTranslated()
	 */
	public String isInStateWaitingToBeTranslatedStr() throws RQLException {
		return StringHelper.convertToString(isInStateWaitingToBeTranslated());
	}

	/**
	 * Liefert true, falls diese Seite sprachvariantenabhängig ist. D.h. mindestens ein Kontent-Element (inkl. Headline, aber ohne
	 * referenced elements) des Templates sprachvariantenabhängig ist, sonst false.
	 */
	public boolean isLanguageVariantDependent() throws RQLException {
		return getTemplate().isLanguageVariantDependent();
	}

	/**
	 * Liefert true, falls diese Seite sprachvariantenUNabhängig ist. D.h. alle Kontent-Elemente des Templates (inkl. Headline, aber
	 * ohne referenced elements) sprachvariantenUNabhängig sind, sonst false.
	 */
	public boolean isLanguageVariantIndependent() throws RQLException {
		return getTemplate().isLanguageVariantIndependent();
	}

	/**
	 * Liefert true, gdw die gegebenen Seite an mindestens einem Link hängt.
	 */
	public boolean isLinked() throws RQLException {

		return !isUnlinked();
	}

	/**
	 * Liefert true, falls diese Seite gerade von einem anderen (als dem angemeldeten Benutzer) gesperrt ist.
	 * <p>
	 * Nur falls true geliefert wird, liefern die folgenden Methoden von wem und seit wann die Sperre besteht.
	 * 
	 * @see #getLockedByUser()
	 * @see #getLockedByUserEmailAddress()
	 * @see #getLockedByUserName()
	 * @see #getLockedSince()
	 * @see #getLockedSinceAsddMMyyyyHmma()
	 */
	public boolean isLocked() throws RQLException {

		return getDetailsStateFlag().testBit(PAGE_STATE_LOCKED_BIT_INDEX)
				&& !getLockedByUserName().equals(getCmsClient().getConnectedUser().getName());
	}

	/**
	 * Liefert true, gdw diese Seite die Elternseite zur gegebenen Seite über den Hauptlink ist.
	 */
	public boolean isMainLinkChild(Page child) throws RQLException {

		return child.getMainLinkParentPage().equals(this);
	}

	/**
	 * Liefert true, gdw diese Seite die Kindseite zur gegebenen Eltern-Seite über den Hauptlink ist.
	 */
	public boolean isMainLinkParent(Page parent) throws RQLException {

		return getMainLinkParentPage().equals(parent);
	}

	/**
	 * Liefert true, falls das Mediaelement leer ist, das auf dem gegebenen templateElement basiert.
	 */
	public boolean isMediaEmpty(String templateElementName) throws RQLException {

		return getMediaElement(templateElementName).isEmpty();
	}

	/**
	 * Liefert true, falls der Benutzer in dieses Mediaelement etwas eingegeben hat.
	 */
	public boolean isMediaValueEntered(String templateElementName) throws RQLException {

		return getMediaElement(templateElementName).isValueEntered();
	}

	/**
	 * Liefert true genau dann zurück, wenn diese Seite mehrfach verlinkt ist.
	 */
	public boolean isMultiLinked() throws RQLException {

		RQLNodeList result = getLinkedFromNodeList();
		if (result == null) {
			return false;
		}
		return result.size() > 1;
	}

	/**
	 * Liefert true, falls diese Seite die Startseite des Projektes ist. Man muss kein Administrator sein, um diese Info zu erhalten.
	 */
	public boolean isProjectStartPage() throws RQLException {

		return getPageGuid().equals(getProject().getStartPage().getPageGuid());
	}

	/**
	 * Liefert true, gdw für die gegebene Seite ein publishing job gerade läuft.
	 * <p>
	 * D.h. dass für diese Seite kein neuer job per RQL erstellbar ist.
	 */
	public boolean isPublishingJobRunning() throws RQLException {

		return getProject().getRunningPublishingJobs().contains(this);
	}

	/**
	 * Liefert true, gdw für die gegebene Seite ein publishing job gerade läuft oder wartet.
	 * <p>
	 * D.h. dass für diese Seite kein neuer job per RQL erstellbar ist.
	 */
	public boolean isPublishingJobRunningOrWaiting() throws RQLException {
		// erst waiting, damit ein gerade von waiting auf running gewechselter job auch erfasst wird
		return isPublishingJobWaiting() || isPublishingJobRunning();
	}

	/**
	 * Liefert true, gdw für die gegebene Seite ein publishing job warted.
	 * <p>
	 * D.h. dass für diese Seite kein neuer job per RQL erstellbar ist.
	 */
	public boolean isPublishingJobWaiting() throws RQLException {

		return getProject().getWaitingPublishingJobs().contains(this);
	}

	/**
	 * Liefert true, falls das Standardfeld Date Element dieser Seite leer ist, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 5 sein
	 */
	public boolean isStandardFieldDateEmpty(String templateElementName) throws RQLException {

		return getStandardFieldDateElement(templateElementName).isEmpty();
	}

	/**
	 * Liefert true, falls der Benutzer in dieses Element etwas eingegeben hat.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 5 sein
	 */
	public boolean isStandardFieldDateValueEntered(String templateElementName) throws RQLException {

		return getStandardFieldDateElement(templateElementName).isValueEntered();
	}

	/**
	 * Liefert true, falls das Standardfeld Numeric Element dieser Seite leer ist, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 48 sein
	 */
	public boolean isStandardFieldNumericEmpty(String templateElementName) throws RQLException {

		return getStandardFieldNumericElement(templateElementName).isEmpty();
	}

	/**
	 * Liefert true, falls der Benutzer in dieses Element etwas eingegeben hat.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 48 sein
	 */
	public boolean isStandardFieldNumericValueEntered(String templateElementName) throws RQLException {

		return getStandardFieldNumericElement(templateElementName).isValueEntered();
	}

	/**
	 * Liefert true, falls das Standardfeld Textelement dieser Seite leer ist, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 1 sein
	 */
	public boolean isStandardFieldTextEmpty(String templateElementName) throws RQLException {

		return getStandardFieldTextElement(templateElementName).isEmpty();
	}

	/**
	 * Liefert true, falls der Benutzer in dieses Element etwas eingegeben hat.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 1 sein
	 */
	public boolean isStandardFieldTextValueEntered(String templateElementName) throws RQLException {

		return getStandardFieldTextElement(templateElementName).isValueEntered();
	}

	/**
	 * Liefert true, falls das Standardfeld user defined element dieser Seite leer ist, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 999 sein
	 */
	public boolean isStandardFieldUserDefinedEmpty(String templateElementName) throws RQLException {

		return getStandardFieldUserDefinedElement(templateElementName).isEmpty();
	}

	/**
	 * Liefert true, falls der Benutzer in dieses Element etwas eingegeben hat.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 999 sein
	 */
	public boolean isStandardFieldUserDefinedValueEntered(String templateElementName) throws RQLException {

		return getStandardFieldUserDefinedElement(templateElementName).isValueEntered();
	}

	/**
	 * Liefert true, falls das Textelement leer ist, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 31 sein
	 */
	public boolean isTextEmpty(String templateElementName) throws RQLException {

		return getTextElement(templateElementName).isEmpty();
	}

	/**
	 * Liefert true, falls der Benutzer in dieses Textelement etwas eingegeben hat.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 31 sein
	 */
	public boolean isTextValueEntered(String templateElementName) throws RQLException {

		return getTextElement(templateElementName).isValueEntered();
	}

	/**
	 * Liefert true, gdw die gegebenen Seite keinen Vorgänger (über MainLink) hat.
	 */
	public boolean isUnlinked() throws RQLException {

		return getLinkedFromNodeList() == null;
	}

	/**
	 * Startet eine Generierung beginnend bei dieser Seite für mehrere Projektvarianten.
	 * 
	 * @param withFollowingPages
	 *            nur diese Seite, oder auch folgende?
	 * @param withRelatedPages
	 *            auch related pages?
	 * @param projectVariantGuids
	 *            Liste der GUIDs
	 * @param separator
	 *            Trennzeichen für die GUIDs der Projektvarianten
	 * @param languageVariantGuid
	 *            GUID der Sprachvariante die generiert werden soll
	 * @see <code>PublishingJob</code>
	 */
	public PublishingJob publish(boolean withFollowingPages, boolean withRelatedPages, String projectVariantGuids, String separator,
			String languageVariantGuid) throws RQLException {

		PublishingJob publishingJob = new PublishingJob(this, withFollowingPages, withRelatedPages);
		publishingJob.addToPublish(projectVariantGuids, separator, languageVariantGuid);
		publishingJob.start();

		return publishingJob;
	}

	/**
	 * Startet eine Generierung beginnend bei dieser Seite für mehrere Projektvarianten.
	 * 
	 * @param withFollowingPages
	 *            nur diese Seite, oder auch folgende?
	 * @param withRelatedPages
	 *            auch related pages?
	 * @param projectVariantGuids
	 *            Liste der GUIDs
	 * @param separator
	 *            Trennzeichen für die GUIDs der Projektvarianten
	 * @param languageVariantGuid
	 *            GUID der Sprachvariante die generiert werden soll
	 * @param mailReceiver
	 *            User der nach jobende eine Mail erhalten soll
	 * @see <code>PublishingJob</code>
	 */
	public PublishingJob publish(boolean withFollowingPages, boolean withRelatedPages, String projectVariantGuids, String separator,
			String languageVariantGuid, User mailReceiver) throws RQLException {

		PublishingJob publishingJob = new PublishingJob(this, withFollowingPages, withRelatedPages);
		publishingJob.addToPublish(projectVariantGuids, separator, languageVariantGuid);
		publishingJob.setMailReceiver(mailReceiver);
		publishingJob.start();

		return publishingJob;
	}

	/**
	 * Startet eine Generierung beginnend bei dieser Seite.
	 * 
	 * @param withFollowingPages
	 *            nur diese Seite, oder auch folgende? (without related pages)
	 * @param projectVariant
	 *            Projektvariante die generiert werden soll
	 * @param languageVariant
	 *            Sprachvariante die generiert werden soll
	 * @see <code>PublishingJob</code>
	 */
	public PublishingJob publish(boolean withFollowingPages, ProjectVariant projectVariant, LanguageVariant languageVariant)
			throws RQLException {

		PublishingJob publishingJob = new PublishingJob(this, withFollowingPages);
		publishingJob.addToPublish(projectVariant, languageVariant);
		publishingJob.start();

		return publishingJob;
	}

	/**
	 * Startet eine Generierung beginnend bei dieser Seite.
	 * 
	 * @param withFollowingPages
	 *            nur diese Seite, oder auch folgende? (without related pages)
	 * @param projectVariantGuid
	 *            GUID der Projektvariante die generiert werden soll
	 * @param languageVariantGuid
	 *            GUID der Sprachvariante die generiert werden soll
	 * @see <code>PublishingJob</code>
	 */
	public PublishingJob publish(boolean withFollowingPages, String projectVariantGuid, String languageVariantGuid)
			throws RQLException {

		PublishingJob publishingJob = new PublishingJob(this, withFollowingPages);
		Project project = getProject();
		publishingJob.addToPublish(project.getProjectVariantByGuid(projectVariantGuid), project
				.getLanguageVariantByGuid(languageVariantGuid));
		publishingJob.start();

		return publishingJob;
	}

	/**
	 * Startet eine Generierung beginnend bei dieser Seite.
	 * 
	 * @param withFollowingPages
	 *            nur diese Seite, oder auch folgende? (without related pages)
	 * @param projectVariantGuid
	 *            GUID der Projektvariante die generiert werden soll
	 * @param languageVariantGuid
	 *            GUID der Sprachvariante die generiert werden soll
	 * @param checkThatLanguageVariantIsAccessible
	 *            =true, the needs access to the given language variant in order to publish it
	 *            <p>
	 *            =false, don't check the language variant guid; user can publish it even without having access to it!
	 * @see <code>PublishingJob</code>
	 */
	public PublishingJob publish(boolean withFollowingPages, String projectVariantGuid, String languageVariantGuid,
			boolean checkThatLanguageVariantIsAccessible) throws RQLException {

		PublishingJob publishingJob = new PublishingJob(this, withFollowingPages);
		publishingJob.addToPublish(projectVariantGuid, languageVariantGuid, checkThatLanguageVariantIsAccessible);
		publishingJob.start();

		return publishingJob;
	}

	/**
	 * Startet eine Generierung beginnend bei dieser Seite für mehrere Projektvarianten.
	 * 
	 * @param withFollowingPages
	 *            nur diese Seite, oder auch folgende? (without related pages)
	 * @param projectVariantGuids
	 *            Liste der GUIDs
	 * @param separator
	 *            Trennzeichen für die GUIDs der Projektvarianten
	 * @param languageVariantGuid
	 *            GUID der Sprachvariante die generiert werden soll
	 * @see <code>PublishingJob</code>
	 */
	public PublishingJob publish(boolean withFollowingPages, String projectVariantGuids, String separator, String languageVariantGuid)
			throws RQLException {

		PublishingJob publishingJob = new PublishingJob(this, withFollowingPages);
		publishingJob.addToPublish(projectVariantGuids, separator, languageVariantGuid);
		publishingJob.start();

		return publishingJob;
	}

	/**
	 * Startet eine Generierung beginnend bei dieser Seite für mehrere Projektvarianten.
	 * 
	 * @param withFollowingPages
	 *            nur diese Seite, oder auch folgende? (without related pages)
	 * @param projectVariantGuids
	 *            Liste der GUIDs
	 * @param separator
	 *            Trennzeichen für die GUIDs der Projektvarianten
	 * @param languageVariantGuid
	 *            GUID der Sprachvariante die generiert werden soll
	 * @param mailReceiver
	 *            User der nach jobende eine Mail erhalten soll
	 * @see <code>PublishingJob</code>
	 */
	public PublishingJob publish(boolean withFollowingPages, String projectVariantGuids, String separator, String languageVariantGuid,
			User mailReceiver) throws RQLException {

		PublishingJob publishingJob = new PublishingJob(this, withFollowingPages);
		publishingJob.addToPublish(projectVariantGuids, separator, languageVariantGuid);
		publishingJob.setMailReceiver(mailReceiver);
		publishingJob.start();

		return publishingJob;
	}

	/**
	 * Startet eine Generierung beginnend bei dieser Seite.
	 * 
	 * @param withFollowingPages
	 *            nur diese Seite, oder auch folgende? (without related pages)
	 * @param projectVariantGuid
	 *            GUID der Projektvariante die generiert werden soll
	 * @param languageVariantGuid
	 *            GUID der Sprachvariante die generiert werden soll
	 * @param mailReceiver
	 *            User der nach jobende eine Mail erhalten soll
	 * @see <code>PublishingJob</code>
	 */
	public PublishingJob publish(boolean withFollowingPages, String projectVariantGuid, String languageVariantGuid, User mailReceiver)
			throws RQLException {

		PublishingJob publishingJob = new PublishingJob(this, withFollowingPages);
		publishingJob.addToPublish(projectVariantGuid, languageVariantGuid);
		publishingJob.setMailReceiver(mailReceiver);
		publishingJob.start();

		return publishingJob;
	}

	/**
	 * Startet eine Generierung beginnend bei dieser Seite für alle Kombinationen der gegebenen Projekt- und Sprachvarianten.
	 * 
	 * @param withFollowingPages
	 *            nur diese Seite, oder auch folgende? (without related pages)
	 * @see <code>PublishingJob</code>
	 */
	public PublishingJob publishAllCombinations(boolean withFollowingPages, java.util.List<ProjectVariant> projectVariants,
			java.util.List<LanguageVariant> languageVariants) throws RQLException {

		PublishingJob publishingJob = new PublishingJob(this, withFollowingPages);
		publishingJob.addToPublishAllCombinations(projectVariants, languageVariants);
		publishingJob.start();

		return publishingJob;
	}

	/**
	 * Startet eine Generierung beginnend bei dieser Seite für alle Kombinationen der gegebenen Projekt- und Sprachvarianten.
	 * 
	 * @param withFollowingPages
	 *            nur diese Seite, oder auch folgende? (without related pages)
	 * @param projectVariantGuids
	 *            Liste der GUIDs
	 * @param separator
	 *            Trennzeichen für die GUIDs der Projektvarianten
	 * @param languageVariantGuids
	 *            Liste der GUIDs der Sprachvariante die generiert werden soll
	 * @see <code>PublishingJob</code>
	 */
	public PublishingJob publishAllCombinations(boolean withFollowingPages, String projectVariantGuids, String separator,
			String languageVariantGuids) throws RQLException {

		PublishingJob publishingJob = new PublishingJob(this, withFollowingPages);
		publishingJob.addToPublishAllCombinations(projectVariantGuids, separator, languageVariantGuids);
		publishingJob.start();

		return publishingJob;
	}

	/**
	 * Startet eine Generierung beginnend bei dieser Seite für alle Kombinationen der gegebenen Projekt- mit allen vorhandenen
	 * Sprachvarianten.
	 * <p>
	 * Prüft die Projekt- und Sprachvarianten im Publizierungpaket dieser Seite.
	 * 
	 * @param withFollowingPages
	 *            nur diese Seite, oder auch folgende? (without related pages)
	 * @param projectVariantGuids
	 *            Liste der GUIDs
	 * @param separator
	 *            Trennzeichen für die GUIDs der Projektvarianten
	 * @see <code>PublishingJob</code>
	 */
	public PublishingJob publishAllCombinationsAllLanguageVariants(boolean withFollowingPages, String projectVariantGuids,
			String separator) throws RQLException {

		return publishAllCombinationsAllLanguageVariants(withFollowingPages, projectVariantGuids, separator, true);
	}

	/**
	 * Startet eine Generierung beginnend bei dieser Seite für alle Kombinationen der gegebenen Projekt- mit allen vorhandenen
	 * Sprachvarianten.
	 * 
	 * @param withFollowingPages
	 *            nur diese Seite, oder auch folgende? (without related pages)
	 * @param projectVariantGuids
	 *            Liste der GUIDs
	 * @param separator
	 *            Trennzeichen für die GUIDs der Projektvarianten
	 * @param checkInPublicationPackage
	 *            =false, prevent the slow check of project- and language variants in publication package
	 * 
	 * @see <code>PublishingJob</code>
	 */
	public PublishingJob publishAllCombinationsAllLanguageVariants(boolean withFollowingPages, String projectVariantGuids,
			String separator, boolean checkInPublicationPackage) throws RQLException {

		PublishingJob publishingJob = new PublishingJob(this, withFollowingPages, false, checkInPublicationPackage);
		publishingJob.addToPublishAllCombinations(projectVariantGuids, separator, getProject().getAllLanguageVariants());
		publishingJob.start();
		return publishingJob;
	}

	/**
	 * Erzeugt eine Referenz zwischen den beiden Image-Elementen dieser Seite.
	 */
	public void referenceImageElementToImageElement(String sourceImageTemplateElementName, String targetImageTemplateElementName)
			throws RQLException {
		ImageElement source = getImageElement(sourceImageTemplateElementName);
		source.referenceTo(getImageElement(targetImageTemplateElementName));
	}

	/**
	 * Workflow: Lehnt diese Seite ab, zurück zum Autor zur Korrektur.
	 * <p>
	 * 
	 * @param noteName
	 *            Name des Verwaltungseintrages, in dem der rejectComment gespeichert wird
	 * @param rejectComment
	 *            Hinweise für die Korrektur
	 */
	public void reject(String noteName, String rejectComment) throws RQLException {

		// check prerequisite state
		if (!isInStateWaitingForRelease()) {
			throw new WrongStateException("Page " + getHeadlineAndId() + " is in unexpected state '" + getStateInfo()
					+ "'. To reject a page it has to be in state 'waiting for release'.");
		}

		// instruct the author
		setNoteValue(noteName, rejectComment);

		// reject page itself
		changeState(PAGE_ACTION_REJECT);
	}

	/**
	 * Workflow: Bestätigt diese Seite, gibt Sie frei. Vom aktuell angemeldeten User aus gesehen.
	 */
	public void release() throws RQLException {

		// check prerequisite state
		if (!isInStateWaitingForRelease()) {
			throw new WrongStateException("Page " + getHeadlineAndId() + " is in unexpected state '" + getStateInfo()
					+ "'. To release a page it has to be in state 'waiting for release'.");
		}

		changeState(PAGE_ACTION_RELEASE);
	}

	/**
	 * Löscht den Dateinamen der Seite.
	 */
	public void removeFilename() throws RQLException {
		setFilename("");
	}

	/**
	 * Ersetzt das Berechtigungspaket mit Namen searchAuthorizationPackageName an dieser Seite gegen newAuthorizationPackage. Es findet
	 * keine Ersetzung statt, falls diese Seite kein Paket hat, oder es nicht das Gegebene ist.
	 * 
	 * @param inherit
	 *            =true startet eine Vererbung von newAuthorizationPackage ab dieser Seite
	 * 
	 * @return true, falls eine Ersetzung stattfand; sonst false
	 */
	public boolean replaceAuthorizationPackage(String searchAuthorizationPackageName, AuthorizationPackage newAuthorizationPackage,
			boolean inherit) throws RQLException {

		AuthorizationPackage current = getAuthorizationPackage();

		// check no package at all
		if (current == null) {
			return false;
		}

		// check against given one
		if (current.getName().equals(searchAuthorizationPackageName)) {
			// same package, replace
			disconnectAuthorizationPackage(current);
			assignAuthorizationPackage(newAuthorizationPackage, inherit);
			return true;
		}

		// different package
		return false;
	}

	/**
	 * Setzt alle änderbaren Werte und Caches auf null, um ein Neulesen zu forcieren.
	 */
	private void resetChangeableValues() throws RQLException {
		detailsNode = null;
		elementsNodeList = null;
		headline = null;
		linksNodeList = null;
	}

	/**
	 * Macht Änderungen an dieser draft Seite rückgängig.
	 * <p>
	 * Neu erstellte Seiten werden wieder gelöscht und Änderungen an bestehen Seiten werden wieder rückgängig gemacht.
	 * 
	 * @throws WrongStateException
	 */
	public void resetDraftState() throws RQLException {

		// undo changes
		if (isInStateSavedAsDraftChanged()) {
			undoChangesPrimitive();
			return;
		}

		// delete new draft pages
		if (isInStateSavedAsDraftNew()) {
			delete(true); // ignore references!
			return;
		}

		// not in draft state
		throw new WrongStateException("Reset draft state canceled, because page " + getHeadlineAndId() + " in state '"
				+ getStateInfo() + "' is not in expected state draft.");
	}

	/**
	 * Holt diese Seite aus dem Papierkorb zurück.
	 * <p>
	 * Auch wenn die Seite im Papierkorb ist, lassen sich diverse RQL mit Seitendetails aufrufen, wenn sie über die page GUID erzeugt
	 * wurde!
	 * 
	 * @see #isInRecycleBin()
	 * @throws #WrongStateException
	 * @see RecycleBin#restorePageById(String)
	 */
	void restore() throws RQLException {
		/* 
		 V7.5 request
		<IODATA loginguid="AF629A1EBB2A4BEC89F533473AEF5871" sessionkey="94148B94AE534560A94996AD9E7DE757">
		<PAGE action="restore" guid="ED0B10019462419AA6F7C18E248462EB" />
		</IODATA>
		 V7.5 response
		<IODATA>
		<PAGE action="restore" guid="ED0B10019462419AA6F7C18E248462EB" sessionkey="94148B94AE534560A94996AD9E7DE757" dialoglanguageid="ENG" languagevariantid="ENG" available="1" pageguid="ED0B10019462419AA6F7C18E248462EB" checkstructureworkflow="1"/>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "  <PAGE action='restore' guid='" + getPageGuid() + "'/>" + "</IODATA>";
		// ignore response
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert eine Teilmenge der gegebenen Liste zurück, an die diese Seite gelinkt werden darf. D.h. das Template dieser Seite ist
	 * allen zurückgegebenen Links vorbelegt.
	 * 
	 * @param assumedTargetLinks
	 *            Liste of MultiLinks
	 * 
	 * @return java.util.List Liste of MultiLinks, Teilmenge von assumedTargetLinks oder leere Liste
	 */
	public java.util.List<MultiLink> selectConnectToLinks(java.util.List assumedTargetLinks) throws RQLException {

		java.util.List<MultiLink> result = new ArrayList<MultiLink>();
		Template template = getTemplate();

		// for all given links check
		for (int i = 0; i < assumedTargetLinks.size(); i++) {
			MultiLink link = (MultiLink) assumedTargetLinks.get(i);
			java.util.List allowedTemplates = link.getAllowedTemplates();
			if (allowedTemplates.contains(template)) {
				result.add(link);
			}
		}
		return result;
	}

	/**
	 * Ändert die Eigenschaften des Krümelpfades dieser Seite.
	 * 
	 * @param isStartPoint
	 *            true = setzt den Startpunkt an dieser Seite
	 * @param include
	 *            true = lässt diese Seite im Krümelpfad aus
	 */
	public void setBreadcrump(boolean isStartPoint, boolean include) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="AA3F38A57BD3436F8198770B0325FBFA" sessionkey="371730812u81s83D6JyY">
		 <PAGE action="save" guid="7594EDC851114FB5A1958BC09A75B601" breadcrumbstartpoint="1" breadcrumbdonotuse="0" ></PAGE>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <PAGE action="save" breadcrumbstartpoint="1" breadcrumbdonotuse="0" sessionkey="371730812u81s83D6JyY" dialoglanguageid="ENG" languagevariantid="ENG" templateguid="" mainlinkguid="9F2C3B5C2D094BD2AF6EC29443C55ACF" guid="7594EDC851114FB5A1958BC09A75B601" id="328" breadcrumbstartpointchanged="1"></PAGE>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "  <PAGE action='save' guid='" + getPageGuid() + "' breadcrumbstartpoint='" + StringHelper.convertTo01(isStartPoint)
				+ "' breadcrumbdonotuse='" + StringHelper.convertTo01(!include) + "'/>" + "</IODATA>";
		callCms(rqlRequest);
	}

	/**
	 * Setzt den details node dieser Seite und liefert diesen auch wieder zurück. Die Prüfung, ob der detailsNode bereits gefüllt ist
	 * oder nicht muss außerhalb erfolgen.
	 */
	private RQLNode setDetailsNode(RQLNode detailsNode) throws RQLException {

		this.detailsNode = detailsNode;
		checkDetailsNode();
		return detailsNode;
	}

	/**
	 * Setzt die elements node list dieser Seite und liefert diese auch wieder zurück.
	 */
	private RQLNodeList setElementsNodeList(RQLNodeList elementsNodeList) throws RQLException {

		this.elementsNodeList = elementsNodeList;
		return elementsNodeList;
	}

	/**
	 * Ändert Inhaltselemente dieser Seite mit nur einem RQL request. Es werden nur die folgenden Elementtypen unterstützt:
	 * <p>
	 * StandardFieldText, StandardFieldUserDefined, StandardFieldNumeric, StandardFieldDate, OptionsList
	 * <p>
	 * Folgende Elementtypen werden nicht unterstützt, da für diese spezielle Updatemethoden benutzt werden müssen:
	 * <p>
	 * ImageElement, MediaElement, TextElement
	 * 
	 * @param elementValuePairs
	 *            Key=Element Objekt, Value=Objekt mit Wert mit passendem Typ. the element value is deleted (via #sessionKey), if the
	 *            string value is empty.
	 * @throws CombinedUpdateNotSupportedException
	 */
	public void setElementValues(Map<Element, Object> elementValuePairs) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="087F79DA22DF4EF385A7A18FDBB238CD" sessionkey="421139875y5k2iP8GF85">
		 <ELEMENTS action="save">
		 <ELT guid="9A872444A0034A428EE1214C0BFC6EEF" value="telephone333.png"/>
		 <ELT guid="..."/>
		 ....
		 </ELEMENTS>
		 </IODATA>
		 V5 response
		 <IODATA>9A872444A0034A428EE1214C0BFC6EEF</IODATA>
		 */

		// update only if values given
		if (!elementValuePairs.isEmpty()) {
			// build request start
			StringBuilder rqlRequest = new StringBuilder();
			rqlRequest.append("<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>");
			rqlRequest.append("<ELEMENTS action='save'>");

			// add all given elements
			Element element = null;
			Object value = null;
			Map.Entry pair = null;
			Iterator pairIterator = elementValuePairs.entrySet().iterator();
			while (pairIterator.hasNext()) {
				pair = (Map.Entry) pairIterator.next();
				element = (Element) pair.getKey();
				// check if element type is supported
				if (!element.isCombinedUpdateSupported()) {
					throw new CombinedUpdateNotSupportedException(
							"Update canceled, because the element with name "
									+ element.getName()
									+ " and type "
									+ element.getClass().getName()
									+ " does not support the combined update method. Please update this element only using the methods on the element itself.");
				}
				value = (Object) pair.getValue();
				String newValue = element.convertToStringValue(value);
				rqlRequest.append("<ELT guid='" + element.getElementGuid() + "' value='"
						+ (newValue == null || newValue.length() == 0 ? "#" + getSessionKey() : StringHelper.escapeHTML(newValue)) + "'/>");
			}

			// add request end
			rqlRequest.append("</ELEMENTS>");
			rqlRequest.append("</IODATA>");

			// call CMS
			callCms(rqlRequest.toString());

			// change element values
			pairIterator = elementValuePairs.entrySet().iterator();
			while (pairIterator.hasNext()) {
				pair = (Map.Entry) pairIterator.next();
				element = (Element) pair.getKey();
				value = (Object) pair.getValue();
				element.updateValue(value);
			}

			// force new read of elements node list
			deleteElementsNodeListCache();
		}
	}

	/**
	 * Ändert den Dateinamen der Seite.
	 */
	public void setFilename(String filename) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="02BDD3C71A2446F3AED7C41C2216AAF3" sessionkey="351980766YKw488X61l2">
		 <PAGE action="save" guid="EF0B9745F3774A0DAA3D7EF4819F6428" name="test2.html"/>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <PAGE action="save" name="test2.html" sessionkey="351980766YKw488X61l2" dialoglanguageid="ENG" languagevariantid="ENG" templateguid="" mainlinkguid="F30C1609C0D24CF1A4B6241592E1CDBA" guid="EF0B9745F3774A0DAA3D7EF4819F6428" id="3568" attributeid="2"/>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "  <PAGE action='save' guid='" + getPageGuid() + "' name='"
				+ (filename.length() == 0 ? "#" + getSessionKey() : filename) + "'/>" + "</IODATA>";
		callCms(rqlRequest);

		// reset details node cache
		deleteDetailsNodeCache();
	}

	/**
	 * Changes the headline of this page by adding the given suffix to the current headline. There will be no blank added
	 * automatically.
	 */
	public void setHeadlineAddSuffix(String suffix) throws RQLException {
		setHeadline(getHeadline() + suffix);
	}

	/**
	 * Changes the headline of this page to the given one.
	 */
	public void setHeadline(String headline) throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="3971D6EB9BD746D187BE0D2682AB7E13" sessionkey="351998080iE024w3q4V7">
		 <PAGE action="save" guid="E58B4BE5382E4372BAD74706414B3FC8" headline="test wf nach wf weg dran cms ex"></PAGE>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <PAGE action="save" headline="test wf nach wf weg dran cms ex" sessionkey="351998080iE024w3q4V7" dialoglanguageid="ENG" languagevariantid="ENG" templateguid="" mainlinkguid="25D2A30716D646DEAE4C71AAE0724F11" guid="E58B4BE5382E4372BAD74706414B3FC8" id="12391" attributeid="2" changed="-1" useconnection="1" userguid="4324D172EF4342669EAF0AD074433393" changeuserguid="4324D172EF4342669EAF0AD074433393" actionflag="65536" pageguid="E58B4BE5382E4372BAD74706414B3FC8" saveexplicituserguid="4324D172EF4342669EAF0AD074433393">	</PAGE>
		 </IODATA>
		 */
		
		// optimize cms call away, in case we happen to know the headline and its unchanged
		if (this.headline != null && this.headline.equals(headline))
			return;

		// call CMS
		StringBuilder sb = new StringBuilder(128);
		sb.append("<IODATA loginguid='").append(getLogonGuid()).append("' sessionkey='").append(getSessionKey()).append("'>")
		.append("  <PAGE action='save' guid='").append(getPageGuid()).append("' ");
		if (headline == null || headline.isEmpty()) {
			sb.append(" headline='#").append(getSessionKey()).append("'");
		} else {
			sb.append(" headline='").append(StringHelper.escapeHTML(headline)).append("'");
		}
		sb.append("/></IODATA>");
		String rqlRequest = sb.toString();
		callCms(rqlRequest);

		// reset details node cache
		deleteDetailsNodeCache();

		// update local cache;
		this.headline = headline;
	}

	/**
	 * Aendert den Wert des ImageElementes dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 2 sein
	 */
	public void setImageValue(String templateElementName, String filename) throws RQLException {

		getImageElement(templateElementName).setFilename(filename);

		// force new read of elements node list
		deleteElementsNodeListCache();
	}

	/**
	 * Deletes the publication package cache of this page.
	 */
	void deletePublicationPackageCache() throws RQLException {
		publicationPackage = null;
	}

	/**
	 * Setzt den Hauptlink dieser Seite auf den gegebenen MultiLink. Vorbedingung: Diese Seite muss child des gegeben MultiLinks sein.
	 */
	public void setMainLink(MultiLink mainLink) throws RQLException {

		// this page has to be a child of the given Link
		if (!mainLink.isChild(this)) {
			throw new NoChildException("The page " + getHeadlineAndId() + " is not a child of the multi link " + mainLink.getName()
					+ " on page " + mainLink.getPage().getHeadlineAndId() + ".");
		}

		// set new main link
		/* 
		 V5 request
		 <IODATA loginguid="C68C231E9D2E453D9F8F3C05A9263293" sessionkey="1021834323434CRb1cP65">
		 <PAGE action="save" guid="C11BCE3CBEBD4547BC3BAB23E588ED71" mainlinkguid="57089D960BAD43559BF36BA2EC9B8818"/>
		 </IODATA>
		 V6 response
		 <IODATA>
		 <PAGE action="save" mainlinkguid="57089D960BAD43559BF36BA2EC9B8818" sessionkey="1021834323434CRb1cP65" dialoglanguageid="ENG" languagevariantid="ENG" templateguid="" guid="C11BCE3CBEBD4547BC3BAB23E588ED71" id="14741"/>
		 </IODATA>
		 </SERVER>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<PAGE action='save' guid='" + getPageGuid() + "' mainlinkguid='" + mainLink.getLinkGuid() + "'/>" + "</IODATA>";
		callCms(rqlRequest);
		// reset cache
		deletePublicationPackageCache();
	}

	/**
	 * Aendert den Wert des Mediaelementes dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 38 sein
	 * @param file
	 *            das file, das mit dem element basierend auf templateElementName, verknüpft werden soll
	 */
	public void setMediaValue(String templateElementName, com.hlcl.rql.as.File file) throws RQLException {

		getMediaElement(templateElementName).setFilename(file.getFilename());

		// force new read of elements node list
		deleteElementsNodeListCache();
	}

	/**
	 * Aendert den Wert des Mediaelementes dieser Seite, das auf dem gegebenen templateElement basiert.
	 * <p>
	 * Der gegebenen filename muss aus dem im Templateelement gesetzten Ordner stammen.
	 * <p>
	 * Ein file aus einem Unterordner kann mit dieser Methode nicht geändert werden!
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 38 sein
	 * @see #setMediaValue(String, String, AssetManagerSubFolder)
	 */
	public void setMediaValue(String templateElementName, String filename) throws RQLException {

		getMediaElement(templateElementName).setFilename(filename);

		// force new read of elements node list
		deleteElementsNodeListCache();
	}

	/**
	 * Aendert den Wert des Mediaelementes dieser Seite, das auf dem gegebenen templateElement basiert.
	 * <p>
	 * Der gegebenen filename muss aus dem gegebenen Assetmanager-Unterordner stammen.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 38 sein
	 */
	public void setMediaValue(String templateElementName, String filename, AssetManagerSubFolder subFolder) throws RQLException {

		getMediaElement(templateElementName).setFilename(filename, subFolder);

		// force new read of elements node list
		deleteElementsNodeListCache();
	}

	/**
	 * Aendert den Wert des Mediaelementes dieser Seite, das auf dem gegebenen templateElement basiert.
	 * <p>
	 * Es findet keine Prüfung statt, ob das File auch in diesem Ordner existiert. Das erhöht die Performance.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 38 sein
	 * @param file
	 *            das file, das mit dem element basierend auf templateElementName, verknüpft werden soll
	 */
	public void setMediaValueWithoutCheck(String templateElementName, com.hlcl.rql.as.File file) throws RQLException {

		getMediaElement(templateElementName).setFilenameWithoutCheck(file.getFilename());

		// force new read of elements node list
		deleteElementsNodeListCache();
	}

	
	/**
	 * Aendert den Wert des Mediaelementes dieser Seite, das auf dem gegebenen templateElement basiert.
	 * <p>
	 * Es findet keine Prüfung statt, ob das File auch in diesem Ordner existiert. Das erhöht die Performance.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 38 sein
	 * @param file
	 *            das file, das mit dem element basierend auf templateElementName, verknüpft werden soll
	 */
	public void setMediaValueWithoutCheck(String templateElementName, String file) throws RQLException {

		getMediaElement(templateElementName).setFilenameWithoutCheck(file);

		// force new read of elements node list
		deleteElementsNodeListCache();
	}

	
	/**
	 * Aendert den Wert des Verwaltungseintrages mit dem gegebenen Namen.
	 * <p>
	 * Die Länge scheint nicht wie im CMS auf 255 Zeichen begrenzt.
	 * 
	 * @param name
	 *            Name des Verwaltungseintrages
	 */
	public void setNoteValue(String name, String value) throws RQLException {

		getNote(name).setValue(value);
	}

	/**
	 * Aendert den Wert der Optionsliste dieser Seite, das auf dem gegebenen templateElement basiert. Der gegebenen Wert value wird dem
	 * Autor angezeigt. Es ist nicht die GUID der OptionListSelection.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss Typ 8 sein
	 */
	public void setOptionListValue(String templateElementName, String value) throws RQLException {

		getOptionList(templateElementName).select(value);

		// force new read of elements node list
		deleteElementsNodeListCache();
	}

	/**
	 * Aendert den Wert des Standardfeld Date Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 5 sein
	 */
	public void setStandardFieldDateValue(String templateElementName, ReddotDate value) throws RQLException {

		getStandardFieldDateElement(templateElementName).setDate(value);

		// force new read of elements node list
		deleteElementsNodeListCache();
	}

	/**
	 * Setzt das heutige Datum in das Standardfeld Date Element dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 5 sein
	 */
	public void setStandardFieldDateValueToToday(String templateElementName) throws RQLException {

		setStandardFieldDateValue(templateElementName, new ReddotDate());

		// force new read of elements node list
		deleteElementsNodeListCache();
	}

	/**
	 * Aendert den Wert des Standardfeld Numeric Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 48 sein
	 */
	public void setStandardFieldNumericValue(String templateElementName, int value) throws RQLException {

		getStandardFieldNumericElement(templateElementName).setInt(value);

		// force new read of elements node list
		deleteElementsNodeListCache();
	}

	/**
	 * Aendert den Wert des Standardfeld Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 1 sein
	 */
	public void setStandardFieldTextValue(String templateElementName, String value) throws RQLException {

		getStandardFieldTextElement(templateElementName).setText(value);

		// force new read of elements node list
		deleteElementsNodeListCache();
	}

	/**
	 * Aendert den Wert des Standardfeld user defined elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * <p>
	 * Achtung: Die festgelegte JavaScript RegEx kann hier nicht geprüft werden!
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 999 sein
	 */
	public void setStandardFieldUserDefinedValue(String templateElementName, String value) throws RQLException {

		getStandardFieldUserDefinedElement(templateElementName).setText(value);

		// force new read of elements node list
		deleteElementsNodeListCache();
	}

	/**
	 * Aendert den Wert des Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 31 sein
	 * @param htmlSourceCode
	 *            der HTML source code
	 */
	public void setTextValue(String templateElementName, String htmlSourceCode) throws RQLException {

		getTextElement(templateElementName).setText(htmlSourceCode);
	}

	/**
	 * Aktualisiert an dieser Seite fehlende Text-Elemente.
	 * <p>
	 * Dazu wird der HTML code dieser Seite für die Darstellung im Smart-Edit (page closed) angefordert. Funktioniert nicht, falls die
	 * Seite im Status draft ist!?
	 */
	public String simulateSmartEditUsage() throws RQLException {
		/* 
		 V5 request
		 <IODATA loginguid="3B3428642F3844B58E9E6E903BC8F6A9" sessionkey="692333517I0uhPs30D57">
		 <REDDOT mode="" translationmode="0" projectguid="06BE79A1D9F549388F06F6B649E27152" loginguid="3B3428642F3844B58E9E6E903BC8F6A9" url="/reddot5/ioRD.asp" 
		 querystring="Action=RedDot&Mode=1&pageguid=A2EF484553384656B6ADCC7876077048&islink=2"/>
		 </IODATA>
		 
		 V5 response 
		 <HTML><HEAD></HEAD><BODY ><rde-dm:attribute mode="condition" attribute="profile.roles" op="containsany" value="MAIN" tag="notag">
		 <rde-dm:if>
		 <!-- two columns block, optional -->
		 ...
		 </div>
		 </rde-dm:if>
		 </rde-dm:attribute></BODY></HTML><SCRIPT language="javascript" src="ioEditElement.js"></SCRIPT>
		 */

		// build the reddot tag
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		String reddotTag = b.getString("rqlSmartEditHtmlTag");
		Object parms[] = new Object[3];
		parms[0] = getProjectGuid();
		parms[1] = getLogonGuid();
		parms[2] = getPageGuid();
		reddotTag = MessageFormat.format(reddotTag, parms);

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>";
		rqlRequest += reddotTag;
		rqlRequest += "</IODATA>";
		return callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert eine sortierte Liste entsprechend der Sortierung der TemplateElemente zurück.
	 * 
	 * @param includeReferences
	 *            =true, auch Links, die Referenzquelle sind werden geliefert (haben keine Childs!) =false, ohne Links, die
	 *            Referenzquelle sind (nur diese haben Childs!)
	 * @return java.util.List mit List oder Container Objekten
	 */
	private java.util.List<MultiLink> sortLinks(java.util.List<MultiLink> links, boolean includeReferences) throws RQLException {

		// no sort needed
		if (links.size() <= 1) {
			return links;
		}

		// create a map; key=template element guid, value=link itself
		Map<String, MultiLink> linksMap = new HashMap<String, MultiLink>(links.size());
		for (Iterator<MultiLink> iter = links.iterator(); iter.hasNext();) {
			MultiLink link = iter.next();
			linksMap.put(link.getTemplateElement().getTemplateElementGuid(), link);
		}

		// get multi link template elements in correct order from template
		java.util.List templateElements = getTemplate().getMultiLinkTemplateElements(includeReferences);

		// build result list
		// for all template elements do
		java.util.List<MultiLink> result = new ArrayList<MultiLink>(links.size());
		for (Iterator iter = templateElements.iterator(); iter.hasNext();) {
			TemplateElement templateElement = (TemplateElement) iter.next();
			MultiLink linkOrNull = linksMap.get(templateElement.getTemplateElementGuid());
			if (linkOrNull != null) {
				// link found, add into result
				result.add(linkOrNull);
			}
		}

		return result;
	}

	/**
	 * Start a mode to add elements which values should be deleted.
	 * 
	 * @see #endDeleteElementValues()
	 * @see Element#deleteValue()
	 * @see TextElement#deleteValue()
	 * @see #addDeleteElementValue(Element)
	 */
	public void startDeleteElementValues() throws RQLException {
		deleteElementValuesRequest = new StringBuilder(256);
	}

	/**
	 * Start a mode to add elements which values should be changed.
	 * 
	 * @see #setElementValues(Map)
	 * @see #startSetElementValues()
	 * @see #addSetOptionListValue(String, String)
	 * @see #addSetStandardFieldNumericValue(String, int)
	 * @see #addSetStandardFieldTextValue(String, String)
	 * @see #addSetStandardFieldUserDefinedValue(String, String)
	 * @see #addCopyOptionListValueFrom(String, Page)
	 * @see #addCopyStandardFieldDateValueFrom(String, Page)
	 * @see #addCopyStandardFieldNumericValueFrom(String, Page)
	 * @see #addCopyStandardFieldTextValueFrom(String, Page)
	 * @see #addCopyStandardFieldUserDefinedValueFrom(String, Page)
	 * @see #endSetElementValues()
	 */
	public void startSetElementValues() throws RQLException {
		setElementValuesMap = new HashMap<Element, Object>();
	}

	/**
	 * Workflow: Übergibt eine Seite im Entwurf zur Prüfung an den Workflow. Vom aktuell angemeldeten User aus gesehen. Oder ohne
	 * Workflow, wird die Änderung des Autors an dieser Seite gespeichert, so dass sie für alle sichtbar ist.
	 * 
	 * @return null, falls submit erfolgreich war java.util.List of TemplateElement, mit den fehlenden Pflichtfeldern
	 */
	public java.util.List<TemplateElement> submitToWorkflow() throws RQLException {

		/* 
		 V6.5 request
		 <IODATA loginguid="FC795754C7754BDCA68170D1B8DD94CF" sessionkey="925833953ar10vo61yBI">
		 <PAGE action="save" guid="792F72BCCE13424FA151B12DFDF203C2" actionflag="4096" globalsave="0"/>
		 </IODATA>
		 V6.5 response (all fields filled)
		 <IODATA>
		 <PAGE action="save" guid="792F72BCCE13424FA151B12DFDF203C2" sessionkey="925833953ar10vo61yBI" dialoglanguageid="ENG" useconnection="1" userguid="4324D172EF4342669EAF0AD074433393" changeuserguid="8898998310DD4513BB8CC1771FFD00BC" mainlinkguid="20467D958D7743488A2356B6CC1099D0" glrights1="-1" glrights2="-1" glrights3="-1" glrights5="-1" glrights6="-1" gldenys1="0" gldenys2="0" gldenys3="0" gldenys5="0" gldenys6="0" rights1="-1" rights2="-1" rights3="-1" rights4="-1" rights5="-1" rights6="-1" saveexplicituserguid="8898998310DD4513BB8CC1771FFD00BC" actionflag="32768" languagevariantid="ENG"/>
		 </IODATA>
		 V6.5 response (missing mandatory fields)
		 <IODATA>
		 <EMPTYELEMENTS  pageguid="C1BAC7FA66734D1EB00223D7D58BA6F6" pageheadline="or - de: Points of Entry">
		 <ELEMENT guid="A28560E00D434A70A8AC87E223B2333E" name="responsible_id" type="1"></ELEMENT>
		 <ELEMENT guid="CB508F4828BD409E893AF3963C87F5B4" name="responsible_name" type="1"></ELEMENT>
		 </EMPTYELEMENTS>
		 </IODATA>
		 */

		// check prerequisite state
		if (!isInStateSavedAsDraft()) {
			throw new WrongStateException("Page " + getHeadlineAndId() + " is in unexpected state '" + getStateInfo()
					+ "'. To submit a page to the workflow it has to be in state 'saved as draft'.");
		}

		// don't use changeState()
		RQLNode response = null;
		try {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ " <PAGE action='save' guid='" + getPageGuid() + "' actionflag='" + PAGE_ACTION_SUBMIT_TO_WORKFLOW
					+ "' globalsave='0'/>" + "</IODATA>";
			response = callCms(rqlRequest);
		} catch (RQLException re) {
			// handle V9.0.1.49 error versioning not activated
			ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
			String message = b.getString("VersioningNotActivatedErrorMessage");
			// ignore but throw all other
			if (!re.getMessage().contains(message)) {
				throw re;
			}
		}

		// force new read of details node
		deleteDetailsNodeCache();

		// wenn Pflichtfelder fehlen, wird keine versioning not activated exception geworfen
		if (response != null) {
			RQLNodeList elements = response.getNodes("ELEMENT");
			// check for missing mandatory fields
			if (elements != null) {
				Template template = getTemplate();
				java.util.List<TemplateElement> result = new ArrayList<TemplateElement>(elements.size());
				for (int i = 0; i < elements.size(); i++) {
					RQLNode node = (RQLNode) elements.get(i);
					result.add(template.getTemplateElementByName(node.getAttribute("name")));
				}
				return result;
			}
		}
		// submit was successfull
		return null;
	}

	/**
	 * Überschreibt den Standardwert für bequemes Debugging.
	 * 
	 * @return String
	 */
	public String toString() {
		String s = super.toString();
		try {
			s = this.getClass().getName() + " (" + getPageId() + ")";
		} catch (RQLException re) {
			// ignore
		}
		return s;
	}

	/**
	 * Ändert die Überschrift dieser Seite, um sie wieder publizieren zu können. Sie wird auf jeden Fall geändert und ist danach wieder
	 * released.
	 * <p> - hängt ein blank an oder
	 * <p> - entfernt an bereits angehängtes blank von der Überschrift wieder
	 * <p>
	 * Seite wird nicht geändert, falls diese Seite ein GUID page ist, also gar keine Überschrift hat.
	 */
	public void touch() throws RQLException {
		if (hasHeadline()) {
			changeHeadline();
			submitToWorkflow();
		}
	}

	/**
	 * Entfernt alle Blanks vorn und hinten vom Wert des Standardfeld Textelements dieser Seite, das auf dem gegebenen templateElement
	 * basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 1 sein
	 */
	public void trimStandardFieldTextValue(String templateElementName) throws RQLException {

		String value = getStandardFieldTextValue(templateElementName);
		value = value.trim();
		setStandardFieldTextValue(templateElementName, value);
	}

	/**
	 * Workflow: Macht Änderungen an dieser Seite rückgängig.
	 * 
	 * @throws WrongStateException
	 */
	public void undoChanges() throws RQLException {

		// check prerequisite state
		if (!isInStateSavedAsDraftChanged()) {
			throw new WrongStateException("Page " + getHeadlineAndId() + " is in unexpected state '" + getStateInfo()
					+ "'. Page has to be in state 'saved as draft (changed)'.");
		}

		// undo
		undoChangesPrimitive();
	}

	/**
	 * Workflow: Macht Änderungen an dieser Seite rückgängig. Ohne check des Status der Seite.
	 */
	private void undoChangesPrimitive() throws RQLException {

		/* 
		 V5 request
		 <IODATA loginguid="3B8AEC32CDE04A169073B1CBBE722A22" sessionkey="371713390O0u5hUE8320">
		 <PAGE action="rejecttempsaved" guid="EF0B9745F3774A0DAA3D7EF4819F6428" />
		 </IODATA>
		 V5 response 
		 <IODATA>
		 <PAGE action="rejecttempsaved" guid="EF0B9745F3774A0DAA3D7EF4819F6428" sessionkey="371713390O0u5hUE8320" dialoglanguageid="ENG" languagevariantid="ENG"/>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ " <PAGE action='rejecttempsaved' guid='" + getPageGuid() + "'/>" + "</IODATA>";
		callCms(rqlRequest);
		// force re-read of all changeable values
		resetChangeableValues();
	}

    /**
     * Entfernt den vorhanden Erscheinungszeitraum des Hauptlinks
     *
     * @throws RQLException
     */
    public void clearMainLinkAppearanceSchedule() throws RQLException {

        MultiLink mainMultiLink = null;

        try {
            mainMultiLink = this.getMainMultiLink();
        }
        catch (UnlinkedPageException upe) {}
        catch (WrongTypeException e) {} // it's a Text Anchor

        if (mainMultiLink != null) {
            if (mainMultiLink.getAppearanceSchedule() != null) {
                this.assignMainLinkAppearanceSchedule(AppearanceSchedule.clearedSchedule());
            }
        }
    }

    /**
     * Weist dem Hauptlink dieser Seite einen Erscheinungszeitraum zu
     *
     * @param appearanceSchedule
     * @throws RQLException
     */
    public void assignMainLinkAppearanceSchedule(AppearanceSchedule appearanceSchedule) throws RQLException {
    	// 1. relationguid des mainlinks finden 
    	RQLNode linkNode = getMainLinkNode(); 
        String relationGuid = linkNode.getAttribute("relationguid");

        // 2. set appearance schedule
        StringBuilder sb = new StringBuilder(128);
        sb.append("<IODATA loginguid=\"").append(this.getLogonGuid()).append("\" sessionkey=\"").append(this.getSessionKey()).append("\">")
                .append("<PAGE><LINKFROM action=\"save\" guid=\"").append(relationGuid)
                .append("\" datebegin=\"").append(appearanceSchedule.getBegin().toMsDoubleString())
                .append("\" dateend=\"").append(appearanceSchedule.getEnd().toMsDoubleString()).append("\" />")
                .append("</PAGE></IODATA>");

        getCmsClient().callCmsWithoutParsing(sb.toString());

        this.getMainMultiLink().setAppearanceSchedule(appearanceSchedule);
    }

    
    /**
     * Weist einem beliebigen Linkt dieser Seite einen Erscheinungszeitraum zu.
     * 
     * @param linkGuid link of the structural element to manipulate.
     * @param appearanceSchedule the schedule to set.
     * @throws RQLException if this page is not linked to the given link guid.
     */
    public void assignLinkAppearanceSchedule(String linkGuid, AppearanceSchedule appearanceSchedule) throws RQLException {
    	// All the Links
    	RQLNodeList nodeList = getLinkedFromNodeList();
    	if (nodeList == null)
    		throw new RQLException("Page is not linked.");
    	
    	// Our Link
    	RQLNode linkNode = null;
    	for (RQLNode n : nodeList) {
    		if (linkGuid.equals(n.getAttribute("guid")))
    			linkNode = n;
    	}
    	
    	if (linkNode == null)
    		throw new RQLException("Page is not linked to element " + linkGuid);
    				
        String relationGuid = linkNode.getAttribute("relationguid");

        // Set appearance schedule
        StringBuilder sb = new StringBuilder(128);
        sb.append("<IODATA loginguid=\"").append(this.getLogonGuid()).append("\" sessionkey=\"").append(this.getSessionKey()).append("\">")
                .append("<PAGE><LINKFROM action=\"save\" guid=\"").append(relationGuid)
                .append("\" datebegin=\"").append(appearanceSchedule.getBegin().toMsDoubleString())
                .append("\" dateend=\"").append(appearanceSchedule.getEnd().toMsDoubleString()).append("\" />")
                .append("</PAGE></IODATA>");

        getCmsClient().callCmsWithoutParsing(sb.toString());
    }
    
    
    /**
     * Weist einem beliebigen Linkt dieser Seite einen Erscheinungszeitraum zu.
     * 
     * @param linkGuid link of the structural element to manipulate.
     * @param appearanceSchedule the schedule to set.
     * @throws RQLException if this page is not linked to the given link guid.
     */
    public void assignLinkAppearanceSchedule(MultiLink link, AppearanceSchedule appearanceSchedule) throws RQLException {
    	assignLinkAppearanceSchedule(link.getLinkGuid(), appearanceSchedule);
    }

    
    /**
     * Liefert alle Sprachvarianten für diese Seite
     *
     * @return
     * @throws RQLException
     */
    public List<LanguageVariant> getLanguageVariants() throws RQLException {
        List<LanguageVariant> languageVariants = Collections.emptyList();

        StringBuilder rqlRequest = new StringBuilder("<IODATA loginguid=\"").append(getLogonGuid()).append("\" sessionkey=\"").append(getSessionKey()).append("\">");
        rqlRequest.append("<PROJECT><LANGUAGEVARIANTS action=\"pageavailable\" pageguid=\"").append(this.getPageGuid()).append("\"/></PROJECT></IODATA>");

        RQLNode response = getCmsClient().callCms(rqlRequest.toString());

        if(response != null) {
            RQLNodeList nodes = response.getNodes(LanguageVariant.RQL_ELEMENT_NAME);
            if(nodes != null){
                languageVariants = new ArrayList<LanguageVariant>();
                for(int i = 0; i < nodes.size(); i++) {
                    RQLNode node = nodes.get(i);
                    LanguageVariant languageVariant = new LanguageVariant(
                            this.project,
                            node.getAttribute("guid"),
                            node.getAttribute("name"),
                            node.getAttribute("rfclanguageid"),
                            node.getAttribute("ismainlanguage"),
                            node.getAttribute("language")
                    );
                    languageVariants.add(languageVariant);
                }
            }
        }

        return languageVariants;
    }

    /**
     * Ändert den aktuellen Benutzer einer Seite in den übergebenen Benutzer
     *
     * @param user Neuer Benutzer
     * @throws RQLException
     */
    public void switchUserTo(User user) throws RQLException {

        if(user == null)
            throw new IllegalArgumentException("User must not be null");

        StringBuilder rqlRequest = new StringBuilder("<IODATA loginguid=\"").append(getLogonGuid()).append("\" sessionkey=\"").append(getSessionKey()).append("\">")
        .append("<PAGE  guid=\"").append(this.getPageGuid()).append("\">")
        .append("<CHANGE><USER action=\"save\" guid=\"").append(user.getUserGuid()).append("\"/></CHANGE>")
        .append("</PAGE></IODATA>");

        getCmsClient().callCmsWithoutParsing(rqlRequest.toString());
    }
    
    
    /**
     * Find some structural element with the given name.
     * The implementation is a little strange.
     * 
     * @param name name of a template element
     * @return never null
     * @throws WrongTypeException if name is not a structure element.
     */
    public StructureElement getStructureElement(String name) throws RQLException {
    	try {
    		return getMultiLink(name);
    	} catch (WrongTypeException e) {
    		/* okay, perhaps it's something else... */
    	}
    	
    	try {
    		return getTextAnchor(name);
    	} catch (WrongTypeException e) {
    		/* okay, perhaps it's something else... */
    	}
    	
    	// are there more?
    	throw new WrongTypeException("Not a StructureElement: " + name);
    }
}
