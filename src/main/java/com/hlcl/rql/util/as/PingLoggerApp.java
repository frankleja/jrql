package com.hlcl.rql.util.as;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import com.hlcl.rql.as.ReddotDate;

public class PingLoggerApp {

	public static void main(String[] args) {

		String ip = args[0];

		String pingCmd = "ping -n 1 " + ip;
		System.out.println(pingCmd);

		try {
			Runtime r = Runtime.getRuntime();

			for (;;) {
				Process p = r.exec(pingCmd);
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String inputLine;
				String timestamp = ReddotDate.formatAsyyyyMMddHHmmss(new Date());
				while ((inputLine = in.readLine()) != null) {
					if (inputLine.startsWith("Reply from")) {
						System.out.println(timestamp + " " + inputLine);
					}
				}
				in.close();
				// sleep
				Thread.sleep(2000);
			}
		}// try
		catch (IOException e) {
			System.out.println(e);
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
}
