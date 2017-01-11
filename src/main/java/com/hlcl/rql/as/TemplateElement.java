package com.hlcl.rql.as;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Diese Klasse beschreibt ein Templateelement. Dies können sowohl RedDot MultiLinks als auch Textfelder sein.
 * 
 * @author LEJAFR
 */
public class TemplateElement implements TemplateContainer {
	static final char FOLDER_SUFFIXES_SEPARATOR_CHAR = ';';

	// different element types
	static final int ANCHOR_IMAGE_TYPE = 27;
	static final int ANCHOR_TEXT_TYPE = 26;
	static final int ATTRIBUTE_TYPE = 1003;
	static final int CONTAINER_TYPE = 28;
	static final int FRAME_TYPE = 3;
	static final int HEADLINE_TYPE = 12;
	static final int IMAGE_TYPE = 2;
	static final int INFO_TYPE = 1002;
	// group all element types with a label text (rd calls it description) together for a convenient type check
	private static final java.util.List<Integer> LABELED_TYPES = new ArrayList<Integer>();
	// group all content element types
	private static final java.util.List<Integer> CONTENT_TYPES = new ArrayList<Integer>();
	private static final java.util.List<Integer> CONTENT_WITHOUT_HEADLINE_TYPES = new ArrayList<Integer>();

	// group all structural element types
	private static final java.util.List<Integer> STRUCTURAL_TYPES = new ArrayList<Integer>();

	static final int LIST_TYPE = 13;
	static final int MEDIA_TYPE = 38;
	static final int OPTIONLIST_TYPE = 8;
	static final int STANDARDFIELD_DATE_TYPE = 5;

	static final int STANDARDFIELD_NUMERIC_TYPE = 48;
	static final int STANDARDFIELD_TEXT_TYPE = 1;
	static final int STANDARDFIELD_USER_DEFINED = 999;
	static final int STANDARDFIELD_EMAIL_TYPE = 50;
	static final int STANDARDFIELD_URL_TYPE = 51;

	static final int TEXT_ASCII_TYPE = 31;

	private static final int TEXT_EDITOR_ACTIVATE_DRAG_DROP_BIT_INDEX = 27; // 2^27=134217728

	// different text element editor settings; power of two
	private static final int TEXT_EDITOR_DO_NOT_ALLOW_WRAPPING_BIT_INDEX = 25; // 2^25=33554432

	private static final int TEXT_EDITOR_PASTE_FORMATTED_BIT_INDEX = 15; // 2^15=32768

	/*
	 * text element settings of text editor toolbar buttons Paste formatted=2^15 Justified=2^21 Align left=2^7 Center=2^8 Align right=2^9 Highlight=2^13 Bold=2^0 Font=2^4 Font color=2^12 Italic=2^1 Font size=2^3 Underline=2^2 Insert image=2^19 Define jump mark=2^20 Insert jump mark=2^18 Insert link=2^17 External URLs allowed=2^23 Define target=2^24 Insert tab=2^10 Insert table=2^16 Insert list=2^14 Clear tab=2^11 Subscript=2^6 Superscript=2^5 Insert horizontal line=2^22 Do not allow wrapping=2^25 Symbol table =2^26 Activate Drag & Drop=2^27 Insert markup=2^28 User-Defined Color=2^29 Spelling=2^30
	 */
	static final int TEXT_HTML_TYPE = 32;

	/**
	 * Liefert eine Map mit den Elementtypen und dem Namen, alphabetisch sortiert.
	 */
	public static Map<Integer, String> getTypeMap() {
		// fill the map in same order then types array
		Map<Integer, String> t2n = new HashMap<Integer, String>();
		t2n.put(new Integer(ANCHOR_IMAGE_TYPE), "Anchor Image");
		t2n.put(new Integer(ANCHOR_TEXT_TYPE), "Anchor Text");
		t2n.put(new Integer(ATTRIBUTE_TYPE), "Attribute");
		t2n.put(new Integer(CONTAINER_TYPE), "Container");
		t2n.put(new Integer(FRAME_TYPE), "Frame");
		t2n.put(new Integer(HEADLINE_TYPE), "Headline");
		t2n.put(new Integer(IMAGE_TYPE), "Image");
		t2n.put(new Integer(INFO_TYPE), "Info");
		t2n.put(new Integer(LIST_TYPE), "List");
		t2n.put(new Integer(MEDIA_TYPE), "Media");
		t2n.put(new Integer(OPTIONLIST_TYPE), "Option List");
		t2n.put(new Integer(STANDARDFIELD_DATE_TYPE), "StandardField E-Mail");
		t2n.put(new Integer(STANDARDFIELD_DATE_TYPE), "StandardField Date");
		t2n.put(new Integer(STANDARDFIELD_NUMERIC_TYPE), "StandardField Numeric");
		t2n.put(new Integer(STANDARDFIELD_TEXT_TYPE), "StandardField Text");
		t2n.put(new Integer(STANDARDFIELD_USER_DEFINED), "StandardField User defined");
		t2n.put(new Integer(TEXT_ASCII_TYPE), "Text ASCII");
		t2n.put(new Integer(TEXT_HTML_TYPE), "Text HTML");
		return t2n;
	}

	/**
	 * Liefert für die gegebene Elementtypnummer einen Anzeigenamen.
	 */
	public static String getTypeName(int type) {
		return getTypeName(new Integer(type));
	}

	/**
	 * Liefert für die gegebene Elementtypnummer einen Anzeigenamen.
	 */
	public static String getTypeName(Integer type) {
		return (String) getTypeMap().get(type);
	}

	/**
	 * Returns the red dot template code for the template element with given name elemenName, e.g. <!IoRedDot_headline> See pattern
	 * definition in com.hlcl.rql.as.rql_fw.properties, parameter templateCodeUseRedDotForElementPattern.
	 */
	public static String getTemplateCodeRedDot(String elementName) throws RQLException {
		String pattern = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw").getString("templateCodeUseRedDotForElementPattern");
		return MessageFormat.format(pattern, elementName);
	}

	/**
	 * Returns the template code for the usage of template element with given name elemenName, e.g. <%headline%> See pattern definition
	 * in com.hlcl.rql.as.rql_fw.properties, parameter templateCodeUseElementPattern.
	 */
	public static String getTemplateCode(String elementName) throws RQLException {
		String pattern = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw").getString("templateCodeUseElementPattern");
		return MessageFormat.format(pattern, elementName);
	}

	/**
	 * Liefert für den gegebenen Elementtyp, ob dieser dieser zu den Inhaltselementen gehört.
	 * <p>
	 * Headline element wird per default als content element betrachtet.
	 */
	static boolean isContentElementType(String typeStr) {
		return isContentElementType(typeStr, true);
	}

	/**
	 * Liefert für den gegebenen Elementtyp, ob dieser dieser zu den Inhaltselementen gehört.
	 */
	static boolean isContentElementType(String typeStr, boolean includeHeadline) {
		int type = Integer.parseInt(typeStr);
		return getContentTypes(includeHeadline).contains(type);
	}

	/**
	 * Liefert alle content element typen zurück immer mit headline type.
	 */
	static java.util.List<Integer> getContentTypes() {
		return getContentTypes(true);
	}

	/**
	 * Liefert alle content element typen zurück entsprechend includeHeadline mit oder ohne headline element type.
	 */
	static java.util.List<Integer> getContentTypes(boolean includeHeadline) {
		if (includeHeadline) {
			return CONTENT_TYPES;
		}
		return CONTENT_WITHOUT_HEADLINE_TYPES;
	}

	/**
	 * Liefert für den gegebenen Elementtyp, ob dieser ein label (rd nennt das feld description) hat oder eben nicht.
	 */
	static boolean isLabeled(int type) {
		return LABELED_TYPES.contains(type);
	}

	/**
	 * Liefert für den gegebenen Elementtyp, ob dieser ein label (rd nennt das feld description) hat oder eben nicht.
	 */
	static boolean isLabeled(String elttype) {
		return isLabeled(Integer.parseInt(elttype));
	}

	static {
		LABELED_TYPES.add(ANCHOR_IMAGE_TYPE);
		LABELED_TYPES.add(ANCHOR_TEXT_TYPE);
		LABELED_TYPES.add(HEADLINE_TYPE);
		LABELED_TYPES.add(IMAGE_TYPE);
		LABELED_TYPES.add(MEDIA_TYPE);
		LABELED_TYPES.add(OPTIONLIST_TYPE);
		LABELED_TYPES.add(STANDARDFIELD_URL_TYPE);
		LABELED_TYPES.add(STANDARDFIELD_EMAIL_TYPE);
		LABELED_TYPES.add(STANDARDFIELD_DATE_TYPE);
		LABELED_TYPES.add(STANDARDFIELD_NUMERIC_TYPE);
		LABELED_TYPES.add(STANDARDFIELD_TEXT_TYPE);
		LABELED_TYPES.add(STANDARDFIELD_USER_DEFINED);
		LABELED_TYPES.add(TEXT_ASCII_TYPE);
		LABELED_TYPES.add(TEXT_HTML_TYPE);

		// content element types
		CONTENT_TYPES.add(HEADLINE_TYPE);
		CONTENT_TYPES.add(IMAGE_TYPE);
		CONTENT_TYPES.add(MEDIA_TYPE);
		CONTENT_TYPES.add(OPTIONLIST_TYPE);
		CONTENT_TYPES.add(STANDARDFIELD_URL_TYPE);
		CONTENT_TYPES.add(STANDARDFIELD_EMAIL_TYPE);
		CONTENT_TYPES.add(STANDARDFIELD_DATE_TYPE);
		CONTENT_TYPES.add(STANDARDFIELD_NUMERIC_TYPE);
		CONTENT_TYPES.add(STANDARDFIELD_TEXT_TYPE);
		CONTENT_TYPES.add(STANDARDFIELD_USER_DEFINED);
		CONTENT_TYPES.add(TEXT_ASCII_TYPE);
		CONTENT_TYPES.add(TEXT_HTML_TYPE);

		// content element types
		CONTENT_WITHOUT_HEADLINE_TYPES.add(IMAGE_TYPE);
		CONTENT_WITHOUT_HEADLINE_TYPES.add(MEDIA_TYPE);
		CONTENT_WITHOUT_HEADLINE_TYPES.add(OPTIONLIST_TYPE);
		CONTENT_WITHOUT_HEADLINE_TYPES.add(STANDARDFIELD_URL_TYPE);
		CONTENT_WITHOUT_HEADLINE_TYPES.add(STANDARDFIELD_EMAIL_TYPE);
		CONTENT_WITHOUT_HEADLINE_TYPES.add(STANDARDFIELD_DATE_TYPE);
		CONTENT_WITHOUT_HEADLINE_TYPES.add(STANDARDFIELD_NUMERIC_TYPE);
		CONTENT_WITHOUT_HEADLINE_TYPES.add(STANDARDFIELD_TEXT_TYPE);
		CONTENT_WITHOUT_HEADLINE_TYPES.add(STANDARDFIELD_USER_DEFINED);
		CONTENT_WITHOUT_HEADLINE_TYPES.add(TEXT_ASCII_TYPE);
		CONTENT_WITHOUT_HEADLINE_TYPES.add(TEXT_HTML_TYPE);

		// structural element types
		STRUCTURAL_TYPES.add(ANCHOR_IMAGE_TYPE);
		STRUCTURAL_TYPES.add(ANCHOR_TEXT_TYPE);
		STRUCTURAL_TYPES.add(FRAME_TYPE);
		STRUCTURAL_TYPES.add(CONTAINER_TYPE);
		STRUCTURAL_TYPES.add(LIST_TYPE);
	}
	// instance variables
	private String defaultValue;
	private RQLNode detailsNode; // cache

