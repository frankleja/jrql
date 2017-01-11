package com.hlcl.rql.hip.as;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hlcl.rql.as.Container;
import com.hlcl.rql.as.List;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.ReddotDate;
import com.hlcl.rql.as.StandardFieldTextElement;
import com.hlcl.rql.as.StringHelper;
import com.hlcl.rql.as.Template;
import com.hlcl.rql.as.WrongTypeException;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.ProjectPage;

/**
 * @author lejafr
 * 
 * This class represents the content page aspect of pages in the HIP project.
 */
public class ContentPage extends ProjectPage {

	private Page responsibleRowCache;
	private Map<String, String> responsibleIdConversionsMapCache;
	private Map<String, String> responsibleId2DepartmentNumberMapCache;

	/**
	 * Construct a content page wrapping the given general page.
	 */
	public ContentPage(Page page) {
		super(page);
	}

	/**
	 * Creates a new page on the blocks container.
	 * 
	 * @param blockTemplate
	 *            Typ der neu erstellten Seite.
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 */
	public Page createBlock(Template blockTemplate, String headline) throws RQLException {
		return getPage().createAndConnectPageAtContainer(getParameter("blocksTmpltElemName"), blockTemplate, headline);
	}

	/**
	 * Returns the first link table block found in this page's container element blocks, or null, if no link table block found.
	 */
	public LinkTableBlock getFirstLinkTableBlock() throws RQLException {
		PageArrayList pages = getPage().getContainerChildPages(getParameter("blocksTmpltElemName"),
				getParameter("linkTableBlockTmpltName"));
		if (pages.isEmpty()) {
			return null;
		}
		return new LinkTableBlock(pages.first());
	}

	/**
	 * Returns the first text block found in this page's container element blocks, or null, if no block found.
	 */
	public TextBlock getFirstTextBlock() throws RQLException {
		PageArrayList pages = getPage()
				.getContainerChildPages(getParameter("blocksTmpltElemName"), getParameter("textBlockTmpltName"));
		if (pages.isEmpty()) {
			return null;
		}
		return new TextBlock(pages.first());
	}

	/**
	 * Try to disconnect the first found text block from this content page, if this page has a text block.
	 * <p>
	 * Returns the disconnected text block or null, if no block found.
	 */
	public TextBlock disconnectFirstTextBlock() throws RQLException {
		TextBlock firstTextBlock = getFirstTextBlock();
		if (firstTextBlock != null) {
			getBlocksContainer().disconnectChild(firstTextBlock.getPage());
			return firstTextBlock;
		}
		// no text block found
		return null;
	}

	/**
	 * Disconnect the given text block from this content page, if this page contains given text block. Otherwise do nothing and return
	 * null.
	 * <p>
	 * Returns the disconnected text block or null, if no block were disconnected.
	 */
	public TextBlock disconnectTextBlock(TextBlock textBlock) throws RQLException {
		Container blocksContainer = getBlocksContainer();
		if (blocksContainer.isChild(textBlock.getPage())) {
			blocksContainer.disconnectChild(textBlock.getPage());
			return textBlock;
		}
		// signal that given text block is not a child
		return null;
	}

	/**
	 * Connects the given text block on this page's container blocks depending on given addAtBottom.
	 * <p>
	 * Returns true, if newly linked and false, if already linked.
	 */
	public boolean connectToExistingTextBlock(TextBlock textBlock, boolean addAtBottom) throws RQLException {
		Container blocksContainer = getBlocksContainer();
		if (blocksContainer.isChild(textBlock.getPage())) {
			return false;
		}
		// link really
		blocksContainer.connectToExistingPage(textBlock.getPage(), addAtBottom);
		return true;
	}

	/**
	 * Returns the value of the standard field text element invisible_info_sft.
	 */
	public String getInvisibleInfo() throws RQLException {
		return getStandardFieldTextValue(getParameter("versionCodeTmpltElemName"));
	}

	/**
	 * Changes the value of the standard field text element invisible_info_sft.
	 */
	public void setInvisibleInfo(String invisibleInfo) throws RQLException {
		setStandardFieldTextValue(getParameter("versionCodeTmpltElemName"), invisibleInfo);
	}

	/**
	 * Returns true, if this content page contains a text block.
	 */
	public boolean hasTextBlock() throws RQLException {
		return getFirstTextBlock() != null;
	}

	/**
	 * Creates a new link table block page on the blocks container.
	 * 
	 * @param headlinePrefix
	 *            Headline of new created link table block page
	 */
	public LinkTableBlock createLinkTableBlock(String headlinePrefix) throws RQLException {
		Template template = getProject().getTemplateByName(getParameter("contentTmpltFldrName"),
				getParameter("linkTableBlockTmpltName"));
		return new LinkTableBlock(getPage().createAndConnectPageAtContainer(getParameter("blocksTmpltElemName"), template,
				headlinePrefix + " " + template.getName()));
	}

	/**
	 * Creates a new page on the blocks container.
	 * 
	 * @param blockTemplate
	 *            Typ der neu erstellten Seite.
	 * @param headline
	 *            Ueberschrift der neu erstellten Seite
	 * @param addAtBottom
	 *            true=>seite wird nach unten verschoben, false=>seite wird nicht verschoben und RD default is oben anlegen
	 */
	public Page createBlock(Template blockTemplate, String headline, boolean addAtBottom) throws RQLException {
		return getPage().createAndConnectPageAtContainer(getParameter("blocksTmpltElemName"), blockTemplate, headline, addAtBottom);
	}

