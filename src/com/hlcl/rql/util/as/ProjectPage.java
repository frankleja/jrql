package com.hlcl.rql.util.as;

import java.util.Map;
import java.util.ResourceBundle;

import com.hlcl.rql.as.AuthorizationPackage;
import com.hlcl.rql.as.MultiLinkedPageException;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.PublicationPackage;
import com.hlcl.rql.as.PublishingJob;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.ReddotDate;
import com.hlcl.rql.as.StandardFieldDateElement;
import com.hlcl.rql.as.StandardFieldNumericElement;
import com.hlcl.rql.as.StandardFieldTextElement;
import com.hlcl.rql.as.Template;
import com.hlcl.rql.as.TemplateElement;
import com.hlcl.rql.as.TextElement;
import com.hlcl.rql.as.UnlinkedPageException;
import com.hlcl.rql.as.User;

/**
 * @author lejafr
 * 
 * This class is an abstract super class for all project related page, which wraps the general page and provide specialized interface.
 * This project page acts as a normal page and is superclass for all content class implementations. An interface for the common page
 * functions is needed to synchronize between Page and ProjectPage.
 */
public abstract class ProjectPage {

	private Page page;
	private ScriptParameters parms;

	/**
	 * Constructor
	 */
	public ProjectPage(Page page) {
		super();

		this.page = page;
	}

	/**
	 * Ordnet dieser Seite das gegebene Berechtigungspaket zu.
	 */
	public void assignAuthorizationPackage(AuthorizationPackage authorizationPackage, boolean inherit) throws RQLException {
		getPage().assignAuthorizationPackage(authorizationPackage, inherit);
	}

	/**
	 * Liefert das Template mit dem gegebenen Namen aus dem gegebenen template folder. Benötigt den session key!
	 */
	public Template getTemplateByName(String templateFolderName, String templateName) throws RQLException {
		return getProject().getTemplateByName(templateFolderName, templateName);
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

		return getPage().createAndConnectPageAtContainer(containerTemplateElementName, template, addAtBottom);
	}

	/**
	 * Erhält für HTML Elemente alle eingegebenen Zeichen (< wird zu &lt;). Ein einzelnes blank (space) wird als Textwert geschrieben
	 * (zu &nbsp;).
	 * 
	 * @see #setTextValue(String, String)
	 */
	public void enterText(String templateElementName, String value) throws RQLException {

		getPage().enterText(templateElementName, value);
	}

	/**
	 * Returns the page id, headline and content class name.
	 */
	public String getInfoText() throws RQLException {
		return getPage().getInfoText();
	}

	/**
	 * Returns the page id, headline and content class name getrennt von separator.
	 */
	public String getInfoText(String separator) throws RQLException {
		return getPage().getInfoText(separator);
	}

	/**
	 * Liefert die E-Mailadresse des User, der diese Seite zuletzt geändert hat.
	 */
	public String getLastChangedByUserEmailAddress() throws RQLException {

		return getPage().getLastChangedByUserEmailAddress();
	}

	/**
	 * Liefert den Namen des User, der diese Seite zuletzt geändert hat.
	 */
	public String getLastChangedByUserName() throws RQLException {

		return getPage().getLastChangedByUserName();
	}

	/**
	 * Liefert den Zeitpunkt der letzten Änderung dieser Seite im Format 26 Aug 2009 6:15 pm.
	 */
	public String getLastChangedOnAsddMMyyyyHmma() throws RQLException {

		return getPage().getLastChangedOnAsddMMyyyyHmma();
	}

	/**
	 * Liefert die E-Mailadresse des Users, der die Seite sperrt oder zuletzt gesperrt hat. Kann auch der angemeldete sein.
	 * 
	 * @see #isLocked()
	 */
	public String getLockedByUserEmailAddress() throws RQLException {
		return getPage().getLockedByUserEmailAddress();
	}

	/**
	 * Liefert den Usernamen des Autors, der die Seite sperrt oder zuletzt gesperrt hat. Kann auch der angemeldete sein.
	 * 
	 * @see #isLocked()
	 */
	public String getLockedByUserName() throws RQLException {
		return getPage().getLockedByUserName();
	}

	/**
	 * Liefert den Zeitpunkt seitdem diese Seite gesperrt ist im Format: 27 Aug 2008 1:41 PM.
	 * 
	 * @see #isLocked()
	 */
	public String getLockedSinceAsddMMyyyyHmma() throws RQLException {
		return getPage().getLockedSinceAsddMMyyyyHmma();
	}

