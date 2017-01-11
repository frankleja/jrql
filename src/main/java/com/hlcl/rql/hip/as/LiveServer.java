package com.hlcl.rql.hip.as;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;

import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StringHelper;

/**
 * @author lejafr TODO move ftp functions to super class WebServer to use it for hlag tidy job on prod website This class represents a RD Live Server.
 *         Mostly the file transfer is encapsulated.
 */
public class LiveServer {

	private String defaultWorkingDirectory; // on the ftp server itself; set when opening a connection
	private FTPClient ftpClient; // cache
	private String password;
	// ftp credentials for import path
	private String server;

	private String user;

	/**
	 * Creates a live server (or other FTP server) representation.
	 * 
	 * @param server
	 *            name of FTP server
	 * @param user
	 *            FTP user name
	 * @param password
	 *            user's password
	 * @param defaultWorkingDirectory
	 *            fully qualified path name (from root); not be changed into immediately; use
	 * @link {@link #changeToDefaultWorkingDirectory()}
	 */
	public LiveServer(String server, String user, String password, String defaultWorkingDirectory) {
		super();
		this.server = server;
		this.user = user;
		this.password = password;
		// make sure it ends always with /
		this.defaultWorkingDirectory = ensureEnding(defaultWorkingDirectory);
	}

	/**
	 * Wechselt in das Default-Unterverzeichnis, das im Konstruktor mitgegeben wurde.
	 */
	public void changeToDefaultWorkingDirectory() throws RQLException {
		changeWorkingDirectory(defaultWorkingDirectory);
	}

	/**
	 * Change into parent directory of current working directory.
	 * 
	 * @see #getCurrentWorkingDirectory()
	 */
	public void changeToParentDirectory() throws RQLException {
		checkFtpClient();
		try {
			ftpClient.changeToParentDirectory();
		} catch (IOException ex) {
			throw new RQLException("Changing to the parent directory via FTP failed.", ex);
		}
	}

	/**
	 * Returns all file names (sorted ascending) of current working directory.
	 * 
	 * @see #getCurrentWorkingDirectory()
	 */
	public SortedSet<String> getFilenamesInCurrentWorkingDirectory() throws RQLException {
		return getFilenamesInCurrentWorkingDirectory(null);
	}

	/**
	 * Returns all file names (sorted ascending) of current working directory matching the given pattern.
	 * 
	 * @param pattern
	 *            if null, all files found will be returned
	 *            otherwise a filename filter for given pattern (the only supported wild card is *) is applied, like hip_*_filter.zip.
	 * @see #getCurrentWorkingDirectory()
	 */
	public SortedSet<String> getFilenamesInCurrentWorkingDirectory(String pattern) throws RQLException {
		return getFilenames(getCurrentWorkingDirectory(), pattern);
	}

	/**
	 * Returns all filenames (without fully qualified path) of all files in given directory path (starting in root).
	 * <p>
	 * Returned filenames will be sorted ascending.
	 * 
	 * @param pathName
	 *            if null, all files found will be returned
	 *            otherwise a filename filter for given pattern (the only supported wild card is *) is applied, like hip_*_filter.zip.
	 */
	public SortedSet<String> getFilenames(String pathName) throws RQLException {
		return getFilenames(pathName, null);
	}
	/**
	 * Returns all filenames (without fully qualified path) of all files in given directory path (starting in root) matching given pattern.
	 * <p>
	 * Returned filenames will be sorted ascending.
	 * 
	 * @param pattern
	 *            if null, all files found will be returned
	 *            otherwise a filename filter for given pattern (the only supported wild card is *) is applied, like hip_*_filter.zip.
	 */
	public SortedSet<String> getFilenames(String pathName, String pattern) throws RQLException {
		checkFtpClient();
		String[] pathNames = null;
		try {
			pathNames = ftpClient.listNames(pathName);
		} catch (IOException ex) {
			throw new RQLException("List all file names via FTP failed.", ex);
		}
		// no array returned
		if (pathNames == null) {
			throw new RQLException("List all file names via FTP failed with an FTP error 550.");
		}

		// filter and sort whole filename ascending
		SortedSet<String> result = new TreeSet<String>();
		for (String path : pathNames) {
			// remove prefixed path
			String filename = StringHelper.removePrefix(path, ensureEnding(pathName));
			
			// filter simply remove path
			if (pattern != null) {
				Pattern patt = StringHelper.convert2Regex(pattern);
				Matcher matcher = patt.matcher(filename);
				if (!matcher.find()) {
					// skip filename not matching given pattern
					continue;
				}
			}
			
			// sort valid filename into result
			result. add(filename);
		}

		return result;
	}

	/**
	 * Ensures that given path ends with /.
	 */
	private String ensureEnding(String pathName) {
		return StringHelper.ensureEnding(pathName, "/");
	}

	/**
	 * Wechselt in das gegebenen Unterverzeichnis.
	 */
	public void changeWorkingDirectory(String directory) throws RQLException {
		checkFtpClient();
		try {
			ftpClient.cwd(directory);
		} catch (IOException ex) {
			throw new RQLException("Changing the directory via FTP failed.", ex);
		}
	}

	/**
	 * Prüft, ob ein FTP Client erstellt wurde.
	 */
	private void checkFtpClient() throws RQLException {
		// check for existing ftp client
		if (ftpClient == null) {
			throw new RQLException("The FTP connection to the LS import directory is not created. You have to use openFtpConnection() before.");
		}
	}

	/**
	 * Schließt die Verbindung zum FTP Server.
	 * 
	 * @see #openFtpConnection()
	 */
	public void closeFtpConnection() throws RQLException {
		try {
			ftpClient.logout();
			ftpClient.disconnect();
			ftpClient = null;
		} catch (IOException ex) {
			throw new RQLException("Disconnect from FTP server did not work.", ex);
		}
	}

