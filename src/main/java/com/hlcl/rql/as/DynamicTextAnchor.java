package com.hlcl.rql.as;

/**
 * This class represents a Dynamic Text Anchor (DynLink).
 * 
 * @author lejafr 
 */
public class DynamicTextAnchor extends DynamicAnchor {
	/**
	 * Erzeugt einen dynamischen Text Anker.
	 *
	 * @param	page	Seite, die diesen Dynamischen Anker beinhaltet.
	 * @param	size	Anzahl der Links in diesem Set.
	 */
	public DynamicTextAnchor(Page page, int size) {
		super(page, size);
	}

	/**
	 * Liefert den ersten Textanchor mit dem gegebenen Namen zurück.
	 *
	 * @param textAnchorName	Name des Anchor
	 */
	public TextAnchor findByName(String textAnchorName) {

		return (TextAnchor) super.findAnchorByName(textAnchorName);
	}
}
