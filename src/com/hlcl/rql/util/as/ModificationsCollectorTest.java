package com.hlcl.rql.util.as;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;

/**
 * This class tests the target performance comparison class.
 */
public class ModificationsCollectorTest {

	/**
	 * test scenario
	 */
	public static void main(String[] args) {
		try {
			long start = System.currentTimeMillis();

			String logonGuid="04850B4128E749BA91C99E4C0F588A54";
			String sessionKey="55E38C3C099543F09A40EF75FF874F2A";
			String projectGuid="06BE79A1D9F549388F06F6B649E27152";

			CmsClient client = new CmsClient(logonGuid);
			Project project = client.getProject(sessionKey, projectGuid);

			Page currentPg = project.getPageById("127290");
			PageArrayList pages = currentPg.getListChildPages("admin_list");

			ModificationsCollector mods = new ModificationsCollector();
			mods.created(pages.getPage(0));
			mods.updated(pages.getPage(1));
			mods.created(pages.getPage(3));
			mods.deactivated(pages.getPage(4));
			mods.reactivated(pages.getPage(4));
			mods.deleted("deleted page info");
			mods.deleted("aaa deleted page info 2");
			
			System.out.println(mods.anyModifications());
			System.out.println(mods.buildModificationsText());
			
			System.out.println("=============================");
			System.out.println(mods.buildModificationsText(true, true, false, true, true));
			
			// display duration
			long end = System.currentTimeMillis();
			System.out.println("Duration=" + (end - start));

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
