package com.hlcl.rql.util.as;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import com.hlcl.rql.as.ReddotDate;

public class PingLogger2App {

	public static void main(String[] args) {

		// split server list by ,
		String[] ips = args[0].split(",");
		long sleepSeconds = Long.parseLong(args[1]);

		Runtime r = Runtime.getRuntime();

		try {
			for (;;) {
				for (int i = 0; i < ips.length; i++) {
					String ip = ips[i];

					String pingCmd = "ping -n 1 " + ip;
					// System.out.println(pingCmd);

					// execute
					Process p = r.exec(pingCmd);

					// collect ping answer
					String answer = "";
					BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						answer += inputLine;
					}
					in.close();
					
					// check and out
					String timestamp = ReddotDate.formatAsyyyyMMddHHmmss(new Date());
					if (answer.contains("Reply from")) {
						System.out.println(timestamp + " " + ip + " reply reached");
					} else {
						System.out.println(timestamp + " " + ip + " " + answer);
					}
				}
				// sleep
				Thread.sleep(sleepSeconds * 1000l);
			}
		} catch (IOException e) {
			System.out.println(e);
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
}
