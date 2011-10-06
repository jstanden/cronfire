package com.cronfire;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.cronfire.endpoint.EndpointUrl;
import com.cronfire.queue.CronFireQueue;
import com.cronfire.settings.EndpointPath;
import com.cronfire.settings.EndpointProfile;

public class CronFireSettings {
	private static HashMap<String, EndpointUrl> endpoints = new HashMap<String, EndpointUrl>(); 
	private static HashMap<String, String> settings = new HashMap<String, String>();
	private static HashMap<String, EndpointProfile> profiles = new HashMap<String, EndpointProfile>();
	
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
	
	public static HashMap<String, EndpointUrl> getEndpoints() {
		return endpoints;
	}
	
	@SuppressWarnings("rawtypes")
	public static void loadConfigFile(String filename) {
		try {
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new File(filename));
			List list;
			
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
					
					String sDelay = ePath.attributeValue("delay");
					Integer delay = (sDelay != null) ? Integer.valueOf(sDelay) : 0;
					
					String sMax = ePath.attributeValue("max");
					Integer max = (sMax != null) ? Integer.valueOf(sMax) : 0;
					
					String start = ePath.attributeValue("start");
					
					EndpointPath path = new EndpointPath(key);
					path.setPath(ePath.getTextTrim());
					path.setDelay(delay);
					path.setMax(max);
					path.setStart(start);
					
					profile.addPath(path);
					//System.out.println(ePath.asXML());
				}
				
				profiles.put(profile.getTag(), profile);
				//System.out.println(e.asXML());
			}
			
			//System.out.println(doc.asXML());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void loadUrls(String fileName) {
		File file = new File(fileName);
		CronFireQueue queue = CronFireQueue.getInstance();
		
		if(!file.exists())
			return;
		
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			
			while(br.ready()) {
				String line = br.readLine();

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

				if(!tokens.contains("defaults"))
					tokens.add(0, "defaults");
				
				while(tokens.size() > 0) {
					String tag = tokens.remove(0);
					
					if(0 == tag.length())
						continue;
					
					if(profiles.containsKey(tag)) {
						//System.out.println(tag + " is a valid profile...");
						
						EndpointProfile profile = profiles.get(tag);
						
						Iterator<Entry<String,EndpointPath>> i = profile.getPaths().entrySet().iterator();
						while(i.hasNext()) {
							Entry<String,EndpointPath> entry = i.next();
							EndpointPath path = entry.getValue();
							
							String suffix = path.getPath();
							if(suffix.startsWith("/"))
								suffix = suffix.substring(1);
								
							EndpointUrl endpoint;
							
							if(endpoints.containsKey(url + path.getKey())) {
								endpoint = endpoints.get(url + path.getKey());
								endpoint.setUrl(url + suffix);
							} else {
								endpoint = new EndpointUrl(url + suffix);
							}
							
							endpoint.setCooldownSecs(path.getDelay());
							
							// [TODO] Check 'start'
							endpoint.delayBySecs(path.getDelay());
							
							// [TODO] Max
							// ...
							
							//System.out.println("Added URL: " + url);
							
							endpoints.put(url + path.getKey(), endpoint);
							
							queue.add(endpoint);
						}
					}
					
					// [TODO] Look up the profile and apply it to this endpoint URL
				}
				
				//System.out.println("URL: " + url);
				//System.out.println("Tokens: " + tokens);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
