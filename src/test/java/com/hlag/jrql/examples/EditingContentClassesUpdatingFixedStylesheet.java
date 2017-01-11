package com.hlag.jrql.examples;

/**
 * 
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

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
public class EditingContentClassesUpdatingFixedStylesheet {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws RQLException, FileNotFoundException, IOException {

		String logonGuid = "0904ABF0E43443D2881FE7481339650E";
		String sessionKey = "490EC675042F4C5A8A6DF1ED63ADD7A6";
		String projectGuid = "268F46EF5EB74A75824856D3DA1C6597";

		// get fixed text styles
		String fixedStyles = IOUtils.toString(new FileInputStream("d:\\texteditor_styles.txt"));

		// open project
		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		for (TemplateFolder folder : project.getAllTemplateFolders()) {
			for (Template template : folder.getTemplates()) {
				for (TemplateElement textElem : template.getTextTemplateElements()) {
					if (textElem.isHtmlText()) {
						textElem.setTextFixedStylesheet(fixedStyles);
						System.out.println(folder.getName() + ";" + template.getName() + ";" + textElem.getName());
					}
				}
			}
		}
	}
}
