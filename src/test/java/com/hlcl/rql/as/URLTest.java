package com.hlcl.rql.as;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * simple test for URL domain name extract
 * 
 * @author lejafr
 */
public class URLTest {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		URL url = new URL("http://reddot.hlcl.com:8081/cms/hlclRemote.asp");
		System.out.println(url.getHost());
		System.out.println(url.getProtocol());
		System.out.println(url.getPort());
		System.out.println(url.getPath());
		System.out.println(url.getQuery());
		
		// always without ending /
		String fp = StringHelper.replace(url.toString(), url.getPath() , "");
		System.out.println(fp);
	}

}