	/**
	 * Sets the responsible information for given domain option list value, page updated to today.
	 * 
	 * @param responsibleId
	 * name of the mailbox; part before @
	 * @param displayName
	 *            clickable name of the mail address
	 */
	public void setFooter(String responsibleId, String displayName) throws RQLException {
		setFooter(responsibleId, displayName, new ReddotDate());
	}

	/**
	 * Sets the responsible information for given domain option list value.
	 * 
	 * @param responsibleId
	 * name of the mailbox; part before @
	 * @param displayName
	 *            clickable name of the mail address
	 * @param updated
	 *            the page updated date
	 */
	public void setFooter(String responsibleId, String displayName, ReddotDate updated) throws RQLException {
		Page page = getPage();
		page.startSetElementValues();
		page.addSetStandardFieldTextValue(getParameter("respIdTmpltElemName"), responsibleId);
		page.addSetStandardFieldTextValue(getParameter("respNameTmpltElemName"), displayName);
		page.addSetStandardFieldDateValue(getParameter("pageUpdatedTmpltElemName"), updated);
		page.endSetElementValues();
	}

	/**
	 * Changes all responsibility related elements in one step.
	 * 
	 * @param requesterUserId
	 *            a max 7 chars length HL user id
	 * @param isResponsibleIdAPerson
	 *            if true, the given responsibleId is a person, otherwise a group mailbox id
	 * @param responsibleId
	 *            a HL user id or group mailbox id
	 * @param responsibleName
	 *            person or group mailbox displayed name in page footer link
	 * @param responsibleDepartmentNumber
	 *            department number from responsible department table
	 * @throws RQLException
	 * @throws InvalidResponsibleDepartmentException
	 *             if given department number cannot be found in table
	 */
	public void setResponsibility(String requesterUserId, boolean isResponsibleIdAPerson, String responsibleId,
			String responsibleName, String responsibleDepartmentNumber) throws RQLException {
		String parmName = "responsibleIdTypeGroupMailboxValue";
		if (isResponsibleIdAPerson) {
			parmName = "responsibleIdTypePersonValue";
		}
		setResponsibility(requesterUserId, getParameter(parmName), responsibleId, responsibleName);
		// link row from table
		linkResponsibleDepartmentByDepartmentNumber(responsibleDepartmentNumber);
	}

	/**
	 * Changes all responsibility related elements in one step. Trigger only one RQL command.
	 * 
	 * @param requesterUserId
	 *            a max 7 chars length HL user id
	 * @param responsibleType
	 *            has to be person or groupMailbox
	 * @param responsibleId
	 *            a HL user id or group mailbox id
	 * @param responsibleName
	 *            person or group mailbox displayed name in page footer link
	 * @throws RQLException
	 */
	public void setResponsibility(String requesterUserId, String responsibleType, String responsibleId, String responsibleName)
			throws RQLException {
		Page page = getPage();
		page.startSetElementValues();
		page.startDeleteElementValues();
		// delete or update
		if (requesterUserId.length() == 0) {
			page.addDeleteStandardFieldTextValue(getParameter("requesterUserIdTmpltElemName"));
		} else {
			page.addSetStandardFieldTextValue(getParameter("requesterUserIdTmpltElemName"), requesterUserId.toLowerCase());
		}
		page.addSetOptionListValue(getParameter("responsibleIdTypeTmpltElemName"), responsibleType);
		// convert a personal user id always to lower case
		if (responsibleType.equals(getParameter("responsibleIdTypePersonValue"))) {
			responsibleId = responsibleId.toLowerCase();
		}
		page.addSetStandardFieldTextValue(getParameter("respIdTmpltElemName"), responsibleId);
		page.addSetStandardFieldTextValue(getParameter("respNameTmpltElemName"), responsibleName);
		page.endDeleteElementValues();
		page.endSetElementValues();
	}

	/**
	 * Sets the page updated to today and returns this reddot date, with date and time part.
	 */
	public ReddotDate updatedToday() throws RQLException {
		ReddotDate today = new ReddotDate();
		setUpdated(today);
		return today;
	}

	/**
	 * Sets the page updated to today and returns this reddot date, only date not time part.
	 */
	public ReddotDate setUpdatedToday() throws RQLException {
		ReddotDate today = ReddotDate.now();
		setUpdated(today);
		return today;
	}

	/**
	 * Sets the page updated to given date.
	 */
	public void setUpdated(Date timestamp) throws RQLException {
		setUpdated(new ReddotDate(timestamp));
	}

	/**
	 * Return true, if this page has at least on row child page linked, otherwise false.
	 * <p>
	 * The child page is not checked if still in responsible department table. Use {@link #isResponsibleDepartmentValid()}
	 */
	public boolean isResponsibleDepartmentLinked() throws RQLException {
		return getResponsibleList().getChildPagesSize() >= 1;
	}

	/**
	 * Returns true, if the linked responsible department row is contained in the responsible department table.
	 * <p>
	 * Returns false if no responsible department row is linked at all.
	 */
	public boolean isResponsibleDepartmentValid() throws RQLException {
		Page rowPg = getResponsibleRow();
		if (rowPg == null) {
			return false;
		}
		return getResponsibleDepartments().contains(rowPg);
	}

