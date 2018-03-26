package com.ru.usty.scheduling;

public class FeedbackProcessInfo {
	private int ID;
	private int queueID;

	FeedbackProcessInfo(int ID, int queueID){
		this.ID = ID;
		this.queueID = queueID;
	}
	
	public int getID() {
		return ID;
	}
	
	public void setID(int ID) {
		this.ID = ID;
	}
	
	public int getQueueID() {
		return queueID;
	}
	
	public void setQueueID(int queueID) {
		this.queueID = queueID;
	}
	
	
	
}
