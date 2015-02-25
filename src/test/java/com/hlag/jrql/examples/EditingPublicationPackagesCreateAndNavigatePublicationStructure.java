package com.hlag.jrql.examples;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.PublicationFolder;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 */
public class EditingPublicationPackagesCreateAndNavigatePublicationStructure {

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

		PublicationFolder rootFolder1 = project.getPublicationRootFolderByGuid("publicationFolderGuid");
		PublicationFolder rootFolder2 = project.getPublicationRootFolderByName("publicationFolderName");

		rootFolder1.createChildFolder("child folder name");
		rootFolder1.getChildByName("child name");
	}
}
