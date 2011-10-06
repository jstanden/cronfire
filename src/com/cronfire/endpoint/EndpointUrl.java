package com.cronfire.endpoint;

import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import com.cronfire.CronFireSettings;

public class EndpointUrl implements Delayed {
	private long delayUntil;
	
	private String url;
	private String interval;
	
	private boolean isRunning = false;
	
	private long cumulativeHits = 0L;
	private long cumulativeRuntimeMs = 0L;
	
	private final Date dateTime;
	
	// [TODO] Can run parallel?
	// [TODO] Tags
	
	public EndpointUrl(String url) {
		this.url = url;
		this.dateTime = new Date();
	}
	
	public void setRunning(boolean b) {
		this.isRunning = b;
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public Integer getNextIntervalAsSecs() {
		Integer intervalDefault = CronFireSettings.getSettingInt("interval_default", 300);
		
		// If no interval is provided, use the default
		if(null == this.interval || 0 == this.interval.length()) {
			return intervalDefault;
		}
		
		// If it's an interval of seconds, return quickly
		if(this.interval.matches("^\\d+$")) {
			return Integer.valueOf(this.interval);
		}
		
		// ... Otherwise, we have a potential list of mixed intervals
		// and we want to keep the earliest timestamp
		String[] periods = this.interval.split(",");
		Integer intervalSecs = 0;
		
		for(String period : periods) {
			// Start can be in 00:00 format or a relative seconds
			if(-1 != period.indexOf(":")) {
				try {
					String[] data = period.split(":");
					
					if(2 != data.length)
						throw new Exception();
					
					MutableDateTime mdt = DateTime.now().toMutableDateTime();
					mdt.setHourOfDay(new Integer(data[0]));
					mdt.setMinuteOfHour(new Integer(data[1]));
					
					if(mdt.isBeforeNow())
						mdt.addDays(1);
					
					Integer compareIntervalSecs = Long.valueOf((mdt.getMillis() - DateTime.now().getMillis()) / 1000).intValue();
					intervalSecs = (0 == intervalSecs || compareIntervalSecs < intervalSecs) ? compareIntervalSecs : intervalSecs; 
					
				} catch(Exception e) {
					intervalSecs = 0;
				}
				
			} else {
				// Secs offset
				Integer compareIntervalSecs = Integer.valueOf(this.interval);
				intervalSecs = (0 == intervalSecs || compareIntervalSecs < intervalSecs) ? compareIntervalSecs : intervalSecs;
			}
			
		}
		
		if(intervalSecs > 0) {
			return intervalSecs;
		} else {
			return intervalDefault;
		}
	}	
	
	@Override
	public int compareTo(Delayed delayed) {
		EndpointUrl endpoint = (EndpointUrl)delayed;
	      if (this.delayUntil < endpoint.delayUntil)
	         return -1;
	      if (this.delayUntil > endpoint.delayUntil)
	         return 1;
	      return this.dateTime.compareTo(endpoint.dateTime);		
	}

	public void delayBySecs(int secs) {
		this.delayUntil = System.currentTimeMillis() + (secs * 1000L);
	}
	
	public void logRuntime(long ms) {
		cumulativeHits++;
		cumulativeRuntimeMs += ms;
	}
	
	public int getAverageRuntime() {
		if(0 == cumulativeHits)
			return 0;
		
		return Math.round(cumulativeRuntimeMs/cumulativeHits);
	}
	
	public long getRunCount() {
		return cumulativeHits;
	}
	
	@Override
	public long getDelay(TimeUnit timeUnit) {
		return timeUnit.convert(this.delayUntil - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}
	
}
