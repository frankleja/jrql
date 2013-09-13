package com.hlcl.rql.util.as;

import java.sql.*;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * This class maintain a big set of page information. It uses a database to collect paga id and headlines. 
 */
public class PageInfoSet {

	private Connection connection;
	private PreparedStatement insertStmt; // cache 

	private String name;
	private ResultSet selectAllRs; // cache 
	private PreparedStatement selectPageStmt; // cache 
	private int SORT_MODE_TEMPLATE_NAME = 1;
	private int SORT_MODE_UNSORTED = 0;

	/**
	 * Construct a page info set. 
	 * @param connection the database connection
	 * @param name	the name of the database table used to collect the page information
	 */
	public PageInfoSet(Connection connection, String name) throws RQLException {
		super();

		this.connection = connection;
		this.name = name;
		
		// initialize
		createTable();
	}

	/**
	 * Adds this page to the set. Does nothing, if the page is already in the set.
	 */
	public void add(Page page) throws RQLException {
		if (!containsPage(page)) {
			addWithoutCheck(page);
		}
	}

	/**
	 * Adds this page to the set. Make sure it is not in set already. 
	 */
	public void addWithoutCheck(Page page) throws RQLException {
		PreparedStatement insertStmt = getInsertStatement();
		try {
			insertStmt.setString(1, page.getPageId());
			insertStmt.setString(2, page.getHeadline());
			insertStmt.setString(3, page.getTemplateName());
			insertStmt.setString(4, page.getCreatedByUserName());
			insertStmt.setString(5, page.getCreatedOn().getAsyyyyMMdd());
			insertStmt.setString(6, page.getPageGuid());
			insertStmt.executeUpdate();
		} catch (SQLException ex) {
			throw new RQLException("Error replacing values in insert statement", ex);
		}
	}

	/**
	 * Returns true, if page is stored in set already. Checked by page ID.
	 */
	public boolean containsPage(Page page) throws RQLException {
		return containsPage(page.getPageId());
	}

	/**
	 * Returns true, if page id is stored in set already.
	 */
	public boolean containsPage(String pageId) throws RQLException {
		PreparedStatement checkStmt = getSelectPageStatement();
		try {
			checkStmt.setString(1, pageId);
			ResultSet rs = checkStmt.executeQuery();
			// move to the only row
			rs.next();
			return rs.getInt(1) > 0;
	
		} catch (SQLException ex) {
			throw new RQLException("Error checking if page exists in set already", ex);
		}
	}

	/**
	 * Creates the table within the database. 
	 */
	private void createTable() throws RQLException {
		String sql = null;
		try {
			Statement stmt = connection.createStatement();
			sql = "CREATE TABLE " + name + " (pageId varchar primary key, headline varchar, template varchar, createdBy varchar, createdOn varchar, pageGuid varchar)";
			stmt.executeUpdate(sql);
		} catch (SQLException ex) {
			throw new RQLException("Error in sql " + sql, ex);
		}
	}

	/**
	 * Returns the page created by user name for the current row.
	 */
	public String getCurrentPageCreatedByUserName() throws RQLException {
		try {
			return getSelectAllRs().getString(4);
		} catch (SQLException ex) {
			throw new RQLException("Error in getting the create by user name", ex);
		}
	}

	/**
	 * Returns the page created on date (in yyyymmdd) for the current row.
	 */
	public String getCurrentPageCreatedOn() throws RQLException {
		try {
			return getSelectAllRs().getString(5);
		} catch (SQLException ex) {
			throw new RQLException("Error in getting the created on date", ex);
		}
	}

	/**
	 * Returns the page GUID for the current row.
	 */
	public String getCurrentPageGuid() throws RQLException {
		try {
			return getSelectAllRs().getString(6);
		} catch (SQLException ex) {
			throw new RQLException("Error in getting the page GUID", ex);
		}
	}
	/**
	 * Returns the page headline for the current row.
	 */
	public String getCurrentPageHeadline() throws RQLException {
		try {
			return getSelectAllRs().getString(2);
		} catch (SQLException ex) {
			throw new RQLException("Error in getting the headline", ex);
		}
	}


	/**
	 * Returns the page ID for the current row.
	 */
	public String getCurrentPageId() throws RQLException {
		try {
			return getSelectAllRs().getString(1);
		} catch (SQLException ex) {
			throw new RQLException("Error in getting the page ID", ex);
		}
	}

	/**
	 * Returns the page template name for the current row.
	 */
	public String getCurrentPageTemplateName() throws RQLException {
		try {
			return getSelectAllRs().getString(3);
		} catch (SQLException ex) {
			throw new RQLException("Error in getting the template name", ex);
		}
	}

	/**
	 * Returns the prepared insert statement.
	 */
	private PreparedStatement getInsertStatement() throws RQLException {
		if (insertStmt == null) {
			String sql = null;
			try {
				sql = "INSERT INTO " + name + " values(?, ?, ?, ?, ?, ?)";
				insertStmt = connection.prepareStatement(sql);
			} catch (SQLException ex) {
				throw new RQLException("Error in sql " + sql, ex);
			}
		}
		return insertStmt;
	}

	/**
	 * Returns the prepared statement to return all page information from the set, unsorted.
	 */
	private ResultSet getSelectAllRs() throws RQLException {
		return getSelectAllRs(SORT_MODE_UNSORTED);
	}

	/**
	 * Returns the prepared statement to return all page information from the set, sorting depends.
	 */
	private ResultSet getSelectAllRs(int sortMode) throws RQLException {
		if (selectAllRs == null) {
			String sql = null;
			try {
				sql = "SELECT pageId, headline, template, createdBy, createdOn, pageGuid from " + name;
				if (sortMode == SORT_MODE_TEMPLATE_NAME) {
					sql += " order by template";
				}
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
				sql = "SELECT count(*) from " + name + " where pageId=?";
				selectPageStmt = connection.prepareStatement(sql);
			} catch (SQLException ex) {
				throw new RQLException("Error in sql " + sql, ex);
			}
		}
		return selectPageStmt;
	}

	/**
	 * Forwards to next row of result set to get all information out of the set. Returns true, if next row is available.<p>
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
	 * Forwards to next row of result set to get all information out of the set. Returns true, if next row is available.<p>
	 * Sort the returned pages by template name.
	 */
	public boolean nextPageSortedByTemplateName() throws RQLException {
		try {
			return getSelectAllRs(SORT_MODE_TEMPLATE_NAME).next();
		} catch (SQLException ex) {
			throw new RQLException("Error in next()", ex);
		}
	}

	/**
	 * Returns the number of pages in this set.
	 */
	public int size() throws RQLException {
		String sql = null;
		int size = 0;
		try {
			sql = "SELECT count(*) from " + name;
			ResultSet rs = connection.createStatement().executeQuery(sql);
			rs.next();
			size = rs.getInt(1); 
		} catch (SQLException ex) {
			throw new RQLException("Error in sql " + sql, ex);
		}
		return size;
	}
}