	private String folderGuid;

	private String folderSuffixes;
	private boolean isDynamic;
	private String labelText; // the eltrddescription attribute value
	private boolean isMandatory;

	private String name;

	// caches
	private RQLNodeList preassignedTemplatesCache;
	private Template template;
	private String templateElementGuid;
	private BigInteger textEditorSettings;
	private String textFixedStylesheet;
	private int type;

	/**
	 * Erzeugt ein neues Templateelement.
	 * 
	 * @param template
	 *            Das Template in dem dieses Element benutzt wird.
	 * @param name
	 *            Name dieses TemplateElements
	 * @param templateElementGuid
	 *            ReDot GUID dieses TemplateElements
	 * @param type
	 *            RedDot Typ dieses Elements
	 * @param isDynamic
	 *            Ob das Anchor Element dynamisch ist (0/1) (type=26 or type=27)
	 * @param folderSuffixes
	 *            Zugelassene Extender für Elemente (type=2 or type=38)
	 * @param textEditorSettings
	 *            editoroptions of text template elements
	 * @param labelText
	 *            the label for this element
	 * @param folderGuid
	 *            the folder guid if this element is a image or media element, sonst empty
	 */
	public TemplateElement(Template template, String name, String templateElementGuid, String type, String isDynamic,
			String folderSuffixes, String textEditorSettings, String textFixedStylesheet, String labelText, String folderGuid, boolean isMandatory) {
		super();

		this.template = template;
		this.name = name;
		this.templateElementGuid = templateElementGuid;
		this.type = Integer.parseInt(type);
		this.isDynamic = "1".equals(isDynamic) ? true : false;
		this.folderSuffixes = folderSuffixes;
		this.textEditorSettings = textEditorSettings == null ? new BigInteger("0") : new BigInteger(textEditorSettings); // 0 means
		// all buttons are allowed
		this.textFixedStylesheet = textFixedStylesheet;
		this.labelText = labelText;
		this.folderGuid = folderGuid;
		this.isMandatory = isMandatory;
	}

	/**
	 * Ergänzt an diesem MultiLink die Templatevorbelegung um das gegebene Template.
	 * <p>
	 * Die Eigenschaft, dass mit connect to existing page auch nur die vorbelegten Templates verbunden werden, wird nicht beeinflusst.
	 * <p>
	 * Achtung: Diese Methode nicht benutzen, falls noch gar kein Template vorbelegt ist, da dann {@link #getPreassignedTemplates()}
	 * alle Templates des Projekts liefert. Dieses TemplateElement muss ein MultiLink Element sein.
	 * 
	 * @see #addPreassignedTemplate(Template, boolean)
	 */
	public void addPreassignedTemplate(Template additionalPreassignedTemplate) throws RQLException {
		addPreassignedTemplate(additionalPreassignedTemplate, false);
	}

	/**
	 * Ergänzt an diesem MultiLink die Templatevorbelegung um das gegebene Template.
	 * <p>
	 * Falls der Parameter restrictConnectToo auf true steht, können bei connect to existing page auch nur die vorbelegten Templates
	 * verbunden werden.
	 * <p>
	 * Achtung: Diese Methode nicht benutzen, falls noch gar kein Template vorbelegt ist, da dann {@link #getPreassignedTemplates()}
	 * alle Templates des Projekts liefert. Dieses TemplateElement muss ein MultiLink Element sein.
	 * 
	 * @see #addPreassignedTemplate(Template, boolean)
	 */
	public void addPreassignedTemplate(Template additionalPreassignedTemplate, boolean restrictConnectToo) throws RQLException {
		// add all assigned templates and the given one
		java.util.List<Template> preassignedTemplates = getPreassignedTemplates();
		preassignedTemplates.add(additionalPreassignedTemplate);
		// preassign the combined list
		setPreassignedTemplates(preassignedTemplates, restrictConnectToo);
	}

