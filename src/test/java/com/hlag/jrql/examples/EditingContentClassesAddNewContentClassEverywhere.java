package com.hlag.jrql.examples;

/**
 * 
 */

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.Template;
import com.hlcl.rql.as.TemplateElement;
import com.hlcl.rql.as.TemplateFolder;

/**
 * @author lejafr
 * 
 */
public class EditingContentClassesAddNewContentClassEverywhere {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0904ABF0E43443D2881FE7481339650E";
		String sessionKey = "490EC675042F4C5A8A6DF1ED63ADD7A6";
		String projectGuid = "268F46EF5EB74A75824856D3DA1C6597";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		Template newTemplate = project.getTemplateByName("content_templates", "text_block");

		for (TemplateFolder folder : project.getAllTemplateFolders()) {
			for (Template template : folder.getTemplates()) {
				// skip not page templates
				if (!template.getName().endsWith("_page")) {
					continue;
				}
				if (template.contains("blocks")) {
					TemplateElement blocksElem = template.getTemplateElementByName("blocks");
					if (blocksElem.isTemplatePreassigned(newTemplate)) {
						blocksElem.addPreassignedTemplate(newTemplate);
						System.out.println(folder.getName() + ";" + template.getName() + ";" + blocksElem.getName());
					}
				}
			}
		}
	}
}
