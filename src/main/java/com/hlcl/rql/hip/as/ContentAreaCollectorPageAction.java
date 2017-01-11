/*
 * Created on 20.05.2005
 *
 */
package com.hlcl.rql.hip.as;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StringHelper;
import com.hlcl.rql.util.as.PageAction;

/**
 * @author lejafr
 * 
 * Diese Klasse sammelt in einer Datenbank für eine gegebene SeitenID die zugehörigen content areas (Treenamen). z.B. sales, company, local
 */
public class ContentAreaCollectorPageAction extends PageAction {

	// constants
	private final String SEPARATOR = ";";

	private Connection connection;
	private String tableName;
	private String contentAreaName;
	private String[] skipTemplateNameSuffixes;

	// caches
	private Page invokedPage;
	private PreparedStatement insertStmt;
	private PreparedStatement updateStmt;
	private ResultSet invokedPageRs; // cache
	private PreparedStatement selectPageStmt;
	private PreparedStatement countPageStmt;

	private ResultSet selectAllRs;

	/**
	 * ContentAreaCollectorPageAction constructor
	 */
	public ContentAreaCollectorPageAction(Connection connection, String tableName, String[] skipTemplateNameSuffixes, String initialContentAreaName)
			throws RQLException {
		super();

		this.connection = connection;
		this.tableName = tableName;
		this.skipTemplateNameSuffixes = skipTemplateNameSuffixes;
		this.contentAreaName = initialContentAreaName;

		// initialize
		createTable();
	}

	/**
	 * ContentAreaCollectorPageAction constructor Attention: before first usage of {@link #invoke(Page)} you have to call
	 * {@link #setContentAreaName(String)}
	 */
	public ContentAreaCollectorPageAction(Connection connection, String tableName, String[] skipTemplateNameSuffixes) throws RQLException {
		super();

		this.connection = connection;
		this.tableName = tableName;
		this.skipTemplateNameSuffixes = skipTemplateNameSuffixes;
		this.contentAreaName = "unknown";

		// initialize
		createTable();
	}

	/**
	 * Returns true, if invoked page is stored in database table already. Checked by page GUID.
	 */
	private boolean isInvokedPageContainedInTable() throws RQLException {
		try {
			return getCountResultSet(invokedPage.getPageGuid()).getInt(1) > 0;
		} catch (SQLException ex) {
			throw new RQLException("Error checking if page with given page id " + invokedPage.getPageId() + " is contained in database table", ex);
		}
	}

	/**
	 * Creates the table within the database. Content areas are separated by ;
	 */
	private void createTable() throws RQLException {
		String sql = null;
		try {
			Statement stmt = connection.createStatement();
			sql = "CREATE TABLE " + tableName
					+ " (pageGuid varchar primary key, pageId varchar, headline varchar, template varchar, contentAreas varchar)";
			stmt.executeUpdate(sql);
		} catch (SQLException ex) {
			throw new RQLException("Error in sql " + sql, ex);
		}
	}

	/**
	 * Liefert den Namen der aktuellen zu klassifierenden content area.
	 */
	public String getContentAreaName() {
		return contentAreaName;
	}

	/**
	 * Returns the list of stored content areas for the given page; separated by ;
	 */
	private String getInvokedPageContentAreas() throws RQLException {
		try {
			return getInvokedPageResultSet().getString(5);
		} catch (SQLException ex) {
			throw new RQLException("Error retrieving the content areas for page with ID " + invokedPage.getPageId(), ex);
		}
	}

	/**
	 * Returns the prepared statement to count(*) for current page.
	 */
	private PreparedStatement getCountPageStatement() throws RQLException {
		if (countPageStmt == null) {
			String sql = null;
			try {
				sql = "SELECT count(*) from " + tableName + " where pageGuid=?";
				countPageStmt = connection.prepareStatement(sql);
			} catch (SQLException ex) {
				throw new RQLException("Error in sql " + sql, ex);
			}
		}
		return countPageStmt;
	}

	/**
	 * Returns the result set with the corresponding page as current row.
	 */
	private ResultSet getCountResultSet(String pageGuid) throws RQLException {
		PreparedStatement countStmt = getCountPageStatement();
		try {
			countStmt.setString(1, pageGuid);
			ResultSet countPageRs = countStmt.executeQuery();
			// move to the only row
			countPageRs.next();
			return countPageRs;

		} catch (SQLException ex) {
			throw new RQLException("Error returning the count(*) page result set from database table", ex);
		}
	}

	/**
	 * Returns the contentAreas for the current row.
	 */
	public String getCurrentPageContentAreas() throws RQLException {
		try {
			return getSelectAllRs().getString(5);
		} catch (SQLException ex) {
			throw new RQLException("Error in getting the content areas", ex);
		}
	}

	/**
	 * Returns the page GUID for the current row.
	 */
	public String getCurrentPageGuid() throws RQLException {
		try {
			return getSelectAllRs().getString(1);
		} catch (SQLException ex) {
			throw new RQLException("Error in getting the page GUID", ex);
		}
	}

	/**
	 * Returns the page headline for the current row.
	 */
	public String getCurrentPageHeadline() throws RQLException {
		try {
			return getSelectAllRs().getString(3);
		} catch (SQLException ex) {
			throw new RQLException("Error in getting the headline", ex);
		}
	}

	/**
	 * Returns the page ID for the current row.
	 */
	public String getCurrentPageId() throws RQLException {
		try {
			return getSelectAllRs().getString(2);
		} catch (SQLException ex) {
			throw new RQLException("Error in getting the page ID", ex);
		}
	}

