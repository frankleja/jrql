package com.hlcl.rql.as;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Diese Klasse beschreibt einen manuell erstellen Publizierungsauftrag, einen laufenden und einen vordefinierten. TODO should use the
 * State pattern and implement possible transitions between.
 * 
 * @author LEJAFR
 */
public class PublishingJob implements ProjectContainer {

	// constants
	private static final int SLEEP_MILLISECONDS = 10000;

	// instance variables only for existing jobs
	private String asyncQueueGuid;
	private String category;
	private String startedByUserName;
	private Project project;

	// dynamically needed to start a job
	private Set<LanguageVariant> languageVariants;
	private Set<String> languageVariantGuids; // 2nd possibility; only via guid
	private User mailReceiver;
	private String name;
	private Set<ProjectVariant> projectVariants;

	// instance variables needed to start a job
	private PublicationPackage publicationPackage;
	private String publishingJobGuid;

	// common instance variables
	private Page startPage;
	private String status;
	private boolean withFollowingPages;
	private boolean withRelatedPages;

	// cache
	private RQLNode detailsNode;

	/**
	 * constructor to start a publishing job
	 */
	public PublishingJob(Page startPage, boolean withFollowingPages) throws RQLException {
		this(startPage, withFollowingPages, false);
	}

	/**
	 * constructor to start a publishing job with or without check in start pages's publication package.
	 */
	public PublishingJob(Page startPage, boolean withFollowingPages, boolean withRelatedPages, boolean checkInPublicationPackage)
			throws RQLException {
		super();

		this.startPage = startPage;
		this.withFollowingPages = withFollowingPages;
		this.withRelatedPages = withRelatedPages;
		this.publicationPackage = checkInPublicationPackage ? startPage.getPublicationPackage() : null;
		this.mailReceiver = null;

		// initialize
		languageVariants = new HashSet<LanguageVariant>();
		languageVariantGuids = new HashSet<String>();
		projectVariants = new HashSet<ProjectVariant>();
	}

	/**
	 * constructor to start a publishing job with check in publication package of start page.
	 */
	public PublishingJob(Page startPage, boolean withFollowingPages, boolean withRelatedPages) throws RQLException {
		this(startPage, withFollowingPages, withRelatedPages, true);
	}

	/**
	 * Constructor for an existing (running) publishing job
	 * 
	 * @throws RQLException
	 */
	public PublishingJob(Project project, Page startPageOrNull, String name, String asyncQueueGuid, String category, String status,
			String jobGuid, String startedByUserName) throws RQLException {
		super();

		this.project = project; // use if start page is null
		this.startPage = startPageOrNull;
		this.name = name;
		this.asyncQueueGuid = asyncQueueGuid;
		this.category = category;
		this.status = status;
		this.publishingJobGuid = jobGuid;
		this.startedByUserName = startedByUserName;
	}

	/**
	 * Constructor for a defined publishing job - not running.
	 * 
	 * @throws RQLException
	 */
	public PublishingJob(Project project, String name, String jobGuid) throws RQLException {
		this.project = project;
		this.name = name;
		this.publishingJobGuid = jobGuid;
	}

	/**
	 * Dieser Generierungsauftrag wird auch die Sprachvariante mit der languageVariantGuid mitgenerieren.
	 */
	private void addLanguageVariant(String languageVariantGuid) {

		languageVariantGuids.add(languageVariantGuid);
	}

	/**
	 * Dieser Generierungsauftrag wird auch die Sprachvariante languageVariant mitgenerieren.
	 * 
	 * @param languageVariant
	 *            mitzugenerierende Sprachvariante
	 */
	private void add(LanguageVariant languageVariant) {

		languageVariants.add(languageVariant);
	}

	/**
	 * Dieser Generierungsauftrag wird auch die Projektvariante projectVariant mitgenerieren.
	 * 
	 * @param projectVariant
	 *            mitzugenerierende Projektvariante
	 */
	private void add(ProjectVariant projectVariant) {

		projectVariants.add(projectVariant);
	}

