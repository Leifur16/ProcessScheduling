package com.ru.usty.scheduling;

public class RRSwitch implements Runnable{

	public static int sleepTime;	
	
	public void setSleepTime(int sleepTime) {
		sleepTime = this.sleepTime;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("switch");
	}

}
