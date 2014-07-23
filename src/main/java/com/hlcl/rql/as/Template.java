package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hlcl.rql.util.as.PageArrayList;

/**
 * Diese Klasse beschreibt ein RedDot Template.
 * 
 * @author LEJAFR
 */
public class Template implements TemplateFolderContainer {
	private String description;

	private RQLNode detailsNode;
	// cache
	private RQLNodeList elementNodeList;
	private String name;
	private java.util.List<TemplateVariant> templateVariantsCache;
	private java.util.List<TemplatePublishAssignment> publishAssignmentsCache;

	// maps element guid to object
	private Map<String, TemplateElement> templateElementsCache;
	private TemplateFolder templateFolder;
	private String templateGuid;

	private Boolean selectInNewPage = null;

	/**
	 * Erzeugt ein neues Template.
	 * 
	 * @param templateFolder
	 *            Verzeichnis, in dem dieses Template gespeichert ist
	 * @param name
	 *            Name dieses Templates
	 * @param templateGuid
	 *            RedDot GUID dieses Templates
	 * @param description
	 *            Beschreibung dieses Templates
	 */
	public Template(TemplateFolder templateFolder, String name, String templateGuid, String description) {
		super();

		this.templateFolder = templateFolder;
		this.name = name;
		this.templateGuid = templateGuid;
		this.description = description;
		// initialize
		initializeTemplateElementsCache();
	}

	/**
	 * Ordnet den ersten maxPages Seiten dieses Templates je dem Seitenelement, das auf dem gegebenen templateElement basiert, das
	 * gegebenen Detail-Element-Berechtigungspaket zu.
	 */
	public void assignAuthorizationPackageToPageElements(AuthorizationPackage authorizationPackage, TemplateElement templateElement,
			int maxPages) throws RQLException {
		PageArrayList pages = getAllPages(maxPages);
		for (Iterator iterator = pages.iterator(); iterator.hasNext();) {
			Page page = (Page) iterator.next();
			page.assignAuthorizationPackage(authorizationPackage, templateElement);
		}
	}

	/**
	 * Entfernt alle zugeordneten Plugins von diesem Template (assign plugins).
	 */
	public void removeAllPlugins() throws RQLException {
		removePlugins(getAssignedPlugins());
	}

	/**
	 * Entfernt die gegebenen Plugins von diesem Template (assign plugins). Lässt andere Zuordnungen unverändert; ist subtraktiv.
	 * <p>
	 * Die gegebenen Plugins müssen dem Projekt zugeordnet sein!
	 */
	public void removePlugins(Set<Plugin> pluginsToRemove) throws RQLException {
		Set<Plugin> toSet = getAssignedPlugins();
		toSet.removeAll(pluginsToRemove);
		setPlugins(toSet);
	}

