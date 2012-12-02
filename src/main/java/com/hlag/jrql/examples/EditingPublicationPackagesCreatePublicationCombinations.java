package com.hlag.jrql.examples;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.LanguageVariant;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.ProjectVariant;
import com.hlcl.rql.as.PublicationPackage;
import com.hlcl.rql.as.PublicationSetting;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StringHelper;

/**
 * @author lejafr
 * 
 */
public class EditingPublicationPackagesCreatePublicationCombinations {

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws RQLException, FileNotFoundException, IOException {

		String logonGuid = "8009AB61D06844F48077A9E4D4880A20";
		String sessionKey = "C63373D9DB394B94BBAD66106540A123";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		// 1. for these packages do
		String[] packages = { "publ_pages_to_languages_root", "publ_pages_to_newsletter" };
		for (int i = 0; i < packages.length; i++) {
			String name = packages[i];
			PublicationPackage publPackage = project.getPublicationPackageByName(name);

			for (PublicationSetting combination : publPackage.getPublicationSettings()) {
				// 2. skip all page config
				if (combination.getProjectVariantName().contains("page_config_xml")) {
					continue;
				}
				System.out.println("  " + combination.getName());

				// 3. get setting’s values
				LanguageVariant lv = combination.getLanguageVariant();
				ProjectVariant pv = combination.getProjectVariant();

				// 4. determine new project variant
				String stage = StringHelper.split(pv.getName(), "_")[0];
				ProjectVariant newPv = project.getProjectVariantByName(stage + "_viewlabels_and_messages_xml");

				// 5. create new setting
				PublicationSetting newCombination = publPackage.addSetting(newPv, lv, pv);
				// 6. further action with newCombination
			} // end for combinations
		} // end for packages
	}
}