	/**
	 * Kopiert die Datei sourceFilename in das aktuelle Arbeitsverzeichnis als targetFilename.
	 * <p>
	 * Die FTP Verbindung muss zuvor durch {@link #openFtpConnection()} geöffnet werden.
	 * 
	 * @return true, if download was successful, otherwise false.
	 */
	public boolean downloadFile(String sourceFilename, String targetFilename) throws RQLException {
		checkFtpClient();
		// get from ftp server
		InputStream in = null;
		try {
			in = ftpClient.retrieveFileStream(sourceFilename);
			if (in == null) {
				return false;
			}
			FileOutputStream target = new FileOutputStream(targetFilename);
			IOUtils.copy(in, target);
			in.close();
			target.close();
			return ftpClient.completePendingCommand();
		} catch (IOException ex) {
			throw new RQLException("Download of file with name " + sourceFilename + " via FTP from server " + server + " failed.", ex);
		}
	}

	/**
	 * Liefert das augenblickliche Arbeitsverzeichnis auf dem FTP Server.
	 * <p>
	 * Never ends with /.
	 */
	public String getCurrentWorkingDirectory() throws RQLException {
		checkFtpClient();
		try {
			return ftpClient.printWorkingDirectory();
		} catch (IOException ex) {
			throw new RQLException("Retrieving the current working direcory on the FTP server failed.", ex);
		}
	}

	/**
	 * @return Returns the name of the ftp server.
	 */
	public String getServerName() {
		return server;
	}

	/**
	 * @return Returns the user for the ftp server connect.
	 */
	public String getUserName() {
		return user;
	}

	/**
	 * Changes the transfer mode to ASCII.
	 * @return true, if change was successful, otherwise false. 
	 */
	public boolean switchToAsciiFileTransfer() throws RQLException {
		checkFtpClient();
		try {
			return ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
		} catch (IOException ex) {
			throw new RQLException("File type could not be changed to ASCII");
		}
	}

	/**
	 * Changes the transfer mode to binary.
	 * @return true, if change was successful, otherwise false. 
	 */
	public boolean switchToBinaryFileTransfer() throws RQLException {
		checkFtpClient();
		try {
			return ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
		} catch (IOException ex) {
			throw new RQLException("File type could not be changed to binary");
		}
	}

	/**
	 * Öffnet die Verbindung zum Importverzeichnis dieses LS per FTP. Setzt als aktuelles Arbeitsverzeichnis den Defaultwert des Konstruktors.
	 * 
	 * @see #closeFtpConnection()
	 */
	public void openFtpConnection() throws RQLException {
		openFtpConnection(defaultWorkingDirectory);
	}

	/**
	 * Öffnet die Verbindung zum Importverzeichnis dieses LS per FTP. Setzt als aktuelles Arbeitsverzeichnis den übergebenen Pfad.
	 * 
	 * @see #closeFtpConnection()
	 */
	public void openFtpConnection(String workingDirectory) throws RQLException {
		try {
			// create a client
			ftpClient = new FTPClient();
			ftpClient.connect(server);
			ftpClient.login(user, password);
			// change into rd import directory
			ftpClient.changeWorkingDirectory(workingDirectory);
		} catch (IOException ioex) {
			throw new RQLException("FTP client could not be created. Please check attributes given in constructor.", ioex);
		}
	}

	/**
	 * Löscht den LS Inhalt aus diesem LS, indem ein deletion file (eine XML Datei) in das Importverzeichnis gestellt wird.
	 * <p>
	 * Die FTP Verbindung muss zuvor durch
	 * 
	 * @see #openFtpConnection() geöffnet werden.
	 */
	public void requestDeletionOfFile(String filename) throws RQLException {
		checkFtpClient();
		// put on ftp server
		LiveServerDeleteRequest request = new LiveServerDeleteRequest(filename);
		OutputStream out = null;
		try {
			out = ftpClient.storeFileStream(request.getFtpFilename());
			request.writeOn(out);
			out.close();
			ftpClient.completePendingCommand();
		} catch (IOException ex) {
			throw new RQLException("Writing of the deletion file to this LS import directory via FTP failed.", ex);
		}
	}

	/**
	 * Kopiert die lokale Daten filename in das aktuelle Arbeitsverzeichnis des FTP Servers.
	 * <p>
	 * Die FTP Verbindung muss zuvor durch
	 * 
	 * @see #openFtpConnection() geöffnet werden.
	 */
	public void uploadFile(String filename) throws RQLException {
		checkFtpClient();
		// put on ftp server
		OutputStream out = null;
		try {
			out = ftpClient.storeFileStream(filename);
			IOUtils.copy(new FileReader(filename), out);
			out.close();
			ftpClient.completePendingCommand();
		} catch (IOException ex) {
			throw new RQLException("Upload of local file with name " + filename + " via FTP to server " + server + " failed.", ex);
		}
	}

	/**
	 * Löscht die Datei mit dem gegebenen Namen und Pfad vom FTP Server. Returns true, if successful.
	 * <p>
	 * Die FTP Verbindung muss zuvor durch
	 * 
	 * @see #openFtpConnection() geöffnet werden.
	 */
	public boolean deleteFile(String filename) throws RQLException {
		checkFtpClient();
		boolean result = false;
		try {
			result = ftpClient.deleteFile(filename);
		} catch (IOException ex) {
			throw new RQLException("Deletion of file with name " + filename + " from FTP server " + server + " failed.", ex);
		}
		return result;
	}
}