	/**
	 * Returns the encapsulated page.
	 */
	public Page getPage() {
		return page;
	}

	/**
	 * Returns the page headline
	 */
	public String getHeadline() throws RQLException {
		return getPage().getHeadline();
	}

	/**
	 * Returns the page headline and ID
	 */
	public String getHeadlineAndId() throws RQLException {
		return getPage().getHeadlineAndId();
	}

	/**
	 * Returns the page headline
	 * 
	 * @deprecated
	 */
	public String getPageHeadline() throws RQLException {
		return getPage().getHeadline();
	}

	/**
	 * Returns the page headline and ID
	 * 
	 * @deprecated
	 */
	public String getPageHeadlineAndId() throws RQLException {
		return getPage().getHeadlineAndId();
	}

	/**
	 * Returns the page id.
	 */
	public String getPageId() throws RQLException {
		return getPage().getPageId();
	}

	/**
	 * Returns the page info: headline, id and template name.
	 */
	public String getPageInfoText() throws RQLException {
		return getPage().getInfoText();
	}

	protected String getParameter(String parameterName) throws RQLException {
		return getParms().get(parameterName);
	}

	/**
	 * Returns the page id with all template name parameters. Would be read in with ScriptParameters.
	 * <p>
	 * Every subclass needs a line within file com/hlcl/rql/util/as/projectPage_parmPageIds.properties.
	 */
	protected String getParametersPageId() {
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.util.as.projectPage_parmPageIds");
		return b.getString(this.getClass().getName());
	}

	/**
	 * Return the script parameters always from the cached parameters in project to speed up.
	 */
	private ScriptParameters getParms() throws RQLException {
		if (parms == null) {
			parms = getProject().getParameters(getParametersPageId(), this.getClass().getName());
		}
		return parms;
	}

	/**
	 * Returns the project from the given page.
	 */
	public Project getProject() {
		return page.getProject();
	}

	/**
	 * Liefert Standardfeld Date Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 5 sein
	 */
	public StandardFieldDateElement getStandardFieldDateElement(String templateElementName) throws RQLException {

		return getPage().getStandardFieldDateElement(templateElementName);
	}

	/**
	 * Liefert den Wert des Standardfeld Date Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 5 sein
	 */
	public ReddotDate getStandardFieldDateValue(String templateElementName) throws RQLException {

		return getPage().getStandardFieldDateValue(templateElementName);
	}

	/**
	 * Liefert Standardfeld Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 1 sein
	 */
	public StandardFieldNumericElement getStandardFieldNumericElement(String templateElementName) throws RQLException {

		return getPage().getStandardFieldNumericElement(templateElementName);
	}

	/**
	 * Liefert den Wert des Standardfeld Numeric Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 48 sein
	 */
	public int getStandardFieldNumericValue(String templateElementName) throws RQLException {

		return getPage().getStandardFieldNumericValue(templateElementName);
	}

	/**
	 * Liefert Standardfeld Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 1 sein
	 */
	public StandardFieldTextElement getStandardFieldTextElement(String templateElementName) throws RQLException {

		return getPage().getStandardFieldTextElement(templateElementName);
	}

	/**
	 * Liefert den Wert des Standardfeld Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 1 sein
	 */
	public String getStandardFieldTextValue(String templateElementName) throws RQLException {

		return getPage().getStandardFieldTextValue(templateElementName);
	}

	/**
	 * Liefert das Template mit der gegebenen GUID vom Project.
	 */
	public Template getTemplateByGuid(String templateGuid) throws RQLException {
		return getProject().getTemplateByGuid(templateGuid);
	}

	/**
	 * Liefert Textelement dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 32 sein
	 */
	public TextElement getTextElement(String templateElementName) throws RQLException {

		return getPage().getTextElement(templateElementName);
	}

	/**
	 * Liefert den Wert des OptionList Elements dieser Seite, das auf dem gegebenen templateElement basiert oder null,
	 * <p>
	 * falls weder diese Optionsliste einen Wert hat noch im Templateelement ein default gesetzt ist.
	 */
	public String getOptionListValue(String templateElementName) throws RQLException {

		return getPage().getOptionListValue(templateElementName);
	}

	/**
	 * Liefert den Wert des Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 32 sein
	 */
	public String getTextValue(String templateElementName) throws RQLException {

		return getPage().getTextValue(templateElementName);
	}

