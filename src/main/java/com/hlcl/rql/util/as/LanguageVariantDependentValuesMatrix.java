/**
 * 
 */
package com.hlcl.rql.util.as;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;

import com.hlcl.rql.as.ContentElement;
import com.hlcl.rql.as.LanguageVariant;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.TemplateElement;

/**
 * This class helps to visualize content element values in different language variants.
 * 
 * @author lejafr
 */
public class LanguageVariantDependentValuesMatrix {

	private ListOrderedMap elementMatrix; // accordingly to <TemplateElement, ListOrderedMap<LanguageVariant, ContentElement>>
	private ListOrderedMap propertyMatrix; // accordingly to <String, ListOrderedMap<LanguageVariant, String>>

	/**
	 * Constructor, creates a matrix object.
	 */
	public LanguageVariantDependentValuesMatrix() {
		super();

		// initialize map
		elementMatrix = new ListOrderedMap();
		propertyMatrix = new ListOrderedMap();
	}

	/**
	 * Remember the given page headline under the given language variant.
	 */
	public void addProperty(String property, String value, LanguageVariant languageVariant) throws RQLException {
		if (propertyMatrix.containsKey(property)) {
			// add at existing property new language variant value
			ListOrderedMap lvMap = getPropertyMap(property);
			lvMap.put(languageVariant, value);
		} else {
			// initialize new property at all
			ListOrderedMap lvMap = new ListOrderedMap();
			lvMap.put(languageVariant, value);
			propertyMatrix.put(property, lvMap);
		}
	}

	/**
	 * Remember the given content elements in given language variant and preserve the order of content elements and language variants.
	 * <p>
	 * Call this add method consistent for the same language variants for all added content elements.
	 */
	public void add(java.util.List<ContentElement> contentElements, LanguageVariant languageVariant) throws RQLException {
		for (ContentElement contentElement : contentElements) {
			add(contentElement, languageVariant);
		}
	}

	/**
	 * Remember the given content element in given language variant and preserve the order of content elements and language variants.
	 * <p>
	 * Call this add method consistent for the same language variants for all added content elements.
	 */
	public void add(ContentElement contentElement, LanguageVariant languageVariant) throws RQLException {
		// get value only to cache it within content element
		contentElement.getValueAsString();

		// add
		TemplateElement templateElement = contentElement.getTemplateElement();
		if (elementMatrix.containsKey(templateElement)) {
			// add for new language variant
			ListOrderedMap lvMap = getElementMap(templateElement);
			lvMap.put(languageVariant, contentElement);
		} else {
			// initialize new template element
			ListOrderedMap lvMap = new ListOrderedMap();
			lvMap.put(languageVariant, contentElement);
			elementMatrix.put(templateElement, lvMap);
		}
	}

	/**
	 * Returns the language variant content element map for the given template element.
	 */
	private ListOrderedMap getElementMap(TemplateElement templateElement) {
		return (ListOrderedMap) elementMatrix.get(templateElement);
	}

	/**
	 * Returns the language variant property map for the given property name.
	 */
	private ListOrderedMap getPropertyMap(String property) {
		return (ListOrderedMap) propertyMatrix.get(property);
	}

	/**
	 * Returns the value of the given property for the given language variant.
	 */
	public String getPropertyValue(String property, LanguageVariant languageVariant) throws RQLException {
		return (String) getPropertyMap(property).get(languageVariant);
	}

	/**
	 * Returns the value of the content element based on given template element for the given language variant.
	 */
	public String getElementValue(TemplateElement templateElement, LanguageVariant languageVariant) throws RQLException {
		return getContentElement(templateElement, languageVariant).getValueAsString();
	}

	/**
	 * Returns the content element based on the given template element and language variant.
	 */
	public ContentElement getContentElement(TemplateElement templateElement, LanguageVariant languageVariant) {
		return (ContentElement) getElementMap(templateElement).get(languageVariant);
	}

	/**
	 * Returns true only, if the given text element has a different value in compareWithLv as in mainLv.<p>
	 * Return false, if both language variants are the same or not a text element or text element, but not language variant dependent. 
	 * @throws RQLException 
	 */
	public boolean isTextValueDifferentFromMainLanguage(TemplateElement templateElement, LanguageVariant mainLv, LanguageVariant compareWithLv) throws RQLException {
		// for main lv itself always false (means no warning message) 
		if (mainLv.equals(compareWithLv)) {
			return false;
		}
		// check text element type and lv dependent and values itself
		if (templateElement.isText() && templateElement.isLanguageVariantDependent()) {
			String mainLvValue = getElementValue(templateElement, mainLv);
			String compareWithValue = getElementValue(templateElement, compareWithLv);
			if (!mainLvValue.equals(compareWithValue)) {
				return true; 
			}
		}
		// default
		return false;
	}

	/**
	 * Returns all template elements of this matrix.
	 */
	public List<TemplateElement> getTemplateElements() {
		return elementMatrix.keyList();
	}
	/**
	 * Returns all properties of this matrix.
	 */
	public List<String> getProperties() {
		return propertyMatrix.keyList();
	}
}
