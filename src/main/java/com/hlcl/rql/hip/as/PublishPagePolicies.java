package com.hlcl.rql.hip.as;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hlcl.rql.as.LanguageVariant;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.ProjectVariant;
import com.hlcl.rql.as.PublishingJob;
import com.hlcl.rql.as.PublishingTarget;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StringHelper;
import com.hlcl.rql.as.User;
import com.hlcl.rql.as.UserGroup;
import com.hlcl.rql.util.as.ScriptParameters;

/**
 * @author lejafr
 * 
 * This class encapsulate rules for the publishing of hip.hlcl.com pages. It acts as an helper class the script publishPage.jsp.
 */
public class PublishPagePolicies {
	private Page startPage;
	private final String parmPageId = "863580";
	private ScriptParameters parms;

	// constants
	private final String DEBUG_LEVEL_DEBUG = "DEBUG";
	private final String DEBUG_LEVEL_INFO = "INFO";

	// caches
	private java.util.List<PublishingTarget> targetsCache;
	private java.util.List<ProjectVariant> pageProjectVariantsUserCanAndPageNeedsToBePublishedCache;
	private String debugLevel;
	private Boolean isPhysicalPageCache;

	/**
	 * Construct a policies helper object.
	 */
	public PublishPagePolicies(Page startPage) throws RQLException {
		super();
		this.startPage = startPage;

		// initialize
		parms = getProject().getParameters(parmPageId);
	}

	/**
	 * Converts the given stage name (like DEVE, TEST, PROD) into a domain name for the gui.
	 * <p>
	 * Assume that the publishing target names consists of 3 parts separated by _ in form: stageName_domainName_serverName.
	 */
	public String convertPageStageToServerName(String stageName) throws RQLException {

		java.util.List<PublishingTarget> targets = getPublishingTargets();

		// find target by name starts with
		PublishingTarget stageTarget = null;
		for (int i = 0; i < targets.size(); i++) {
			PublishingTarget target = targets.get(i);
			if (target.getName().startsWith(stageName)) {
				stageTarget = target;
			}
		}

		// split and return middle name, if found
		return stageTarget == null ? stageTarget.getName() : stageName + " - " + StringHelper.split(stageTarget.getName(), "_")[1];
	}

	/**
	 * Returns the publishing job for page and page config; configured with project- and language variants.
	 */
	public PublishingJob createPagePublishingJob(String targetStage, boolean withFollowingPages, boolean withRelatedPages) throws RQLException {

		// prepare job configuration
		PublishingJob result = new PublishingJob(startPage, withFollowingPages, withRelatedPages, jobPackageCheck());
		String[] lvGuids = getJobLanguageVariantGuids();
		
		for (ProjectVariant projectVariant : getJobProjectVariants(targetStage)) {
			for (int i = 0; i < lvGuids.length; i++) {
				String lvGuid = lvGuids[i];
				// never check that user has access to language variant
				result.addToPublish(projectVariant.getProjectVariantGuid(), lvGuid, false);
			}
		}
		return result;
	}

	/**
	 * Ändert den debug level auf DEBUG.
	 */
	public void forceDebug() throws RQLException {
		debugLevel = DEBUG_LEVEL_DEBUG;
	}

	/**
	 * Returns the current user.
	 */
	private User getConnectedUser() throws RQLException {
		return startPage.getCmsClient().getConnectedUser();
	}

	/**
	 * Returns the debug level string
	 */
	public String getDebugLevel() throws RQLException {
		if (debugLevel == null) {
			debugLevel = parms.get("debugLevel");
		}
		return debugLevel;
	}

	/**
	 * Returns the only one language variant (=English) GUID.
	 */
	public String getJobFirstLanguageVariantGuid() throws RQLException {

		return getJobLanguageVariantGuids()[0];
	}

	/**
	 * Returns all language variant GUIDs of the publishing job.
	 */
	public String[] getJobLanguageVariantGuids() throws RQLException {
		return getLanguageVariantGuidsPageNeedsToBePublished();
	}

	/**
	 * Returns a separated string with all project variant GUIDs for the given stage.
	 * <p>
	 * Parameter targetStage has to be one of {@link #getPagePossibleStagesForUserAndPage()}.
	 */
	public String getJobProjectVariantGuids(String targetStage, String separator) throws RQLException {
		java.util.List<String> result = new ArrayList<String>();
		for (ProjectVariant pv : getJobProjectVariants(targetStage)) {
			result.add(pv.getProjectVariantGuid());
		}
		return StringHelper.toString(result, separator);
	}

	/**
	 * Returns a list of all project variants for the given stage.
	 * <p>
	 * Parameter targetStage has to be one of {@link #getPagePossibleStagesForUserAndPage()}.
	 */
	public java.util.List<ProjectVariant> getJobProjectVariants(String targetStage) throws RQLException {

		java.util.List<ProjectVariant> result = new ArrayList<ProjectVariant>();
		for (ProjectVariant projectVariant : getPageProjectVariantsUserCanAndPageNeedsToBePublished()) {
			if (projectVariant.getName().startsWith(targetStage)) {
				result.add(projectVariant);
			}
		}
		return result;
	}
	/**
	 * Returns the main language variant (=English) GUID.
	 */
	public String[] getLanguageVariantGuidsPageNeedsToBePublished() throws RQLException {

		String[] result = new String[1];
		result[0] = startPage.getProject().getMainLanguageVariantGuid();
		return result;
	}

