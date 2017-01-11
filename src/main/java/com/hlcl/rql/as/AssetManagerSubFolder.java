package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt einen Unterordner eines AssetManager Ordners. <p>
 * Dieser kann nur als Unterverzeichnis eines AssetManagers im Dateisystem existieren.<p>
 * Die RQLs für einen solchen Unterordner scheinen die gleichen zu sein wie für normale Folder.
 * 
 * @author LEJAFR
 */
public class AssetManagerSubFolder extends Folder {
	private String description;
	private AssetManagerFolder parentAssetManagerFolder;
	private String path;

	/**
	 * constructor
	 * @param project com.hlcl.rql.as.Project
	 * @param name java.lang.String
	 * @param subFolderGuid java.lang.String
	 * TODO not finished regarding super class constructor (path is doubled)
	 */
	public AssetManagerSubFolder(Project project, AssetManagerFolder parentAssetManagerFolder, String name, String subFolderGuid, String path, String description) {
		super(project, name, subFolderGuid);
		this.parentAssetManagerFolder = parentAssetManagerFolder;
		this.path = path;
		this.description = description;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Liefert den parent Assetmanagerordner zu dem dieser Unterordner gehört.
	 */
	public AssetManagerFolder getParentAssetManagerFolder() {
		return parentAssetManagerFolder;
	}

	/**
	 * @return Returns the path of this subfolder without the parent asset manager's path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Liefert den vollständigen Pfad dieses Subfolder im Dateisystem, falls die Inhalte im Dateisystem abgelegt werden.
	 * 
	 * @throws WrongStorageTypeException
	 *             falls die Inhalte dieses Folders nicht im Dateisystem abgelegt werden
	 */
	public String getFileSystemPath() throws RQLException {
		return getParentAssetManagerFolder().getFileSystemPath() + getPath();
	}
	/**
	 * @return Returns the project
	 */
	public Project getProject() {
		return getParentAssetManagerFolder().getProject();
	}

	/**
	 * Liefert die RedDot GUID dieses Unterordners zurück; Angleichung an Konvention.
	 */
	public String getSubFolderGuid() {
		return getFolderGuid();
	}

	/**
	 * Liefert true, falls dieser folder ein Unterordner eines AssetManagers ist. Liefert immer true.
	 */
	public boolean isAssetManagerSubFolder() {
		return true;
	}

}
