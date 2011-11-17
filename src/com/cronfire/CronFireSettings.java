package com.cronfire;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.cronfire.endpoint.EndpointHost;
import com.cronfire.endpoint.EndpointPath;
import com.cronfire.endpoint.EndpointProfile;
import com.cronfire.endpoint.EndpointUrl;
import com.cronfire.queue.CronFireQueue;

public class CronFireSettings {
	private static HashMap<String, String> settings = new HashMap<String, String>();
	private static HashMap<String, EndpointHost> hosts = new HashMap<String, EndpointHost>(); 
	private static HashMap<String, EndpointUrl> endpoints = new HashMap<String, EndpointUrl>(); 
	private static HashMap<String, EndpointProfile> profiles = new HashMap<String, EndpointProfile>();
	private static ConcurrentHashMap<String, AtomicInteger> pathRunningCounts = new ConcurrentHashMap<String, AtomicInteger>();
	
	private CronFireSettings() { /* No instantiation */ }

	public static String getSetting(String key) {
		if(settings.containsKey(key))
			return settings.get(key);
		return null;
	}
	
	public static String getSetting(String key, String defaultValue) {
		if(settings.containsKey(key))
			return settings.get(key);
		return defaultValue;
	}
	
	public static boolean getSettingBoolean(String key, boolean defaultValue) {
		boolean val = false;
		
		if(settings.containsKey(key)) {
			try {
				val = Boolean.valueOf(settings.get(key)).booleanValue();
			} catch(Exception e) {
				val = false;
				e.printStackTrace();
			}
		}
		
		return val;
	}
	
	public static int getSettingInt(String key, int defaultValue) {
		int val = 0;
		
		if(settings.containsKey(key)) {
			try {
				val = Integer.valueOf(settings.get(key)).intValue();
			} catch(Exception e) {
				val = 0;
				e.printStackTrace();
			}
		}
		
		return val;
	}
	
	public static long getSettingLong(String key, long defaultValue) {
		long val = 0L;
		
		if(settings.containsKey(key)) {
			try {
				val = Long.valueOf(settings.get(key)).longValue();
			} catch(Exception e) {
				val = 0;
				e.printStackTrace();
			}
		}
		
		return val;
	}
	
	public static double getSettingDouble(String key, double defaultValue) {
		double val = 0;
		
		if(settings.containsKey(key)) {
			try {
				val = Double.valueOf(settings.get(key)).doubleValue();
			} catch(Exception e) {
				val = 0;
				e.printStackTrace();
			}
		}
		
		return val;
	}
	
	public static void setSetting(String key, String value) {
		settings.put(key, value);
	}
	
	public static HashMap<String, EndpointHost> getHosts() {
		return hosts;
	}
	
	public static HashMap<String, EndpointUrl> getEndpoints() {
		return endpoints;
	}
	
	public static HashMap<String, EndpointProfile> getProfiles() {
		return profiles;
	}
	
	public static ConcurrentHashMap<String, AtomicInteger> getPathRunningCounts() {
		return pathRunningCounts;
	}
	
