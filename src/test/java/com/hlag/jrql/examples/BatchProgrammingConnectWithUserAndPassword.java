package com.hlag.jrql.examples;

/**
 * 
 */

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.PasswordAuthentication;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StringHelper;
import org.apache.log4j.Logger;

/**
 * @author lejafr
 * 
 */
public class BatchProgrammingConnectWithUserAndPassword {

	/**
	 * @param args
	 * @throws RQLException 
	 */
	public static void main(String[] args) throws RQLException {

//		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
//		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
//		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";
//
//		CmsClient client = new CmsClient(logonGuid);
//		Project project1 = client.getProjectByGuid("project guid");
//		Project project2 = client.getProjectByName("project name");

		CmsClient client = null;
		try {
			String user = args[0];
			String pw = args[1];
			String[] projectNames = StringHelper.split(args[2], ",");

			client = new CmsClient(new PasswordAuthentication(user, pw));

			for (int i = 0; i < projectNames.length; i++) {
				String projectName = projectNames[i];
                                System.out. println("# projectName" +projectName);
                                Logger.getLogger("# projectName" +projectName);
				Project project = client.getProjectByName(projectName);
                                    //project.

				// do what you want
			}
		} catch (RQLException ex) {
			String error = "";
			Throwable re = ex.getReason();
			if (re != null) {
				error += re.getMessage();
			}
		} finally {
			client.disconnect();
		}
	}
}
