package com.hlcl.rql.as;

/**
 * Insert the type's description here.
 * 
 * @author lejafr 
 */
public interface FolderContainer extends ProjectContainer {
/**
 * Liefert den Folder.
 */
public Folder getFolder() throws RQLException;
/**
 * Liefert die RedDot GUID dieses Folders.
 */
public String getFolderGuid();
}
