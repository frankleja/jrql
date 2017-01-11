package com.hlcl.rql.hip.as;

/**
 * @author lejafr
 * 
 * This class holds responsible department row data without references to jRQL.
 */
public class ResponsibleTableRow {

	private String pageGuid;
	private String responsibleArea;
	private String responsibleBackupRccUserId;
	private String responsibleBackupRccUserName;
	private String responsibleDepartmentName;
	private String responsibleDepartmentNumber;
	private String responsibleRccUserId;
	private String responsibleRccUserName;
	private String responsibleStatisticArea;
	private String responsibleWorkArea;

	/**
	 * Constructor - save all given data
	 */
	public ResponsibleTableRow(String pageGuid, String responsibleArea, String responsibleBackupRccUserId, String responsibleBackupRccUserName,
			String responsibleDepartmentName, String responsibleDepartmentNumber, String responsibleRccUserId, String responsibleRccUserName,
			String responsibleStatisticArea, String responsibleWorkArea) {
		this.pageGuid = pageGuid;
		this.responsibleArea = responsibleArea;
		this.responsibleBackupRccUserId = responsibleBackupRccUserId;
		this.responsibleBackupRccUserName = responsibleBackupRccUserName;
		this.responsibleDepartmentName = responsibleDepartmentName;
		this.responsibleDepartmentNumber = responsibleDepartmentNumber;
		this.responsibleRccUserId = responsibleRccUserId;
		this.responsibleRccUserName = responsibleRccUserName;
		this.responsibleStatisticArea = responsibleStatisticArea;
		this.responsibleWorkArea = responsibleWorkArea;
	}

	/**
	 * Returns the responsible row's area (Corporate, Region Europe).
	 */
	public String getResponsibleArea() {
		return responsibleArea;
	}

	/**
	 * Returns the CMS page GUID of this row page data. 
	 */
	public String getPageGuid() {
		return pageGuid;
	}

	/**
	 * Returns true, if the page GUID of this row page data isEquals() pageGuidToCheck. 
	 */
	public boolean isPageGuidEquals(String pageGuidToCheck) {
		return getPageGuid().equals(pageGuidToCheck);
	}

	/**
	 * Returns the responsible row's backup regional content coordinator's user name (lejafr, strutku).
	 */
	public String getResponsibleBackupRccUserId() {
		return responsibleBackupRccUserId;
	}

	/**
	 * Returns the responsible row's backup regional content coordinator's user name (Frank Leja, Kurt Strutz).
	 */
	public String getResponsibleBackupRccUserName() {
		return responsibleBackupRccUserName;
	}

	/**
	 * Returns the responsible row's source department name.
	 */
	public String getResponsibleDepartmentName() {
		return responsibleDepartmentName;
	}

	/**
	 * Returns the responsible row's source department number.
	 */
	public String getResponsibleDepartmentNumber() {
		return responsibleDepartmentNumber;
	}

	/**
	 * Returns the responsible row's regional content coordinator's user id (lejafr, strutku).
	 */
	public String getResponsibleRccUserId() {
		return responsibleRccUserId;
	}

	/**
	 * Returns the responsible row's regional content coordinator's user name (Frank Leja, Kurt Strutz).
	 */
	public String getResponsibleRccUserName() {
		return responsibleRccUserName;
	}

	/**
	 * Returns the responsible row's mail subject statistic area (COM, SAL).
	 */
	public String getResponsibleStatisticArea() {
		return responsibleStatisticArea;
	}

	/**
	 * Returns the responsible row's mail subject work area (business-administration, trade-management).
	 */
	public String getResponsibleWorkArea() {
		return responsibleWorkArea;
	}

}
