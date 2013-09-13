package com.hlcl.rql.as;

/**
 * @author lejafr This class represents the usage reference of the given file from a page image element.
 */
public class PageImageElementUsageReference extends UsageReference {

	private ImageElement imageElement;

	/**
	 * @param file
	 *            parent file this reference belongs to
	 * @param languageVariantsCodes
	 *            the 3 letter codes of the language variants this file is referenced
	 * @param imageElement
	 *            the page image element using the given file
	 * @throws RQLException
	 */
	public PageImageElementUsageReference(File file, String languageVariantsCodes, ImageElement imageElement) throws RQLException {
		super(file, languageVariantsCodes);
		this.imageElement = imageElement;
	}

	/**
	 * Returns the page headline and id.
	 * 
	 * @throws RQLException
	 * 
	 * @see com.hlcl.rql.as.UsageReference#getElementHostName()
	 */
	@Override
	public String getElementHostName() throws RQLException {
		return imageElement.getPage().getHeadlineAndId();
	}

	/**
	 * Returns the name of the content class element the page image element is based on.
	 * 
	 * @see com.hlcl.rql.as.UsageReference#getElementName()
	 */
	@Override
	public String getElementName() {
		return imageElement.getTemplateElementName();
	}

	/**
	 * Returns the type name, fix page media element.
	 * 
	 * @see com.hlcl.rql.as.UsageReference#getType()
	 */
	@Override
	public String getType() {
		return "page image element";
	}

	/**
	 * Returns true, if this file usage reference can be renamed. This works only if the image element's template element uses a folder
	 * which is stored on a network location.
	 * 
	 * @throws RQLException
	 * 
	 * @see TemplateElement#isFolderStoredInFileSystemUnc()
	 * @see Page#isChangeable(java.util.List)
	 */
	@Override
	public boolean canBeRenamed() throws RQLException {
		return imageElement.getPage().isChangeable(getChangeableLanguageVariants()) && imageElement.isTemplateElementFolderStoredInFileSystemUnc();
	}

	/**
	 * Reflect the renaming of the file, which is already finished, on this usage reference as well.
	 */
	public void renameTo(String newFilename) throws RQLException {
		imageElement.setFilename(newFilename, getChangeableLanguageVariants());
	}

	/**
	 * Returns true, if this reference should be treated language variant dependent or not. Subclasses refer this decision back to the
	 * underlaying template element.
	 */
	public boolean isLanguageVariantDependent() throws RQLException {
		return imageElement.isTemplateElementLanguageVariantDependent();
	}
}