	/**
	 * Dieser Generierungsauftrag wird auch die gegebenen Projektvariante in der gegebenen Sprachvariante languageVariant generieren.
	 */
	public void addToPublish(ProjectVariant projectVariant, LanguageVariant languageVariant) throws RQLException {

		// check if within publication package
		if (publicationPackage != null && !publicationPackage.containsSettingFor(projectVariant, languageVariant)) {
			throw new InvalidPublishingRequestException("Page with ID " + startPage.getPageId()
					+ " could not published for project variant named " + projectVariant.getName() + " and language variant named "
					+ languageVariant.getName() + ", because the publication package named " + publicationPackage.getName()
					+ " has no publication setting for the requested combination.");
		}

		add(projectVariant);
		add(languageVariant);
	}

	/**
	 * Dieser Generierungsauftrag wird auch die gegebenen Projektvariante in der gegebenen Sprachvariante mit languageVariantGuid
	 * generieren.
	 * <p>
	 * Es wird hier nicht geprüft, ob es im Exportpaket der startPage auch eine entsprechende Kombination gibt!
	 */
	private void addToPublish(ProjectVariant projectVariant, String languageVariantGuid) throws RQLException {

		add(projectVariant);
		addLanguageVariant(languageVariantGuid);
	}

	/**
	 * Bei diesem Generierungsauftrag wird auch die Sprachvariante languageVariant mitgeneriert.
	 * 
	 */
	public void addToPublish(PublicationSetting publicationSetting) throws RQLException {

		// check if within publication package
		if (publicationPackage != null && !publicationPackage.contains(publicationSetting)) {
			throw new InvalidPublishingRequestException("Page with ID " + startPage.getPageId()
					+ " could not published for publication setting named " + publicationSetting.getName());
		}

		add(publicationSetting.getProjectVariant());
		add(publicationSetting.getLanguageVariant());
	}

	/**
	 * Dieser Generierungsauftrag wird auch die gegebenen Projektvariante in der gegebenen Sprachvariante languageVariant generieren.
	 */
	public void addToPublish(String projectVariantGuid, String languageVariantGuid) throws RQLException {

		addToPublish(projectVariantGuid, languageVariantGuid, true);
	}

	/**
	 * Dieser Generierungsauftrag wird auch die gegebenen Projektvariante in der gegebenen Sprachvariante languageVariant generieren.
	 * 
	 * @param checkThatLanguageVariantIsAccessible
	 *            =true, the needs access to the given language variant in order to publish it
	 *            <p>
	 *            =false, don't check the language variant guid; user can publish it even without having access to it!
	 */
	public void addToPublish(String projectVariantGuid, String languageVariantGuid, boolean checkThatLanguageVariantIsAccessible)
			throws RQLException {

		Project project = getProject();
		if (checkThatLanguageVariantIsAccessible) {
			// read lv and pv
			addToPublish(project.getProjectVariantByGuid(projectVariantGuid), project.getLanguageVariantByGuid(languageVariantGuid));
		} else {
			// publish lv by guid only
			addToPublish(project.getProjectVariantByGuid(projectVariantGuid), languageVariantGuid);
		}
	}

	/**
	 * Dieser Generierungsauftrag wird auch alle gegebenen Projektvarianten in der gegebenen Sprachvariante generieren.
	 */
	public void addToPublish(String projectVariantGuids, String separator, String languageVariantGuid) throws RQLException {

		// add project variants
		String[] guids = projectVariantGuids.split(separator);
		for (int i = 0; i < guids.length; i++) {
			addToPublish(guids[i], languageVariantGuid);
		}
	}

	/**
	 * Dieser Generierungsauftrag wird alle Kombinationen der gegebenen Projekt- und Sprachvarianten generieren.
	 */
	public void addToPublishAllCombinations(String projectVariantGuids, String separator, String languageVariantGuids)
			throws RQLException {

		// for all language variants publish all project variants
		String[] guids = languageVariantGuids.split(separator);
		for (int i = 0; i < guids.length; i++) {
			addToPublish(projectVariantGuids, separator, guids[i]);
		}
	}