	/**
	 * Return true, if given page is equal the linked responsible row page.
	 * <p>
	 * Return false, if no responsible department row page is linked at all on this page. Attention is quite slow, if no table row is
	 * linked, because every call forces a rql to get linked row.
	 */
	public boolean isResponsibleDepartmentEquals(Page responsibleTableRow) throws RQLException {
		return isResponsibleDepartmentLinked() && getResponsibleRow().equals(responsibleTableRow);
	}

	/**
	 * Returns the responsible row's area (Corporate, Region Europe). Use only, if {@link #isResponsibleDepartmentLinked()} is true.
	 */
	public String getResponsibleArea() throws RQLException {
		return getResponsibleRow().getStandardFieldTextValue(getParameter("responsibleAreaTmpltElemName"));
	}

	/**
	 * Returns the responsible department row's page GUID or an empty string if no row is linked at all, for faster check.
	 */
	public String getResponsibleDepartmentRowPageGuid() throws RQLException {
		if (isResponsibleDepartmentLinked()) {
			return getResponsibleRow().getPageGuid();
		} else {
			return "";
		}
	}

	/**
	 * Returns the responsible row's source department number. Use only, if {@link #isResponsibleDepartmentLinked()} is true.
	 */
	public String getResponsibleDepartmentNumber() throws RQLException {
		return getResponsibleRow().getHeadline();
	}

	/**
	 * Returns the responsible row's source department number or given value if no responsible department row is linked.
	 */
	public String getResponsibleDepartmentNumberIfAvailable(String ifNotAvailable) throws RQLException {
		if (isResponsibleDepartmentLinked()) {
			return getResponsibleDepartmentNumber();
		}
		return ifNotAvailable;
	}

	/**
	 * Returns the responsible row's source department number and name or given value if page didn't have a responsible_pseudo_list or
	 * no responsible department row is linked.
	 */
	public String getResponsibleDepartmentNumberAndNameIfAvailable(String separator, String ifNotAvailable) throws RQLException {
		if (isResponsibleDepartmentLinked()) {
			return getResponsibleDepartmentNumber() + separator + getResponsibleDepartmentName();
		}
		return ifNotAvailable;
	}

	/**
	 * Returns the responsible row's source department name or given value if no responsible department row is linked.
	 */
	public String getResponsibleDepartmentNameIfAvailable(String ifNotAvailable) throws RQLException {
		if (isResponsibleDepartmentLinked()) {
			return getResponsibleDepartmentName();
		}
		return ifNotAvailable;
	}

	/**
	 * Returns the responsible row's source department name. Use only, if {@link #isResponsibleDepartmentLinked()} is true.
	 */
	public String getResponsibleDepartmentName() throws RQLException {
		return getResponsibleRow().getStandardFieldTextValue(getParameter("responsibleSourceDepartmentNameTmpltElemName"));
	}

	/**
	 * Returns the responsible row's mail subject statistic area (COM, SAL). Use only, if {@link #isResponsibleDepartmentLinked()} is
	 * true.
	 */
	public String getResponsibleStatisticArea() throws RQLException {
		return getResponsibleRow().getOptionListValue(getParameter("responsibleMailSubjectStatisticAreaTmpltElemName"));
	}

	/**
	 * Returns the responsible row's mail subject work area (business-administration, trade-management). Use only, if
	 * {@link #isResponsibleDepartmentLinked()} is true.
	 */
	public String getResponsibleWorkArea() throws RQLException {
		return getResponsibleRow().getStandardFieldTextValue(getParameter("responsibleMailWorkAreaTmpltElemName"));
	}

	/**
	 * Returns true, if the reponsible_id on this page is a group mailbox, otherwise false.
	 */
	public boolean isResponsibleIdAGroupMailbox() throws RQLException {
		return getOptionListValue(getParameter("responsibleIdTypeTmpltElemName")).equals(
				getParameter("responsibleIdTypeGroupMailboxValue"));
	}

	/**
	 * Returns the option list value of responsible_id_type; personal or nonPersonal.
	 */
	public String getResponsibleIdType() throws RQLException {
		return getOptionListValue(getParameter("responsibleIdTypeTmpltElemName"));
	}

	/**
	 * Returns true, if the reponsible_id on this page is configured as a person, otherwise false.
	 */
	public boolean isResponsibleIdAPerson() throws RQLException {
		return getOptionListValue(getParameter("responsibleIdTypeTmpltElemName")).equals(getParameter("responsibleIdTypePersonValue"));
	}

	/**
	 * Returns the responsible row's regional content coordinator's user id (lejafr, strutku). Use only, if
	 * {@link #isResponsibleDepartmentLinked()} is true.
	 */
	public String getResponsibleRccUserId() throws RQLException {
		return getResponsibleRow().getStandardFieldTextValue(getParameter("responsibleRccUserIdTmpltElemName"));
	}

	/**
	 * Returns the responsible row's regional content coordinator's user name (Frank Leja, Kurt Strutz). Use only, if
	 * {@link #isResponsibleDepartmentLinked()} is true.
	 */
	public String getResponsibleRccUserName() throws RQLException {
		return getResponsibleRow().getStandardFieldTextValue(getParameter("responsibleRccUserNameTmpltElemName"));
	}

