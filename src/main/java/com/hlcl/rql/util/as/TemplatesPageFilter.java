package com.hlcl.rql.util.as;

import java.util.ArrayList;
import java.util.List;

import com.hlcl.rql.as.*;

/**
 * @author lejafr
 *
 * Diese Klasse filtert Seiten nach den gegebenen Templatenamen. 
 * 
 * Die Selektion kann invertiert werden. 
 * Dann liefert der Filter nur Seiten, die nicht auf den gegebenen Templates basieren.
 */
public class TemplatesPageFilter extends PageFilterImpl {

	private boolean invertSelection;
	private java.util.List<String> templateNames;

	/**
	 * TemplatesPageFilter constructor für die inverse Selektion.
	 *
	 *@param	templates	Liste mit erlaubten Templates
	 *@param	invertSelection	=true, liefert alle Seiten, die nicht auf templateNames basieren
	 *							=false, (default) liefert alle Seite, basierend auf templateNames
	 */
	public TemplatesPageFilter(java.util.List<Template> templates, boolean invertSelection) {
		super();

		initialize(templates);
		this.invertSelection = invertSelection;
	}

	/**
	 * TemplatesPageFilter constructor comment.
	 *
	 *@param	templateNames	Liste mit Templatenamen, z.B. text_block,image_block,text_table_block
	 *@param	delimiter		Trennzeichen der Liste templateNames
	 */
	public TemplatesPageFilter(String templateNames, String delimiter) {
		super();

		initialize(templateNames, delimiter);
		invertSelection = false;
	}

	/**
	 * TemplatesPageFilter constructor für die inverse Selektion.
	 *
	 *@param	templateNames	Liste mit Templatenamen, z.B. text_block,image_block,text_table_block
	 *@param	delimiter		Trennzeichen der Liste templateNames
	 *@param	invertSelection	=true, liefert alle Seiten, die nicht auf templateNames basieren
	 *							=false, (default) liefert alle Seite, basierend auf templateNames
	 */
	public TemplatesPageFilter(String templateNames, String delimiter, boolean invertSelection) {
		super();

		initialize(templateNames, delimiter);
		this.invertSelection = invertSelection;
	}

	/** 
	 * @see PageFilterImpl#check(Page)
	 */
	public boolean check(Page page) throws RQLException {

		boolean result = templateNames.contains(page.getTemplateName());
		if (invertSelection) {
			result = !result;
		}
		return result;
	}

	/** 
	 * Initialize the converted list of template names.
	 */
	private void initialize(List<Template> templates) {
		this.templateNames = new ArrayList<String>(templates.size());
		// collect names only
		for (Template template : templates) {
			templateNames.add(template.getName());
		}
	}

	/** 
	 * Initialize the converted list of template names.
	 */
	private void initialize(String templateNames, String delimiter) {
		this.templateNames = StringHelper.split(templateNames, delimiter.charAt(0));
	}
}
