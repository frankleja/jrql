package com.hlcl.rql.hip.as;

import java.util.*;

import com.hlcl.rql.as.*;
import com.hlcl.rql.util.as.*;
import com.hlcl.rql.util.as.Comparators;
import com.hlcl.rql.util.as.ScriptParameters;

/**
 * @author lejafr
 *
 * This class replaces some standard methods in order to add possible content classes even if they are not preassigned.
 * In the HIP project there are some content classes, like manual_toc_page which are created only via script and not preassigned.
 * 
 * For example, moving a manual toc page to a leaf list page should be possible, even the manual toc page is not preassigned.   
 */
public class TemplatePreassignmentsHelper {

	private ScriptParameters preassignmentMappings;
	private Project project;

	/**
	 * Construct the helper instance for the given page.
	 */
	public TemplatePreassignmentsHelper(Page assignmentMappingPage) {
		super();

		preassignmentMappings = new ScriptParameters(assignmentMappingPage);
		project = assignmentMappingPage.getProject();
	}

	/**
	 * Sammelt in ein Set alle vorbelegten Templates aller MultiLink Templateelemente dieses Templates,
	 * ergänzt um weitere, die nicht per RQL erhältlich sind.
	 *  
	 * @param	includeReferences	=true, auch Elemente, die Referenzquelle sind werden geliefert (haben keine Childs!)
	 * 								=false, ohne Element, die Referenzquelle sind (nur diese haben Childs!)
	 * @return	java.util.Set	of Templates
	 */
	public java.util.Set<Template> collectPreassignedTemplatesOfAllMultiLinkElements(Template template, boolean includeReferences) throws RQLException {

		java.util.Set<Template> result = new HashSet<Template>();

		// for all multi link template elements
		java.util.List multiLinks = template.getMultiLinkTemplateElements(includeReferences);
		for (int i = 0; i < multiLinks.size(); i++) {
			TemplateElement linkElem = (TemplateElement) multiLinks.get(i);
			// add some not preassigned too
			result.addAll(getPreassignedTemplates(linkElem));
		}
		return result;
	}

	/**
	 * Liefert alle vorbelegten Templates ergänzt um weitere, die nicht per RQL erhältlich sind.
	 */
	public java.util.List<Template> getAllowedTemplates(MultiLink link) throws RQLException {
		return getPreassignedTemplates(link.getTemplateElement());
	}

	/**
	 * Liefert eine Liste von Kindseiten, deren Template am gegebenen MultiLink nicht erlaubt ist. Berücksichtigt die additional preassignments.
	 */
	public PageArrayList getNotAllowedChildPages(MultiLink link) throws RQLException {

		PageArrayList childs = link.getChildPages();
		TemplatesPageFilter filter = new TemplatesPageFilter(getAllowedTemplates(link), true);
		return childs.select(filter);
	}

	/**
	 * Liefert alle vorbelegten Templates ergänzt um weitere, die nicht per RQL erhältlich sind.
	 */
	public java.util.List<Template> getPreassignedTemplates(TemplateElement templateElement) throws RQLException {

		java.util.List<Template> templates = templateElement.getPreassignedTemplates();

		// build key
		String key = templateElement.getTemplateFolder().getName();
		key += "," + templateElement.getTemplate().getName();
		key += "," + templateElement.getName();

		// get template guids
		String value = preassignmentMappings.get(key);
		if (value == null) {
			return templates;
		}

		// wrap all guids into templates
		String[] templateGuids = value.split(",");
		for (int i = 0; i < templateGuids.length; i++) {
			String templateGuid = templateGuids[i];
			templates.add(project.getTemplateByGuid(templateGuid));
		}
		
		// sort by name
		SortedSet<Template> sorted = new TreeSet<Template>(Comparators.getTemplateNameComparator());
		sorted.addAll(templates);
		return new ArrayList<Template>(sorted);
	}

	/**
	 * Liefert true, falls es mindestens einen Link in targetPage gibt an den 
	 * eine Kindseite diese Links verschoben werden kann, sonst false.
	 * Es werden die TemplateVorbelegungen dieses Links und der targetPage ausgewertet.
	 * 
	 * @param	includeReferences	=true, auch TemplateElemente, die Referenzquelle sind werden geliefert (haben keine Childs!)
	 * 								=false, ohne TemplateElemente, die Referenzquelle sind (nur diese haben Childs!)
	 */
	public boolean isAtLeastOneChildMoveableToTarget(MultiLink link, Page targetPage, boolean includeReferences) throws RQLException {

		java.util.Set targetTemplates = collectPreassignedTemplatesOfAllMultiLinkElements(targetPage.getTemplate(), includeReferences);
		java.util.List ownTemplates = getPreassignedTemplates(link.getTemplateElement());

		// try to find at least one match
		for (int i = 0; i < ownTemplates.size(); i++) {
			Template ownTmplt = (Template) ownTemplates.get(i);
			// one found; end
			if (targetTemplates.contains(ownTmplt)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Liefert true, falls die gegebene Seite an den gegebenen MultiLink verknüpft werden darf, sonst false. <p>
	 * Berücksichtigt neben den Templatevorbelegungen auch zusätzliche, z.B. manual_toc_page, die in RD nicht zugelassen, aber erlaubt sind.
	 */
	public boolean isConnectToExistingPageAllowed(MultiLink multiLink, Page connectToPage) throws RQLException {

		java.util.List<Template> templates = multiLink.getAllowedTemplates();

		// check rd preassignments first
		Template connectToTemplate = connectToPage.getTemplate();
		if (templates.contains(connectToTemplate)) {
			return true;
		}
		// build key
		TemplateElement templateElement = multiLink.getTemplateElement();
		String key = templateElement.getTemplateFolder().getName();
		key += "," + templateElement.getTemplate().getName();
		key += "," + templateElement.getName();

		// get additional allowed template guids
		String value = preassignmentMappings.get(key);
		if (value == null) {
			// nothing more allowed
			return false;
		}
		return StringHelper.split(value, ',').contains(connectToTemplate.getTemplateGuid());
	}
	/**
	 * Liefert eine Teilmenge der gegebenen Liste zurück, an die diese Seite gelinkt werden darf. 
	 * D.h. das Template dieser Seite ist allen zurückgegebenen Links vorbelegt.
	 * Es werden auch zugelassene Templates an Elementen berücksichtigt, die nicht über RQL zurückgegeben werden.
	 *
	 * @param	assumedTargetLinks	Liste of MultiLinks
	 * 
	 * @return	java.util.List	Liste of MultiLinks, Teilmenge von assumedTargetLinks oder leere Liste
	 */
	public java.util.List<MultiLink> selectConnectToLinks(Page child, java.util.List assumedTargetLinks) throws RQLException {

		java.util.List<MultiLink> result = new ArrayList<MultiLink>();
		Template template = child.getTemplate();

		// for all given links check
		for (int i = 0; i < assumedTargetLinks.size(); i++) {
			MultiLink link = (MultiLink) assumedTargetLinks.get(i);
			java.util.List allowedTemplates = getPreassignedTemplates(link.getTemplateElement());
			if (allowedTemplates.contains(template)) {
				result.add(link);
			}
		}
		return result;
	}

}