	/**
	 * Liefert true, falls mindestens ein template name den gesuchten suffix besitzt.
	 * <p>
	 */
	public boolean arePreassignedTemplateNamesEndsWith(String suffix) throws RQLException {
		java.util.List<String> names = getPreassignedTemplateNames();
		for (Iterator iter = names.iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			if (name.endsWith(suffix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Ordnet diesem Templateelement das gegebene Berechtigungspaket zu.
	 * <p>
	 * Das Paket wird nicht an die entsprechenden Seitenelemente angehängt.
	 */
	public void assignAuthorizationPackage(AuthorizationPackage authorizationPackage) throws RQLException {
		assignAuthorizationPackage(authorizationPackage, -1);
	}

	/**
	 * Ordnet diesem Templateelement das gegebene Berechtigungspaket zu.
	 * <p>
	 * Den analogen Seitenelemente kann das gegebenen Detail-Element-Berechtigungspaket gleich mit zugeordent werden.
	 * 
	 * @param assignToMaxPages
	 *            if > 0: es werden die maxPages Anzahl von Seiten dieses Templates nachgezogen if <=0: Seiten werden gar nicht
	 *            verändert
	 * 
	 */
	public void assignAuthorizationPackage(AuthorizationPackage authorizationPackage, int assignToMaxPages) throws RQLException {

		// check content elements
		if (isContentElement() && !authorizationPackage.isDetailedElementAuthorizationPackage()) {
			throw new WrongTypeException("Authorization package with name " + authorizationPackage.getName()
					+ " has wrong type and cannot be linked to content element " + getName() + " in template " + getTemplateName()
					+ ".");
		}
		// check structural elements
		if (isStructuralElement() && !authorizationPackage.isDetailedLinkAuthorizationPackage()) {
			throw new WrongTypeException("Authorization package with name " + authorizationPackage.getName()
					+ " has wrong type and cannot be linked to structural element " + getName() + " in template " + getTemplateName()
					+ ".");
		}
		// check type
		if (authorizationPackage.isDetailedElementAuthorizationPackage() || authorizationPackage.isDetailedLinkAuthorizationPackage()) {
			// call CMS for this template element
			getProject().assignAuthorizationPackage("ELEMENT", getTemplateElementGuid(), authorizationPackage);

			// assign to maxPages as well
			if (assignToMaxPages > 0) {
				getTemplate().assignAuthorizationPackageToPageElements(authorizationPackage, this, assignToMaxPages);
			}
		} else {
			throw new WrongTypeException("Authorization package with name " + authorizationPackage.getName()
					+ " has wrong type and cannot be linked to template element " + getName() + " in template " + getTemplateName()
					+ ".");
		}
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getTemplateFolder().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines
	 * Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Löscht dieses Templateelement.
	 */
	public void delete() throws RQLException {
		/*
		 * V6.5 request <IODATA loginguid="B9263B3DDF1E41BEB95445418C7F1DFD" sessionkey="1021834323EK6d4Iy2443"> <PROJECT> <TEMPLATE> <ELEMENT action="delete" guid="D5AE16A5F8914B058AC2D6AB64B5F347" deletereal="1" /> </TEMPLATE> </PROJECT> </IODATA> V6.5 response <IODATA>ok </IODATA>
		 */

		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <TEMPLATE>"
				+ "    <ELEMENT action='delete' deletereal='1' guid='" + getTemplateElementGuid() + "' />" + "  </TEMPLATE>"
				+ "</IODATA>";
		callCmsWithoutParsing(rqlRequest);

		// invalidate the elements cache in template object
		getTemplate().deleteTemplateElementsCache();
	}

	/**
	 * Löscht den lokalen details node Cache.
	 */
	private void deleteDetailsNodeCache() {
		detailsNode = null;
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getTemplateFolder().getCmsClient();
	}

	/**
	 * Liefert den Attributnamen aus dem XML Response in Abhängigkeit vom Typ dieses Template-Elements.
	 */
	private String getDefaultAttributeName() throws RQLException {

		if (isStandardField() || isOptionList()) {
			return "eltdefaultvalue";
		} else if (isText()) {
			return "eltdefaulttext";
		}
		if (isFile()) {
			return "eltsrc";
		}

		// other types should not be
		throw new WrongTypeException("A default attribute name cannot be determined for the template element with name " + getName()
				+ " of type " + getType() + " in template " + getTemplate().getName() + ".");
	}

	/**
	 * Liefert den defaultwert zurück. Liefert null, falls dieses Element keinen Defaultwert besitzt.
	 */
	public String getDefaultValue() throws RQLException {

		/*
		 * V6.5 request <IODATA loginguid="2750CBF98D0A499781570CBF8429BC78" sessionkey="1021834323aradh4y3506"> <TEMPLATE> <ELEMENT action="load" guid="F1240A40C0974FC4AF9B7D2E4902A05B"/> </TEMPLATE> </IODATA> V6.5 response <IODATA> <TEMPLATE sessionkey="1021834323aradh4y3506" dialoglanguageid="ENG" languagevariantid="ENG"> <ELEMENT action="load" languagevariantid="ENG" dialoglanguageid="ENG" parentguid="" parenttable="TPL" eltautoheight="1" eltautowidth="1" eltautoborder="1" eltignoreworkflow="0" eltlanguageindependent="0" guid="F1240A40C0974FC4AF9B7D2E4902A05B" eltname="img_back" elttype="2" eltid="27" eltislink="0" eltfolderguid="1399FC72ABCB4E839DE8D91E83CCDCFF" eltsuffixes="" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="1" eltrequired="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="1" eltinvisibleinpage="0" elthideinform="0" eltdragdrop="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0" eltrdexamplesubdirguid="" eltsrc="back.gif" eltalt="" templateguid="E4B2C96E7F5F422CB1836457301262A4"/> </TEMPLATE> </IODATA>
		 */

		// cache default value
		if (defaultValue == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <TEMPLATE>"
					+ "    <ELEMENT action='load' guid='" + getTemplateElementGuid() + "' />" + "  </TEMPLATE>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			String attributeName = getDefaultAttributeName();
			defaultValue = rqlResponse.getNode("ELEMENT").getAttribute(attributeName);
		}
		return defaultValue;
	}

	/**
	 * Sets the default file and folder on this image or media element.
	 * <p>
	 * Attention: The default file is not checked against any other property on this template element, e.g. suffixes, width, height and
	 * so on.
	 * <p>
	 * Switches behind the scenes to main language variant, if LV independent and not in main LV.
	 * 
	 * @throws WrongTypeException
	 *             if you try to use it not on an image or media template element.
	 */
	public void setDefaultFile(File defaultFile) throws RQLException {
		if (!hasFolder()) {
			throw new WrongTypeException("The template element with name " + getName() + " has no folder, because it is of type "
					+ getTypeName() + ".");
		}
		Project project = getProject();
		if (isLanguageVariantIndependent()) {
			if (project.isCurrentLanguageVariantMainLanguage()) {
				// change it
				save("eltsrc='" + defaultFile.getFilename() + "' languagevariantguid='" + project.getMainLanguageVariantGuid()
						+ "' eltlanguageindependent='" + StringHelper.convertTo01(isLanguageVariantIndependent()) + "' eltfolderguid",
						defaultFile.getFolderGuid());
			} else {
				// change to main lv
				LanguageVariant currentLv = project.getCurrentLanguageVariant();
				try {
					// change to main
					project.switchCurrentLanguageVariantToMainLanguage();
					// save under main lv
					save("eltsrc='" + defaultFile.getFilename() + "' languagevariantguid='" + project.getMainLanguageVariantGuid()
							+ "' eltlanguageindependent='" + StringHelper.convertTo01(isLanguageVariantIndependent())
							+ "' eltfolderguid", defaultFile.getFolderGuid());
				} finally {
					// restore lv
					project.setCurrentLanguageVariant(currentLv);
				}
			}
		} else {
			// if lv dependent simply save
			save("eltsrc='" + defaultFile.getFilename() + "' eltfolderguid", defaultFile.getFolderGuid());
		}
	}

	/**
	 * Sets the default file on this image or media element. Keeps the folder unchanged and didn't check, if the file exists!
	 * <p>
	 * Attention: The default file is not checked against any other property on this template element, e.g. suffixes, width, height and
	 * so on.
	 * <p>
	 * Switches behind the scenes to main language variant, if LV independent and not in main LV.
	 * 
	 * @throws WrongTypeException
	 *             if you try to use it not on an image or media template element.
	 */
	public void setDefaultFilename(String filename) throws RQLException {
		if (!hasFolder()) {
			throw new WrongTypeException("The template element with name " + getName() + " has no folder, because it is of type "
					+ getTypeName() + ".");
		}
		Project project = getProject();
		if (isLanguageVariantIndependent()) {
			if (project.isCurrentLanguageVariantMainLanguage()) {
				// change it
				save("languagevariantguid='" + project.getMainLanguageVariantGuid() + "' eltlanguageindependent='"
						+ StringHelper.convertTo01(isLanguageVariantIndependent()) + "' eltsrc", filename);
			} else {
				// change to main lv
				LanguageVariant currentLv = project.getCurrentLanguageVariant();
				try {
					// change to main
					project.switchCurrentLanguageVariantToMainLanguage();
					// save under main lv
					save("languagevariantguid='" + project.getMainLanguageVariantGuid() + "' eltlanguageindependent='"
							+ StringHelper.convertTo01(isLanguageVariantIndependent()) + "' eltsrc", filename);
				} finally {
					// restore lv
					project.setCurrentLanguageVariant(currentLv);
				}
			}
		} else {
			// if lv dependent simply save
			save("eltsrc", filename);
		}

	}

	/**
	 * Sets the default filename on this template element in all given language variants. Keeps the folder unchanged and didn't check,
	 * if the file exists!
	 * <p>
	 * Attention: The default file is not checked against any other property on this template element, e.g. suffixes, width, height and
	 * so on.
	 * 
	 * @see #setDefaultFilename(String)
	 * @throws WrongTypeException
	 *             if you try to use it not on an image or media template element.
	 */
	public void setDefaultFilename(String filename, java.util.List<LanguageVariant> languageVariants) throws RQLException {
		Project project = getProject();
		LanguageVariant currentLv = project.getCurrentLanguageVariant();

		try {
			// update an lv dependent element
			for (LanguageVariant lv : languageVariants) {
				project.setCurrentLanguageVariant(lv);
				setDefaultFilename(filename);
			}
		} finally {
			// restore language variant
			project.setCurrentLanguageVariant(currentLv);
		}
	}

	/**
	 * Return for an image or media element the default file. Returns null, if no default image or download fle is configured.
	 * 
	 * @throws WrongTypeException
	 *             if you try to use it not on an image or media template element.
	 */
	public File getDefaultFile() throws RQLException {
		if (hasFolder() && hasDefaultValue()) {
			return new File(getFolder(), getDefaultValue());
		}
		// signal has no default file;
		return null;
	}

	/**
	 * Liefert einen neuen RQLNode mit allen Attributen und child nodes dieses Templateelements zurück, um es in einem neuen Template
	 * anzulegen.
	 * <p>
	 */
	RQLNode getCopyNode() throws RQLException {
		String skipAttributeNames = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw").getString("copyTemplateElementSkipAttributes");
		return getDetailsNode().copy(skipAttributeNames, ",");
	}

	/**
	 * Liefert den RQLNode mit weiteren Information zu diesem Templateelement zurück.
	 */
	private RQLNode getDetailsNode() throws RQLException {

		/*
		 * nur die Elementdaten 
		  V7.5 request 
		  <IODATA loginguid="A27CF949C92948BFAB6BE65BD17AC55A" sessionkey="6C9199C6B9A644BDBDE98BA1285D6A5A"> 
		  <TEMPLATE> 
		  <ELEMENT action="load" guid="232A21C04F984DA78C6769AA6C0220BE"/> 
		  </TEMPLATE> </IODATA>
		   V7.5 response 
		   <IODATA> 
		   <TEMPLATE sessionkey="6C9199C6B9A644BDBDE98BA1285D6A5A" dialoglanguageid="ENG" languagevariantid="ENG"> 
		   <ELEMENT action="load" languagevariantid="ENG" dialoglanguageid="ENG" parentguid="" parenttable="TPL" eltignoreworkflow="0" eltdirectedit="0" eltlanguageindependent="0" eltparentelementguid="7463EBB74CFE4E879D451AD39D19ABA9" eltparentelementname="responsible_id" guid="232A21C04F984DA78C6769AA6C0220BE" eltname="page_updated" elttype="5" eltid="53" eltislink="0" eltfolderguid="" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="1" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" elthideinform="0" eltdragdrop="0" eltxhtmlcompliant="0" eltdonothtmlencode="0" eltuserdefinedallowed="1" eltextendedlist="0" eltformatno="-1" eltlcid="2057" eltformatting="dd MMM yyyy" templateguid="E5D49F4326E249A48B74BBB81008011F" eltrddescription="UPDATED DATE of this page"/> 
		   </TEMPLATE> </IODATA>
		   
		   V9 response templte image element
		<IODATA>
		<TEMPLATE sessionkey="3CAAC2A997E84B67929D9E0E51263D31" dialoglanguageid="ENG" languagevariantid="ENG">
			<ELEMENT action="load" languagevariantid="ENG" dialoglanguageid="ENG" parentguid="" parenttable="TPL" eltlanguageindependent="1" eltignoreworkflow="0" eltpresetalt="0" eltautoborder="0" eltautowidth="0" eltautoheight="0" eltconvert="0" guid="9E6812B24A48412B9E0E222CAFB65142" eltname="bookmark_add" elttype="2" eltid="36" eltislink="0" eltfolderguid="39F41C700C814519A0DA2C15E267F78E" eltsuffixes="jpg;jpeg;gif;png" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="1" eltrequired="0" usepagemainlinktargetcontainer="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" elthideinform="0" eltdragdrop="0" eltxhtmlcompliant="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0" eltflags="16" eltrdexamplesubdirguid="39F41C700C814519A0DA2C15E267F78E" eltsrc="bookmark_add.png" eltalt="" eltsrcsubdirguid="39F41C700C814519A0DA2C15E267F78E" templateguid="0B496BBEF7764906A7286C0757415F05" eltrddescription="the bookmark image for a page not currently bookmarked"/>
		</TEMPLATE>
		</IODATA>
		 */

		/*
		 * erweiterte Elementdaten mit parent elements und option list selections V9 request 
		<IODATA user="lejafr" loginguid="C39A459C2D83457C9896D6406509D37E" sessionkey="66DFAAF7FEDE497AA116A8EB3113C12A"> 
		<PROJECT> 
		<TEMPLATE> 
		<ELEMENT action="load" guid="A49052E3F743483A9814503F4C6948F6"> 
		<SELECTIONS action="load" guid="A49052E3F743483A9814503F4C6948F6"/> 
		</ELEMENT> 
		</TEMPLATE> 
		</PROJECT> 
		</IODATA> 
		V9 response 
		<IODATA> 
		<TEMPLATE languagevariantid="ENG" parentobjectname="PROJECT" useconnection="1" dialoglanguageid="ENG"> 
		<ELEMENT action="load" languagevariantid="ENG" dialoglanguageid="ENG" parentguid="" parenttable="TPL" eltignoreworkflow="0" eltlanguageindependent="0" eltlanguagedependentvalue="0" eltlanguagedependentname="0" eltparentelementguid="475487C27CD94A17BE6079E2148594E7" eltparentelementname="responsible_area" guid="A49052E3F743483A9814503F4C6948F6" eltname="responsible_mail_subject_statistic_area" elttype="8" eltid="10" eltislink="0" eltfolderguid="" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="1" usepagemainlinktargetcontainer="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" elthideinform="0" eltdragdrop="0" eltxhtmlcompliant="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0" eltflags="512" templateguid="94A47892718042ADBC4822A9CC7C67EC" eltorderby="2" eltrddescription="statistic area in e-mail subject"> 
		<SELECTIONS> 
		<SELECTION guid="24E9973989FC4B038896C84AF2954EAB" identifier="24E9973989FC4B038896C84AF2954EAB" description="BAF" value="BAF"> 
		<ITEM languageid="ENG" name="BAF">BAF</ITEM> 
		</SELECTION> 
		<SELECTION guid="4F438F4C4E0D459D85DD130840A28ACD" identifier="4F438F4C4E0D459D85DD130840A28ACD" description="COM" value="COM"> 
		<ITEM languageid="ENG" name="COM">COM</ITEM> 
		</SELECTION> <SELECTION guid="C1DDB7B7918E4FE5B3DBADB06E543E1A" identifier="C1DDB7B7918E4FE5B3DBADB06E543E1A" description="HUR" value="HUR"> 
		<ITEM languageid="ENG" name="HUR">HUR</ITEM> 
		</SELECTION> 
		<SELECTION guid="E060EFEE50004E3EA33069ACF654DF24" identifier="E060EFEE50004E3EA33069ACF654DF24" description="OPS" value="OPS"> 
		<ITEM languageid="ENG" name="OPS">OPS</ITEM> 
		</SELECTION> 
		<SELECTION guid="E96AFF53B8F8447E94AC484E4633545C" identifier="E96AFF53B8F8447E94ACF84E4633545C" description="SAL" value="SAL"> 
		<ITEM languageid="ENG" name="SAL">SAL</ITEM> 
		</SELECTION> 
		<SELECTION guid="B919AB617C6D4652A8D4FAC4CA367B56" identifier="B919AB617C6D4652A8D4FAC4CA367B56" description="test" value="test"> 
		<ITEM languageid="ENG" name="test">test</ITEM> 
		</SELECTION>
		 </SELECTIONS> 
		 </ELEMENT> 
		 </TEMPLATE> 
		 </IODATA>
		 */
		// cache the node with details information
		if (detailsNode == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "' ><TEMPLATE>"
					+ " <ELEMENT action='load' guid='" + getTemplateElementGuid() + "'/>" + "</TEMPLATE></IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			detailsNode = rqlResponse.getNode("ELEMENT");
		}
		return detailsNode;
	}

	/**
	 * Liefert den Ordnernamen, der für ein Image- oder Mediaelement im Template gesetzt ist.
	 */
	public String getFolderName() throws RQLException {
		return getFolder().getName();
	}

	/**
	 * Liefert den Ordner, der für ein Image- oder Mediaelement im Template gesetzt ist.
	 * 
	 * @throws WrongTypeException
	 *             if this template element did not have a folder at all
	 */
	public Folder getFolder() throws RQLException {
		if (hasFolder()) {
			return getProject().getFolderByGuid(folderGuid);
		} else {
			throw new WrongTypeException("The template element with name " + getName() + " has no folder, because it is of type "
					+ getTypeName() + ".");
		}
	}

	/**
	 * Liefert true, falls die Inhalte des Folders oder AssetManagers im Dateisystem über einen UNC Pfad gespeichert werden, also aus
	 * dem Netzwerk erreichbar sind. An UNC path is starting always with \\.
	 * 
	 * @throws WrongTypeException
	 *             if this template element did not have a folder at all
	 */
	public boolean isFolderStoredInFileSystemUnc() throws RQLException {
		return getFolder().isStoredInFileSystemUnc();
	}

	/**
	 * Liefert die Anzahl aller Dateien des TemplateElementOrdners für alle ungültigen Suffixes dieses Template-Elements.
	 * 
	 * @see #getInvalidFolderSuffixes(String, String)
	 */
	public int getInvalidFolderSuffixesFilesSize(String maxValidSuffixes, String separator) throws RQLException {
		return getInvalidFolderSuffixesFiles(maxValidSuffixes, separator).size();
	}

	/**
	 * Liefert alle Dateien des TemplateElementOrdners für alle ungültigen Suffixes dieses Template-Elements.
	 * 
	 * @see #getInvalidFolderSuffixes(String, String)
	 */
	public java.util.List<File> getInvalidFolderSuffixesFiles(String maxValidSuffixes, String separator) throws RQLException {
		List<String> invalidFolderSuffixes = getInvalidFolderSuffixes(maxValidSuffixes, separator);
		List<File> result = new ArrayList<File>();
		for (String extender : invalidFolderSuffixes) {
			result.addAll(getFolder().searchFilesByExtender(extender));
		}
		return result;
	}

	/**
	 * Liefert die Liste der ungültigen Ordner Suffixes (htm;css;xml) dieses Template-Elements.
	 * <p>
	 * Es werden nur ungültige Suffixes geliefert, die diesem TemplateElement zugeordnet sind.
	 * <p>
	 * Enthält maxValidSuffixes einen Suffix, der aktuell nicht in diesem Templateelement zugelassen ist, wird dieser nicht
	 * zurückgeliefert!
	 */
	public java.util.List<String> getInvalidFolderSuffixes(String maxValidSuffixes, String separator) throws RQLException {
		java.util.List<String> current = getFolderSuffixes();
		java.util.List<String> valid = StringHelper.split(maxValidSuffixes, separator.charAt(0));
		// collect all invalid
		java.util.List<String> result = new ArrayList<String>();
		for (String cs : current) {
			if (!valid.contains(cs)) {
				result.add(cs);
			}
		}
		return result;
	}

	/**
	 * Liefert die Liste der Ordner Suffixes (htm;css;xml) dieses Template-Elements.
	 * 
	 * @return java.lang.String
	 */
	public String getFolderSuffixesStr() throws RQLException {

		// only for image and media available
		if (!hasFolder()) {
			throw new WrongTypeException("Template element " + getName() + " is neither an image nor a media element.");
		}

		return folderSuffixes;
	}

	/**
	 * Liefert die Liste der Ordner Suffixes (htm;css;xml) dieses Template-Elements.
	 * 
	 * @return java.lang.String
	 */
	public java.util.List<String> getFolderSuffixes() throws RQLException {
		return StringHelper.split(getFolderSuffixesStr(), FOLDER_SUFFIXES_SEPARATOR_CHAR);
	}

	/**
	 * Ändert die Liste der Dateisuffixes dieses Folder Templateelements und liefert alle dann ungültigen Files des Ordners zurück.
	 */
	public java.util.List<File> setFolderSuffixes(String folderSuffixes, String separator) throws RQLException {
		// collect invalid files
		List<File> invalidFiles = getInvalidFolderSuffixesFiles(folderSuffixes, separator);

		// save new suffixes
		save("eltsuffixes", folderSuffixes.replace(separator.charAt(0), FOLDER_SUFFIXES_SEPARATOR_CHAR));

		return invalidFiles;
	}

	/**
	 * Liefert den label text dieses elements. Liefert null, falls keiner gesetzt ist.
	 * <p>
	 * Der label text kann HTML Tags enthalten!
	 */
	public String getLabelText() {
		if (labelText != null) {
			return StringHelper.unescapeHTML(labelText);
		}
		return null;
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {
		return getTemplate().getLogonGuid();
	}

	/**
	 * Liefert den Namen des Template-Elements.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getName() {
		return name;
	}

	/**
	 * Liefert die Liste aller vorbelegten Templatenamen dieses TemplateElements.
	 * <p>
	 * Dieses TemplateElement muss ein MultiLink Element sein.
	 * 
	 * @return java.util.List of Templatenames (list of Strings)
	 * @see Template
	 */
	public java.util.List<String> getPreassignedTemplateNames() throws RQLException {

		java.util.List<String> preassignedTemplateNames = new ArrayList<String>();
		RQLNodeList elementNodeList = getPreassignedTemplatesNodeList();
		if (elementNodeList != null) {
			RQLNode templateNode = null;

			// collect preassigned templates
			for (int i = 0; i < elementNodeList.size(); i++) {
				templateNode = elementNodeList.get(i);
				preassignedTemplateNames.add(templateNode.getAttribute("name"));
			}
		}
		return preassignedTemplateNames;
	}

	/**
	 * Liefert true, falls das gegebenen Template an diesem TemplateElement (nur MultiLink) vorbelegt ist.
	 * 
	 * @throws WrongTypeException
	 */
	public boolean isTemplatePreassigned(Template template) throws RQLException {

		return getPreassignedTemplates().contains(template);
	}

	/**
	 * Liefert die Liste aller vorbelegten Templates dieses TemplateElements.
	 * <p>
	 * Dieses TemplateElement muss ein MultiLink Element sein.
	 * 
	 * @return java.util.List of Templates
	 * @see Template
	 * @throws WrongTypeException
	 */
	public java.util.List<Template> getPreassignedTemplates() throws RQLException {

		java.util.List<Template> preassignedTemplates = new ArrayList<Template>();
		// wrap only if template element has preassignemts
		RQLNodeList elementNodeList = getPreassignedTemplatesNodeList();
		if (elementNodeList != null) {
			RQLNode templateNode = null;

			// collect preassigned templates
			for (int i = 0; i < elementNodeList.size(); i++) {
				templateNode = elementNodeList.get(i);
				preassignedTemplates
						.add(new Template(getProject().getTemplateFolderByGuid(templateNode.getAttribute("folderguid")), templateNode
								.getAttribute("name"), templateNode.getAttribute("guid"), templateNode.getAttribute("description")));
			}
		}
		return preassignedTemplates;
	}

	/**
	 * Liefert die Liste aller vorbelegten Templates dieses TemplateElements.
	 * <p>
	 * Dieses TemplateElement muss ein MultiLink Element sein.
	 * 
	 * @param templateFolder
	 *            Wirkt als Filter; es werden nur Templates aus diesm Folder zurückgeliefert.
	 * @return java.util.List of Templates
	 * @see Template
	 */
	public java.util.List<Template> getPreassignedTemplatesByFolder(TemplateFolder templateFolder) throws RQLException {

		RQLNodeList elementNodeList = getPreassignedTemplatesNodeList();

		// filter only if template element has preassignemts
		java.util.List<Template> preassignedTemplates = new ArrayList<Template>();
		if (elementNodeList != null) {
			RQLNode templateNode = null;
			String fldrGuid = templateFolder.getTemplateFolderGuid();

			// collect preassigned templates
			for (int i = 0; i < elementNodeList.size(); i++) {
				templateNode = elementNodeList.get(i);
				if (templateNode.getAttribute("folderguid").equals(fldrGuid)) {
					preassignedTemplates.add(new Template(templateFolder, templateNode.getAttribute("name"), templateNode
							.getAttribute("guid"), templateNode.getAttribute("description")));
				}
			}
		}
		return preassignedTemplates;
	}

	/**
	 * Liefert die RQLNodeList aller vorbelegten Templates dieses TemplateElements.
	 * 
	 * @throws WrongTypeException
	 */
	private RQLNodeList getPreassignedTemplatesNodeList() throws RQLException {

		if (hasPreassignedTemplates()) {
			return getPreassignedTemplatesNodeListPrimitive();
		}
		// no template preassigned
		return new RQLNodeList();
	}

	/**
	 * Liefert für einen MultiLink die Anzahl der vorbelegten Templates.
	 */
	public int getNumberOfPreassignedTemplates() throws RQLException {
		return getPreassignedTemplatesNodeList().size();
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
		return getProject().getProjectGuid();
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public java.lang.String getSessionKey() {
		return getTemplate().getSessionKey();
	}

	/**
	 * Liefert die Locale dieses standard field date elements.
	 * 
	 * @throws WrongTypeException
	 *             falls dieses element keine standard field date ist
	 */
	public Locale getStandardFieldDateLocale() throws RQLException {

		if (!isStandardFieldDate()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type standard field date. The locale can be retrieved only for this type of elements.");
		}
		return getCmsClient().getLocaleByLcid(getDetailsNode().getAttribute("eltlcid"));
	}

	/**
	 * Liefert das Template zu dem dieses Element gehoert.
	 */
	public Template getTemplate() {

		return template;
	}

	/**
	 * Liefert die GUID des Template-Elements.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getTemplateElementGuid() {
		return templateElementGuid;
	}

	/**
	 * Liefert den Template-Folder, in dem dieses Template enthalten ist.
	 * 
	 * @return TemplateFolder
	 */
	public TemplateFolder getTemplateFolder() {
		return getTemplate().getTemplateFolder();
	}

	/**
	 * Liefert den Template-Folder GUID, in dem dieses Template enthalten ist.
	 * 
	 * @return TemplateFolder
	 */
	public String getTemplateFolderGuid() {
		return getTemplate().getTemplateFolderGuid();
	}

	/**
	 * Liefert die GUID des Templates vom Container.
	 */
	public java.lang.String getTemplateGuid() {
		return getTemplate().getTemplateGuid();
	}

	/**
	 * Liefert den Namen des Templates in dem dieses Templateelement enthalten ist.
	 */
	public String getTemplateName() throws RQLException {
		return getTemplate().getName();
	}

	/**
	 * Liefert die zugewiesenen Stylesheets dieses HTML Textelements. RD Funktion Assign fixed Stylesheets. Liefert null, falls nichts
	 * zugewiesen.
	 */
	public String getTextFixedStylesheet() throws RQLException {

		if (!isHtmlText()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type HTML text. A fixed stylesheet can retrieved only on HTML text elements.");
		}
		if (textFixedStylesheet == null) {
			return null;
		}
		return StringHelper.unescapeHTML(textFixedStylesheet);
	}

	/**
	 * Liefert den Typ des Template-Elements.
	 */
	private int getType() {

		return type;
	}

	/**
	 * Liefert einen Namen für den Elementtyp.
	 */
	public String getTypeName() {
		return TemplateElement.getTypeName(getType());
	}

	/**
	 * Liefert den Formatstring für dieses standard field date elements, z.b. dd MMM yyyy.
	 * 
	 * @throws WrongTypeException
	 *             falls dieses element keine standard field date ist oder nicht userdefined ist.
	 * @see #isDateFormatUserDefined()
	 * @see #getStandardFieldDateLocale()
	 */
	public Locale getUserDefinedDateFormat() throws RQLException {

		if (!isStandardFieldDate()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type standard field date. The locale can be retrieved only for this type of elements.");
		}
		if (!isDateFormatUserDefined()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type standard field date. The locale can be retrieved only for this type of elements.");
		}
		return getCmsClient().getLocaleByLcid(getDetailsNode().getAttribute("eltlcid"));
	}

	/**
	 * Liefert true genau dann, wenn dieses TemplateElement einen Vorgabewert hat.
	 */
	public boolean hasDefaultValue() throws RQLException {

		return getDefaultValue() != null;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Image oder ein Media ist.
	 */
	public boolean hasFolder() {

		return getType() == IMAGE_TYPE || getType() == MEDIA_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn an diesem MultiLink Template-Element mindestens ein Template explizit zugelassen ist.
	 */
	public boolean hasPreassignedTemplates() throws RQLException {

		return getPreassignedTemplatesNodeListPrimitive().size() < getProject().getNumberOfAllTemplates();
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein ASCII Text ist.
	 */
	public boolean isAsciiText() {

		return getType() == TEXT_ASCII_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Attribute ist.
	 */
	public boolean isAttribute() {

		return getType() == ATTRIBUTE_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Container ist.
	 */
	public boolean isContainer() {

		return getType() == CONTAINER_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein content element ist.
	 * 
	 * @see #isStructuralElement()
	 */
	public boolean isContentElement() {
		return CONTENT_TYPES.contains(getType());
	}

	/**
	 * Liefert true, falls dieses standard field date element ein benutzerdefiniertes Datumsformat besitzt, sonst false.
	 * 
	 * @throws WrongTypeException
	 *             falls dieses element keine standard field date ist
	 */
	public boolean isDateFormatUserDefined() throws RQLException {

		if (!isStandardFieldDate()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type standard field date. The date format type can be retrieved only for this type of elements.");
		}
		return "-1".equals(getDetailsNode().getAttribute("eltformatno"));
	}

	/**
	 * 
	 */
	private boolean isDynamic() {

		return isDynamic;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein dynamischer Textlink ist.
	 */
	public boolean isDynamicTextAnchor() {

		return getType() == ANCHOR_TEXT_TYPE && isDynamic();
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Image oder ein Media ist.
	 */
	public boolean isFile() {

		return hasFolder();
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Frame ist.
	 */
	public boolean isFrame() {

		return getType() == FRAME_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element eine Überschrift ist.
	 */
	public boolean isHeadline() {

		return getType() == HEADLINE_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein HTML Text ist.
	 */
	public boolean isHtmlText() {

		return getType() == TEXT_HTML_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Image ist.
	 */
	public boolean isImage() {

		return getType() == IMAGE_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Infoelement ist.
	 */
	public boolean isInfo() {

		return getType() == INFO_TYPE;
	}

	/**
	 * Liefert true, falls dieses Element sprachvariantenabhängig ist, sonst false.
	 * <p>
	 * Attention: Returns default value true, if the attribute eltlanguageindependent is not found in RQL response at all. In old
	 * projects I discovered this problem, which (without this default value) could be solved only to open end save the resprective
	 * template element (which is inconvenient).
	 * <p>
	 * Attention: This property is available in headline elementes only in versions before 9.0.1.49. Moved to setting in content class.
	 * 
	 * @see Template#isAdoptHeadlineToAllLanguageVariants()
	 */
	public boolean isLanguageVariantDependent() throws RQLException {
		String attrVal = getDetailsNode().getAttribute("eltlanguageindependent");
		return attrVal == null ? true : attrVal.equals("0");
	}

	/**
	 * Liefert true, falls dieses Element sprachvariantenunabhängig ist, sonst false.
	 * <p>
	 * Attention: This property is available in headline elementes only in versions before 9.0.1.49. Moved to setting in content class.
	 * 
	 * @see Template#isAdoptHeadlineToAllLanguageVariants()
	 */

	public boolean isLanguageVariantIndependent() throws RQLException {
		return !isLanguageVariantDependent();
	}

	/**
	 * Ändert die Eigenschaft 'language variant independent content' für dieses Element.
	 * <p>
	 * Attention: This property is available in headline elementes only in versions before 9.0.1.49. Moved to setting in content class.
	 * 
	 * @see Template#setIsAdoptHeadlineToAllLanguageVariants(boolean)
	 */
	public void setIsLanguageVariantIndependent(boolean isLanguageVariantIndependent) throws RQLException {
		setIsLanguageVariantDependent(!isLanguageVariantIndependent);
	}

	/**
	 * Ändert die Eigenschaft 'language variant independent content' für dieses Element.
	 * <p>
	 * Attention: This property is available in headline elementes only in versions before 9.0.1.49. Moved to setting in content class.
	 * 
	 * @see Template#setIsAdoptHeadlineToAllLanguageVariants(boolean)
	 */
	public void setIsLanguageVariantDependent(boolean isLanguageVariantDependent) throws RQLException {

		save("eltlanguageindependent", StringHelper.convertTo01(!isLanguageVariantDependent));
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element eine Liste ist.
	 */
	public boolean isList() {

		return getType() == LIST_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Mediaelement ist.
	 */
	public boolean isMedia() {

		return getType() == MEDIA_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein MultiLink ist.
	 */
	public boolean isMultiLink() {

		return isContainer() || isList();
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element eine Optionsliste ist.
	 */
	public boolean isOptionList() {

		return getType() == OPTIONLIST_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Standardfeld ist.
	 */
	public boolean isStandardField() {

		return isStandardFieldText() || isStandardFieldNumeric() || isStandardFieldDate() || isStandardFieldUserDefined() || isStandardFieldEmail() || isStandardFieldUrl();
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Standardfeld E-Mail ist.
	 */
	public boolean isStandardFieldEmail() {

		return getType() == STANDARDFIELD_EMAIL_TYPE;
	}

	
	/**
	 * Testet auf Standard URL-Felder.
	 */
	public boolean isStandardFieldUrl() {

		return getType() == STANDARDFIELD_URL_TYPE;
	}
	
	
	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Standardfeld Date ist.
	 */
	public boolean isStandardFieldDate() {

		return getType() == STANDARDFIELD_DATE_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element vom Typ Standardfeld Numerisch ist.
	 */
	public boolean isStandardFieldNumeric() {

		return getType() == STANDARDFIELD_NUMERIC_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element eine Standardfeld Text ist.
	 */
	public boolean isStandardFieldText() {

		return getType() == STANDARDFIELD_TEXT_TYPE;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element eine Standardfeld User defined ist.
	 */
	public boolean isStandardFieldUserDefined() {

		return getType() == STANDARDFIELD_USER_DEFINED;
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein structural element ist.
	 * 
	 * @see #isContentElement()
	 */
	public boolean isStructuralElement() {
		return STRUCTURAL_TYPES.contains(getType());
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Standardfeld ist.
	 */
	public boolean isText() {

		return isAsciiText() || isHtmlText();
	}

	/**
	 * Liefert true genau dann, wenn dieses Template-Element ein Textlink ist.
	 */
	public boolean isTextAnchor() {

		return getType() == ANCHOR_TEXT_TYPE && !isDynamic();
	}

	/**
	 * Erstellt eine RD Referenz von diesem StandardFieldTextElement (als Source) zum gegebenen Seitenelement.
	 * <p>
	 * Achtung: Nur als Administrator aufrufbar!
	 * <p>
	 * Seitenelemente werden nicht referenziert!
	 */
	public void referenceTo(StandardFieldTextElement targetElement) throws RQLException {
		referenceTo(targetElement, -1);
	}

	/**
	 * Erstellt eine RD Referenz von diesem StandardFieldTextElement (als Source) zum gegebenen Seitenelement.
	 * <p>
	 * Achtung: Nur als Administrator aufrufbar!
	 * 
	 * @param assignToMaxPages
	 *            if > 0: es werden die maxPages Anzahl von Seiten dieses Templates nachgezogen if <=0: Seiten werden gar nicht
	 *            verändert
	 */
	public void referenceTo(StandardFieldTextElement targetElement, int assignToMaxPages) throws RQLException {
		if (!isStandardFieldText()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type standard field text. The reference could not be set.");
		}
		// referece the template element itself
		getProject().referenceElement(getTemplateElementGuid(), targetElement.getElementGuid(), "project.4156");

		// reference in maxPages as well
		if (assignToMaxPages > 0) {
			getTemplate().referencePageElements(this, targetElement, assignToMaxPages);
		}
	}

	/**
	 * Erstellt eine RD Referenz von diesem TextElement (als Source) zum gegebenen Seitenelement.
	 * <p>
	 * Achtung: Nur als Administrator aufrufbar!
	 * <p>
	 * Seitenelemente werden nicht referenziert!
	 */
	public void referenceTo(TextElement targetElement) throws RQLException {
		referenceTo(targetElement, -1);
	}

	/**
	 * Erstellt eine RD Referenz von diesem TextElement (als Source) zum gegebenen Seitenelement.
	 * <p>
	 * Achtung: Nur als Administrator aufrufbar!
	 * 
	 * @param assignToMaxPages
	 *            if > 0: es werden die maxPages Anzahl von Seiten dieses Templates nachgezogen if <=0: Seiten werden gar nicht
	 *            verändert
	 */
	public void referenceTo(TextElement targetElement, int assignToMaxPages) throws RQLException {
		if (!isText()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type text (ASCII or HTML). The reference could not be set.");
		}
		// referece the template element itself
		getProject().referenceElement(getTemplateElementGuid(), targetElement.getElementGuid(), "project.4157");

		// reference in maxPages as well
		if (assignToMaxPages > 0) {
			getTemplate().referencePageElements(this, targetElement, assignToMaxPages);
		}
	}

	/**
	 * Erstellt eine RD Referenz von diesem MultiLinkElement (Container oder Liste als Source) zum gegebenen Seitenelement.
	 * <p>
	 * Achtung: Nur als Administrator aufrufbar! TODO Die Konstante 4157 ersetzen.
	 * 
	 * @param assignToMaxPages
	 *            if > 0: es werden die maxPages Anzahl von Seiten dieses Templates nachgezogen if <=0: Seiten werden gar nicht
	 *            verändert
	 */
	public void referenceTo(MultiLink targetLink, int assignToMaxPages) throws RQLException {
		if (!isMultiLink()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type multi link (List or Container). The reference could not be set.");
		}
		// referece the template element itself
		getProject().referenceElement(getTemplateElementGuid(), targetLink.getLinkGuid(), "project.4157");

		// reference in maxPages as well
		if (assignToMaxPages > 0) {
			getTemplate().referencePageLinks(this, targetLink, assignToMaxPages);
		}
	}

	/**
	 * Löscht an diesem MultiLink die Templatevorbelegung für das gegebene Template.
	 * <p>
	 * Dieses TemplateElement muss ein MultiLink Element sein.
	 */
	public void removePreassignedTemplate(Template templateToRemove) throws RQLException {

		/*
		 * V6.5 request <IODATA loginguid="5079A7CB62AD4C3086A80F82B801E85A" sessionkey="102183432328P3Cs33526"> <TEMPLATE> <ELEMENT action="unlink" guid="CFF5538F51FE4F549798CA4F6D3A81D3"> <TEMPLATES> <TEMPLATE guid="453F235E22A0465691ED55C12B3B8F4D"/> </TEMPLATES> </ELEMENT> </TEMPLATE> </IODATA> V6.5 response <IODATA>ok </IODATA>
		 */

		// check type of template element
		if (!isMultiLink()) {
			throw new WrongTypeException("Template element " + this.getName() + " is neither a List nor a Container.");
		}

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <TEMPLATE>"
				+ "    <ELEMENT action='unlink' guid='" + getTemplateElementGuid() + "'>" + "      <TEMPLATES>"
				+ "    <TEMPLATE guid='" + templateToRemove.getTemplateGuid() + "'/></TEMPLATES></ELEMENT></TEMPLATE></IODATA>";
		callCmsWithoutParsing(rqlRequest); // ignore ok return value
		// empty cache
		preassignedTemplatesCache = null;
	}

	/**
	 * Ändert den Namen dieses Template-Elements.
	 */
	public void rename(String elementName) throws RQLException {
		setName(elementName);
	}

	/**
	 * Ändert in diesem Templateelement das gegebenen Attribut auf den gegebenen Wert.
	 */
	private void save(String attributeName, String attributeValue) throws RQLException {

		/*
		 * V5 request 
		<IODATA loginguid="9F990C57F3AF4DB99059A7F35B1DA9A7" sessionkey="10218343235AEO4I6xi5B"> 
		<PROJECT> 
		<TEMPLATE> 
		<ELEMENTS> 
		<ELEMENT action="save" guid="121AFAF802EC491E9504770A394E09EA" eltignoreworkflow="0" elthideinform="1" eltdragdrop="0" eltdirectedit="0" eltlanguageindependent="0" eltrequired="0" eltinvisibleinclient="0" eltinvisibleinpage="0" eltuserdefinedallowed="1" eltdonothtmlencode="0" elteditorialelement="0" eltrddescription="#10218343235AEO4I6xi5B" eltrdexample="#10218343235AEO4I6xi5B" eltmaxsize="#10218343235AEO4I6xi5B" eltdefaultvalue="#10218343235AEO4I6xi5B" eltformatno="#10218343235AEO4I6xi5B" eltlcid="" eltformatting="" eltbeginmark="#10218343235AEO4I6xi5B" eltendmark="#10218343235AEO4I6xi5B" elttype="1" eltverifytermregexp="#10218343235AEO4I6xi5B" eltparentelementguid="#10218343235AEO4I6xi5B" eltparentelementname="#10218343235AEO4I6xi5B" /> 
		</ELEMENTS> 
		</TEMPLATE> 
		</PROJECT> 
		</IODATA> 
		V5 response 
		<IODATA> 
		<ELEMENT action="save" elthideinform="1" eltinvisibleinclient="0" lastcontentconnector="-1" deletereal="" contentconnectorerror="" languagevariantid="ENG" dialoglanguageid="ENG" parentguid="" parenttable="TPL" ignoreguids="" templateguid="E5D49F4326E249A48B74BBB81008011F" eltuserdefinedallowed="1" guid="121AFAF802EC491E9504770A394E09EA" type="1" changed="-1"> 
		</ELEMENT> 
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + " <PROJECT>"
				+ "  <TEMPLATE guid='" + getTemplateGuid() + "'>" + "    <ELEMENT action='save' guid='" + getTemplateElementGuid()
				+ "' " + attributeName + "='" + attributeValue + "'/>" + "  </TEMPLATE>" + " </PROJECT>" + "</IODATA>";
		callCms(rqlRequest);

		// delete details cache
		deleteDetailsNodeCache();
	}

	/**
	 * Ändert die Eigenschaft 'Activate DirectEdit' für Textelemente.
	 */
	public void setActivateDirectEdit(boolean activated) throws RQLException {

		if (!isText()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type Text. DirectEdit can be enabled/disabled on text elements only.");
		}
		save("eltdirectedit", StringHelper.convertTo01(activated));
	}

	/**
	 * Ändert die Eigenschaft 'Activate Drag & Drop' für Mediaelemente.
	 */
	public void setActivateDragAndDrop(boolean activated) throws RQLException {

		if (!isMedia()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type Media. Drag and Drop can be enabled/disabled on Mediaelements only.");
		}
		save("eltdragdrop", StringHelper.convertTo01(activated));
	}

	/**
	 * Ändert die description dieses Elements, das auf dem Formular als Label verwendet wird.
	 */
	public void setDescription(String description) throws RQLException {

		save("eltrddescription", StringHelper.escapeHTML(description));
	}

	/**
	 * Ändert die Eigenschaft 'Do not use in form' für dieses Element.
	 */
	public void setDoNotUseInForm(boolean doNotUse) throws RQLException {

		save("elthideinform", StringHelper.convertTo01(doNotUse));
	}

	/**
	 * Liefert true, falls dieses Element in der Formularansicht gezeigt wird ('Do not use in form'=unchecked).
	 */
	public boolean isUsedInForm() throws RQLException {

		return getDetailsNode().getAttribute("elthideinform").equals("0");
	}

	/**
	 * Liefert true, falls dieses Element relevant für den Workflow ist('Not relevant for workflow'=unchecked).
	 */
	public boolean isRelevantForWorkflow() throws RQLException {

		return getDetailsNode().getAttribute("eltignoreworkflow").equals("0");
	}

	/**
	 * Liefert true, falls dieses Element im SmartTree nicht angezeigt wird('Hide in project structure'=checked).
	 */
	public boolean isHideInProjectStructure() throws RQLException {

		return getDetailsNode().getAttribute("eltinvisibleinclient").equals("1");
	}

	/**
	 * Liefert true, falls dieses Element im SmartTree angezeigt wird('Hide in project structure'=unchecked).
	 */
	public boolean isVisibleInProjectStructure() throws RQLException {

		return !isHideInProjectStructure();
	}

	/**
	 * Ändert die Eigenschaft 'Hide in project structure' für dieses Element.
	 */
	public void setHideInProjectStructure(boolean hidden) throws RQLException {

		save("eltinvisibleinclient", StringHelper.convertTo01(hidden));
	}

	/**
	 * Ändert die Eigenschaft 'Hide in project structure' für dieses Element.
	 */
	public void setVisibleInProjectStructure(boolean visible) throws RQLException {

		setHideInProjectStructure(!visible);
	}

	/**
	 * Ändert die Eigenschaft 'Not relevant for workflow' für dieses Element.
	 */
	public void setNotRelevantForWorkflow(boolean relevant) throws RQLException {

		save("eltignoreworkflow", StringHelper.convertTo01(relevant));
	}

	/**
	 * Ändert die Eigenschaft 'Not relevant for workflow' für dieses Element.
	 */
	public void setRelevantForWorkflow(boolean relevant) throws RQLException {

		setNotRelevantForWorkflow(!relevant);
	}

	/**
	 * Ändert die Eigenschaft 'Eingabe zwingend erforderlich' für dieses Element.
	 */
	public void setIsMandatory(boolean isMandatory) throws RQLException {

		save("eltrequired", StringHelper.convertTo01(isMandatory));
	}
	
	
	public boolean getIsMandatory() {
		return isMandatory;
	}

	/**
	 * Ändert den label text dieses elements. Der label text kann HTML Tags enthalten!
	 * <p>
	 * Im RQL muss das user defined Datumsformat mitgeliefert werden, da es sonst gelöscht wird.
	 */
	public void setLabelText(String labelText) throws RQLException {

		// save to cms, restore date formatting attributes
		String attrName = "";
		if (isStandardFieldDate()) {
			RQLNode detailsNode = getDetailsNode();
			attrName = "eltformatno='" + detailsNode.getAttribute("eltformatno") + "' ";
			attrName += "eltlcid='" + detailsNode.getAttribute("eltlcid") + "' ";
			// restore user defined date format
			if (isDateFormatUserDefined()) {
				attrName += "eltformatting='" + detailsNode.getAttribute("eltformatting") + "' ";
			}
		}
		// add the label and save in cms
		this.labelText = StringHelper.escapeHTML(labelText);
		attrName += "eltrddescription";
		save(attrName, this.labelText);
	}

	/**
	 * Ändert den Namen dieses Template-Elements.
	 */
	public void setName(String elementName) throws RQLException {
		save("eltname", elementName);
		// update cached name
		this.name = elementName;
	}

	/**
	 * Setzt an diesem MultiLink die Templatevorbelegung auf das gegebene Template.
	 * <p>
	 * Falls der Parameter restrictConnectToo auf true steht, können bei connect to existing page auch nur die vorbelegten Templates
	 * verbunden werden.
	 * <p>
	 * Dieses TemplateElement muss ein MultiLink Element sein.
	 * <p>
	 * 
	 * @see #addPreassignedTemplate(Template, boolean)
	 */
	public void setPreassignedTemplate(Template preassignedTemplate, boolean restrictConnectToo) throws RQLException {
		java.util.List<Template> preassignedTemplates = new ArrayList<Template>();
		preassignedTemplates.add(preassignedTemplate);
		setPreassignedTemplates(preassignedTemplates, restrictConnectToo);
	}

	/**
	 * Setzt an diesem MultiLink die Templatevorbelegung für die gegebenen Templates.
	 * <p>
	 * Falls der Parameter restrictConnectToo auf true steht, können bei connect to existing page auch nur die vorbelegten Templates
	 * verbunden werden.
	 * <p>
	 * Dieses TemplateElement muss ein MultiLink Element sein.
	 */
	private void setPreassignedTemplates(java.util.List<Template> preassignedTemplates, boolean restrictConnectToo)
			throws RQLException {

		/*
		 * V7.5 request <IODATA loginguid="5079A7CB62AD4C3086A80F82B801E85A" sessionkey="102183432328P3Cs33526"> <TEMPLATE> <ELEMENT action="assign" guid="CFF5538F51FE4F549798CA4F6D3A81D3"> <TEMPLATES extendedrestriction='1'> <TEMPLATE guid="453F235E22A0465691ED55C12B3B8F4D"/> ... </TEMPLATES> </ELEMENT> </TEMPLATE> </IODATA> V7.5 response <IODATA>ok </IODATA>
		 */

		// check type of template element
		if (!isMultiLink()) {
			throw new WrongTypeException("Template element " + this.getName() + " is neither a List nor a Container.");
		}

		// build request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <TEMPLATE>"
				+ "    <ELEMENT action='assign' guid='" + getTemplateElementGuid() + "'>";

		// add property to rql request, if connect to existing page should use only the preassigned templates
		if (restrictConnectToo) {
			rqlRequest += "<TEMPLATES extendedrestriction='1'>";
		} else {
			rqlRequest += "<TEMPLATES>";
		}

		// add all given templates to preassign
		for (Template template : preassignedTemplates) {
			rqlRequest += "<TEMPLATE guid='" + template.getTemplateGuid() + "'/>\n";
		}

		// close request xml
		rqlRequest += "</TEMPLATES></ELEMENT></TEMPLATE></IODATA>";

		// call CMS
		callCmsWithoutParsing(rqlRequest); // ignore ok return value
		// empty cache
		preassignedTemplatesCache = null;
	}

	/**
	 * Ändert dieses TemplateElement so, dass es im SmartTree angezeigt wird.
	 */
	public void setShowInProjectStructure(boolean show) throws RQLException {

		setHideInProjectStructure(!show);
	}

	/**
	 * Ändert die Eigenschaft 'Activate Drag & Drop' für Textelemente.
	 */
	public void setTextEditorActivateDragDrop(boolean showButton) throws RQLException {

		if (!isHtmlText()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type HTML text. Activate drag & Drop can be enabled/disabled on HTML text elements only.");
		}
		if (showButton) {
			textEditorSettings = textEditorSettings.clearBit(TEXT_EDITOR_ACTIVATE_DRAG_DROP_BIT_INDEX);
		} else {
			textEditorSettings = textEditorSettings.setBit(TEXT_EDITOR_ACTIVATE_DRAG_DROP_BIT_INDEX);
		}
		save("elteditoroptions", textEditorSettings.toString());
	}

	/**
	 * Enabled/Disabled den Toolbarbutton im Texteditor 'Do not allow wrapping' für Textelemente.
	 * 
	 * @param showButton
	 *            =true zeigt den Button an; =false versteckt den Button
	 */
	public void setTextEditorDoNotAllowWrapping(boolean showButton) throws RQLException {

		if (!isHtmlText()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type HTML text. Do not allow wrapping can be enabled/disabled on HTML text elements only.");
		}
		if (showButton) {
			textEditorSettings = textEditorSettings.clearBit(TEXT_EDITOR_DO_NOT_ALLOW_WRAPPING_BIT_INDEX);
		} else {
			textEditorSettings = textEditorSettings.setBit(TEXT_EDITOR_DO_NOT_ALLOW_WRAPPING_BIT_INDEX);
		}
		save("elteditoroptions", textEditorSettings.toString());
	}

	/**
	 * Enabled/Disabled den Toolbarbutton im Texteditor 'Paste Formatted' für Textelemente.
	 * 
	 * @param showButton
	 *            =true zeigt den Button an; =false versteckt den Button
	 */
	public void setTextEditorPasteFormatted(boolean showButton) throws RQLException {

		if (!isHtmlText()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type HTML text. PasteFormatted can be enabled/disabled on HTML text elements only.");
		}
		if (showButton) {
			textEditorSettings = textEditorSettings.clearBit(TEXT_EDITOR_PASTE_FORMATTED_BIT_INDEX);
		} else {
			textEditorSettings = textEditorSettings.setBit(TEXT_EDITOR_PASTE_FORMATTED_BIT_INDEX);
		}
		save("elteditoroptions", textEditorSettings.toString());
	}

	/**
	 * Ändert die zugewiesenen Stylesheets dieses HTML Textelements. RD Funktion Assign fixed Stylesheets.
	 */
	public void setTextFixedStylesheet(String stylesheets) throws RQLException {

		if (!isHtmlText()) {
			throw new WrongTypeException("Template element " + getName()
					+ " is not of type HTML text. A fixed stylesheet can assigned only on HTML text elements.");
		}
		textFixedStylesheet = StringHelper.escapeHTML(stylesheets);
		save("eltstylesheetdata", textFixedStylesheet);
	}

	/**
	 * Ändert dieses TemplateElement so, dass es in Formularen angezeigt wird, wenn useInForm true ist.
	 */
	public void setUseInForm(boolean useInForm) throws RQLException {
		setDoNotUseInForm(!useInForm);
	}

	/**
	 * Ändert dieses Datumselement auf das gegebene userdefinierte Format für das gegebene Gebietsschema.
	 */
	public void setUserDefinedDateFormat(Locale locale, String dateFormat) throws RQLException {

		// check if of type standard field date
		if (!isStandardFieldDate()) {
			throw new WrongTypeException("Template element " + getName() + " is not of type Standard-Field Date.");
		}

		// use save, but with >1 attributes
		save("eltformatno='-1' eltlcid='" + locale.getLcid() + "' eltformatting", dateFormat);
	}

	/**
	 * Liefert die RQLNodeList aller vorbelegten Templates dieses TemplateElements.
	 * <p>
	 * Dieses TemplateElement muss ein MultiLink Element sein. ACHTUNG: Dieses RQL liefert nur die Templates, die der angemeldete Autor
	 * auch erstellen kann!
	 * 
	 * @return java.util.List of Templates
	 * @see Template
	 * @throws WrongTypeException
	 */
	private RQLNodeList getPreassignedTemplatesNodeListPrimitive() throws RQLException {

		/*
		 * V5 request <IODATA loginguid="9CD51B59DF96453BA177EF8E47C336F3" sessionkey="35197944314oGL62J7pE"> <TEMPLATE> <ELEMENT guid="CFF5538F51FE4F549798CA4F6D3A81D3"> <TEMPLATES action="list"/> </ELEMENT> </TEMPLATE> </IODATA> V5 response <IODATA> <TEMPLATES> <TEMPLATE guid="6D624494686C4CC9B4827CF0191A7A68" name="image_block" description="a block containing an image only" folderguid="6A6740BC44F7459081BFD1F25B1BF8F6" selectinnewpage="0" /> <TEMPLATE guid="F4AC60E9DA0E4A3BA4533C4C2C309DDD" name="text_block" description="a block of text" folderguid="6A6740BC44F7459081BFD1F25B1BF8F6" selectinnewpage="0" /> </TEMPLATES> </IODATA>
		 */

		// check type of template element
		if (!isMultiLink()) {
			throw new WrongTypeException("Template element " + this.getName() + " is neither a List nor a Container.");
		}

		// call CMS
		if (preassignedTemplatesCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "  <TEMPLATE>"
					+ "    <ELEMENT guid='" + getTemplateElementGuid() + "'>" + "      <TEMPLATES action='list'/>" + "    </ELEMENT>"
					+ "  </TEMPLATE>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			preassignedTemplatesCache = rqlResponse.getNodes("TEMPLATE");
		}
		return preassignedTemplatesCache;
	}

	/**
	 * Ändert die Negation der Eigenschaft 'Do not convert characters to HTML' für Textelemente.
	 */
	public void setConvertCharacters2Html(boolean encodeHtmlEntities) throws RQLException {
		// only possible on these elements
		if (!(isStandardField() || isText() || isOptionList() || isHeadline())) {
			throw new WrongTypeException(
					"Template element "
							+ getName()
							+ " is not one of following types: Headline, StandardField, Text, OptionList. Do not convert characters to HTML can be enabled/disabled only on these elements.");
		}
		save("eltdonothtmlencode", StringHelper.convertTo01(!encodeHtmlEntities));
	}

	/**
	 * Debug output.
	 */
	public String toString() {
		return this.getClass().getName() + " (" + getName() + ")";
	}

	/**
	 * Liefert für das Save einer Optionsliste alle Selection Tags (inkl. SELECTIONS Tag drumherum) als Attribut eltoptionlistdata=....
	 */
	String getOptionListSelectionsSaveAttribute() throws RQLException {
		return "eltoptionlistdata='" + getOptionListSelectionsSaveAttributeValue() + "' ";
	}

	/**
	 * Liefert für eine Optionsliste alle Selection Tags (inkl. SELECTIONS Tag drumherum) konvertier als Attributevalue für das save.
	 */
	private String getOptionListSelectionsSaveAttributeValue() throws RQLException {
		return StringHelper.encodeHtml(getOptionListSelectionsTag());
	}

	/**
	 * Liefert den RQL response im XML format mit allen Optionslisten-Einträgen zu diesem option list Templateelement zurück.
	 * <p>
	 * Das SELECTIONS tag umklammert die Antwort.
	 */
	private String getOptionListSelectionsTag() throws RQLException {

		/*
		 * erweiterte Elementdaten mit option list selections V9 request <IODATA loginguid="C39A459C2D83457C9896D6406509D37E" sessionkey="66DFAAF7FEDE497AA116A8EB3113C12A"> <PROJECT> <TEMPLATE> <ELEMENT action="load" guid="A49052E3F743483A9814503F4C6948F6"> <SELECTIONS action="load" guid="A49052E3F743483A9814503F4C6948F6"/> </ELEMENT> </TEMPLATE> </PROJECT> </IODATA> V9 response <IODATA> <TEMPLATE languagevariantid="ENG" parentobjectname="PROJECT" useconnection="1" dialoglanguageid="ENG"> <ELEMENT action="load" languagevariantid="ENG" dialoglanguageid="ENG" parentguid="" parenttable="TPL" eltignoreworkflow="0" eltlanguageindependent="0" eltlanguagedependentvalue="0" eltlanguagedependentname="0" eltparentelementguid="475487C27CD94A17BE6079E2148594E7" eltparentelementname="responsible_area" guid="A49052E3F743483A9814503F4C6948F6" eltname="responsible_mail_subject_statistic_area" elttype="8" eltid="10" eltislink="0" eltfolderguid="" eltcrlftobr="0" eltisdynamic="0" eltonlyhrefvalue="0" eltrequired="1" usepagemainlinktargetcontainer="0" elteditorialelement="0" eltpicsalllanguages="0" eltinvisibleinclient="0" eltinvisibleinpage="0" elthideinform="0" eltdragdrop="0" eltxhtmlcompliant="0" eltdonothtmlencode="0" eltuserdefinedallowed="0" eltextendedlist="0" eltflags="512" templateguid="94A47892718042ADBC4822A9CC7C67EC" eltorderby="2" eltrddescription="statistic area in e-mail subject"> <SELECTIONS> <SELECTION guid="24E9973989FC4B038896C84AF2954EAB" identifier="24E9973989FC4B038896C84AF2954EAB" description="BAF" value="BAF"> <ITEM languageid="ENG" name="BAF">BAF</ITEM> </SELECTION> <SELECTION guid="4F438F4C4E0D459D85DD130840A28ACD" identifier="4F438F4C4E0D459D85DD130840A28ACD" description="COM" value="COM"> <ITEM languageid="ENG" name="COM">COM</ITEM> </SELECTION> <SELECTION guid="C1DDB7B7918E4FE5B3DBADB06E543E1A" identifier="C1DDB7B7918E4FE5B3DBADB06E543E1A" description="HUR" value="HUR"> <ITEM languageid="ENG" name="HUR">HUR</ITEM> </SELECTION> <SELECTION guid="E060EFEE50004E3EA33069ACF654DF24" identifier="E060EFEE50004E3EA33069ACF654DF24" description="OPS" value="OPS"> <ITEM languageid="ENG" name="OPS">OPS</ITEM> </SELECTION> <SELECTION guid="E96AFF53B8F8447E94AC484E4633545C" identifier="E96AFF53B8F8447E94ACF84E4633545C" description="SAL" value="SAL"> <ITEM languageid="ENG" name="SAL">SAL</ITEM> </SELECTION> <SELECTION guid="B919AB617C6D4652A8D4FAC4CA367B56" identifier="B919AB617C6D4652A8D4FAC4CA367B56" description="test" value="test"> <ITEM languageid="ENG" name="test">test</ITEM> </SELECTION> </SELECTIONS> </ELEMENT> </TEMPLATE> </IODATA>
		 */

		// check type
		if (!isOptionList()) {
			throw new WrongTypeException("Template element " + getName() + " of content class " + getTemplateName()
					+ " is not an option list. Please correct your program.");
		}

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "' ><PROJECT><TEMPLATE>"
				+ " <ELEMENT action='load' guid='" + getTemplateElementGuid() + "'>" + "<SELECTIONS action='load' guid='"
				+ getTemplateElementGuid() + "'/>" + "</ELEMENT></TEMPLATE></PROJECT></IODATA>";
		String xmlResponse = callCmsWithoutParsing(rqlRequest);
		return StringHelper.getTag(xmlResponse, "SELECTIONS");
	}

	/**
	 * Zwei Templateelemente werden als identisch interpretiert, falls beide die gleiche GUID haben.
	 */
	public boolean equals(Object obj) {
		TemplateElement second = (TemplateElement) obj;
		return this.getTemplateElementGuid().equals(second.getTemplateElementGuid());
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

		return getTemplateElementGuid().hashCode();
	}

	/**
	 * Returns the template code for the usage of this template element, e.g. <%headline%>
	 * 
	 * @see #getTemplateCode(String)
	 */
	public String getTemplateCode() throws RQLException {
		return getTemplateCode(getName());
	}

	/**
	 * Returns the template code for the red dot of this template element, e.g. <!IoRedDot_headline>
	 * 
	 * @see #getTemplateCodeRedDot(String)
	 */
	public String getTemplateCodeRedDot() throws RQLException {
		return getTemplateCodeRedDot(getName());
	}
	/**
	 * Returns true, if the name of this template element is equal to at least one of the given strings.
	 */
	public boolean isNameEquals(String... elementNames) throws RQLException {
		String templateName = getName();
		for (String name : elementNames) {
			if (templateName.equals(name)) {
				return true;
			}
		}
		// otherwise
		return false;
	}

	
	/**
	 * Structure elements: Check for extended restrictions.
	 * 
	 * @return true if eltextendedrestriction="1"
	 */
	public boolean hasExtendedRestrictions() throws RQLException {
		String v = getDetailsNode().getAttribute("eltextendedrestriction");
		return v != null && v.equals("1");
	}
	
	
	/**
	 * Links: Load definitions of what can be created here.
	 * 
	 * @return never null.
	 */
	public TemplateGroups loadExtendedRestrictions() throws RQLException {
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "  <TEMPLATELIST action='load' linkguid='"+templateElementGuid+"' assigntemplates='1' withpagedefinitions='1' />"
				+ "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		return new TemplateGroups(getProject(), rqlResponse.getNode("TEMPLATEGROUPS"));
	}
	
	
	/**
	 * Links: Change the list of templates that can be used here.
	 * 
	 * @param checkbox the global checkbox, should be set when list is not empty.
	 * @param templateGuids The GUIDs to allows
	 */
	public void saveExtendedRestrictions(boolean checkbox, Collection<String> templateGuids) throws RQLException {
		StringBuilder sb = new StringBuilder(256);
		sb.append("<IODATA loginguid='").append(getLogonGuid()).append("' sessionkey='").append(getSessionKey()).append("'>");
		sb.append("<TEMPLATE><ELEMENT action='assign' guid='").append(templateElementGuid).append("'>");
		sb.append("<TEMPLATES extendedrestriction='").append(checkbox?"1":"0").append("'>");
		for (String guid : templateGuids) {
			sb.append("<TEMPLATE guid='").append(guid).append("' />");
		}
		sb.append("</TEMPLATES>");
		sb.append("</ELEMENT></TEMPLATE>");
		sb.append("</IODATA>");
		callCms(sb.toString());
	}
}
