package com.hlcl.rql.util.as;

import com.hlcl.rql.as.*;

/**
 * @author lejafr
 *
 * This class publishes all not navigation pages below a node creating a publishing job for each.
 * Not navigation pages are every page which template is outside of navigationTemplateFolder. 
 */
public class TreePartPublisher {

	private TemplateFolder navigationTemplateFolder;
	private boolean withFollowingPages;
	private String projectVariantGuids;
	private String separator; // for project variant guid list
	private String languageVariantGuid;
	// can be null
	private User mailReceiver;

	/**
	 * Construct a publisher with all publishing settings.
	 * Mail to mailReceiver triggert.
	 */
	public TreePartPublisher(
		TemplateFolder navigationTemplateFolder,
		boolean withFollowingPages,
		String projectVariantGuids,
		String separator,
		String languageVariantGuid,
		User mailReceiver) {
		super();
		this.navigationTemplateFolder = navigationTemplateFolder;
		this.withFollowingPages = withFollowingPages;
		this.projectVariantGuids = projectVariantGuids;
		this.separator = separator;
		this.languageVariantGuid = languageVariantGuid;
		this.mailReceiver = mailReceiver;
	}
	/**
	 * Construct a publisher with all publishing settings.
	 * No mail from publishing job.
	 */
	public TreePartPublisher(TemplateFolder navigationTemplateFolder, boolean withFollowingPages, String projectVariantGuids, String separator, String languageVariantGuid) {
		this(navigationTemplateFolder, withFollowingPages, projectVariantGuids, separator, languageVariantGuid, null);
	}
	/**
	 * Startet das Suchen und Erstellen der PublishingJobs ab der gegebenen Navigationsseite.
	 */
	public int startFromPage(Page startNavigationPage) throws RQLException {

		int numberOfJobsCreated = 0;
		return doRecursive(startNavigationPage, numberOfJobsCreated);
	}
	/**
	 * Erzeugt einen publishing job, parametrisiert ihn und startet ihn.
	 */
	private void startJob(Page page) throws RQLException {

		PublishingJob publishingJob = new PublishingJob(page, withFollowingPages);
		publishingJob.addToPublish(projectVariantGuids, separator, languageVariantGuid);
		// set mail receiver
		if (mailReceiver != null) {
			publishingJob.setMailReceiver(mailReceiver);
		}
		publishingJob.start();
	}
	/**
	 * Startet das Suchen und Erstellen der PublishingJobs ab der gegebenen Navigationsseite.
	 */
	private int doRecursive(Page page, int numberOfJobsCreated) throws RQLException {

		if (navigationTemplateFolder.contains(page.getTemplate())) {
			// go deeper for all children of given navigation page
			PageArrayList childs = page.getChildPages();
			for (int i = 0; i < childs.size(); i++) {
				Page child = (Page) childs.get(i);
				numberOfJobsCreated = doRecursive(child, numberOfJobsCreated);
			}
		} else {
			// publish given page, because it is not a navigation page
			try {
				startJob(page);
				numberOfJobsCreated += 1;
			} catch (PageAlreadyInPublishingQueueException ex) {
				// ignore this exception
			}
		}
		return numberOfJobsCreated;
	}
}
