package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Diese Klasse beschreibt einen RedDot AssetManagerFolder-Dateiordner (catalog typ=1).
 * 
 * @author LEJAFR
 */
public class AssetManagerFolder extends Folder {

	private RQLNodeList subFolderNodeList;

	/**
	 * AssetManagerFolder constructor comment.
	 * 
	 * @param project
	 *            com.hlcl.rql.as.Project
	 * @param name
	 *            java.lang.String
	 * @param folderGuid
	 *            java.lang.String
	 * @param hideInTextEditor
	 *            0 or 1 if folder hidden in text editor
	 * @param saveType
	 *            number between 0 and 3 where the folder content is stored
	 * @param path
	 *            path in file system, if saveType=2
	 */
	public AssetManagerFolder(Project project, String name, String folderGuid, String hideInTextEditor, String saveType, String path) {
		super(project, name, folderGuid, hideInTextEditor, saveType, path);
	}

	/**
	 * Erzeugt ein SubFolder object.
	 */
	private AssetManagerSubFolder buildSubFolder(RQLNode subfolderNode) throws RQLException {
		return new AssetManagerSubFolder(getProject(), this, subfolderNode.getAttribute("name"), subfolderNode.getAttribute("guid"),
				subfolderNode.getAttribute("path"), subfolderNode.getAttribute("description"));
	}

