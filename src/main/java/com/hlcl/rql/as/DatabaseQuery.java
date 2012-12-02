package com.hlcl.rql.as;

/**
 * This class encapsulates an SQL statement and offer access to parts of the SELECT statement.
 * 
 * @author LEJAFR
 */
public class DatabaseQuery {

	private String selectStatement;

	/**
	 * Creates a database query from the given SELECT statement.
	 */
	public DatabaseQuery(String selectStatement) {

		// equalize the key words
		String result = selectStatement;
		result = StringHelper.replace(result, "select", "SELECT");
		result = StringHelper.replace(result, "from", "FROM");
		result = StringHelper.replace(result, "where", "WHERE");
		
		this.selectStatement = result;
	}

	/**
	 * Returns the whole where clause of this statement.
	 */
	public String getWhereClause() {
		return StringHelper.splitAt1stOccurenceFromLeft(selectStatement, "WHERE")[1];
	}

	/**
	 * Returns the encapsulated SELECT statement.
	 */
	public String getSelectStatement() {
		return selectStatement;
	}

	/**
	 * Returns value of the given columnName the where clause of this statement.<p>
	 * Deliver the value HAPAGL for statement ... where matchcode_name = 'HAPAGL' and matchcode_suppl = 043" and given columnName = machcode_name and logicalDelimiter = and and operator = =<p>
	 * Returns null, if column name could not be found.  
	 */
	public String getWhereClauseColumnValue(String columnName, String logicalDelimiter, String operator) {
		String where = getWhereClause();
		String[] clauses = StringHelper.split(where, logicalDelimiter.toLowerCase());
		
		// check all clauses and split at operator
		for (int i = 0; i < clauses.length; i++) {
			String clause = clauses[i].trim();
			if (clause.startsWith(columnName)) {
				// column name found; extract value
				String value = StringHelper.split(clause, operator)[1].trim();
				// remove ' if a string literal
				return StringHelper.replace(value, "'", "");
			}
		}
		// not found
		return null;
	}

}
