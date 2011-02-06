package com.hlag.jrql.examples;

/**
 * 
 */

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.LanguageVariant;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.ProjectVariant;
import com.hlcl.rql.as.PublishingJob;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 */
public class PublishingJobsGettingProjectAndLanguageVariants {

	/**
	 * @param args
	 * @throws RQLException
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		ProjectVariant pv1 = project.getProjectVariantByGuid("project variant guid");
		ProjectVariant pv2 = project.getProjectVariantByName("project variant name");

		LanguageVariant lv1 = project.getLanguageVariantByGuid("language variant guid");
		LanguageVariant lv2 = project.getLanguageVariantByName("language variant name");
		LanguageVariant lv3 = project.getLanguageVariantByLanguage("3 letter language code");
		LanguageVariant lv4 = project.getLanguageVariantByRfcLanguageId("2 letter language code");
	}
}
