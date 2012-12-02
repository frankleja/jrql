package com.hlag.jrql.examples;

/**
 * 
 */

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.PublicationPackage;
import com.hlcl.rql.as.PublicationSetting;
import com.hlcl.rql.as.PublishingTarget;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 */
public class EditingPublicationPackagesDefinePublishingTargets {

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws RQLException, FileNotFoundException, IOException {

		String logonGuid = "43321CA565FC4D91ACFD3F3E4AB2D92B";
		String sessionKey = "840BC272ED864906BB44E86F60A1B81B";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		// input values
		PublishingTarget[] targets = new PublishingTarget[4];
		targets[0] = project.getPublishingTargetByNameStartsWith("WORK_");
		targets[1] = project.getPublishingTargetByNameStartsWith("DEVE_");
		targets[2] = project.getPublishingTargetByNameStartsWith("TEST_");
		targets[3] = project.getPublishingTargetByNameStartsWith("PROD_");

		PublicationPackage publPackage = project.getPublicationPackageByName("publ_pages_to_about_us");
		for (PublicationSetting combination : publPackage.getPublicationSettings()) {
			String pvStage = combination.getProjectVariantName().substring(0, 4);

			// add all targets until given stage
			for (int i = 0; i < targets.length; i++) {
				PublishingTarget target = targets[i];
				combination.addPublishingTo(target);

				// add ends at stage
				if (target.getName().substring(0, 4).equals(pvStage)) {
					break;
				}
			}
		}
	}
}
