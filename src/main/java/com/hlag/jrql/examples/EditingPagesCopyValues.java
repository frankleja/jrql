package com.hlag.jrql.examples;

/**
 * 
 */

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.ImageElement;
import com.hlcl.rql.as.MediaElement;
import com.hlcl.rql.as.OptionList;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StandardFieldDateElement;

/**
 * @author lejafr TODO needs post
 */
public class EditingPagesCopyValues {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		Page sourcePage = project.getPageById("4711");
		Page targetPage = project.getPageById("4712");

		// copy all content elements (source and target should have same content class)
		// include headline and ignore referenced elements
		targetPage.copyContentElementValuesFrom(sourcePage, true, false);

		// copy image filename of sourcePage image element templateElementName to image element with same name of targetPage
		targetPage.copyImageValueFrom("templateElementName", sourcePage);
		// copy image filename of sourcePage image element named templateElementName to image element with same name of targetPage
		sourcePage.copyImageValueTo("templateElementName", targetPage);
		// copy image filename from sourcePage image element named sourceTemplateElementName to targetElement (an image element of
		// targetPage)
		ImageElement targetImageElement = targetPage.getImageElement("targetTemplateElementName");
		sourcePage.copyImageValueTo("sourceTemplateElementName", targetImageElement);

		// similar for other element types
		// media element
		targetPage.copyMediaValueFrom("templateElementName", sourcePage);
		sourcePage.copyMediaValueTo("templateElementName", targetPage);
		MediaElement targetMediaElement = targetPage.getMediaElement("templateElementName");
		sourcePage.copyMediaValueTo("sourceTemplateElementName", targetMediaElement);
		
		// option list
		targetPage.copyOptionListValueFrom("templateElementName", sourcePage);
		sourcePage.copyOptionListValueTo("templateElementName", targetPage);
		OptionList targetOptionList = targetPage.getOptionList("listTemplateElementName");
		sourcePage.copyOptionListValueTo("sourceTemplateElementName", targetOptionList);
		
		// standard field date
		targetPage.copyStandardFieldDateValueFrom("templateElementName", sourcePage);
		sourcePage.copyStandardFieldDateValueTo("templateElementName", targetPage);
		StandardFieldDateElement targetDateElement = targetPage.getStandardFieldDateElement("templateElementName");
		sourcePage.copyStandardFieldDateValueTo("sourceTemplateElementName", targetDateElement);
		
		// standard field numeric
		targetPage.copyStandardFieldNumericValueFrom("templateElementName", sourcePage);
		//...
		//...
		
		// standard field text
		targetPage.copyStandardFieldTextValueFrom("templateElementName", sourcePage);
		//...
		//...
		
		// text element 
		targetPage.copyTextValueFrom("templateElementName", sourcePage);
		//...
		//...

		// notes
		targetPage.copyNoteValueFrom("noteName", sourcePage);
		//...
		//...
		
	}
}
