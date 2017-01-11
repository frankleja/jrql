package com.hlcl.rql.as;

import java.util.ArrayList;

/**
 * Structure Elements: Allowed Content Classes.
 */
public class TemplateGroups {

	
	protected Project project;
	
	
	protected final java.util.List<Template> allTemplates = new ArrayList<Template>();
	protected final java.util.List<TemplateGroup> allGroups = new ArrayList<TemplateGroup>();

	
	/** GUIDs der Templates, bei denen selectinnewpage gesetzt ist. */
	protected final java.util.List<String> templateGuidsToSelectInNewPage = new ArrayList<String>();
	boolean hasExtendedRestrictions;
	
	
	/**
	 * Construct from a <TEMPLATEGROUPS ...> response.
	 */
	public TemplateGroups(Project project, RQLNode templateGroups) throws RQLException {
		this.project = project;
		this.hasExtendedRestrictions = "1".equals(templateGroups.getAttribute("extendedrestriction"));
		
		for (RQLNode groupNode : templateGroups.getNodes("TEMPLATEGROUP")) {
			TemplateGroup group = new TemplateGroup(groupNode.getAttribute("guid"), groupNode.getAttribute("name"));
			allGroups.add(group);

			for (RQLNode templateNode : groupNode.getNodes("TEMPLATE")) {
				Template template = new Template(project.getTemplateFolderByGuid(templateNode.getAttribute("folderguid")),
												 templateNode.getAttribute("name"),
												 templateNode.getAttribute("guid"),
												 templateNode.getAttribute("description"));
				template.setSelectInNewPage("1".equals(templateNode.getAttribute("selectinnewpage")));
				
				allTemplates.add(template);
				group.templates.add(template);

				if (template.getSelectInNewPage()) {
					templateGuidsToSelectInNewPage.add(template.getTemplateGuid());
				}
			}
		}
	}

	
	public boolean hasExtendedRestrictions() {
		return hasExtendedRestrictions;
	}
	
	
	/**
	 * Direkter Zugriff auf die Liste der GUIDs, die erlaubt sein sollen.
	 * @return direkte referenz auf eine manipulierbare Liste.
	 */
	public java.util.List<String> getTemplateGuidsToSelectInNewPage() {
		return templateGuidsToSelectInNewPage;
	}
	
}
