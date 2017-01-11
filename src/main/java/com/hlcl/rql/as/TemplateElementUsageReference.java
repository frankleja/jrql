package com.hlcl.rql.as;

/**
 * @author lejafr This class represents the usage reference of the given file from a template element.
 */
public class TemplateElementUsageReference extends UsageReference {

	private TemplateElement templateElement;

	/**
	 * @param file
	 *            parent file this reference belongs to
	 * @param usedLanguageVariantCodes
	 *            the 3 letter codes of the language variants this file is referenced
	 * @param templateElement
	 *            the template element of type image or media using the given file as a default
	 * @throws RQLException
	 */
	public TemplateElementUsageReference(File file, String usedLanguageVariantCodes, TemplateElement templateElement)
			throws RQLException {
		super(file, usedLanguageVariantCodes);
		this.templateElement = templateElement;
	}

	/**
	 * Returns the name of the content class.
	 * 
	 * @throws RQLException
	 * 
	 * @see com.hlcl.rql.as.UsageReference#getElementHostName()
	 */
	@Override
	public String getElementHostName() throws RQLException {
		return templateElement.getTemplateName();
	}

	/**
	 * Returns the name of the content class element.
	 * 
	 * @see com.hlcl.rql.as.UsageReference#getElementName()
	 */
	@Override
	public String getElementName() {
		return templateElement.getName();
	}

	/**
	 * Returns the type name, fix content class element.
	 * 
	 * @see com.hlcl.rql.as.UsageReference#getType()
	 */
	@Override
	public String getType() {
		return "content class element";
	}

	/**
	 * Returns true, if this file usage reference can be renamed. This works only if the template element uses a folder which is stored
	 * on a network location.
	 * 
	 * @throws RQLException
	 * 
	 * @see TemplateElement#isFolderStoredInFileSystemUnc()
	 */
	@Override
	public boolean canBeRenamed() throws RQLException {
		return templateElement.isFolderStoredInFileSystemUnc();
	}

	/**
	 * Returns true, if this reference should be treated language variant dependent or not. Subclasses refer this decision back to the
	 * underlaying template element.
	 */
	public boolean isLanguageVariantDependent() throws RQLException {
		return templateElement.isLanguageVariantDependent();
	}

	/**
	 * Reflect the renaming of the file, which is already finished, on this usage reference as well.
	 * <p>
	 * Change the default file on this template element to the new filename.
	 */
	public void renameTo(String newFilename) throws RQLException {
		templateElement.setDefaultFilename(newFilename, getChangeableLanguageVariants());
	}

}