	/**
	 * Returns the responsible row's backup regional content coordinator's user name (Frank Leja, Kurt Strutz). Use only, if
	 * {@link #isResponsibleDepartmentLinked()} is true.
	 */
	public String getResponsibleBackupRccUserName() throws RQLException {
		return getResponsibleRow().getStandardFieldTextValue(getParameter("responsibleRccBackupUserNameTmpltElemName"));
	}

	/**
	 * Returns the responsible row's backup regional content coordinator's user name (lejafr, strutku). Use only, if
	 * {@link #isResponsibleDepartmentLinked()} is true.
	 */
	public String getResponsibleBackupRccUserId() throws RQLException {
		return getResponsibleRow().getStandardFieldTextValue(getParameter("responsibleRccBackupUserIdTmpltElemName"));
	}

	/**
	 * Copy the responsibility data (responsible_id_type, responsible_id, responsible_name and link the department row) if available at
	 * source.
	 */
	public void copyResponsibilityFrom(ContentPage source) throws RQLException {
		// link same row
		if (source.isResponsibleDepartmentLinked()) {
			linkResponsibleDepartment(source.getResponsibleRow());
		}
		// try to copy values
		startSetElementValues();
		addSetOptionListValue(getParameter("responsibleIdTypeTmpltElemName"), source.getResponsibleIdType());
		if (!source.isResponsibleIdEmpty()) {
			addSetStandardFieldTextValue(getParameter("respIdTmpltElemName"), source.getResponsibleId());
		}
		if (!source.isResponsibleNameEmpty()) {
			addSetStandardFieldTextValue(getParameter("respNameTmpltElemName"), source.getResponsibleName());
		}
		endSetElementValues();
	}

	/**
	 * Connect the given responsible table row page to this page.
	 * <p>
	 * Removes another linked table row page before, if necessary.
	 * 
	 * @throws WrongTypeException,
	 *             if the given page is not an instance of responsible_table_row
	 */
	public void linkResponsibleDepartment(Page responsibleTableRowPg) throws RQLException {
		String templateName = getParameter("responsibleRowTemplateName");
		if (!responsibleTableRowPg.isBasedOnTemplate(templateName)) {
			throw new WrongTypeException("You try to link page with wrong template " + responsibleTableRowPg.getTemplateName()
					+ ". You can link only pages of content class " + templateName + ".");
		}

		// remove and connect
		List responsibleList = getResponsibleList();
		responsibleList.disconnectAllChilds();
		responsibleList.connectToExistingPage(responsibleTableRowPg, false);

		// force re-read
		deleteResponsibleRowCache();
	}

	/**
	 * Connect page with the given responsible table row page guid to this page.
	 * <p>
	 * Removes another linked table row page before, if necessary.
	 * 
	 * @throws WrongTypeException,
	 *             if the given page is not an instance of responsible_table_row
	 * @throws InvalidResponsibleDepartmentException
	 *             if given department number cannot be found in table
	 */
	public void linkResponsibleDepartmentByPageGuid(String responsibleTableRowPageGuid) throws RQLException {
		Page depPageOrNull = getResponsibleDeparmentRowByPageGuid(responsibleTableRowPageGuid);
		if (depPageOrNull == null) {
			throw new InvalidResponsibleDepartmentException("Responsible department row for given page guid "
					+ responsibleTableRowPageGuid + " cannot be found. Try with a valid number again.");
		}
		linkResponsibleDepartment(depPageOrNull);
	}

	/**
	 * Connect page with the given responsible table department number to this page.
	 * <p>
	 * Removes another linked table row page before, if necessary.
	 * 
	 * @throws WrongTypeException,
	 *             if the given page is not an instance of responsible_table_row
	 * @throws InvalidResponsibleDepartmentException
	 *             if given department number cannot be found in table
	 */
	public void linkResponsibleDepartmentByDepartmentNumber(String responsibleTableRowDepartmentNumber) throws RQLException {
		Page depPageOrNull = getResponsibleDeparmentRowByNumber(responsibleTableRowDepartmentNumber);
		if (depPageOrNull == null) {
			throw new InvalidResponsibleDepartmentException("Responsible department row for given number "
					+ responsibleTableRowDepartmentNumber + " cannot be found. Try with a valid number again.");
		}
		linkResponsibleDepartment(depPageOrNull);
	}

	/**
	 * Löscht den responsible row cache.
	 */
	private void deleteResponsibleRowCache() {
		responsibleRowCache = null;
	}

	/**
	 * Returns the first table row from the responsible_pseudo_list or null, if not page is linked.
	 * <p>
	 * No check anymore, if linked page is still in responsible departments table. Use {@link #isResponsibleDepartmentValid()}
	 */
	private Page getResponsibleRow() throws RQLException {
		if (responsibleRowCache == null) {
			responsibleRowCache = getResponsibleList().getFirstChildPage();
		}
		return responsibleRowCache;
	}

	/**
	 * Returns the pseudo list used to link the responsible row.
	 */
	private List getResponsibleList() throws RQLException {
		return getPage().getList(getParameter("responsiblePseudoListTmpltElemName"));
	}

	/**
	 * Returns true, if this page contains the pseudo list used to link the responsible row (responsible_pseudo_list).
	 */
	public boolean hasResponsibleList() throws RQLException {
		return contains(getParameter("responsiblePseudoListTmpltElemName"));
	}

	/**
	 * Returns the responsible department row page from the table for the given page GUID or null, if not found.
	 */
	private Page getResponsibleDeparmentRowByPageGuid(String pageGuid) throws RQLException {
		return getResponsibleDepartments().findByPageGuid(pageGuid);
	}

