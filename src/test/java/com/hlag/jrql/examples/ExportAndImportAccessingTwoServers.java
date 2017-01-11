package com.hlag.jrql.examples;

/**
 * 
 */

import java.net.MalformedURLException;
import java.net.URL;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.PasswordAuthentication;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 */
public class ExportAndImportAccessingTwoServers {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws RQLException, MalformedURLException {

		// a) with existing session data
		String logonGuidOld="36AE9002974742C48B5F38AD889A51A2";
		String sessionKeyOld="72EF63F6333B4C01AD4B568974B95424";
		String projectGuidOld="06BE79A1D9F549388F06F6B649E27152";

		CmsClient clientOld = new CmsClient(logonGuidOld, new URL("http://reddot.hlcl.com/cms/hlclRemoteRql.asp"));
		Project projectOld = clientOld.getProject(sessionKeyOld, projectGuidOld);

		String logonGuid="7DDFD5482B57475A9004584FC6C1F207";
		String sessionKey="1A7CF419554244B9B737DFC3D23D13A5";
		String projectGuid="73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid, new URL("http://kswfrd02/cms/hlclRemoteRql.asp"));
		Project project = client.getProject(sessionKey, projectGuid);

		// by old page id
		Page sourcePg = projectOld.getPageById("127290");
		System.out.println(sourcePg.getInfoText());
		
		// by new page id
		Page targetPg = project.getPageById("2");
		System.out.println(targetPg.getInfoText());
		
		// b) with user name and password
		CmsClient client1 = new CmsClient(new PasswordAuthentication("lejafr", "pw"), new URL("http://reddot.hlcl.com/cms/hlclRemoteRql.asp"));
		Project project1 = client1.getProjectByName("hip.hlcl.com");
		
		CmsClient client2 = new CmsClient(new PasswordAuthentication("lejafradm", "pw"), new URL("http://kswfrd02/cms/hlclRemoteRql.asp"));
		Project project2 = client2.getProjectByName("hlag_wm2008");
		
		Page source1Pg = project1.getPageById("127290");
		System.out.println(source1Pg.getInfoText());
		
		// by new page id
		Page target2Pg = project2.getPageById("2");
		System.out.println(target2Pg.getInfoText());
		
		client1.disconnect();
		client2.disconnect();
	}
}
