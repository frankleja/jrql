package com.hlcl.rql.util.as;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StringHelper;

/**
 * @author Frank Leja
 * 
 * This class helps to write an Excel tab file field by field.
 */
public class TabFileWriter {

	private String encoding;
	private String filename;
	private final static String separator = "\t";
	private boolean firstField;

	private BufferedWriter writer;

	/**
	 * Constructor using default encoding UTF-8 and \t as field separator.
	 * <p>
	 * If filename ends with .tab Excel can open it directly.
	 */
	public TabFileWriter(String filename) {
		super();
		this.filename = filename;

		// sets defaults
		encoding = "UTF-8";
		firstField = true;
	}

	/**
	 * Constructor using given encoding and \t as field separator.
	 * <p>
	 * If filename ends with .tab Excel can open it directly.
	 */
	public TabFileWriter(String filename, String encoding) {
		super();
		this.filename = filename;
		this.encoding = encoding;
		firstField = true;
	}

	/**
	 * Starts a new line (append the line separator). Per default the buffer is flushed.
	 */
	public void newLine() throws RQLException {
		try {
			BufferedWriter writer2 = getWriter();
			writer2.newLine();
			writer2.flush();
			firstField = true;
		} catch (IOException ex) {
			throw new RQLException("IOException writing the line separator into file " + filename + ".", ex);
		}
	}

	/**
	 * Appends a new boolean column value into current line.
	 */
	public void append(boolean value) throws RQLException {
		append(value ? "true" : "false");
	}

	/**
	 * Appends a new boolean column value into current line.
	 */
	public void append(int value) throws RQLException {
		append(String.valueOf(value));
	}

	/**
	 * Adds empty fields for the number of columns.
	 */
	public void indent(int numberOfColumns) throws RQLException {
		for (int i = 1; i <= numberOfColumns; i++) {
			append("");
		}
	}

	/**
	 * Appends a new column value into current line after replacing all \t and \n with blanks.
	 */
	public void append(String columnValue) throws RQLException {
		if (!firstField) {
			appendPrim(separator, false);
		}
		appendPrim(columnValue, true);
		firstField = false;
	}

	/**
	 * Appends a new column value into current line. replace all \t and \n with blanks if requested.
	 */
	private void appendPrim(String columnValue, boolean escape) throws RQLException {
		try {
			BufferedWriter writer = getWriter();
			String replace = columnValue;
			if (escape) {
				replace = StringHelper.replace(replace, separator, " ");
				replace = StringHelper.replace(replace, "\n", " ");
			}
			writer.append(replace);
		} catch (IOException ex) {
			throw new RQLException("IOException appending the given value " + columnValue + " into file " + filename + ".", ex);
		}
	}

	/**
	 * Appends all given column values into current line.
	 */
	public void append(String[] columnValues) throws RQLException {
		for (String colValue : columnValues) {
			append(colValue);
		}
	}

	/**
	 * Closes the file. Didn't write a line separator.
	 */
	public void close() throws RQLException {
		try {
			writer.close();
		} catch (IOException ex) {
			throw new RQLException("IOException closing the file " + filename + ".", ex);
		}
	}

	/**
	 * Lazy initializes the encapsulated buffered writer.
	 */
	private BufferedWriter getWriter() throws RQLException {
		if (writer == null) {
			try {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), encoding));
				firstField = true;
			} catch (UnsupportedEncodingException ex) {
				throw new RQLException("Unsupported encoding " + encoding + ". See JavaDoc and try again.", ex);
			} catch (FileNotFoundException ex) {
				throw new RQLException("File not found exception for filename " + filename + ".", ex);
			}
		}
		return writer;
	}
}