	/**
	 * Returns the responsible department row page from the table for the given department number or null, if not found.
	 */
	private Page getResponsibleDeparmentRowByNumber(String departmentNumber) throws RQLException {
		return getResponsibleDepartments().findByHeadline(departmentNumber);
	}

	/**
	 * Returns the responsibility departments pages (cached within project).
	 */
	public PageArrayList getResponsibleDepartments() throws RQLException {
		Project project = getProject();
		String responsiblityPagesCacheId = getResponsiblityPagesCacheId();
		PageArrayList rowPages = project.getPages(responsiblityPagesCacheId);
		if (rowPages == null) {
			// get row pages
			com.hlcl.rql.as.List departmentsList = getDepartmentsTableList();
			rowPages = departmentsList.getChildPages();
			// cache within project
			project.putPages(responsiblityPagesCacheId, rowPages);
		}
		return rowPages;
	}

	/**
	 * Returns the list MultiLink element from the central responsible departments table.
	 */
	private com.hlcl.rql.as.List getDepartmentsTableList() throws RQLException {
		Page tablePg = getProject().getPageById(getParameter("responsibleTablePageId"));
		com.hlcl.rql.as.List departmentsList = tablePg.getList(getParameter("departmentsListTmpltElemName"));
		return departmentsList;
	}

	/**
	 * Returns the project's pages cache ID for the responsibility table.
	 */
	private String getResponsiblityPagesCacheId() {
		return this.getClass().getName() + "responsibilityPages";
	}

	/**
	 * Sets the page updated to given date.
	 */
	public void setUpdated(ReddotDate date) throws RQLException {
		getPage().setStandardFieldDateValue(getParameter("pageUpdatedTmpltElemName"), date);
	}

	/**
	 * Sets the page updated to the given date, if wrapped page has element page_updated. Otherwise do nothing.
	 */
	public void setUpdatedIfPageContainsElement(Date timestamp) throws RQLException {
		if (containsPageUpdated()) {
			setUpdated(timestamp);
		}
	}

	/**
	 * Gets the responsible user name if template element available
	 * 
	 * @param ifNotAvailable
	 * @throws RQLException
	 */
	public String getResponsibleUserNameIfAvailable(String ifNotAvailable) throws RQLException {
		Page page = getPage();
		String templateElementName = getParameter("respNameTmpltElemName");
		if (page.contains(templateElementName)) {
			StandardFieldTextElement rdText = page.getStandardFieldTextElement(templateElementName);
			if (rdText != null) {
				return rdText.getText();
			}
		}
		return ifNotAvailable;
	}

	/**
	 * Gets the responsible user id if template element available
	 * 
	 * @param ifNotAvailable
	 * @throws RQLException
	 */
	public String getResponsibleUserIdIfAvailable(String ifNotAvailable) throws RQLException {
		Page page = getPage();
		String templateElementName = getParameter("respIdTmpltElemName");
		if (page.contains(templateElementName)) {
			StandardFieldTextElement rdText = page.getStandardFieldTextElement(templateElementName);
			if (rdText != null) {
				return rdText.getText();
			}
		}
		return ifNotAvailable;
	}

	/**
	 * Returns the responsible user id or group mailbox id (element responsible_id).
	 * 
	 * @see #isResponsibleIdAGroupMailbox()
	 * @see #isResponsibleIdAPerson()
	 */
	public String getResponsibleId() throws RQLException {
		return getStandardFieldTextValue(getParameter("respIdTmpltElemName"));
	}

	/**
	 * Returns the name of the responsible user or the title of group mailbox name shown as blue link text in page footer.
	 */
	public String getResponsibleName() throws RQLException {
		return getStandardFieldTextValue(getParameter("respNameTmpltElemName"));
	}

	/**
	 * Returns the content requester user id.
	 */
	public String getRequesterUserId() throws RQLException {
		return getStandardFieldTextValue(getParameter("requesterUserIdTmpltElemName"));
	}

	/**
	 * Returns the content requester user id in lower case.
	 */
	public String getRequesterUserIdLowerCase() throws RQLException {
		return getStandardFieldTextValue(getParameter("requesterUserIdTmpltElemName")).toLowerCase();
	}

	/**
	 * Returns the content requester user id or ifNotAvailable, if this page hasn't this field or is empty
	 */
	public String getRequesterUserId(String ifNotAvailable) throws RQLException {
		if (contains(getParameter("respIdTmpltElemName"))) {
			return getRequesterUserId();
		}
		return ifNotAvailable;
	}

	/**
	 * Liefert true, falls diese content page ein page last updated element (im Footer) besitzt.
	 */
	public boolean containsPageUpdated() throws RQLException {
		return contains(getParameter("pageUpdatedTmpltElemName"));
	}

	/**
	 * Liefert den im Feld responsible_name vorhandenen Namen des Verantwortlichen oder null, falls das Feld leer ist.
	 */
	public String getResponsibleNameDisplayName() throws RQLException {
		// check if set
		if (!isResponsibleNameFilled()) {
			return null;
		}
		// use regex
		Pattern pattern = Pattern.compile(getParameter("responsibleNameDisplayNameRegEx"));
		String responsibleName = getResponsibleName();
		Matcher matcher = pattern.matcher(responsibleName);
		if (matcher.find() && matcher.groupCount() == 1) {
			return matcher.group(1);
		}

		// default
		return responsibleName;
	}