	/**
	 * Liefert true, falls diese Seite durch den angegebenen User änderbar ist. Liefert true, falls diese Seite freigegeben ist.
	 * Liefert true, falls diese Seite im Draft oder auf Korrektur des angemeldeten Users steht.
	 */
	public boolean isChangeable() throws RQLException {
		return getPage().isChangeable();
	}

	/**
	 * Liefert true, falls diese Seite im Status draft ist (neu erstellt oder geändert).
	 * 
	 * Dieser Test wirkt global. Er liefert immer true, falls die Seite im draft steht, egal ob beim angemeldeten Benutzer oder einem
	 * anderen.
	 * 
	 * @see User#getPagesSavedAsDraft(Project)
	 */
	public boolean isInStateSavedAsDraft() throws RQLException {

		return getPage().isInStateSavedAsDraft();
	}

	/**
	 * Liefert true, falls diese Seite gerade von einem anderen (als dem angemeldeten Benutzer) gesperrt ist.
	 * <p>
	 * Nur falls true geliefert wird, liefern die folgenden Methoden von wem und seit wann die Sperre besteht.
	 */
	public boolean isLocked() throws RQLException {
		return getPage().isLocked();
	}

	/**
	 * Liefert true, falls das Textelements dieser Seite, das auf dem gegebenen templateElement basiert, leer ist.
	 */
	public boolean isTextEmpty(String templateElementName) throws RQLException {

		return getPage().isTextEmpty(templateElementName);
	}

	/**
	 * Ändert alle Inhaltselemente dieser Seite mit nur einem RQL request.
	 * 
	 * @param elementValuePairs
	 *            Key=Element Objekt, Value=Objekt mit Wert mit passendem Typ
	 */
	public void setElementValues(Map elementValuePairs) throws RQLException {
		getPage().setElementValues(elementValuePairs);
	}

	/**
	 * Aendert den Dateinamen auf den gegebenen Wert.
	 */
	public void setFilename(String filename) throws RQLException {
		getPage().setFilename(filename);
	}

	/**
	 * Aendert die Überschrift dieser Seite.
	 */
	public void setHeadline(String headline) throws RQLException {
		getPage().setHeadline(headline);
	}

	/**
	 * Aendert den Wert des Standardfeld Date Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 5 sein
	 */
	public void setStandardFieldDateValue(String templateElementName, ReddotDate value) throws RQLException {
		getPage().setStandardFieldDateValue(templateElementName, value);
	}

	/**
	 * Aendert den Wert des Standardfeld Numeric Elements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 48 sein
	 */
	public void setStandardFieldNumericValue(String templateElementName, int value) throws RQLException {
		getPage().setStandardFieldNumericValue(templateElementName, value);
	}

	/**
	 * Aendert den Wert des Standardfeld Textelements dieser Seite, das auf dem gegebenen templateElement basiert.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss vom Typ 1 sein
	 */
	public void setStandardFieldTextValue(String templateElementName, String value) throws RQLException {
		getPage().setStandardFieldTextValue(templateElementName, value);
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
		getPage().setTextValue(templateElementName, htmlSourceCode);
	}

	/**
	 * Workflow: Übergibt eine Seite im Entwurf zur Prüfung an den Workflow. Vom aktuell angemeldeten User aus gesehen. Oder ohne
	 * Workflow, wird die Änderung des Autors an dieser Seite gespeichert, so dass sie für alle sichtbar ist.
	 * 
	 * @return null, falls submit erfolgreich war java.util.List of TemplateElement, mit den fehlenden Pflichtfeldern
	 */
	public java.util.List<TemplateElement> submitToWorkflow() throws RQLException {
		return getPage().submitToWorkflow();
	}

	/**
	 * Liefert true genau dann, wenn die Seite ein Element hat, das auf dem gegebenen TemplateElement basiert.
	 */
	public boolean contains(String templateElementName) throws RQLException {

		return getPage().contains(templateElementName);
	}

	/**
	 * Liefert einen Text, der den Status dieser Seite beschreibt.
	 */
	public String getStateInfo() throws RQLException {

		return getPage().getStateInfo();
	}

	/**
	 * Liefert den Namen des User, der diese Seite erstellt hat. Liefert 'Unknown author', falls der User bereits gelöscht wurde.
	 * 
	 * @see Page#hasCreatedUser()
	 */
	public String getCreatedByUserName() throws RQLException {

		return getPage().getCreatedByUserName();
	}

