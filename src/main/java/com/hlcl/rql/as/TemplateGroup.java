package com.hlcl.rql.as;

import java.util.ArrayList;

/**
 * A list of templates.
 */
public class TemplateGroup {

	protected final java.util.List<Template> templates = new ArrayList<Template>();
	private String templateGroupGuid;
	private String name;
	 


	public TemplateGroup(String templateGroupGuid, String name) {
		this.templateGroupGuid = templateGroupGuid;
		this.name = name;
	}


	public String getName() {
		return name;
	}
	
	
	public String getTemplateGroupGuid() {
		return templateGroupGuid;
	}
	
}