	/**
	 * Returns true, if this page has a filled (=non empty) field responsible name.
	 */
	public boolean isResponsibleNameFilled() throws RQLException {
		return !isResponsibleNameEmpty();
	}

	/**
	 * Converts the given responsible id's oldValue into a new if needed (check is case insensitive). Otherwise return the given value.
	 * <p>
	 * Is used to replace e.g. betriebsrat.hamburg with betrham
	 */
	private String convertResponsibleId(String oldValue) throws RQLException {
		String valueOrNull = getResponsibleIdConversions().get(oldValue.toLowerCase());
		return valueOrNull == null ? oldValue : valueOrNull;
	}

	/**
	 * Map converting responsible ids (mostly group mailboxes) into one value
	 */
	private Map<String, String> getResponsibleIdConversions() throws RQLException {
		if (responsibleIdConversionsMapCache == null) {
			responsibleIdConversionsMapCache = StringHelper.convertToMap(getParameter("responsibleIdValueConversions"), ",", "=");
		}
		return responsibleIdConversionsMapCache;
	}

	/**
	 * Maps known responsible ids (mainly group mailboxes) to department numbers
	 */
	private Map<String, String> getResponsibleId2DepartmentNumberMap() throws RQLException {
		if (responsibleId2DepartmentNumberMapCache == null) {
			responsibleId2DepartmentNumberMapCache = StringHelper.convertToMap(getParameter("responsibleId2DepartmentNumberMap"), ",",
					"=");
		}
		return responsibleId2DepartmentNumberMapCache;
	}

	/**
	 * Try to convert page into new responsibility data as much as possible.
	 * <P>
	 * Returns true, it at least one value has changed (link department row not included).
	 */
	public boolean convertResponsibility() throws RQLException {
		// use combined update approach
		startSetElementValues();

		// determine department number from responsible name
		String depNoToLinkOrNull = getResponsibleNameDepartmentNumber();

		// convert responsible id and type
		String respIdTmpltElemName = getParameter("respIdTmpltElemName");
		StandardFieldTextElement respIdElem = getStandardFieldTextElement(respIdTmpltElemName);
		if (!respIdElem.isEmpty()) {
			String newRespId = respIdElem.getText();

			// 1. remove invalid parts (like @hlag.com)
			String[] invalidSuffixes = StringHelper.split(getParameter("responsibleIdSuffixes2delete"), ",");
			newRespId = StringHelper.removeSuffixes(newRespId, invalidSuffixes, true); // ignore case

			// 2. replace group mailboxes with group mailbox ID (case insensitive)
			newRespId = convertResponsibleId(newRespId);

			// 3. update value
			if (!respIdElem.getText().equals(newRespId)) {
				addSetStandardFieldTextValue(respIdTmpltElemName, newRespId);
			}

			// set responsible type to nonPersonal for known group mailboxes
			java.util.List<String> knownGroupMailboxIds = getKnownGroupMailboxIds();
			if (knownGroupMailboxIds.contains(newRespId.toLowerCase())) {
				// change type to nonPersonal
				addSetOptionListValue(getParameter("responsibleIdTypeTmpltElemName"),
						getParameter("responsibleIdTypeGroupMailboxValue"));
			}

			// determine department number for group mailboxes
			Map<String, String> responsibleId2DepartmentNumberMap = getResponsibleId2DepartmentNumberMap();
			String mapDepNo = responsibleId2DepartmentNumberMap.get(newRespId.toLowerCase());
			if (mapDepNo != null) {
				depNoToLinkOrNull = mapDepNo;
			}
		}

		// try to link the department row for the determined department number, if no row linked
		boolean linked = false;
		if (!isResponsibleDepartmentLinked() && depNoToLinkOrNull != null) {
			Page newRowPg = getResponsibleDepartments().findByHeadline(depNoToLinkOrNull);
			if (newRowPg != null) {
				linkResponsibleDepartment(newRowPg);
				linked = true;
			}
		}

		// try to remove department text from responsible_name field, if a department was linked
		if (linked) {
			String separator = " - ";
			String respNameTmpltElemName = getParameter("respNameTmpltElemName");
			StandardFieldTextElement respNameElem = getStandardFieldTextElement(respNameTmpltElemName);
			if (!respNameElem.isEmpty()) {
				String respName = respNameElem.getText();
				int pos = respName.indexOf(separator);
				if (pos >= 0) {
					String newName = respName.substring(pos + separator.length());
					addSetStandardFieldTextValue(respNameTmpltElemName, newName);
				}
			}
		}

		// finish element updates
		int howManyChanged = endNumberOfSetElements();
		endSetElementValues();

		return howManyChanged != 0;
	}

	/**
	 * Returns the known group mailbox IDs.
	 */
	public java.util.List<String> getKnownGroupMailboxIds() throws RQLException {
		String knownGroupMailboxIds = getParameter("knownGroupMailboxIds");
		return StringHelper.split(knownGroupMailboxIds, ',');
	}

	/**
	 * Returns true, if this page's field responsible name is empty.
	 */
	public boolean isResponsibleNameEmpty() throws RQLException {
		return getPage().isStandardFieldTextEmpty(getParameter("respNameTmpltElemName"));
	}