	@SuppressWarnings("rawtypes")
	public static void loadConfigFile(String fileName) {
		profiles.clear();
		pathRunningCounts.clear();
		
		settings.put("config_file", fileName);
		
		try {
			SAXReader reader = new SAXReader();
			File file = new File(fileName);
			Document doc = reader.read(file);
			List list;
		
			// Set mtime
			settings.put("config_file_mtime", Long.toString(file.lastModified()));			
			
			// Settings
			list = doc.selectNodes("//settings/setting");
			for(Iterator iter = list.iterator(); iter.hasNext(); ) {
				Element e = (Element) iter.next();
				String key = e.attributeValue("key");
				String value = e.getText();
				CronFireSettings.setSetting(key, value);
			}
			
			// Profiles
			list = doc.selectNodes("//profile");
			for(Iterator iter = list.iterator(); iter.hasNext(); ) {
				Element e = (Element) iter.next();
				
				String tag = e.attributeValue("tag");
				
				EndpointProfile profile = new EndpointProfile(tag);
				
				// Paths
				List paths = e.selectNodes("path");
				for(Iterator i = paths.iterator(); i.hasNext(); ) {
					Element ePath = (Element) i.next();
					
					String key = ePath.attributeValue("key");
					String interval = ePath.attributeValue("interval");
					String sMax = ePath.attributeValue("max");
					
					EndpointPath path = new EndpointPath(key);
					path.setTag(tag);
					path.setSuffix(ePath.getTextTrim());
					path.setInterval(interval);
					
					if(sMax != null)
						path.setMax(Integer.valueOf(sMax));
					
					pathRunningCounts.putIfAbsent(key, new AtomicInteger());
					profile.addPath(path);
				}
				
				profiles.put(profile.getTag(), profile);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void loadUrls(String fileName) {
		hosts.clear();
		
		settings.put("urls_file", fileName);
		File file = new File(fileName);
		CronFireQueue queue = CronFireQueue.getInstance();
		
		// Set mtime
		settings.put("urls_file_mtime", Long.toString(file.lastModified()));			
		
		if(!file.exists())
			return;
		
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			
			Set<String> newEndpoints = new HashSet<String>();
			
			while(br.ready()) {
				String line = br.readLine();

				if(0 == line.length())
					continue;
				
				ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(line.split("\\s+")));

				if(0 == tokens.size())
					continue;
				
				// Pull the first token
				String url = tokens.remove(0);
				
				// Make sure the URL ends with a slash
				if(!url.endsWith("/"))
					url = url.concat("/");
				
				// Skip the line if it begins with a comment
				if(url.startsWith("#"))
					continue;

				// Store the host
				EndpointHost host = new EndpointHost(url);
				hosts.put(url, host);
				
				if(!tokens.contains("defaults"))
					tokens.add(0, "defaults");
				
				while(tokens.size() > 0) {
					String tag = tokens.remove(0);
					
					if(0 == tag.length())
						continue;
					
					if(profiles.containsKey(tag)) {
						EndpointProfile profile = profiles.get(tag);
						
						Iterator<Entry<String,EndpointPath>> i = profile.getPaths().entrySet().iterator();
						while(i.hasNext()) {
							Entry<String,EndpointPath> entry = i.next();
							EndpointPath path = entry.getValue();
							
							String suffix = path.getSuffix();
							if(suffix.startsWith("/"))
								suffix = suffix.substring(1);
								
							EndpointUrl endpoint;
							
							if(endpoints.containsKey(url + path.getKey())) {
								endpoint = endpoints.get(url + path.getKey());
								endpoint.setUrl(url + suffix);
								queue.getQueue().remove(endpoint); // we may reschedule
							} else {
								endpoint = new EndpointUrl(url + suffix);
							}
							
							endpoint.setHost(host);
							endpoint.setPath(path);
							
							// Default to scheduling by delay into future
							int secs = endpoint.getNextIntervalAsSecs();
							endpoint.delayBySecs(secs);
							
							endpoints.put(url + path.getKey(), endpoint);
							newEndpoints.add(url + path.getKey());
							
							host.getQueue().add(endpoint);
						}
					}
					
				} // end tags
				
			} // end urls
			
			// Scan old endpoints and delete any that no longer exist
			for(Iterator<Entry<String,EndpointUrl>> i = endpoints.entrySet().iterator(); i.hasNext(); ) {
				Entry<String,EndpointUrl> entry = i.next();
				String key = entry.getKey();
				EndpointUrl endpoint = entry.getValue();

				// If the endpoint has been dynamically removed
				if(!newEndpoints.contains(key)) {
					i.remove();
					endpoint.getHost().getQueue().remove(endpoint);
					queue.getQueue().remove(endpoint);
				}
			}

			// Loop through hosts and add the next queue item to master queue
			for(Iterator<Entry<String,EndpointHost>> i = hosts.entrySet().iterator(); i.hasNext(); ) {
				Entry<String,EndpointHost> entry = i.next();
				EndpointHost host = entry.getValue();
				
				// Queue the next available item
				if(!host.getQueue().isEmpty()) {
					EndpointUrl endpoint = host.getQueue().peek();
					host.getQueue().remove(endpoint);
					queue.getQueue().add(endpoint);
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
