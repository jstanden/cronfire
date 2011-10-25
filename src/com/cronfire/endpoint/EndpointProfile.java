package com.cronfire.endpoint;

import java.util.HashMap;

public class EndpointProfile {
	private String tag;
	private HashMap<String, EndpointPath> paths = new HashMap<String, EndpointPath>();
	
	public EndpointProfile(String tag) {
		this.tag = tag;
	}
	
	public void addPath(EndpointPath path) {
		paths.put(path.getKey(), path);
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public HashMap<String, EndpointPath> getPaths() {
		return paths;
	}
}
