package com.hlcl.rql.as;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Diese Klasse beschreibt den Bericht eines beendeten Generierungsauftrages. Er wird im CMS unter Administer Publication / Job Reports
 * gelistet.
 * 
 * @author LEJAFR
 */
public class PublishingJobReport implements ProjectContainer {

	// constants
	protected final static String TREESEGMENT_TYPE = "project.7040";
	private String name;

	private Project project;
	private String reportGuid;
	private ReddotDate startDate;

	/**
	 * constructor to create a publishing job report
	 * 
	 * @param info
	 *            =Start:27.02.2006/Status:finished
	 */
	public PublishingJobReport(Project project, String reportGuid, String name, String info) throws RQLException {
		super();

		this.project = project;
		this.reportGuid = reportGuid;
		this.name = name;

		parseStartDate(info);
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
	 * Löscht diesen Jobreport.
	 */
	public void delete() throws RQLException {
		/* 
		V5 request
		<IODATA user="lejafr" loginguid="64B3D239978F4BE38EAB3DD5325EA7E8">
		<PROJECT guid="06BE79A1D9F549388F06F6B649E27152" sessionkey="1021834323m6fdJv6Vd84">
		<EXPORTREPORT guid="DEE3E09591E04AC1BF5A81BCBF9B7823" action="delete"/>
		</PROJECT>
		</IODATA>
		V5 response
		<IODATA>
		<EXPORTREPORT guid="DEE3E09591E04AC1BF5A81BCBF9B7823" action="delete" languagevariantid="ENG" dialoglanguageid="ENG"/>
		</IODATA>
		*/

		// build request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>" + "<PROJECT sessionkey='" + getSessionKey() + "'>"
				+ "   <EXPORTREPORT action='delete' guid='" + getReportGuid() + "'/>" + "</PROJECT>" + "</IODATA>";
		// call CMS, ignore answer
		callCmsWithoutParsing(rqlRequest);
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
	 * Liefert den Namen in der Form "Liner Shipping (PageID: 160427)" zurück.
	 */
	public String getName() {
		return name;
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
	 * Liefert die GUID dieses Jobreports.
	 */
	private String getReportGuid() {

		return reportGuid;
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getProject().getSessionKey();
	}

	/**
	 * Liefert das Startdatum des Jobs, zu dem dieser Report erstellt wurde.
	 */
	public ReddotDate getStartDate() {

		return startDate;
	}

	/**
	 * Setzt das Startdatum aus der report info V6.5.0.41=Start:27.02.2006/Status:finished V7.5.0.48=Start:7/3/2007 12:24:43
	 * AM/Status:finished.
	 */
	private void parseStartDate(String info) throws RQLException {
		String[] arr = info.split(" ");
		String[] arr2 = arr[0].split(":");
		String dateStr = arr2[1];

		startDate = ReddotDate.parseSmartTreeDate(dateStr);
		// check 
		if (startDate == null) {
			throw new RQLException("A publishing job report object cannot be created, because the date " + dateStr + " could not be parsed with German or US Local in DateFormat.SHORT. Please adjust the parameters publishingJobParseDate* in com.hlcl.rlq.as.rql_fw.properties; for symbols see http://download.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html");
		}
	}
}
