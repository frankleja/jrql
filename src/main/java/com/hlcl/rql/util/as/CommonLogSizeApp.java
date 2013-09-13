package com.hlcl.rql.util.as;

import java.io.File;
import java.io.FilenameFilter;
import java.util.SortedMap;
import java.util.TreeMap;

import com.hlcl.rql.as.StringHelper;

/**
 * Summarize the common log file's file size and group per day.
 * 
 * @author LEJAFR
 */
public class CommonLogSizeApp {

	public static void main(String[] args) {
		// get parms
		String path = args[0];
		String filenameEnd = args[1];

		String timeFrom = args[2];
		String timeTo = args[3];

		// define filter class
		class CommonLogFilenameFilter implements FilenameFilter {
			private String filenameEnd;
			private String timeFrom;
			private String timeTo;

			public CommonLogFilenameFilter(String filenameEnd, String timeFrom, String timeTo) {
				super();
				this.filenameEnd = filenameEnd;
				this.timeFrom = timeFrom;
				this.timeTo = timeTo;
			}

			public boolean accept(File dir, String name) {
				// only common log files
				if (!name.endsWith(filenameEnd)) {
					return false;
				}
				// check time
				String[] parts = StringHelper.split(name, "_");
				if (timeFrom.compareTo(parts[1]) <= 0 && parts[1].compareTo(timeTo) <= 0) {
					return true;
				}
				// ignore
				return false;
			}
		}

		// get files
		File folder = new File(path);
		FilenameFilter filter = new CommonLogFilenameFilter(filenameEnd, timeFrom, timeTo);
		File[] files = folder.listFiles(filter);

		// collect file size and group by day
		SortedMap<String, Long> usage = new TreeMap<String, Long>();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			long size = file.length();
			String day = StringHelper.split(file.getName(), "_")[0];
			
			if (usage.containsKey(day)) {
				// add file size
				long old = usage.get(day);
				usage.put(day, old + size);
			} else {
				// add new key
				usage.put(day, size);
			}
		}
		
		// write out size in MB per day
		System.out.println("day,common log size in MB");
		for (String day : usage.keySet()) {
			System.out.println(day + "," + usage.get(day)/1000000L);
		}
	}
}