	/**
	 * Liefert diese oder die Vorgängerseite (über MainLink) zurück, die das gegebene Templateelement besitzt.
	 */
	public Page getPredecessorPageContainingElement(String templateElementName) throws RQLException {
		return getPage().getPredecessorPageContainingElement(templateElementName);
	}

	/**
	 * Liefert den Zeitpunkt der Erstellung dieser Seite.
	 */
	public ReddotDate getCreatedOn() throws RQLException {

		return getPage().getCreatedOn();
	}

	/**
	 * Aendert den Wert der Optionsliste dieser Seite, das auf dem gegebenen templateElement basiert. Der gegebenen Wert value wird dem
	 * Autor angezeigt. Es ist nicht die GUID der OptionListSelection.
	 * 
	 * @param templateElementName
	 *            TemplateElement muss Typ 8 sein
	 */
	public void setOptionListValue(String templateElementName, String value) throws RQLException {
		getPage().setOptionListValue(templateElementName, value);
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

		return getPage().getListChildPages(listTemplateElementName);
	}

	/**
	 * Liefert das Berechtigungspaket (vom Typ=normal=page) dieser Seite, niemals das globale. Liefert null, falls diese Seite kein
	 * Berechtigungspaket hat.
	 */
	public AuthorizationPackage getAuthorizationPackage() throws RQLException {
		return getPage().getAuthorizationPackage();
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
		return getPage().getList(listTemplateElementName);
	}

	/**
	 * Liefert die RedDot GUID der Seite.
	 */
	public String getPageGuid() {
		return getPage().getPageGuid();
	}

	/**
	 * Liefert den Namen des Templates auf diese Seite basiert.
	 */
	public String getTemplateName() throws RQLException {
		return getPage().getTemplateName();
	}

