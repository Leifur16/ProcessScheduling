package com.ru.usty.scheduling;

import java.util.Queue;

import com.ru.usty.scheduling.process.ProcessExecution;

public class RoundRobinTimer implements Runnable{
	
	private int quantum;
	ProcessExecution processExecution;
	
	
	// Constructor
	public RoundRobinTimer(int quantum) {
		this.quantum = quantum;
	}
	
	@Override
	public void run() {
		while(true) {
			
			if(Scheduler.rrMayDie) {return;}
			try {
				Thread.sleep(quantum);
				//System.out.println("This thing is running ");
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Scheduler.nextQueue();
			
		}
	}
}


