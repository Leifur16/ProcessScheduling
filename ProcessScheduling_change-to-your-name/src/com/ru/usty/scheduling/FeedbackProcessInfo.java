package com.ru.usty.scheduling;

public class FeedbackProcessInfo {
	
	int ID;
	int queueID;

	FeedbackProcessInfo(int ID, int queueID){
		this.ID = ID;
		this.queueID = queueID;
	}
	
	public int getID() {
		return ID;
	}
	
	public int getQueueID() {
		return queueID;
	}
	
}
