package com.ru.usty.scheduling;

import java.util.HashMap;
import java.util.Map;


public class SRTSchedule implements Comparable<SRTSchedule> {
	private int processID;
	private long remainingTime;
	private Map<Integer, Long> remainingTimeAndID;
	
	public SRTSchedule() {
		remainingTimeAndID = new HashMap<Integer, Long>();
	}
	
	public SRTSchedule(int proccess, long remainingTime) {
		this.processID = proccess;
		this.remainingTime = remainingTime;	
	}
	
	public void addProcess(int id, long remainingTime) {
		remainingTimeAndID.put(id, remainingTime);
	}
	
	public void updateRemainingTime(long remainingTime) {
		this.remainingTime = remainingTime;
	}
	
	public long getRemainingTime() {
		return remainingTime;
	}
	
	public int getProcessID() {
		return processID;
	}
	
	public long getRemainingTimeForId(int id) {
		return remainingTimeAndID.get(id);
	}
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof SRTSchedule)) {
			return false;
		}
		SRTSchedule s = (SRTSchedule) o;
		if(this.remainingTime == s.getRemainingTime()) {
			return true;
		}
		
		return false;
	}

	@Override
	public int compareTo(SRTSchedule srt) {
		
		if(this.equals(srt)) {
			return 0;
		}
		else if(getRemainingTime() > srt.getRemainingTime()) {
			
			return 1;
		}
		else {
			return -1;
		}
		
	}
}
