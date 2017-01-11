package com.hlcl.rql.hip.as;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.PasswordAuthentication;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.ReddotDate;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.ScriptParameters;

/**
 * @author lejafr
 * 
 * This class uses a singleton instance of this class to provide the list of responsible departments refreshed only once per hour.
 */
public class ResponsibleTableBlock {
	// hold the unique instance
	private static final ResponsibleTableBlock uniqueInstance = new ResponsibleTableBlock();

	// parameter caches
	private Map<String, String> initParms;

	// table data
	private java.util.List<ResponsibleTableRow> responsibleDepartments;

	// refresh properties
	private int refreshIntervalInMinutes; // read from CMS
	private ReddotDate lastRefreshed;

	/**
	 * Returns the unique instance of this class - singleton.
	 */
	public static ResponsibleTableBlock getInstance() {
		return uniqueInstance;
	}

	/**
	 * non public Constructor, use {@link #getInstance()}
	 */
	private ResponsibleTableBlock() {
		super();
	}

	/**
	 * Liefert all Parameter aus der .properties Datei.
	 */
	private Map<String, String> getInitParms() {
		if (initParms == null) {
			initParms = new HashMap<String, String>();
			// fill from file
			ResourceBundle b = ResourceBundle.getBundle(this.getClass().getName());
			Enumeration<String> keys = b.getKeys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				initParms.put(key, b.getString(key));
			}
		}
		return initParms;
	}

	/**
	 * Liefert den Projektnamen aus der .properties Datei.
	 */
	private String getProjectName() {
		return getInitParms().get("projectName");
	}

	/**
	 * Holt alle responsible departments from CMS und cached diese ohne Referenz zu jRQL.
	 * 
	 * @throws RQLException
	 */
	private void readTableDataFromCms() throws RQLException {
		CmsClient client = null;
		try {
			// login
			client = new CmsClient(new PasswordAuthentication(getUserName(), getPassword()));
			Project project = client.getProjectByName(getProjectName());

			// prepare parameters
			ScriptParameters parms = project.getParameters(getParmPageId());

			// remember in instance to steer next refresh
			refreshIntervalInMinutes = Integer.parseInt(parms.get("refreshIntervalInMinutes"));

			// get row pages
			Page tablePg = project.getPageById(parms.get("responsibleTablePageId"));
			PageArrayList rowPages = tablePg.getListChildPages(parms.get("departmentsListTmpltElemName"));

			// get all data and save within data holder rows
			responsibleDepartments = new ArrayList<ResponsibleTableRow>(rowPages.size());
			for (Iterator iterator = rowPages.iterator(); iterator.hasNext();) {
				Page rowPg = (Page) iterator.next();

				// copy and save data
				ResponsibleTableRow rowData = new ResponsibleTableRow(rowPg.getPageGuid(), rowPg.getStandardFieldTextValue(parms
						.get("responsibleAreaTmpltElemName")), rowPg.getStandardFieldTextValue(parms.get("responsibleRccBackupUserIdTmpltElemName")),
						rowPg.getStandardFieldTextValue(parms.get("responsibleRccBackupUserNameTmpltElemName")), rowPg
								.getStandardFieldTextValue(parms.get("responsibleSourceDepartmentNameTmpltElemName")), rowPg.getHeadline(), rowPg
								.getStandardFieldTextValue(parms.get("responsibleRccUserIdTmpltElemName")), rowPg.getStandardFieldTextValue(parms
								.get("responsibleRccUserNameTmpltElemName")), rowPg.getOptionListValue(parms
								.get("responsibleMailSubjectStatisticAreaTmpltElemName")), rowPg.getStandardFieldTextValue(parms
								.get("responsibleMailWorkAreaTmpltElemName")));
				responsibleDepartments.add(rowData);
			}

			// remember last refresh time only if no exception
			lastRefreshed = new ReddotDate();
		} finally {
			// release client
			if (client != null) {
				client.disconnect();
			}
		}
	}

	/**
	 * Liefert den Benutzernamen aus der .properties Datei.
	 */
	private String getUserName() {
		return getInitParms().get("userName");
	}

	/**
	 * Liefert das Benutzerpasswort aus der .properties Datei.
	 */
	private String getPassword() {
		return getInitParms().get("password");
	}

	/**
	 * Liefert die Page ID mit allen ScriptParametern aus der .properties Datei.
	 */
	private String getParmPageId() {
		return getInitParms().get("parmPageId");
	}

	/**
	 * Sorgt dafür, dass beim nächten Aufruf von {@link #getResponsibleDepartments()} die Daten neu vom CMS gelesen werden.
	 */
	public void forceRefreshFromCms() {
		// with this lastRefreshed will be always < now and read will occur
		// original value is restored from cms
		refreshIntervalInMinutes = 0;
	}

	/**
	 * Liefert den Zeitpunkt der letzten Aktualisierung.
	 */
	public String getLastRefreshedTimestampAsddMMyyyyHmma() {
		return lastRefreshed == null ? "never" : lastRefreshed.getAsddMMyyyyHmma();
	}

	/**
	 * Liefert den Zeitpunkt der nächsten automatischen Aktualisierung.
	 */
	public ReddotDate getNextRefreshTime() {
		return new ReddotDate(lastRefreshed).rollMinutes(refreshIntervalInMinutes);
	}

	/**
	 * Liefert den Zeitpunkt der nächsten automatischen Aktualisierung.
	 */
	public String getNextRefreshTimeAsddMMyyyyHmma() {
		return getNextRefreshTime().getAsddMMyyyyHmma();
	}

	/**
	 * Liefert die Anzahl der Minuten vom Aufruf bis zur nächsten automatischen Aktualisierung.
	 */
	public long getNextRefreshTimeAsmm() {
		return getNextRefreshTime().minusAsmm(new ReddotDate());
	}

	/**
	 * Liefert true, falls die Daten vom CMS neu gelesen werden müssen.
	 */
	public boolean needsRefreshFromCms() {
		return lastRefreshed == null || getNextRefreshTime().before(new ReddotDate());
	}

	/**
	 * Liefert alle verantwortlichen Abteilungen aus der RCC tabelle.
	 * <p>
	 * Das aufwendige Einlesen der Tabellendaten wird nur alle refreshIntervalInMinutes (in CMS parm page 924152) Minuten ausgeführt.
	 * 
	 * @throws RQLException
	 */
	public synchronized java.util.List<ResponsibleTableRow> getResponsibleDepartments() throws RQLException {
		// read new version from cms if older than 1h
		if (needsRefreshFromCms()) {
			readTableDataFromCms();
		}
		return responsibleDepartments;
	}

	/**
	 * Returns the responsible department row page from the table for the given page GUID or null, if not found.<p>
	 * Never refreshs the list of responsible departments from CMS; means they has to be requested before.
	 */
	public ResponsibleTableRow findResponsibleDeparmentByPageGuid(String pageGuid) throws RQLException {
		for (ResponsibleTableRow row : responsibleDepartments) {
			if (row.isPageGuidEquals(pageGuid)) {
				return row;
			}
		}
		// signal not found
		return null;
	}

}
