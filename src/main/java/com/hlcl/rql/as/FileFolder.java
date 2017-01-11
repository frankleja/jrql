package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt einen normalen RedDot Dateiordner (catalog typ=0).
 * 
 * @author LEJAFR
 */
public class FileFolder extends Folder {
	/**
	 * FileFolder constructor comment.
	 * @param project com.hlcl.rql.as.Project
	 * @param name java.lang.String
	 * @param folderGuid java.lang.String
	 * @param hideInTextEditor	0 or 1 if folder hidden in text editor
	 * @param saveType	number between 0 and 3 where the folder content is stored
	 * @param path	path in file system, if saveType=2
	 */
	public FileFolder(Project project, String name, String folderGuid, String hideInTextEditor, String saveType, String path) {
		super(project, name, folderGuid, hideInTextEditor, saveType, path);
	}
	/**
	 * Liefert true, weil dieser folder ein einfacher FileFolder ist.
	 */
	public boolean isFileFolder() {

		return true;
	}
}