	/**
	 * Liefert genau dann true, wenn der gegebene Dateiname in einem AssetManager Subfolder existiert.
	 * <p>
	 * Liefert true, falls dieser AssetManager keinen Subfolder hat!
	 * 
	 * @see Folder#exists(String)
	 * @see #exists(String)
	 */
	public boolean existsInSubFolder(String filename) throws RQLException {
		// if not in this asset manager check the subfolders if existing
		if (!hasSubFolders()) {
			return true;
		} else {
			for (AssetManagerSubFolder subFolder : getSubFolders()) {
				if (subFolder.exists(filename)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Liefert den SubFolderNode fÃ¼r die gegebene GUID vom CMS zurÃ¼ck.
	 */
	private RQLNode findSubFolderNodeByGuid(String subfolderGuid) throws RQLException {

		// find folder
		RQLNode subFolderNode = null;
		RQLNodeList subFolderList = getSubFolderNodeList();

		if (subFolderList == null) {
			throw new ElementNotFoundException("Sub folder with GUID " + subfolderGuid
					+ " could not be found, because asset manager with name " + getName() + " has no subfolders at all.");
		}

		for (int i = 0; i < subFolderList.size(); i++) {
			subFolderNode = subFolderList.get(i);

			if (subFolderNode.getAttribute("guid").equals(subfolderGuid)) {
				return subFolderNode;
			}
		}
		throw new ElementNotFoundException("Sub folder with GUID " + subfolderGuid
				+ " could not be found for asset manager with name " + getName() + ".");
	}

	/**
	 * Liefert den SubFolderNode fÃ¼r den gegebenen Namen vom CMS zurÃ¼ck.
	 * 
	 * @param name
	 *            Name des Unterordners.
	 * @return RQLNode
	 * @see <code>RQLNode</code>
	 */
	private RQLNode findSubFolderNodeByName(String name) throws RQLException {

		// find folder
		RQLNode subFolderNode = null;
		RQLNodeList subFolderList = getSubFolderNodeList();

		for (int i = 0; i < subFolderList.size(); i++) {
			subFolderNode = subFolderList.get(i);

			if (subFolderNode.getAttribute("name").equals(name)) {
				return subFolderNode;
			}
		}
		throw new ElementNotFoundException("Sub folder named " + name + " could not be found for asset manager with name " + getName()
				+ ".");
	}

	/**
	 * Liefert den Unterordner mit der gegebenen GUID dieses Assetmanagers zurück.
	 */
	public AssetManagerSubFolder getSubFolderByGuid(String subfolderGuid) throws RQLException {
		return buildSubFolder(findSubFolderNodeByGuid(subfolderGuid));
	}

	/**
	 * Liefert den Unterordner mit dem gegebenen Namen dieses Assetmanagers zurück.
	 */
	public AssetManagerSubFolder getSubFolderByName(String subfolderName) throws RQLException {
		return buildSubFolder(findSubFolderNodeByName(subfolderName));
	}

	/**
	 * Liefert die RQLNodeList aller Unterordner dieses Assetmanagers zurÃ¼ck.
	 */
	private RQLNodeList getSubFolderNodeList() throws RQLException {
		/* 
		 V7.5 request
		 <IODATA loginguid="0A3B18FA31F641A7BFFD9E125871579F" sessionkey="4B47DBA80DE24C67B16C2E5C49551897">
		 <FOLDER guid="DB7234C07B6947EE8AACD6E271DDF32C">
		 <SUBFOLDER action="list"/>
		 </FOLDER>
		 </IODATA>
		 V7.5 response (asset manager stored in filesystem)
		 <IODATA><FOLDERS>
		 <FOLDER guid="2E80FAB2635745A996A03A3E65FD9A21" parentfolderguid="1B0280BBF0E64E70AF7D1D5575E52510" linkedprojectguid="" linkedfolderguid="" 
		 foldertype="0" catalog="1" name="orga_charts_pdf" path="orga_charts_pdf\" description="" folderrights="2147483647"/>
		 </FOLDERS></IODATA>
		 */

		if (subFolderNodeList == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + "<FOLDER guid='"
					+ getFolderGuid() + "'>" + "<SUBFOLDER action='list'/>" + "</FOLDER></IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			subFolderNodeList = rqlResponse.getNodes("FOLDER");
		}
		return subFolderNodeList;
	}

	/**
	 * Liefert alle Unterordner dieses Assetmanagers. Um Unterordner haben zu kÃ¶nnen mÃ¼ssen die Assets im Dateisystem liegen, was
	 * hier nicht geprÃ¼ft wird.
	 * <p>
	 * Liegen die Assets nicht im Dateisystem, wird eine leere Liste geliefert.
	 */
	public java.util.List<AssetManagerSubFolder> getSubFolders() throws RQLException {
		// check if subfolders available
		java.util.List<AssetManagerSubFolder> result = new ArrayList<AssetManagerSubFolder>();
		RQLNodeList nodes = getSubFolderNodeList();
		if (nodes == null || nodes.size() == 0) {
			return result;
		}

		// wrap into subfolders
		for (int i = 0; i < nodes.size(); i++) {
			RQLNode node = nodes.get(i);
			result.add(buildSubFolder(node));
		}

		return result;
	}

	/**
	 * Liefert true, falls dieser Assetmanager keinen einzigen Unterordner hat, sonst false.
	 */
	public boolean hasNoSubFolders() throws RQLException {
		return !hasSubFolders();
	}

	/**
	 * Liefert true, falls dieser Assetmanager Unterordner hat, sonst false.
	 */
	public boolean hasSubFolders() throws RQLException {
		// check if subfolders available
		RQLNodeList nodes = getSubFolderNodeList();
		if (nodes == null || nodes.size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * Liefert true, falls die gegebene GUID einen Subfolder dieses AssetManagers bezeichnet, sonst false.
	 */
	public boolean containsSubFolderByGuid(String subFolderGuid) throws RQLException {
		// check if subfolders available
		RQLNodeList nodes = getSubFolderNodeList();
		if (nodes == null || nodes.size() == 0) {
			return false;
		}
		for (int i = 0; i < nodes.size(); i++) {
			RQLNode node = nodes.get(i);
			if (node.getAttribute("guid").equals(subFolderGuid)) {
				return true;
			}
		}
		// not found in list
		return true;
	}

	/**
	 * Liefert true, weil dieser folder ein AssetManager ist.
	 */
	public boolean isAssetManagerFolder() {
		return true;
	}

	/**
	 * Refreshs the thumbnail and file attributes for the given file in this asset manager folder.
	 * <p>
	 * Attention: In a cluster system this RQL command can be used only local, means on the same machine as updated before.
	 */
	public void refreshFileInformation(File file) throws RQLException {
		refreshFileInformation(file.getFilename());
	}

	/**
	 * Refreshs the thumbnail and file attributes for the given file in this asset manager folder.
	 * <p>
	 * Attention: In a cluster system this RQL command can be used only local, means on the same machine as updated before.
	 */
	public void refreshFileInformation(String filename) throws RQLException {
		/* V6.5 request
		 <IODATA loginguid="2FD42621B54B4154A1D1BB38A7F1347A" sessionkey="10218343230r4s2t6C8Yg">
		 <MEDIA>
		 <FOLDER guid="A9DD33A18B334D48B560E4D95987AE69" tempdir="e:\server\CMS\ASP\RedDotTemp\2FD42621B54B4154A1D1BB38A7F1347A\">
		 <FILE action="update" sourcename="test.png" />
		 </FOLDER>
		 </MEDIA>
		 </IODATA>
		 V6.5 response 
		 <IODATA>
		 <THUMB filename="test.png" guid="AD016BDE60F342C29CE8146ED5D6E600"/>
		 </IODATA>
		 */

		// call CMS
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		String rdTempDir = b.getString("localRedDotTempDir");
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + " <MEDIA>"
				+ "  <FOLDER guid='" + getFolderGuid() + "' tempdir='" + rdTempDir + getLogonGuid() + "\\'>"
				+ "   <FILE action='update' sourcename='" + filename + "' />" + "  </FOLDER>" + " </MEDIA>" + "</IODATA>";
		callCmsWithoutParsing(rqlRequest);
	}
	
	
	public java.util.List<FileAttribute> getCatalogAttributes() throws RQLException {
		 String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				 + "<MEDIA><FOLDER guid='"+getFolderGuid()+"'><CATALOGATTRIBUTES action='list' /></FOLDER></MEDIA>"
				 + "</IODATA>";
		 RQLNode rs = callCms(rqlRequest);
		 RQLNodeList nl = rs.getNodesRecursive("FILEATTRIBUTE");
		 ArrayList<FileAttribute> out = new ArrayList<FileAttribute>(nl.size());

		 for (RQLNode n : nl) {
			 out.add(new FileAttribute(this, n));
		 }
		 
		 return out;
	}
	
	
	@Override
	public java.util.List<File> searchFiles(String pattern, String suffixes, String exceptionSuffix) throws RQLException {
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>"
				+ "<MEDIA>" + "<FOLDER guid='" + getFolderGuid() + "'>" + "<FILES action='list' searchtext='" + pattern;
		if (suffixes != null && suffixes.trim().length() > 0) {
			rqlRequest = rqlRequest + "' pattern='" + suffixes;
		}
		rqlRequest = rqlRequest + "' maxcount='999999' startcount='1' />" + "</FOLDER>" + "</MEDIA>"
				+ "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		RQLNodeList fileNodeList = rqlResponse.getNodesRecursive("FILE");
		java.util.List<File> files = new ArrayList<File>(fileNodeList.size());

		for (int i = 0; i < fileNodeList.size(); i++) {
			RQLNode fileNode = fileNodeList.get(i);
			String filename = fileNode.getAttribute("name");
			if (exceptionSuffix != null && filename.substring(filename.lastIndexOf(".") + 1).equals(exceptionSuffix)) {
				continue;
			}
			// reverse engineering shows that the file guid is hidden in a wrongly documented field. "guid" is missing
			files.add(new File(this, filename,
						fileNode.getAttribute("date"),
						fileNode.getAttribute(fileNode.hasAttribute("guid") ? "guid" : "thumbguid")));
		}
		return files;
	}

}
