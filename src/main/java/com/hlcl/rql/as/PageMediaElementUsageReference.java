package com.hlcl.rql.as;

/**
 * @author lejafr This class represents the usage reference of the given file from a page media element.
 */
public class PageMediaElementUsageReference extends UsageReference {

	private MediaElement mediaElement;

	/**
	 * @param file
	 *            parent file this reference belongs to
	 * @param languageVariantsCodes
	 *            the 3 letter codes of the language variants this file is referenced
	 * @param mediaElement
	 *            the page media element using the given file
	 * @throws RQLException
	 */
	public PageMediaElementUsageReference(File file, String languageVariantsCodes, MediaElement mediaElement) throws RQLException {
		super(file, languageVariantsCodes);
		this.mediaElement = mediaElement;
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
		return mediaElement.getPage().getHeadlineAndId();
	}

	/**
	 * Returns the name of the content class element the page image element is based on.
	 * 
	 * @see com.hlcl.rql.as.UsageReference#getElementName()
	 */
	@Override
	public String getElementName() {
		return mediaElement.getTemplateElementName();
	}

	/**
	 * Returns the type name, fix page media element.
	 * 
	 * @see com.hlcl.rql.as.UsageReference#getType()
	 */
	@Override
	public String getType() {
		return "page media element";
	}

	/**
	 * Returns true, if this file usage reference can be renamed. This works only if the media element's template element uses a folder
	 * which is stored on a network location.
	 * 
	 * @throws RQLException
	 * 
	 * @see TemplateElement#isFolderStoredInFileSystemUnc()
	 * @see Page#isChangeable(java.util.List)
	 */
	@Override
	public boolean canBeRenamed() throws RQLException {
		return mediaElement.getPage().isChangeable(getChangeableLanguageVariants()) && mediaElement.isTemplateElementFolderStoredInFileSystemUnc();
	}

	/**
	 * Returns true, if this reference should be treated language variant dependent or not. Subclasses refer this decision back to the
	 * underlaying template element.
	 */
	public boolean isLanguageVariantDependent() throws RQLException {
		return mediaElement.isTemplateElementLanguageVariantDependent();
	}

	/**
	 * Reflect the renaming of the file, which is already finished, on this usage reference as well.
	 */
	public void renameTo(String newFilename) throws RQLException {
		mediaElement.setFilename(newFilename, getChangeableLanguageVariants());
	}

}
