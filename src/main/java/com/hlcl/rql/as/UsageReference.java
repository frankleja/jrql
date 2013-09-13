package com.hlcl.rql.as;

import java.util.ArrayList;

/**
 * @author lejafr This class is the abstract superclass of all usage references of a file within a folder. The classes below
 *         encapsulate the result of the show usage RQL response.
 */
public abstract class UsageReference implements ProjectContainer {

	private java.util.List<LanguageVariant> usedLanguageVariants;
	private File file;

	static final String PARENT_TREETYPE_PAGE = "page";
	static final String PARENT_TREETYPE_CONTENTCLASS = "app.4015";

	/**
	 * UsageReference constructor comment.
	 * 
	 * @param file
	 *            parent file this reference belongs to
	 * @param usedLanguageVariantCodes
	 *            the 3 letter codes of the language variants this file is referenced
	 * @throws RQLException
	 */
	public UsageReference(File file, String usedLanguageVariantCodes) throws RQLException {
		this.file = file;
		parseUsedLanguageVariants(usedLanguageVariantCodes);
	}

	/**
	 * Returns a type string for this reference; subclass responsibility.
	 */
	public abstract String getType();

	/**
	 * Returns the page id and headline or the name of the content class.
	 */
	public abstract String getElementHostName() throws RQLException;

	/**
	 * Reflect the renaming of the file, which is already finished, on this usage reference as well.
	 */
	public abstract void renameTo(String newFilename) throws RQLException;

	/**
	 * Returns true, if this reference should be treated language variant dependent or not. Subclasses refer this decision back to the
	 * underlaying template element.
	 */
	public abstract boolean isLanguageVariantDependent() throws RQLException;

	/**
	 * Returns the name of the template element.
	 */
	public abstract String getElementName() throws RQLException;

	/**
	 * Returns all language variants in which this file is used. For language variant independent elements all language variants are
	 * returned.
	 */
	public java.util.List<LanguageVariant> getUsedLanguageVariants() {
		return usedLanguageVariants;
	}

	/**
	 * Returns all language variants in which this file can be updated. For language variant independent elements only the main
	 * language variant is returned.
	 * 
	 * @throws RQLException
	 * 
	 * @see FileElement#setFilename(String, java.util.List)
	 */
	public java.util.List<LanguageVariant> getChangeableLanguageVariants() throws RQLException {
		if (!isLanguageVariantDependent()) {
			// lv independent, only main lv
			java.util.List<LanguageVariant> result = new ArrayList<LanguageVariant>();
			result.add(getProject().getMainLanguageVariant());
			return result;
		} else {
			// lv dependent, use all
			return getUsedLanguageVariants();
		}
	}

	/**
	 * Returns all language variants rfc language IDs in which this file is used.
	 * 
	 * @see LanguageVariant#getRfcLanguageId();
	 */
	public String getUsedLanguageVariantRfcLanguageIds(String delimiter) {
		String result = "";
		for (LanguageVariant lv : usedLanguageVariants) {
			result += lv.getRfcLanguageId() + delimiter;
		}
		return StringHelper.removeSuffix(result, delimiter);
	}

	/**
	 * Returns the file, this usage reference belongs to.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Parses and set the used language variants from the given 3 letter lv codes, e.g. ENG, ESN, CHS, DEU.
	 * 
	 * @throws RQLException
	 */
	private void parseUsedLanguageVariants(String usedLanguageVariantCodes) throws RQLException {
		this.usedLanguageVariants = new ArrayList<LanguageVariant>();

		// remove blanks and split
		String[] codes = StringHelper.split(StringHelper.removeBlanks(usedLanguageVariantCodes), ",");
		for (int i = 0; i < codes.length; i++) {
			String lvCode = codes[i];
			this.usedLanguageVariants.add(getProject().getLanguageVariantByLanguage(lvCode));
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
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getFile().getCmsClient();
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {
		return getFile().getLogonGuid();
	}

	/**
	 * Liefert das Projekt.
	 */
	public Project getProject() {
		return getFile().getProject();
	}

	/**
	 * Liefert true, falls die augenblickliche Sprachvariante die Hauptsprachvariante ist, sonst false.
	 * 
	 * @throws RQLException
	 */
	public boolean isCurrentLanguageVariantMainLanguage() throws RQLException {
		return getProject().isCurrentLanguageVariantMainLanguage();
	}

	/**
	 * Liefert die RedDot GUID des Projekts.
	 */
	public String getProjectGuid() throws RQLException {
		return getFile().getProjectGuid();
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getFile().getSessionKey();
	}

	/**
	 * Returns true, if this file usage reference can be renamed. This works only if this file reference is stored in an folder which
	 * is stored on a network location.
	 * 
	 * @throws RQLException
	 * 
	 * @see Folder#isStoredInFileSystemUnc()
	 */
	public abstract boolean canBeRenamed() throws RQLException;
}
