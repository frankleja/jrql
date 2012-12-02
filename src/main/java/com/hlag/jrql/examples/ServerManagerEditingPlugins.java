package com.hlag.jrql.examples;

import java.util.List;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Plugin;
import com.hlcl.rql.as.RQLException;

/**
 * TODO needs post
 * 
 * @author lejafr
 */
public class ServerManagerEditingPlugins {

	/**
	 * @param args
	 * @throws RQLException
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);

		// import a plugin from xml definition 
		// file path valid on server where MS is installed
		Plugin plugin = client.importPlugin("E:\\Server\\CMS\\ASP\\PlugIns\\jrql\\cancelWaitingJobs.xml");
		System.out.println(plugin.getPluginGuid());
		System.out.println(plugin.getName() + " " + plugin.isActive());

		// assign projects to plug-in and activate
		String[] guids = { "06BE79A1D9F549388F06F6B649E27152", "73671509FA5C43ED8FC4171AD0298AD2" };
		plugin.assignProjects(guids);
		System.out.println(plugin.isActive());

		// enabling/disabling group of plug-ins
		List<Plugin> enabledPlugins = client.enablePluginsByNameContains("PROD", true);
		System.out.println(enabledPlugins);
		List<Plugin> disabledPlugins = client.disablePluginsByNameContains("PROD", true);
		System.out.println(disabledPlugins);
		
		// delete plug-ins
		int anzahl = client.deletePluginsByNameContains("jRQL", false);
		System.out.println(anzahl);

	}
}