	/**
	 * Lässt nur die gegebenen Plugins an diesem Template zu (assign plugins); alle anderen werden deaktiviert.
	 * <p>
	 * Die gegebenen Plugins müssen dem Projekt zugeordnet sein!
	 */
	public void setPlugins(Set<Plugin> assignedPlugins) throws RQLException {

		/* 
		 V7.5 request
		<IODATA sessionkey="F31867043921409181040B020BBFC6C4" loginguid="D42F04D382884848BE59E0A30A42C5B2">
		<TARGET target="ioTreeApp4015" guid="077C3C07FAC9442790C9C83C1DE89A26">
		<PLUGINS action="save">
		<PLUGIN guid="A24F11B518CB4BE9BEEF72058A96BCAF" checked="1"/>
		<PLUGIN guid="92E2AA232F0B41A3B413D7BF7C1BAF81" checked="1"/>
		<PLUGIN guid="F6B65CE8029B4FD4A63A9A45DB1B492D" checked="1"/>
		</PLUGINS>
		</TARGET>
		</IODATA>
		V7.5 response
		 */

		// build 1st part of request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<TARGET target='ioTreeApp4015' guid='" + getTemplateGuid() + "'>" + "<PLUGINS action='save' >";

		// build request for all plugins needed to set
		java.util.List<Plugin> allPlugins = getCmsClient().getAllPlugins();
		for (Iterator<Plugin> iterator = allPlugins.iterator(); iterator.hasNext();) {
			Plugin plugin = iterator.next();
			rqlRequest += "<PLUGIN guid='" + plugin.getPluginGuid() + "' checked='" + (assignedPlugins.contains(plugin) ? "1" : "0")
					+ "' />";
		}
		// finish request
		rqlRequest += "</PLUGINS></TARGET></IODATA>";

		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Lässt die gegebenen Plugins an diesem Template zu (assign plugins). Lässt andere Zuordnungen unverändert; ist additiv.
	 * <p>
	 * Die gegebenen Plugins müssen dem Projekt zugeordnet sein!
	 */
	public void addPlugins(Set<Plugin> pluginsToAdd) throws RQLException {
		Set<Plugin> toSet = getAssignedPlugins();
		toSet.addAll(pluginsToAdd);
		setPlugins(toSet);
	}

	/**
	 * Ordnet alle Plugins deren Name mit dem gegebenen Prefix beginnen diesem Template zu (assign plugins).
	 * <p>
	 * Lässt andere Zuordnungen unverändert; ist additiv.
	 * <p>
	 * Die gegebenen Plugins müssen dem Projekt zugeordnet sein!
	 */
	public void addPlugins(String pluginNamePrefix) throws RQLException {
		addPlugins(getCmsClient().getPluginsByNamePrefix(pluginNamePrefix));
	}

	/**
	 * Liefert die Publishing-Zuordnung für die gegebene Projektvariante.
	 */
	private TemplatePublishAssignment buildPublishAssignment(RQLNode assignmentNode) throws RQLException {
		ProjectVariant projectVariant = getProject().getProjectVariantByGuid(assignmentNode.getAttribute("projectvariantguid"));
		TemplateVariant templateVariant = getTemplateVariantByGuid(assignmentNode.getAttribute("guid"));
		String notgenerate = assignmentNode.getAttribute("donotgenerate");
		boolean publish = notgenerate == null || notgenerate.equals("0");
		return new TemplatePublishAssignment(projectVariant, templateVariant, publish);
	}

	/**
	 * Erzeugt ein TemplateElement aus dem gegebenen Node. Liefert das Templateelement aus dem Cache, falls vorhanden.
	 */
	private TemplateElement buildTemplateElement(RQLNode elementNode) {
		String elemGuid = elementNode.getAttribute("guid");
		// check if already in cache
		TemplateElement result;
		if (templateElementsCache.containsKey(elemGuid)) {
			result = templateElementsCache.get(elemGuid);
		} else {
			result = new TemplateElement(this, elementNode.getAttribute("eltname"), elemGuid, elementNode.getAttribute("elttype"),
					elementNode.getAttribute("eltisdynamic"), elementNode.getAttribute("eltsuffixes"), elementNode
							.getAttribute("elteditoroptions"), elementNode.getAttribute("eltstylesheetdata"), elementNode
							.getAttribute("eltrddescription"), elementNode.getAttribute("eltfolderguid"), "1".equals(elementNode.getAttribute("eltrequired")));
			templateElementsCache.put(elemGuid, result);
		}
		return result;
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {

		return getTemplateFolder().callCms(rqlRequest);
	}

	/**
	 * Liefert true, falls der Name mit dem gegebenen Suffix endet.
	 */
	public boolean isNameEndsWith(String templateNameSuffix) throws RQLException {

		return getName().endsWith(templateNameSuffix);
	}

	/**
	 * Liefert true, falls der Name den gegebenen namePart beinhaltet.
	 */
	public boolean isNameContains(String namePart) throws RQLException {

		return getName().contains(namePart);
	}

	/**
	 * Liefert true, falls der Name des content class folder den gegebenen namePart beinhaltet; works case sensitive.
	 * 
	 * @see #isTemplateFolderNameContains(String, boolean)
	 */
	public boolean isTemplateFolderNameContains(String namePart) throws RQLException {
		return isTemplateFolderNameContains(namePart, false);
	}

	/**
	 * Liefert true, falls der Name des content class folder den gegebenen namePart beinhaltet.
	 * 
	 * @param ignoreCase
	 *            if true, it works case insensitive
	 */
	public boolean isTemplateFolderNameContains(String namePart, boolean ignoreCase) throws RQLException {
		if (ignoreCase) {
			return getTemplateFolderName().toLowerCase().contains(namePart.toLowerCase());
		} else {
			return getTemplateFolderName().contains(namePart);
		}
	}

	/**
	 * Liefert true, falls der Name dieses Templates mit mindestens einem der gegebenen Namen beginnt.
	 */
	public boolean isNameStartsWith(String... templateNamePrefixes) throws RQLException {
		String templateName = getName();
		for (String name : templateNamePrefixes) {
			if (templateName.startsWith(name)) {
				return true;
			}
		}
		// otherwise
		return false;
	}

	/**
	 * Liefert true, falls der Name dieses Templates auf mindestens einen der gegebenen Namen endet.
	 */
	public boolean isNameEndsWith(String... templateNameSuffixes) throws RQLException {
		String templateName = getName();
		for (String name : templateNameSuffixes) {
			if (templateName.endsWith(name)) {
				return true;
			}
		}
		// otherwise
		return false;
	}

	/**
	 * Liefert true, falls der Name dieses Templates mit mindestens einem der gegebenen Namen übereinstimmt.
	 */
	public boolean isNameEquals(String... templateNames) throws RQLException {
		String templateName = getName();
		for (String name : templateNames) {
			if (templateName.equals(name)) {
				return true;
			}
		}
		// otherwise
		return false;
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines
	 * Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Sammelt in ein Set alle vorbelegten Templates aller MultiLink Templateelemente dieses Templates.
	 * 
	 * @param includeReferences
	 *            =true, auch Elemente, die Referenzquelle sind werden geliefert (haben keine Childs!) =false, ohne Element, die
	 *            Referenzquelle sind (nur diese haben Childs!)
	 * @return java.util.Set of Templates
	 */
	public java.util.Set<Template> collectPreassignedTemplatesOfAllMultiLinkElements(boolean includeReferences) throws RQLException {

		java.util.Set<Template> result = new HashSet<Template>();

		// for all multi link template elements
		java.util.List<TemplateElement> multiLinks = getMultiLinkTemplateElements(includeReferences);
		for (int i = 0; i < multiLinks.size(); i++) {
			TemplateElement linkElem = (TemplateElement) multiLinks.get(i);
			result.addAll(linkElem.getPreassignedTemplates());
		}
		return result;
	}

	/**
	 * Adds a copy of all given content class elements with the given prefix from the source content class.
	 * <p>
	 * Attention: Following will not be copied: Default and sample texts, child element of and references.
	 */
	public void copyElementsFrom(Template sourceTemplate, String namePrefix) throws RQLException {
		copyElementsFrom(sourceTemplate.getTemplateElementsByPrefix(namePrefix));
	}

	/**
	 * Adds a copy of all given content class elements to this content class.
	 * <p>
	 * Attention: Following will not be copied: Default and sample texts, child element of and references.
	 */
	public void copyElementsFrom(java.util.List<TemplateElement> templateElements) throws RQLException {
		for (TemplateElement templateElement : templateElements) {
			copyElementFromPrimitive(templateElement);
		}

		// force new read of elements
		deleteTemplateElementsCache();
	}

	/**
	 * Adds a copy of given content class element to this content class.
	 */
	public void copyElementFrom(TemplateElement templateElement) throws RQLException {

		copyElementFromPrimitive(templateElement);

		// force new read of elements
		deleteTemplateElementsCache();
	}

	/**
	 * Adds a copy of given content class element to this content class. Caches will not be invalidated!
	 * 
	 * @throws RQLException
	 */
	private void copyElementFromPrimitive(TemplateElement templateElement) throws RQLException {
		/* 
		 V7.5 request
		<IODATA loginguid="64509B9AC6F14433860E0B43ECE017D8" sessionkey="57E35FF9AFE34A71A200BC668C24A403">
		  <PROJECT>
		    <TEMPLATE guid="822CD84A8B374C9DB54949EA1F85E448">
		      <ELEMENT action="save" elttype="1" eltname="test_field"
		       eltrddescription="SmartEdit Beschreibung"
		       eltlanguageindependent="1"      />
		    </TEMPLATE>
		  </PROJECT>
		</IODATA>
		V7.5 response
		<IODATA>
		<ELEMENT action="save" elttype="1" eltname="test_field" eltrddescription="SmartEdit Beschreibung" eltlanguageindependent="1" dialoglanguageid="ENG" parentguid="822CD84A8B374C9DB54949EA1F85E448" parenttable="TPL" templateguid="822CD84A8B374C9DB54949EA1F85E448" eltuserdefinedallowed="1" oldname="" guid="F1224D9A7807446391F630485109DB63" type="1" languagevariantid="ENG" changed="-1"/>
		</IODATA>
		 */

		// raise error if already existing
		if (contains(templateElement.getName())) {
			throw new ElementAlreadyExistingException("An Element with name " + templateElement.getName()
					+ " is already existing in template " + getName() + ".");
		}

		// prepare command
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'><PROJECT>"
				+ "    <TEMPLATE guid='" + getTemplateGuid() + "'>" + "<ELEMENT action='save' ";

		// copy all element node attributes into save rql request
		String temp = "";
		RQLNode copyNode = templateElement.getCopyNode();
		for (String key : copyNode.getAttributeKeys()) {
			temp += key + "='" + StringHelper.encodeHtml(copyNode.getAttribute(key)) + "' ";
		}

		// on option list copy selections as well
		if (templateElement.isOptionList()) {
			temp += templateElement.getOptionListSelectionsSaveAttribute();
		}

		// close command
		rqlRequest += temp + "></ELEMENT></TEMPLATE></PROJECT></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert true genau dann, wenn dieses Template ein Element mit dem Namen templateElementName hat.
	 */
	public boolean contains(String templateElementName) throws RQLException {

		TemplateElement templateElementOrNull = findTemplateElementByName(templateElementName);
		return templateElementOrNull != null;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template ein TemplateElement mit dem gleichen Namen wie das gegebene Templateelement
	 * beinhaltet.
	 */
	public boolean contains(TemplateElement templateElement) throws RQLException {

		return contains(templateElement.getName());
	}

	/**
	 * Liefert true, falls der SmartEdit TemplateCode einen roten punkt für den gegebenen Elementnamen enthält.
	 */
	private boolean containsSmartEditTemplateCodeRedDot(String elementName) throws RQLException {
		return containsTemplateCodeRedDot(getSmartEditTemplateCode(), elementName);
	}

	/**
	 * Liefert true, falls der gegebene TemplateCode das gegebene Element elementName verwendet.
	 */
	boolean containsTemplateCode(String templateCode, String elementName) throws RQLException {
		return templateCode.contains(TemplateElement.getTemplateCode(elementName));
	}

	/**
	 * Liefert true, falls der gegebene TemplateCode für das gegebene Element elementName einen roten Punkt enthält.
	 */
	boolean containsTemplateCodeRedDot(String templateCode, String elementName) throws RQLException {
		return templateCode.contains(TemplateElement.getTemplateCodeRedDot(elementName));
	}

	/**
	 * Erstellt ein neues Standardfeld Text an diesem Template.
	 * <p>
	 * Liefert, mangels Konstruktur in Klasse TemplateElement, nur die ElementGUID des angelegten Elements zurück.
	 */
	public TemplateElement createStandardFieldText(String templateElementName, String label, boolean isLanguageDependent)
			throws RQLException {

		/* 
		 V7.5 request
		<IODATA loginguid="64509B9AC6F14433860E0B43ECE017D8" sessionkey="57E35FF9AFE34A71A200BC668C24A403">
		  <PROJECT>
		    <TEMPLATE guid="822CD84A8B374C9DB54949EA1F85E448">
		      <ELEMENT action="save" elttype="1" eltname="test_field"
		       eltrddescription="SmartEdit Beschreibung"
		       eltlanguageindependent="1"      />
		    </TEMPLATE>
		  </PROJECT>
		</IODATA>
		V7.5 response
		<IODATA>
		<ELEMENT action="save" elttype="1" eltname="test_field" eltrddescription="SmartEdit Beschreibung" eltlanguageindependent="1" dialoglanguageid="ENG" parentguid="822CD84A8B374C9DB54949EA1F85E448" parenttable="TPL" templateguid="822CD84A8B374C9DB54949EA1F85E448" eltuserdefinedallowed="1" oldname="" guid="F1224D9A7807446391F630485109DB63" type="1" languagevariantid="ENG" changed="-1"/>
		</IODATA>
		 */

		// raise error if already existing
		if (contains(templateElementName)) {
			throw new ElementAlreadyExistingException("An Element with name " + templateElementName
					+ " is already existing in template " + getName() + ".");
		}

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
				+ "    <TEMPLATE guid='" + getTemplateGuid() + "'>" + "      <ELEMENT action='save' elttype='1' eltname='"
				+ templateElementName + "' eltrddescription='" + label + "' eltlanguageindependent='"
				+ StringHelper.convertTo01(!isLanguageDependent) + "'/>" + "    </TEMPLATE>" + "  </PROJECT>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		// invalidate elements cache
		deleteTemplateElementsCache();

		return getTemplateElementByGuid(rqlResponse.getNode("ELEMENT").getAttribute("guid"));
	}

	/**
	 * Löscht das Templateelement mit dem gegebenen Namen.
	 * 
	 * @param name
	 *            Name des Templateelementes.
	 */
	public void deleteTemplateElementByName(String name) throws RQLException {

		getTemplateElementByName(name).delete();
	}

	/**
	 * Löscht alle gegebenen Templateelemente elementNames, die durch delimiter getrennt sein müssen.
	 */
	public void deleteTemplateElements(String elementNames, String delimiter) throws RQLException {

		String[] names = StringHelper.split(elementNames, delimiter);

		// spread deletion
		for (int i = 0; i < names.length; i++) {
			String elemName = names[i];
			getTemplateElementByName(elemName).delete();
		}
	}

	/**
	 * Templates mit der gleichen GUID werden als gleich betrachtet.
	 */
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		Template other = (Template) obj;
		return getTemplateGuid().equals(other.getTemplateGuid());
	}

	/**
	 * Liefert das Templateelement mit der gegebenen GUID vom CMS zurück.
	 * 
	 * @param templateElementGuid
	 *            GUID des gesuchten Template-Elementes.
	 * @return TemplateElement or null
	 * @see TemplateElement
	 */
	private TemplateElement findTemplateElementByGuid(String templateElementGuid) throws RQLException {

		// find template element
		RQLNodeList templateElementNodeList = getTemplateElementNodeList();
		RQLNode elementNode = null;

		for (int i = 0; i < templateElementNodeList.size(); i++) {
			elementNode = templateElementNodeList.get(i);

			if (elementNode.getAttribute("guid").equals(templateElementGuid)) {
				// wrap template data
				return buildTemplateElement(elementNode);
			}
		}
		return null;
	}

	/**
	 * Liefert das Templateelement mit dem gegebenen Namen vom CMS zurück.
	 * 
	 * @param name
	 *            Name des Templateelementes.
	 * @return TemplateElement or null
	 * @see TemplateElement
	 */
	private TemplateElement findTemplateElementByName(String name) throws RQLException {

		// find template element
		RQLNodeList templateElementNodeList = getTemplateElementNodeList();

		// no element at all
		if (templateElementNodeList == null) {
			return null;
		}

		RQLNode elementNode = null;
		for (int i = 0; i < templateElementNodeList.size(); i++) {
			elementNode = templateElementNodeList.get(i);

			if (elementNode.getAttribute("eltname").equals(name)) {
				// wrap template data
				return buildTemplateElement(elementNode);
			}
		}
		return null;
	}

	/**
	 * Liefert das Administrations-Element mit dem gegebenen Namen vom CMS zurück.
	 * 
	 * @param name
	 *            Name des Administrations-Element
	 * @return TemplateElement
	 * @see TemplateElement
	 */
	public AdministrationElement getAdministrationElementByName(String name) throws RQLException {

		AdministrationElement elem = getAdministrationElementByNamePrimitive(name);
		if (elem == null) {
			throw new ElementNotFoundException("Administration element named " + name + " could not be found in the template "
					+ getName() + ".");
		} else {
			return elem;
		}
	}

	/**
	 * Liefert das Administrations-Element mit dem gegebenen Namen vom CMS zurück oder null, falls es nicht existiert.
	 * 
	 * @param name
	 *            Name des Administrations-Element
	 */
	private AdministrationElement getAdministrationElementByNamePrimitive(String name) throws RQLException {

		/* 
		 V5 request
		 <IODATA loginguid="9C9E4816ECFE417CAF206F0F805CF650" sessionkey="371636704537Cv67o88E"> 
		 <PROJECT>
		 <TEMPLATE guid="5C4E160F28C04BB999108028554AA596">
		 <ADMINISTRATIONELEMENTS action="list" />
		 </TEMPLATE>
		 </PROJECT>
		 </IODATA> 
		 V5 response
		 <IODATA>
		 <ADMINISTRATIONELEMENTS>
		 <ADMINISTRATIONELEMENT guid="FB06DFFF65FD434CA09A6CF628A95905" name="linking information" type="2" />
		 <ADMINISTRATIONELEMENT guid="412656C9F03C426CA2BB78C7A184D447" name="workflow information" type="2" />
		 </ADMINISTRATIONELEMENTS>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
				+ "    <TEMPLATE guid='" + getTemplateGuid() + "'>" + "      <ADMINISTRATIONELEMENTS action='list'/>"
				+ "    </TEMPLATE>" + "  </PROJECT>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNodeList elementNodeList = rqlResponse.getNodes("ADMINISTRATIONELEMENT");

		// check if no admin element at all
		if (elementNodeList == null) {
			return null;
		}

		for (int i = 0; i < elementNodeList.size(); i++) {
			RQLNode elementNode = elementNodeList.get(i);

			if (elementNode.getAttribute("name").equals(name)) {
				// wrap template data
				return new AdministrationElement(this, name, elementNode.getAttribute("guid"), elementNode.getAttribute("type"));
			}
		}
		// not with this name
		return null;
	}

	/**
	 * Gibt alle Seiten zurück, die auf diesem Template basieren. Das Ergebnis könnte sehr groß werden. Benötigt den session key!
	 * 
	 * @return List of Pages
	 */
	public PageArrayList getAllPages(int maxPages) throws RQLException {
		return getProject().getAllPagesBasedOn(this, maxPages);
	}

	/**
	 * Gibt die Anzahl aller Seiten zurück (<=maxPages), die auf diesem Template basieren. Benötigt den session key!
	 */
	public int getAllPagesSize(int maxPages) throws RQLException {
		RQLNodeList nodeList = getProject().getAllPagesBasedOnNodeList(this, maxPages);
		return nodeList == null ? 0 : nodeList.size();
	}

	/**
	 * Liefert eine java.util.List von Ascii Text Templateelementen.
	 */
	public java.util.List<TemplateElement> getAsciiTextTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.TEXT_ASCII_TYPE);
	}

	/**
	 * Liefert eine java.util.List von Ascii Text Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten)
	 * erfüllen.
	 */
	public java.util.List<TemplateElement> getAsciiTextTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getAsciiTextTemplateElements(), namePattern);
	}

	/**
	 * Liefert die zugeordneten Plugins dieses Templates. Replaces with getPlugins().
	 * 
	 * @deprecated since 18 Jan 2011
	 */
	public Set<Plugin> getAssignedPlugins() throws RQLException {
		return getPlugins();
	}

	/**
	 * Liefert die zugeordneten Plugins dieses Templates.
	 */
	public Set<Plugin> getPlugins() throws RQLException {
		/* 
		 V7.5 request
		<IODATA sessionkey="FA311A7B6EFA4B5ABFA3CA4FA3936FA2" loginguid="8CE30DFC58E4445FB66805BD0E0FAD6E">
		<TARGET target="ioTreeApp4015" guid="0064879F9A1D4337A1C488F37731EBB7">
		<PLUGINS action="load"/>
		</TARGET>
		</IODATA>
		V7.5 response
		<TARGET target="ioTreeApp4015" guid="0064879F9A1D4337A1C488F37731EBB7">
		<PLUGINS action="load">
		<PLUGIN guid="5816BE4225134C4595E9D2B4D59A215D" name="Analyzer" active="0" checked="0"/>
		<PLUGIN guid="3ECEB43369364449BACD7F1B3E82815F" name="cancelWaitingJobs.jsp - LOCAL" active="0" checked="0"/>
		<PLUGIN guid="BA7DF9E46EB8450C9BF347D47E709DA2" name="cancelWaitingJobs.jsp - PROD" active="1" checked="0"/>
		<PLUGIN guid="A64E94837D81494CBD3D3055759362D6" name="changeChilds.jsp - LOCAL" active="0" checked="0"/>
		<PLUGIN guid="79F325D9389F4E7EB8484F6537CE9BD2" name="changeChilds.jsp - PROD" active="0" checked="0"/>
		...
		</PLUGINS>
		</TARGET>
		 */

		// build 1st part of request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<TARGET target='ioTreeApp4015' guid='" + getTemplateGuid() + "'>" + "<PLUGINS action='load' >"
				+ "</PLUGINS></TARGET></IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		// maybe no plugin assigned
		Set<Plugin> result = new HashSet<Plugin>();
		RQLNodeList pluginsNodes = rqlResponse.getNodes("PLUGIN");
		if (pluginsNodes == null) {
			return result;
		}

		// wrap checked plugins into objects
		for (int i = 0; i < pluginsNodes.size(); i++) {
			RQLNode node = pluginsNodes.get(i);
			if ("1".equals(node.getAttribute("checked"))) {
				result.add(getCmsClient().buildPlugin(node));
			}
		}
		return result;
	}

	/**
	 * Liefert eine java.util.List von Attribute Templateelementen.
	 */
	public java.util.List<TemplateElement> getAttributeTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.ATTRIBUTE_TYPE);
	}

	/**
	 * Liefert eine java.util.List von Attribute Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten)
	 * erfüllen.
	 */
	public java.util.List<TemplateElement> getAttributeTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getAttributeTemplateElements(), namePattern);
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {

		return getTemplateFolder().getCmsClient();
	}

	
	/**
	 * Liefert eine java.util.List von Container Templateelementen.
	 */
	public java.util.List<TemplateElement> getContainerTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.CONTAINER_TYPE);
	}

	/**
	 * Liefert eine java.util.List von Container Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten)
	 * erfüllen.
	 */
	public java.util.List<TemplateElement> getContainerTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getContainerTemplateElements(), namePattern);
	}

	/**
	 * Liefert alle Inhalts-Templateelemente dieses Templates inklusive referenzierter Elemente.
	 * <p>
	 * Starting with V9.0.1.49 the headline element did not have the attribute language variant dependent. It's moved to the content
	 * class.
	 * <p>
	 * Headline elements are included in versions < V9.0.1.49 and not included since that version.
	 * 
	 * @see #isAdoptHeadlineToAllLanguageVariants()
	 */
	public java.util.List<TemplateElement> getContentElements() throws RQLException {
		return getContentElements(!hasAdoptHeadlineToAllLanguageVariants(), true);
	}

	/**
	 * Liefert alle Inhalts-Templateelemente dieses Templates abhängig von den gegeben Werten.
	 */
	public java.util.List<TemplateElement> getContentElements(boolean includeHeadline, boolean includeReferences) throws RQLException {
		return getContentElements(includeHeadline, includeReferences, null, null);
	}

	/**
	 * Liefert alle Inhalts-Templateelemente dieses Templates abhängig von den gegeben Werten.
	 */
	public java.util.List<TemplateElement> getContentElements(boolean includeHeadline, boolean includeReferences,
			String ignoreElementNames, String separator) throws RQLException {

		java.util.List<TemplateElement> result = new ArrayList<TemplateElement>();

		// find template element
		RQLNodeList templateElementNodeList = getTemplateElementNodeList();

		// no element at all
		if (templateElementNodeList == null) {
			return result;
		}

		RQLNode elementNode = null;
		for (int i = 0; i < templateElementNodeList.size(); i++) {
			elementNode = templateElementNodeList.get(i);
			// check if not needed in result
			if (ignoreElementNames != null
					&& StringHelper.contains(ignoreElementNames, separator, elementNode.getAttribute("name"), true)) {
				continue;
			}
			// filter only content elements
			if (TemplateElement.isContentElementType(elementNode.getAttribute("elttype"), includeHeadline)) {
				// check for references
				if (!includeReferences && elementNode.getAttribute("eltrefelementguid") != null) {
					continue;
				}
				// add to return value and wrap template data
				result.add(buildTemplateElement(elementNode));
			}
		}
		return result;
	}

	/**
	 * Liefert die Beschreibung dieses Templates.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getDescription() {
		return description;
	}

	/**
	 * Liefert den RQLNode mit weiteren Information für dieses Template zurueck.
	 */
	private RQLNode getDetailsNode() throws RQLException {

		/* 
		 V7.5 request
		<IODATA loginguid="F810D978C5044CE9BA9B73DACAB136EB" sessionkey="31BEC8E317AC4E3F83E6745C11A81808">
		<PROJECT>
		<TEMPLATE action="load" guid="F9B0D8D18F524D32BFB75BDB0E5D60E9" />
		</PROJECT>
		</IODATA>
		 V7.5 response
		 <IODATA>
		 <TEMPLATE action="load" guid="E5D49F4326E249A48B74BBB81008011F" languagevariantid="ENG" parentobjectname="PROJECT" useconnection="1" dialoglanguageid="ENG" templaterights="2147483647" 
		 name="article_page" description="a magazine article page" approverequired="0" framesetafterlist="0" filenamerequired="0" ignoreglobalworkflow="0" keywordrequired="0" selectinnewpage="1" 
		 folderguid="6A6740BC44F7459081BFD1F25B1BF8F6" folderrelease="0" praefixguid="2C6FADC2DD774995877C30A8BFB83130" suffixguid="" requiredcategory="">
		 </TEMPLATE>
		 </IODATA>
		 */

		// cache the node with page details information
		if (detailsNode == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ " <TEMPLATE action='load' guid='" + getTemplateGuid() + "'/>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			detailsNode = rqlResponse.getNode("TEMPLATE");
		}
		return detailsNode;
	}

	/**
	 * Liefert eine java.util.List von Frame Templateelementen.
	 */
	public java.util.List<TemplateElement> getFrameTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.FRAME_TYPE);
	}

	/**
	 * Liefert eine java.util.List von Frame Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten) erfüllen.
	 */
	public java.util.List<TemplateElement> getFrameTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getFrameTemplateElements(), namePattern);
	}

	/**
	 * Liefert eine java.util.List von Headline Templateelementen.
	 */
	public java.util.List<TemplateElement> getHeadlineTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.HEADLINE_TYPE);
	}

	/**
	 * Liefert eine java.util.List von Headline Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten) erfüllen.
	 */
	public java.util.List<TemplateElement> getHeadlineTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getHeadlineTemplateElements(), namePattern);
	}

	/**
	 * Liefert eine java.util.List von HTML Text Templateelementen.
	 */
	public java.util.List<TemplateElement> getHtmlTextTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.TEXT_HTML_TYPE);
	}

	/**
	 * Liefert eine java.util.List von HtmlText Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten) erfüllen.
	 */
	public java.util.List<TemplateElement> getHtmlTextTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getHtmlTextTemplateElements(), namePattern);
	}

	/**
	 * Liefert eine java.util.List von Image Templateelementen.
	 */
	public java.util.List<TemplateElement> getImageTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.IMAGE_TYPE);
	}

	/**
	 * Liefert eine java.util.List von Image Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten) erfüllen.
	 */
	public java.util.List<TemplateElement> getImageTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getImageTemplateElements(), namePattern);
	}

	/**
	 * Liefert eine java.util.List von Image- und Media-Templateelementen.
	 */
	public java.util.List<TemplateElement> getFolderTemplateElements() throws RQLException {
		List<TemplateElement> result = getImageTemplateElements();
		result.addAll(getMediaTemplateElements());
		return result;
	}

	/**
	 * Liefert eine java.util.List von Folder Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten) erfüllen.
	 */
	public java.util.List<TemplateElement> getFolderTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getFolderTemplateElements(), namePattern);
	}

	/**
	 * Liefert eine java.util.List von Info Templateelementen.
	 */
	public java.util.List<TemplateElement> getInfoTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.INFO_TYPE);
	}

	/**
	 * Liefert eine java.util.List von Info Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten) erfüllen.
	 */
	public java.util.List<TemplateElement> getInfoTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getInfoTemplateElements(), namePattern);
	}

	/**
	 * Liefert eine java.util.List Templateelemente, die ein Label (RD nennt das Feld description) haben.
	 */
	public java.util.List<TemplateElement> getLabeledTemplateElements() throws RQLException {
		// get all elements
		RQLNodeList templateElementNodeList = getTemplateElementNodeList();
		java.util.List<TemplateElement> elements = new ArrayList<TemplateElement>();

		// no template elements at all
		if (templateElementNodeList == null) {
			return elements;
		}

		for (int i = 0; i < templateElementNodeList.size(); i++) {
			RQLNode elementNode = templateElementNodeList.get(i);
			// select labeled types only
			if (TemplateElement.isLabeled(elementNode.getAttribute("elttype"))) {
				// wrap template data
				elements.add(buildTemplateElement(elementNode));
			}
		}
		return elements;
	}

	/**
	 * Liefert alle Templateelemente dieses Templates, die Sprachvariantenabhängig sind.
	 */
	public java.util.List<TemplateElement> getLanguageVariantDependentTemplateElements() throws RQLException {

		java.util.List<TemplateElement> result = new ArrayList<TemplateElement>();

		// filter
		for (TemplateElement templateElement : getContentElements()) {
			if (templateElement.isLanguageVariantDependent()) {
				result.add(templateElement);
			}
		}

		return result;
	}

	/**
	 * Liefert alle Templateelemente dieses Templates, die Sprachvariantenunabhängig sind.
	 */
	public java.util.List<TemplateElement> getLanguageVariantIndependentTemplateElements() throws RQLException {

		java.util.List<TemplateElement> result = new ArrayList<TemplateElement>();

		// filter
		for (TemplateElement templateElement : getContentElements()) {
			if (templateElement.isLanguageVariantIndependent()) {
				result.add(templateElement);
			}
		}

		return result;
	}

	/**
	 * Liefert eine java.util.List von List Templateelementen.
	 */
	public java.util.List<TemplateElement> getListTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.LIST_TYPE);
	}

	/**
	 * Liefert eine java.util.List von List Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten) erfüllen.
	 */
	public java.util.List<TemplateElement> getListTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getListTemplateElements(), namePattern);
	}

	/**
	 * Liefert die Logon GUID vom Container.
	 * 
	 * @see Project
	 */
	public String getLogonGuid() {

		return getTemplateFolder().getLogonGuid();
	}

	/**
	 * Liefert eine java.util.List von Media Templateelementen.
	 */
	public java.util.List<TemplateElement> getMediaTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.MEDIA_TYPE);
	}

	/**
	 * Liefert eine java.util.List von Media Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten) erfüllen.
	 */
	public java.util.List<TemplateElement> getMediaTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getMediaTemplateElements(), namePattern);
	}

	/**
	 * Liefert alle MultiLink Templateelemente. MultiLinks, die Referenzquellen sind werden mitgeliefert.
	 * 
	 * @return java.util.List of TemplateElements
	 */
	public java.util.List<TemplateElement> getMultiLinkTemplateElements() throws RQLException {

		return getMultiLinkTemplateElements(true);
	}

	/**
	 * Liefert alle MultiLink Templateelemente (LIST_TYPE, CONTAINER_TYPE).
	 * 
	 * @param includeReferences
	 *            =true, auch Elemente, die Referenzquelle sind werden geliefert (haben keine Childs!) =false, ohne Element, die
	 *            Referenzquelle sind (nur diese haben Childs!)
	 * @return java.util.List of TemplateElements
	 */
	public java.util.List<TemplateElement> getMultiLinkTemplateElements(boolean includeReferences) throws RQLException {
		RQLNodeList allElements = getTemplateElementNodeList();
		java.util.List<TemplateElement> out = new ArrayList<TemplateElement>(allElements == null ? 0 : allElements.size());

		// no multi links in template
		if (allElements == null) {
			return out;
		}

		for (RQLNode elementNode : allElements) {

			// include list or container only
			int type = Integer.parseInt(elementNode.getAttribute("elttype"));
			if (type == TemplateElement.LIST_TYPE || type == TemplateElement.CONTAINER_TYPE) {
				if (!includeReferences && elementNode.getAttribute("eltrefelementguid") != null) {
					continue;
				}
				// wrap template data
				out.add(buildTemplateElement(elementNode));
			}
		}
		return out;
	}


	/**
	 * Liefert alle Struktur-Templateelemente (LIST_TYPE, CONTAINER_TYPE, TEXT_ANCHOR,s tatic).
	 * 
	 * @param includeReferences
	 *            =true, auch Elemente, die Referenzquelle sind werden geliefert (haben keine Childs!) =false, ohne Element, die
	 *            Referenzquelle sind (nur diese haben Childs!)
	 * @return java.util.List of TemplateElements
	 */
	public java.util.List<TemplateElement> getStructureTemplateElements(boolean includeReferences) throws RQLException {
		RQLNodeList allElements = getTemplateElementNodeList();
		java.util.List<TemplateElement> out = new ArrayList<TemplateElement>(allElements == null ? 0 : allElements.size());

		// no multi links in template
		if (allElements == null) {
			return out;
		}

		for (RQLNode elementNode : allElements) {

			// include list or container only
			int type = Integer.parseInt(elementNode.getAttribute("elttype"));
			if (type == TemplateElement.LIST_TYPE || type == TemplateElement.CONTAINER_TYPE || type == TemplateElement.ANCHOR_TEXT_TYPE) {
				if (!includeReferences && elementNode.getAttribute("eltrefelementguid") != null) {
					continue;
				}
				// wrap template data
				TemplateElement e = buildTemplateElement(elementNode);
				if (type == TemplateElement.ANCHOR_TEXT_TYPE && e.isTextAnchor()) {
					out.add(e);
				} else if (type != TemplateElement.ANCHOR_TEXT_TYPE) {
					out.add(e);
				} else {
					// dynamic text anchor
				}
			}
		}
		return out;
	}

	
	
	
	/**
	 * Liefert alle MultiLink Templateelemente mit vorgegebenen namen.
	 * 
	 * @param names
	 *            a List of template element names
	 * @return java.util.List of TemplateElements
	 */
	public java.util.List<TemplateElement> getMultiLinkTemplateElements(java.util.List names) throws RQLException {

		// find template element
		java.util.List source = getMultiLinkTemplateElements();
		java.util.List<TemplateElement> target = new ArrayList<TemplateElement>();
		TemplateElement templateElement = null;

		for (int i = 0; i < source.size(); i++) {
			templateElement = (TemplateElement) source.get(i);

			// collect only template elements with the given names
			if (names.contains(templateElement.getName())) {
				target.add(templateElement);
			}
		}
		return target;
	}

	/**
	 * Liefert den Namen der content class.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getName() {
		return name;
	}

	/**
	 * Liefert den Namen des content class folders und dieser content class in der Form 'folder name/name'.
	 */
	public String getNameWithFolder() {
		return getTemplateFolderName() + "/" + getName();
	}

	/**
	 * Liefert eine java.util.List von OptionList Templateelementen.
	 */
	public java.util.List<TemplateElement> getOptionListTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.OPTIONLIST_TYPE);
	}

	/**
	 * Liefert eine java.util.List von Templateelementen, deren Namen zum namePattern (muss genau ein {0} enthalten) passen.
	 */
	private java.util.List<TemplateElement> selectTemplateElementsByName(java.util.List<TemplateElement> source, String namePattern)
			throws RQLException {
		Pattern pattern = StringHelper.convertFormatPattern2Regex(namePattern);
		java.util.List<TemplateElement> result = new ArrayList<TemplateElement>();
		for (TemplateElement templateElement : source) {
			Matcher matcher = pattern.matcher(templateElement.getName());
			if (matcher.find()) {
				result.add(templateElement);
			}
		}
		return result;
	}

	/**
	 * Liefert eine java.util.List von OptionList Templateelementen. namePattern muss genau ein {0} enthalten.
	 */
	public java.util.List<TemplateElement> getOptionListTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getOptionListTemplateElements(), namePattern);
	}

	/**
	 * Liefert das Präfixobject dieses Templates. Falls kein Präfix definiert ist, wird null geliefert.
	 */
	private Affix getPrefix() throws RQLException {
		String prefixGuid = getPrefixGuid();
		return prefixGuid.length() == 0 ? null : getProject().getAffixByGuid(prefixGuid);
	}

	/**
	 * Liefert die GUID des Prefixe dieses Template.
	 */
	private String getPrefixGuid() throws RQLException {
		return getDetailsNode().getAttribute("praefixguid");
	}

	/**
	 * Liefert die Eigenschaft "Changing headline is effective for all language variants" dieses Template.
	 * <p>
	 * Da diese Eigenschaft erst seit V9.0.1.49 existiert, wird in früheren Versionen immer false geliefert.
	 */
	public boolean isAdoptHeadlineToAllLanguageVariants() throws RQLException {
		return hasAdoptHeadlineToAllLanguageVariants()
				&& StringHelper.convertToBooleanFrom01(getDetailsNode().getAttribute("adoptheadlinetoalllanguages"));
	}

	/**
	 * Ändert die Eigenschaft 'Changing headline is effective for all language variants' für diese content class.
	 * <p>
	 * Attention: New feature since V9.0.1.49!
	 */
	public void setIsAdoptHeadlineToAllLanguageVariants(boolean adoptHeadlineToAllLanguageVariants) throws RQLException {
		save("adoptheadlinetoalllanguages", StringHelper.convertTo01(adoptHeadlineToAllLanguageVariants));
	}

	/**
	 * Liefert true, falls die Eigenschaft "Changing headline is effective for all language variants" an diesem Template existiert.
	 * <p>
	 * Dies ist ab V9.0.1.49 der Fall. In früheren Version wird false geliefert.
	 */
	public boolean hasAdoptHeadlineToAllLanguageVariants() throws RQLException {
		return getDetailsNode().getAttribute("adoptheadlinetoalllanguages") != null;
	}

	/**
	 * Ändert in diesem Template das gegebenen Attribut auf den gegebenen Wert.
	 */
	private void save(String attributeName, String attributeValue) throws RQLException {

		/* 
		 V9 request
		<IODATA loginguid="F810D978C5044CE9BA9B73DACAB136EB" sessionkey="31BEC8E317AC4E3F83E6745C11A81808">
		  <PROJECT>
		    <TEMPLATE action="save" guid="F9B0D8D18F524D32BFB75BDB0E5D60E9" adoptheadlinetoalllanguages="0"/>
		  </PROJECT>
		</IODATA> 
		 V9 response
		<IODATA>F9B0D8D18F524D32BFB75BDB0E5D60E9
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'><PROJECT>"
				+ "<TEMPLATE action='save' guid='" + getTemplateGuid() + "' " + attributeName + "='" + attributeValue
				+ "'/></PROJECT></IODATA>";
		callCms(rqlRequest);

		// delete details cache
		detailsNode = null;
	}

	/**
	 * Liefert den Prefixnamen dieses Templates. z.B. leaf_list_
	 * <p>
	 * Liefert null, falls kein Präfix definiert ist.
	 */
	public String getPrefixName() throws RQLException {
		Affix prefixOrNull = getPrefix();
		if (prefixOrNull == null) {
			return null;
		}
		return prefixOrNull.getName();
	}

	/**
	 * Liefert das Projekt.
	 */
	public Project getProject() {

		return getTemplateFolder().getProject();
	}

	/**
	 * Liefert die RedDot GUID des Projekts.
	 */
	public String getProjectGuid() throws RQLException {
		return getTemplateFolder().getProjectGuid();
	}

	/**
	 * Liefert die Publishing-Zuordnung für die gegebene Projektvariante oder null, falls keine gefunden wurde.
	 */
	public TemplatePublishAssignment getPublishAssignment(ProjectVariant projectVariant) throws RQLException {
		return getPublishAssignment(projectVariant.getProjectVariantGuid());
	}

	/**
	 * Liefert die Publishing-Zuordnung für die gegebene Guid der Projektvariante oder null, falls keine definiert sind (keine
	 * TemplateVarianten).
	 */
	public TemplatePublishAssignment getPublishAssignment(String projectVariantGuid) throws RQLException {
		for (TemplatePublishAssignment assignment : getPublishAssignments()) {
			if (assignment.getProjectVariantGuid().equals(projectVariantGuid)) {
				return assignment;
			}
		}
		throw new ElementNotFoundException("Publish assignment for project variant with uid " + projectVariantGuid
				+ " cannot be found in template " + getName());
	}

	/**
	 * Liefert die RQLNodeList mit allen Zuordnungen zwischen Templatevarianten und Projektvarianten. Reddot: Assign Project Variant
	 * auf dem Templates node einer Content-Klasse.
	 * 
	 * @return RQLNodeList
	 * @see RQLNodeList
	 */
	private RQLNodeList getPublishAssignmentNodeList() throws RQLException {
		/* 
		 V6.5 request
		 <IODATA loginguid="8601812988C14A58BD35090DCFAAFE1E" sessionkey="1021834323P8KPp744v0A">
		 <PROJECT>
		 <TEMPLATE guid="CE92011D1E7E407FAD74BE4E2E6C6C2F">
		 <TEMPLATEVARIANTS action="projectvariantslist" />
		 </TEMPLATE>
		 </PROJECT>
		 </IODATA> 
		 V6.5 response
		 <IODATA>
		 <TEMPLATE languagevariantid="ENG" parentobjectname="PROJECT" useconnection="1" dialoglanguageid="ENG" guid="CE92011D1E7E407FAD74BE4E2E6C6C2F">
		 <TEMPLATEVARIANTS action="projectvariantslist" languagevariantid="ENG" dialoglanguageid="ENG" templateguid="CE92011D1E7E407FAD74BE4E2E6C6C2F" folderguid="">
		 <TEMPLATEVARIANT draft="0" guid="70761D18042B48A2965D790CE50803BD" projectvariantguid="AF0CA920091C4A449E16B2C42B81C2B3"/>
		 <TEMPLATEVARIANT draft="0" guid="70761D18042B48A2965D790CE50803BD" projectvariantguid="8491B46045904DA6A7F1836B06271436" donotgenerate="1"/>
		 <TEMPLATEVARIANT draft="0" guid="70761D18042B48A2965D790CE50803BD" projectvariantguid="C51B659BD3D74998A4F74DB896211387"/>
		 <TEMPLATEVARIANT draft="0" guid="70761D18042B48A2965D790CE50803BD" projectvariantguid="CCA1669D6F124CED85706E0EB573EE77"/>
		 <TEMPLATEVARIANT draft="0" guid="70761D18042B48A2965D790CE50803BD" projectvariantguid="B372E579A1954CFCBA74375B02230EDA"/>
		 <TEMPLATEVARIANT draft="0" guid="70761D18042B48A2965D790CE50803BD" projectvariantguid="9E3D18531F994269B89ECE39845A7F8A"/>
		 <TEMPLATEVARIANT draft="0" guid="D597B5959E15497ABD79181603B25DD6" projectvariantguid="9F922A0194F648C580367248A7C901A6"/>
		 <TEMPLATEVARIANT draft="0" guid="70761D18042B48A2965D790CE50803BD" projectvariantguid="797D786045E04D38A02B84488EB5D843"/>
		 <TEMPLATEVARIANT draft="0" guid="95155CC2E1564317BD476A7EA576A9D9" projectvariantguid="60DDFC7B99AF4860A0DD0E950C823E46"/>
		 <TEMPLATEVARIANT draft="0" guid="D597B5959E15497ABD79181603B25DD6" projectvariantguid="A333ED0B60424DBDBB2CD64B8F8BA6F6"/>
		 <TEMPLATEVARIANT draft="0" guid="70761D18042B48A2965D790CE50803BD" projectvariantguid="7FADE58885E540ED83A046949C209FC0"/>
		 <TEMPLATEVARIANT draft="0" guid="95155CC2E1564317BD476A7EA576A9D9" projectvariantguid="5681A8F68BDF467D9B6F044EDD035492"/>
		 </TEMPLATEVARIANTS>
		 </TEMPLATE>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
				+ "    <TEMPLATE guid='" + getTemplateGuid() + "'>" + "      <TEMPLATEVARIANTS action='projectvariantslist'/>"
				+ "    </TEMPLATE>" + "  </PROJECT>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNodes("TEMPLATEVARIANT");
	}

	/**
	 * Liefert für alle Projektvarianten die Publishing-Zuordnungen.
	 * 
	 * @return list of TemplatePublishAssignment
	 */
	public java.util.List<TemplatePublishAssignment> getPublishAssignments() throws RQLException {
		if (publishAssignmentsCache == null) {
			publishAssignmentsCache = new ArrayList<TemplatePublishAssignment>();
			RQLNodeList assignmentNodeList = getPublishAssignmentNodeList();

			// check for empty list
			if (assignmentNodeList == null) {
				return publishAssignmentsCache;
			}

			// convert into objects
			for (int i = 0; i < assignmentNodeList.size(); i++) {
				RQLNode node = (RQLNode) assignmentNodeList.get(i);
				publishAssignmentsCache.add(buildPublishAssignment(node));
			}
		}
		return publishAssignmentsCache;
	}

	/**
	 * Liefert alle Publishing-Zuordnungen dieses Templates, deren Projektvariante projectVariantPart enthält, check with contains().
	 * <p>
	 * Liefert eine leere Liste, falls keine gefunden wurde.
	 */
	public java.util.List<TemplatePublishAssignment> getPublishAssignments(String projectVariantNamePart) throws RQLException {
		java.util.List<TemplatePublishAssignment> result = new ArrayList<TemplatePublishAssignment>();

		for (TemplatePublishAssignment templatePublishAssignment : getPublishAssignments()) {
			if (templatePublishAssignment.getProjectVariantName().contains(projectVariantNamePart)) {
				result.add(templatePublishAssignment);
			}
		}
		return result;
	}

	/**
	 * Liefert die TemplateVariante dieses Templates, die für die gegebene Projektvariante gepublished wird.
	 */
	public TemplateVariant getPublishedTemplateVariantFor(ProjectVariant projectVariant) throws RQLException {
		return getPublishAssignment(projectVariant).getTemplateVariant();
	}

	/**
	 * Liefert die TemplateVariante dieses Templates, die für die gegebene Projektvariante gepublished wird.
	 */
	public TemplateVariant getPublishedTemplateVariantFor(String projectVariantGuid) throws RQLException {
		return getPublishAssignment(projectVariantGuid).getTemplateVariant();
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {

		return getTemplateFolder().getSessionKey();
	}

	/**
	 * Liefert eine Liste der GUIDs aller Templateelemente, für die es Elemente mit deren Namen + shadowElementsNameSuffix gibt.
	 * Liefert eine leere Liste, falls keine gefunden wurden. Beispiel: suffix = workflow_unlinked_flag Liste der Elemente, deren Namen
	 * auf diesen Suffix enden = list_of_section_workflow_unlinked_flag, media_files_workflow_unklinked_flag Methode liefert die GUIDs
	 * der TemplateElemente list_of_section und media_files, falls diese existieren.
	 * 
	 * @param shadowElementsNameSuffix
	 *            Suffix der Schattenelemente
	 * @return list of TemplateElement GUIDs
	 * @see #getShadowedTemplateElements(String)
	 */
	public String getShadowedTemplateElementGuids(String shadowElementsNameSuffix, String delimiter) throws RQLException {
		// convert to guids
		String result = "";
		java.util.List elements = getShadowedTemplateElements(shadowElementsNameSuffix);
		for (int i = 0; i < elements.size(); i++) {
			TemplateElement element = (TemplateElement) elements.get(i);
			result += element.getTemplateElementGuid() + delimiter;
		}
		result = StringHelper.trim(result, delimiter);
		return result;
	}

	/**
	 * Liefert eine Liste aller Templateelemente, für die es Elemente mit deren Namen + shadowElementsNameSuffix gibt. Liefert eine
	 * leere Liste, falls keine gefunden wurden. Beispiel: suffix = workflow_unlinked_flag Liste der Elemente, deren Namen auf diesen
	 * Suffix enden = list_of_section_workflow_unlinked_flag, media_files_workflow_unklinked_flag Methode liefert die TemplateElemente
	 * list_of_section und media_files, falls diese existieren.
	 * 
	 * @param shadowElementsNameSuffix
	 *            Suffix der Schattenelemente
	 * @return list of TemplateElements
	 * @see #getShadowedTemplateElementGuids(String, String)
	 */
	public java.util.List<TemplateElement> getShadowedTemplateElements(String shadowElementsNameSuffix) throws RQLException {

		// build list of names of searched elements
		String searchedNames = "";
		String delimiter = ",";
		java.util.List shadowElements = getTemplateElementsBySuffix(shadowElementsNameSuffix);
		for (int i = 0; i < shadowElements.size(); i++) {
			TemplateElement shadowElement = (TemplateElement) shadowElements.get(i);
			// remove suffix
			searchedNames += StringHelper.replace(shadowElement.getName(), shadowElementsNameSuffix, "") + delimiter;
		}
		// remove last ,
		searchedNames = StringHelper.trim(searchedNames, delimiter);

		// collect all elements by searchedNames
		java.util.List<TemplateElement> result = new ArrayList<TemplateElement>();
		RQLNodeList templateElementNodeList = getTemplateElementNodeList();
		RQLNode elementNode = null;
		for (int i = 0; i < templateElementNodeList.size(); i++) {
			elementNode = templateElementNodeList.get(i);
			if (StringHelper.contains(searchedNames, delimiter, elementNode.getAttribute("eltname"), true)) {
				result.add(buildTemplateElement(elementNode));
			}
		}

		return result;
	}

	/**
	 * Change the template code for Smart Edit template variant appending a HTML comment with the usage of the given element. Comment
	 * is added even the element is used already within the template code, there is no check before!
	 * 
	 * @param surroundCommentWithIoRangeReddotMode
	 *            true= surround the comment with an IoRangeRedDot Tag, so the comment will only appear in Smart Edit Mode, false= no
	 *            surrounding
	 * @throws ElementNotFoundException
	 *             if the given template element is not contained in the template this variant belongs to.
	 */
	public void appendSmartEditTemplateCodeAddComment(TemplateElement referencedElement, boolean surroundCommentWithIoRangeReddotMode)
			throws RQLException {
		getSmartEditTemplateVariant().appendTemplateCodeAddComment(referencedElement, surroundCommentWithIoRangeReddotMode);
	}

	/**
	 * Liefert den Templatecode der Templatevariante zurück, die für die Display format Projektvariante konfiguriert ist.
	 * <p>
	 * Oder: Liefert den Templatecode zurück, der im SmartEdit angezeigt wird.
	 * 
	 * @see #getSmartEditTemplateVariant() using same RQL command
	 */
	public String getSmartEditTemplateCode() throws RQLException {

		/* 
		 V7.5 request
		<IODATA loginguid="2DAEC2FD2A6048D08C3B96ABC377CD60" sessionkey="4F8FEFF083B04626AB147558933ACDE6">
		  <PROJECT>
		    <TEMPLATE guid="B98F20944FE646F8A10522B00DDE9C35">
		      <TEMPLATEVARIANTS action="loadfirst" />
		    </TEMPLATE>
		  </PROJECT>
		</IODATA>
		V7.5 response
		<IODATA>
		<TEMPLATE languagevariantid="DEU" parentobjectname="PROJECT" useconnection="1" dialoglanguageid="ENG" guid="0B496BBEF7764906A7286C0757415F05" name="olb_page" templaterights="2147483647">
		<TEMPLATEVARIANT action="load" readonly="1" languagevariantid="DEU" dialoglanguageid="ENG" templaterights="2147483647" templateguid="0B496BBEF7764906A7286C0757415F05" flags="256" description="" fileextension="xml" doxmlencode="0" nostartendmarkers="1" insertstylesheetinpage="0" containerpagereference="0" createdate="39637.4113773148" createuserguid="4324D172EF4342669EAF0AD074433393" createusername="lejafr" changeddate="39741.5923148148" changeduserguid="4324D172EF4342669EAF0AD074433393" changedusername="lejafr" pdforientation="default" guid="8443F16189A0419AAE677AF2E37DE4EC" name="page_config_XML" draft="0" waitforrelease="0">
		&lt;!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"&gt;
		&lt;html xmlns="http://www.w3.org/1999/xhtml"&gt;
		&lt;head&gt;
		&lt;meta http-equiv="Content-Type" content="text/html; charset=utf-8" /&gt;
		...
		&lt;/body&gt;
		&lt;/html&gt;
		</TEMPLATEVARIANT>
		</TEMPLATE>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
				+ "    <TEMPLATE guid='" + getTemplateGuid() + "'>" + "      <TEMPLATEVARIANTS action='loadfirst' />"
				+ "    </TEMPLATE>" + "  </PROJECT>" + "</IODATA>";
		String rqlResponse = callCmsWithoutParsing(rqlRequest);
		return StringHelper.unescapeHTML(StringHelper.getTextBetweenTag(rqlResponse, "TEMPLATEVARIANT"));
	}

	/**
	 * Returns the template variant used in Smart Edit.
	 * 
	 * @see #getSmartEditTemplateCode() using same RQL command
	 */
	public TemplateVariant getSmartEditTemplateVariant() throws RQLException {

		/* 
		 V7.5 request
		<IODATA loginguid="2DAEC2FD2A6048D08C3B96ABC377CD60" sessionkey="4F8FEFF083B04626AB147558933ACDE6">
		  <PROJECT>
		    <TEMPLATE guid="B98F20944FE646F8A10522B00DDE9C35">
		      <TEMPLATEVARIANTS action="loadfirst" />
		    </TEMPLATE>
		  </PROJECT>
		</IODATA>
		V7.5 response
		<IODATA>
		<TEMPLATE languagevariantid="DEU" parentobjectname="PROJECT" useconnection="1" dialoglanguageid="ENG" guid="0B496BBEF7764906A7286C0757415F05" name="olb_page" templaterights="2147483647">
		<TEMPLATEVARIANT action="load" readonly="1" languagevariantid="DEU" dialoglanguageid="ENG" templaterights="2147483647" templateguid="0B496BBEF7764906A7286C0757415F05" flags="256" description="" fileextension="xml" doxmlencode="0" nostartendmarkers="1" insertstylesheetinpage="0" containerpagereference="0" createdate="39637.4113773148" createuserguid="4324D172EF4342669EAF0AD074433393" createusername="lejafr" changeddate="39741.5923148148" changeduserguid="4324D172EF4342669EAF0AD074433393" changedusername="lejafr" pdforientation="default" guid="8443F16189A0419AAE677AF2E37DE4EC" name="page_config_XML" draft="0" waitforrelease="0">
		&lt;!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"&gt;
		&lt;html xmlns="http://www.w3.org/1999/xhtml"&gt;
		&lt;head&gt;
		&lt;meta http-equiv="Content-Type" content="text/html; charset=utf-8" /&gt;
		...
		&lt;/body&gt;
		&lt;/html&gt;
		</TEMPLATEVARIANT>
		</TEMPLATE>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
				+ "    <TEMPLATE guid='" + getTemplateGuid() + "'>" + "      <TEMPLATEVARIANTS action='loadfirst' />"
				+ "    </TEMPLATE>" + "  </PROJECT>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return getTemplateVariantByGuid(rqlResponse.getNode("TEMPLATEVARIANT").getAttribute("guid"));
	}

	/**
	 * Liefert eine java.util.List von StandardField Date Templateelementen.
	 */
	public java.util.List<TemplateElement> getStandardFieldDateTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.STANDARDFIELD_DATE_TYPE);
	}

	/**
	 * Liefert eine java.util.List von StandardField Email Templateelementen.
	 */
	public java.util.List<TemplateElement> getStandardFieldEmailTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.STANDARDFIELD_EMAIL_TYPE);
	}

	/**
	 * Liefert eine java.util.List von StandardField URL Templateelementen.
	 */
	public java.util.List<TemplateElement> getStandardFieldUrlTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.STANDARDFIELD_URL_TYPE);
	}

	/**
	 * Liefert eine java.util.List von StandardFieldDate Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten)
	 * erfüllen.
	 */
	public java.util.List<TemplateElement> getStandardFieldDateTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getStandardFieldDateTemplateElements(), namePattern);
	}

	/**
	 * Liefert eine java.util.List von StandardField Numeric Templateelementen.
	 */
	public java.util.List<TemplateElement> getStandardFieldNumericTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.STANDARDFIELD_NUMERIC_TYPE);
	}

	/**
	 * Liefert eine java.util.List von StandardFieldNumeric Templateelementen, deren namen das gegebenen namePattern (muss {0}
	 * enthalten) erfüllen.
	 */
	public java.util.List<TemplateElement> getStandardFieldNumericTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getStandardFieldNumericTemplateElements(), namePattern);
	}

	/**
	 * Liefert eine java.util.List von StandardField Text Templateelementen.
	 */
	public java.util.List<TemplateElement> getStandardFieldTextTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.STANDARDFIELD_TEXT_TYPE);
	}

	/**
	 * Liefert eine java.util.List von StandardFieldText Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten)
	 * erfüllen.
	 */
	public java.util.List<TemplateElement> getStandardFieldTextTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getStandardFieldTextTemplateElements(), namePattern);
	}

	/**
	 * Liefert das Templateelement mit der gegebenen GUID vom CMS zurück.
	 * 
	 * @param templateElementGuid
	 *            GUID des gesuchten Templateelementes.
	 * @return TemplateElement
	 * @see TemplateElement
	 */
	public TemplateElement getTemplateElementByGuid(String templateElementGuid) throws RQLException {

		TemplateElement templateElementOrNull = findTemplateElementByGuid(templateElementGuid);

		if (templateElementOrNull == null) {
			throw new ElementNotFoundException("Template element with GUID " + templateElementGuid
					+ " could not be found in the template " + getName() + ".");
		}
		return templateElementOrNull;
	}

	/**
	 * Liefert das Templateelement mit dem gegebenen Namen vom CMS zurück.
	 * 
	 * @param name
	 *            Name des Templateelementes.
	 * @return TemplateElement
	 * @see TemplateElement
	 */
	public TemplateElement getTemplateElementByName(String name) throws RQLException {

		TemplateElement templateElementOrNull = findTemplateElementByName(name);

		if (templateElementOrNull == null) {
			throw new ElementNotFoundException("Template element named " + name + " could not be found in the template " + getName()
					+ ".");
		}
		return templateElementOrNull;
	}

	/**
	 * Liefert die RQLNodeList mit allen Elementen dieses Templates.
	 * 
	 * @return RQLNodeList
	 * @see RQLNodeList
	 */
	private RQLNodeList getTemplateElementNodeList() throws RQLException {

		/* 
		 V5 request
		 <IODATA loginguid="2A51E6531D1A4D02935BBCF4CD05A3EA" sessionkey="421138853e8oC524a665">
		 <PROJECT> 
		 <TEMPLATE action="load" guid="EA4EEEC6734E4458AF8F1FF4E1427201"> 
		 <ELEMENTS childnodesasattributes="1" action="load"/>
		 </TEMPLATE>
		 </PROJECT>
		 </IODATA> 
		 V5 response
		 <IODATA>
		 <TEMPLATE action="load" guid="EA4EEEC6734E4458AF8F1FF4E1427201" languagevariantid="ENG" parentobjectname="PROJECT" useconnection="1" dialoglanguageid="ENG" templaterights="2147483647" name="node" description="menu item followed by a further menu item" approverequired="0" framesetafterlist="0" filenamerequired="0" ignoreglobalworkflow="0" webserverpreview="0" selectinnewpage="0" folderguid="4C5E6DEA5CF4424DB44553E12484B54B" folderrelease="0" praefixguid="" suffixguid="">
		 <ELEMENTS childnodesasattributes="1" action="load" languagevariantid="ENG" dialoglanguageid="ENG" parentguid="EA4EEEC6734E4458AF8F1FF4E1427201" parenttable="TPL">
		 
		 <ELEMENT eltname="container" 	elttype="28" eltid="1" 	eltislink="2" languagevariantid="ENG" guid="3DED4381C1314A348138D73E97346803" templateguid="EA4EEEC6734E4458AF8F1FF4E1427201" eltfolderguid="" eltistargetcontainer="0" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0"/>
		 
		 <ELEMENT eltname="id" 		elttype="1002" eltid="2" eltislink="0" languagevariantid="ENG" eltsubtype="7" eltformatbutton="1" guid="FCFD5794C8EB42DEA20E9D5AC66F3572" templateguid="EA4EEEC6734E4458AF8F1FF4E1427201" 	eltfolderguid="" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0"/>
		 <ELEMENT eltname="lastModified" elttype="1002" eltid="3" eltislink="0" languagevariantid="ENG" eltsubtype="1" eltformatbutton="2" guid="593EF294186A438ABE8ACE2B6C5F6664" templateguid="EA4EEEC6734E4458AF8F1FF4E1427201" eltfolderguid="" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0" eltformatting="YYYYMMDD" eltformatno="-1"/>
		 <ELEMENT eltname="loginguid" 	elttype="1002" eltid="5" eltislink="0" languagevariantid="ENG" eltsubtype="24" eltformatbutton="1" guid="CA0BDF46A86A45A3BC5F88F0A0B1D5DF" templateguid="EA4EEEC6734E4458AF8F1FF4E1427201"  eltfolderguid="" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0"/>
		 <ELEMENT eltname="pageguid" 	 elttype="1002" eltid="7" eltislink="0" languagevariantid="ENG" eltsubtype="17" eltformatbutton="1" guid="ADF11C5B6E814AD38B2F2CA77A86A006" templateguid="EA4EEEC6734E4458AF8F1FF4E1427201" eltfolderguid="" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0"/>
		 <ELEMENT eltname="sessionguid" elttype="1002" eltid="6" eltislink="0" languagevariantid="ENG" eltsubtype="28" eltformatbutton="1" guid="EB9433EDFA1F4EE29A8C84A14252AEAF" templateguid="EA4EEEC6734E4458AF8F1FF4E1427201" eltfolderguid="" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0"/>
		 
		 <ELEMENT eltname="title" 		elttype="12" eltid="4" 	eltislink="0" languagevariantid="ENG" guid="2CF3DDF397DB40B191EBA946C386A5CA" templateguid="EA4EEEC6734E4458AF8F1FF4E1427201" eltfolderguid="" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0"/>
		 <ELEMENT eltname="dynLink" elttype="26" eltisdynamic="1" languagevariantid="DEU" elttargetcontainerguid="" guid="1BB634828C1246D5A4AB7CBF637EA955" templateguid="F0CEF2CAD9C241B2A26D5242E0766372" eltid="2" eltislink="1" eltfolderguid="" eltcrlftobr="0" eltonlyhrefvalue="0" eltrequired="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0"/>
		 </ELEMENTS>
		 </TEMPLATE>
		 </IODATA>
		 media file 
		 <ELEMENT eltsuffixes="htm;css;xml" languagevariantid="ENG" eltpresetalt="1" eltignoreworkflow="0" eltlanguageindependent="0" eltconvertmode="NO" guid="8369E7139AEB48038DB8BFF218C3BDB9" templateguid="80F10160A84043AAA248AE4E52163873" eltname="media_file" elttype="38" eltid="33" eltislink="0" eltfolderguid="DB7234C07B6947EE8AACD6E271DDF32C" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="1" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0" eltalt=""/>
		 image element
		<ELEMENT languagevariantid="ENG" eltlanguageindependent="0" eltignoreworkflow="0" eltconvert="0" eltautoheight="1" eltautowidth="1" eltautoborder="1" eltpresetalt="0" guid="38B6F21D7A614F2EB42F11FBA03F8BAE" templateguid="049ED6976DE74C81B3C5FDBBBB6B298C" eltname="filetype" elttype="2" eltid="37" eltislink="0" eltfolderguid="C66CE50C719B4F13BE25EBF53895AC0D" eltsuffixes="jpg;jpeg;gif;png" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="0" usepagemainlinktargetcontainer="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="1" eltinvisibleinpage="0" elthideinform="0" eltdragdrop="0" eltxhtmlcompliant="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0" eltflags="8" eltrdexamplesubdirguid="" eltsrc="pdf.gif" eltalt="" eltsrcsubdirguid="C66CE50C719B4F13BE25EBF53895AC0D" eltrddescription="TYPE of file"/>
		 option list
		 <ELEMENT eltname="chapter_main_headline_style" elttype="8" eltoptionlistdata="&lt;SELECTIONS&gt;&lt;SELECTION guid=&quot;2D1CE49EB55E4D9DA0469B7B6D38D4A3&quot; description=&quot;Headline level 1 (H1)&quot; value=&quot;h1&quot;/&gt;&lt;SELECTION guid=&quot;EC076685CE5E48E5A2E00A98DDB31B34&quot; description=&quot;Headline level 2 (H2)&quot; value=&quot;h2&quot;/&gt;&lt;/SELECTIONS&gt;" languagevariantid="ENG" eltparentelementguid="2F93FF8C590849DB8014B14FA2688196" eltparentelementname="chapter_main_number" guid="B43CC82F676748ABB924922AE575BE98" templateguid="DC4F5C047D684D49A1F390AE67F1E63F" eltid="44" eltislink="0" eltfolderguid="" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0" eltdefaultvalue="EC076685CE5E48E5A2E00A98DDB31B34" eltrddescription="HEADLINE LEVEL of main" />
		 */

		// retrieve elements only once
		if (elementNodeList == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
					+ "    <TEMPLATE action='load' guid='" + getTemplateGuid() + "'>"
					+ "      <ELEMENTS childnodesasattributes='1' action='load'/>" + "    </TEMPLATE>" + "  </PROJECT>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			elementNodeList = rqlResponse.getNodes("ELEMENT");
		}
		return elementNodeList;
	}

	/**
	 * Liefert alle Templateelemente dieses Templates.
	 */
	public java.util.List<TemplateElement> getTemplateElements() throws RQLException {

		java.util.List<TemplateElement> result = new ArrayList<TemplateElement>();

		// find template element
		RQLNodeList templateElementNodeList = getTemplateElementNodeList();

		// no element at all
		if (templateElementNodeList == null) {
			return result;
		}

		RQLNode elementNode = null;
		for (int i = 0; i < templateElementNodeList.size(); i++) {
			elementNode = templateElementNodeList.get(i);
			// wrap template data
			result.add(buildTemplateElement(elementNode));
		}
		return result;
	}

	/**
	 * Liefert eine Liste aller Templateelemente, deren Name mit dem gegebenen Suffix enden, case sensitiv.
	 * <p>
	 * Liefert eine leere Liste, falls keine gefunden wurden.
	 * 
	 * @param nameSuffix
	 *            Suffix der zu suchenden Elemente (z.B. workflow_unlinked_flag)
	 * @return list of TemplateElements
	 */
	public java.util.List<TemplateElement> getTemplateElementsBySuffix(String nameSuffix) throws RQLException {

		RQLNodeList nodeList = getTemplateElementNodeList();

		// collect all with given suffix
		java.util.List<TemplateElement> elements = new ArrayList<TemplateElement>();
		for (int i = 0; i < nodeList.size(); i++) {
			RQLNode node = nodeList.get(i);
			if (node.getAttribute("eltname").endsWith(nameSuffix)) {
				elements.add(buildTemplateElement(node));
			}
		}

		return elements;
	}

	/**
	 * Liefert eine Liste aller Templateelemente, deren Name mit dem gegebenen Prefix beginnen, case sensitiv.
	 * <p>
	 * Liefert eine leere Liste, falls keine gefunden wurden.
	 * 
	 * @param namePrefix
	 *            Prefix der zu suchenden Elemente
	 * @return list of TemplateElements
	 */
	public java.util.List<TemplateElement> getTemplateElementsByPrefix(String namePrefix) throws RQLException {

		RQLNodeList nodeList = getTemplateElementNodeList();

		// collect all with given suffix
		java.util.List<TemplateElement> elements = new ArrayList<TemplateElement>();
		for (int i = 0; i < nodeList.size(); i++) {
			RQLNode node = nodeList.get(i);
			if (node.getAttribute("eltname").startsWith(namePrefix)) {
				elements.add(buildTemplateElement(node));
			}
		}

		return elements;
	}

	/**
	 * Liefert alle Templateelemente des gegebenen Typs.
	 * 
	 * @return java.util.List of TemplateElements
	 * @see TemplateElement for possible types
	 */
	public java.util.List<TemplateElement> getTemplateElementsByType(int type) throws RQLException {

		// get all elements
		RQLNodeList templateElementNodeList = getTemplateElementNodeList();
		java.util.List<TemplateElement> elements = new ArrayList<TemplateElement>();

		// no template elements at all
		if (templateElementNodeList == null) {
			return elements;
		}

		String typeStr = Integer.toString(type);
		for (int i = 0; i < templateElementNodeList.size(); i++) {
			RQLNode elementNode = templateElementNodeList.get(i);

			// select type
			if (elementNode.getAttribute("elttype").equals(typeStr)) {
				// wrap template data
				elements.add(buildTemplateElement(elementNode));
			}
		}
		return elements;
	}

	/**
	 * Liefert alle Templateelemente des gegebenen Typs und Namens.
	 * 
	 * @param elementNamePattern
	 *            Muster (mit genau einem wildcard *) für die Prüfung des Elementnamens, arbeitet case sensitive
	 * @return java.util.List of TemplateElements
	 * @see TemplateElement for possible types
	 */
	public java.util.List<TemplateElement> getTemplateElementsByType(int type, String elementNamePattern) throws RQLException {

		// get all elements
		RQLNodeList templateElementNodeList = getTemplateElementNodeList();
		java.util.List<TemplateElement> elements = new ArrayList<TemplateElement>();

		// no template elements at all
		if (templateElementNodeList == null) {
			return elements;
		}

		String typeStr = Integer.toString(type);
		for (int i = 0; i < templateElementNodeList.size(); i++) {
			RQLNode elementNode = templateElementNodeList.get(i);

			// select type and name
			if (elementNode.getAttribute("elttype").equals(typeStr)
					&& StringHelper.matches(elementNode.getAttribute("eltname"), elementNamePattern)) {
				// wrap template data
				elements.add(buildTemplateElement(elementNode));
			}
		}
		return elements;
	}

	/**
	 * Liefert den Template-Folder, in dem dieses Template enthalten ist.
	 * 
	 * @return TemplateFolder
	 */
	public TemplateFolder getTemplateFolder() {
		return templateFolder;
	}

	/**
	 * Liefert die Template Folder GUID.
	 * 
	 */
	public String getTemplateFolderGuid() {

		return getTemplateFolder().getTemplateFolderGuid();
	}

	/**
	 * Liefert den Namen des Template Folders.
	 * 
	 */
	public String getTemplateFolderName() {

		return getTemplateFolder().getName();
	}

	/**
	 * Returns true, if the name of the template folder of this template ends with one of the given suffixes.
	 * <p>
	 * Returns false, if template folder doesn't end with one of the given suffixes.
	 */
	public boolean isTemplateFolderNameEndsWith(String... folderNameSuffixes) throws RQLException {
		String templateFolderName = getTemplateFolderName();
		for (String name : folderNameSuffixes) {
			if (templateFolderName.endsWith(name)) {
				return true;
			}
		}
		// otherwise
		return false;
	}

	/**
	 * Insert the method's description here.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getTemplateGuid() {
		return templateGuid;
	}

	/**
	 * Liefert die TemplateVariante mit der gegebenen GUID zurück.
	 * 
	 * @param templateVariantGuid
	 *            GUID der gesuchten TemplateVariante.
	 */
	public TemplateVariant getTemplateVariantByGuid(String templateVariantGuid) throws RQLException {

		// find template element
		RQLNodeList templateVariantNodeList = getTemplateVariantNodeList();
		RQLNode variantNode = null;

		for (int i = 0; i < templateVariantNodeList.size(); i++) {
			variantNode = templateVariantNodeList.get(i);
			String guid = variantNode.getAttribute("guid");
			if (guid.equals(templateVariantGuid)) {
				// wrap data
				return new TemplateVariant(this, guid, variantNode.getAttribute("name"));
			}
		}
		throw new ElementNotFoundException("Template variant with guid " + templateVariantGuid + " cannot be found in template "
				+ getName());
	}

	/**
	 * Liefert die TemplateVariante mit dem gegebenen Namen zurück oder null falls nicht gefunden.
	 * 
	 * @param templateVariantName
	 *            Name der gesuchten TemplateVariante, z.B. HTML, DEL_XML, XML
	 */
	public TemplateVariant getTemplateVariantByName(String templateVariantName) throws RQLException {

		// find template element
		RQLNodeList templateVariantNodeList = getTemplateVariantNodeList();
		if (templateVariantNodeList == null) {
			return null;
		}
		RQLNode variantNode = null;

		for (int i = 0; i < templateVariantNodeList.size(); i++) {
			variantNode = templateVariantNodeList.get(i);
			String name = variantNode.getAttribute("name");
			if (name.equals(templateVariantName)) {
				// wrap data
				return new TemplateVariant(this, variantNode.getAttribute("guid"), name);
			}
		}
		return null;
	}

	/**
	 * Liefert die Namen aller TemplateVarianten dieses Templates.
	 * 
	 * @return java.util.List of TemplateVariantNames
	 */
	public java.util.List<String> getTemplateVariantNames() throws RQLException {

		// get all elements
		RQLNodeList templateVariantNodeList = getTemplateVariantNodeList();
		java.util.List<String> names = new ArrayList<String>();

		// no template elements at all
		if (templateVariantNodeList == null) {
			return names;
		}

		for (int i = 0; i < templateVariantNodeList.size(); i++) {
			RQLNode variantNode = templateVariantNodeList.get(i);
			names.add(variantNode.getAttribute("name"));
		}
		return names;
	}

	/**
	 * Liefert die Namen aller TemplateVarianten dieses Templates separiert mit dem gegebenen separator.
	 */
	public String getTemplateVariantNames(String separator) throws RQLException {
		return StringHelper.toString(getTemplateVariantNames(), separator);
	}

	/**
	 * Liefert die RQLNodeList mit allen TemplateVarianten dieses Templates.
	 * 
	 * @return RQLNodeList
	 * @see RQLNodeList
	 */
	private RQLNodeList getTemplateVariantNodeList() throws RQLException {
		/* 
		 V6.5 request
		 <IODATA loginguid="8601812988C14A58BD35090DCFAAFE1E" sessionkey="1021834323P8KPp744v0A">
		 <PROJECT>
		 <TEMPLATE guid="CE92011D1E7E407FAD74BE4E2E6C6C2F">
		 <TEMPLATEVARIANTS action="list"/>
		 </TEMPLATE>
		 </PROJECT>
		 </IODATA>  
		 V6.5 response
		 <IODATA>
		 <TEMPLATE languagevariantid="ENG" parentobjectname="PROJECT" useconnection="1" dialoglanguageid="ENG" guid="CE92011D1E7E407FAD74BE4E2E6C6C2F">
		 <TEMPLATEVARIANTS action="list" languagevariantid="ENG" dialoglanguageid="ENG" templateguid="CE92011D1E7E407FAD74BE4E2E6C6C2F" folderguid="" lockuserguid="" lockdate="" lockusername="" lockusermail="" lock="0" draft="0" waitforrelease="0">
		 <TEMPLATEVARIANT guid="95155CC2E1564317BD476A7EA576A9D9" name="DEL_XML" description="" isstylesheet="0" doxmlencode="0" draft="0" waitforrelease="0" nostartendmarkers="1" createdate="38831,4372569444" createuserguid="4324D172EF4342669EAF0AD074433393" createusername="lejafr" changeddate="38831,4380902778" changeduserguid="4324D172EF4342669EAF0AD074433393" changedusername="lejafr"/>
		 <TEMPLATEVARIANT guid="70761D18042B48A2965D790CE50803BD" name="HTML" description="" isstylesheet="0" doxmlencode="0" draft="0" waitforrelease="0" nostartendmarkers="0" createdate="37748,6578125" changeddate="38904,6156481481" changeduserguid="4324D172EF4342669EAF0AD074433393" changedusername="lejafr"/>
		 <TEMPLATEVARIANT guid="D597B5959E15497ABD79181603B25DD6" name="XML" description="" isstylesheet="0" doxmlencode="0" draft="0" waitforrelease="0" nostartendmarkers="0" createdate="38278,6304282407" changeddate="0"/>
		 </TEMPLATEVARIANTS>
		 </TEMPLATE>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <PROJECT>"
				+ "    <TEMPLATE action='load' guid='" + getTemplateGuid() + "'>" + "      <TEMPLATEVARIANTS action='list'/>"
				+ "    </TEMPLATE>" + "  </PROJECT>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNodes("TEMPLATEVARIANT");
	}

	/**
	 * Liefert alle TemplateVarianten dieses Templates.
	 * 
	 * @return java.util.List of TemplateVariants
	 */
	public java.util.List<TemplateVariant> getTemplateVariants() throws RQLException {

		if (templateVariantsCache == null) {
			templateVariantsCache = new ArrayList<TemplateVariant>();

			// get all elements
			RQLNodeList templateVariantNodeList = getTemplateVariantNodeList();

			// no template elements at all
			if (templateVariantNodeList == null) {
				return templateVariantsCache;
			}

			for (int i = 0; i < templateVariantNodeList.size(); i++) {
				RQLNode variantNode = templateVariantNodeList.get(i);
				templateVariantsCache
						.add(new TemplateVariant(this, variantNode.getAttribute("guid"), variantNode.getAttribute("name")));
			}
		}
		return templateVariantsCache;
	}

	/**
	 * Liefert eine java.util.List von Text Anchor Templateelementen.
	 */
	public java.util.List<TemplateElement> getTextAnchorTemplateElements() throws RQLException {
		return getTemplateElementsByType(TemplateElement.ANCHOR_TEXT_TYPE);
	}

	/**
	 * Liefert eine java.util.List von TextAnchor Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten)
	 * erfüllen.
	 */
	public java.util.List<TemplateElement> getTextAnchorTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getTextAnchorTemplateElements(), namePattern);
	}

	/**
	 * Liefert eine java.util.List von Text Templateelementen, sowohl Ascii als auch HTML Text-Templatelementen.
	 */
	public java.util.List<TemplateElement> getTextTemplateElements() throws RQLException {
		java.util.List<TemplateElement> elems = getHtmlTextTemplateElements();
		elems.addAll(getAsciiTextTemplateElements());
		return elems;
	}

	/**
	 * Liefert eine java.util.List von Text Templateelementen, deren namen das gegebenen namePattern (muss {0} enthalten) erfüllen.
	 */
	public java.util.List<TemplateElement> getTextTemplateElements(String namePattern) throws RQLException {
		return selectTemplateElementsByName(getTextTemplateElements(), namePattern);
	}

	/**
	 * Liefert alle Elemente dieses Templates, die in keiner Templatevariante (im SourceCode) verwendet werden.
	 * <p>
	 * D.h. diese Elemente haben in allen Templatevarianten auch keinen roten Punkt.
	 */
	public java.util.List<TemplateElement> getUnusedTemplateElements() throws RQLException {
		java.util.List<TemplateElement> result = new ArrayList<TemplateElement>();

		// select unused elements
		java.util.List<TemplateElement> elements = getTemplateElements();
		for (TemplateElement templateElement : elements) {
			if (!usesTemplateElement(templateElement.getName())) {
				result.add(templateElement);
			}
		}
		return result;
	}

	/**
	 * Liefert true, falls dieses Template ein Admin element mit dem gegebenen Namen hat.
	 */
	public boolean hasAdministrationElement(String name) throws RQLException {
		return getAdministrationElementByNamePrimitive(name) != null;
	}

	/**
	 * HashCode wir auf GUID zurückgeführt.
	 */
	public int hashCode() {

		return getTemplateGuid().hashCode();
	}

	/**
	 * initialize the elements cache
	 */
	private void initializeTemplateElementsCache() {
		templateElementsCache = new HashMap<String, TemplateElement>();
	}

	/**
	 * Deletes the cache for elements and forces a new read.
	 */
	void deleteTemplateElementsCache() {
		elementNodeList = null;
		initializeTemplateElementsCache();
	}

	/**
	 * Liefert true, falls das gegebenen TemplateElement einen roten Punkt im SmartEdit HTML code besitzt, sonst false.
	 */
	public boolean isElementChangeableInSmartEditByName(String elementName) throws RQLException {
		// search for the red dot in display format template code
		return containsSmartEditTemplateCodeRedDot(elementName);
	}

	/**
	 * Liefert true, falls das gegebenen TemplateElement einen roten Punkt im SmartEdit HTML code besitzt, sonst false.
	 */
	public boolean isElementChangeableInSmartEditByName(TemplateElement templateElement) throws RQLException {
		// search for the red dot in display format template code
		return containsSmartEditTemplateCodeRedDot(templateElement.getName());
	}

	/**
	 * Zieht eine Referenz in den ersten maxPages Seiten dieses Templates zu je dem Seitenelement, das auf dem gegebenen
	 * templateElement basiert.
	 * <p>
	 * Die Seitenelemente werden auf das gegebene targetElement verwiesen.
	 */
	public void referencePageElements(TemplateElement templateElement, StandardFieldTextElement targetElement, int maxPages)
			throws RQLException {

		PageArrayList pages = getAllPages(maxPages);
		for (Iterator iterator = pages.iterator(); iterator.hasNext();) {
			Page page = (Page) iterator.next();
			page.getStandardFieldTextElement(templateElement.getName()).referenceTo(targetElement);
			page.freeOccupiedMemory();
		}
	}

	/**
	 * Zieht eine Referenz in den ersten maxPages Seiten dieses Templates zu je dem Seitenelement, das auf dem gegebenen
	 * templateElement basiert.
	 * <p>
	 * Die Seitenelemente werden auf das gegebene targetElement verwiesen.
	 */
	public void referencePageElements(TemplateElement templateElement, TextElement targetElement, int maxPages) throws RQLException {

		PageArrayList pages = getAllPages(maxPages);
		for (Iterator iterator = pages.iterator(); iterator.hasNext();) {
			Page page = (Page) iterator.next();
			page.getTextElement(templateElement.getName()).referenceTo(targetElement);
		}
	}

	/**
	 * Zieht eine Referenz zwischen dem Templateelement mit dem gegebenen Namen in diesem Templates zum gegebenen targetLink einer
	 * Seite.
	 * <p>
	 * Die Links in den ersten maxPages Seiten werden auf den gegebenen targetLink verwiesen.
	 */
	public void referencePageLinks(String templateElementName, MultiLink targetLink, int maxPages) throws RQLException {
		referencePageLinks(getTemplateElementByName(templateElementName), targetLink, maxPages);
	}

	/**
	 * Zieht eine Referenz in den ersten maxPages Seiten dieses Templates zu je dem Seitenlink, das auf dem gegebenen templateElement
	 * basiert.
	 * <p>
	 * Die Seitenlinks werden auf den gegebenen targetLink verwiesen.
	 */
	public void referencePageLinks(TemplateElement templateElement, MultiLink targetLink, int maxPages) throws RQLException {

		PageArrayList pages = getAllPages(maxPages);
		for (Iterator iterator = pages.iterator(); iterator.hasNext();) {
			Page page = (Page) iterator.next();
			page.getMultiLink(templateElement.getName()).referenceTo(targetLink);
		}
	}

	/**
	 * Ändert an der TemplateVariante dieses Templates mit dem gegebenen Namen, ob diese TemplateVariante gepublished wird oder nicht.
	 * Die der TemplateVariante zugeordnete ProjectVariante wird nicht verändert!
	 * 
	 * @param projectVariant
	 *            die Projektvariante, für die publish geändert werden soll
	 * @return null, if no publishing assignment is found or the assignment, if the change was successful
	 */
	public TemplatePublishAssignment setPublishAssignment(ProjectVariant projectVariant, boolean publish) throws RQLException {
		TemplatePublishAssignment assignment = getPublishAssignment(projectVariant);
		if (assignment != null) {
			assignment.setPublish(publish);
			return assignment;
		}
		// identify nothing changed
		return null;
	}

	/**
	 * Ändert für alle PublishAssignments mit dem gegebenen project variant partial name (check with contains()), das publish yes or no
	 * setting.
	 * 
	 * @return true, if at least on setting was changed, otherwise false.
	 */
	public boolean setPublishAssignment(String projectVariantPart, boolean publish) throws RQLException {
		java.util.List<TemplatePublishAssignment> assignments = getPublishAssignments(projectVariantPart);
		if (assignments.size() == 0) {
			return false;
		}

		// distribute set
		for (TemplatePublishAssignment templatePublishAssignment : assignments) {
			templatePublishAssignment.setPublish(publish);
		}
		// identify changes
		return true;
	}

	/**
	 * For debugging only.
	 */
	public String toString() {
		return this.getClass().getName() + " (" + getName() + ")";
	}

	/**
	 * Liefert true, falls der gegebene TemplateCode das gegebene Element elementName verwendet oder dafür einen roten Punkt enthält.
	 */
	boolean usesTemplateCodeElement(String templateCode, String elementName) throws RQLException {
		return containsTemplateCode(templateCode, elementName) || containsTemplateCodeRedDot(templateCode, elementName);
	}

	/**
	 * Liefert true, falls das gegebene Element elementName in keiner TemplateVariante im Quellcode verwendet wird oder einen roten
	 * Punkt enthält.
	 */
	public boolean usesTemplateElement(String elementName) throws RQLException {
		java.util.List<TemplateVariant> variants = getTemplateVariants();
		for (TemplateVariant templateVariant : variants) {
			if (templateVariant.usesTemplateCodeElement(elementName)) {
				return true;
			}
		}
		// not fount
		return false;
	}

	/**
	 * Liefert true, falls das gegebene Element element in keiner TemplateVariante im Quellcode verwendet wird oder einen roten Punkt
	 * enthält.
	 */
	public boolean usesTemplateElement(TemplateElement element) throws RQLException {
		return usesTemplateElement(element.getName());
	}

	/**
	 * Liefert true, falls Seiten dieser Klasse sprachvariantenabhängig sind. D.h. mindestens ein Kontent-Elemente (inkl. Headline
	 * falls < 9.0.1.49, aber ohne referenced elements) dieses Templates sprachvariantenabhängig ist, sonst false.
	 */
	public boolean isLanguageVariantDependent() throws RQLException {
		if (hasAdoptHeadlineToAllLanguageVariants() && !isAdoptHeadlineToAllLanguageVariants()) {
			return true;
		}
		List<TemplateElement> contentElements = getContentElements(!hasAdoptHeadlineToAllLanguageVariants(), false);
		for (TemplateElement elem : contentElements) {
			if (elem.isLanguageVariantDependent()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Liefert true, falls Seiten dieser Klasse sprachvariantenUNabhängig sind. D.h. alle Kontent-Elemente (inkl. Headline, aber ohne
	 * referenced elements) dieses Templates sprachvariantenUNabhängig sind, sonst false.
	 */
	public boolean isLanguageVariantIndependent() throws RQLException {
		return !isLanguageVariantDependent();
	}

	
	/**
	 * Links: Soll dieses Tempate erlaubt sein?
	 */
	public void setSelectInNewPage(boolean v) {
		this.selectInNewPage = v;
	}

	
	public boolean getSelectInNewPage() {
		if (selectInNewPage == null)
			throw new IllegalStateException("setSelectInNewPage() has never been called.");

		return this.selectInNewPage.booleanValue();
	}
}
