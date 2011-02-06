package com.hlag.jrql.examples;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.PublicationFolder;
import com.hlcl.rql.as.PublicationPackage;
import com.hlcl.rql.as.PublicationSetting;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 */
public class EditingPublicationPackagesSetPublishedPages {

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

		// input value
		String subFolderName = "about_us";

		PublicationPackage pubPackage = project.getPublicationPackageByName("publ_pages_to_" + subFolderName);
		java.util.List<PublicationSetting> settings = pubPackage.getPublicationSettings();

		for (PublicationSetting setting : settings) {
			System.out.println(setting.getName());
			String settingName = setting.getName();

			// skip display_do_not_use* project variant
			if (settingName.indexOf("do_not_use") > 0) {
				continue;
			}

			// 1. determine root folder by pv
			PublicationFolder targetFolder = null;
			if (settingName.indexOf("pages_html") > 0 || settingName.indexOf("viewlabels") > 0) {
				targetFolder = project.getPublicationRootFolderByName("docRoot");
			}
			if (settingName.indexOf("page_config_xml") > 0) {
				targetFolder = project.getPublicationRootFolderByName("pageConfig");
			}

			// 2. crawl to folder by lv
			System.out.println("  " + setting.getLanguageVariant().getRfcLanguageId());
			targetFolder = targetFolder.getChildByName(setting.getLanguageVariant().getRfcLanguageId());

			// 3. crawl to given folder by name
			targetFolder = targetFolder.getChildByName(subFolderName);

			// set folder
			setting.setPublishedPages(targetFolder);
		}
	}
}
