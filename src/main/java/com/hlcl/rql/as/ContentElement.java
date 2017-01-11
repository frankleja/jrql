package com.hlcl.rql.as;

/**
 * This interface unify handling of Element and TextElement objects.
 * 
 * @author LEJAFR
 */
public interface ContentElement {
	/**
	 * Returns the content element's value as string. Browse implementors to find default conversion.
	 */
	public String getValueAsString() throws RQLException;

	/**
	 * Returns the template element this content element is based on. 
	 */
	public TemplateElement getTemplateElement() throws RQLException;

	/**
	 * Returns the template element name this content element is based on. 
	 */
	public String getTemplateElementName() throws RQLException;
}
