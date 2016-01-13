package com.hlcl.rql.as;


/**
 * Definition of a file attribute.
 */
public class FileAttribute {

	private Folder parent;
	private String guid;
	private String type;
	private String name;

	/**
	 * Construct from <CATALOGATTRIBUTES action="list" />
	 * @param parent the folder this is defined in.
	 * @param node FILEATTRIBUTE node
	 */
	public FileAttribute(Folder parent, RQLNode node) {
		this.parent = parent;
		this.guid = node.getAttribute("guid");
		this.type = node.getAttribute("type");
		this.name = node.getAttribute("name");
		// node.getAttribute("rights5")
	}

	
	public Folder getFolder() {
		return parent;
	}

	
	public String getFileAttributeGuid() {
		return guid;
	}

	
	public String getType() {
		return type;
	}

	
	public String getName() {
		return name;
	}

	
	public int hashCode() {
		return guid.hashCode();
	}
	
	
	public boolean equals(Object o) {
		if (getClass() != o.getClass())
			return false;
		return guid.equals(((FileAttribute) o).guid);
	}
}
