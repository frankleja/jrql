package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt eine JobQueue mit einer unveränderlichen Jobliste.
 * Die Ordnung der Liste spiegelt den Stand in der Queue wieder. 
 * 
 * @author LEJAFR
 */
public class JobQueue implements ProjectContainer {

	static final String JOB_CATEGORY_PUBLICATION = "0"; // Publication

	static final String JOB_STATUS_WAITING_FOR_START = "3"; // wird gestartet werden
	static final String JOB_STATUS_RUNNING = "2";
	static final String JOB_STATUS_STOPPT = "1"; // paused? 
	private java.util.List<PublishingJob> jobs;

	private Project project;

	/**
	 * constructor comment.
	 */
	public JobQueue(Project project, java.util.List<PublishingJob> jobs) {
		super();

		this.project = project;
		this.jobs = jobs;
	}

	/**
	 * Sendet eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getProject().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck.
	 * Erforderlich für die Ermittlung des Werts eines Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Canceled alle Jobs in dieser Queue.
	 */
	public void cancelAll() throws RQLException {
		for (int i = 0; i < jobs.size(); i++) {
			PublishingJob job = (PublishingJob) jobs.get(i);
			job.cancel();
		}
	}

	/**
	 * Liefert true, falls diese JobQueue einen Job für die gegebenen Seite startPage enthält. 
	 */
	public boolean contains(Page startPage) throws RQLException {
		for (int i = 0; i < jobs.size(); i++) {
			PublishingJob job = (PublishingJob) jobs.get(i);
			// check only jobs with a start page constructed from name 
			if (job.hasStartPage() && job.getStartPage().equals(startPage)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Liefert den Job an Index i.
	 */
	public Object get(int i) {

		return jobs.get(i);
	}

	/**
	 * Liefert den Job an Index i.
	 */
	public PublishingJob getJob(int i) {

		return (PublishingJob) jobs.get(i);
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	/**
	 * Liefert die RedDot logon GUID des users unter dem das script läuft. 
	 * Dies ist nicht die des Users, falls er angemeldet ist!
	 *
	 *@see <code>getOwnLoginGuid</code>
	 */
	public String getLogonGuid() {
		return getProject().getLogonGuid();
	}

	/**
	 * Liefert das Project, zu dem dieser Folder gehoert.
	 */
	public Project getProject() {

		return project;
	}

	/**
	 * Liefert die RedDot GUID des Projekts. 
	 */
	public String getProjectGuid() throws RQLException {
		return getProject().getProjectGuid();
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getProject().getSessionKey();
	}

	/**
	 * Liefert die Anzahl der Jobs in der Queue.
	 */
	public int size() {

		return jobs.size();
	}
}