	/**
	 * Returns true, if this page's field responsible id is empty.
	 */
	public boolean isResponsibleIdEmpty() throws RQLException {
		return getPage().isStandardFieldTextEmpty(getParameter("respIdTmpltElemName"));
	}

	/**
	 * Liefert die im Feld responsible_name vorhandene Abteilungsnummer oder null, falls nicht identifizierbar.
	 */
	public String getResponsibleNameDepartmentNumber() throws RQLException {
		// check if set
		if (isResponsibleNameFilled()) {
			// use regex
			Pattern pattern = Pattern.compile(getParameter("responsibleNameDepartmentNumberRegEx"));
			Matcher matcher = pattern.matcher(getResponsibleName());
			if (matcher.find() && matcher.groupCount() == 1) {
				return matcher.group(1);
			}
		}
		// default
		return null;
	}

	/**
	 * Liefert die Vorgängerseite (über MainLink) zurück, die das Feld responsible_id besitzt, um von dort die Werte kopieren zu
	 * können.
	 */
	public ContentPage getPredecessorPageContainingResponsibility() throws RQLException {
		return new ContentPage(super.getPredecessorPageContainingElement(getParameter("respIdTmpltElemName")));
	}

	/**
	 * Liefert true, falls die im Feld responsible_name vorhandene Abteilungsnummer identifizierbar ist, sonst false.
	 */
	public boolean hasResponsibleNameDepartmentNumber() throws RQLException {
		return getResponsibleNameDepartmentNumber() != null;
	}

	/**
	 * Liefert true, falls die im Feld responsible_name vorhandene Abteilungsnummer identifizierbar ist und diese in der Tabelle
	 * vorkommt, sonst false.
	 */
	public boolean isResponsibleNameDepartmentNumberValid() throws RQLException {
		String responsibleNameDepartmentNumber = getResponsibleNameDepartmentNumber();
		if (responsibleNameDepartmentNumber != null) {
			return getResponsibleDepartments().findByHeadlineStartsWith(responsibleNameDepartmentNumber) != null;
		}
		return false;
	}

	/**
	 * Gets the updated date if template element available
	 * 
	 * @param ifNotAvailable
	 * @throws RQLException
	 */
	public String getUpdatedOnIfAvailable(String ifNotAvailable) throws RQLException {
		Page page = getPage();
		String templateElementName = getParameter("pageUpdatedTmpltElemName");
		if (page.contains(templateElementName)) {
			ReddotDate rdDate = page.getStandardFieldDateValue(templateElementName);
			if (rdDate != null) {
				return rdDate.getAsyyyyMMdd();
			}
		}
		return ifNotAvailable;
	}

	/**
	 * Liefert den Namen den Berechtigungspaketes (typ=page) ohne den Prefix work_area_rights_, falls vorhanden.
	 * <p>
	 * Liefert einen leeren String, falls diese Seite keine Bereichtigungspaket hat.
	 */
	public String getAuthorizationPackageNameWithoutPrefix() throws RQLException {
		return StringHelper.removePrefix(getAuthorizationPackageName(), getParameter("authorizationPackagePrefix"));
	}

	/**
	 * Liefert true, falls diese Seite freigegeben ist.
	 */
	public boolean isInStateReleased() throws RQLException {
		return getPage().isInStateReleased();
	}

	/**
	 * Liefert true, falls diese Seite die im HIP typischen Elementes das page footers hat.
	 * <p>
	 * Es wird auf vorhandensein des Feldes responsible_id geprüft.
	 */
	public boolean hasPageFooterElements() throws RQLException {
		return contains(getParameter("respIdTmpltElemName"));
	}

	/**
	 * Prüft das feld responsible_id in Abhängigkeit vom typ personal or nonPersonal.
	 */
	public String checkResponsibleId() throws RQLException {
		String id = getResponsibleId().trim();
		int length = id.length();
		if (length == 0) {
			return "missing";
		}
		if (isResponsibleIdAPerson()) {
			if (length < 4) {
				return "too short";
			}
			if (length > 7) {
				return "too long";
			}
			// use regex
			Pattern pattern = Pattern.compile(getParameter("checkUserIdRegEx"));
			Matcher matcher = pattern.matcher(id);
			if (!matcher.find()) {
				return "invalid chars";
			}
		} else if (isResponsibleIdAGroupMailbox()) {
			if (length < 4) {
				return "too short";
			}
			// use regex
			Pattern pattern = Pattern.compile(getParameter("checkGroupMailboxIdRegEx"));
			Matcher matcher = pattern.matcher(id);
			if (!matcher.find()) {
				return "invalid chars";
			}
		}
		// otherwise
		return "valid";
	}

	/**
	 * Prüft das feld responsible_name.
	 */
	public String checkResponsibleName() throws RQLException {
		String id = getResponsibleName().trim();
		int length = id.length();
		if (length == 0) {
			return "missing";
		}
		String responsibleNameDepartmentNumber = getResponsibleNameDepartmentNumber();
		if (responsibleNameDepartmentNumber != null) {
			return "invalid depNo=" + responsibleNameDepartmentNumber;
		}
		// otherwise
		return "valid";
	}

	/**
	 * Creates a new recycling table block on the blocks container of this content page.
	 * 
	 * @throws RQLException
	 */
	public RecyclingTableBlock createRecyclingTableBlock() throws RQLException {
		Container ctr = getBlocksContainer();
		String templateName = getParameter("recyclingBlockTmpltName");
		Template template = getTemplateByName(getParameter("contentTmpltFldrName"), templateName);
		return new RecyclingTableBlock(ctr.createAndConnectPage(template, getHeadline() + " " + templateName, true));
	}

