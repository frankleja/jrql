package com.hlcl.rql.as;

/**
 * TextAnchor structural element, not a dynamic text anchor.
 * 
 * @author lejafr
 */
public class TextAnchor extends Anchor {
	
	
	/**
	 * Container constructor comment.
	 * 
	 * @param page
	 *            Seite, die diesen Link beinhaltet.
	 * @param name
	 *            Name des Links auf Seite page
	 * @param anchorGuid
	 *            GUID des Ankers auf Seite page
	 */
	public TextAnchor(Page page, TemplateElement templateElement, String name, String anchorGuid, boolean isReferenceSource) {
		super(page, templateElement, name, anchorGuid, isReferenceSource);
	}

	
	/**
	 * Construct from the actual node.
	 * 
	 * @param page containing htis link.
	 * @param textAnchorTemplateElement the element definition
	 * @param anchorNode the "<LINKS action="load"/>" response
	 */
	public TextAnchor(Page page, TemplateElement textAnchorTemplateElement, RQLNode anchorNode) {
		super(page, textAnchorTemplateElement, anchorNode);
	}

}
