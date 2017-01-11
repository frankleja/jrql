package com.hlcl.rql.as;

/**
 * Easier access to the project context.
 */
public class AbstractProjectContainer implements ProjectContainer {

	protected Project project;

	public AbstractProjectContainer(Project project) {
		this.project = project;
	}
	
	@Override
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getCmsClient().callCms(rqlRequest);
	}

	@Override
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	@Override
	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}

	@Override
	public String getLogonGuid() {
		return getProject().getLogonGuid();
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public String getProjectGuid() throws RQLException {
		return getProject().getProjectGuid();
	}

	@Override
	public String getSessionKey() {
		return getProject().getSessionKey();
	}

}
