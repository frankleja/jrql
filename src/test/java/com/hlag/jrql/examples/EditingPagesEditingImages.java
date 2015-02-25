package com.hlag.jrql.examples;

/**
 * 
 */

import com.hlcl.rql.as.AssetManagerFolder;
import com.hlcl.rql.as.AssetManagerSubFolder;
import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.ImageElement;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 */
public class EditingPagesEditingImages {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		Page currentPg = project.getPageById("4711");
		boolean empty = currentPg.isImageEmpty("templateElementName");
		boolean entered = currentPg.isImageValueEntered("templateElementName");

		String filename = currentPg.getImageValue("templateElementName");
		currentPg.setImageValue("templateElementName", "filename");
		currentPg.deleteImageValue("templateElementName");

		Page sourcePg = project.getPageById("4712");
		currentPg.copyImageValueFrom("templateElementName", sourcePg);

		Page targetPg = project.getPageById("4713");
		ImageElement targetElement = targetPg.getImageElement("templateElementName");
		currentPg.copyImageValueTo("sourceTemplateElementName", targetElement);
		currentPg.copyImageValueTo("templateElementName", targetPg);

		boolean isRefSource = currentPg.isImageElementReferenceSource("templateElementName");
		currentPg.referenceImageElementToImageElement("sourceImageTemplateElementName", "targetImageTemplateElementName");
		currentPg.deleteImageElementReference("templateElementName");

		currentPg.downloadImage("templateElementName", "d:\\temp\\image.png", false);
		
		// TODO needs post
		AssetManagerFolder assetManager = project.getAssetManagerByName("assetManagerFolderName");
		AssetManagerSubFolder assetManagerSubFolderByName = project.getAssetManagerSubFolderByName("view_pdf/orga_charts");
		
	}
}