	/**
	 * Dieser Generierungsauftrag wird alle Kombinationen der gegebenen Projekt- und Sprachvarianten generieren.
	 */
	public void addToPublishAllCombinations(String projectVariantGuids, String separator,
			java.util.List<LanguageVariant> languageVariants) throws RQLException {
		// for all language variants publish all project variants
		for (LanguageVariant lv : languageVariants) {
			addToPublish(projectVariantGuids, separator, lv.getLanguageVariantGuid());
		}
	}

	/**
	 * Dieser Generierungsauftrag wird alle Kombinationen der gegebenen Projekt- und Sprachvarianten generieren.
	 */
	public void addToPublishAllCombinations(java.util.List<ProjectVariant> projectVariants,
			java.util.List<LanguageVariant> languageVariants) throws RQLException {

		for (Iterator iterator = languageVariants.iterator(); iterator.hasNext();) {
			LanguageVariant languageVariant = (LanguageVariant) iterator.next();
			for (Iterator iterator2 = projectVariants.iterator(); iterator2.hasNext();) {
				ProjectVariant projectVariant = (ProjectVariant) iterator2.next();
				addToPublish(projectVariant, languageVariant);
			}
		}
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getCmsClient().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines
	 * Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Cancel this job.
	 */
	public void cancel() throws RQLException {
		/* request V6
		 <IODATA user="lejafr" loginguid="36BF97DE01334066B80A8EA71700814E">
		 <PROJECT projectguid="06BE79A1D9F549388F06F6B649E27152" sessionkey="1021834323363bF211l76">
		 <EXPORT guid="EB135CEA27E744AB85F53D443EEA617F" action="cancel"/>
		 </PROJECT>
		 </IODATA>
		 response V6
		 <IODATA><EXPORT action="cancel" languagevariantid="ENG" dialoglanguageid="ENG" projectguid="06BE79A1D9F549388F06F6B649E27152" guid="2BFD3DB07F1A4A9A9460592BE39D51E0"/>
		 </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + " <PROJECT projectguid='" + getProjectGuid()
				+ "' sessionkey='" + getSessionKey() + "'>" + " <EXPORT action='cancel' guid='" + getAsyncQueueGuid() + "'/>"
				+ "</PROJECT>" + "</IODATA>";
		// ignore result
		callCms(rqlRequest);
	}

	/**
	 * @return GUID of asynch queue
	 */
	public String getAsyncQueueGuid() {
		return asyncQueueGuid;
	}

	/**
	 * @return job category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return the user's name starting this existing job.
	 * @throws RQLException
	 */
	public String getStartedByUserName() throws RQLException {
		return startedByUserName;
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	/**
	 * Liefert die RedDot logon GUID des users unter dem das script läuft. Dies ist nicht die des Users, falls er angemeldet ist!
	 * 
	 * @see <code>getOwnLoginGuid</code>
	 */
	public String getLogonGuid() {
		return getProject().getLogonGuid();
	}

	/**
	 * Liefert den User, der eine mail nach Ende des Jobs erhalten soll. Ist null, falls keine Mail versandt werden soll.
	 */
	public User getMailReceiver() {
		return mailReceiver;
	}

	/**
	 * Liefert den Namen zurück.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Liefert true, falls der existierende Job eine startseite im Namen (ID: ) hat und damit auch ein Objekt in startPage.
	 */
	public boolean hasStartPage() {
		return getStartPage() != null;
	}

	/**
	 * Liefert das Project, zu dem dieser Folder gehoert.
	 */
	public Project getProject() {
		if (project == null && hasStartPage()) {
			project = getStartPage().getProject();
		}
		return project;
	}

	/**
	 * Liefert die RedDot GUID des Projekts.
	 */
	public String getProjectGuid() throws RQLException {
		return getProject().getProjectGuid();
	}

	/**
	 * Liefert die RedDot GUID dieses Generierungsauftrages.
	 */
	public String getPublishingJobGuid() throws RQLException {
		return publishingJobGuid;
	}

	/**
	 * Liefert alle laufenden asynchrone Jobs.
	 * <p>
	 * Kann nur von Administratoren verwendet werden!
	 */
	private Map getRunningJobs() throws RQLException {
		RQLNode rqlResponse = getProject().getAsyncJobsRootNode();

		// map nodes per page id
		Map jobs = new HashMap();
		RQLNode list1Node = rqlResponse.getNode("PROCESSLIST1");
		if (list1Node != null) {
			RQLNodeList runningJobsNodeList = list1Node.getNodes("ASYNCQUEUE");
			for (int i = 0; i < runningJobsNodeList.size(); i++) {
				RQLNode jobNode = runningJobsNodeList.get(i);
				String name = jobNode.getAttribute("name");
				int start = name.indexOf("ID:");
				int end = name.indexOf(")", start);
				Integer pageId = Integer.getInteger(name.substring(start + 3, end).trim());
				jobs.put(pageId, jobNode);
			}
		}
		return jobs;
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getProject().getSessionKey();
	}

	/**
	 * Liefert die Startseite dieses Generierungsauftrages.
	 */
	public Page getStartPage() {

		return startPage;
	}

	/**
	 * @return the state of this job
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Setzt den User, der nach Jobende eine Mail erhalten soll.
	 */
	public void setMailReceiver(User user) {
		mailReceiver = user;
	}

	/**
	 * Started diesen Generierungsauftrag.
	 */
	public void start() throws RQLException {

		/* 
		 V5 request
		 <IODATA loginguid="4CDC1675087A4AA0A36FE9EFC6A5F7B8">
		 <PROJECT guid="06BE79A1D9F549388F06F6B649E27152" sessionkey="571683732g8m33Lo1hD4">
		 <PAGE guid="8D97BC251CA842609AA90955DF6E3D13">
		 <EXPORTJOB action="save" generatenextpages="1" generaterelativepages="1" >
		 <LANGUAGEVARIANTS action="checkassigning">
		 <LANGUAGEVARIANT guid="E6FC9644A75945729B018F98C6299D50" checked="1"/>
		 </LANGUAGEVARIANTS>
		 <PROJECTVARIANTS action="checkassigning">
		 <PROJECTVARIANT guid="7FADE58885E540ED83A046949C209FC0" checked="1"/>
		 </PROJECTVARIANTS>
		 </EXPORTJOB>
		 </PAGE>
		 </PROJECT>
		 </IODATA>
		 V5 response
		 <IODATA>
		 <EXPORTJOB generatenextpages="1" action="start" projectguid="06BE79A1D9F549388F06F6B649E27152" guid="8D97BC251CA842609AA90955DF6E3D13">
		 <LANGUAGEVARIANTS action="checkassigning">
		 <LANGUAGEVARIANT guid="E6FC9644A75945729B018F98C6299D50" checked="1"/>
		 </LANGUAGEVARIANTS>
		 <PROJECTVARIANTS action="checkassigning">
		 <PROJECTVARIANT guid="7FADE58885E540ED83A046949C209FC0" checked="1"/>
		 </PROJECTVARIANTS>
		 </EXPORTJOB>
		 </IODATA>
		 */

		// check if at least one language and project variant is given
		if (languageVariants.size() + languageVariantGuids.size() == 0 || projectVariants.isEmpty()) {
			throw new IncompletePublishingJobException("Publishing job for page with headline " + startPage.getHeadline()
					+ " has no project- and/or language variant.");
		}

		// build request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<PROJECT guid='"
				+ getProjectGuid() + "'>" + "   <PAGE guid='" + startPage.getPageGuid() + "'>"
				+ "    <EXPORTJOB action='save' generatenextpages='" + (withFollowingPages ? "1" : "0") + "' generaterelativepages='"
				+ (withRelatedPages ? "1" : "0") + "' email='" + (mailReceiver != null ? mailReceiver.getUserGuid() : "") + "'>"
				+ "     <LANGUAGEVARIANTS action='checkassigning'>";

		// add language variant objects
		Iterator iterator = languageVariants.iterator();
		while (iterator.hasNext()) {
			LanguageVariant languageVariant = (LanguageVariant) iterator.next();
			rqlRequest = rqlRequest + "<LANGUAGEVARIANT guid='" + languageVariant.getLanguageVariantGuid() + "' checked='1'/>";
		}
		// add language variant guids
		iterator = languageVariantGuids.iterator();
		while (iterator.hasNext()) {
			String languageVariantGuid = (String) iterator.next();
			rqlRequest = rqlRequest + "<LANGUAGEVARIANT guid='" + languageVariantGuid + "' checked='1'/>";
		}

		rqlRequest = rqlRequest + "     </LANGUAGEVARIANTS>" + "     <PROJECTVARIANTS action='checkassigning'>";

		// add proeject variants
		iterator = projectVariants.iterator();
		while (iterator.hasNext()) {
			ProjectVariant projectVariant = (ProjectVariant) iterator.next();
			rqlRequest = rqlRequest + "<PROJECTVARIANT guid='" + projectVariant.getProjectVariantGuid() + "' checked='1'/>";
		}

		rqlRequest = rqlRequest + "     </PROJECTVARIANTS>" + "    </EXPORTJOB>" + "   </PAGE>" + " </PROJECT>" + "</IODATA>";

		// call CMS and save job guid
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		String msgDe = b.getString("pageAlreadyInPublishingQueueDe");
		String msgEn = b.getString("pageAlreadyInPublishingQueueEn");
		RQLNode rqlResponse = null;
		try {
			rqlResponse = callCms(rqlRequest);
		} catch (RQLException rqle) {
			// handle already waiting to be published
			if (rqle.getMessage().indexOf(msgDe) >= 0 || rqle.getMessage().indexOf(msgEn) >= 0) {
				throw new PageAlreadyInPublishingQueueException("Publishing for page " + startPage.getHeadlineAndId()
						+ " is currently running or waiting in queue. New job for this page cannot be startet.");
			} else {
				throw rqle;
			}
		}
		publishingJobGuid = rqlResponse.getNode("EXPORTJOB").getAttribute("guid");
	}

	/**
	 * Wartet, bis dieser Generierungsauftrag abgeschlossen ist. ACHTUNG! Das warten kann nur mit ADMINISTRATOR Recht ausgeführt
	 * werden!
	 */
	public void waitUntilFinished() throws RQLException {

		Map runningJobs = getRunningJobs();
		while (runningJobs.containsKey(startPage.getPageIdAsInteger())) {
			try {
				Thread.sleep(SLEEP_MILLISECONDS);
			} catch (InterruptedException ie) {
				// proceed only
			}
			runningJobs = getRunningJobs();
		}
	}

	/**
	 * Liefert die Anzahl der gesendeten Mails - eine pro Kombination.
	 */
	public int getNumberOfPublishingMails() throws RQLException {
		return (languageVariantGuids.size() + languageVariants.size()) * projectVariants.size();
	}

	/**
	 * Liefert die Projektvarianten dieses Jobs zurück.
	 */
	public Set<ProjectVariant> getProjectVariants() throws RQLException {
		return projectVariants;
	}

	/**
	 * Liefert die Sprachvarianten dieses Jobs zurück.
	 */
	public Set<LanguageVariant> getLanguageVariants() throws RQLException {
		return languageVariants;
	}

	/**
	 * Liefert die Sprachvarianten GUIDs dieses Jobs zurück.
	 */
	public Set<String> getLanguageVariantGuids() throws RQLException {
		return languageVariantGuids;
	}

	/**
	 * Liefert true, falls dieser vordefinierte Publizierungsauftrag gerade aktiv ist, sonst false.
	 */
	public boolean isActive() throws RQLException {
		return "1".equals(getDetailsNode().getAttribute("active"));
	}

	/**
	 * Enable/disable an automatic publishing job.
	 * <p>
	 * ATTENTION: I experienced a problem with this RQL command. The checkbox state is shown correctly, BUT the job didn't react
	 * appropriately!
	 * <p>
	 * THIS METHOD IS NOT RELIABLE!
	 */
	public void setIsActive(boolean isActive) throws RQLException {
		save("active", StringHelper.convertTo01(isActive));
	}

	/**
	 * Liefert true, falls die Eigenschaft active dieses vordefinierten Publizierungsauftrages dem gegebenen Wert entspricht.
	 */
	public boolean isActive(boolean isActive) throws RQLException {
		return isActive && isActive();
	}

	/**
	 * Liefert den details node für diesen definierten Publizierungsjob zurück.
	 */
	private RQLNode getDetailsNode() throws RQLException {
		/*
		 V7.5 request 
		<IODATA loginguid="E86954A19D204CDF8AFCAFC6AF692E45" sessionkey="8B2A75E07AF74738AF1688F2567D9429">
		  <PROJECT projectguid="06BE79A1D9F549388F06F6B649E27152">   
		    <EXPORTJOB action="load" guid="0940D932D95D4DAE8EBD6060DD57D33D" /> 
		  </PROJECT>               
		</IODATA> 
		 V7.5 request 
		<IODATA>
		<EXPORTJOB action="load" languagevariantid="ENG" dialoglanguageid="ENG" projectguid="06BE79A1D9F549388F06F6B649E27152" 
		guid="0940D932D95D4DAE8EBD6060DD57D33D" name="a business admin tree to PROD" generaterelativepages="0" 
		startguid="F1DFB56FE82B476ABC474D8565C474A4" starttype="PAG" timetableguid="71F56A88A635477DB2CAA2EEB0D4CA07" 
		manual="0" toppriority="0" email="" startname="Business Administration tree" status="1" begindate="0" enddate="0" 
		createddate="39353.5001851852" changeddate="40092.670625" createduserguid="4324D172EF4342669EAF0AD074433393" 
		changeduserguid="4324D172EF4342669EAF0AD074433393" days="" time="" reddotserver="" generatenextpages="0" 
		active="1" priority="1"/>
		</IODATA>
		 */

		if (detailsNode == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
					+ "<PROJECT projectguid='" + getProjectGuid() + "' >" + "   <EXPORTJOB action='load' guid='"
					+ getPublishingJobGuid() + "'/>" + "</PROJECT>" + "</IODATA>";
			detailsNode = callCms(rqlRequest).getNode("EXPORTJOB");
		}
		return detailsNode;
	}

	/**
	 * Speichert Änderungen an diesem vordefinierten Publishing job.
	 */
	private void save(String attributeName, String attributeValue) throws RQLException {
		/* 
		 V7.5 request
		<IODATA loginguid="E86954A19D204CDF8AFCAFC6AF692E45" sessionkey="8B2A75E07AF74738AF1688F2567D9429">
		  <PROJECT>   
		    <EXPORTJOB action="save" guid="0940D932D95D4DAE8EBD6060DD57D33D" active="1" /> 
		  </PROJECT>               
		</IODATA> 
		 V7.5 response
		<IODATA>
		<EXPORTJOB action="save" active="1" languagevariantid="ENG" dialoglanguageid="ENG" projectguid="06BE79A1D9F549388F06F6B649E27152" guid="0940D932D95D4DAE8EBD6060DD57D33D" timetableguid="71F56A88A635477DB2CAA2EEB0D4CA07"/>
		</IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'><PROJECT>"
				+ "<EXPORTJOB action='save' guid='" + getPublishingJobGuid() + "' " + attributeName + "='" + attributeValue + "' />"
				+ "</PROJECT></IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}

}