	/**
	 * Returns the prepared insert statement.
	 */
	private PreparedStatement getInsertStatement() throws RQLException {
		if (insertStmt == null) {
			String sql = null;
			try {
				sql = "INSERT INTO " + tableName + " values(?, ?, ?, ?, ?)";
				insertStmt = connection.prepareStatement(sql);
			} catch (SQLException ex) {
				throw new RQLException("Error in sql " + sql, ex);
			}
		}
		return insertStmt;
	}

	/**
	 * Returns the result set with the corresponding page as current row.
	 */
	private ResultSet getInvokedPageResultSet() throws RQLException {
		if (invokedPageRs == null) {
			PreparedStatement readStmt = getSelectPageStatement();
			try {
				readStmt.setString(1, invokedPage.getPageGuid());
				invokedPageRs = readStmt.executeQuery();
				// move to the only row
				invokedPageRs.next();
			} catch (SQLException ex) {
				throw new RQLException("Error returning the page result set from database table", ex);
			}
		}
		return invokedPageRs;
	}

	/**
	 * Returns the prepared statement to return all page information from the database table.
	 */
	private ResultSet getSelectAllRs() throws RQLException {
		if (selectAllRs == null) {
			String sql = null;
			try {
				sql = "SELECT pageGuid, pageId, headline, template, contentAreas from " + tableName;
				selectAllRs = connection.createStatement().executeQuery(sql);
			} catch (SQLException ex) {
				throw new RQLException("Error in sql " + sql, ex);
			}
		}
		return selectAllRs;
	}

	/**
	 * Returns the prepared statement to search for a page by page id.
	 */
	private PreparedStatement getSelectPageStatement() throws RQLException {
		if (selectPageStmt == null) {
			String sql = null;
			try {
				sql = "SELECT pageGuid, pageId, headline, template, contentAreas from " + tableName + " where pageGuid=?";
				selectPageStmt = connection.prepareStatement(sql);
			} catch (SQLException ex) {
				throw new RQLException("Error in sql " + sql, ex);
			}
		}
		return selectPageStmt;
	}

	/**
	 * Returns the prepared update statement. 2 parameters: 1=contentAreas, 2=pageGuid
	 */
	private PreparedStatement getUpdateStatement() throws RQLException {
		if (updateStmt == null) {
			String sql = null;
			try {
				sql = "UPDATE " + tableName + " set contentAreas = ? WHERE pageGuid = ?";
				updateStmt = connection.prepareStatement(sql);
			} catch (SQLException ex) {
				throw new RQLException("Error in prepare update sql statement" + sql, ex);
			}
		}
		return updateStmt;
	}

	/**
	 * Adds currently invoked page to the database initially. Make sure it is not in set already.
	 */
	private void insertInvokedPage() throws RQLException {
		PreparedStatement insertStmt = getInsertStatement();
		try {
			insertStmt.setString(1, invokedPage.getPageGuid());
			insertStmt.setString(2, invokedPage.getPageId());
			insertStmt.setString(3, invokedPage.getHeadline());
			insertStmt.setString(5, contentAreaName);
			insertStmt.executeUpdate();
		} catch (SQLException ex) {
			throw new RQLException("Error on insert page record for " + invokedPage.getHeadlineAndId() + " in database", ex);
		}
	}

	/**
	 * Klassifiziert die gegebene Seite zu der aktuellen content area.
	 * 
	 * @see com.hlcl.rql.util.as.PageAction#invoke
	 */
	public void invoke(Page page) throws RQLException {
		// skip all *_fragment pages
		if (!StringHelper.endsWithOneOf(page.getTemplateName(), skipTemplateNameSuffixes)) {
			// remember for reference
			setInvokedPage(page);

			// check if page is already classified
			if (isInvokedPageContainedInTable()) {
				String contentAreas = getInvokedPageContentAreas();
				contentAreas += SEPARATOR + contentAreaName;
				updateInvokedPageContentAreas(contentAreas);
			} else {
				// not classified before; adds the given page under current content area
				insertInvokedPage();
			}
		}
	}

	/**
	 * Merkt sich die Seite, für die diese PageAction aufgerufen wurde.
	 */
	private void setInvokedPage(Page page) {
		invokedPage = page;
		
		// reset caches
		invokedPageRs = null;
	}

	/**
	 * Forwards to next row of result set to get all information out of the set. Returns true, if next row is available.
	 * <p>
	 * Did not sort the pages in any kind.
	 */
	public boolean nextPage() throws RQLException {
		try {
			return getSelectAllRs().next();
		} catch (SQLException ex) {
			throw new RQLException("Error in next()", ex);
		}
	}

	/**
	 * Ändert die content area für folgende {@link #invoke(Page)} Aufrufe.
	 */
	public void setContentAreaName(String contentAreaName) {
		this.contentAreaName = contentAreaName;
	}

	/**
	 * Returns the number of classified pages (count(*) of database table.
	 */
	public int size() throws RQLException {
		String sql = null;
		int size = 0;
		try {
			sql = "SELECT count(*) from " + tableName;
			ResultSet rs = connection.createStatement().executeQuery(sql);
			rs.next();
			size = rs.getInt(1);
		} catch (SQLException ex) {
			throw new RQLException("Error in sql " + sql, ex);
		}
		return size;
	}

	/**
	 * Update the result set for the current row (the page the actions runs for) with the given content areas.
	 */
	private void updateInvokedPageContentAreas(String contentAreas) throws RQLException {
		try {
			PreparedStatement updateStmt = getUpdateStatement();
			updateStmt.setString(1, contentAreas);
			updateStmt.setString(2, invokedPage.getPageGuid());
			updateStmt.executeUpdate();
		} catch (SQLException ex) {
			throw new RQLException("Error on update content areas for page", ex);
		}
	}

}
