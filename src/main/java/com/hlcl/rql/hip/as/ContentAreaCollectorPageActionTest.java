/**
 * 
 */
package com.hlcl.rql.hip.as;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.PageArrayList;

/**
 * Test of this page action.
 */
public class ContentAreaCollectorPageActionTest {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		try {
			long start = System.currentTimeMillis();

			String logonGuid="69322821E01E49A9A50CF20E382C6CA2";
			String sessionKey="FB46C0BE11BE48CD8645D17E381851D3";
			String projectGuid="06BE79A1D9F549388F06F6B649E27152";

			CmsClient client = new CmsClient(logonGuid);
			Project project = client.getProject(sessionKey, projectGuid);

			Page startPg = project.getPageById("27007");

			// prepare action
			Class.forName("org.hsqldb.jdbcDriver");
			Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:cacpat", "sa", "");
			String[] skipSuffixes = {"_fragment"};
			ContentAreaCollectorPageAction action = new ContentAreaCollectorPageAction(connection, "cacpat", skipSuffixes, "company");

			// 1. add all
			PageArrayList children = startPg.getListChildPages("content_pages_list");
			for (Iterator iterator = children.iterator(); iterator.hasNext();) {
				Page child = (Page) iterator.next();
				action.invoke(child);
			}
			
			// 2. add some with different content area
			action.setContentAreaName("business_administration");
			// already existing pages
			action.invoke(project.getPageById("161"));
			action.invoke(project.getPageById("713"));
			// new page
			action.invoke(project.getPageById("27001"));

			// 3. retrieve save values
			while(action.nextPage()) {
				System.out.printf("%s\t%s\t%s\t%s\n", action.getCurrentPageGuid(), action.getCurrentPageId(), action.getCurrentPageHeadline(), action.getCurrentPageContentAreas());
			}
			
			// end
			connection.close();
			
			// display duration
			long end = System.currentTimeMillis();
			System.out.println("Duration=" + (end - start));

		} catch (SQLException ex) {
			ex.printStackTrace();
			System.out.print(ex.getMessage());
		} catch (RQLException ex) {
			ex.printStackTrace();
			System.out.print(ex.getMessage());

			Throwable re = ex.getReason();
			if (re != null) {
				re.printStackTrace();
				System.out.print(re.getMessage());
			}
		}
	}

}
