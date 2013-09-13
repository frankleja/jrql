package com.hlag.jrql.examples;

/**
 * 
 */

import java.util.ArrayList;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.LanguageVariant;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.RecycleBin;

/**
 * @author lejafr TODO needs post
 */
public class EditingPagesDeletePageIntoAndFromRecycleBin {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		// delete a page
		Page currentPg = project.getPageById("4711");
		currentPg.delete();
		// now currentPg cannot be used anymore

		// delete ignoring references
		currentPg = project.getPageById("4712");
		currentPg.delete(true);
		
		// remove page from recycle bin
		currentPg.deleteFromRecycleBin();
		currentPg.deleteFromRecycleBinInCurrentLanguageVariant();
		
		// remove page from recycle bin for given language variants
		java.util.List<LanguageVariant> languageVariants = new ArrayList<LanguageVariant>();
		languageVariants.add(project.getLanguageVariantByRfcLanguageId("en"));
		languageVariants.add(project.getLanguageVariantByRfcLanguageId("zh"));
		currentPg.deleteFromRecycleBin(languageVariants);
		
		// restore page from recycle bin
		RecycleBin recycleBin = project.getRecycleBin();
		recycleBin.restorePageById("4711");
	}
}
