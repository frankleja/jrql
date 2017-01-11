package com.hlcl.rql.util.as;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class HttpHeadersApp {

	public static void main(String[] args) {

		
		try {
			URL url = new URL("http://wwwtest.hlcl.com/feeds/en/news/rate_amendments.rss");
			URLConnection conn = url.openConnection();

			// header
			Map<String, List<String>> headerFields = conn.getHeaderFields();
			for (String name : headerFields.keySet()) {
				System.out.println(name + ":" + headerFields.get(name).get(0));
			}
			
			// body
			InputStream in = conn.getInputStream();
			int c;
			while ((c = in.read()) > 0) {
				System.out.print((char) c);
			}
			in.close();
		} catch (MalformedURLException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
}