	/**
	 * Zwei Seitenobjekte werden als identisch interpretiert, falls beide die gleiche GUID haben.
	 * 
	 * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
	 * @see java.lang.Boolean#hashCode()
	 * @see java.util.Hashtable
	 */
	public boolean equals(ProjectPage page2) {
		return getPage().equals(page2.getPage());
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
		getPage().addSetOptionListValue(templateElementName, value);
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addSetStandardFieldTextValue(String templateElementName, String value) throws RQLException {
		getPage().addSetStandardFieldTextValue(templateElementName, value);
	}

	/**
	 * Adds the given page element for the given template element to the list of elements which value will be changed on this page.
	 * 
	 * @see #startSetElementValues()
	 * @see #endSetElementValues()
	 */
	public void addSetStandardFieldDateValue(String templateElementName, ReddotDate value) throws RQLException {
		getPage().addSetStandardFieldDateValue(templateElementName, value);
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
	 * @see #addSetOptionListValue(String, String)
	 * @see #addSetStandardFieldNumericValue(String, int)
	 * @see #addSetStandardFieldNumericValue(String, ReddotDate)
	 * @see #addSetStandardFieldTextValue(String, String)
	 * @see #endSetElementValues()
	 */
	public void endSetElementValues() throws RQLException {
		getPage().endSetElementValues();
	}

	/**
	 * Start a mode to add elements which values should be changed.
	 * 
	 * @see #setElementValues(Map)
	 * @see #startSetElementValues()
	 * @see #addSetOptionListValue(String, String)
	 * @see #addSetStandardFieldNumericValue(String, int)
	 * @see #addSetStandardFieldNumericValue(String, ReddotDate)
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
		getPage().startSetElementValues();
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
		return getPage().endNumberOfSetElements();
	}

	/**
	 * Liefert den Dateinamen dieser Seite unter Properties vom CMS zurück. Liefert null oder leeren string, falls keiner gesetzt ist.
	 * <p>
	 * Dies ist nicht der generierte Dateiname auf der site.
	 * 
	 * @see #getPublishedFilename(String)
	 */
	public String getFilename() throws RQLException {
		return getPage().getFilename();
	}

	/**
	 * Returns the name of the authorization package's name (used to identify the user group able to edit this page).
	 * <p>
	 * Returns an empty string, if page didn't have a package.
	 */
	public String getAuthorizationPackageName() throws RQLException {
		return getPage().getAuthorizationPackageName();
	}

	/**
	 * Returns a list of option list element values of this page. Some entries might be null.
	 * <p>
	 * Size of returned list is equal to size of given arguments.
	 * 
	 * @param templateElementNamePattern
	 *            Pattern of the template elementn name containing exactly one argument {0}
	 * @param arguments
	 *            each argument string will be inserted into {0} and the value of this option list element will be collected
	 * 
	 * @see #getOptionListValue(String)
	 */
	public java.util.List<String> getOptionListValues(String templateElementNamePattern, String... arguments) throws RQLException {
		return getPage().getOptionListValues(templateElementNamePattern, arguments);
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
	 * @throws RQLException
	 * 
	 * @see #getOptionListValue(String)
	 */
	public java.util.List<String> getOptionListValues(String templateElementNamePattern, boolean skipEmptyValues, int... arguments)
			throws RQLException {
		return getPage().getOptionListValues(templateElementNamePattern, skipEmptyValues, arguments);
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
		return getPage().getOptionListValues(templateElementNamePattern, skipEmptyValues, arguments);
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
		return getPage().publish(withFollowingPages, projectVariantGuids, separator, languageVariantGuid);
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
		return getPage().getListChildPages(listTemplateElementName, childTemplateName);
	}

	/**
	 * Kopiert die Werte der Optionsliste passend zu namePattern (muss ein {0} enthalten) der gegebenen Seite sourcePage in
	 * gleichnamige Elemete dieser Seite.
	 * <p>
	 * Kopie erfolgt nur, wenn das Element einen eingegebenen Wert besitzt. Der Vorgabewert aus dem Template wird nicht kopiert!
	 */
	public void copyOptionListValuesFrom(Page sourcePage, String namePattern) throws RQLException {
		getPage().copyOptionListValuesFrom(sourcePage, namePattern);
	}

	/**
	 * Kopiert den Wert des Textelements der gegebenen Seite sourcePage, das auf dem gegebenen templateElement basiert in das
	 * gleichnamige Element dieser Seite.
	 */
	public void copyTextValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		getPage().copyTextValueFrom(templateElementName, sourcePage);
	}

	/**
	 * Kopiert den Wert des Imageelements der gegebenen Seite sourcePage, das auf dem gegebenen templateElement basiert in das
	 * gleichnamige Element dieser Seite.
	 */
	public void copyImageValueFrom(String templateElementName, Page sourcePage) throws RQLException {
		getPage().copyImageValueFrom(templateElementName, sourcePage);
	}

	/**
	 * @return true, if this recycling table block is multi linked, otherwise false
	 * @see Page#isMultiLinked()
	 */
	public boolean isMultiLinked() throws RQLException {
		return getPage().isMultiLinked();
	}

	/**
	 * /** Liefert das Exportpaket über das diese Seite generiert werden muss. Liefert gegebenenfalls das globale Exportpaket zurück.
	 * Liefert null, falls kein Exportpaket bestimmt werden kann. This method is quite slow, because of the underlaying RQL, if the
	 * publicatoin packet is comprehensive.
	 * <p>
	 * The main link publication package is cached within this page to speed up publishing path calculation.
	 * <p>
	 * <b>Attention:</b> Do not use this method in same program together with functions to update/change the publication package. The
	 * cache in this page might not be reset accordingly. So it is possible to get an publication package object with the old
	 * (unchanged) data!
	 * 
	 * @see Page#getPublicationPackage()
	 */
	public PublicationPackage getPublicationPackage() throws RQLException {
		return getPage().getPublicationPackage();
	}

	/**
	 * Deletes this project page. This oject cannot be userd afterwards.
	 */
	public void delete() throws RQLException {
		getPage().delete();

		// deactivate this object
		this.page = null;
		this.parms = null;
	}

	/**
	 * @return true, if this recycling table block is multi linked, otherwise false
	 * @throws MultiLinkedPageException
	 * @throws UnlinkedPageException
	 * @see Page#getParentPage()
	 */
	public Page getParentPage() throws RQLException {
		return getPage().getParentPage();
	}
	/**
	 * Gibt den Speicher aller Caches wieder frei für die GC. Dieses Seitenobjekt bleibt voll funktionsfähig! Folgende Zugriffe auf
	 * diese Seite füllen die Caches einfach wieder.
	 * 
	 * @see #clearLanguageVariantDependentCaches()
	 */
	public void freeOccupiedMemory() {
		getPage().freeOccupiedMemory();
	}

}
