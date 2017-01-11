package com.hlcl.rql.util.as;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;

/**
 * This class tests the target performance comparison class.
 */
public class TargetPerformanceComparisonTest {

	/**
	 * test scenario
	 */
	public static void main(String[] args) {
		try {
			long start = System.currentTimeMillis();

			String logonGuid="D0EF5D1C8FCB41CDAE1E6CF03C971A5D";
			String sessionKey="8A1A58AD2E694CE4A6E6615345F34244";
			String projectGuid="06BE79A1D9F549388F06F6B649E27152";

			CmsClient client = new CmsClient(logonGuid);
			Project project = client.getProject(sessionKey, projectGuid);

			Page currentPg = project.getPageById("908278");
			PageArrayList currentPages = currentPg.getListChildPages("content_pages_list");
			System.out.println("current= " + currentPages.collectHeadlines());
			
			// targets
			String[] targets = { "Sales Steering", "Allocation Management" };
			
			// results
			TargetPerformanceComparison comparison = new TargetPerformanceComparison(currentPages, targets, "getStandardFieldTextValue", "invisible_info_sft");

			System.out.println("1 remove " + comparison.get1PagesWhichHaveToBeRemoved());

			System.out.println("2 update " + comparison.get2PagesWhichHasToBeUpdated().collectHeadlines());

			System.out.println("3 add " + comparison.get3PageAttributesWhichHasToBeAdded());
			
			System.out.println("4 sort accordingly to " + comparison.get4PagesSortOrder());

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
