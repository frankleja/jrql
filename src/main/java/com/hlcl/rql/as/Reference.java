package com.hlcl.rql.as;

/**
 * A Reference is made basically made of a link between a "source guid" and a "target guid".
 * The "source guid" refers to a StructureElement of some page.
 */
public class Reference extends AbstractProjectContainer implements ProjectContainer {

	private String targetGuid;
	private RQLNode pageNode;
	private RQLNode linkNode;

	public Reference(Project project, String targetGuid) {
		super(project);
		this.targetGuid = targetGuid;
	}
	
	/*
	 * 
<IODATA>
  <PAGES>
    <PAGE guid="[!guid_page!]" id="7429" headline="My Headline">
      <LINKS>
        <LINK guid="[!guid_link!]" type="28" languagevariantguid="" 
         languagevariantid="" languagevariantname="" value="container1"/>
        ...
      </LINKS>
    </PAGE>
    ...
  </PAGES>
</IODATA>
	 */
	public void fromResult(RQLNode pageNode, RQLNode linkNode) {
		this.pageNode = pageNode;
		this.linkNode = linkNode;
	}

	
	public String getSourcePageGuid() {
		return pageNode.getAttribute("guid");
	}
	
	
	public String getTargetGuid() {
		return targetGuid;
	}

	
	public Page getSourcePage() throws RQLException {
		return new Page(project,
				getSourcePageGuid(),
				pageNode.getAttribute("id"),
				pageNode.getAttribute("headline"));
	}
	
	
	public String getSourceLinkGuid() {
		return linkNode.getAttribute("guid");
	}
	
	
	public String getSourceLinkName() {
		return linkNode.getAttribute("value");
	}
	
	
	public int getSourceLinkType() {
		return Integer.parseInt(linkNode.getAttribute("type"));
	}
	
}