	/**
	 * Returns an ordered list (in fact a set) with all stage names (WORK,DEVE,TEST,PROD,ARCHIVE=first part of pv name)
	 * <p>
	 * the user can publish the given start page.
	 * <p>
	 * Assumes, that the stage name is at the beginning of the project variant name!
	 */
	public java.util.List<String> getPagePossibleStagesForUserAndPage() throws RQLException {

		Set<String> set = new HashSet<String>();

		// add first part (until 1st _) from all
		java.util.List<ProjectVariant> pvs = getPageProjectVariantsUserCanAndPageNeedsToBePublished();
		for (ProjectVariant projectVariant : pvs) {
			set.add(StringHelper.splitAt1stOccurenceFromLeft(projectVariant.getName(), "_")[0]);
		}

		// order the set into given order
		return StringHelper.orderElements(set, parms.get("orderedStageNames"), ",");
	}

	/**
	 * Returns a list of project variant name suffixes to which the start page has to be published.
	 * <p>
	 * pages_HTML, media_constraints_XML or structure_XML
	 */
	private java.util.List<String> getPageProjectVariantNameSuffixes() throws RQLException {
		java.util.List<String> result = new ArrayList<String>();

		// default for all is pages_HTML
		result.add(parms.get("projectVariantPagesNameSuffix"));

		// optional media_constraints_xml for some template folders
		String startPgTmpltFldrGuid = startPage.getTemplateFolderGuid();
		String mediaFolderGuids = parms.get("mediaConstraintsTemplateFolderGuids");
		if (StringHelper.contains(mediaFolderGuids, ",", startPgTmpltFldrGuid, false)) {
			result.add(parms.get("projectVariantMediaConstraintsNameSuffix"));
		}

		// optional structure_XML for some template folders
		String structureFolderGuids = parms.get("structureTemplateFolderGuids");
		if (StringHelper.contains(structureFolderGuids, ",", startPgTmpltFldrGuid, false)) {
			result.add(parms.get("projectVariantStructureNameSuffix"));
		}

		return result;
	}

	/**
	 * Returns a list of all project variants the connected user can publish and the start page needs to be published (no mail).
	 */
	public java.util.List<ProjectVariant> getPageProjectVariantsUserCanAndPageNeedsToBePublished() throws RQLException {

		if (pageProjectVariantsUserCanAndPageNeedsToBePublishedCache == null) {
			pageProjectVariantsUserCanAndPageNeedsToBePublishedCache = new ArrayList<ProjectVariant>();

			// determine project variants the page needs to be published
			java.util.List<String> pagePvNameSuffixes = getPageProjectVariantNameSuffixes();

			// check each project variant the use has the right to publish
			for (Iterator<ProjectVariant> iterator = getPublishableProjectVariants().iterator(); iterator.hasNext();) {
				ProjectVariant pv = (ProjectVariant) iterator.next();

				// match user variants with page variants
				if (StringHelper.endsWithOneOf(pv.getName(), pagePvNameSuffixes)) {
					pageProjectVariantsUserCanAndPageNeedsToBePublishedCache.add(pv);
				}
			}
		}
		return pageProjectVariantsUserCanAndPageNeedsToBePublishedCache;
	}

	/**
	 * Returns the current user's project.
	 */
	private Project getProject() {
		return startPage.getProject();
	}

	/**
	 * Returns a list of all project variants the connected user can publish (has the right to publish).
	 */
	private java.util.List<ProjectVariant> getPublishableProjectVariants() throws RQLException {
		return getConnectedUser().getPublishableProjectVariants();
	}

	/**
	 * Returns the publishing targets, cached.
	 */
	public java.util.List<PublishingTarget> getPublishingTargets() throws RQLException {
		if (targetsCache == null) {
			targetsCache = getProject().getPublishingTargets();
		}
		return targetsCache;
	}

	/**
	 * Returns the start page.
	 */
	public Page getStartPage() throws RQLException {
		return startPage;
	}

	/**
	 * Returns true, if the start page is a block or other page (_row).
	 * 
	 * @see #isPhysicalPage()
	 */
	public boolean isBlockPage() throws RQLException {
		return !isPhysicalPage();
	}

	/**
	 * Returns true, if debug information should be displayed.
	 */
	public boolean isDebug() throws RQLException {
		return DEBUG_LEVEL_DEBUG.equals(getDebugLevel());
	}

	/**
	 * Returns true, if the start page is a physical page.
	 * 
	 * @see #isBlockPage()
	 */
	public boolean isPhysicalPage() throws RQLException {
		if (isPhysicalPageCache == null) {
			PhysicalPage checker = new PhysicalPage(startPage);
			isPhysicalPageCache = new Boolean(checker.isPhysicalPage());
		}
		return isPhysicalPageCache.booleanValue();
	}

	/**
	 * Returns true, if the start page has to be published with following pages, for HIP always false.
	 */
	public boolean isPublishFollowing() throws RQLException {
		return false;
	}

	/**
	 * Returns true, if published with following pages checkbox is enabled or not.
	 * <p>
	 * True only for physical pages and users in special group.
	 */
	public boolean isPublishFollowingEnabled() throws RQLException {
		return isPhysicalPage() && parms.isConnectedUserInUserGroup(parms.get("publishPageWithFollowingUsersGroupName"));
	}

	/**
	 * Returns true, if the start page has to be published with related pages, for HIP always true.
	 */
	public boolean isPublishRelated() throws RQLException {
		return true;
	}

	/**
	 * Returns true, if published with related pages checkbox is enabled or not, for hip always false.
	 */
	public boolean isPublishRelatedEnabled() throws RQLException {
		return false;
	}

	/**
	 * Returns true if both publishing jobs should be created with check in publication package.
	 */
	private boolean jobPackageCheck() throws RQLException {
		return StringHelper.convertToBoolean(parms.get("publishingJobsPublicationPackageCheck"));
	}
}
