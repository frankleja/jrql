package com.hlcl.rql.hip.as;

import com.hlcl.rql.as.*;
import com.hlcl.rql.util.as.PageArrayList;

/**
 * @author lejafr
 *
 * This class extend the CMS standard function 'inherit workflow' so, that some multilinks will not get a workflow.
 */
public class InheritWorkflow {
	// suffix of template element names which should not get a workflow
	private String shadowElementsNameSuffix;
	private Workflow workflow; 
	 
	/**
	 * Construct a inherit workflow object.
	 * 
	 * @param	shadowElementsNameSuffix	all links which have a shadow element will not get a workflow assigned
	 * @see Template#getShadowedTemplateElements(String)
	 */
	public InheritWorkflow(Workflow workflow, String shadowElementsNameSuffix) {
		super();
		this.workflow = workflow;
		this.shadowElementsNameSuffix = shadowElementsNameSuffix;
	}
	/**
	 * Startet das Vererben bei dem Container mit Namen containerTemplateElementName, das in der gegebenen Seite startPage existieren muss.
	 */
	public void inheritFromMultiLink(MultiLink link) throws RQLException {
		
		// for every child do
		PageArrayList childs = link.getChildPages();
		for (int i = 0; i < childs.size(); i++) {
			Page child = (Page) childs.get(i);
			inheritRecursive(child);
		}
	}
	/**
	 * Vererbt den Workflow ausgehend vom gegebenen Link an die MultiLinks aller Kindseiten, die einen benötigen.
	 * Die MultiLinks (z.b. list_of_sections), zu denen es ein Schattenelement (list_of_sections_workflow_unlinked_flag) gibt,
	 * erhalten keinen Workflow angehängt, da er dort nicht erwünscht ist.  
	 */
	private void inheritRecursive(Page page) throws RQLException {
			
		// assign workflow to appropriate links on child
		java.util.List links = page.getMultiLinksWithoutShadowedOnes(shadowElementsNameSuffix, false);
		for (int j = 0; j < links.size(); j++) {
			MultiLink link = (MultiLink) links.get(j);
			link.assignWorkflow(workflow, false);
		}
		
		// try for every link of child
		PageArrayList childs = page.getChildPages();
		for (int i = 0; i < childs.size(); i++) {
			Page child = (Page) childs.get(i);
			inheritRecursive(child);
		}
	}
}
