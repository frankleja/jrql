package com.hlcl.rql.hip.as;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.PasswordAuthentication;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.ProjectVariant;
import com.hlcl.rql.as.PublishingJob;
import com.hlcl.rql.as.RQLException;

/**
 * Test of class PublishPagePolicies.
 * 
 * @author: LEJAFR
 */
public class PublishPagePoliciesTest {
	/**
	 * Test constructor comment.
	 */
	public PublishPagePoliciesTest() {
		super();
	}

	/**
	 * Starts the application.
	 * 
	 * @param args
	 *            an array of command-line arguments
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws InterruptedException
	 */
	public static void main(java.lang.String[] args) throws RQLException, IOException, InterruptedException {

		CmsClient client = null;
		try {
			long start = System.currentTimeMillis();

//			String logonGuid="BCABEC6E236941C09CD79ADBAB953ADD";
//			String sessionKey="451A8C3F3C46431D9ADF9975E48FE450";
//			String projectGuid="06BE79A1D9F549388F06F6B649E27152";
//
//			CmsClient client = new CmsClient(logonGuid);
//			Project project = client.getProject(sessionKey, projectGuid);
			
			client = new CmsClient(new PasswordAuthentication("testuser_compass", "testuser368"));
			Project project = client.getProjectByName("hip.hlcl.com");
			System.out.println(client.getConnectedUser().getName());

			// check different pages
			String[] pageIds = {"127290", "624522", "656879", "7375"};
			for (int i = 0; i < pageIds.length; i++) {
				Page startPg = project.getPageById(pageIds[i]);
				System.out.println(startPg.getInfoText());
				
				// build helper class
				PublishPagePolicies policies = new PublishPagePolicies(startPg);
//				System.out.println(policies.getPageProjectVariantNameSuffixes());

				java.util.List<String> stages = policies.getPagePossibleStagesForUserAndPage();
				System.out.println("  " + stages);

				// check job creation
				PublishingJob job = policies.createPagePublishingJob(stages.get(0), false, false);
				System.out.println("  # mails = " + job.getNumberOfPublishingMails());

				// list project variants
				Set<ProjectVariant> pvs = job.getProjectVariants();
				for (Iterator iterator = pvs.iterator(); iterator.hasNext();) {
					ProjectVariant projectVariant = (ProjectVariant) iterator.next();
					System.out.println("  " + projectVariant.getName());
				}
			}

			
//			client = new CmsClient(new PasswordAuthentication("test_area-news-other", "test2008"));
//			Project project = client.getProjectByName("hlag_wm2008");
//			System.out.println(client.getConnectedUser().getName());

//			// check different pages
//			String[] pageIds = {"10611", "11021", "2685", "51", "3478", "4111" };
//			for (int i = 0; i < pageIds.length; i++) {
//				Page startPg = project.getPageById(pageIds[i]);
//				System.out.println(startPg.getInfoText());
//
//				// build helper class
//				PublishPagePolicies helper = new PublishPagePolicies(startPg);
//				java.util.List<String> stages = helper.getPagePossibleStagesForUserAndPage();
//				System.out.println("  " + stages);
//
//				// mail distribution test
//				if (helper.hasMailDistribution()) {
//					System.out.println("  has mail distribution");
//					java.util.List<String> mailStages = helper.getMailPossibleStagesForUserAndPage();
//					System.out.println("    mail " + mailStages);
//					for (Iterator iterator = mailStages.iterator(); iterator.hasNext();) {
//						String stage = (String) iterator.next();
//						System.out.println("    mail stage " + stage + " " + helper.convertMailStageToServerName(stage));
//					}
//					// out project- and language variants of job
//					PublishingJob mailJob = helper.createMailPublishingJob(mailStages.get(0));
//					Set<ProjectVariant> pvs = mailJob.getProjectVariants();
//					for (Iterator iterator = pvs.iterator(); iterator.hasNext();) {
//						ProjectVariant projectVariant = (ProjectVariant) iterator.next();
//						System.out.println("    mail job " + projectVariant.getName());
//					}
//				}
//
//				// check job creation
//				PublishingJob job = helper.createPagePublishingJob(stages.get(0), false, false);
//				System.out.println("  # mails = " + job.getNumberOfPublishingMails());
//				// job.setMailReceiver(client.getConnectedUser());
//				// job.start();
//
//				// list project variants and languages
//				Set<ProjectVariant> pvs = job.getProjectVariants();
//				for (Iterator iterator = pvs.iterator(); iterator.hasNext();) {
//					ProjectVariant projectVariant = (ProjectVariant) iterator.next();
//					System.out.println("  " + projectVariant.getName());
//				}
//				Set<String> guids = job.getLanguageVariantGuids();
//				for (Iterator iterator = guids.iterator(); iterator.hasNext();) {
//					String lvGuid = (String) iterator.next();
//					System.out.println("  " + lvGuid);
//				}
//				
//			}
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
		} finally {
			client.disconnect();
		}
	}
}
