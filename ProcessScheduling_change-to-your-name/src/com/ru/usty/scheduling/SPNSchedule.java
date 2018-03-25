package com.ru.usty.scheduling;

import java.util.HashMap;
import java.util.Map;


public class SPNSchedule implements Comparable<SPNSchedule> {
	int processID;
	long totalTime;
	public Map<Integer, Long> timerAndID;
	
	public SPNSchedule() {
		timerAndID = new HashMap<Integer, Long>();
	}
	
	public SPNSchedule(int proccess, long time) {
		// TODO Auto-generated constructor stub
		this.processID = proccess;
		this.totalTime = time;	
	}
	
	public void addProcess(int id, long time) {
		timerAndID.put(id, time);
	}
	
	
	
	public long getTime() {
		return totalTime;
	}
	
	public int getProcessID() {
		return processID;
	}
	
	public long getTimeForId(int id) {
		return timerAndID.get(id);
	}
	@Override
	public boolean equals(Object o) {
		//return this.getTime() == spn.getTime();
		if(!(o instanceof SPNSchedule)) {
			return false;
		}
		SPNSchedule s = (SPNSchedule) o;
		if(this.totalTime == s.getTime()) {
			return true;
		}
		
		return false;
	}

	@Override
	public int compareTo(SPNSchedule spn) {
		
		if(this.equals(spn)) {
			return 0;
		}
		else if(getTime() > spn.getTime()) {
			
			return 1;
		}
		else {
			return -1;
		}
		
	}
}