	/**
	 * Returns the blocks container of this content page.
	 */
	private Container getBlocksContainer() throws RQLException {
		return getPage().getContainer(getParameter("blocksTmpltElemName"));
	}

	/**
	 * Returns the first recycling table block from the blocks container or null, if it doesn't have a recycling table block page at
	 * all.
	 */
	public RecyclingTableBlock getRecyclingTableBlock() throws RQLException {
		Container bottomCtr = getBlocksContainer();
		PageArrayList children = bottomCtr.getChildPagesForTemplate(getParameter("recyclingBlockTmpltName"));
		// no one found
		if (children.size() == 0) {
			return null;
		}
		// wrap
		return new RecyclingTableBlock(children.first());
	}

	/**
	 * Returns the recycling table block from the blocks container or creates a new one, if this content page didn't have one before.
	 */
	public RecyclingTableBlock getOrCreateRecyclingTableBlock() throws RQLException {
		RecyclingTableBlock blockPg = getRecyclingTableBlock();
		// no one, create new
		if (blockPg == null) {
			blockPg = createRecyclingTableBlock();
		}
		return blockPg;
	}

	/**
	 * Returns the first enhanced list table block from the blocks container or null, if it doesn't have such a block page at all.
	 */
	public EnhancedListTableBlock getEnhancedListTableBlock() throws RQLException {
		Container bottomCtr = getBlocksContainer();
		PageArrayList children = bottomCtr.getChildPagesForTemplate(getParameter("enhancedListTableBlockTmpltName"));
		// no one found
		if (children.size() == 0) {
			return null;
		}
		// wrap
		return new EnhancedListTableBlock(children.first());
	}

	/**
	 * Creates a new recycling table block on the blocks container of this content page.
	 * 
	 * @throws RQLException
	 */
	public EnhancedListTableBlock createEnhancedListTableBlock() throws RQLException {
		Container ctr = getBlocksContainer();
		String templateName = getParameter("enhancedListTableBlockTmpltName");
		Template template = getTemplateByName(getParameter("contentTmpltFldrName"), templateName);
		return new EnhancedListTableBlock(ctr.createAndConnectPage(template, getHeadline() + " " + templateName, true));
	}

	/**
	 * Returns the first enhanced list table block from the blocks container or creates a new one, if this content page didn't have one
	 * before.
	 */
	public EnhancedListTableBlock getOrCreateEnhancedListTableBlock() throws RQLException {
		EnhancedListTableBlock blockPg = getEnhancedListTableBlock();
		// no one, create new
		if (blockPg == null) {
			blockPg = createEnhancedListTableBlock();
		}
		return blockPg;
	}

	/**
	 * Returns for all block pages of template filterTemplateName within this content page all physical parent pages (including this).
	 * Block parent pages which aren't a physical pages are investigated further recursively. A tree upwards ending with a block page
	 * will not be returned.
	 */
	public Set<PhysicalPage> collectBlockPagesPhysicalParents(String filterTemplateName) throws RQLException {
		Set<PhysicalPage> result = new HashSet<PhysicalPage>();
		PageArrayList blockPages = getBlocksContainer().getChildPagesForTemplate(filterTemplateName);
		// check parent of all block pages
		for (int i = 0; i < blockPages.size(); i++) {
			Page blockPage = blockPages.getPage(i);
			collectPhysicalParentsPrim(result, blockPage);
		}
		return result;
	}

	/**
	 * Returns all block pages of this content page.
	 */
	public PageArrayList getBlockPages() throws RQLException {
		return getBlocksContainer().getChildPages();
	}

	/**
	 * Returns the template names separated by given separator string of all block pages of this content page.
	 */
	public String getBlockPagesTemplateNames(String separator) throws RQLException {
		return getBlocksContainer().getChildPagesTemplateNames(separator);
	}

	/**
	 * Returns for all block pages of this content page all physical parent pages (including this). Block parent pages which aren't a
	 * physical pages are investigated further recursively. A tree upwards ending with a block page will not be returned.
	 */
	public Set<PhysicalPage> collectBlockPagesPhysicalParents() throws RQLException {
		Set<PhysicalPage> result = new HashSet<PhysicalPage>();
		PageArrayList blockPages = getBlocksContainer().getChildPages();
		// check parent of all block pages
		for (int i = 0; i < blockPages.size(); i++) {
			Page blockPage = blockPages.getPage(i);
			collectPhysicalParentsPrim(result, blockPage);
		}
		return result;
	}

	/**
	 * Collects all block page parent physical pages into result. Calls itself recursively.
	 * 
	 * @throws RQLException
	 */
	private void collectPhysicalParentsPrim(Set<PhysicalPage> result, Page blockPage) throws RQLException {
		PageArrayList blockParents = blockPage.getParentPages();
		for (int j = 0; j < blockParents.size(); j++) {
			PhysicalPage physPg = new PhysicalPage(blockParents.getPage(j));
			// include only physical parent pages
			if (physPg.isPhysicalPage()) {
				result.add(physPg);
			} else {
				// parent is block page, check parents
				collectPhysicalParentsPrim(result, physPg.getPage());
			}
		}
	}
}
